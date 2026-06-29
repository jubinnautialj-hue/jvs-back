import assembly from './assembly'
import formatKey from './format'
export default class MJsonEditor extends assembly{
  constructor (
    type='jsonEditor',
    label='JSON编译器',
    span=24,
    prop='jsonEditor'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.span = 24
    this.showFrom = ['prop','label','jurisdiction', 'disabled', 'span']
    this.showJurisdiction = ['所有用户']
    this.sqlType = 'varchar'
    this.linkbind = ''
    // 校验
    this.rules = [
      { required: false, message: '请输入' + this.label , trigger: 'change' }
    ]
  }
}