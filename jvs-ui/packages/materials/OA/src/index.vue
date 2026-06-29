<template>
  <div class="oa-box">
    <div v-if="applyData && applyData.length > 0" class="oa-list">
      <div v-for="(item, key) in applyData" :key="key" class="oa-list-item" :style="getStyle(key, applyData.length)">
        <div class="entry-item-card" @click="applyHandle(item)">
          <img v-if="item.icon" :src="item.icon" class="icon" alt="" >
          <div class="text">
            <el-tooltip class="item" effect="dark" :content="item.name" placement="top-start">
              <span>{{item.name}}</span>
            </el-tooltip>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="empty-block"></div>
    <!-- 流程申请 -->
    <el-dialog
      :title="title"
      width="75%"
      :fullscreen="true"
      append-to-body
      :visible.sync="flowVisible"
      :before-close="flowClose">
      <div v-if="flowVisible && afterSubmit" class="submit-after-dialog">
        <p><i class="el-icon-success"></i></p>
        <h4>提交成功</h4>
        <p><span>数据已提交并保存</span></p>
        <el-row>
          <el-button size="mini" type="primary" @click="againOpen">继续提交</el-button>
          <el-button size="mini" @click="flowClose">关闭</el-button>
        </el-row>
      </div>
      <div v-if="flowVisible && !afterSubmit">
        <iframe
          :src="formUrl"
          class="apply-form-iframe"
          scrolling="auto"
          v-if="isOut"
        >
        </iframe>
        <showForm v-else :formUrl="formUrl" :formId="formId" :approverList="approverList" @close="closeHandle"/>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { entryList } from '../../../../src/views/flowable/api/flowable'
import ShowForm from "../../../../src/views/flowable/views/componet/info";
export default {
  name: 'OA',
  components: { ShowForm },
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
    isLock:{
      type:Boolean,
      default:false
    }
  },
  data () {
    return {
      applyData: [],
      title: '',
      formId: '',
      formUrl: '',
      flowVisible: false,
      afterSubmit: false,
      approverList: [],
      isOut: false,
    }
  },
  created () {
    this.getEntryList()
  },
  methods: {
    getEntryList () {
      this.applyData = []
      entryList().then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          res.data.data.filter(rit => {
            if(rit.flowDesigns && rit.flowDesigns.length > 0) {
              this.applyData = this.applyData.concat(rit.flowDesigns)
            }
          })
        }
      })
    },
    // 申请
    applyHandle (it) {
      this.title = it.name
      this.approverList = []
      if(it.formId) {
        this.formId = it.formId
        this.formUrl = `/page-design-ui/#/form/info?id=${it.formId}&dataModelId=${it.dataModelId}&flowId=${it.id}&jvsAppId=${it.jvsAppId}&api=use&canDynamicAddNode=${it.canDynamicAddNode}`
        this.isOut = false
        if(it.manualNodes && it.manualNodes.length > 0) {
          for(let i in it.manualNodes) {
            // 发起人自选
            if((it.manualNodes[i].props && it.manualNodes[i].props.type == "SELF_SELECT" && (!it.manualNodes[i].props.targetObj.personnels || it.manualNodes[i].props.targetObj.personnels.length == 0))
              || (it.manualNodes[i].props.type == "ASSIGN_USER" && it.extend.enableDynamicApprover)) {
              this.approverList.push({
                nodeId: it.manualNodes[i].id,
                nodeName: it.manualNodes[i].name,
                approvers: it.manualNodes[i].props.targetObj.personnels || [],
                props: it.manualNodes[i].props
              })
            }
          }
        }
        this.flowVisible = true
      }else{
        this.$confirm('确认启动流程？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let temp = {
            id: it.id
          }
          startProcess(temp).then(res => {
            if(res.data && res.data.code == 0) {
              this.$notify({
                title: '提示',
                message: '启动成功',
                position: 'bottom-right',
                type: 'success'
              });
            }
          })
        }).catch(() => {});
      }
    },
    // 关闭流程申请表单
    flowClose () {
      this.flowVisible = false
      this.afterSubmit = false
      this.title = ""
    },
    closeHandle (bool) {
      if(bool) {
        this.afterSubmit = true
      }
    },
    openEvent () {
      this.$emit('openEvent', 'workplace')
    },
    againOpen () {
      this.afterSubmit = false
      this.dialogVisible = false
      this.dialogVisible = true
    },
    getStyle (index, length) {
      let colMargin0 = 20 // 外部列间距
      let colNum = 24 // 外部行的列数
      let colWidth = (this.layWidth - (colMargin0 * (colNum + 1))) / colNum // 外部每列的宽
      
      let str = 'box-sizing: border-box;'
      let minWidth = 172 // 最小宽
      let maxWidth = 220 // 最大宽
      let baseMagin = 14 // 固定间距
      let useCount = 0 // 每行个数

      let totalW =  Math.round(colWidth * this.w + Math.max(0, this.w - 1) * colMargin0)
      if(!this.isLock) {
        totalW = totalW -2 // 2：外部列的padding1
      }
      totalW = totalW - 28 // 32：该组件的padding14
      let minwNumber = Math.floor(totalW / minWidth)
      let maxwNumber = Math.floor(totalW / maxWidth)
      if((totalW % minWidth === 0)) {
        minwNumber -= 1
      }
      if(totalW % maxWidth === 0) {
        maxwNumber -= 1
      }
      // 判断最小宽+间距总和
      if(((Math.min(minwNumber, length) - 1) * baseMagin + Math.min(minwNumber, length) * minWidth) > totalW) {
        minwNumber -= 1
        while(minwNumber == 0) {
          if(!(((Math.min(minwNumber, length) - 1) * baseMagin + Math.min(minwNumber, length) * minWidth) > totalW)) {
            break;
          }
          minwNumber -= 1
        }
        if(!(minwNumber > 0)) {
         maxwNumber -= 1
          while(maxwNumber == 0) {   
            if(!(((Math.min(maxwNumber, length) - 1) * baseMagin + Math.min(maxwNumber, length) * maxWidth) > totalW)) {
              break;
            }
            maxwNumber -= 1
          }
          if(!(maxwNumber > 0)) {
            maxwNumber = 0
          }
        }
      }
      // 判断最大宽+间距总和
      if(((Math.min(maxwNumber, length) - 1) * baseMagin + Math.min(maxwNumber, length) * maxWidth) > totalW) {
        maxwNumber = 0
      }
      useCount = Math.max(minwNumber, maxwNumber)
      // console.log(minwNumber, maxwNumber, useCount, totalW, this.w)
      if(useCount > 0) {
        let itemWidth = Number.parseInt((totalW - ((useCount - 1) * baseMagin)) / useCount)
        str += `width: ${itemWidth}px;`
        if((Number(index)+1) % useCount === 1) {
          str += `margin-left: 0;`
        }
      }
      return str
    }
  }
}
</script>

<style lang="scss" scoped>
.oa-box{
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  overflow: hidden;
  .oa-list{
    padding-top: 20px;
    padding-left: 14px;
    padding-right: 14px;
    height: 100%;
    font-family: Source Han Sans-Regular, Source Han Sans;
    font-weight: 400;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    box-sizing: border-box;
    &::-webkit-scrollbar{
      display: none;
    }
    .oa-list-item{
      margin-bottom: 14px;
      margin-left: 14px;
      width: 172px;
      height: 56px;
      background: #F5F6F7;
      border-radius: 6px;
      cursor: pointer;
      .entry-item-card{
        width: 100%;
        height: 100%;
        padding: 0 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
        .icon{
          display: block;
          width: 32px;
          height: 32px;
          border-radius: 6px;
        }
        .text{
          margin-left: 12px;
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: pre;
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
.submit-after-dialog{
  position: absolute;
  width: 100%;
  top: calc(50% - 70px);
  left: 0;
  z-index: 1001;
  p{
    text-align: center;
    margin: 10px 0 0 0;
    i{
      color: #5b8bff;
      font-size: 32px;
    }
    span{
      color: #909399;
    }
  }
  h4{
    text-align: center;
    margin: 10px 0 0 0;
  }
  .el-row{
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 10px;
  }
}
</style>