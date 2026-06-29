<template>
  <div :class="{'message-box': true, 'with-title': (componentSetting.name || componentSetting.titleTxt)}">
    <div class="head-title">
      <span v-if="insideList && insideList.length > 0" @click="openEvent">查看更多 <i class="el-icon-arrow-right"></i></span>
    </div>
    <div v-if="insideList && insideList.length > 0" class="message-list">
      <div v-for="item in insideList" :key="item.id" class="message-list-item">
        <div class="left">
          <svg aria-hidden="true">
            <use :xlink:href="item.readIs ? '#icon-jvs-tongzhi-yidu' : '#icon-jvs-tongzhi'"></use>
          </svg>
        </div>
        <div class="center">
          <div class="head">
            <div class="message-title" @click.stop="openMessMore(item)">
              <span>{{item.msgContent.title}}</span>
            </div>
            <div class="right">
              <div v-if="item.open && item.msgContent.content" class="cell-message" @click.stop="$set(item, 'open', false);$forceUpdate();">
                <span>收起</span>
                <svg aria-hidden="true">
                  <use xlink:href="#icon-jvs-danchuang-zhankai"></use>
                </svg>
              </div>
              <div class="create-time">{{item.createTime}}</div>
            </div>
          </div>
          <div :class="{'message-content': true, 'cell': !(item.open)}">
            <div v-if="item.open" v-html="item.msgContent.content"></div>
            <span v-else>{{item.msgContent.content | getMessageText}}</span>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="empty-block"></div>
  </div>
</template>

<script>
import { messageaPage } from '../../../../src/api/admin/message'
export default {
  name: 'Message',
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
  filters: {
    getMessageText (html) {
      let str = html
      if(html && html.startsWith('<')) {
        let div = document.createElement('div')
        div.innerHTML = html
        str = div.innerText
      }
      return str
    }
  },
  data () {
    return {
      messageLoading: false,
      insideList:[],
    }
  },
  created () {
    this.getMessagePage() 
  },
  methods: {
    getMessagePage () {
      this.messageLoading = true
      let obj = {
        current: 1,
        size: 4,
      }
      messageaPage(obj).then(res=>{
        if(res.data.code == 0){
          this.insideList = JSON.parse(JSON.stringify(res.data.data.records))
          this.insideList.forEach(item => {
            item.msgContent = JSON.parse(item.msgContent)
          })
        }
      }).finally(res=>{
        this.messageLoading = false
      })
    },
    openMessMore (item) {
      if(item.msgContent.content && item.msgContent.content.length > 20) {
        this.$set(item, 'open', !(item.open))
        this.$forceUpdate()
      }
    },
    openEvent () {
      this.$emit('openEvent', 'message')
    }
  }
}
</script>

<style lang="scss" scoped>
.message-box{
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  &.with-title{
    .head-title{
      position: absolute;
      right: 0;
      top: -30px;
      height: unset;
    }
    .message-list{
      height: 100%;
    }
  }
  .head-title{
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 0 24px;
    height: 40px;
    span{
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 14px;
      color: #1E6FFF;
      cursor: pointer;
      line-height: 20px;
      i{
        font-size: 12px;
      }
    }
  }
  .message-list{
    height: calc(100% - 40px);
    padding-left: 24px;
    padding-right: 12px;
    margin-right: 12px;
    overflow: hidden;
    overflow-y: auto;
    .message-list-item{
      position: relative;
      display: flex;
      align-items: flex-start;
      margin-top: 16px;
      padding-bottom: 16px;
      border-bottom: 1px solid #EEEFF0;
      .left{
        svg{
          width: 32px;
          height: 32px;
          border-radius: 4px;
        }
      }
      .center{
        margin: 0 16px;
        flex: 1;
        overflow: hidden;
        .head{
          display: flex;
          align-items: center;
          justify-content: space-between;
        }
        .message-title{
          height: 20px;
          font-family: Source Han Sans-Bold, Source Han Sans;
          font-weight: 700;
          font-size: 14px;
          color: #363B4C;
          line-height: 20px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: pre;
          cursor: pointer;
        }
        .message-content{
          margin-top: 4px;
          height: auto;
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
          line-height: 20px;
        }
        .cell{
          height: 20px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      .right{
        display: flex;
        align-items: center;
        height: 20px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 14px;
        color: #6F7588;
        line-height: 20px;
        .cell-message{
          margin-right: 10px;
          font-size: 12px;
          color: #363b4c;
          font-family: Source Han Sans-Regular, Source Han Sans;
          cursor: pointer;
          svg{
            width: 12px;
            height: 12px;
          }
        }
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
</style>