<template>
  <div>
    child iframe
  </div>
</template>

<script>
  export default {
    mounted(){
      window.addEventListener('message', this.handleReceiveMsg)
    },
    methods:{
      handleReceiveMsg(e){
        if (Object.prototype.toString.call(e.data) === '[object String]') {
          // 过滤开发环境来自webpack发送的possMessage 消息
          const message = JSON.parse(e.data);
          if(message.command==="jvs-iframe-msg"){
            // 约定的command为jvs-iframe-msg
            console.log(message.params)
          }
        }
      }
    }
  }
</script>
