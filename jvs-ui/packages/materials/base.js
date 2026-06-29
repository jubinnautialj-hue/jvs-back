export default function (formData,fields){
  const baseTemplate = {
    position: {
      label: '文本对齐',
      slot: (h) => {
        return (<position-selector v-model={formData.position} on={{'update:value':(value,that)=>(that.$parent.changeVal({key:'position',val:value}))}}></position-selector>)
      }
    },
    textFontSize: {
      label: '字体大小',
      slot: (h) => {
        return (
          <div style="display:flex;align-items: center;">
            <customInputNumber v-model={formData.textFontSize} controls-position="right" min={12} max={256} customStyle="width: 100%" pramaKey="textFontSize" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
            <span style="margin-left: 10px;font-weight:bold">px</span>
          </div>
        )
      }
    },
    baseFontSize: {
      label: '基础字体大小',
      slot: (h) => {
        return (
          <div style="display:flex;align-items: center;">
            <customInputNumber v-model={formData.baseFontSize} controls-position="right" min={12} max={256} customStyle="width: 100%" pramaKey="baseFontSize" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
            <span style="margin-left: 10px;font-weight:bold">px</span>
          </div>
        )
      },
      tips: 'baseFontSizeTips'
    },
    textColor: {
      label: '字体颜色',
      slot: (h) => <standard-color-picker color={formData.textColor}  on={{'update:color':(value,that)=>(that.$parent.changeVal({key:'textColor',val:value}))}}/>
    },
    // fontFamily: {
    //   label: '字体库',
    //   slot: (h) => <font-selector v-model={formData.fontFamily} showRefresh on={{'update:value':(value,that)=>(that.$parent.changeVal({key:'fontFamily',val:value}))}} size="mini"></font-selector>
    // },
    padding: {
      label: '盒子内边距',
      slot: (h) => {
        return (
          <div style="display:flex;align-items: center;">
            <customInputNumber v-model={formData.padding} controls-position="right" min={0} max={256} customStyle="width: 120px" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}} pramaKey="padding"/>
            <span style="margin-left: 10px;font-weight:bold">px</span>
          </div>
        )
      }
    },
    textShadow: {
      label: '字体阴影',
      type: 'input',
      attrs: {
        placeholder: 'e.g. "0 0 1px #464646"'
      },
      // formItemStyle:{maxWidth:'calc(100% - 50px)'},
      tips: 'textShadowTips'
    },
    iconShadow: {
      label: '图标阴影',
      type: 'input',
      tips: 'iconShadowTips'
    },
    showTitle: {
      label: '标题LOGO',
      type: 'switch',
      attrs: {
        'active-text': '展示顶部标题LOGO'
      }
    },
    clickActionType: {
      when: (formData) => formData.showTitle,
      label: '点击LOGO',
      type: 'select',
      option: {
        list: [
          { label: '无', value: 0 },
          { label: '刷新列表', value: 1 },
          { label: '跳转主页', value: 2 }
        ],
        label: 'label',
        value: 'value'
      },
      tips: '配置LOGO的点击事件'
    }
  }
  const result= {}
  if (typeof fields === 'string') {
    result[fields] = baseTemplate[fields]
  } else {
    fields.map(key => {
      result[key] = baseTemplate[key]
    })
  }
  return result
}