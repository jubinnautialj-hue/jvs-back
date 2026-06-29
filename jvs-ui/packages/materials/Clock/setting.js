import pick from '../base'
export default {
  formData: {
    duration: 5000,
    position: 5,
    textFontSize: 64,
    textColor: '#d8d8d8',
    textShadow: '0 0 1px #464646',
    fontFamily: '',
    padding: 10,
    textHollow: false,
    textHollowBorder: 1,
    textHollowBg: 'rgba(0,0,0,0)'
  },
  formConf (formData) {
    return {
      duration: {
        label: '刷新频率',
        type: 'input-number',
        attrs: {
          'controls-position': 'right',
          min: 1000,
          max: 60000,
          step: 1000,
          style: 'width: 100%'
        },
        tips: 'durationTips'
      },
      ...pick(formData, [
        'position',
        'textFontSize',
        'textColor',
        // 'textShadow',
        // 'fontFamily',
        // 'padding'
      ]),
      // textHollow: {
      //   label: '镂空字效果',
      //   type: 'switch',
      //   tips: 'textStrokeTips'
      // },
      // textHollowBorder: {
      //   when: (formData) => formData.textHollow,
      //   label: '镂空边框',
      //   slot: (h) => {
      //     return (
      //       <div style="display:flex;align-item: center">
      //       <customInputNumber v-model={formData.textHollowBorder} controls-position="right" min={1} max={10} pramaKey="textHollowBorder" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
      //         <span style="margin-left: 10px;font-weight:bold">px</span>
      //       </div>
      //     )
      //   }
      // },
      // textHollowBg: {
      //   when: (formData) => formData.textHollow,
      //   label: '镂空背景色',
      //   slot: (h) => <standard-color-picker show-alpha vModel={formData.textHollowBg} />
      // }
    }
  },
}
