<template>
  <div class="tool-box">
    <design-tool :title="'逻辑设计'" :needLoading="true">
      <template slot="right">
        <p>
          <el-popover
            v-model="logshow"
            placement="bottom"
            trigger="click">
            <div v-if="logshow" class="log-list-table">
              <jvs-table :option="logOption" :data="logData" @on-load="logHandle" @row-click="rowClick">
                <template slot="status" slot-scope="scope">
                  <el-tag :type="scope.row.status ? '' : 'warning'">{{scope.row.status ? '成功' : '失败'}}</el-tag>
                </template>
              </jvs-table>
            </div>
            <i class="el-icon-time" slot="reference" style="cursor:pointer;font-size: 20px;"></i>
          </el-popover>
        </p>
        <p>
          <el-tooltip effect="dark" content="测试" placement="top">
            <!-- <img :src="rightToolIcon.testIcon" alt="" @click="testHandle"> -->
            <i class="el-icon-video-play" style="cursor:pointer;font-size: 20px;" @click="testHandle"></i>
          </el-tooltip>
        </p>
<!--        <p>-->
<!--          <el-tooltip effect="dark" content="pc端预览" placement="top">-->
<!--            <img style="width: 16px;height: 16px" :src="pcView" alt="" @click="view('pc')">-->
<!--          </el-tooltip>-->
<!--        </p>-->
      </template>
    </design-tool>
  </div>
</template>
<script>
import saveicon from "@/const/img/保存.png"
import titlePageHeader from '@/components/page-header/titlePageHeader'
import designTool from '@/components/design-tool/DesignTool'
import testicon from "@/views/rule/const/img/测试.png";
import { runlogRevert } from '../../api/rule'
export default {
  name: 'design-bar',
  components: {titlePageHeader, designTool},
  props: {
    infoData: {
      type: Object
    }
  },
  data () {
    return {
      rightToolIcon: {
        testIcon: testicon
      },
      saveIcon: saveicon,
      role: [], // 权限设置
      roleType: true, // 权限类型,true 应用 权限，false 自定义权限
      operationList: [], // 操作权限list
      permissionVisible: false, // 权限弹框
      deptId: [],
      dialogVisible: false,
      submitLoading: false, // 保存loading
      logshow: false,
      logData: [],
      logOption: {
        addBtn: false,
        menu: false,
        hideTop: true,
        column: [
          {
            label: '执行状态',
            prop: 'status',
            slot: true
          },
          {
            label: '时间',
            prop: 'startTime'
          },
          {
            label: '耗时(ms)',
            prop: 'totalExecutionTime'
          }
        ]
      }
    }
  },
  created () {
  },
  methods: {
    testHandle() {
      this.$emit('testHandle')
    },
    // 预览
    view(type) {
    },
    logHandle () {
      if(this.infoData) {
        runlogRevert(this.infoData.jvsAppId, this.infoData.secret).then(res => {
          if(res.data && res.data.code == 0) {
            this.logData = res.data.data
          }
        })
        this.logshow = true
      }
    },
    rowClick (data) {
      if(data.row && data.row.result) {
        this.$emit('revertLog', data.row.result)
      }
    }
  }
}
</script>
<style lang="scss" scoped>
/deep/.chart-select{
  .chart-select-box{
    height: 65vh;
    overflow-y: auto;
    padding: 0 10px;
    .chart-select-item{
      .item-content-title{
        font-size: 16px;
        margin-bottom: 16px;
      }
      .item-content{
        width: calc(100% - 10px);
        margin: 0 5px;
        display: grid;
        grid-template-columns: 30% 30% 30%;
        grid-column-gap: 5%;
        .item-content-chart{
          margin-bottom: 30px;
          .chart-item{
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            height: 22vh;
            border-radius: 4px;
            box-shadow: 0 0 5px rgba(57, 57, 57, 0.1);
            cursor: pointer;
            &:hover{
              box-shadow: 0 0 5px rgba(57, 57, 57, 0.2);
            }
            .chart-item-footer{
              height: 24px;
              font-size: 12px;
              line-height: 24px;
              padding: 0 8px;
              color: #a8a8a8;
              background-color: #f7f9fa;
            }
            img {
              width: 100%;
              height: 84%;
            }
          }
          .chart-item-card{
            height: auto;
          }
        }
      }
    }
  }
}
.tool-box{
  .form-design-tool{
    font-size: 20px;
    cursor: pointer;
    color: #353535;
    cursor: pointer;
    &:hover{
      //color: #0D76FC;
    }
  }
  .cont-top {
    overflow: hidden;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #606266;
    background: #fff;
    // margin-top: 8px;
    // padding: 8px 0;
    border-radius: 5px;
    .cont-top-item {
      display: flex;
      justify-content: space-between;
      p {
        margin: 0 10px;
        height: 32px;
        line-height: 32px;
      }
    }
  }
}
.gallery-list-top {
  // min-height: 70px;
  // display: flex;
  // align-items: center;
  justify-content: space-between;
  // padding: 10px 10px;
  border-radius: 5px;
  background-color: #fff;
  .tree-auth {
    height: 100%;
    display: flex;
    align-items: center;
    .el-button,
    .el-cascader {
      margin-right: 10px;
    }
  }
  .gallery-list-top-right{
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 5px;
    div{
      display: flex;
      align-items: center;
      .assembly-el-menu{
        width: 80px!important;
        .assembly-el-menu-item{
          width: 80px!important;
          .el-submenu__title{
            width: 100%;
            padding: 0;
          }
        }
      }
      .el-switch, .el-select{
        margin-left: 15px;
      }
    }
  }

}
.gallery-list-top-left .assembly-el-menu .el-submenu__icon-arrow{
  display: none;
}

.gallery-list-top-bottom{
  background-color: #ffffff;
  display: flex;
  margin: 0 8px;
  padding: 10px 5px;
  padding-left: 40px;
}


.el-tabs__new-tab{
  border: 1px solid #409EFF;
  color: #409EFF;
}
.el-tabs__item{
  background: #fff;
}
.el-menu.assembly-el-menu {
  height: 20px;
  top: -2px!important;
  width: auto !important;
  .el-submenu__icon-arrow {
    display: none;
  }
  .assembly-el-menu-item {
    color: #222;
    box-sizing: border-box;
    // margin: 0 10px;
    text-align: center;
    line-height: 20px;
    padding: 0;
    border-radius: 5px;
    height: 100%;
    .el-submenu__title {
      height: 100%;
      line-height: 32px;
      color: #222;
      border: 0;
      padding: 0;
    }
  }
  .assembly-el-menu-item:nth-of-type(1) {
    margin-left: 0;
  }
  .assembly-el-menu-item.is-active {
    .el-submenu__title {
      border: 0;
    }
  }
  >.el-submenu .el-submenu__title{
    height: 20px;
    line-height: 20px;
  }
  .el-submenu__icon-arrow{
    display: none;
  }
}
.log-list-table{
  width: 350px;
  /deep/.jvs-table{
    .el-table__body-wrapper{
      height: auto!important;
    }
  }
}
</style>
