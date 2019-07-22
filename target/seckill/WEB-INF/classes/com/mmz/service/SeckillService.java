package com.mmz.service;

import com.mmz.dto.Exposer;
import com.mmz.dto.SeckillExecution;
import com.mmz.entity.Seckill;
import com.mmz.exception.RepeatKillException;
import com.mmz.exception.SeckillCloseException;
import com.mmz.exception.SeckillException;

import java.util.List;

/**
 * 业务接口:站在使用者(程序员)的角度设计接口 三个方面:
 * 1.方法定义粒度，方法定义的要非常清楚
 * 2.参数，要越简练越好
 * 3.返回类型(return类型一定要友好/或者return异常，我们允许的异常)
 */

public interface SeckillService {

    /**
     * 查询全部的秒杀记录
     *
     * @return
     */
    public List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     *
     * @param seckillId
     * @return
     */
    public Seckill getById(long seckillId);


    // 再往下，是我们最重要的行为的一些接口

    /**
     * 在秒杀开启时输出秒杀接口的地址，否则输出系统时间和秒杀时间
     *
     * @param seckillId 秒杀商品Id
     * @return 根据对应的状态返回对应的状态实体
     * Exposer: 在dto包中创建Exposer.java，用于封装秒杀的地址信息
     */
    public Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作，有可能失败，有可能成功，所以要抛出我们允许的异常
     *
     * @param seckillId 秒杀的商品ID
     * @param userPhone 手机号码
     * @param md5 md5加密值
     * @return 根据不同的结果返回不同的实体信息
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException,
            RepeatKillException, SeckillCloseException;


}
