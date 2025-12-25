package com.zhuanghd.controller;

import com.zhuanghd.device.request.DeviceRequest;
import com.zhuanghd.result.BaseFunction;
import com.zhuanghd.service.DeviceService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/device")
@Api(tags = "设备相关接口")
@Slf4j
public class DeviceController extends BaseFunction {
    @Autowired
    private DeviceService deviceService;

    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> detail(@RequestParam Long id) {
        try {
            Map<String, Object> result = deviceService.detail(id);
            return returnResponse("获取设备详情成功", true, result);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> add(@RequestBody DeviceRequest param) {
        try {
            deviceService.add(param);
            return returnResponse("添加新设备成功", true);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam Long id) {
        try {
            deviceService.delete(id);
            return returnResponse("删除设备成功", true);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @PutMapping("/modify/{id}")
    // TODO 这里的validate是否需要配置 class
    public ResponseEntity<Map<String, Object>> modify(@PathVariable long id, @Validated @RequestBody DeviceRequest param) {
        try {
            deviceService.modify(id, param);
            return returnResponse("更新设备信息成功", true);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(@RequestParam(defaultValue = "1") int current,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(required = false) String duty,
                                                    @RequestParam(required = false) Long place_id,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) String startTime,
                                                    @RequestParam(required = false) String endTime) {
        try {
            log.info("获取设备列表 - current: {}, size: {}, duty: {}, place_id: {}, status: {}, startTime: {}, endTime: {}", 
                    current, size, duty, place_id, status, startTime, endTime);
                    
            Map<String, Object> params = new HashMap<>();
            // 添加筛选参数
            if (duty != null && !duty.isEmpty()) {
                params.put("duty", duty);
            }
            if (place_id != null) {
                params.put("placeId", place_id);
            }
            if (status != null) {
                params.put("status", status);
            }
            if (startTime != null && !startTime.isEmpty()) {
                params.put("startTime", startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                params.put("endTime", endTime);
            }
            
            Map<String, Object> result = deviceService.deviceList(current, size, params);
            return returnResponse("获得设备列表成功", true, result);
        } catch (Exception e) {
            log.error("获取设备列表失败", e);
            return returnResponse(e.getMessage(), false);
        }
    }
}
