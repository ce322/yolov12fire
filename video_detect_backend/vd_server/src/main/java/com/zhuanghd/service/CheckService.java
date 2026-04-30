package com.zhuanghd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanghd.check.request.CheckRequest;
import com.zhuanghd.entity.CheckDO;

import java.util.Map;

/**
 * @author 00103933
 * @description 针对表【vd_check】的数据库操作Service
 * @createDate 2025-03-17 16:55:57
 */
public interface CheckService extends IService<CheckDO> {

    void add(CheckRequest param);

    Map<String, Object> detail(Long id);

    void delete(Long id);

    void modify(CheckRequest param, Long id);

    Map<String, Object> getList(int current, int size, Map<String, Object> param);
}
