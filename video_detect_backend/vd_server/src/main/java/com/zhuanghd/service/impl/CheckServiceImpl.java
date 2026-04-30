package com.zhuanghd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuanghd.check.request.CheckRequest;
import com.zhuanghd.entity.CheckDO;
import com.zhuanghd.service.CheckService;
import com.zhuanghd.mapper.CheckMapper;
import com.zhuanghd.utils.SnowflakeIdWorker;
import com.zhuanghd.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.zhuanghd.constant.NormalConstant.WORKER_ID;

/**
 * @author 00103933
 * @description 针对表【vd_check】的数据库操作Service实现
 * @createDate 2025-03-17 16:55:57
 */
@Service
@Slf4j
public class CheckServiceImpl extends ServiceImpl<CheckMapper, CheckDO>
        implements CheckService {

    @Override
    public void add(CheckRequest param) {
        CheckDO checkDO = new CheckDO();
        BeanUtils.copyProperties(param, checkDO);
        checkDO.setId(new SnowflakeIdWorker(WORKER_ID).nextId());
        checkDO.setUserId(UserHolder.getUser().getId());
        checkDO.setCreateTime(LocalDateTime.now());
        checkDO.setUpdateTime(LocalDateTime.now());
        checkDO.setDeleteFlag(0);
        this.save(checkDO);
    }

    @Override
    public Map<String, Object> detail(Long id) {
        Long userId = UserHolder.getUser().getId();
        LambdaQueryWrapper<CheckDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckDO::getUserId, userId);
        queryWrapper.eq(CheckDO::getId, id);
        CheckDO checkDO = this.getOne(queryWrapper);
        HashMap<String, Object> result = new HashMap<>();
        result.put("object",checkDO);
        return result;
    }

    @Override
    public void delete(Long id) {
        CheckDO checkDO = new CheckDO();
        checkDO.setDeleteFlag(1);
        Long userId = UserHolder.getUser().getId();
        LambdaUpdateWrapper<CheckDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CheckDO::getUserId, userId).eq(CheckDO::getId, id);
        this.update(checkDO, updateWrapper);
    }

    @Override
    public void modify(CheckRequest param, Long id) {
        CheckDO checkDO = new CheckDO();
        BeanUtils.copyProperties(param, checkDO);
        LambdaUpdateWrapper<CheckDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CheckDO::getUserId, UserHolder.getUser().getId()).eq(CheckDO::getId, id);
        this.update(checkDO, updateWrapper);
    }

    @Override
    public Map<String, Object> getList(int current, int size, Map<String, Object> param) {
        try {
            log.info("查询检查记录列表，参数: current={}, size={}, param={}", current, size, param);
            
            LambdaQueryWrapper<CheckDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CheckDO::getUserId, UserHolder.getUser().getId()).eq(CheckDO::getDeleteFlag,0);
            
            // 按责任人筛选
            if (param.containsKey("duty") && param.get("duty") != null) {
                queryWrapper.like(CheckDO::getDuty, param.get("duty"));
            }
            
            // 按评分筛选
            if (param.containsKey("score") && param.get("score") != null) {
                queryWrapper.eq(CheckDO::getScore, param.get("score"));
            }
            
            // 按地点筛选
            if (param.containsKey("place") && param.get("place") != null) {
                queryWrapper.eq(CheckDO::getPlace, param.get("place"));
            }
            
            // 按检查时间范围筛选
            if (param.containsKey("startTime") && param.get("startTime") != null) {
                String startTimeStr = (String) param.get("startTime");
                queryWrapper.ge(CheckDO::getTime, startTimeStr + " 00:00:00");
            }
            
            if (param.containsKey("endTime") && param.get("endTime") != null) {
                String endTimeStr = (String) param.get("endTime");
                queryWrapper.le(CheckDO::getTime, endTimeStr + " 23:59:59");
            }
            
            // 确保按创建时间倒序排序
            queryWrapper.orderByDesc(CheckDO::getCreateTime);
            
            log.info("执行检查记录查询");
            Page<CheckDO> page = new Page<>(current, size);
            Page<CheckDO> checkDOS = this.page(page, queryWrapper);
            log.info("检查记录查询完成，总记录数: {}", checkDOS.getTotal());
            
            return BeanUtil.beanToMap(checkDOS);
        } catch (Exception e) {
            log.error("检查记录列表查询失败", e);
            // 返回包含错误信息的Map，而不是抛出异常
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "检查记录列表查询失败: " + e.getMessage());
            return errorResult;
        }
    }
}




