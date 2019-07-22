package com.mmz.exception;

/**
 * @author : mengmuzi
 * create at:  2019-07-22  10:02
 * @description: 秒杀相关的所有业务异常
 */
public class SeckillException extends RuntimeException {

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
