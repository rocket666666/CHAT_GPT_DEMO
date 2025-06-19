<template>
  <div class="home-container">
    <div class="decorative-header">
      <div class="decorative-line"></div>
    </div>
    
    <!-- 顶部导航栏 -->
    <div class="top-nav-bar">
      <div class="system-info">
        <h2>AI电商助手</h2>
        <p class="subtitle">企业级AI辅助电商解决方案</p>
      </div>
      <div class="user-info">
        <span class="current-time">{{ currentTime }}</span>
        <el-dropdown trigger="click">
          <span class="user-dropdown-link">
            <el-avatar :size="32" class="user-avatar">{{ username.charAt(0) }}</el-avatar>
            {{ username }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="changePassword">修改密码</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="grid-container">
        
        <!-- 关键词生成 -->
        <div class="grid-item" @click="navigateTo('keyword-generation')">
          <div class="icon-wrapper">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <h3>关键词生成</h3>
          <p>基于AI生成电商关键词</p>
        </div>
        
        <!-- 产品优化描述 -->
        <div class="grid-item" @click="navigateTo('product-description')">
          <div class="icon-wrapper">
            <el-icon><Edit /></el-icon>
          </div>
          <h3>产品优化描述</h3>
          <p>AI优化产品描述文案</p>
        </div>
        
        <!-- 备货建议 -->
        <div class="grid-item" @click="navigateTo('inventory-advice')">
          <div class="icon-wrapper">
            <el-icon><Box /></el-icon>
          </div>
          <h3>备货建议</h3>
          <p>智能商品备货推荐</p>
        </div>
        
        <!-- 利润助手 -->
        <div class="grid-item" @click="navigateTo('profit-assistant')">
          <div class="icon-wrapper">
            <el-icon><Money /></el-icon>
          </div>
          <h3>利润助手</h3>
          <p>AI智能利润分析</p>
        </div>
        
        <!-- 库存管理 -->
        <div class="grid-item" @click="navigateTo('inventory-management')">
          <div class="icon-wrapper">
            <el-icon><Files /></el-icon>
          </div>
          <h3>库存管理</h3>
          <p>库存监控与转移</p>
        </div>
        
        <!-- 销售助手 -->
        <div class="grid-item" @click="navigateTo('sales-assistant')">
          <div class="icon-wrapper">
            <el-icon><Files /></el-icon>
          </div>
          <h3>销售助手</h3>
          <p>销售辅助神器</p>
        </div>

        <!-- 帮助中心 -->
        <div class="grid-item" @click="navigateTo('help-center')">
          <div class="icon-wrapper">
            <el-icon><QuestionFilled /></el-icon>
          </div>
          <h3>帮助中心</h3>
          <p>使用说明与帮助</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Picture,
  ChatDotRound,
  Edit,
  VideoCamera,
  Box,
  Money,
  Files,
  QuestionFilled
} from '@element-plus/icons-vue'

export default {
  name: 'HomePage',
  components: {
    Picture,
    ChatDotRound,
    Edit,
    VideoCamera,
    Box,
    Money,
    Files,
    QuestionFilled
  },
  setup() {
    const router = useRouter()
    const username = ref(localStorage.getItem('username') || '用户')
    const currentTime = ref('--:--:--')
    let timeInterval = null
    
    const updateTime = () => {
      const now = new Date()
      const hours = now.getHours().toString().padStart(2, '0')
      const minutes = now.getMinutes().toString().padStart(2, '0')
      const seconds = now.getSeconds().toString().padStart(2, '0')
      currentTime.value = `${hours}:${minutes}:${seconds}`
    }
    
    onMounted(() => {
      updateTime()
      timeInterval = setInterval(updateTime, 1000)
    })
    
    onUnmounted(() => {
      if (timeInterval) {
        clearInterval(timeInterval)
      }
    })
    
    const navigateTo = (route) => {
      router.push(`/${route}`)
    }
    
    const changePassword = () => {
      ElMessage.info('密码修改功能正在开发中')
    }
    
    const logout = () => {
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      ElMessage.success('已成功退出登录')
      router.push('/login')
    }
    
    return {
      username,
      currentTime,
      navigateTo,
      changePassword,
      logout
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.decorative-header {
  height: 4px;
  background: linear-gradient(90deg, #1890ff 0%, #52c41a 100%);
}

.top-nav-bar {
  background-color: white;
  padding: 16px 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.system-info {
  display: flex;
  flex-direction: column;
}

.system-info h2 {
  margin: 0;
  color: #1e3f66;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  margin: 4px 0 0;
  color: #8c8c8c;
  font-size: 14px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 24px;
}

.current-time {
  color: #8c8c8c;
  font-size: 14px;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #1e3f66;
}

.user-avatar {
  background-color: #1890ff;
  color: white;
}

.main-content {
  padding: 24px;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.grid-item {
  background: white;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  transition: all 0.3s;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.grid-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f0f5ff;
}

.icon-wrapper .el-icon {
  font-size: 24px;
  color: #1890ff;
}

.grid-item h3 {
  margin: 0 0 8px;
  color: #1e3f66;
  font-size: 18px;
  font-weight: 500;
}

.grid-item p {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}
</style> 