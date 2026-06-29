import assembly from './assembly'
import formatKey from './format'
export default class MBluetoothBeacon extends assembly{
  constructor (
    type='bluetoothBeacon',
    label='蓝牙信标',
    span=24,
    prop='bluetoothBeacon'+ formatKey.numberToString(new Date().getTime())
  ) {
    super(type,label,span,prop);
    this.span = 24
    this.multiple = true
    this.allowinput = false
    this.showFrom = ['label','span', 'prop','sqlType', 'disabled']
    this.rules = []
    this.sqlType = 'array'
    // 校验
    this.rules = []
  }
}
