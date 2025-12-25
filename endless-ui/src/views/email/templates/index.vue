<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['email:templates:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['email:templates:edit']"
          :disabled="single"
          icon="el-icon-edit"
          plain
          size="mini"
          type="success"
          @click="handleUpdate"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['email:templates:remove']"
          :disabled="multiple"
          icon="el-icon-delete"
          plain
          size="mini"
          type="danger"
          @click="handleDelete"
        >删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['email:templates:export']"
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

    <el-table v-loading="loading" :data="templatesList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="ID" prop="id"/>
      <el-table-column align="center" label="服务器ID" prop="serverId">
        <template slot-scope="scope">
          {{ scope.row.serverId || '未指定' }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime"/>
      <el-table-column align="center" label="更新时间" prop="updateTime"/>
      <!-- <el-table-column label="待审核" align="center" prop="pendingTemp" />
      <el-table-column label="通过" align="center" prop="passTemp" />
      <el-table-column label="拒绝" align="center" prop="refuseTemp" />
      <el-table-column label="移除" align="center" prop="removeTemp" />
      <el-table-column label="封禁" align="center" prop="banTemp" />
      <el-table-column label="解禁" align="center" prop="pardonTemp" />
      <el-table-column label="邮箱验证" align="center" prop="verifyTemp" />
      <el-table-column label="系统告警" align="center" prop="warningTemp" /> -->
      <el-table-column align="center" label="备注" prop="remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['email:templates:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['email:templates:remove']"
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

    <!-- 邮件模板编辑器 -->
    <template-editor
      :form-data="form"
      :title="title"
      :visible.sync="open"
      @success="handleSuccess"
    />
  </div>
</template>

<script>
import {delTemplates, getTemplates, listTemplates} from "@/api/email/templates";
import TemplateEditor from "./TemplateEditor";

export default {
  name: "Templates",
  components: {
    TemplateEditor
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 自定义邮件通知模板表格数据
      templatesList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        serverId: [
          {required: true, message: "服务器ID不能为空", trigger: "blur"}
        ],
        createTime: [
          {required: true, message: "创建时间不能为空", trigger: "blur"}
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询自定义邮件通知模板列表 */
    getList() {
      this.loading = true;
      listTemplates(this.queryParams).then(response => {
        this.templatesList = response.rows;
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
        createTime: null,
        createBy: null,
        serverId: null,
        reviewTemp: null,
        pendingTemp: null,
        passTemp: null,
        refuseTemp: null,
        removeTemp: null,
        banTemp: null,
        pardonTemp: null,
        verifyTemp: null,
        warningTemp: null,
        updateTime: null,
        updateBy: null,
        remark: null
      };
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
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加自定义邮件通知模板";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getTemplates(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改自定义邮件通知模板";
      });
    },
    /** 处理编辑成功事件 */
    handleSuccess() {
      this.getList();
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除自定义邮件通知模板编号为"' + ids + '"的数据项？').then(function () {
        return delTemplates(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('email/templates/export', {
        ...this.queryParams
      }, `templates_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style scoped>
.app-container {
  background: #fff;
  padding: 20px;
}

.mb8 {
  margin-bottom: 8px;
}

.small-padding >>> .cell {
  padding-left: 8px;
  padding-right: 8px;
}

.fixed-width >>> .el-button {
  width: 60px;
  padding: 6px 10px;
}

.template-preview >>> img {
  max-width: 100%;
}

.template-preview >>> table {
  border-collapse: collapse;
  width: 100%;
}

.template-preview >>> table,
.template-preview >>> th,
.template-preview >>> td {
  border: 1px solid #ddd;
  padding: 8px;
}

.template-preview >>> th {
  background-color: #f2f2f2;
  text-align: left;
}
</style>
