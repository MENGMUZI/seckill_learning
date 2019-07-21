package com.mmz.dao;

import com.mmz.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 配置Spring和Junit整合,junit启动时加载springIOC容器 spring-test,junit
 */
@RunWith(SpringJUnit4ClassRunner.class)
// 告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    // 注入Dao实现类依赖
    @Resource
    private SeckillDao seckillDao;

    @Test
    public void testQueryById() {
        long id = 1000;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
        /**
         * 1000元秒杀iphone6
         * Seckill{seckillId=1000, name='1000元秒杀iphone6', number=100, createTime=Mon
         * Jul 22 05:44:38 CST 2019, startTime=Mon Jul 01 13:00:00 CST 2019, endTime=Tue
         * Jul 02 13:00:00 CST 2019}
         */
    }

    /**
     * 如果之前没有在DAO接口的多参数方法里在形参前加上@Param注解，
     * 那么在这里进行单元测试时，MyBatis会报绑定参数失败的错误，因为无法找到参数。
     * 这是因为Java没有保存形参的记录，
     * Java在运行的时候会把queryAll(int offset,int limit)中的参数变成这样queryAll(int arg0,int arg1)，
     * 导致MyBatis无法识别这两个参数。
     */
    @Test
    public void testQueryAll() {
        List<Seckill> seckills = seckillDao.queryAll(0,100);
        for (Seckill seckill : seckills) {
            System.out.println(seckill);
        }
    }

    @Test
    public void testReduceNumber() {
        Date date = new Date();
        int updateCount = seckillDao.reduceNumber(1000L,date);
        System.out.println(updateCount);

    }
}