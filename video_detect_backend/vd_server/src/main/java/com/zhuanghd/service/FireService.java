package com.zhuanghd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.google.protobuf.ServiceException;
import com.zhuanghd.entity.FireDO;
import com.zhuanghd.entity.VideoDO;
import com.zhuanghd.fire.request.FireQueryRequest;
import com.zhuanghd.fire.request.FireRequest;

import java.util.Map;

/**
* @author 00103933
* @description 针对表【vd_fire】的数据库操作Service
* @createDate 2025-03-25 11:19:27
*/
public interface FireService extends IService<FireDO> {
    
    /**
     * 保存火灾记录
     * @param param 火灾记录请求
     * @return 是否成功
     */
    boolean save(FireRequest param);
    
    /**
     * 更新火灾记录
     * @param id 记录ID
     * @param param 火灾记录请求
     * @return 是否成功
     */
    boolean updateFire(Long id, FireRequest param);
    
    /**
     * 获取火灾记录列表
     * @param current 当前页
     * @param size 每页大小
     * @param param 查询参数
     * @return 分页结果
     */
    Map<String, Object> fireList(int current, int size, Map<String, Object> param) throws ServiceException;
    
    /**
     * 条件查询火灾记录
     * @param queryParam 查询条件
     * @return 查询结果
     */
    Map<String, Object> queryFireRecords(FireQueryRequest queryParam);
    
    /**
     * 从视频数据同步到火灾记录
     * @param videoDO 视频数据对象
     * @return 是否成功
     */
    boolean syncFromVideo(VideoDO videoDO);

    /**
     * 获取火灾记录对应的视频信息
     * @param fireId 火灾记录ID
     * @return 视频信息
     */
    Map<String, Object> getFireVideo(Long fireId);

    /**
     * 获取火灾记录对应的视频文件资源
     * @param fireId 火灾记录ID
     * @return 视频文件资源信息
     */
    Map<String, Object> getFireVideoResource(Long fireId);
}
