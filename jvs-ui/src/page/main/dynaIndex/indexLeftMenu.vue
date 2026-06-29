<template>
  <div class="dynaIndex-left-menu">
    <div class="left-menu-top">
      <div class="left-menu-title">门户</div>
      <div class="left-menu-home-list">
        <div
          v-for="home in homeList"
          :key="home.id"
          :class="{'left-menu-home-list-item': true, 'active': active == home.id, 'target': (moveTarget && (home.id == moveTarget.id))}"
          @click="setActive(home.id)"
          :draggable="true"
          @dragstart="moveHomeStart(home)"
          @dragenter="moveHomeing(home)"
          @dragend="moveHomeEnd"
        >
          <svg v-if="active == home.id" aria-hidden="true">
            <use xlink:href="#icon-jvs-xuanzhong"></use>
          </svg>
          <span :title="home.title">{{home.title}}</span>
          <i class="el-icon-delete" @click.stop="deleteItem(home)"></i>
        </div>
      </div>
    </div>
    <div class="left-menu-slider">
      <svg aria-hidden="true">
        <use xlink:href="#icon-jvs-tuodong1"></use>
      </svg>
    </div>
    <div class="left-menu-content">
      <div class="search">
        <div v-if="showInput" style="flex: 1;margin-right: 8px;">
          <el-input v-model="keyword" size="mini" @input="searchHandle"></el-input>
        </div>
        <div v-else class="title">卡片</div>
        <i class="el-icon-search" :style="showInput ? 'color: #1E6FFF;' : ''" @click="showInput=!showInput;"></i>
      </div>
      <div class="left-menu-card-list">
        <draggable v-for="card in searchList" :key="card.type" @start="handleDragstart($event, card)" @end="handleDragEnd" v-model="searchList" :options="draggableOptions">
          <div  class="left-menu-card-item">
            <svg aria-hidden="true">
              <use :xlink:href="`#${card.icon}`"></use>
            </svg>
            <span>{{card.name}}</span>
          </div>
        </draggable>
      </div>
    </div>
  </div>
</template>
<script>
import { getStore } from "@/util/store";
import eventBus from "@/util/vuebus";
import { sortHomeList } from './api'
export default {
  name: 'dynaIndexLeftMenu',
  data () {
    return {
      active: '',
      showInput: false,
      keyword: '',
      searchList: [],
      draggableOptions: {
        preventOnFilter: false,
        sort: false,
        disabled: false,
        ghostClass: 'tt',
        forceFallback: true
      },
      currentItem: null,
      moveSource: null,
      moveTarget: null,
    }
  },
  computed: {
    homeList () {
      return getStore({name: 'dynaIndexLeftList'}) || []
    },
    indexData () {
      return getStore({name: 'dynaIndexLeftData'})
    },
    componentList () {
      return getStore({name: 'dynaIndexLeftComList'})
    }
  },
  created () {
    if(this.indexData) {
      this.active = this.indexData.id
    }
    if(this.componentList) {
      this.searchList = JSON.parse(JSON.stringify(this.componentList))
    }
  },
  methods: {
    setActive (id) {
      this.active = id
      eventBus.$emit('dynaIndex', {oprate: 'changeHome', data: {id: id}})
      this.$forceUpdate()
    },
    deleteItem (item) {
      eventBus.$emit('dynaIndex', {oprate: 'deleteHome', data: {id: item.id}})
    },
    searchHandle () {
      if(this.keyword) {
        let list = []
        this.componentList.filter(com => {
          if(com.name.includes(this.keyword)) {
            list.push(com)
          }
        })
        this.searchList = list
        this.$forceUpdate()
      }else{
        this.searchList = JSON.parse(JSON.stringify(this.componentList))
      }
    },
    handleDragstart(e, item) {
      this.currentItem = item
    },
    handleDragEnd(evt, e) {
      eventBus.$emit('dynaIndex', {oprate: 'addCom', data: this.currentItem})
      this.$forceUpdate()
    },
    initStore () {
      eventBus.$emit('dynaIndex', {oprate: 'initStore'})
    },
    moveHomeStart (home) {
      this.moveSource = JSON.parse(JSON.stringify(home))
    },
    moveHomeing (home) {
      this.moveTarget = JSON.parse(JSON.stringify(home))
    },
    moveHomeEnd () {
      if(this.moveTarget && this.moveTarget.id != this.moveSource.id) {
        let from = -1
        this.homeList.filter((bit, bix) => {
          if(bit.id == this.moveSource.id) {
            from = bix
          }
        })
        if(from > -1) {
          
          this.$forceUpdate()
        }
        let to = -1
        this.homeList.filter((bit, bix) => {
          if(bit.id == this.moveTarget.id) {
            to = bix
          }
        })
        if(from > -1 && to > -1) {
          let tempId = []
          this.homeList.filter(hit => {
            tempId.push(hit.id)
          })
          tempId.splice(from, 1)
          tempId.splice(to, 0, this.moveSource.id)
          sortHomeList(tempId).then(res => {
            this.homeList.splice(from, 1)
            this.homeList.splice(to, 0, this.moveSource)
            if(res.data && res.data.code == 0) {
              eventBus.$emit('dynaIndex', {oprate: 'fresh'})
            }
          })
          this.$forceUpdate()
        }
      }
      this.moveSource = null
      this.moveTarget = null
    }
  }
}
</script>
<style lang="scss" scoped>
.dynaIndex-left-menu{
  width: 100%;
  height: 100%;
  background: #fff;
  overflow: hidden;
  overflow-y: auto;
  user-select: none;
  .left-menu-title{
    height: 48px;
    line-height: 48px;
    padding: 0 24px;
    font-family: Microsoft YaHei-Bold, Microsoft YaHei;
    font-weight: 700;
    font-size: 14px;
    color: #363B4C;
  }
  .left-menu-top{
    padding-bottom: 17px;
    border-bottom: 1px solid #EEEFF0;
    .left-menu-title{
      background: linear-gradient( 179deg, rgba(30,111,255,0.05) 0%, rgba(30,111,255,0) 100%);
    }
    .left-menu-home-list{
      padding: 0 8px;
      .left-menu-home-list-item{
        height: 32px;
        padding: 0 16px;
        border-radius: 4px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
        display: flex;
        align-items: center;
        box-sizing: border-box;
        overflow: hidden;
        cursor: pointer;
        svg{
          width: 16px;
          height: 16px;
          fill: #1E6FFF;
        }
        span{
          margin-left: 24px;
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: pre;
        }
        .el-icon-delete{
          display: none;
          color: #F56C6C;
          font-size: 16px;
          font-weight: bold;
          margin-left: 3px;
        }
        &.active{
          background: #DDEAFF;
          color: #1E6FFF;
          span{
            margin-left: 8px;
          }
        }
        &:hover{
          background: #DDEAFF;
          .el-icon-delete{
            display: inline;
          }
        }
        &+.left-menu-home-list-item{
          margin-top: 10px;
        }
        &.target{
          position: relative;
          overflow: unset;
          &::after{
            content: '';
            position: absolute;
            top: -8px;
            left: 0;
            width: 100%;
            height: 1px;
            background: #1E6FFF;
          }
        }
      }
    }
  }
  .left-menu-slider{
    width: 100%;
    height: 24px;
    margin-top: -12px;
    display: flex;
    justify-content: center;
    svg{
      width: 24px;
      height: 24px;
    }
  }
  .left-menu-content{
    .search{
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 28px;
      padding: 0 16px;
      box-sizing: border-box;
      .title{
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 14px;
        color: #363B4C;
        margin: 0;
        padding: 0;
      }
      .el-icon-search{
        cursor: pointer;
        font-size: 16px;
      }
    }
    .left-menu-card-list{
      padding: 0 8px;
      padding-bottom: 8px;
      user-select: none;
      .left-menu-card-item{
        margin-top: 10px;
        display: flex;
        align-items: center;
        height: 32px;
        padding: 0 16px;
        box-sizing: border-box;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
        cursor: pointer;
        border-radius: 4px;
        svg{
          width: 16px;
          height: 16px;
          margin-right: 8px;
        }
        span{
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: pre;
        }
        &:hover{
          background: #F5F6F7;
        }
      }
    }
  }
}
</style>