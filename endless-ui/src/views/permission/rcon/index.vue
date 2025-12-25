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
          <el-form-item label="服务器" prop="serverId">
            <el-select v-model="queryParams.serverId" clearable placeholder="请选择服务器" style="width: 100%">
              <el-option v-for="server in serverList" :key="server.id" :label="server.nameTag" :value="server.id"/>
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
        <el-col :span="6" style="text-align: right">
          <el-form-item>
            <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['permission:rcon:add']" icon="el-icon-plus" plain size="mini" type="primary"
                   @click="handleAdd">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['permission:rcon:remove']" :disabled="multiple" icon="el-icon-delete" plain size="mini"
                   type="danger" @click="handleDelete">删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="permissionList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="ID" prop="id" width="60"/>
      <el-table-column align="center" label="用户" prop="userName" width="120"/>
      <el-table-column align="center" label="RCON服务器" min-width="150" prop="serverName"/>
      <el-table-column align="center" label="权限类型" prop="permissionType" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.permissionType === 'admin' ? 'danger' : 'success'">
            {{ scope.row.permissionType === 'admin' ? '管理员' : '自定义' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="权限详情" width="250">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.canViewLog" size="mini" style="margin: 2px" type="info">查看日志</el-tag>
          <el-tag v-if="scope.row.canExecuteCmd" size="mini" style="margin: 2px" type="primary">执行命令</el-tag>
          <el-tag v-if="scope.row.canManage" size="mini" style="margin: 2px" type="warning">管理配置</el-tag>
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
          <el-button v-hasPermi="['permission:rcon:edit']" icon="el-icon-edit" size="mini" type="text"
                     @click="handleUpdate(scope.row)">修改
          </el-button>
          <el-button v-hasPermi="['permission:rcon:remove']" icon="el-icon-delete" size="mini" type="text"
                     @click="handleDelete(scope.row)">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :limit.sync="queryParams.pageSize" :page.sync="queryParams.pageNum" :total="total"
                @pagination="getList"/>

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="600px">
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
            <el-form-item label="服务器" prop="serverId">
              <el-select v-model="form.serverId" placeholder="请选择服务器" style="width: 100%">
                <el-option v-for="server in serverList" :key="server.id" :label="server.nameTag" :value="server.id"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="权限类型" prop="permissionType">
          <el-select v-model="form.permissionType" placeholder="请选择权限类型" style="width: 100%"
                     @change="handlePermissionTypeChange">
            <el-option label="管理员(全部权限)" value="admin"/>
            <el-option label="自定义权限" value="custom"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.permissionType === 'custom'" label="权限配置">
          <el-checkbox-group v-model="permissionChecks">
            <el-checkbox label="canViewLog">查看日志</el-checkbox>
            <el-checkbox label="canExecuteCmd">执行命令</el-checkbox>
            <el-checkbox label="canManage">管理配置</el-checkbox>
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
        <el-form-item label="命令白名单" prop="cmdWhitelist">
          <el-input v-model="form.cmdWhitelist" :rows="2"
                    placeholder="允许执行的命令，多个用逗号分隔，留空表示不限制" type="textarea"/>
        </el-form-item>
        <el-form-item label="命令黑名单" prop="cmdBlacklist">
          <el-input v-model="form.cmdBlacklist" :rows="2" placeholder="禁止执行的命令，多个用逗号分隔" type="textarea"/>
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
  addRconPermission,
  delRconPermission,
  getRconPermission,
  listRconPermission,
  updateRconPermission
} from "@/api/permission/rcon";
import {listUser} from "@/api/system/user";
import {listServerlist} from "@/api/server/serverlist";

export default {
  name: "RconPermission",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0,
      permissionList: [], userList: [], serverList: [], title: "", open: false, permissionChecks: [],
      queryParams: {pageNum: 1, pageSize: 10, userId: null, serverId: null, status: null},
      form: {},
      rules: {
        userId: [{required: true, message: "请选择用户", trigger: "change"}],
        serverId: [{required: true, message: "请选择RCON服务器", trigger: "change"}],
        permissionType: [{required: true, message: "请选择权限类型", trigger: "change"}]
      }
    };
  },
  created() {
    this.getList();
    this.loadUserList();
    this.loadServerList();
  },
  methods: {
    getList() {
      this.loading = true;
      listRconPermission(this.queryParams).then(response => {
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
    loadServerList() {
      listServerlist({pageNum: 1, pageSize: 1000}).then(response => {
        this.serverList = response.rows || [];
      });
    },
    handlePermissionTypeChange(type) {
      if (type === 'admin') {
        this.permissionChecks = ['canViewLog', 'canExecuteCmd', 'canManage'];
      }
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        id: null,
        userId: null,
        serverId: null,
        permissionType: 'custom',
        canViewLog: 0,
        canExecuteCmd: 0,
        canManage: 0,
        cmdWhitelist: null,
        cmdBlacklist: null,
        expireTime: null,
        status: "0",
        remark: null
      };
      this.permissionChecks = [];
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
      this.title = "新增RCON权限";
    },
    handleUpdate(row) {
      this.reset();
      getRconPermission(row.id || this.ids).then(response => {
        this.form = response.data;
        this.permissionChecks = [];
        if (this.form.canViewLog) this.permissionChecks.push('canViewLog');
        if (this.form.canExecuteCmd) this.permissionChecks.push('canExecuteCmd');
        if (this.form.canManage) this.permissionChecks.push('canManage');
        this.open = true;
        this.title = "修改RCON权限";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.canViewLog = this.permissionChecks.includes('canViewLog') ? 1 : 0;
          this.form.canExecuteCmd = this.permissionChecks.includes('canExecuteCmd') ? 1 : 0;
          this.form.canManage = this.permissionChecks.includes('canManage') ? 1 : 0;
          if (this.form.id != null) {
            updateRconPermission(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addRconPermission(this.form).then(() => {
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
      this.$modal.confirm('是否确认删除选中的权限记录？').then(() => delRconPermission(ids)).then(() => {
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
