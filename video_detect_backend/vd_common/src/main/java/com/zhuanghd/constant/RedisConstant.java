package com.zhuanghd.constant;

import java.util.concurrent.TimeUnit;

/**
 * Author: zhuanghongdong Create_time: 2024/10/21 Description: redis常量类
 */

public class RedisConstant {
	public static final String LOGIN_CAPTCHA_KEY = "login:captcha:";
	public static final Long LOGIN_CAPTCHA_TTL = 1L;    // 10分钟
	public static final TimeUnit LOGIN_CAPTCHA_TIMEUNIT = TimeUnit.MINUTES;
	public static final String LOGIN_TOKEN_KEY = "login:token:";
	public static final Long LOGIN_TOKEN_TTL = 86400000L;    // 1天
	public static final TimeUnit LOGIN_TOKEN_TIMEUNIT = TimeUnit.MILLISECONDS;
}
