<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="queryParams.name"
          clearable
          placeholder="请输入机器人名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="QQ" prop="botQq">
        <el-input
          v-model="queryParams.botQq"
          clearable
          placeholder="请输入机器人QQ"
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
          v-hasPermi="['bot:config:add']"
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
          v-hasPermi="['bot:config:edit']"
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
          v-hasPermi="['bot:config:remove']"
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
          v-hasPermi="['bot:config:export']"
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

    <el-table
      v-loading="loading"
      :cell-style="{ padding: '8px 0' }"
      :data="configList"
      :header-cell-style="{background:'#f8f8f9'}"
      border
      highlight-current-row
      stripe
      style="width: 100%; margin-top: 8px;"
      @selection-change="handleSelectionChange"
    >
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="主键" prop="id" width="80"/>
      <el-table-column align="center" label="机器人名称" min-width="120" prop="name" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-button
            type="text"
            @click="handleViewManagers(scope.row)"
          >{{ scope.row.name }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column align="center" label="机器人QQ" min-width="120" prop="botQq" show-overflow-tooltip/>
      <el-table-column align="center" label="群组" min-width="180" prop="groupIds" show-overflow-tooltip>
        <template slot-scope="scope">
          <el-tooltip effect="light" placement="top">
            <div slot="content">
              <div v-for="(group, index) in scope.row.groupIds.split(',')" :key="index">
                {{ group }}
              </div>
            </div>
            <span>{{ scope.row.groupIds }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column align="center" label="命令前缀" min-width="100" prop="commandPrefix"/>
      <el-table-column align="center" label="最后登录时间" prop="lastLoginTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastLoginTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="最后心跳时间" prop="lastHeartbeatTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastHeartbeatTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="启用状态" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 0 ? 'danger' : 'success'">
            {{ scope.row.status === 0 ? '停用' : '启用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="备注" min-width="120" prop="remark" show-overflow-tooltip/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="140">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['bot:config:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-divider direction="vertical"/>
          <el-button
            v-hasPermi="['bot:config:remove']"
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

    <!-- 添加或修改QQ机器人配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="700px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="机器人名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入机器人名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="机器人QQ" prop="botQq">
              <el-input v-model="form.botQq" placeholder="请输入机器人QQ"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="HTTP地址" prop="httpUrl">
              <el-input v-model="form.httpUrl" placeholder="请输入HTTP通讯地址"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="WS地址" prop="wsUrl">
              <el-input v-model="form.wsUrl" placeholder="请输入websocket地址"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="命令前缀" prop="commandPrefix">
              <el-input v-model="form.commandPrefix" placeholder="请输入命令前缀"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="秘钥" prop="token">
              <el-input v-model="form.token" placeholder="请输入秘钥" show-password/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="启用状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label=0>停用</el-radio>
                <el-radio :label=1>启用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="群组" prop="groupIds">
          <div class="group-container">
            <div v-for="(group, index) in groupList" :key="index" class="group-item">
              <el-input
                v-model="group.value"
                class="group-input"
                placeholder="请输入群号"
                @change="handleGroupChange"
              >
                <template slot="append">
                  <el-button
                    v-if="groupList.length > 1"
                    icon="el-icon-delete"
                    style="color: #F56C6C; padding: 0 5px"
                    type="text"
                    @click.prevent="removeGroup(index)"
                  />
                </template>
              </el-input>
            </div>
            <el-button
              icon="el-icon-plus"
              plain
              size="small"
              style="margin-top: 10px"
              type="primary"
              @click.prevent="addGroup"
            >添加群组
            </el-button>
          </div>
        </el-form-item>

        <!--        <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="最后登录" prop="lastLoginTime">
                      <el-date-picker clearable
                        v-model="form.lastLoginTime"
                        type="datetime"
                        value-format="yyyy-MM-dd HH:mm:ss"
                        placeholder="请选择最后登录时间">
                      </el-date-picker>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="最后心跳" prop="lastHeartbeatTime">
                      <el-date-picker clearable
                        v-model="form.lastHeartbeatTime"
                        type="datetime"
                        value-format="yyyy-MM-dd HH:mm:ss"
                        placeholder="请选择最后心跳时间">
                      </el-date-picker>
                    </el-form-item>
                  </el-col>
                </el-row>-->

        <!--        <el-form-item label="机器人描述" prop="description">
                  <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入机器人描述"/>
                </el-form-item>-->

        <!--        <el-form-item label="错误信息" prop="errorMsg">
                  <el-input v-model="form.errorMsg" type="textarea" :rows="3" placeholder="请输入错误信息"/>
                </el-form-item>-->

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" :rows="2" placeholder="请输入备注" type="textarea"/>
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
import {addConfig, delConfig, getConfig, listConfig, updateConfig} from "@/api/bot/config";

export default {
  name: "Config",
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
      // QQ机器人配置表格数据
      configList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        botQq: null,
        status: null,
      },
      // 表单参数
      form: {
        id: null,
        name: null,
        botQq: null,
        httpUrl: null,
        wsUrl: null,
        token: null,
        groupIds: null,
        commandPrefix: null,
        description: null,
        lastLoginTime: null,
        lastHeartbeatTime: null,
        errorMsg: null,
        status: '1', // 默认启用
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        remark: null
      },
      // 表单校验
      rules: {
        name: [
          {
            required: true, message: "机器人名称不能为空", trigger: "blur"
          }
        ],
        httpUrl: [
          {
            required: true, message: "HTTP通讯地址不能为空", trigger: "blur"
          }
        ],
        wsUrl: [
          {
            required: true, message: "websocket地址不能为空", trigger: "blur"
          }
        ],
        groupIds: [
          {
            required: true, message: "群组不能为空", trigger: "blur"
          }
        ],
        status: [
          {
            required: true, message: "启用状态不能为空", trigger: "change"
          }
        ],
      },
      groupList: [{value: ''}],
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询QQ机器人配置列表 */
    getList() {
      this.loading = true;
      listConfig(this.queryParams).then(response => {
        this.configList = response.rows;
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
        name: null,
        botQq: null,
        httpUrl: null,
        wsUrl: null,
        token: null,
        groupIds: null,
        commandPrefix: null,
        description: null,
        lastLoginTime: null,
        lastHeartbeatTime: null,
        errorMsg: null,
        status: '1', // 默认启用
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        remark: null
      };
      this.groupList = [{value: ''}];
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
      this.title = "添加QQ机器人配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getConfig(id).then(response => {
        this.form = response.data;
        // 处理群组数据
        if (this.form.groupIds) {
          this.groupList = this.form.groupIds.split(',').map(group => ({
            value: group
          }));
        }
        this.open = true;
        this.title = "修改QQ机器人配置";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addConfig(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除QQ机器人配置编号为"' + ids + '"的数据项？').then(function () {
        return delConfig(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('bot/config/export', {
        ...this.queryParams
      }, `config_${new Date().getTime()}.xlsx`)
    },
    /** 添加群组输入框 */
    addGroup() {
      this.groupList.push({
        value: ''
      });
    },
    /** 删除群组输入框 */
    removeGroup(index) {
      this.groupList.splice(index, 1);
      this.handleGroupChange();
    },
    /** 群组变更处理 */
    handleGroupChange() {
      const groups = this.groupList.map(item => item.value).filter(value => value !== '');
      this.form.groupIds = groups.join(',');
    },
    /** 查看管理员列表 */
    handleViewManagers(row) {
      this.$router.push({
        path: '/bot/manager',
        query: {
          botId: row.id,
          botName: row.name
        }
      });
    },
  }
};
</script>

<style scoped>
.group-container {
  width: 100%;
}

.group-item {
  margin-bottom: 10px;
}

.group-item:last-child {
  margin-bottom: 5px;
}

.group-input {
  width: 100%;
}

.group-input :deep(.el-input-group__append) {
  padding: 0 10px;
}

.group-input :deep(.el-input-group__append .el-button) {
  margin: 0;
}


.el-table ::v-deep .el-table__row:hover > td {
  background-color: #f5f7fa !important;
}

.el-table ::v-deep .el-table__header th {
  background-color: #f8f8f9 !important;
  color: #606266;
  font-weight: bold;
  height: 45px;
}

.el-table ::v-deep .el-table__body td {
  padding: 8px 0;
}

.el-button--text + .el-divider--vertical + .el-button--text {
  color: #F56C6C;
}

.el-button--text + .el-divider--vertical + .el-button--text:hover {
  background: rgba(245, 108, 108, 0.1);
}

</style>
