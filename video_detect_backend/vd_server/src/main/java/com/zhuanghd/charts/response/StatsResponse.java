package com.zhuanghd.charts.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 统计数据响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {
    // 统计指标
    private String title;
    
    // 分类数据 - 例如：[{"name": "类别1", "value": 10}, {"name": "类别2", "value": 20}]
    private List<Map<String, Object>> categoryData;
    
    // 时间序列数据 - 例如: [{"date": "2023-01", "count": 10}, {"date": "2023-02", "count": 20}]
    private List<Map<String, Object>> timeSeriesData;
    
    // 其他统计数据
    private Map<String, Object> extraData;
} 