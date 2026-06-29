<template>
  <div>
    <el-table
      class="table-tree"
      :data="formData[prop]"
      style="width: 100%;"
      :row-key="option.rowKey"
      border
      default-expand-all
      :tree-props="{children: children, hasChildren: 'hasChildren'}">
      <el-table-column v-for="(column, cindex) in option.column" :label="column.label" :prop="column.prop" :key="column.prop + '_' + cindex" :width="column.width">
        <template slot-scope="scope">
          <span v-if="column.disabledRow && scope.row.disabled">-</span>
          <span v-else-if="option.childrenType && option.hasChildren && option.itemsPathEnd && ((scope.row[option.rowKey] && scope.row[option.rowKey].endsWith(option.itemsPathEnd)) ? (column.prop != option.hasChildren.prop) : false)">{{scope.row[column.prop] || '-'}}</span>
          <span v-else v-show="column.display ? (scope.row[column.display.prop] == column.display.value) : true">
            <el-input v-if="column.type == 'input' || (!column.type)" v-model="scope.row[column.prop]" :disabled="option.disabled" size="mini" @change="rowChange(scope.row, column.prop)"></el-input>
            <el-select v-if="column.type == 'select'" v-model="scope.row[column.prop]" :disabled="option.disabled" size="mini" :clearable="column.clearable" @change="rowChange(scope.row, column.prop)">
              <el-option
                v-for="item in column.dicData"
                :key="column.prop+'_'+item[column.props ? column.props.value : 'value']"
                :label="item[column.props ? column.props.label : 'label']"
                :value="item[column.props ? column.props.value : 'value']">
              </el-option>
            </el-select>
            <el-switch v-if="column.type == 'switch' && (!column.showCheck)" v-model="scope.row[column.prop]" :disabled="option.disabled" @change="rowChange(scope.row, column.prop)"></el-switch>
            <el-checkbox v-if="column.type == 'switch' && column.showCheck" v-model="scope.row[column.prop]" :disabled="option.disabled" @change="rowChange(scope.row, column.prop)"></el-checkbox>
          </span>
        </template>
      </el-table-column>
      <el-table-column v-if="!option.disabled" label="操作" width="60px">
        <template slot-scope="scope">
          <div class="line-icon-button">
            <div
              v-if="(option.addBtn !== false) && (option.hasChildren ? (option.hasChildren.add.split(',').indexOf(scope.row[option.hasChildren.prop]) > -1) : true)"
              class="button add-icon-button"
              @click="addLineHandle(scope.row, scope.$index)">
              <svg aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-xinjian"></use>
              </svg>
            </div>
            <div v-if="!(option.childrenType && option.hasChildren && option.itemsPathEnd && scope.row[option.rowKey] && scope.row[option.rowKey].endsWith(option.itemsPathEnd) && option.hasChildren.value.split(',').indexOf(scope.row[option.hasChildren.prop]) == -1)" class="button delete-icon-button" @click="deleteHandle(scope.row, scope.$index)">
              <span class="border-line"></span>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div class="table-tree-bottom">
      <div v-if="!option.disabled" class="top-button">
        <div class="button" @click="addHandle">
          <div class="icon">
            <svg aria-hidden="true">
              <use xlink:href="#jvs-ui-icon-xinjian"></use>
            </svg>
          </div>
          <span>新增一行</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: 'tableTree',
  props: {
    formData: {
      type: Object
    },
    prop: {
      type: String
    },
    children: {
      type: String,
      default: 'children'
    },
    option: {
      type: Object
    },
    itemsText: {
      type: String,
      default: 'Items'
    },
    itemsDefaultChildrenDicText: {
      type: String,
      default: 'string'
    }
  },
  data () {
    return {}
  },
  methods: {
    addHandle () {
      let tp = {}
      this.$set(tp, this.option.rowKey, `keyPath_${this.formData[this.prop].length+1}`)
      this.formData[this.prop].push(tp)
      this.$forceUpdate()
    },
    addLineHandle (row, index) {
      this.eachDataByPath(row[this.option.rowKey], '', this.formData[this.prop], 'add')
      this.$forceUpdate()
    },
    deleteHandle (row, index) {
      if(row[this.option.rowKey]) {
        let paths = row[this.option.rowKey].split('.')
        if(paths && paths.length > 1) {
          let parentPath = paths.slice(0, paths.length-1).join('.')
          this.eachDataByPath(parentPath, row[this.option.rowKey], this.formData[this.prop], 'delete')
        }else{
          let rindex = -1
          this.formData[this.prop].filter((rit, rix) => {
            if(rit[this.option.rowKey] == row[this.option.rowKey]) {
              rindex = rix
            }
          })
          if(rindex > -1) {
            this.formData[this.prop].splice(rindex, 1)
          }
        }
      }else{
        this.formData[this.prop].splice(index, 1)
      }
      this.$forceUpdate()
    },
    eachDataByPath (parentPath, path, data, oprate) {
      for(let i in data) {
        if(data[i][this.option.rowKey] == parentPath) {
          if(oprate == 'delete') {
            let index = -1
            data[i].children.filter((ch, cix) => {
              if(ch[this.option.rowKey] == path) {
                index = cix
              }
            })
            if(index > -1) {
              data[i].children.splice(index, 1)
            }
          }
          if(oprate == 'add') {
            if(!data[i].children) {
              this.$set(data[i], 'children', [])
            }
            let tp = {}
            this.$set(tp, this.option.rowKey, `${data[i].path}._keyPath${data[i].children.length+1}`)
            data[i].children.push(tp)
          }
        }else{
          if(data[i].children && data[i].children.length > 0) {
            this.eachDataByPath(parentPath, path, data[i].children, oprate)
          }
        }
      }
    },
    rowChange (row, prop) {
      if(prop == this.option.pathKey) {
        this.formatDataPath(this.formData[this.prop])
      }
      if(this.option.hasChildren && prop == this.option.hasChildren.prop) {
        this.formatDataPath(this.formData[this.prop], null, 'clear', row[this.option.rowKey])
      }
    },
    formatDataPath (data, parent, oprate, path) {
      for(let i in data) {
        if(this.option.hasChildren) {
          if(oprate == 'clear') {
            if(data[i][this.option.rowKey] == path) {
              if(data[i][this.children] && data[i][this.children].length > 0) {
                this.$set(data[i], this.children, [])
              }else{
                if((this.option.hasChildren.value.split(',').indexOf(data[i][this.option.hasChildren.prop]) > -1) && (data[i][this.option.hasChildren.prop] != this.option.hasChildren.add)) {
                  let items = {}
                  this.$set(items, this.option.rowKey, data[i][this.option.rowKey]+this.option.itemsPathEnd)
                  this.$set(items, this.option.pathKey, this.itemsText)
                  this.$set(items, this.option.hasChildren.prop, this.itemsDefaultChildrenDicText)
                  this.$set(data[i], this.children, [items])
                }
              }
            }
          }else{
            if(this.option.hasChildren.value.split(',').indexOf(data[i][this.option.hasChildren.prop]) == -1) {
              if(data[i][this.children]) {
                this.$set(data[i], this.children, [])
              }
            }
          }
        }
        if(data[i][this.option.pathKey]) {
          if(parent) {
            this.$set(data[i], this.option.rowKey, (data[i][this.option.pathKey] == this.itemsText) ? (parent+this.option.itemsPathEnd) : (parent + '.' + data[i][this.option.pathKey]))
          }else{
            this.$set(data[i], this.option.rowKey, data[i][this.option.pathKey])
          }
          if(data[i][this.children] && data[i][this.children].length > 0) {
            this.formatDataPath(data[i][this.children], data[i][this.option.rowKey] ? data[i][this.option.rowKey] : data[i][this.option.pathKey], oprate, path)
          }
        }
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.el-table.table-tree{
  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
  font-weight: 400;
  color: #606266;
  font-size: 12px;
  /deep/.el-table__row{
    .cell{
      display: flex;
      align-items: center;
    }
  }
}
.table-tree-bottom{
  .top-button{
    margin-top: 8px;
    .button{
      width: 80px;
      display: flex;
      align-items: center;
      cursor: pointer;
      .icon{
        width: 16px;
        height: 16px;
        background: #1E6FFF;
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 4px;
        svg{
          width: 12px;
          height: 12px;
          fill: #fff;
        }
      }
      span{
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #1E6FFF;
        line-height: 18px;
      }
    } 
  }
}

.line-icon-button{
  display: flex;
  align-items: center;
  .button{
    width: 16px;
    height: 16px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    cursor: pointer;
    &+.button{
      margin-left: 10px;
    }
  }
  .add-icon-button{
    background: #1E6FFF;
    svg{
      width: 12px;
      height: 12px;
      fill: #fff;
    }
  }
  .delete-icon-button{
    background: #36B452;
    .border-line{
      width: 10px;
      height: 2px;
      background: #fff;
      border-radius: 2px;
    }
  }
}

</style>