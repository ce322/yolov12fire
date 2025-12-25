package com.zhuanghd.controller;

import com.zhuanghd.check.request.CheckRequest;
import com.zhuanghd.device.request.DeviceRequest;
import com.zhuanghd.result.BaseFunction;
import com.zhuanghd.service.CheckService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/check")
@Api(tags = "检查相关接口")
@Slf4j
public class CheckController extends BaseFunction {

    @Autowired
    private CheckService checkService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> add(@RequestBody CheckRequest param) {
        try {
            checkService.add(param);
            return returnResponse("添加检查成功", true);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> detail(@RequestParam Long id) {
        try {
            Map<String, Object> result = checkService.detail(id);
            return returnResponse("获得检查详情成功", true, result);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam Long id) {
        try {
            checkService.delete(id);
            return returnResponse("删除检查成功", true);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @PutMapping("/modify/{id}")
    public ResponseEntity<Map<String, Object>> modify(@RequestBody CheckRequest param, @PathVariable Long id) {
        try {
            checkService.modify(param, id);
            return returnResponse("修改检查内容成功", true);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(@RequestParam(defaultValue = "1") int current,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(required = false) String duty,
                                                   @RequestParam(required = false) Long place,
                                                   @RequestParam(required = false) Integer score,
                                                   @RequestParam(required = false) String startTime,
                                                   @RequestParam(required = false) String endTime) {
        try {
            log.info("获取检查列表 - current: {}, size: {}, duty: {}, place: {}, score: {}, startTime: {}, endTime: {}", 
                    current, size, duty, place, score, startTime, endTime);
                    
            Map<String, Object> params = new HashMap<>();
            // 添加筛选参数
            if (duty != null && !duty.isEmpty()) {
                params.put("duty", duty);
            }
            if (place != null) {
                params.put("place", place);
            }
            if (score != null) {
                params.put("score", score);
            }
            if (startTime != null && !startTime.isEmpty()) {
                params.put("startTime", startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                params.put("endTime", endTime);
            }
            
            Map<String, Object> result = checkService.getList(current, size, params);
            return returnResponse("获取查找列表成功", true, result);
        } catch (Exception e) {
            log.error("获取检查列表失败", e);
            return returnResponse(e.getMessage(), false);
        }
    }
}
