/**
 * 不打开链接，直接下载
 */
import { downloadRequest } from "@/api/common";

export default {
  install(Vue, options) {
    Vue.prototype.$downloadUrl = function (item) {
      // console.log(item);
      let { url, name, uid } = item;
      if (url) {
        let link = document.createElement("a");
        link.style.display = "none";
        if (url.startsWith("/mgr") && !url.includes("print")) {
          // 文件流
          url = `/mgr/document/dcLibrary/file/get/file/${item.id}`;
          downloadRequest(url).then((res) => {
            name = res.headers["content-disposition"].split(";")[1];
            name = name.split("=")[1];
            name = decodeURI(name);
            if (name) {
              link.download = name;
            }
            let isExcel = false;
            if (
              name &&
              name.includes(".") &&
              ["xlsx", "xls"].indexOf(name.split(".")[1]) > -1
            ) {
              isExcel = true;
            }
            var blob = new Blob(
              [res.data],
              isExcel
                ? {
                    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8",
                  }
                : null
            );
            link.href = URL.createObjectURL(blob);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
          });
        } else {
          // 文件
          if (window.navigator.userAgent.includes("DingTalk")) {
            if (url.includes("?")) {
              url += "&ddtab=true";
            } else {
              url += "?ddtab=true";
            }
          }

          const xhr = new XMLHttpRequest();
          xhr.open("GET", url, true);
          xhr.responseType = "blob"; // 设置响应类型为Blob

          // 监听进度
          xhr.onprogress = (event) => {
            console.log(event);
            if (event.lengthComputable) {
              // 显示百分比
              const percentComplete = (event.loaded / event.total) * 100;
              console.log(`下载进度: ${percentComplete.toFixed(2)}%`);
              this.$showProgress({
                name: name || "下载文件",
                uid,
                percentComplete,
              });
            } else {
              // 显示字节数
              console.log(`下载进度: ${event.loaded}kb`);
            }
          };

          xhr.onload = () => {
            const blob = xhr.response;
            const downloadUrl = URL.createObjectURL(blob);
            link.href = downloadUrl;
            link.download = name || "下载文件";
            link.click();
            document.body.removeChild(link);
          };

          xhr.send();

          // fetch(url)
          //   .then((res) => {
          //     // console.log(res);
          //     return res.blob();
          //   })
          //   .then((blob) => {
          //     link.href = URL.createObjectURL(blob);
          //     link.download = name || "下载文件";
          //     document.body.appendChild(link);
          //     link.click();
          //     document.body.removeChild(link);
          //   });
        }
      }
    };
  },
};
