<template>
  <div class="err-code-manage">
    <jvs-table
      :option="tableOption"
      :loading="tableLoading"
      :data="tableData"
      pageheadertitle='意见反馈'
      :page="page"
      @on-load="getList"
      @search-change="searchChange"
      @delRow="delRowHandle"
      @addRow="addRowHandle"
      @editRow="editRowHandle"
    >
      <template slot="menuRight">
        <div class="table-show-right-tool">
          <p @click="searchChange(queryParams)">
            <i class="el-icon-refresh-right"></i>
            <span>{{$langt('table.fresh')}}</span>
          </p>
        </div>
      </template>
      <template slot="createByHeadImg" slot-scope="scoped">
        <img style="cursor: pointer" :width="35" :height="35" :src="scoped.row.createByHeadImg" alt=""/>
      </template>
      <template slot="img" slot-scope="scoped">
        <img v-if="scoped.row.img.length > 0" style="cursor: pointer" :width="35" :height="35" :src="scoped.row.img[0]" alt="" @click="handlePreviewImg(scoped.row.img)"/>
      </template>
    </jvs-table>
    <el-image-viewer v-if="showViewer" :z-index="9999" :on-close="closeViewer" :url-list="previewSrcList"/>
  </div>
</template>

<script>
import {getOpinionList} from "./api";

export default {
  name: "index",
  components: {
    'el-image-viewer': () => import('element-ui/packages/image/src/image-viewer')
  },
  data () {
    return {
      showViewer: false,
      tableData: [],
      previewSrcList: [],
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
        page: true,
        submitLoading: false,
        column: [
          {
            label: '头像',
            prop: 'createByHeadImg',
            slot: true
          },
          {
            label: '用户',
            prop: 'createBy',
          },
          {
            label: '创建时间',
            prop: 'createTime',
            datetype: 'datetime',
            type: "datePicker",
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
          },
          {
            label: '内容',
            prop: 'text',
          },
          {
            label: '图片',
            prop: 'img',
            slot: true
          }
        ]
      },
    }
  },
  created () {
    this.tableOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`opinion.column.${col.prop}.label`)
      }
    })
  },
  methods: {
    // 预览图片
    handlePreviewImg(img) {
      this.showViewer = true
      this.previewSrcList = [...img]
    },
    // 关闭图片预览
    closeViewer() {
      this.showViewer = false
    },
    // 编辑、查看
    editRowHandle(row) {
    },
    // 添加
    addRowHandle(form) {
    },
    // 删除
    delRowHandle (row) {
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
      this.tableLoading = true
      let temp = JSON.parse(JSON.stringify(this.queryParams))
      getOpinionList(Object.assign(query, temp)).then(res => {
        if (res.data.code==0) {
          this.page.total=res.data.data.total
          this.page.currentPage=res.data.data.current
          this.tableData=res.data.data.records
          this.tableLoading = false
        }
      })
    },
  }
}
</script>