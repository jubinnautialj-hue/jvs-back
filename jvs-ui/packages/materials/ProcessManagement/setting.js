import img1 from '../../customForm/cardImg/1.png'
export default {
  formData: {
    componentName: '',
    cardImg: img1,
    codeImg: '',
    backImg: '',
    textFontSize: 18,
    textColor: '#333',
  },
  global: {
    w: 4,
    h: 3,
    boxShadow: '0 4px 26px #99999914'
  },
  customAttributes: [
    {
      prop: 'componentName',
      type: 'input',
      label: '组件名称'
    },
    {
      prop: 'cardImg',
      type: 'image',
      label: '图片'
    }
],
  formConf(formData) {
    return {}
  }
}