import pick from '../base'
export default {
    formData: {
        jumpType: 1,
        rememberHistory: false,
        rememberHistoryList: [],
        boxShadow: '0 0 4px #aab2b2',
        boxBackground: 'rgba(255,255,255,0.9)',
        position: 5,
        textColor: '#d8d8d8',
        // textShadow: '0 0 1px #464646',
        padding: 10,
        fontFamily: '',
        maxWidth:600,
        boxRadius:4,
        searchHeight:38,
        jumpUrl:'',
        btnType:1,
        btnContent:'搜索',
        btnHeight:32,
        btnTextColor:'rgba(255, 255, 255, 1)',
        btnBackground:'rgba(31,95,255,1)',
        btnFontSize:14,
        btnRadius:4,
        btnPadding:10
    },
    formConf (formData) {
        return {
            // rememberHistory: {
            //     label: '记录搜索历史',
            //     type: 'switch',
            //     tips: '最多只会记录10条'
            // },
            jumpType: {
                label: '搜索打开方式',
                type: 'radio-group',
                radio: {
                  list: [
                    {
                      name: '新窗口打开',
                      value: 1
                    },
                    {
                      name: '当前页跳转',
                      value: 2
                    }
                  ],
                  label: 'name',
                  value: 'value'
                },
                cormItemClass:['custom-radio-inline-flex'],
            },
            jumpUrl:{
                label:'搜索访问地址',
                type:'input',
            },
            position: {
                label: '内容对齐',
                slot: (h) => {
                  return (<position-selector v-model={formData.position} on={{'update:value':(value,that)=>(that.$parent.changeVal({key:'position',val:value}))}}></position-selector>)
                }
            },
            // boxBackground: {
            //     label: '搜索栏背景',
            //     slot: (h) => <standard-color-picker color={formData.boxBackground} show-alpha on={{'update:color':(value,that)=>(that.$parent.changeVal({key:'boxBackground',val:value}))}}/>
            // },
            // boxShadow: {
            //     label: '搜索栏阴影',
            //     type: 'input',
            //     tips: 'shadowTips'
            // },
            boxRadius: {
                label: '搜索栏圆角',
                type: 'input-number',
                attrs: {
                    'controls-position': 'right',
                    min: 0,
                    max: 40,
                    style: 'width: 120px'
                }
            },
            searchHeight:{
                label:'搜索栏高度',
                type:'input-number',
                attrs: {
                    'controls-position': 'right',
                    min: 30,
                    style: 'width: 120px'
                }
            },
            maxWidth: {
                label: '最大宽度',
                type: 'input-number',
                attrs: {
                    'controls-position': 'right',
                    min: 300,
                    max: 1920,
                    style: 'width: 120px'
                }
            },
            ...pick(formData, [
                // 'padding'
            ]),
            // btnType: {
            //     label: '按钮显示方式',
            //     type: 'radio-group',
            //     radio: {
            //       list: [
            //         {
            //           name: '图标',
            //           value: 1
            //         },
            //         {
            //           name: '文本',
            //           value: 2
            //         }
            //       ],
            //       label: 'name',
            //       value: 'value'
            //     },
            //     cormItemClass:['custom-radio-inline-flex'],
            // },
            // btnContent:{
            //     when: (formData) => formData.btnType === 2,
            //     label:'按钮文本',
            //     type:'input',
            //     attrs:{
            //         minlength:2,
            //         maxlength:8,
            //     }
            // },
            // btnHeight:{
            //     when: (formData) => formData.btnType === 2,
            //     label: '按钮高度',
            //     slot:   (h) => {
            //         return (
            //           <div style="display:flex;align-items: center;">
            //             <customInputNumber v-model={formData.btnHeight} controls-position="right" min={0} max={256} customStyle="width: 120px" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}} pramaKey="btnHeight"/>
            //             <span style="margin-left: 10px;font-weight:bold">px</span>
            //           </div>
            //         )
            //     }
            // },     
            // btnTextColor:{
            //     when: (formData) => formData.btnType === 2,
            //     label: '按钮字体颜色',
            //     slot: (h) => <standard-color-picker color={formData.btnTextColor} show-alpha on={{'update:color':(value,that)=>(that.$parent.changeVal({key:'btnTextColor',val:value}))}}/>
            // },
            // btnBackground:{
            //     when: (formData) => formData.btnType === 2,
            //     label: '按钮背景颜色',
            //     slot: (h) => <standard-color-picker color={formData.btnBackground} show-alpha on={{'update:color':(value,that)=>(that.$parent.changeVal({key:'btnBackground',val:value}))}}/>
            // },
            // btnFontSize:{
            //     when: (formData) => formData.btnType === 2,
            //     label: '按钮字体大小',
            //     type: 'input-number',
            //     attrs: {
            //         'controls-position': 'right',
            //         min: 12,
            //         max: 32,
            //         style: 'width: 120px'
            //     }
            // },
            // btnRadius:{
            //     when: (formData) => formData.btnType === 2,
            //     label: '按钮圆角',
            //     type: 'input-number',
            //     attrs: {
            //         'controls-position': 'right',
            //         min: 0,
            //         max: 40,
            //         style: 'width: 120px'
            //     }
            // },
            // btnPadding:{
            //     when: (formData) => formData.btnType === 2,
            //     label: '按钮内边距',
            //     slot:   (h) => {
            //         return (
            //           <div style="display:flex;align-items: center;">
            //             <customInputNumber v-model={formData.btnPadding} controls-position="right" min={0} max={256} customStyle="width: 120px" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}} pramaKey="btnPadding"/>
            //             <span style="margin-left: 10px;font-weight:bold">px</span>
            //           </div>
            //         )
            //     }
            // },           
        }
    }
}