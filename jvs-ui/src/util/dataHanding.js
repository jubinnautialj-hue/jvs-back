/**
 * 根据数组[{},{},{}...]中某个对象的key的value，查询该对象另一个key的value
 * @param {Array} array 数组
 * @param {String} queryKey 要查询的key
 * @param {String} queryValue 要查询的value
 * @param {String} getKey 要获取的key
 */
export function getValueByKey(array, queryKey, queryValue, getKey) {
  let getValue = "";
  array.forEach((item) => {
    item[queryKey] !== undefined &&
      queryValue !== undefined &&
      item[queryKey].toString() === queryValue.toString() &&
      (getValue = getKey ? item[getKey] : item);
  });
  return getValue;
}

/**
 * 按照对象数组[{},{},{}...]的某个object key，进行数组排序
 * @param {String} key 要排序的key
 * @param {String} sort 正序/倒序：asc/desc，默认为asc
 */
export function arraySort(key, sort) {
  return function (a, b) {
    if (sort === "asc" || sort === undefined || sort === "") {
      // 正序：a[key] > b[key]
      if (a[key] > b[key]) return 1;
      else if (a[key] < b[key]) return -1;
      else return 0;
    } else if (sort === "desc") {
      // 倒序：a[key] < b[key]
      if (a[key] < b[key]) return 1;
      else if (a[key] > b[key]) return -1;
      else return 0;
    }
  };
}
