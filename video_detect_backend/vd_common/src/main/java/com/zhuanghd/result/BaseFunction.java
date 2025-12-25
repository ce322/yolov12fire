package com.zhuanghd.result;

import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BaseFunction {
	/**
	 * 返回处理信息以及前端所需参数
	 *
	 * @param msg
	 * @param status
	 * @param data
	 * @return
	 */
	public ResponseEntity<Map<String, Object>> returnResponse(
			String msg, boolean status, Object data) {
		Map<String, Object> body = new HashMap<>();
		body.put("msg", msg);
		body.put("data", data);
		if (status) {
			body.put("status", status);
			body.put("code", 200);
		} else {
			body.put("status", status);
			body.put("code", 500);
		}

		return ResponseEntity.status(200).body(body);
	}

	/**
	 * 返回处理信息
	 *
	 * @param msg
	 * @param status
	 * @return
	 */
	public ResponseEntity<Map<String, Object>> returnResponse(String msg, boolean status) {
		Map<String, Object> body = new HashMap<>();
		body.put("msg", msg);
		if (status) {
			body.put("status", status);
			body.put("code", 200);
		} else {
			body.put("status", status);
			body.put("code", 500);
		}
		return ResponseEntity.status(200).body(body);
	}

	/**
	 * 自定义返回信息
	 *
	 * @param data
	 * @param status
	 * @return
	 */
	public ResponseEntity<Map<String, Object>> returnResponse(
			Map<String, Object> data, boolean status) {
		Map<String, Object> body = new HashMap<>();
		if (status) {
			body.put("status", status);
			body.put("code", 200);
		} else {
			body.put("status", status);
			body.put("code", 500);
		}
		Set<Entry<String, Object>> set = data.entrySet();
		Iterator<Entry<String, Object>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> me = iterator.next();
			body.put(me.getKey(), me.getValue());
		}
		ResponseEntity<Map<String, Object>> result = ResponseEntity.status(200).body(body);
		return result;
	}

	/**
	 * 把对象中的 String 类型的null字段，转换为空字符串
	 *
	 * @param <T>
	 * 		待转化对象类型
	 * @param cls
	 * 		待转化对象
	 * @return 转化好的对象
	 */
	public static <T> T noNullStringAttr(T cls) {
		Field[] fields = cls.getClass().getDeclaredFields();
		if (fields == null || fields.length == 0) {
			return cls;
		}
		for (Field field : fields) {
			if ("String".equals(field.getType().getSimpleName())) {
				field.setAccessible(true);
				try {
					Object value = field.get(cls);
					if (value == null) {
						field.set(cls, "");
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return cls;
	}
}
