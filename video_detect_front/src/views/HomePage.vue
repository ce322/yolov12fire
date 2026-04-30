<template>
  <div class="home-container">
    <!-- 左侧导航栏 -->
    <el-menu
      class="sidebar"
      :collapse="isCollapse"
      default-active="1"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
    >
      <!-- 收起按钮 -->
      <el-menu-item class="collapse-button" @click="toggleCollapse">
        <img
          :src="isCollapse ? collapseIcon : expandIcon"
          class="custom-icon"
        />
        <span v-if="!isCollapse">&emsp;<b>VDDT</b></span>
      </el-menu-item>
      <el-menu-item index="1" @click="navigateTo('/')">
        <el-icon>
          <House />
        </el-icon>
        <span>首页</span>
      </el-menu-item>
      <el-menu-item index="2" @click="navigateTo('/monitor')">
        <el-icon>
          <Camera />
        </el-icon>
        <span>实时监控</span>
      </el-menu-item>
      <el-menu-item index="3" @click="navigateTo('/upload')">
        <el-icon>
          <Pointer />
        </el-icon>
        <span>视频上传</span>
      </el-menu-item>
      <el-menu-item index="4" @click="navigateTo('/fireRecord')">
        <el-icon> <Warning /></el-icon>
        <span>火灾记录</span>
      </el-menu-item>
      <el-menu-item index="5" @click="navigateTo('/place')">
        <el-icon><Coordinate /></el-icon>
        <span>添加地点</span>
      </el-menu-item>
      <el-menu-item index="6" @click="navigateTo('/device')">
        <el-icon><Box /></el-icon>
        <span>设备注册</span>
      </el-menu-item>
      <el-menu-item index="7" @click="navigateTo('/check')">
        <el-icon><Checked /></el-icon>
        <span>检查记录</span>
      </el-menu-item>
      <el-menu-item index="8" @click="navigateTo('/charts')">
        <el-icon><PieChart /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>
      <el-menu-item index="10" @click="navigateTo('/profile')">
        <el-icon>
          <User />
        </el-icon>
        <span>个人信息</span>
      </el-menu-item>
      <el-menu-item index="11" @click="navigateTo('/settings')">
        <el-icon>
          <Setting />
        </el-icon>
        <span>设置</span>
      </el-menu-item>
      <div class="copyright" v-show="!isCollapse">
        <p class="system-name">基于 SpringBoot 与 YOLO 的视频流火灾烟雾检测系统</p>
        <p class="developer">&copy; 开发：hrc</p>
      </div>
    </el-menu>

    <!-- 右侧内容区域 -->
    <div class="main-content">
      <!-- 顶部用户信息栏 -->
      <div class="top-bar">
        <div class="page-title">
          {{ currentPageName }}
        </div>
        <!-- 右边：用户信息 -->
        <div class="user-info">
          <el-avatar :size="40" :src="avatarUrl" />
          <span class="username">{{ nickName }}</span>
          <el-dropdown>
            <span class="el-dropdown-link">
              <el-icon>
                <ArrowDown />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="navigateTo('/profile')"
                  >个人信息</el-dropdown-item
                >
                <el-dropdown-item @click="handleLogout">登出</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapState } from "vuex";
import {
  House,
  Warning,
  Camera,
  Box,
  Coordinate,
  Pointer,
  Checked,
  PieChart,
  User,
  Setting,
  ArrowDown,
} from "@element-plus/icons-vue";
import { queryAddress, logout } from "@/services/api";
import { ElMessage } from "element-plus";

export default {
  name: "HomePage",
  components: {
    House,
    Warning,
    Camera,
    Box,
    Coordinate,
    Pointer,
    Checked,
    PieChart,
    User,
    Setting,
    ArrowDown,
  },
  data() {
    return {
      isCollapse: false, // 默认不折叠导航栏
      collapseIcon: require("@/assets/logo.png"),
      expandIcon: require("@/assets/logo.png"),
      currentPageName: "首页", // 当前页面名字
      avatarUrl: require("@/assets/default-avatar.png"), // 添加默认头像
    };
  },
  computed: {
    ...mapGetters(["nickName"]),
    ...mapState({
      user: (state) => state.user, // 确保Vuex中有userInfo
    }),
    sidebarWidth() {
      return this.isCollapse ? "64px" : "200px"; // 动态计算侧边栏宽度
    },
  },
  watch: {
    // 监听路由变化，更新当前页面名字
    $route(to) {
      this.updatePageName(to.path);
    },
  },
  created() {
    // 初始化时设置当前页面名字
    this.updatePageName(this.$route.path);
    this.fetchAvatar(); // 添加头像获取
  },
  methods: {
    async fetchAvatar() {
      try {
        if (!this.user?.id) {
          return;
        }
        if (this.user.icon==null){
          return;
        }
        const response = await queryAddress(this.user.icon);
        if (response && response.data) {
          // 转换本地路径为可访问URL（根据实际后端配置调整）
          if (response.data.url != null) {
            this.avatarUrl = response.data.url;
          }
        }
      } catch (error) {
        ElMessage.error("获取头像失败");
        console.error("头像获取错误:", error);
      }
    },
    toggleCollapse() {
      this.isCollapse = !this.isCollapse; // 切换导航栏的收起状态
    },
    navigateTo(path) {
      this.$router.push(path);
    },
    handleLogout() {
      // 调用登出API
      logout()
        .then(() => {
          // 清除本地状态
          this.$store.dispatch("logout");
          // 跳转到登录页
          this.$router.push("/login");
          localStorage.clear;
        })
        .catch((error) => {
          console.error("登出失败:", error);
          // 即使API调用失败，也清除本地状态
          this.$store.dispatch("logout");
          this.$router.push("/login");
        });
    },
    // 根据路由更新页面名字
    updatePageName(path) {
      const pageMap = {
        "/": "基于SpringBoot与YOLO的视频流火灾烟雾检测系统",
        "/monitor": "实时监控",
        "/upload": "视频上传",
        "/fireRecord": "火灾记录",
        "/place": "添加地点",
        "/device": "设备注册",
        "/check": "检查记录",
        "/charts": "仪表盘",
        "/file": "文件",
        "/profile": "个人信息",
        "/settings": "设置",
      };
      this.currentPageName = pageMap[path] || "未知页面";
    },
  },
};
</script>

<style scoped>
.home-container {
  display: flex;
  height: 100vh;
  overflow: hidden; /* 防止页面滚动 */
}

.sidebar {
  height: 100%;
  min-height: 100vh; /* 确保最小高度占满视窗 */
  background-color: #304156;
  transition: width 0.3s;
  position: sticky; /* 使导航栏固定 */
  top: 0;
  left: 0;
  z-index: 10;
}

.sidebar:not(.el-menu--collapse) {
  width: 200px;
}

.collapse-button {
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto; /* 内容区域可滚动 */
  height: 100vh;
}

.top-bar {
  height: 60px;
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between; /* 左右布局 */
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 5;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.custom-icon {
  width: 24px;
  height: 24px;
}

.username {
  font-size: 16px;
  font-weight: bold;
}

.el-dropdown-link {
  cursor: pointer;
}

.content {
  flex: 1;
  padding: 20px;
  background-color: #f5f5f5;
}

.aside {
  background-color: #545c64;
  position: relative; /* 为版权信息的绝对定位提供参考 */
  height: 100vh;
}

.el-menu-vertical {
  height: calc(100% - 60px); /* 为版权信息留出空间 */
}

.copyright {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: left;
  color: #bfcbd9;
  font-size: 12px;
  padding: 0 20px;
  line-height: 1.5;
  width: 100%;
  box-sizing: border-box;
}

.copyright p {
  margin: 0;
  opacity: 0.8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.copyright .system-name {
  font-size: 12px;
  margin-bottom: 4px;
}

.copyright .developer {
  font-size: 11px;
  opacity: 0.6;
}
</style>