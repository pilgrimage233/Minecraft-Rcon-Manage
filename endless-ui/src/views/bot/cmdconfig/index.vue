<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="90px" size="small">
      <el-form-item label="群组ID" prop="groupId">
        <el-input
          v-model="queryParams.groupId"
          clearable
          placeholder="请输入群组ID"
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="指令关键字" prop="commandKey">
        <el-input
          v-model="queryParams.commandKey"
          clearable
          placeholder="请输入指令关键字"
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="指令分类" prop="commandCategory">
        <el-select
          v-model="queryParams.commandCategory"
          clearable
          placeholder="请选择指令分类"
          style="width: 200px"
        >
          <el-option label="普通用户" value="user"/>
          <el-option label="管理员" value="admin"/>
          <el-option label="超级管理员" value="super"/>
          <el-option label="系统通知" value="system"/>
        </el-select>
      </el-form-item>
      <el-form-item label="启用状态" prop="isEnabled">
        <el-select
          v-model="queryParams.isEnabled"
          clearable
          placeholder="请选择状态"
          style="width: 200px"
        >
          <el-option :value="1" label="启用"/>
          <el-option :value="0" label="禁用"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['bot:cmdconfig:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleAdd"
        >新增配置
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['bot:cmdconfig:edit']"
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
          v-hasPermi="['bot:cmdconfig:remove']"
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
          v-hasPermi="['bot:cmdconfig:edit']"
          icon="el-icon-refresh-right"
          plain
          size="mini"
          type="warning"
          @click="handleClearCache"
        >清除缓存
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['bot:cmdconfig:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="info"
          @click="handleExport"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="cmdconfigList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="ID" prop="id" width="80"/>
      <el-table-column align="center" label="群组ID" prop="groupId" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.groupId === 'default'" size="small" type="info">默认配置</el-tag>
          <span v-else>{{ scope.row.groupId }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="指令关键字" min-width="120" prop="commandKey">
        <template slot-scope="scope">
          <el-tag size="small">{{ scope.row.commandKey }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="指令名称" min-width="120" prop="commandName"/>
      <el-table-column align="center" label="分类" prop="commandCategory" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.commandCategory === 'user'" size="small" type="success">普通用户</el-tag>
          <el-tag v-else-if="scope.row.commandCategory === 'admin'" size="small" type="warning">管理员</el-tag>
          <el-tag v-else-if="scope.row.commandCategory === 'super'" size="small" type="danger">超级管理员</el-tag>
          <el-tag v-else-if="scope.row.commandCategory === 'system'" size="small" type="info">系统通知</el-tag>
          <span v-else>{{ scope.row.commandCategory }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="isEnabled" width="100">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.isEnabled"
            v-hasPermi="['bot:cmdconfig:edit']"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column align="center" label="禁用提示" min-width="150" prop="disabledMessage" show-overflow-tooltip/>
      <el-table-column align="center" label="备注" min-width="120" prop="remark" show-overflow-tooltip/>
      <el-table-column align="center" label="更新时间" prop="updateTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="150">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['bot:cmdconfig:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['bot:cmdconfig:remove']"
            icon="el-icon-delete"
            size="mini"
            style="color: #f56c6c"
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

    <!-- 添加或修改群组指令功能配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="群组ID" prop="groupId">
          <el-input v-model="form.groupId" placeholder="请输入群组ID，default 表示默认配置">
            <template slot="prepend">QQ群</template>
          </el-input>
          <span class="form-tip">提示：输入 default 表示默认配置，适用于所有未单独配置的群组</span>
        </el-form-item>
        <el-form-item label="指令关键字" prop="commandKey">
          <el-input v-model="form.commandKey" placeholder="请输入指令关键字（主命令名称）"/>
          <span class="form-tip">提示：如"白名单申请"、"查询在线"等</span>
        </el-form-item>
        <el-form-item label="指令显示名称" prop="commandName">
          <el-input v-model="form.commandName" placeholder="请输入指令显示名称"/>
        </el-form-item>
        <el-form-item label="指令分类" prop="commandCategory">
          <el-select v-model="form.commandCategory" placeholder="请选择指令分类" style="width: 100%">
            <el-option label="普通用户功能" value="user"/>
            <el-option label="管理员功能" value="admin"/>
            <el-option label="超级管理员功能" value="super"/>
            <el-option label="系统通知功能" value="system"/>
          </el-select>
        </el-form-item>
        <el-form-item label="是否启用" prop="isEnabled">
          <el-radio-group v-model="form.isEnabled">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="禁用提示消息" prop="disabledMessage">
          <el-input
            v-model="form.disabledMessage"
            :rows="3"
            placeholder="当功能被禁用时，向用户显示的提示消息"
            type="textarea"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            :rows="2"
            placeholder="请输入备注信息"
            type="textarea"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addCmdconfig,
  clearCache,
  delCmdconfig,
  getCmdconfig,
  listCmdconfig,
  updateCmdconfig
} from "@/api/bot/cmdconfig";

export default {
  name: "Cmdconfig",
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
      // 群组指令功能配置表格数据
      cmdconfigList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        groupId: null,
        commandKey: null,
        commandName: null,
        commandCategory: null,
        isEnabled: null,
        disabledMessage: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        groupId: [
          {required: true, message: "群组ID不能为空", trigger: "blur"}
        ],
        commandKey: [
          {required: true, message: "指令关键字不能为空", trigger: "blur"}
        ],
        commandCategory: [
          {required: true, message: "指令分类不能为空", trigger: "change"}
        ],
        isEnabled: [
          {required: true, message: "是否启用不能为空", trigger: "change"}
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询群组指令功能配置列表 */
    getList() {
      this.loading = true;
      listCmdconfig(this.queryParams).then(response => {
        this.cmdconfigList = response.rows;
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
        groupId: null,
        commandKey: null,
        commandName: null,
        commandCategory: null,
        isEnabled: null,
        disabledMessage: null,
        createTime: null,
        updateTime: null,
        createBy: null,
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
      this.title = "添加群组指令功能配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getCmdconfig(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改群组指令功能配置";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateCmdconfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addCmdconfig(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除群组指令功能配置编号为"' + ids + '"的数据项？').then(function () {
        return delCmdconfig(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('bot/cmdconfig/export', {
        ...this.queryParams
      }, `cmdconfig_${new Date().getTime()}.xlsx`)
    },
    /** 状态开关切换 */
    handleStatusChange(row) {
      let text = row.isEnabled === 1 ? "启用" : "禁用";
      this.$modal.confirm('确认要"' + text + '""' + row.commandKey + '"功能吗？').then(() => {
        return updateCmdconfig(row);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
        this.getList();
      }).catch(() => {
        row.isEnabled = row.isEnabled === 0 ? 1 : 0;
      });
    },
    /** 清除缓存 */
    handleClearCache() {
      this.$modal.confirm('确认要清除所有指令配置缓存吗？清除后下次查询将重新从数据库加载。').then(() => {
        return clearCache();
      }).then(() => {
        this.$modal.msgSuccess("缓存清除成功");
      }).catch(() => {
      });
    }
  }
};
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  display: block;
  margin-top: 5px;
}
</style>
