import request from '@/utils/request'

// 查询节点服务器列表
export function listServer(query) {
  return request({
    url: '/node/server/list',
    method: 'get',
    params: query
  })
}

// 查询节点服务器详细
export function getServer(id) {
  return request({
    url: '/node/server/' + id,
    method: 'get'
  })
}

// 新增节点服务器
export function addServer(data) {
  return request({
    url: '/node/server',
    method: 'post',
    data: data
  })
}

// 修改节点服务器
export function updateServer(data) {
  return request({
    url: '/node/server',
    method: 'put',
    data: data
  })
}

// 删除节点服务器
export function delServer(id) {
  return request({
    url: '/node/server/' + id,
    method: 'delete'
  })
}

// 获取节点服务器主机信息
export function getServerInfo(id) {
  return request({
    url: `/node/server/getServerInfo/${id}`,
    method: 'get'
  })
}

// 获取节点服务器负载信息
export function getServerLoad(id) {
  return request({
    url: `/node/server/getServerLoad/${id}`,
    method: 'get'
  })
}

// 获取服务器文件列表
export function getFileList(data) {
  return request({
    url: '/node/server/getFileList',
    method: 'post',
    data: data
  })
}

// 下载文件
export function downloadFile(id, path) {
  return request({
    url: '/node/server/download',
    method: 'post',
    data: {
      id: id,
      path: path
    },
    responseType: 'blob'
  })
}

// 保存文件
export function saveFile(data) {
  const formData = new FormData();
  formData.append('id', data.id);
  formData.append('path', data.path);
  // 将文本内容转换为 Blob 并作为文件上传
  const blob = new Blob([data.content], {type: 'text/plain'});
  // 使用文件路径的最后一部分作为文件名
  const fileName = data.path.split('\\').pop();
  formData.append('file', blob, fileName);

  return request({
    url: '/node/server/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    // 阻止 axios 自动设置 Content-Type
    transformRequest: [(data) => data]
  })
}

// 从URL下载文件到服务器
export function downloadFromUrl(data) {
  return request({
    url: '/node/server/downloadFromUrl',
    method: 'post',
    data: data
  })
}
