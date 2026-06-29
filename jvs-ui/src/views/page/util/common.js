import moment from "moment";

/**
 * 扩展组件、日期组件默认值
 */
export function getDefaultData(obj, arr, userInfo) {
  arr.forEach(item => {
    if(item.isDefault) {
      switch (item.type) {
        case 'user':
          obj[item.prop] = obj[item.prop] ? obj[item.prop] : (item.multiple ? [userInfo.id] : userInfo.id);
          obj[item.prop + '_1'] = obj[item.prop + '_1'] ? obj[item.prop + '_1'] : (item.multiple ? [userInfo.realName].join(',') : userInfo.realName);
          break;
        case 'department':
          if(userInfo.deptId) {
            obj[item.prop] = obj[item.prop] ? obj[item.prop] : (item.multiple ? [userInfo.deptId] : userInfo.deptId);
          }
          if(userInfo.deptName) {
            obj[item.prop + '_1'] = obj[item.prop + '_1'] ? obj[item.prop + '_1'] : (item.multiple ? [userInfo.deptName].join(',') : userInfo.deptName);
          }
          if(userInfo.dept && userInfo.dept instanceof Array && userInfo.dept.length > 0 && item.multiple) {
            let ids = []
            let names = []
            userInfo.dept.filter(dit => {
              ids.push(dit.deptId)
              names.push(dit.deptName)
            })
            if(ids.length > 0) {
              obj[item.prop] = ids
            }
            if(names.length > 0) {
              obj[item.prop + '_1'] = names
            }
          }
          break;
        case 'job':
          obj[item.prop] = obj[item.prop] ? obj[item.prop] : (item.multiple ? [userInfo.jobId] : userInfo.jobId);
          obj[item.prop + '_1'] = obj[item.prop + '_1'] ? obj[item.prop + '_1'] : (item.multiple ? [userInfo.jobName].join(',') : userInfo.jobName);
          break;
        case 'role':
          if(item.multiple) {
            obj[item.prop] = obj[item.prop] ? obj[item.prop] : userInfo.roleIds;
          }
          break;
        default: break;
      }
    }
    // 日期组件默认值
    if(item.defaultDate && (!item.formula)) {
      switch(item.datetype) {
        case 'date':
          obj[item.prop] = obj[item.prop] ? obj[item.prop] : moment().format('YYYY-MM-DD')
          break;
        case 'week':
          obj[item.prop] = obj[item.prop] ? obj[item.prop] : moment().format('YYYY-MM-DD')
          break;
        case 'month':
          obj[item.prop] = obj[item.prop] ? obj[item.prop] : moment().format('YYYY-MM')
          break;
        case 'year':
          obj[item.prop] = obj[item.prop] ? obj[item.prop] : moment().format('YYYY')
          break;
        case 'dates':
          obj[item.prop] = obj[item.prop] ? obj[item.prop] : moment().format('YYYY-MM-DD')
          break;
        case 'datetime':
          obj[item.prop] = obj[item.prop] ? obj[item.prop] : moment().format('YYYY-MM-DD HH:mm:ss')
          break;
        case 'datetimerange':
          obj[item.prop] = obj[item.prop] ? obj[item.prop] : [moment().format('YYYY-MM-DD 00:00:00'), moment().format('YYYY-MM-DD 23:59:59')]
          break;
        case 'daterange':
          obj[item.prop] = obj[item.prop] ? obj[item.prop] : [moment().format('YYYY-MM-DD'), moment().format('YYYY-MM-DD')]
          break;
        case 'monthrange':
          obj[item.prop] = obj[item.prop] ? obj[item.prop] : [moment().format('YYYY-MM'), moment().format('YYYY-MM')]
          break;
        default: break;
      }
    }
  })
  return obj
}
