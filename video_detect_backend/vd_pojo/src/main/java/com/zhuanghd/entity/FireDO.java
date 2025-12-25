package com.zhuanghd.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.Builder;

/**
 * @TableName vd_fire
 */
@TableName(value ="vd_fire")
@Data
@Builder
public class FireDO implements Serializable {
    @TableId(value = "id")
    private Long id;

    @TableField(value = "pic")
    private Long pic;

    @TableField(value = "start_time")
    private Date startTime;

    @TableField(value = "end_time")
    private Date endTime;

    @TableField(value = "place_id")
    private Long placeId;

    @TableField(value = "reason")
    private String reason;

    @TableField(value = "situation")
    private String situation;

    @TableField(value = "prob")
    private Double prob;

    @TableField(value = "fire_flag")
    private Integer fireFlag;

    @TableField(value = "smoke_prob")
    private Double smokeProb;

    @TableField(value = "smoke_flag")
    private Integer smokeFlag;
    
    @TableField(value = "video_id")
    private Long videoId;
    
    @TableField(value = "user_id")
    private Long userId;
    
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}