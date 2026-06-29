<template>
  <div class="system-space-page">
    <div class="page-title">存储空间</div>
    <el-divider/>
    <div class="system-space-data">
      <div class="item">
        <div class="item-title">文件存储空间</div>
        <div class="item-size">{{ sizeData.fileSumSize }}</div>
        <div class="item-desc">包含应用、文件、低代码、系统等多种业务所有文件格式统计</div>
      </div>
      <div style="width: 1px;background-color: #DCDFE6;height: 100px;"/>
      <div class="item">
        <div class="item-title">轻应用数据存储空间</div>
        <div class="item-size">{{ sizeData.dataSumSize }}</div>
        <div class="item-desc">数据量多，统计非为实时数据</div>
      </div>
    </div>
    <el-divider/>
    <div style="font-size: 14px">存储分布</div>
    <jvs-table
      :option="tableOption"
      :loading="tableLoading"
      :data="tableData"
      pageheadertitle='存储分布'
      :page="page"
      @on-load="getList"
      @search-change="searchChange"
    >
    </jvs-table>
  </div>
</template>

<script>
import store from "@/store";
import {getSpace} from "@/views/upms/views/systemSpace/api";

export default {
  name: "shortMessage",
  components: {
  },
  data () {
    return {
      tableData: [],
      sizeData: {},
      queryParams: {},
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      tableLoading: false,
      tableOption: {
        showOverflow: true,
        search: true,
        editBtn: false,
        viewBtn: false,
        addBtn: false,
        menu: false,
        cancal: false,
        page: false,
        submitLoading: false,
        column: [
          {
            label: '应用名称',
            width: '180',
            prop: 'name',
          },
          {
            label: '已使用容量',
            prop: 'size',
            sort: true
          },
        ]
      },
    }
  },
  created() {
    this.getSpace()
  },
  methods: {
    getSpace() {
      getSpace().then(res => {
        if (res.data && res.data.code == 0) {
          this.sizeData = JSON.parse(JSON.stringify(res.data.data))
          this.tableData = res.data.data.list || []
        }
      })
    },
    searchChange (form) {
      this.queryParams = form
      this.getList()
    },
    getList (page) {
      let query={
        current: this.page.currentPage,
        size: this.page.pageSize
      }
    },
  }
}
</script>

<style lang="scss" scoped>
.system-space-page {
  padding: 40px;
  border-radius: 6px;
  background-color: #ffffff;
  height: calc(100vh - 30px);
  overflow-y: auto;
  /deep/ .el-table__body-wrapper{
    overflow-y: auto !important;
    height:calc(100vh - 400px) !important;
    max-height: calc(100vh - 400px) !important;
  }
  .page-title{
    font-size: 18px;
  }
  .system-space-data{
    display: flex;
    justify-content: space-between;
    align-items: center;
    .item{
      padding: 0 20px;
      width: 50%;
      .item-title{
        font-size: 20px;
        margin-bottom: 10px;
        font-weight: 600;
      }
      .item-size{
        font-size: 24px;
        line-height: 48px;
      }
      .item-desc{
        color: #a2a3a5;
        font-size: 12px;
      }
    }
  }
}
</style>
