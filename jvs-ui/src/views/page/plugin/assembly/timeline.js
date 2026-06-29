
import assembly from './assembly'
import formatKey from './format'
export default class MTimeLine extends assembly{
  constructor (
    type='timeline',
    label='时间线',
    span=24,
    prop='timeline'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.span = 24
    this.showFrom = ['label','prop','span']
    this.timestamp = 'timestamp'
    this.content = 'content'
    this.rules = []
  }
}