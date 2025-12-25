package com.zhuanghd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.google.protobuf.ServiceException;
import com.zhuanghd.entity.PlaceDO;
import com.zhuanghd.place.request.PlaceList;
import com.zhuanghd.place.request.PlaceRequest;

import java.util.List;
import java.util.Map;

public interface PlaceService extends IService<PlaceDO> {
	void addPlace(PlaceRequest param) throws ServiceException;

	Map<String, Object> getDetail(Long id) throws ServiceException;

	void deletePlace(Long id) throws ServiceException;

	void updatePlace(Long id, PlaceRequest param) throws ServiceException;

	Map<String, Object> placeList(int pageNum, int pageSize,Map<String,Object> param);

	List<Map<String,Object>> getPlace();
}
