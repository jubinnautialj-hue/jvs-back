export function clone(obj) {
  if (obj === null) return null
  if (typeof obj !== 'object') return obj
  if (obj.constructor === Date) return new Date(obj)
  if (obj.constructor === RegExp) return new RegExp(obj)
  const newObj = new obj.constructor()
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      const val = obj[key]
      newObj[key] = typeof val === 'object' ? clone(val) : val
    }
  }
  return newObj
}

// 获取UID
export function uid() {
  return Math.random().toString(16).slice(2)
}

export function getMaxY(list){
  const yList = list.map(item => item.y || 0)
  const maxY = Math.max(...yList)
  const index = yList.indexOf(maxY)
  if (~index) {
    const h = list[index].h
    return (maxY + h) || 0
  } else {
    return 0
  }
}

// 获取文件类型
export function getFileType(path) {
  const index = path.lastIndexOf('.')
  if (~index) {
    return path.substr(index + 1).toLocaleLowerCase()
  }
  return null
}