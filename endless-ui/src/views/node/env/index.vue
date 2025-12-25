<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="80px" size="small">
      <el-form-item label="节点服务器" prop="nodeId">
        <el-select v-model="queryParams.nodeId" clearable placeholder="请选择节点服务器" style="width: 200px">
          <el-option
            v-for="server in nodeServerList"
            :key="server.id"
            :label="server.serverName"
            :value="server.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Java版本" prop="version">
        <el-input
          v-model="queryParams.version"
          clearable
          placeholder="请输入Java版本"
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="环境名称" prop="envName">
        <el-input
          v-model="queryParams.envName"
          clearable
          placeholder="请输入环境名称"
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="架构" prop="arch">
        <el-select v-model="queryParams.arch" clearable placeholder="请选择架构" style="width: 200px">
          <el-option label="x86" value="x86"/>
          <el-option label="x64" value="x64"/>
          <el-option label="arm64" value="arm64"/>
        </el-select>
      </el-form-item>
      <el-form-item label="默认版本" prop="isDefault">
        <el-select v-model="queryParams.isDefault" clearable placeholder="请选择" style="width: 200px">
          <el-option :value="1" label="是"/>
          <el-option :value="0" label="否"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="valid">
        <el-select v-model="queryParams.valid" clearable placeholder="请选择状态" style="width: 200px">
          <el-option :value="1" label="有效"/>
          <el-option :value="0" label="无效"/>
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
          v-hasPermi="['node:env:add']"
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
          v-hasPermi="['node:env:add']"
          :loading="scanning"
          icon="el-icon-search"
          plain
          size="mini"
          type="info"
          @click="handleScanEnvironments"
        >扫描环境
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['node:env:add']"
          icon="el-icon-download"
          plain
          size="mini"
          type="success"
          @click="handleInstallJava"
        >一键安装
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['node:env:edit']"
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
          v-hasPermi="['node:env:remove']"
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
          v-hasPermi="['node:env:export']"
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

    <el-table v-loading="loading" :data="envList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="ID" prop="id" width="80"/>
      <el-table-column align="center" label="节点服务器" prop="serverName" width="150">
        <template slot-scope="scope">
          <el-tag size="small" type="info">
            {{ scope.row.serverName || scope.row.nodeId }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="环境名称" prop="envName" show-overflow-tooltip width="150"/>
      <el-table-column align="center" label="Java版本" prop="version" width="120"/>
      <el-table-column align="center" label="架构" prop="arch" width="80"/>
      <el-table-column align="center" label="安装类型" prop="type" width="100"/>
      <el-table-column align="center" label="JAVA_HOME" min-width="200" prop="javaHome" show-overflow-tooltip/>
      <el-table-column align="center" label="默认版本" prop="isDefault" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isDefault === 1 ? 'success' : 'info'" size="small">
            {{ scope.row.isDefault === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="valid" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.valid === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.valid === 1 ? '有效' : '无效' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="来源" prop="source" width="120"/>
      <el-table-column align="center" label="备注" min-width="150" prop="remark" show-overflow-tooltip/>
      <el-table-column align="center" class-name="small-padding fixed-width" fixed="right" label="操作" width="150">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['node:env:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['node:env:remove']"
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

    <!-- 一键安装Java对话框 -->
    <el-dialog :before-close="handleDialogClose" :close-on-click-modal="false" :visible.sync="installDialogVisible"
               append-to-body
               title="一键安装Java环境" width="600px">
      <el-form ref="installForm" :model="installForm" :rules="installRules" label-width="120px">
        <el-form-item label="节点服务器" prop="nodeId">
          <el-select v-model="installForm.nodeId" :disabled="installing" placeholder="请选择节点服务器"
                     style="width: 100%">
            <el-option
              v-for="server in nodeServerList"
              :key="server.id"
              :label="server.serverName"
              :value="server.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Java版本" prop="version">
          <el-select v-model="installForm.version" :disabled="installing" placeholder="请选择Java版本"
                     style="width: 100%">
            <el-option
              :disabled="!isVersionSupported('8')"
              label="Java 8"
              value="8"
            />
            <el-option
              :disabled="!isVersionSupported('11')"
              label="Java 11"
              value="11"
            />
            <el-option
              :disabled="!isVersionSupported('17')"
              label="Java 17 (推荐)"
              value="17"
            />
            <el-option
              :disabled="!isVersionSupported('21')"
              label="Java 21"
              value="21"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="安装路径" prop="installPath">
          <el-input v-model="installForm.installPath" :disabled="installing"
                    placeholder="例如: f:\Endless\Endless-Node\java"/>
          <span style="color: #909399; font-size: 12px;">
            将在此路径下创建Java安装目录
          </span>
        </el-form-item>
        <el-form-item label="供应商" prop="vendor">
          <el-select v-model="installForm.vendor" :disabled="installing" placeholder="请选择供应商" style="width: 100%"
                     @change="handleVendorChange">
            <el-option label="Azul Zulu (推荐)" value="Zulu">
              <span>Azul Zulu</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 10px;">企业级支持，Lunar 官方JDK </span>
            </el-option>
            <el-option label="Adoptium" value="Adoptium">
              <span>Adoptium</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 10px;">开源稳定，支持所有版本</span>
            </el-option>
            <el-option label="Amazon Corretto" value="Corretto">
              <span>Amazon Corretto</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 10px;">AWS优化，生产就绪</span>
            </el-option>
            <el-option label="Microsoft OpenJDK" value="Microsoft">
              <span>Microsoft OpenJDK</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 10px;">仅支持 11/17/21</span>
            </el-option>
            <el-option label="Oracle GraalVM" value="GraalVM">
              <span>Oracle GraalVM</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 10px;">高性能，仅支持 17/21</span>
            </el-option>
          </el-select>
          <span style="color: #909399; font-size: 12px; display: block; margin-top: 5px;">
            {{ getVendorTip() }}
          </span>
        </el-form-item>

        <!-- 安装进度显示 -->
        <div v-if="installing" style="margin-top: 20px;">
          <el-progress :percentage="installProgress" :status="installStatus"></el-progress>
          <div ref="installLogsContainer" class="install-logs-container"
               style="margin-top: 10px; padding: 10px; background: #f5f7fa; border-radius: 4px; max-height: 200px; overflow-y: auto;">
            <div v-for="(log, index) in installLogs" :key="index"
                 style="font-size: 12px; line-height: 1.8; color: #606266; margin-bottom: 4px;">
              <i :class="getLogIcon(log.type)" :style="{color: getLogColor(log.type)}"></i>
              {{ log.message }}
            </div>
          </div>
        </div>

        <el-alert
          v-if="!installing"
          :closable="false"
          style="margin-top: 15px"
          title="提示：安装过程可能需要几分钟，请耐心等待。系统会自动下载并解压Java环境。"
          type="info"
        />
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button v-if="!installing && installStatus !== 'success'" @click="cancelInstall">取 消</el-button>
        <el-button v-if="installStatus === 'success'" type="success" @click="handleInstallComplete">完成</el-button>
        <el-button v-if="installStatus !== 'success'" :disabled="installing" :loading="installing" type="primary"
                   @click="submitInstall">
          {{ installing ? '安装中...' : '开始安装' }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 添加或修改节点Java多版本环境管理对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="700px">
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="节点服务器" prop="nodeId">
              <el-select v-model="form.nodeId" placeholder="请选择节点服务器" style="width: 100%">
                <el-option
                  v-for="server in nodeServerList"
                  :key="server.id"
                  :label="server.serverName"
                  :value="server.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Java版本" prop="version">
              <el-input v-model="form.version" placeholder="例如: 17.0.2"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="环境名称" prop="envName">
              <el-input v-model="form.envName" placeholder="自定义环境名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="架构" prop="arch">
              <el-select v-model="form.arch" placeholder="请选择架构" style="width: 100%">
                <el-option label="x86" value="x86"/>
                <el-option label="x64" value="x64"/>
                <el-option label="arm64" value="arm64"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="安装类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择安装类型" style="width: 100%">
                <el-option label="JDK" value="JDK"/>
                <el-option label="JRE" value="JRE"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源" prop="source">
              <el-input v-model="form.source" placeholder="例如: Oracle, OpenJDK"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Java安装根路径" prop="path">
          <el-input v-model="form.path" placeholder="例如: /usr/lib/jvm/java-17">
            <el-button slot="append" :loading="verifying" icon="el-icon-search" @click="handleVerifyPath">验证
            </el-button>
          </el-input>
        </el-form-item>
        <el-form-item label="JAVA_HOME路径" prop="javaHome">
          <el-input v-model="form.javaHome" placeholder="例如: /usr/lib/jvm/java-17"/>
        </el-form-item>
        <el-form-item label="bin目录路径" prop="binPath">
          <el-input v-model="form.binPath" placeholder="例如: /usr/lib/jvm/java-17/bin"/>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="默认版本" prop="isDefault">
              <el-radio-group v-model="form.isDefault">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路径是否有效" prop="valid">
              <el-radio-group v-model="form.valid">
                <el-radio :label="1">有效</el-radio>
                <el-radio :label="0">无效</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" :rows="3" placeholder="请输入备注信息" type="textarea"/>
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
import {addEnv, cancelInstall, delEnv, getEnv, listEnv, scanEnv, updateEnv, verifyEnv} from "@/api/node/env";
import {listServer} from "@/api/node/server";

export default {
  name: "Env",
  data() {
    return {
      // 验证加载状态
      verifying: false,
      // 扫描加载状态
      scanning: false,
      // 安装加载状态
      installing: false,
      installProgress: 0,
      installStatus: '',
      installLogs: [],
      installEventSource: null,
      installAbortController: null,
      installTaskId: null,
      // 安装对话框
      installDialogVisible: false,
      installForm: {
        nodeId: null,
        version: '17',
        installPath: '',
        vendor: 'Zulu'
      },
      installRules: {
        nodeId: [
          {required: true, message: "请选择节点服务器", trigger: "change"}
        ],
        version: [
          {required: true, message: "请选择Java版本", trigger: "change"}
        ],
        installPath: [
          {required: true, message: "请输入安装路径", trigger: "blur"}
        ],
        vendor: [
          {required: true, message: "请选择供应商", trigger: "change"}
        ]
      },
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
      // 节点Java多版本环境管理表格数据
      envList: [],
      // 节点服务器列表
      nodeServerList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        nodeId: null,
        version: null,
        envName: null,
        path: null,
        javaHome: null,
        binPath: null,
        type: null,
        arch: null,
        isDefault: null,
        valid: null,
        source: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        nodeId: [
          {required: true, message: "节点ID不能为空", trigger: "change"}
        ],
        version: [
          {required: true, message: "Java版本号不能为空", trigger: "blur"}
        ],
        path: [
          {required: true, message: "Java安装根路径不能为空", trigger: "blur"}
        ],
        type: [
          {required: true, message: "安装类型不能为空", trigger: "change"}
        ],
        isDefault: [
          {required: true, message: "默认版本不能为空", trigger: "change"}
        ],
        valid: [
          {required: true, message: "路径是否有效不能为空", trigger: "change"}
        ],
      }
    };
  },
  created() {
    // 从路由参数获取节点ID
    const nodeId = this.$route.query.nodeId;
    if (nodeId) {
      this.queryParams.nodeId = Number(nodeId);
    }
    this.getList();
    this.getNodeServerList();
  },
  methods: {
    /** 查询节点服务器列表 */
    getNodeServerList() {
      listServer({}).then(response => {
        this.nodeServerList = response.rows || [];
      });
    },
    /** 查询节点Java多版本环境管理列表 */
    getList() {
      this.loading = true;
      listEnv(this.queryParams).then(response => {
        this.envList = response.rows;
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
        nodeId: null,
        version: null,
        envName: null,
        path: null,
        javaHome: null,
        binPath: null,
        type: null,
        arch: null,
        isDefault: null,
        valid: null,
        source: null,
        status: null,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        remark: null
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
      // 确保节点列表已加载
      if (this.nodeServerList.length === 0) {
        this.getNodeServerList();
      }
      this.open = true;
      this.title = "添加节点Java多版本环境管理";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getEnv(id).then(response => {
        this.form = response.data;
        // 确保nodeId是数字类型，以匹配下拉选项的value
        if (this.form.nodeId) {
          this.form.nodeId = Number(this.form.nodeId);
        }
        this.open = true;
        this.title = "修改节点Java多版本环境管理";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateEnv(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addEnv(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除节点Java多版本环境管理编号为"' + ids + '"的数据项？').then(function () {
        return delEnv(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('node/env/export', {
        ...this.queryParams
      }, `env_${new Date().getTime()}.xlsx`)
    },
    /** 根据节点ID获取节点名称 */
    getNodeServerName(nodeId) {
      const server = this.nodeServerList.find(s => s.id === nodeId);
      return server ? server.serverName : nodeId;
    },
    /** 验证Java路径 */
    handleVerifyPath() {
      if (!this.form.nodeId) {
        this.$message.warning('请先选择节点服务器');
        return;
      }
      if (!this.form.path || this.form.path.trim() === '') {
        this.$message.warning('请输入Java安装根路径');
        return;
      }

      this.verifying = true;
      verifyEnv({
        nodeId: this.form.nodeId,
        path: this.form.path
      }).then(response => {
        if (response.code === 200 && response.data) {
          const data = response.data;
          if (data.valid) {
            this.$message.success('验证成功！');
            // 自动填充信息
            if (!this.form.version) {
              this.form.version = data.version;
            }
            if (!this.form.javaHome) {
              this.form.javaHome = data.javaHome;
            }
            if (!this.form.binPath) {
              this.form.binPath = data.binPath;
            }
            if (!this.form.arch) {
              this.form.arch = data.arch;
            }
            if (!this.form.source) {
              this.form.source = data.vendor;
            }
            if (!this.form.envName) {
              this.form.envName = `Java ${data.version} (${data.vendor})`;
            }
            this.form.valid = 1;
          } else {
            this.$message.error('验证失败: ' + (data.error || '未知错误'));
            this.form.valid = 0;
          }
        }
      }).catch(() => {
        this.$message.error('验证失败');
      }).finally(() => {
        this.verifying = false;
      });
    },
    /** 扫描Java环境 */
    handleScanEnvironments() {
      // 检查是否选择了节点
      const nodeId = this.queryParams.nodeId;
      if (!nodeId) {
        this.$message.warning('请先在搜索条件中选择节点服务器');
        return;
      }

      this.scanning = true;
      scanEnv(nodeId).then(response => {
        if (response.code === 200 && response.data) {
          const environments = response.data.environments || [];
          if (environments.length === 0) {
            this.$message.info('未扫描到Java环境');
            return;
          }

          // 显示扫描结果对话框
          this.$confirm(`扫描到 ${environments.length} 个Java环境，是否批量导入？`, '扫描结果', {
            confirmButtonText: '导入',
            cancelButtonText: '取消',
            type: 'info'
          }).then(() => {
            this.importScannedEnvironments(nodeId, environments);
          });
        }
      }).catch(() => {
        this.$message.error('扫描失败');
      }).finally(() => {
        this.scanning = false;
      });
    },
    /** 导入扫描到的环境 */
    importScannedEnvironments(nodeId, environments) {
      // 先检查已存在的环境，避免重复导入
      const existingVersions = this.envList
        .filter(env => env.nodeId === nodeId)
        .map(env => env.version);

      // 过滤掉已存在的版本
      const newEnvironments = environments.filter(env => {
        return !existingVersions.includes(env.version);
      });

      if (newEnvironments.length === 0) {
        this.$message.warning('所有扫描到的Java环境都已存在，无需导入');
        return;
      }

      // 如果有部分已存在，提示用户
      const skippedCount = environments.length - newEnvironments.length;
      if (skippedCount > 0) {
        this.$message.info(`跳过 ${skippedCount} 个已存在的环境，准备导入 ${newEnvironments.length} 个新环境`);
      }

      // 导入新环境
      let successCount = 0;
      let failCount = 0;
      const failedVersions = [];

      const promises = newEnvironments.map(env => {
        const nodeEnv = {
          nodeId: nodeId,
          version: env.version,
          envName: `Java ${env.version} (${env.vendor})`,
          path: env.javaHome,
          javaHome: env.javaHome,
          binPath: env.binPath,
          type: env.type,
          arch: env.arch,
          isDefault: 0,
          valid: 1,
          source: env.vendor
        };
        return addEnv(nodeEnv)
          .then(() => {
            successCount++;
          })
          .catch(error => {
            failCount++;
            failedVersions.push(env.version);
            console.error(`导入Java ${env.version}失败:`, error);
          });
      });

      Promise.all(promises).then(() => {
        if (failCount === 0) {
          this.$message.success(`成功导入 ${successCount} 个Java环境`);
        } else if (successCount > 0) {
          this.$message.warning(`成功导入 ${successCount} 个，失败 ${failCount} 个环境（版本: ${failedVersions.join(', ')}）`);
        } else {
          this.$message.error(`导入失败，所有环境都无法导入`);
        }
        this.getList();
      });
    },
    /** 一键安装Java */
    handleInstallJava() {
      this.installForm = {
        nodeId: this.queryParams.nodeId || null,
        version: '17',
        installPath: '',
        vendor: 'Zulu'
      };
      this.installing = false;
      this.installProgress = 0;
      this.installStatus = '';
      this.installLogs = [];
      this.installDialogVisible = true;
      this.$nextTick(() => {
        this.$refs['installForm'].clearValidate();
      });
    },
    /** 提交安装 */
    submitInstall() {
      if (this.installing) return;

      this.$refs['installForm'].validate(valid => {
        if (valid) {
          this.installing = true;
          this.installProgress = 0;
          this.installStatus = '';
          this.installLogs = [];

          this.startInstallWithSSE();
        }
      });
    },

    /** 使用SSE开始安装 */
    startInstallWithSSE() {
      // 创建新的 AbortController
      this.installAbortController = new AbortController();

      // 使用主控端的代理接口
      const url = process.env.VUE_APP_BASE_API + '/node/env/install';

      fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + this.$store.getters.token
        },
        body: JSON.stringify({
          nodeId: this.installForm.nodeId,
          version: this.installForm.version,
          installPath: this.installForm.installPath,
          vendor: this.installForm.vendor
        }),
        signal: this.installAbortController.signal
      }).then(response => {
        if (!response.ok) {
          throw new Error('请求失败');
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = ''; // 用于存储不完整的数据

        const readStream = () => {
          reader.read().then(({done, value}) => {
            if (done) {
              // 处理剩余的buffer
              if (buffer.trim()) {
                this.processSSELine(buffer);
              }
              this.installing = false;
              return;
            }

            // 将新数据追加到buffer
            buffer += decoder.decode(value, {stream: true});

            // 按行分割
            const lines = buffer.split('\n');

            // 保留最后一个可能不完整的行
            buffer = lines.pop() || '';

            // 处理完整的行
            lines.forEach(line => {
              this.processSSELine(line);
            });

            readStream();
          }).catch(error => {
            console.error('读取流失败:', error);
            this.installing = false;
            this.$message.error('安装失败: ' + error.message);
          });
        };

        readStream();
      }).catch(error => {
        // 如果是用户主动取消，不显示错误消息
        if (error.name === 'AbortError') {
          console.log('安装已被用户取消');
          this.installLogs.push({
            type: 'warning',
            message: '安装已取消',
            time: new Date().toLocaleTimeString()
          });
        } else {
          console.error('安装Java失败:', error);
          this.$message.error('安装失败: ' + error.message);
        }
        this.installing = false;
      });
    },

    /** 处理单行SSE数据 */
    processSSELine(line) {
      line = line.trim();
      if (line.startsWith('data:')) {
        try {
          const jsonStr = line.substring(5).trim();
          if (jsonStr) {
            const data = JSON.parse(jsonStr);
            this.handleInstallProgress(data);
          }
        } catch (e) {
          console.error('解析SSE数据失败:', line, e);
        }
      }
    },

    /** 处理安装进度 */
    handleInstallProgress(data) {
      // 保存任务ID
      if (data.type === 'task_created' && data.taskId) {
        this.installTaskId = data.taskId;
        console.log('任务ID:', this.installTaskId);
      }

      if (data.progress !== undefined) {
        this.installProgress = Math.min(100, Math.max(0, data.progress));
      }

      if (data.message) {
        this.installLogs.push({
          type: data.type || 'info',
          message: data.message,
          time: new Date().toLocaleTimeString()
        });

        // 自动滚动到底部
        this.$nextTick(() => {
          const logContainer = this.$refs.installLogsContainer;
          if (logContainer) {
            logContainer.scrollTop = logContainer.scrollHeight;
          }
        });
      }

      if (data.success === true || data.type === 'success') {
        this.installStatus = 'success';
        this.installProgress = 100;
        this.installing = false;

        // 显示成功消息框
        this.$alert(
          `Java ${data.version || ''} 安装成功！环境已自动添加到列表中。`,
          '安装完成',
          {
            confirmButtonText: '确定',
            type: 'success',
            callback: () => {
              this.installDialogVisible = false;
              this.getList();
            }
          }
        );
      } else if (data.type === 'error') {
        this.installStatus = 'exception';
        this.installing = false;
        this.$message.error(data.message || '安装失败');
      }
    },

    /** 取消安装 */
    cancelInstall() {
      if (this.installing) {
        this.$confirm('安装正在进行中，确定要取消吗？这将停止下载并清理临时文件。', '提示', {
          confirmButtonText: '确定取消',
          cancelButtonText: '继续安装',
          type: 'warning'
        }).then(() => {
          // 调用后端取消接口
          this.cancelInstallTask();
        }).catch(() => {
          // 用户选择继续安装
        });
        return;
      }
      this.installDialogVisible = false;
      this.installLogs = [];
      this.installProgress = 0;
      this.installStatus = '';
      this.installTaskId = null;
    },

    /** 调用后端取消安装任务 */
    cancelInstallTask() {
      if (!this.installTaskId) {
        this.$message.warning('任务ID不存在，无法取消');
        // 仍然中止前端连接
        if (this.installAbortController) {
          this.installAbortController.abort();
          this.installAbortController = null;
        }
        this.installing = false;
        this.installDialogVisible = false;
        return;
      }

      // 调用取消接口
      cancelInstall({
        nodeId: this.installForm.nodeId,
        taskId: this.installTaskId
      }).then(response => {
        this.$message.success('已取消安装');
      }).catch(error => {
        console.error('取消安装失败:', error);
        this.$message.error('取消失败');
      }).finally(() => {
        // 中止前端连接
        if (this.installAbortController) {
          this.installAbortController.abort();
          this.installAbortController = null;
        }
        this.installing = false;
        this.installStatus = 'exception';
        this.installDialogVisible = false;
        this.installLogs = [];
        this.installProgress = 0;
        this.installTaskId = null;
      });
    },

    /** 处理安装完成 */
    handleInstallComplete() {
      this.installDialogVisible = false;
      this.installLogs = [];
      this.installProgress = 0;
      this.installStatus = '';
      this.getList();
    },

    /** 处理对话框关闭 */
    handleDialogClose(done) {
      if (this.installing) {
        this.$confirm('安装正在进行中，确定要关闭吗？关闭后安装将被取消。', '提示', {
          confirmButtonText: '确定关闭',
          cancelButtonText: '继续安装',
          type: 'warning'
        }).then(() => {
          // 调用取消接口
          if (this.installTaskId) {
            cancelInstall({
              nodeId: this.installForm.nodeId,
              taskId: this.installTaskId
            }).catch(error => {
              console.error('取消安装失败:', error);
            });
          }

          // 中止前端连接
          if (this.installAbortController) {
            this.installAbortController.abort();
            this.installAbortController = null;
          }
          this.installing = false;
          this.installStatus = '';
          this.installLogs = [];
          this.installProgress = 0;
          this.installTaskId = null;
          done();
        }).catch(() => {
          // 用户选择继续安装
        });
      } else {
        done();
      }
    },

    /** 获取日志图标 */
    getLogIcon(type) {
      const icons = {
        'info': 'el-icon-info',
        'success': 'el-icon-success',
        'error': 'el-icon-error',
        'warning': 'el-icon-warning'
      };
      return icons[type] || 'el-icon-info';
    },

    /** 获取日志颜色 */
    getLogColor(type) {
      const colors = {
        'info': '#409EFF',
        'success': '#67C23A',
        'error': '#F56C6C',
        'warning': '#E6A23C'
      };
      return colors[type] || '#909399';
    },

    /** 判断版本是否被当前供应商支持 */
    isVersionSupported(version) {
      const vendor = this.installForm.vendor;

      // 定义每个供应商支持的版本
      const supportedVersions = {
        'Adoptium': ['8', '11', '17', '21'],
        'Corretto': ['8', '11', '17', '21'],
        'Zulu': ['8', '11', '17', '21'],
        'Microsoft': ['11', '17', '21'],
        'GraalVM': ['17', '21']
      };

      const versions = supportedVersions[vendor] || ['8', '11', '17', '21'];
      return versions.includes(version);
    },

    /** 处理供应商变化 */
    handleVendorChange(vendor) {
      // 检查当前选择的版本是否被新供应商支持
      const currentVersion = this.installForm.version;

      if (!this.isVersionSupported(currentVersion)) {
        // 如果不支持，自动选择该供应商支持的推荐版本
        const recommendedVersions = {
          'Microsoft': '17',
          'GraalVM': '17'
        };

        const newVersion = recommendedVersions[vendor] || '17';
        this.installForm.version = newVersion;

        this.$message.info(`${vendor} 不支持 Java ${currentVersion}，已自动切换到 Java ${newVersion}`);
      }
    },

    /** 获取供应商提示 */
    getVendorTip() {
      const vendor = this.installForm.vendor;
      const tips = {
        'Adoptium': '开源且稳定，适合大多数场景，支持 Java 8/11/17/21',
        'Corretto': 'AWS 优化版本，适合云环境和生产部署，支持 Java 8/11/17/21',
        'Zulu': '企业级 OpenJDK，提供长期支持，支持 Java 8/11/17/21',
        'Microsoft': '微软构建的 OpenJDK，适合 Azure 环境，支持 Java 11/17/21',
        'GraalVM': '高性能 JDK，支持多语言和 AOT 编译，支持 Java 17/21'
      };
      return tips[vendor] || '请选择供应商';
    }
  }
};
</script>
