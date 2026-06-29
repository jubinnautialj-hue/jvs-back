<template>
  <!-- 申请入口 -->
  <div class="flowable-entry">
    <div class="list">
      <el-collapse v-model="activeNames" @change="handleChange">
        <el-collapse-item
          v-for="(item, key) in tableData"
          :key="key"
          :title="`${item.designGroup}(${getModelList(item.designGroup).length})`"
          :name="item.designGroup"
        >
          <div class="type-list">
            <div class="entry-item-card"  v-for="it in getModelList(item.designGroup)" :key="it.modelId" @click="applyHandle(it)">
              <img v-if="it.icon" :src="it.icon" class="icon" alt="" >
              <div style="margin-left: 12px">
                <el-tooltip class="item" effect="dark" :content="it.name" placement="top-start">
                  <span>{{it.name}}</span>
                </el-tooltip>
                <div style="font-size: 13px;color: #a2a3a5;height: 24px;line-height: 24px">{{it.description}}</div>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
    <el-dialog
      :title="title"
      v-if="dialogVisible"
      width="75%"
      :fullscreen="true"
      :visible.sync="dialogVisible"
      :before-close="handleClose">
      <div v-if="afterSubmit" class="submit-after-dialog">
        <p><i class="el-icon-success"></i></p>
        <h4>提交成功</h4>
        <p><span>数据已提交并保存</span></p>
        <el-row>
          <el-button size="mini" type="primary" @click="againOpen">继续提交</el-button>
          <el-button size="mini" @click="handleClose">关闭</el-button>
        </el-row>
      </div>
      <div v-else>
        <iframe
          :src="formUrl"
          class="apply-form-iframe"
          scrolling="auto"
          v-if="isOut"
        >
        </iframe>
        <showForm v-else :formUrl="formUrl" :approverList="approverList" @close="closeHandle"/>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {entryList} from '../api/flowable'
import showForm from './componet/info'
import entryImg from '../styles/entry.png'
import {startProcess} from './componet/api'
export default {
  name: 'flowable-entry',
  components: {showForm},
  data () {
    return {
      activeNames: [],
      tableData: [],
      title: '',
      formUrl: '',
      dialogVisible: false,
      isOut: false,
      img: entryImg,
      currentItem: {},
      approverList: [],
      afterSubmit: false
    }
  },
  methods: {
    getModelList(val) {
      const index = this.tableData.findIndex(item => {
        return item.designGroup === val
      })
      return this.tableData[index].flowDesigns
    },
    handleChange(val) {
      // console.log(val)
    },
    getList () {
      entryList().then(res => {
        if(res.data.code == 0) {
          this.tableData = res.data.data
          this.currentItem = this.tableData[0]
          this.tableData.forEach(item => {
            this.activeNames.push(item.designGroup)
          })
        }
      })
    },
    // 申请
    applyHandle (it) {
      this.title = it.name
      this.approverList = []
      if(it.formId) {
        this.formUrl = `/page-design-ui/#/form/info?id=${it.formId}&dataModelId=${it.dataModelId}&flowId=${it.id}&jvsAppId=${it.jvsAppId}`
        this.isOut = false
        if(it.manualNodes && it.manualNodes.length > 0) {
          for(let i in it.manualNodes) {
            // 发起人自选
            if(it.manualNodes[i].props && it.manualNodes[i].props.type == "SELF_SELECT" && (!it.manualNodes[i].props.targetObj.personnels || it.manualNodes[i].props.targetObj.personnels.length == 0)) {
              this.approverList.push({
                nodeId: it.manualNodes[i].id,
                nodeName: it.manualNodes[i].name,
                approvers: [],
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
    // 关闭表单
    handleClose () {
      this.dialogVisible = false
      this.title = ""
    },
    closeHandle (bool) {
      if(bool) {
        this.afterSubmit = true
        // this.handleClose()
      }
    },
    // 分类入口显示
    typeList (item) {
      // console.log(item)
      this.currentItem = item
    },
    againOpen () {
      this.afterSubmit = false
      this.dialogVisible = false
      this.dialogVisible = true
    }
  },
  created () {
    this.getList()
  }
}
</script>
<style lang="scss" scoped>
.flowable-entry{
  // background: #F8FAFF;
  background: #fff;
  padding: 20px 10px;
  padding-left: 27px;
  //height: calc(100vh - 15px);
  height: 100vh;
  overflow: hidden;
  overflow-y: auto;
  box-sizing: border-box;
  .top{
    margin-top: 27px;
    .left{
      a{
        display: block;
        width: 450px;
      }
      img{
        display: block;
        width: 450px;
        height: 160px;
      }
    }
  }
  /deep/.list{
    h4{
      color: #333;
      font-size: 21px;
      line-height: 41px;
      height: 41px;
      margin: 10px 0;
      font-weight: normal;
    }
    .type-list{
      display: flex;
      flex-wrap: wrap;
    }
    .el-collapse{
      border: none;
      .el-collapse-item__arrow{
        margin-left: 4px;
      }
      .el-collapse-item__header{
        border: none;
      }
      .el-collapse-item__wrap{
        border: none;
      }
    }
  }
  .entry-item-card{
    position: relative;
    width: 210px;
    height: 68px;
    display: flex;
    align-items: center;
    background: #fff;
    border: 1px solid #e0e0e0;
    border-radius: 6px;
    padding-right: 10px;
    margin-bottom: 40px;
    box-sizing: border-box;
    cursor: pointer;
    margin: 27px;
    margin-left: 0;
    margin-bottom: 0;
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
      color: #333;
      word-break: keep-all;
      width: 135px;
      overflow: hidden;
      white-space: pre;
      text-overflow: ellipsis;
    }
  }
  .entry-item-card:hover{
    span{
      color: #3471FF;
    }
  }
}
.flowable-entry::-webkit-scrollbar{
  display: none;
}
.apply-form-iframe{
  border: 0;
  width: 100%;
  height: calc(100vh - 114px);
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
<style lang="scss">
.flowable-entry{
  .list{
    .type-title{
      font-size: 18px;
      color: #333;
      height: 42px;
      height: auto;
      line-height: 42px;
      .el-breadcrumb__inner{
        color: #333;
        cursor: pointer;
      }
      .el-breadcrumb__separator{
        font-weight: normal;
        color: #BFBFBF;
        margin: 0 20px;
      }
      .active{
        color: #3471FF;
      }
    }
  }
}
</style>
