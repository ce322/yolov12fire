package com.zhuanghd.alert.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class RealtimeDetectionItem {

    @NotBlank(message = "检测标签不能为空")
    private String label;

    @NotNull(message = "置信度不能为空")
    private Double confidence;

    /**
     * [x, y, width, height]
     */
    private List<Integer> box;
}
