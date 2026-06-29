<template>
  <div class="rule-assembly-box" style="height:100%;">
    <div class="item-panel-search">
      <el-input placeholder="搜索" v-model="keyword" clearable @change="getSearchList"><i slot="prefix" class="el-input__icon el-icon-search"></i></el-input>
    </div>
    <div v-if="loading" class="loading-back"></div>
    <div v-else v-show="!keyword" class="left-tool-list-box">
      <div v-for="(tp, tix) in Funcs" :key="'tool-type-'+tp.value" :class="{'rule-assembly-list': true, 'open': activeNames.indexOf(tp.value) > -1}">
        <div class="title">
          <div class="t-left" @click="openList(tp.value, tp, tix)">
            <svg class="svg" aria-hidden="true">
              <use :xlink:href="`#${activeNames.indexOf(tp.value) > -1 ? 'icon-jvs-a-zu6027' : 'icon-jvs-a-zu3872'}`"></use>
            </svg>
            <span class="label">{{tp.label}}</span>
            <span v-if="tp.children && tp.children.length > 0" class="number"> / {{tp.children.length}}个</span>
          </div>
        </div>
        <el-collapse-transition>
          <ul v-show="activeNames.indexOf(tp.value) > -1" :class="{'left-tool-list': true, 'loading': loadingIndex == tix}">
            <draggable v-for="(item,index) in tp.children" :key="index" @start="handleDragstart($event, item)" @end="handleDragEnd" v-model="tp.children" :options="{...draggableOptions, disabled: !item.draggable}">
              <li
                :class="{'getItem': true, 'disabled': !item.draggable}"
                :data-shape="item.shape"
                :data-type="item.type"
                :data-size="item.size"
                :title="item.draggable ? '' : (item.statusMsg || '功能异常不可使用')"
              >
                <div class="icon">
                  <svg aria-hidden="true" class="pannel-type-icon">
                    <use :xlink:href="'#' + item.icon"></use>
                  </svg>
                </div>
                <span>{{item.functionName}}</span>
                <el-popover
                  v-if="item.explain"
                  placement="right"
                  trigger="hover">
                  <div class="assembly-info-con-box" v-html="item.explain"></div>
                  <span slot="reference" class="info-back-box">
                    <svg aria-hidden="true">
                      <use xlink:href="#icon-jvs-biaozhu"></use>
                    </svg>
                  </span>
                </el-popover>
              </li>
            </draggable>
          </ul>
        </el-collapse-transition>
      </div>
    </div>
    <div v-if="keyword" class="left-tool-list-box">
      <div class="rule-assembly-list open">
        <ul class="left-tool-list">
          <draggable v-for="(item,index) in searchList" :key="index" @start="handleDragstart($event, item)" @end="handleDragEnd" v-model="searchList" :options="{...draggableOptions, disabled: !item.draggable}">
            <li
              :class="{'getItem': true, 'disabled': !item.draggable}"
              :data-shape="item.shape"
              :data-type="item.type"
              :data-size="item.size"
              :title="item.draggable ? '' : (item.statusMsg || '功能异常不可使用')"
            >
              <div class="icon">
                <svg aria-hidden="true" class="pannel-type-icon">
                  <use :xlink:href="'#' + item.icon"></use>
                </svg>
              </div>
              <span>{{item.functionName}}</span>
              <el-popover
                v-if="item.explain"
                placement="right"
                trigger="hover">
                <div class="assembly-info-con-box" v-html="item.explain"></div>
                <span slot="reference" class="info-back-box">
                  <svg aria-hidden="true">
                    <use xlink:href="#icon-jvs-biaozhu"></use>
                  </svg>
                </span>
              </el-popover>
            </li>
          </draggable>
        </ul>
      </div>
    </div>
  </div>
</template>
<script>
import draggable from 'vuedraggable'
import eventBus from "../../../utils/eventBus";
import okSvg from "./ok.svg";
import {getFuncList, searchFuncList} from  '../../../api/rule'
var mousePosition = {
  left: -1,
  top: -1
}
export default {
  components : {draggable},
  props: {
    ruleInfo: {
      type: Object
    },
    itempannelshow: Boolean
  },
  data() {
    return {
      page: null,
      command: null,
      offsetX: 0,
      offsetY: 0,
      list: [],
      activeNames: ['common'],
      nodeMenu: {},
      // draggable配置参数参考 https://www.cnblogs.com/weixin186/p/10108679.html
      draggableOptions: {
        preventOnFilter: false,
        sort: false,
        disabled: false,
        ghostClass: 'tt',
        // 不使用H5原生的配置
        forceFallback: true
      },
      Funcs: null,
      funcGroup: [],
      loading: true,
      keyword: '',
      searchList: [],
      loadingIndex: -1,
    };
  },
  created() {
    /**
     * 以下是为了解决在火狐浏览器上推拽时弹出tab页到搜索问题
     * @param event
     */
    if (this.isFirefox()) {
      document.body.ondrop = function (event) {
        // 解决火狐浏览器无法获取鼠标拖拽结束的坐标问题
        mousePosition.left = event.layerX
        mousePosition.top = event.clientY - 50
        event.preventDefault();
        event.stopPropagation();
      }
    }
    this.getFuncListHandle()
    eventBus.$on("regetFunc", bool => {
      if(bool) {
        this.getFuncListHandle()
      }
    });
  },
  methods: {
    // 获取所有方法
    getFuncListHandle () {
      let paramTemp = {}
      if(this.ruleInfo) {
        if(this.ruleInfo.jvsAppId) {
          paramTemp.jvsAppId = this.ruleInfo.jvsAppId
        }
        if(this.ruleInfo.id) {
          paramTemp.componentId = this.ruleInfo.id
          paramTemp.id = this.ruleInfo.id
        }
      }
      if(this.$route.query) {
        if(this.$route.query.id) {
          paramTemp.id = this.$route.query.id
        }
        if(this.$route.query.jvsAppId) {
          paramTemp.jvsAppId = this.$route.query.jvsAppId
        }
        if(this.$route.query.componentId) {
          paramTemp.componentId = this.$route.query.componentId
        }
      }
      this.loading = true
      getFuncList(paramTemp).then(res => {
        if(res.data && res.data.code == 0) {
          this.activeNames = []
          let temp = JSON.parse(JSON.stringify(res.data.data))
          let list = []
          for(let i in temp) {
            let obj = {
              label: temp[i].groupName,
              value: temp[i].groupName,
              children: temp[i].list,
            }
            for(let j in obj.children) {
              obj.children[j].label = obj.children[j].functionName
              obj.children[j].name = obj.children[j].functionName
              obj.children[j].icon = obj.children[j].icon || 'https://gw.alipayobjects.com/zos/rmsportal/czNEJAmyDpclFaSucYWB.svg'
              obj.children[j].type = "node"
              obj.children[j].shape = "customNode"
              obj.children[j].color = "#1890ff"
              obj.children[j].stateImage = okSvg
              // 是否禁用
              obj.children[j].draggable = obj.children[j].status ? true : false
              if(obj.children[j].draggable == false) {
                obj.children[j].statusMsg = obj.children[j].statusMsg || ''
              }
              if(obj.children[j].functionName == '开始') {
                obj.children[j].nodeType = 'start'
                obj.children[j].draggable = true
              }
              if(obj.children[j].functionName == '结束') {
                obj.children[j].nodeType = 'end'
                obj.children[j].draggable = true
              }
            }
            if(i < 1 && obj.children && obj.children.length > 0) {
              this.activeNames.push(obj.value)
            }
            list.push(obj)
          }
          this.Funcs = list
          this.loading = false
        }else{
          this.loading = false
        }
      }).catch(e => {
        this.loading = false
      })
    },
    handleDragstart(e, item) {
      this.offsetX = e.offsetX;
      this.offsetY = e.offsetY;
      this.nodeMenu = {
        id: `node-${item.name}-${new Date().getTime()}`,
        name: item.functionName,
        type: 'task',
        left: 0,
        top: 0,
        ico: item.icon,
        state: '',
        data: item
      }
    },
    handleDragEnd(evt, e) {
      console.log(evt)
      eventBus.$emit('add', evt, this.nodeMenu, mousePosition)
      this.$emit('close')
      this.$forceUpdate()
    },
    // 是否是火狐浏览器
    isFirefox() {
      var userAgent = navigator.userAgent
      if (userAgent.indexOf("Firefox") > -1) {
        return true
      }
      return false
    },
    isSvg (url) {
      return url.startsWith('<')
    },
    getSearchList () {
      if(this.keyword) {
        this.searchList = []
        let paramTemp = {
          name: this.keyword
        }
        if(this.ruleInfo) {
          if(this.ruleInfo.jvsAppId) {
            paramTemp.jvsAppId = this.ruleInfo.jvsAppId
          }
          if(this.ruleInfo.id) {
            paramTemp.componentId = this.ruleInfo.id
            paramTemp.id = this.ruleInfo.id
          }
        }
        if(this.$route.query) {
          if(this.$route.query.id) {
            paramTemp.id = this.$route.query.id
          }
          if(this.$route.query.jvsAppId) {
            paramTemp.jvsAppId = this.$route.query.jvsAppId
          }
          if(this.$route.query.componentId) {
            paramTemp.componentId = this.$route.query.componentId
          }
        }
        this.loading = true
        searchFuncList(paramTemp).then(res => {
          if(res.data && res.data.code == 0) {
            this.searchList = res.data.data
            for(let i in this.searchList) {
              this.$set(this.searchList[i], 'draggable', this.searchList[i].status ? true : false)
              if(this.searchList[i].draggable == false) {
                this.$set(this.searchList[i], 'statusMsg', this.searchList[i].statusMsg || '')
              }
            }
          }
          this.loading = false
        }).catch(e => {
          this.loading = false
        })
      }
      this.$forceUpdate()
    },
    openList (value, tp, tix) {
      let index = this.activeNames.indexOf(value)
      if(index > -1) {
        this.activeNames.splice(index, 1)
      }else{
        this.activeNames.push(value)
        if(tp.children && tp.children.length == 0) {
          this.loadingIndex = tix
          let paramTemp = {
            group: [tp.value]
          }
          if(this.ruleInfo) {
            if(this.ruleInfo.jvsAppId) {
              paramTemp.jvsAppId = this.ruleInfo.jvsAppId
            }
            if(this.ruleInfo.id) {
              paramTemp.componentId = this.ruleInfo.id
              paramTemp.id = this.ruleInfo.id
            }
          }
          if(this.$route.query) {
            if(this.$route.query.id) {
              paramTemp.id = this.$route.query.id
            }
            if(this.$route.query.jvsAppId) {
              paramTemp.jvsAppId = this.$route.query.jvsAppId
            }
            if(this.$route.query.componentId) {
              paramTemp.componentId = this.$route.query.componentId
            }
          }
          getFuncList(paramTemp).then(res => {
            this.loadingIndex = -1
            if(res.data && res.data.code == 0) {
              if(res.data.data && res.data.data.length > 0) {
                let obj = {
                  label: res.data.data[0].groupName,
                  value: res.data.data[0].groupName,
                  children: res.data.data[0].list,
                }
                for(let j in obj.children) {
                  obj.children[j].label = obj.children[j].functionName
                  obj.children[j].name = obj.children[j].functionName
                  obj.children[j].icon = obj.children[j].icon || 'https://gw.alipayobjects.com/zos/rmsportal/czNEJAmyDpclFaSucYWB.svg'
                  obj.children[j].type = "node"
                  obj.children[j].shape = "customNode"
                  obj.children[j].color = "#1890ff"
                  obj.children[j].stateImage = okSvg
                  // 是否禁用
                  obj.children[j].draggable = obj.children[j].status ? true : false
                  if(obj.children[j].draggable == false) {
                    obj.children[j].statusMsg = obj.children[j].statusMsg || ''
                  }
                  if(obj.children[j].functionName == '开始') {
                    obj.children[j].nodeType = 'start'
                    obj.children[j].draggable = true
                  }
                  if(obj.children[j].functionName == '结束') {
                    obj.children[j].nodeType = 'end'
                    obj.children[j].draggable = true
                  }
                }
                if(obj.value == value) {
                  this.$set(this.Funcs[tix], 'children', obj.children)
                }
              }
            }
          }).catch(e => {
            this.loadingIndex = -1
          })
        }
      }
      this.$forceUpdate()
    },
  },
  watch: {
    itempannelshow: {
      handler(newVal, oldVal) {
        if(!newVal) {
          this.keyword = ''
        }
        this.$forceUpdate()
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.rule-assembly-box{
  height: calc(100% - 30px);
  .loading-back{
    background-color: rgba(255, 255, 255, 0.8);
    background-image: url('../../../../../../public/jvs-ui-public/img/loading.gif');
    background-repeat: no-repeat;
    background-position: center;
    background-position: center;
  }
  .item-panel-search{
    margin-top: 2px;
    padding: 0 16px;
    /deep/.el-input{
      .el-input__inner{
        border-color: #EEEFF0;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 14px;
        color: #6F7588;
        &::placeholder{
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          font-size: 14px;
          color: #6F7588;
        }
      }
      .el-input__prefix{
        i{
          color: #6F7588;
          font-size: 14px;
          font-weight: bold;
        }
      }
    }
  }
  .left-tool-list-box{
    height: calc(100% - 38px);
    padding-left: 16px;
    overflow: hidden;
    overflow-y: auto;
    .rule-assembly-list{
      margin-top: 16px;
      &.open+.rule-assembly-list{
        margin-top: 0;
      }
      .title{
        display: flex;
        align-items: center;
        justify-content: space-between;
        .t-left{
          display: flex;
          align-items: center;
          cursor: pointer;
          svg{
            width: 14px;
            height: 14px;
            fill: #363B4C;
            margin-right: 5px;
          }
          span{
            font-family: Source Han Sans-Bold, Source Han Sans;
            font-weight: 700;
            font-size: 14px;
            color: #363B4C;
            line-height: 20px;
          }
          .number{
            margin-left: 4px;
            font-size: 12px;
            color: #6F7588;
          }
        }
        .t-right{
          display: flex;
          align-items: center;
          padding-right: 16px;
          div{
            width: 24px;
            height: 24px;
            padding: 4px;
            box-sizing: border-box;
            border-radius: 4px;
            cursor: pointer;
            &.clicked, &:hover{
              background-color: #DEEAFF;
            }
            &.clicked{
              svg{
                fill: #1E6FFF;
              }
            }
            &+div{
              margin-left: 4px;
            }
            svg{
              width: 16px;
              height: 16px;
              fill: #6F7588;
            }
          }
        }
      }
      .left-tool-list{
        margin: 0;
        padding: 0;
        margin-top: 10px;
        margin-right: 12px;
        display: flex;
        flex-wrap: wrap;
        &.oprate{
          .getItem{
            .del-icon-svg{
              display: block;
            }
            &:hover{
              background: none;
              .icon{
                background: #E4EDFF;
              }
              .info-back-box{
                display: none;
              }
              
            }
          }
        }
        &.loading{
          min-height: 200px;
          background-image: url('../../../../../../public/jvs-ui-public/img/loading.gif');
          background-repeat: no-repeat;
          background-position: center;
        }
        >div{
          position: relative;
          margin-bottom: 5px;
          cursor: pointer;
          &:nth-of-type(3n+2) {
            margin: 0 calc(50% - 108px);
          }
        }
        .getItem{
          margin-bottom: 4px;
          width: 72px;
          height: 72px;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          box-sizing: border-box;
          border-radius: 4px;
          &:hover{
            background: #F5F6F7;
            .icon{
              background: #E4EDFF;
            }
            .info-back-box{
              display: block;
            }
          }
          &.disabled{
            cursor: not-allowed;
          }
          .icon{
            margin-bottom: 4px;
            width: 40px;
            height: 40px;
            background: #F5F6F7;
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            svg{
              width: 18px;
              height: 18px;
            }
          }
          span{
            display: block;
            width: 72px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 12px;
            color: #363B4C;
            line-height: 17px;
            text-align: center;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: pre;
          }
          .info-back-box{
            display: none;
            position: absolute;
            top: 0;
            right: 0;
            width: 16px;
            height: 16px;
            svg{
              width: 16px;
              height: 16px;
              fill: #6F7588;
            }
            cursor: pointer;
          }
          .del-icon-svg{
            position: absolute;
            right: 8px;
            top: 0;
            width: 16px;
            height: 16px;
            box-sizing: border-box;
            display: none;
            &.edit{
              border: 1px solid rgba(54, 180, 82, .4);
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              div{
                width: 14px;
                height: 14px;
                background-color: #36B452;
                display: flex;
                align-items: center;
                justify-content: center;
                border-radius: 50%;
                svg{
                  fill: #fff;
                  width: 8px;
                  height: 8px;
                }
              }
            }
          }
        }
      }
    }
  }
}
.assembly-info-con-box{
  padding: 4px 16px;
  max-width: 288px;
  max-height: 146px;
  overflow: hidden;
  overflow-y: auto;
  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
  font-weight: 400;
  font-size: 14px;
  color: #6F7588;
  line-height: 18px;
}
</style>