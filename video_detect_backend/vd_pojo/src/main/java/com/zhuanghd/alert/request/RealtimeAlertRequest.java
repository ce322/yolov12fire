package com.zhuanghd.alert.request;

import lombok.Data;

import javax.validation.Valid;
import java.util.List;

@Data
public class RealtimeAlertRequest {

    private Long placeId;

    @Valid
    private List<RealtimeDetectionItem> detections;
}
