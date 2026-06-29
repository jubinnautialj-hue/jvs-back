export default [
	{
		path: '/flowable',
		name: '流程设计',
		component: () =>
            import ( /* webpackChunkName: "page" */ './views/index'),
		meta: {
			keepAlive: true,
			isTab: false,
			isAuth: false
		}
	},
	{
		path: '/processDesign',
		name: '流程设计',
		component: () =>
            import ( /* webpackChunkName: "page" */ './views/design/index'),
		meta: {
			keepAlive: true,
			isTab: false,
			isAuth: false
		}
	},
	{
		path: '/tasklist',
		name: '待办任务',
		component: () =>
            import ( /* webpackChunkName: "page" */ './views/taskList'),
		meta: {
			keepAlive: true,
			isTab: false,
			isAuth: false
		}
	},
	{
		path: '/taskRecord',
		name: '审核记录',
		component: () =>
            import ( /* webpackChunkName: "page" */ './views/taskRecord'),
		meta: {
			keepAlive: true,
			isTab: false,
			isAuth: false
		}
	},
	{
		path: '/applylist',
		name: '我的申请',
		component: () =>
            import ( /* webpackChunkName: "page" */ './views/apply'),
		meta: {
			keepAlive: true,
			isTab: false,
			isAuth: false
		}
	},
	{
		path: '/entry',
		name: '申请入口',
		component: () =>
            import ( /* webpackChunkName: "page" */ './views/entry'),
		meta: {
			keepAlive: true,
			isTab: false,
			isAuth: false
		}
	},
	{
		path: '/cclist',
		name: '抄送列表',
		component: () =>
            import ( /* webpackChunkName: "page" */ './views/ccList'),
		meta: {
			keepAlive: true,
			isTab: false,
			isAuth: false
		}
	},
	{
		path: '/proxylist',
		name: '代理列表',
		component: () =>
            import ( /* webpackChunkName: "page" */ './views/proxyList'),
		meta: {
			keepAlive: true,
			isTab: false,
			isAuth: false
		}
	},
	{
		path: '/processmanage',
		name: '流程管理',
		component: () => import ( /* webpackChunkName: "page" */ './views/processManage'),
		meta: {
			keepAlive: true,
			isTab: false,
			isAuth: false
		}
	}
]
