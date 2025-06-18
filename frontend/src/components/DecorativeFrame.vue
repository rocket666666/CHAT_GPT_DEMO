<template>
  <div class="decorative-frame">
    <div class="decorative-header">
      <div class="decorative-line"></div>
    </div>
    
    <div class="content-frame">
      <slot></slot>
      <div class="decorative-corner top-right"></div>
      <div class="decorative-corner bottom-left"></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DecorativeFrame'
}
</script>

<style scoped>
.decorative-frame {
  position: relative;
  min-height: 100%;
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

@media (max-width: 768px) {
  .content-frame {
    padding: 20px 15px;
  }
}
</style> 