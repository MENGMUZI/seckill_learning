package com.mmz.service.impl;

import com.mmz.dao.SeckillDao;
import com.mmz.dao.SuccessKilledDao;
import com.mmz.dao.cache.RedisDao;
import com.mmz.dto.Exposer;
import com.mmz.dto.SeckillExecution;
import com.mmz.entity.Seckill;
import com.mmz.entity.SuccessKilled;
import com.mmz.enums.SeckillStatEnum;
import com.mmz.exception.RepeatKillException;
import com.mmz.exception.SeckillCloseException;
import com.mmz.exception.SeckillException;
import com.mmz.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

/**
 * @author : mengmuzi
 * create at:  2019-07-22  10:17
 * @description:  用来存放接口的实现类SeckillServiceImpl
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    //日志对象(使用slf4j)
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired //另外，@Resource，@Inject 是J2EE规范的一些注解
    private SeckillDao seckillDao;
    @Autowired
    private SuccessKilledDao successKilledDao;
    @Autowired
    private RedisDao redisDao;

    //加入一个混淆字符串(秒杀接口)的salt，为了我避免用户猜出我们的md5值，值任意给，越复杂越好
    private final String salt ="2342432KBKHIK5#$@#^%&*(&*()()*&^%$*";


    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    /**
     * 1.优化秒杀暴露接口 ==》 使用Redis优化地址暴露接口
     *  原本查询秒杀商品时是通过主键直接去数据库查询的，选择将数据缓存在Redis，在查询秒杀商品时先去Redis缓存中查询，
     *  以此降低数据库的压力。如果在缓存中查询不到数据再去数据库中查询，再将查询到的数据放入Redis缓存中，
     *  这样下次就可以直接去缓存中直接查询到。
     *
     *  以上属于数据访问层的逻辑（DAO层），所以我们需要在dao包下新建一个cache目录，在该目录下新建RedisDao.java，用来存取缓存。
     */
    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        // 优化点:缓存优化:超时的基础上维护一致性
        // 1.访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(seckill == null){
            //2.缓存中没有，访问数据库
            seckill = seckillDao.queryById(seckillId);
            if(seckill == null){
                return new Exposer(false,seckillId);
            }else{
                //3.如果数据库中存在，则放入Redis缓存中
                redisDao.putSeckill(seckill);
            }
        }
        //若是秒杀未开启
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();

        //系统当前时间
        Date nowTime = new Date();
        if(nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()){
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        //秒杀开启，返回秒杀商品的id、用给接口加密的md5,md5加密不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }
    //生成MD5码
    private String getMD5(long seckillId){
        String base = seckillId + "/" +salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    ///秒杀是否成功，成功:减库存，增加明细；失败:抛出异常，事务回滚
    /**
     * 使用注解控制事务方法的优点：
     *  1.开发团队达成一致约定，明确标注事务方法的编程风格
     *  2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部（保证事务方法里面是很干净的/效率的）
     *  3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制（MYSQL 表级锁、行级锁）
     */
    @Transactional
    @Override
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException,
            RepeatKillException, SeckillCloseException {
        if(md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");//秒杀数据被重写了
        }

        //执行秒杀逻辑:减库存+增加购买明细
        Date nowTime = new Date();

        /**
         * 2.秒杀操作的并发优化
         * 用户的秒杀操作分为两步：减库存、插入购买明细，我们在这里进行简单的优化，
         * 就是将原本先update（减库存）再进行insert（插入购买明细）的步骤改成：先insert再update。
         *
         * 首先是在更新操作的时候给行加锁，插入并不会加锁，如果更新操作在前，那么就需要执行完更新和插入以后事务提交或回滚才释放锁。
         * 而如果插入在前，更新在后，那么只有在更新时才会加行锁，之后在更新完以后事务提交或回滚释放锁。
         *
         * 在这里，插入是可以并行的，而更新由于会加行级锁是串行的。
         *
         * 也就是说是更新在前加锁和释放锁之间两次的网络延迟和GC，如果插入在前则加锁和释放锁之间只有一次的网络延迟和GC，也就是减少的持有锁的时间。
         *
         * 这里先insert并不是忽略了库存不足的情况，而是因为insert和update是在同一个事务里，光是insert并不一定会提交，只有在update成功才会提交，所以并不会造成过量插入秒杀成功记录。
         */
        try{
            // 否则更新了库存，秒杀成功,增加明细
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            // 看是否该明细被重复插入，即用户是否重复秒杀
            if (insertCount <= 0) {
                throw new RepeatKillException("seckill repeated");
            } else {

                // 减库存,热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    // 没有更新库存记录，说明秒杀结束 rollback
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    // 秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息 commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }

        }catch (SeckillCloseException e1){
            throw e1;

        }catch (RepeatKillException e2){
            throw e2;

        } catch (Exception e){
            logger.error(e.getMessage(),e);
            //将编译时异常转化为运行时异常
            throw new SeckillException("seckill inner error:" + e.getStackTrace());
            /**
             * 注意：
             * 我们捕获了运行时异常，原因是Spring的事务默认是发生了RuntimeException才会回滚，
             * 发生了其他异常不会回滚，所以在最后的catch块里通过
             * throw new SeckillException("seckill inner error :"+e.getMessage());
             * 将编译期异常转化为运行期异常。
             *
             */
        }

    }


}
