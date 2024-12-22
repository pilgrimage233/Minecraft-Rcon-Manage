<template>
  <div class="app-wrapper">
    <div class="form-container">
      <h2>白名单申请</h2>
      <el-form :model="form" label-width="auto">
        <el-form-item label="ID：">
          <el-input v-model="form.userName" placeholder="请输入游戏名称"></el-input>
        </el-form-item>
        <el-form-item label="QQ：">
          <el-input v-model="form.qqNum" placeholder="请输入QQ号"></el-input>
        </el-form-item>
        <el-form-item label="是否正版：">
          <el-radio-group v-model="form.onlineFlag">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述：" label-width="auto">
          <el-input v-model="form.remark" placeholder="请输入描述 非必填" type="textarea"></el-input>
        </el-form-item>
        <el-button v-loading.fullscreen.lock="loading" type="primary" @click="submitForm">提 交</el-button>
        <el-button v-loading.fullscreen.lock="loading" type="success" @click="getOnlinePlayer">查询人数</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue';
import {ElMessage} from 'element-plus';
import axios from 'axios';

const http = axios.create({
  baseURL: 'https://application.shenzhuo.vip', // 替换为你的域名或IP地址
  timeout: 5000
});

const form = reactive({
  userName: '',
  qqNum: '',
  onlineFlag: '',
  remark: ''
});

let loading = false;

const submitForm = () => {
  if (!form.userName || !form.qqNum || !form.onlineFlag) {
    ElMessage.error('请填写完整信息');
    // qq号验证
  } else if (!/^\d{5,11}$/.test(form.qqNum)) {
    ElMessage.error('QQ号格式错误');
  } else {
    loading = true;
    http.post('/mc/whitelist/apply', form).then((res) => {
      if (res.data.code === 200) {
        ElMessage.success(res.data.msg);
      } else {
        ElMessage.error(res.data.msg || '未知错误，请联系管理员');
      }
      loading = false;
    }).catch((error) => {
      console.error('提交表单请求出错：', error);
      ElMessage.error('提交表单时发生错误，请检查网络或联系管理员');
      loading = false;
    });
  }
};
const getOnlinePlayer = () => {
  loading = true;
  http.get('/server/serverlist/getOnlinePlayer').then((res) => {
    if (res.data.code === 200) {
      ElMessage.success(JSON.stringify(res.data.data));
    } else {
      ElMessage.error(res.data.msg || '未知错误，请联系管理员');
    }
    loading = false;
  }).catch((error) => {
    console.error('查询在线玩家请求出错：', error);
    ElMessage.error('查询在线玩家时发生错误，请检查网络或联系管理员');
    loading = false;
  });
};
</script>

<style scoped>
.app-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(to bottom right, #ffb3ba, #ffdfba);
}

.form-container {
  background-color: rgba(255, 255, 255, 0.8);
  padding: 25px;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
  width: min(100%, 500px);
}
</style>