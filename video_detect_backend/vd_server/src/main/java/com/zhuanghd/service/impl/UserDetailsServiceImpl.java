package com.zhuanghd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuanghd.entity.LoginUserDetail;
import com.zhuanghd.entity.UserDO;
import com.zhuanghd.exception.BaseException;
import com.zhuanghd.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.zhuanghd.constant.MessageConstant.ACCOUNT_PWD_ERROR;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	private UserMapper userMapper;

	@Override
	public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
		// 根据用户名查询用户信息 这里的用户名为手机号
		LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();
		UserDO userDO = userMapper.selectOne(queryWrapper.eq(UserDO::getPhone, phone));

		// 如果没有用户 抛出异常
		if (userDO == null) {
			throw new BaseException(ACCOUNT_PWD_ERROR);
		}

		// TODO 查询权限信息，封装到UserDetail对象类中

		// 将用户信息封装到UserDetail对象类中返回
		return new LoginUserDetail(userDO);
	}
}
