package com.zhuanghd.check.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class CheckRequest {
    @NotBlank(message = "负责人不能为空")
    private String duty;

    @NotBlank(message = "责任人联系方式不能为空")
    @JsonProperty("duty_tel")
    private String dutyTel;

    @NotBlank(message = "地点不能为空")
    private String place;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @NotBlank(message = "时间不能为空")
    private LocalDateTime time;

    @NotBlank(message = "持续时间不能为空")
    @JsonProperty("d_time")
    private Integer dTime;

    private String situation;

    private Integer score;
}
