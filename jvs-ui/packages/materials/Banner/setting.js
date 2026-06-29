import img1 from '../../customForm/bannerImg/1.png'
export default {
    formData: {
        componentName:'',
        // banner:banner1,
      codeImg:'',
      backImg:'',
        position: 5,
        textFontSize: 18,
        textColor: 'rgba(51, 51, 51, 1)',
        bannerList: [
            {
                banner: img1,
                title: '',
                content: '',
                textColor: 'rgba(51, 51, 51, 1)',
            }
        ]
    },
    global:{
      w:24,
      h:10,
      boxShadow:'0 4px 26px #99999914'
    },
    customAttributes: [
        {
            prop: 'bannerList',
            type: 'list',
            column: [
                {
                    prop: 'banner',
                    type: 'banner',
                    label: '轮播图1'
                },
            ]
        },
    ],
    formConf (formData) {
        return {
            // ...pick(formData, [
            //     'textFontSize',
            //     'textColor',
            //     'position',
            // ]),
        }
    }
}
