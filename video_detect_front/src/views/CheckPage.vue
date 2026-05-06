<template>
  <div class="monitor-container">
    <el-card class="monitor-card">
      <!-- 筛选表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="责任人">
          <el-input v-model="searchForm.duty" placeholder="输入责任人" clearable />
        </el-form-item>
        <el-form-item label="地点">
          <el-select v-model="searchForm.place" placeholder="选择地点" style="width: 180px">
            <el-option
              v-for="item in placeList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="searchForm.score" placeholder="选择评分" style="width: 120px">
            <el-option label="很差" :value="0">
              <el-tag type="danger" effect="dark">很差</el-tag>
            </el-option>
            <el-option label="差" :value="1">
              <el-tag type="warning" effect="dark">差</el-tag>
            </el-option>
            <el-option label="中等" :value="2">
              <el-tag type="info" effect="dark">中等</el-tag>
            </el-option>
            <el-option label="良好" :value="3">
              <el-tag type="success" effect="dark">良好</el-tag>
            </el-option>
            <el-option label="优秀" :value="4">
              <el-tag type="primary" effect="dark">优秀</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="检查时间">
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

      <!-- 添加检查按钮 -->
      <div style="margin-bottom: 20px">
        <el-button type="primary" @click="openAddDialog"
          >添加检查记录</el-button
        >
      </div>

      <!-- 表格 -->
      <el-table
        :data="pagedCheckList"
        stripe
        style="width: 100%"
        :cell-style="{ verticalAlign: 'middle', textAlign: 'center' }"
        :header-cell-style="{ textAlign: 'center' }"
      >
        <!-- 责任人 -->
        <el-table-column prop="duty" label="责任人" width="100" />
        <!-- 责任人联系方式 -->
        <el-table-column prop="duty_tel" label="责任人联系方式" width="150" />
        <!-- 地点 -->
        <el-table-column label="地点" width="180">
          <template #default="{ row }">
            {{ placeMap[row.place] }}
          </template>
        </el-table-column>
        <!-- 持续时间 -->
        <el-table-column prop="d_time" label="持续时间(h)" width="100" />
        <!-- 检查时间 -->
        <el-table-column prop="time" label="检查时间" width="180">
          <template #default="{ row }">
            {{ row.time === '1949-01-01 01:01:00' ? 'N/A' : row.time }}
          </template>
        </el-table-column>
        <!-- 评分 -->
        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getScoreTagType(row.score)" 
              effect="dark">
              {{ scoreLabel(row.score) }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 情况 -->
        <el-table-column prop="situation" label="情况" />
        <!-- 操作列 -->
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetails(row)"
              >详情</el-button
            >
            <el-button type="danger" size="small" @click="deleteCheck(row)"
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

      <!-- 添加检查记录对话框 -->
      <el-dialog v-model="addDialogVisible" title="添加检查记录" width="500px">
        <el-form :model="addForm" label-width="100px">
          <!-- 检查人 -->
          <el-form-item label="检查人" required>
            <el-input v-model="addForm.duty" placeholder="请输入检查人" />
          </el-form-item>

          <!-- 检查人联系方式 -->
          <el-form-item label="联系方式" required>
            <el-input
              v-model="addForm.duty_tel"
              placeholder="请输入检查人联系方式"
            />
          </el-form-item>

          <!-- 地点 -->
          <el-form-item label="地点" required>
            <el-select v-model="addForm.place" placeholder="请选择地点">
              <el-option
                v-for="location in placeList"
                :key="location.id"
                :label="location.name"
                :value="location.id"
              />
            </el-select>
          </el-form-item>

          <!-- 检查时间 -->
          <el-form-item label="时间" required>
            <el-date-picker
              v-model="addForm.time"
              type="datetime"
              placeholder="选择时间"
            />
          </el-form-item>

          <!-- 持续时间 -->
          <el-form-item label="持续时间(h)" required>
            <el-input
              v-model="addForm.d_time"
              type="number"
              placeholder="请填写持续时间"
            />
          </el-form-item>

          <!-- 检查情况 -->
          <el-form-item label="检查情况" required>
            <el-input v-model="addForm.situation" placeholder="请选择检查情况">
            </el-input>
          </el-form-item>

          <!-- 评分 -->
          <el-form-item label="评分" required>
            <el-select v-model="addForm.score" placeholder="请选择评分">
              <el-option label="很差" :value="0">
                <el-tag type="danger" effect="dark">很差</el-tag>
              </el-option>
              <el-option label="差" :value="1">
                <el-tag type="warning" effect="dark">差</el-tag>
              </el-option>
              <el-option label="中等" :value="2">
                <el-tag type="info" effect="dark">中等</el-tag>
              </el-option>
              <el-option label="良好" :value="3">
                <el-tag type="success" effect="dark">良好</el-tag>
              </el-option>
              <el-option label="优秀" :value="4">
                <el-tag type="primary" effect="dark">优秀</el-tag>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAddCheck">确认</el-button>
        </template>
      </el-dialog>

      <!-- 详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="检查记录详情"
        width="500px"
      >
        <el-form :model="detailForm" label-width="100px">
          <el-form-item label="责任人">
            <el-input v-model="detailForm.duty" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="detailForm.duty_tel" />
          </el-form-item>
          <el-form-item label="地点">
            <el-input :value="placeMap[detailForm.place]" disabled />
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker v-model="detailForm.create_time" type="datetime" disabled />
          </el-form-item>
          <el-form-item label="更新时间">
            <el-date-picker v-model="detailForm.update_time" type="datetime" disabled />
          </el-form-item>
          <el-form-item label="检查时间">
            <el-date-picker
              v-model="detailForm.time"
              type="datetime"
              placeholder="选择时间"
            />
          </el-form-item>
          <el-form-item label="持续时间(h)">
            <el-input v-model="detailForm.d_time" type="number" />
          </el-form-item>
          <el-form-item label="情况">
            <el-input v-model="detailForm.situation" />
          </el-form-item>
          <el-form-item label="评分">
            <el-select v-model="detailForm.score" placeholder="请选择评分">
              <el-option label="很差" :value="0">
                <el-tag type="danger" effect="dark">很差</el-tag>
              </el-option>
              <el-option label="差" :value="1">
                <el-tag type="warning" effect="dark">差</el-tag>
              </el-option>
              <el-option label="中等" :value="2">
                <el-tag type="info" effect="dark">中等</el-tag>
              </el-option>
              <el-option label="良好" :value="3">
                <el-tag type="success" effect="dark">良好</el-tag>
              </el-option>
              <el-option label="优秀" :value="4">
                <el-tag type="primary" effect="dark">优秀</el-tag>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="detailDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveDetail">保存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import {
  checkList,
  checkDelete,
  checkDetail,
  checkUpdate,
  checkAdd,
  getPlace,
} from "@/services/api";
import dayjs from "dayjs";
import { exportToExcelLikeCsv } from "@/utils/exportExcel";
export default {
  name: "CheckPage",
  data() {
    return {
      placeList: [],
      placeMap: {}, // 地点ID到名称的映射
      checkList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      pageDuty: null,
      pageScore: null,
      addDialogVisible: false,
      detailDialogVisible: false, // 详情对话框是否显示
      addForm: {
        duty: "",
        duty_tel: "",
        place: "",
        score: "",
        situation: "",
        time: "",
        d_time: "",
      },
      detailForm: {
        id: null,
        duty: "",
        duty_tel: "",
        place: "",
        create_time: "",
        update_time: "",
        time: "",
        d_time: 0,
        situation: "",
        score: 2,
      },
      searchForm: {
        duty: "",
        place: "",
        score: "",
        timeRange: [],
      },
    };
  },
  computed: {
    pagedCheckList() {
      return this.checkList.slice(0, this.pageSize);
    },
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

    // 获取地点方法
    async fetchLocations() {
      try {
        const response = await getPlace();
        if (response.code === 200) {
          this.placeList = response.data;
          // 创建ID到名称的映射
          this.placeMap = this.placeList.reduce((map, item) => {
            map[item.id] = item.name;
            return map;
          }, {});
        }
      } catch (error) {
        console.error("获取地点列表错误:", error);
      }
    },

    // 获取检查记录列表
    async fetchCheckList() {
      try {
        // 直接传递查询参数，不使用params对象
        const response = await checkList(
          this.currentPage,
          this.pageSize,
          {
            duty: this.searchForm.duty || null,
            place: this.searchForm.place || null,
            score: this.searchForm.score !== '' ? Number(this.searchForm.score) : null,
            startTime: this.searchForm.timeRange && this.searchForm.timeRange.length > 0 ? this.searchForm.timeRange[0] : null,
            endTime: this.searchForm.timeRange && this.searchForm.timeRange.length > 1 ? this.searchForm.timeRange[1] : null
          }
        );
        
        if (response.code === 200) {
          this.checkList = response.data.records;
          this.total = response.data.total;
        } else {
          console.error("获取检查记录失败:", response);
        }
      } catch (error) {
        console.error("获取检查记录错误:", error);
      }
    },

    async confirmAddCheck() {
      // 表单验证
      if (
        !this.addForm.duty ||
        !this.addForm.duty_tel ||
        !this.addForm.place ||
        !this.addForm.score ||
        !this.addForm.situation ||
        !this.addForm.time
      ) {
        this.$message.warning("请填写完整信息");
        return;
      }
      this.addForm.score = Number(this.addForm.score);
      this.addForm.time = dayjs(this.addForm.time).format(
        "YYYY-MM-DD HH:mm:ss"
      );
      console.log("提交的时间值:", this.addForm.time);
      try {
        // 调用 checkAdd 接口
        const response = await checkAdd(this.addForm);

        if (response.code === 200) {
          // 添加成功
          this.addDialogVisible = false;
          this.resetAddForm();
          this.fetchCheckList(); // 重新获取检查记录列表
        }
      } catch (error) {
        // 网络请求失败
        console.error("添加检查记录错误:", error);
      }
    },

    // 查看详情
    async viewDetails(row) {
      try {
        const response = await checkDetail(row.id); // 调用详情接口
        if (response.code === 200) {
          // 确保数字类型
          this.detailForm = {
            ...response.data,
            score: Number(response.data.score),
          };
          this.detailForm = response.data.object; // 填充详情数据
          if(response.data.object.time == "1949-01-01T01:01:00"||response.data.object.time.includes("1949-01-01 01:01:00")
          ||new Date(response.data.object.time).getTime() === new Date("1949-01-01 01:01:00").getTime()){
            this.detailForm.time = null;
          }
          this.detailDialogVisible = true; // 打开详情对话框
        }
      } catch (error) {
        console.error("获取详情错误:", error);
      }
    },

    // 保存详情
    async saveDetail() {
      try {
        this.detailForm.time = dayjs(this.detailForm.time).format(
          "YYYY-MM-DD HH:mm:ss"
        );
        if(this.detailForm.time == "Invalid Date"){
          this.detailForm.time = "1949-01-01 01:01:00";
        }
        const response = await checkUpdate(this.detailForm); // 调用更新接口
        if (response.code === 200) {
          this.detailDialogVisible = false; // 关闭详情对话框
          this.fetchCheckList(); // 刷新表格数据
        }
      } catch (error) {
        console.error("保存详情错误:", error);
      }
    },

    // 删除检查记录
    async deleteCheck(row) {
      try {
        await this.$confirm("确定删除该检查记录吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
        const response = await checkDelete(row.id);

        if (response.code === 200) {
          this.fetchCheckList();
        }
      } catch (error) {
        if (error !== "cancel") {
          console.error("删除错误:", error);
        }
      }
    },

    scoreLabel(score) {
      const labels = {
        0: "很差",
        1: "差",
        2: "中等",
        3: "良好",
        4: "优秀",
      };
      return labels[score] || "未知";
    },

    // 打开添加检查记录对话框
    openAddDialog() {
      this.addDialogVisible = true;
    },

    // 分页切换
    handlePageChange(page) {
      this.currentPage = page; // 更新当前页码
      this.fetchCheckList(); // 重新获取数据
    },

    // 重置添加表单
    resetAddForm() {
      this.addForm = {
        duty: "", // 检查人
        duty_tel: "", // 检查人联系方式
        place: "", // 地点
        score: "", // 评分
        situation: "", // 检查情况
        time: "", // 检查时间
        d_time: "",
      };
    },

    handleSearch() {
      // 实现搜索逻辑
      console.log("搜索条件:", this.searchForm);
      this.currentPage = 1; // 重置到第一页
      try {
        this.fetchCheckList(); // 重新获取数据
      } catch (error) {
        console.error('搜索检查记录出错:', error);
      }
    },

    resetSearch() {
      // 重置搜索表单
      this.searchForm = {
        duty: "",
        place: "",
        score: "",
        timeRange: [],
      };
      this.currentPage = 1; // 重置到第一页
      try {
        this.fetchCheckList(); // 重新获取数据
      } catch (error) {
        console.error('重置搜索出错:', error);
      }
    },

    getScoreTagType(score) {
      const types = {
        0: 'danger',
        1: 'warning',
        2: 'info',
        3: 'success',
        4: 'primary',
      };
      return types[score] || 'info';
    },

  },
  mounted() {
    this.fetchCheckList();
    this.fetchLocations(); // 初始化时获取地点列表
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

.el-pagination {
  margin-top: 20px;
  text-align: center;
  position: sticky; /* 使分页固定在底部 */
  bottom: 0;
  background: white; /* 防止内容遮挡 */
  z-index: 1; /* 确保分页在最上层 */
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