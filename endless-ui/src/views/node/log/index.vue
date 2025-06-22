<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="100px" size="small">
      <el-form-item label="节点ID" prop="nodeId">
        <el-input
          v-model="queryParams.nodeId"
          clearable
          placeholder="请输入节点ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作类型" prop="operationType">
        <el-select v-model="queryParams.operationType" clearable placeholder="请选择操作类型">
          <el-option label="新增节点" value="1"/>
          <el-option label="修改节点" value="2"/>
          <el-option label="删除节点" value="3"/>
          <el-option label="下载日志" value="4"/>
          <el-option label="启动游戏服务器" value="5"/>
          <el-option label="停止游戏服务器" value="6"/>
          <el-option label="重启游戏服务器" value="7"/>
          <el-option label="强制终止游戏服务器" value="8"/>
          <el-option label="新增游戏服务器" value="9"/>
          <el-option label="修改游戏服务器" value="10"/>
          <el-option label="删除游戏服务器" value="11"/>
        </el-select>
      </el-form-item>
      <el-form-item label="操作目标" prop="operationTarget">
        <el-select v-model="queryParams.operationTarget" clearable placeholder="请选择操作目标">
          <el-option label="节点服务器" value="1"/>
          <el-option label="游戏服务器" value="2"/>
        </el-select>
      </el-form-item>
      <el-form-item label="操作状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择操作状态">
          <el-option label="成功" value="0"/>
          <el-option label="失败" value="1"/>
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
          v-hasPermi="['node:log:export']"
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

    <el-table v-loading="loading" :data="logList" style="width: 100%">
      <el-table-column align="center" label="操作时间" prop="createTime" width="160"/>
      <el-table-column align="center" label="节点ID" prop="nodeId" width="100"/>
      <el-table-column align="center" label="操作类型" prop="operationType" width="120">
        <template slot-scope="scope">
          <el-tag :type="getOperationTypeTag(scope.row.operationType)">
            {{ getOperationTypeLabel(scope.row.operationType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作目标" prop="operationTarget" width="100">
        <template slot-scope="scope">
          <el-tag>{{ getOperationTargetLabel(scope.row.operationTarget) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作名称" min-width="150" prop="operationName" show-overflow-tooltip/>
      <el-table-column align="center" label="方法名称" min-width="150" prop="methodName" show-overflow-tooltip/>
      <el-table-column align="center" label="执行耗时" prop="executionTime" width="100">
        <template slot-scope="scope">
          {{ scope.row.executionTime }}ms
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作状态" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
            {{ scope.row.status === '0' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="120">
        <template slot-scope="scope">
          <el-button
            icon="el-icon-view"
            size="mini"
            type="text"
            @click="handleView(scope.row)"
          >详情
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

    <!-- 查看操作日志详情对话框 -->
    <el-dialog :visible.sync="open" append-to-body title="操作日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="日志ID">{{ form.id }}</el-descriptions-item>
        <el-descriptions-item label="节点ID">{{ form.nodeId }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTag(form.operationType)">
            {{ getOperationTypeLabel(form.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作目标">
          <el-tag>{{ getOperationTargetLabel(form.operationTarget) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="节点对象ID">{{ form.nodeObjId }}</el-descriptions-item>
        <el-descriptions-item label="游戏服务器对象ID">{{ form.gameServerObjId }}</el-descriptions-item>
        <el-descriptions-item label="操作名称">{{ form.operationName }}</el-descriptions-item>
        <el-descriptions-item label="方法名称">{{ form.methodName }}</el-descriptions-item>
        <el-descriptions-item label="执行耗时">{{ form.executionTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作者IP">{{ form.operationIp }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="form.status === '0' ? 'success' : 'danger'">
            {{ form.status === '0' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ form.createTime }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="操作参数">
          <el-input
            v-model="form.operationParam"
            :rows="3"
            readonly
            type="textarea"
          />
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="操作结果">
          <el-input
            v-model="form.operationResult"
            :rows="3"
            readonly
            type="textarea"
          />
        </el-descriptions-item>
        <el-descriptions-item v-if="form.status === '1'" :span="2" label="错误消息">
          <el-input
            v-model="form.errorMsg"
            :rows="3"
            readonly
            type="textarea"
          />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import {getLog, listLog} from "@/api/node/log";

export default {
  name: "Log",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 操作日志表格数据
      logList: [],
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        nodeId: null,
        operationType: null,
        operationTarget: null,
        status: null
      },
      // 表单参数
      form: {}
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询操作日志列表 */
    getList() {
      this.loading = true;
      listLog(this.queryParams).then(response => {
        this.logList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
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
    /** 查看按钮操作 */
    handleView(row) {
      this.reset();
      const id = row.id;
      getLog(id).then(response => {
        this.form = response.data;
        this.open = true;
      });
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        nodeId: null,
        operationType: null,
        operationTarget: null,
        nodeObjId: null,
        gameServerObjId: null,
        operationName: null,
        methodName: null,
        operationParam: null,
        operationResult: null,
        executionTime: null,
        operationIp: null,
        status: null,
        errorMsg: null,
        createTime: null
      };
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('node/log/export', {
        ...this.queryParams
      }, `log_${new Date().getTime()}.xlsx`)
    },
    // 获取操作类型标签样式
    getOperationTypeTag(type) {
      const typeMap = {
        '1': 'success',
        '2': 'warning',
        '3': 'danger',
        '4': 'info',
        '5': 'success',
        '6': 'warning',
        '7': 'warning',
        '8': 'danger',
        '9': 'success',
        '10': 'warning',
        '11': 'danger'
      };
      return typeMap[type] || 'info';
    },
    // 获取操作类型标签文本
    getOperationTypeLabel(type) {
      const typeMap = {
        '1': '新增节点',
        '2': '修改节点',
        '3': '删除节点',
        '4': '下载日志',
        '5': '启动游戏服务器',
        '6': '停止游戏服务器',
        '7': '重启游戏服务器',
        '8': '强制终止游戏服务器',
        '9': '新增游戏服务器',
        '10': '修改游戏服务器',
        '11': '删除游戏服务器'
      };
      return typeMap[type] || type;
    },
    // 获取操作目标标签文本
    getOperationTargetLabel(target) {
      const targetMap = {
        '1': '节点服务器',
        '2': '游戏服务器'
      };
      return targetMap[target] || target;
    }
  }
};
</script>
