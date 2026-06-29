import assembly from './assembly'
import formatKey from './format'
export default class MIframe extends assembly{
  constructor (
    type='iframe',
    label='网页',
    span=24,
    prop='iframe'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.span = 24
    this.showFrom = ['label', 'prop', 'defaultValue', 'jurisdiction','span', 'iframeheight', 'sqlType'] // 'iframeurl'
    this.iframeheight = 100
    this.iframeurl = ''
    this.showJurisdiction = ['所有用户']
    this.rules = []
    this.sqlType = 'varchar'
    this.linkbind = ''
  }
}