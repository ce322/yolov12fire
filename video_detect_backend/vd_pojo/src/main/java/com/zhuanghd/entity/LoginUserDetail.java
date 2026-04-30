package com.zhuanghd.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Author:zhuanghongdong Description:LoginUserDetail实现了UserDetails的方法，用以为UserDetailServiceImpl返回userDetail对象的封装
 */

@Data
public class LoginUserDetail implements UserDetails {

	private UserDO userDO;

	public LoginUserDetail(UserDO userDO) {
		this.userDO = userDO;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public String getPassword() {
		return userDO.getPassword();
	}

	@Override
	public String getUsername() {
		return userDO.getPhone();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		if (userDO.getStatus() == 1) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
