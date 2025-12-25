package com.zhuanghd.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Author:zhuanghongdong Create:2024/10/22 Description:密码工具类
 */

@Component
public class PasswordUtil {
	/**
	 * 使用BCrypt算法加密密码，默认加密强度为10
	 *
	 * @param password
	 * @return
	 */
	public static String encryptPassword(String password) {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		return bCryptPasswordEncoder.encode(password);
	}

	/**
	 * 验证密码是否有效
	 *
	 * @param rawPassword
	 * @param encodePassword
	 * @return
	 */
	public static boolean validatePassword(String rawPassword, String encodePassword) {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		return bCryptPasswordEncoder.matches(rawPassword, encodePassword);
	}

}
