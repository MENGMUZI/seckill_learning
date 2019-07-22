package com.mmz.entity;

import java.util.Date;

/**
 * @author : mengmuzi
 * create at:  2019-07-21  17:16
 * @description:
 */
public class SuccessKilled {

    private Long seckillId;

    private Long userPhone;

    private Short state;

    private Date createTime;

    // 多对一,因为一件商品在库存中有很多数量，对应的购买明细也有很多。
    private Seckill seckill;

    public SuccessKilled() {
    }

    public SuccessKilled(Long seckillId, Long userPhone, Short state, Date createTime, Seckill seckill) {
        this.seckillId = seckillId;
        this.userPhone = userPhone;
        this.state = state;
        this.createTime = createTime;
        this.seckill = seckill;
    }

    public Long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(Long seckillId) {
        this.seckillId = seckillId;
    }

    public Long getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(Long userPhone) {
        this.userPhone = userPhone;
    }

    public Short getState() {
        return state;
    }

    public void setState(Short state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Seckill getSeckill() {
        return seckill;
    }

    public void setSeckill(Seckill seckill) {
        this.seckill = seckill;
    }

    @Override
    public String toString() {
        return "SuccessKilled{" +
                "seckillId=" + seckillId +
                ", userPhone=" + userPhone +
                ", state=" + state +
                ", createTime=" + createTime +
                '}';
    }
}
