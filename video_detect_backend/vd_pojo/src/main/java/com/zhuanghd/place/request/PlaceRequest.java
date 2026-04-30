package com.zhuanghd.place.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PlaceRequest {
	@NotBlank(message = "名称不能为空")
	private String name;

	@NotBlank(message = "详细地址不能为空")
	private String address;

	@NotBlank(message = "照片序号不能为空")
	private Long pic;

	@NotBlank(message = "责任人不能为空")
	private String duty;

	@NotBlank(message = "责任人联系方式不能为空")
	@JsonProperty("duty_tel")
	private String dutyTel;

	@NotBlank(message = "省份不能为空")
	private String province;

	@NotBlank(message = "市不能为空")
	private String town;

	@NotBlank(message = "区不能为空")
	private String area;

	@NotBlank(message = "状态不能为空",groups = updatePlace.class)
	private Integer status;

	private interface updatePlace{

	}
}
