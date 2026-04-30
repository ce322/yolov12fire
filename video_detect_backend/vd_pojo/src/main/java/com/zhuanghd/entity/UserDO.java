package com.zhuanghd.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

@TableName(value = "vd_user")
@Data
@Builder
public class UserDO implements Serializable {

    @TableId(value = "id")
    private Long id;

    @TableField(value = "nick_name")
    private String nickName;

    @TableField(value = "password")
    private String password;

    @TableField(value = "icon")
    private Long icon;

    @TableField(value = "phone")
    private String phone;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(value = "types")
    private Integer types;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "email")
    private String email;

    @TableField(value = "gender")
    private Date gender;

    @TableField(value = "address")
    private String address;

    @TableField(value = "sex")
    private Integer sex;

    @TableField(value = "age")
    private Integer age;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}