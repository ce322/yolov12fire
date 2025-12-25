package com.zhuanghd.user.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Author:zhuanghongdong CreateTime:2024/10/17 Description:用户登录请求对象
 */

@Data
public class UserLogin {
	@NotBlank(message = "手机号不能为空")
	private String phone;
	@NotBlank(message = "密码不能为空", groups = loginByPassword.class)
	private String password;
	@NotBlank(message = "验证码不能为空", groups = loginByCaptcha.class)
	private String captcha;

	private interface loginByCaptcha {
	}

	private interface loginByPassword {
	}

}
