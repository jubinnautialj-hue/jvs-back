import pick from '../base'
export default {
    formData: {
      iconForm:1,
      iconUrl:'',
      coverLink:'',
      requestId:'',
      customText: '',
      // boxBackColor:'',
      titleFontColor:'rgba(14,20,33,1)',
      titleFontSize:16,
      filePath:'',
      documentDesc:'',
      descFontColor:'#758296',
      descFontSize:14,
      btnText:'',
      btnClickType:1,
      btnClickUrl:'',
      contentPadding:10,
    },
    global:{
      w:4,
      h:6,
      boxShadow:'0 4px 26px #99999914'
    },
    formConf (formData) {
        return {
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
            // requestId:{
            //   label: '展示文库',
            //   slot:(h)=><customSelectBox value={formData.requestId} optionProp={{label:'name',value:"id"}}  pramaKey="requestId" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}></customSelectBox>,
            //   rules: [{
            //       required: true,
            //       validator: (rule, value, callback) => {
            //         if (!formData.requestId) {
            //           callback(new Error('请选择要展示的文库'))
            //         }
            //         callback();
            //       },
            //   }],
            // },
            coverLink:{
                label: '封面',
                slot:(h)=><uploadFile value={formData.coverLink}  pramaKey="coverLink" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
            },
            
            // boxBackColor:{
            //   label: '背景颜色',
            //   slot: (h) => <standard-color-picker color={formData.boxBackColor}  on={{'update:color':(value,that)=>(that.$parent.changeVal({key:'boxBackColor',val:value}))}}/>
            // },
            // customText:{
            //     label: '文档标题',
            //     type: 'input',
            //     attrs: {
            //         placeholder: '可配置显示自定义文档标题'
            //     },
            //     rules: [{
            //         required: true,
            //         validator: (rule, value, callback) => {
            //           if (!formData.customText) {
            //             callback(new Error('请输入文档标题'))
            //           }
            //           callback();
            //         },
            //     }],
            // },
            // titleFontSize:{
            //     label: '标题字体大小',
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
            //     label: '标题颜色',
            //     slot: (h) => <standard-color-picker color={formData.titleFontColor}  on={{'update:color':(value,that)=>(that.$parent.changeVal({key:'titleFontColor',val:value}))}}/>
            // },
            // documentDesc:{
            //     label: '文档描述',
            //     type: 'input',
            //     attrs: {
            //       placeholder: '可配置显示自定义文档描述内容',
            //       maxlength:60
            //     },
            //     rules: [{
            //         required: true,
            //         validator: (rule, value, callback) => {
            //           if (!formData.documentDesc) {
            //             callback(new Error('请输入文档描述'))
            //           }
            //           callback();
            //         },
            //     }],
            // },
            // descFontSize:{
            //   label: '描述字体大小',
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
            //     label: '描述颜色',
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
            // btnClickType:{
            //   label: '打开方式',
            //   type: 'radio-group',
            //   radio: {
            //     list: [
            //       {
            //         name: '新窗口打开',
            //         value: 1
            //       },
            //       {
            //         name: '当前页跳转',
            //         value: 2
            //       }
            //     ],
            //     label: 'name',
            //     value: 'value'
            //   },
            //   cormItemClass:['custom-radio-inline-flex'],
            // },
            // btnClickUrl:{
            //   label: '按钮跳转地址',
            //   type: 'input',
            //   attrs: {
            //     placeholder: '可配置点击事件后跳转地址',
            //     type:'textarea',
            //     autosize:{minRows: 2}
            //   },
            //   rules: [{
            //       required: true,
            //       validator: (rule, value, callback) => {
            //         if (!formData.btnClickUrl) {
            //           callback(new Error('请输入跳转地址'))
            //         }
            //         callback();
            //       },
            //   }],
            // },
            // contentPadding:{
            //     label: '内容内边距',
            //     slot:   (h) => {
            //         return (
            //           <div style="display:flex;align-items: center;">
            //             <customInputNumber v-model={formData.contentPadding} controls-position="right" min={0} max={256} customStyle="width: 120px" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}} pramaKey="contentPadding"/>
            //             <span style="margin-left: 10px;font-weight:bold">px</span>
            //           </div>
            //         )
            //     }
            // },
            // ...pick(formData, [])
        }
    }
}