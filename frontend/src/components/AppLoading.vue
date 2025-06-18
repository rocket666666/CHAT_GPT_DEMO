<template>
  <div class="loading-container" :class="{ 'fullscreen': fullscreen }">
    <div class="loading-content">
      <div class="loading-spinner">
        <div class="spinner-ring"></div>
      </div>
      <div class="loading-text">{{ text }}</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AppLoading',
  props: {
    text: {
      type: String,
      default: '加载中...'
    },
    fullscreen: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.loading-container.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(2px);
  z-index: 9999;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  margin-bottom: 15px;
}

.spinner-ring {
  width: 100%;
  height: 100%;
  border: 3px solid transparent;
  border-top-color: var(--primary-color, #3a7bd5);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 16px;
  color: var(--primary-dark, #1e3f66);
  font-weight: 500;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style> 