package com.zhuanghd.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @TableName vd_check
 */
@TableName(value ="vd_check")
@Data
public class CheckDO {
    // TODO 这里前后端都要给位duty_tel的形式
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    private String duty;

    @JsonProperty("duty_tel")
    private String dutyTel;

    private String place;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("update_time")
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    @JsonProperty("d_time")
    private Integer dTime;

    private String situation;

    private Integer score;

    @JsonProperty("delete_flag")
    private Integer deleteFlag;
}