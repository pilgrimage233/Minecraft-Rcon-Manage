<template>
  <div class="app-container">
    <div v-if="botId" class="bot-info">
      <h3>{{ botName }}的管理员列表</h3>
    </div>
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="管理名称" prop="managerName">
        <el-input
          v-model="queryParams.managerName"
          clearable
          placeholder="请输入管理员名称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="QQ" prop="managerQq">
        <el-input
          v-model="queryParams.managerQq"
          clearable
          placeholder="请输入QQ"
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
          v-hasPermi="['bot:manager:add']"
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
          v-hasPermi="['bot:manager:edit']"
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
          v-hasPermi="['bot:manager:remove']"
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
          v-hasPermi="['bot:manager:export']"
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

    <el-table v-loading="loading" :data="managerList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="主键" prop="id"/>
      <el-table-column align="center" label="管理员名称" prop="managerName"/>
      <el-table-column align="center" label="QQ" prop="managerQq"/>
      <el-table-column align="center" label="权限类型" prop="permissionType">
        <template slot-scope="scope">
          <el-tag :type="scope.row.permissionType === 0 ? 'danger' : 'info'">
            {{ scope.row.permissionType === 0 ? '超级管理员' : '普通管理员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="最后活动时间" prop="lastActiveTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastActiveTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 0 ? 'danger' : 'success'">
            {{ scope.row.status === 0 ? '停用' : '启用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="备注" prop="remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['bot:manager:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['bot:manager:remove']"
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

    <!-- 添加或修改QQ机器人管理员对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="700px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="管理员名称" prop="managerName">
              <el-input v-model="form.managerName" placeholder="请输入管理员名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="QQ号" prop="managerQq">
              <el-input v-model="form.managerQq" placeholder="请输入QQ号"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="关联机器人" prop="botId">
              <el-select v-model="form.botId" placeholder="请选择关联机器人" style="width: 100%">
                <el-option
                  v-for="bot in botOptions"
                  :key="bot.id"
                  :label="bot.name"
                  :value="bot.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限类型" prop="permissionType">
              <el-select v-model="form.permissionType" placeholder="请选择权限类型" style="width: 100%">
                <el-option :value="0" label="超级管理员"/>
                <el-option :value="1" label="普通管理员"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="0">禁用</el-radio>
                <el-radio :label="1">启用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="center">管理群组</el-divider>

        <el-row class="group-container">
          <el-col :span="24">
            <el-form-item>
              <el-button
                icon="el-icon-plus"
                plain
                size="mini"
                style="margin-bottom: 10px"
                type="primary"
                @click="handleAddQqBotManagerGroup"
              >添加群组
              </el-button>
              <el-table
                ref="qqBotManagerGroup"
                :data="qqBotManagerGroupList"
                :row-class-name="rowQqBotManagerGroupIndex"
                border
                style="width: 100%"
                @selection-change="handleQqBotManagerGroupSelectionChange"
              >
                <el-table-column align="center" type="selection" width="55"/>
                <el-table-column align="center" label="序号" prop="index" width="50"/>
                <el-table-column label="群号" prop="groupId">
                  <template slot-scope="scope">
                    <el-select
                      v-model="scope.row.groupId"
                      filterable
                      placeholder="请选择群号"
                      style="width: 100%"
                    >
                      <el-option
                        v-for="group in groupOptions"
                        :key="group"
                        :label="group"
                        :value="group"
                      />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="状态" prop="status" width="120">
                  <template slot-scope="scope">
                    <el-select v-model="scope.row.status" placeholder="请选择状态">
                      <el-option :value="0" label="禁用"/>
                      <el-option :value="1" label="启用"/>
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column align="center" label="操作" width="100">
                  <template slot-scope="scope">
                    <el-button
                      icon="el-icon-delete"
                      style="color: #F56C6C"
                      type="text"
                      @click="removeQqBotManagerGroup(scope.$index)"
                    >删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-form-item>
          </el-col>
        </el-row>

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
import {addManager, delManager, getManager, listManager, updateManager} from "@/api/bot/manager";
import {listConfig} from "@/api/bot/config";

export default {
  name: "Manager",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 子表选中数据
      checkedQqBotManagerGroup: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // QQ机器人管理员表格数据
      managerList: [],
      // QQ机器人管理员群组关联表格数据
      qqBotManagerGroupList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        managerName: null,
        managerQq: null,
        permissionType: null,
        status: null,
        botId: null,
      },
      // 表单参数
      form: {
        id: null,
        botId: null,
        managerName: null,
        managerQq: null,
        permissionType: 1,
        lastActiveTime: null,
        status: 1,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        remark: null
      },
      botId: null,
      botName: null,
      // 表单校验
      rules: {
        botId: [
          {required: true, message: "关联的机器人ID不能为空", trigger: "change"}
        ],
        managerName: [
          {required: true, message: "管理员名称不能为空", trigger: "blur"}
        ],
        managerQq: [
          {required: true, message: "QQ号不能为空", trigger: "blur"},
          {pattern: /^\d{5,11}$/, message: "请输入正确的QQ号", trigger: "blur"}
        ],
        permissionType: [
          {required: true, message: "权限类型不能为空", trigger: "change"}
        ],
        status: [
          {required: true, message: "状态不能为空", trigger: "change"}
        ]
      },
      botOptions: [],
      groupOptions: [], // 群号选项列表
    };
  },
  created() {
    // 从路由参数中获取机器人ID和名称
    const {botId, botName} = this.$route.query;
    if (botId) {
      this.botId = parseInt(botId);
      this.botName = botName;
      this.queryParams.botId = this.botId;
      this.form.botId = this.botId;
    }
    this.getList();
  },
  methods: {
    /** 查询QQ机器人管理员列表 */
    getList() {
      this.loading = true;
      listManager(this.queryParams).then(response => {
        this.managerList = response.rows;
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
        managerName: null,
        managerQq: null,
        permissionType: 1,
        lastActiveTime: null,
        status: 1,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        remark: null
      };
      this.qqBotManagerGroupList = [];
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
      if (this.botId) {
        this.form.botId = this.botId;
        // 获取机器人列表以设置群组选项
        listConfig({pageSize: 999, status: 1}).then(response => {
          this.botOptions = response.rows;
          // 找到当前机器人并设置群组选项
          const selectedBot = this.botOptions.find(bot => bot.id === this.botId);
          if (selectedBot && selectedBot.groupIds) {
            this.groupOptions = selectedBot.groupIds.split(',').map(item => item.trim());
          }
        });
      } else {
        this.getBotList();
      }
      this.form.status = 1;
      this.form.permissionType = 1;
      this.open = true;
      this.title = "添加QQ机器人管理员";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;

      // 先获取机器人列表
      listConfig({pageSize: 999, status: 1}).then(response => {
        this.botOptions = response.rows;

        // 然后获取管理员详情
        getManager(id).then(response => {
          this.form = response.data;

          this.form.status = parseInt(this.form.status);
          this.form.permissionType = parseInt(this.form.permissionType);
          this.form.botId = parseInt(this.form.botId);

          // 根据选中的机器人更新群组选项
          const selectedBot = this.botOptions.find(bot => bot.id === this.form.botId);
          if (selectedBot && selectedBot.groupIds) {
            this.groupOptions = selectedBot.groupIds.split(',').map(item => item.trim());
          }

          // 设置群组列表数据
          if (response.data.qqBotManagerGroupList && response.data.qqBotManagerGroupList.length > 0) {
            this.qqBotManagerGroupList = response.data.qqBotManagerGroupList.map(item => ({
              ...item,
              status: parseInt(item.status), // 确保状态是数字类型
              groupId: item.groupId.toString() // 确保群号是字符串类型
            }));
          } else {
            this.qqBotManagerGroupList = [];
          }

          this.open = true;
          this.title = "修改QQ机器人管理员";
        });
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.qqBotManagerGroupList.length === 0) {
            this.$modal.msgError("请至少添加一个管理群组");
            return;
          }

          const invalidGroup = this.qqBotManagerGroupList.some(item =>
            !item.groupId || item.status === '');
          if (invalidGroup) {
            this.$modal.msgError("请完善群组信息");
            return;
          }

          this.form.qqBotManagerGroupList = this.qqBotManagerGroupList;
          if (this.form.id != null) {
            updateManager(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addManager(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除QQ机器人管理员编号为"' + ids + '"的数据项？').then(function () {
        return delManager(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** QQ机器人管理员群组关联序号 */
    rowQqBotManagerGroupIndex({row, rowIndex}) {
      row.index = rowIndex + 1;
    },
    /** QQ机器人管理员群组关联添加按钮操作 */
    handleAddQqBotManagerGroup() {
      let obj = {
        groupId: "",
        status: 1, // 默认启用
        remark: ""
      };
      this.qqBotManagerGroupList.push(obj);
    },
    /** QQ机器人管理员群组关联删除按钮操作 */
    handleDeleteQqBotManagerGroup() {
      if (this.checkedQqBotManagerGroup.length == 0) {
        this.$modal.msgError("请先选择要删除的QQ机器人管理员群组关联数据");
      } else {
        const qqBotManagerGroupList = this.qqBotManagerGroupList;
        const checkedQqBotManagerGroup = this.checkedQqBotManagerGroup;
        this.qqBotManagerGroupList = qqBotManagerGroupList.filter(function (item) {
          return checkedQqBotManagerGroup.indexOf(item.index) == -1
        });
      }
    },
    /** 复选框选中数据 */
    handleQqBotManagerGroupSelectionChange(selection) {
      this.checkedQqBotManagerGroup = selection.map(item => item.index)
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('bot/manager/export', {
        ...this.queryParams
      }, `manager_${new Date().getTime()}.xlsx`)
    },
    /** 获取机器人列表 */
    getBotList() {
      listConfig({pageSize: 999, status: 1}).then(response => {
        this.botOptions = response.rows;
      });
    },
    /** 删除单个群组 */
    removeQqBotManagerGroup(index) {
      this.qqBotManagerGroupList.splice(index, 1);
    },
  },
  watch: {
    'form.botId': {
      handler(newVal) {
        if (newVal) {
          // 找到选中的机器人配置
          const selectedBot = this.botOptions.find(bot => bot.id === newVal);
          if (selectedBot && selectedBot.groupIds) {
            // 将群号字符串转换为数组
            this.groupOptions = selectedBot.groupIds.split(',').map(item => item.trim());

            // 如果是修改操作，保留已有的群组数据
            if (!this.form.id) {
              // 只有在新增时才清空群组列表
              this.qqBotManagerGroupList = [];
            } else {
              // 在修改时，过滤掉不在新群组列表中的群组
              this.qqBotManagerGroupList = this.qqBotManagerGroupList.filter(item =>
                this.groupOptions.includes(item.groupId.toString())
              );
            }
          } else {
            this.groupOptions = [];
            this.qqBotManagerGroupList = [];
          }
        } else {
          this.groupOptions = [];
          this.qqBotManagerGroupList = [];
        }
      },
      immediate: true
    },
    '$route.query.botId': {
      handler(newVal) {
        if (newVal) {
          this.botId = parseInt(newVal);
          this.botName = this.$route.query.botName;
          this.queryParams.botId = this.botId;
          this.getList();
        } else {
          this.botId = null;
          this.botName = null;
          this.queryParams.botId = null;
          this.getList();
        }
      },
      immediate: true
    },
    '$route.query.botName': {
      handler(newVal) {
        this.botName = newVal;
      },
      immediate: true
    }
  }
};
</script>

<style scoped>
.group-container {
  margin-top: 10px;
}

.bot-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #409EFF;
}

.bot-info h3 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 500;
}

.el-table ::v-deep .el-table__header th {
  background-color: #f8f8f9 !important;
  color: #606266;
  font-weight: bold;
  height: 40px;
}

.el-table ::v-deep .el-table__row:hover > td {
  background-color: #f5f7fa !important;
}

.el-select ::v-deep .el-input__inner {
  border-radius: 4px;
}

/* 表格中的下拉选择框样式调整 */
.el-table ::v-deep .el-select .el-input__inner {
  border: none;
  background: transparent;
}

.el-table ::v-deep .el-select:hover .el-input__inner {
  background-color: #f5f7fa;
}
</style>
