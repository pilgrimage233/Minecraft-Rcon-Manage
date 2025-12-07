<template>
  <div class="app-container mcs-container">
    <!-- 搜索区域 -->
    <el-card v-show="showSearch" class="search-card" shadow="never">
      <el-form ref="queryForm" :inline="true" :model="queryParams" label-width="80px" size="small">
        <el-form-item label="名称" prop="name">
          <el-input
            v-model="queryParams.name"
            clearable
            placeholder="请输入服务器名称"
            prefix-icon="el-icon-search"
            style="width: 200px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="核心类型" prop="coreType">
          <el-select
            v-model="queryParams.coreType"
            clearable
            placeholder="请选择核心类型"
            style="width: 140px"
          >
            <el-option
              v-for="opt in coreTypeOptions"
              :key="opt"
              :label="opt"
              :value="opt"
            >
              <span style="font-size: 14px; margin-right: 6px;">{{ coreTypeIcons[opt] }}</span>
              <span>{{ opt }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="核心版本" prop="version">
          <el-input
            v-model="queryParams.version"
            clearable
            placeholder="请输入核心版本"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="queryParams.status"
            clearable
            placeholder="请选择状态"
            style="width: 140px"
          >
            <el-option
              v-for="(label, value) in statusDict"
              :key="value"
              :label="label"
              :value="value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="最后启动" prop="lastStartTime">
          <el-date-picker
            v-model="queryParams.lastStartTime"
            clearable
            placeholder="请选择最后启动时间"
            style="width: 180px"
            type="date"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item label="最后停止" prop="lastStopTime">
          <el-date-picker
            v-model="queryParams.lastStopTime"
            clearable
            placeholder="请选择最后停止时间"
            style="width: 180px"
            type="date"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item>
          <el-button icon="el-icon-search" size="small" type="primary" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 节点筛选提示 -->
    <el-alert
      v-if="routeNodeId"
      :closable="true"
      :title="`正在按节点筛选：Node ID = ${routeNodeId}`"
      class="node-filter-alert"
      show-icon
      type="info"
      @close="clearNodeFilter"
    />

    <!-- 操作按钮区域 -->
    <el-card class="toolbar-card" shadow="never">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            v-hasPermi="['node:mcs:add']"
            icon="el-icon-plus"
            size="small"
            type="primary"
            @click="handleAdd"
          >新增实例
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            v-hasPermi="['node:mcs:edit']"
            :disabled="single"
            icon="el-icon-edit"
            plain
            size="small"
            type="success"
            @click="handleUpdate"
          >修改
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            v-hasPermi="['node:mcs:remove']"
            :disabled="multiple"
            icon="el-icon-delete"
            plain
            size="small"
            type="danger"
            @click="handleDelete"
          >删除
          </el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            v-hasPermi="['node:mcs:export']"
            icon="el-icon-download"
            plain
            size="small"
            type="warning"
            @click="handleExport"
          >导出
          </el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="mcsList" border stripe @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column type="expand">
        <template slot-scope="props">
          <div class="expand-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="expand-item">
                  <span class="expand-label">服务端目录：</span>
                  <span class="expand-value">{{ props.row.serverPath || '-' }}</span>
                </div>
                <div class="expand-item">
                  <span class="expand-label">启动命令：</span>
                  <span class="expand-value">{{ props.row.startStr || '-' }}</span>
                </div>
                <div class="expand-item">
                  <span class="expand-label">其他JVM参数：</span>
                  <span class="expand-value">{{ props.row.jvmArgs || '-' }}</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="expand-item">
                  <span class="expand-label">节点UUID：</span>
                  <span class="expand-value">{{ props.row.nodeUuid || '-' }}</span>
                </div>
                <div class="expand-item">
                  <span class="expand-label">描述：</span>
                  <span class="expand-value">{{ props.row.description || '-' }}</span>
                </div>
                <div class="expand-item">
                  <span class="expand-label">备注：</span>
                  <span class="expand-value">{{ props.row.remark || '-' }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </template>
      </el-table-column>
        <el-table-column align="center" label="实例ID" prop="id" width="80"/>
        <el-table-column align="center" label="节点ID" prop="nodeId" width="90"/>
        <el-table-column align="center" label="服务器名称" min-width="140" prop="name" show-overflow-tooltip>
        <template slot-scope="scope">
          <i class="el-icon-s-platform" style="color: #409EFF; margin-right: 5px;"></i>
          <span style="font-weight: 500;">{{ scope.row.name }}</span>
        </template>
      </el-table-column>
        <el-table-column align="center" label="核心类型" prop="coreType" width="110">
        <template slot-scope="scope">
          <el-tag size="small" type="info">{{ scope.row.coreType }}</el-tag>
        </template>
      </el-table-column>
        <el-table-column align="center" label="核心版本" prop="version" width="110"/>
        <el-table-column align="center" label="内存配置" width="130">
        <template slot-scope="scope">
          <div style="font-size: 12px;">
            <div>XMX: {{ scope.row.jvmXmx }}MB</div>
            <div style="color: #909399;">XMS: {{ scope.row.jvmXms }}MB</div>
          </div>
        </template>
      </el-table-column>
        <el-table-column align="center" label="状态" prop="status" width="110">
          <template slot-scope="scope">
            <el-tag :type="statusTagType(scope.row.status)" effect="dark" size="medium">
              <i :class="getStatusIcon(scope.row.status)" style="margin-right: 4px;"></i>
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="最后启动" prop="lastStartTime" width="110">
          <template slot-scope="scope">
            <span style="font-size: 12px;">{{ parseTime(scope.row.lastStartTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="最后停止" prop="lastStopTime" width="110">
          <template slot-scope="scope">
            <span style="font-size: 12px;">{{ parseTime(scope.row.lastStopTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="220">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['node:mcs:list']"
            icon="el-icon-monitor"
            size="mini"
            type="primary"
            @click="openTerminal(scope.row)"
          >控制台
          </el-button>
          <el-button
            v-hasPermi="['node:mcs:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['node:mcs:remove']"
            icon="el-icon-delete"
            size="mini"
            style="color: #F56C6C;"
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
    </el-card>

    <!-- 添加或修改实例管理对话框 -->
    <el-dialog
      :title="title"
      :visible.sync="open"
      append-to-body
      class="mcs-dialog"
      width="1000px"
      @close="cancel"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="130px" size="small">
        <!-- 基本信息 -->
        <div class="form-section">
          <div class="section-header">
            <i class="el-icon-info"></i>
            <span>基本信息</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="所属节点ID" prop="nodeId">
                <el-input v-model="form.nodeId" :disabled="true" placeholder="自动绑定路由参数">
                  <i slot="prefix" class="el-icon-connection"></i>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="节点UUID" prop="nodeUuid">
                <el-input v-model="form.nodeUuid" :disabled="true" placeholder="自动绑定路由参数">
                  <i slot="prefix" class="el-icon-key"></i>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="服务器名称" prop="name">
                <el-input v-model="form.name" placeholder="例如：Survival-1">
                  <i slot="prefix" class="el-icon-s-platform"></i>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="核心类型" prop="coreType">
                <el-select v-model="form.coreType" placeholder="选择核心类型" style="width: 100%">
                  <el-option v-for="opt in coreTypeOptions" :key="opt" :label="opt" :value="opt">
                    <span style="font-size: 16px; margin-right: 8px;">{{ coreTypeIcons[opt] }}</span>
                    <span>{{ opt }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="核心版本" prop="version">
                <el-input v-model="form.version" placeholder="例如：1.20.1">
                  <i slot="prefix" class="el-icon-price-tag"></i>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- JVM配置 -->
        <div class="form-section">
          <div class="section-header">
            <i class="el-icon-cpu"></i>
            <span>JVM配置</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="Java环境" prop="javaEnvId">
                <el-select v-model="form.javaEnvId" clearable placeholder="选择Java环境或自定义" style="width: 100%"
                           @change="handleJavaEnvChange">
                  <el-option
                    v-for="env in javaEnvList"
                    :key="env.id"
                    :label="`${env.envName} (${env.version})`"
                    :value="env.id"
                  >
                    <span style="float: left">{{ env.envName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ env.version }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="Java可执行路径" prop="javaPath">
                <el-input v-model="form.javaPath" placeholder="留空使用系统默认，或指定完整路径如：/usr/bin/java">
                  <i slot="prefix" class="el-icon-link"></i>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="最大内存(XMX)" prop="jvmXmx">
                <el-input v-model="form.jvmXmx" placeholder="例如：4096">
                  <template slot="append">MB</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="最小内存(XMS)" prop="jvmXms">
                <el-input v-model="form.jvmXms" placeholder="例如：1024">
                  <template slot="append">MB</template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="其他JVM参数" prop="jvmArgs">
            <el-input
              v-model="form.jvmArgs"
              :autosize="{ minRows: 2, maxRows: 4 }"
              placeholder="例如：-XX:+UseG1GC -XX:+ParallelRefProcEnabled"
              type="textarea"
            />
          </el-form-item>
        </div>

        <!-- 启动配置 -->
        <div class="form-section">
          <div class="section-header">
            <i class="el-icon-video-play"></i>
            <span>启动配置</span>
          </div>
          <el-form-item label="服务端所在目录" prop="serverPath">
            <el-input
              v-model="form.serverPath"
              :autosize="{ minRows: 1, maxRows: 3 }"
              placeholder="服务器根目录绝对路径，例如：/home/mc/server"
              type="textarea"
            >
              <i slot="prefix" class="el-icon-folder-opened"></i>
            </el-input>
          </el-form-item>
          <el-form-item label="启动命令" prop="startStr">
            <el-input
              v-model="form.startStr"
              :autosize="{ minRows: 2, maxRows: 6 }"
              placeholder="完整启动命令，例如：java -Xmx4096M -Xms1024M -jar paper.jar nogui"
              type="textarea"
            />
          </el-form-item>
        </div>

        <!-- 备注信息 -->
        <div class="form-section">
          <div class="section-header">
            <i class="el-icon-document"></i>
            <span>备注信息</span>
          </div>
          <el-form-item label="服务器描述" prop="description">
            <el-input
              v-model="form.description"
              :autosize="{ minRows: 2, maxRows: 5 }"
              placeholder="该实例的用途、注意事项等"
              type="textarea"
            />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="form.remark"
              :autosize="{ minRows: 1, maxRows: 3 }"
              placeholder="可选备注信息"
              type="textarea"
            />
          </el-form-item>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button icon="el-icon-check" type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {addMcs, delMcs, getMcs, listMcs, updateMcs} from "@/api/node/mcs";
import {getServer} from "@/api/node/server";
import {listEnv} from "@/api/node/env";

export default {
  name: "Mcs",
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
      // 实例管理表格数据
      mcsList: [],
      // Java环境列表
      javaEnvList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 状态字典
      statusDict: {
        '0': '未启动',
        '1': '运行中',
        '2': '已停止',
        '3': '异常'
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        nodeId: null,
        nodeUuid: null,
        name: null,
        serverPath: null,
        startStr: null,
        jvmXmx: null,
        jvmXms: null,
        jvmArgs: null,
        coreType: null,
        version: null,
        status: null,
        lastStartTime: null,
        lastStopTime: null,
        description: null,
      },
      // 路由参数
      routeNodeId: this.$route.query.nodeId,
      routeNodeUuid: this.$route.query.nodeUuid,
      // 表单参数
      form: {},
      coreTypeOptions: [
        'Vanilla',
        'Paper',
        'Spigot',
        'Bukkit',
        'CraftBukkit',
        'Purpur',
        'Pufferfish',
        'Airplane',
        'Tuinity',
        'Fabric',
        'Forge',
        'NeoForge',
        'Quilt',
        'Sponge',
        'Mohist',
        'Arclight',
        'CatServer',
        'Magma',
        'Banner',
        'Leaves',
        'Folia',
        'Velocity',
        'BungeeCord',
        'Waterfall'
      ],
      // 核心类型图标映射
      coreTypeIcons: {
        'Vanilla': '🍦',
        'Paper': '📄',
        'Spigot': '🔧',
        'Bukkit': '📦',
        'CraftBukkit': '⚒️',
        'Purpur': '💜',
        'Pufferfish': '🐡',
        'Airplane': '✈️',
        'Tuinity': '⚡',
        'Fabric': '🧵',
        'Forge': '⚙️',
        'NeoForge': '🔨',
        'Quilt': '🧶',
        'Sponge': '🧽',
        'Mohist': '🌋',
        'Arclight': '💡',
        'CatServer': '🐱',
        'Magma': '🔥',
        'Banner': '🚩',
        'Leaves': '🍃',
        'Folia': '🌿',
        'Velocity': '🚀',
        'BungeeCord': '🔗',
        'Waterfall': '💧'
      },
      // 表单校验
      rules: {
        nodeId: [
          {required: true, message: "所属节点ID不能为空", trigger: "blur"}
        ],
        nodeUuid: [
          {required: true, message: "节点UUID不能为空", trigger: "blur"}
        ],
        name: [
          {required: true, message: "服务器名称不能为空", trigger: "blur"}
        ],
        serverPath: [
          {required: true, message: "服务端所在目录不能为空", trigger: "blur"}
        ],
        startStr: [
          {required: true, message: "启动命令不能为空", trigger: "blur"}
        ],
        jvmXmx: [
          {required: true, message: "最大堆内存(XMX)不能为空", trigger: "blur"}
        ],
        jvmXms: [
          {required: true, message: "最小堆内存(XMS)不能为空", trigger: "blur"}
        ],
        coreType: [
          {required: true, message: "核心类型(如：Paper、Spigot、Bukkit等)不能为空", trigger: "change"}
        ],
        version: [
          {required: true, message: "核心版本不能为空", trigger: "blur"}
        ],
      }
    };
  },
  created() {
    // 使用路由参数中的nodeId进行筛选
    if (this.$route.query.nodeId) {
      this.queryParams.nodeId = this.$route.query.nodeId;
      this.routeNodeId = this.$route.query.nodeId;
    }
    if (this.$route.query.nodeUuid) {
      this.queryParams.nodeUuid = this.$route.query.nodeUuid;
      this.routeNodeUuid = this.$route.query.nodeUuid;
    } else if (this.$route.query.nodeId) {
      // 如果有nodeId但没有nodeUuid，调用getNodeInfo获取
      this.getNodeInfo(this.$route.query.nodeId);
    }
    this.getList();
  },
  watch: {
    // 监听路由变化
    '$route.query': {
      handler(newQuery) {
        if (newQuery.nodeId) {
          this.queryParams.nodeId = newQuery.nodeId;
          this.routeNodeId = newQuery.nodeId;
          this.queryParams.pageNum = 1;

          if (newQuery.nodeUuid) {
            this.queryParams.nodeUuid = newQuery.nodeUuid;
            this.routeNodeUuid = newQuery.nodeUuid;
          } else {
            // 如果有nodeId但没有nodeUuid，调用getNodeInfo获取
            this.getNodeInfo(newQuery.nodeId);
          }

          this.getList();
        }
      },
      deep: true
    }
  },
  methods: {
    /** 查询实例管理列表 */
    getList() {
      this.loading = true;
      listMcs(this.queryParams).then(response => {
        this.mcsList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 获取状态文本
    getStatusText(status) {
      return this.statusDict[status] || '未知';
    },
    // 获取状态标签类型
    statusTagType(status) {
      const statusMap = {
        '0': 'info',      // 未启动 - 灰色
        '1': 'success',   // 运行中 - 绿色
        '2': 'info',      // 已停止 - 灰色
        '3': 'danger'     // 异常 - 红色
      };
      return statusMap[status] || 'warning';
    },
    // 获取状态图标
    getStatusIcon(status) {
      const iconMap = {
        '0': 'el-icon-video-pause',     // 未启动
        '1': 'el-icon-video-play',      // 运行中
        '2': 'el-icon-switch-button',   // 已停止
        '3': 'el-icon-warning'          // 异常
      };
      return iconMap[status] || 'el-icon-question';
    },
    // 清空节点筛选
    clearNodeFilter() {
      // 移除路由参数
      this.$router.push({path: '/node/mcs/index'});
      // 清空查询参数
      this.queryParams.nodeId = null;
      this.queryParams.nodeUuid = null;
      this.routeNodeId = null;
      this.routeNodeUuid = null;
      this.queryParams.pageNum = 1;
      this.getList();
    },
    // 获取节点信息
    getNodeInfo(nodeId) {
      if (!nodeId) return;
      getServer(nodeId).then(response => {
        if (response.code === 200 && response.data) {
          this.queryParams.nodeUuid = response.data.uuid;
          this.routeNodeUuid = response.data.uuid;
        }
      }).catch(() => {
        // 获取失败不影响后续操作
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
        nodeId: this.routeNodeId || null,
        nodeUuid: this.routeNodeUuid || null,
        nodeInstancesId: null,
        name: null,
        serverPath: null,
        startStr: null,
        javaEnvId: null,
        javaPath: null,
        jvmXmx: null,
        jvmXms: null,
        jvmArgs: null,
        coreType: null,
        version: null,
        status: null,
        lastStartTime: null,
        lastStopTime: null,
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
    /** 加载Java环境列表 */
    loadJavaEnvList() {
      if (!this.routeNodeId) return;
      listEnv({nodeId: this.routeNodeId, valid: 1}).then(response => {
        this.javaEnvList = response.rows || [];
      }).catch(() => {
        this.javaEnvList = [];
      });
    },
    /** Java环境变更处理 */
    handleJavaEnvChange(envId) {
      if (!envId) {
        this.form.javaPath = null;
        return;
      }
      const env = this.javaEnvList.find(e => e.id === envId);
      if (env && env.binPath) {
        // 获取节点服务器信息以确定操作系统类型
        if (this.routeNodeId) {
          getServer(this.routeNodeId).then(response => {
            if (response.code === 200 && response.data) {
              const osType = (response.data.osType || '').toLowerCase();
              const isWindows = osType.includes('windows') || osType.includes('win');
              const javaExe = isWindows ? 'java.exe' : 'java';
              const separator = isWindows ? '\\' : '/';
              this.form.javaPath = `${env.binPath}${separator}${javaExe}`;
            }
          }).catch(() => {
            // 降级处理：如果获取失败，使用默认逻辑
            this.setDefaultJavaPath(env.binPath);
          });
        } else {
          // 如果没有节点ID，使用默认逻辑
          this.setDefaultJavaPath(env.binPath);
        }
      }
    },
    /** 设置默认Java路径（降级方案） */
    setDefaultJavaPath(binPath) {
      // 根据binPath中的路径分隔符判断操作系统
      const isWindows = binPath.includes('\\') || binPath.includes('C:') || binPath.includes('D:');
      const javaExe = isWindows ? 'java.exe' : 'java';
      const separator = isWindows ? '\\' : '/';
      this.form.javaPath = `${binPath}${separator}${javaExe}`;
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
      this.loadJavaEnvList();
      this.open = true;
      this.title = "添加实例管理";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      this.loadJavaEnvList();
      const id = row.id || this.ids
      getMcs(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改实例管理";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateMcs(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addMcs(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除实例管理编号为"' + ids + '"的数据项？').then(function () {
        return delMcs(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    // 打开实例控制台页面
    openTerminal(row) {
      this.$router.push({path: '/node/mcs/terminal', query: {serverId: row.id}});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('node/mcs/export', {
        ...this.queryParams
      }, `mcs_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style lang="scss" scoped>
.mcs-container {
  padding: 20px;
  background: #f0f2f5;

  .search-card {
    margin-bottom: 16px;
    border-radius: 8px;

    ::v-deep .el-card__body {
      padding: 20px;
    }

    .el-form {
      .el-form-item {
        margin-bottom: 10px;
      }
    }
  }

  .node-filter-alert {
    margin-bottom: 16px;
    border-radius: 8px;
  }

  .toolbar-card {
    margin-bottom: 16px;
    border-radius: 8px;

    ::v-deep .el-card__body {
      padding: 16px 20px;
    }

    .mb8 {
      margin-bottom: 0;
    }
  }

  .table-card {
    border-radius: 8px;

    ::v-deep .el-card__body {
      padding: 20px;
    }

    .el-table {
      border-radius: 4px;
      overflow: hidden;

      th {
        background: #f5f7fa;
        color: #606266;
        font-weight: 600;
      }

      .expand-content {
        padding: 20px 50px;
        background: #fafafa;
        border-radius: 4px;
        margin: 10px 0;

        .expand-item {
          margin-bottom: 12px;
          line-height: 1.8;
          font-size: 13px;

          &:last-child {
            margin-bottom: 0;
          }

          .expand-label {
            color: #606266;
            font-weight: 500;
            margin-right: 8px;
          }

          .expand-value {
            color: #303133;
            word-break: break-all;
          }
        }
      }
    }
  }
}

// Dialog样式优化
::v-deep .mcs-dialog {
  .el-dialog {
    border-radius: 12px;
    overflow: hidden;
  }

  .el-dialog__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 20px 24px;

    .el-dialog__title {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
    }

    .el-dialog__headerbtn {
      top: 20px;
      right: 24px;

      .el-dialog__close {
        color: #fff;
        font-size: 20px;
        font-weight: bold;

        &:hover {
          color: #f0f0f0;
        }
      }
    }
  }

  .el-dialog__body {
    padding: 24px;
    max-height: 65vh;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: #dcdfe6;
      border-radius: 3px;

      &:hover {
        background: #c0c4cc;
      }
    }
  }

  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #e4e7ed;
    background: #fafafa;

    .dialog-footer {
      text-align: right;

      .el-button {
        padding: 10px 24px;
        border-radius: 6px;
      }
    }
  }

  .form-section {
    margin-bottom: 24px;
    padding: 20px;
    background: #f9fafb;
    border-radius: 8px;
    border: 1px solid #e4e7ed;

    &:last-child {
      margin-bottom: 0;
    }

    .section-header {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: 2px solid #409EFF;
      color: #303133;
      font-size: 15px;
      font-weight: 600;

      i {
        margin-right: 8px;
        font-size: 18px;
        color: #409EFF;
      }
    }

    .el-form-item {
      margin-bottom: 18px;

      &:last-child {
        margin-bottom: 0;
      }

      ::v-deep .el-form-item__label {
        color: #606266;
        font-weight: 500;
      }

      ::v-deep .el-input__inner,
      ::v-deep .el-textarea__inner {
        border-radius: 6px;
        transition: all 0.3s;

        &:focus {
          border-color: #409EFF;
          box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
        }
      }

      ::v-deep .el-input.is-disabled .el-input__inner {
        background-color: #f5f7fa;
        color: #909399;
      }
    }
  }
}

// 按钮样式优化
.el-button {
  border-radius: 6px;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }
}

// 标签样式
.el-tag {
  border-radius: 4px;
  font-weight: 500;
}
</style>
