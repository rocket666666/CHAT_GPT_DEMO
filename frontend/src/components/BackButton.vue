<template>
  <div class="back-button-container">
    <el-button @click="goBack" type="primary" text class="back-button">
      <el-icon class="back-icon"><ArrowLeft /></el-icon>
      返回
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
  props: {
    // 可以指定返回的目标路径，默认返回首页
    targetPath: {
      type: String,
      default: '/'
    },
    // 是否返回上一页而不是指定路径
    goToPrevious: {
      type: Boolean,
      default: false
    }
  },
  setup(props) {
    const router = useRouter()
    
    const goBack = () => {
      if (props.goToPrevious) {
        router.back()
      } else {
        router.push(props.targetPath)
      }
    }
    
    return {
      goBack
    }
  }
}
</script>

<style scoped>
.back-button-container {
  margin-bottom: 20px;
}

.back-button {
  font-size: 14px;
  font-weight: 500;
  color: #1e3f66;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.back-button:hover {
  background-color: rgba(30, 63, 102, 0.08);
  color: #1e3f66;
}

.back-icon {
  margin-right: 6px;
  font-size: 14px;
}
</style> 