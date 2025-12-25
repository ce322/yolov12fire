<template>
  <div class="monitor-container">
    <el-card class="monitor-card">
      <!-- 轮播图 -->
      <el-carousel height="300px" trigger="click">
        <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
          <img
            :src="item.src"
            alt="轮播图图片"
            style="width: 100%; height: 100%; object-fit: cover"
          />
        </el-carousel-item>
      </el-carousel>

      <h2>🔍 系统简介</h2>
      <p>
        &emsp;&emsp;本系统是一套基于Spring Boot与YOLO的智能视频流处理与目标检测平台，专注于火灾与烟雾的自动识别与预警。通过引入先进的AI视觉识别技术与高效的系统架构设计，旨在提升传统火灾检测系统在实时性、准确率与稳定性方面的表现。
      </p>

      <!-- 项目功能 -->
      <div class="section">
        <h2>🧠 核心功能亮点</h2>
        <ul>
          <li>
            <strong>🎥 视频流数据处理</strong>：支持视频流上传与实时处理，系统通过异步处理机制与分布式ID生成优化性能，确保在高并发场景下依然保持流畅、快速的响应能力。
          </li>
          <li>
            <strong>📊 数据可视化界面</strong>：前端采用Vue 3、Element Plus 与 ECharts，提供实时监控视图、统计图表与历史数据分析界面，让数据更直观、操作更便捷。
          </li>
        </ul>
      </div>

      <!-- 项目意义 -->
      <div class="section">
        <h2>🏗️ 系统架构优势</h2>
        <ul>
          <li>
            <strong>前后端分离设计</strong>：前端 Vue.js 与后端 Spring Boot 解耦开发，提升系统的可维护性与可扩展性
          </li>
          <li>
            <strong>高性能后端支撑</strong>：整合 MyBatis-Plus、Redis 缓存、JWT 鉴权，实现数据安全与请求高效响应
          </li>
          <li>
            <strong>稳定高并发处理</strong>：基于 Spring 异步机制和分布式设计，应对大规模数据访问与检测请求
          </li>
        </ul>
      </div>

      <div class="section">
        <h2>📌 应用前景与价值</h2>
        <p>
          &emsp;&emsp;本系统不仅适用于智能安防与火灾预警领域，还可扩展应用于其他目标检测场景，具备良好的泛化能力与技术可移植性，为AI视觉检测的实际落地提供了完整解决方案。
        </p>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "MainPage",
  data() {
    return {
      carouselItems: [
        { src: "/Snipaste_2025-03-18_14-41-12.png" },
        { src: "/Snipaste_2025-03-18_14-41-29.png" },
        { src: "/Snipaste_2025-03-18_14-41-53.png" },
      ],
    };
  },
};
</script>

<style scoped>
.monitor-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 90vh;
}

.monitor-card {
  width: 95%;
  height: 90%;
  padding: 20px;
  text-align: left; /* 内容左对齐 */
  overflow-y: auto; /* 如果内容过多，允许滚动 */
}

h1 {
  text-align: center;
  margin-bottom: 20px;
}

h2 {
  margin-top: 20px;
  margin-bottom: 10px;
}

ul {
  padding-left: 20px;
}

li {
  margin-bottom: 10px;
}

footer {
  margin-top: 40px;
  text-align: center;
  font-size: 14px;
  color: #666;
}
</style>