<template>
  <div class="tree-dictionaries">
    <platform-page-header title="应用字典" :is-more="false" :desc="$langt('common.devWarning')"/>
    <div v-if="$functionEnable('应用字典')">
      <div class="jvs-table left-tree-table">
        <PageHeader title="分类字典" :class="{'jvs-table-titleTop': true, 'jvs-table-top': true}">
          <div class="top">
            <el-row style="margin-left:10px;width: 230px;">
              <jvs-button size="mini" type="primary" icon="el-icon-plus" permisionFlag="jvs_app_tree_add" @click="addEditItem(null)">新增</jvs-button>
              <el-upload
                v-if="$permissionMatch('jvs_app_tree_import')"
                style="display:inline-block;margin:0 10px;"
                action="/mgr/jvs-design/platform/app/tree/import"
                :multiple="false"
                :limit="1"
                ref="uploadBtn"
                :file-list="fileList"
                :show-file-list="false"
                :on-success="uploadSuccess"
                :on-error="errHandle"
                :headers="headers">
                <jvs-button size="mini" type="primary">导入</jvs-button>
              </el-upload>
              <jvs-button v-if="$permissionMatch('jvs_app_tree_export')" size="mini" type="primary" @click="exportAll">下载模板</jvs-button>
            </el-row>
            <jvs-form style="flex:1;" :option="searchOption" :formData="searchForm" @submit="getList"></jvs-form>
          </div>
        </PageHeader>
      </div>
      <div v-if="dataLoading" class="loading-back"/>
      <el-tree class="tree-dic" :data="data" :props="defaultProps" :expand-on-click-node="false">
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span class="label">{{ data.parentId == -1 ? data.name : node.label }}</span>
        <span class="tool">
          <el-popover
            placement="bottom"
            width="30"
            v-model="data.show"
            trigger="click">
            <ul class="tool-type-list">
              <li v-if="$permissionMatch('jvs_app_tree_edit')" @click.stop="addEditItem(data)">
                <span>编辑</span>
              </li>
              <li v-if="$permissionMatch('jvs_app_tree_add')" @click.stop="addEditItem(data, 'children')">
                <span>新增子项</span>
              </li>
              <li v-if="$permissionMatch('jvs_app_tree_export')" @click.stop="exportItem(data)">
                <span>导出</span>
              </li>
              <li v-if="$permissionMatch('jvs_app_tree_delete')" @click.stop="delItem(data)">
                <span style="color: #F56C6C;">删除</span>
              </li>
            </ul>
            <i slot="reference" class="el-icon-more"></i>
          </el-popover>
        </span>
      </span>
      </el-tree>
    </div>
    <div class="no-permission-img" v-else>
      <img src="@/const/img/noPermission.jpg" alt=""/>
    </div>
    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      append-to-body
      :before-close="handleClose">
      <jvs-form v-if="dialogVisible" :option="formOption" :formData="form" @submit="submitHandle"></jvs-form>
    </el-dialog>
  </div>
</template>
<script>
import PageHeader from '@/components/page-header/PageHeader'
import {getTreeList, addTree, editTree, delTree, exportTreeDic} from '@/api/application'
import PlatformPageHeader from "@/components/page-header/PlatformPageHeader";
export default {
  components: {PlatformPageHeader, PageHeader},
  data() {
    return {
      dataLoading: false,
      data: [], // 所有树形结构
      defaultProps: {
        children: 'children',
        label: 'name',
        value: 'value'
      },
      searchForm: {},
      searchOption: {
        cancal: false,
        submitBtnText: "查询",
        labelWidth: 'auto',
        search: true,
        inline: true,
        isSearch: true,
        column: [
          {
            label: "名称",
            prop: "name",
            span: 6
          }
        ]
      },
      title: "", // 弹框标题
      dialogVisible: false,
      type: '', // 操作类型
      form: {}, // 字典项
      formOption: {
        cancal: false,
        submitLoading: false,
        column: [
          {
            label: "名称",
            prop: "name",
            rules: [
              { required: true, message: '请输入名称', trigger: 'blur' },
              { max: 20, message: '不得超过20个字', trigger: 'blur' }
            ],
            display: true
          },
          {
            label: "值",
            prop: "value",
            rules: [
              { required: true, message: '请输入值', trigger: 'blur' }
            ],
            display: true
          }
        ]
      },
      currentData: null,
      fileList: [],
      headers: {
        tenantId: this.$store.getters.userInfo.tenantId,
        Authorization: 'Bearer ' + this.$store.getters.access_token
      }
    }
  },
  methods: {
    // 导出
    exportItem(data) {
      if (data && data.extend) {
        exportTreeDic({ uniqueName: data.extend.uniqueName}).then(res => {
          if (res.data) {
            let name = res.headers["content-disposition"].split(";")[1]
            name = name.split("=")[1]
            name = decodeURI(name)
            this.downloadFile(name, res.data)
          }
        })
      }
    },
    // 下载文件
    downloadFile(filename, content) {
      var elink = document.createElement('a')
      if(filename) {
        elink.download = filename
      }
      elink.style.display = 'none'
      var blob = new Blob([content], {})
      elink.href = URL.createObjectURL(blob)

      document.body.appendChild(elink)
      elink.click()

      document.body.removeChild(elink)
    },
    getList(form) {
      this.dataLoading = true
      getTreeList(form).then(res => {
        if(res.data.code == 0) {
          this.data = res.data.data
          this.dataLoading = false
        } else {
          this.dataLoading = false
        }
      }).catch(err => {
        this.dataLoading = false
      })
    },
    addEditItem (data, type) {
      if(data) {
        this.currentData = data
        this.form = JSON.parse(JSON.stringify(data))
        this.type = 'edit'
        this.title = '编辑'
        if(data.extend && data.extend.value) {
          this.$set(this.form, 'value', data.extend.value)
        }
        if(type == 'children') {
          this.type = 'children'
          this.form = {
            name: '',
            value: '',
            parentId: data.id,
          }
          this.title = '新增子项'
        }
        this.formOption.column.filter(item => {
          if(['value'].indexOf(item.prop) > -1) {
            item.display = this.form.parentId == -1 ? false : true
          }
        })
      }else{
        this.form = {
          name: '',
          value: '',
          parentId: -1
        }
        this.type = 'add'
        this.title = '新增'
        this.formOption.column.filter(item => {
          if([ 'value'].indexOf(item.prop) > -1) {
            item.display = false
          }
        })
      }
      this.dialogVisible = true
    },
    submitHandle (form) {
      this.formOption.submitLoading = true
      if(['add', 'children'].indexOf(this.type) > -1) {
        addTree(form).then(res => {
          if(res.data.code == 0) {
            let str = '新增成功'
            if(this.type == 'children') {
              str = '新增子项成功'
              this.currentData.show = false
            }
            this.$notify({
              title: '提示',
              message: str,
              position: 'bottom-right',
              type: 'success'
            });
            this.formOption.submitLoading = false
            this.handleClose()
            this.getList()
          }
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      }
      if(this.type == 'edit') {
        editTree(form).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '修改成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.currentData.show = false
            this.formOption.submitLoading = false
            this.handleClose()
            this.getList()
          }
        }).catch(e => {
          this.formOption.submitLoading = false
        })
      }
    },
    handleClose () {
      this.form = {}
      this.title = ''
      this.type = ''
      this.dialogVisible = false
      this.$forceUpdate()
    },
    delItem (data) {
      this.$confirm('确定删除此项', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delTree(data.id).then(res => {
          if(res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
          }
        })
      }).catch(e => {})
    },
    exportAll () {
      let str = `/mgr/jvs-design/platform/app/tree/download/template`
      this.$openUrl(str, "_self")
    },
    uploadSuccess (res, file, fileList) {
      if(res.code == 0) {
        this.$notify({
          title: '提示',
          message: '导入成功',
          position: 'bottom-right',
          type: 'success'
        });
        this.getList()
        this.fileList = []
      }else{
        this.$refs.uploadBtn.clearFiles()
        this.$notify({
          title: '提示',
          message: res.msg,
          position: 'bottom-right',
          type: 'error'
        });
      }
    },
    // 导入失败
    errHandle (err, file, fileList) {
      this.$refs.uploadBtn.clearFiles()
      this.$notify({
        title: '提示',
        message: err,
        position: 'bottom-right',
        type: 'error'
      });
    },
  },
  created () {
    this.getList()
  }
}
</script>
<style lang="scss" scoped>
.tree-dictionaries{
  height: 100%;
  background-color: #ffffff;
  overflow: hidden;
  position: relative;
  /deep/.platform-page-header{
    padding: 0;
    padding-bottom: 15px;
    .title{
      display: none;
    }
  }
  .loading-back{
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    box-sizing: border-box;
    background-color: rgba(255, 255, 255, 0.8);
    background-image: url('../../../public/jvs-ui-public/img/loading.gif');
    background-repeat: no-repeat;
    background-position: center;
    background-position: center;
    z-index: 100;
  }
  /deep/.top{
    background: #fff;
    padding: 10px;
    padding-top: 20px;
    overflow: hidden;
    display: flex;
    border-bottom: 1px solid #DCDFE6;
    .jvs-form .el-form-item.form-btn-bar .el-form-item__content{
      text-align: left;
    }
  }
  .tree-dic{
    height: calc(100% - 134px);
    overflow: hidden;
    overflow-y: auto;
    padding: 10px 10px 0 10px;
    .custom-tree-node{
      flex: 1;
      display: flex;
      justify-content: space-between;
      align-items: center;
      height: 100%;
      .tool{
        margin-right: 10px;
        display: none;
      }
    }
    .custom-tree-node:hover{
      .tool{
        display: block;
      }
    }
  }
}
.tool-buttons{
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  margin: 0;
  padding: 0;
  p{
    margin: 0;
    padding: 0;
  }
}
.tool-type-list{
  padding: 0;
  margin: 0;
  li{
    display: flex;
    align-items: center;
    height: 32px;
    line-height: 32px;
    cursor: pointer;
    padding: 6px 24px;
    i{
      margin-right: 10px;
      font-size: 14px!important;
    }
  }
  li:hover{
    background: #F5F7FA;
  }
  li:nth-last-of-type(1) {
    margin-bottom: 0;
  }
}
</style>
<style lang="scss">
.tree-dictionaries{
  .pageheader-top{
    .pageheader-line{
      margin-left: 30px;
    }
  }
}
</style>
