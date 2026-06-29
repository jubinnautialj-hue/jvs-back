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
      w:6,
      h:8,
      boxShadow:'0 4px 26px #99999914'
    },
    formConf (formData) {
        return {
        }
    }
}