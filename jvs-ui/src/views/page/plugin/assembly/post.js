import assembly from './assembly'
import formatKey from './format'
export default class MPost extends assembly{
  constructor (
    type='job',
    label='岗位选择',
    span=24,
    prop='job'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.multiple = true
    this.showFrom = ['label','span','multiple','prop','sqlType', 'disabled', 'isDefault'] // , 'regular'
    this.rules = []
    this.sqlType = 'array'
    // 校验
    this.rules = [
      { required: false, message: '请选择岗位', trigger: 'change' }
    ]
  }
}
