<template>
  <div class="sort-info">
    <div class="page-sort-setting-list">
      <div
        v-for="(row, index) in tableData"
        :key="'page-sort-set-item-'+index"
        :draggable="false"
        @dragstart="moveSTStart(row)"
        @dragenter="moveSTing(row)"
        @dragend="moveSTEnd"
        :class="{'page-sort-setting-list-item open': true, 'target': (moveTarget && (JSON.stringify(row) == JSON.stringify(moveTarget)))}">
        <div class="heade">
          <div class="name">{{row.fieldName}}</div>
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
            <span class="label">字段名称</span>
            <div class="con">
              <el-select v-model="row.fieldKey" size="mini" @change="fieldKeyChange(row)">
                <el-option
                  v-for="item in keyDicData"
                  :key="'key-item-'+index+item.value"
                  :label="item.value"
                  :value="item.value"
                >
                <span style="float: left;">{{item.label}}</span>
                <span style="float: right;color: #8492a6;font-size: 13px;">{{item.value}}</span>
                </el-option>
              </el-select>
            </div>
          </div>
          <div class="item-body-item">
            <span class="label">排序类型</span>
            <div class="con">
              <el-select v-model="row.direction" size="mini">
                <el-option label="升序" value="ASC"></el-option>
                <el-option label="降序" value="DESC"></el-option>
              </el-select>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="add-button-tool" @click="addRowHandle">
      <i class="el-icon-plus"></i>
      <span>添加排序条件</span>
    </div>
  </div>
</template>
<script>
export default {
  name: 'sort-info',
  props: {
    data: {
      type: Array,
      default: () => {
        return []
      }
    },
    tableSetNameOption: {
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
      keyDicData: []
    }
  },
  created () {
    this.getDicData()
  },
  methods: {
    // 新增一行
    addRowHandle () {
      this.tableData.push({direction: 'DESC'})
    },
    // fieldKey
    fieldKeyChange (row) {
      let tempLabel = ''
      for(let i in this.keyDicData) {
        if(this.keyDicData[i].value == row.fieldKey) {
          tempLabel = this.keyDicData[i].label
        }
      }
      row.fieldName = tempLabel
    },
    // 删除行
    deleteRow (row, index) {
      this.tableData.splice(index, 1)
    },
    getDicData () {
      let temp = []
      for(let i in this.tableSetNameOption) {
        temp.push({
          ...this.tableSetNameOption[i],
          label: this.tableSetNameOption[i].label || this.tableSetNameOption[i].fieldName,
          value: this.tableSetNameOption[i].prop || this.tableSetNameOption[i].fieldKey,
        })
      }
      this.keyDicData = temp
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
  watch: {
    tableSetNameOption (newVal, oldVal) {
      this.getDicData()
    }
  }
}
</script>
<style lang="scss" scoped>
.sort-info{
  max-height: 100%;
  padding-bottom: 16px;
  box-sizing: border-box;
  .page-sort-setting-list{
    padding: 0 16px;
    max-height: calc(100% - 64px);
    overflow: hidden;
    overflow-y: auto;
    &::-webkit-scrollbar{
      display: none;
    }
    .page-sort-setting-list-item{
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
