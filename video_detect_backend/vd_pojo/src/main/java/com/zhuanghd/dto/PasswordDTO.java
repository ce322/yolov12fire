package com.zhuanghd.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Author:zhuanghongdong
 * CreateTime:2024/10/21
 * Description:密码DTO
 */

@Data
@ApiModel("用户修改密码的数据类型")
public class PasswordDTO {
    @ApiModelProperty("旧密码")
    private String oldPassword;
    @ApiModelProperty("新密码")
    private String newPassword;
    @ApiModelProperty("确认新密码")
    private String confirmPassword;
}
