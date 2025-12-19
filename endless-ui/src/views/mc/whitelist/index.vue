<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="申请时间" prop="time">
        <el-date-picker v-model="queryParams.time"
                        clearable
                        placeholder="请选择申请时间"
                        type="date"
                        value-format="yyyy-MM-dd">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="游戏名称" prop="userName">
        <el-input
          v-model="queryParams.userName"
          clearable
          placeholder="请输入游戏名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="账号类型" prop="onlineFlag">
        <el-select v-model="queryParams.onlineFlag" clearable placeholder="账号类型">
          <el-option
            v-for="dict in dict.type.online_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="用户QQ" prop="qqNum">
        <el-input
          v-model="queryParams.qqNum"
          clearable
          placeholder="请输入用户QQ"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审核用户" prop="reviewUsers">
        <el-input
          v-model="queryParams.reviewUsers"
          clearable
          placeholder="请输入审核用户"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="添加状态" prop="addState">
        <el-select v-model="queryParams.addState" clearable placeholder="添加状态">
          <el-option
            v-for="dict in dict.type.white_add_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="添加时间" prop="addTime">
        <el-date-picker v-model="queryParams.addTime"
                        clearable
                        placeholder="请选择添加时间"
                        type="date"
                        value-format="yyyy-MM-dd">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker v-model="queryParams.startTime"
                        clearable
                        placeholder="请选择开始时间"
                        type="date"
                        value-format="yyyy-MM-dd">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker v-model="queryParams.endTime"
                        clearable
                        placeholder="请选择结束时间"
                        type="date"
                        value-format="yyyy-MM-dd">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="移除时间" prop="removeTime">
        <el-date-picker v-model="queryParams.removeTime"
                        clearable
                        placeholder="请选择移除时间"
                        type="date"
                        value-format="yyyy-MM-dd">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['mc:whitelist:add']"
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
          v-hasPermi="['mc:whitelist:edit']"
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
          v-hasPermi="['mc:whitelist:remove']"
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
          v-hasPermi="['mc:whitelist:export']"
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
          v-hasPermi="['mc:whitelist:import']"
          icon="el-icon-upload2"
          plain
          size="mini"
          type="info"
          @click="handleBatchApply"
        >批量申请
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="whitelistList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="用户编号" prop="id"/>
      <el-table-column align="center" label="申请时间" prop="time" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.time, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="游戏名称" prop="userName" show-overflow-tooltip/>
      <el-table-column align="center" label="UUID" prop="userUuid" show-overflow-tooltip/>
      <el-table-column align="center" label="渠道来源" prop="createBy">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.createBy && scope.row.createBy.startsWith('BOT')" type="warning">机器人</el-tag>
          <el-tag v-else-if="scope.row.createBy && scope.row.createBy.startsWith('WEB')" type="success">网站</el-tag>
          <el-tag v-else-if="scope.row.createBy && scope.row.createBy.startsWith('ADMIN')" type="danger">管理员</el-tag>
          <el-tag v-else type="info">其他</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="账号类型" prop="onlineFlag">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.online_status" :value="scope.row.onlineFlag"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="用户QQ" prop="qqNum">
        <template slot-scope="scope">
          <el-popover placement="top-start" trigger="hover" width='auto'>
            <img :src="'https://q1.qlogo.cn/g?b=qq&nk=' + scope.row.qqNum + '&s=4'"
                 style="width: 140px; height: 140px">
            <el-button slot="reference" size="small" type="text">{{ scope.row.qqNum }}</el-button>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column align="center" label="描述" prop="remark" show-overflow-tooltip/>
      <el-table-column align="center" label="审核用户" prop="reviewUsers" show-overflow-tooltip/>
      <el-table-column align="center" label="审核状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.white_examine_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="添加状态" prop="addState">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.white_add_status" :value="scope.row.addState"/>
        </template>
      </el-table-column>

      <el-table-column align="center" label="添加时间" prop="addTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.addTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="移除原因" prop="removeReason"/>
      <el-table-column align="center" label="移除时间" prop="removeTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.removeTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            :disabled="scope.row.status === '1'"
            :loading="actionLoading['agree_' + scope.row.id]"
            icon="el-icon-check"
            size="mini"
            type="text"
            @click="handleAgree(scope.row)"
          >同意
          </el-button>
          <el-button
            :disabled="scope.row.status === '2'"
            :loading="actionLoading['refuse_' + scope.row.id]"
            icon="el-icon-close"
            size="mini"
            type="text"
            @click="handleRefuse(scope.row)"
          >拒绝
          </el-button>
          <el-button
            v-hasPermi="['mc:whitelist:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['mc:whitelist:remove']"
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

    <el-dialog :visible.sync="dialogVisible" style="border: 2px solid red;" width="30%">
      <MinecraftSkin :username="selectedUserName"/>
    </el-dialog>

    <!-- 添加或修改白名单对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <div v-loading="formLoading">
        <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="medium">
        <el-form-item label="游戏名称" prop="userName">
          <el-input v-model="form.userName" :style="{width: '100%'}" clearable placeholder="请输入游戏名称">
          </el-input>
        </el-form-item>
        <el-form-item label="QQ号" prop="qqNum">
          <el-input v-model="form.qqNum" :style="{width: '100%'}" clearable placeholder="请输入QQ号">
          </el-input>
        </el-form-item>
        <el-form-item label="账号类型" prop="onlineFlag">
          <el-radio-group v-model="form.onlineFlag" size="small">
            <el-radio :label="1">正版</el-radio>
            <el-radio :label="0">离线</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.id != null" label="审核结果" prop="status">
          <el-radio-group v-model="form.status" size="small">
            <el-radio v-for="(item, index) in statusOptions" :key="index" :disabled="item.disabled"
                      :label="item.value">{{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="服务器" prop="server">
          <el-select v-model="serverList" multiple placeholder="请选择">
            <el-option
              v-for="item in serverOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            :disabled="form.id != null"
            :style="{width: '100%'}"
            placeholder="选择开始时间"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            :style="{width: '100%'}"
            placeholder="选择结束时间"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm"
          ></el-date-picker>
        </el-form-item>
        <el-form-item v-if="form.id != null" label="移除白名单" prop="addState">
          <el-switch v-model="addState"></el-switch>
        </el-form-item>
        <el-form-item v-if="addState" label="移除原因" prop="removeReason">
          <el-input v-model="form.removeReason" :style="{width: '100%'}" clearable placeholder="请输入移除原因">
          </el-input>
        </el-form-item>
        <el-form-item v-if="form.id != null" label="全局封禁" prop="banFlag">
          <el-switch v-model="form.banFlag"></el-switch>
        </el-form-item>
        <el-form-item v-if="form.banFlag" label="封禁原因" prop="bannedReason">
          <el-input v-model="form.bannedReason" :style="{width: '100%'}" clearable placeholder="请输入封禁原因">
          </el-input>
        </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button :loading="formLoading" type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 批量申请对话框 -->
    <el-dialog :visible.sync="batchApplyOpen" append-to-body title="批量申请白名单" width="500px">
      <div v-loading="batchLoading">
        <el-form ref="batchForm" :model="batchForm" label-width="100px">
        <el-form-item label="上传模板">
          <el-upload
            ref="upload"
            :action="upload.url"
            :auto-upload="false"
            :disabled="upload.isUploading"
            :headers="upload.headers"
            :limit="1"
            :on-progress="handleFileUploadProgress"
            :on-success="handleFileSuccess"
            accept=".xlsx"
            drag
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <div slot="tip" class="el-upload__tip">只能上传xlsx文件</div>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="downloadTemplate">下载模板</el-button>
        </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button :loading="batchLoading" type="primary" @click="submitBatchForm">确 定</el-button>
        <el-button @click="cancelBatch">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  addWhiteListForAdmin,
  delWhitelist,
  downloadTemplate,
  getServerList,
  getWhitelist,
  listWhitelist,
  updateWhitelist
} from "@/api/mc/whitelist";
import serverlist from "@/views/server/serverlist/index.vue";
import {getToken} from "@/utils/auth";

export default {
  name: "Whitelist",
  computed: {
    serverlist() {
      return serverlist
    }
  },
  dicts: ['white_examine_status', 'white_add_status', 'online_status'],
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
      // 白名单表格数据
      whitelistList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      addState: false,
      // 表单提交loading
      formLoading: false,
      // 批量申请loading
      batchLoading: false,
      // 操作按钮loading状态
      actionLoading: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        time: null,
        userName: null,
        userUuid: null,
        onlineFlag: null,
        qqNum: null,
        reviewUsers: null,
        status: null,
        addState: null,
        addTime: null,
        startTime: null,
        endTime: null,
        removeReason: null,
        removeTime: null
      },
      // 表单参数
      form: {addState: false, banFlag: false, onlineFlag: 0, startTime: null, endTime: null},
      // 表单校验
      rules: {
        userName: [{
          required: true,
          message: '请输入游戏名称',
          trigger: 'blur'
        }],
        qqNum: [{
          required: true,
          message: '请输入QQ号',
          trigger: 'blur'
        }],
        startTime: [{
          required: true,
          message: '请选择开始时间',
          trigger: 'change'
        }],
        removeReason: [],
        status: [],
      },
      statusOptions: [{
        "label": "通过",
        "value": 1
      }, {
        "label": "拒绝",
        "value": 2
      }],
      servers: [],
      serverOptions: [], // 服务器列表
      serverList: [],
      selectedUserName: "",
      dialogVisible: false,
      batchApplyOpen: false,
      batchForm: {},
      upload: {
        headers: {
          Authorization: 'Bearer ' + getToken()
        },
        url: '',
        isUploading: false
      }
    };
  },
  created() {
    this.getList();
    this.upload.url = process.env.VUE_APP_BASE_API + '/mc/whitelist/importTemplate';
  },
  methods: {
    /** 查询白名单列表 */
    getList() {
      this.loading = true;
      listWhitelist(this.queryParams).then(response => {
        this.whitelistList = response.rows;
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
        time: null,
        userName: null,
        userUuid: null,
        onlineFlag: 0,
        qqNum: null,
        remark: null,
        reviewUsers: null,
        status: null,
        addState: null,
        addTime: null,
        removeReason: null,
        removeTime: null,
        servers: null,
        banFlag: false,
        bannedReason: null,
        startTime: null,
        endTime: null
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
      // 获取服务器列表
      this.serverOptions = [{label: "全部", value: "all"}];
      this.serverList = [];
      this.handleServerList();
      // 设置默认值
      this.form.status = 1; // 默认通过
      // 设置默认开始时间为当前时间
      const now = new Date();
      this.form.startTime = this.parseTime(now, '{y}-{m}-{d} {h}:{i}');
      this.open = true;
      this.title = "添加白名单";
    },
    /*同意按钮操作*/
    handleAgree(row) {
      const loadingKey = 'agree_' + row.id;
      this.$set(this.actionLoading, loadingKey, true);

      row.status = '1';
      // 默认同意全部服务器
      row.servers = "all";
      if (row.id != null) {
        updateWhitelist(row).then(response => {
          this.$modal.msgSuccess("修改成功");
          this.getList();
        }).catch(() => {
          // 处理错误
        }).finally(() => {
          this.$set(this.actionLoading, loadingKey, false);
        });
      }
    },
    /*拒绝按钮操作*/
    handleRefuse(row) {
      const loadingKey = 'refuse_' + row.id;
      this.$set(this.actionLoading, loadingKey, true);

      row.status = '2';
      if (row.id != null) {
        updateWhitelist(row).then(response => {
          this.$modal.msgSuccess("修改成功");
          this.getList();
        }).catch(() => {
          // 处理错误
        }).finally(() => {
          this.$set(this.actionLoading, loadingKey, false);
        });
      }
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      this.serverOptions = [{label: "全部", value: "all"}];
      this.serverList = [];
      this.handleServerList();
      const id = row.id || this.ids
      getWhitelist(id).then(response => {
        this.form = response.data;
        this.form.banFlag = this.form.addState === '9';
        this.open = true;
        this.title = "修改白名单";
        // 开始时间在修改时如无数据则默认当前时间，且该字段在修改时锁定
        if (!this.form.startTime) {
          const now = new Date();
          this.form.startTime = this.parseTime(now, '{y}-{m}-{d} {h}:{i}');
        }
        if (this.form.addState === '2') {
          this.addState = true;
        }
        // 回显审核状态
        this.form.status = this.form.status === '1' ? 1 : 2;
        // 根据servers字段回显服务器列表
        if (this.form.servers !== null) {
          const split = this.form.servers.split(",");
          split.forEach((item) => {
            this.serverOptions.forEach((option) => {
              if (option.value == item) {
                this.serverList.push(option.value);
              }
            });
          });
        }
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.formLoading = true;
          // 白名单服务器列表
          if (this.serverList.includes("all")) {
            this.form.servers = this.serverOptions.map(item => item.value).join(",");
          } else {
            this.form.servers = this.serverList.join(",");
          }
          if (this.form.id != null) {
            // this.form.isBanned = this.form.isBanned ? 'true' : 'false';
            this.form.addState = this.addState;
            console.log("form-->", this.form)
            updateWhitelist(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            }).catch(() => {
              // 处理错误
            }).finally(() => {
              this.formLoading = false;
            });
          } else {
            // 添加二次确认
            this.$modal.confirm('确认添加该玩家到白名单吗？').then(() => {
              // 使用管理员添加接口
              addWhiteListForAdmin(this.form).then(response => {
                this.$modal.msgSuccess("新增成功");
                this.open = false;
                this.getList();
              }).catch(() => {
                // 处理错误
              }).finally(() => {
                this.formLoading = false;
              });
            }).catch(() => {
              this.formLoading = false;
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除白名单编号为"' + ids + '"的数据项？').then(function () {
        return delWhitelist(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('mc/whitelist/export', {
        ...this.queryParams
      }, `whitelist_${new Date().getTime()}.xlsx`)
    },
    /** 服务器列表 */
    handleServerList() {
      getServerList().then(response => {
        let date = response.data;
        date.forEach((item) => {
          if (item.status === 1) {
            this.serverOptions.push({label: item.nameTag, value: item.id});
          }
        });
      });
    },
    openDialog(userName) {
      this.selectedUserName = userName;
      this.dialogVisible = true;
    },
    handleBatchApply() {
      this.batchApplyOpen = true;
    },
    handleFileUploadProgress(event, file) {
      this.upload.isUploading = true;
      this.batchLoading = true;
    },
    handleFileSuccess(response, file) {
      this.upload.isUploading = false;
      this.batchLoading = false;
      this.$refs.upload.clearFiles();
      if (response.code === 200) {
        this.$modal.msgSuccess(response.msg);
      } else {
        this.$modal.msgError(response.msg || "上传失败");
      }
      this.batchApplyOpen = false;
      this.getList();
    },
    downloadTemplate() {
      downloadTemplate().then(response => {
        const blob = new Blob([response], {type: 'application/vnd.ms-excel'});
        const link = document.createElement('a');
        const fileName = '白名单批量申请模板.xlsx';
        if (window.navigator.msSaveOrOpenBlob) {
          window.navigator.msSaveOrOpenBlob(blob, fileName);
        } else {
          link.style.display = 'none';
          link.href = window.URL.createObjectURL(blob);
          link.setAttribute('download', fileName);
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
        }
      });
    },
    submitBatchForm() {
      if (this.$refs.upload.uploadFiles.length === 0) {
        this.$modal.msgWarning("请先选择文件");
        return;
      }
      this.batchLoading = true;
      this.$refs.upload.submit();
    },
    cancelBatch() {
      this.batchApplyOpen = false;
      this.resetBatchForm();
    },
    resetBatchForm() {
      this.batchForm = {};
      this.$refs.upload.clearFiles();
    }
  },
  watch: {
    open(val) {
      if (!val) {
        this.reset();
        this.addState = false;
        this.formLoading = false;
      }
    },
    dialogVisible(newVal) {
      console.log('dialogVisible changed to:', newVal);
    },
    batchApplyOpen(newVal) {
      if (!newVal) {
        this.resetBatchForm();
        this.batchLoading = false;
      }
    }
  }
};
</script>
