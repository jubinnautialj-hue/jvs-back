<template>
  <div class="oa-box">
    <div v-if="taskListData && taskListData.length > 0" class="task-list-box">
      <task-list ref="taskList" v-show="false" :notRequireData="true" @fresh="getListData" />
      <div class="my-task-list">
        <div v-for="(item, index) in taskListData" :key="item.id+'-'+index" class="my-task-list-item">
          <div class="left" @click="dealHandle(item)">
            <div class="title">{{item.title || (item.createBy+'发起的'+item.name)}}</div>
            <div class="info">
              <div class="time">{{item.createTime}}</div>
              <div class="node-name">{{item.nodeName}}审批中</div>
            </div>
          </div>
          <div class="right">
            <div class="button" @click.stop="dealHandle(item)">
              <span>办理</span>
              <i class="el-icon-arrow-right"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="empty-block"></div>
  </div>
</template>

<script>
import TaskList from "../../../../src/views/flowable/views/taskList";
import { queryTaskList } from '../../../../src/views/flowable/api/flowable'
export default {
  name: 'OaTask',
  components: { TaskList },
  props: {
    w: Number,
    layWidth: Number,
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
      loading: false,
      taskListData: []
    }
  },
  created () {
    this.getListData()
  },
  methods: {
    getListData () {
      this.loading = true
      queryTaskList({current: 1, size: 4}).then(res => {
        if(res.data.code == 0 && res.data.data) {
          this.loading = false
          this.taskListData = res.data.data.records
        }
      })
    },
    dealHandle (row, type) {
      this.$refs.taskList.dealHandle(row, type)
    },
    openEvent () {
      this.$emit('openEvent', 'workplace')
    }
  }
}
</script>

<style lang="scss" scoped>
.oa-box{
  width: 100%;
  height: 100%;
  padding-bottom: 16px;
  box-sizing: border-box;
  overflow: hidden;
  .task-list-box{
    padding-left: 24px;
    padding-right: 12px;
    margin-right: 12px;
    height: 100%;
    font-family: Source Han Sans-Regular, Source Han Sans;
    font-weight: 400;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    .my-task-list{
      width: 100%;
      .my-task-list-item{
        margin-top: 17px;
        padding-bottom: 18px;
        border-bottom: 1px solid #EEEFF0;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .left{
          height: 48px;
          padding-left: 7px;
          border-left: 4px solid #36B452;
          cursor: pointer;
          .title{
            height: 23px;
            font-family: Source Han Sans-Bold, Source Han Sans;
            font-weight: 700;
            font-size: 16px;
            color: #363B4C;
            line-height: 23px;
          }
          .info{
            margin-top: 8px;
            display: flex;
            align-items: center;
            height: 20px;
            .time{
              font-weight: 400;
              font-size: 14px;
              color: #6F7588;
            }
            .node-name{
              margin-left: 16px;
              padding: 2px 4px;
              font-weight: 400;
              font-size: 12px;
              color: #36B452;
              background: #EFF9F2;
            }
          }
        }
        .right{
          font-weight: 400;
          font-size: 14px;
          color: #1E6FFF;
          .button{
            cursor: pointer;
          }
        }
        &:nth-of-type(3n+2){
          .left{
            border-color: #1E6FFF;
            .info{
              .node-name{
                background: #EDF4FF;
                color: #1E6FFF;
              }
            }
          }
        }
        &:nth-of-type(3n+3){
          .left{
            border-color: #FF9736;
            .info{
              .node-name{
                background: #FFF7EF;
                color: #FF9736;
              }
            }
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
