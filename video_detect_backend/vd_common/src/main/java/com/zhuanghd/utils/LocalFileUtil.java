package com.zhuanghd.utils;

import cn.hutool.core.util.RandomUtil;
import com.zhuanghd.exception.BaseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;

/**
 * Author: zhuanghongdong Date: 2024/12/13 Description: 文件上传到本地工具类
 */

public class LocalFileUtil {
	public static String UPLOAD_FOLDER = "E:/vddt";
	public static String THUMBNAIL_FOLDER = "E:/vddt/thumbnails";
	public static String VIDEO_FOLDER = "E:/vddt/video";
	public static String MODEL_PATH = "D:/code/java_project/video-detect/video_detect_yolo/ultralytics-main/best12.pt";
//	public static String MODEL_PATH = "E:/Graduation_project/7.program/video-detect/video_detect_yolo/ultralytics-main/best8.pt";

	// 检查目录是否存在，不存在则创建
	public static void ensureDirectoryExists(String directoryPath) {
		File directory = new File(directoryPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	public static String generateFilePath() {
		String userid = String.valueOf(UserHolder.getUser().getId());
		String year = String.valueOf(LocalDateTime.now().getYear());
		String month = String.valueOf(LocalDateTime.now().getMonth().getValue());
		return UPLOAD_FOLDER + "/" + userid + "/" + year + "/" + month;
	}
	
	public static String generateVideoPath() {
		String userid = String.valueOf(UserHolder.getUser().getId());
		String year = String.valueOf(LocalDateTime.now().getYear());
		String month = String.valueOf(LocalDateTime.now().getMonth().getValue());
		String path = VIDEO_FOLDER + "/" + userid + "/" + year + "/" + month;
		ensureDirectoryExists(path);
		return path;
	}
	
	public static String generateThumbnailPath() {
		String userid = String.valueOf(UserHolder.getUser().getId());
		String year = String.valueOf(LocalDateTime.now().getYear());
		String month = String.valueOf(LocalDateTime.now().getMonth().getValue());
		String path = THUMBNAIL_FOLDER + "/" + userid + "/" + year + "/" + month;
		ensureDirectoryExists(path);
		return path;
	}

	public static String generateFileName(MultipartFile file) {
		String fileName = "VIDEO_" + RandomUtil.randomNumbers(11);
		String extension = getFileExtension(file);
		return fileName + extension;
	}
	
	public static String generateThumbnailName(String originalFileName) {
		return originalFileName.substring(0, originalFileName.lastIndexOf(".")) + "_thumbnail.jpg";
	}
	
	/**
	 * 根据视频ID获取视频访问路径
	 * @param videoId 视频ID
	 * @param videoUrl 视频存储路径
	 * @return HTTP访问路径
	 */
	public static String getVideoAccessPath(Long videoId, String videoUrl) {
		if (videoUrl == null || videoUrl.isEmpty()) {
			return null;
		}
		// 转换为HTTP访问路径
		return "http://localhost:8081/video/" + videoId;
	}

	/**
	 * 将文件路径转换为字节数组
	 *
	 * @param pathStr
	 * @return
	 */
	public static byte[] getBytesByFile(String pathStr) {
		File file = new File(pathStr);
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
			byte[] bytes = new byte[1024];
			int n;
			while ((n = fileInputStream.read(bytes)) != -1) {
				byteArrayOutputStream.write(bytes, 0, n);
			}
			fileInputStream.close();
			byteArrayOutputStream.close();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void getFileByBytes(byte[] bytes, String filePath, String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			File dir = new File(filePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			file = new File(filePath + File.separator + fileName);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获得上传文件扩展名
	 *
	 * @param file
	 * @return
	 */
	public static String getFileExtension(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
		if (originalFilename != null && originalFilename.contains(".")) {
			return originalFilename.substring(originalFilename.lastIndexOf("."));
		}
		throw new BaseException("文件类型不符合");
	}
	
	/**
	 * 校验视频格式
	 *
	 * @param extension
	 * @return
	 */
	public static boolean isValidVideoFormat(String extension) {
		if (extension == null) {
			return false;
		}
		extension = extension.toLowerCase();
		return extension.equals(".mp4") || extension.equals(".mov") || 
		       extension.equals(".avi") || extension.equals(".wmv") || 
		       extension.equals(".flv") || extension.equals(".mkv");
	}
}
