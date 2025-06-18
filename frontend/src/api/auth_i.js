import axios from 'axios';

//const API_URL = 'http://localhost:8089/api/auth';
const API_URL = 'http://172.17.194.44:8089/api/auth';

const authApi = {
  // 用户登录
  login(username, password, rememberMe = false) {
    return axios.post(`${API_URL}/login`, {
      username,
      password,
      rememberMe
    });
  },

  // 用户注册
  register(username, password) {
    return axios.post(`${API_URL}/register`, {
      username,
      password
    });
  },

  // 刷新令牌
  refreshToken(refreshToken) {
    return axios.post(`${API_URL}/refresh-token`, { refreshToken });
  },

  // 验证令牌
  validateToken(token) {
    return axios.get(`${API_URL}/validate-token?token=${token}`);
  },

  // 初始化管理员账户(仅开发环境)
  initAdmin() {
    return axios.post(`${API_URL}/init-admin`);
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
    // 保留记住的用户名，除非显式清除
  },

  // 检查是否已登录
  isLoggedIn() {
    return localStorage.getItem('isLoggedIn') === 'true';
  }
};

export default authApi; 