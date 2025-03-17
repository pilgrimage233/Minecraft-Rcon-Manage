<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="服务器ID" prop="serverId">
        <el-input
          v-model="queryParams.serverId"
          clearable
          placeholder="请输入服务器ID"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="指令" prop="command">
        <el-input
          v-model="queryParams.command"
          clearable
          placeholder="请输入指令"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="模糊匹配" prop="vagueMatching">
        <el-input
          v-model="queryParams.vagueMatching"
          clearable
          placeholder="请输入模糊匹配"
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
          v-hasPermi="['server:public:add']"
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
          v-hasPermi="['server:public:edit']"
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
          v-hasPermi="['server:public:remove']"
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
          v-hasPermi="['server:public:export']"
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

    <el-table v-loading="loading" :data="publicList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <!--      <el-table-column label="${comment}" align="center" prop="id" />-->
      <el-table-column align="center" label="服务器" prop="serverId"/>
      <el-table-column align="center" label="指令" prop="command"/>
      <el-table-column align="center" label="启用状态" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="模糊匹配" prop="vagueMatching"/>
      <el-table-column align="center" label="描述" prop="remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['server:public:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['server:public:remove']"
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

    <!-- 添加或修改公开命令对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="服务器ID">
          <el-select v-model="serverList" :disabled=editFlag multiple placeholder="请选择服务器">
            <el-option
              v-for="item in serverOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="指令" prop="command">
          <el-input v-model="form.command" placeholder="请输入指令"/>
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入描述"/>
        </el-form-item>
        <el-form-item label="模糊匹配" prop="vagueMatching">
          <el-radio-group v-model="form.status" size="small">
            <el-radio v-for="(item, index) in vagueMatchingOptions" :key="index" :disabled="item.disabled"
                      :label="item.value">{{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="启用" prop="status">
          <el-switch v-model="form.status"></el-switch>
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
import {addPublic, delPublic, getPublic, listPublic, updatePublic} from "@/api/server/public";
import {getServerList} from "@/api/mc/whitelist";

export default {
  name: "Public",
  dicts: ['sys_normal_disable'],
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
      // 公开命令表格数据
      publicList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        serverId: null,
        command: null,
        status: null,
        vagueMatching: null,
      },
      // 表单参数
      form: {status: false},
      // 表单校验
      rules: {
        serverId: [
          {required: true, message: "服务器ID不能为空", trigger: "blur"}
        ],
        command: [
          {required: true, message: "指令不能为空", trigger: "blur"}
        ],
        status: [
          {required: true, message: "启用状态不能为空", trigger: "change"}
        ],
      },
      serverList: [],
      serverOptions: [], // 服务器列表
      vagueMatchingOptions: [{
        "label": "是",
        "value": 1
      }, {
        "label": "否",
        "value": 0
      }],
      editFlag: false
    };
  },
  created() {
    this.getList();
    this.handleServerList();
  },
  methods: {
    /** 查询公开命令列表 */
    getList() {
      this.loading = true;
      listPublic(this.queryParams).then(response => {
        response.rows.forEach((item) => {
          item.serverId = this.serverOptions.filter(option => item.serverId.split(",").includes(option.value.toString())).map(option => option.label).join(",");
        });
        this.publicList = response.rows;
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
        serverId: null,
        command: null,
        status: null,
        vagueMatching: null,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        remark: null
      };
      this.serverList = [];
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
      this.editFlag = false;
      this.open = true;
      this.title = "添加公开命令";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      this.editFlag = true;
      const id = row.id || this.ids
      getPublic(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改公开命令";
        // 根据servers字段回显服务器列表
        if (this.form.serverId !== null) {
          this.serverOptions.forEach((option) => {
            if (option.value == this.form.serverId) {
              this.serverList.push(option.value);
            }
          });
        }
      });
    },
    /** 提交按钮 */
    submitForm() {
      console.log(this.serverList);
      // 服务器ID
      this.form.serverId = this.serverList.join(",");
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 启用状态转换
          this.form.status = this.form.status ? 1 : 0;
          if (this.form.id != null) {
            updatePublic(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addPublic(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除公开命令编号为"' + ids + '"的数据项？').then(function () {
        return delPublic(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('server/public/export', {
        ...this.queryParams
      }, `public_${new Date().getTime()}.xlsx`)
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
  }
};
</script>
