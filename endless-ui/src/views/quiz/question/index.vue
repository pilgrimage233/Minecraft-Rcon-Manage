<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="问题内容" prop="questionText">
        <el-input
          v-model="queryParams.questionText"
          placeholder="请输入问题内容"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="问题类型" prop="questionType">
        <el-select v-model="queryParams.questionType" placeholder="请选择问题类型" clearable size="small">
          <el-option label="单选题" :value="1" />
          <el-option label="多选题" :value="2" />
          <el-option label="填空题" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否必答" prop="isRequired">
        <el-select v-model="queryParams.isRequired" placeholder="请选择是否必答" clearable size="small">
          <el-option label="是" :value="1" />
          <el-option label="否" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['quiz:question:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['quiz:question:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['quiz:question:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['quiz:question:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="questionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="问题内容" align="center" prop="questionText" :show-overflow-tooltip="true" min-width="180" />
      <el-table-column label="问题类型" align="center" prop="questionType" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.questionType === 1 ? 'primary' : (scope.row.questionType === 2 ? 'success' : 'warning')">
            {{ scope.row.questionType === 1 ? '单选题' : (scope.row.questionType === 2 ? '多选题' : '填空题') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否必答" align="center" prop="isRequired" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isRequired === 1 ? 'danger' : 'info'">
            {{ scope.row.isRequired === 1 ? '必答' : '非必答' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序顺序" align="center" prop="sortOrder" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['quiz:question:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['quiz:question:remove']"
          >删除</el-button>
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

    <!-- 添加或修改白名单申请题库问题对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1000px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="问题类型" prop="questionType">
              <el-select v-model="form.questionType" placeholder="请选择问题类型" style="width: 100%">
                <el-option label="单选题" :value="1" />
                <el-option label="多选题" :value="2" />
                <el-option label="填空题" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否必答" prop="isRequired">
              <el-select v-model="form.isRequired" placeholder="请选择是否必答" style="width: 100%">
                <el-option label="是" :value="1" />
                <el-option label="否" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="问题内容" prop="questionText">
          <el-input v-model="form.questionText" type="textarea" :rows="3" placeholder="请输入问题内容" />
        </el-form-item>
        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>

        <el-divider content-position="center">答案管理</el-divider>
        <div class="answer-container">
          <div class="answer-header">
            <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAddWhitelistQuizAnswer">添加答案</el-button>
            <el-button type="danger" icon="el-icon-delete" size="mini" @click="handleDeleteWhitelistQuizAnswer">删除选中</el-button>
          </div>
          <el-table :data="whitelistQuizAnswerList" :row-class-name="rowWhitelistQuizAnswerIndex" @selection-change="handleWhitelistQuizAnswerSelectionChange" ref="whitelistQuizAnswer">
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column label="序号" align="center" prop="index" width="50"/>
            <el-table-column label="答案内容" prop="answerText" min-width="100">
              <template slot-scope="scope">
                <el-input v-model="scope.row.answerText" type="textarea" :rows="2" placeholder="请输入答案内容" />
              </template>
            </el-table-column>
            <el-table-column label="是否正确答案" prop="isCorrect" width="120">
              <template slot-scope="scope">
                <el-switch
                  v-model="scope.row.isCorrect"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="是"
                  inactive-text="否"
                  @change="handleAnswerCorrectChange(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="排序" prop="sortOrder" width="150">
              <template slot-scope="scope">
                <el-input-number v-model="scope.row.sortOrder" :min="0" :max="999" size="mini" />
              </template>
            </el-table-column>
            <el-table-column label="得分" prop="score" width="150">
              <template slot-scope="scope">
                <el-input-number
                  v-model="scope.row.score"
                  :min="0"
                  :max="100"
                  :precision="1"
                  :step="0.5"
                  size="mini"
                  placeholder="请输入得分"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listQuestion, getQuestion, delQuestion, addQuestion, updateQuestion } from "@/api/quiz/question";

export default {
  name: "Question",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 子表选中数据
      checkedWhitelistQuizAnswer: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 白名单申请题库问题表格数据
      questionList: [],
      // 白名单申请题目答案表格数据
      whitelistQuizAnswerList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        questionText: null,
        questionType: null,
        isRequired: null,
        status: null,
      },
      // 表单参数
      form: {
        id: null,
        questionText: null,
        questionType: null,
        isRequired: null,
        sortOrder: 0,
        status: 1,
        remark: null,
        delFlag: null
      },
      // 表单校验
      rules: {
        questionText: [
          { required: true, message: "问题内容不能为空", trigger: "blur" }
        ],
        questionType: [
          { required: true, message: "问题类型不能为空", trigger: "change" }
        ],
        isRequired: [
          { required: true, message: "是否必答不能为空", trigger: "change" }
        ]
      }
    };
  },
  watch: {
    'form.questionType': {
      handler(newVal) {
        // 当问题类型改变为单选题时，确保只有一个答案被标记为正确
        if (newVal === 1 && this.whitelistQuizAnswerList.length > 0) {
          // 找到所有标记为正确的答案
          const correctAnswers = this.whitelistQuizAnswerList.filter(item => item.isCorrect === 1);
          // 如果有多个正确答案，只保留第一个为正确
          if (correctAnswers.length > 1) {
            correctAnswers.forEach((item, index) => {
              if (index > 0) {
                item.isCorrect = 0;
              }
            });
            this.$message.warning('单选题只能设置一个正确答案，已自动调整');
          }
        }
      },
      immediate: true
    }
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询白名单申请题库问题列表 */
    getList() {
      this.loading = true;
      listQuestion(this.queryParams).then(response => {
        this.questionList = response.rows;
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
        questionText: null,
        questionType: null,
        isRequired: null,
        sortOrder: 0,
        status: 1,
        remark: null,
        delFlag: null
      };
      this.whitelistQuizAnswerList = [];
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
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加白名单申请题库问题";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getQuestion(id).then(response => {
        this.form = response.data;
        this.whitelistQuizAnswerList = response.data.whitelistQuizAnswerList;
        this.open = true;
        this.title = "修改白名单申请题库问题";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 验证答案 - 填空题不需要添加答案选项
          if (this.form.questionType !== 3 && this.whitelistQuizAnswerList.length === 0) {
            this.$message.error('请至少添加一个答案选项');
            return;
          }

          // 如果是单选题，确保只有一个正确答案
          if (this.form.questionType === 1) {
            const correctCount = this.whitelistQuizAnswerList.filter(item => item.isCorrect === 1).length;
            if (correctCount === 0) {
              this.$message.error('单选题必须设置一个正确答案');
              return;
            }
            if (correctCount > 1) {
              this.$message.error('单选题只能设置一个正确答案');
              return;
            }
          }

          // 如果是多选题，确保至少有一个正确答案
          if (this.form.questionType === 2) {
            const correctCount = this.whitelistQuizAnswerList.filter(item => item.isCorrect === 1).length;
            if (correctCount === 0) {
              this.$message.error('多选题必须至少设置一个正确答案');
              return;
            }
          }

          this.form.whitelistQuizAnswerList = this.whitelistQuizAnswerList;
          if (this.form.id != null) {
            updateQuestion(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addQuestion(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除白名单申请题库问题编号为"' + ids + '"的数据项？').then(function() {
        return delQuestion(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
	/** 白名单申请题目答案序号 */
    rowWhitelistQuizAnswerIndex({ row, rowIndex }) {
      row.index = rowIndex + 1;
    },
    /** 白名单申请题目答案添加按钮操作 */
    handleAddWhitelistQuizAnswer() {
      let obj = {
        answerText: "",
        isCorrect: 0,
        sortOrder: this.whitelistQuizAnswerList.length + 1,
        score: 0,
        remark: ""
      };
      // 如果是单选题且已有正确答案，确保新增的答案不是正确答案
      if (this.form.questionType === 1 && this.whitelistQuizAnswerList.some(item => item.isCorrect === 1)) {
        obj.isCorrect = 0;
      }
      this.whitelistQuizAnswerList.push(obj);
    },
    /** 白名单申请题目答案删除按钮操作 */
    handleDeleteWhitelistQuizAnswer() {
      if (this.checkedWhitelistQuizAnswer.length == 0) {
        this.$modal.msgError("请先选择要删除的白名单申请题目答案数据");
      } else {
        const whitelistQuizAnswerList = this.whitelistQuizAnswerList;
        const checkedWhitelistQuizAnswer = this.checkedWhitelistQuizAnswer;
        this.whitelistQuizAnswerList = whitelistQuizAnswerList.filter(function(item) {
          return checkedWhitelistQuizAnswer.indexOf(item.index) == -1
        });
      }
    },
    /** 复选框选中数据 */
    handleWhitelistQuizAnswerSelectionChange(selection) {
      this.checkedWhitelistQuizAnswer = selection.map(item => item.index)
    },
    /** 处理答案正确状态变更 */
    handleAnswerCorrectChange(row) {
      // 如果是单选题，并且当前答案被设置为正确
      if (this.form.questionType === 1 && row.isCorrect === 1) {
        // 将其他所有答案设置为不正确
        this.whitelistQuizAnswerList.forEach(item => {
          if (item !== row && item.isCorrect === 1) {
            item.isCorrect = 0;
          }
        });
        this.$message.info('单选题只能有一个正确答案');
      }
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('quiz/question/export', {
        ...this.queryParams
      }, `question_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style scoped>
.answer-container {
  margin-top: 20px;
}
.answer-header {
  margin-bottom: 15px;
}
.answer-header .el-button {
  margin-right: 10px;
}
.el-divider {
  margin: 20px 0;
}
</style>
