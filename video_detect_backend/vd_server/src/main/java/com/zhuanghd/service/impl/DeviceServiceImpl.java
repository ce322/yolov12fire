package com.zhuanghd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.protobuf.ServiceException;
import com.zhuanghd.device.request.DeviceRequest;
import com.zhuanghd.entity.PlaceDO;
import com.zhuanghd.mapper.DeviceMapper;
import com.zhuanghd.entity.DeviceDO;
import com.zhuanghd.mapper.PlaceMapper;
import com.zhuanghd.service.DeviceService;
import com.zhuanghd.utils.SnowflakeIdWorker;
import com.zhuanghd.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zhuanghd.constant.NormalConstant.WORKER_ID;

/**
 * @author 00103933
 * @description 针对表【vd_device】的数据库操作Service实现
 * @createDate 2025-03-17 08:17:36
 */
@Service
@Slf4j
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, DeviceDO>
        implements DeviceService {

    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private PlaceMapper placeMapper;

    @Override
    public Map<String, Object> detail(Long id) throws ServiceException {
        DeviceDO deviceDO = deviceMapper.selectById(id);
        if (deviceDO == null) {
            throw new ServiceException("不存在该设备");
        }
        if (!deviceDO.getUserId().equals(UserHolder.getUser().getId()) || deviceDO.getDeleteFlag() == 1) {
            throw new ServiceException("访问资源无效");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("object", deviceDO);
        return result;
    }

    @Override
    public void add(DeviceRequest param) throws ServiceException {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
//        查看用户地址是否存在和有效
        LambdaQueryWrapper<PlaceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlaceDO::getUserId, userId);
        queryWrapper.eq(PlaceDO::getStatus, 1);
        queryWrapper.eq(PlaceDO::getId, param.getPlaceId());
        if (placeMapper.selectCount(queryWrapper) != 1) {
            throw new ServiceException("地址资源无效");
        }
        DeviceDO deviceDO = new DeviceDO();
        BeanUtils.copyProperties(param, deviceDO);
        deviceDO.setId(new SnowflakeIdWorker(WORKER_ID).nextId());
        deviceDO.setUserId(userId);
        deviceDO.setCreateTime(now);
        deviceDO.setUpdateTime(now);
        deviceDO.setStatus(1);
        deviceDO.setDeleteFlag(0);
        this.save(deviceDO);
    }

    @Override
    public void delete(Long id) throws ServiceException {
        Long userId = UserHolder.getUser().getId();

        DeviceDO deviceDO = new DeviceDO();
        deviceDO.setDeleteFlag(1);
        deviceDO.setUpdateTime(LocalDateTime.now());

        LambdaUpdateWrapper<DeviceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DeviceDO::getId, id).eq(DeviceDO::getUserId, userId);

        int update = deviceMapper.update(deviceDO, updateWrapper);
        if (update != 1) {
            throw new ServiceException("删除资源无效");
        }
    }

    @Override
    public void modify(Long id, DeviceRequest param) throws ServiceException {
        Long userId = UserHolder.getUser().getId();
        DeviceDO deviceDO = new DeviceDO();
        //判断place是否为该用户的
        if (!placeMapper.selectById(param.getPlaceId()).getUserId().equals(userId)) {
            throw new ServiceException("不存在该地点");
        }
        BeanUtils.copyProperties(param, deviceDO);
        if (param.getCheckTime() == null) {
            deviceDO.setCheckTime(LocalDateTime.of(1949,1,1,1,1));
        }
        deviceDO.setUpdateTime(LocalDateTime.now());
        LambdaUpdateWrapper<DeviceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DeviceDO::getUserId, userId).eq(DeviceDO::getId, id);
        int update = deviceMapper.update(deviceDO, updateWrapper);
        if (update != 1) {
            throw new ServiceException("更新资源无效");
        }
    }

    @Override
    public Map<String, Object> deviceList(int current, int pageSize, Map<String, Object> param) throws ServiceException {
        try {
            log.info("查询设备列表，参数: current={}, pageSize={}, param={}", current, pageSize, param);
            
            Page<DeviceDO> page = new Page<>(current, pageSize);
            LambdaQueryWrapper<DeviceDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeviceDO::getUserId, UserHolder.getUser().getId());
            queryWrapper.eq(DeviceDO::getDeleteFlag, 0);
            
            // 按地点筛选
            if (param.containsKey("placeId") && param.get("placeId") != null) {
                queryWrapper.eq(DeviceDO::getPlaceId, param.get("placeId"));
            }
            
            // 按负责人筛选
            if (param.containsKey("duty") && param.get("duty") != null) {
                queryWrapper.like(DeviceDO::getDuty, param.get("duty"));
            }
            
            // 按状态筛选
            if (param.containsKey("status") && param.get("status") != null) {
                queryWrapper.eq(DeviceDO::getStatus, param.get("status"));
            }
            
            // 按创建时间范围筛选
            if (param.containsKey("startTime") && param.get("startTime") != null) {
                String startTimeStr = (String) param.get("startTime");
                queryWrapper.ge(DeviceDO::getCreateTime, startTimeStr + " 00:00:00");
            }
            
            if (param.containsKey("endTime") && param.get("endTime") != null) {
                String endTimeStr = (String) param.get("endTime");
                queryWrapper.le(DeviceDO::getCreateTime, endTimeStr + " 23:59:59");
            }
            
            // 确保按创建时间倒序排列
            queryWrapper.orderByDesc(DeviceDO::getCreateTime);
            
            log.info("执行设备查询");
            Page<DeviceDO> deviceDOS = this.page(page, queryWrapper);
            log.info("设备查询完成，总记录数: {}", deviceDOS.getTotal());
            
            return BeanUtil.beanToMap(deviceDOS);
        } catch (Exception e) {
            log.error("设备列表查询失败", e);
            throw new ServiceException("设备列表查询失败: " + e.getMessage());
        }
    }
}




