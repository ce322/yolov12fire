package com.zhuanghd.controller;

import com.zhuanghd.result.BaseFunction;
import com.zhuanghd.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController extends BaseFunction {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(MultipartFile file) {
        try {
            Map<String, Object> result = fileService.upload(file);
            System.out.println("上传返回结果：" + result);
            return returnResponse("上传成功", true, result);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @GetMapping("/query-address")
    public ResponseEntity<Map<String, Object>> queryAddress(@RequestParam Long id) {
        try{
            Map<String,Object> result = fileService.queryAddress(id);
            return returnResponse("获取地址成功", true, result);
        }catch (Exception e){
            return returnResponse(e.getMessage(), false);
        }

    }

    @GetMapping("/query-thumb-address")
    public ResponseEntity<Map<String, Object>> queryThumbAddress(@RequestParam Long id) {
        try{
            Map<String,Object> result = fileService.queryThumbAddress(id);
            return returnResponse("获取地址成功", true, result);
        }catch (Exception e){
            return returnResponse(e.getMessage(), false);
        }

    }
}
