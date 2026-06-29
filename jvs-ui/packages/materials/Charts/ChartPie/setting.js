export default {
    formData: {
      iconForm:1,
        chartName:'',
        legend:true,
        colorList:[
            '#1E6FFF',
            '#8EB7FF',
            '#D2E2FF',
            '#DDEAFF',
            '#E4EDFF',
        ],
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
      w: 8,
      h: 8,
      boxShadow:'0 4px 26px #99999914'
    },
    customAttributes: [
        {
            prop: 'chartName',
            type: 'input',
            label: '组件名称'
        },
        {
            prop: 'colorList',
            type: 'color',
            label: '颜色方案',
        },
        {
            prop: 'legend',
            type: 'radio',
            label: '图例',
            dicData: [
                {
                    label: '显示',
                    value: true
                },
                {
                    label: '隐藏',
                    value: false
                }
            ]
        }
    ],
    formConf (formData) {
        return {
        }
    }
}
