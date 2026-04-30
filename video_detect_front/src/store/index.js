import { createStore } from 'vuex';
import { setToken, removeToken } from '@/utils/auth';

// 用户状态类型定义
const state = {
    user: JSON.parse(localStorage.getItem('user')) || null,
    token: localStorage.getItem('token') || null,
};

// 用户信息验证
const validateUserData = (userData) => {
    if (!userData || typeof userData !== 'object') {
        throw new Error('无效的用户数据');
    }
    if (!userData.id || !userData.token) {
        throw new Error('用户数据缺少必要字段');
    }
    return {
        id: userData.id,
        nickName: userData.nickName || '未命名用户',
        icon: userData.icon || '',
        token: userData.token
    };
};

export default createStore({
    state,
    mutations: {
        setUser(state, userData) {
            try {
                // 验证并规范化用户数据
                const validatedData = validateUserData(userData);
                
                // 清除旧数据
                state.user = null;
                state.token = null;
                localStorage.removeItem('user');
                removeToken();

                // 设置新数据
                state.user = {
                    id: validatedData.id,
                    nickName: validatedData.nickName,
                    icon: validatedData.icon,
                };
                state.token = validatedData.token;

                // 保存到本地存储
                localStorage.setItem('user', JSON.stringify(state.user));
                setToken(state.token);
            } catch (error) {
                console.error('设置用户信息失败:', error);
                throw error;
            }
        },
        clearUser(state) {
            try {
                state.user = null;
                state.token = null;
                localStorage.removeItem('user');
                removeToken();
            } catch (error) {
                console.error('清除用户信息失败:', error);
                throw error;
            }
        },
    },
    actions: {
        async login({ commit }, userData) {
            try {
                commit('setUser', userData);
            } catch (error) {
                console.error('登录失败:', error);
                throw error;
            }
        },
        async logout({ commit }) {
            try {
                commit('clearUser');
            } catch (error) {
                console.error('登出失败:', error);
                throw error;
            }
        },
    },
    getters: {
        user: (state) => state.user,
        token: (state) => state.token,
        nickName: (state) => state.user?.nickName || '游客',
        avatar: (state) => state.user?.icon || '',
        isAuthenticated: (state) => !!state.token,
    },
});
