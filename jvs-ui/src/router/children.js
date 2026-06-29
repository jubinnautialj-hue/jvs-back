import upmsRouter from "@/views/upms/router";
import pageRouter from "@/views/page/router"
import ruleRouter from "@/views/rule/router"
import flowableRouter from "@/views/flowable/router"
import docRouter from "@/views/document/router"
import weixinMPRouter from "@/views/weixin-mp/router"
import iconRouter from '@/views/iconmanage/router'
import printRputer from '@/views/print/router'
import customviewRouter from '@/views/customView/router'
import devDemoViewRouter from '@/views/devDemoView/router'
// 第三步 demoIndex 源代码接入 注册组件路由
import demoIndex from '@/views/demoIndex/router'
export default [...demoIndex,...upmsRouter, ...pageRouter, ...ruleRouter, ...flowableRouter, ...docRouter, ...weixinMPRouter, ...iconRouter, ...printRputer, ...customviewRouter, ...devDemoViewRouter]

// 监听子页面传值
window.addEventListener('message',function(e){
  if(e.data) {
    if(e.data.command == 'fresh') {
      location.reload()
    }
  }
},false);
