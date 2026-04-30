package com.zhuanghd.hander;

import com.alibaba.fastjson.JSON;
import com.zhuanghd.result.Result;
import com.zhuanghd.utils.WebUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.zhuanghd.constant.MessageConstant.AUTHORITIES_INS;


/**
 * Author:zhuanghongdong
 * Create_time:2024/10/24
 * Description：权限不足错误处理
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Result<Object> result = Result.error(AUTHORITIES_INS);
        String jsonString = JSON.toJSONString(result);
        WebUtils.renderString(response, jsonString);
    }
}
