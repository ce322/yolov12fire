<template>
  <div class="register-container">
    <el-card class="register-box">
      <h1 class="register-title">注册您的账号</h1>
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
        <el-form-item prop="phone">
          <el-input v-model="registerForm.phone" placeholder="输入手机号" />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-container">
            <el-input v-model="registerForm.captcha" placeholder="输入验证码" />
            <el-button 
              type="primary" 
              plain 
              :disabled="countdown > 0" 
              @click="sendCode"
            >
              {{ countdown > 0 ? `${countdown}秒后重发` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="输入密码"
            show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="再次输入密码"
            show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister">注册</el-button>
        </el-form-item>
      </el-form>
      <div class="login-link">
        已有账号? <router-link to="/login">去登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script>
import { register, sendCaptcha } from '@/services/api';
import { ElMessage } from 'element-plus';

export default {
  name: 'RegisterPage',
  data() {
    return {
      registerForm: {
        phone: '',
        captcha: '',
        password: '',
        confirmPassword: '',
      },
      registerRules: {
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ],
        captcha: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { pattern: /^\d{4,6}$/, message: '验证码格式不正确', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: this.validateConfirmPassword, trigger: 'blur' },
        ],
      },
      loading: false,
      countdown: 0,
      timer: null
    };
  },
  beforeUnmount() {
    // 组件销毁前清除定时器
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    // 发送验证码
    async sendCode() {
      try {
        // 验证手机号
        const valid = await this.$refs.registerFormRef.validateField('phone');
        if (!valid) {
          return;
        }
        
        // 发送验证码请求
        const response = await sendCaptcha(this.registerForm.phone);
        if (response) {
          ElMessage.success('验证码已发送，请注意查收');
          // 开始倒计时
          this.countdown = 60;
          this.timer = setInterval(() => {
            if (this.countdown > 0) {
              this.countdown--;
            } else {
              clearInterval(this.timer);
            }
          }, 1000);
        }
      } catch (error) {
        console.error('发送验证码失败:', error);
      }
    },
    
    // 处理注册
    async handleRegister() {
      if (!this.$refs.registerFormRef) return;
      
      try {
        const valid = await this.$refs.registerFormRef.validate();
        if (!valid) return;
        
        this.loading = true;
        
        // 构造注册数据
        const userData = {
          phone: this.registerForm.phone,
          captcha: this.registerForm.captcha,
          password: this.registerForm.password
        };
        
        // 调用注册 API
        const response = await register(userData);
        
        if (response) {
          ElMessage.success('注册成功，请登录');
          this.$router.push('/login'); // 注册成功后跳转到登录页
        }
      } catch (error) {
        console.error('注册失败:', error);
        ElMessage.error('注册失败，请检查输入信息');
      } finally {
        this.loading = false;
      }
    },
    
    // 验证确认密码
    validateConfirmPassword(rule, value, callback) {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    },
  },
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
}

.register-box {
  width: 400px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  color: #000000;
  margin-bottom: 20px;
}

.el-form-item {
  margin-bottom: 20px;
}

.captcha-container {
  display: flex;
  align-items: center;
}

.captcha-container .el-input {
  flex: 1;
  margin-right: 10px;
}

.captcha-container .el-button {
  width: 120px;
}

.el-button {
  width: 100%;
  margin-top: 10px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
}

.login-link a {
  color: #000000;
  text-decoration: none;
}
</style>