<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" class="search-form"
             label-width="85px" size="small">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="用户" prop="userId">
            <el-select v-model="queryParams.userId" clearable filterable placeholder="请选择用户" style="width: 100%">
              <el-option v-for="user in userList" :key="user.userId" :label="user.nickName" :value="user.userId"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="节点" prop="nodeId">
            <el-select v-model="queryParams.nodeId" clearable placeholder="请选择节点" style="width: 100%"
                       @change="handleNodeChange">
              <el-option v-for="node in nodeList" :key="node.id" :label="node.name" :value="node.id"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="实例" prop="instanceId">
            <el-select v-model="queryParams.instanceId" clearable placeholder="请选择实例" style="width: 100%">
              <el-option v-for="instance in instanceList" :key="instance.id" :label="instance.name"
                         :value="instance.id"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择状态" style="width: 100%">
              <el-option label="正常" value="0"/>
              <el-option label="停用" value="1"/>
            </el-select>
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
        <el-button v-hasPermi="['permission:instance:add']" icon="el-icon-plus" plain size="mini" type="primary"
                   @click="handleAdd">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['permission:instance:remove']" :disabled="multiple" icon="el-icon-delete" plain
                   size="mini" type="danger" @click="handleDelete">删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="permissionList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="ID" prop="id" width="60"/>
      <el-table-column align="center" label="用户" prop="userName" width="100"/>
      <el-table-column align="center" label="节点" prop="nodeName" width="120"/>
      <el-table-column align="center" label="实例" min-width="120" prop="instanceName"/>
      <el-table-column align="center" label="权限类型" prop="permissionType" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.permissionType === 'admin' ? 'danger' : 'success'">
            {{ scope.row.permissionType === 'admin' ? '管理员' : '自定义' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="权限详情" width="280">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.canView" size="mini" style="margin: 2px" type="info">查看</el-tag>
          <el-tag v-if="scope.row.canStart" size="mini" style="margin: 2px" type="success">启动</el-tag>
          <el-tag v-if="scope.row.canStop" size="mini" style="margin: 2px" type="warning">停止</el-tag>
          <el-tag v-if="scope.row.canRestart" size="mini" style="margin: 2px" type="primary">重启</el-tag>
          <el-tag v-if="scope.row.canConsole" size="mini" style="margin: 2px">控制台</el-tag>
          <el-tag v-if="scope.row.canFile" size="mini" style="margin: 2px" type="info">文件</el-tag>
          <el-tag v-if="scope.row.canConfig" size="mini" style="margin: 2px" type="warning">配置</el-tag>
          <el-tag v-if="scope.row.canDelete" size="mini" style="margin: 2px" type="danger">删除</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="过期时间" prop="expireTime" width="160">
        <template slot-scope="scope">
          <span v-if="scope.row.expireTime">{{ parseTime(scope.row.expireTime) }}</span>
          <span v-else>永久</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{
              scope.row.status === '0' ? '正常' : '停用'
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="150">
        <template slot-scope="scope">
          <el-button v-hasPermi="['permission:instance:edit']" icon="el-icon-edit" size="mini" type="text"
                     @click="handleUpdate(scope.row)">修改
          </el-button>
          <el-button v-hasPermi="['permission:instance:remove']" icon="el-icon-delete" size="mini" type="text"
                     @click="handleDelete(scope.row)">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :limit.sync="queryParams.pageSize" :page.sync="queryParams.pageNum" :total="total"
                @pagination="getList"/>


    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="700px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户" prop="userId">
              <el-select v-model="form.userId" filterable placeholder="请选择用户" style="width: 100%">
                <el-option v-for="user in userList" :key="user.userId" :label="user.nickName" :value="user.userId"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="节点" prop="nodeId">
              <el-select v-model="form.nodeId" placeholder="请选择节点" style="width: 100%"
                         @change="handleFormNodeChange">
                <el-option v-for="node in nodeList" :key="node.id" :label="node.name" :value="node.id"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="实例" prop="instanceId">
              <el-select v-model="form.instanceId" placeholder="请选择实例" style="width: 100%">
                <el-option v-for="instance in formInstanceList" :key="instance.id" :label="instance.name"
                           :value="instance.id"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限类型" prop="permissionType">
              <el-select v-model="form.permissionType" placeholder="请选择权限类型" style="width: 100%"
                         @change="handlePermissionTypeChange">
                <el-option label="管理员(全部权限)" value="admin"/>
                <el-option label="自定义权限" value="custom"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="form.permissionType === 'custom'" label="权限配置">
          <el-checkbox-group v-model="permissionChecks">
            <el-checkbox label="canView">查看</el-checkbox>
            <el-checkbox label="canStart">启动</el-checkbox>
            <el-checkbox label="canStop">停止</el-checkbox>
            <el-checkbox label="canRestart">重启</el-checkbox>
            <el-checkbox label="canConsole">控制台</el-checkbox>
            <el-checkbox label="canFile">文件管理</el-checkbox>
            <el-checkbox label="canConfig">配置修改</el-checkbox>
            <el-checkbox label="canDelete">删除实例</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="过期时间" prop="expireTime">
              <el-date-picker v-model="form.expireTime" clearable placeholder="留空表示永久" style="width: 100%"
                              type="datetime" value-format="yyyy-MM-dd HH:mm:ss"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="0">正常</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="控制台命令白名单" prop="consoleCmdWhitelist">
          <el-input v-model="form.consoleCmdWhitelist" :rows="2"
                    placeholder="允许执行的命令，多个用逗号分隔，留空表示不限制" type="textarea"/>
        </el-form-item>
        <el-form-item label="控制台命令黑名单" prop="consoleCmdBlacklist">
          <el-input v-model="form.consoleCmdBlacklist" :rows="2" placeholder="禁止执行的命令，多个用逗号分隔"
                    type="textarea"/>
        </el-form-item>
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
import {
  addInstancePermission,
  delInstancePermission,
  getInstancePermission,
  listInstancePermission,
  updateInstancePermission
} from "@/api/permission/instance";
import {listServer} from "@/api/node/server";
import {listMcs} from "@/api/node/mcs";
import {listUser} from "@/api/system/user";

export default {
  name: "InstancePermission",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      permissionList: [],
      userList: [],
      nodeList: [],
      instanceList: [],
      formInstanceList: [],
      title: "",
      open: false,
      permissionChecks: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        nodeId: null,
        instanceId: null,
        status: null
      },
      form: {},
      rules: {
        userId: [{required: true, message: "请选择用户", trigger: "change"}],
        nodeId: [{required: true, message: "请选择节点", trigger: "change"}],
        instanceId: [{required: true, message: "请选择实例", trigger: "change"}],
        permissionType: [{required: true, message: "请选择权限类型", trigger: "change"}]
      }
    };
  },
  created() {
    this.getList();
    this.loadUserList();
    this.loadNodeList();
  },
  methods: {
    getList() {
      this.loading = true;
      listInstancePermission(this.queryParams).then(response => {
        this.permissionList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    loadUserList() {
      listUser({pageNum: 1, pageSize: 1000}).then(response => {
        this.userList = response.rows;
      });
    },
    loadNodeList() {
      listServer({pageNum: 1, pageSize: 1000}).then(response => {
        this.nodeList = response.rows;
      });
    },
    loadInstanceList(nodeId) {
      if (nodeId) {
        listMcs({nodeId: nodeId, pageNum: 1, pageSize: 1000}).then(response => {
          this.instanceList = response.rows;
        });
      } else {
        this.instanceList = [];
      }
    },
    handleNodeChange(nodeId) {
      this.queryParams.instanceId = null;
      this.loadInstanceList(nodeId);
    },
    handleFormNodeChange(nodeId) {
      this.form.instanceId = null;
      if (nodeId) {
        listMcs({nodeId: nodeId, pageNum: 1, pageSize: 1000}).then(response => {
          this.formInstanceList = response.rows;
        });
      } else {
        this.formInstanceList = [];
      }
    },
    handlePermissionTypeChange(type) {
      if (type === 'admin') {
        this.permissionChecks = ['canView', 'canStart', 'canStop', 'canRestart', 'canConsole', 'canFile', 'canConfig', 'canDelete'];
      }
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        id: null, userId: null, nodeId: null, instanceId: null, permissionType: 'custom',
        canView: 0, canStart: 0, canStop: 0, canRestart: 0, canConsole: 0, canFile: 0, canConfig: 0, canDelete: 0,
        consoleCmdWhitelist: null, consoleCmdBlacklist: null, expireTime: null, status: "0", remark: null
      };
      this.permissionChecks = [];
      this.formInstanceList = [];
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增实例权限";
    },
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getInstancePermission(id).then(response => {
        this.form = response.data;
        this.permissionChecks = [];
        if (this.form.canView) this.permissionChecks.push('canView');
        if (this.form.canStart) this.permissionChecks.push('canStart');
        if (this.form.canStop) this.permissionChecks.push('canStop');
        if (this.form.canRestart) this.permissionChecks.push('canRestart');
        if (this.form.canConsole) this.permissionChecks.push('canConsole');
        if (this.form.canFile) this.permissionChecks.push('canFile');
        if (this.form.canConfig) this.permissionChecks.push('canConfig');
        if (this.form.canDelete) this.permissionChecks.push('canDelete');
        if (this.form.nodeId) {
          this.handleFormNodeChange(this.form.nodeId);
        }
        this.open = true;
        this.title = "修改实例权限";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.canView = this.permissionChecks.includes('canView') ? 1 : 0;
          this.form.canStart = this.permissionChecks.includes('canStart') ? 1 : 0;
          this.form.canStop = this.permissionChecks.includes('canStop') ? 1 : 0;
          this.form.canRestart = this.permissionChecks.includes('canRestart') ? 1 : 0;
          this.form.canConsole = this.permissionChecks.includes('canConsole') ? 1 : 0;
          this.form.canFile = this.permissionChecks.includes('canFile') ? 1 : 0;
          this.form.canConfig = this.permissionChecks.includes('canConfig') ? 1 : 0;
          this.form.canDelete = this.permissionChecks.includes('canDelete') ? 1 : 0;
          if (this.form.id != null) {
            updateInstancePermission(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addInstancePermission(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除选中的权限记录？').then(() => {
        return delInstancePermission(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.search-form {
  background-color: #fff;
  padding: 20px 20px 0;
  margin-bottom: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
</style>
