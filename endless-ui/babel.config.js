module.exports = {
  presets: [
    '@vue/cli-plugin-babel/preset'
  ],
  // 生产环境移除 console（需要安装 babel-plugin-transform-remove-console）
  env: {
    production: {
      plugins: []
    }
  }
}
