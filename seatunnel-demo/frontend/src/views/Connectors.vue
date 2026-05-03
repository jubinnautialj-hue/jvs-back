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
              <el-tag :type="getConnectorTypeTagType(connector.type)" size="small">
                {{ getConnectorTypeText(connector.type) }}
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
            <el-tag :type="getConnectorTypeTagType(selectedConnector.type)" size="small">
              {{ getConnectorTypeText(selectedConnector.type) }}
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
            :type="getConnectorTypeTagType(selectedConnector.type)"
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
        <el-button type="primary" @click="quickCreateFromDetail(selectedConnector)">
          使用此连接器创建任务
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleSelectVisible" title="选择连接器用途" width="500px">
      <div v-if="pendingConnector">
        <el-alert :type="getConnectorTypeTagType(pendingConnector.type)" show-icon>
          <template #title>
            连接器：{{ pendingConnector.name }}
          </template>
          <template #default>
            <span v-if="pendingConnector.type === 'both'">
              此连接器<strong>既可作为数据源 (Source)</strong>，也可<strong>作为目标 (Sink)</strong>
            </span>
            <span v-else-if="pendingConnector.type === 'source'">
              此连接器<strong>只能作为数据源 (Source)</strong>
            </span>
            <span v-else-if="pendingConnector.type === 'sink'">
              此连接器<strong>只能作为目标 (Sink)</strong>
            </span>
          </template>
        </el-alert>

        <div style="margin-top: 30px;">
          <el-radio-group v-model="selectedRole">
            <el-radio
              v-if="pendingConnector.type === 'both' || pendingConnector.type === 'source'"
              value="source"
              style="display: block; margin-bottom: 20px; padding: 15px; background: #f5f7fa; border-radius: 8px;"
            >
              <span style="font-size: 16px; font-weight: bold;">📥 作为数据源 (Source)</span>
              <p style="margin: 5px 0 0 20px; color: #909399; font-size: 13px;">
                从 {{ pendingConnector.name }} 读取数据，同步到其他平台
              </p>
            </el-radio>
            <el-radio
              v-if="pendingConnector.type === 'both' || pendingConnector.type === 'sink'"
              value="sink"
              style="display: block; padding: 15px; background: #f5f7fa; border-radius: 8px;"
            >
              <span style="font-size: 16px; font-weight: bold;">📤 作为目标 (Sink)</span>
              <p style="margin: 5px 0 0 20px; color: #909399; font-size: 13px;">
                将数据从其他平台写入 {{ pendingConnector.name }}
              </p>
            </el-radio>
          </el-radio-group>
        </div>
      </div>

      <template #footer>
        <el-button @click="roleSelectVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCreateTask">
          确认并创建任务
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

const roleSelectVisible = ref(false)
const pendingConnector = ref(null)
const selectedRole = ref('source')

const filteredConnectors = computed(() => {
  if (connectorType.value === 'all') {
    return connectors.value
  }
  return connectors.value.filter(c => {
    if (c.type === 'both') {
      return true
    }
    return c.type === connectorType.value
  })
})

const getConnectorTypeText = (type) => {
  switch (type) {
    case 'source':
      return '数据源'
    case 'sink':
      return '目标'
    case 'both':
      return '双向支持'
    default:
      return type
  }
}

const getConnectorTypeTagType = (type) => {
  switch (type) {
    case 'source':
      return 'primary'
    case 'sink':
      return 'success'
    case 'both':
      return 'warning'
    default:
      return 'info'
  }
}

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
  pendingConnector.value = connector
  
  if (connector.type === 'source') {
    selectedRole.value = 'source'
    roleSelectVisible.value = true
  } else if (connector.type === 'sink') {
    selectedRole.value = 'sink'
    roleSelectVisible.value = true
  } else {
    selectedRole.value = 'source'
    roleSelectVisible.value = true
  }
}

const quickCreateFromDetail = (connector) => {
  detailVisible.value = false
  quickCreate(connector)
}

const confirmCreateTask = () => {
  roleSelectVisible.value = false
  
  const params = {
    connectorName: pendingConnector.value.name,
    role: selectedRole.value
  }
  
  ElMessage.info(`已选择 ${pendingConnector.value.name} 作为 ${selectedRole.value === 'source' ? '数据源' : '目标'}`)
  router.push({
    path: '/create',
    query: params
  })
}

const getConfigSample = (connector) => {
  const name = connector.name
  const type = connector.type

  if (type === 'both') {
    if (name === 'MySQL') {
      return `# MySQL 既可以作为 Source，也可以作为 Sink

# ===== 作为 Source (数据源) =====
source {
  MySQL {
    url = "jdbc:mysql://localhost:3306/test"
    user = "root"
    password = "password"
    table = "users"
  }
}

# ===== 作为 Sink (数据目标) =====
sink {
  MySQL {
    url = "jdbc:mysql://localhost:3306/target"
    user = "root"
    password = "password"
    table = "users_result"
  }
}

# 注意：Doris → MySQL 是常见的反向同步场景
# 用于将分析结果回写到业务库`
    }
    if (name === 'Doris') {
      return `# Doris 既可以作为 Source，也可以作为 Sink

# ===== 作为 Sink (数据目标) - 常见场景 =====
sink {
  Doris {
    fenodes = "localhost:8030"
    user = "root"
    password = "password"
    database = "demo"
    table = "users_ods"
  }
}

# ===== 作为 Source (数据源) - 反向同步 =====
source {
  Doris {
    fenodes = "localhost:8030"
    user = "root"
    password = "password"
    database = "demo"
    table = "analysis_result"
  }
}

# 反向同步场景：Doris → MySQL
# 用于将聚合分析结果回写到业务 MySQL`
    }
    if (name === 'ClickHouse') {
      return `# ClickHouse 既可以作为 Source，也可以作为 Sink

# ===== 作为 Sink (数据目标) =====
sink {
  ClickHouse {
    host = "localhost"
    port = 8123
    database = "default"
    table = "orders_ods"
  }
}

# ===== 作为 Source (数据源) - 反向同步 =====
source {
  ClickHouse {
    host = "localhost"
    port = 8123
    database = "default"
    table = "report_summary"
  }
}`
    }
    if (name === 'PostgreSQL') {
      return `# PostgreSQL 既可以作为 Source，也可以作为 Sink

# ===== 作为 Source (数据源) =====
source {
  PostgreSQL {
    url = "jdbc:postgresql://localhost:5432/test"
    user = "postgres"
    password = "password"
    table = "public.orders"
  }
}

# ===== 作为 Sink (数据目标) =====
sink {
  PostgreSQL {
    url = "jdbc:postgresql://localhost:5432/target"
    user = "postgres"
    password = "password"
    table = "public.analysis_result"
  }
}`
    }
    if (name === 'Kafka') {
      return `# Kafka 既可以作为 Source，也可以作为 Sink

# ===== 作为 Source (消费消息) =====
source {
  Kafka {
    bootstrap.servers = "localhost:9092"
    topic = "user_events"
    group.id = "seatunnel-consumer"
  }
}

# ===== 作为 Sink (生产消息) =====
sink {
  Kafka {
    bootstrap.servers = "localhost:9092"
    topic = "processed_events"
  }
}`
    }
    if (name === 'Redis') {
      return `# Redis 既可以作为 Source，也可以作为 Sink

# ===== 作为 Source (读取缓存) =====
source {
  Redis {
    host = "localhost"
    port = 6379
    key_pattern = "user:*"
  }
}

# ===== 作为 Sink (写入缓存) =====
sink {
  Redis {
    host = "localhost"
    port = 6379
    data_type = "key"
  }
}`
    }
  }

  if (type === 'source') {
    if (name === 'MySQL') {
      return `source {
  MySQL {
    url = "jdbc:mysql://localhost:3306/test"
    user = "root"
    password = "password"
    table = "users"
  }
}`
    }
    if (name === 'PostgreSQL') {
      return `source {
  PostgreSQL {
    url = "jdbc:postgresql://localhost:5432/test"
    user = "postgres"
    password = "password"
    table = "public.orders"
  }
}`
    }
  }

  if (type === 'sink') {
    if (name === 'Doris') {
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
    if (name === 'ClickHouse') {
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

  return `# ${name} 配置示例
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
