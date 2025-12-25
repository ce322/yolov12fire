<template>
  <div class="monitor-container">
    <el-card class="monitor-card">
      <!-- 筛选条件 -->
      <el-form :model="filterForm" label-width="80px" style="margin-bottom: 20px" inline>
        <!-- 时间顺序 -->
        <el-form-item label="时间顺序">
          <el-select v-model="filterForm.timeOrder" placeholder="请选择时间顺序" style="width: 120px">
            <el-option label="升序" value="asc" />
            <el-option label="降序" value="desc" />
          </el-select>
        </el-form-item>

        <!-- 时间范围 -->
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 700px"
          />
        </el-form-item>

        <!-- 是否着火 -->
        <el-form-item label="着火">
          <el-select v-model="filterForm.fireFlag" placeholder="请选择" style="width: 120px">
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
            <el-option label="全部" :value="null" />
          </el-select>
        </el-form-item>
        
        <!-- 是否有烟雾 -->
        <el-form-item label="烟雾">
          <el-select v-model="filterForm.smokeFlag" placeholder="请选择" style="width: 120px">
            <el-option label="有" :value="1" />
            <el-option label="无" :value="0" />
            <el-option label="全部" :value="null" />
          </el-select>
        </el-form-item>

        <!-- 地点选择 -->
        <el-form-item label="地点">
          <el-select v-model="filterForm.placeId" placeholder="请选择地点" clearable style="width: 160px">
            <el-option v-for="location in placeList" :key="location.id" :label="location.name" :value="location.id" />
          </el-select>
        </el-form-item>

        <!-- 筛选按钮 -->
        <el-form-item>
          <el-button type="primary" @click="queryRecords">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table
        :data="pagedFireRecords"
        v-loading="loading"
        stripe style="width: 100%"
        :cell-style="{ verticalAlign: 'middle', textAlign: 'center' }"
        :header-cell-style="{ textAlign: 'center' }"
      >
        <!-- 缩略图 -->
        <el-table-column label="图片" width="150">
          <template #default="{ row }">
            <el-image 
              :src="row.pic || defaultImage" 
              fit="cover" 
              style="width: 100px; height: 60px"
              :preview-src-list="[row.pic || defaultImage]"
              :preview-teleported="true"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>

        <!-- 开始时间 -->
        <el-table-column prop="startTime" label="开始时间" width="120">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>

        <!-- 结束时间 -->
        <el-table-column prop="endTime" label="结束时间" width="120">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>

        <!-- 地点 -->
        <el-table-column label="地点" width="120">
          <template #default="{ row }">
            {{ placeMap[row.placeId] || "未知地点" }}
          </template>
        </el-table-column>

        <!-- 是否起火 -->
        <el-table-column label="起火" width="60">
          <template #default="{ row }">
            <el-tag :type="row.fireFlag === 1 ? 'success' : 'danger'">
              {{ row.fireFlag === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 火灾概率 -->
        <el-table-column label="火灾概率" width="80">
          <template #default="{ row }">
            {{ row.prob ? (row.prob * 100).toFixed(2) : '0' }}%
          </template>
        </el-table-column>
        
        <!-- 是否有烟雾 -->
        <el-table-column label="烟雾" width="60">
          <template #default="{ row }">
            <el-tag :type="row.smokeFlag === 1 ?  'success' : 'danger'">
              {{ row.smokeFlag === 1 ? '有' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 烟雾概率 -->
        <el-table-column label="烟雾概率" width="80">
          <template #default="{ row }">
            {{ row.smokeProb ? (row.smokeProb * 100).toFixed(2) : '0' }}%
          </template>
        </el-table-column>

        <!-- 原因 -->
        <el-table-column prop="reason" label="原因" width="180" />

        <!-- 情况 -->
        <el-table-column prop="situation" label="情况" />

        <!-- 操作列 -->
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editRecord(row)">编辑</el-button>
            <el-button type="success" size="small" @click="getProcessedVideo(row)" disabled>视频</el-button>
            <el-button type="danger" size="small" @click="deleteRecord(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        style="margin-top: 20px; text-align: center"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" title="编辑火灾记录" width="500px">
      <el-form :model="editForm" label-width="100px" ref="editFormRef">
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="editForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="editForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="editForm.place" disabled></el-input>
        </el-form-item>
        <el-form-item label="情况">
          <el-input v-model="editForm.situation" placeholder="请输入情况描述"></el-input>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="editForm.reason" placeholder="请输入起火原因"></el-input>
        </el-form-item>
        <el-form-item label="是否起火">
          <el-select v-model="editForm.fireFlag" placeholder="请选择">
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否有烟雾">
          <el-select v-model="editForm.smokeFlag" placeholder="请选择">
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="火灾概率" >
          <el-input v-model="editForm.probPercent" disabled></el-input>
          <span style="margin-left: 10px;">%</span>
        </el-form-item>
        <el-form-item label="烟雾概率">
          <el-input v-model="editForm.smokeProbPercent" disabled></el-input>
          <span style="margin-left: 10px;">%</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 视频播放对话框 -->
    <el-dialog v-model="videoDialogVisible" title="处理后视频" width="800px">
      <div v-if="processedVideoUrl" class="video-container">
        <video
          ref="videoPlayer"
          controls
          autoplay
          style="width: 100%; max-height: 500px"
          @error="handleVideoError"
        >
          <source :src="processedVideoUrl" type="video/mp4" />
          <source :src="processedVideoUrl" type="video/x-msvideo" />
          <source :src="processedVideoUrl" type="video/avi" />
          您的浏览器不支持视频播放
        </video>
        <div v-if="videoErrorMessage" class="video-error">
          <el-alert
            :title="videoErrorMessage"
            type="error"
            :closable="false"
            show-icon
          />
          <el-button type="primary" @click="downloadVideo" style="margin-top: 10px">
            下载视频文件
          </el-button>
        </div>
      </div>
      <div v-else class="video-loading">
        <el-empty description="暂无处理后视频" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
// getFireDetail
import { fireList, queryFireRecords, updateFire, deleteFire, getFireVideo } from '@/services/api';
import { getPlace, queryThumbAddress } from '@/services/api';
import { ElMessageBox } from 'element-plus';
import dayjs from 'dayjs';
import { Picture} from '@element-plus/icons-vue';


export default {
  name: "FireRecordPage",
  components: {
    Picture,
  },
  data() {
    return {
      defaultImage: require("@/assets/default-avatar.png"),
      loading: false,
      fireRecords: [],
      placeList: [],
      placeMap: {}, // 地点ID到名称的映射
      filterForm: {
        timeOrder: 'desc', // 默认降序
        fireFlag: null, // 是否着火
        smokeFlag: null, // 是否有烟雾
        placeId: null, // 地点
      },
      timeRange: null,
      currentPage: 1, // 当前页码
      pageSize: 10, // 每页显示条数
      total: 0, // 总记录数
      dialogVisible: false,
      videoDialogVisible: false, // 视频播放对话框
      processedVideoUrl: '', // 处理后视频URL
      editForm: {
        id: null,
        startTime: '',
        endTime: '',
        placeId: null,
        reason: '',
        situation: '',
        fireFlag: 1,
        prob: 0,
        probPercent: 0,
        smokeProb: 0,
        smokeProbPercent: 0,
        smokeFlag: 0,
        pic: '',
      },
      videoErrorMessage: '', // 视频播放错误信息
    };
  },
  computed: {
    // 分页后的数据
    pagedFireRecords() {
      return this.fireRecords;
    },
  },
  created() {
    this.fetchLocations();
    this.fetchFireRecords();
  },
  methods: {
    // 格式化日期时间
    formatDateTime(time) {
      return time ? dayjs(time).format("YYYY-MM-DD HH:mm:ss") : "N/A";
    },
    
    // 获取地点列表
    async fetchLocations() {
      try {
        const response = await getPlace();
        if (response && response.status) {
          this.placeList = response.data || [];
          // 创建ID到名称的映射
          this.placeMap = this.placeList.reduce((map, item) => {
            map[item.id] = item.name;
            return map;
          }, {});
        }
      } catch (error) {
        console.error('获取地点列表失败', error);
      }
    },
    
    // 获取火灾记录列表
    async fetchFireRecords() {
      try {
        this.loading = true;
        const response = await fireList(this.currentPage, this.pageSize, {
          timeOrder: this.filterForm.timeOrder
        });
        if (response && response.status) {
          this.fireRecords = response.data.records || [];
          this.total = response.data.total || 0;
          
          // 处理图片显示和获取视频缩略图
          await this.processFireRecordsImages();
          
          // 获取视频信息
          for (const record of this.fireRecords) {
            try {
              const videoResponse = await getFireVideo(record.id);
              if (videoResponse && videoResponse.code === 200 && videoResponse.data) {
                record.videoUrl = videoResponse.data.videoUrl;
                if (videoResponse.data.thumbnailUrl) {
                  record.pic = videoResponse.data.thumbnailUrl;
                }
              }
            } catch (error) {
              console.error('获取视频信息失败:', error);
            }
          }
        }
      } catch (error) {
        console.error('获取火灾记录列表失败', error);
      } finally {
        this.loading = false;
      }
    },
    
    // 处理火灾记录图片显示
    async processFireRecordsImages() {
      if (!this.fireRecords || this.fireRecords.length === 0) return;
      
      for (const record of this.fireRecords) {
        if (record.pic) {
          try {
            // 检查是否已经是URL格式
            if (record.pic.startsWith('http')) {
              continue;
            }
            
            const response = await queryThumbAddress(record.pic);
            if (response && response.code === 200 && response.data && response.data.url) {
              record.pic = response.data.url;
            } else {
              record.pic = this.defaultImage;
            }
          } catch (error) {
            console.error('获取图片地址失败:', error);
            record.pic = this.defaultImage;
          }
        } else {
          record.pic = this.defaultImage;
        }
      }
    },
    
    // 条件查询火灾记录
    async queryRecords() {
      try {
        this.loading = true;
        const queryParam = { ...this.filterForm };
        
        // 处理时间范围
        if (this.timeRange && this.timeRange.length === 2) {
          queryParam.startTimeBegin = this.timeRange[0];
          queryParam.startTimeEnd = this.timeRange[1];
        }
        
        console.log('查询条件：', queryParam);
        
        const response = await queryFireRecords(queryParam);
        if (response && response.status) {
          this.fireRecords = response.data.records || [];
          this.total = response.data.total || 0;
          this.currentPage = 1; // 重置到第一页
          
          // 处理图片显示和获取视频缩略图
          await this.processFireRecordsImages();
          
          // 获取视频信息
          for (const record of this.fireRecords) {
            try {
              const videoResponse = await getFireVideo(record.id);
              if (videoResponse && videoResponse.code === 200 && videoResponse.data) {
                record.videoUrl = videoResponse.data.videoUrl;
                if (videoResponse.data.thumbnailUrl) {
                  record.pic = videoResponse.data.thumbnailUrl;
                }
              }
            } catch (error) {
              console.error('获取视频信息失败:', error);
            }
          }
          
          if (this.fireRecords.length === 0) {
            console.log('没有找到符合条件的记录');
          }
        }
      } catch (error) {
        console.error('查询火灾记录失败', error);
      } finally {
        this.loading = false;
      }
    },
    
    // 更新火灾状态
    async updateFireFlag(row) {
      try {
        const response = await updateFire(row.id, {
          fireFlag: row.fireFlag
        });
        
        if (!(response && response.status)) {
          // 恢复原值
          this.fetchFireRecords();
        }
      } catch (error) {
        console.error('更新失败', error);
        // 恢复原值
        this.fetchFireRecords();
      }
    },
    
    // 更新烟雾状态
    async updateSmokeFlag(row) {
      try {
        const response = await updateFire(row.id, {
          smokeFlag: row.smokeFlag
        });
        
        if (!(response && response.status)) {
          // 恢复原值
          this.fetchFireRecords();
        }
      } catch (error) {
        console.error('更新失败', error);
        // 恢复原值
        this.fetchFireRecords();
      }
    },
    
    // 编辑记录
    editRecord(row) {
      this.editForm = { ...row };
      
      // 百分比转换
      if (this.editForm.prob !== undefined && this.editForm.prob !== null) {
        this.editForm.probPercent = (this.editForm.prob * 100).toFixed(2);
      } else {
        this.editForm.probPercent = 0;
      }
      
      if (this.editForm.smokeProb !== undefined && this.editForm.smokeProb !== null) {
        this.editForm.smokeProbPercent = (this.editForm.smokeProb * 100).toFixed(2);
      } else {
        this.editForm.smokeProbPercent = 0;
      }
      
      // 设置地点显示
      this.editForm.place = this.placeMap[this.editForm.placeId] || "未知地点";
      
      // 确保编辑框图片显示正确
      if (!this.editForm.pic || this.editForm.pic === this.defaultImage) {
        this.editForm.pic = '';
      }
      
      this.dialogVisible = true;
    },
    
    // 获取处理后的视频
    async getProcessedVideo(row) {
      try {
        // 如果已经有视频URL，直接使用
        if (row.videoUrl) {
          this.processedVideoUrl = row.videoUrl;
          this.videoDialogVisible = true;
          this.videoErrorMessage = '';
          return;
        }

        console.log('正在获取处理后的视频...');
        
        // 调用接口获取视频信息
        const response = await getFireVideo(row.id);
        
        if (response && response.code === 200) {
          // 设置视频URL
          this.processedVideoUrl = response.data.videoUrl;
          // 保存到记录中
          row.videoUrl = response.data.videoUrl;
          
          // 如果有缩略图，也保存下来
          if (response.data.thumbnailUrl) {
            row.pic = response.data.thumbnailUrl;
          }
          
          // 显示视频对话框
          this.videoDialogVisible = true;
          this.videoErrorMessage = '';
        }
      } catch (error) {
        console.error('获取视频失败', error);
        this.videoErrorMessage = '获取视频失败';
      }
    },
    
    // 下载视频文件
    downloadVideo() {
      if (!this.processedVideoUrl) {
        console.warn('视频URL不存在');
        return;
      }
      
      try {
        // 创建一个临时链接
        const downloadLink = document.createElement('a');
        downloadLink.href = this.processedVideoUrl;
        downloadLink.target = '_blank';
        downloadLink.download = 'fire_video.mp4'; // 默认下载文件名
        
        // 添加到DOM并触发点击
        document.body.appendChild(downloadLink);
        downloadLink.click();
        
        // 移除链接
        document.body.removeChild(downloadLink);
      } catch (error) {
        console.error('下载视频失败', error);
      }
    },
    
    // 提交编辑表单
    submitEdit() {
      this.$refs.editFormRef.validate(async (valid) => {
        if (valid) {
          // 将百分比转换回小数
          const formData = { ...this.editForm };
          if (formData.probPercent !== undefined) {
            formData.prob = parseFloat(formData.probPercent) / 100;
            delete formData.probPercent;
          }
          
          if (formData.smokeProbPercent !== undefined) {
            formData.smokeProb = parseFloat(formData.smokeProbPercent) / 100;
            delete formData.smokeProbPercent;
          }
          
          // 删除不需要的字段
          delete formData.place;
          
          // 转换日期时间格式
          if (formData.startTime) {
            formData.startTime = dayjs(formData.startTime).format('YYYY-MM-DD HH:mm:ss');
          }
          if (formData.endTime) {
            formData.endTime = dayjs(formData.endTime).format('YYYY-MM-DD HH:mm:ss');
          }
          
          try {
            const response = await updateFire(this.editForm.id, formData);
            if (response && response.status) {
              this.dialogVisible = false;
              this.fetchFireRecords();
            }
          } catch (error) {
            console.error('更新记录时出错:', error);
          }
        } else {
          console.log('表单验证失败');
          return false;
        }
      });
    },
    
    // 删除记录
    deleteRecord(row) {
      ElMessageBox.confirm('确定要删除该记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await deleteFire(row.id);
          if (response && response.status) {
            this.fetchFireRecords(); // 刷新列表
          }
        } catch (error) {
          console.error('删除失败', error);
        }
      }).catch(() => {
        // 取消删除
      });
    },
    
    // 分页切换
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchFireRecords();
    },
    
    // 重置筛选条件
    resetFilters() {
      this.filterForm = {
        timeOrder: 'desc',
        fireFlag: null,
        smokeFlag: null,
        placeId: null,
      };
      this.timeRange = null;
      this.currentPage = 1; // 重置到第一页
      this.fetchFireRecords();
    },
    updateProb() {
      this.editForm.prob = this.editForm.probPercent / 100;
    },
    updateSmokeProb() {
      this.editForm.smokeProb = this.editForm.smokeProbPercent / 100;
    },
    handleVideoError(event) {
      this.videoErrorMessage = event.target.error ? event.target.error.message : '未知错误';
    },
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
  overflow: auto; /* 允许内容滚动 */
}

.el-table {
  margin-top: 20px;
}

.el-image {
  border-radius: 4px;
}

.el-pagination {
  margin-top: 20px;
  text-align: center;
  position: sticky; /* 使分页固定在底部 */
  bottom: 0;
  background: white; /* 防止内容遮挡 */
  z-index: 1; /* 确保分页在最上层 */
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.video-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.video-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.image-error {
  width: 100px;
  height: 60px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.video-error {
  margin-top: 10px;
  text-align: center;
}

.fire-record-page {
  padding: 20px;
}

.filter-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

/* 筛选表单样式 */
.el-form {
  background-color: #f9fafc;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
}

.action-buttons {
  margin-left: 10px;
}

.table-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.fire-indicator {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 5px;
}

.fire-indicator.active {
  background-color: #F56C6C;
}

.fire-indicator.inactive {
  background-color: #909399;
}

.video-actions {
  margin-top: 10px;
  text-align: center;
}
</style>

<style>
/* 全局样式确保图片预览显示在最上层 */
.el-image-viewer__wrapper {
  z-index: 2999 !important;
}
</style>