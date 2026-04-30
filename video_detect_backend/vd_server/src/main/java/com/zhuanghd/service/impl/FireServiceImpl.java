package com.zhuanghd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.protobuf.ServiceException;
import com.zhuanghd.entity.FireDO;
import com.zhuanghd.config.AlertProperties;
import com.zhuanghd.entity.VideoDO;
import com.zhuanghd.fire.request.FireQueryRequest;
import com.zhuanghd.fire.request.FireRequest;
import com.zhuanghd.service.AlertNotifyService;
import com.zhuanghd.service.FireService;
import com.zhuanghd.service.VideoService;
import com.zhuanghd.mapper.FireMapper;
import com.zhuanghd.utils.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import static com.zhuanghd.constant.NormalConstant.WORKER_ID;

/**
* @author 00103933
* @description 针对表【vd_fire】的数据库操作Service实现
* @createDate 2025-03-25 11:19:27
*/
@Service
@Slf4j
public class FireServiceImpl extends ServiceImpl<FireMapper, FireDO>
    implements FireService{

    @Autowired
    private VideoService videoService;

    @Autowired
    private AlertNotifyService alertNotifyService;

    @Autowired
    private AlertProperties alertProperties;

    @Override
    public boolean save(FireRequest param) {
        FireDO fireDO = FireDO.builder().build();
        BeanUtils.copyProperties(param, fireDO);
        
        // 确保设置了用户ID
        if (fireDO.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        return super.save(fireDO);
    }

    @Override
    public boolean updateFire(Long id, FireRequest param) {
        FireDO fireDO = this.getById(id);
        if (fireDO == null) {
            throw new RuntimeException("火灾记录不存在");
        }
        
        // 保存原始用户ID，确保不会被修改
        Long originalUserId = fireDO.getUserId();
        
        BeanUtils.copyProperties(param, fireDO);
        
        // 恢复原始用户ID，防止被篡改
        fireDO.setId(id);
        fireDO.setUserId(originalUserId);
        
        return this.updateById(fireDO);
    }

    @Override
    public Map<String, Object> fireList(int current, int size, Map<String, Object> param) throws ServiceException {
        Page<FireDO> page = new Page<>(current, size);

        if (param.get("userId") == null) {
            throw new ServiceException("用户无效");
        }

        LambdaQueryWrapper<FireDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(FireDO::getUserId, param.get("userId"));

        // 地点ID过滤
        if (param.containsKey("placeId") && param.get("placeId") != null) {
            lambdaQueryWrapper.eq(FireDO::getPlaceId, param.get("placeId"));
        }
        
        // 是否火灾过滤
        if (param.containsKey("fireFlag") && param.get("fireFlag") != null) {
            lambdaQueryWrapper.eq(FireDO::getFireFlag, param.get("fireFlag"));
        }
        
        // 是否烟雾过滤
        if (param.containsKey("smokeFlag") && param.get("smokeFlag") != null) {
            lambdaQueryWrapper.eq(FireDO::getSmokeFlag, param.get("smokeFlag"));
        }
        
        // 开始时间范围过滤
        if (param.containsKey("startTime") && param.get("startTime") != null) {
            lambdaQueryWrapper.ge(FireDO::getStartTime, param.get("startTime"));
        }
        
        // 结束时间范围过滤
        if (param.containsKey("endTime") && param.get("endTime") != null) {
            lambdaQueryWrapper.le(FireDO::getEndTime, param.get("endTime"));
        }
        
        // 按开始时间降序排序
        lambdaQueryWrapper.orderByDesc(FireDO::getStartTime);
        
        // 执行查询
        Page<FireDO> result = this.page(page, lambdaQueryWrapper);
        
        // 返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        map.put("pages", result.getPages());
        map.put("current", result.getCurrent());
        map.put("size", result.getSize());
        
        return map;
    }

    @Override
    public Map<String, Object> queryFireRecords(FireQueryRequest queryParam) {
        Page<FireDO> page = new Page<>(1, 100); // 默认查询前100条记录
        LambdaQueryWrapper<FireDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        
        // 用户ID过滤
        if (queryParam.getUserId() != null) {
            lambdaQueryWrapper.eq(FireDO::getUserId, queryParam.getUserId());
        }
        
        // 地点ID过滤
        if (queryParam.getPlaceId() != null) {
            lambdaQueryWrapper.eq(FireDO::getPlaceId, queryParam.getPlaceId());
        }
        
        // 是否火灾过滤
        if (queryParam.getFireFlag() != null) {
            lambdaQueryWrapper.eq(FireDO::getFireFlag, queryParam.getFireFlag());
        }
        
        // 是否烟雾过滤
        if (queryParam.getSmokeFlag() != null) {
            lambdaQueryWrapper.eq(FireDO::getSmokeFlag, queryParam.getSmokeFlag());
        }
        
        // 开始时间范围过滤
        if (queryParam.getStartTimeBegin() != null) {
            lambdaQueryWrapper.ge(FireDO::getStartTime, queryParam.getStartTimeBegin());
        }
        
        if (queryParam.getStartTimeEnd() != null) {
            lambdaQueryWrapper.le(FireDO::getStartTime, queryParam.getStartTimeEnd());
        }
        
        // 排序方式
        if ("asc".equalsIgnoreCase(queryParam.getTimeOrder())) {
            lambdaQueryWrapper.orderByAsc(FireDO::getStartTime);
        } else {
            lambdaQueryWrapper.orderByDesc(FireDO::getStartTime);
        }
        
        // 执行查询
        Page<FireDO> result = this.page(page, lambdaQueryWrapper);
        
        // 返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        
        return map;
    }

    @Override
    public boolean syncFromVideo(VideoDO videoDO) {
        try {
            if (videoDO == null) {
                return false;
            }
            
            // 检查视频状态是否已处理完成
            if (videoDO.getStatus() != 2) {
                log.warn("视频状态不是已处理完成，不同步到火灾记录: videoId={}, status={}", 
                        videoDO.getId(), videoDO.getStatus());
                return false;
            }
            
            // 检查视频处理时长
            Integer dTime = videoDO.getDTime();
            if (dTime == null || dTime <= 0) {
                log.warn("视频处理时长无效，不同步到火灾记录: videoId={}, dTime={}", 
                        videoDO.getId(), dTime);
                return false;
            }
            
            // 检查火灾或烟雾概率
            Double fireProbability = videoDO.getFireProbability();
            Double smokeProbability = videoDO.getSmokeProbability();
            
            if ((fireProbability == null) &&
                    (smokeProbability == null)) {
                log.info("视频未检测到火灾或烟雾，不同步到火灾记录: videoId={}, fireProbability={}, smokeProbability={}", 
                        videoDO.getId(), fireProbability, smokeProbability);
                return false;
            }
            
            // 根据概率设置标志//
            int fireFlag = (fireProbability != null && fireProbability >= alertProperties.getFireThreshold()) ? 1 : 0;
            int smokeFlag = (smokeProbability != null && smokeProbability >= alertProperties.getSmokeThreshold()) ? 1 : 0;
            
            // 设置开始和结束时间
            Date startTime = videoDO.getProcessTime() != null 
                    ? convertToDate(videoDO.getProcessTime()) 
                    : new Date();
            Date endTime = new Date(startTime.getTime() + (dTime * 1000L));
            
            // 生成火灾记录ID
            long fireId = new SnowflakeIdWorker(WORKER_ID).nextId();
            
            FireDO fireDO = FireDO.builder()
                    .id(fireId)
                    .placeId(videoDO.getPlaceId())
                    .prob(videoDO.getFireProbability())
                    .smokeProb(videoDO.getSmokeProbability())
                    .fireFlag(fireFlag)
                    .smokeFlag(smokeFlag)
                    .startTime(startTime)
                    .endTime(endTime)
                    .videoId(videoDO.getId())
                    .userId(videoDO.getUserId()) // 从视频记录同步用户ID
                    .build();
            
            // 保存火灾记录
            boolean result = this.save(fireDO);
            
            if (result) {
                log.info("视频数据同步到火灾记录成功: videoId={}, fireId={}, 持续时间={}秒", 
                        videoDO.getId(), fireId, dTime);
                if (fireFlag == 1 || smokeFlag == 1) {
                    alertNotifyService.notifyFireAlert(fireDO);
                }
            } else {
                log.error("视频数据同步到火灾记录失败: videoId={}", videoDO.getId());
            }
            
            return result;
        } catch (Exception e) {
            log.error("视频数据同步到火灾记录异常", e);
            return false;
        }
    }
    
    /**
     * 将LocalDateTime转换为Date
     */
    private Date convertToDate(java.time.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return new Date();
        }
        return java.sql.Timestamp.valueOf(localDateTime);
    }

    @Override
    public Map<String, Object> getFireVideo(Long fireId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取火灾记录
            FireDO fireDO = this.getById(fireId);
            if (fireDO == null) {
                result.put("success", false);
                result.put("message", "火灾记录不存在");
                return result;
            }
            
            // 检查是否有关联的视频ID
            if (fireDO.getVideoId() == null) {
                result.put("success", false);
                result.put("message", "该火灾记录没有关联视频");
                return result;
            }
            
            // 获取视频信息
            VideoDO videoDO = getVideoById(fireDO.getVideoId());
            if (videoDO == null) {
                result.put("success", false);
                result.put("message", "关联视频不存在");
                return result;
            }
            
            // 视频URL信息
            Map<String, Object> videoInfo = new HashMap<>();
            
            // 处理后的视频URL
            String videoUrl = getVideoUrl(videoDO);
            videoInfo.put("videoUrl", videoUrl);
            
            // 缩略图URL
            if (videoDO.getThumbnail() != null && !videoDO.getThumbnail().isEmpty()) {
                String thumbnailUrl = getThumbnailUrl(videoDO.getThumbnail());
                videoInfo.put("thumbnailUrl", thumbnailUrl);
            }
            
            // 其他视频相关信息
            videoInfo.put("videoId", videoDO.getId());
            videoInfo.put("title", videoDO.getTitle());
            videoInfo.put("uploadTime", videoDO.getUploadTime());
            videoInfo.put("processTime", videoDO.getProcessTime());
            videoInfo.put("duration", videoDO.getDTime());
            
            result.put("success", true);
            result.put("message", "获取视频信息成功");
            result.put("data", videoInfo);
        } catch (Exception e) {
            log.error("获取视频信息失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取视频信息失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getFireVideoResource(Long fireId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取火灾记录
            FireDO fireDO = this.getById(fireId);
            if (fireDO == null) {
                result.put("success", false);
                result.put("message", "火灾记录不存在");
                return result;
            }
            
            // 检查是否有关联的视频ID
            if (fireDO.getVideoId() == null) {
                result.put("success", false);
                result.put("message", "该火灾记录没有关联视频");
                return result;
            }
            
            // 获取视频信息
            VideoDO videoDO = getVideoById(fireDO.getVideoId());
            if (videoDO == null) {
                result.put("success", false);
                result.put("message", "关联视频不存在");
                return result;
            }
            
            // 获取视频文件路径，优先使用处理后的视频
            String videoPath = videoDO.getProcessedUrl() != null && !videoDO.getProcessedUrl().isEmpty() 
                    ? videoDO.getProcessedUrl() : videoDO.getVideoUrl();
            
            if (videoPath == null || videoPath.isEmpty()) {
                result.put("success", false);
                result.put("message", "视频文件不存在");
                return result;
            }
            
            // 检查文件是否存在
            File file = new File(videoPath);
            if (!file.exists()) {
                result.put("success", false);
                result.put("message", "视频文件不存在于存储位置");
                return result;
            }
            
            // 获取视频文件信息
            Map<String, Object> resourceInfo = new HashMap<>();
            resourceInfo.put("filePath", videoPath);
            resourceInfo.put("fileName", file.getName());
            resourceInfo.put("fileSize", file.length());
            resourceInfo.put("extension", videoDO.getExtension());
            
            // 确定内容类型
            String contentType = getContentType(videoDO.getExtension());
            resourceInfo.put("contentType", contentType);
            
            result.put("success", true);
            result.put("message", "获取视频文件资源成功");
            result.put("data", resourceInfo);
        } catch (Exception e) {
            log.error("获取视频文件资源失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取视频文件资源失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 根据ID获取视频信息
     */
    private VideoDO getVideoById(Long videoId) {
        try {
            return videoService.getById(videoId);
        } catch (Exception e) {
            log.error("获取视频信息失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取视频URL
     */
    private String getVideoUrl(VideoDO videoDO) {
        try {
            String videoPath;
            
            if (videoDO.getProcessedUrl() != null && !videoDO.getProcessedUrl().isEmpty()) {
                // 从处理后的视频路径中获取文件名
                File processedFile = new File(videoDO.getProcessedUrl());
                String fileName = processedFile.getName();
                // 使用静态资源映射访问视频
                videoPath = "http://localhost:8081/videos/" + fileName;
            } else {
                // 从原始视频路径中获取文件名
                File originalFile = new File(videoDO.getVideoUrl());
                String fileName = originalFile.getName();
                // 使用静态资源映射访问视频
                videoPath = "http://localhost:8081/videos/" + fileName;
            }
            
            return videoPath;
        } catch (Exception e) {
            log.error("获取视频URL失败: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 获取缩略图URL
     */
    private String getThumbnailUrl(String thumbnailPath) {
        try {
            return thumbnailPath.replace("E:/vddt/thumbnails", "http://localhost:8081/thumbnails");
        } catch (Exception e) {
            log.error("获取缩略图URL失败: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 获取内容类型
     */
    private String getContentType(String extension) {
        if (extension == null) {
            return "application/octet-stream";
        }
        
        switch (extension.toLowerCase()) {
            case ".mp4":
                return "video/mp4";
            case ".avi":
                return "video/x-msvideo";
            case ".mov":
                return "video/quicktime";
            case ".wmv":
                return "video/x-ms-wmv";
            case ".flv":
                return "video/x-flv";
            case ".mkv":
                return "video/x-matroska";
            default:
                return "application/octet-stream";
        }
    }
}





