import {
  validatenull
} from '@/util/validate'
import website from '@/const/website'
import * as CryptoJS from'crypto-js'
import { enCodeKey} from '@/const/webEncryDecryConst';

const keyName = website.key + '-';
/**
 * 存储localStorage
 */
export const setStore = (params = {}) => {
  let {
    name,
    content,
    type,
  } = params;
  name = keyName + name
  let obj = {
    dataType: typeof (content),
    content: content,
    type: type,
    datetime: new Date().getTime()
  }
  if(typeof content == 'string' || typeof content == 'object') {
    let bool = true
    if(typeof content == 'string' && !content) {
      bool = false
    }
    if(typeof content == 'object') {
      obj.content = JSON.stringify(obj.content)
    }
    if(bool) {
      obj = encryption({
        data: obj,
        key: enCodeKey,
        param: ["content"]
      })
    }
  }
  localStorage.setItem(name, JSON.stringify(obj));
}
/**
 * 获取localStorage
 */

export const getStore = (params = {}) => {
  let {
    name,
    debug,
    type
  } = params;
  name = keyName + name
  let obj = {},
    content;
  obj = localStorage.getItem(name);
  if (validatenull(obj)) {
    if(type){
      return;
    }else{
      obj = localStorage.getItem(name);
    }
  }else{
    try {
      obj = JSON.parse(obj);
    } catch{
      return obj;
    }
    if (debug) {
      return obj;
    }
    try {
      if(obj.dataType == 'string' || obj.dataType == 'object') {
        obj = decryption({
          data: obj,
          key: enCodeKey,
          param: ["content"]
        })
        if(obj.dataType == 'object') {
          obj.content = JSON.parse(obj.content)
        }
      }
    } catch (error) {
      clearStore()
    }
    
    if (obj.dataType == 'string') {
      content = obj.content;
    } else if (obj.dataType == 'number') {
      content = Number(obj.content);
    } else if (obj.dataType == 'boolean') {
      content = eval(obj.content);
    } else if (obj.dataType == 'object') {
      content = obj.content;
    }
    return content;
  }
}
/**
 * 删除localStorage
 */
export const removeStore = (params = {}) => {
  let {
    name,
    type
  } = params;
  name = keyName + name
  sessionStorage.removeItem(name);
  localStorage.removeItem(name);
}

/**
 * 获取全部localStorage
 */
export const getAllStore = (params = {}) => {
  let list = [];
  let {
    type
  } = params;
  for (let i = 0; i <= localStorage.length; i++) {
    list.push({
      name: localStorage.key(i),
      content: getStore({
        name: localStorage.key(i),
      })
    })

  }
  return list;

}

/**
 * 清空全部localStorage
 */
export const clearStore = (params = {}) => {
  let { type } = params;
  sessionStorage.clear();
  localStorage.clear()
}

/**
 *加密处理
 */
 export const encryption = (params) => {
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
      var data = result[ele]
      key = CryptoJS.enc.Utf8.parse(key)
      var iv = key
      // 加密
      var encrypted = CryptoJS.AES.encrypt(
        data,
        key, {
          iv: iv,
          mode: CryptoJS.mode.CBC,
          padding: CryptoJS.pad.Pkcs7
        })
      result[ele] = encrypted.ciphertext.toString()
    })
  }
  return result
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
          padding: CryptoJS.pad.Pkcs7
      })
      result[ele] = decrypted.toString(CryptoJS.enc.Utf8)
    })
  }
  return result
}
