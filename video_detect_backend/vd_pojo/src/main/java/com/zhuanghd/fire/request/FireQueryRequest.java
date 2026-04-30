package com.zhuanghd.fire.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 火灾记录查询请求
 */
@Data
public class FireQueryRequest {
    
    /**
     * 时间排序方式：asc升序，desc降序
     */
    private String timeOrder;
    
    /**
     * 开始时间范围
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTimeBegin;
    
    /**
     * 结束时间范围
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTimeEnd;
    
    /**
     * 是否着火
     */
    private Integer fireFlag;
    
    /**
     * 地点ID
     */
    private Long placeId;

    /**
     * 是否有烟雾
     */
    private Integer smokeFlag;
    
    /**
     * 用户ID
     */
    private Long userId;
} 