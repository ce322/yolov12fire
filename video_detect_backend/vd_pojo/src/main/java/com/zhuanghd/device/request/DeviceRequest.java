package com.zhuanghd.device.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class DeviceRequest {
    @NotNull(message = "添加地址不能为空")
    @JsonProperty("place_id")
    private Long placeId;

    @NotBlank(message = "设备名字不能为空")
    private String name;

    private Long pic;

    @NotBlank(message = "设备来源不能为空")
    private String res;

    @NotBlank(message = "设备责任人不能为空")
    private String duty;

    @JsonProperty("duty_tel")
    @NotBlank(message = "责任人电话不能为空")
    private String dutyTel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JsonProperty("check_time")
    private LocalDateTime checkTime;

    @NotBlank(message = "状态不能为空", groups = {UpdateDevice.class})
    private Integer status;

    private interface UpdateDevice {
    }
}
