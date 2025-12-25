package com.zhuanghd.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName(value = "vd_place")
@Data
@Builder
public class PlaceDO implements Serializable {
    @TableId(value = "id")
    private Long id;
    @TableField(value = "user_id")
    @JsonProperty("user_id")
    private Long userId;
    @TableField(value = "name")
    private String name;
    @TableField(value = "address")
    private String address;
    @TableField(value = "pic")
    private Long pic;
    @TableField(value = "duty")
    private String duty;
    @TableField(value = "duty_tel")
    @JsonProperty("duty_tel")
    private String dutyTel;
    @TableField(value = "create_time")
    @JsonProperty("create_time")
    private LocalDateTime createTime;
    @TableField(value = "update_time")
    @JsonProperty("update_time")
    private LocalDateTime updateTime;
    @TableField(value = "status")
    private Integer status;
    @TableField(value = "delete_flag")
    @JsonProperty("delete_flag")
    private Integer deleteFlag;
    @TableField(value = "province")
    private String province;
    @TableField(value = "town")
    private String town;
    @TableField(value = "area")
    private String area;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
