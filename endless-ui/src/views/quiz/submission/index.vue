<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="UUID" prop="playerUuid">
        <el-input
          v-model="queryParams.playerUuid"
          clearable
          placeholder="请输入玩家UUID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="玩家名称" prop="playerName">
        <el-input
          v-model="queryParams.playerName"
          clearable
          placeholder="请输入玩家名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="总得分" prop="totalScore">
        <el-input
          v-model="queryParams.totalScore"
          clearable
          placeholder="请输入总得分"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="提交时间" prop="submitTime">
        <el-date-picker v-model="queryParams.submitTime"
                        clearable
                        placeholder="请选择提交时间"
                        type="date"
                        value-format="yyyy-MM-dd">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="审核时间" prop="reviewTime">
        <el-date-picker v-model="queryParams.reviewTime"
                        clearable
                        placeholder="请选择审核时间"
                        type="date"
                        value-format="yyyy-MM-dd">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['quiz:submission:edit']"
          :disabled="single"
          icon="el-icon-edit"
          plain
          size="mini"
          type="success"
          @click="handleUpdate"
        >审核
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['quiz:submission:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="submissionList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="ID" prop="id"/>
      <el-table-column align="center" label="玩家名称" prop="playerName" show-overflow-tooltip/>
      <el-table-column align="center" label="玩家UUID" prop="playerUuid" show-overflow-tooltip/>
      <el-table-column align="center" label="总得分" prop="totalScore"/>
      <el-table-column align="center" label="通过状态" prop="passStatus" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.passStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.passStatus === 1 ? '已通过' : '未通过' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="提交时间" prop="submitTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.submitTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="审核时间" prop="reviewTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.reviewTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="审核人" prop="reviewer"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            icon="el-icon-view"
            size="mini"
            type="text"
            @click="handleView(scope.row)"
          >查看
          </el-button>
          <el-button
            v-hasPermi="['quiz:submission:remove']"
            icon="el-icon-delete"
            size="mini"
            type="text"
            @click="handleDelete(scope.row)"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :limit.sync="queryParams.pageSize"
      :page.sync="queryParams.pageNum"
      :total="total"
      @pagination="getList"
    />

    <!-- 添加或修改答题记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="800px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="玩家UUID" prop="playerUuid">
              <el-input v-model="form.playerUuid" disabled placeholder="请输入玩家UUID"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="玩家名称" prop="playerName">
              <el-input v-model="form.playerName" disabled placeholder="请输入玩家名称"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="总得分" prop="totalScore">
              <el-input v-model="form.totalScore" disabled placeholder="请输入总得分"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通过状态" prop="passStatus">
              <el-select v-model="form.passStatus" placeholder="请选择通过状态" style="width: 100%">
                <el-option :value="0" label="未通过"/>
                <el-option :value="1" label="已通过"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="提交时间" prop="submitTime">
              <el-date-picker
                v-model="form.submitTime"
                disabled
                placeholder="选择提交时间"
                style="width: 100%"
                type="datetime">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审核时间" prop="reviewTime">
              <el-date-picker
                v-model="form.reviewTime"
                disabled
                placeholder="选择审核时间"
                style="width: 100%"
                type="datetime">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="审核人" prop="reviewer">
          <el-input v-model="form.reviewer" placeholder="请输入审核人"/>
        </el-form-item>
        <el-form-item label="审核备注" prop="reviewComment">
          <el-input v-model="form.reviewComment" placeholder="请输入审核备注" type="textarea"/>
        </el-form-item>

        <el-divider content-position="center">答题详情</el-divider>
        <el-table ref="whitelistQuizSubmissionDetail" :data="whitelistQuizSubmissionDetailList"
                  :row-class-name="rowWhitelistQuizSubmissionDetailIndex">
          <el-table-column align="center" label="序号" prop="index" width="50"/>
          <el-table-column label="问题内容" min-width="200" prop="questionText" show-overflow-tooltip/>
          <el-table-column label="问题类型" prop="questionType" width="100">
            <template slot-scope="scope">
              <el-tag
                :type="scope.row.questionType === 1 ? 'primary' : scope.row.questionType === 2 ? 'success' : scope.row.questionType === 3 ?  'danger' : 'warning'">
                {{
                  scope.row.questionType === 1 ? '单选题'
                    : scope.row.questionType === 2 ? '多选题'
                      : scope.row.questionType === 3 ? '填空题' : '随机验证'
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="玩家答案" min-width="150" prop="playerAnswer" show-overflow-tooltip/>
          <el-table-column label="是否正确" prop="isCorrect" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.isCorrect === 1 ? 'success' : 'danger'">
                {{ scope.row.isCorrect === 1 ? '正确' : '错误' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" label="得分" prop="score" width="80"/>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {delSubmission, getSubmission, listSubmission, updateSubmission} from "@/api/quiz/submission";

export default {
  name: "Submission",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 子表选中数据
      checkedWhitelistQuizSubmissionDetail: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 答题记录表格数据
      submissionList: [],
      // 白名单申请答题详情表格数据
      whitelistQuizSubmissionDetailList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        playerUuid: null,
        playerName: null,
        totalScore: null,
        passStatus: null,
        submitTime: null,
        reviewTime: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        passStatus: [
          {required: true, message: "通过状态不能为空", trigger: "change"}
        ],
        reviewer: [
          {required: true, message: "审核人不能为空", trigger: "blur"}
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询答题记录列表 */
    getList() {
      this.loading = true;
      listSubmission(this.queryParams).then(response => {
        this.submissionList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        playerUuid: null,
        playerName: null,
        totalScore: null,
        passStatus: null,
        submitTime: null,
        reviewTime: null,
        reviewer: null,
        reviewComment: null,
        remark: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        delFlag: null
      };
      this.whitelistQuizSubmissionDetailList = [];
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.reset();
      const id = row.id || this.ids
      getSubmission(id).then(response => {
        this.form = response.data;
        this.whitelistQuizSubmissionDetailList = response.data.whitelistQuizSubmissionDetailList;
        this.open = true;
        this.title = "查看答题记录";
      });
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getSubmission(id).then(response => {
        this.form = response.data;
        this.whitelistQuizSubmissionDetailList = response.data.whitelistQuizSubmissionDetailList;
        this.open = true;
        this.title = "审核答题记录";
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$confirm("是否删除该答题记录?", "提示", {
        type: "warning"
      }).then(() => {
        delSubmission(row.id).then(response => {
          this.$modal.msgSuccess("删除成功");
          this.getList();
        });
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 设置审核时间为当前时间
          this.form.reviewTime = new Date();
          updateSubmission(this.form).then(response => {
            this.$modal.msgSuccess("审核成功");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    /** 白名单申请答题详情序号 */
    rowWhitelistQuizSubmissionDetailIndex({row, rowIndex}) {
      row.index = rowIndex + 1;
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('quiz/submission/export', {
        ...this.queryParams
      }, `submission_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
