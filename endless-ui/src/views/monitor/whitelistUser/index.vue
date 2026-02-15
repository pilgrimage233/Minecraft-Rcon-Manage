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
  </div>
</template>

<script>
import {forceLogout, getSummary, list} from '@/api/monitor/whitelistUser'

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
      total: 0,
      pageNum: 1,
      pageSize: 10,
      queryParams: {
        qqNum: undefined,
        userName: undefined
      }
    }
  },
  created() {
    this.fetchSummary()
    this.getList()
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
