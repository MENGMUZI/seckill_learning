package com.mmz.dao;

import com.mmz.entity.Seckill;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SeckillDao {
    /**
     * 减库存
     * @param seckillId
     * @param killTime
     * @return 更新的记录行数，如果返回值<1则表示更新失败
     */
    public int reduceNumber(@Param("seckillId") Long seckillId, @Param("killTime") Date killTime);

    /**
     * 根据id查询秒杀商品
     *
     * @param seckillId
     * @return
     */
    public Seckill queryById(long seckillId);

    /**
     * 根据偏移量查询秒杀商品列表
     *
     * @param offset
     * @param limit
     * @return
     */
    public List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);



}
