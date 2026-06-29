<template>
  <div class="bulletin-page">
    <jvs-table
      :pageheadertitle='pageheadertitle'
      :option="option"
      :data="tableData"
      :loading="tableLoading"
      :page="page"
      @on-load="getList"
      @search-change="searchChange"
      @addRow="addRowHandle"
      @editRow="editRowHandle"
      @delRow="delRowHandle"
    >
      <template slot="menuRight">
        <div class="table-show-right-tool">
          <p @click="searchChange(queryParams)">
            <i class="el-icon-refresh-right"></i>
            <span>{{$langt('table.fresh')}}</span>
          </p>
        </div>
      </template>
    </jvs-table>
  </div>
</template>
<script>
import {add, edit, del,getUserLevelList} from './api'
export default {
  components: {},
  props: {
    propData: {
      type: Object
    }
  },
  data () {
    return {
      submitType: 'add',
      dialogTitle: '',
      tableLoading: false, // loading显示
      tableData: [], // 列表数据
      pageheadertitle: '公告管理', // fixed me  表名
      option: {
        page: true,
        addBtn: this.$permissionMatch("jvs_level_add"),
        editBtn: this.$permissionMatch("jvs_level_edit"),
        delBtn: this.$permissionMatch("jvs_level_delete"),
        canncal: false,
        viewBtn: false,
        // align: 'center',
        // menuAlign: 'center',
        search: true,
        cancal: false,
        showOverflow: true,
        column: [
          {
            label: '排序',
            prop: 'sort',
          },
          {
            label: '等级名称',
            prop: 'name',
          },
          {
            label: '首页地址',
            prop: 'indexUrl',
            addDisplay: false,
            editDisplay: false,
          },
        ]
      },
      appList: [],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
      },
      queryParams: {}, // 查询条件
      editor: null,
      previewEditor: null,
    }
  },
  watch: {
    formData: {
      handler(newVal, oldVal) {
        this.formOption.column[1].display = newVal.type === 'PC';
      },
      deep: true
    }
  },
  created () {
    this.option.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`userLevel.${col.prop}.label`)
      }
    })
  },
  methods: {
    // 表格分页查询
    getList (page) {
      this.tableLoading = true
      getUserLevelList().then(res => {
        if (res.data.code==0) {
          this.tableData=res.data.data
        }
        this.tableLoading = false
      }).catch(err => {
        this.tableLoading = false
      })
    },
    // 条件查询
    searchChange (form) {
      this.queryParams=form
      this.getList()
    },
    // 关闭 弹窗
    handleClose() {
      this.formData = {
        appKeys:[],
        contentType: 'IMG',
        type: 'PC'
      }
      this.dialogVisible = false
    },
    // 新增
    addRowHandle (form) {
      form.sort = Number(form.sort)
      add(form).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.addSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.handleClose()
          this.getList()
        }
        this.formOption.submitLoading = false
      }).catch(err => {
        this.formOption.submitLoading = false
      })
    },
    // 编辑
    editRowHandle (form) {
      edit(form).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.editSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.handleClose()
          this.getList()
        }
        this.formOption.submitLoading = false
      }).catch(err => {
        this.formOption.submitLoading = false
      })
    },
    // 删除
    delRowHandle (row) {
      del(row).then(res => {
        if(res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.delSuccess'),
            position: 'bottom-right',
            type: 'success'
          });
          this.getList()
        }
      })
    }
  }
}
</script>
<style lang="scss">
.bulletin-page{
  .el-dialog.is-fullscreen{
    .el-dialog__body{
      padding: 0;
    }
  }
}
</style>
