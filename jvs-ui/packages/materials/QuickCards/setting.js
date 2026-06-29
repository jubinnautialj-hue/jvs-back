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
            coverLink:{
                label: '封面',
                slot:(h)=><uploadFile value={formData.coverLink}  pramaKey="coverLink" on={{'changeVal':(value,that)=>(that.$parent.changeVal(value))}}/>
            },
        }
    }
}