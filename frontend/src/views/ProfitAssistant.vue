<template>
  <div class="profit-assistant">
    <div class="decorative-header">
      <div class="decorative-line"></div>
    </div>
    
    <div class="content-frame">
      <page-header 
        title="利润助手" 
        subtitle="AI智能利润分析工具"
      />
      
      <div class="main-content">
        <div class="intro-section">
          <p>利用AI技术，分析您的销售数据、库存状况和定价策略，为您提供最优化的利润方案。</p>
        </div>
        
        <div class="iframe-container">
          <iframe
            src="http://localhost/chatbot/GtrMfxVGYJriNv6S"
            style="width: 100%; height: 100%; min-height: 700px"
            frameborder="0"
            allow="microphone">
          </iframe>
        </div>
        
        <div class="tips-section">
          <h3>使用提示</h3>
          <el-alert
            title="您可以向AI助手询问以下问题："
            type="info"
            :closable="false"
            show-icon>
          </el-alert>
          <ul class="tips-list">
            <li>"分析最近30天内利润最高的10个产品"</li>
            <li>"哪些商品的库存成本过高？"</li>
            <li>"我的定价策略是否合理？"</li>
            <li>"如何优化我的产品组合以提高整体利润？"</li>
            <li>"预测下个月的销售趋势和利润"</li>
          </ul>
        </div>
      </div>
      
      <div class="decorative-corner top-right"></div>
      <div class="decorative-corner bottom-left"></div>
    </div>
  </div>
</template>

<script>
import PageHeader from '@/components/PageHeader.vue'

export default {
  name: 'ProfitAssistant',
  components: {
    PageHeader
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

.intro-section {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 25px;
  border: 1px solid #eaedf3;
}

.intro-section p {
  margin: 0;
  color: #5c6a7a;
  line-height: 1.6;
  font-size: 16px;
}

.iframe-container {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 0;
  border: 1px solid #eaedf3;
  overflow: hidden;
}

.tips-section {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 25px;
  border: 1px solid #eaedf3;
}

h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #2c3e50;
  font-weight: 500;
}

.tips-list {
  padding-left: 20px;
  margin-top: 15px;
}

.tips-list li {
  margin-bottom: 10px;
  color: #5c6a7a;
  line-height: 1.5;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .profit-assistant {
    padding: 20px 15px;
  }
  
  .content-frame {
    padding: 20px 15px;
  }
  
  .intro-section, .tips-section {
    padding: 20px 15px;
  }
}
</style> 