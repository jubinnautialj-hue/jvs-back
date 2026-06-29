export default [
  {
    path: '/customview/index',
    name: '自定义页面',
    component: () => import( /* webpackChunkName: "page" */ './index')
  },
]
