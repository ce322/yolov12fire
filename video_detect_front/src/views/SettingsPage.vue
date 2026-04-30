<template>
  <div class="settings-container">
    <el-tabs type="border-card" class="settings-tabs">
      <!-- 密码修改 -->
      <el-tab-pane label="密码设置">
        <div class="settings-section">
          <h3>修改密码</h3>
          <el-form 
            :model="passwordForm" 
            ref="passwordFormRef" 
            :rules="passwordRules" 
            label-width="120px"
            class="password-form"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input 
                v-model="passwordForm.oldPassword" 
                type="password" 
                show-password
                placeholder="请输入当前密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input 
                v-model="passwordForm.newPassword" 
                type="password" 
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                @click="submitPasswordChange" 
                :loading="passwordForm.loading"
              >
                保存修改
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 系统设置 -->
      <el-tab-pane label="系统设置">
        <div class="settings-section">
          <h3>系统偏好</h3>
          
          <div class="system-settings">
            <el-row class="system-item">
              <el-col :span="8">
                <span class="setting-label">界面主题</span>
              </el-col>
              <el-col :span="16">
                <el-radio-group v-model="systemSettings.theme" @change="applyTheme">
                  <el-radio label="light">亮色</el-radio>
                  <el-radio label="dark">暗色</el-radio>
                  <el-radio label="system">跟随系统</el-radio>
                </el-radio-group>
              </el-col>
            </el-row>
            
            <el-row class="system-item">
              <el-col :span="8">
                <span class="setting-label">语言</span>
              </el-col>
              <el-col :span="16">
                <el-select v-model="systemSettings.language" @change="applyLanguage">
                  <el-option label="中文" value="zh-cn" />
                  <el-option label="English" value="en" />
                </el-select>
              </el-col>
            </el-row>
            
            <el-button type="primary" @click="saveSystemSettings" class="save-system-button">
              保存设置
            </el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { changePassword, logout } from '@/services/api';
import { useRouter } from 'vue-router';

export default {
  name: 'SettingsPage',
  setup() {
    const router = useRouter();
    // 密码表单数据
    const passwordFormRef = ref(null);
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
      loading: false
    });
    
    // 密码验证规则
    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少6个字符', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少6个字符', trigger: 'blur' },
        { 
          validator: (rule, value, callback) => {
            if (value === passwordForm.oldPassword) {
              callback(new Error('新密码不能与当前密码相同'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        { 
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入的密码不一致'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ],
    };
    
    // 系统设置
    const systemSettings = reactive({
      theme: localStorage.getItem('theme') || 'light',
      language: localStorage.getItem('language') || 'zh-cn'
    });
    
    // 应用主题
    const applyTheme = (theme) => {
      document.documentElement.setAttribute('data-theme', theme);
      if (theme === 'system') {
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        document.documentElement.classList.toggle('dark-mode', prefersDark);
      } else {
        document.documentElement.classList.toggle('dark-mode', theme === 'dark');
      }
    };
    
    // 应用语言
    const applyLanguage = (language) => {
      document.documentElement.setAttribute('lang', language);
      // 这里可以添加更多语言切换逻辑，如果有国际化库
    };

    // 添加系统主题变化监听
    const setupSystemThemeWatcher = () => {
      if (systemSettings.theme === 'system') {
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
          document.documentElement.classList.toggle('dark-mode', e.matches);
        });
      }
    };
    
    // 提交密码修改
    const submitPasswordChange = async () => {
      if (!passwordFormRef.value) return;
      
      try {
        await passwordFormRef.value.validate(async (valid) => {
          if (!valid) {
            return false;
          }
          
          passwordForm.loading = true;
          
          try {
            const response = await changePassword(
              passwordForm.oldPassword,
              passwordForm.newPassword,
              passwordForm.confirmPassword
            );
            
            if (response && response.code === 200) {
              ElMessage.success('密码修改成功，即将登出...');
              resetPasswordForm();
              // 延迟1秒后登出
              setTimeout(async () => {
                await logout();
                router.push('/login');
              }, 1000);
            } else {
              ElMessage.error(response?.msg || '密码修改失败');
            }
          } catch (error) {
            console.error('密码修改错误:', error);
            ElMessage.error('密码修改失败');
          } finally {
            passwordForm.loading = false;
          }
        });
      } catch (error) {
        console.error('表单验证错误:', error);
      }
    };
    
    // 重置密码表单
    const resetPasswordForm = () => {
      if (passwordFormRef.value) {
        passwordFormRef.value.resetFields();
      }
    };
    
    // 保存系统设置
    const saveSystemSettings = () => {
      localStorage.setItem('theme', systemSettings.theme);
      localStorage.setItem('language', systemSettings.language);
      
      applyTheme(systemSettings.theme);
      applyLanguage(systemSettings.language);
      
      ElMessage.success('系统设置已保存');
    };
    
    // 页面加载时应用设置
    onMounted(() => {
      applyTheme(systemSettings.theme);
      applyLanguage(systemSettings.language);
      setupSystemThemeWatcher();
    });
    
    return {
      passwordFormRef,
      passwordForm,
      passwordRules,
      systemSettings,
      submitPasswordChange,
      resetPasswordForm,
      saveSystemSettings,
      applyTheme,
      applyLanguage
    };
  }
};
</script>

<style scoped>
.settings-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.settings-title {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 20px;
  text-align: center;
}

.settings-tabs {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.settings-section {
  padding: 20px;
}

.settings-section h3 {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 20px;
  color: #303133;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.password-form {
  max-width: 500px;
}

.notification-item {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #f8f8f8;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.notification-label {
  font-weight: 500;
  display: block;
  margin-bottom: 4px;
}

.notification-desc {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.notification-switch {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.save-notification-button,
.save-system-button {
  margin-top: 20px;
}

.system-item {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.setting-label {
  font-weight: 500;
}

@media (max-width: 768px) {
  .system-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .system-item .el-col {
    width: 100%;
  }
  
  .system-item .el-col:first-child {
    margin-bottom: 8px;
  }
}

/* 暗色主题样式 */
:root[data-theme="dark"] .settings-container,
.dark-mode .settings-container {
  background-color: #1f1f1f;
  color: #e0e0e0;
}

:root[data-theme="dark"] .settings-title,
.dark-mode .settings-title {
  color: #e0e0e0;
}

:root[data-theme="dark"] .settings-section h3,
.dark-mode .settings-section h3 {
  color: #e0e0e0;
  border-bottom-color: #3e3e3e;
}

:root[data-theme="dark"] .setting-label,
.dark-mode .setting-label {
  color: #e0e0e0;
}
</style>