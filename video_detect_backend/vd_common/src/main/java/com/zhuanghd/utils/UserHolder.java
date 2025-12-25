package com.zhuanghd.utils;

import com.zhuanghd.dto.UserDTO;
import com.zhuanghd.user.bo.UserBO;

/**
 * Author:zhuanghongdong
 * CreateTime:2024/10/17
 * Description:用户线程工具类
 */

public class UserHolder {
    public static final ThreadLocal<UserBO> tl = new ThreadLocal<>();

    public static void saveUser(UserBO userBO) {
        tl.set(userBO);
    }

    public static UserBO getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }
}
