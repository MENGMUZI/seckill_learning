package com.mmz.enums;
/**
 * 在代码里还存在着硬编码的情况，比如秒杀结果返回的state和stateInfo参数信息是输出给前端的，
 * 这些字符串应该考虑用常量枚举类封装起来，方便重复利用，也易于维护。
 */

public enum SeckillStatEnum {

    SUCCESS(1,"恭喜你！秒杀成功"),
    END(0,"很遗憾！晚来一步，秒杀结束了"),
    REPEAT_KILL(-1,"小主，重复秒杀了"),
    INNER_ERROR(-2,"小主对不起，出现内部系统异常"),
    DATE_REWRITE(-3,"可恶，数据被篡改了");

    private int state;
    private String info;

    SeckillStatEnum(int state, String info){
        this.state = state;
        this.info = info;
    }

    public int getState() {
        return state;
    }

    public String getInfo() {
        return info;
    }

    //迭代所有的情况
    public static SeckillStatEnum stateOf(int index){
        for(SeckillStatEnum element : values()){
            if(element.getState() == index){
                return element;
            }
        }
        return null;
    }
}
