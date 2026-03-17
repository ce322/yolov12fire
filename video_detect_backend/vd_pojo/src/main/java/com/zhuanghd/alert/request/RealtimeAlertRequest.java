package com.zhuanghd.alert.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class RealtimeAlertRequest {

    @NotNull(message = "地点ID不能为空")
    private Long placeId;

    @Valid
    private List<RealtimeDetectionItem> detections;
}
