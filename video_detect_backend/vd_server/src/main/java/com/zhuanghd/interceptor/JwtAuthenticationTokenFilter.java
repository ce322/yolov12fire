package com.zhuanghd.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.zhuanghd.constant.RedisConstant;
import com.zhuanghd.entity.LoginUserDetail;
import com.zhuanghd.entity.UserDO;
import com.zhuanghd.user.bo.UserBO;
import com.zhuanghd.utils.JwtUtils;
import com.zhuanghd.user.vo.UserLoginVO;
import com.zhuanghd.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 在请求头获得token
        String token = request.getHeader("token");
        // 判断token是否为空 判断token是否过期 直接放行
        if (StrUtil.isBlank(token) || jwtUtils.isTokenExpired(token)) {
            // 此时的SecurityContextHolder没有用户信息 会被后面的过滤器拦截
            filterChain.doFilter(request, response);
            return;
        }

        // 解析token当中的userid
        Long userId = jwtUtils.extractUserId(token);
        // 判断token是否有效
        if (!jwtUtils.validateToken(token, userId)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从redis中获得用户信息
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(RedisConstant.LOGIN_TOKEN_KEY + token);
        UserBO userBO = BeanUtil.fillBeanWithMap(entries, new UserBO(), CopyOptions.create().setIgnoreNullValue(true));
        UserHolder.saveUser(userBO);
        // 生成UserDetail对象 并将对象存入securityContextHolder当中
        UserDO userDO = BeanUtil.copyProperties(userBO, UserDO.class);
        LoginUserDetail loginUserDetail = new LoginUserDetail(userDO);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUserDetail, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 放行
        filterChain.doFilter(request, response);
    }
}
