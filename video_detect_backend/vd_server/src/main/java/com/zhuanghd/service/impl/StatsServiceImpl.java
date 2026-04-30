package com.zhuanghd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuanghd.charts.response.StatsResponse;
import com.zhuanghd.entity.CheckDO;
import com.zhuanghd.entity.DeviceDO;
import com.zhuanghd.entity.FireDO;
import com.zhuanghd.entity.PlaceDO;
import com.zhuanghd.mapper.CheckMapper;
import com.zhuanghd.mapper.DeviceMapper;
import com.zhuanghd.mapper.FireMapper;
import com.zhuanghd.mapper.PlaceMapper;
import com.zhuanghd.service.StatsService;
import com.zhuanghd.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StatsServiceImpl implements StatsService {

    @Autowired
    private CheckMapper checkMapper;
    
    @Autowired
    private DeviceMapper deviceMapper;
    
    @Autowired
    private FireMapper fireMapper;
    
    @Autowired
    private PlaceMapper placeMapper;

    @Override
    public List<StatsResponse> getCheckStats(Map<String, Object> params) {
        log.info("获取检查记录统计数据，参数：{}", params);
        List<StatsResponse> result = new ArrayList<>();
        Long userId = UserHolder.getUser().getId();
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<CheckDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CheckDO::getUserId, userId)
                    .eq(CheckDO::getDeleteFlag, 0);
            
            // 处理时间范围
            if (params.containsKey("startTime") && params.get("startTime") != null) {
                String startTimeStr = (String) params.get("startTime");
                queryWrapper.ge(CheckDO::getTime, startTimeStr + " 00:00:00");
            }
            
            if (params.containsKey("endTime") && params.get("endTime") != null) {
                String endTimeStr = (String) params.get("endTime");
                queryWrapper.le(CheckDO::getTime, endTimeStr + " 23:59:59");
            }
            
            // 查询数据
            List<CheckDO> checkList = checkMapper.selectList(queryWrapper);
            
            // 1. 按评分统计
            StatsResponse scoreStats = getCheckScoreStats(checkList);
            result.add(scoreStats);
            
            // 2. 按时间统计
            StatsResponse timeStats = getCheckTimeStats(checkList);
            result.add(timeStats);
            
            return result;
        } catch (Exception e) {
            log.error("获取检查记录统计数据异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取检查评分统计
     */
    private StatsResponse getCheckScoreStats(List<CheckDO> checkList) {
        // 按评分分组
        Map<Integer, Long> scoreCountMap = checkList.stream()
                .collect(Collectors.groupingBy(CheckDO::getScore, Collectors.counting()));
        
        // 转换为前端需要的格式
        List<Map<String, Object>> categoryData = new ArrayList<>();
        
        String[] scoreLabels = {"很差", "差", "中等", "良好", "优秀"};
        for (int i = 0; i < 5; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", scoreLabels[i]);
            item.put("value", scoreCountMap.getOrDefault(i, 0L));
            categoryData.add(item);
        }
        
        return StatsResponse.builder()
                .title("检查评分分布")
                .categoryData(categoryData)
                .build();
    }
    
    /**
     * 获取检查时间统计
     */
    private StatsResponse getCheckTimeStats(List<CheckDO> checkList) {
        // 按月份分组
        Map<String, Long> monthCountMap = checkList.stream()
                .collect(Collectors.groupingBy(
                        check -> {
                            LocalDateTime time = check.getTime();
                            return time.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                        },
                        Collectors.counting()
                ));
        
        // 转换为前端需要的格式
        List<Map<String, Object>> timeSeriesData = new ArrayList<>();
        
        // 排序月份
        List<String> sortedMonths = new ArrayList<>(monthCountMap.keySet());
        Collections.sort(sortedMonths);
        
        for (String month : sortedMonths) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", month);
            item.put("count", monthCountMap.get(month));
            timeSeriesData.add(item);
        }
        
        return StatsResponse.builder()
                .title("检查记录时间趋势")
                .timeSeriesData(timeSeriesData)
                .build();
    }

    @Override
    public List<StatsResponse> getDeviceStats(Map<String, Object> params) {
        log.info("获取设备统计数据，参数：{}", params);
        List<StatsResponse> result = new ArrayList<>();
        Long userId = UserHolder.getUser().getId();
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<DeviceDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeviceDO::getUserId, userId)
                    .eq(DeviceDO::getDeleteFlag, 0);
            
            // 查询数据
            List<DeviceDO> deviceList = deviceMapper.selectList(queryWrapper);
            
            // 1. 按状态统计
            StatsResponse statusStats = getDeviceStatusStats(deviceList);
            result.add(statusStats);
            
            // 2. 按地点统计
            StatsResponse placeStats = getDevicePlaceStats(deviceList);
            result.add(placeStats);
            
            return result;
        } catch (Exception e) {
            log.error("获取设备统计数据异常", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取设备状态统计
     */
    private StatsResponse getDeviceStatusStats(List<DeviceDO> deviceList) {
        // 按状态分组
        Map<Integer, Long> statusCountMap = deviceList.stream()
                .collect(Collectors.groupingBy(DeviceDO::getStatus, Collectors.counting()));
        
        // 转换为前端需要的格式
        List<Map<String, Object>> categoryData = new ArrayList<>();
        
        Map<String, Object> enabledItem = new HashMap<>();
        enabledItem.put("name", "启用");
        enabledItem.put("value", statusCountMap.getOrDefault(1, 0L));
        categoryData.add(enabledItem);
        
        Map<String, Object> disabledItem = new HashMap<>();
        disabledItem.put("name", "禁用");
        disabledItem.put("value", statusCountMap.getOrDefault(0, 0L));
        categoryData.add(disabledItem);
        
        return StatsResponse.builder()
                .title("设备状态分布")
                .categoryData(categoryData)
                .build();
    }
    
    /**
     * 获取设备地点统计
     */
    private StatsResponse getDevicePlaceStats(List<DeviceDO> deviceList) {
        // 查询所有地点
        LambdaQueryWrapper<PlaceDO> placeWrapper = new LambdaQueryWrapper<>();
        placeWrapper.eq(PlaceDO::getUserId, UserHolder.getUser().getId())
                .eq(PlaceDO::getDeleteFlag, 0);
        List<PlaceDO> placeList = placeMapper.selectList(placeWrapper);
        
        // 构建地点ID到名称的映射
        Map<Long, String> placeMap = placeList.stream()
                .collect(Collectors.toMap(PlaceDO::getId, PlaceDO::getName));
        
        // 按地点分组统计
        Map<Long, Long> placeCountMap = deviceList.stream()
                .collect(Collectors.groupingBy(DeviceDO::getPlaceId, Collectors.counting()));
        
        // 转换为前端需要的格式
        List<Map<String, Object>> categoryData = new ArrayList<>();
        
        for (Map.Entry<Long, Long> entry : placeCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            String placeName = placeMap.getOrDefault(entry.getKey(), "未知地点");
            item.put("name", placeName);
            item.put("value", entry.getValue());
            categoryData.add(item);
        }
        
        return StatsResponse.builder()
                .title("设备地点分布")
                .categoryData(categoryData)
                .build();
    }

    @Override
    public List<StatsResponse> getFireStats(Map<String, Object> params) {
        log.info("获取火灾记录统计数据，参数：{}", params);
        List<StatsResponse> result = new ArrayList<>();
        Long userId = UserHolder.getUser().getId();
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<FireDO> queryWrapper = new LambdaQueryWrapper<>();
            
            // 处理时间范围
            if (params.containsKey("startTime") && params.get("startTime") != null) {
                String startTimeStr = (String) params.get("startTime");
                queryWrapper.ge(FireDO::getStartTime, startTimeStr + " 00:00:00");
            }
            
            if (params.containsKey("endTime") && params.get("endTime") != null) {
                String endTimeStr = (String) params.get("endTime");
                queryWrapper.le(FireDO::getStartTime, endTimeStr + " 23:59:59");
            }
            
            // 查询数据
            List<FireDO> fireList = fireMapper.selectList(queryWrapper);
            
            // 1. 火灾/非火灾统计
            StatsResponse fireStatusStats = getFireStatusStats(fireList);
            result.add(fireStatusStats);
            
            // 2. 按地点统计
            StatsResponse firePlaceStats = getFirePlaceStats(fireList);
            result.add(firePlaceStats);
            
            // 3. 火灾时间趋势
            StatsResponse fireTimeStats = getFireTimeStats(fireList);
            result.add(fireTimeStats);
            
            return result;
        } catch (Exception e) {
            log.error("获取火灾记录统计数据异常", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取火灾状态统计
     */
    private StatsResponse getFireStatusStats(List<FireDO> fireList) {
        // 按是否火灾分组
        Map<Integer, Long> fireStatusCountMap = fireList.stream()
                .collect(Collectors.groupingBy(FireDO::getFireFlag, Collectors.counting()));
        
        // 转换为前端需要的格式
        List<Map<String, Object>> categoryData = new ArrayList<>();
        
        Map<String, Object> fireItem = new HashMap<>();
        fireItem.put("name", "火灾");
        fireItem.put("value", fireStatusCountMap.getOrDefault(1, 0L));
        categoryData.add(fireItem);
        
        Map<String, Object> noFireItem = new HashMap<>();
        noFireItem.put("name", "非火灾");
        noFireItem.put("value", fireStatusCountMap.getOrDefault(0, 0L));
        categoryData.add(noFireItem);
        
        return StatsResponse.builder()
                .title("火灾/非火灾统计")
                .categoryData(categoryData)
                .build();
    }
    
    /**
     * 获取火灾地点统计
     */
    private StatsResponse getFirePlaceStats(List<FireDO> fireList) {
        // 查询所有地点
        LambdaQueryWrapper<PlaceDO> placeWrapper = new LambdaQueryWrapper<>();
        placeWrapper.eq(PlaceDO::getUserId, UserHolder.getUser().getId())
                .eq(PlaceDO::getDeleteFlag, 0);
        List<PlaceDO> placeList = placeMapper.selectList(placeWrapper);
        
        // 构建地点ID到名称的映射
        Map<Long, String> placeMap = placeList.stream()
                .collect(Collectors.toMap(PlaceDO::getId, PlaceDO::getName));
        
        // 只统计火灾记录
        List<FireDO> onlyFireList = fireList.stream()
                .filter(fire -> fire.getFireFlag() == 1)
                .collect(Collectors.toList());
        
        // 按地点分组统计
        Map<Long, Long> placeCountMap = onlyFireList.stream()
                .collect(Collectors.groupingBy(FireDO::getPlaceId, Collectors.counting()));
        
        // 转换为前端需要的格式
        List<Map<String, Object>> categoryData = new ArrayList<>();
        
        for (Map.Entry<Long, Long> entry : placeCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            String placeName = placeMap.getOrDefault(entry.getKey(), "未知地点");
            item.put("name", placeName);
            item.put("value", entry.getValue());
            categoryData.add(item);
        }
        
        return StatsResponse.builder()
                .title("火灾地点分布")
                .categoryData(categoryData)
                .build();
    }
    
    /**
     * 获取火灾时间趋势
     */
    private StatsResponse getFireTimeStats(List<FireDO> fireList) {
        // 只统计火灾记录
        List<FireDO> onlyFireList = fireList.stream()
                .filter(fire -> fire.getFireFlag() == 1)
                .collect(Collectors.toList());
        
        // 按月份分组
        Map<String, Long> monthCountMap = onlyFireList.stream()
                .collect(Collectors.groupingBy(
                        fire -> {
                            Date startTime = fire.getStartTime();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(startTime);
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH) + 1;
                            return year + "-" + (month < 10 ? "0" + month : month);
                        },
                        Collectors.counting()
                ));
        
        // 转换为前端需要的格式
        List<Map<String, Object>> timeSeriesData = new ArrayList<>();
        
        // 排序月份
        List<String> sortedMonths = new ArrayList<>(monthCountMap.keySet());
        Collections.sort(sortedMonths);
        
        for (String month : sortedMonths) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", month);
            item.put("count", monthCountMap.get(month));
            timeSeriesData.add(item);
        }
        
        return StatsResponse.builder()
                .title("火灾记录时间趋势")
                .timeSeriesData(timeSeriesData)
                .build();
    }

    @Override
    public List<StatsResponse> getPlaceStats(Map<String, Object> params) {
        log.info("获取地点统计数据，参数：{}", params);
        List<StatsResponse> result = new ArrayList<>();
        Long userId = UserHolder.getUser().getId();
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<PlaceDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PlaceDO::getUserId, userId)
                    .eq(PlaceDO::getDeleteFlag, 0);
            
            // 查询数据
            List<PlaceDO> placeList = placeMapper.selectList(queryWrapper);
            
            // 1. 按省份统计
            StatsResponse provinceStats = getPlaceProvinceStats(placeList);
            result.add(provinceStats);
            
            return result;
        } catch (Exception e) {
            log.error("获取地点统计数据异常", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取地点省份统计
     */
    private StatsResponse getPlaceProvinceStats(List<PlaceDO> placeList) {
        // 按省份分组
        Map<String, Long> provinceCountMap = placeList.stream()
                .collect(Collectors.groupingBy(PlaceDO::getProvince, Collectors.counting()));
        
        // 转换为前端需要的格式
        List<Map<String, Object>> categoryData = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : provinceCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            categoryData.add(item);
        }
        
        return StatsResponse.builder()
                .title("地点省份分布")
                .categoryData(categoryData)
                .build();
    }
    
    @Override
    public List<Map<String, Object>> getPlaceDeviceStats() {
        log.info("获取各地点设备数量统计");
        List<Map<String, Object>> result = new ArrayList<>();
        Long userId = UserHolder.getUser().getId();
        
        try {
            // 查询所有地点
            LambdaQueryWrapper<PlaceDO> placeWrapper = new LambdaQueryWrapper<>();
            placeWrapper.eq(PlaceDO::getUserId, userId)
                    .eq(PlaceDO::getDeleteFlag, 0);
            List<PlaceDO> placeList = placeMapper.selectList(placeWrapper);
            
            // 查询所有设备
            LambdaQueryWrapper<DeviceDO> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(DeviceDO::getUserId, userId)
                    .eq(DeviceDO::getDeleteFlag, 0);
            List<DeviceDO> deviceList = deviceMapper.selectList(deviceWrapper);
            
            // 按地点ID分组，统计设备数量
            Map<Long, Long> placeDeviceCountMap = deviceList.stream()
                    .collect(Collectors.groupingBy(DeviceDO::getPlaceId, Collectors.counting()));
            
            // 构建结果数据
            for (PlaceDO place : placeList) {
                Map<String, Object> placeData = new HashMap<>();
                placeData.put("name", place.getName());
                placeData.put("value", placeDeviceCountMap.getOrDefault(place.getId(), 0L));
                result.add(placeData);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取各地点设备数量统计异常", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Map<String, Object>> getPlaceCheckStats() {
        log.info("获取各地点检查次数统计");
        List<Map<String, Object>> result = new ArrayList<>();
        Long userId = UserHolder.getUser().getId();
        
        try {
            // 查询所有地点
            LambdaQueryWrapper<PlaceDO> placeWrapper = new LambdaQueryWrapper<>();
            placeWrapper.eq(PlaceDO::getUserId, userId)
                    .eq(PlaceDO::getDeleteFlag, 0);
            List<PlaceDO> placeList = placeMapper.selectList(placeWrapper);
            
            // 创建地点ID到名称的映射
            Map<Long, String> placeIdToNameMap = placeList.stream()
                    .collect(Collectors.toMap(PlaceDO::getId, PlaceDO::getName));
                
            // 创建地点名称到ID的映射
            Map<String, Long> placeNameToIdMap = placeList.stream()
                    .collect(Collectors.toMap(PlaceDO::getName, PlaceDO::getId));

            // 查询所有检查记录
            LambdaQueryWrapper<CheckDO> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(CheckDO::getUserId, userId)
                    .eq(CheckDO::getDeleteFlag, 0);
            List<CheckDO> checkList = checkMapper.selectList(checkWrapper);

            // 按地点名称分组，统计检查次数
            Map<String, Long> placeCheckCountMap = checkList.stream()
                    .filter(check -> check.getPlace() != null && !check.getPlace().isEmpty())
                    .collect(Collectors.groupingBy(CheckDO::getPlace, Collectors.counting()));

            // 构建结果数据
            for (PlaceDO place : placeList) {
                Map<String, Object> placeData = new HashMap<>();
                if (placeCheckCountMap.getOrDefault(String.valueOf(place.getId()), 0L)==0){
                    continue;
                }
                placeData.put("name", place.getName());
                placeData.put("value", placeCheckCountMap.getOrDefault(String.valueOf(place.getId()), 0L));
                result.add(placeData);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取各地点检查次数统计异常", e);
            return Collections.emptyList();
        }
    }
} 