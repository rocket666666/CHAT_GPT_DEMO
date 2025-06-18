import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import axios from 'axios'

const app = createApp(App)

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(ElementPlus, { size: 'default' })

// 添加请求拦截器，打印每个请求的详情
axios.interceptors.request.use(config => {
  console.log('发送请求:', {
    method: config.method,
    url: config.url,
    data: config.data,
    headers: config.headers
  })
  return config
})

app.mount('#app') 