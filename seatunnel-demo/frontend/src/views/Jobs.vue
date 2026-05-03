<template>
  <div class="jobs-container">
    <el-card class="header-card">
      <div class="header-content">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索任务名称或ID..."
          style="width: 300px"
          clearable
          @keyup.enter="loadJobs"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <div class="header-buttons">
          <el-button type="primary" @click="loadJobs">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button type="success" @click="$router.push('/create')">
            <el-icon><Plus /></el-icon>
            新建任务
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;" shadow="never">
      <el-table
        :data="filteredJobs"
        v-loading="loading"
        style="width: 100%"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="jobId" label="任务ID" width="200">
          <template #default="scope">
            <el-tag size="small" type="info">{{ scope.row.jobId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="jobName" label="任务名称" min-width="180">
          <template #default="scope">
            <div class="job-name-cell">
              <span class="job-name">{{ scope.row.jobName }}</span>
              <span v-if="scope.row.description" class="job-desc">{{ scope.row.description }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="jobStatus" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.jobStatus?.code)">
              {{ getStatusText(scope.row.jobStatus?.code) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数据统计" width="200">
          <template #default="scope">
            <div class="stats-cell">
              <div class="stat-item">
                <span class="stat-label">读取:</span>
                <span class="stat-value">{{ formatNumber(scope.row.readCount) }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">写入:</span>
                <span class="stat-value">{{ formatNumber(scope.row.writeCount) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              link
              size="small"
              @click="viewDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button
              v-if="scope.row.jobStatus?.code === 'RUNNING'"
              type="warning"
              link
              size="small"
              @click="stopJob(scope.row)"
            >
              停止
            </el-button>
            <el-button
              v-if="scope.row.jobStatus?.code === 'RUNNING' || scope.row.jobStatus?.code === 'PENDING'"
              type="info"
              link
              size="small"
              @click="refreshJobInfo(scope.row)"
            >
              刷新状态
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="filteredJobs.length === 0 && !loading" description="暂无任务数据" />
    </el-card>

    <el-dialog v-model="detailVisible" title="任务详情" width="700px">
      <el-descriptions :column="2" border v-if="currentJob">
        <el-descriptions-item label="任务ID">{{ currentJob.jobId }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ currentJob.jobName }}</el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag :type="getStatusType(currentJob.jobStatus?.code)">
            {{ getStatusText(currentJob.jobStatus?.code) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentJob.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentJob.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(currentJob.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ formatTime(currentJob.finishTime) }}</el-descriptions-item>
        <el-descriptions-item label="错误信息">
          <span v-if="currentJob.errorMsg" style="color: #f56c6c;">{{ currentJob.errorMsg }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider v-if="currentJob" />

      <template v-if="currentJob">
        <h4 style="margin-bottom: 15px; font-weight: 600;">配置信息</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="never">
              <template #header>
                <span>Source (数据源)</span>
              </template>
              <pre class="json-preview">{{ formatJson(currentJob.sourceConfig) }}</pre>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never">
              <template #header>
                <span>Sink (目标)</span>
              </template>
              <pre class="json-preview">{{ formatJson(currentJob.sinkConfig) }}</pre>
            </el-card>
          </el-col>
        </el-row>
      </template>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentJob && currentJob.jobStatus?.code === 'RUNNING'"
          type="warning"
          @click="stopJobInDetail"
        >
          停止任务
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import api from '@/api'

const jobs = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const detailVisible = ref(false)
const currentJob = ref(null)

let refreshInterval = null

const filteredJobs = computed(() => {
  if (!searchKeyword.value) {
    return jobs.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return jobs.value.filter(job => 
    job.jobId?.toLowerCase().includes(keyword) ||
    job.jobName?.toLowerCase().includes(keyword)
  )
})

const loadJobs = async () => {
  loading.value = true
  try {
    const res = await api.listJobs()
    jobs.value = res.data || []
  } catch (error) {
    console.error('加载任务列表失败', error)
  } finally {
    loading.value = false
  }
}

const getStatusType = (code) => {
  const types = {
    RUNNING: 'primary',
    FINISHED: 'success',
    CANCELLED: 'info',
    FAILED: 'danger',
    PENDING: 'warning'
  }
  return types[code] || 'info'
}

const getStatusText = (code) => {
  const texts = {
    RUNNING: '运行中',
    FINISHED: '已完成',
    CANCELLED: '已取消',
    FAILED: '失败',
    PENDING: '等待中'
  }
  return texts[code] || code || '未知'
}

const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  return num.toLocaleString()
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const formatJson = (obj) => {
  if (!obj) return '{}'
  try {
    return JSON.stringify(obj, null, 2)
  } catch {
    return JSON.stringify(obj)
  }
}

const tableRowClassName = ({ row }) => {
  if (row.jobStatus?.code === 'RUNNING') return 'running-row'
  if (row.jobStatus?.code === 'FAILED') return 'failed-row'
  return ''
}

const viewDetail = (row) => {
  currentJob.value = { ...row }
  detailVisible.value = true
}

const refreshJobInfo = async (row) => {
  try {
    const res = await api.getJobInfo(row.jobId)
    if (res.data) {
      Object.assign(row, res.data)
      ElMessage.success('状态已更新')
    }
  } catch (error) {
    console.error('刷新任务状态失败', error)
  }
}

const stopJob = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要停止任务 "${row.jobName}" 吗？`,
      '停止任务',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await api.stopJob(row.jobId)
    row.jobStatus = { code: 'CANCELLED', desc: '已取消' }
    row.finishTime = new Date()
    ElMessage.success('任务已停止')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停止任务失败', error)
    }
  }
}

const stopJobInDetail = async () => {
  if (!currentJob.value) return
  await stopJob(currentJob.value)
  detailVisible.value = false
}

const startAutoRefresh = () => {
  refreshInterval = setInterval(() => {
    const hasRunning = jobs.value.some(j => 
      j.jobStatus?.code === 'RUNNING' || j.jobStatus?.code === 'PENDING'
    )
    if (hasRunning) {
      loadJobs()
    }
  }, 3000)
}

onMounted(() => {
  loadJobs()
  startAutoRefresh()
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.jobs-container {
  padding: 10px;
}

.header-card {
  border: none;
  border-radius: 8px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.job-name-cell {
  display: flex;
  flex-direction: column;
}

.job-name {
  font-weight: 600;
  color: #303133;
}

.job-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.stats-cell {
  display: flex;
  flex-direction: column;
}

.stat-item {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.stat-label {
  color: #909399;
  margin-right: 5px;
}

.stat-value {
  color: #409EFF;
  font-weight: 600;
}

.json-preview {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  max-height: 200px;
  overflow-y: auto;
}

:deep(.running-row) {
  background-color: #ecf5ff !important;
}

:deep(.failed-row) {
  background-color: #fef0f0 !important;
}

:deep(.running-row:hover > td) {
  background-color: #d9ecff !important;
}

:deep(.failed-row:hover > td) {
  background-color: #fde2e2 !important;
}
</style>
