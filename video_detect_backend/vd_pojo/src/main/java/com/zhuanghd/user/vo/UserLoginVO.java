package com.zhuanghd.user.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserLoginVO {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("用户名")
    private String nickName;

    @ApiModelProperty("JWT-TOKEN")
    private String token;

    @ApiModelProperty("头像")
    private Long icon;
}
