package com.zhuanghd.user.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Author: zhuanghongdong Date: 2024/12/13 Description:
 */

@Data
public class UserBO implements Serializable {
	private Long id;
	@JsonProperty("nick_name")
	private String nickName;
	private Long icon;
	private String phone;
	@JsonProperty("update_time")
	private LocalDateTime updateTime;
	@JsonProperty("create_time")
	private LocalDateTime createTime;
	private Integer types;
	private Integer status;
}
