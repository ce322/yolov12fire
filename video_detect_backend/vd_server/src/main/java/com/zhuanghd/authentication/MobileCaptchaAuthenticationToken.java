package com.zhuanghd.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Author: zhuanghongdong Date: 2024/12/12 Description:
 */

public class MobileCaptchaAuthenticationToken extends AbstractAuthenticationToken {
	private static final long serialVersionUID = 570L;
	private Object credentials;
	private Object principal;
	private String phone;
	private String captcha;

	public MobileCaptchaAuthenticationToken(Object principal, Object credentials,
			Collection<? extends GrantedAuthority> authorities
	) {
		super(authorities);
		this.credentials = credentials;
		this.principal = principal;
		super.setAuthenticated(true);
	}

	public MobileCaptchaAuthenticationToken(String phone, String captcha) {
		super(null);
		this.phone = phone;
		this.captcha = captcha;
		super.setAuthenticated(false);
	}

	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		if (isAuthenticated) {
			throw new IllegalArgumentException(
					"Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
		} else {
			super.setAuthenticated(false);
		}
	}

	public void eraseCredentials() {
		super.eraseCredentials();
		this.credentials = null;
	}

	public Object getCredentials() {
		return this.credentials;
	}

	public Object getPrincipal() {
		return this.principal;
	}

	public String getPhone() {
		return phone;
	}

	public String getCaptcha() {
		return captcha;
	}
}
