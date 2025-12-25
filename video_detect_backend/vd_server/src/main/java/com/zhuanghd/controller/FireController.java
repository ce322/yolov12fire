package com.zhuanghd.controller;

import com.zhuanghd.entity.FireDO;
import com.zhuanghd.entity.VideoDO;
import com.zhuanghd.fire.request.FireQueryRequest;
import com.zhuanghd.fire.request.FireRequest;
import com.zhuanghd.result.BaseFunction;
import com.zhuanghd.service.FireService;
import com.zhuanghd.service.VideoService;
import com.zhuanghd.utils.LocalFileUtil;
import com.zhuanghd.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fire")
@Api(tags = "火灾记录相关接口")
@Slf4j
public class FireController extends BaseFunction {

    @Autowired
    private FireService fireService;
    
    @Autowired
    private VideoService videoService;

    /**
     * 添加火灾记录
     */
    @PostMapping("/add")
    @ApiOperation("添加火灾记录")
    public ResponseEntity<Map<String, Object>> addFire(@Validated @RequestBody FireRequest param) {
        try {
            // 设置当前登录用户ID
            Long userId = UserHolder.getUser().getId();
            param.setUserId(userId);
            
            fireService.save(param);
            return returnResponse("添加火灾记录成功", true);
        } catch (Exception e) {
            log.error("添加火灾记录失败: {}", e.getMessage(), e);
            return returnResponse(e.getMessage(), false);
        }
    }

    /**
     * 获取火灾记录详情
     */
    @GetMapping("/id/{id}")
    @ApiOperation("获取火灾记录详情")
    public ResponseEntity<Map<String, Object>> getFireDetail(@PathVariable Long id) {
        try {
            FireDO fire = fireService.getById(id);
            
            // 校验当前用户是否有权限查看该记录
            if (fire != null) {
                Long currentUserId = UserHolder.getUser().getId();
                if (!currentUserId.equals(fire.getUserId())) {
                    return returnResponse("您没有权限查看此记录", false);
                }
            }
            
            return returnResponse("获取火灾记录详情成功", true, fire);
        } catch (Exception e) {
            log.error("获取火灾记录详情失败: {}", e.getMessage(), e);
            return returnResponse(e.getMessage(), false);
        }
    }

    /**
     * 更新火灾记录
     */
    @PutMapping("/update/{id}")
    @ApiOperation("更新火灾记录")
    public ResponseEntity<Map<String, Object>> updateFire(@PathVariable Long id,
            @Valid @RequestBody FireRequest param) {
        try {
            // 获取要更新的记录，验证所有权
            FireDO existingFire = fireService.getById(id);
            if (existingFire == null) {
                return returnResponse("火灾记录不存在", false);
            }
            
            // 校验当前用户是否有权限更新该记录
            Long currentUserId = UserHolder.getUser().getId();
            if (!currentUserId.equals(existingFire.getUserId())) {
                return returnResponse("您没有权限更新此记录", false);
            }
            
            // 设置用户ID，确保不会被更改
            param.setUserId(currentUserId);
            
            fireService.updateFire(id, param);
            return returnResponse("更新火灾记录成功", true);
        } catch (Exception e) {
            log.error("更新火灾记录失败: {}", e.getMessage(), e);
            return returnResponse(e.getMessage(), false);
        }
    }

    /**
     * 删除火灾记录
     */
    @DeleteMapping("/delete")
    @ApiOperation("删除火灾记录")
    public ResponseEntity<Map<String, Object>> deleteFire(@RequestParam(name = "id") Long id) {
        try {
            // 获取要删除的记录，验证所有权
            FireDO existingFire = fireService.getById(id);
            if (existingFire == null) {
                return returnResponse("火灾记录不存在", false);
            }
            
            // 校验当前用户是否有权限删除该记录
            Long currentUserId = UserHolder.getUser().getId();
            if (!currentUserId.equals(existingFire.getUserId())) {
                return returnResponse("您没有权限删除此记录", false);
            }
            
            fireService.removeById(id);
            return returnResponse("删除火灾记录成功", true);
        } catch (Exception e) {
            log.error("删除火灾记录失败: {}", e.getMessage(), e);
            return returnResponse(e.getMessage(), false);
        }
    }

    /**
     * 获取火灾记录列表
     */
    @GetMapping("/list")
    @ApiOperation("获取火灾记录列表")
    public ResponseEntity<Map<String, Object>> fireList(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Map<String, Object> param) {
        try {
            // 添加当前用户ID作为查询条件，只查询当前用户的记录
            Long userId = UserHolder.getUser().getId();
            param.put("userId", userId);
            
            Map<String, Object> result = fireService.fireList(current, size, param);
            return returnResponse("获取火灾记录列表成功", true, result);
        } catch (Exception e) {
            log.error("获取火灾记录列表失败: {}", e.getMessage(), e);
            return returnResponse(e.getMessage(), false);
        }
    }

    /**
     * 条件查询火灾记录
     */
    @PostMapping("/query")
    @ApiOperation("条件查询火灾记录")
    public ResponseEntity<Map<String, Object>> queryFireRecords(@RequestBody FireQueryRequest queryParam) {
        try {
            // 添加当前用户ID作为查询条件，只查询当前用户的记录
            Long userId = UserHolder.getUser().getId();
            queryParam.setUserId(userId);
            
            Map<String, Object> result = fireService.queryFireRecords(queryParam);
            return returnResponse("查询火灾记录成功", true, result);
        } catch (Exception e) {
            log.error("查询火灾记录失败: {}", e.getMessage(), e);
            return returnResponse(e.getMessage(), false);
        }
    }
    
    /**
     * 根据火灾记录ID获取对应的视频信息
     */
    @GetMapping("/video/{fireId}")
    @ApiOperation("获取火灾记录对应的视频信息")
    public ResponseEntity<Map<String, Object>> getFireVideo(@PathVariable Long fireId) {
        try {
            // 获取火灾记录，验证所有权
            FireDO existingFire = fireService.getById(fireId);
            if (existingFire == null) {
                return returnResponse("火灾记录不存在", false);
            }
            
            // 校验当前用户是否有权限查看该记录的视频
            Long currentUserId = UserHolder.getUser().getId();
            if (!currentUserId.equals(existingFire.getUserId())) {
                return returnResponse("您没有权限查看此记录的视频", false);
            }
            
            Map<String, Object> result = fireService.getFireVideo(fireId);
            
            if ((boolean) result.get("success")) {
                return returnResponse("获取视频信息成功", true, result.get("data"));
            } else {
                return returnResponse((String) result.get("message"), false);
            }
        } catch (Exception e) {
            log.error("获取视频信息失败: {}", e.getMessage(), e);
            return returnResponse("获取视频信息失败: " + e.getMessage(), false);
        }
    }
    
    /**
     * 根据火灾记录ID获取对应的视频文件
     */
    @GetMapping("/video/file/{fireId}")
    @ApiOperation("获取火灾记录对应的视频文件")
    public ResponseEntity<Resource> getFireVideoFile(@PathVariable Long fireId) {
        try {
            // 获取火灾记录，验证所有权
            FireDO existingFire = fireService.getById(fireId);
            if (existingFire == null) {
                throw new RuntimeException("火灾记录不存在");
            }
            
            // 校验当前用户是否有权限查看该记录的视频文件
            Long currentUserId = UserHolder.getUser().getId();
            if (!currentUserId.equals(existingFire.getUserId())) {
                throw new RuntimeException("您没有权限查看此记录的视频文件");
            }
            
            // 获取视频文件资源信息
            Map<String, Object> resourceInfo = fireService.getFireVideoResource(fireId);
            
            if (!(boolean) resourceInfo.get("success")) {
                throw new RuntimeException((String) resourceInfo.get("message"));
            }
            
            Map<String, Object> data = (Map<String, Object>) resourceInfo.get("data");
            String filePath = (String) data.get("filePath");
            String contentType = (String) data.get("contentType");
            String fileName = (String) data.get("fileName");
            
            // 构建文件资源
            Resource resource = new FileSystemResource(new File(filePath));
            
            // 创建返回响应
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("获取视频文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取视频文件失败: " + e.getMessage());
        }
    }
} 