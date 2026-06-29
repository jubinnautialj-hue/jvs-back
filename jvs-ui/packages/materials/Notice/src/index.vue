<template>
  <div class="notice-box">
    <div v-if="noticeList && noticeList.length > 0" class="notice-list">
      <div v-for="item in noticeList" :key="item.id" class="notice-list-item" @click="openItem(item)">
        <div :class="'type ' + item.label">{{item.label}}</div>
        <div class="content">{{item.title}}</div>
        <div class="time">{{item.createTime}}</div>
      </div>
    </div>
    <div v-else class="empty-block"></div>
    <!-- 详情 -->
    <el-dialog
      :title="bulletin.title + '公告'"
      :visible.sync="dialogVisible"
      width="800px"
      append-to-body
      :before-close="handleClose">
      <div style="max-height: 800px;overflow-y: auto">
        <section v-html="bulletin.content"></section>
      </div>
    </el-dialog>
    <el-dialog
      class="image-dialog-box"
      :visible.sync="imgVisible"
      fullscreen
      append-to-body
      :before-close="handleClose">
      <div class="dialog-box" @click="handleClose">
        <img v-if="imgVisible" :src="bulletin.content" alt=""/>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getBulletinList } from '../../../../src/api/admin/home'
export default {
  name: 'Notice',
  props: {
    element: {
      type: Object
    },
    componentSetting: {
      type: Object,
      default() {
        return {}
      }
    },
    options: {
      type: Object,
      default() {
        return {}
      }
    },
  },
  data () {
    return {
      noticeList: [],
      dialogVisible: false,
      imgVisible: false,
      bulletin: {},
    }
  },
  created () {
    this.getList()
  },
  methods: {
    getList () {
      getBulletinList({count: 5}).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.noticeList = res.data.data
        }
      })
    },
    openItem (item) {
      this.bulletin = JSON.parse(JSON.stringify(item))
      if(this.bulletin.content && this.bulletin.content.length > 0) {
        this.bulletin.content = this.bulletin.content.replace('<img ', `<img style="max-width: 710px;"`)
        if(this.bulletin.contentType === 'TEXT') {
          this.dialogVisible = true
        }else {
          this.imgVisible = true
        }
      }
    },
    handleClose() {
      this.dialogVisible = false
      this.imgVisible = false
    },
  }
}
</script>

<style lang="scss" scoped>
.notice-box{
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  .notice-list{
    padding-left: 24px;
    padding-right: 12px;
    margin-right: 12px;
    height: 100%;
    font-family: Source Han Sans-Regular, Source Han Sans;
    font-weight: 400;
    overflow: hidden;
    overflow-y: auto;
    .notice-list-item{
      margin-top: 17px;
      display: flex;
      align-items: center;
      cursor: pointer;
      .type{
        width: 44px;
        height: 20px;
        border-radius: 2px;
        text-align: center;
        line-height: 20px;
        &.活动{
          background: #FFF7EF;
          color: #FF9736;
        }
        &.消息{
          background: #E8FFFB;
          color: #14C9C9;
        }
        &.通知{
          background: #E8F3FF;
          color: #1E6FFF;
        }
      }
      .content{
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: pre;
        font-size: 14px;
        color: #4E5969;
        margin: 0 14px 0 12px;
      }
      .time{
        font-family: Source Han Sans, Source Han Sans;
        font-weight: 400;
        font-size: 14px;
        color: #6F7588;
      }
    }
  }
  .empty-block{
    position: relative;
    min-height: 160px;
    background-image: url('../../../../src/const/img/emptyImage.svg');
    background-repeat: no-repeat;
    background-position: center;
    background-size: 260px 123px;
    padding-top: 10px;
  }
  .empty-block::after{
    content: '暂无数据';
    line-height: 30px;
    color: #909399;
    font-size: 12px;
    text-align: center;
    display: block;
    width: 100%;
    position: absolute;
    top: 148px;
  }
}
.image-dialog-box{
  /deep/.el-dialog{
    background: transparent;
    .el-dialog__header{
      display: none;
    }
    .el-dialog__body{
      padding: 0;
    }
  }
  .dialog-box{
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    width: 100vw;
    z-index: 9999;
    text-align: center;
    img{
      max-width: 800px;
      min-width: 400px;
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translateY(-50%) translateX(-50%);
      z-index: 10000;
    }
  }
}
</style>