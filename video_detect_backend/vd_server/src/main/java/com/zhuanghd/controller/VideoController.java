package com.zhuanghd.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanghd.entity.VideoDO;
import com.zhuanghd.exception.BaseException;
import com.zhuanghd.result.BaseFunction;
import com.zhuanghd.service.VideoService;
import com.zhuanghd.utils.LocalFileUtil;
import com.zhuanghd.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@Api(tags = "视频相关接口")
@Slf4j
@RequestMapping("/video")
public class VideoController extends BaseFunction {

	@Autowired
	private VideoService videoService;

	@ApiOperation("上传视频")
	@PostMapping("/upload")
	public ResponseEntity<Map<String, Object>> localUpload(@RequestParam("file") MultipartFile param) {
		try {
			// 修改为返回上传的视频信息
			VideoDO video = videoService.localUpload(param);
			
			// 构建返回结果
			Map<String, Object> result = new HashMap<>();
			result.put("id", video.getId());
			result.put("title", video.getTitle());
			result.put("uploadTime", video.getUploadTime());
			
			return returnResponse("上传成功", true, result);
		} catch (Exception e) {
			log.error("视频上传失败", e);
			return returnResponse(e.getMessage(), false);
		}
	}
	
	@ApiOperation("处理视频")
	@PostMapping("/process")
	public ResponseEntity<Map<String, Object>> processVideo(
			@RequestParam("videoId") Long videoId,
			@RequestParam("placeId") Long placeId) {
		try {
			// 调用服务处理视频
			videoService.processVideo(videoId, placeId);
			return returnResponse("视频处理请求已发送", true);
		} catch (Exception e) {
			log.error("视频处理请求失败", e);
			return returnResponse(e.getMessage(), false);
		}
	}
	
	@ApiOperation("获取视频列表")
	@GetMapping("/list")
	public ResponseEntity<Map<String, Object>> getVideoList(
			@RequestParam(value = "current", defaultValue = "1") int current,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		try {
			// 创建分页对象
			Page<VideoDO> page = new Page<>(current, size);
			
			// 构建查询条件
			LambdaQueryWrapper<VideoDO> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(VideoDO::getUserId, UserHolder.getUser().getId())
					    .orderByDesc(VideoDO::getUploadTime);
			
			// 执行分页查询
			Page<VideoDO> videoPage = videoService.page(page, queryWrapper);
			
			// 构建返回结果
			Map<String, Object> result = new HashMap<>();
			result.put("records", videoPage.getRecords());
			result.put("total", videoPage.getTotal());
			result.put("current", videoPage.getCurrent());
			result.put("size", videoPage.getSize());
			
			return returnResponse("获取视频列表成功", true, result);
		} catch (Exception e) {
			log.error("获取视频列表失败", e);
			return returnResponse(e.getMessage(), false);
		}
	}
	
	@ApiOperation("获取视频详情")
	@GetMapping("/detail")
	public ResponseEntity<Map<String, Object>> getVideoDetail(@RequestParam("id") Long id) {
		try {
			VideoDO videoDO = videoService.getById(id);
			if (videoDO == null) {
				return returnResponse("视频不存在", false);
			}
			
			// 检查是否是当前用户的视频
			if (!videoDO.getUserId().equals(UserHolder.getUser().getId())) {
				return returnResponse("无权限访问该视频", false);
			}
			
			// 转换为HTTP访问路径
			Map<String, Object> videoInfo = new HashMap<>();
			videoInfo.put("id", videoDO.getId());
			videoInfo.put("title", videoDO.getTitle());
			videoInfo.put("uploadTime", videoDO.getUploadTime());
			videoInfo.put("status", videoDO.getStatus());
			videoInfo.put("processTime", videoDO.getProcessTime());
			videoInfo.put("fireProbability", videoDO.getFireProbability());
			videoInfo.put("smokeProbability", videoDO.getSmokeProbability());
			videoInfo.put("dTime", videoDO.getDTime());
			videoInfo.put("videoUrl", LocalFileUtil.getVideoAccessPath(videoDO.getId(), videoDO.getVideoUrl()));
			
			return returnResponse("获取视频详情成功", true, videoInfo);
		} catch (Exception e) {
			log.error("获取视频详情失败", e);
			return returnResponse(e.getMessage(), false);
		}
	}
	
	@ApiOperation("获取视频缩略图地址")
	@GetMapping("/thumbnail")
	public ResponseEntity<Map<String, Object>> getThumbnail(@RequestParam("id") Long id) {
		try {
			VideoDO videoDO = videoService.getById(id);
			if (videoDO == null) {
				return returnResponse("视频不存在", false);
			}
			
			// 检查是否是当前用户的视频
			if (!videoDO.getUserId().equals(UserHolder.getUser().getId())) {
				return returnResponse("无权限访问该视频", false);
			}
			
			// 获取缩略图路径
			String thumbnailPath = videoDO.getThumbnail();
			if (thumbnailPath == null || thumbnailPath.isEmpty()) {
				return returnResponse("缩略图不存在", false);
			}
			
			// 转换为HTTP访问路径
			String httpPath = thumbnailPath.replace("E:/vddt_thumbnails", "http://localhost:8081/thumbnails");
			
			Map<String, Object> result = new HashMap<>();
			result.put("url", httpPath);
			
			return returnResponse("获取缩略图地址成功", true, result);
		} catch (Exception e) {
			log.error("获取缩略图地址失败", e);
			return returnResponse(e.getMessage(), false);
		}
	}
	
	@ApiOperation("根据ID获取视频文件")
	@GetMapping("/{id}")
	public ResponseEntity<Resource> getVideoFile(@PathVariable Long id) {
		try {
			VideoDO videoDO = videoService.getById(id);
			if (videoDO == null) {
				throw new BaseException("视频不存在");
			}
			
			// 获取视频文件路径
			String videoPath = videoDO.getVideoUrl();
			if (videoPath == null || videoPath.isEmpty()) {
				throw new BaseException("视频文件不存在");
			}
			
			// 构建文件资源
			File file = new File(videoPath);
			if (!file.exists()) {
				throw new BaseException("视频文件不存在于存储位置");
			}
			
			// 确定内容类型
			String contentType;
			if (videoDO.getExtension() != null) {
				switch (videoDO.getExtension().toLowerCase()) {
					case ".mp4":
						contentType = "video/mp4";
						break;
					case ".avi":
						contentType = "video/x-msvideo";
						break;
					case ".mov":
						contentType = "video/quicktime";
						break;
					case ".wmv":
						contentType = "video/x-ms-wmv";
						break;
					case ".flv":
						contentType = "video/x-flv";
						break;
					case ".mkv":
						contentType = "video/x-matroska";
						break;
					default:
						contentType = "application/octet-stream";
				}
			} else {
				contentType = "application/octet-stream";
			}
			
			// 创建返回响应
			Resource resource = new FileSystemResource(file);
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
					.body(resource);
			
		} catch (BaseException e) {
			log.error("获取视频文件失败: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("获取视频文件异常", e);
			throw new BaseException("获取视频文件异常: " + e.getMessage());
		}
	}
}
