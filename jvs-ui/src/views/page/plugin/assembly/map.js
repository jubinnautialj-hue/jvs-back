import assembly from './assembly'
import formatKey from './format'
export default class MPositionMap extends assembly{
  constructor (
    type='positionMap',
    label='',
    span=24,
    prop='positionMap'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.showFrom = ['prop','label','span', 'disabled']
    this.disabled = false
    this.label = '定位'
    this.rules = []
  }
}