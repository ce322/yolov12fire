package com.zhuanghd.utils;

/**
 * Author: zhuanghongdong
 * Create_time: 2024/10/14
 * Description: 正则表达式匹配
 */
public abstract class RegexPatterns {
    /**
     * 手机号正则表达式
     */
    public static final String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";

    /**
     * 邮箱正则表达式
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /**
     * 密码正则表达式：
     */
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-zA-Z])[A-Za-z0-9]{8,32}$";

    /**
     * 验证码正则表达式：6位的数字、字母
     */
    public static final String VERIFY_CODE_REGEX = "^[a-zA-Z\\d]{6}$";


}
