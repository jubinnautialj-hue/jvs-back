import assembly from './assembly'
import formatKey from './format'
export default class MSarialNumber extends assembly{
  constructor (
    type='serialNumber',
    label='',
    span=24,
    prop='serialNumber'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.showFrom = ['prop','label','span', 'orderPrefix', 'orderTimeMark', 'orderDigit', 'orderResetRule']
    this.label = '流水号'
    this.text= '前缀+时间标识+序号'
    this.rules = []
    this.orderPrefix = ''
    this.orderTimeMark = 'n'
    this.orderDigit = 5
    this.orderResetRule = 'n'
  }
}