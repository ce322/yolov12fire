package com.zhuanghd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.google.protobuf.ServiceException;
import com.zhuanghd.entity.FileDO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
* @author 00103933
* @description 针对表【vd_file】的数据库操作Service
* @createDate 2025-03-19 17:09:05
*/
public interface FileService extends IService<FileDO> {

    Map<String, Object> upload(MultipartFile file) throws ServiceException, IOException;

    Map<String, Object> queryAddress(Long id) throws ServiceException;

    Map<String, Object> queryThumbAddress(Long id) throws ServiceException;
}
