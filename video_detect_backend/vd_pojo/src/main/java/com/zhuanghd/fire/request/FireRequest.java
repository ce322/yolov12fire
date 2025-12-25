package com.zhuanghd.fire.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 火灾记录请求
 */
@Data
public class FireRequest {
    
    private String pic;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    
    @NotNull(message = "地点ID不能为空")
    private Long placeId;
    
    private String reason;
    
    private String situation;
    
    private Double prob;
    
    private Integer fireFlag;
    
    private Double smokeProb;
    
    private Integer smokeFlag;
    
    private Long userId;
} 