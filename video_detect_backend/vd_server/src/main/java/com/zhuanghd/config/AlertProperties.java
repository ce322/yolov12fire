package com.zhuanghd.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alert")
public class AlertProperties {
    /**
     * 是否开启报警通知
     */
    private boolean enabled = false;

    /**
     * 火灾告警概率阈值
     */
    private double fireThreshold = 0.30;

    /**
     * 烟雾告警概率阈值
     */
    private double smokeThreshold = 0.25;

    /**
     * 发件人邮箱，未配置则使用 spring.mail.username
     */
    private String mailFrom;

    /**
     * 告警冷却时间（秒），防止实时检测高频重复发送
     */
    private int cooldownSeconds = 60;
}


