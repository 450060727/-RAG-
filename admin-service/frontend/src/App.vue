<!-- 管理端根组件：仅作为路由视图容器 -->
<template>
  <!-- 路由出口，匹配到的页面组件将渲染在这里 -->
  <router-view />
</template>

<script setup>
// 根组件暂无额外逻辑
</script>

<style>
/* 全局重置：去除默认边距并统一盒模型 */
* { margin: 0; padding: 0; box-sizing: border-box; }
/* 确保 html、body、#app 占满视口高度 */
html, body, #app { height: 100%; }
</style>
