<template>
    <div class="forgot-password-container">
      <el-card class="forgot-password-box">
        <h1 class="forgot-password-title">忘记密码</h1>
        <el-form :model="forgotPasswordForm" :rules="forgotPasswordRules" ref="forgotPasswordFormRef">
          <el-form-item label="手机号" prop="email">
            <el-input
              v-model="forgotPasswordForm.email"
              placeholder="请输入你的手机号码"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit">提交</el-button>
          </el-form-item>
        </el-form>
        <div class="login-link">
          Remember your password? <router-link to="/login"> 登录 </router-link>
        </div>
      </el-card>
    </div>
  </template>
  
  <script>
  export default {
    name: 'ForgotPasswordPage',
    data() {
      return {
        forgotPasswordForm: {
          email: '',
        },
        forgotPasswordRules: {
          email: [
            { required: true, message: 'Email is required', trigger: 'blur' },
            { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' },
          ],
        },
      };
    },
    methods: {
      handleSubmit() {
        this.$refs.forgotPasswordFormRef.validate((valid) => {
          if (valid) {
            console.log('Submitting email:', this.forgotPasswordForm.email);
            // 调用忘记密码 API
            this.$message.success('Password reset instructions sent to your email.');
          } else {
            console.log('Form validation failed');
            return false;
          }
        });
      },
    },
  };
  </script>
  
  <style scoped>
  .forgot-password-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f5f5f5;
  }
  
  .forgot-password-box {
    width: 400px;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .forgot-password-title {
    text-align: center;
    color: #42b983;
    margin-bottom: 20px;
  }
  
  .el-form-item {
    margin-bottom: 20px;
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
    color: #42b983;
    text-decoration: none;
  }
  </style>