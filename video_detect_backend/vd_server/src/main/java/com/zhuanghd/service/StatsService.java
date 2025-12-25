package com.zhuanghd.service;

import com.zhuanghd.charts.response.StatsResponse;

import java.util.List;
import java.util.Map;

/**
 * 统计数据服务接口
 */
public interface StatsService {
    
    /**
     * 获取检查记录统计数据
     * @param timeRange 时间范围（可选）
     * @return 统计数据
     */
    List<StatsResponse> getCheckStats(Map<String, Object> params);
    
    /**
     * 获取设备统计数据
     * @param timeRange 时间范围（可选）
     * @return 统计数据
     */
    List<StatsResponse> getDeviceStats(Map<String, Object> params);
    
    /**
     * 获取火灾记录统计数据
     * @param timeRange 时间范围（可选）
     * @return 统计数据
     */
    List<StatsResponse> getFireStats(Map<String, Object> params);
    
    /**
     * 获取地点统计数据
     * @param timeRange 时间范围（可选）
     * @return 统计数据
     */
    List<StatsResponse> getPlaceStats(Map<String, Object> params);
    
    /**
     * 获取各地点设备数量统计
     * @return 地点和设备数量的映射列表
     */
    List<Map<String, Object>> getPlaceDeviceStats();
    
    /**
     * 获取各地点检查次数统计
     * @return 地点和检查次数的映射列表
     */
    List<Map<String, Object>> getPlaceCheckStats();
} 