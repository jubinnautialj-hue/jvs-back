<template>
  <div :class="{'crud-box': true, 'curd-box-useback': (componentSetting && componentSetting.useBack), 'with-more': (linkList && linkList.length > 0), 'with-title': (componentSetting && componentSetting.name)}">
    <div v-if="linkList && linkList.length > 0" class="head-title">
      <span v-if="linkList.length == 1" @click="openEvent(linkList[0])">更多 <i class="el-icon-arrow-right"></i></span>
      <el-popover
        v-if="linkList.length > 1"
        placement="right"
        trigger="hover">
        <div class="print-list-items">
          <ul>
            <li v-for="pi in linkList" :key="pi.id" @click="openEvent(pi)">
              <span>{{pi.name}}</span>
            </li>
          </ul>
        </div>
        <span slot="reference">更多 <i class="el-icon-arrow-right"></i></span>
      </el-popover>
    </div>
    <jvs-table
      refs="multipleTable"
      :index="true"
      :loading="tableLoading"
      :page="page"
      :option="option"
      :data="tableData"
      @on-load="getListData">
    </jvs-table>
  </div>
</template>

<script>
import { getModelFields, getCurrentComponentOptions } from '../../../../src/page/main/dynaIndex/api'
export default {
  name: 'CRUD',
  props: {
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
    reqUrl: {
      type: String
    }
  },
  data () {
    return {
      queryFormData: {},
      option: {
        page: true,
        addBtn: false,
        showOverflow: true,
        menu: false,
        column: [],
      },
      tableData: [],
      tableLoading: false,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000], // 分页大小
      },
      linkList: []
    }
  },
  created () {
    console.log(this.element)
    if(this.element.queryParams) {
      let obj = {}
      this.element.queryParams.forEach(it => {
        obj[it.prop] = it.value
      })
      this.queryFormData = JSON.parse(JSON.stringify(obj))
      if(this.queryFormData.current) {
        this.$set(this.page, 'currentPage', Number(this.queryFormData.current))
      }
      if(this.queryFormData.size) {
        this.$set(this.page, 'pageSize', Number(this.queryFormData.size))
      }
    }
    if(this.queryFormData.modelIdentifier && this.queryFormData.fields && this.queryFormData.fields.length > 0) {
      getModelFields(this.queryFormData.modelIdentifier, this.queryFormData.fields).then(res => {
        if(res.data && res.data.code == 0) {
          res.data.data.filter(it => {
            if(it.type == 'imageUpload') {
              it.type = 'image'
            }
            if(it.type == 'image') {
              it.imgWidth = 80
              it.imgHeight = 80
            }
            if(it.type == 'fileUpload') {
              it.type = 'file'
            }
          })
          this.option.column = res.data.data
        }
      })
    }
  },
  methods: {
    getListData () {
      if(this.reqUrl) {
        let params = {
          type: this.element.material,
          componentMetaData: this.element.componentMetaData,
          queryParams: JSON.parse(JSON.stringify(this.element.queryParams))
        }
        let addCurrent = true
        let addSize = true
        params.queryParams.filter(qit => {
          if(qit.prop == 'current') {
            qit.value = this.page.currentPage + ''
            addCurrent = false
          }
          if(qit.prop == 'size') {
            qit.value = this.page.pageSize + ''
            addSize = false
          }
        })
        if(addCurrent) {
          params.queryParams.push({
            prop: 'current',
            value: this.page.currentPage + ''
          })
        }
        if(addSize) {
          params.queryParams.push({
            prop: 'size',
            value: this.page.pageSize + ''
          })
        }
        this.tableLoading = true
        getCurrentComponentOptions(this.reqUrl, params).then(res => {
          if(res.data && res.data.code == 0 && res.data.data && res.data.data.data) {
            this.$set(this.page, 'total', res.data.data.data.total)
            this.$set(this.page, 'currentPage', res.data.data.data.current)
            this.tableData = res.data.data.data.records
          }
          if(res.data.data.pages) {
            this.linkList = res.data.data.pages
          }
          this.tableLoading = false
        }).catch(e => {
          this.tableLoading = false
        })
      }
    },
    openEvent (link) {
      this.$openUrl(`/page-design-ui/#/list/use?id=${link.id}&jvsAppId=${link.jvsAppId}&dataModelId=${link.dataModelId}`)
    }
  }
}
</script>

<style lang="scss" scoped>
.crud-box{
  display: flex;
  align-items: center;
  height: 100%;
  width: 100%;
  user-select: none;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  overflow: hidden;
  border-radius: 4px;
  padding: 0 10px;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
  .head-title{
    position: fixed;
    top: 0;
    right: 0;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding: 0 12px;
    height: 40px;
    z-index: 1;
    span{
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 14px;
      color: #1E6FFF;
      cursor: pointer;
      line-height: 20px;
      i{
        font-size: 12px;
      }
    }
  }
  /deep/.jvs-table{
    width: 100%;
    height: 100%;
    .jvs-table-titleTop{
      display: none;
    }
    .table-body-box{
      height: calc(100% - 48px);
      .el-table{
        height: 100%;
        .el-table__body-wrapper{
          height: calc(100% - 40px)!important;
          .el-table__empty-block{
            min-height: unset;
            .el-table__empty-text{
              width: 50%;
              height: 100%;
              line-height: 30px;
              &::before{
                width: 100%;
                height: calc(100% - 40px);
                background-size: 130px 80%;
              }
            }
          }
        }
      }
    }
    .tablepagination{
      padding: 10px 0;
    }
  }
  &.with-more{
    padding-top: 40px;
    &.with-title{
      padding-top: 0;
      .head-title{
        top: 12px;
      }
    }
  }
}
.curd-box-useback{
  /deep/.jvs-table{
    background: transparent;
    .table-body-box{
      background: transparent;
      .el-table{
        background: transparent;
        .el-table__header{
          tr, th{
            background: transparent;
          }
        }
      }
    }
    .tablepagination{
      background: transparent;
      button, .el-input__inner{
        background: transparent;
      }
    }
  }
}

.print-list-items{
  ul{
    list-style: none;
    margin: 0;
    padding: 0;
  }
  li{
    height: 28px;
    line-height: 28px;
    padding: 6px 24px;
    cursor: pointer;
    transition: 0.3s;
    &:hover{
      transition: 0.3s;
      background-color: #eff2f7;
    }
    i{
      margin-right: 10px;
    }
  }
}
</style>