import pick from '../base'
import img1 from '../../customForm/backImg/1.png'
export default {
    formData: {
        componentName:'',
      codeImg:'',
      backImg:img1,
        textFontSize: 18,
        textColor: '#333',
    },
    global:{
      w:5,
      h:5,
      boxShadow:'0 4px 26px #99999914'
    },
    customAttributes: [
        {
            prop: 'componentName',
            type: 'input',
            label: '组件名称'
        },
        {
            prop: 'backImg',
            type: 'backImg',
            label: '图片'
        }
    ],
    formConf (formData) {
        return {
            codeImg:{
                label: '二维码',
                slot:(h)=><uploadFile value={formData.codeImg}  pramaKey="codeImg" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
            },
        }
    }
}
