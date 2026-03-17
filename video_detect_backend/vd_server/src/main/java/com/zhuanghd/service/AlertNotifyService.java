package com.zhuanghd.service;

import com.zhuanghd.alert.request.RealtimeDetectionItem;
import com.zhuanghd.entity.FireDO;

import java.util.List;

public interface AlertNotifyService {

    /**
     * 发送火灾/烟雾告警通知
     */
    void notifyFireAlert(FireDO fireRecord);

    /**
     * 实时检测告警（基于监控页逐帧检测）
     * @return true 表示本次实际发送了告警
     */
    boolean notifyRealtimeAlert(Long userId, Long placeId, List<RealtimeDetectionItem> detections);
}

