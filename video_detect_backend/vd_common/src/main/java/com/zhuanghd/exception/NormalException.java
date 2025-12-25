package com.zhuanghd.exception;

/**
 * Author: zhuanghongdong
 * Create_time: 2024/10/15
 * Descripton: 账号不存在异常
 */

public class NormalException extends BaseException {
    public NormalException() {}

    public NormalException(String msg) {
        super(msg);
    }
}
