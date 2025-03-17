<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="服务器ID" prop="serverId">
        <el-input
          v-model="queryParams.serverId"
          clearable
          placeholder="请输入服务器ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="在线加白" prop="onlineAddWhitelistCommand">
        <el-input
          v-model="queryParams.onlineAddWhitelistCommand"
          clearable
          placeholder="请输入在线加白指令"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="离线加白" prop="offlineAddWhitelistCommand">
        <el-input
          v-model="queryParams.offlineAddWhitelistCommand"
          clearable
          placeholder="请输入离线加白指令"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="在线移白" prop="onlineRmWhitelistCommand">
        <el-input
          v-model="queryParams.onlineRmWhitelistCommand"
          clearable
          placeholder="请输入在线移白指令"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="离线移白" prop="offlineRmWhitelistCommand">
        <el-input
          v-model="queryParams.offlineRmWhitelistCommand"
          clearable
          placeholder="请输入离线移白指令"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="在线封禁" prop="onlineAddBanCommand">
        <el-input
          v-model="queryParams.onlineAddBanCommand"
          clearable
          placeholder="请输入在线封禁指令"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="离线封禁" prop="offlineAddBanCommand">
        <el-input
          v-model="queryParams.offlineAddBanCommand"
          clearable
          placeholder="请输入离线封禁指令"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="在线解封" prop="onlineRmBanCommand">
        <el-input
          v-model="queryParams.onlineRmBanCommand"
          clearable
          placeholder="请输入在线解封指令"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="离线解封" prop="offlineRmBanCommand">
        <el-input
          v-model="queryParams.offlineRmBanCommand"
          clearable
          placeholder="请输入离线解封指令"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['mc:command:add']"
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
          v-hasPermi="['mc:command:edit']"
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
          v-hasPermi="['mc:command:remove']"
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
          v-hasPermi="['mc:command:export']"
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

    <el-table v-loading="loading" :data="commandList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="主键ID" prop="id"/>
      <el-table-column align="center" label="服务器ID" prop="serverId"/>
      <el-table-column align="center" label="在线加白指令" prop="onlineAddWhitelistCommand" show-overflow-tooltip/>
      <el-table-column align="center" label="离线加白指令" prop="offlineAddWhitelistCommand" show-overflow-tooltip/>
      <el-table-column align="center" label="在线移白指令" prop="onlineRmWhitelistCommand" show-overflow-tooltip/>
      <el-table-column align="center" label="离线移白指令" prop="offlineRmWhitelistCommand" show-overflow-tooltip/>
      <el-table-column align="center" label="在线加封指令" prop="onlineAddBanCommand" show-overflow-tooltip/>
      <el-table-column align="center" label="离线加封指令" prop="offlineAddBanCommand" show-overflow-tooltip/>
      <el-table-column align="center" label="在线解封指令" prop="onlineRmBanCommand" show-overflow-tooltip/>
      <el-table-column align="center" label="离线解封指令" prop="offlineRmBanCommand" show-overflow-tooltip/>
      <el-table-column align="center" label="描述" prop="remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['mc:command:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['mc:command:remove']"
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

    <!-- 添加或修改指令管理对话框 -->
    <el-dialog :before-close="handleClose" :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="服务器ID" prop="server">
          <el-select v-model="serverList" :disabled=editFlag multiple placeholder="请选择服务器">
            <el-option
              v-for="item in serverOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="在线加白" prop="onlineAddWhitelistCommand">
          <el-input v-model="form.onlineAddWhitelistCommand" placeholder="请输入在线加白指令"/>
        </el-form-item>
        <el-form-item label="离线加白" prop="offlineAddWhitelistCommand">
          <el-input v-model="form.offlineAddWhitelistCommand" placeholder="请输入离线加白指令"/>
        </el-form-item>
        <el-form-item label="在线移白" prop="onlineRmWhitelistCommand">
          <el-input v-model="form.onlineRmWhitelistCommand" placeholder="请输入在线移白指令"/>
        </el-form-item>
        <el-form-item label="离线移白" prop="offlineRmWhitelistCommand">
          <el-input v-model="form.offlineRmWhitelistCommand" placeholder="请输入离线移白指令"/>
        </el-form-item>
        <el-form-item label="在线加封" prop="onlineAddBanCommand">
          <el-input v-model="form.onlineAddBanCommand" placeholder="请输入在线加封指令"/>
        </el-form-item>
        <el-form-item label="离线加封" prop="offlineAddBanCommand">
          <el-input v-model="form.offlineAddBanCommand" placeholder="请输入离线加封指令"/>
        </el-form-item>
        <el-form-item label="在线解封" prop="onlineRmBanCommand">
          <el-input v-model="form.onlineRmBanCommand" placeholder="请输入在线解封指令"/>
        </el-form-item>
        <el-form-item label="离线解封" prop="offlineRmBanCommand">
          <el-input v-model="form.offlineRmBanCommand" placeholder="请输入离线解封指令"/>
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入描述"/>
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
import {addCommand, delCommand, getCommand, listCommand, updateCommand} from "@/api/mc/command";
import {getServerList} from "@/api/mc/whitelist";

export default {
  name: "Command",
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
      // 指令管理表格数据
      commandList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        serverId: null,
        onlineAddWhitelistCommand: null,
        offlineAddWhitelistCommand: null,
        onlineRmWhitelistCommand: null,
        offlineRmWhitelistCommand: null,
        onlineAddBanCommand: null,
        offlineAddBanCommand: null,
        onlineRmBanCommand: null,
        offlineRmBanCommand: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        serverId: [
          {required: true, message: "服务器ID不能为空", trigger: "blur"}
        ],
      },
      serverList: [],
      serverOptions: [], // 服务器列表
      editFlag: false
    };
  },
  created() {
    this.getList();
    this.handleServerList();
  },
  methods: {
    /** 查询指令管理列表 */
    getList() {
      this.loading = true;
      listCommand(this.queryParams).then(response => {
        this.commandList = response.rows;
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
        serverId: null,
        createTime: null,
        createBy: null,
        onlineAddWhitelistCommand: null,
        offlineAddWhitelistCommand: null,
        onlineRmWhitelistCommand: null,
        offlineRmWhitelistCommand: null,
        onlineAddBanCommand: null,
        offlineAddBanCommand: null,
        onlineRmBanCommand: null,
        offlineRmBanCommand: null,
        updateTime: null,
        updateBy: null,
        remark: null
      };
      this.serverList = [];
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
      this.editFlag = false;
      this.open = true;
      this.title = "添加指令管理";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.editFlag = true;
      this.reset();
      const id = row.id || this.ids
      getCommand(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改指令管理";
        // 根据serverId字段回显服务器列表，支持多个服务器ID
        if (this.form.serverId !== null) {
          this.serverList = this.form.serverId.split(',').map(id => parseInt(id));
        }
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.serverList.length === 0) {
            this.$modal.msgError("请选择服务器");
            return;
          }
          // 服务器ID，保持原有格式
          this.form.serverId = this.serverList.join(",");

          if (this.form.id != null) {
            updateCommand(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addCommand(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除指令管理编号为"' + ids + '"的数据项？').then(function () {
        return delCommand(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('mc/command/export', {
        ...this.queryParams
      }, `command_${new Date().getTime()}.xlsx`)
    },
    /** 服务器列表 */
    handleServerList() {
      getServerList().then(response => {
        let date = response.data;
        date.forEach((item) => {
          if (item.status === 1) {
            this.serverOptions.push({label: item.nameTag, value: item.id});
          }
        });
      });
    },
    // 关闭前的确认回调,防止误触造成的数据丢失
    handleClose(done) {
      this.$confirm('确认关闭？')
        .then(_ => {
          done();
        })
        .catch(_ => {
        });
    }
  }
};
</script>
