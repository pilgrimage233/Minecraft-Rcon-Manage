<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <i class="el-icon-files stat-icon" style="color: #409EFF"></i>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalBackups || 0 }}</div>
              <div class="stat-label">备份总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <i class="el-icon-folder-opened stat-icon" style="color: #67C23A"></i>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalSizeFormatted || '0 B' }}</div>
              <div class="stat-label">总大小</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <i class="el-icon-time stat-icon" style="color: #E6A23C"></i>
            <div class="stat-content">
              <div class="stat-value stat-value-time">{{ statistics.latestBackupTime || '-' }}</div>
              <div class="stat-label">最新备份</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <i class="el-icon-document-copy stat-icon" style="color: #F56C6C"></i>
            <div class="stat-content">
              <div class="stat-value">{{ backupTables.length }}</div>
              <div class="stat-label">备份表数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索区域 -->
    <el-form v-show="showSearch" ref="queryForm" :inline="true" :model="queryParams" size="small">
      <el-form-item label="备份类型" prop="backupType">
        <el-select v-model="queryParams.backupType" clearable placeholder="请选择备份类型">
          <el-option label="全量备份" value="full_backup"/>
          <el-option label="定时备份" value="scheduled_backup"/>
          <el-option label="回滚备份" value="rollback_backup"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['system:backup:add']"
          icon="el-icon-plus"
          plain
          size="mini"
          type="primary"
          @click="handleManualBackup"
        >手动备份
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          v-hasPermi="['system:backup:remove']"
          :disabled="multiple"
          icon="el-icon-delete"
          plain
          size="mini"
          type="danger"
          @click="handleDelete"
        >删除
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="backupList" @selection-change="handleSelectionChange">
      <el-table-column align="center" type="selection" width="55"/>
      <el-table-column :show-overflow-tooltip="true" align="center" label="备份ID" prop="backupId" width="200"/>
      <el-table-column align="center" label="备份类型" prop="backupTypeDesc" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.backupType === 'full_backup'" type="success">{{ scope.row.backupTypeDesc }}</el-tag>
          <el-tag v-else-if="scope.row.backupType === 'scheduled_backup'" type="info">{{
              scope.row.backupTypeDesc
            }}
          </el-tag>
          <el-tag v-else-if="scope.row.backupType === 'rollback_backup'" type="danger">{{
              scope.row.backupTypeDesc
            }}
          </el-tag>
          <el-tag v-else type="warning">{{ scope.row.backupTypeDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="创建时间" prop="createTime" width="180"/>
      <el-table-column align="center" label="数据库" prop="database" width="120"/>
      <el-table-column align="center" label="表数量" prop="tableCount" width="100"/>
      <el-table-column align="center" label="大小" prop="sizeFormatted" width="120"/>
      <el-table-column align="center" label="版本" prop="version" width="100"/>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['system:backup:query']"
            icon="el-icon-view"
            size="mini"
            type="text"
            @click="handleDetail(scope.row)"
          >详情
          </el-button>
          <el-button
            v-hasPermi="['system:backup:restore']"
            icon="el-icon-refresh-left"
            size="mini"
            type="text"
            @click="handleRestore(scope.row)"
          >单表恢复
          </el-button>
          <el-button
            v-if="scope.row.backupType === 'scheduled_backup'"
            v-hasPermi="['system:backup:restore']"
            icon="el-icon-refresh"
            size="mini"
            type="text"
            @click="handleRestoreAll(scope.row)"
          >全量回滚
          </el-button>
          <el-button
            v-hasPermi="['system:backup:remove']"
            icon="el-icon-delete"
            size="mini"
            type="text"
            @click="handleDelete(scope.row)"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 备份详情对话框 -->
    <el-dialog :close-on-click-modal="false" :visible.sync="detailOpen" append-to-body title="备份详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="备份ID">{{ detailData.backupId }}</el-descriptions-item>
        <el-descriptions-item label="备份类型">{{ detailData.backupTypeDesc }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="数据库">{{ detailData.database }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ detailData.version }}</el-descriptions-item>
        <el-descriptions-item label="大小">{{ detailData.sizeFormatted }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="备份路径">{{ detailData.backupPath }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">备份的表列表
        ({{ detailData.tableCount || (detailData.tables ? detailData.tables.length : 0) }})
      </el-divider>
      <el-tag
        v-for="(table, index) in detailData.tables"
        :key="'table-' + index"
        style="margin: 5px"
        type="info"
      >{{ table }}
      </el-tag>

      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 恢复数据对话框 -->
    <el-dialog :close-on-click-modal="false" :visible.sync="restoreOpen" append-to-body title="单表恢复" width="600px">
      <el-alert
        :closable="false"
        description="恢复操作将覆盖当前表的数据，请谨慎操作！建议先备份当前数据。"
        show-icon
        style="margin-bottom: 20px"
        title="警告"
        type="warning"
      ></el-alert>

      <el-form ref="restoreForm" :model="restoreForm" label-width="100px">
        <el-form-item label="备份ID">
          <el-input v-model="restoreForm.backupId" disabled/>
        </el-form-item>
        <el-form-item label="选择表" prop="tableName">
          <el-select v-model="restoreForm.tableName" placeholder="请选择要恢复的表" style="width: 100%">
            <el-option
              v-for="(table, index) in restoreForm.tables"
              :key="'restore-' + index"
              :label="table"
              :value="table"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="restoreOpen = false">取 消</el-button>
        <el-button :loading="restoreLoading" type="primary" @click="submitRestore">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 全量回滚对话框 -->
    <el-dialog :close-on-click-modal="false" :visible.sync="restoreAllOpen" append-to-body title="全量回滚"
               width="700px">
      <el-alert
        :closable="false"
        description="全量回滚将恢复该备份中的所有表数据，这将覆盖当前数据库中对应表的所有数据！此操作不可逆，请务必谨慎！"
        show-icon
        style="margin-bottom: 20px"
        title="危险操作警告"
        type="error"
      ></el-alert>

      <el-form ref="restoreAllForm" :model="restoreAllForm" label-width="120px">
        <el-form-item label="备份ID">
          <el-input v-model="restoreAllForm.backupId" disabled/>
        </el-form-item>
        <el-form-item label="备份时间">
          <el-input v-model="restoreAllForm.createTime" disabled/>
        </el-form-item>
        <el-form-item label="将要恢复的表">
          <div
            style="max-height: 200px; overflow-y: auto; border: 1px solid #DCDFE6; padding: 10px; border-radius: 4px;">
            <el-tag
              v-for="(table, index) in restoreAllForm.tables"
              :key="'restoreAll-' + index"
              size="small"
              style="margin: 3px"
              type="danger"
            >{{ table }}
            </el-tag>
          </div>
          <div style="margin-top: 10px; color: #909399; font-size: 12px;">
            共 {{ restoreAllForm.tables.length }} 个表将被恢复
          </div>
        </el-form-item>
        <el-form-item label="确认操作">
          <el-input
            v-model="restoreAllForm.confirmText"
            placeholder="请输入 RESTORE 确认执行全量回滚"
            style="width: 100%"
          />
          <div style="margin-top: 5px; color: #F56C6C; font-size: 12px;">
            * 请输入 "RESTORE" 以确认此危险操作
          </div>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="restoreAllOpen = false">取 消</el-button>
        <el-button
          :disabled="restoreAllForm.confirmText !== 'RESTORE'"
          :loading="restoreAllLoading"
          type="danger"
          @click="submitRestoreAll"
        >确认回滚
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  delBackup,
  getBackup,
  getBackupTables,
  getStatistics,
  listBackup,
  manualBackup,
  restoreAllTables,
  restoreTable
} from "@/api/system/backup";

export default {
  name: "Backup",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 备份表格数据
      backupList: [],
      // 查询参数
      queryParams: {
        backupType: undefined
      },
      // 统计信息
      statistics: {},
      // 可备份的表列表
      backupTables: [],
      // 详情对话框
      detailOpen: false,
      detailData: {},
      // 单表恢复对话框
      restoreOpen: false,
      restoreLoading: false,
      restoreForm: {
        backupId: '',
        tableName: '',
        tables: []
      },
      // 全量回滚对话框
      restoreAllOpen: false,
      restoreAllLoading: false,
      restoreAllForm: {
        backupId: '',
        createTime: '',
        tables: [],
        confirmText: ''
      }
    };
  },
  created() {
    this.getList();
    this.getStatistics();
    this.getBackupTables();
  },
  methods: {
    /** 查询备份列表 */
    getList() {
      this.loading = true;
      listBackup(this.queryParams).then(response => {
        this.backupList = response.rows;
        this.loading = false;
      });
    },
    /** 获取统计信息 */
    getStatistics() {
      getStatistics().then(response => {
        this.statistics = response.data;
      });
    },
    /** 获取可备份的表列表 */
    getBackupTables() {
      getBackupTables().then(response => {
        this.backupTables = response.data;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.backupId);
      this.multiple = !selection.length;
    },
    /** 手动备份 */
    handleManualBackup() {
      this.$modal.confirm('是否确认执行手动备份？').then(() => {
        return manualBackup();
      }).then(() => {
        this.$modal.msgSuccess("备份任务已启动，请稍后刷新查看");
        this.getList();
        this.getStatistics();
      }).catch(() => {
      });
    },
    /** 详情按钮操作 */
    handleDetail(row) {
      const backupId = row.backupId;
      getBackup(backupId).then(response => {
        this.detailData = response.data;
        this.detailOpen = true;
      });
    },
    /** 单表恢复按钮操作 */
    handleRestore(row) {
      this.restoreForm = {
        backupId: row.backupId,
        tableName: '',
        tables: row.tables || []
      };
      this.restoreOpen = true;
    },
    /** 提交单表恢复 */
    submitRestore() {
      if (!this.restoreForm.tableName) {
        this.$modal.msgWarning("请选择要恢复的表");
        return;
      }

      this.$modal.confirm('确认要恢复表 "' + this.restoreForm.tableName + '" 的数据吗？此操作将覆盖当前数据！').then(() => {
        this.restoreLoading = true;
        return restoreTable(this.restoreForm.backupId, this.restoreForm.tableName);
      }).then(() => {
        this.$modal.msgSuccess("恢复成功");
        this.restoreOpen = false;
        this.restoreLoading = false;
        // 刷新列表以显示新创建的回滚备份
        this.getList();
        this.getStatistics();
      }).catch(() => {
        this.restoreLoading = false;
      });
    },
    /** 全量回滚按钮操作 */
    handleRestoreAll(row) {
      this.restoreAllForm = {
        backupId: row.backupId,
        createTime: row.createTime,
        tables: row.tables || [],
        confirmText: ''
      };
      this.restoreAllOpen = true;
    },
    /** 提交全量回滚 */
    submitRestoreAll() {
      if (this.restoreAllForm.confirmText !== 'RESTORE') {
        this.$modal.msgWarning("请输入 RESTORE 确认操作");
        return;
      }

      this.$modal.confirm('您即将执行全量回滚操作，这将恢复 ' + this.restoreAllForm.tables.length + ' 个表的数据！此操作不可逆，确认继续吗？').then(() => {
        this.restoreAllLoading = true;

        // 调用全量回滚接口
        return restoreAllTables(this.restoreAllForm.backupId);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg || "全量回滚成功");
        this.restoreAllOpen = false;
        this.restoreAllLoading = false;
        // 刷新列表以显示新创建的回滚备份
        this.getList();
        this.getStatistics();
      }).catch(() => {
        this.restoreAllLoading = false;
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const backupIds = row.backupId || this.ids;
      this.$modal.confirm('是否确认删除备份"' + backupIds + '"？').then(() => {
        return delBackup(backupIds);
      }).then(() => {
        this.getList();
        this.getStatistics();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    }
  }
};
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 15px;
  min-height: 80px;
}

.stat-icon {
  font-size: 48px;
  margin-right: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
  margin-bottom: 8px;
}

.stat-value-time {
  font-size: 16px;
  white-space: nowrap;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  line-height: 1.2;
}
</style>
