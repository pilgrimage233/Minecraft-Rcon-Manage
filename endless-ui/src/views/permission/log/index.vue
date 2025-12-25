<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" class="search-form"
             label-width="85px" size="small">
      <el-row :gutter="20">
        <el-col :span="5">
          <el-form-item label="操作用户" prop="userName">
            <el-input v-model="queryParams.userName" clearable placeholder="请输入操作用户"
                      @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="资源类型" prop="resourceType">
            <el-select v-model="queryParams.resourceType" clearable placeholder="请选择资源类型" style="width: 100%">
              <el-option label="RCON服务器" value="rcon_server"/>
              <el-option label="节点服务器" value="node_server"/>
              <el-option label="MC实例" value="mc_instance"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="操作类型" prop="actionType">
            <el-select v-model="queryParams.actionType" clearable placeholder="请选择操作类型" style="width: 100%">
              <el-option label="授权" value="grant"/>
              <el-option label="撤销" value="revoke"/>
              <el-option label="修改" value="modify"/>
              <el-option label="访问" value="access"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择状态" style="width: 100%">
              <el-option label="成功" value="0"/>
              <el-option label="失败" value="1"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="4" style="text-align: right">
          <el-form-item>
            <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="10">
          <el-form-item label="操作时间">
            <el-date-picker v-model="dateRange" end-placeholder="结束日期" range-separator="-" start-placeholder="开始日期"
                            style="width: 100%" type="daterange" value-format="yyyy-MM-dd"/>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['permission:log:remove']" :disabled="multiple" icon="el-icon-delete" plain size="mini"
                   type="danger" @click="handleDelete">删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['permission:log:remove']" icon="el-icon-delete" plain size="mini" type="danger"
                   @click="handleClean">清空
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['permission:log:export']" icon="el-icon-download" plain size="mini" type="warning"
                   @click="handleExport">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="日志ID" prop="id" width="70"/>
      <el-table-column align="center" label="操作用户" prop="userName" width="100"/>
      <el-table-column align="center" label="资源类型" prop="resourceType" width="110">
        <template slot-scope="scope">
          <el-tag :type="getResourceTypeTag(scope.row.resourceType)" size="small">
            {{ getResourceTypeName(scope.row.resourceType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="资源名称" min-width="120" prop="resourceName" show-overflow-tooltip/>
      <el-table-column align="center" label="操作类型" prop="actionType" width="80">
        <template slot-scope="scope">
          <el-tag :type="getActionTypeTag(scope.row.actionType)" size="small">{{
              getActionTypeName(scope.row.actionType)
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="目标类型" prop="targetType" width="80">
        <template slot-scope="scope">{{ scope.row.targetType === 'user' ? '用户' : '角色' }}</template>
      </el-table-column>
      <el-table-column align="center" label="目标名称" prop="targetName" width="100"/>
      <el-table-column align="center" label="操作IP" prop="ipAddress" width="130"/>
      <el-table-column align="center" label="状态" prop="status" width="70">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small">
            {{ scope.row.status === '0' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作时间" prop="createTime" width="160"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="100">
        <template slot-scope="scope">
          <el-button icon="el-icon-view" size="mini" type="text" @click="handleView(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :limit.sync="queryParams.pageSize" :page.sync="queryParams.pageNum" :total="total"
                @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog :visible.sync="detailOpen" append-to-body title="日志详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="操作用户">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="资源类型">{{ getResourceTypeName(detail.resourceType) }}</el-descriptions-item>
        <el-descriptions-item label="资源名称">{{ detail.resourceName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getActionTypeName(detail.actionType) }}</el-descriptions-item>
        <el-descriptions-item label="目标类型">{{
            detail.targetType === 'user' ? '用户' : '角色'
          }}
        </el-descriptions-item>
        <el-descriptions-item label="目标名称">{{ detail.targetName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ detail.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status === '0' ? '成功' : '失败' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="权限详情">
          <pre style="margin: 0; white-space: pre-wrap;">{{ formatJson(detail.permissionDetail) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMsg" :span="2" label="错误信息">{{
            detail.errorMsg
          }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import {cleanPermissionLog, delPermissionLog, getPermissionLog, listPermissionLog} from "@/api/permission/log";

export default {
  name: "PermissionLog",
  data() {
    return {
      loading: true, ids: [], multiple: true, showSearch: true, total: 0, logList: [],
      detailOpen: false, detail: {}, dateRange: [],
      queryParams: {pageNum: 1, pageSize: 10, userName: null, resourceType: null, actionType: null, status: null}
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      const params = {...this.queryParams};
      if (this.dateRange && this.dateRange.length === 2) {
        params.params = {beginTime: this.dateRange[0], endTime: this.dateRange[1]};
      }
      listPermissionLog(params).then(response => {
        this.logList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getResourceTypeName(type) {
      const map = {'rcon_server': 'RCON服务器', 'node_server': '节点服务器', 'mc_instance': 'MC实例'};
      return map[type] || type;
    },
    getResourceTypeTag(type) {
      const map = {'rcon_server': 'danger', 'node_server': 'warning', 'mc_instance': 'success'};
      return map[type] || 'info';
    },
    getActionTypeName(type) {
      const map = {'grant': '授权', 'revoke': '撤销', 'modify': '修改', 'access': '访问'};
      return map[type] || type;
    },
    getActionTypeTag(type) {
      const map = {'grant': 'success', 'revoke': 'danger', 'modify': 'warning', 'access': 'info'};
      return map[type] || 'info';
    },
    formatJson(str) {
      if (!str) return '';
      try {
        return JSON.stringify(JSON.parse(str), null, 2);
      } catch (e) {
        return str;
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.multiple = !selection.length;
    },
    handleView(row) {
      getPermissionLog(row.id).then(response => {
        this.detail = response.data;
        this.detailOpen = true;
      });
    },
    handleDelete() {
      const ids = this.ids;
      this.$modal.confirm('是否确认删除选中的日志记录？').then(() => delPermissionLog(ids)).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    handleClean() {
      this.$modal.confirm('是否确认清空所有权限操作日志？').then(() => cleanPermissionLog()).then(() => {
        this.getList();
        this.$modal.msgSuccess("清空成功");
      }).catch(() => {
      });
    },
    handleExport() {
      this.download('permission/log/export', {...this.queryParams}, `permission_log_${new Date().getTime()}.xlsx`);
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
