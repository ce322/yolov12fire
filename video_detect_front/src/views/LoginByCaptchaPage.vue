<template>
    <div class="login-container">
      <el-card class="login-box">
        <h1 class="login-title">验证码登录</h1>
        <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
          <el-form-item prop="phone">
            <el-input v-model="loginForm.phone" placeholder="输入手机号" />
          </el-form-item>
          <el-form-item prop="captcha">
            <div class="captcha-container">
              <el-input v-model="loginForm.captcha" placeholder="输入验证码" />
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
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
          </el-form-item>
        </el-form>
        <div class="links">
            <router-link to="/login">密码登录</router-link> |
            <router-link to="/register">注册</router-link> |
            <router-link to="/forgot-password">忘记密码</router-link>
         </div>
      </el-card>
    </div>
  </template>
  
  <script>
  import { loginByCaptcha, sendCaptcha } from '@/services/api';
  
  export default {
    name: 'LoginByCaptchaPage',
    data() {
      return {
        loginForm: {
          phone: '',
          captcha: '',
        },
        loginRules: {
          phone: [
            { required: true, message: '请输入手机号', trigger: 'blur' },
            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
          ],
          captcha: [
            { required: true, message: '请输入验证码', trigger: 'blur' },
            { pattern: /^\d{4,6}$/, message: '验证码格式不正确', trigger: 'blur' }
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
          const valid = await this.$refs.loginFormRef.validateField('phone');
          if (!valid) {
            return;
          }
          
          // 发送验证码请求
          const response = await sendCaptcha(this.loginForm.phone);
          if (response) {
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
      
      // 验证码登录
      async handleLogin() {
        if (!this.$refs.loginFormRef) return;
        
        try {
          this.loading = true; // 启用加载状态
          await this.$refs.loginFormRef.validate(); // 表单验证
          
          // 调用验证码登录 API
          const response = await loginByCaptcha(this.loginForm.phone, this.loginForm.captcha);
          console.log('验证码登录响应:', response);
          
          if (response && response.code === 200 && response.data) {
            // 将用户信息存入 Vuex
            await this.$store.dispatch('login', response.data);
            
            // 等待下一个 tick，确保状态更新完成
            await this.$nextTick();
            
            this.$router.push('/');
          }
        } catch (error) {
          console.error('登录失败:', error);
        } finally {
          this.loading = false;
        }
      }
    },
  };
  </script>
  
  <style scoped>
  .login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f5f5f5;
  }
  
  .login-box {
    width: 400px;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .login-title {
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
  
  .links {
    text-align: center;
    margin-top: 20px;
  }
  
  .links a {
    color: #000000;
    text-decoration: none;
  }
  </style>