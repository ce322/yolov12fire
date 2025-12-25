package com.zhuanghd.authentication;

import com.zhuanghd.exception.BaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static com.zhuanghd.constant.RedisConstant.LOGIN_CAPTCHA_KEY;

/**
 * Author: zhuanghongdong Date: 2024/12/12 Description:
 */

public class MobileCaptchaAuthenticationProvider implements AuthenticationProvider {
	private UserDetailsService userDetailsService;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		MobileCaptchaAuthenticationToken mobileCaptchaAuthenticationToken = (MobileCaptchaAuthenticationToken)authentication;
		String phone = mobileCaptchaAuthenticationToken.getPhone();
		String captcha = mobileCaptchaAuthenticationToken.getCaptcha();

		String trueCaptcha = stringRedisTemplate.opsForValue().get(LOGIN_CAPTCHA_KEY + phone);

		if (!captcha.equals(trueCaptcha)) {
			throw new BaseException("验证码错误");
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(phone);
		if (userDetails == null) {
			throw new UsernameNotFoundException("用户不存在");
		} else {
			MobileCaptchaAuthenticationToken result = new MobileCaptchaAuthenticationToken(userDetails, null,
					userDetails.getAuthorities());
			return result;
		}

	}

	@Override
	public boolean supports(Class<?> aClass) {
		return MobileCaptchaAuthenticationToken.class.isAssignableFrom(aClass);
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}
}
