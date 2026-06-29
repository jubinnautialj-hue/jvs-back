<template>
  <div class="headtitle-info">
    <div class="page-headtitle-setting-list">
      <div
        v-for="(row, index) in tableData"
        :key="'page-hesdtitle-set-item-'+index"
        :draggable="false"
        @dragstart="moveHTStart(row)"
        @dragenter="moveHTing(row)"
        @dragend="moveHTEnd"
        :class="{'page-headtitle-setting-list-item open': true, 'target': (moveTarget && (JSON.stringify(row) == JSON.stringify(moveTarget)))}">
        <div class="heade">
          <div class="name">{{row.tableTitle}}</div>
          <div class="heade-tool">
            <div class="con-box">
              <svg aria-hidden="true" @click="deleteRow(row, index)">
                <use xlink:href="#jvs-ui-icon-shanchuyonghu"></use>
              </svg>
              <!-- <span class="divider-line"></span>
              <svg class="move-icon" aria-hidden="true" @mousedown="moveHTStart(row)">
                <use xlink:href="#icon-jvs-tuodong"></use>
              </svg> -->
            </div>
          </div>
        </div>
        <div class="item-body">
          <div class="item-body-item">
            <span class="label">表头名称</span>
            <div class="con">
              <el-input v-model="row.tableTitle" placeholder="请输入表头名称" size="mini" @change="$emit('getColumn', true)"></el-input>
            </div>
          </div>
          <div class="item-body-item">
            <span class="label">覆盖字段</span>
            <div class="con">
              <el-select v-model="row.fieldKey" multiple @change="fieldKeyChange(row, index)" size="mini" clearable>
                <el-option
                  v-for="item in keyDicData"
                  :key="'key-item-'+index+item.value"
                  :label="item.label"
                  :value="item.value"
                  :disabled="alreadyKey.indexOf(item.value) > -1 && row.fieldKey.indexOf(item.value) == -1"
                ></el-option>
              </el-select>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="add-button-tool" @click="addRowHandle">
      <i class="el-icon-plus"></i>
      <span>添加表头</span>
    </div>
  </div>
</template>
<script>
export default {
  name: 'head-info',
  props: {
    data: {
      type: Array,
      default: () => {
        return []
      }
    },
    designColumn: {
      type: Array
    }
  },
  computed: {
    tableData: {
      get () {
        return this.data
      },
      set () {}
    }
  },
  data () {
    return {
      moveSource: null,
      moveTarget: null,
      keyDicData: [],
      alreadyKey: []
    }
  },
  created () {
    this.getDicData()
    this.getAlready()
  },
  methods: {
    // 新增一行
    addRowHandle () {
      this.tableData.push({fieldKey: []})
    },
    // fieldKey
    fieldKeyChange (row, index) {
      this.getAlready()
      this.$forceUpdate()
      this.$emit('getColumn', true)
    },
    // 删除行
    deleteRow (row, index) {
      this.tableData.splice(index, 1)
      this.getAlready()
      this.$emit('getColumn', true)
    },
    getDicData () {
      let temp = []
      for(let i in this.designColumn) {
        temp.push({
          ...this.designColumn[i],
          label: this.designColumn[i].showChinese,
          value: this.designColumn[i].aliasColumnName
        })
      }
      this.keyDicData = temp
    },
    getAlready () {
      let temp = []
      for(let i in this.tableData) {
        if(this.tableData[i].fieldKey && this.tableData[i].fieldKey.length > 0) {
          for(let j in this.tableData[i].fieldKey) {
            if(temp.indexOf(this.tableData[i].fieldKey[j]) == -1) {
              temp.push(this.tableData[i].fieldKey[j])
            }
          }
        }
      }
      this.alreadyKey = temp
      this.$forceUpdate()
    },
    moveHTStart (row) {
      this.moveSource = JSON.parse(JSON.stringify(row))
    },
    moveHTing (row) {
      this.moveTarget = JSON.parse(JSON.stringify(row))
    },
    moveHTEnd () {
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
  watch: {
    designColumn (newVal, oldVal) {
      this.getDicData()
    }
  }
}
</script>
<style lang="scss" scoped>
.headtitle-info{
  height: 100%;
  .page-headtitle-setting-list{
    padding: 0 16px;
    max-height: calc(100% - 64px);
    overflow: hidden;
    overflow-y: auto;
    &::-webkit-scrollbar{
      display: none;
    }
    .page-headtitle-setting-list-item{
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
          align-items: center;
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
    margin: 16px;
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
