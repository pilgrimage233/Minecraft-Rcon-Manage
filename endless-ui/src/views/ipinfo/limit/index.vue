<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="IP地址" prop="ip">
        <el-input
          v-model="queryParams.ip"
          clearable
          placeholder="请输入IP地址"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="请求次数" prop="count">
        <el-input
          v-model="queryParams.count"
          clearable
          placeholder="请输入请求次数"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="省" prop="province">
        <el-input
          v-model="queryParams.province"
          clearable
          placeholder="请输入省"
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
      <!--      <el-form-item label="区县" prop="county">
              <el-input
                v-model="queryParams.county"
                placeholder="请输入区县"
                clearable
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>-->
      <el-form-item label="经度" prop="longitude">
        <el-input
          v-model="queryParams.longitude"
          clearable
          placeholder="请输入经度"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="纬度" prop="latitude">
        <el-input
          v-model="queryParams.latitude"
          clearable
          placeholder="请输入纬度"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['ipinfo:limit:edit']"
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
          v-hasPermi="['ipinfo:limit:remove']"
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
          v-hasPermi="['ipinfo:limit:export']"
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

    <el-table v-loading="loading" :data="limitList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="IP地址" prop="ip" show-overflow-tooltip/>
      <el-table-column align="center" label="UA标识" prop="userAgent" show-overflow-tooltip/>
      <el-table-column align="center" label="创建者" prop="createBy" show-overflow-tooltip/>
      <el-table-column align="center" label="更新者" prop="updateBy" show-overflow-tooltip/>
      <el-table-column align="center" label="请求次数" prop="count"/>
      <el-table-column align="center" label="省" prop="province" show-overflow-tooltip/>
      <el-table-column align="center" label="地市" prop="city" show-overflow-tooltip/>
      <!--      <el-table-column label="区县" align="center" prop="county" show-overflow-tooltip/>-->
      <el-table-column align="center" label="经度" prop="longitude" show-overflow-tooltip/>
      <el-table-column align="center" label="纬度" prop="latitude" show-overflow-tooltip/>
      <el-table-column align="center" label="请求参数" prop="bodyParams" show-overflow-tooltip/>
      <el-table-column align="center" label="描述" prop="remark" show-overflow-tooltip/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['ipinfo:limit:edit']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleUpdate(scope.row)"
          >修改
          </el-button>
          <el-button
            v-hasPermi="['ipinfo:limit:remove']"
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

    <!-- 添加或修改IP限流对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="IP地址" prop="ip">
          <el-input v-model="form.ip" placeholder="请输入IP地址"/>
        </el-form-item>
        <el-form-item label="UA标识" prop="userAgent">
          <el-input v-model="form.userAgent" disabled placeholder="请输入内容" type="textarea"/>
        </el-form-item>
        <el-form-item label="请求次数" prop="count">
          <el-input v-model="form.count" placeholder="请输入请求次数"/>
        </el-form-item>
        <el-form-item label="省" prop="province">
          <el-input v-model="form.province" placeholder="请输入省"/>
        </el-form-item>
        <el-form-item label="地市" prop="city">
          <el-input v-model="form.city" placeholder="请输入地市"/>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input v-model="form.longitude" disabled placeholder="请输入经度"/>
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input v-model="form.latitude" disabled placeholder="请输入纬度"/>
        </el-form-item>
        <el-form-item label="请求参数" prop="bodyParams">
          <el-input v-model="form.bodyParams" disabled placeholder="请输入内容" type="textarea"/>
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入描述"/>
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
import {addLimit, delLimit, getLimit, listLimit, updateLimit} from "@/api/ipinfo/limit";

export default {
  name: "Limit",
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
      // IP限流表格数据
      limitList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        uuid: null,
        ip: null,
        userAgent: null,
        count: null,
        province: null,
        city: null,
        county: null,
        longitude: null,
        latitude: null,
        bodyParams: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        id: [
          {required: true, message: "主键ID不能为空", trigger: "blur"}
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询IP限流列表 */
    getList() {
      this.loading = true;
      listLimit(this.queryParams).then(response => {
        this.limitList = response.rows;
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
        uuid: null,
        createTime: null,
        createBy: null,
        updateTime: null,
        updateBy: null,
        ip: null,
        userAgent: null,
        count: null,
        province: null,
        city: null,
        county: null,
        longitude: null,
        latitude: null,
        bodyParams: null,
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
      this.title = "添加IP限流";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getLimit(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改IP限流";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateLimit(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addLimit(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除IP限流编号为"' + ids + '"的数据项？').then(function () {
        return delLimit(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('ipinfo/limit/export', {
        ...this.queryParams
      }, `limit_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
