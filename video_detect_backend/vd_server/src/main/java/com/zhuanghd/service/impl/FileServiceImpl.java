package com.zhuanghd.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.protobuf.ServiceException;
import com.zhuanghd.entity.FileDO;
import com.zhuanghd.mapper.FileMapper;
import com.zhuanghd.service.FileService;
import com.zhuanghd.utils.LocalFileUtil;
import com.zhuanghd.utils.SnowflakeIdWorker;
import com.zhuanghd.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static com.zhuanghd.constant.NormalConstant.WORKER_ID;

/**
 * @author 00103933
 * @description 针对表【vd_file】的数据库操作Service实现
 * @createDate 2025-03-19 17:09:05
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FileDO> implements FileService {

    @Autowired
    private FileMapper fileMapper;

    // 定义允许的文件扩展名
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png"));

    @Override
    public Map<String, Object> upload(MultipartFile file) throws ServiceException, IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new ServiceException("上传文件为空");
        }

        // 检查文件类型是否符合要求
        String fileExtension = LocalFileUtil.getFileExtension(file);
        if (!(fileExtension != null && ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase()))) {
            throw new ServiceException("文件类型不符合，仅支持 " + String.join(", ", ALLOWED_EXTENSIONS) + " 格式");
        }

        // 获取文件字节数据
        byte[] bytes = file.getBytes();

        // 生成文件存储路径
        String filePath = LocalFileUtil.generateFilePath();
        Path path = Paths.get(filePath);

        // 创建目录（如果不存在）
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        // 生成文件唯一ID
        long id = new SnowflakeIdWorker(WORKER_ID).nextId();

        // 构建文件对象并保存到数据库
        FileDO fileDO = new FileDO();
        fileDO.setId(id);
        fileDO.setUserId(UserHolder.getUser().getId());
        fileDO.setFileName(id + fileExtension);
        fileDO.setUrl(filePath);
        fileDO.setExtension(fileExtension);
        fileDO.setUploadTime(LocalDateTime.now());
        fileMapper.insert(fileDO);

        // 将文件保存到本地
        LocalFileUtil.getFileByBytes(bytes, filePath, id + fileExtension);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        return result;
    }

    @Override
    public Map<String, Object> queryAddress(Long id) throws ServiceException {
        FileDO fileDO = this.getById(id);

        if (fileDO == null) {
            throw new ServiceException("不存在该文件");
        }

        // 判断用户是否存在该图片
        if (!fileDO.getUserId().equals(UserHolder.getUser().getId())) {
            throw new ServiceException("资源无效");
        }

        HashMap<String, Object> result = new HashMap<>();
        String localPath = fileDO.getUrl();
        String replace = StrUtil.replace(localPath, "E:/vddt", "http://localhost:8081/upload");
        result.put("url", replace +"/"+ fileDO.getFileName());
        return result;
    }

    @Override
    public Map<String, Object> queryThumbAddress(Long id) throws ServiceException {
        FileDO fileDO = this.getById(id);

        if (fileDO == null) {
            throw new ServiceException("不存在该文件");
        }

        // 判断用户是否存在该图片
        if (!fileDO.getUserId().equals(UserHolder.getUser().getId())) {
            throw new ServiceException("资源无效");
        }

        HashMap<String, Object> result = new HashMap<>();
        String localPath = fileDO.getUrl();
        String replace = StrUtil.replace(localPath, "E:/vddt_thumbnails", "http://localhost:8081/thumbnails");
        result.put("url", replace +"/"+ fileDO.getFileName());
        return result;
    }
}





