import assembly from './assembly'
import formatKey from './format'
export default class MTab extends assembly{
  constructor (
    type='tab',
    label='选项卡',
    span=24,
    prop='tab'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.span = 24
    this.showFrom = ['label', 'span', 'prop','url','datatype','dicData','jurisdiction','sqlType', 'option']
    // 数据类型 option 和 url
    this.datatype = 'option'
    // 请求接口
    this.url = ''
    // 下拉框的选项
    this.dicData = [{
      label: '选项一',
      name: 'first'
    }, {
      label: '选项二',
      name: 'second'
    }, {
      label: '选项三',
      name: 'third'
    }]

    this.activeName = this.dicData[0].name
    this.column = {} // 选项对应的表单项
    // 校验
    this.rules = []
    this.showJurisdiction = ['所有用户']
    this.sqlType = 'object'
    this.linkbind = ''
    this.detachData = true
  }
  addcolumn (key,data) {
    if(!this.column[key]) {
      this.column[key] = []
    }
    data.parentType = this.type
    data.parentKey = `${this.parentKey ? (this.parentKey + '.') : ''}${this.prop}.${key}`
    if(data.type == 'flowTable') {
      data.span = 24
      data.tableColumn.filter(ci => {
        ci.parentKey = (data.parentKey+'.'+data.prop)
      })
    }
    this.column[key].push(data)
  }
  deletecolumn (key,prop) {
    this.column[key] = this.column[key].filter(item => item.prop != prop)
    // console.log(this.column)
  }
  deletedicData (index) {
    this.dicData.splice(index, 1)
  }
  adddicData () {
    let obj = {}
    let key = 'newValue' + this.dicData.length
    obj[key] = []
    this.column[key] = obj
    this.dicData.push({label: '新的选择', value: ('newValue' + this.dicData.length)})
  }
  handleClick(activeName) {
    // console.log(activeName)
    if(activeName || activeName === 0 || activeName === false) {
      this.activeName = activeName
    }
  }
}