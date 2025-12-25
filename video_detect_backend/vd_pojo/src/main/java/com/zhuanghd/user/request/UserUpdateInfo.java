package com.zhuanghd.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@ApiModel("用户信息更新请求")
public class UserUpdateInfo implements Serializable {

    @ApiModelProperty("用户昵称")
    private String nickName;

    @ApiModelProperty("用户头像ID")
    private Long icon;

    @ApiModelProperty("邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @ApiModelProperty("地址")
    private String address;

    @ApiModelProperty("性别：0-女，1-男，2-未知")
    @Min(value = 0, message = "性别参数错误")
    @Max(value = 2, message = "性别参数错误")
    private Integer sex;

    @ApiModelProperty("年龄")
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 120, message = "年龄不能超过120")
    private Integer age;
} 