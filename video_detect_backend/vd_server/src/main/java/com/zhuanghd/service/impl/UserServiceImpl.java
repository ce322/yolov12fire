package com.zhuanghd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.protobuf.ServiceException;
import com.zhuanghd.authentication.MobileCaptchaAuthenticationToken;
import com.zhuanghd.dto.PasswordDTO;
import com.zhuanghd.entity.UserDO;
import com.zhuanghd.user.bo.UserBO;
import com.zhuanghd.user.request.UserLogin;
import com.zhuanghd.entity.LoginUserDetail;
import com.zhuanghd.result.Result;
import com.zhuanghd.service.UserService;
import com.zhuanghd.service.FileService;
import com.zhuanghd.mapper.UserMapper;
import com.zhuanghd.user.request.UserRegister;
import com.zhuanghd.user.request.UserUpdateInfo;
import com.zhuanghd.utils.*;
import com.zhuanghd.user.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.zhuanghd.constant.JwtClaimsConstant.USER_ID;
import static com.zhuanghd.constant.MessageConstant.*;
import static com.zhuanghd.constant.NormalConstant.*;
import static com.zhuanghd.constant.RedisConstant.*;
import static com.zhuanghd.utils.PasswordUtil.*;

/**
 * @author 庄泓东
 * @description 针对表【vd_user】的数据库操作Service实现
 * @createDate 2024-10-09 16:11:52
 */

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private FileService fileService;

    /**
     * 用户登录业务
     *
     * @param param
     * @return
     */
    @Override
    public UserLoginVO login(UserLogin param) throws ServiceException {
        Authentication authenticationToken;
        // 封装Authentication对象
        if (param.getCaptcha() == null && param.getPassword() != null) {
            authenticationToken = new UsernamePasswordAuthenticationToken(param.getPhone(),
                    param.getPassword()); // 账号密码登录
        } else if (param.getPassword() == null && param.getCaptcha() != null) {
            authenticationToken = new MobileCaptchaAuthenticationToken(param.getPhone(), param.getCaptcha()); // 账号验证码登录
        } else {
            throw new ServiceException("错误登录请求");
        }
        // 通过AuthenticationManager的authenticate方法来进行用户认证
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        // 获得用户信息
        LoginUserDetail loginUserDetail = (LoginUserDetail) authentication.getPrincipal();
        UserDO userDO = loginUserDetail.getUserDO();
        // 生成token
        HashMap<String, Object> claim = new HashMap<>();
        claim.put(USER_ID, userDO.getId());
        String token = JwtUtils.sign(claim);
        // 生成UserBO对象
        UserBO userBO = new UserBO();
        BeanUtils.copyProperties(userDO, userBO);
        // 生成UserLoginVO对象
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(userDO, userLoginVO);
        userLoginVO.setToken(token);
        Map<String, Object> userMap = BeanUtil.beanToMap(userBO, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true)
                        .setFieldValueEditor(
                                (fieldName, fieldValue) -> fieldValue == null ? "" : fieldValue.toString()));
        // 存入redis
        stringRedisTemplate.opsForHash().putAll(LOGIN_TOKEN_KEY + token, userMap);
        stringRedisTemplate.expire(LOGIN_TOKEN_KEY + token, LOGIN_TOKEN_TTL, LOGIN_TOKEN_TIMEUNIT);
        // 返回
        return userLoginVO;
    }

    /**
     * 用户注册业务
     *
     * @param param
     * @return
     */
    @Override
    public void register(UserRegister param) throws ServiceException {
        // 判断手机号是否合法
        String phone = param.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new ServiceException(PHONE_ERROR);
        }

        // 验证验证码对错
        String captcha = stringRedisTemplate.opsForValue().get(LOGIN_CAPTCHA_KEY + phone);
        if (!param.getCaptcha().equals(captcha)) {
            throw new ServiceException(CODE_ERROR);
        }

        // 判断两次密码是否一样
        String password = param.getPassword();
        if (password.equals(param.getSecondPassword())) {
            throw new ServiceException("前后密码不一致");
        }

        // 判断手机号是否已经注册
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDO::getPhone, phone);
        UserDO userDO = userMapper.selectOne(lambdaQueryWrapper);
        if (userDO != null) {
            throw new ServiceException(ACCOUNT_EXIST);
        }

        UserDO newUserDO = UserDO.builder()
                .id(new SnowflakeIdWorker(WORKER_ID).nextId())
                .nickName(NICKNAME_PREFIX + RandomUtil.randomString(NICKNAME_LENGTH))
                .password(PasswordUtil.encryptPassword(password))
                .phone(phone)
                .createTime(LocalDateTime.now())
                .build();
        int insert = userMapper.insert(newUserDO);
        if (insert == 0) {
            throw new ServiceException("插入新用户失败");
        }
    }

    /**
     * 发送验证码业务
     *
     * @param phone
     * @return
     */
    @Override
    public void sendCaptcha(String phone) throws ServiceException {
        // 判断手机号是否符合要求
        if (phone.isEmpty() || RegexUtils.isPhoneInvalid(phone)) {
            throw new ServiceException(PHONE_ERROR);
        }
        // 判断缓存中是否存在该手机号的验证码
        String captcha = RandomUtil.randomNumbers(6);
        Boolean boo = stringRedisTemplate.opsForValue()
                .setIfAbsent(LOGIN_CAPTCHA_KEY + phone, captcha, LOGIN_CAPTCHA_TTL, LOGIN_CAPTCHA_TIMEUNIT);
        if (!boo) {
            throw new ServiceException("请稍等片刻");
        }
        log.debug("验证码发送成功，验证码为: {}", captcha);
    }

    /**
     * 修改密码业务
     *
     * @param passwordDTO
     * @return
     */
    @Override
    public void editPassword(PasswordDTO passwordDTO) throws ServiceException {
        String oldPassword = passwordDTO.getOldPassword();
        String newPassword = passwordDTO.getNewPassword();

        // 判断密码是否相等
        if (!newPassword.equals(passwordDTO.getConfirmPassword())) {
            throw new ServiceException(NEW_PASSWORD_INC);
        }

        // 判断密码格式是否正确
        if (RegexUtils.isPasswordInvalid(newPassword)) {
            throw new ServiceException(PASSWORD_FORMAT_ERROR);
        }

        // 获得该用户信息
        Long userId = UserHolder.getUser().getId();

        // 获得数据库user对象
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDO::getId, userId);
        UserDO userDO = userMapper.selectOne(queryWrapper);

        // 判断密码的是否正确
        if (validatePassword(oldPassword, userDO.getPassword())) {
            userDO.setPassword(encryptPassword(newPassword));
            userMapper.updateById(userDO);
            return;
        }
        throw new ServiceException(PASSWORD_ERROR);
    }

    /**
     * 用户注销业务
     *
     * @param token
     * @throws ServiceException
     */
    @Override
    public void logout(String token) throws ServiceException {
        Boolean boo = stringRedisTemplate.delete(LOGIN_TOKEN_KEY + token);
        if (!boo) {
            throw new ServiceException("LOGOUT_FAIL");
        }
    }

    @Override
    public Map<String, Object> queryDetail() throws ServiceException {
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            throw new ServiceException("请先登录");
        }

        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDO::getId, userId);
        UserDO userDO = userMapper.selectOne(lambdaQueryWrapper);

        if (userDO == null) {
            throw new ServiceException("用户不存在");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("nickName", userDO.getNickName());
        userInfo.put("phone", userDO.getPhone());
        userInfo.put("icon", userDO.getIcon());
        userInfo.put("email", userDO.getEmail());
        userInfo.put("address", userDO.getAddress());
        userInfo.put("sex", userDO.getSex());
        userInfo.put("age", userDO.getAge());

        // 将数字性别转为字符串便于前端显示
        String sexStr = userDO.getSex() != null ? (userDO.getSex() == 1 ? "男" : "女") : "";
        userInfo.put("sex", sexStr);

        return userInfo;
    }

    @Override
    public void updateUserInfo(UserUpdateInfo userUpdateInfo) throws ServiceException {
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            throw new ServiceException("请先登录");
        }

        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDO::getId, userId);
        UserDO userDO = userMapper.selectOne(lambdaQueryWrapper);

        if (userDO == null) {
            throw new ServiceException("用户不存在");
        }

        boolean needUpdate = false;

        if (userUpdateInfo.getNickName() != null && !StrUtil.isBlank(userUpdateInfo.getNickName())) {
            userDO.setNickName(userUpdateInfo.getNickName());
            needUpdate = true;
        }

        if (userUpdateInfo.getIcon() != null) {
            userDO.setIcon(userUpdateInfo.getIcon());
            needUpdate = true;
        }

        if (userUpdateInfo.getEmail() != null) {
            userDO.setEmail(userUpdateInfo.getEmail());
            needUpdate = true;
        }

        if (userUpdateInfo.getAddress() != null) {
            userDO.setAddress(userUpdateInfo.getAddress());
            needUpdate = true;
        }

        if (userUpdateInfo.getSex() != null) {
            // 直接设置数字性别值
            userDO.setSex(userUpdateInfo.getSex());
            needUpdate = true;
        }

        if (userUpdateInfo.getAge() != null) {
            userDO.setAge(userUpdateInfo.getAge());
            needUpdate = true;
        }

        if (needUpdate) {
            userDO.setUpdateTime(LocalDateTime.now());
            int result = userMapper.updateById(userDO);
            if (result <= 0) {
                throw new ServiceException("更新用户信息失败");
            }

            // 更新Redis中的用户信息
            UserBO userBO = UserHolder.getUser();
            BeanUtils.copyProperties(userDO, userBO);
            UserHolder.saveUser(userBO);
        }
    }

    @Override
    public Map<String, Object> uploadAvatar(MultipartFile file) throws ServiceException {
        try {
            // 获取当前用户ID
            Long userId = UserHolder.getUser().getId();
            if (userId == null) {
                throw new ServiceException("请先登录");
            }

            // 上传文件并获取文件ID
            Map<String, Object> uploadResult = fileService.upload(file);
            if (uploadResult == null || !uploadResult.containsKey("id")) {
                throw new ServiceException("文件上传失败");
            }

            Long fileId = (Long) uploadResult.get("id");

            // 更新用户头像ID
            LambdaQueryWrapper<UserDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserDO::getId, userId);
            UserDO userDO = userMapper.selectOne(lambdaQueryWrapper);

            if (userDO == null) {
                throw new ServiceException("用户不存在");
            }

            userDO.setIcon(fileId);
            userDO.setUpdateTime(LocalDateTime.now());

            int result = userMapper.updateById(userDO);
            if (result <= 0) {
                throw new ServiceException("更新用户头像失败");
            }

            // 更新Redis中的用户信息
            UserBO userBO = UserHolder.getUser();
            userBO.setIcon(fileId);
            UserHolder.saveUser(userBO);

            return uploadResult;
        } catch (IOException e) {
            throw new ServiceException("文件上传失败: " + e.getMessage());
        }
    }
}




