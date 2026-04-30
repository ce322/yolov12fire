package com.zhuanghd.user.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Author: zhuanghongdong Date: 2024/12/10 Description:
 */

@Data
public class UserRegister {
	@NotBlank(message = "手机号不能为空")
	private String phone;
	@NotBlank(message = "密码不能为空")
	private String password;
	@NotBlank(message = "确认密码不能为空")
	private String secondPassword;
	@NotBlank(message = "验证码不能为空")
	private String captcha;
}
