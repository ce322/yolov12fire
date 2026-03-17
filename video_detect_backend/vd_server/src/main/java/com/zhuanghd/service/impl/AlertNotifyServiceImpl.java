package com.zhuanghd.service.impl;

import com.zhuanghd.alert.request.RealtimeDetectionItem;
import com.zhuanghd.config.AlertProperties;
import com.zhuanghd.entity.FireDO;
import com.zhuanghd.entity.UserDO;
import com.zhuanghd.mapper.FireMapper;
import com.zhuanghd.mapper.UserMapper;
import com.zhuanghd.utils.SnowflakeIdWorker;
import com.zhuanghd.service.AlertNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.zhuanghd.constant.NormalConstant.WORKER_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertNotifyServiceImpl implements AlertNotifyService {

    private final JavaMailSender mailSender;
    private final UserMapper userMapper;
    private final FireMapper fireMapper;
    private final AlertProperties alertProperties;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    /**
     * 每个用户的告警冷却时间控制
     */
    private final Map<Long, Long> userLastAlertTimeMap = new ConcurrentHashMap<>();

    @Override
    public void notifyFireAlert(FireDO fireRecord) {
        if (!alertProperties.isEnabled()) {
            return;
        }

        if (fireRecord == null || fireRecord.getUserId() == null) {
            return;
        }

        UserDO user = userMapper.selectById(fireRecord.getUserId());
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            log.warn("告警发送失败：用户不存在或未配置邮箱, userId={}", fireRecord.getUserId());
            return;
        }

        try {
            sendMail(user.getEmail(), "[视频火灾检测告警] 请及时处理", buildMailContent(fireRecord));
            log.info("告警邮件发送成功: userId={}, email={}, fireId={}", user.getId(), user.getEmail(), fireRecord.getId());
        } catch (Exception e) {
            log.error("告警邮件发送失败: userId={}, fireId={}", fireRecord.getUserId(), fireRecord.getId(), e);
        }
    }

    @Override
    public boolean notifyRealtimeAlert(Long userId, Long placeId, List<RealtimeDetectionItem> detections) {
        if (userId == null || placeId == null || detections == null || detections.isEmpty()) {
            return false;
        }

        List<RealtimeDetectionItem> riskDetections = detections.stream()
                .filter(Objects::nonNull)
                .filter(this::isRiskDetection)
                .collect(Collectors.toList());

        if (riskDetections.isEmpty()) {
            return false;
        }

        FireDO fireDO = buildRealtimeFireRecord(userId, placeId, riskDetections);
        fireMapper.insert(fireDO);
        log.info("实时检测结果已写入火灾记录: fireId={}, userId={}, placeId={}", fireDO.getId(), userId, placeId);

        if (!alertProperties.isEnabled()) {
            return true;
        }

        long now = System.currentTimeMillis();
        long cooldownMs = Math.max(alertProperties.getCooldownSeconds(), 0) * 1000L;
        Long lastAlertTime = userLastAlertTimeMap.get(userId);
        if (lastAlertTime != null && now - lastAlertTime < cooldownMs) {
            return true;
        }

        UserDO user = userMapper.selectById(userId);
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            log.warn("实时告警发送失败：用户不存在或未配置邮箱, userId={}", userId);
            return true;
        }

        String subject = "[实时检测告警] 监控画面发现火灾/烟雾风险";
        String content = buildRealtimeMailContent(placeId, riskDetections);
        sendMail(user.getEmail(), subject, content);
        userLastAlertTimeMap.put(userId, now);
        log.info("实时告警邮件发送成功: userId={}, placeId={}, hitCount={}", userId, placeId, riskDetections.size());
        return true;
    }


    private FireDO buildRealtimeFireRecord(Long userId, Long placeId, List<RealtimeDetectionItem> detections) {
        double fireProb = detections.stream()
                .filter(i -> i.getLabel() != null && i.getLabel().toLowerCase().contains("fire"))
                .map(RealtimeDetectionItem::getConfidence)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(0D);

        double smokeProb = detections.stream()
                .filter(i -> i.getLabel() != null && i.getLabel().toLowerCase().contains("smoke"))
                .map(RealtimeDetectionItem::getConfidence)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(0D);

        int fireFlag = fireProb >= alertProperties.getFireThreshold() ? 1 : 0;
        int smokeFlag = smokeProb >= alertProperties.getSmokeThreshold() ? 1 : 0;
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 1000L);

        String situation = detections.stream()
                .limit(3)
                .map(item -> item.getLabel() + ":" + String.format("%.2f%%", (item.getConfidence() == null ? 0D : item.getConfidence()) * 100))
                .collect(Collectors.joining("; "));

        return FireDO.builder()
                .id(new SnowflakeIdWorker(WORKER_ID).nextId())
                .startTime(startTime)
                .endTime(endTime)
                .placeId(placeId)
                .reason("实时检测自动记录")
                .situation(situation)
                .prob(fireProb)
                .fireFlag(fireFlag)
                .smokeProb(smokeProb)
                .smokeFlag(smokeFlag)
                .videoId(0L)
                .userId(userId)
                .build();
    }

    private boolean isRiskDetection(RealtimeDetectionItem item) {
        String label = item.getLabel() == null ? "" : item.getLabel().toLowerCase();
        double confidence = item.getConfidence() == null ? 0D : item.getConfidence();
        boolean isFire = label.contains("fire") && confidence >= alertProperties.getFireThreshold();
        boolean isSmoke = label.contains("smoke") && confidence >= alertProperties.getSmokeThreshold();
        return isFire || isSmoke;
    }

    private void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        String from = StringUtils.hasText(alertProperties.getMailFrom()) ? alertProperties.getMailFrom() : mailUsername;
        if (StringUtils.hasText(from)) {
            message.setFrom(from);
        }
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    private String buildRealtimeMailContent(Long placeId, List<RealtimeDetectionItem> detections) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append("实时检测发现风险目标，请及时处理。\n\n")
                .append("检测时间: ").append(time).append("\n")
                .append("地点ID: ").append(placeId == null ? "-" : placeId).append("\n")
                .append("命中数量: ").append(detections.size()).append("\n\n")
                .append("命中详情:\n");

        for (int i = 0; i < detections.size(); i++) {
            RealtimeDetectionItem d = detections.get(i);
            sb.append(i + 1).append(") label=").append(d.getLabel())
                    .append(", confidence=").append(String.format("%.2f%%", (d.getConfidence() == null ? 0D : d.getConfidence()) * 100));
            if (d.getBox() != null && !d.getBox().isEmpty()) {
                sb.append(", box=").append(d.getBox());
            }
            sb.append("\n");
        }
        sb.append("\n请登录系统核查实时监控页面。\n");
        return sb.toString();
    }

    private String buildMailContent(FireDO fireRecord) {
        String startTime = formatDate(fireRecord.getStartTime());
        String endTime = formatDate(fireRecord.getEndTime());
        return "检测系统发现疑似火灾/烟雾，请尽快核查。\n\n"
                + "检测结果如下：\n"
                + "- 记录ID: " + valueOf(fireRecord.getId()) + "\n"
                + "- 地点ID: " + valueOf(fireRecord.getPlaceId()) + "\n"
                + "- 视频ID: " + valueOf(fireRecord.getVideoId()) + "\n"
                + "- 火灾概率: " + percent(fireRecord.getProb()) + "\n"
                + "- 烟雾概率: " + percent(fireRecord.getSmokeProb()) + "\n"
                + "- 火灾标志: " + (fireRecord.getFireFlag() != null && fireRecord.getFireFlag() == 1 ? "是" : "否") + "\n"
                + "- 烟雾标志: " + (fireRecord.getSmokeFlag() != null && fireRecord.getSmokeFlag() == 1 ? "是" : "否") + "\n"
                + "- 开始时间: " + startTime + "\n"
                + "- 结束时间: " + endTime + "\n\n"
                + "请登录系统查看详情。";
    }

    private String percent(Double value) {
        if (value == null) {
            return "0.00%";
        }
        return String.format("%.2f%%", value * 100);
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private String valueOf(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }
}
