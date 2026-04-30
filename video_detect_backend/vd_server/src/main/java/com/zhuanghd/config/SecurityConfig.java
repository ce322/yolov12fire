package com.zhuanghd.config;

import com.zhuanghd.authentication.MobileCaptchaAuthenticationProvider;
import com.zhuanghd.hander.AuthenticationEntryPointImpl;
import com.zhuanghd.interceptor.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author:zhuanghongdong Create_time:2024/10/24 Description:springSecurity配置类
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
	@Autowired
	private AuthenticationEntryPointImpl authenticationEntryPoint;
	@Autowired
	private UserDetailsService userDetailsService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public MobileCaptchaAuthenticationProvider mobileCaptchaAuthenticationProvider() {
		MobileCaptchaAuthenticationProvider mobileCaptchaAuthenticationProvider = new MobileCaptchaAuthenticationProvider();
		mobileCaptchaAuthenticationProvider.setUserDetailsService(userDetailsService);
		return mobileCaptchaAuthenticationProvider;
	}

	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		daoAuthenticationProvider.setUserDetailsService(userDetailsService);
		return daoAuthenticationProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		ArrayList<AuthenticationProvider> authenticationProviders = new ArrayList<>();
		authenticationProviders.add(mobileCaptchaAuthenticationProvider());
		authenticationProviders.add(daoAuthenticationProvider());
		ProviderManager authenticationManager = new ProviderManager(authenticationProviders);
		return authenticationManager;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource())) // 启用 CORS
				// 关闭csrf防护
				.csrf().disable()
				// 关闭session
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// 请求权限
				.authorizeRequests()
				// 白名单
				.antMatchers("/user/*").permitAll()
				.antMatchers("/upload/**").permitAll()
				.antMatchers("/videos/**").permitAll()
				.antMatchers("/thumbnails/**").permitAll()
				.antMatchers("/doc.html").permitAll()
				// 黑名单
				.anyRequest().authenticated()   //其他所有请求都需要认证
				.and()
				.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
				.and()
				.addFilterAfter(jwtAuthenticationTokenFilter, LogoutFilter.class)
		;
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080")); // 允许的前端域名
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE")); // 允许的 HTTP 方法
		configuration.setAllowedHeaders(Arrays.asList("*")); // 允许的请求头
		configuration.setAllowCredentials(true); // 允许携带凭证
		configuration.setMaxAge(3600L); // 预检请求的缓存时间（秒）

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration); // 应用到所有路径
		return source;
	}

}
