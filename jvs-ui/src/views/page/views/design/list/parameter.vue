<template>
  <div class="parameters-form">
    <div class="page-param-setting-list">
      <div
        v-for="(row, index) in tableData"
        :key="'page-param-set-item-'+index"
        :draggable="false"
        @dragstart="moveSTStart(row)"
        @dragenter="moveSTing(row)"
        @dragend="moveSTEnd"
        :class="{'page-param-setting-list-item open': true, 'target': (moveTarget && (JSON.stringify(row) == JSON.stringify(moveTarget)))}">
        <div class="heade">
          <div class="name">{{getLabel(row.key)}}</div>
          <div class="heade-tool">
            <div class="con-box">
              <svg aria-hidden="true" @click="deleteRow(row, index)">
                <use xlink:href="#jvs-ui-icon-shanchuyonghu"></use>
              </svg>
              <!-- <span class="divider-line"></span>
              <svg class="move-icon" aria-hidden="true" @mousedown="moveSTStart(row)">
                <use xlink:href="#icon-jvs-tuodong"></use>
              </svg> -->
            </div>
          </div>
        </div>
        <div class="item-body">
          <div class="item-body-item">
            <span class="label">字段名</span>
            <div class="con">
              <el-select v-model="row.key" placeholder="请选择" size="mini" v-if="custFilterList && custFilterList.length > 0">
                <el-option
                  v-for="it in custFilterList"
                  :key="it.fieldDto.fieldKey+'-'+index"
                  v-show="needShow(it.fieldDto.fieldKey)"
                  :label="it.fieldDto.fieldKey"
                  :value="it.fieldDto.fieldKey">
                  <span style="float: left;">{{ it.fieldDto.fieldName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px;">{{ it.fieldDto.fieldKey }}</span>
                </el-option>
              </el-select>
            </div>
          </div>
          <div class="item-body-item">
            <span class="label">规则</span>
            <div class="con">
              <el-select v-model="row.operator" placeholder="请选择" size="mini">
                <el-option label="等于" value="eq" v-if="getAttrVal('dataQueryTypes', row.key).indexOf('eq') > -1"></el-option>
                <el-option label="不等于" value="ne" v-if="getAttrVal('dataQueryTypes', row.key).indexOf('ne') > -1"></el-option>
                <el-option label="包含" value="in" v-if="getAttrVal('dataQueryTypes', row.key).indexOf('in') > -1"></el-option>
                <el-option label="不包含" value="notIn" v-if="getAttrVal('dataQueryTypes', row.key).indexOf('notIn') > -1"></el-option>
                <el-option label="大于" value="gt" v-if="getAttrVal('dataQueryTypes', row.key).indexOf('gt') > -1"></el-option>
                <el-option label="大于等于" value="ge" v-if="getAttrVal('dataQueryTypes', row.key).indexOf('ge') > -1"></el-option>
                <el-option label="小于" value="lt" v-if="getAttrVal('dataQueryTypes', row.key).indexOf('lt') > -1"></el-option>
                <el-option label="小于等于" value="le" v-if="getAttrVal('dataQueryTypes', row.key).indexOf('le') > -1"></el-option>
              </el-select>
            </div>
          </div>
          <div class="item-body-item">
            <span class="label">值</span>
            <div class="con">
              <el-input v-if="row.key && getAttrVal('values', row.key).length == 0" v-model="row.value" size="mini"></el-input>
              <el-select v-else v-model="row.value" placeholder="请选择或输入" size="mini" filterable allow-create multiple :collapse-tags="false">
                <el-option v-for="(fi, fix) in getAttrVal('values', row.key)" :key="'fitem-'+fix" :label="fi.name" :value="fi.value"></el-option>
              </el-select>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="add-button-tool" @click="addRowHandle">
      <i class="el-icon-plus"></i>
      <span>添加列表过滤</span>
    </div>
  </div>
</template>
<script>
import {getDataModelDataFilter} from "@/views/page/api/list";
export default {
  name: 'parameters-form',
  props: {
    data: {
      type: Array,
      default: () => {
        return []
      }
    },
    roleList: {
      type: Array
    },
    dataModelId: {
      type: String
    },
  },
  computed: {
    tableData: {
      get () {
        return this.data
      },
      set () {}
    }
  },
  data(){
    return{
      moveSource: null,
      moveTarget: null,
      custFilterList: [],
    }
  },
  methods: {
    addRowHandle () {
      this.tableData.push({})
    },
    deleteRow (row, index) {
      this.tableData.splice(index, 1)
    },
    getAttrVal (attr, val) {
      let temp = []
      for(let i in this.custFilterList) {
        if(this.custFilterList[i].fieldDto['fieldKey'] == val) {
          if(this.custFilterList[i][attr]) {
            temp = this.custFilterList[i][attr]
          }
        }
      }
      return temp
    },
    needShow (key) {
      let bool = true
      this.tableData.filter(item => {
        if(item.key && item.key == key) {
          bool = false
        }
      })
      return bool
    },
    getLabel (value) {
      let str = value
      if(value && this.custFilterList && this.custFilterList.length > 0) {
        this.custFilterList.filter(cit => {
          if(cit.fieldDto.fieldKey == value) {
            str = cit.fieldDto.fieldName
          }
        })
      }
      return str
    },
    moveSTStart (row) {
      this.moveSource = JSON.parse(JSON.stringify(row))
    },
    moveSTing (row) {
      this.moveTarget = JSON.parse(JSON.stringify(row))
    },
    moveSTEnd () {
      if(this.moveTarget && JSON.stringify(this.moveTarget) != JSON.stringify(this.moveSource)) {
        let from = -1
        this.tableData.filter((bit, bix) => {
          if(JSON.stringify(bit) == JSON.stringify(this.moveSource)) {
            from = bix
          }
        })
        if(from > -1) {
          this.tableData.splice(from, 1)
          this.$forceUpdate()
        }
        let to = -1
        this.tableData.filter((bit, bix) => {
          if(JSON.stringify(bit) == JSON.stringify(this.moveTarget)) {
            to = bix
          }
        })
        if(to > -1) {
          this.tableData.splice(to, 0, this.moveSource)
          this.$forceUpdate()
        }
      }
      this.moveSource = null
      this.moveTarget = null
    }
  },
  created () {
    getDataModelDataFilter(this.$route.query.jvsAppId, this.dataModelId).then(res => {
      if(res.data && res.data.code == 0) {
        this.custFilterList = res.data.data
      }
    })
  }
}
</script>
<style lang="scss" scoped>
.parameters-form{
  max-height: 100%;
  padding-bottom: 16px;
  box-sizing: border-box;
  .page-param-setting-list{
    padding: 0 16px;
    max-height: calc(100% - 64px);
    overflow: hidden;
    overflow-y: auto;
    &::-webkit-scrollbar{
      display: none;
    }
    .page-param-setting-list-item{
      margin-top: 16px;
      position: relative;
      &.open{
        .heade{
          border-radius: 4px 4px 0 0;
        }
      }
      &.target{
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
      .heade{
        height: 44px;
        background: #F5F6F7;
        border-radius: 4px;
        border: 1px solid #EEEFF0;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 16px;
        .name{
          font-family: Source Han Sans-Medium, Source Han Sans;
          font-weight: 500;
          font-size: 14px;
          color: #363B4C;
        }
        .heade-tool{
          display: flex;
          align-items: center;
          svg{
            width: 16px;
            height: 16px;
            fill: #6F7588;
            cursor: pointer;
          }
          .move-icon{
            cursor: move;
          }
          .divider-line{
            display: block;
            width: 1px;
            height: 14px;
            background: #EEEFF0;
            margin: 0 8px;
          }
          .con-box{
            display: flex;
            align-items: center;
          }
        }
      }
      .item-body{
        border: 1px solid #EEEFF0;
        border-top: 0;
        border-radius: 0 0 4px 4px;
        background: #fff;
        padding-bottom: 9px;
        overflow: hidden;
        .item-body-item{
          display: flex;
          align-items: baseline;
          padding: 0 16px;
          min-height: 32px;
          margin-top: 9px;
          .label{
            width: 56px;
            margin-right: 16px;
            font-family: Microsoft YaHei-Regular, Microsoft YaHei;
            font-weight: 400;
            font-size: 14px;
            color: #6F7588;
            word-break: keep-all;
          }
          .con{
            width: calc(100% - 72px);
            display: flex;
            align-items: center;
            svg{
              width: 16px;
              height: 16px;
              fill: #6F7588;
              cursor: pointer;
            }
          }
        }
      }
    }
  }
  .add-button-tool{
    margin: 16px 16px 0 16px;
    height: 32px;
    background: #E4EDFF;
    border-radius: 4px;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #1E6FFF;
    text-align: center;
    line-height: 32px;
    cursor: pointer;
    span{
      margin-left: 6px;
    }
  }
}
</style>
