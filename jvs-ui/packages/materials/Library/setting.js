import pick from '../base'
export default {
    formData: {
      iconForm:1,
      iconUrl:'',
      iconFileLink:'',
      titleFontColor:'#0e1421',
      titleFontSize:18,
      descFontColor:'#758296',
      descFontSize:14,
      btnText:'',
      btnClickType:1,
      contentPadding:10,
      requestId:'',
    },
    global:{
      w:4,
      h:7,
    },
    formConf (formData){
        return {
            requestId:{
                label: '展示文库',
                slot:(h)=><customSelectBox value={formData.requestId} optionProp={{label:'name',value:"id"}}  pramaKey="requestId" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}></customSelectBox>,
                rules: [{
                    required: true,
                    validator: (rule, value, callback) => {
                      if (!formData.requestId) {
                        callback(new Error('请选择要展示的文库'))
                      }
                      callback();
                    },
                }],
            },
            // iconForm:{
            //     label: '图片来源方式',
            //     type: 'radio-group',
            //     radio: {
            //       list: [
            //         {
            //           name: 'url',
            //           value: 1
            //         },
            //         {
            //           name: '本地文件',
            //           value: 2
            //         }
            //       ],
            //       label: 'name',
            //       value: 'value'
            //     },
            //     cormItemClass:['custom-radio-inline-flex'],
            // },
            // iconUrl:{
            //     when: (formData) => formData.iconForm === 1,
            //     label: '图片地址',
            //     type:'input',
            //     attrs:{
            //         type:'textarea',
            //         placeholder: '输入图片URL',
            //         autosize:{minRows: 2}
            //     }
            // },
            // iconFileLink:{
            //     when: (formData) => formData.iconForm === 2,
            //     label: '图片地址',
            //     slot:(h)=><uploadFile value={formData.iconFileLink}  pramaKey="iconFileLink" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
            // },
            // titleFontSize:{
            //     label: '文库字体大小',
            //     slot: (h) => {
            //         return (
            //           <div>
            //             <customInputNumber v-model={formData.titleFontSize} controls-position="right" min={12} max={256} pramaKey="titleFontSize" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
            //             <span style="margin-left: 10px;font-weight:bold">px</span>
            //           </div>
            //         )
            //       }
            // },
            // titleFontColor:{
            //     label: '文库颜色',
            //     slot: (h) => <standard-color-picker color={formData.titleFontColor}  on={{'update:color':(value,that)=>(that.$parent.changeVal({key:'titleFontColor',val:value}))}}/>
            // },
            // descFontSize:{
            //   label: '内容字体大小',
            //   slot: (h) => {
            //       return (
            //         <div>
            //           <customInputNumber v-model={formData.descFontSize} controls-position="right" min={12} max={256} pramaKey="descFontSize" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
            //           <span style="margin-left: 10px;font-weight:bold">px</span>
            //         </div>
            //       )
            //   }
            // },
            // descFontColor:{
            //     label: '内容颜色',
            //     slot: (h) => <standard-color-picker color={formData.descFontColor}  on={{'update:color':(value,that)=>(that.$parent.changeVal({key:'descFontColor',val:value}))}}/>
            // },
            // btnText:{
            //     label: '按钮文本',
            //     type: 'input',
            //     attrs: {
            //         maxlength:10,
            //         placeholder: '可配置显示自定义按钮文本'
            //     }
            // },
            btnClickType:{
              label: '点击事件',
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
            }
        }
    }
}