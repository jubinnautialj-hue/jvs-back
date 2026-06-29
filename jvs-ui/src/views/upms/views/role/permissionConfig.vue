<template>
  <el-dialog
    title="添加权限配置"
    width="1000px"
    :visible.sync="dialogVisible"
    :before-close="handleClose">
    <div class="permission-config-box">
      <div class="left-tree">
        <el-tree
          v-if="treeData && treeData.length > 0"
          ref="tree"
          class="permission-tree"
          :data="treeData"
          :props="defaultProps"
          node-key="id"
          :highlight-current="true"
          :expand-on-click-node="false"
          :default-expand-all="false"
          :default-expanded-keys="[treeData[0].id]"
          @node-expand="nodeExpand"
          @node-collapse="nodeCollapse"
          @node-click="handleNodeClick"
        >
          <div class="custom-tree-node" slot-scope="{ node, data }">
            <span>{{ data.name }}</span>
            <span v-if="data.extend && data.extend.type !== 'button'">
              <el-button
                v-if="data.extend.type && data.extend.type !== 'button'"
                type="text"
                size="mini"
                @click="handleEditNode(data)">
                编辑
              </el-button>
              <el-button
                type="text"
                size="mini"
                @click="handleAdd(data)">
                {{ data.extend.type == 'menu' ? '资源管理' : '添加下级' }}
              </el-button>
              <el-button
                v-if="data.extend.type && data.extend.type !== 'button'"
                type="text"
                size="mini"
                @click="handleDelNode(data)">
                <span style="color: #F56C6C!important;">删除</span>
              </el-button>
            </span>
          </div>
        </el-tree>
      </div>
      <div class="right-form">
<!--        <div style="margin-bottom: 20px;">{{ formType }}</div>-->
        <div style="margin-bottom: 20px;" v-if="formType">{{ currentNode.name }}（{{ formType }}）</div>
        <jvs-form v-if="option.column.length > 0" ref="permissionForm" :option="option" :formData="formData" @submit="handleSubmit" @cancalClick="handleClose"></jvs-form>
        <jvs-table v-if="formType === '资源管理'" class="btn-permission-table" :option="tableOption" :data="tableData">
          <template slot="headerTop">
            <el-button size="mini" style="margin-top: 16px" @click="handleAddRow">新增一行</el-button>
          </template>
          <template slot="name" scope="scope">
            <el-input size="mini" v-model="scope.row.name"></el-input>
          </template>
          <template slot="permission" scope="scope">
            <el-input size="mini" v-model="scope.row.permission"></el-input>
          </template>
          <template slot="api" scope="scope">
            <el-select style="width: 100%" v-model="scope.row.api" multiple filterable allow-create collapse-tags placeholder="请选择接口" size="mini" class="api-select">
              <el-option
                v-for="(item, key) in apiList"
                :key="key"
                :label="item"
                :value="item">
              </el-option>
            </el-select>
<!--             <el-input size="mini" v-model="scope.row.api"></el-input>-->
          </template>
          <template slot="menu" scope="scope">
            <el-button size="mini" type="text" @click="handleDel(scope.row)">删除</el-button>
          </template>
        </jvs-table>
        <el-button v-if="formType === '资源管理'" size="mini" style="margin-top: 20px;" type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </div>
    <div slot="footer" class="dialog-footer">
<!--      <el-button size="mini" type="primary" @click="handleSubmit">确 定</el-button>-->
<!--      <el-button size="mini" @click="handleClose">取 消</el-button>-->
    </div>
  </el-dialog>
</template>

<script>
import {deleteMenu, editButton, editMenu} from "@/views/upms/views/role/api";

export default {
  name: "permissionConfig",
  props: {
    terminalList: {
      type: Array,
      default() {
        return []
      }
    }
  },
  computed: {
    // treeData() {
    //   return this.terminalList.length > 0 ? this.terminalList[0].children : []
    // }
  },
  data () {
    return {
      treeData: [],
      apiList: [],
      tableOption: {
        viewBtn: false,
        addBtn: false,
        editBtn: false,
        delBtn: false,
        showOverflow: false,
        // align: 'center',
        // menuAlign: 'center',
        menuWidth: 100,
        column: [
          {
            label: "资源名称",
            prop: "name",
            slot: true
          },
          {
            label: "资源标识",
            prop: "permission",
            slot: true
          },
          {
            label: "接口地址",
            prop: "api",
            slot: true,
            width: 200
          }
        ]
      },
      tableData: [],
      isEdit: false, // 是否编辑操作
      option: {
        emptyBtn: false,
        // submitBtn: false,
        cancal: false,
        submitBtnText: '确定',
        labelWidth: '100px',
        submitLoading: false,
        formAlign: 'top',
        column: []
      },
      formData: {},
      dialogVisible: false,
      currentNode: {},
      defaultProps: {
        children: 'children',
        label: 'name'
      },
      formType: '',
      permissionType: '',
      defaultColumn: [
        {
          label: '名称',
          prop: 'name',
        },
        {
          label: '标识',
          prop: 'permission',
        },
        {
          label: '排序',
          prop: 'sort',
          type: 'inputNumber',
        }
      ],
    }
  },
  methods: {
    // 删除一行数据
    handleDel(row) {
      const index = this.tableData.findIndex(item => {
        if (item.id) {
          return item.id === row.id
        } else  {
          return item.no === row.no
        }
      })
      this.tableData.splice(index, 1)
    },
    // 新增一行数据
    handleAddRow() {
      const obj = {
        no: new Date().getTime(),
        name: '',
        permission: '',
        api: [],
        parentId: this.currentNode.id,
        type: 'button'
      }
      this.tableData.push(obj)
    },
    initTreeData(list) {
      this.treeData = list.length > 0 ? list[0].children : []
    },
    openDialog() {
      this.initTreeData(this.terminalList)
      this.dialogVisible = true
    },
    handleClose() {
      this.dialogVisible = false
      this.$set(this.option, 'column', [])
      this.$set(this, 'formData', {})
      this.formType = ''
      this.tableData = []
      this.$set(this, 'treeData', [])
    },
    handleSubmit(form) {
      // console.log(this.currentNode)
      const params = {
        parentId: this.currentNode.id,
        name: this.formData.name,
        sort: this.formData.sort,
        permission: this.formData.permission,
        type: this.permissionType
      }
      // console.log(params)
      if (this.permissionType !== 'button') {
        if (this.isEdit) {
          params.id = this.currentNode.id
        }
        editMenu(params).then(res => {
          if (res.data && res.data.code == 0) {
            // this.$message.success('操作成功')
            this.$notify({
              title: '提示',
              message: '操作成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.$emit('getTreeData')
          }
        })
      } else {
        editButton(this.tableData, this.currentNode.id).then(res => {
          if (res.data && res.data.code == 0) {
            // this.$message.success('操作成功')
            this.$notify({
              title: '提示',
              message: '操作成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.$emit('getTreeData')
          }
        })
        console.log(this.tableData)
      }
      this.formType = ''
      this.$set(this.option, 'column', [])
      this.$set(this, 'formData', {})
      // this.handleClose()
    },
    // 删除节点
    handleDelNode(data) {
      this.$confirm('确定删除此项', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteMenu(data.id).then(res => {
          if (res.data && res.data.code == 0) {
            // this.$message.success('操作成功')
            this.$notify({
              title: '提示',
              message: '操作成功',
              position: 'bottom-right',
              type: 'success'
            });
            this.$emit('getTreeData')
          }
        })
      }).catch(e => {})
    },
    // 编辑节点
    handleEditNode(data) {
      this.isEdit = true
      // this.option.column = []
      this.$set(this.option, 'column', [])
      this.$set(this, 'formData', {})
      this.$forceUpdate()
      // this.formData = {}
      this.$nextTick(() => {
        this.currentNode = JSON.parse(JSON.stringify(data))
        this.formType = '编辑';
        this.permissionType = data.extend.type
        this.option.column = this.defaultColumn
        this.formData = JSON.parse(JSON.stringify(data.extend))
      })
    },
    // 添加下级
    handleAdd(data) {
      this.tableData = []
      this.isEdit = false
      // this.option.column = []
      this.$set(this.option, 'column', [])
      this.$set(this, 'formData', {})
      this.$forceUpdate()
      this.$nextTick(() => {
        this.currentNode = JSON.parse(JSON.stringify(data))
        console.log(data.extend.type)
        // this.permissionType = data.extend.layer && data.extend.layer < 3 ? 'menu' : 'button'
        switch (data.extend.type) {
          case '':
            this.formType = '添加目录';
            this.permissionType = 'group'
            this.option.column = this.defaultColumn
            break;
          case 'group':
            this.permissionType = 'menu'
            this.formType = '添加页面';
            this.option.column = this.defaultColumn
            break;
          case 'menu':
            this.permissionType = 'button'
            this.formType = '资源管理';
            break;
          default:
            break;
        }
        if (data.children && data.children.length > 0) {
          this.tableData = data.children.map(item => {
            return {
              id: item.extend.id,
              permission: item.extend.permission,
              api: item.extend.api,
              name: item.extend.name,
              parentId: item.parentId,
              type: item.extend.type
            }
          })
        }
      })
      // this.$forceUpdate()
    },
    handleNodeClick(data) {
      console.log(data)
    },
    nodeExpand(){
      this.formType = ''
      this.$set(this.option, 'column', [])
      this.$set(this, 'formData', {})
    },
    nodeCollapse(){
      this.formType = ''
      this.$set(this.option, 'column', [])
      this.$set(this, 'formData', {})
    },
  }
}
</script>

<style lang="scss" scoped>
.tool-type-list{
  padding: 0;
  margin: 0;
  li{
    display: flex;
    align-items: center;
    height: 32px;
    line-height: 32px;
    cursor: pointer;
    //padding-left: 20px;
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
.permission-config-box{
  display: flex;
  .left-tree{
    height: 500px;
    overflow-y: auto;
    width: 360px;
    margin-right: 20px;
    .permission-tree{
      .custom-tree-node{
        width: 100%;
        padding-right: 20px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .tool{
          display: none;
        }
        &:hover{
          .tool{
            display: block;
          }
        }
      }
    }
  }
  .right-form{
    //width: calc(100% - 260px);
    width: 540px;
    height: 500px;
    /deep/.jvs-form .el-form-item.form-btn-bar .el-form-item__content{
      text-align: left;
    }
    .btn-permission-table{
      /deep/.el-table .el-table__body-wrapper {
        //height: calc(100% - 220px)!important;
        height: 280px!important;
        //overflow-y: auto;
      }
      /deep/.table-top{
        display: none;
      }
    }
    /deep/.api-select{
      width: 100%;
      .el-input{
        .el-input__inner{
          display: flex;
        }
      }
      .el-select__tags{
        .el-tag:nth-of-type(1){
          max-width: 160px;
          position: relative;
          .el-select__tags-text{
            max-width: 60px;
            display: inline-block;
            overflow: hidden;
            white-space: pre;
            text-overflow: ellipsis;
          }
        }
      }
    }
  }
}
.dialog-footer{
  text-align: center;
}
</style>
