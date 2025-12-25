<template>
  <div class="login-container">
    <el-card class="login-box">
      <h1 class="login-title">密码登录</h1>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="输入手机号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="links">
        <router-link to="/login-by-captcha">验证码登录</router-link> |
        <router-link to="/register">注册</router-link> |
        <router-link to="/forgot-password">忘记密码</router-link>
      </div>
    </el-card>
  </div>
</template>

<script>
import { login } from '@/services/api.js';

export default {
  name: 'LoginPage',
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
      },
      loginRules: {
        username: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      },
      rememberMe: false,
      loading: false, 
    };
  },

  mounted() {
    // 检查本地存储的账号信息
    const savedUsername = localStorage.getItem('username');
    const savedPassword = localStorage.getItem('password');
    // 如果有 自动填写
    if (savedUsername && savedPassword) {
      this.loginForm.username = savedUsername;
      this.loginForm.password = savedPassword;
      this.rememberMe = true;
    }
  },

  // 登录逻辑
  methods: {
    async handleLogin() {
      if (!this.$refs.loginFormRef) return;
      
      try {
        this.loading = true;
        await this.$refs.loginFormRef.validate();
        
        const response = await login(this.loginForm.username, this.loginForm.password);
        console.log('登录响应:', response);
        
        if (response && response.code === 200 && response.data) {
          // 先更新 Vuex store
          await this.$store.dispatch('login', response.data);

          // 记住密码逻辑
          if (this.rememberMe) {
            localStorage.setItem('username', this.loginForm.username);
            localStorage.setItem('password', this.loginForm.password);
          } else {
            localStorage.removeItem('username');
            localStorage.removeItem('password');
          }

          // 等待下一个 tick，确保状态更新完成
          await this.$nextTick();
          
          // 最后进行路由跳转
          this.$router.push('/');
        }
      } catch (error) {
        console.error('登录失败:', error);
      } finally {
        this.loading = false;
      }
    },
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
