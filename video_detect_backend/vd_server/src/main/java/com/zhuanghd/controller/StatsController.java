package com.zhuanghd.controller;

import com.zhuanghd.charts.response.StatsResponse;
import com.zhuanghd.result.BaseFunction;
import com.zhuanghd.service.StatsService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stats")
@Api(tags = "统计数据接口")
@Slf4j
public class StatsController extends BaseFunction {

    @Autowired
    private StatsService statsService;
    
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> getCheckStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        
        List<StatsResponse> result = statsService.getCheckStats(params);
        return returnResponse("获取检查统计数据成功", true, result);
    }
    
    @GetMapping("/device")
    public ResponseEntity<Map<String, Object>> getDeviceStats() {
        Map<String, Object> params = new HashMap<>();
        List<StatsResponse> result = statsService.getDeviceStats(params);
        return returnResponse("获取设备统计数据成功", true, result);
    }
    
    @GetMapping("/fire")
    public ResponseEntity<Map<String, Object>> getFireStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        
        List<StatsResponse> result = statsService.getFireStats(params);
        return returnResponse("获取火灾统计数据成功", true, result);
    }
    
    @GetMapping("/place")
    public ResponseEntity<Map<String, Object>> getPlaceStats() {
        Map<String, Object> params = new HashMap<>();
        List<StatsResponse> result = statsService.getPlaceStats(params);
        return returnResponse("获取地点统计数据成功", true, result);
    }
    
    @GetMapping("/place-device")
    public ResponseEntity<Map<String, Object>> getPlaceDeviceStats() {
        List<Map<String, Object>> result = statsService.getPlaceDeviceStats();
        return returnResponse("获取地点设备数量统计成功", true, result);
    }
    
    @GetMapping("/place-check")
    public ResponseEntity<Map<String, Object>> getPlaceCheckStats() {
        List<Map<String, Object>> result = statsService.getPlaceCheckStats();
        return returnResponse("获取地点检查次数统计成功", true, result);
    }
} 