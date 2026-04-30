package com.zhuanghd.utils;

import com.zhuanghd.exception.BaseException;
import io.jsonwebtoken.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static com.zhuanghd.constant.JwtClaimsConstant.USER_ID;
import static com.zhuanghd.constant.MessageConstant.TOKEN_ERROR;

/**
 * Author:zhuanghongdong Create:2024/10/22 Description:jwt工具类
 */

@Component
@Data
@ConfigurationProperties(prefix = "vd.jwt")
public class JwtUtils {

	/**
	 * 普通用户jwt令牌相关配置
	 */
	private static String UserSecretKey;
	private static long UserTtl;
	private static String UserTokenName;

	/**
	 * 用户生成Jwt
	 *
	 * @param claims
	 * @return
	 */
	public static String sign(Map<String, Object> claims) {
		try {
			// 指定签名算法
			SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
			// Jwt的过期时间
			long expMillis = System.currentTimeMillis() + UserTtl;
			Date exp = new Date(expMillis);
			// 设置Jwt的body
			JwtBuilder builder = Jwts.builder()
					.setClaims(claims)
					.signWith(signatureAlgorithm, UserSecretKey.getBytes(StandardCharsets.UTF_8))
					.setExpiration(exp);
			return builder.compact();
		} catch (Exception e) {
			throw new BaseException(e.getMessage());
		}
	}

	/**
	 * 用户Token解密
	 *
	 * @param token
	 * @return
	 */
	public static Claims parseJwt(String token) {
		try {
			return Jwts.parser()
					// 设置签名密钥
					.setSigningKey(UserSecretKey.getBytes(StandardCharsets.UTF_8))
					// 设置需要解析的Jwt
					.parseClaimsJws(token).getBody();
		} catch (SignatureException | ExpiredJwtException e) {
			throw new BaseException(TOKEN_ERROR);
		}

	}

	/**
	 * 提取token中的id信息
	 *
	 * @param token
	 * @return
	 */
	public Long extractUserId(String token) {
		try {
			Claims claims = parseJwt(token);
			return claims.get(USER_ID, Long.class);
		} catch (Exception e) {
			throw new BaseException("提取token中的id信息异常");
		}
	}

	/**
	 * 判断token是否有效
	 *
	 * @param token
	 * @param userId
	 * @return
	 */
	public boolean validateToken(String token, Long userId) {
		try {
			Long id = extractUserId(token);
			return (id.equals(userId) && !isTokenExpired(token));
		} catch (Exception e) {
			throw new BaseException("验证token异常");
		}
	}

	/**
	 * 判断token是否过期
	 *
	 * @param token
	 * @return
	 */
	public boolean isTokenExpired(String token) {
		try {
			Claims claims = parseJwt(token);
			Date expiration = claims.getExpiration();
			return expiration.before(new Date());
		} catch (Exception e) {
			throw new BaseException("token判断是否过期异常");
		}
	}

	public void setUserSecretKey(String userSecretKey) {
		UserSecretKey = userSecretKey;
	}

	public void setUserTtl(long userTtl) {
		UserTtl = userTtl;
	}

	public void setUserTokenName(String userTokenName) {
		UserTokenName = userTokenName;
	}
}
