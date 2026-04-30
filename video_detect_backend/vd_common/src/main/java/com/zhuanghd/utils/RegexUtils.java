package com.zhuanghd.utils;

import cn.hutool.core.util.StrUtil;
import com.zhuanghd.exception.BaseException;

import static com.zhuanghd.utils.RegexPatterns.*;

public class RegexUtils {

	/**
	 * 判断手机号格式是否无效
	 *
	 * @param phone
	 * @return true:不符合，false：符合
	 */
	public static boolean isPhoneInvalid(String phone) {
		return mismatch(phone, PHONE_REGEX);
	}

	/**
	 * 是否是无效邮箱格式
	 *
	 * @param email
	 * 		要校验的邮箱
	 * @return true:不符合，false：符合
	 */
	public static boolean isEmailInvalid(String email) {
		return mismatch(email, EMAIL_REGEX);
	}

	/**
	 * 是否是无效验证码格式
	 *
	 * @param code
	 * 		要校验的验证码
	 * @return true:不符合，false：符合
	 */
	public static boolean isCodeInvalid(String code) {
		return mismatch(code, VERIFY_CODE_REGEX);
	}

	/**
	 * 是否无效密码格式
	 *
	 * @param password
	 * 		要校验的密码
	 * @return true:不符合，false：符合
	 */
	public static boolean isPasswordInvalid(String password) {
		return mismatch(password, PASSWORD_REGEX);
	}

	/**
	 * 判断是否符合正则表达式
	 *
	 * @param str
	 * @param regex
	 * @return
	 */
	private static boolean mismatch(String str, String regex) {
		try{
			if (StrUtil.isBlank(str)) {
				return true;
			}
			return !str.matches(regex);
		}catch (Exception e){
			throw new BaseException("系统错误： "+e.getMessage());
		}
	}
}

