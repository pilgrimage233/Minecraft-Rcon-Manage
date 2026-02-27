<template>
  <div class="app-container">
    <el-row :gutter="20" class="summary-row">
      <el-col :span="12">
        <el-card class="summary-card">
          <div class="summary-title">注册用户数量</div>
          <div class="summary-value">{{ summary.registeredCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="summary-card">
          <div class="summary-title">在线用户数量</div>
          <div class="summary-value online">{{ summary.onlineCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-form ref="queryForm" :inline="true" :model="queryParams" label-width="68px" size="small">
      <el-form-item label="QQ号" prop="qqNum">
        <el-input
          v-model="queryParams.qqNum"
          clearable
          placeholder="请输入QQ号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="账号" prop="userName">
        <el-input
          v-model="queryParams.userName"
          clearable
          placeholder="请输入账号"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="list.slice((pageNum-1)*pageSize,pageNum*pageSize)"
      style="width: 100%;"
    >
      <el-table-column align="center" label="序号" type="index">
        <template slot-scope="scope">
          <span>{{ (pageNum - 1) * pageSize + scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="账号" prop="userName"/>
      <el-table-column align="center" label="QQ号" prop="qqNum"/>
      <el-table-column align="center" label="白名单ID" prop="whitelistId"/>
      <el-table-column align="center" label="头衔" prop="roleTitle"/>
      <el-table-column align="center" label="等级" prop="roleLevel" width="90"/>
      <el-table-column align="center" label="可发起投票" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.canInitiateVote === 1 ? 'success' : 'info'">
            {{ scope.row.canInitiateVote === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态">
        <template slot-scope="scope">
          <el-tag type="success">在线</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="登录时间" prop="loginTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.loginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="过期时间" prop="expireTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.expireTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['monitor:whitelist-user:forceLogout']"
            icon="el-icon-delete"
            size="mini"
            type="text"
            @click="handleForceLogout(scope.row)"
          >强退
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :limit.sync="pageSize" :page.sync="pageNum" :total="total"/>

    <el-divider content-position="left">用户等级与头衔管理</el-divider>

    <el-table v-loading="roleLoading" :data="registeredList" style="width: 100%; margin-top: 12px;">
      <el-table-column align="center" label="用户ID" prop="id" width="100"/>
      <el-table-column align="center" label="账号" prop="userName"/>
      <el-table-column align="center" label="QQ号" prop="qqNum"/>
      <el-table-column align="center" label="头衔" prop="roleTitle"/>
      <el-table-column align="center" label="等级" prop="roleLevel" width="90"/>
      <el-table-column align="center" label="发起投票" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.canInitiateVote === 1 ? 'success' : 'info'">
            {{ scope.row.canInitiateVote === 1 ? '可发起' : '仅跟投' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="160">
        <template slot-scope="scope">
          <el-button
            v-hasPermi="['monitor:whitelist-user:role']"
            icon="el-icon-edit"
            size="mini"
            type="text"
            @click="handleEditRole(scope.row)"
          >编辑
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :visible.sync="roleOpen" append-to-body title="编辑用户等级与头衔" width="520px">
      <el-form ref="roleForm" :model="roleForm" :rules="roleRules" label-width="110px" size="small">
        <el-form-item label="账号">
          <el-input v-model="roleForm.userName" disabled/>
        </el-form-item>
        <el-form-item label="头衔" prop="roleTitle">
          <el-input v-model="roleForm.roleTitle" placeholder="如：成员/管理员/Owner/仲裁官"/>
        </el-form-item>
        <el-form-item label="等级" prop="roleLevel">
          <el-input-number v-model="roleForm.roleLevel" :max="999" :min="1" controls-position="right"
                           style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="发起投票权限" prop="canInitiateVote">
          <el-switch
            v-model="roleCanInitiateBool"
            active-text="可发起"
            inactive-text="仅跟投"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRoleForm">确 定</el-button>
        <el-button @click="roleOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {forceLogout, getSummary, list, listRegisteredUsers, updateWhitelistUserRole} from '@/api/monitor/whitelistUser'

export default {
  name: 'WhitelistUserOnline',
  data() {
    return {
      loading: true,
      summary: {
        registeredCount: 0,
        onlineCount: 0
      },
      list: [],
      registeredList: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      roleLoading: false,
      roleOpen: false,
      roleCanInitiateBool: false,
      roleForm: {
        userId: null,
        userName: '',
        roleLevel: 1,
        roleTitle: '成员',
        canInitiateVote: 0
      },
      roleRules: {
        roleTitle: [{required: true, message: '头衔不能为空', trigger: 'blur'}],
        roleLevel: [{required: true, message: '等级不能为空', trigger: 'change'}]
      },
      queryParams: {
        qqNum: undefined,
        userName: undefined
      }
    }
  },
  created() {
    this.fetchSummary()
    this.getList()
    this.getRegisteredList()
  },
  methods: {
    fetchSummary() {
      getSummary().then(response => {
        this.summary = response.data || {registeredCount: 0, onlineCount: 0}
      })
    },
    getList() {
      this.loading = true
      list(this.queryParams).then(response => {
        this.list = response.rows || []
        this.total = response.total || 0
        this.loading = false
      })
    },
    getRegisteredList() {
      this.roleLoading = true
      listRegisteredUsers({}).then(response => {
        this.registeredList = response.rows || []
      }).finally(() => {
        this.roleLoading = false
      })
    },
    handleQuery() {
      this.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleForceLogout(row) {
      this.$modal.confirm('是否确认强退账号为"' + row.userName + '"的用户？').then(() => {
        return forceLogout(row.token)
      }).then(() => {
        this.getList()
        this.fetchSummary()
        this.$modal.msgSuccess('强退成功')
      }).catch(() => {
      })
    },
    handleEditRole(row) {
      this.roleForm = {
        userId: row.id,
        userName: row.userName,
        roleLevel: row.roleLevel || 1,
        roleTitle: row.roleTitle || '成员',
        canInitiateVote: row.canInitiateVote || 0
      }
      this.roleCanInitiateBool = this.roleForm.canInitiateVote === 1
      this.roleOpen = true
    },
    submitRoleForm() {
      this.$refs.roleForm.validate(valid => {
        if (!valid) {
          return
        }
        const payload = {
          userId: this.roleForm.userId,
          roleLevel: this.roleForm.roleLevel,
          roleTitle: this.roleForm.roleTitle,
          canInitiateVote: this.roleCanInitiateBool ? 1 : 0
        }
        updateWhitelistUserRole(payload).then(() => {
          this.$modal.msgSuccess('更新成功')
          this.roleOpen = false
          this.getRegisteredList()
          this.getList()
        })
      })
    }
  }
}
</script>

<style scoped>
.summary-row {
  margin-bottom: 20px;
}

.summary-card {
  text-align: center;
}

.summary-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.summary-value.online {
  color: #67c23a;
}
</style>
