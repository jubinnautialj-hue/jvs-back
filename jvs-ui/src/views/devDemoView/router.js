export default [
  {
    path: '/devDemoView',
    name: '源码开发示例',
    component: () => import( /* webpackChunkName: "page" */ './views/index')
  },
  {
    path: '/devDemoViewInfo',
    name: '源码开发示例详情页',
    component: () => import( /* webpackChunkName: "page" */ './views/info')
  },
]
