package com.zhuanghd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.protobuf.ServiceException;
import com.zhuanghd.entity.PlaceDO;
import com.zhuanghd.mapper.PlaceMapper;
import com.zhuanghd.place.request.PlaceList;
import com.zhuanghd.place.request.PlaceRequest;
import com.zhuanghd.service.PlaceService;
import com.zhuanghd.utils.RegexUtils;
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
import java.util.stream.Collectors;

import static com.zhuanghd.constant.NormalConstant.WORKER_ID;

@Service
@Slf4j
public class PlaceServiceImpl extends ServiceImpl<PlaceMapper, PlaceDO> implements PlaceService {

    @Autowired
    private PlaceMapper placeMapper;

    @Override
    public void addPlace(PlaceRequest param) throws ServiceException {
        // 验证传入参数有用性
        if (RegexUtils.isPhoneInvalid(param.getDutyTel())) {
            throw new ServiceException("手机格式错误");
        }

        PlaceDO placeDO = PlaceDO.builder()
                .id(new SnowflakeIdWorker(WORKER_ID).nextId())
                .userId(UserHolder.getUser().getId())
                .name(param.getName())
                .address(param.getAddress())
                .pic(param.getPic())
                .duty(param.getDuty())
                .dutyTel(param.getDutyTel())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .status(1)
                .deleteFlag(0)
                .province(param.getProvince())
                .town(param.getTown())
                .area(param.getArea()).build();

        placeMapper.insert(placeDO);
    }

    @Override
    public Map<String, Object> getDetail(Long id) throws ServiceException {
        Map<String, Object> result = new HashMap<>();
        PlaceDO placeDO = placeMapper.selectById(id);
        // 判断资源拥有者是否对应
        if (!placeDO.getUserId().equals(UserHolder.getUser().getId())  || placeDO.getDeleteFlag() == 1) {
            throw new ServiceException("无效访问");
        }
        result.put("object", placeDO);
        return result;
    }

    @Override
    public void deletePlace(Long id) throws ServiceException {
        PlaceDO placeDO = placeMapper.selectById(id);
        // 判断资源拥有者是否对应
        if (!placeDO.getUserId().equals(UserHolder.getUser().getId())  || placeDO.getDeleteFlag() == 1) {
            throw new ServiceException("无效访问");
        }
        placeDO.setDeleteFlag(1);
        placeMapper.updateById(placeDO);
    }

    @Override
    public void updatePlace(Long id, PlaceRequest param) throws ServiceException {
        PlaceDO placeDO = placeMapper.selectById(id);
        // 判断资源拥有者是否对应
        if (!placeDO.getUserId().equals(UserHolder.getUser().getId())  || placeDO.getDeleteFlag() == 1) {
            throw new ServiceException("无效访问");
        }
        BeanUtils.copyProperties(param, placeDO);
        placeDO.setUpdateTime(LocalDateTime.now());
        placeMapper.updateById(placeDO);
    }

    @Override
    public Map<String, Object> placeList(int pageNum, int pageSize, Map<String, Object> param) {
        Long userId = UserHolder.getUser().getId();
        Page<PlaceDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PlaceDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(PlaceDO::getUserId, userId).eq(PlaceDO::getDeleteFlag, 0);
        // 添加筛选条件
        if (param.containsKey("area")) {
            if (param.get("area") != null) {
                lambdaQueryWrapper.like(PlaceDO::getArea, param.get("area"));
            }
        }
        if (param.containsKey("town")) {
            if (param.get("town") != null) {
                lambdaQueryWrapper.like(PlaceDO::getTown, param.get("town"));
            }
        }
        if (param.containsKey("duty")) {
            if (param.get("duty") != null) {
                lambdaQueryWrapper.like(PlaceDO::getDuty, param.get("duty"));
            }
        }
        if (param.containsKey("province")) {
            if (param.get("province") != null) {
                lambdaQueryWrapper.like(PlaceDO::getProvince, param.get("province"));
            }
        }
        if (param.containsKey("status")) {
            if (param.get("status") != null) {
                lambdaQueryWrapper.eq(PlaceDO::getStatus, param.get("status"));
            }
        }
        lambdaQueryWrapper.orderByDesc(PlaceDO::getCreateTime);

        Page<PlaceDO> placeDOPage = this.page(page, lambdaQueryWrapper);
        return BeanUtil.beanToMap(placeDOPage);
    }

    @Override
    public List<Map<String,Object>> getPlace() {
        LambdaQueryWrapper<PlaceDO> queryWrapper = new LambdaQueryWrapper<PlaceDO>().eq(PlaceDO::getUserId, UserHolder.getUser().getId())
                .eq(PlaceDO::getDeleteFlag, 0)
                .eq(PlaceDO::getStatus, 1);
        // 获得该用户的所有可用地点
        List<PlaceDO> placeDOS = this.list(queryWrapper);
        return placeDOS.stream().map(placeDO -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", placeDO.getId());
            map.put("name", placeDO.getName());
            return map;
        }).collect(Collectors.toList());
    }

}
