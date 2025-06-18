<template>
  <div class="login-container">
    <div class="floating-dots">
      <div class="dot"></div>
      <div class="dot"></div>
      <div class="dot"></div>
    </div>
    
    <div class="decorative-header">
      <div class="decorative-line"></div>
    </div>
    
    <div class="login-content">
      <div class="login-card">
        <div class="login-logo">
          <h1>AI电商助手</h1>
          <p class="logo-subtitle">企业级智能化电商解决方案</p>
        </div>
        
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="用户名">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password>
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <a href="javascript:void(0)" class="forgot-password">忘记密码?</a>
          </div>
          
          <el-form-item>
            <el-button type="primary" @click="handleLogin" :loading="loading" class="login-button">
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <p>© 2025 AI电商助手 版权所有</p>
        </div>
      </div>
      
      <div class="decorative-corner login-top-right"></div>
      <div class="decorative-corner login-bottom-left"></div>
    </div>
  </div>
</template>

<script>
import { User, Lock } from '@element-plus/icons-vue'
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import authApi from '../api/auth'

export default {
  name: 'Login',
  components: {
    User,
    Lock
  },
  setup() {
    const router = useRouter()
    const loginFormRef = ref(null)
    const loading = ref(false)
    const rememberMe = ref(false)
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    }
    
    // 组件挂载时检查是否已登录
    onMounted(() => {
      // 检查是否已登录
      if (authApi.isLoggedIn()) {
        router.replace('/')
        return
      }
      
      // 检查是否记住了用户名
      const savedUsername = localStorage.getItem('rememberedUsername')
      if (savedUsername) {
        loginForm.username = savedUsername
        rememberMe.value = true
      }
      
      // 创建一个演示账户(仅开发环境)
      createDemoAccount()
    })
    
    // 创建演示账户
    const createDemoAccount = () => {
      authApi.initAdmin()
        .then(response => {
          console.log('演示账户就绪:', response.data.message)
        })
        .catch(error => {
          console.error('创建演示账户失败:', error)
        })
    }
    
    const handleLogin = () => {
      loginFormRef.value.validate((valid) => {
        if (valid) {
          loading.value = true
          
          authApi.login(loginForm.username, loginForm.password, rememberMe.value)
            .then(response => {
              const data = response.data
              
              if (data.success) {
                // 存储认证令牌
                authApi.saveToken(data.token)
                authApi.saveUser(data.username)
                
                // 如果选择了记住我，保存用户名
                if (rememberMe.value) {
                  localStorage.setItem('rememberedUsername', loginForm.username)
                } else {
                  localStorage.removeItem('rememberedUsername')
                }
                
                // 显示欢迎消息
                ElMessage({
                  message: data.message || '登录成功，欢迎回来！',
                  type: 'success',
                  duration: 2000
                })
                
                // 如果有提示密码即将过期，显示提醒
                if (data.passwordExpiring) {
                  ElMessage({
                    message: data.message,
                    type: 'warning',
                    duration: 5000
                  })
                }
                
                // 跳转到首页
                router.push('/')
              } else {
                // 登录失败
                ElMessage({
                  message: data.message || '用户名或密码错误',
                  type: 'error'
                })
              }
            })
            .catch(error => {
              let errorMessage = '登录失败'
              
              if (error.response && error.response.data) {
                errorMessage = error.response.data.message || '登录失败，请稍后再试'
              }
              
              ElMessage({
                message: errorMessage,
                type: 'error'
              })
            })
            .finally(() => {
              loading.value = false
            })
        }
      })
    }
    
    return {
      loginForm,
      rules,
      loginFormRef,
      loading,
      rememberMe,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background-color: #f0f2f5;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(58, 123, 213, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(30, 63, 102, 0.05) 0%, transparent 50%),
    linear-gradient(to bottom, rgba(18, 35, 64, 0.02) 0%, rgba(8, 21, 43, 0.01) 100%);
}

/* 装饰性几何背景元素 */
.login-container::before,
.login-container::after {
  content: "";
  position: absolute;
  z-index: -1;
}

.login-container::before {
  width: 60vh;
  height: 60vh;
  background: linear-gradient(135deg, rgba(58, 123, 213, 0.1) 0%, rgba(30, 63, 102, 0.05) 100%);
  border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  top: -15vh;
  right: -15vh;
  animation: morphingShape 20s ease-in-out infinite alternate;
}

.login-container::after {
  width: 50vh;
  height: 50vh;
  background: linear-gradient(135deg, rgba(30, 63, 102, 0.08) 0%, rgba(58, 123, 213, 0.03) 100%);
  border-radius: 63% 37% 54% 46% / 55% 48% 52% 45%;
  bottom: -15vh;
  left: -15vh;
  animation: morphingShape 15s ease-in-out infinite;
  animation-delay: -5s;
}

/* 漂浮的小圆点 */
.login-container .floating-dots {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: -1;
  opacity: 0.5;
}

.login-container .dot {
  position: absolute;
  border-radius: 50%;
  background-color: rgba(58, 123, 213, 0.05);
  z-index: -1;
}

.login-container .dot:nth-child(1) {
  width: 100px;
  height: 100px;
  top: 20%;
  left: 15%;
  animation: floating 10s infinite ease-in-out;
}

.login-container .dot:nth-child(2) {
  width: 50px;
  height: 50px;
  top: 40%;
  right: 10%;
  animation: floating 8s infinite ease-in-out;
  animation-delay: -2s;
}

.login-container .dot:nth-child(3) {
  width: 75px;
  height: 75px;
  bottom: 15%;
  left: 25%;
  animation: floating 12s infinite ease-in-out;
  animation-delay: -4s;
}

.decorative-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
  overflow: hidden;
  z-index: 1;
}

.decorative-line {
  height: 100%;
  background: linear-gradient(90deg, #1e3f66 0%, #3a7bd5 50%, #1e3f66 100%);
}

.login-content {
  width: 90%;
  max-width: 460px;
  position: relative;
  background-color: rgba(255, 255, 255, 0.85);
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 0;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
  z-index: 2;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.login-content:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
}

.login-card {
  padding: 40px;
}

.login-logo {
  text-align: center;
  margin-bottom: 30px;
}

.login-logo h1 {
  color: #1e3f66;
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  background: linear-gradient(90deg, #1e3f66, #3a7bd5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
}

.logo-subtitle {
  color: #5c6a7a;
  margin-top: 8px;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 14px;
}

.forgot-password {
  color: #3a7bd5;
  text-decoration: none;
}

.forgot-password:hover {
  text-decoration: underline;
}

.login-button {
  width: 100%;
  padding: 12px 20px;
  font-size: 16px;
  background: linear-gradient(90deg, #1e3f66, #3a7bd5);
  transition: transform 0.2s ease;
}

.login-button:hover {
  transform: translateY(-2px);
}

.login-tips {
  text-align: center;
  margin-top: 15px;
  padding: 10px;
  background-color: rgba(58, 123, 213, 0.08);
  border-radius: 4px;
}

.login-tips p {
  margin: 0;
  font-size: 14px;
  color: var(--primary-dark, #1e3f66);
}

.login-footer {
  text-align: center;
  font-size: 12px;
  color: #8792a8;
  margin-top: 40px;
}

.login-footer p {
  margin: 0;
}

.decorative-corner {
  position: absolute;
  width: 80px;
  height: 80px;
  opacity: 0.1;
}

.login-top-right {
  top: 0;
  right: 0;
  border-right: 2px solid #3a7bd5;
  border-top: 2px solid #3a7bd5;
  border-top-right-radius: 8px;
}

.login-bottom-left {
  bottom: 0;
  left: 0;
  border-left: 2px solid #3a7bd5;
  border-bottom: 2px solid #3a7bd5;
  border-bottom-left-radius: 8px;
}

@keyframes morphingShape {
  0% {
    border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  }
  25% {
    border-radius: 58% 42% 75% 25% / 76% 46% 54% 24%;
  }
  50% {
    border-radius: 50% 50% 33% 67% / 55% 27% 73% 45%;
  }
  75% {
    border-radius: 33% 67% 58% 42% / 63% 68% 32% 37%;
  }
  100% {
    border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  }
}

@keyframes floating {
  0% {
    transform: translateY(0) translateX(0);
  }
  25% {
    transform: translateY(-10px) translateX(5px);
  }
  50% {
    transform: translateY(5px) translateX(-5px);
  }
  75% {
    transform: translateY(-5px) translateX(-8px);
  }
  100% {
    transform: translateY(0) translateX(0);
  }
}

@media (max-width: 768px) {
  .login-content {
    width: 90%;
  }
  
  .login-card {
    padding: 30px 20px;
  }
  
  .login-container::before,
  .login-container::after {
    opacity: 0.5;
  }
}

.login-page {
  height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background: radial-gradient(ellipse at center, rgba(35, 7, 77, 0.6) 0%, rgba(5, 5, 20, 0.9) 70%);
  background-image: linear-gradient(45deg, rgba(18, 35, 64, 0.8) 0%, rgba(30, 63, 102, 0.7) 100%);
  background-size: cover;
  background-position: center;
  background-blend-mode: overlay;
}

.login-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 1.5rem;
  text-align: center;
  background: linear-gradient(45deg, #4b6cb7 0%, #182848 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
</style> 