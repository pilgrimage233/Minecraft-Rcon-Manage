<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="指令" prop="cmd">
        <el-input
          v-model="queryParams.cmd"
          clearable
          placeholder="请输入指令"
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
          v-hasPermi="['regular:command:add']"
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
          v-hasPermi="['regular:command:edit']"
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
          v-hasPermi="['regular:command:remove']"
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
          v-hasPermi="['regular:command:export']"
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
      <el-table-column align="center" label="主键" prop="id"/>
      <el-table-column align="center" label="任务名称" prop="taskName"/>
      <el-table-column align="center" label="指令" prop="cmd"/>
      <el-table-column align="center" label="执行结果" prop="result"/>
      <el-table-column align="center" label="结果保留次数" prop="historyCount"/>
      <el-table-column align="center" label="历史结果" prop="historyResult">
        <template slot-scope="scope">
          <el-popover
            placement="right"
            popper-class="history-result-popover"
            trigger="hover"
            width="400"
          >
            <div class="history-result-content">
              <div v-for="(result, time) in parseHistoryResult(scope.row.historyResult)" :key="time"
                   class="history-item">
                <div class="history-time">{{ time }}</div>
                <div class="history-text">{{ result }}</div>
              </div>
            </div>
            <el-button slot="reference" type="text">
              查看历史记录 ({{ getHistoryCount(scope.row.historyResult) }})
            </el-button>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column align="center" label="Cron" prop="cron"/>
      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
            {{ scope.row.status === 0 ? '正常' : '暂停' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="执行次数" prop="executeCount"/>
      <el-table-column align="center" label="备注" prop="remark"/>
      <el-table-column align="center" label="执行服务器" prop="executeServer">
        <template slot-scope="scope">
          <el-tag
            v-for="serverId in scope.row.executeServer ? scope.row.executeServer.split(',') : []"
            :key="serverId"
            size="mini"
            style="margin-right: 4px"
          >
            {{ getServerName(serverId) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['regular:command:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['regular:command:remove']"
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

    <!-- 添加或修改定时命令对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名称"/>
        </el-form-item>
        <el-form-item label="执行指令" prop="cmd">
          <el-input v-model="form.cmd" placeholder="请输入执行指令"/>
        </el-form-item>
        <el-form-item label="执行服务器" prop="executeServer">
          <el-select v-model="form.executeServer" multiple placeholder="请选择执行服务器" style="width: 100%"
                     @change="handleServerChange">
            <el-option
              v-if="serverOptions.length > 1"
              key="all"
              label="全部服务器"
              value="all"
            />
            <el-option
              v-for="item in serverOptions"
              :key="item.uuid"
              :label="item.nameTag"
              :value="item.id + ''"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Cron表达式" prop="cron">
          <el-input v-model="form.cron" placeholder="请输入Cron表达式">
            <template slot="append">
              <el-button type="primary" @click="handleShowCron">
                生成表达式
                <i class="el-icon-time el-icon--right"></i>
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="item in [{label: '正常', value: 0}, {label: '暂停', value: 1}]"
              :key="item.value"
              :label="item.value"
            >{{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="历史保留数" prop="historyCount">
          <el-input-number v-model="form.historyCount" :max="100" :min="1"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入内容" type="textarea"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- Cron表达式生成器弹窗 -->
    <el-dialog :visible.sync="openCron" append-to-body class="scrollbar" destroy-on-close title="Cron表达式生成器">
      <crontab :expression="expression" @fill="crontabFill" @hide="openCron=false"></crontab>
    </el-dialog>
  </div>
</template>

<script>
import {addCommand, delCommand, getCommand, getServerList, listCommand, updateCommand} from "@/api/regular/command";

import Crontab from '@/components/Crontab' // 引入 Cron 组件

export default {
  name: "Command",
  components: {Crontab},  // 注册组件
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
      // 定时命令表格数据
      commandList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示Cron表达式生成器弹窗
      openCron: false,
      // 传入的表达式
      expression: "",
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        taskName: null,
        cmd: null,
        result: null,
        historyCount: null,
        historyResult: null,
        cron: null,
        status: null,
        executeCount: null
      },
      // 服务器选项列表
      serverOptions: [],
      // 表单参数
      form: {
        id: undefined,
        taskName: undefined,
        cmd: undefined,
        executeServer: [],  // 改为数组以支持多选
        cron: undefined,
        historyCount: 10,
        status: 0,
        remark: undefined
      },
      // 表单校验
      rules: {
        taskName: [
          {required: true, message: "任务名称不能为空", trigger: "blur"}
        ],
        cmd: [
          {required: true, message: "执行指令不能为空", trigger: "blur"}
        ],
        cron: [
          {required: true, message: "Cron表达式不能为空", trigger: "blur"}
        ],
        historyCount: [
          {required: true, message: "历史保留数不能为空", trigger: "blur"}
        ],
        executeServer: [
          {required: true, message: "请选择执行服务器", trigger: "change"}
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getServerList();  // 获取服务器列表
  },
  methods: {
    /** 查询定时命令列表 */
    getList() {
      this.loading = true;
      listCommand(this.queryParams).then(response => {
        this.commandList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 获取服务器列表 */
    getServerList() {
      getServerList().then(response => {
        this.serverOptions = response.data;
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
        id: undefined,
        taskName: undefined,
        cmd: undefined,
        executeServer: [],  // 重置时清空选择
        cron: undefined,
        historyCount: 10,
        status: 0,
        remark: undefined
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
      this.title = "添加定时命令";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getCommand(id).then(response => {
        let data = response.data;
        // 处理服务器数据
        data.executeServer = data.executeServer === 'all' ? ['all'] :
          (data.executeServer ? data.executeServer.split(',') : []);
        this.form = data;
        this.open = true;
        this.title = "修改定时命令";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const form = {
            ...this.form,
            executeServer: this.form.executeServer.includes('all') ? 'all' :
              (Array.isArray(this.form.executeServer) ? this.form.executeServer.join(',') : this.form.executeServer)
          };

          if (form.id != null) {
            updateCommand(form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addCommand(form).then(response => {
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
      this.$modal.confirm('是否确认删除定时命令编号为"' + ids + '"的数据项？').then(function () {
        return delCommand(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('regular/command/export', {
        ...this.queryParams
      }, `command_${new Date().getTime()}.xlsx`)
    },
    /** cron表达式按钮操作 */
    handleShowCron() {
      this.expression = this.form.cron;
      this.openCron = true;
    },
    /** 确定后回传值 */
    crontabFill(value) {
      this.form.cron = value;
    },
    /** 解析历史结果JSON字符串 */
    parseHistoryResult(historyResult) {
      try {
        // 由于后端返回的是双重JSON字符串，需要解析两次
        const parsed = JSON.parse(JSON.parse(historyResult));
        return parsed;
      } catch (e) {
        return {};
      }
    },
    /** 获取历史记录数量 */
    getHistoryCount(historyResult) {
      try {
        const parsed = this.parseHistoryResult(historyResult);
        return Object.keys(parsed).length;
      } catch (e) {
        return 0;
      }
    },
    /** 处理服务器选择变化 */
    handleServerChange(value) {
      if (value.includes('all')) {
        // 如果选择了"全部"，则只保留"all"
        this.form.executeServer = ['all'];
      } else if (value.length === this.serverOptions.length) {
        // 如果选择的服务器数量等于所有服务器数量，自动切换为"全部"
        this.form.executeServer = ['all'];
      }
    },
    /** 根据服务器ID获取服务器名称 */
    getServerName(serverId) {
      if (serverId === 'all') {
        return '全部服务器';
      }
      const server = this.serverOptions.find(item => item.id + '' === serverId);
      return server ? server.nameTag : '未知服务器';
    }
  }
}
;
</script>

<style scoped>
.history-result-content {
  max-height: 300px;
  overflow-y: auto;
}

.history-item {
  padding: 8px;
  border-bottom: 1px solid #eee;
}

.history-item:last-child {
  border-bottom: none;
}

.history-time {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}

.history-text {
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  color: #303133;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
}
</style>
