import request from '@/router/axios'

// 根据类型返回树数据
export function getUserTreeType(data){
  return request({
    url: `/mgr/jvs-auth//user/user/selected`,
    method:'post',
    data: data
  })
}

// 分页查询用户
export function getUserListPage(params){
  return request({
    url: `/mgr/jvs-auth//user/user/selected/page`,
    method: 'post',
    data: params
  })
}

// 根据工作流id和节点id查询可选用户
export function getUserListPageByFlow(data){
  return request({
    url: `/mgr/jvs-design/base/workflow/user/selected/page`,
    method: 'post',
    data: data
  })
}

// 导出  下载文件
export const downloadRequest = (url) => {
  return request({
    url: url,
    method: 'get',
    responseType: 'arraybuffer',
  })
}

// 获取所有用户
export function getSelectedUsers(data) {
  return request({
    url: `/mgr/jvs-auth//user/all`,
    method: "post",
    data: data
  });
}

// 执行逻辑
export const ruleRun = (jvsAppId, ruleKey, data, headers) => {
  let tp = {
    url: `/mgr/jvs-design/app/use/${jvsAppId}/rule/run/${ruleKey}`,
    method: 'post',
    data: data
  }
  if(headers) {
    tp.headers = headers
  }
  return request(tp)
}

// 逻辑下载文件
export const ruleDownLoad = (jvsAppId, data) => {
  let tp = {
    url: `/mgr/jvs-design/app/use/${jvsAppId}/rule/download`,
    method: 'post',
    responseType: 'blob',
    data: data
  }
  return request(tp)
}

// 获取配置文件名
export const getPlugins = (name)=>{
  return request({
    url:`/get/nacos/config/${name}`,
    method: 'get',
    headers: {
      isToken: false,
    },
  })
}

// 获取应用授权信息
export function getLicense(data){
  return request({
    url:`/license`,
    method:'get',
    params:data
  })
}

// 校验
export function veriLicense(data){
  return request({
    url:`/license/verification`,
    method:"post",
    isReturn:true,
    data: data
  })
}

// 消息通知选择人员选项
export function getNOticeUserOption(jvsAppId, modelId){
  return request({
    url:`/mgr/jvs-design/app/design/${jvsAppId}/data/notice/user/options/${modelId}`,
    method:'get'
  })
}

// 检查配置是否配置了如果有配置则可以使用，未配置，则不能使用
export function getTenantConfig(){
  return request({
    url: `/mgr/jvs-auth/tenant/admin/base/check`,
    method:'get'
  })
}

// 获取上传凭证
export function getCertificate(data){
  return request({
    url: `/mgr/jvs-auth/largeFile/uploadCertificate/${data.bucketName}/${data.filename}/${data.num}`,
    method: 'get',
    params: {module : (data.module || '/jvs-ui/form/')}
  })
}

// 三方应用登录对接类型列表
export const getOtherTypeList = () => {
  return request({
    url: `/auth/just/types`,
    method: 'get'
  })
}

// 获取应用标识映射
export function getIdentificationMappings(identifiers){
  return request({
    url:`/mgr/jvs-design/base/identification/mappings`,
    method:'post',
    data: {
      identifiers: identifiers
    }
  })
}




// 执行逻辑通过标识内部调用
export const ruleRunApi = (jvsAppIdentification,ruleIdentification, data, method) => {
/**
 * 参数说明
 * {jvsAppIdentification} 必须传递应用标识，不能为应用Id
 * {ruleIdentification}  逻辑标识，支持路径形式
 * {data}     入参，作为逻辑流转过程中使用的参数
 * {method}  请求类型 ，post delete put get ,默认为post
*/
  let tp = {
    url: `/mgr/jvs-design/rule/openapi/${jvsAppIdentification}/${ruleIdentification}`,
    method: method || 'post',
  }
  if(data) {
    if(method == 'get'){
      tp.params = data
    }else{
      tp.data = data
    }
  }
  return request(tp)
}