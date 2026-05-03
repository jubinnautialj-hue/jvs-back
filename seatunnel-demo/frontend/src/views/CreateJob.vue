<template>
  <div class="create-job-container">
    <el-steps :active="activeStep" align-center style="margin-bottom: 30px;">
      <el-step title="选择模板" />
      <el-step title="配置数据源" />
      <el-step title="配置目标" />
      <el-step title="确认提交" />
    </el-steps>

    <el-card class="step-card" shadow="never">
      <div v-if="activeStep === 0" class="step-content">
        <h3 style="margin-bottom: 20px; font-weight: 600;">选择任务模板</h3>
        <el-radio-group v-model="selectedTemplate">
          <el-row :gutter="20">
            <el-col :span="8" v-for="template in templates" :key="template.name">
              <el-radio-button :label="template" style="width: 100%;">
                <div class="template-card">
                  <div class="template-header">
                    <span class="template-icon">{{ getSourceIcon(template.source) }}</span>
                    <span class="template-arrow">→</span>
                    <span class="template-icon">{{ getSinkIcon(template.sink) }}</span>
                  </div>
                  <div class="template-name">{{ template.name }}</div>
                  <div class="template-desc">{{ template.description }}</div>
                  <el-tag :type="template.mode === 'streaming' ? 'warning' : 'primary'" size="small" style="margin-top: 10px;">
                    {{ template.mode === 'streaming' ? '实时 CDC' : '批量同步' }}
                  </el-tag>
                </div>
              </el-radio-button>
            </el-col>
          </el-row>
        </el-radio-group>

        <el-divider />

        <h4 style="margin-bottom: 15px; font-weight: 600;">或者创建自定义任务</h4>
        <el-form :model="customForm" label-width="120px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="任务名称">
                <el-input v-model="customForm.jobName" placeholder="请输入任务名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="运行模式">
                <el-select v-model="customForm.mode" placeholder="请选择运行模式" style="width: 100%;">
                  <el-option label="批量模式 (Batch)" value="batch" />
                  <el-option label="流模式 (Streaming/CDC)" value="streaming" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="任务描述">
            <el-input
              v-model="customForm.description"
              type="textarea"
              :rows="2"
              placeholder="请输入任务描述（可选）"
            />
          </el-form-item>
        </el-form>
      </div>

      <div v-if="activeStep === 1" class="step-content">
        <h3 style="margin-bottom: 20px; font-weight: 600;">配置数据源 (Source)</h3>
        
        <el-alert v-if="selectedTemplate" :type="selectedTemplate.source === 'MySQL' || selectedTemplate.source === 'PostgreSQL' ? 'info' : 'warning'" style="margin-bottom: 20px;" show-icon>
          <template #title>
            当前模板：{{ selectedTemplate.name }}
          </template>
          <template #default>
            数据源类型：<strong>{{ selectedTemplate.source }}</strong>，目标类型：<strong>{{ selectedTemplate.sink }}</strong>
            <br>
            <span v-if="selectedTemplate.category">分类：{{ selectedTemplate.category }}</span>
          </template>
        </el-alert>
        
        <el-tabs v-model="sourceType">
          <el-tab-pane label="MySQL" name="MySQL">
            <el-form :model="sourceForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库地址">
                    <el-input v-model="sourceForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口">
                    <el-input-number v-model="sourceForm.port" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库名">
                    <el-input v-model="sourceForm.database" placeholder="test" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="表名">
                    <el-input v-model="sourceForm.table" placeholder="users" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="用户名">
                    <el-input v-model="sourceForm.username" placeholder="root" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="密码">
                    <el-input v-model="sourceForm.password" type="password" placeholder="password" show-password />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="PostgreSQL" name="PostgreSQL">
            <el-form :model="sourceForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库地址">
                    <el-input v-model="sourceForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口">
                    <el-input-number v-model="sourceForm.portPg" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库名">
                    <el-input v-model="sourceForm.database" placeholder="test" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Schema">
                    <el-input v-model="sourceForm.schema" placeholder="public" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="Oracle" name="Oracle">
            <el-alert title="Oracle 数据源配置" type="info" show-icon>
              <template #default>
                配置 Oracle 连接信息，支持 SID 和 Service Name 两种模式
              </template>
            </el-alert>
          </el-tab-pane>

          <el-tab-pane label="Doris" name="Doris">
            <el-form :model="sourceForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="FE 地址">
                    <el-input v-model="sourceForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="FE HTTP 端口">
                    <el-input-number v-model="sourceForm.feHttpPort" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库名">
                    <el-input v-model="sourceForm.database" placeholder="demo" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="表名">
                    <el-input v-model="sourceForm.table" placeholder="analysis_result" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="ClickHouse" name="ClickHouse">
            <el-form :model="sourceForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="服务地址">
                    <el-input v-model="sourceForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口">
                    <el-input-number v-model="sourceForm.chPort" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库名">
                    <el-input v-model="sourceForm.database" placeholder="default" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="表名">
                    <el-input v-model="sourceForm.table" placeholder="report_summary" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="Hive" name="Hive">
            <el-form :model="sourceForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="Metastore 地址">
                    <el-input v-model="sourceForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Metastore 端口">
                    <el-input-number v-model="sourceForm.hivePort" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库名">
                    <el-input v-model="sourceForm.database" placeholder="default" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="表名">
                    <el-input v-model="sourceForm.table" placeholder="user_profile" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="Kafka" name="Kafka">
            <el-form :model="sourceForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="Kafka 地址">
                    <el-input v-model="sourceForm.kafkaBootstrap" placeholder="localhost:9092" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Topic">
                    <el-input v-model="sourceForm.kafkaTopic" placeholder="user_events" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="消费组 ID">
                    <el-input v-model="sourceForm.kafkaGroupId" placeholder="seatunnel-consumer" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="Elasticsearch" name="Elasticsearch">
            <el-form :model="sourceForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="服务地址">
                    <el-input v-model="sourceForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口">
                    <el-input-number v-model="sourceForm.esPort" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="索引名">
                    <el-input v-model="sourceForm.table" placeholder="products_index" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="Redis" name="Redis">
            <el-form :model="sourceForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="服务地址">
                    <el-input v-model="sourceForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口">
                    <el-input-number v-model="sourceForm.redisPort" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="Key 模式">
                    <el-input v-model="sourceForm.redisKeyPattern" placeholder="user:*" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div v-if="activeStep === 2" class="step-content">
        <h3 style="margin-bottom: 20px; font-weight: 600;">配置目标 (Sink)</h3>
        
        <el-alert v-if="selectedTemplate" type="info" style="margin-bottom: 20px;" show-icon>
          <template #title>
            当前模板：{{ selectedTemplate.name }}
          </template>
          <template #default>
            目标类型：<strong>{{ selectedTemplate.sink }}</strong>，从 <strong>{{ selectedTemplate.source }}</strong> 同步
          </template>
        </el-alert>
        
        <el-tabs v-model="sinkType">
          <el-tab-pane label="MySQL" name="MySQL">
            <el-form :model="sinkForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库地址">
                    <el-input v-model="sinkForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口">
                    <el-input-number v-model="sinkForm.port" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库名">
                    <el-input v-model="sinkForm.database" placeholder="test" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="表名">
                    <el-input v-model="sinkForm.table" placeholder="users_ods" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="用户名">
                    <el-input v-model="sinkForm.username" placeholder="root" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="密码">
                    <el-input v-model="sinkForm.password" type="password" placeholder="password" show-password />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="Doris" name="Doris">
            <el-form :model="sinkForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="FE 地址">
                    <el-input v-model="sinkForm.feHost" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="FE HTTP 端口">
                    <el-input-number v-model="sinkForm.feHttpPort" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库名">
                    <el-input v-model="sinkForm.database" placeholder="demo" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="表名">
                    <el-input v-model="sinkForm.table" placeholder="users_ods" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="ClickHouse" name="ClickHouse">
            <el-form :model="sinkForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="服务地址">
                    <el-input v-model="sinkForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口">
                    <el-input-number v-model="sinkForm.chPort" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库名">
                    <el-input v-model="sinkForm.database" placeholder="default" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="表名">
                    <el-input v-model="sinkForm.table" placeholder="orders_ods" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="PostgreSQL" name="PostgreSQL">
            <el-form :model="sinkForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库地址">
                    <el-input v-model="sinkForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口">
                    <el-input-number v-model="sinkForm.portPg" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="数据库名">
                    <el-input v-model="sinkForm.database" placeholder="test" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="表名">
                    <el-input v-model="sinkForm.table" placeholder="analysis_result" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="Elasticsearch" name="Elasticsearch">
            <el-form :model="sinkForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="服务地址">
                    <el-input v-model="sinkForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口">
                    <el-input-number v-model="sinkForm.esPort" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="索引名">
                    <el-input v-model="sinkForm.table" placeholder="products_index" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="Redis" name="Redis">
            <el-form :model="sinkForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="服务地址">
                    <el-input v-model="sinkForm.host" placeholder="localhost">
                      <template #prepend>Host</template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="端口">
                    <el-input-number v-model="sinkForm.redisPort" :min="1" :max="65535" style="width: 100%;" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="Key 前缀">
                    <el-input v-model="sinkForm.redisKeyPattern" placeholder="result:*" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="Kafka" name="Kafka">
            <el-form :model="sinkForm" label-width="140px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="Kafka 地址">
                    <el-input v-model="sinkForm.kafkaBootstrap" placeholder="localhost:9092" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Topic">
                    <el-input v-model="sinkForm.kafkaTopic" placeholder="processed_events" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="Hive" name="Hive">
            <el-alert title="Hive 数据仓库" type="info" show-icon>
              <template #default>
                配置 Hive Metastore 和 HDFS 路径信息
              </template>
            </el-alert>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div v-if="activeStep === 3" class="step-content">
        <h3 style="margin-bottom: 20px; font-weight: 600;">确认配置</h3>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">{{ currentJobName }}</el-descriptions-item>
          <el-descriptions-item label="运行模式">
            {{ currentMode === 'streaming' ? '流模式 (CDC实时同步)' : '批量模式' }}
          </el-descriptions-item>
          <el-descriptions-item label="数据源">{{ sourceType }}</el-descriptions-item>
          <el-descriptions-item label="目标">{{ sinkType }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ currentDescription || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <h4 style="margin-bottom: 15px; font-weight: 600;">SeaTunnel 配置预览</h4>
        <el-card shadow="never" style="background: #f5f7fa;">
          <pre class="config-preview">{{ previewConfig }}</pre>
        </el-card>

        <el-divider />

        <el-alert title="提示" type="info" show-icon>
          <template #default>
            点击"提交任务"按钮，将创建并启动一个 SeaTunnel 数据同步任务。任务将在模拟环境中运行，您可以在任务管理页面查看状态。
          </template>
        </el-alert>
      </div>

      <div class="step-actions">
        <el-button v-if="activeStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="activeStep < 3" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="activeStep === 3" type="primary" :loading="submitting" @click="submitJob">
          提交任务
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

const router = useRouter()
const route = useRoute()

const activeStep = ref(0)
const submitting = ref(false)

const templates = ref([])
const selectedTemplate = ref(null)

const sourceType = ref('MySQL')
const sinkType = ref('Doris')

const customForm = ref({
  jobName: '',
  mode: 'batch',
  description: ''
})

const sourceForm = ref({
  host: 'localhost',
  port: 3306,
  portPg: 5432,
  chPort: 8123,
  hivePort: 9083,
  esPort: 9200,
  redisPort: 6379,
  feHttpPort: 8030,
  database: 'test',
  table: 'users',
  schema: 'public',
  username: 'root',
  password: '',
  kafkaBootstrap: 'localhost:9092',
  kafkaTopic: 'user_events',
  kafkaGroupId: 'seatunnel-consumer',
  redisKeyPattern: 'user:*'
})

const sinkForm = ref({
  host: 'localhost',
  port: 8123,
  feHost: 'localhost',
  feHttpPort: 8030,
  database: 'demo',
  table: 'users_ods',
  username: 'root',
  password: '',
  portPg: 5432,
  chPort: 8123,
  esPort: 9200,
  redisPort: 6379,
  kafkaBootstrap: 'localhost:9092',
  kafkaTopic: 'processed_events',
  redisKeyPattern: 'result:*'
})

const fromConnector = ref(false)
const connectorName = ref('')
const connectorRole = ref('')

const currentJobName = computed(() => {
  if (selectedTemplate.value) {
    return selectedTemplate.value.name
  }
  return customForm.value.jobName || '未命名任务'
})

const currentMode = computed(() => {
  if (selectedTemplate.value) {
    return selectedTemplate.value.mode
  }
  return customForm.value.mode
})

const currentDescription = computed(() => {
  if (selectedTemplate.value) {
    return selectedTemplate.value.description
  }
  return customForm.value.description
})

const previewConfig = computed(() => {
  const config = {
    env: {
      'job.mode': currentMode.value === 'streaming' ? 'STREAMING' : 'batch',
      parallelism: 5
    },
    source: [
      {
        plugin_name: sourceType.value,
        url: `jdbc:${sourceType.value.toLowerCase()}://${sourceForm.value.host}:${sourceType.value === 'PostgreSQL' ? sourceForm.value.portPg : sourceForm.value.port}/${sourceForm.value.database}`,
        table: sourceForm.value.table
      }
    ],
    transform: [
      {
        plugin_name: 'FieldMapper',
        field_mapper: {}
      }
    ],
    sink: [
      {
        plugin_name: sinkType.value,
        table: sinkForm.value.table
      }
    ]
  }
  return JSON.stringify(config, null, 2)
})

const loadTemplates = async () => {
  try {
    const res = await api.getTemplates()
    templates.value = res.data || []
    if (templates.value.length > 0) {
      selectedTemplate.value = templates.value[0]
    }
  } catch (error) {
    console.error('加载模板失败', error)
  }
}

const getSourceIcon = (source) => {
  const icons = {
    MySQL: '🐬',
    PostgreSQL: '🐘',
    Oracle: '🔶',
    Doris: '🐚',
    ClickHouse: '🏠',
    Hive: '🐝',
    Kafka: '📨',
    Elasticsearch: '🔍',
    Redis: '🗄️'
  }
  return icons[source] || '📊'
}

const getSinkIcon = (sink) => {
  const icons = {
    Doris: '🐚',
    ClickHouse: '🏠',
    Hive: '🐝',
    MySQL: '🐬',
    PostgreSQL: '🐘',
    Oracle: '🔶',
    Elasticsearch: '🔍',
    Redis: '🗄️',
    Kafka: '📨'
  }
  return icons[sink] || '📦'
}

const nextStep = () => {
  if (activeStep.value < 3) {
    if (activeStep.value === 0 && selectedTemplate.value) {
      sourceType.value = selectedTemplate.value.source
      sinkType.value = selectedTemplate.value.sink
      customForm.value.mode = selectedTemplate.value.mode
      customForm.value.description = selectedTemplate.value.description
      if (!customForm.value.jobName) {
        customForm.value.jobName = selectedTemplate.value.name
      }
    }
    activeStep.value++
  }
}

const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

const submitJob = async () => {
  submitting.value = true
  try {
    const jobData = {
      jobName: currentJobName.value,
      description: currentDescription.value,
      env: {
        'job.mode': currentMode.value === 'streaming' ? 'STREAMING' : 'batch',
        parallelism: 5
      },
      source: [
        {
          plugin_name: sourceType.value,
          host: sourceForm.value.host,
          port: sourceType.value === 'PostgreSQL' ? sourceForm.value.portPg : sourceForm.value.port,
          database: sourceForm.value.database,
          table: sourceForm.value.table
        }
      ],
      sink: [
        {
          plugin_name: sinkType.value,
          database: sinkForm.value.database,
          table: sinkForm.value.table
        }
      ]
    }

    const res = await api.submitJob(jobData)
    ElMessage.success(`任务提交成功！任务ID: ${res.data}`)
    router.push('/jobs')
  } catch (error) {
    console.error('提交任务失败', error)
  } finally {
    submitting.value = false
  }
}

const processRouteParams = () => {
  const { connectorName: cName, role } = route.query
  
  if (cName && role) {
    fromConnector.value = true
    connectorName.value = cName
    connectorRole.value = role
    
    if (role === 'source') {
      sourceType.value = cName
      customForm.value.jobName = `从 ${cName} 同步任务`
      customForm.value.description = `数据源：${cName}`
      ElMessage.info(`已预设 ${cName} 作为数据源，请选择目标平台并配置连接信息`)
    } else if (role === 'sink') {
      sinkType.value = cName
      customForm.value.jobName = `同步到 ${cName} 任务`
      customForm.value.description = `目标：${cName}`
      ElMessage.info(`已预设 ${cName} 作为目标，请选择数据源并配置连接信息`)
    }
  }
}

onMounted(() => {
  loadTemplates()
  processRouteParams()
})
</script>

<style scoped>
.create-job-container {
  padding: 10px;
}

.step-card {
  border-radius: 8px;
  min-height: 500px;
}

.step-content {
  padding: 10px;
}

.template-card {
  padding: 20px;
  text-align: center;
  min-height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.template-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.template-icon {
  font-size: 36px;
}

.template-arrow {
  font-size: 24px;
  color: #409EFF;
  margin: 0 15px;
}

.template-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.template-desc {
  font-size: 12px;
  color: #909399;
}

.step-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.config-preview {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
