const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true
})

module.exports = {
  devServer: {
    proxy: {
      '/api': { // 代理所有以 /api 开头的请求
        target: 'http://localhost:8081', // 后端地址
        changeOrigin: true, // 允许跨域
        pathRewrite: {
          '^/api': '', // 去掉 /api 前缀
        },
        onProxyReq: (proxyReq, req, res) => {
          console.log('Proxy Request:', req.url); // 打印代理后的请求路径
        },
      },
    },
  },
};