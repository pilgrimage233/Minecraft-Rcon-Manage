<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" class="search-form"
             label-width="85px"
             size="small">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="UUID" prop="uuid">
            <el-input
              v-model="queryParams.uuid"
              clearable
              placeholder="请输入节点UUID"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="节点名称" prop="name">
            <el-input
              v-model="queryParams.name"
              clearable
              placeholder="请输入节点名称"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="服务器IP" prop="ip">
            <el-input
              v-model="queryParams.ip"
              clearable
              placeholder="请输入服务器IP"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="API端口" prop="port">
            <el-input
              v-model="queryParams.port"
              clearable
              placeholder="请输入API端口"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="通信协议" prop="protocol">
            <el-select v-model="queryParams.protocol" clearable placeholder="请选择通信协议" style="width: 100%">
              <el-option label="HTTP" value="http"/>
              <el-option label="HTTPS" value="https"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择状态" style="width: 100%">
              <el-option
                v-for="dict in dict.type.sys_normal_disable"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="节点版本" prop="version">
            <el-input
              v-model="queryParams.version"
              clearable
              placeholder="请输入节点版本"
              @keyup.enter.native="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="最后心跳" prop="lastHeartbeat">
            <el-date-picker
              v-model="queryParams.lastHeartbeat"
              clearable
              placeholder="请选择最后心跳时间"
              style="width: 100%"
              type="date"
              value-format="yyyy-MM-dd">
            </el-date-picker>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24" style="text-align: right">
          <el-form-item>
            <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['node:server:add']"
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
          v-hasPermi="['node:server:edit']"
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
          v-hasPermi="['node:server:remove']"
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
          v-hasPermi="['node:server:export']"
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

    <el-table v-loading="loading" :data="serverList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="节点ID" prop="id"/>
      <el-table-column align="center" label="节点UUID" prop="uuid" show-overflow-tooltip/>
      <el-table-column align="center" label="节点名称" prop="name"/>
      <el-table-column align="center" label="服务器IP" prop="ip"/>
      <el-table-column align="center" label="API端口" prop="port"/>
      <el-table-column align="center" label="通信协议" prop="protocol">
        <template slot-scope="scope">
          <el-tag :type="scope.row.protocol === 'https' ? 'success' : 'danger'">
            {{ scope.row.protocol.toUpperCase() }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="status" width="110">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)" effect="dark" size="medium">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="最后心跳时间" prop="lastHeartbeat" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastHeartbeat, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="节点版本" prop="version"/>
      <el-table-column align="center" label="操作系统类型" prop="osType"/>
      <el-table-column align="center" label="节点描述" prop="description"/>
      <el-table-column align="center" label="备注" prop="remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="240">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['node:server:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['node:server:remove']"
            icon="el-icon-delete"
            size="mini"
            type="text"
            @click="handleDelete(scope.row)"
          >删除
          </el-button>
          <el-button
            v-hasPermi="['node:server:list']"
            icon="el-icon-monitor"
            size="mini"
            type="text"
            @click="handleNodeClick(scope.row)"
          >信息
          </el-button>
          <el-button
            v-hasPermi="['node:server:list']"
            icon="el-icon-folder"
            size="mini"
            type="text"
            @click="handleFileClick(scope.row)"
          >文件
          </el-button>
          <el-button
            v-hasPermi="['node:mcs:list']"
            icon="el-icon-collection"
            size="mini"
            type="text"
            @click="handleMcsClick(scope.row)"
          >实例
          </el-button>
          <el-button
            v-hasPermi="['node:env:list']"
            icon="el-icon-setting"
            size="mini"
            type="text"
            @click="handleEnvClick(scope.row)"
          >环境
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

    <!-- 添加或修改节点服务器对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body custom-class="server-dialog" width="600px">
      <el-form ref="form" :model="form" :rules="rules" class="server-form" label-width="100px">
        <el-form-item label="节点名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入节点名称"/>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务器IP" prop="ip">
              <el-input v-model="form.ip" placeholder="请输入服务器IP"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="API端口" prop="port">
              <el-input v-model="form.port" placeholder="请输入API端口"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="通信协议" prop="protocol">
              <el-select v-model="form.protocol" placeholder="请选择通信协议" style="width: 100%">
                <el-option label="HTTP" value="http"/>
                <el-option label="HTTPS" value="https"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="秘钥" prop="token">
              <el-input v-model="form.token" placeholder="请输入秘钥" show-password/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.sys_normal_disable"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="节点描述" prop="description">
          <el-input v-model="form.description" :rows="3" placeholder="请输入节点描述" type="textarea"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" :rows="2" placeholder="请输入备注" type="textarea"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button :loading="testLoading" icon="el-icon-connection" @click="handleTestConnection">测试连接</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>


  </div>
</template>

<script>
import {addServer, delServer, getServer, listServer, testConnection, updateServer} from "@/api/node/server";

export default {
  name: "Server",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 测试连接加载状态
      testLoading: false,
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
      // 节点服务器表格数据
      serverList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      statusDict: {
        '0': '正常',
        '1': '停止',
        '2': '故障',
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        uuid: null,
        name: null,
        ip: null,
        port: null,
        protocol: null,
        status: null,
        lastHeartbeat: null,
        version: null,
        osType: null,
        description: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          {required: true, message: "节点名称不能为空", trigger: "blur"}
        ],
        ip: [
          {required: true, message: "服务器IP不能为空", trigger: "blur"}
        ],
        port: [
          {required: true, message: "API端口不能为空", trigger: "blur"}
        ],
        token: [
          {required: true, message: "秘钥不能为空", trigger: "blur"}
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询节点服务器列表 */
    getList() {
      this.loading = true;
      listServer(this.queryParams).then(response => {
        this.serverList = response.rows;
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
        uuid: null,
        name: null,
        ip: null,
        port: null,
        protocol: null,
        token: null,
        status: null,
        lastHeartbeat: null,
        version: null,
        osType: null,
        description: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
        delFlag: null
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
      this.title = "添加节点服务器";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getServer(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改节点服务器";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateServer(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addServer(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除节点服务器编号为"' + ids + '"的数据项？').then(function () {
        return delServer(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('node/server/export', {
        ...this.queryParams
      }, `server_${new Date().getTime()}.xlsx`)
    },
    /** 节点名称点击操作 */
    handleNodeClick(row) {
      this.$router.push(`/node/server/info/${row.id}`);
    },
    /** 文件按钮点击操作 */
    handleFileClick(row) {
      this.$router.push(`/node/server/file/${row.id}`);
    },
    /** 实例按钮点击操作 */
    handleMcsClick(row) {
      this.$router.push({path: '/node/mcs/index', query: {nodeId: row.id, nodeUuid: row.uuid}});
    },
    /** 环境按钮点击操作 */
    handleEnvClick(row) {
      this.$router.push({path: '/node/env/index', query: {nodeId: row.id}});
    },
    /** 测试连接 */
    handleTestConnection() {
      // 验证必填字段
      if (!this.form.ip || !this.form.port || !this.form.protocol || !this.form.token) {
        this.$message.warning('请先填写完整的节点信息（IP、端口、协议、秘钥）');
        return;
      }

      this.testLoading = true;
      testConnection(this.form).then(response => {
        if (response.code === 200) {
          const data = response.data || {};
          this.$message.success(`连接成功！节点版本: ${data.version || '未知'}`);
        } else {
          this.$message.error(response.msg || '连接失败');
        }
      }).catch(error => {
        this.$message.error('连接失败: ' + (error.message || '网络错误'));
      }).finally(() => {
        this.testLoading = false;
      });
    },
    // 获取状态文本
    getStatusText(status) {
      return this.statusDict[status] || '未知';
    },
    // 获取状态标签类型
    statusTagType(status) {
      const statusMap = {
        '0': 'success',
        '1': 'info',
        '2': 'danger'
      };
      return statusMap[status] || 'warning';
    },
  }
};
</script>

<style lang="scss" scoped>
.server-dialog {
  .server-form {
    padding: 20px 20px 0;

    .el-form-item {
      margin-bottom: 22px;
    }

    .el-input, .el-select {
      width: 100%;
    }

    .el-textarea__inner {
      min-height: 80px;
    }
  }

  .dialog-footer {
    text-align: right;
    padding: 20px;
    border-top: 1px solid #e8e8e8;
  }
}

.search-form {
  background-color: #fff;
  padding: 20px 20px 0;
  margin-bottom: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);

  .el-form-item {
    margin-bottom: 20px;
  }

  .el-input, .el-select, .el-date-picker {
    width: 100%;
  }
}


</style>
