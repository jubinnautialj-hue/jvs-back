
import * as CryptoJS from'crypto-js'
// 表单序列化
export const serialize = data => {
  let list = []
  Object.keys(data).forEach(ele => {
    list.push(`${ele}=${data[ele]}`)
  })
  return list.join('&')
}
/**
 * 去除对象中的空值参数，null和undefined
 */
export const noEmptyOfObject = (obj) => {
  if(typeof obj == 'object') {
    if(obj instanceof Array) {
      return obj
    }else{
      let temp = {}
      for(let i in obj) {
        if(obj[i] || obj[i] === false || obj[i] === 0) {
          temp[i] = obj[i]
        }
      }
      return temp
    }
  }else{
    return obj
  }
}

/**
 *解密处理
 */
export const decryption = (params) => {
  let {
    data,
    type,
    param,
    key
  } = params
  const result = JSON.parse(JSON.stringify(data))
  if (type === 'Base64') {
    param.forEach(ele => {
      result[ele] = btoa(result[ele])
    })
  } else {
    param.forEach(ele => {
      var data = CryptoJS.enc.Base64.stringify(CryptoJS.enc.Hex.parse(result[ele]))
      key = CryptoJS.enc.Utf8.parse(key)
      var iv = key
      // 解密
      let decrypted = CryptoJS.AES.decrypt(
        data,
        key, {
          iv: iv,
          mode: CryptoJS.mode.CBC,
          padding: CryptoJS.pad.ZeroPadding
      })
      result[ele] = decrypted.toString(CryptoJS.enc.Utf8)
    })
  }
  return result
}