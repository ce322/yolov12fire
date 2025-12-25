package com.zhuanghd.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * @TableName vd_video
 */
@TableName(value = "vd_video")
@Data
@Builder
public class VideoDO implements Serializable {
	@TableId(value = "id")
	private Long id;
	@TableField(value = "user_id")
	private Long userId;
	@TableField(value = "video_url")
	private String videoUrl;
	@TableField(value = "upload_time")
	private LocalDateTime uploadTime;
	@TableField(value = "status")
	private Integer status;
	@TableField(value = "title")
	private String title;
	@TableField(value = "thumbnail")
	private String thumbnail;
	@TableField(value = "extension")
	private String extension;
	@TableField(value = "fire_probability")
	private Double fireProbability;
	@TableField(value = "smoke_probability")
	private Double smokeProbability;
	@TableField(value = "process_time")
	private LocalDateTime processTime;
	@TableField(value = "place_id")
	private Long placeId;
	@TableField(value = "d_time")
	private Integer dTime;
	@TableField(value = "processed_url")
	private String processedUrl;

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
}