import request from "@/router/axios"

// 获取下拉列表
export const getSelectData = (str, method, params, designId, headers) => {
  let temp = {
    url: str,
    method: method ? method : 'get'
  }
  if(params) {
    if(method && method == 'post') {
      temp.data = params
    }else{
      temp.params = params
    }
  }
  if (designId) {
    temp.headers = {
      'designId': designId
    }
  }
  if(headers) {
    if(!temp.headers) {
      temp.headers = {}
    }
    temp.headers = Object.assign(temp.headers, headers)
  }
  return request(temp)
}

// 自定义上传base64
export const customUpload = (str, file, fileName, module) => {
  let fetchForm = new FormData()
  fetchForm.append('file', base64ToFile(encodeURI(file), fileName))
  fetchForm.append('module', module ? module : '/jvs-ui/form/')
  fetchForm.append('fileName', fileName)
  let temp = {
    url: str,
    method: 'post',
    headers: {
      serialize:false,
      'type':'FormData',
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    data: fetchForm,
  }
  return request(temp)
}

function base64ToFile(base64, fileName) {
  let arr = base64.split(",");
  let mime = arr[0].match(/:(.*?);/)[1];

  let bstr = atob(arr[1]);
  let n = bstr.length;
  let u8arr = new Uint8Array(n);
 
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  return new File([u8arr], fileName, { type: mime });
}

// 获取知识库内容(用户本身)
export function getUserLibrary(data){
  return request({
    url:`/mgr/document/home/dcLibrary/my`,
    method:"post",
    data:data
  })
}

// 通过id获取知识库内容
export function getByIdLibrary(params){
  return request({
    url:`/mgr/document/home/dcLibrary/query`,
    method:'post',
    data:params
  })
}

// 通过url获取数据内容
export function sendRequestUrl(url){
  return request({
    url:url,
    method:'post'
  })
}

// 获取当前的所有公开文库
export function getPublicLibrary(data){
  return request({
    url:`/mgr/document/home/dcLibrary/public`,
    method:'post',
    data
  })
}