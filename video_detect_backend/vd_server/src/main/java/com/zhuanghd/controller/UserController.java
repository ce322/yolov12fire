package com.zhuanghd.controller;

import com.zhuanghd.dto.PasswordDTO;
import com.zhuanghd.result.BaseFunction;
import com.zhuanghd.user.request.UserLogin;
import com.zhuanghd.service.UserService;
import com.zhuanghd.user.request.UserRegister;
import com.zhuanghd.user.request.UserUpdateInfo;
import com.zhuanghd.user.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
@Slf4j
public class UserController extends BaseFunction {

    @Autowired
    private UserService userService;

    @ApiOperation("用户登录接口")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Validated @RequestBody UserLogin param) {
        try {
            UserLoginVO result = userService.login(param);
            return returnResponse("登录成功", true, result);
        } catch (Exception e) {
            return returnResponse("登录认证失败", false);
        }
    }

    @ApiOperation("用户登出接口")
    @DeleteMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("token") String token) {
        try {
            userService.logout(token);
            return returnResponse("用户登出成功", true);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @ApiOperation("验证码发送接口")
    @PostMapping("/sendCaptcha")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestParam(value = "phone") String phone) {
        try {
            userService.sendCaptcha(phone);
            return returnResponse("发送验证码成功", true);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @ApiOperation("用户用验证码登录接口")
    @PostMapping("/login2")
    public ResponseEntity<Map<String, Object>> loginByCaptcha(@Valid @RequestBody UserLogin param) {
        try {
            UserLoginVO result = userService.login(param);
            return returnResponse("用户登录成功", true, result);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @ApiOperation("用户注册接口")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Validated @RequestBody UserRegister param) {
        try {
            userService.register(param);
            return returnResponse("用户注册成功", true);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @ApiOperation("修改密码接口")
    @PutMapping("/edit-password")
    public ResponseEntity<Map<String, Object>> editPassword(@RequestBody PasswordDTO passwordDTO) {
        try {
            userService.editPassword(passwordDTO);
            return returnResponse("密码更新成功", true);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    //TODO 忘记密码

    @ApiOperation("获取用户详情接口")
    @GetMapping("/query-detail")
    public ResponseEntity<Map<String, Object>> queryDetail() {
        try {
            Map<String, Object> result = userService.queryDetail();
            return returnResponse("获取用户基本信息成功", true, result);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @ApiOperation("更新用户信息接口")
    @PutMapping("/update-info")
    public ResponseEntity<Map<String, Object>> updateUserInfo(@Validated @RequestBody UserUpdateInfo userUpdateInfo) {
        try {
            userService.updateUserInfo(userUpdateInfo);
            return returnResponse("更新用户信息成功", true);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }

    @ApiOperation("上传用户头像接口")
    @PostMapping("/upload-avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = userService.uploadAvatar(file);
            return returnResponse("上传头像成功", true, result);
        } catch (Exception e) {
            return returnResponse(e.getMessage(), false);
        }
    }
}

