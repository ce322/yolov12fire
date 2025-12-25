<template>
  <div class="upload-container">
    <el-card class="upload-card">
      <el-steps :active="activeStep" finish-status="success" style="margin-bottom: 20px">
        <el-step title="上传视频"></el-step>
        <el-step title="设置参数"></el-step>
        <el-step title="处理完成"></el-step>
      </el-steps>
      
      <!-- 第一步：上传视频 -->
      <div v-if="activeStep === 0">
        <el-form :model="uploadForm" label-width="120px">
          <!-- 上传视频 -->
          <el-upload
            class="upload-demo"
            drag
            :action="''"
            :http-request="customUpload"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-change="handleFileChange"
            :limit="1"
            :file-list="uploadForm.fileList"
            list-type="text"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽视频到此处 或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持格式：MP4、AVI、MOV，最大500MB
              </div>
            </template>
          </el-upload>
        </el-form>
      </div>
      
      <!-- 第二步：设置参数 -->
      <div v-if="activeStep === 1">
        <el-form :model="processForm" label-width="120px">
          <!-- 选择地点 -->
          <el-form-item label="地点" required>
            <el-select
              v-model="processForm.placeId"
              placeholder="请选择地点"
              style="width: 100%"
            >
              <el-option
                v-for="building in buildings"
                :key="building.id"
                :label="building.name"
                :value="building.id"
              />
            </el-select>
          </el-form-item>
          
          <!-- 视频详情 -->
          <el-form-item label="视频详情">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="视频ID">{{ uploadedVideoId }}</el-descriptions-item>
              <el-descriptions-item label="文件名">{{ uploadedVideoName }}</el-descriptions-item>
              <el-descriptions-item label="上传时间">{{ uploadedVideoTime }}</el-descriptions-item>
            </el-descriptions>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 第三步：处理完成 -->
      <div v-if="activeStep === 2">
        <el-result
          icon="success"
          title="视频已提交处理"
          sub-title="系统正在对视频进行处理，处理完成后将生成火灾检测记录。您可以在火灾记录页面查看处理结果。"
        >
        </el-result>
      </div>
      
      <!-- 操作按钮 -->
      <div class="button-container">
        <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep === 0" type="primary" @click="submitUpload" :loading="uploading">
          上传视频
        </el-button>
        <el-button v-if="activeStep === 1" type="primary" @click="startProcessing" :loading="processing">
          开始处理
        </el-button>
        <el-button v-if="activeStep === 2" type="primary" @click="goToFireRecord">
          查看火灾记录
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { uploadVideo, processVideo, getPlace } from "@/services/api";
import { UploadFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

export default {
  name: "UploadPage",
  components: {
    UploadFilled
  },
  data() {
    return {
      // 步骤控制
      activeStep: 0,
      
      // 上传表单
      uploadForm: {
        fileList: [], // 上传的文件列表
      },
      
      // 处理表单
      processForm: {
        placeId: null, // 选择的地点
      },
      
      // 上传后的视频信息
      uploadedVideoId: null,
      uploadedVideoName: "",
      uploadedVideoTime: "",
      
      uploading: false,
      processing: false,
      buildings: [],
      videoFile: null,
    };
  },
  created() {
    this.fetchPlaces();
  },
  methods: {
    // 获取地点列表
    async fetchPlaces() {
      try {
        const response = await getPlace();
        if (response && response.code === 200) {
          this.buildings = response.data;
        } else {
          ElMessage.error("获取地点列表失败");
        }
      } catch (error) {
        console.error("获取地点列表失败:", error);
        ElMessage.error("获取地点列表失败");
      }
    },
    
    // 处理文件变化
    handleFileChange(file) {
      if (file && file.raw) {
        this.videoFile = file.raw;
      } else {
        this.videoFile = null;
      }
    },
    
    // 自定义上传方法
    customUpload(options) {
      this.uploading = true;
      const { file } = options;
      const formData = new FormData();
      formData.append('file', file);
      
      uploadVideo(formData)
        .then(response => {
          this.uploading = false;
          if (response && response.code === 200) {
            ElMessage.success('上传成功');
            // 保存上传成功的视频信息
            this.uploadedVideoId = response.data.id;
            this.uploadedVideoName = response.data.title;
            this.uploadedVideoTime = this.formatDateTime(response.data.uploadTime);
            // 进入下一步
            this.activeStep = 1;
          } else {
            ElMessage.error(response?.msg || '上传失败');
          }
        })
        .catch(() => {
          this.uploading = false;
          ElMessage.error('上传失败，请重试');
        });
    },
    
    // 上传成功回调
    handleUploadSuccess() {
      ElMessage.success("视频上传成功");
    },
    
    // 上传失败回调
    handleUploadError() {
      ElMessage.error("视频上传失败");
    },
    
    // 上传前的校验
    beforeUpload(file) {
      const isVideo = file.type.startsWith("video/");
      const isLt500MB = file.size / 1024 / 1024 < 500;

      if (!isVideo) {
        ElMessage.error("只能上传视频文件");
      }
      if (!isLt500MB) {
        ElMessage.error("视频大小不能超过 500MB");
      }
      return isVideo && isLt500MB;
    },
    
    // 提交上传
    submitUpload() {
      if (this.uploadForm.fileList.length === 0) {
        ElMessage.warning("请上传视频文件");
        return;
      }
      
      // 触发上传
      document.querySelector('.el-upload__input').click();
    },
    
    // 开始处理视频
    startProcessing() {
      if (!this.processForm.placeId) {
        ElMessage.warning("请选择地点");
        return;
      }
      
      this.processing = true;
      
      // 发送处理请求
      processVideo(this.uploadedVideoId, this.processForm.placeId)
        .then(response => {
          this.processing = false;
          if (response && response.code === 200) {
            ElMessage.success('视频处理请求已发送');
            this.activeStep = 2;
          } else {
            ElMessage.error(response?.msg || '处理请求失败');
          }
        })
        .catch(error => {
          this.processing = false;
          ElMessage.error('处理请求失败');
          console.error(error);
        });
    },
    
    // 上一步
    prevStep() {
      if (this.activeStep > 0) {
        this.activeStep--;
      }
    },
    
    // 去视频列表页
    goToVideoList() {
      this.$router.push('/video-list');
    },
    
    // 去火灾记录页
    goToFireRecord() {
      this.$router.push('/fireRecord');
    },
    
    // 重置表单
    resetForm() {
      this.uploadForm = {
        fileList: [],
      };
      this.processForm = {
        placeId: null,
      };
      this.activeStep = 0;
      this.uploadedVideoId = null;
      this.uploadedVideoName = "";
      this.uploadedVideoTime = "";
      this.videoFile = null;
      ElMessage.info("表单已重置");
    },
    
    // 格式化日期时间
    formatDateTime(time) {
      if (!time) return '';
      const date = new Date(time);
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`;
    }
  },
};
</script>

<style scoped>
.upload-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 90vh;
}

.upload-card {
  width: 95%;
  height: 90%;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 使表单和按钮分开 */
}

.el-form-item {
  margin-bottom: 20px;
}

.el-upload__tip {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.time-note {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 上传容器 */
.custom-upload {
  min-height: 220px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px dashed #84bbf1;
  border-radius: 8px;
  background-color: #f9f9f9;
  transition: all 0.3s ease-in-out;
}

.custom-upload:hover {
  border-color: #66b1ff;
  background-color: #eef5ff;
}

/* 上传内容 */
.upload-box {
  text-align: center;
  padding: 20px;
}

.upload-box i {
  font-size: 50px;
  color: #409eff;
  margin-bottom: 10px;
}

/* 按钮容器样式 */
.button-container {
  display: flex;
  justify-content: center; /* 水平居中 */
  margin-top: 20px; /* 与表单内容保持距离 */
}
</style>