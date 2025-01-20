<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="玩家昵称" prop="userName">
        <el-input
          v-model="queryParams.userName"
          clearable
          placeholder="请输入玩家昵称"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="QQ号" prop="qq">
        <el-input
          v-model="queryParams.qq"
          clearable
          placeholder="请输入QQ号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="身份" prop="identity">
        <el-input
          v-model="queryParams.identity"
          clearable
          placeholder="请输入身份"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="省份" prop="province">
        <el-input
          v-model="queryParams.province"
          clearable
          placeholder="请输入省份"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="地市" prop="city">
        <el-input
          v-model="queryParams.city"
          clearable
          placeholder="请输入地市"
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
          v-hasPermi="['player:details:add']"
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
          v-hasPermi="['player:details:edit']"
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
          v-hasPermi="['player:details:remove']"
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
          v-hasPermi="['player:details:export']"
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

    <el-table v-loading="loading" :data="detailsList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="ID" prop="id"/>
      <el-table-column align="center" label="玩家昵称" prop="userName"/>
      <el-table-column align="center" label="QQ号" prop="qq"/>
      <el-table-column align="center" label="身份" prop="identity">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.player_identity" :value="scope.row.identity"/>
        </template>
      </el-table-column>
      <el-table-column align="center" label="上线时间" prop="lastOnlineTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastOnlineTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="离线时间" prop="lastOfflineTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastOfflineTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="游戏时长" prop="gameTime">
        <template slot-scope="scope">
          <span>{{ formatGameTime(scope.row.gameTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="省份" prop="province"/>
      <el-table-column align="center" label="地市" prop="city"/>
      <el-table-column align="center" label="备注" prop="remark"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['player:details:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
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

    <!-- 添加或修改玩家详情对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="玩家昵称" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入玩家昵称"/>
        </el-form-item>
        <el-form-item label="QQ号" prop="qq">
          <el-input v-model="form.qq" placeholder="请输入QQ号"/>
        </el-form-item>
        <el-form-item label="身份" prop="identity">
          <el-radio-group v-model="form.identity" size="small">
            <el-radio v-for="(item, index) in identityOptions" :key="index" :disabled="item.disabled"
                      :label="item.value">{{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="form.province" placeholder="请输入省份"/>
        </el-form-item>
        <el-form-item label="地市" prop="city">
          <el-input v-model="form.city" placeholder="请输入地市"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注"/>
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
import {addDetails, delDetails, getDetails, listDetails, updateDetails} from "@/api/player/details";

export default {
  name: "Details",
  dicts: ['player_identity'],
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
      // 玩家详情表格数据
      detailsList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: null,
        qq: null,
        identity: null,
        lastTime: null,
        province: null,
        city: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        userName: [
          {
            required: true, message: "玩家昵称不能为空", trigger: "blur"
          }
        ],
        identity: [
          {
            required: true, message: "身份不能为空", trigger: "blur"
          }
        ],
        createTime: [
          {
            required: true, message: "创建时间不能为空", trigger: "blur"
          }
        ],
        createBy: [
          {
            required: true, message: "创建者不能为空", trigger: "blur"
          }
        ],
      },
      identityOptions: [{
        "label": "玩家",
        "value": "player"
      }, {
        "label": "管理员",
        "value": "operator"
      }],
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 格式化游戏时间 */
    formatGameTime(minutes) {
      if (!minutes) return '-';
      if (minutes < 10) { // 小于10分钟显示分钟
        return `${minutes}分钟`;
      }
      const hours = (minutes / 60).toFixed(1);
      return `${hours}小时`;
    },
    /** 查询玩家详情列表 */
    getList() {
      this.loading = true;
      listDetails(this.queryParams).then(response => {
        this.detailsList = response.rows;
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
        userName: null,
        qq: null,
        identity: null,
        lastOnlineTime: null,
        lastOfflineTime: null,
        gameTime: null,
        province: null,
        city: null,
        whitelistId: null,
        banlistId: null,
        parameters: null,
        createTime: null,
        updateTime: null,
        createBy: null,
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
      this.open = true;
      this.title = "添加玩家详情";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getDetails(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改玩家详情";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateDetails(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addDetails(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除玩家详情编号为"' + ids + '"的数据项？').then(function () {
        return delDetails(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('player/details/export', {
        ...this.queryParams
      }, `details_${new Date().getTime()}.xlsx`)
    }
  }
}
;
</script>
