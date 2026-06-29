export const hex2Rgb = (hexValue, alpha = 1) => {
  const regex = /^#([0-9A-Fa-f]{6}),(?:\d+(?:\.\d*)?|\.\d+)$/
  const match = regex.exec(hexValue)
  if (match) (hexValue = `#${match[1]}`) && (alpha = match[0].split(",")[1])

  const rgx = /^#?([a-f\d])([a-f\d])([a-f\d])$/i
  const hex = hexValue.replace(rgx, (m, r, g, b) => r + r + g + g + b + b)
  const rgb = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  if(!rgb) return hexValue
  const r = parseInt(rgb[1], 16),
    g = parseInt(rgb[2], 16),
    b = parseInt(rgb[3], 16);
  return `rgba(${r},${g},${b},${alpha})`
};

// 主题颜色
export const themeList = [
  {
    name: '默认主题',
    list: ['#1E6FFF', '#629BFF', '#9ABEFF', '#BCD4FF', '#D2E2FF', '#E8F0FF'],
    value: 'default'
  },
  {
    name: '默认主题1',
    list: ['#1E6FFF', '#14C9C9', '#FDCC00', '#275C96', '#FD7172', '#DC3422'],
    value: 'default1'
  },
  {
    name: '默认主题2',
    list: ['#4789F7', '#1CD0FB', '#F1878B', '#4ED964', '#F7DB94', '#9693B9'],
    value: 'default2'
  },
  {
    name: '默认主题3',
    list: ['#079ADA', '#A1DFF6', '#E7F0F4', '#FFE190', '#FD8F59', '#DC3422'],
    value: 'default3'
  },
  {
    name: '默认主题4',
    list: ['#355E89', '#5FA6C8', '#8AC7DC', '#FEE6B5', '#FBB75E', '#E76253'],
    value: 'default4'
  },
  {
    name: '默认主题5',
    list: ['#5096C2', '#E17F48', '#E9C78E', '#E9A199', '#94D4C3', '#266FA4'],
    value: 'default5'
  },
  {
    name: '默认主题6',
    list: ['#4D6279', '#67A086', '#C3D3BA', '#FDD8A7', '#EBA35F', '#C75D3B'],
    value: 'default6'
  },
  {
    name: '默认主题7',
    list: ['#6C8163', '#BBC956', '#D6E2CE', '#FCF3D4', '#E1A371', '#C78364'],
    value: 'default7'
  },
  {
    name: '默认主题8',
    list: ['#2F7F7D', '#2CA7A9', '#59C5BE', '#91D5D7', '#BBD5D6', '#D6E6E7'],
    value: 'default8'
  },
  {
    name: '默认主题9',
    list: ['#EEEEAF', '#B7E8A0', '#6AC4AF', '#39ACB9', '#1D91BE', '#052480'],
    value: 'default9'
  },
  {
    name: '默认主题10',
    list: ['#93590F', '#E4CA8F', '#A8DED5', '#1B7B79', '#838EA0', '#E4E5E0'],
    value: 'default10'
  },
  {
    name: '默认主题11',
    list: ['#DF9E9B', '#8AB2DE', '#999ACC', '#90CDCE', '#CEE2B6', '#EEB4D3'],
    value: 'default11'
  },
  {
    name: '默认主题12',
    list: ['#3B0A63', '#7B1B6E', '#BC3951', '#ED6824', '#FBB41B', '#FFE192'],
    value: 'default12'
  },
  {
    name: '默认主题13',
    list: ['#7559BC', '#244CA4', '#E76A80', '#F8AF2A', '#7292CC', '#CCBA72'],
    value: 'default13'
  },
  {
    name: '默认主题14',
    list: ['#4E659D', '#B7A8CF', '#FECEA1', '#B6766C', '#E5AEC4', '#6890A2'],
    value: 'default14'
  }
]

const bottomGrid = {
  top: '4%',
  left: '4%',
  right: '4%',
  bottom: '30px',
  containLabel: true
}, topGrid = {
  top: '30px',
  left: '4%',
  right: '4%',
  bottom: '4%',
  containLabel: true
}, leftGrid = {
  top: '4%',
  left: '90px',
  right: '4%',
  bottom: '4%',
  containLabel: true
}, rightGrid = {
  top: '4%',
  left: '4%',
  right: '75px',
  bottom: '4%',
  containLabel: true
}

export const legendPosition = {
  'top': {
    orient: 'horizontal',
    left: 'center',
    top: 'top',
    grid: topGrid
  },
  'top-left': {
    orient: 'horizontal',
    left: 'left',
    top: 'top',
    grid: topGrid
  },
  'top-right': {
    orient: 'horizontal',
    left: 'right',
    top: 'top',
    grid: topGrid
  },
  'bottom': {
    orient: 'horizontal',
    bottom: 'bottom',
    left: 'center',
    grid: bottomGrid
  },
  'bottom-left': {
    orient: 'horizontal',
    left: 'left',
    bottom: 'bottom',
    grid: bottomGrid
  },
  'bottom-right': {
    orient: 'horizontal',
    left: 'right',
    bottom: 'bottom',
    grid: bottomGrid
  },
  'left': {
    orient: 'vertical',
    left: 'left',
    bottom: 'center',
    grid: leftGrid,
    padding: [5, 0, 5, 5]
  },
  'left-top': {
    orient: 'vertical',
    left: 'left',
    top: 'top',
    bottom: 'top',
    grid: leftGrid,
    padding: [5, 0, 5, 5]
  },
  'left-bottom': {
    orient: 'vertical',
    left: 'left',
    top: 'bottom',
    bottom: 'bottom',
    grid: leftGrid,
    padding: [5, 0, 5, 5]
  },
  'right': {
    orient: 'vertical',
    right: 'left',
    bottom: 'center',
    grid: rightGrid,
    padding: [5, 5, 5, 0]
  },
  'right-top': {
    orient: 'vertical',
    top: 'top',
    right: 'left',
    grid: rightGrid,
    padding: [5, 5, 5, 0]
  },
  'right-bottom': {
    orient: 'vertical',
    right: 'left',
    top: 'bottom',
    grid: rightGrid,
    padding: [5, 5, 5, 0]
  },
}

/**
 * 格式化字段
 * @param {*} value 原始数据
 * @param {*} formatConfig 格式化配置
 */
export const formatField = (value, formatConfig) => {
  let tempValue = formatConfig.formatBase === '1' ? value / formatConfig.unit : value
  let str = ''
  let str1 = ''
  let str2 = ''
  let arr = String(tempValue).split('e-')
  let arr1 = arr[0].split('.')
  // 保留小数位
  if (formatConfig.effective) {
    if (arr[1]) {
      if (arr1[1]) {
        str = tempValue.toFixed(Number(arr[1]) + String(arr1[1]).length)
      } else {
        str = tempValue.toFixed(Number(arr[1]))
      }
      let strArr = str.split('.')
      str = strArr[0]
      if (strArr[1]) {
        str = strArr[0] + '.'
        let count = formatConfig.decimal
        for (let i = 0; i < strArr[1].length; i++) {
          if (count !== 0) {
            str += strArr[1][i]
          }
          if (strArr[1][i] != 0) count--
        }
      }
    } else {
      str = getRes(tempValue, formatConfig.decimal)
    }
  } else {
    str = tempValue.toFixed(formatConfig.decimal)
  }
  // 千分位分隔符
  if (formatConfig.separator) {
    str1 = String(str).split('.')[0]
    str2 = String(str).split('.')[1] || ''
    const reg = /(?=\d{3}$)/g
    str1 = str1.replace(reg, ',')
    str = str2 ? `${str1}.${str2}` : str1
  }
  return str
}