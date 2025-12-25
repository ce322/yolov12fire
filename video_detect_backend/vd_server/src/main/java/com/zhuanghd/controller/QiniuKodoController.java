//package com.zhuanghd.controller;
//
//import com.zhuanghd.utils.QiniuKodoUtil;
//import io.swagger.annotations.Api;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//import java.io.UnsupportedEncodingException;
//
//@RestController
//@Api(tags = "qiniu")
//@RequestMapping("qiniukodo")
//public class QiniuKodoController {
//
//    @Resource
//    QiniuKodoUtil qiniuKodoUtil;
//
//
//    @RequestMapping("upload")
//    public void upload(String localFilePath) {
//        qiniuKodoUtil.upload(localFilePath);
//    }
//
//    @RequestMapping("listSpaceFiles")
//    public void listSpaceFiles() {
//        qiniuKodoUtil.listSpaceFiles();
//    }
//
//    @RequestMapping("getFileUrl")
//    public void getFileUrl(String fileName) {
//        try {
//            qiniuKodoUtil.getFileUrl(fileName);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @RequestMapping("deleteFile")
//    public void deleteFile(String[] fileList) {
//        qiniuKodoUtil.deleteFile(fileList);
//    }
//}
