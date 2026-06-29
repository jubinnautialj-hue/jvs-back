
import assembly from './assembly'
import formatKey from './format'
export default class MConnectForm extends assembly{
  constructor (
    type='connectForm',
    label='关联表单',
    span=24,
    prop='connetForm'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.showFrom = ['label', 'span', 'prop','collapsetags','placeholder',
      'clearable','disabled','filterable','url','defaultValue','dicData',// 'multiple'
    ]
    this.hasChildren = false
    this.multiple = false
    this.collapsetags = false
    this.disabled = false
    this.filterable = true
    this.allowcreate = false
    this.placeholder = '请选择' + this.label
    this.clearable = true
    this.defaultValue = ''
    this.dataModelId = ''
    // 请求接口
    this.url = ''
    // 下拉框的选项
    this.dicData = []

    // 校验
    this.rules = [
      { required: false, message: '请选择' + this.label , trigger: 'change' },
    ]

    // 字典的label value配置
    this.props = {
      label: '',
      value: 'id'
    }
  }
}