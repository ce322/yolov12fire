package com.zhuanghd.service;

import com.google.protobuf.ServiceException;
import com.zhuanghd.dto.PasswordDTO;
import com.zhuanghd.entity.UserDO;
import com.zhuanghd.user.request.UserLogin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanghd.result.Result;
import com.zhuanghd.user.request.UserRegister;
import com.zhuanghd.user.request.UserUpdateInfo;
import com.zhuanghd.user.vo.UserLoginVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author 庄泓东
 * @description 针对表【vd_user】的数据库操作Service
 * @createDate 2024-10-09 16:11:52
 */
public interface UserService extends IService<UserDO> {
	UserLoginVO login(UserLogin param) throws ServiceException;

	void register(UserRegister param) throws ServiceException;

	void sendCaptcha(String phone) throws ServiceException;

	void editPassword(PasswordDTO passwordDTO) throws ServiceException;

	void logout(String token) throws ServiceException;

    Map<String, Object> queryDetail() throws ServiceException;
    
    void updateUserInfo(UserUpdateInfo userUpdateInfo) throws ServiceException;
    
    Map<String, Object> uploadAvatar(MultipartFile file) throws ServiceException;
}
