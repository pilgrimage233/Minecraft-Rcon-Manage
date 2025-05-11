<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="机器人ID" prop="botId">
        <el-input
          v-model="queryParams.botId"
          clearable
          placeholder="请输入机器人ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="日志类型" prop="logType">
        <el-select v-model="queryParams.logType" clearable placeholder="请选择日志类型">
          <el-option :value="1" label="接收消息"/>
          <el-option :value="2" label="发送消息"/>
          <el-option :value="3" label="方法调用"/>
          <el-option :value="4" label="系统事件"/>
        </el-select>
      </el-form-item>
      <el-form-item label="发送者" prop="senderId">
        <el-input
          v-model="queryParams.senderId"
          clearable
          placeholder="请输入发送者ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="方法名称" prop="methodName">
        <el-input
          v-model="queryParams.methodName"
          clearable
          placeholder="请输入方法名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="时间范围" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          end-placeholder="结束日期"
          range-separator="至"
          start-placeholder="开始日期"
          type="daterange"
          value-format="yyyy-MM-dd"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['bot:log:remove']"
          :disabled="multiple"
          icon="el-icon-delete"
          plain
          size="mini"
          type="danger"
          @click="handleDelete"
        >批量删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['bot:log:export']"
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

    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="ID" prop="id" width="80"/>
      <el-table-column align="center" label="机器人ID" prop="botId" width="100"/>
      <el-table-column align="center" label="日志类型" prop="logType" width="100">
        <template slot-scope="scope">
          <el-tag :type="getLogTypeTag(scope.row.logType)">
            {{ getLogTypeName(scope.row.logType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="发送者" prop="senderId" width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.senderId }}</span>
          <el-tag v-if="scope.row.senderType" size="mini" type="info">{{ scope.row.senderType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="接收者" prop="receiverId" width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.receiverId }}</span>
          <el-tag v-if="scope.row.receiverType" size="mini" type="info">{{ scope.row.receiverType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" align="center" label="消息内容" prop="messageContent">
        <template slot-scope="scope">
          <span v-if="scope.row.messageContent">{{ scope.row.messageContent }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" align="center" label="方法名称" prop="methodName" width="150">
        <template slot-scope="scope">
          <span v-if="scope.row.methodName">{{ scope.row.methodName }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="执行时间" prop="executionTime" width="100">
        <template slot-scope="scope">
          <span v-if="scope.row.executionTime">{{ scope.row.executionTime }}ms</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime" width="160"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="150">
        <template slot-scope="scope">
          <el-button
            icon="el-icon-view"
            size="mini"
            type="text"
            @click="handleView(scope.row)"
          >详情
          </el-button>
          <el-button
            v-hasPermi="['bot:log:remove']"
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

    <!-- 查看日志详情对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ form.id }}</el-descriptions-item>
        <el-descriptions-item label="机器人ID">{{ form.botId }}</el-descriptions-item>
        <el-descriptions-item label="日志类型">
          <el-tag :type="getLogTypeTag(form.logType)">{{ getLogTypeName(form.logType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="消息ID">{{ form.messageId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发送者ID">{{ form.senderId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发送者类型">{{ form.senderType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="接收者ID">{{ form.receiverId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="接收者类型">{{ form.receiverType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="消息类型">{{ form.messageType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ form.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">消息内容</el-divider>
      <div class="message-content">
        <pre v-if="form.messageContent">{{ form.messageContent }}</pre>
        <span v-else>-</span>
      </div>

      <el-divider content-position="left">方法调用信息</el-divider>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="方法名称">{{ form.methodName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{
            form.executionTime ? form.executionTime + 'ms' : '-'
          }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">方法参数</el-divider>
      <div class="json-content">
        <pre v-if="form.methodParams">{{ formatJson(form.methodParams) }}</pre>
        <span v-else>-</span>
      </div>

      <el-divider content-position="left">方法结果</el-divider>
      <div class="json-content">
        <pre v-if="form.methodResult">{{ formatJson(form.methodResult) }}</pre>
        <span v-else>-</span>
      </div>

      <el-divider v-if="form.errorMessage" content-position="left">错误信息</el-divider>
      <div v-if="form.errorMessage" class="error-content">
        <el-alert
          :closable="false"
          :title="form.errorMessage"
          show-icon
          type="error">
        </el-alert>

        <el-divider content-position="left">堆栈信息</el-divider>
        <div class="stack-trace">
          <pre>{{ form.stackTrace }}</pre>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {delLog, getLog, listLog} from "@/api/bot/log";
import {addDateRange} from "@/utils/ruoyi";

export default {
  name: "Log",
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
      // 机器人日志表格数据
      logList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 日期范围
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        botId: null,
        logType: null,
        messageId: null,
        senderId: null,
        senderType: null,
        receiverId: null,
        receiverType: null,
        messageContent: null,
        messageType: null,
        methodName: null,
        methodParams: null,
        methodResult: null,
        executionTime: null,
        errorMessage: null,
        stackTrace: null,
        beginTime: null,
        endTime: null
      },
      // 表单参数
      form: {}
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询机器人日志列表 */
    getList() {
      this.loading = true;
      listLog(addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.logList = response.rows;
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
        botId: null,
        logType: null,
        messageId: null,
        senderId: null,
        senderType: null,
        receiverId: null,
        receiverType: null,
        messageContent: null,
        messageType: null,
        methodName: null,
        methodParams: null,
        methodResult: null,
        executionTime: null,
        errorMessage: null,
        stackTrace: null,
        createTime: null
      };
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 查看详情按钮操作 */
    handleView(row) {
      this.reset();
      const id = row.id || this.ids
      getLog(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "查看日志详情";
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除机器人日志编号为"' + ids + '"的数据项？').then(function () {
        return delLog(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('bot/log/export', {
        ...this.queryParams
      }, `log_${new Date().getTime()}.xlsx`)
    },
    // 获取日志类型名称
    getLogTypeName(type) {
      const typeMap = {
        1: '接收消息',
        2: '发送消息',
        3: '方法调用',
        4: '系统事件'
      };
      return typeMap[type] || '未知类型';
    },
    // 获取日志类型标签样式
    getLogTypeTag(type) {
      const tagMap = {
        1: 'success',
        2: 'primary',
        3: 'warning',
        4: 'info'
      };
      return tagMap[type] || '';
    },
    // 格式化JSON
    formatJson(jsonString) {
      try {
        const obj = JSON.parse(jsonString);
        return JSON.stringify(obj, null, 2);
      } catch (e) {
        return jsonString;
      }
    }
  }
};
</script>

<style scoped>
.message-content, .json-content, .stack-trace {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 10px;
  margin: 10px 0;
  max-height: 300px;
  overflow: auto;
}

.message-content pre, .json-content pre, .stack-trace pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: monospace;
}

.error-content {
  margin: 10px 0;
}

.el-divider {
  margin: 20px 0;
}
</style>
