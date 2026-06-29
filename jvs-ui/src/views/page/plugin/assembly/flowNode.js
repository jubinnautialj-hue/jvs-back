
import assembly from './assembly'
import formatKey from './format'
export default class MFlowNode extends assembly{
  constructor (
    type='flowNode',
    label='动态流程',
    span=24,
    prop='flowNode'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.span =24
    this.showFrom = ['label', 'prop', 'span', 'sqlType', 'disabled']
    this.sqlType = 'varchar'
    this.rules = []
    this.linkbind = ''
  }
}