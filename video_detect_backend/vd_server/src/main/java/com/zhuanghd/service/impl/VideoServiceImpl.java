package com.zhuanghd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.protobuf.ServiceException;
import com.zhuanghd.entity.VideoDO;
import com.zhuanghd.service.FireService;
import com.zhuanghd.service.VideoService;
import com.zhuanghd.mapper.VideoMapper;
import com.zhuanghd.utils.LocalFileUtil;
import com.zhuanghd.utils.SnowflakeIdWorker;
import com.zhuanghd.utils.UserHolder;
import com.zhuanghd.utils.VideoProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static com.zhuanghd.constant.NormalConstant.WORKER_ID;

/**
 * @author 庄泓东
 * @description 针对表【vd_video】的数据库操作Service实现
 * @createDate 2024-10-30 16:31:34
 */
@Slf4j
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, VideoDO>
		implements VideoService {

	@Autowired
	private VideoMapper videoMapper;
	
	@Autowired
	private VideoProcessUtil videoProcessUtil;
	
	@Autowired
	private FireService fireService;

	@Override
	public VideoDO localUpload(MultipartFile param) throws ServiceException, IOException {
		// 校验文件
		if (param.isEmpty() || Objects.isNull(param)) {
			throw new ServiceException("上传文件为空");
		}
		
		// 获取文件扩展名并验证
		String extension = LocalFileUtil.getFileExtension(param);
		if (!LocalFileUtil.isValidVideoFormat(extension)) {
			throw new ServiceException("不支持的视频格式，请上传mp4、avi、mov、wmv、flv或mkv格式的视频");
		}
		
		// 生成存储路径和文件名
		byte[] bytes = param.getBytes();
		String filePath = LocalFileUtil.generateVideoPath();
		String fileName = LocalFileUtil.generateFileName(param);
		Path path = Paths.get(filePath + "/");
		if (!Files.isWritable(path)) {
			Files.createDirectories(path);
		}
		
		// 生成视频ID
		long videoId = new SnowflakeIdWorker(WORKER_ID).nextId();
		
		// 生成缩略图路径和文件名
		String thumbnailPath = LocalFileUtil.generateThumbnailPath();
		String thumbnailName = LocalFileUtil.generateThumbnailName(fileName);
		
		// 构建视频对象
		VideoDO videoDO = VideoDO.builder()
				.id(videoId)
				.title(param.getOriginalFilename())
				.videoUrl(filePath + "/" + fileName)
				.uploadTime(LocalDateTime.now())
				.extension(extension)
				.status(0) // 0=待处理，1=处理中，2=处理完成，-1=处理失败
				.userId(UserHolder.getUser().getId())
				.thumbnail(thumbnailPath + "/" + thumbnailName)
				.build();
		
		// 上传原始文件
		LocalFileUtil.getFileByBytes(bytes, filePath, fileName);
		
		// 保存到数据库
		videoMapper.insert(videoDO);
		
		// 返回保存的视频对象
		return videoDO;
	}
	
	@Override
	public void processVideo(Long videoId, Long placeId) throws ServiceException {
		// 查询视频信息
		VideoDO videoDO = videoMapper.selectById(videoId);
		if (videoDO == null) {
			throw new ServiceException("视频不存在: " + videoId);
		}
		
		// 检查视频状态
		if (videoDO.getStatus() != 0) {
			throw new ServiceException("视频已经在处理或已处理完成");
		}
		
		// 检查用户权限
		if (!videoDO.getUserId().equals(UserHolder.getUser().getId())) {
			throw new ServiceException("无权限处理该视频");
		}
		
		// 更新地点ID
		videoDO.setPlaceId(placeId);
		videoMapper.updateById(videoDO);
		
		// 异步处理视频
		processVideoAsync(videoId);
	}
	
	/**
	 * 异步处理视频
	 * @param videoId 视频ID
	 */
	@Async
	public void processVideoAsync(long videoId) {
		try {
			// 查询视频信息
			VideoDO videoDO = videoMapper.selectById(videoId);
			if (videoDO == null) {
				log.error("视频不存在: {}", videoId);
				return;
			}
			
			// 更新视频状态为处理中
			videoDO.setStatus(1);
			videoMapper.updateById(videoDO);
			
			// 原始视频路径
			String videoPath = videoDO.getVideoUrl();
			
			// 步骤1：使用FFmpeg处理视频：消音、压缩到640分辨率、转为AVI格式（覆盖原视频）
			boolean processSuccess = videoProcessUtil.processVideo(videoPath);
			if (!processSuccess) {
				log.error("视频FFmpeg处理失败: {}", videoId);
				videoDO.setStatus(-1); // 处理失败
				videoMapper.updateById(videoDO);
				return;
			}
			
			// 计算处理后视频的路径(results/文件名)
			String processedDir = videoPath.substring(0, videoPath.lastIndexOf("/")) + "/results";
			String processedFile = new File(videoPath).getName();
			String processedPath = processedDir + "/" + processedFile;
			
			// 步骤2：使用YOLOv8模型检测火灾和烟雾（覆盖原视频）
			Map<String, Object> detectionResults = videoProcessUtil.detectFireAndSmoke(videoPath);
			
			// 保存处理后的视频路径
			File processedVideoFile = new File(processedPath);
			if (processedVideoFile.exists()) {
				videoDO.setProcessedUrl(processedPath);
				log.info("设置处理后视频路径: {}", processedPath);
			} else {
				// 如果处理后的视频不存在，使用原始视频路径
				videoDO.setProcessedUrl(videoPath);
				log.warn("处理后视频不存在，使用原始路径: {}", videoPath);
			}
			
			// 步骤3：生成缩略图
			boolean thumbnailSuccess = videoProcessUtil.generateThumbnail(videoPath, videoDO.getThumbnail());
			if (!thumbnailSuccess) {
				log.error("缩略图生成失败: {}", videoId);
				// 缩略图失败不影响后续处理
			}
			
			// 更新视频信息
			Double fireProbability = (Double) detectionResults.getOrDefault("FireDO", 0.0);
			Double smokeProbability = (Double) detectionResults.getOrDefault("Smoke", 0.0);
			Integer dTime = (Integer) detectionResults.getOrDefault("Duration", 0);
			
			videoDO.setFireProbability(fireProbability);
			videoDO.setSmokeProbability(smokeProbability);
			videoDO.setDTime(dTime);
			videoDO.setProcessTime(LocalDateTime.now());
			videoDO.setStatus(2); // 处理完成
			
			// 保存结果
			videoMapper.updateById(videoDO);
			
			log.info("视频处理完成: {}, 火灾概率: {}, 烟雾概率: {}, 视频时长: {}秒", 
					videoId, fireProbability, smokeProbability, dTime);
			
			// 同步数据到火灾记录表
			boolean syncSuccess = fireService.syncFromVideo(videoDO);
			if (syncSuccess) {
				log.info("视频数据已同步到火灾记录表: {}", videoId);
			} else {
				log.error("视频数据同步到火灾记录表失败: {}", videoId);
			}
			
		} catch (Exception e) {
			log.error("视频处理异常: " + videoId, e);
			// 更新视频状态为处理失败
			VideoDO videoDO = videoMapper.selectById(videoId);
			if (videoDO != null) {
				videoDO.setStatus(-1);
				videoMapper.updateById(videoDO);
			}
		}
	}
}




