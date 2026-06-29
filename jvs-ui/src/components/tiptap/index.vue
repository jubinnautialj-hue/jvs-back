<template>
  <div :class="className" @paste.stop.prevent="pasteImg($event)">
    <el-tiptap
      class="editor-box"
      :id="id"
      :height="height"
      :ref="refs"
      lang="zh"
      :charCounterCount="false"
      :extensions="extensions"
      @onUpdate="onUpdate"
      :content="html"
      :spellcheck="false"
      :menu-bubble-options="{ 'keep-in-bounds': false }"
      :placeholder="placeholder"
      :showMenubar="false"
    />
  </div>
</template>

<script>
import {Loading} from 'element-ui'
export default {
  props:{
    className:{
      type:String,
      default:""
    },
    height:{
      type:String,
      default:'455px'
    },
    id:{
      type:String,
      default:'editor-box'
    },
    refs:{
      type:String,
      default:'tiptapEdit'
    },
    html:{
      type:String,
      default:''
    },
    placeholder:{
      type:String,
      default:'请输入内容'
    },
    extensions:{
      type:Array,
      default:[]
    }
  },
  data() {
    return {

    };
  },
  created() {

  },
  mounted() {

  },
  methods: {
    pasteImage (url) {
      let editor = this.$refs[this.refs].editor
      let view = editor.view
      const node =  view.state.schema.nodes.image.create({
        src: url,
        height:100
      })
      const transaction = view.state.tr.replaceSelectionWith(node);
      view.dispatch(transaction)
    },
    pasteImg(e) {
      const cbd = e.clipboardData;
	    const ua = window.navigator.userAgent;
	    // 如果是 Safari 直接 return
	    if ( !(e.clipboardData && e.clipboardData.items) ) {
	      return ;
	    }
	    if(cbd.items && cbd.items.length === 2 && cbd.items[0].kind === "string" && cbd.items[1].kind === "file" &&
        cbd.types && cbd.types.length === 2 && cbd.types[0] === "text/plain" && cbd.types[1] === "Files" &&
        ua.match(/Macintosh/i) && Number(ua.match(/Chrome\/(\d{2})/i)[1]) < 49){
        return;
	    }
	    for(let i = 0; i < cbd.items.length; i++) {
        let item = cbd.items[i];
        if (item.kind == 'string') {
          return;
        }
        if(item.kind == "file"){
          // blob 就是从剪切板获得的文件，可以进行上传或其他操作
          const blob = item.getAsFile();
          // console.log(blob)
          if (blob.size === 0) {
            return;
          }
          const fd = new FormData()
          fd.append('file', blob)
          fd.append('module','/jvs-ui/form/')
          $.ajax({
            url : '/mgr/jvs-auth/upload/jvs-public',
            type : 'POST',
            data : fd,
            headers: {
              "Authorization" : 'Bearer ' + this.$store.getters.access_token
            },
            // 用于对data参数进行序列化处理 这里必须false
            processData : false,
            // 不去设置Content-Type请求头
            contentType : false, //必须
            beforeSend:function(){},
            success : function(result) {},
            error : function(res) {
              console.log("error");
            }
          }).then(result => {
              const env = process.env
              let wstUrl = ''
              // let WS = window.location.protocol=='http:'?'ws':"wss"    //url
              let WS = window.location.protocol=='http:'?'http':"https"   //src
              if (env.NODE_ENV == 'development') {
                wstUrl = `${WS}://` + location.host
              } else {
                wstUrl = `${WS}://` + location.host
              }
              result.data.fileLink = wstUrl+result.data.fileLink
              // console.log(result.data.fileLink)
            if(result.code == 0 && result.data && result.data.fileLink) {
              this.pasteImage(result.data.fileLink);
            }else{
              console.log("失败");
            }
          })
        }
      }
    },
    onUpdate(output, options) {
      const { getHTML,getJSON } = options;
      if(getHTML().indexOf("data:image")!=-1){
        let arr = getJSON().content
        // 获取所有的img标签
        let imgDom = document.getElementById(this.id).getElementsByTagName('img')
        arr.forEach((item,index)=>{
          if(item.content){
            item.content.forEach((items,indexs)=>{
              if(items.type=='image' && items.attrs.src.indexOf("data:image")!=-1){
                // base64转换file
                let arrD = items.attrs.src.split(',')
                let mime = arrD[0].match(/:(.*?);/)[1] , bstr  = atob(arrD[1]),n = bstr.length,u8arr = new Uint8Array(n)
                while(n--){
                  u8arr[n] = bstr.charCodeAt(n)
                }
                let file = new File([u8arr],new Date().getTime(),{
                  type:mime
                })
                for(let i = 0;i<imgDom.length;i++){
                  if(imgDom[i].getAttribute('src') && imgDom[i].getAttribute('src').indexOf("data:image")!=-1 && !items.id){
                    items.id = 'image-upload-'+index+'-'+indexs+'-'+i
                    imgDom[i].parentNode.setAttribute('id',items.id)
                    items.loading = Loading.service({
                      target: document.getElementById(items.id),
                    });
                  }
                }
                if(file.size/1024/1024<=5){
                  const fd = new FormData()
                  fd.append('file', file)
                  fd.append('module','/jvs-ui/form/')
                  $.ajax({
                    url : '/mgr/jvs-auth/upload/jvs-public',
                    type : 'POST',
                    data : fd,
                    headers: {
                      "Authorization" : 'Bearer ' + this.$store.getters.access_token
                    },
                    // 用于对data参数进行序列化处理 这里必须false
                    processData : false,
                    // 不去设置Content-Type请求头
                    contentType : false, //必须
                    beforeSend:function(){},
                    success : function(result) {},
                    error : function(res) {
                      console.log("error");
                    }
                  }).then(result => {
                    const env = process.env
                    let wstUrl = ''
                    // let WS = window.location.protocol=='http:'?'ws':"wss"    //url
                    let WS = window.location.protocol=='http:'?'http':"https"   //src
                    if (env.NODE_ENV == 'development') {
                      wstUrl = `${WS}://` + location.host
                    } else {
                      wstUrl = `${WS}://` + location.host
                    }
                    result.data.fileLink = wstUrl+result.data.fileLink
                    // console.log(result.data.fileLink)
                    if(result.code == 0 && result.data && result.data.fileLink) {
                      items.attrs.src = result.data.fileLink
                    }else{
                      console.log("失败");
                    }
                    items.loading.close();
                    this.$emit('changeHtml', getHTML())
                  })
                }else{
                  items.attrs.src = ''
                  items.loading.close();
                  this.$emit('changeHtml', getHTML())
                }
              }
            })
          }
        })
      }else{
        this.$emit('changeHtml', getHTML())
      }
    },
  }
};
</script>

<style scoped lang="scss">
.commentTipTap{
  /deep/.el-tiptap-editor__menu-bar{
    padding:0px;
    border: none;
  }
  /deep/.el-tiptap-editor__content{
    border: none;
    padding: 0px 20px;
  }
  /deep/.el-tiptap-editor__menu-bar:before{
    height: 0px;
  }
  /deep/.ProseMirror{
    min-height: 100px;
  }
}
</style>
<style lang="scss">
.el-tiptap-popper__menu{
  padding: 0 12px;
}
</style>
