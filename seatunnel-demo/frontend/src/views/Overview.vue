<template>
  <div class="overview-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8" v-for="stat in stats" :key="stat.label">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <span class="stat-icon">{{ stat.icon }}</span>
            <div class="stat-info">
              <span class="stat-value">{{ stat.value }}</span>
              <span class="stat-label">{{ stat.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>SeaTunnel 是什么？</span>
            </div>
          </template>
          <div class="description-content">
            <p>
              <strong>SeaTunnel</strong> 是一个超高性能、分布式的数据集成工具，它可以让你轻松地在各种数据源之间进行数据同步。
            </p>
            <el-divider />
            <h4>核心优势：</h4>
            <ul class="feature-list">
              <li>
                <el-tag type="primary">统一配置</el-tag>
                <span>一套配置，支持多种数据源（MySQL、PostgreSQL、Oracle、Doris、ClickHouse 等）</span>
              </li>
              <li>
                <el-tag type="success">CDC 实时同步</el-tag>
                <span>支持 MySQL Binlog、PostgreSQL WAL 等日志捕获，实现毫秒级数据同步</span>
              </li>
              <li>
                <el-tag type="warning">分布式执行</el-tag>
                <span>基于 Spark/Flink 引擎，支持大规模数据并行处理</span>
              </li>
              <li>
                <el-tag type="info">易用性强</el-tag>
                <span>声明式配置，无需编写代码，降低数据集成门槛</span>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>快速开始</span>
            </div>
          </template>
          <div class="quick-start-content">
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in activities"
                :key="index"
                :icon="activity.icon"
                :type="activity.type"
                :color="activity.color"
                :size="activity.size"
                :timestamp="activity.timestamp"
              >
                <el-card shadow="never">
                  <h4>{{ activity.title }}</h4>
                  <p style="color: #606266; font-size: 13px;">{{ activity.content }}</p>
                  <el-button
                    v-if="activity.action"
                    type="primary"
                    size="small"
                    @click="handleQuickAction(activity.action)"
                  >
                    {{ activity.actionText }}
                  </el-button>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>SeaTunnel 架构说明</span>
              <el-button type="primary" size="small" @click="loadOverview">
                刷新数据
              </el-button>
            </div>
          </template>
          <div class="architecture-content">
            <el-row :gutter="30">
              <el-col :span="8">
                <div class="arch-section">
                  <div class="arch-icon">📥</div>
                  <h4>Source (数据源)</h4>
                  <p>从各种数据源读取数据</p>
                  <el-tag type="info" size="small" style="margin: 3px;">MySQL</el-tag>
                  <el-tag type="info" size="small" style="margin: 3px;">PostgreSQL</el-tag>
                  <el-tag type="info" size="small" style="margin: 3px;">Oracle</el-tag>
                  <el-tag type="info" size="small" style="margin: 3px;">...</el-tag>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="arch-section center-section">
                  <div class="arch-icon">⚙️</div>
                  <h4>SeaTunnel Engine</h4>
                  <p>数据转换和处理引擎</p>
                  <el-tag type="primary" size="small" style="margin: 3px;">Transform</el-tag>
                  <el-tag type="primary" size="small" style="margin: 3px;">Filter</el-tag>
                  <el-tag type="primary" size="small" style="margin: 3px;">CDC</el-tag>
                  <div class="arrow-flow">
                    <span class="arrow">→</span>
                  </div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="arch-section">
                  <div class="arch-icon">📤</div>
                  <h4>Sink (目标)</h4>
                  <p>写入到各种数据目标</p>
                  <el-tag type="success" size="small" style="margin: 3px;">Doris</el-tag>
                  <el-tag type="success" size="small" style="margin: 3px;">ClickHouse</el-tag>
                  <el-tag type="success" size="small" style="margin: 3px;">Hive</el-tag>
                  <el-tag type="success" size="small" style="margin: 3px;">...</el-tag>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { RefreshRight, Document, Connection, VideoPlay } from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()

const stats = ref([
  { label: '总任务数', value: 0, icon: '📊' },
  { label: '运行中', value: 0, icon: '▶️' },
  { label: '已完成', value: 0, icon: '✅' }
])

const activities = ref([
  {
    title: '查看任务列表',
    content: '查看当前所有的 SeaTunnel 同步任务状态',
    icon: Document,
    type: 'primary',
    timestamp: '第一步',
    action: 'jobs',
    actionText: '查看任务'
  },
  {
    title: '创建新任务',
    content: '使用模板或自定义配置创建数据同步任务',
    icon: Connection,
    type: 'success',
    timestamp: '第二步',
    action: 'create',
    actionText: '创建任务'
  },
  {
    title: '快速体验',
    content: '一键启动一个示例任务，体验 SeaTunnel 的功能',
    icon: VideoPlay,
    type: 'warning',
    timestamp: '快速体验',
    action: 'quick',
    actionText: '启动示例'
  }
])

const loadOverview = async () => {
  try {
    const res = await api.getOverview()
    const data = res.data
    if (data.stats) {
      stats.value = data.stats
    }
  } catch (error) {
    console.error('加载概览失败', error)
  }
}

const handleQuickAction = (action) => {
  if (action === 'jobs') {
    router.push('/jobs')
  } else if (action === 'create') {
    router.push('/create')
  } else if (action === 'quick') {
    startQuickDemo()
  }
}

const startQuickDemo = async () => {
  try {
    ElMessage.info('正在启动示例任务...')
    const res = await api.quickStart('mysql-to-doris')
    ElMessage.success(`示例任务已启动，任务ID: ${res.data}`)
    router.push('/jobs')
  } catch (error) {
    console.error('启动示例失败', error)
  }
}

onMounted(() => {
  loadOverview()
})
</script>

<style scoped>
.overview-container {
  padding: 10px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  border: none;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 48px;
  margin-right: 20px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.info-card {
  border-radius: 8px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.description-content {
  color: #606266;
  line-height: 1.8;
}

.description-content h4 {
  margin: 15px 0 10px 0;
  color: #303133;
}

.feature-list {
  list-style: none;
  padding: 0;
}

.feature-list li {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.feature-list li .el-tag {
  margin-right: 12px;
  min-width: 90px;
  text-align: center;
}

.quick-start-content {
  padding: 10px 0;
}

.architecture-content {
  padding: 20px 0;
}

.arch-section {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  border-radius: 12px;
  position: relative;
}

.arch-section.center-section {
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
}

.arch-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.arch-section h4 {
  margin-bottom: 10px;
  color: #303133;
}

.arch-section p {
  color: #909399;
  font-size: 13px;
}

.arrow-flow {
  position: absolute;
  right: -20px;
  top: 50%;
  transform: translateY(-50%);
}

.arrow {
  font-size: 36px;
  color: #409EFF;
}
</style>
