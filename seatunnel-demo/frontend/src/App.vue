<template>
  <el-config-provider :locale="zhCn">
    <el-container class="app-container">
      <el-aside width="220px" class="aside">
        <div class="logo">
          <span class="logo-text">🚀 SeaTunnel</span>
          <span class="logo-sub">数据集成平台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>概览</span>
          </el-menu-item>
          <el-menu-item index="/jobs">
            <el-icon><List /></el-icon>
            <span>任务管理</span>
          </el-menu-item>
          <el-menu-item index="/create">
            <el-icon><Plus /></el-icon>
            <span>创建任务</span>
          </el-menu-item>
          <el-menu-item index="/connectors">
            <el-icon><Connection /></el-icon>
            <span>连接器</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <span class="header-title">{{ currentPageTitle }}</span>
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { House, List, Plus, Connection } from '@element-plus/icons-vue'

const route = useRoute()

const activeMenu = computed(() => route.path)

const currentPageTitle = computed(() => {
  const titles = {
    '/': '概览',
    '/jobs': '任务管理',
    '/create': '创建任务',
    '/connectors': '连接器'
  }
  return titles[route.path] || '概览'
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.app-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid #3a4a5b;
}

.logo-text {
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}

.logo-sub {
  color: #bfcbd9;
  font-size: 12px;
  margin-top: 2px;
}

.el-menu {
  border-right: none;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
