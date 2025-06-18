<template>
  <div class="page-header">
    <h1 class="title">{{ title }}</h1>
    <p class="subtitle">{{ subtitle }}</p>
  </div>
</template>

<script>
export default {
  name: 'PageHeader',
  props: {
    title: {
      type: String,
      required: true
    },
    subtitle: {
      type: String,
      default: ''
    }
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}

.title {
  margin: 0 0 5px 0;
  font-size: 26px;
  font-weight: 500;
  color: #2c3e50;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #5c6a7a;
}
</style> 