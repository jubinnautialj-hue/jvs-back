import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Overview',
    component: () => import('@/views/Overview.vue')
  },
  {
    path: '/jobs',
    name: 'Jobs',
    component: () => import('@/views/Jobs.vue')
  },
  {
    path: '/create',
    name: 'CreateJob',
    component: () => import('@/views/CreateJob.vue')
  },
  {
    path: '/connectors',
    name: 'Connectors',
    component: () => import('@/views/Connectors.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
