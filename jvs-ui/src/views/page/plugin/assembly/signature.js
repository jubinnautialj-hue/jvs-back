import assembly from './assembly'
import formatKey from './format'
export default class MSignature extends assembly{
  constructor (
    type='signature',
    label='',
    span=24,
    prop='signature'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.span = 24
    this.showFrom = ['prop','label','span', 'disabled']
    this.disabled = false
    this.label = '手写签名'
    this.rules = []
  }
}