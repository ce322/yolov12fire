<template>
  <div class="monitor-container">
    <el-card class="monitor-card">
      <!-- 筛选条件 -->
      <el-form :model="filterForm" label-width="80px" style="margin-bottom: 20px" inline class="filter-form">
        <!-- 省份 -->
        <el-form-item label="省份">
          <el-input v-model="filterForm.province" placeholder="请输入省份" style="width: 160px"></el-input>
        </el-form-item>

        <!-- 市 -->
        <el-form-item label="市">
          <el-input v-model="filterForm.town" placeholder="请输入市" style="width: 160px"></el-input>
        </el-form-item>

        <!-- 区 -->
        <el-form-item label="区">
          <el-input v-model="filterForm.area" placeholder="请输入区" style="width: 160px"></el-input>
        </el-form-item>

        <!-- 责任人 -->
        <el-form-item label="责任人">
          <el-input v-model="filterForm.duty" placeholder="请输入责任人" style="width: 160px"></el-input>
        </el-form-item>

        <!-- 状态 -->
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>

        <!-- 筛选按钮 -->
        <el-form-item>
          <el-button type="primary" @click="filterPlaces">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="success" @click="exportExcel">导出Excel</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 添加地点按钮 -->
      <div class="button-container">
        <el-button type="primary" @click="openAddDialog">添加地点</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        :data="pagedPlaceList"
        stripe
        style="width: 100%"
        :cell-style="cellStyle"
        :header-cell-style="headerCellStyle"
        v-loading="loading"
      >
        <!-- 地点图片 -->
        <el-table-column label="地点图片" width="150">
          <template #default="{ row }">
            <div class="image-container">
              <el-image 
                :src="row.pic || defaultImage" 
                fit="cover" 
                class="place-image"
                :preview-src-list="[row.pic || defaultImage]"
                :preview-teleported="true"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
          </template>
        </el-table-column>

        <!-- 地点名称 -->
        <el-table-column prop="name" label="地点名称" width="150" />

        <!-- 省份 -->
        <el-table-column prop="province" label="省份" width="100" />

        <!-- 市区 -->
        <el-table-column prop="town" label="市" width="100" />

        <!-- 区域 -->
        <el-table-column prop="area" label="区" width="100" />

        <!-- 地址 -->
        <el-table-column prop="address" label="地址" />

        <!-- 负责人 -->
        <el-table-column prop="duty" label="负责人" width="150" />

        <!-- 负责人手机号 -->
        <el-table-column prop="duty_tel" label="负责人手机号" width="150" />

        <!-- 状态 -->
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? "启用" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="editPlace(row)"
                >详情</el-button
              >
              <el-button type="danger" size="small" @click="deletePlace(row)"
                >删除</el-button
              >
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="handlePageChange"
      />

      <!-- 添加地点对话框 -->
      <el-dialog v-model="addDialogVisible" title="添加地点" width="500px">
        <el-form :model="addForm" label-width="100px">
          <el-form-item label="地点图片">
            <el-upload
              :http-request="handleImageUpload"
              :before-upload="beforeImageUpload"
              :limit="1"
            >
              <el-button type="primary">上传图片</el-button>
              <template #tip>
                <div class="el-upload__tip">请上传地点图片，且不超过 2MB</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item label="地点名称">
            <el-input v-model="addForm.name" placeholder="请输入地点名称" />
          </el-form-item>
          <el-form-item label="省份">
            <el-input v-model="addForm.province" placeholder="请输入省份" />
          </el-form-item>
          <el-form-item label="市">
            <el-input v-model="addForm.town" placeholder="请输入市" />
          </el-form-item>
          <el-form-item label="区">
            <el-input v-model="addForm.area" placeholder="请输入区" />
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="addForm.address" placeholder="请输入地址" />
          </el-form-item>
          <el-form-item label="负责人">
            <el-input v-model="addForm.duty" placeholder="请输入负责人" />
          </el-form-item>
          <el-form-item label="负责人手机号">
            <el-input
              v-model="addForm.duty_tel"
              placeholder="请输入负责人手机号"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAddPlace">确认</el-button>
        </template>
      </el-dialog>

      <!-- 编辑地点对话框 -->
      <el-dialog v-model="editDialogVisible" title="详情" width="500px">
        <el-form :model="editForm" label-width="100px">
          <el-form-item label="地点图片">
            <div class="preview-image" v-if="editForm.picUrl">
              <el-image 
                :src="editForm.picUrl" 
                fit="cover" 
                style="width: 150px; height: 90px; margin-bottom: 10px;" 
                :preview-src-list="[editForm.picUrl]"
              />
            </div>
            <el-upload
              :http-request="handleEditImageUpload"
              :before-upload="beforeImageUpload"
              :limit="1"
            >
              <el-button type="primary">上传图片</el-button>
              <template #tip>
                <div class="el-upload__tip">请上传地点图片，且不超过 2MB</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item label="地点名称">
            <el-input v-model="editForm.name" placeholder="请输入地点名称" />
          </el-form-item>
          <el-form-item label="省份">
            <el-input v-model="editForm.province" placeholder="请输入省份" />
          </el-form-item>
          <el-form-item label="市">
            <el-input v-model="editForm.town" placeholder="请输入市" />
          </el-form-item>
          <el-form-item label="区">
            <el-input v-model="editForm.area" placeholder="请输入区" />
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="editForm.address" placeholder="请输入地址" />
          </el-form-item>
          <el-form-item label="负责人">
            <el-input v-model="editForm.duty" placeholder="请输入负责人" />
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input
              v-model="editForm.duty_tel"
              placeholder="请输入负责人联系方式"
            />
          </el-form-item>
          <!-- 状态 -->
          <el-form-item label="状态">
            <el-select v-model="editForm.status" placeholder="请选择状态">
              <el-option label="禁用" :value="0" />
              <el-option label="启用" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-input v-model="editForm.create_time" disabled />
          </el-form-item>
          <el-form-item label="更新时间">
            <el-input v-model="editForm.update_time" disabled />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmEditPlace">保存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { ElMessage } from "element-plus";
import {
  placeList,
  placeDetail,
  placeDelete,
  placeUpdate,
  placeAdd,
  queryAddress,
  uploadFile
} from "@/services/api";
import { Picture } from '@element-plus/icons-vue';
import { exportToExcelLikeCsv } from '@/utils/exportExcel';

export default {
  name: "PlaceAdditionPage",
  components: {
    Picture
  },
  data() {
    return {
      placeList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      addDialogVisible: false,
      editDialogVisible: false,
      loading: false,
      defaultImage: require("@/assets/default-avatar.png"),
      addForm: {
        pic: "",
        name: "",
        address: "",
        duty: "",
        duty_tel: "",
        province: "",
        area: "",
        town: "",
      },
      editForm: {
        id: null,
        pic: "",
        name: "",
        status: "",
        address: "",
        duty: "",
        duty_tel: "",
        province: "",
        area: "",
        town: "",
        create_time: "",
        update_time: "",
      },
      filterForm: {
        province: "",
        town: "",
        area: "",
        duty: "",
        status: "",
      },
    };
  },
  computed: {
    pagedPlaceList() {
      return this.placeList;
    },
    cellStyle() {
      return { verticalAlign: "middle", textAlign: "center" };
    },
    headerCellStyle() {
      return { verticalAlign: "middle", textAlign: "center" };
    },
  },
  created() {
    this.fetchPlaceList();
  },
  methods: {
    exportExcel() {
      const rows = (this.fireRecords || this.placeList || this.deviceList || this.checkList || []).map((item) => item);
      const headers = this.getExportHeaders();
      const mappedRows = rows.map((item) => {
        const r = {};
        headers.forEach((h) => { r[h.key] = typeof h.value === "function" ? h.value(item) : item[h.key]; });
        return r;
      });
      exportToExcelLikeCsv(this.getExportFileName(), headers, mappedRows);
    },
    getExportFileName() {
      return this.$route?.path === "/fireRecord" ? "火灾记录" : this.$route?.path === "/place" ? "地点记录" : this.$route?.path === "/device" ? "设备记录" : "检查记录";
    },
    getExportHeaders() {
      if (this.$route?.path === "/fireRecord") return [
        { key: "startTime", label: "开始时间", value: (i) => this.formatDateTime(i.startTime) },
        { key: "endTime", label: "结束时间", value: (i) => this.formatDateTime(i.endTime) },
        { key: "place", label: "地点", value: (i) => this.placeMap[i.placeId] || "未知地点" },
        { key: "fireFlag", label: "起火", value: (i) => i.fireFlag === 1 ? "是" : "否" },
        { key: "smokeFlag", label: "烟雾", value: (i) => i.smokeFlag === 1 ? "有" : "无" },
        { key: "reason", label: "原因" },
        { key: "situation", label: "情况" }
      ];
      if (this.$route?.path === "/place") return [
        { key: "name", label: "地点名称" },{ key: "province", label: "省份" },{ key: "town", label: "市" },{ key: "area", label: "区" },{ key: "address", label: "地址" },{ key: "duty", label: "负责人" },{ key: "duty_tel", label: "负责人手机号" },{ key: "status", label: "状态", value: (i)=>i.status===1?"启用":"禁用" }
      ];
      if (this.$route?.path === "/device") return [
        { key: "name", label: "设备编号" },{ key: "place", label: "地点", value:(i)=>this.placeMap[i.place_id]||"未知地点" },{ key: "res", label: "资源" },{ key: "duty", label: "负责人" },{ key: "duty_tel", label: "负责人手机号" },{ key: "check_time", label: "检查时间", value:(i)=>this.formatDateTime(i.check_time) },{ key: "status", label: "状态", value:(i)=>i.status===1?"启用":"禁用" }
      ];
      return [
        { key: "duty", label: "责任人" },{ key: "duty_tel", label: "责任人联系方式" },{ key: "place", label: "地点", value:(i)=>this.placeMap[i.place]||"" },{ key: "d_time", label: "持续时间(h)" },{ key: "time", label: "检查时间" },{ key: "score", label: "评分", value:(i)=>this.scoreLabel?this.scoreLabel(i.score):i.score },{ key: "situation", label: "情况" }
      ];
    },
    // 获取地点列表
    async fetchPlaceList() {
      try {
        this.loading = true;
        const filters = {
          duty: this.filterForm.duty || null,
          province: this.filterForm.province || null,
          town: this.filterForm.town || null,
          area: this.filterForm.area || null,
          status: this.filterForm.status === "" ? null : this.filterForm.status
        };
        
        const response = await placeList(
          this.currentPage,
          this.pageSize,
          filters
        );
        if (response.code === 200) {
          this.placeList = response.data.records;
          this.total = response.data.total;
          
          // 处理图片显示
          this.processPlaceImages();
        }
      } catch (error) {
        console.error("获取地点列表失败:", error);
      } finally {
        this.loading = false;
      }
    },
    
    // 处理地点图片显示
    async processPlaceImages() {
      if (!this.placeList || this.placeList.length === 0) return;
      
      for (const place of this.placeList) {
        if (place.pic) {
          try {
            const response = await queryAddress(place.pic);
            if (response && response.data && response.data.url) {
              place.pic = response.data.url;
            } else {
              place.pic = this.defaultImage;
            }
          } catch (error) {
            console.error('获取图片地址失败:', error);
            place.pic = this.defaultImage;
          }
        } else {
          place.pic = this.defaultImage;
        }
      }
    },

    // 打开添加地点对话框
    openAddDialog() {
      this.addDialogVisible = true;
    },

    // 确认添加地点
    async confirmAddPlace() {
      if (
        !this.addForm.name ||
        !this.addForm.address ||
        !this.addForm.duty ||
        !this.addForm.duty_tel ||
        !this.addForm.pic
      ) {
        ElMessage.warning("请填写完整信息");
        return;
      }
      try {
        const response = await placeAdd(this.addForm);

        if (response && response.code === 200) {
          this.addDialogVisible = false;
          this.resetAddForm();
          this.fetchPlaceList();
        }
      } catch (error) {
        console.error("添加地点错误:", error);
      }
    },

    // 重置添加表单
    resetAddForm() {
      this.addForm = {
        pic: "",
        name: "",
        address: "",
        duty: "",
        duty_tel: "",
        province: "",
        area: "",
        town: "",
      };
    },

    // 打开编辑地点对话框
    async editPlace(row) {
      try {
        const response = await placeDetail(row.id); // 调用获取详情接口
        if (response.code === 200) {
          const placeData = response.data.object;
          this.editForm = {
            id: placeData.id,
            name: placeData.name,
            address: placeData.address,
            pic: placeData.pic || "",
            duty: placeData.duty,
            duty_tel: placeData.duty_tel,
            province: placeData.province,
            town: placeData.town,
            area: placeData.area,
            status: placeData.status,
            create_time: this.formatDate(placeData.create_time), 
            update_time: this.formatDate(placeData.update_time), 
          };
          
          // 处理图片显示
          if (this.editForm.pic) {
            try {
              const imageResponse = await queryAddress(this.editForm.pic);
              if (imageResponse && imageResponse.data && imageResponse.data.url) {
                this.editForm.picUrl = imageResponse.data.url; // 存储显示URL
              } else {
                this.editForm.picUrl = this.defaultImage;
              }
            } catch (error) {
              console.error('获取图片地址失败:', error);
              this.editForm.picUrl = this.defaultImage;
            }
          } else {
            this.editForm.picUrl = this.defaultImage;
          }
          
          this.editDialogVisible = true;
        }
      } catch (error) {
        console.error("获取地点详情失败:", error);
      }
    },

    formatDate(dateString) {
      if (!dateString) return "";
      const date = new Date(dateString);
      return date.toLocaleString(); // 根据需要调整日期格式
    },

    // 确认编辑地点
    async confirmEditPlace() {
      try {
        const response = await placeUpdate(this.editForm);
        if (response.code === 200) {
          this.editDialogVisible = false;
          this.fetchPlaceList();
        }
      } catch (error) {
        console.error("更新地点失败:", error);
      }
    },

    // 删除地点
    async deletePlace(row) {
      try {
        await this.$confirm("确定删除该地点吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
        const response = await placeDelete(row.id);
        if (response.code === 200) {
          this.fetchPlaceList();
        }
      } catch (error) {
        if (error !== "cancel") {
          console.error("删除地点失败:", error);
        }
      }
    },

    // 分页切换
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchPlaceList();
    },

    // 图片上传处理
    async handleImageUpload({ file }) {
      try {
        const response = await uploadFile(file);

        if (response && response.code === 200) {
          this.addForm.pic = response.data.id;
        }
      } catch (error) {
        console.error("图片上传错误:", error);
      }
    },

    // 编辑图片上传处理
    async handleEditImageUpload({ file }) {
      try {
        const response = await uploadFile(file);

        if (response && response.code === 200) {
          this.editForm.pic = response.data.id;
        }
      } catch (error) {
        console.error("图片上传错误:", error);
      }
    },

    // 图片上传前的校验
    beforeImageUpload(file) {
      const isImage = file.type.startsWith("image/");
      const isLt2MB = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        ElMessage.error("只能上传图片文件");
        return false;
      }
      if (!isLt2MB) {
        ElMessage.error("图片大小不能超过 2MB");
        return false;
      }
      return true;
    },

    // 筛选地点
    async filterPlaces() {
      this.currentPage = 1;
      this.fetchPlaceList();
    },

    // 重置筛选条件
    resetFilters() {
      this.filterForm = {
        province: "",
        town: "",
        area: "",
        duty: "",
        status: "",
      };
      this.currentPage = 1;
      this.fetchPlaceList();
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

.button-container {
  margin-bottom: 20px;
}

.image-container {
  display: flex;
  justify-content: center;
}

.place-image {
  width: 100px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.filter-form {
  background-color: #f9fafc;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
  position: sticky;
  bottom: 0;
  background: white;
  z-index: 1;
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
</style>

<style>
/* 全局样式确保图片预览显示在最上层 */
.el-image-viewer__wrapper {
  z-index: 2999 !important;
}
</style>