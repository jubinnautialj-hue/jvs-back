import pick from '../base'
export default {
    formData: {
        titleFontColor:'rgba(14,20,33,1)',
        titleFontSize:16,
        descFontColor:'rgba(14,20,33,1)',
        descFontSize:16,
        btnText:'',
        btnClickType:1,
        contentPadding:10,
        reqType:1,
        reqUrl:"",
        showNum:4
    },
    formConf (formData){
        return {
            titleFontSize:{
                label: '文库字体大小',
                slot: (h) => {
                    return (
                      <div>
                        <customInputNumber v-model={formData.titleFontSize} controls-position="right" min={12} max={256} pramaKey="titleFontSize" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
                        <span style="margin-left: 10px;font-weight:bold">px</span>
                      </div>
                    )
                  }
            },
            titleFontColor:{
                label: '文库颜色',
                slot: (h) => <standard-color-picker color={formData.titleFontColor}  on={{'update:color':(value,that)=>(that.$parent.changeVal({key:'titleFontColor',val:value}))}}/>
            },
            descFontSize:{
              label: '内容字体大小',
              slot: (h) => {
                  return (
                    <div>
                      <customInputNumber v-model={formData.descFontSize} controls-position="right" min={12} max={256} pramaKey="descFontSize" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
                      <span style="margin-left: 10px;font-weight:bold">px</span>
                    </div>
                  )
              }
            },
            descFontColor:{
                label: '内容颜色',
                slot: (h) => <standard-color-picker color={formData.descFontColor}  on={{'update:color':(value,that)=>(that.$parent.changeVal({key:'descFontColor',val:value}))}}/>
            },
            btnText:{
                label: '按钮文本',
                type: 'input',
                attrs: {
                    maxlength:10,
                    placeholder: '可配置显示自定义按钮文本'
                }
            },
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
            },
            reqType:{
                label: '获取方式',
                type: 'radio-group',
                attrs: {
                    class: 'block-radio-group'
                },
        
                radio: {
                    list: [
                        {
                            name: '获取用户文库',
                            value: 1
                        },
                        {
                            name: '通过接口获取',
                            value: 2
                        },
                        {
                            name: '通过ID获取',
                            value: 3
                        }
                    ],
                    label: 'name',
                    value: 'value'
                },
                // cormItemClass:['custom-radio-inline-flex'],
            },
            reqUrl:{
                when: (formData) => formData.reqType !== 1,
                type:'input',
                attrs:{
                    type:'textarea',
                    placeholder: formData.reqType===3?'请输入文库ID':'输入获取数据的请求地址',
                    autosize:{minRows: 2}
                },
                rules: [{
                    required: true,
                    validator: (rule, value, callback) => {
                      if (!formData.reqUrl) {
                        callback(new Error(formData.reqType===3?'请输入文库ID':'输入获取数据的请求地址'))
                      }
                      callback();
                    },
                }],
                tips: formData.reqType===3?'多个ID的时候用逗号分割':formData.reqType===2?'目前只能支持GET请求方式':''
            },
            showNum:{
                label:"展示列表数量",
                slot: (h) => {
                    return (
                      <div>
                        <customInputNumber v-model={formData.showNum} controls-position="right" min={3} pramaKey="showNum" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
                        <span style="margin-left: 10px;font-weight:bold">px</span>
                      </div>
                    )
                }
            },
            contentPadding:{
                label: '内容间距',
                slot:   (h) => {
                    return (
                      <div style="display:flex;align-items: center;">
                        <customInputNumber v-model={formData.contentPadding} controls-position="right" min={0} max={256} customStyle="width: 120px" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}} pramaKey="contentPadding"/>
                        <span style="margin-left: 10px;font-weight:bold">px</span>
                      </div>
                    )
                }
            },
        }
    }
}