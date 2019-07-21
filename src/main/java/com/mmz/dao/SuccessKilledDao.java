package com.mmz.dao;

import com.mmz.entity.SuccessKilled;

public interface SuccessKilledDao {

    /**
     * 插入购买的明细，可以过滤重复
     * @param seckillId
     * @param userphone
     * @return 插入的行数，如果返回值<1则表示插入失败
     */
    public int insertSuccessKilled(Long seckillId, Long userphone);

    /**
     * 根据id查询SuccessKilled并携带秒杀商品对象实体
     *
     * @param seckillId
     * @return
     */
    public SuccessKilled queryByIdWithSeckill(Long seckillId, Long userphone);
}
