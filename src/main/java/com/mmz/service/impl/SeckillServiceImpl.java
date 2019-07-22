package com.mmz.service.impl;

import com.mmz.dao.SeckillDao;
import com.mmz.dao.SuccessKilledDao;
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
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

/**
 * @author : mengmuzi
 * create at:  2019-07-22  10:17
 * @description:  用来存放接口的实现类SeckillServiceImpl
 */
public class SeckillServiceImpl implements SeckillService {

    //日志对象(使用slf4j)
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private SeckillDao seckillDao;

    private SuccessKilledDao successKilledDao;

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

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill = seckillDao.queryById(seckillId);
        if(seckill == null){
            return new Exposer(false,seckillId);
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
    @Override
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException,
            RepeatKillException, SeckillCloseException {
        if(md5 == null || md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");//秒杀数据被重写了
        }

        //执行秒杀逻辑:减库存+增加购买明细
        Date nowTime = new Date();

        try{
            //减库存
            int updateCount = seckillDao.reduceNumber(seckillId,nowTime);
            if(updateCount <= 0){
                //没有更新库存记录，说明秒杀结束
                throw new SeckillCloseException("seckill is closed");
            }else {
                //否则更新了库存，秒杀成功,增加明细
                int insertCount = successKilledDao.insertSuccessKilled(seckillId,userPhone);
                //看是否该明细被重复插入，即用户是否重复秒杀
                if(insertCount <= 0){
                    throw new RepeatKillException("seckill repeated");
                }else{
                    //秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,successKilled);
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
