import Layout from "@/page/main/index/";
export default [
  {
    path: "/wel",
    component: Layout,
    redirect: "/wel/index",
    meta: {
      isAuth: false,
    },
    children: [
      {
        path: "index",
        name: "首页",
        closable: true,
        meta: {
          isAuth: false,
        },
        component: () =>
          import(/* webpackChunkName: "views" */ "@/page/main/wel"),
      },
    ],
  },
  {
    path: "/info",
    component: Layout,
    redirect: "/info/index",
    children: [
      {
        path: "index",
        name: "个人信息",
        component: () =>
          import(/* webpackChunkName: "page" */ "@/views/main/user/info"),
      },
    ],
  },
  {
    path: "/userinfo",
    name: "个人信息",
    component: () =>
      import(/* webpackChunkName: "page" */ "@/views/main/user/index"),
    meta: {
      keepAlive: true,
      isTab: true,
      isAuth: false,
    },
  },
  {
    path: "/templateManage",
    name: "模板列表",
    component: () =>
      import(/* webpackChunkName: "page" */ "@/components/template/manage"),
    meta: {
      keepAlive: true,
      isTab: false,
      isAuth: false,
    },
  },
  {
    path: "/applicationInfo",
    name: "应用详情",
    component: () =>
      import(/* webpackChunkName: "page" */ "@/page/main/index/index"),
    meta: {
      keepAlive: true,
      isTab: false,
      isAuth: false,
    },
  },
  {
    path: "/ruleList",
    name: "业务逻辑",
    component: () => import(/* webpackChunkName: "page" */ "@/page/rule/index"),
    meta: {
      keepAlive: true,
      isTab: false,
      isAuth: false,
    },
  },
  // 新增的示例页面路由
  {
    path: "/process",
    component: Layout,
    redirect: "/process/index",
    children: [
      {
        path: "index",
        name: "流程审批",
        meta: {
          isAuth: false,
        },
        component: () =>
          import(
            /* webpackChunkName: "views" */ "@/views/flowable/views/taskProcess"
          ),
      },
    ],
  },
  // iframe接收主框架数据示例
  {
    path: "/iframe/child",
    name: "子iframe",
    component: () =>
      import(/* webpackChunkName: "page" */ "@/components/iframe/child"),
  },
];
