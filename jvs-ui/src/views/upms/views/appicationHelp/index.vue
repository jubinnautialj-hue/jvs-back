<template>
  <div class="app-help-page">
    <platform-page-header title="应用帮助" :is-more="true" :desc="$langt('common.devWarning')"/>
    <div v-if="$functionEnable('应用帮助')" style="padding: 20px;">
      <!-- 暂时屏蔽 -->
      <div v-if="false" class="tabs-box">
        <el-tabs v-model="activeName" @tab-click="handleTabClick">
          <el-tab-pane v-for="(item, key) in appList" :key="key" :label="item.label" :name="item.name"></el-tab-pane>
          <el-tab-pane label="用户管理" name="first"></el-tab-pane>
          <el-tab-pane label="配置管理" name="second"></el-tab-pane>
          <el-tab-pane label="角色管理" name="third"></el-tab-pane>
          <el-tab-pane label="定时任务补偿" name="fourth"></el-tab-pane>
        </el-tabs>
      </div>
      <el-form size="mini" :inline="true" :model="formInline" class="demo-form-inline">
        <el-form-item :label="$langt('appicationhelp.column.label.label')">
          <el-input v-model="queryParams.label" :placeholder="$langt('appicationhelp.column.label.label')"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSubmit">{{$langt('form.search')}}</el-button>
        </el-form-item>
      </el-form>
      <jvs-button icon="el-icon-plus" type="primary" @click="handleAdd">{{$langt('table.add')}}</jvs-button>
      <div class="table-box">
        <el-table
          v-loading="tableLoading"
          height="calc(100vh - 200px)"
          :data="tableData"
          :default-expand-all="false"
          row-key="id"
          :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
          <!--        <el-table-column-->
          <!--          prop="clientName"-->
          <!--          label="终端名称"-->
          <!--          width="180">-->
          <!--        </el-table-column>-->
          <el-table-column
            prop="type"
            :label="$langt('appicationhelp.column.type.label')"
            width="180">
          </el-table-column>
          <el-table-column
            prop="name"
            :label="$langt('appicationhelp.column.name.label')"
            width="180">
          </el-table-column>
          <el-table-column
            prop="label"
            :label="$langt('appicationhelp.column.label.label')"
            width="180">
          </el-table-column>
          <el-table-column
            prop="content"
            :label="$langt('appicationhelp.column.content.label')"
            width="180">
          </el-table-column>
          <el-table-column :label="$langt('table.oprate')">
            <template slot-scope="scope">
              <jvs-button size="mini" type="text" @click="handleEdit(scope.$index, scope.row)">{{$langt('table.edit')}}</jvs-button>
              <jvs-button size="mini" type="text" @click="handleAdd(scope.row)">{{$langt('appicationhelp.addHelp')}}</jvs-button>
              <jvs-button size="mini" type='text' @click="handleDel(scope.row)"><span style="color: #F56C6C;">{{$langt('table.delete')}}</span></jvs-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div class="no-permission-img" v-else>
      <img src="@/const/img/noPermission.jpg" alt=""/>
    </div>
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      top="6vh"
      width="60%"
      :close-on-click-modal="false"
      :before-close="handleClose">
      <div v-if="dialogVisible">
        <jvs-form :option="formOption" :formData="formData" @submit="handleSubmit" @cancalClick="handleClose">
        </jvs-form>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {getAppList} from "@/views/upms/views/application/api";
import {addHelp, deleteHelp, editHelp, getAppHelpList} from "@/views/upms/views/appicationHelp/api";
import PlatformPageHeader from "@/components/page-header/PlatformPageHeader";

export default {
  name: "index",
  components: {PlatformPageHeader},
  data() {
    return {
      formInline: {
        user: '',
        region: ''
      },
      parentId: null,
      activeName: '',
      dialogTitle: this.$langt('table.add'),
      dialogVisible: false,
      queryParams: {
        clientId: 'frame'
      },
      appList: [
      ],
      tableData: [],
      tableLoading: false,
      formData: {},
      formOption: {
        emptyBtn: false,
        submitBtnText: this.$langt('common.confirm'),
        submitLoading: false,
        column: [
          {
            label: "标识",
            prop: "label",
            rules: [{ required: true, message: '请输入标识', trigger: 'change' }],
          },
          {
            label: "内容",
            prop: "content",
            display: true,
            rules: [{ required: true, message: '请输入内容', trigger: 'change' }],
          },
          {
            label: "名称",
            prop: "name",
            display: false,
            rules: [{ required: true, message: '请输入名称', trigger: 'change' }],
          },
          {
            label: "类型",
            prop: "type",
            display: false,
            rules: [{ required: true, message: '请输入类型', trigger: 'change' }],
          },
        ]
      },
    }
  },
  created() {
    this.getAppList()
    this.formOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`appicationhelp.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`appicationhelp.column.${col.prop}.placeholder`)
      }
    })
  },
  mounted() {
  },
  methods: {
    onSubmit() {
      if (this.queryParams.label === '') {
        delete this.queryParams.label
      }
      this.getAppHelpList()
    },
    handleSubmit(form) {
      if (this.dialogTitle == this.$langt('table.add')) {
        if (this.parentId) {
          form.parentId = this.parentId
        }
        form.clientId = this.queryParams.clientId
        addHelp(form).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.addSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.dialogVisible = false
            this.getAppHelpList()
          }
        })
      } else {
        editHelp(form).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.editSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.dialogVisible = false
            this.getAppHelpList()
          }
        })
      }
    },
    // 获取所有应用帮助
    getAppHelpList() {
      this.tableLoading = true
      getAppHelpList(this.queryParams).then(res => {
        if (res.data && res.data.code == 0) {
          this.tableData = res.data.data || []
        }
        this.tableLoading = false
      }).catch(err => {
        this.tableLoading = false
      })
    },
    // 获取应用列表
    getAppList() {
      const params = {
        size: 1000,
        current: 1
      }
      getAppList(params).then(res => {
        if (res.data && res.data.code == 0) {
          const arr = res.data.data.records || []
          this.appList = arr.map(item => {
            return {
              label: item.name,
              name: item.appKey
            }
          })
          this.$nextTick(() => {
            this.activeName = this.appList[0].name
            // this.queryParams.clientId = this.appList[0].name
            this.queryParams.clientId = 'frame'
            this.getAppHelpList()
          })
        }
      }).catch(err => {
      })
    },
    handleTabClick(tab, event) {
      console.log(tab.name);
      this.queryParams.clientId = tab.name
      this.getAppHelpList()
    },
    // 新增帮助
    handleAdd(row) {
      if (row.id) {
        this.parentId = row.id
      }
      this.formOption.column[2].display = true
      this.formOption.column[3].display = true
      this.dialogTitle = this.$langt('table.add')
      this.dialogVisible = true
    },
    // 编辑帮助
    handleEdit(index, row) {
      this.formOption.column[2].display = false
      this.formOption.column[3].display = false
      this.dialogTitle = this.$langt('table.edit')
      this.formData = JSON.parse(JSON.stringify(row))
      this.dialogVisible = true
    },
    // 删除
    handleDel(row) {
      this.$confirm('确定删除该帮助吗?', this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        deleteHelp(row.id).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.delSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.getAppHelpList()
          }
        })
      }).catch(() => {
      })
    },
    handleClose() {
      this.dialogVisible = false
      this.formData = {}
    },
  },
}
</script>

<style lang="scss" scoped>
.app-help-page{
  height: 100%;
  background-color: #ffffff;
  /deep/.platform-page-header{
    padding: 0;
    padding-bottom: 15px;
    .title{
      display: none;
    }
  }
  .table-box{
    margin-top: 20px;
    /deep/.el-loading-mask{
      background-image: url('../../../../styles/loading.gif');
      background-repeat: no-repeat;
      background-position: center;
      //background-size: 300px 240px;
      .el-loading-spinner{
        .circular{
          display: none;
        }
      }
    }
    /deep/.el-table{
      margin-top: 0!important;
      .el-table__header-wrapper{
        margin: 0!important;
        border-radius: 6px;
        tr{
          th{
            box-sizing: border-box;
            height: 48px;
            //background-color: #F3F3F3;
            background-color: #F6F6F6;
            color: #333333;
            font-size: 14px;
            font-weight: normal;
            border: 0;
          }
        }
      }
      .el-table__body-wrapper{
        .el-table__body{
          tr{
            color: #555555;
            font-size: 12px;
            td{
              box-sizing: border-box;
              //height: 45px;
              height: 60px;
              .el-button--text{
                font-size: 12px;
                color: #3471FF;
              }
            }
          }
          tr:hover > td{
            background: #EFF2F7;
          }
        }
        .el-table__empty-block{
          border: 0;
          .el-table__empty-text{
            display: block;
            width: 457px;
          }
          .el-table__empty-text::before{
            content: "";
            display: block;
            width: 457px;
            height: 180px;
            background-image: url('../../../../const/img/emptyImage.svg');
            background-size: 260px 123px;
            background-repeat: no-repeat;
            background-position: center;
          }
        }
      }
    }
  }
}
</style>
