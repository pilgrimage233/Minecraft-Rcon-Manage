<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" class="search-form"
             label-width="85px" size="small">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="模板名称" prop="templateName">
            <el-input v-model="queryParams.templateName" clearable placeholder="请输入模板名称"
                      @keyup.enter.native="handleQuery"/>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="资源类型" prop="resourceType">
            <el-select v-model="queryParams.resourceType" clearable placeholder="请选择资源类型" style="width: 100%">
              <el-option label="RCON服务器" value="rcon_server"/>
              <el-option label="节点服务器" value="node_server"/>
              <el-option label="MC实例" value="mc_instance"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="请选择状态" style="width: 100%">
              <el-option label="正常" value="0"/>
              <el-option label="停用" value="1"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-form-item>
            <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['permission:template:add']" icon="el-icon-plus" plain size="mini" type="primary"
                   @click="handleAdd">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['permission:template:remove']" :disabled="multiple" icon="el-icon-delete" plain
                   size="mini" type="danger" @click="handleDelete">删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="templateList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column align="center" label="ID" prop="id" width="60"/>
      <el-table-column align="center" label="模板名称" min-width="150" prop="templateName"/>
      <el-table-column align="center" label="模板标识" prop="templateKey" width="150"/>
      <el-table-column align="center" label="资源类型" prop="resourceType" width="120">
        <template slot-scope="scope">
          <el-tag :type="getResourceTypeTag(scope.row.resourceType)">{{
              getResourceTypeName(scope.row.resourceType)
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="系统内置" prop="isSystem" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isSystem === 1 ? 'warning' : 'info'">{{
              scope.row.isSystem === 1 ? '是' : '否'
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{
              scope.row.status === '0' ? '正常' : '停用'
            }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="描述" min-width="200" prop="description" show-overflow-tooltip/>
      <el-table-column align="center" label="创建时间" prop="createTime" width="160"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="150">
        <template slot-scope="scope">
          <el-button v-hasPermi="['permission:template:edit']" icon="el-icon-edit" size="mini" type="text"
                     @click="handleUpdate(scope.row)">修改
          </el-button>
          <el-button v-if="scope.row.isSystem !== 1" v-hasPermi="['permission:template:remove']" icon="el-icon-delete"
                     size="mini" type="text" @click="handleDelete(scope.row)">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :limit.sync="queryParams.pageSize" :page.sync="queryParams.pageNum" :total="total"
                @pagination="getList"/>

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" append-to-body width="700px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模板名称" prop="templateName">
              <el-input v-model="form.templateName" placeholder="请输入模板名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板标识" prop="templateKey">
              <el-input v-model="form.templateKey" placeholder="请输入模板标识(唯一)"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资源类型" prop="resourceType">
              <el-select v-model="form.resourceType" placeholder="请选择资源类型" style="width: 100%"
                         @change="handleResourceTypeChange">
                <el-option label="RCON服务器" value="rcon_server"/>
                <el-option label="节点服务器" value="node_server"/>
                <el-option label="MC实例" value="mc_instance"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="0">正常</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="权限配置">
          <el-checkbox-group v-model="permissionChecks">
            <template v-if="form.resourceType === 'mc_instance'">
              <el-checkbox label="canView">查看</el-checkbox>
              <el-checkbox label="canStart">启动</el-checkbox>
              <el-checkbox label="canStop">停止</el-checkbox>
              <el-checkbox label="canRestart">重启</el-checkbox>
              <el-checkbox label="canConsole">控制台</el-checkbox>
              <el-checkbox label="canFile">文件管理</el-checkbox>
              <el-checkbox label="canConfig">配置修改</el-checkbox>
              <el-checkbox label="canDelete">删除实例</el-checkbox>
            </template>
            <template v-else-if="form.resourceType === 'node_server'">
              <el-checkbox label="canView">查看节点</el-checkbox>
              <el-checkbox label="canManageInstance">管理实例</el-checkbox>
              <el-checkbox label="canCreateInstance">创建实例</el-checkbox>
              <el-checkbox label="canDeleteInstance">删除实例</el-checkbox>
              <el-checkbox label="canManageEnv">管理环境</el-checkbox>
              <el-checkbox label="canViewLog">查看日志</el-checkbox>
            </template>
            <template v-else-if="form.resourceType === 'rcon_server'">
              <el-checkbox label="canView">查看</el-checkbox>
              <el-checkbox label="canConnect">连接</el-checkbox>
              <el-checkbox label="canCommand">执行命令</el-checkbox>
              <el-checkbox label="canConfig">修改配置</el-checkbox>
              <el-checkbox label="canDelete">删除</el-checkbox>
            </template>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" :rows="3" placeholder="请输入模板描述" type="textarea"/>
        </el-form-item>
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
import {addTemplate, delTemplate, getTemplate, listTemplate, updateTemplate} from "@/api/permission/template";

export default {
  name: "PermissionTemplate",
  data() {
    return {
      loading: true, ids: [], single: true, multiple: true, showSearch: true, total: 0,
      templateList: [], title: "", open: false, permissionChecks: [],
      queryParams: {pageNum: 1, pageSize: 10, templateName: null, resourceType: null, status: null},
      form: {},
      rules: {
        templateName: [{required: true, message: "请输入模板名称", trigger: "blur"}],
        templateKey: [{required: true, message: "请输入模板标识", trigger: "blur"}],
        resourceType: [{required: true, message: "请选择资源类型", trigger: "change"}]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listTemplate(this.queryParams).then(response => {
        this.templateList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getResourceTypeName(type) {
      const map = {'rcon_server': 'RCON服务器', 'node_server': '节点服务器', 'mc_instance': 'MC实例'};
      return map[type] || type;
    },
    getResourceTypeTag(type) {
      const map = {'rcon_server': 'danger', 'node_server': 'warning', 'mc_instance': 'success'};
      return map[type] || 'info';
    },
    handleResourceTypeChange() {
      this.permissionChecks = [];
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        id: null,
        templateName: null,
        templateKey: null,
        resourceType: 'mc_instance',
        permissionConfig: null,
        description: null,
        isSystem: 0,
        status: "0",
        remark: null
      };
      this.permissionChecks = [];
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增权限模板";
    },
    handleUpdate(row) {
      this.reset();
      getTemplate(row.id || this.ids).then(response => {
        this.form = response.data;
        if (this.form.permissionConfig) {
          try {
            const config = JSON.parse(this.form.permissionConfig);
            this.permissionChecks = Object.keys(config).filter(k => config[k]);
          } catch (e) {
            this.permissionChecks = [];
          }
        }
        this.open = true;
        this.title = "修改权限模板";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          const config = {};
          this.permissionChecks.forEach(p => {
            config[p] = true;
          });
          this.form.permissionConfig = JSON.stringify(config);
          if (this.form.id != null) {
            updateTemplate(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addTemplate(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除选中的模板？').then(() => delTemplate(ids)).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.search-form {
  background-color: #fff;
  padding: 20px 20px 0;
  margin-bottom: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
</style>
