const base= ''
export default [
  {
    path: base+'/print/design',
    name: '打印设计',
    component: () =>
      import( /* webpackChunkName: "page" */ './design')
  },
  {
    path: base+'/print/show',
    name: '打印',
    component: () =>
      import( /* webpackChunkName: "page" */ './print'),
      meta: {
        keepAlive: true,
        isTab: true,
        isAuth: false
      }
  },
  {
    path: base+'/print/fields',
    name: '打印',
    component: () =>
      import( /* webpackChunkName: "page" */ './printField')
  }
]
