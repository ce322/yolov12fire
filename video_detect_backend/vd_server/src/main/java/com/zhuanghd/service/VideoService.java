package com.zhuanghd.service;

import com.google.protobuf.ServiceException;
import com.zhuanghd.entity.VideoDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
* @author 庄泓东
* @description 针对表【vd_video】的数据库操作Service
* @createDate 2024-10-30 16:31:34
*/
public interface VideoService extends IService<VideoDO> {
	/**
	 * 上传视频到本地，但不进行处理
	 * @param param 视频文件
	 * @return 保存的视频对象
	 */
	VideoDO localUpload(MultipartFile param) throws ServiceException, IOException;
	
	/**
	 * 处理指定ID的视频
	 * @param videoId 视频ID
	 * @param placeId 地点ID
	 */
	void processVideo(Long videoId, Long placeId) throws ServiceException;
}
