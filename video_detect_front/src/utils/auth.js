// Token 相关常量
const TOKEN_KEY = 'token';

// 获取 token
export const getToken = () => {
    try {
        return localStorage.getItem(TOKEN_KEY);
    } catch (error) {
        console.error('获取 token 失败:', error);
        return null;
    }
};

// 设置 token
export const setToken = (token) => {
    try {
        if (!token) {
            throw new Error('无效的 token');
        }
        localStorage.setItem(TOKEN_KEY, token);      
    } catch (error) {
        console.error('设置 token 失败:', error);
        throw error;
    }
};

// 移除 token
export const removeToken = () => {
    try {
        localStorage.removeItem(TOKEN_KEY);
    } catch (error) {
        console.error('移除 token 失败:', error);
        throw error;
    }
};

// 检查是否已登录
export const isAuthenticated = () => {
    try {
        return !!getToken();
    } catch (error) {
        console.error('检查登录状态失败:', error);
        return false;
    }
};

// 获取请求头中的认证信息
export const getAuthHeader = () => {
    try {
        const token = getToken();
        return token ? { Authorization: token } : {};
    } catch (error) {
        console.error('获取认证头信息失败:', error);
        return {};
    }
};

export function isTokenExpired(token) {
  try {
      // 1. 分割 Token，获取 Payload 部分
      const payloadBase64 = token.split('.')[1];

      // 2. 将 Base64 解码为 JSON 字符串
      const payloadJson = atob(payloadBase64);

      // 3. 解析 JSON 字符串为对象
      const payload = JSON.parse(payloadJson);

      // 4. 检查 exp 字段是否存在
      if (!payload.exp) {
          console.warn('Token 没有过期时间字段 (exp)，视为永不过期');
          return false; // 没有过期时间，视为永不过期
      }

      // 5. 检查是否过期
      const currentTime = Math.floor(Date.now() / 1000); // 当前时间（秒）
      return currentTime >= payload.exp; // 如果当前时间 >= 过期时间，则 Token 过期
  } catch (error) {
      console.error('解析 Token 失败:', error);
      return true; // 解析失败，视为过期
  }
}
