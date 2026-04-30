<template>
  <div class="monitor-container">
    <el-card class="monitor-card">
      <el-carousel height="300px" trigger="click">
        <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
          <img
            :src="item.src"
            alt="消防宣传图片"
            style="width: 100%; height: 100%; object-fit: cover"
          />
        </el-carousel-item>
      </el-carousel>

      <h2>🏠 首页导览</h2>
      <p>
        &emsp;&emsp;欢迎使用消防安全管理平台。首页聚焦“日常消防知识 + 实操指引 +
        值班提醒”，帮助值班人员快速完成巡检、处置与上报，降低火灾风险。
      </p>

      <div class="section">
        <h2>🔥 消防基础知识（建议每日复习）</h2>
        <ul>
          <li>
            <strong>火灾处置口诀</strong>：先报警、再疏散、后扑救；优先保障人员安全，切勿盲目进入浓烟区域。
          </li>
          <li>
            <strong>火灾类型识别</strong>：电气火灾先断电；油类火灾禁止用水；设备类初起火灾优先选用干粉或二氧化碳灭火器。
          </li>
          <li>
            <strong>逃生原则</strong>：弯腰低姿、湿巾捂口鼻、不乘电梯，沿安全出口有序撤离。
          </li>
        </ul>
      </div>

      <div class="section">
        <h2>🧯 灭火器使用 Tips</h2>
        <ul>
          <li>
            <strong>四步法</strong>：提（提起灭火器）→ 拔（拔掉保险销）→ 握（握住喷管）→ 压（压下压把扫射火焰根部）。
          </li>
          <li>
            <strong>安全距离</strong>：建议在 2~3 米外对准火焰根部左右扫射，避免距离过近被火舌反扑。
          </li>
          <li>
            <strong>检查周期</strong>：每月检查压力表、瓶体、喷管和有效期，发现异常立即更换并登记。
          </li>
        </ul>
      </div>

      <div class="section">
        <h2>📋 值班人员操作手册（简版）</h2>
        <ol>
          <li>上岗前确认监控画面、网络、告警提示音状态正常。</li>
          <li>发现疑似火情时，先在“实时监控”进行二次确认并截图留档。</li>
          <li>立即电话通知现场负责人，同步组织周边人员撤离。</li>
          <li>在“火灾记录”页面完成事件上报，填写时间、地点、处置结果。</li>
          <li>处置结束后在“检查记录”补全复盘信息，形成闭环。</li>
        </ol>
      </div>

      <div class="section warning-section">
        <h2>⚠️ 今日值守提醒</h2>
        <ul>
          <li>重点关注配电间、仓储区、厨房等高风险区域。</li>
          <li>严禁堵塞消防通道，确保疏散指示和应急照明可用。</li>
          <li>交接班必须口头+书面双确认，避免漏检漏报。</li>
        </ul>
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
  text-align: left;
  overflow-y: auto;
}

h2 {
  margin-top: 20px;
  margin-bottom: 10px;
}

ul,
ol {
  padding-left: 20px;
}

li {
  margin-bottom: 10px;
  line-height: 1.6;
}

.warning-section {
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 8px;
  padding: 12px 16px;
}
</style>
