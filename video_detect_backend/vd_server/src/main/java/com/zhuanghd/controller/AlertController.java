package com.zhuanghd.controller;

import com.zhuanghd.alert.request.RealtimeAlertRequest;
import com.zhuanghd.result.BaseFunction;
import com.zhuanghd.service.AlertNotifyService;
import com.zhuanghd.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/alert")
@Api(tags = "实时告警接口")
@Slf4j
public class AlertController extends BaseFunction {

    @Autowired
    private AlertNotifyService alertNotifyService;

    @ApiOperation("实时检测触发告警")
    @PostMapping("/realtime")
    public ResponseEntity<Map<String, Object>> realtimeAlert(@Valid @RequestBody RealtimeAlertRequest request) {
        try {
            if (UserHolder.getUser() == null) {
                return returnResponse("用户未登录", false);
            }
            Long userId = UserHolder.getUser().getId();
            boolean sent = alertNotifyService.notifyRealtimeAlert(userId, request.getPlaceId(), request.getDetections());
            if (sent) {
                return returnResponse("告警已发送", true);
            }
            return returnResponse("当前帧未命中告警条件或处于冷却中", true);
        } catch (Exception e) {
            log.error("实时告警触发失败", e);
            return returnResponse("实时告警触发失败: " + e.getMessage(), false);
        }
    }
}
