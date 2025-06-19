<template>
  <div class="profit-assistant">
    <div class="decorative-header">
      <div class="decorative-line"></div>
    </div>
    
    <div class="content-frame">
      <back-button />
      <page-header 
        title="利润助手" 
        subtitle="AI智能利润分析工具"
      />
      
      <div class="main-content">
        <div class="iframe-container">
          <iframe 
            src="http://localhost/chatbot/3qLGCOZ5UkuClE2B" 
            style="width: 100%; height: 100%; min-height: 700px" 
            frameborder="0" 
            allow="microphone">
          </iframe>
        </div>
      </div>
      
      <div class="decorative-corner top-right"></div>
      <div class="decorative-corner bottom-left"></div>
    </div>
  </div>
</template>

<script>
import PageHeader from '@/components/PageHeader.vue'
import BackButton from '@/components/BackButton.vue'

export default {
  name: 'ProfitAssistant',
  components: {
    PageHeader,
    BackButton
  }
}
</script>

<style scoped>
.profit-assistant {
  padding: 30px 20px;
  background-color: #f0f2f5;
  background-image: linear-gradient(to bottom, rgba(18, 35, 64, 0.03) 0%, rgba(8, 21, 43, 0.01) 100%);
  min-height: 100vh;
  position: relative;
}

.decorative-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
  overflow: hidden;
}

.decorative-line {
  height: 100%;
  background: linear-gradient(90deg, #1e3f66 0%, #3a7bd5 50%, #1e3f66 100%);
}

.content-frame {
  max-width: 1200px;
  margin: 0 auto;
  background-color: rgba(255, 255, 255, 0.5);
  border-radius: 6px;
  box-shadow: 0 3px 15px rgba(0, 0, 0, 0.05);
  padding: 25px 30px;
  position: relative;
  border: 1px solid rgba(30, 63, 102, 0.08);
  backdrop-filter: blur(5px);
  overflow: hidden;
}

.decorative-corner {
  position: absolute;
  width: 50px;
  height: 50px;
  opacity: 0.1;
}

.top-right {
  top: 0;
  right: 0;
  border-right: 2px solid #3a7bd5;
  border-top: 2px solid #3a7bd5;
  border-top-right-radius: 6px;
}

.bottom-left {
  bottom: 0;
  left: 0;
  border-left: 2px solid #3a7bd5;
  border-bottom: 2px solid #3a7bd5;
  border-bottom-left-radius: 6px;
}

.main-content {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.iframe-container {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 0;
  border: 1px solid #eaedf3;
  overflow: hidden;
  width: 100%;
  height: 750px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .profit-assistant {
    padding: 20px 15px;
  }
  
  .content-frame {
    padding: 20px 15px;
  }
}
</style> 