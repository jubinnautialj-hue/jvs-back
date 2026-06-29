export default {
    formData: {
        rightImg:'https://image.moyincloud.com/20220609-102544.png',
        boxBackgroundColor:'#1f5fff',
        position:5,
        titleTxt:'快速找到解决方案，轻松解决问题',
        descTxt:'请问你需要什么帮助？请输入关键词搜索'
    },
    global:{
        w:24,
        h:8
    },
    formConf(formData){
        return {
            rightImg:{
                label:'介绍配图',
                slot:(h)=><uploadFile value={formData.rightImg}  pramaKey="rightImg" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
            },
            boxBackgroundColor:{
                label:'背景色',
                slot: (h) => <standard-color-picker color={formData.boxBackgroundColor} show-alpha on={{'update:color':(value,that)=>(that.$parent.changeVal({key:'boxBackgroundColor',val:value}))}}/>
            },
            titleTxt:{
                label:'主题内容',
                type:'input',
            },
            descTxt:{
                label:'副题内容',
                type:'input',
            },
            position: {
                label: '内容对齐',
                slot: (h) => {
                    return (<position-selector v-model={formData.position} on={{'update:value':(value,that)=>(that.$parent.changeVal({key:'position',val:value}))}}></position-selector>)
                }
            },
        }
    }
}