import { createRouter, createWebHistory } from "vue-router";
import HomePage from "../views/HomePage.vue";
import LoginPage from "../views/LoginPage.vue";
import RegisterPage from "../views/RegisterPage.vue";
import ForgotPasswordPage from "../views/ForgotPasswordPage.vue";
import ProfilePage from "../views/ProfilePage.vue";
import SettingsPage from "../views/SettingsPage.vue";
import MainPage from "../views/MainPage.vue";
import LoginByCaptchaPage from "@/views/LoginByCaptchaPage.vue";
import UploadPage from "@/views/UploadPage.vue";
import { getToken, isTokenExpired, removeToken } from "@/utils/auth";
import MonitorPage from '@/views/MonitorPage.vue';
import FireRecordPage from '@/views/FireRecordPage.vue';
import PlaceAddtitionPage from '@/views/PlaceAdditionPage.vue';
import DeviceRegistrationPage from '@/views/DeviceRegistrationPage.vue';
import ChartsPage from '@/views/ChartsPage.vue';
import CheckPage from '@/views/CheckPage.vue'

const routes = [
    {
        path: '/',
        component: HomePage,
        meta: { requiresAuth: true }, 
        children: [
            { path: '', name: 'Main', component: MainPage },
            { path: 'monitor', name: 'Monitor', component: MonitorPage },
            { path: 'upload', name: 'Upload', component: UploadPage },
            { path: 'fireRecord', name: 'FireRecord', component: FireRecordPage },
            { path: 'place', name: 'PlaceAddtition', component: PlaceAddtitionPage },
            { path: 'device', name: 'DeviceRegistration', component: DeviceRegistrationPage },
            { path: 'charts', name: 'Charts', component: ChartsPage },
            { path: 'check', name: 'Check', component: CheckPage },
            { path: 'profile', name: 'Profile', component: ProfilePage },
            { path: 'settings', name: 'Settings', component: SettingsPage },
        ],
    },
    { path: '/login', name: 'Login', component: LoginPage },
    { path: '/register', name: 'Register', component: RegisterPage },
    { path: '/forgot-password', name: 'ForgotPassword', component: ForgotPasswordPage },
    { path: '/login-by-captcha', name: 'LoginByCaptcha', component: LoginByCaptchaPage }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 全局前置守卫：检查 Token 过期
router.beforeEach(async (to, from, next) => {
    const token = getToken();
    
    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!token) {
            next('/login');
            return;
        }
        
        try {
            if (isTokenExpired(token)) {
                removeToken();
                next('/login');
                return;
            }
            next();
        } catch (error) {
            console.error('Token 验证失败:', error);
            removeToken();
            next('/login');
        }
    } else {
        next();
    }
});

export default router;
