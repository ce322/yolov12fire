package com.zhuanghd.hander;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.zhuanghd.result.Result;
import com.zhuanghd.utils.WebUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.zhuanghd.constant.MessageConstant.LOGIN_FAIL;

/**
 * Author:zhuanghongdong Create_time:2024/10/24 Description：身份认证失败处理
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		Result<Object> result = Result.error(LOGIN_FAIL);
		response.setContentType("text/json;charset=utf-8");
		response.getWriter().write(new Gson().toJson(result));
	}
}
