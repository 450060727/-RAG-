<script>
// uni-app 的 App.vue 不能有 template，只有 script（生命周期）+ style
export default {
  // 应用启动生命周期
  onLaunch() {
    console.log('App Launch')
  }
}
</script>

<style>
/* 全局页面基础样式 */
page {
  background-color: #f5f5f5;
  color: #333;
  font-size: 15px;
}
</style>
