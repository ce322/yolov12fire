<template>
  <div class="profile-container">
    <el-card class="profile-card" shadow="hover">
      <div class="profile-header">
        <div class="avatar-container">
          <el-avatar class="avatar-img" :src="avatarUrl" :size="120">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <div class="avatar-actions">
            <el-upload
              class="avatar-upload"
              :show-file-list="false"
              :http-request="uploadAvatar"
              :before-upload="beforeAvatarUpload"
              accept="image/jpeg,image/png"
            >
              <el-button type="primary" size="small" :loading="uploading" class="upload-btn">
                <el-icon><Upload /></el-icon> 更换头像
              </el-button>
            </el-upload>
          </div>
        </div>
        <h2 class="profile-title">个人资料</h2>
      </div>

      <div class="profile-info">
        <el-form :model="user" ref="form" label-width="80px" :rules="rules" status-icon>
          <el-form-item label="用户名" prop="nickName">
            <el-input v-model="user.nickName" :disabled="!isEditing" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="user.phone" disabled />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="user.gender" :disabled="!isEditing">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="0">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="user.email" :disabled="!isEditing" placeholder="请输入邮箱" clearable>
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="地址" prop="address">
            <el-input v-model="user.address" :disabled="!isEditing" placeholder="请输入地址" clearable>
              <template #prefix>
                <el-icon><Location /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <el-input-number
              v-model="user.age"
              :min="1"
              :max="120"
              :disabled="!isEditing"
              controls-position="right"
              placeholder="请输入年龄"
              class="age-input"
            />
          </el-form-item>
        </el-form>
      </div>

      <div class="profile-actions">
        <el-button v-if="!isEditing" type="primary" @click="editUser" :icon="Edit">
          编辑
        </el-button>
        <template v-if="isEditing">
          <el-button type="success" @click="saveUser" :icon="Check">
            保存
          </el-button>
          <el-button type="warning" @click="cancelEdit" :icon="Close">
            取消
          </el-button>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, } from "vue";
import { ElMessage, ElLoading } from "element-plus";
import {
  getUserInfo,
  updateUserInfo,
  uploadUserAvatar,
  queryAddress,
} from "@/services/api";
import { 
  UserFilled, 
  Upload, 
  Message, 
  Location,
  Edit,
  Check,
  Close
} from '@element-plus/icons-vue';

export default {
  name: "ProfilePage",
  components: {
    UserFilled,
    Upload,
    Message,
    Location,
  },
  setup() {
    const isEditing = ref(false);
    const uploading = ref(false);
    const form = ref(null);
    const defaultIcon = require("@/assets/default-avatar.png");
    const avatarUrl = ref(defaultIcon);

    const user = reactive({
      nickName: "",
      phone: "",
      icon: "",
      email: "",
      address: "",
      sex: "",
      age: null,
      gender: 1,
    });

    const originalUser = reactive({});

    // 增强表单验证规则
    const rules = reactive({
      nickName: [
        { required: true, message: "用户名不能为空", trigger: "blur" },
        { min: 2, max: 20, message: "用户名长度在2-20个字符之间", trigger: "blur" }
      ],
      email: [
        { required: true, message: "邮箱不能为空", trigger: "blur" },
        { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" }
      ],
      age: [
        { required: true, message: "年龄不能为空", trigger: "blur" },
        { type: "number", message: "年龄必须为数字值", trigger: "blur" }
      ],
      address: [
        { required: false, message: "请输入地址", trigger: "blur" }
      ]
    });

    // 获取用户头像
    const fetchAvatar = async () => {
      try {
        if (!user.icon) {
          avatarUrl.value = defaultIcon;
          return;
        }
        const response = await queryAddress(user.icon);
        if (response && response.data && response.data.url) {
          avatarUrl.value = response.data.url;
        } else {
          avatarUrl.value = defaultIcon;
        }
      } catch (error) {
        console.error("获取头像错误:", error);
        avatarUrl.value = defaultIcon;
        ElMessage.error("获取头像失败，已使用默认头像");
      }
    };

    // 获取用户信息
    const fetchUserInfo = async () => {
      const loading = ElLoading.service({
        lock: true,
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      
      try {
        const response = await getUserInfo();
        if (response && response.data) {
          Object.assign(user, response.data);
          
          // 统一性别处理逻辑
          user.gender = mapSexToGender(user.sex);
          
          // 保存原始数据，用于取消编辑时恢复
          Object.assign(originalUser, JSON.parse(JSON.stringify(user)));
          
          // 获取头像
          await fetchAvatar();
          
          ElMessage.success("用户信息加载成功");
        }
      } catch (error) {
        console.error("获取用户信息错误:", error);
        ElMessage.error("获取用户信息失败，请稍后重试");
      } finally {
        loading.close();
      }
    };

    // 性别映射函数
    const mapSexToGender = (sex) => {
      if (typeof sex === 'string') {
        return sex === "男" ? 1 : 0;
      } else if (sex !== null && sex !== undefined) {
        return parseInt(sex);
      }
      return 1; // 默认值
    };

    // 编辑用户信息
    const editUser = () => {
      isEditing.value = true;
    };

    // 保存用户信息
    const saveUser = async () => {
      if (!form.value) return;
      
      try {
        const valid = await form.value.validate();
        if (valid) {
          const loading = ElLoading.service({
            lock: true,
            text: '保存中...',
            background: 'rgba(0, 0, 0, 0.7)'
          });
          
          const userData = {
            nickName: user.nickName,
            sex: user.gender,
            email: user.email,
            address: user.address,
            age: user.age,
          };
          
          const response = await updateUserInfo(userData);
          
          if (response && response.code === 200) {
            ElMessage.success("保存成功");
            // 更新原始数据
            Object.assign(originalUser, JSON.parse(JSON.stringify(user)));
            isEditing.value = false;
          } else {
            ElMessage.error(response?.message || "保存失败");
          }
          
          loading.close();
        }
      } catch (error) {
        console.error("保存用户信息错误:", error);
        ElMessage.error("保存失败，请检查表单数据");
      }
    };

    // 取消编辑，恢复原始数据
    const cancelEdit = () => {
      Object.assign(user, JSON.parse(JSON.stringify(originalUser)));
      isEditing.value = false;
      ElMessage.info("已取消编辑");
    };

    // 头像上传前的检查
    const beforeAvatarUpload = (file) => {
      const isJPG = file.type === "image/jpeg" || file.type === "image/png";
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        ElMessage.error("头像只能是 JPG 或 PNG 格式!");
        return false;
      }
      if (!isLt2M) {
        ElMessage.error("头像大小不能超过 2MB!");
        return false;
      }

      return isJPG && isLt2M;
    };

    // 上传头像
    const uploadAvatar = async (options) => {
      try {
        uploading.value = true;
        const formData = new FormData();
        formData.append("file", options.file);

        const response = await uploadUserAvatar(formData);

        if (response && response.data) {
          user.icon = response.data.id; // 保存头像ID
          await fetchAvatar(); // 刷新头像显示
          ElMessage.success("头像上传成功");
          
          // 同步更新原始数据中的头像信息
          originalUser.icon = user.icon;
        } else {
          ElMessage.error(response?.message || "头像上传失败");
        }
      } catch (error) {
        console.error("上传头像错误:", error);
        ElMessage.error("头像上传失败，请稍后重试");
      } finally {
        uploading.value = false;
      }
    };

    onMounted(() => {
      fetchUserInfo();
    });

    return {
      user,
      isEditing,
      uploading,
      avatarUrl,
      form,
      rules,
      editUser,
      saveUser,
      cancelEdit,
      beforeAvatarUpload,
      uploadAvatar,
      // 图标
      UserFilled,
      Upload,
      Message,
      Location,
      Edit,
      Check,
      Close
    };
  },
};
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 90vh;
  padding: 20px;
  background-color: #f5f7fa;
}

.profile-card {
  width: 500px;
  max-width: 95%;
  padding: 20px;
  transition: all 0.3s ease;
  border-radius: 8px;
}

.profile-card:hover {
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.profile-title {
  margin-top: 20px;
  font-weight: 600;
  color: #303133;
  font-size: 24px;
}

.avatar-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-img {
  border: 3px solid #fff;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease;
}

.avatar-img:hover {
  transform: scale(1.05);
}

.avatar-actions {
  margin-top: 15px;
}

.upload-btn {
  display: flex;
  align-items: center;
  gap: 5px;
}

.profile-info {
  margin-bottom: 30px;
}

.profile-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.el-form-item {
  margin-bottom: 22px;
}

.age-input {
  width: 100%;
}

/* 添加输入框激活效果 */
:deep(.el-input__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 5px rgba(64, 158, 255, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-card {
    width: 100%;
    padding: 15px;
  }
  
  .profile-header {
    margin-bottom: 20px;
  }
  
  .avatar-img {
    width: 100px;
    height: 100px;
  }
  
  .profile-title {
    font-size: 20px;
  }
  
  .el-form-item {
    margin-bottom: 15px;
  }
}

@media (max-width: 480px) {
  .profile-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .profile-actions .el-button {
    width: 100%;
    margin-bottom: 10px;
  }
}
</style>
