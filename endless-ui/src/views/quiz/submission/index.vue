<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="UUID" prop="playerUuid">
        <el-input
          v-model="queryParams.playerUuid"
          placeholder="请输入玩家UUID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="玩家名称" prop="playerName">
        <el-input
          v-model="queryParams.playerName"
          placeholder="请输入玩家名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="总得分" prop="totalScore">
        <el-input
          v-model="queryParams.totalScore"
          placeholder="请输入总得分"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="提交时间" prop="submitTime">
        <el-date-picker clearable
                        v-model="queryParams.submitTime"
                        type="date"
                        value-format="yyyy-MM-dd"
                        placeholder="请选择提交时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="审核时间" prop="reviewTime">
        <el-date-picker clearable
                        v-model="queryParams.reviewTime"
                        type="date"
                        value-format="yyyy-MM-dd"
                        placeholder="请选择审核时间">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['quiz:submission:edit']"
        >审核
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['quiz:submission:export']"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="submissionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="ID" align="center" prop="id"/>
      <el-table-column label="玩家名称" align="center" prop="playerName" show-overflow-tooltip/>
      <el-table-column label="玩家UUID" align="center" prop="playerUuid" show-overflow-tooltip/>
      <el-table-column label="总得分" align="center" prop="totalScore"/>
      <el-table-column label="通过状态" align="center" prop="passStatus" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.passStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.passStatus === 1 ? '已通过' : '未通过' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" align="center" prop="submitTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.submitTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核时间" align="center" prop="reviewTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.reviewTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核人" align="center" prop="reviewer"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['quiz:submission:edit']"
          >审核
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改答题记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="玩家UUID" prop="playerUuid">
              <el-input v-model="form.playerUuid" placeholder="请输入玩家UUID" disabled/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="玩家名称" prop="playerName">
              <el-input v-model="form.playerName" placeholder="请输入玩家名称" disabled/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="总得分" prop="totalScore">
              <el-input v-model="form.totalScore" placeholder="请输入总得分" disabled/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通过状态" prop="passStatus">
              <el-select v-model="form.passStatus" placeholder="请选择通过状态" style="width: 100%">
                <el-option label="未通过" :value="0"/>
                <el-option label="已通过" :value="1"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="提交时间" prop="submitTime">
              <el-date-picker
                v-model="form.submitTime"
                type="datetime"
                placeholder="选择提交时间"
                disabled
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审核时间" prop="reviewTime">
              <el-date-picker
                v-model="form.reviewTime"
                type="datetime"
                placeholder="选择审核时间"
                disabled
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="审核人" prop="reviewer">
          <el-input v-model="form.reviewer" placeholder="请输入审核人"/>
        </el-form-item>
        <el-form-item label="审核备注" prop="reviewComment">
          <el-input v-model="form.reviewComment" type="textarea" placeholder="请输入审核备注"/>
        </el-form-item>

        <el-divider content-position="center">答题详情</el-divider>
        <el-table :data="whitelistQuizSubmissionDetailList" :row-class-name="rowWhitelistQuizSubmissionDetailIndex"
                  ref="whitelistQuizSubmissionDetail">
          <el-table-column label="序号" align="center" prop="index" width="50"/>
          <el-table-column label="问题内容" prop="questionText" min-width="200" show-overflow-tooltip/>
          <el-table-column label="问题类型" prop="questionType" width="100">
            <template slot-scope="scope">
              <el-tag
                :type="scope.row.questionType === 1 ? 'primary' : (scope.row.questionType === 2 ? 'success' : 'warning')">
                {{ scope.row.questionType === 1 ? '单选题' : (scope.row.questionType === 2 ? '多选题' : '填空题') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="玩家答案" prop="playerAnswer" min-width="150" show-overflow-tooltip/>
          <el-table-column label="是否正确" prop="isCorrect" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.isCorrect === 1 ? 'success' : 'danger'">
                {{ scope.row.isCorrect === 1 ? '正确' : '错误' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="得分" prop="score" width="80" align="center"/>
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
import {getSubmission, listSubmission, updateSubmission} from "@/api/quiz/submission";

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
