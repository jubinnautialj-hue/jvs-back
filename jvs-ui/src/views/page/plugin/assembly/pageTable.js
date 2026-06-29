import assembly from './assembly'
import formatKey from './format'
export default class MPageTable extends assembly{
  constructor (
    type='pageTable',
    label='列表页',
    span=24,
    prop='pageTable'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.span = 24
    this.showFrom = ['label', 'span', 'jurisdiction','maxheight','sqlType']
    this.maxheight = 300
    this.showJurisdiction = ['所有用户']
    this.rules = []
    this.sqlType = 'varchar'
    this.linkbind = ''
  }
}