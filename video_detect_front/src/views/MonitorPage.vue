<template>
  <div class="monitor-container">
    <el-card class="monitor-card">
      <div class="page-layout">
        <!-- 左侧控制面板区域 -->
        <div class="left-panel">
          <div class="panel-title">
            <i class="el-icon-setting"></i> 控制面板
          </div>
          
          <div class="control-panel">
            <el-select v-model="selectedPlaceId" placeholder="请选择监测地点" style="width: 80%; margin: 10px 0">
              <el-option v-for="item in placeOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-select
              v-model="selectedModelName"
              placeholder="请选择检测模型"
              style="width: 80%; margin: 10px 0"
              :disabled="isDetecting || isSwitchingModel"
              @change="handleModelChange"
            >
              <el-option v-for="item in modelOptions" :key="item.name" :label="item.label" :value="item.name" />
            </el-select>
            <el-button type="primary" @click="startCamera" icon="el-icon-video-play">调用摄像头</el-button>
            <el-button type="danger" @click="stopCamera" icon="el-icon-video-pause">停用摄像头</el-button>
            <el-button type="success" @click="startDetection" icon="el-icon-search" :disabled="!stream || isDetecting">开始检测</el-button>
            <el-button type="warning" @click="stopDetection" icon="el-icon-close" :disabled="!isDetecting">停止检测</el-button>
          </div>
          
          <div v-if="errorMessage" class="error-message">
            <i class="el-icon-warning"></i>
            {{ errorMessage }}
          </div>
          
          <div class="info-section" v-if="stream">
            <div class="section-divider">
              <span><i class="el-icon-info"></i> 设备信息</span>
            </div>
            
            <div class="camera-info">
              <div class="info-row">
                <span class="info-label">状态:</span>
                <span class="info-value" :class="{'status-active': stream && !isDetecting, 'status-detecting': isDetecting}">
                  {{ getCameraStatus() }}
                </span>
              </div>
              <div class="info-row">
                <span class="info-label">分辨率:</span>
                <span class="info-value">{{ videoWidth }} × {{ videoHeight }}</span>
              </div>
            </div>
            
            <div class="section-divider" v-if="isDetecting">
              <span><i class="el-icon-data-analysis"></i> 检测统计</span>
            </div>
            
            <div class="detection-info" v-if="isDetecting">
              <div class="info-row">
                <span class="info-label">已检测:</span>
                <span class="info-value">{{ detectionCount }} 帧</span>
              </div>
              <div class="info-row">
                <span class="info-label">检测帧率:</span>
                <span class="info-value">{{ detectionFps.toFixed(1) }} fps</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧视频区域 -->
        <div class="right-panel">
          <div class="video-container">
            <div class="video-wrapper">
              <div class="video-header">
                <div class="camera-icon">
                  <i class="el-icon-video-camera"></i>
                  <span>实时视频监控</span>
                </div>
                <div class="camera-status" :class="{ 'active': stream, 'detecting': isDetecting }">
                  {{ getCameraStatus() }}
                </div>
              </div>
              <div class="video-frame">
                <video id="video" autoplay playsinline width="640" height="480"></video>
                <canvas id="canvas" v-show="isDetecting" width="640" height="480"></canvas>
                <div v-if="!stream" class="no-camera">
                  <i class="el-icon-video-camera-solid"></i>
                  <p>请点击"调用摄像头"按钮开始</p>
                </div>
              </div>
              <div class="video-footer">
                <div class="time-display">{{ currentTime }}</div>
                <div class="detection-badge" v-if="isDetecting">
                  <i class="el-icon-warning-outline"></i> 实时检测中
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getPlace, sendRealtimeAlert, getDetectModelConfig, switchDetectModel } from '@/services/api';

export default {
  data() {
    return {
      stream: null,
      isDetecting: false,
      isLoading: false,
      canvas: null,
      ctx: null,
      videoElement: null,
      detectionInterval: null,
      errorMessage: '',
      apiUrl: 'http://localhost:5000/api/detect', // Flask后端API路径
      apiHealthUrl: 'http://localhost:5000/api/health', // 健康检查API路径
      lastFrameTime: 0,
      detectionCount: 0,
      detectionFps: 0,
      framesSinceLastFpsUpdate: 0,
      lastFpsUpdateTime: 0,
      videoWidth: 640,
      videoHeight: 640,
      currentTime: '',
      timeUpdateInterval: null,
      lastRealtimeAlertAt: 0,
      errorCount: 0,
      selectedPlaceId: null,
      placeOptions: [],
      selectedModelName: '',
      modelOptions: [],
      isSwitchingModel: false
    };
  },
  methods: {
    startCamera() {
      this.errorMessage = '';
      this.videoElement = document.getElementById('video');
      if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        navigator.mediaDevices.getUserMedia({ 
          video: {
            width: { exact: this.videoWidth },
            height: { exact: this.videoHeight }
          } 
        })
          .then(stream => {
            this.stream = stream;
            this.videoElement.srcObject = stream;
            
            // 监听视频元数据加载，确保获取正确的视频尺寸
            this.videoElement.onloadedmetadata = () => {
              this.videoWidth = this.videoElement.videoWidth;
              this.videoHeight = this.videoElement.videoHeight;
            };
          })
          .catch(error => {
            console.error('Error accessing media devices.', error);
            // 如果指定分辨率失败，尝试使用默认设置
            this.errorMessage = '无法以指定分辨率访问摄像头，尝试使用默认设置...';
            
            // 使用默认设置重试
            navigator.mediaDevices.getUserMedia({ video: true })
              .then(stream => {
                this.stream = stream;
                this.videoElement.srcObject = stream;
                
                // 监听视频元数据加载，获取实际视频尺寸
                this.videoElement.onloadedmetadata = () => {
                  this.videoWidth = this.videoElement.videoWidth;
                  this.videoHeight = this.videoElement.videoHeight;
                  this.errorMessage = '';
                };
              })
              .catch(defaultError => {
                console.error('Error accessing media devices with default settings.', defaultError);
                this.errorMessage = '无法访问摄像头，请检查权限设置';
              });
          });
      } else {
        console.error('getUserMedia not supported on your browser!');
        this.errorMessage = '您的浏览器不支持摄像头访问';
      }
    },
    stopCamera() {
      this.stopDetection();
      if (this.stream) {
        this.stream.getTracks().forEach(track => track.stop());
        this.stream = null;
        this.videoElement.srcObject = null;
      }
    },
    setupCanvas() {
      this.canvas = document.getElementById('canvas');
      this.ctx = this.canvas.getContext('2d');
      
      // 设置canvas与视频相同大小
      this.canvas.width = this.videoWidth;
      this.canvas.height = this.videoHeight;
      
      // 定位canvas覆盖在视频上方
      this.canvas.style.position = 'absolute';
      this.canvas.style.top = '0';
      this.canvas.style.left = '0';
    },
    startDetection() {
      if (!this.stream) return;
      if (!this.selectedPlaceId) {
        this.errorMessage = "请先选择监测地点";
        return;
      }
      
      // 重置计数器和错误信息
      this.errorMessage = '';
      this.detectionCount = 0;
      this.detectionFps = 0;
      this.lastFpsUpdateTime = Date.now();
      this.framesSinceLastFpsUpdate = 0;
      this.errorCount = 0;
      
      this.isDetecting = true;
      this.setupCanvas();
      
      // 设置检测间隔，每200ms发送一帧到后端（降低频率减轻后端压力）
      this.captureAndSendFrame(); // 立即执行一次
      this.detectionInterval = setInterval(() => {
        this.captureAndSendFrame();
      }, 200);
    },
    stopDetection() {
      this.isDetecting = false;
      this.isLoading = false;
      if (this.detectionInterval) {
        clearInterval(this.detectionInterval);
        this.detectionInterval = null;
      }
      
      // 隐藏Canvas，显示原始视频
      if (this.canvas) {
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
      }
    },
    captureAndSendFrame() {
      if (!this.isDetecting || !this.stream) return;
      
      // 如果上一帧还在处理中，跳过这一帧
      if (this.isLoading) return;
      
      this.isLoading = true;
      
      // 在canvas上绘制当前视频帧
      this.ctx.drawImage(this.videoElement, 0, 0, this.canvas.width, this.canvas.height);
      
      // 获取图像数据 - 使用较低质量以提高传输速度
      const imageData = this.canvas.toDataURL('image/jpeg', 0.7);
      const base64Data = imageData.split(',')[1]; // 移除data:image/jpeg;base64,前缀
      
      // 记录当前时间
      this.lastFrameTime = Date.now();
      
      // 发送到后端进行检测
      fetch(this.apiUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ 
          image: base64Data,
          timestamp: this.lastFrameTime
        })
      })
      .then(response => {
        if (!response.ok) {
          throw new Error(`后端服务返回错误: ${response.status}`);
        }
        return response.json();
      })
      .then(data => {
        // 处理检测结果
        if (this.isDetecting) { // 确保仍处于检测状态
          this.displayDetectionResults(data);
          this.handleRealtimeAlert(data);
          this.detectionCount++;
          
          // 计算FPS
          this.framesSinceLastFpsUpdate++;
          const now = Date.now();
          const elapsed = now - this.lastFpsUpdateTime;
          if (elapsed > 1000) { // 每秒更新一次FPS
            this.detectionFps = (this.framesSinceLastFpsUpdate * 1000) / elapsed;
            this.framesSinceLastFpsUpdate = 0;
            this.lastFpsUpdateTime = now;
          }
        }
        this.isLoading = false;
      })
      .catch(error => {
        console.error('与后端通信错误:', error);
        this.errorMessage = `检测失败: ${error.message}`;
        this.errorCount += 1;
        this.isLoading = false;
        
        // 如果连续出错，考虑停止检测
        if (this.errorCount > 5) {
          this.stopDetection();
        }
      });
    },

    handleRealtimeAlert(data) {
      if (!data || !Array.isArray(data.detections) || data.detections.length === 0) {
        return;
      }

      const riskDetections = data.detections.filter((item) => {
        const label = String(item.label || '').toLowerCase();
        const confidence = Number(item.confidence || item.score || 0);
        return (label.includes('fire') && confidence >= 0.30) || (label.includes('smoke') && confidence >= 0.25);
      });

      if (riskDetections.length === 0) {
        return;
      }

      const now = Date.now();
      if (now - this.lastRealtimeAlertAt < 60 * 1000) {
        return;
      }

      this.lastRealtimeAlertAt = now;
      sendRealtimeAlert({
        placeId: this.selectedPlaceId,
        detections: riskDetections.map((item) => ({
          label: item.label,
          confidence: item.confidence || item.score || 0,
          box: item.box
        }))
      }).catch((e) => {
        console.error('实时告警调用失败:', e);
      });
    },
    displayDetectionResults(data) {
      // 清除画布
      this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
      
      // 首先绘制原始图像
      this.ctx.drawImage(this.videoElement, 0, 0, this.canvas.width, this.canvas.height);
      
      // 处理后端返回的检测结果
      if (data && data.detections && data.detections.length > 0) {
        data.detections.forEach(detection => {
          const [x, y, width, height] = detection.box;
          const score = detection.confidence || detection.score || 0;
          const label = `${detection.label}: ${(score * 100).toFixed(1)}%`;
          
          // 绘制边界框
          this.ctx.strokeStyle = '#00FF00';
          this.ctx.lineWidth = 3;
          this.ctx.strokeRect(x, y, width, height);
          
          // 绘制标签背景
          this.ctx.fillStyle = 'rgba(0, 255, 0, 0.5)';
          this.ctx.fillRect(x, y - 25, label.length * 8, 25);
          
          // 绘制标签文本
          this.ctx.fillStyle = '#000000';
          this.ctx.font = 'bold 16px Arial';
          this.ctx.fillText(label, x + 5, y - 7);
        });
      }
    },
    async loadPlaceOptions() {
      try {
        const response = await getPlace();
        if (response && response.status && Array.isArray(response.data)) {
          this.placeOptions = response.data;
          if (!this.selectedPlaceId && this.placeOptions.length > 0) {
            this.selectedPlaceId = this.placeOptions[0].id;
          }
        }
      } catch (error) {
        console.error('获取地点列表失败:', error);
      }
    },
    async loadModelOptions() {
      try {
        const response = await getDetectModelConfig();
        if (!response || !response.models) {
          this.errorMessage = '获取模型配置失败';
          return;
        }
        this.modelOptions = Object.entries(response.models).map(([name, path]) => ({
          name,
          label: `${name} (${path})`
        }));
        this.selectedModelName = response.current_model || '';
      } catch (error) {
        console.error('加载模型配置失败:', error);
      }
    },
    async handleModelChange(modelName) {
      if (!modelName) return;
      if (this.isDetecting) {
        this.errorMessage = '请先停止检测再切换模型';
        return;
      }
      this.isSwitchingModel = true;
      this.errorMessage = '';
      try {
        const response = await switchDetectModel(modelName);
        if (!response || response.success === false) {
          this.errorMessage = response && response.error ? response.error : '模型切换失败';
          return;
        }
        this.$message.success(`已切换到模型: ${response.current_model}`);
      } catch (error) {
        console.error('模型切换失败:', error);
        this.errorMessage = '模型切换失败';
      } finally {
        this.isSwitchingModel = false;
      }
    },
    // 检查后端健康状态
    checkBackendHealth() {
      fetch(this.apiHealthUrl)
        .then(response => response.json())
        .then(data => {
          if (data.status === 'ok') {
            console.log('后端服务正常运行');
          } else {
            this.errorMessage = '后端服务状态异常';
          }
        })
        .catch(error => {
          console.error('无法连接到后端服务:', error);
          this.errorMessage = '无法连接到后端服务，请确保服务已启动';
        });
    },
    // 获取摄像头状态文本
    getCameraStatus() {
      if (!this.stream) return '未开启';
      if (this.isDetecting) return '目标检测中';
      return '摄像头已开启';
    },
    // 更新当前时间
    updateCurrentTime() {
      const now = new Date();
      const hours = now.getHours().toString().padStart(2, '0');
      const minutes = now.getMinutes().toString().padStart(2, '0');
      const seconds = now.getSeconds().toString().padStart(2, '0');
      this.currentTime = `${hours}:${minutes}:${seconds}`;
    }
  },
  mounted() {
    // this.startCamera(); // 注释掉自动启动摄像头
    this.checkBackendHealth(); // 页面加载时检查后端状态
    this.loadPlaceOptions();
    this.loadModelOptions();
    
    // 开始时间更新
    this.updateCurrentTime();
    this.timeUpdateInterval = setInterval(() => {
      this.updateCurrentTime();
    }, 1000);
  },
  beforeUnmount() {
    this.stopCamera();
    
    // 清除时间更新定时器
    if (this.timeUpdateInterval) {
      clearInterval(this.timeUpdateInterval);
    }
  }
}
</script>

<style scoped>
.monitor-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 90vh;
  background-color: #f5f7fa;
}

.monitor-card {
  width: 95%;
  height: 90%;
  padding: 20px;
  overflow: auto;
}

.page-layout {
  display: flex;
  height: 100%;
}

.left-panel {
  width: 30%;
  padding-right: 20px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #ebeef5;
  overflow-y: auto;
}

.panel-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
  display: flex;
  align-items: center;
}

.panel-title i {
  margin-right: 8px;
  color: #409EFF;
  font-size: 20px;
}

.right-panel {
  width: 70%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.control-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.control-panel .el-button {
  margin: 10px;
  width: 80%;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s;
  height: 40px;
  border-radius: 20px;
}

.control-panel .el-button i {
  margin-right: 5px;
  font-size: 16px;
}

.control-panel .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.section-divider {
  display: flex;
  align-items: center;
  margin: 15px 0 10px;
  color: #606266;
  font-size: 14px;
  font-weight: bold;
}

.section-divider:before, .section-divider:after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: #dcdfe6;
}

.section-divider:before {
  margin-right: 10px;
}

.section-divider:after {
  margin-left: 10px;
}

.section-divider i {
  margin-right: 5px;
  color: #409EFF;
}

.info-section {
  margin-top: 20px;
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 5px 0;
  border-bottom: 1px dashed #ebeef5;
}

.info-label {
  color: #606266;
  font-weight: bold;
}

.info-value {
  color: #303133;
}

.status-active {
  color: #67c23a;
}

.status-detecting {
  color: #e6a23c;
}

.video-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.video-wrapper {
  width: 90%;
  height: 90%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  border: 1px solid #dcdfe6;
  background-color: #2c3e50;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.video-wrapper:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.video-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #34495e;
  padding: 10px 15px;
  color: white;
  height: 40px;
  border-bottom: 1px solid #2c3e50;
}

.camera-icon {
  display: flex;
  align-items: center;
}

.camera-icon i {
  margin-right: 8px;
  font-size: 18px;
}

.camera-status {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 14px;
  background-color: #95a5a6;
  color: white;
  transition: all 0.3s ease;
}

.camera-status.active {
  background-color: #27ae60;
}

.camera-status.detecting {
  background-color: #f39c12;
  animation: pulse 1.5s infinite;
}

.video-frame {
  position: relative;
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #000;
}

.video-frame video, .video-frame canvas {
  width: 640px;
  height: 480px;
  object-fit: contain;
}

.video-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #2c3e50;
  padding: 8px 15px;
  color: white;
  height: 30px;
  border-top: 1px solid #34495e;
}

.time-display {
  font-family: monospace;
  font-size: 14px;
  color: #ecf0f1;
}

.detection-badge {
  display: flex;
  align-items: center;
  background-color: #e74c3c;
  padding: 3px 8px;
  border-radius: 10px;
  font-size: 12px;
  animation: pulse 1.5s infinite;
}

.detection-badge i {
  margin-right: 4px;
  font-size: 14px;
}

.no-camera {
  position: absolute;
  top: 50%; /* 让它上下居中 */
  left: 35%; /* 让它离左侧 1/4 处 */
  transform: translateY(-50%); /* 调整自身居中 */
  color: #95a5a6;
}

.no-camera i {
  font-size: 50px;
  margin-bottom: 15px;
  color: #bdc3c7;
}

.no-camera p {
  font-size: 16px;
  color: #7f8c8d;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.loading-spinner {
  border: 5px solid rgba(255, 255, 255, 0.3);
  border-top: 5px solid #ffffff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
}

.loading-text {
  color: white;
  margin-top: 10px;
  font-weight: bold;
}

.error-message {
  color: #f56c6c;
  text-align: left;
  margin-top: 10px;
  padding: 10px 15px;
  background-color: #fef0f0;
  border-radius: 4px;
  border-left: 4px solid #f56c6c;
  display: flex;
  align-items: center;
}

.error-message i {
  font-size: 18px;
  margin-right: 8px;
}

.camera-info, .detection-info {
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

@media (max-width: 1200px) {
  .page-layout {
    flex-direction: column;
  }
  
  .left-panel, .right-panel {
    width: 100%;
    padding-right: 0;
    border-right: none;
  }
  
  .left-panel {
    margin-bottom: 20px;
    max-height: 300px;
    border-bottom: 1px solid #ebeef5;
    padding-bottom: 20px;
  }
}
</style>
