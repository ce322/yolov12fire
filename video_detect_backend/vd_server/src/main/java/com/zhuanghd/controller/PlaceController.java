package com.zhuanghd.controller;

import com.zhuanghd.place.request.PlaceList;
import com.zhuanghd.place.request.PlaceRequest;
import com.zhuanghd.result.BaseFunction;
import com.zhuanghd.service.PlaceService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/place")
@Api(tags = "地址相关接口")
@Slf4j
public class PlaceController extends BaseFunction {

	@Autowired
	private PlaceService placeService;

	/**
	 * 添加地址
	 */
	@PostMapping("/add")
	public ResponseEntity<Map<String, Object>> addPlace(@Validated @RequestBody PlaceRequest param) {
		try {
			placeService.addPlace(param);
			return returnResponse("添加地址成功", true);
		} catch (Exception e) {
			return returnResponse(e.getMessage(), false);
		}
	}

	@GetMapping("/id/{id}")
	public ResponseEntity<Map<String, Object>> getDetail(@PathVariable Long id) {
		try {
			Map<String, Object> result = placeService.getDetail(id);
			return returnResponse("获得地址详情成功", true, result);
		} catch (Exception e) {
			return returnResponse(e.getMessage(), false);
		}
	}

	@DeleteMapping("/delete")
	public ResponseEntity<Map<String, Object>> deletePlace(@RequestParam(name = "id") Long id) {
		try {
			placeService.deletePlace(id);
			return returnResponse("删除地址成功", true);
		} catch (Exception e) {
			return returnResponse(e.getMessage(), false);
		}
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<Map<String, Object>> updatePlace(@PathVariable Long id,
			@RequestBody @Valid PlaceRequest param) {
		try {
			placeService.updatePlace(id, param);
			return returnResponse("地址更新成功", true);
		} catch (Exception e) {
			return returnResponse(e.getMessage(), false);
		}
	}

	@GetMapping("/list")
	public ResponseEntity<Map<String, Object>> placeList(@RequestParam(defaultValue = "1") int current
			, @RequestParam(defaultValue = "10") int size
			, @RequestParam Map<String,Object> param) {
		try {
			Map<String, Object> result = placeService.placeList(current,size,param);
			return returnResponse("获取地址列表成功", true, result);
		} catch (Exception e) {
			return returnResponse(e.getMessage(), false);
		}
	}

	@GetMapping("/get-place")
	public ResponseEntity<Map<String, Object>> getPlace() {
		try{
			List<Map<String,Object>> result = placeService.getPlace();
			return returnResponse("获取用户登记地点成功", true, result);
		}catch (Exception e){
			return returnResponse(e.getMessage(), false);
		}
	}
}
