<template>
  <div class="monitor-container">
    <el-card class="monitor-card">
      <!-- 筛选表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="地点">
          <el-select
            v-model="searchForm.place_id"
            placeholder="选择地点"
            style="width: 180px"
          >
            <el-option
              v-for="item in placeList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-input
            v-model="searchForm.duty"
            placeholder="输入负责人名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="选择状态"
            style="width: 120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="exportExcel">导出Excel</el-button>
        </el-form-item>
      </el-form>

      <!-- 添加设备按钮 -->
      <div style="margin-bottom: 20px">
        <el-button type="primary" @click="openAddDialog">添加设备</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        :data="deviceList"
        stripe
        style="width: 100%"
        :cell-style="{ verticalAlign: 'middle', textAlign: 'center' }"
        :header-cell-style="{ textAlign: 'center' }"
        v-loading="loading"
      >
        <!-- 设备图片 -->
        <el-table-column label="设备图片" width="150">
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

        <!-- 设备名称 -->
        <el-table-column prop="name" label="设备编号" width="150" />

        <!-- 地点 -->
        <el-table-column label="地点" width="120">
          <template #default="{ row }">
            {{ placeMap[row.place_id] || "未知地点" }}
          </template>
        </el-table-column>

        <!-- 资源 -->
        <el-table-column prop="res" label="资源" width="150" />

        <!-- 负责人 -->
        <el-table-column prop="duty" label="负责人" width="150" />

        <!-- 负责人手机号 -->
        <el-table-column prop="duty_tel" label="负责人手机号" width="150" />

        <!-- 检查时间 -->
        <el-table-column label="检查时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.check_time) }}
          </template>
        </el-table-column>

        <!-- 状态 -->
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? "启用" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editDevice(row)"
              >详情</el-button
            >
            <el-button type="danger" size="small" @click="deleteDevice(row)"
              >删除</el-button
            >
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

      <!-- 添加设备对话框 -->
      <el-dialog v-model="addDialogVisible" title="添加设备" width="500px">
        <el-form :model="addForm" label-width="120px">
          <el-form-item label="设备图片">
            <el-upload
              :http-request="handleImageUpload"
              :before-upload="beforeImageUpload"
              :limit="1"
            >
              <el-button type="primary">上传图片</el-button>
              <template #tip>
                <div class="el-upload__tip">请上传设备图片，且不超过 2MB</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item label="设备编号" required>
            <el-input v-model="addForm.name" placeholder="请输入编号" />
          </el-form-item>
          <el-form-item label="地点" required>
            <el-select
              v-model="addForm.place_id"
              placeholder="请选择地点"
              filterable
              clearable
            >
              <el-option
                v-for="location in placeList"
                :key="location.id"
                :label="location.name"
                :value="location.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="资源" required>
            <el-input v-model="addForm.res" placeholder="请输入资源" />
          </el-form-item>
          <el-form-item label="负责人" required>
            <el-input v-model="addForm.duty" placeholder="请输入负责人" />
          </el-form-item>
          <el-form-item label="负责人手机号" required>
            <el-input
              v-model="addForm.duty_tel"
              placeholder="请输入负责人手机号"
            />
          </el-form-item>
          <el-form-item label="状态" required>
            <el-select v-model="addForm.status" placeholder="请选择状态">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAddDevice">确认</el-button>
        </template>
      </el-dialog>

      <!-- 编辑设备对话框 -->
      <el-dialog v-model="editDialogVisible" title="设备详情" width="500px">
        <el-form :model="editForm" label-width="100px">
          <el-form-item label="设备图片">
            <div class="preview-image" v-if="editForm.picUrl">
              <el-image
                :src="editForm.picUrl"
                fit="cover"
                style="width: 150px; height: 90px; margin-bottom: 10px"
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
                <div class="el-upload__tip">请上传设备图片，且不超过 2MB</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item label="设备名称" required>
            <el-input v-model="editForm.name" placeholder="请输入设备名称" />
          </el-form-item>
          <el-form-item label="地点" required>
            <el-select
              v-model="editForm.place_id"
              placeholder="请选择地点"
              filterable
              clearable
            >
              <el-option
                v-for="location in placeList"
                :key="location.id"
                :label="location.name"
                :value="location.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="资源" required>
            <el-input v-model="editForm.res" placeholder="请输入资源" />
          </el-form-item>
          <el-form-item label="负责人" required>
            <el-input v-model="editForm.duty" placeholder="请输入负责人" />
          </el-form-item>
          <el-form-item label="负责人手机号" required>
            <el-input
              v-model="editForm.duty_tel"
              placeholder="请输入负责人手机号"
            />
          </el-form-item>
          <el-form-item label="状态" required>
            <el-select v-model="editForm.status" placeholder="请选择状态">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="检查时间">
            <el-date-picker
              v-model="editForm.check_time"
              type="datetime"
              placeholder="选择时间"
            />
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="editForm.create_time"
              type="datetime"
              disabled
            />
          </el-form-item>
          <el-form-item label="更新时间">
            <el-date-picker
              v-model="editForm.update_time"
              type="datetime"
              disabled
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmEditDevice">保存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import {
  deviceList,
  deviceDelete,
  deviceDetail,
  deviceUpdate,
  deviceAdd,
  getPlace,
  queryAddress,
  uploadFile,
} from "@/services/api";
import dayjs from "dayjs";
import { exportToExcelLikeCsv } from "@/utils/exportExcel";
import { Picture } from "@element-plus/icons-vue";

export default {
  name: "DeviceManagementPage",
  components: {
    Picture,
  },
  data() {
    return {
      defaultImage: require("@/assets/default-avatar.png"),
      deviceList: [],
      placeList: [],
      placeMap: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      duty: null,
      place_id: null,
      loading: false,
      uploadUrl: "http://localhost:8081/file/upload",
      addDialogVisible: false,
      editDialogVisible: false,
      addForm: {
        pic: "",
        place_id: "",
        name: "",
        res: "",
        duty: "",
        duty_tel: "",
        status: 1,
      },
      editForm: {
        id: "",
        pic: "",
        place_id: "",
        name: "",
        res: "",
        duty: "",
        duty_tel: "",
        status: 1,
        create_time: "",
        update_time: "",
      },
      searchForm: {
        place_id: "",
        duty: "",
        status: "",
        timeRange: [],
      },
    };
  },
  mounted() {
    this.fetchDeviceList();
    this.fetchLocations();
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
    async fetchLocations() {
      try {
        const response = await getPlace();
        if (response.code === 200) {
          this.placeList = response.data;
          this.placeMap = this.placeList.reduce((map, item) => {
            map[item.id] = item.name;
            return map;
          }, {});
        }
      } catch (error) {
        console.error('获取地点列表失败', error);
      }
    },

    // 获取设备列表
    async fetchDeviceList() {
      this.loading = true;
      try {
        // 直接传递查询参数，不使用params对象
        const response = await deviceList(this.currentPage, this.pageSize, {
          duty: this.searchForm.duty || null,
          place_id: this.searchForm.place_id || null,
          status: this.searchForm.status !== "" ? this.searchForm.status : null,
          startTime:
            this.searchForm.timeRange && this.searchForm.timeRange.length > 0
              ? this.searchForm.timeRange[0]
              : null,
          endTime:
            this.searchForm.timeRange && this.searchForm.timeRange.length > 1
              ? this.searchForm.timeRange[1]
              : null,
        });

        if (response.code === 200) {
          this.deviceList = response.data.records;
          this.total = response.data.total;

          // 处理设备图片
          this.processDeviceImages();
        }
      } catch (error) {
        console.error("获取设备列表错误:", error);
      } finally {
        this.loading = false;
      }
    },

    // 处理设备图片显示
    async processDeviceImages() {
      if (!this.deviceList || this.deviceList.length === 0) return;

      for (const device of this.deviceList) {
        if (device.pic) {
          try {
            const response = await queryAddress(device.pic);
            if (response && response.data && response.data.url) {
              device.pic = response.data.url;
            } else {
              device.pic = this.defaultImage;
            }
          } catch (error) {
            console.error("获取图片地址失败:", error);
            device.pic = this.defaultImage;
          }
        } else {
          device.pic = this.defaultImage;
        }
      }
    },

    // 打开添加对话框
    openAddDialog() {
      this.addDialogVisible = true;
    },

    // 确认添加设备
    async confirmAddDevice() {
      if (!this.validateForm(this.addForm)) return;

      try {
        const response = await deviceAdd(this.addForm);

        if (response && response.code === 200) {
          this.addDialogVisible = false;
          this.resetAddForm();
          this.fetchDeviceList();
        }
      } catch (error) {
        console.error("添加设备错误:", error);
      }
    },

    // 表单验证
    validateForm(form) {
      const requiredFields = [
        "name",
        "place_id",
        "res",
        "duty",
        "duty_tel",
        "status",
      ];

      for (const field of requiredFields) {
        // 特殊处理 status 字段
        if (field === "status") {
          if (form[field] !== 0 && !form[field]) {
            this.$message.warning(`请填写${this.getFieldLabel(field)}`);
            return false;
          }
        } else if (!form[field]) {
          this.$message.warning(`请填写${this.getFieldLabel(field)}`);
          return false;
        }
      }
      return true;
    },

    // 字段标签映射
    getFieldLabel(field) {
      const labels = {
        name: "设备名称",
        place_id: "地点",
        res: "资源",
        duty: "负责人",
        duty_tel: "负责人手机号",
        status: "状态",
      };
      return labels[field] || "必填字段";
    },

    // 重置添加表单
    resetAddForm() {
      this.addForm = {
        pic: "",
        place_id: "",
        name: "",
        res: "",
        duty: "",
        duty_tel: "",
        status: 1,
      };
    },

    // 打开编辑对话框
    async editDevice(row) {
      try {
        const res = await deviceDetail(row.id);
        if (res.code === 200) {
          const deviceData = res.data.object;
          this.editForm = {
            ...deviceData,
            status: Number(deviceData.status), // 强制转换为数字
            place_id: deviceData.place_id.toString(),
            create_time: this.formatTime(deviceData.create_time),
            check_time: this.formatTime(deviceData.check_time),
            update_time: this.formatTime(deviceData.update_time),
          };

          // 处理图片显示
          if (this.editForm.pic) {
            try {
              const imageResponse = await queryAddress(this.editForm.pic);
              if (
                imageResponse &&
                imageResponse.data &&
                imageResponse.data.url
              ) {
                this.editForm.picUrl = imageResponse.data.url; // 存储显示URL
              } else {
                this.editForm.picUrl = this.defaultImage;
              }
            } catch (error) {
              console.error("获取图片地址失败:", error);
              this.editForm.picUrl = this.defaultImage;
            }
          } else {
            this.editForm.picUrl = this.defaultImage;
          }

          this.editDialogVisible = true;
        }
      } catch (error) {
        console.error("获取设备详情失败", error);
      }
    },

    // 确认修改设备
    async confirmEditDevice() {
      if (!this.validateForm(this.editForm)) return;

      try {
        console.log(this.editForm.check_time);
        if (this.editForm.check_time != null || this.editForm.check_time) {
          this.editForm.check_time = dayjs(this.editForm.check_time).format(
            "YYYY-MM-DD HH:mm:ss"
          );
        }
        if (this.editForm.check_time == "Invalid Date") {
          this.editForm.check_time = "";
        }
        const res = await deviceUpdate(this.editForm);
        if (res.code === 200) {
          this.editDialogVisible = false;
          this.fetchDeviceList();
        }
      } catch (error) {
        console.error("更新设备失败", error);
      }
    },

    // 删除设备
    async deleteDevice(row) {
      try {
        await this.$confirm("确定删除该设备吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });

        const res = await deviceDelete(row.id);
        if (res.code === 200) {
          this.fetchDeviceList();
        }
      } catch (error) {
        if (error !== "cancel") {
          console.error("删除设备失败", error);
        }
      }
    },

    // 分页切换
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchDeviceList();
    },

    // 时间格式化
    formatTime(timestamp) {
      if (!timestamp) return "";
      // 检查特殊的空值日期 1949-01-01 01:01:00
      if (
        timestamp.includes("1949-01-01 01:01:00") ||
        timestamp === "1949-01-01T01:01:00" ||
        new Date(timestamp).getTime() ===
          new Date("1949-01-01 01:01:00").getTime()
      ) {
        return "";
      }
      const date = new Date(timestamp);
      return date.toLocaleString();
    },

    formatDateTime(time) {
      if (!time) return "N/A";
      // 检查特殊的空值日期 1949-01-01 01:01:00
      if (
        time.includes("1949-01-01 01:01:00") ||
        time === "1949-01-01T01:01:00" ||
        new Date(time).getTime() === new Date("1949-01-01 01:01:00").getTime()
      ) {
        return "N/A";
      }
      return dayjs(time).format("YYYY-MM-DD HH:mm:ss");
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

    async handleEditImageUpload({ file }) {
      try {
        const response = await uploadFile(file);

        if (response && response.code === 200) {
          this.editForm.pic = response.data.id;
          this.editForm.picUrl = response.data.url; // 更新预览图URL
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
        this.$message.error("只能上传图片文件");
        return false;
      }
      if (!isLt2MB) {
        this.$message.error("图片大小不能超过 2MB");
        return false;
      }
      return true;
    },

    handleSearch() {
      this.currentPage = 1;
      try {
        this.fetchDeviceList();
      } catch (error) {
        console.error("搜索设备出错:", error);
      }
    },

    resetSearch() {
      this.searchForm = {
        place_id: "",
        duty: "",
        status: "",
        timeRange: [],
      };
      this.currentPage = 1;
      try {
        this.fetchDeviceList();
      } catch (error) {
        console.error("重置搜索出错:", error);
      }
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
  overflow: auto;
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

.search-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.el-form-item {
  margin-right: 15px;
  margin-bottom: 10px;
}
</style>

<style>
/* 全局样式确保图片预览显示在最上层 */
.el-image-viewer__wrapper {
  z-index: 2999 !important;
}
</style>