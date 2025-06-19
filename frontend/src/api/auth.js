import api, { API_ENDPOINTS } from './config';

const authApi = {
  // 用户登录
  login(username, password, rememberMe = false) {
    return api.post(API_ENDPOINTS.auth.login, {
      username,
      password,
      rememberMe
    });
  },

  // 用户注册
  register(username, password) {
    return api.post(API_ENDPOINTS.auth.register, {
      username,
      password
    });
  },

  // 刷新令牌
  refreshToken(refreshToken) {
    return api.post(API_ENDPOINTS.auth.refreshToken, { refreshToken });
  },

  // 验证令牌
  validateToken(token) {
    return api.get(`${API_ENDPOINTS.auth.validateToken}?token=${token}`);
  },

  // 初始化管理员账户(仅开发环境)
  initAdmin() {
    return api.post(API_ENDPOINTS.auth.initAdmin);
  },

  // 获取当前存储的令牌
  getToken() {
    return localStorage.getItem('token');
  },

  // 保存令牌到本地存储
  saveToken(token) {
    localStorage.setItem('token', token);
  },

  // 保存用户信息
  saveUser(username) {
    localStorage.setItem('username', username);
    localStorage.setItem('isLoggedIn', 'true');
  },

  // 获取用户信息
  getUser() {
    return {
      username: localStorage.getItem('username'),
      isLoggedIn: localStorage.getItem('isLoggedIn') === 'true'
    };
  },

  // 退出登录
  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('isLoggedIn');
  },

  // 检查是否已登录
  isLoggedIn() {
    return localStorage.getItem('isLoggedIn') === 'true';
  }
};

export default authApi; 