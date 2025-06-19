import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/login_i',
    name: 'Login_i',
    component: () => import('../views/Login_i.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/home_i',
    name: 'home_i',
    component: () => import('../views/Home_i.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/keyword-generation',
    name: 'KeywordGeneration',
    component: () => import('../views/KeywordGeneration.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/product-description',
    name: 'ProductDescription',
    component: () => import('../views/ProductDescription.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/inventory-advice',
    name: 'InventoryAdvice',
    component: () => import('../views/InventoryAdvice.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profit-assistant',
    name: 'ProfitAssistant',
    component: () => import('../views/ProfitAssistant.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/inventory-management',
    name: 'InventoryManagement',
    component: () => import('../views/InventoryManagement.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/sales-assistant',
    name: 'SalesAssistant',
    component: () => import('../views/SalesAssistant.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/help-center',
    name: 'HelpCenter',
    component: () => import('../views/HelpCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory('/chat_gpt_demo'),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn')
  
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (isLoggedIn && to.path === '/login') {
    next('/home')
  } else {
    next()
  }
})

export default router 