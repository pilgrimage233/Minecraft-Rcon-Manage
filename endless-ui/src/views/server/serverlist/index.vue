<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="服务器名" prop="nameTag">
        <el-input
          v-model="queryParams.nameTag"
          clearable
          placeholder="请输入服务器名称标签"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="服务器IP" prop="ip">
        <el-input
          v-model="queryParams.ip"
          clearable
          placeholder="请输入服务器IP"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="远程端口" prop="rconPort">
        <el-input
          v-model="queryParams.rconPort"
          clearable
          placeholder="请输入远程端口号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime"
                        clearable
                        placeholder="请选择创建时间"
                        type="date"
                        value-format="yyyy-MM-dd">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="创建者" prop="creatBy">
        <el-input
          v-model="queryParams.creatBy"
          clearable
          placeholder="请输入创建者"
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
          v-hasPermi="['server:serverlist:add']"
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
          v-hasPermi="['server:serverlist:edit']"
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
          v-hasPermi="['server:serverlist:remove']"
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
          v-hasPermi="['server:serverlist:export']"
          icon="el-icon-download"
          plain
          size="mini"
          type="warning"
          @click="handleExport"
        >导出
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['server:serverlist:refresh']"
          icon="el-icon-refresh"
          plain
          size="mini"
          type="info"
          @click="handleRefresh"
        >刷新缓存
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="serverlistList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="主键ID" prop="id"/>
      <el-table-column align="center" label="服务器名称" prop="nameTag"/>
      <el-table-column align="center" label="远程地址" prop="ip"/>
      <el-table-column align="center" label="远程端口号" prop="rconPort"/>
      <el-table-column align="center" label="游玩地址" prop="playAddress"/>
      <el-table-column align="center" label="地址端口" prop="playAddressPort"/>
      <el-table-column align="center" label="版本" prop="serverVersion"/>
      <el-table-column align="center" label="核心" prop="serverCore">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.serverCore">{{ scope.row.serverCore }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建者" prop="createBy"/>
      <el-table-column align="center" label="启用状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.server_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="描述" prop="remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['server:serverlist:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['server:serverlist:remove']"
            icon="el-icon-delete"
            size="mini"
            type="text"
            @click="handleDelete(scope.row)"
          >删除
          </el-button>
          <el-button
            icon="el-icon-monitor"
            size="mini"
            type="text"
            @click="openConsole(scope.row)"
          >终端
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

    <!-- 添加或修改服务器信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <div v-if="form.id" style="color: #F56C6C; margin-bottom: 15px;">
        提示：修改服务器信息时，密码是加密后的，不需要修改保持默认即可
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-position="left" label-width="100px"
               size="medium">
        <el-row>
          <el-col :span="12">
            <el-form-item label="游玩地址" prop="playAddress">
              <el-input v-model="form.playAddress" :style="{width: '95%'}" clearable placeholder="请输入游玩地址">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地址端口" prop="playAddressPort">
              <el-input v-model="form.playAddressPort" :style="{width: '95%'}" clearable placeholder="请输入游玩端口">
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="服务器版本" prop="serverVersion">
              <el-input
                v-model="form.serverVersion"
                :style="{width: '95%'}"
                clearable
                placeholder="请输入服务器版本">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务器核心" prop="serverCore">
              <el-select v-model="form.serverCore" :style="{width: '95%'}" clearable placeholder="请选择服务器核心">
                <el-option
                  v-for="item in serverCoreOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="form.serverCore === 'Custom'" label="自定义核心" prop="customServerCore">
          <el-input v-model="form.customServerCore" :style="{width: '95%'}" clearable
                    placeholder="请输入自定义服务器核心名称"></el-input>
        </el-form-item>
        <el-form-item label="服务器名称" prop="nameTag">
          <el-input v-model="form.nameTag" :style="{width: '100%'}" clearable placeholder="请输入服务器名称">
          </el-input>
        </el-form-item>
        <el-form-item label="RCON地址" prop="ip">
          <el-input v-model="form.ip" :style="{width: '100%'}" clearable placeholder="请输入服务器IP/域名">
          </el-input>
        </el-form-item>
        <el-form-item label="远程端口号" prop="rconPort">
          <el-input v-model="form.rconPort" :style="{width: '100%'}" clearable placeholder="请输入远程端口号">
          </el-input>
        </el-form-item>
        <el-form-item label="远程密码" prop="rconPassword">
          <el-input v-model="form.rconPassword" :style="{width: '100%'}" clearable placeholder="请输入远程密码">
            <template slot="append">
              <el-tooltip content="密码将被加密存储" placement="top">
                <i class="el-icon-lock"></i>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" :autosize="{minRows: 4, maxRows: 4}" :style="{width: '100%'}"
                    placeholder="请输入描述" type="textarea"></el-input>
        </el-form-item>
        <el-form-item label="启用" prop="status" required>
          <el-switch v-model="form.status" active-color="#67F48A" inactive-color="#EF4E4E"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 控制台对话框 -->
    <el-dialog
      :close-on-click-modal="false"
      :title="'服务器控制台 - ' + currentServer.nameTag"
      :visible.sync="consoleVisible"
      append-to-body
      custom-class="console-dialog"
      width="900px"
      @close="handleConsoleClose"
    >
      <div class="console-container">
        <div class="terminal-header">
          <span :class="{ 'connected': isConnected }" class="status-indicator"></span>
          <span class="server-info">
            {{ currentServer.ip }}:{{ currentServer.rconPort }}
          </span>
          <el-button
            class="refresh-btn"
            icon="el-icon-refresh"
            size="mini"
            type="primary"
            @click="reconnectServer"
          >重新连接
          </el-button>
          <el-button
            class="history-btn"
            icon="el-icon-time"
            size="mini"
            type="info"
            @click="showHistory"
          >历史记录
          </el-button>
        </div>
        <div class="terminal-wrapper">
          <div id="terminal" ref="terminal"></div>
        </div>
        <div class="console-input">
          <el-autocomplete
            ref="commandInput"
            v-model="command"
            :disabled="!isConnected"
            :fetch-suggestions="queryMinecraftCommands"
            class="minecraft-command-input"
            placeholder="输入命令后按 Enter 发送"
            popper-append-to-body
            :trigger-on-focus="true"
            @select="handleCommandSelect"
            @keyup.enter.native="sendCommand"
          >
            <template slot-scope="{ item }">
              <div class="command-suggestion">
                <span class="command-keyword">{{ item.value }}</span>
                <span class="command-desc">{{ item.description }}</span>
              </div>
            </template>
            <el-button slot="append" :disabled="!isConnected" @click="sendCommand">发送</el-button>
          </el-autocomplete>
        </div>
      </div>
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog
      :visible.sync="historyVisible"
      append-to-body
      title="命令历史记录"
      width="900px"
    >
      <el-table
        v-loading="historyLoading"
        :data="historyList"
        height="400px"
        style="width: 100%"
      >
        <el-table-column
          label="执行命令"
          prop="command"
          show-overflow-tooltip
        />
        <el-table-column
          label="执行结果"
          prop="response"
          show-overflow-tooltip
        />
        <el-table-column
          align="center"
          label="执行时间"
          prop="executeTime"
          width="160"
        >
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.executeTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>

        <el-table-column
          align="center"
          label="耗时"
          prop="runTime"
          width="160"
        />

        <el-table-column
          align="center"
          label="执行用户"
          prop="user"
          width="160"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.user }}</span>
          </template>
        </el-table-column>
        <el-table-column
          align="center"
          label="操作"
          width="80"
        >
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              @click="reuseCommand(scope.row.command)"
            >重用
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="historyTotal > 0"
        :limit.sync="historyQuery.pageSize"
        :page.sync="historyQuery.pageNum"
        :total="historyTotal"
        @pagination="getHistoryList"
      />
    </el-dialog>
  </div>
</template>

<script>
import {
  addServerlist,
  delServerlist,
  getServerlist,
  listServerlist,
  refreshCache,
  updateServerlist
} from "@/api/server/serverlist";
import {Terminal} from 'xterm';
import {FitAddon} from 'xterm-addon-fit';
import {WebLinksAddon} from 'xterm-addon-web-links';
import 'xterm/css/xterm.css';
import {highlightMinecraftSyntax, MINECRAFT_KEYWORDS} from '@/utils/minecraftSyntax';
import {connectRcon, executeCommand, getCommandHistory} from "@/api/server/rcon";

export default {
  inheritAttrs: false,
  components: {},
  dicts: ['server_status'],
  props: [],
  name: "Serverlist",
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
      // 服务器信息表格数据
      serverlistList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        uuid: null,
        nameTag: null,
        ip: null,
        rconPort: null,
        rconPassword: null,
        createTime: null,
        createBy: null,
        status: null,
      },
      /** 服务器核心选项 */
      serverCoreOptions: [
        {value: 'Vanilla', label: 'Vanilla'},
        {value: 'Spigot', label: 'Spigot'},
        {value: 'Paper', label: 'Paper'},
        {value: 'Forge', label: 'Forge'},
        {value: 'Fabric', label: 'Fabric'},
        {value: 'Bukkit', label: 'Bukkit'},
        {value: 'CraftBukkit', label: 'CraftBukkit'},
        {value: 'Sponge', label: 'Sponge'},
        {value: 'Mohist', label: 'Mohist'},
        {value: 'CatServer', label: 'CatServer'},
        {value: 'Arclight', label: 'Arclight'},
        {value: 'Purpur', label: 'Purpur'},
        {value: 'Custom', label: '自定义'}
      ],
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        nameTag: [{
          required: true,
          message: '请输入服务器名称',
          trigger: 'blur'
        }],
        ip: [{
          required: true,
          message: '请输入服务器IP',
          trigger: 'blur'
        }],
        rconPort: [{
          required: true,
          message: '请输入远程端口号',
          trigger: 'blur'
        }],
        rconPassword: [{
          required: true,
          message: '请输入远程密码',
          trigger: 'blur'
        }],
        playAddress: [{
          required: true,
          message: '请输入游玩地址',
          trigger: 'blur'
        }],
        playAddressPort: [{
          required: true,
          message: '请输入游玩端口',
          trigger: 'blur'
        }],
        serverVersion: [{
          required: true,
          message: '请输入服务器版本',
          trigger: 'blur'
        }],
        serverCore: [{
          required: true,
          message: '请选择服务器核心',
          trigger: 'change'
        }]
      },
      consoleVisible: false,
      currentServer: {},
      terminal: null,
      command: '',
      fitAddon: null,
      isConnected: false,
      historyVisible: false,
      historyLoading: false,
      historyList: [],
      historyTotal: 0,
      historyQuery: {
        pageNum: 1,
        pageSize: 10,
        serverId: undefined
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询服务器信息列表 */
    getList() {
      this.loading = true;
      listServerlist(this.queryParams).then(response => {
        this.serverlistList = response.rows;
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
        nameTag: null,
        ip: null,
        rconPort: null,
        rconPassword: null,
        status: null,
        remark: null,
        playAddress: null,
        playAddressPort: null,
        serverVersion: null,
        serverCore: null
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
      this.title = "添加服务器信息";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getServerlist(id).then(response => {
        this.form = response.data;
        this.form.status = this.form.status === 1;
        this.open = true;
        this.title = "修改服务器信息";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.status = this.form.status ? 1 : 0;
          if (this.form.id != null) {
            updateServerlist(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addServerlist(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除服务器信息编号为"' + ids + '"的数据项？').then(function () {
        return delServerlist(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('server/serverlist/export', {
        ...this.queryParams
      }, `serverlist_${new Date().getTime()}.xlsx`)
    },
    handleRefresh() {
      refreshCache().then(() => {
        this.$modal.msgSuccess("刷新成功");
      })
    },
    /** 打开控制台 */
    openConsole(row) {
      this.currentServer = row;
      this.consoleVisible = true;
      this.$nextTick(() => {
        if (!this.terminal) {
          this.initTerminal();
        }
        this.fitAddon.fit();
        // 连接到服务器
        this.connectToServer();
      });
    },
    /** 初始化终端 */
    initTerminal() {
      this.terminal = new Terminal({
        cursorBlink: true,
        theme: {
          background: '#1a1a1a',
          foreground: '#f0f0f0',
          cursor: '#f0f0f0',
          selection: 'rgba(255, 255, 255, 0.2)',
          black: '#000000',
          red: '#ff5555',
          green: '#50fa7b',
          yellow: '#f1fa8c',
          blue: '#6272a4',
          magenta: '#ff79c6',
          cyan: '#8be9fd',
          white: '#f8f8f2',
        },
        fontSize: 15,
        fontFamily: 'Consolas, "Courier New", monospace',
        lineHeight: 1.3,
        scrollback: 1000,
        allowTransparency: true,
        padding: 10,
        windowsMode: true,  // 更好的 Windows 兼容性
      });

      this.fitAddon = new FitAddon();
      this.terminal.loadAddon(this.fitAddon);
      this.terminal.loadAddon(new WebLinksAddon());

      this.terminal.open(this.$refs.terminal);
      this.fitAddon.fit();

      // 监听窗口大小变化
      window.addEventListener('resize', () => {
        this.fitAddon.fit();
      });
    },
    /** 连接到服务器 */
    async connectToServer() {
      try {
        this.terminal.clear();
        this.terminal.writeln('\x1b[90m正在连接到服务器...\x1b[0m');

        const response = await connectRcon(this.currentServer.id);

        if (response.code === 200) {
          this.isConnected = true;
          this.terminal.writeln('');  // 添加空行
          this.terminal.writeln('\x1b[32m✓ 连接成功!\x1b[0m');
          this.terminal.writeln('\x1b[34m┌──────────────────────────────────────┐\x1b[0m');
          this.terminal.writeln(`\x1b[34m│\x1b[0m 服务器: ${this.currentServer.nameTag}`);
          this.terminal.writeln(`\x1b[34m│\x1b[0m 地址: ${this.currentServer.ip}:${this.currentServer.rconPort}`);
          this.terminal.writeln('\x1b[34m└──────────────────────────────────────┘\x1b[0m');
          this.terminal.writeln('');
          this.terminal.writeln('\x1b[90m输入命令开始操作，输入 help 获取帮助\x1b[0m\n');
        } else {
          throw new Error(response.msg);
        }
      } catch (error) {
        this.isConnected = false;
        this.terminal.writeln('\x1b[31m✗ 连接失败: ' + error.message + '\x1b[0m');
      }
    },
    /** 重新连接服务器 */
    reconnectServer() {
      this.isConnected = false;
      this.connectToServer();
    },
    /** 关闭控制台时清理 */
    handleConsoleClose() {
      this.isConnected = false;
      this.command = '';
      if (this.terminal) {
        this.terminal.clear();
      }
    },
    /** 发送命令 */
    async sendCommand() {
      if (!this.command) return;

      try {
        // 高亮显示发送的命令
        const highlightedCommand = highlightMinecraftSyntax(this.command);
        this.terminal.writeln('\x1b[36m❯\x1b[0m ' + highlightedCommand);

        const response = await executeCommand(this.currentServer.id, this.command);

        if (response.code === 200) {
          const responseText = response.data.response;
          if (responseText) {
            this.terminal.writeln(''); // 添加空行
            const lines = responseText.split(/([\/])/g);
            lines.forEach((line, index) => {
              if (line === '/') {
                if (index < lines.length - 1) {
                  this.terminal.writeln('\n' + line + lines[index + 1]);
                  lines[index + 1] = '';
                }
              } else if (line.trim() && !lines[index - 1]?.includes('/')) {
                this.terminal.writeln(line);
              }
            });
            this.terminal.writeln(''); // 添加空行
          }
        } else {
          throw new Error(response.msg);
        }

        // 清空命令输入
        this.command = '';
        // 关闭补全下拉框
        if (this.$refs.commandInput) {
          this.$refs.commandInput.$refs.input.blur();
        }
      } catch (error) {
        this.terminal.writeln('\x1b[31m✗ 错误: ' + error.message + '\x1b[0m');
        if (error.message.includes('连接已断开')) {
          this.isConnected = false;
        }
      }
    },
    queryMinecraftCommands(queryString, cb) {
      // 添加更详细的命令说明
      const commands = MINECRAFT_KEYWORDS.map(cmd => {
        let description;
        switch (cmd) {
          case 'give':
            description = '给予玩家物品';
            break;
          case 'tp':
          case 'teleport':
            description = '传送玩家到指定位置';
            break;
          case 'kill':
            description = '杀死目标实体';
            break;
          case 'gamemode':
          case 'gm':
            description = '更改游戏模式';
            break;
          case 'time':
            description = '更改或查询游戏时间';
            break;
          case 'weather':
            description = '更改天气';
            break;
          default:
            description = '执行 ' + cmd + ' 命令';
        }
        return {
          value: cmd,
          description: description
        };
      });

      const results = queryString
        ? commands.filter(cmd =>
          cmd.value.toLowerCase().includes(queryString.toLowerCase())
        )
        : commands;

      cb(results);
    },
    /** 处理命令选择 */
    handleCommandSelect(item) {
      this.command = item.value;
    },
    /** 显示历史记录 */
    showHistory() {
      this.historyVisible = true;
      this.historyQuery.serverId = this.currentServer.id;
      this.getHistoryList();
    },
    /** 获取历史记录列表 */
    async getHistoryList() {
      this.historyLoading = true;
      try {
        const response = await getCommandHistory(this.historyQuery);
        this.historyList = response.rows;
        this.historyTotal = response.total;
      } catch (error) {
        this.$modal.msgError("获取历史记录失败");
      }
      this.historyLoading = false;
    },
    /** 重用历史命令 */
    reuseCommand(command) {
      this.command = command;
      this.historyVisible = false;
      // 聚焦到命令输入框
      this.$nextTick(() => {
        this.$refs.commandInput.$refs.input.focus();
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.console-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.console-container {
  display: flex;
  flex-direction: column;
  height: 600px;
  background: #1a1a1a;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.terminal-header {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background: #282a36;
  border-bottom: 1px solid #333;

  .status-indicator {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #ff5555;
    margin-right: 8px;
    transition: background-color 0.3s ease;

    &.connected {
      background: #50fa7b;
    }
  }

  .server-info {
    color: #909399;
    font-size: 12px;
    flex: 1;
  }

  .refresh-btn {
    margin-left: 8px;
  }

  .history-btn {
    margin-left: 8px;
  }
}

.terminal-wrapper {
  flex: 1;
  padding: 16px;
  background: #1a1a1a;
  overflow: hidden;

  #terminal {
    height: 100%;
    border-radius: 4px;
    overflow: hidden;

    :deep(.xterm) {
      padding: 8px;
    }
  }
}

.console-input {
  padding: 16px;
  background: #282a36;
  border-top: 1px solid #333;

  :deep(.el-input-group__append) {
    background: #6272a4;
    border-color: #6272a4;
    color: white;
    transition: all 0.3s ease;

    &:hover {
      background: #7282b4;
      border-color: #7282b4;
    }

    &:active {
      background: #5262a4;
      border-color: #5262a4;
    }
  }

  :deep(.el-input__inner) {
    background: #1a1a1a;
    border-color: #333;
    color: #fff;
    transition: all 0.3s ease;

    &:focus {
      border-color: #6272a4;
      box-shadow: 0 0 0 2px rgba(98, 114, 164, 0.1);
    }
  }
}

.command-suggestion {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  cursor: pointer;

  &:hover {
    background-color: #2c2c2c;
  }

  .command-keyword {
    color: #00b4b4;
    font-weight: bold;
    margin-right: 8px;
  }

  .command-desc {
    color: #909399;
    font-size: 12px;
  }
}

.minecraft-command-input {
  width: 100%;
}

// 历史记录表格中的长文本显示省略
:deep(.el-table) {
  .cell {
    white-space: nowrap;
  }
}
</style>
