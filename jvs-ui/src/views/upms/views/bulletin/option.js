import Vue from "vue"
export const tableOption={
  page: true,
  addBtn: false,
  editBtn: false,
  canncal: false,
  viewBtn: false,
  delBtn: Vue.prototype.$permissionMatch("jvs_bulletin_delete"),
  // align: 'center',
  // menuAlign: 'center',
  search: false,
  cancal: false,
  showOverflow: true,
  column: [
    {
      label: '标题',
      search: true,
      prop: 'title',
    },
    {
      label: '内容',
      prop: 'content',
      type: 'textarea'
    },
    {
      label: '状态',
      prop: 'publish',
      addDisplay: false,
      editDisplay: false,
      viewDisplay: false,
      slot: true,
    },
    {
      label: "类型",
      prop: 'type',
      dicData: [
        {label: '小程序', value: 'MOBILE'},
        {label: 'PC', value: 'PC'},
      ]
    },
    {
      label: "终端类型",
      prop: 'appKeys',
      dicData: [],
      props: {
        label: 'name',
        value: 'appKey'
      }
    },
    {
      label: "生效时间",
      prop: "startTime",
      addDisplay: false,
      editDisplay: false,
      // sort: true
    },
    {
      label: "失效时间",
      prop: "endTime",
    },
    {
      label: '创建时间',
      prop: 'createTime'
    }
  ]
}
