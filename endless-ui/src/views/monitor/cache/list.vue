<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="8">
        <el-card style="height: calc(100vh - 125px)">
          <div slot="header">
            <span><i class="el-icon-collection"></i> 缓存列表</span>
            <el-button
              icon="el-icon-refresh-right"
              style="float: right; padding: 3px 0"
              type="text"
              @click="refreshCacheNames()"
            ></el-button>
          </div>
          <el-table
            v-loading="loading"
            :data="cacheNames"
            :height="tableHeight"
            highlight-current-row
            style="width: 100%"
            @row-click="getCacheKeys"
          >
            <el-table-column
              label="序号"
              type="index"
              width="60"
            ></el-table-column>

            <el-table-column
              :formatter="nameFormatter"
              :show-overflow-tooltip="true"
              align="center"
              label="缓存名称"
              prop="cacheName"
            ></el-table-column>

            <el-table-column
              :show-overflow-tooltip="true"
              align="center"
              label="备注"
              prop="remark"
            />
            <el-table-column
              align="center"
              class-name="small-padding fixed-width"
              label="操作"
              width="60"
            >
              <template slot-scope="scope">
                <el-button
                  icon="el-icon-delete"
                  size="mini"
                  type="text"
                  @click="handleClearCacheName(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card style="height: calc(100vh - 125px)">
          <div slot="header">
            <span><i class="el-icon-key"></i> 键名列表</span>
            <el-button
              icon="el-icon-refresh-right"
              style="float: right; padding: 3px 0"
              type="text"
              @click="refreshCacheKeys()"
            ></el-button>
          </div>
          <el-table
            v-loading="subLoading"
            :data="cacheKeys"
            :height="tableHeight"
            highlight-current-row
            style="width: 100%"
            @row-click="handleCacheValue"
          >
            <el-table-column
              label="序号"
              type="index"
              width="60"
            ></el-table-column>
            <el-table-column
              :formatter="keyFormatter"
              :show-overflow-tooltip="true"
              align="center"
              label="缓存键名"
            >
            </el-table-column>
            <el-table-column
              align="center"
              class-name="small-padding fixed-width"
              label="操作"
              width="60"
            >
              <template slot-scope="scope">
                <el-button
                  icon="el-icon-delete"
                  size="mini"
                  type="text"
                  @click="handleClearCacheKey(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card :bordered="false" style="height: calc(100vh - 125px)">
          <div slot="header">
            <span><i class="el-icon-document"></i> 缓存内容</span>
            <el-button
              icon="el-icon-refresh-right"
              style="float: right; padding: 3px 0"
              type="text"
              @click="handleClearCacheAll()"
            >清理全部
            </el-button
            >
          </div>
          <el-form :model="cacheForm">
            <el-row :gutter="32">
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存名称:" prop="cacheName">
                  <el-input v-model="cacheForm.cacheName" :readOnly="true"/>
                </el-form-item>
              </el-col>
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存键名:" prop="cacheKey">
                  <el-input v-model="cacheForm.cacheKey" :readOnly="true"/>
                </el-form-item>
              </el-col>
              <el-col :offset="1" :span="22">
                <el-form-item label="缓存内容:" prop="cacheValue">
                  <el-input
                    v-model="cacheForm.cacheValue"
                    :readOnly="true"
                    :rows="8"
                    type="textarea"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {
  clearCacheAll,
  clearCacheKey,
  clearCacheName,
  getCacheValue,
  listCacheKey,
  listCacheName
} from "@/api/monitor/cache";

export default {
  name: "CacheList",
  data() {
    return {
      cacheNames: [],
      cacheKeys: [],
      cacheForm: {},
      loading: true,
      subLoading: false,
      nowCacheName: "",
      tableHeight: window.innerHeight - 200
    };
  },
  created() {
    this.getCacheNames();
  },
  methods: {
    /** 查询缓存名称列表 */
    getCacheNames() {
      this.loading = true;
      listCacheName().then(response => {
        this.cacheNames = response.data;
        this.loading = false;
      });
    },
    /** 刷新缓存名称列表 */
    refreshCacheNames() {
      this.getCacheNames();
      this.$modal.msgSuccess("刷新缓存列表成功");
    },
    /** 清理指定名称缓存 */
    handleClearCacheName(row) {
      clearCacheName(row.cacheName).then(response => {
        this.$modal.msgSuccess("清理缓存名称[" + row.cacheName + "]成功");
        this.getCacheKeys();
      });
    },
    /** 查询缓存键名列表 */
    getCacheKeys(row) {
      const cacheName = row !== undefined ? row.cacheName : this.nowCacheName;
      if (cacheName === "") {
        return;
      }
      this.subLoading = true;
      listCacheKey(cacheName).then(response => {
        this.cacheKeys = response.data;
        this.subLoading = false;
        this.nowCacheName = cacheName;
      });
    },
    /** 刷新缓存键名列表 */
    refreshCacheKeys() {
      this.getCacheKeys();
      this.$modal.msgSuccess("刷新键名列表成功");
    },
    /** 清理指定键名缓存 */
    handleClearCacheKey(cacheKey) {
      clearCacheKey(cacheKey).then(response => {
        this.$modal.msgSuccess("清理缓存键名[" + cacheKey + "]成功");
        this.getCacheKeys();
      });
    },
    /** 列表前缀去除 */
    nameFormatter(row) {
      return row.cacheName.replace(":", "");
    },
    /** 键名前缀去除 */
    keyFormatter(cacheKey) {
      return cacheKey.replace(this.nowCacheName, "");
    },
    /** 查询缓存内容详细 */
    handleCacheValue(cacheKey) {
      getCacheValue(this.nowCacheName, cacheKey).then(response => {
        this.cacheForm = response.data;
      });
    },
    /** 清理全部缓存 */
    handleClearCacheAll() {
      clearCacheAll().then(response => {
        this.$modal.msgSuccess("清理全部缓存成功");
      });
    }
  },
};
</script>
