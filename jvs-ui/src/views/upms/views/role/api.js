import request from "@/router/axios";

// 获取角色和分组信息
export function getRoleGroupList () {
  return request({
    url: `/mgr/jvs-auth/role/all/group`,
    method: 'get'
  })
}

// 获取全部角色信息
/**
 * tenantRole  租户角色
 * userRole   用户角色
 */
export function getRoleList(type, data) {
  return request({
    url: `/mgr/jvs-auth/role/all`,
    method: "GET",
    params: data
  });
}

// 角色下的用户
export function getUserByRoleId(params) {
  return request({
    url: `/mgr/jvs-auth/role/user`,
    method: "GET",
    params: params
  });
}

// 添加角色
export function addRole(data) {
  return request({
    url: "/mgr/jvs-auth/role/save",
    method: "POST",
    data: data
  });
}
// 修改角色
export function editRole(data) {
  return request({
    url: "/mgr/jvs-auth/role/update",
    method: "PUT",
    data: data
  });
}

// 删除角色
export function delRoleId(id) {
  return request({
    url: `/mgr/jvs-auth/role/${id}`,
    method: "DELETE"
  });
}

// 移除用户
export function removeUSerByRoleId(roleId, userId) {
  return request({
    url: `/mgr/jvs-auth/role/user/${roleId}/${userId}`,
    method: "DELETE"
  });
}

// 获取角色数据权限
export function getRoleDataAuth(roleId) {
  return request({
    url: `/mgr/jvs-auth/role/role/data/${roleId}`,
    method: "GET",
  });
}

// 获取所有数据权限
export function getAllDataAuth() {
  return request({
    url: `/mgr/jvs-auth/role/role/data`,
    method: "GET",
  });
}

// 角色授权数据权限
export function bindRoleDataAuth(roleId, data) {
  return request({
    url: `/mgr/jvs-auth/role/role/data/${roleId}`,
    method: "PUT",
    data: data,
  });
}

// 获取所有菜单
export function getAllMenu(params) {
  return request({
    url: `/mgr/jvs-auth/role/role/menus`,
    method: "GET",
    params: params,
  });
}

// 获取角色菜单权限
export function getMenuAuth(roleId) {
  return request({
    url: `/mgr/jvs-auth/role/role/${roleId}`, //`/mgr/jvs-auth/role/role/menus/${roleId}/${clientId}`,
    method: "GET",
  });
}

// 角色授权
export function bindMenuAuth(data, roleId) {
  return request({
    url: `/mgr/jvs-auth/role/role/${roleId}`, // `/mgr/jvs-auth/role/role/${roleId}/${applyKey}`,
    method: "PUT",
    data: data,
  });
}

// 用户添加到某个角色
export function editUserRole(roleId, data) {
  return request({
    url: `/mgr/jvs-auth/role/user/${roleId}`,
    method: "put",
    data: data
  });
}

// 编辑菜单
export function editMenu(data) {
  return request({
    url: "/mgr/jvs-auth/menu/menu",
    method: "POST",
    data: data
  });
}

// 编辑菜单
export function editButton(data, menuId) {
  return request({
    url: `/mgr/jvs-auth/menu/button/${menuId}`,
    method: "POST",
    data: data
  });
}

// 删除菜单
export function deleteMenu(id) {
  return request({
    url: `/mgr/jvs-auth//menu/menu/${id}`,
    method: "delete",
  });
}


// 获取资源树
export function getPermissionTree() {
  return request({
    url: `/mgr/jvs-auth/permission/list`,
    method: "GET",
  })
}

// 获取全部资源
export function getPermissionAll() {
  return request({
    url: `/mgr/jvs-auth/permission/all`,
    method: "GET",
  })
}

// 新增资源
export function addPermission(data) {
  return request({
    url: `/mgr/jvs-auth/permission`,
    method: "post",
    data: data
  })
}

// 修改资源
export function editPermission(data) {
  return request({
    url: `/mgr/jvs-auth/permission`,
    method: "put",
    data: data
  })
}

// 删除资源
export function delPermission(id) {
  return request({
    url: `/mgr/jvs-auth/permission/${id}`,
    method: "delete"
  })
}

// 获取某个角色下的部门列表
export function getDeptByRoleId(params) {
  return request({
    url: `/mgr/jvs-auth/role/dept`,
    method: "get",
    params: params
  })
}

// 部门添加到某个角色
export function addDeptRole(roleId, data) {
  return request({
    url: `/mgr/jvs-auth/role/dept/${roleId}`,
    method: "post",
    data: data
  });
}

// 修改角色下的部门
export function editDeptRole(roleId, data) {
  return request({
    url: `/mgr/jvs-auth/role/dept/${roleId}`,
    method: "put",
    data: data
  });
}

// 移除角色部门
export function removeDeptByRoleId(roleId, deptId) {
  return request({
    url: `/mgr/jvs-auth/role/dept/${roleId}/${deptId}`,
    method: "delete"
  })
}

// 角色用户管理范围
export function saveDeptRange(data) {
  return request({
    url: `/mgr/jvs-auth/role/user/scope`,
    method: "put",
    data: data
  });
}

// 获取所有的角色分组
export function getRoleGroupArray () {
  return request({
    url: `/mgr/jvs-auth/role/group`,
    method: 'get',
  })
}

// 新增、修改用户分组
export function addRoleGroup (data) {
  return request({
    url: `/mgr/jvs-auth/role/group`,
    method: "post",
    data: data
  })
}

// 删除角色分组
export function removeRoleGroup (id) {
  return request({
    url: `/mgr/jvs-auth/role/group/${id}`,
    method: "delete"
  })
}