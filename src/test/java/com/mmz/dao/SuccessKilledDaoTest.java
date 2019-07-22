package com.mmz.dao;

import com.mmz.entity.Seckill;
import com.mmz.entity.SuccessKilled;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

    @Resource
    private SuccessKilledDao successKilledDao;

    /**
     * 由于我们使用了联合主键，在insert时使用了ignore关键字，
     * 所以对于testInsertSuccessKilled()重复插入同一条数据是无效的，
     * 会被过滤掉，确保了一个用户不能重复秒杀同一件商品。
     *
     */
    @Test
    public void testInsertSuccessKilled() {
        long id = 1003L;
        long phone = 13812343599L;
        int insertCount = successKilledDao.insertSuccessKilled(id,phone);
        System.out.println(insertCount);
    }



    @Test
    public void testQueryByIdWithSeckill() {
        long id = 1003L;
        long phone = 13812343599L;
        SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(id , phone);
        System.out.println(successKilled);
        System.out.println(successKilled.getSeckill());
    }
}