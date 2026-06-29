export default {
  formData: {
    titleTxt: '链接导航'
  },
  global:{
    w: 8,
    h: 6
  },
  formConf(formData) {
    return {
      linkList: {
        label: '导航配置',
        type: 'listMap',
        column: [
          {
            label: '显示图片',
            prop: 'icon',
            type: 'imageSelector'
          },
          {
            label: '链接地址',
            prop: 'url',
            type: 'textarea'
          },
          {
            label: '导航名称',
            prop: 'name',
            type: 'input'
          }
        ]
      }
    }
  }
}