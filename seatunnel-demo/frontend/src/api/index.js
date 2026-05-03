import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export const api = {
  getOverview() {
    return request.get('/demo/overview')
  },

  getConnectors() {
    return request.get('/demo/connectors')
  },

  getTemplates() {
    return request.get('/demo/templates')
  },

  quickStart(templateName) {
    return request.post(`/demo/quick-start/${templateName}`)
  },

  submitJob(jobData) {
    return request.post('/seatunnel/job/submit', jobData)
  },

  getJobInfo(jobId) {
    return request.get(`/seatunnel/job/${jobId}`)
  },

  stopJob(jobId) {
    return request.post('/seatunnel/job/stop', { jobId })
  },

  listJobs() {
    return request.get('/seatunnel/jobs')
  }
}

export default api
