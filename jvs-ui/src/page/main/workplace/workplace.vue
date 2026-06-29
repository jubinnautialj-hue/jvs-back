<template>
  <div id="otherouterbox" class="workplace-page">
    <div v-if="dataLoading" class="loading-back"/>
    <div class="flowable-page" v-if="currentPage === '待我处理'">
      <task-list/>
    </div>
    <div class="flowable-page" v-if="currentPage === '我创建的'">
      <apply/>
    </div>
    <div class="flowable-page" v-if="currentPage === '抄送我的'">
      <cc-list :fromWorkPlace="true" />
    </div>
    <div class="flowable-page" v-if="currentPage === '审核记录'">
      <task-record :fromWorkPlace="true" />
    </div>
    <img v-if="currentPage === null" class="workplace-img" src="@/const/img/workplace.png" alt=""/>
    <div v-if="formIndex ? true : currentPage === null" class="workplace-content">
      <div class="workplace-header">
        <div class="header-list-item" v-for="(item, key) in headerList" :key="key" :style="getStyle(key, headerList.length)" @click="headerItemClick(item)">
          <div>
            <div class="label">{{ item.label }}</div>
            <div class="number">{{ item.number }}</div>
          </div>
          <svg class="icon" aria-hidden="true">
            <use :xlink:href="'#'+item.icon"></use>
          </svg>
        </div>
      </div>
      <div class="work-flow-apply">
        <h2>流程申请</h2>
        <div class="apply-content">
          <el-input size="mini" placeholder="请输入标题搜索" v-model="queryValue">
            <el-button slot="append" icon="el-icon-search" @click="getApplyList"></el-button>
          </el-input>
          <div v-for="(item, key) in applyData" :key="key">
            <div class="group-name">{{item.designGroup}}</div>
            <div class="type-list">
              <div class="entry-item-card"  v-for="it in item.flowDesigns" :key="it.id" @click="applyHandle(it)">
                <img v-if="it.icon" :src="it.icon" class="icon" alt="" >
                <div style="margin-left: 12px">
                  <el-tooltip class="item" effect="dark" :content="it.name" placement="top-start">
                    <span>{{it.name}}</span>
                  </el-tooltip>
                  <div style="font-size: 13px;color: #a2a3a5;height: 24px;line-height: 24px">{{it.description}}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-dialog
      :title="title"
      width="75%"
      :fullscreen="true"
      append-to-body
      :visible.sync="dialogVisible"
      :before-close="handleClose">
      <div v-if="dialogVisible && afterSubmit" class="submit-after-dialog">
        <p><i class="el-icon-success"></i></p>
        <h4>提交成功</h4>
        <p><span>数据已提交并保存</span></p>
        <el-row>
          <el-button size="mini" type="primary" @click="againOpen">继续提交</el-button>
          <el-button size="mini" @click="handleClose">关闭</el-button>
        </el-row>
      </div>
      <div v-if="dialogVisible && !afterSubmit">
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
    <el-dialog
      class="hide-header-dialog"
      v-if="formIndex"
      :title="currentPage"
      :fullscreen="true"
      append-to-body
      :visible.sync="openVisible"
      :before-close="openClose"
    >
      <div class="open-by-index-box" v-if="openVisible">
        <div class="close-icon">
          <i class="el-icon-close" @click="openClose"></i>
        </div>
        <div v-if="currentPage === '待我处理'">
          <task-list/>
        </div>
        <div v-if="currentPage === '我创建的'">
          <apply/>
        </div>
        <div v-if="currentPage === '抄送我的'">
          <cc-list :fromWorkPlace="true" />
        </div>
        <div v-if="currentPage === '审核记录'">
          <task-record :fromWorkPlace="true" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {entryList} from "@/views/flowable/api/flowable";
import {getTaskStatistic, startProcess} from "@/views/flowable/views/componet/api";
import ShowForm from "@/views/flowable/views/componet/info";
import TaskList from "@/views/flowable/views/taskList";
import TaskRecord from "@/views/flowable/views/taskRecord";
import Apply from "@/views/flowable/views/apply";
import CcList from "@/views/flowable/views/ccList";

export default {
  name: "workplace",
  components: {TaskList, Apply, CcList, TaskRecord, ShowForm},
  props: {
    changeModeUserRadom: {
      type: Number
    },
    formIndex: {
      type: Boolean
    },
    w: Number,
    layWidth: Number,
    isLock:{
      type:Boolean,
      default:false
    }
  },
  data () {
    return {
      title: '',
      formId: '',
      formUrl: '',
      dialogVisible: false,
      afterSubmit: false,
      approverList: [],
      isOut: false,
      queryValue: null,
      dataLoading: false,
      applyData: [],
      headerList: [
        { label: '待我处理', number: 0, icon: 'icon-icon_4-04' },
        { label: '我创建的', number: 0, icon: 'icon-zuoyefenpeisvg1' },
        { label: '抄送我的', number: 0, icon: 'icon-icon_3-02' },
        { label: '审核记录', number: 0, icon: 'icon-icon_4-06' },
      ],
      categoryList: [],
      currentIndex: 0,
      currentLabel: '全部',
      currentPage: null,
      openVisible: false
    }
  },
  created() {
    if(this.formIndex) {
      // this.loadInit()
      this.getTaskStatic()
    }
  },
  methods: {
    loadInit () {
      this.dataLoading = true
      this.getApplyList()
      this.getTaskStatic()
    },
    getTaskStatic() {
      getTaskStatistic().then(res => {
        if (res.data && res.data.code == 0) {
          this.$emit('getCount', res.data.data)
          const obj = res.data.data
          this.headerList.forEach(item => {
            switch (item.label) {
              case '待我处理': item.number = obj.pendingCount;
              break;
              case '我创建的': item.number = obj.selfCreateCount;
              break;
              case '抄送我的': item.number = obj.carbonCopyCount;
              break;
              case '审核记录': item.number = obj.selfApproveCount;
              break;
              default:;
              break;
            }
          })
        }
      })
    },
    init() {
      this.currentPage = null
      this.loadInit()
    },
    // 点击跳转
    headerItemClick(obj) {
      this.currentPage = obj.label
      if(this.formIndex) {
        this.openVisible = true
      }
    },
    againOpen () {
      this.afterSubmit = false
      this.dialogVisible = false
      this.dialogVisible = true
    },
    // 关闭表单
    handleClose () {
      this.dialogVisible = false
      this.afterSubmit = false
      this.title = ""
      this.loadInit()
    },
    closeHandle (bool) {
      if(bool) {
        this.afterSubmit = true
        // this.handleClose()
      }
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
        this.dialogVisible = true
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
              // this.$message.success('启动成功')
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
    getModelList(val) {
      const index = this.applyData.findIndex(item => {
        return item.designGroup === val
      })
      return index > -1 ? this.applyData[index].flowDesigns || [] : []
    },
    // 获取流程申请数据
    getApplyList() {
      let params = {}
      if(this.queryValue) {
        params.name = this.queryValue
      }
      entryList(params).then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.applyData = [...res.data.data]
          this.dataLoading = false
        } else {
          this.dataLoading = false
        }
      }).catch(err => {
        this.dataLoading = false
      })
    },
    // 点击分类
    handleCategoryClick(label, key) {
      this.currentIndex = key
      this.currentLabel = label
    },
    openClose () {
      this.openVisible = false
      this.currentPage = null
      this.loadInit()
    },
    getStyle (index, length) {
      let colMargin0 = 20 // 外部列间距
      let colNum = 24 // 外部行的列数
      let colWidth = (this.layWidth - (colMargin0 * (colNum + 1))) / colNum // 外部每列的宽
      
      let str = 'box-sizing: border-box;'
      if(this.formIndex) {
        let minWidth = 160 // 最小宽
        let maxWidth = 240 // 最大宽
        let baseMagin = 20 // 固定间距
        let useCount = 0 // 每行个数

        let totalW =  Math.round(colWidth * this.w + Math.max(0, this.w - 1) * colMargin0)
        if(!this.isLock) {
          totalW = totalW -2 // 2：外部列的padding1
        }
        totalW = totalW - 32 - 16 // 2：外部列的padding1  32：该组件的padding16  16：卡片盒子的padding8
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
          if((Number(index)+1) % useCount === 0) {
            str += `margin-right: 0;`
          }
        }
      }
      return str
    }
  },
  watch: {
    changeModeUserRadom: {
      handler (newVal, oldVal) {
        if(newVal > -1) {
          this.loadInit()
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
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
.workplace-page{
  position: relative;
  height: 100%;
  padding: 0 20px;
  padding-top: 20px;
  box-sizing: border-box;
  overflow-y: auto;
  .workplace-img{
    width: 400px;
    position: absolute;
    top: 10px;
    right: 100px;
    z-index: 1;
  }
  .loading-back{
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    box-sizing: border-box;
    background-color: rgba(255, 255, 255, 0.8);
    background-image: url('../../../../public/jvs-ui-public/img/loading.gif');
    background-repeat: no-repeat;
    background-position: center;
    background-position: center;
    //background-size: 200px 160px;
    z-index: 2;
  }
  .workplace-content{
    border-radius: 6px;
    background-color: #ffffff;
    min-height: calc(100vh - 130px);
    padding: 20px;
    .workplace-header{
      display: flex;
      width: 80vw;
      flex-wrap: wrap;
      .header-list-item{
        margin-bottom: 20px;
        cursor: pointer;
        padding: 0 20px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 240px;
        height: 100px;
        border-radius: 4px;
        box-shadow: 0 0 8px #f0f0f0;
        margin-right: 20px;
        &:hover{
          box-shadow: 0 0 8px #e0e0e0;
        }
        .icon{
          width: 40px;
          height: 40px;
        }
        .label{
          font-size: 14px;
          margin-bottom: 10px;
        }
        .number{
          font-size: 22px;
        }
      }
    }
    .work-flow-apply{
      /deep/.apply-content{
        padding: 0 20px;
        .el-input-group{
          width: 300px;
          .el-input-group__append{
            background-color: #ffffff;
            padding: 0 16px;
          }
        }
        .category-list{
          margin: 20px 0;
          display: flex;
          .category-item{
            color: #a2a3a5;
            padding: 6px 16px;
            cursor: pointer;
            border-radius: 4px;
          }
          .category-item-active{
            background-color: #e6e6e6;
            color: #333333;
          }
        }
        .group-name{
          font-size: 14px;
          color: #a2a3a5;
          padding: 0 16px;
          margin-top: 20px;
        }
        .type-list{
          display: flex;
          flex-wrap: wrap;
          .entry-item-card{
            position: relative;
            width: 210px;
            height: 68px;
            display: flex;
            align-items: center;
            background: #fff;
            box-shadow: 0 0 8px #f0f0f0;
            border-radius: 6px;
            padding-right: 10px;
            margin-bottom: 40px;
            box-sizing: border-box;
            cursor: pointer;
            margin: 27px;
            margin-left: 0;
            margin-bottom: 0;
            &:hover{
              box-shadow: 0 0 8px #e0e0e0;
              span{
                color: #3471FF;
              }
            }
            .icon{
              width:40px;
              height:40px;
              margin-left: 10px;
            }
            i{
              font-size: 36px;
              margin-left: 10px;
              color: #FC723C;
            }
            span{
              font-size: 13px;
              //margin-left: 20px;
              color: #333;
              word-break: keep-all;
              //display: inline-block;
              width: 135px;
              overflow: hidden;
              white-space: pre;
              text-overflow: ellipsis;
            }
          }
        }
      }
    }
  }
}
.el-dialog__wrapper.hide-header-dialog{
  /deep/.el-dialog{
    .el-dialog__header{
      display: none;
    }
    .el-dialog__body{
      padding: 0!important;
    }
  }
}
.open-by-index-box{
  padding: 20px 20px 0 20px;
  height: calc(100vh - 20px);
  position: relative;
  .close-icon{
    position: absolute;
    top: 10px;
    right: 10px;
    i{
      font-size: 20px;
      cursor: pointer;
    }
  }
  >div:not(.close-icon){
    height: 100%;
    >div{
      height: 100%;
      /deep/.jvs-table{
        height: 100%;
        .table-body-box{
          height: calc(100% - 108px - 68px);
          .el-table{
            height: 100%;
            .el-table__body-wrapper {
              height: calc(100% - 40px)!important;
            }
          }
        }
      }
    }
  }
}
</style>
