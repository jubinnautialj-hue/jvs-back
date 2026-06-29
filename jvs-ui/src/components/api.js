import request from '@/router/axios'

// 用户列表
export function getUserList(query) {
  return request({
    url: "/mgr/jvs-auth/usermanager/list",
    method: "get",
    params: query
  });
}

// 获取用户的部门菜单
export function getDeptList() {
  return request({
    url: "/mgr/jvs-auth/dept/all",
    method: "get",
  });
}

// 获取全部角色信息
export function getRoleList() {
  return request({
    url: "/mgr/jvs-auth/role/all",
    method: "GET"
  });
}

// 获取职位列表
export function getPostList() {
  return request({
    url: "/mgr/jvs-auth/job/list",
    method: "GET"
  });
}

// 数据源下拉框
export function getDataSourceInfoList(data) {
  return request({
    url: "/mgr/jvs-design/datasource/dataSelect",
    method: "post",
    data: data
  });
}

// 获取图片列表
export const getImageList = (params) => {
  return request({
    url: `/mgr/jvs-auth/sys/file/list`,
    method: 'get',
    params
  })
}

// 获取标签集
export const getLabelList = (params) => {
  return request({
    url: `/mgr/jvs-auth/fileLabel`,
    method: 'get',
    params
  })
}

// 获取数据集信息
export function getModelName(jvsAppId, modelId) {
  return request({
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/modelName/${modelId}`,
    method: "get"
  });
}

// 修改数据集名称
export function editModelName(jvsAppId, modelName, modelId) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/dynamic/data/${modelName}/${modelId}`,
    method: "put"
  });
}

// 修改数据集编号
export function editModelNo(jvsAppId, modelId, params) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/dynamic/data/collection_name/${modelId}`,
    method: "put",
    params: params
  });
}

// 数据模型配置
export function editModelSetting(jvsAppId, modelId, data) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/dynamic/data/${modelId}`,
    method: "post",
    data: data
  });
}

// 数据联动
export function dataModelTriggering(jvsAppId, designId, modelId, data, headers) {
  let tp = {
    url: `/mgr/jvs-design/app/use/${jvsAppId}/dynamic/data/DataModelTriggering/${designId}/${modelId}`,
    method: "post",
    data: data
  }
  if(headers) {
    tp.headers = headers
  }
  return request(tp);
}

// 文件导入
export function fileImport(data, onUploadProgressBack, businessId){
  let headers = {
    serialize:false,
    type: "FormData",
    "Content-Type": "application/x-www-form-urlencoded"
  }
  if(businessId) {
    headers.businessId = businessId
  }
  return request({
    url: `/mgr/jvs-auth/upload/jvs-form-design`,
    method: 'post',
    headers: headers,
    data:data,
    onUploadProgress(progressEvent){
      let val= (progressEvent.loaded / progressEvent.total * 100).toFixed(0);
      if(onUploadProgressBack && typeof onUploadProgressBack == 'function'){
        onUploadProgressBack(val);
      }
    }
  })
}

// 获取数据模型的字段列表
export function getModelFieldList(jvsAppId, modelId) {
  return request({
    url: `/mgr/jvs-design/app/design/${jvsAppId}/data/model/design/fields/${modelId}`,
    method: "get"
  });
}

// 获取已发布工作流审批节点设计集合
export function getFlowNodeListById(designId, params) {
  return request({
    url: `/mgr/jvs-design/base/workflow/task/node/approve/nodes/${designId}`,
    method: "get",
    params: params
  });
}

// 删除文件
export const deleteFile = (data) => {
  return request({
    url: `/mgr/jvs-auth/sys/file`,
    method: 'delete',
    params:  data
  })
}

// 获取动态表单组件可设计属性
export function getDynamicFormTypeAttr() {
  return request({
    url: `/mgr/jvs-design/base/form/design/dynamic/field/attribute`,
    method: "get",
  });
}