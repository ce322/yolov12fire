package com.zhuanghd.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Author:zhuanghongdong
 * CreateTime:2024/10/17
 * Description:用户dto
 */
@Data
@ApiModel("线程的用户数据类型")
public class UserDTO {
    @ApiModelProperty("id")
    private Long id;
    @ApiModelProperty("用户名")
    private String nickName;
    @ApiModelProperty("头像")
    private Long icon;
}
