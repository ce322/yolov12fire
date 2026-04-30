package com.zhuanghd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.google.protobuf.ServiceException;
import com.zhuanghd.device.request.DeviceRequest;
import com.zhuanghd.entity.DeviceDO;

import java.util.Map;

/**
 * @author 00103933
 * @description 针对表【vd_device】的数据库操作Service
 * @createDate 2025-03-17 08:17:36
 */
public interface DeviceService extends IService<DeviceDO> {

    Map<String, Object> detail(Long id) throws ServiceException;

    void add(DeviceRequest param) throws ServiceException;

    void delete(Long id) throws ServiceException;

    void modify(Long id, DeviceRequest param) throws ServiceException;

    Map<String, Object> deviceList(int current, int pageSize, Map<String, Object> param) throws ServiceException;
}
