import assembly from './assembly'
import formatKey from './format'
export default class MChildrenForm extends assembly{
  constructor (
    type='childrenForm',
    label='',
    span=24,
    prop='childrenForm'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.showFrom = ['prop','label', 'formId', 'disabled', 'span']
    this.label = '子表单'
    this.rules = []
    this.formId = ''
    this.disabled = false
  }
}