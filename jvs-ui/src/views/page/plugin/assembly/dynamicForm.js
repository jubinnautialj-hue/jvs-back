
import assembly from './assembly'
import formatKey from './format'
export default class MDynamicForm extends assembly{
  constructor (
    type='dynamicForm',
    label='动态表单',
    col=24,
    prop='dynamicForm'+ formatKey.numberToString(new Date().getTime()),
  ) {
    super(type,label,col,prop);
    this.span = 24
    this.showFrom = ['label', 'col', 'prop', 'span']
    this.rules = []
    this.column = []
  }
}