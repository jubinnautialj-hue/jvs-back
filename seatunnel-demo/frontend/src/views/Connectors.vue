<template>
  <div class="connectors-container">
    <el-card shadow="never" style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>SeaTunnel 连接器</span>
          <el-radio-group v-model="connectorType" size="small">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="source">数据源 (Source)</el-radio-button>
            <el-radio-button label="sink">目标 (Sink)</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <p style="color: #606266; margin-bottom: 20px;">
        SeaTunnel 提供了丰富的连接器，支持与各种数据源和数据目标进行数据同步。
        下面展示了一些常用的连接器及其功能说明。
      </p>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="8" v-for="connector in filteredConnectors" :key="connector.name">
        <el-card class="connector-card" shadow="hover">
          <div class="connector-header">
            <span class="connector-icon">{{ connector.icon }}</span>
            <div>
              <h4 class="connector-name">{{ connector.name }}</h4>
              <el-tag :type="connector.type === 'source' ? 'primary' : 'success'" size="small">
                {{ connector.type === 'source' ? '数据源' : '目标' }}
              </el-tag>
            </div>
          </div>
          <p class="connector-desc">{{ connector.description }}</p>
          
          <el-divider />
          
          <h5 style="margin-bottom: 10px; font-weight: 600;">核心特性</h5>
          <div class="features">
            <el-tag v-for="feature in connector.features" :key="feature" size="small" style="margin: 3px;">
              {{ feature }}
            </el-tag>
          </div>

          <div class="connector-actions">
            <el-button type="primary" link size="small" @click="showConnectorDetail(connector)">
              查看详情
            </el-button>
            <el-button type="success" link size="small" @click="quickCreate(connector)">
              快速创建任务
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>Source 架构说明</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(step, index) in sourceSteps"
              :key="index"
              :type="step.type"
              :timestamp="step.timestamp"
            >
              <h4>{{ step.title }}</h4>
              <p style="color: #606266; font-size: 13px;">{{ step.content }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>Sink 架构说明</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(step, index) in sinkSteps"
              :key="index"
              :type="step.type"
              :timestamp="step.timestamp"
            >
              <h4>{{ step.title }}</h4>
              <p style="color: #606266; font-size: 13px;">{{ step.content }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="detailVisible" title="连接器详情" width="600px">
      <div v-if="selectedConnector">
        <div class="detail-header">
          <span class="detail-icon">{{ selectedConnector.icon }}</span>
          <div>
            <h3>{{ selectedConnector.name }}</h3>
            <el-tag :type="selectedConnector.type === 'source' ? 'primary' : 'success'" size="small">
              {{ selectedConnector.type === 'source' ? '数据源 (Source)' : '目标 (Sink)' }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <h4 style="margin-bottom: 10px; font-weight: 600;">描述</h4>
        <p style="color: #606266; line-height: 1.8;">{{ selectedConnector.description }}</p>

        <el-divider />

        <h4 style="margin-bottom: 15px; font-weight: 600;">支持的功能</h4>
        <div class="feature-detail">
          <el-tag
            v-for="feature in selectedConnector.features"
            :key="feature"
            :type="selectedConnector.type === 'source' ? 'primary' : 'success'"
            size="large"
            effect="dark"
            style="margin: 5px;"
          >
            {{ feature }}
          </el-tag>
        </div>

        <el-divider />

        <h4 style="margin-bottom: 15px; font-weight: 600;">配置示例</h4>
        <el-card shadow="never" style="background: #f5f7fa;">
          <pre class="config-sample">{{ getConfigSample(selectedConnector) }}</pre>
        </el-card>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="quickCreate(selectedConnector)">
          使用此连接器创建任务
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

const router = useRouter()

const connectorType = ref('all')
const connectors = ref([])
const detailVisible = ref(false)
const selectedConnector = ref(null)

const filteredConnectors = computed(() => {
  if (connectorType.value === 'all') {
    return connectors.value
  }
  return connectors.value.filter(c => c.type === connectorType.value)
})

const sourceSteps = ref([
  {
    title: '连接数据源',
    content: '根据配置的连接信息，与数据源建立连接',
    type: 'primary',
    timestamp: 'Step 1'
  },
  {
    title: '读取数据',
    content: '从数据源读取数据，支持全量读取和增量读取',
    type: 'success',
    timestamp: 'Step 2'
  },
  {
    title: 'CDC 实时捕获 (可选)',
    content: '对于支持 CDC 的数据源，可实时捕获数据变更事件',
    type: 'warning',
    timestamp: 'Step 3'
  },
  {
    title: '发送到 SeaTunnel',
    content: '将读取的数据发送到 SeaTunnel 引擎进行处理',
    type: 'info',
    timestamp: 'Step 4'
  }
])

const sinkSteps = ref([
  {
    title: '接收数据',
    content: '从 SeaTunnel 引擎接收处理后的数据',
    type: 'primary',
    timestamp: 'Step 1'
  },
  {
    title: '数据转换',
    content: '根据目标数据格式进行必要的数据类型转换',
    type: 'success',
    timestamp: 'Step 2'
  },
  {
    title: '批量写入',
    content: '采用批量写入策略，优化写入性能',
    type: 'warning',
    timestamp: 'Step 3'
  },
  {
    title: '确认写入',
    content: '确保数据写入成功，并记录写入统计信息',
    type: 'info',
    timestamp: 'Step 4'
  }
])

const loadConnectors = async () => {
  try {
    const res = await api.getConnectors()
    connectors.value = res.data || []
  } catch (error) {
    console.error('加载连接器失败', error)
    connectors.value = [
      {
        name: 'MySQL',
        type: 'source',
        icon: '🐬',
        description: '最流行的开源关系型数据库，广泛应用于各类 Web 应用。SeaTunnel 支持 MySQL Binlog CDC 实时同步。',
        features: ['全量同步', 'CDC 实时同步', 'Binlog 解析', '增量读取']
      },
      {
        name: 'PostgreSQL',
        type: 'source',
        icon: '🐘',
        description: '功能强大的开源对象关系型数据库系统，支持复杂查询和事务处理。',
        features: ['全量同步', 'WAL CDC', '逻辑复制', 'JSON 支持']
      },
      {
        name: 'Oracle',
        type: 'source',
        icon: '🔶',
        description: '企业级关系型数据库，具有高可用性、高安全性和高性能。',
        features: ['全量同步', 'LogMiner', 'RAC 支持', '事务支持']
      },
      {
        name: 'Doris',
        type: 'sink',
        icon: '🐚',
        description: '现代化的 MPP 分析型数据库，适用于多维数据分析和实时报表场景。',
        features: ['批量写入', '实时写入', '主键模型', '聚合模型']
      },
      {
        name: 'ClickHouse',
        type: 'sink',
        icon: '🏠',
        description: '用于联机分析(OLAP)的列式数据库管理系统，具有极高的查询性能。',
        features: ['批量写入', '分区支持', '物化视图', '低延迟']
      },
      {
        name: 'Hive',
        type: 'sink',
        icon: '🐝',
        description: '基于 Hadoop 的数据仓库工具，支持使用 SQL 查询存储在 HDFS 中的数据。',
        features: ['批量写入', '分区表', 'ORC/Parquet', 'ACID 支持']
      },
      {
        name: 'Elasticsearch',
        type: 'sink',
        icon: '🔍',
        description: '分布式搜索和分析引擎，适用于全文搜索、日志分析等场景。',
        features: ['批量写入', '实时索引', '文档更新', '聚合查询']
      }
    ]
  }
}

const showConnectorDetail = (connector) => {
  selectedConnector.value = connector
  detailVisible.value = true
}

const quickCreate = (connector) => {
  detailVisible.value = false
  router.push('/create')
  ElMessage.info(`已选择 ${connector.name} 连接器，进入任务创建页面`)
}

const getConfigSample = (connector) => {
  if (connector.type === 'source') {
    if (connector.name === 'MySQL') {
      return `source {
  MySQL {
    url = "jdbc:mysql://localhost:3306/test"
    user = "root"
    password = "password"
    table = "users"
  }
}`
    }
    if (connector.name === 'PostgreSQL') {
      return `source {
  PostgreSQL {
    url = "jdbc:postgresql://localhost:5432/test"
    user = "postgres"
    password = "password"
    table = "public.orders"
  }
}`
    }
  } else {
    if (connector.name === 'Doris') {
      return `sink {
  Doris {
    fenodes = "localhost:8030"
    user = "root"
    password = "password"
    database = "demo"
    table = "users_ods"
  }
}`
    }
    if (connector.name === 'ClickHouse') {
      return `sink {
  ClickHouse {
    host = "localhost"
    port = 8123
    database = "default"
    table = "orders_ods"
  }
}`
    }
  }
  return `# ${connector.name} 配置示例
# 请参考官方文档获取完整配置`
}

onMounted(() => {
  loadConnectors()
})
</script>

<style scoped>
.connectors-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.connector-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.connector-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.connector-icon {
  font-size: 48px;
  margin-right: 15px;
}

.connector-name {
  margin: 0 0 8px 0;
  color: #303133;
}

.connector-desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  margin: 0;
}

.features {
  display: flex;
  flex-wrap: wrap;
}

.connector-actions {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
  display: flex;
  justify-content: flex-start;
  gap: 10px;
}

.detail-header {
  display: flex;
  align-items: center;
}

.detail-icon {
  font-size: 64px;
  margin-right: 20px;
}

.detail-header h3 {
  margin: 0 0 8px 0;
}

.feature-detail {
  display: flex;
  flex-wrap: wrap;
}

.config-sample {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  color: #303133;
}
</style>
