<template>
  <div class="form-log-page">
    <div ref="rscroll" :class="{'log-list': true, 'loading': logLoading}">
      <div v-for="(item, index) in logList" :key="'log-list-item-'+index" class="list-item">
        <div class="not-comment">
          <div class="not-comment-info">
            <img :src="item.headImg" alt="">
            <span>{{item.realName}}</span>
          </div>
          <span>{{item.updateTime}}</span>
        </div>
        <div v-if="item.content &&  item.content.attachment && item.content.attachment.length > 0" class="file-list">
          <div v-for="(file, fix) in item.content.attachment" :key="'file-item-'+fix" class="file-list-item">
            <span class="file-name" @click="previewFile(file)">{{file.originalFileName}}</span>
          </div>
        </div>
        <div class="log-content" style="cursor: pointer;font-size: 14px;padding: 1px 15px;background-color: #f9fcfd;margin-top:10px;border-radius:5px;">
          <div class="m-t-xs img-mtxs" @click="handleImgClick" v-html="checkLink(item.content.contents)"></div>
        </div>
      </div>
    </div>
    <div class="footer">
      <div class="footer-file">
        <el-upload
          ref="headImgUpload"
          action="/mgr/jvs-auth/upload/jvs-public"
          :file-list="imgFileList"
          :show-file-list="true"
          :data="{module: '/jvs-ui/form/'}"
          :on-remove="headImgRemove"
          :headers="headers"
          :on-success="headImgSuccessHandle"
          :on-error="headImgErrHandle">
          <i class="el-icon-paperclip" style="font-size: 24px;cursor: pointer;"></i>
        </el-upload>
      </div>
      <div class="footer-comment">
        <span style="flex: 1;width: 100%;">
          <tipTapComponent height="100px" slot="reference" id="commentTipTap" ref="commentText" refs="commentTipTap" :html="comment"
            className='commentTipTap' placeholder="输入评论可粘贴图片" :extensions="commentExtensions" @changeHtml="changeHtml">
          </tipTapComponent>
        </span>
        <el-button type="primary" size="mini" :loading="commentLoading" style="margin-top: 10px;height: 32px;font-size: 14px;" @click="submitComment">评论</el-button>
      </div>
    </div>
    <!-- 预览图片 -->
    <el-image-viewer v-if="showViewer" :z-index="9999" :on-close="closeViewer" :url-list="[previewSrcList]"/>
  </div>
</template>
<script>
import {
  Doc,
  Text,
  Paragraph,
  Bold,
  Underline,
  Italic,
  ListItem,
  BulletList,
  OrderedList,
  TodoItem,
  TodoList,
  TextAlign,
  Indent,
  Link,
  Image,
  Table,
  TableHeader,
  TableCell,
  TableRow,
  TrailingNode,
} from 'element-tiptap'
import { Base64 } from 'js-base64'
import tipTapComponent from '@/components/tiptap/index'
import { getStore } from '@/util/store.js'
import { getDataLogList, addDataLogComment } from '@/views/page/api/newDesign'
export default {
  name: 'formLog',
  components: {
    tipTapComponent,
    'el-image-viewer': () => import('element-ui/packages/image/src/image-viewer')
  },
  props: {
    jvsAppId: {
      type: String
    },
    dataModelId: {
      type: String
    },
    dataId: {
      type: String
    }
  },
  data () {
    return {
      previewSrcList: [],
      showViewer: false,
      logLoading: false,
      logList: [],
      imgFileList: [],
      headers: {
        tenantId: this.$store.getters.userInfo.tenantId,
        Authorization: ('Bearer '+ this.$store.getters.access_token)
      },
      comment:'',
      commentExtensions:[
        new Doc(),
        new Text(),
        new Paragraph(),
        new Image({
          uploadRequest(file) {
            const fd = new FormData()
            fd.append('file', file)
            fd.append('module','/jvs-ui/form/')
            return $.ajax({
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
              if(result.code == 0 && result.data && result.data.fileLink) {
                return result.data.fileLink
              }else{
                console.log("失败");
              }
            })
          }
        }),
        new Link({ bubble: true }),
      ],
      commentLoading: false,
    }
  },
  created () {
    this.getList()
  },
  methods: {
    getList () {
      this.logLoading = true
      getDataLogList(this.jvsAppId, this.dataModelId, this.dataId).then(res => {
        if(res.data && res.data.code == 0) {
          this.logList = res.data.data
          this.$nextTick(() => {
            this.$refs.rscroll.scrollTop = this.$refs.rscroll.scrollHeight
          })
        }
        this.logLoading = false
      }).catch(e => {
        this.logLoading = false
      })
    },
    headImgRemove (file, fileList) {
      this.imgFileList = fileList
    },
    headImgSuccessHandle (res, file, fileList) {
      if(res.code == 0) {
        this.imgFileList = JSON.parse(JSON.stringify(fileList))
        this.imgFileList.splice(fileList.length-1, 1, {...res.data, name: res.data.originalFileName})
        this.$notify({
          title: '提示',
          message: '上传成功',
          position: 'bottom-right',
          type: 'success'
        })
        this.$forceUpdate()
      }else{
        this.$refs.headImgUpload.clearFiles()
        this.$notify({
          title: '提示',
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        })
      }
    },
    headImgErrHandle (err, file, fileList) {
      this.$refs.headImgUpload.clearFiles()
      this.$notify({
        title: '提示',
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
    // 富文本框评论值更新
    changeHtml(val){
      this.comment = val
    },
    submitComment () {
      this.commentLoading = true
      addDataLogComment(this.jvsAppId, this.dataModelId, this.dataId, {attachment: this.imgFileList, contents: this.comment}).then(res => {
        if(res.data && res.data.code == 0) {
          this.imgFileList = []
          this.comment = ''
          this.getList()
        }
        this.commentLoading = false
      }).catch(e => {
        this.commentLoading = false
      })
    },
    previewFile (row) {
      let protocolhost = getStore({name: 'kkfileUrl'}) || ''
      if(protocolhost && row.fileLink) {
        let view_url = `${protocolhost}/onlinePreview?forceUpdatedCache=true&officePreviewType=pdf&url=` + encodeURIComponent(Base64.encode(decodeURIComponent((row.fileLink.startsWith('/') ? `${location.origin}` : '') + row.fileLink)))
        this.$openUrl(view_url, '_blank')
      }
    },
    checkLink (text) {
      let reg = /(http:\/\/|https:\/\/)((\w|=|\?|\.|\/|&|-)+)/g;
      if(!reg.exec(text)){
        return text
      }
      return text;
    },
    // 图片预览
    handleImgClick(text) {
      //判断不是图片时则不预览大图
      if(this.previewSrcList = text.target.currentSrc) {
        this.showViewer = true
      }else if(!text.target.currentSrc){
        this.showViewer = false
      }
    },
    // 关闭图片预览
    closeViewer() {
      this.showViewer = false
    },
  }
}
</script>
<style lang="scss" scoped>
.form-log-page{
  height: 100%;
  display: flex;
  flex-direction: column;
  .log-list{
    flex: 1;
    padding: 13px 20px 0 20px;
    overflow: auto;
    box-sizing: border-box;
    .list-item{
      display: flex;
      justify-content: flex-start;
      align-items: baseline;
      vertical-align: middle;
      flex-direction: column;
      width: 100%;
      .not-comment{
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
        color: #606266;
        font-size: 14px;
        .not-comment-info{
          display: flex;
          align-items: center;
          img{
            display: block;
            width: 24px;
            height: 24px;
            border-radius: 50%;
            margin-right: 10px;
          }
        }
        .tool-btn{
          margin-left: 10px;
          display: none;
          font-size: 12px;
          cursor: pointer;
          color: #1E6FFF;
        }
        &:hover{
          .tool-btn{
            display: inline;
          }
        }
      }
      .file-list{
        width: 100%;
        .file-list-item{
          width: 100%;
          height: 24px;
          margin-top: 6px;
          font-size: 12px;
          color: #1E6FFF;
          line-height: 24px;
          padding-left: 34px;
          padding-right: 10px;
          box-sizing: border-box;
          .file-name{
            cursor: pointer;
            width: 100%;
            display: inline-block;
            overflow: hidden;
            white-space: pre;
            text-overflow: ellipsis;
          }
          &:hover{
            background: #F5F6F7;
          }
        }
      }
      .log-content{
        overflow: hidden;
        text-overflow: ellipsis;
        background-color: #f9fcfd;
        border-radius: 5px;
        padding-left: 12px;
        margin-bottom: 6px;
        margin-top: 6px;
        width: 100%;
        box-sizing: border-box;
        /deep/.img-mtxs{
          img{
            max-width: 50%;
            width: fit-content;
            height: unset;
          }
        }
      }
    }
    &.loading{
      background-color: rgba(255, 255, 255, 0.8);
      background-image: url('../../../../../public/jvs-ui-public/img/loading.gif');
      background-repeat: no-repeat;
      background-position: center;
      background-position: center;
      z-index: 100;
    }
  }
  .footer{
    padding: 0 18px 10px 0;
    box-sizing: border-box;
    border-top: 1px solid #e5e5e5;
    width: 100%;
    background-color: #fff;
    min-height: 150px;
    display: flex;
    flex-direction: column;
    .footer-file{
      padding: 6px 0 6px 14px;
    }
    .footer-comment{
      flex: 1;
      display: flex;
      align-items: flex-end;
      flex-direction: column;
      border: none !important;
    }
  }
}
</style>
