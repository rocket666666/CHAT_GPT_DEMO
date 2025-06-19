<template>
  <div class="back-button-container">
    <el-button 
      type="default" 
      :icon="ArrowLeft" 
      @click="goBack"
      class="back-button"
    >
      返回首页
    </el-button>
  </div>
</template>

<script>
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

export default {
  name: 'BackButton',
  components: {
    ArrowLeft
  },
  setup() {
    const router = useRouter()
    
    const goBack = () => {
      router.push('/home')
    }
    
    return {
      ArrowLeft,
      goBack
    }
  }
}
</script>

<style scoped>
.back-button-container {
  margin-bottom: 20px;
  text-align: left;
}

.back-button {
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(30, 63, 102, 0.2);
  color: #1e3f66;
  border-radius: 4px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.back-button:hover {
  background-color: rgba(30, 63, 102, 0.05);
  border-color: rgba(30, 63, 102, 0.3);
  color: #1e3f66;
  transform: none;
}

.back-button:focus {
  background-color: rgba(30, 63, 102, 0.05);
  border-color: rgba(30, 63, 102, 0.3);
  color: #1e3f66;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .back-button-container {
    margin-bottom: 15px;
  }
  
  .back-button {
    padding: 8px 12px;
    font-size: 13px;
  }
}
</style> 