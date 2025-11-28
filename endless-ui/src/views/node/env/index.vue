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
import {addEnv, delEnv, getEnv, listEnv, scanEnv, updateEnv, verifyEnv} from "@/api/node/env";
import {listServer} from "@/api/node/server";

export default {
  name: "Env",
  data() {
    return {
      // 验证加载状态
      verifying: false,
      // 扫描加载状态
      scanning: false,
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
    }
  }
};
</script>
