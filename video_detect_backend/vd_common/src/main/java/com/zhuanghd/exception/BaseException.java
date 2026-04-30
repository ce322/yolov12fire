package com.zhuanghd.exception;

/**
 * Author: zhuanghongdong
 * Create_time: 2024/10/15
 * Descripton: 基础异常处理父类
 */
public class BaseException extends RuntimeException {
    public BaseException() {}

    public BaseException(String msg) {
        super(msg);
    }

}
