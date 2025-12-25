package com.zhuanghd.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @TableName vd_device
 */
@TableName(value ="vd_device")
@Data
public class DeviceDO {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("place_id")
    private Long placeId;

    private String name;

    private Long pic;

    private String res;

    private String duty;

    @JsonProperty("duty_tel")
    private String dutyTel;

    @JsonProperty("create_time")
    private LocalDateTime createTime;

    @JsonProperty("update_time")
    private LocalDateTime updateTime;

    @JsonProperty("check_time")
    private LocalDateTime checkTime;

    private Integer status;

    private Integer deleteFlag;
}