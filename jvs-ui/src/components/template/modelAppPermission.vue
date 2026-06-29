<template>
  <div class="model-app-permission">
    <div v-if="enableVersionFeature === false" class="app-permission-upgrade">
      <div class="up-title">功能升级</div>
      <div class="up-box">
        <div class="up-title-little">多模式轻应用升级</div>
        <div class="up-sec-title">为提供更规范的轻应用配置，可以将应用升级多为模式，不影响其它应用：</div>
        <div class="up-con">
          <div class="up-con-item">
            <div class="up-con-item-title">1、升级后应用支持版本迭代 </div>
          </div>
          <div class="up-con-item">
            <div class="up-con-item-title">2、升级后不同模式的业务数据将分离存储 </div>
          </div>
          <div class="up-con-item">
            <div class="up-con-item-title">3、多模式的轻应用更方便源码定制化接入升级 </div>
          </div>
          <div class="up-con-item">
            <div class="up-con-item-title">4、多模式的轻应用将支持版本手动升级，版本回退等功能 </div>
          </div>
        </div>
        <div class="up-title-little">权限功能升级</div>
        <div class="up-sec-title">为提供更便捷的授权体验，升级后对此应用权限调整，不影响其它应用。在这次更新中，您将享受以下改进：</div>
        <div class="up-con">
          <div class="up-con-item">
            <div class="up-con-item-title">1、简化授权流程： </div>
            <div class="up-con-item-info">不再需要在设计阶段设置功能权限和数据权限，使整体使用更为轻松便捷。</div>
          </div>
          <div class="up-con-item">
            <div class="up-con-item-title">2、历史授权信息清理： </div>
            <div class="up-con-item-info">升级后会清空旧版的权限配置，需要重新配置权限</div>
          </div>
          <div class="up-con-item">
            <div class="up-con-item-title">3、模式差异权限支持： </div>
            <div class="up-con-item-info">新功能允许在不同模式之间差异化地设置权限，以更好地满足不同用户和场景的需求。</div>
          </div>
        </div>
        <div class="up-bottom">
          <el-button type="primary" @click="upgradeHandle">同意升级</el-button>
        </div>
      </div>
    </div>
    <div class="content-box">
      <!-- 权限分类 -->
      <div class="left-type-list">
        <div class="left-type-list-item">
          <div class="group-title">
            <el-button type="primary" size="mini" class="add-button" @click="addGroupItem('DATA')">新增权限组</el-button>
          </div>
          <div class="group-list">
            <div class="el-tree">
              <div v-for="data in groupTypeList" :key="'role-item-'+data.id" v-if="data.permissionType == 'DATA'" :class="{'el-tree-node ': true, 'is-current': (currentId == data.id)}">
                <div class="el-tree-node__content" @click="groupHandleClick(data)">
                  <span class="is-leaf el-tree-node__expand-icon el-icon-caret-right"></span>
                  <span class="customize-tree-node">
                    <span>
                      <i :class="data.icon"></i>
                      <span class="customize-tree-node-label">{{ data.groupName }}</span>
                    </span>
                    <span class="more-icon">
                      <el-popover
                        popper-class="hover-popver-list"
                        placement="right"
                        width="50"
                        v-model="data.moretool"
                        @show="groupHandleClick(data)"
                        trigger="click">
                        <ul class="base-type-list">
                          <li @click.stop="editGroup(data)">
                            <span>修改</span>
                          </li>
                          <li @click.stop="() => delRow(data)">
                            <span style="color: #F56C6C;">删除</span>
                          </li>
                        </ul>
                        <i slot="reference" class="el-icon-more iconhover" @click.stop="moreRole(data)"></i>
                      </el-popover>
                    </span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="rowData" class="right-box">
        <!-- 成员选择 -->
        <div class="top-box">
          <div style="display: flex;align-items: center;justify-content: space-between;">
            <div>
              <el-button size="mini" type="primary" icon="el-icon-plus" class="add-button" @click="handleUserSelectOpen">选择人员/角色/部门</el-button>
            </div>
            <div>
              <el-button type="primary" size="mini" :loading="savedLoading" class="add-button"  @click="saveCurrentPermission">保存</el-button>
            </div>
          </div>
          <div v-if="rowData.member" style="position: relative;padding: 10px 0;">
            <el-tag
              v-for="tag in rowData.member.personnels"
              size="small"
              style="margin-right: 4px;margin-bottom: 4px;"
              :key="tag.id"
              @close="handleDelUser(tag.id, rowData.member.personnels)"
              closable>
              {{getTagName(tag)}}
            </el-tag>
          </div>
        </div>
        <div class="bottom-box">
          <!-- 标识勾选 -->
          <div class="right-permission-list">
            <!-- 数据权限组 -->
            <div v-if="rowData.permissionType == 'DATA'" class="permission-table">
              <div class="table-title">
                <div class="title-item" style="width: 160px;box-sizing: border-box;">模型名称</div>
                <div class="title-item" style="width: calc(100% - 160px);border-right: 0;;box-sizing: border-box;">权限设置</div>
              </div>
              <div class="table-body">
                <div v-if="permissionLoading" class="loading-back"/>
                <div class="table-body-item" v-for="(item, key) in dataPermissionData" :key="key">
                  <div class="category common-border">{{ item.name }}</div>
                  <div class="object" style="width: calc(100% - 160px);">
                    <div class="object-box">
                      <div class="object-item-list">
                        <div class="object-item" v-for="(obj, objKey) in item.dataPermissionList" :key="'data-perm-item-checkbox-item-'+key+'-'+objKey">
                          <div class="object-item-name">
                            <span :class="{'check-label': true, 'isCheck': obj.check}" @click="dataCheckChange(obj)">
                              <span class="check"></span>
                              <span class="label">{{obj.label}}</span>
                              <span v-if="obj.value == 'form_item' && obj.check" style="margin-left:10px;color: #3471FF;" @click.stop="setFieldCondition(item)">设置条件</span>
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <userSelector
      ref="userSelector"
      :selectable="true"
      :userEnable="true"
      :roleEnable="true"
      :dept-enable="true"
      @submit="addCheckUSer">
    </userSelector>
    <!-- 新增  修改 权限组 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      :before-close="dialogClose">
      <div v-if="dialogVisible">
        <jvs-form :option="groupFormOption" :formData="groupFormData" @submit="submitGroup" @cancalClick="dialogClose"></jvs-form>
      </div>
    </el-dialog>
    <!-- 设置条件 -->
    <el-dialog
      title="设置条件"
      width="800px"
      :visible.sync="condiVisible"
      :close-on-click-modal="false"
      :before-close="condiClose">
      <div v-if="condiVisible" class="condi-box">
        <h4>数据会按照如下条件进行筛选</h4>
        <div v-if="currentItem && custFilterList && custFilterList.length > 0">
          <el-row v-for="(condi, index) in condiList" :key="'condi'+index">
            <el-select v-model="condi.key" placeholder="请选择" size="mini" v-if="custFilterList && custFilterList.length > 0" @change="keyChange(index)">
              <el-option
                v-for="it in custFilterList"
                :key="it.fieldDto.fieldKey+'-'+index"
                v-show="needShow(it.fieldDto.fieldKey)"
                :label="it.fieldDto.fieldName"
                :value="it.fieldDto.fieldKey">
                <span style="float: left">{{ it.fieldDto.fieldName }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">{{ it.fieldDto.fieldKey }}</span>
              </el-option>
            </el-select>
            <el-select v-model="condi.operator" placeholder="请选择" size="mini">
              <el-option label="等于" value="eq" v-if="getAttrVal('dataQueryTypes', condi.key).indexOf('eq') > -1"></el-option>
              <el-option label="不等于" value="ne" v-if="getAttrVal('dataQueryTypes', condi.key).indexOf('ne') > -1"></el-option>
              <el-option label="包含" value="in" v-if="getAttrVal('dataQueryTypes', condi.key).indexOf('in') > -1"></el-option>
              <el-option label="不包含" value="notIn" v-if="getAttrVal('dataQueryTypes', condi.key).indexOf('notIn') > -1"></el-option>
            </el-select>
            <el-input v-if="getAttrVal('values', condi.key).length == 0" v-model="condi.value" size="mini"></el-input>
            <el-select v-else v-model="condi.value" placeholder="请选择或输入" size="mini" filterable allow-create multiple collapse-tags style="position: relative;">
              <el-option v-for="(fi, fix) in getAttrVal('values', condi.key)" :key="'fitem-'+fix" :label="fi.name" :value="fi.value"></el-option>
            </el-select>
            <i class="del-button-row el-icon-delete" @click="delItemOfList(condiList, index)"></i>
          </el-row>
          <el-button icon="el-icon-plus" type="text" @click="addCondi">添加</el-button>
        </div>
        <p class="sub-row">
          <el-button type="primary" size="mini" @click="submitCondi">确定</el-button>
          <el-button size="mini" @click="condiClose">取消</el-button>
        </p>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import UserSelector from "@/components/basic-assembly/userSelector";
import { getDataModelDataFilter} from "@/views/page/api/list";
import { getAllModel, getAppPermissionGroupList, addAppPermissionGroup, editAppPermissionGroup, delAppPermissionGroup, saveAppPermissionGroup, getAppPermissionByGroup, upgradeVersion } from './api'
export default {
  name: 'modelAppPermission',
  components: {UserSelector},
  props: {
    appInfo: {
      type: Object
    }
  },
  data () {
    return {
      currentId: '',
      rowData: null,
      groupTypeList: [],
      lastRow: null,
      permissionLoading: false,
      groupShowSort: ['DATA'],
      dialogTitle: '',
      dialogVisible: false,
      groupFormData: null,
      groupFormOption: {
        emptyBtn: false,
        column: [
          {
            label: '权限组名称',
            prop: 'groupName',
            rules: [ { required: true, message: '请输入权限组名称', trigger: 'blur' }, ]
          },
          {
            label: '权限组类型',
            prop: 'permissionType',
            type: 'select',
            disabled: false,
            dicData: [
              { label: '功能权限组', value: 'OPERATION' },
              { label: '数据权限组', value: 'DATA' }
            ],
            rules: [ { required: true, message: '请选择权限组类型', trigger: 'change' }, ]
          }
        ]
      },
      dataPermissionList: [
        {label: '全部数据', value: 'all'},
        {label: '本人提交', value: 'self'},
        {label: '本部门提交', value: 'curr_dept'},
        {label: '本部门及下级部门提交', value: 'curr_dept_tree'},
        {label: '提交\\流转\\抄送包含本人', value: 'flowTaskPersons'},
        {label: '待我审批', value: 'task_pending_approval'},
        {label: '根据表单内容设置过滤条件', value: 'form_item'},
      ],
      condiVisible: false,
      condiList: [],
      custFilterList: [],
      pageList: [],
      dataPermissionData: [],
      savedLoading: false,
      enableVersionFeature: false
    }
  },
  created () {
    this.getPageList()
    this.getAppPermissionGroupList()
    if(this.appInfo.enableVersionFeature) {
      this.enableVersionFeature = true
    }
  },
  methods: {
    getAppPermissionGroupList () {
      if(this.appInfo && this.appInfo.id) {
        getAppPermissionGroupList(this.appInfo.id, 'MODEL').then(res => {
          if(res.data && res.data.code == 0) {
            this.groupTypeList = res.data.data
            if(this.groupTypeList && this.groupTypeList.length > 0) {
              let hasIn = false
              this.groupTypeList.filter(git => {
                if(git.id == this.currentId) {
                  hasIn = true
                }
              })
              if(!hasIn) {
                this.$nextTick(() => {
                  this.groupHandleClick(this.groupTypeList[0])
                })
              }
            }
          }
        })
      }
    },
    getPageList () {
      if(this.appInfo && this.appInfo.id) {
        getAllModel(this.appInfo.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.pageList = res.data.data
            this.formatDataPermissionData()
          }
        })
      }
    },
    formatDataPermissionData () {
      this.dataPermissionData = []
      this.pageList.filter(item => {
        let tp = JSON.parse(JSON.stringify(this.dataPermissionList))
        this.dataPermissionData.push({...item, dataPermissionList: tp})
      })
      this.$forceUpdate()
    },
    // 新增分组
    addGroupItem (type) {
      this.groupFormOption.column.filter(item => {
        if(item.prop == 'permissionType') {
          item.disabled = true
        }
      })
      this.groupFormData = {
        permissionType: type,
        groupType: 'MODEL'
      }
      this.dialogTitle = '新增权限组'
      this.dialogVisible = true
    },
    // 更多
    moreRole (item) {
      if(this.lastRow) {
        this.lastRow.moretool = false
      }
      this.lastRow = item
    },
    // 修改分组
    editGroup (row) {
      this.groupFormOption.column.filter(item => {
        if(item.prop == 'permissionType') {
          item.disabled = true
        }
      })
      this.groupFormData = JSON.parse(JSON.stringify(row))
      this.dialogTitle = '修改权限组'
      this.dialogVisible = true
    },
    // 删除分组
    delRow (row) {
      this.$confirm('将删除此分组, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delAppPermissionGroup(this.appInfo.id, row.id).then(({ data }) => {
          if (data.code===0) {
            this.$notify({
              title: '提示',
              message: '删除成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.getAppPermissionGroupList()
          }
        })
      }).catch(() => {});
    },
    submitGroup () {
      let fun = new Function()
      let obj = {}
      if(this.groupFormData.id) {
        fun = editAppPermissionGroup
        obj = {
          id: this.groupFormData.id,
          permissionType: this.groupFormData.permissionType,
          groupName: this.groupFormData.groupName,
          groupType: 'MODEL'
        }
      }else{
        fun = addAppPermissionGroup
        obj = {
          permissionType: this.groupFormData.permissionType,
          groupName: this.groupFormData.groupName,
          groupType: 'MODEL'
        }
      }
      fun(this.appInfo.id, obj).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: `${this.dialogTitle}成功`,
            position: 'bottom-right',
            type: 'success'
          })
          this.getAppPermissionGroupList()
          this.dialogClose()
        }
      })
    },
    dialogClose () {
      this.groupFormData = null
      this.dialogVisible = false
      this.dialogTitle = ''
    },
    // 选择分组
    groupHandleClick (data, node, dom) {
      if(data) {
        if(this.currentId != data.id) {
          this.rowData = JSON.parse(JSON.stringify(data))
          if(data.permissionType == 'DATA') {
            this.formatDataPermissionData()
          }
          this.currentId = data.id
          getAppPermissionByGroup(this.appInfo.id, this.currentId).then(res => {
            if(res.data && res.data.code == 0) {
              this.showAlreadyCheck(res.data.data)
            }
          })
        }
      }else {
        this.rowData = null
        this.currentId = undefined
      }
    },
    // 打开人员选择组件
    handleUserSelectOpen () {
      this.$refs.userSelector.openDialog(this.rowData.member ? this.rowData.member.personnels : [])
    },
    // 删除标签
    handleDelUser (id, arr) {
      const index = arr.findIndex(item => {
        return item.id === id
      })
      arr.splice(index, 1)
      this.$forceUpdate()
    },
    // 添加人员 提交
    addCheckUSer (checkList) {
      if(!this.rowData.member) {
        this.$set(this.rowData, 'member', {})
      }
      this.$set(this.rowData.member, 'personnels', checkList)
      this.$refs.userSelector.closeDialog()
      this.$forceUpdate()
    },
    // 获取tag中文标识
    getTagName (obj) {
      let header = ''
      switch (obj.type) {
        case 'user':
          break;
        case 'role':
          header = '（角色）';
          break;
        case 'dept':
          header = '（部门）';
          break;
        case 'group':
          header = '（群组）';
          break;
        default: break;
      }
      return header + obj.name
    },
    // 设置条件
    setFieldCondition (item) {
      if(item.conditionList) {
        this.condiList = JSON.parse(JSON.stringify(item.conditionList))
        this.condiList.filter(item => {
          if(typeof item.value == 'string') {
            if(!item.value) {
              item.value = []
            }else{
              item.value = [item.value]
            }
          }
        })
      }
      this.currentItem = JSON.parse(JSON.stringify(item))
      this.getDataModelFilterList(item.id)
    },
    getDataModelFilterList (dataModelId) {
      getDataModelDataFilter(this.appInfo.id, dataModelId).then(res => {
        if(res.data && res.data.code == 0) {
          this.custFilterList = res.data.data
          this.condiVisible = true
        }
      })
    },
    addCondi () {
      this.condiList.push({})
      this.$forceUpdate()
    },
    delItemOfList (list, index) {
      list = list.splice(index, 1)
      this.$forceUpdate()
    },
    submitCondi () {
      for(let i in this.dataPermissionData) {
        if(this.dataPermissionData[i].id == this.currentItem.id) {
          this.$set(this.dataPermissionData[i], 'conditionList', this.condiList)
        }
      }
      this.condiClose()
    },
    // 关闭设置条件
    condiClose () {
      this.condiList = []
      this.condiVisible = false
      this.currentItem = null
    },
    getAttrVal (attr, val) {
      let temp = []
      for(let i in this.custFilterList) {
        if(this.custFilterList[i].fieldDto['fieldKey'] == val) {
          if(this.custFilterList[i][attr]) {
            temp = this.custFilterList[i][attr]
          }
        }
      }
      return temp
    },
    needShow (key) {
      let bool = true
      this.condiList.filter(item => {
        if(item.key && item.key == key) {
          bool = false
        }
      })
      return bool
    },
    keyChange (index) {
      this.$set(this.condiList[index], 'value', [])
    },
    dataCheckChange (obj) {
      obj.check = !obj.check
      this.$forceUpdate()
    },
    saveCurrentPermission () {
      let temp = {
        member: {
          personType: 'custom',
          personnels: this.rowData.member ? this.rowData.member.personnels : []
        },
        permissions: []
      }
      if(this.rowData.permissionType == 'DATA') {
        for(let i in this.dataPermissionData) {
          let tob = {
            designId: this.dataPermissionData[i].id,
            permission: {}
          }
          if(this.dataPermissionData[i].dataPermissionList && this.dataPermissionData[i].dataPermissionList.length > 0) {
            let dtp = []
            this.dataPermissionData[i].dataPermissionList.filter(dit => {
              if(dit.check) {
                dtp.push(dit.value)
              }
            })
            if(dtp.length > 0) {
              this.$set(tob.permission, 'scopeList', dtp)
            }
          }
          if(this.dataPermissionData[i].conditionList && this.dataPermissionData[i].conditionList.length > 0) {
            this.$set(tob.permission, 'conditionList', this.dataPermissionData[i].conditionList)
          }
          if(tob.permission && tob.permission.scopeList && tob.permission.scopeList.length > 0) {
            temp.permissions.push(tob)
          }
        }
      }
      this.savedLoading = true
      saveAppPermissionGroup(this.rowData.jvsAppId, this.rowData.id, temp).then(res => {
        if(res.data && res.data.code == 0) {
          this.savedLoading = false
          this.$notify({
            title: '提示',
            message: `保存成功`,
            position: 'bottom-right',
            type: 'success'
          })
          this.getAppPermissionGroupList()
        }
      }).catch(e => {
        this.savedLoading = false
      })
    },
    // 回显
    showAlreadyCheck (list) {
      if(list && list.length > 0) {
        if(this.rowData.permissionType == 'DATA') {
          for(let i in this.dataPermissionData) {
            list.filter(lit => {
              if(this.dataPermissionData[i].id == lit.designId) {
                if(lit.permission) {
                  if(lit.permission.scopeList && lit.permission.scopeList.length > 0) {
                    for(let j in this.dataPermissionData[i].dataPermissionList) {
                      if(lit.permission.scopeList.indexOf(this.dataPermissionData[i].dataPermissionList[j].value) > -1) {
                        this.$set(this.dataPermissionData[i].dataPermissionList[j], 'check', true)
                      }
                    }
                  }
                  if(lit.permission.conditionList && lit.permission.conditionList.length > 0) {
                    this.$set(this.dataPermissionData[i], 'conditionList', lit.permission.conditionList)
                  }
                }
              }
            })
          }
        }
      }
      this.$forceUpdate()
    },
    upgradeHandle () {
      this.$confirm('是否确认升级?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        upgradeVersion(this.appInfo.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.enableVersionFeature = true
            this.$emit('setAppInfo', 'enableVersionFeature', true)
            this.$notify({
              title: '提示',
              message: `升级成功`,
              position: 'bottom-right',
              type: 'success'
            })
            this.$forceUpdate()
          }
        })
      }).catch(e => {})
    }
  }
}
</script>
<style lang="scss" scoped>
.model-app-permission{
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
  .app-permission-upgrade{
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    box-sizing: border-box;
    background-color: #fff;
    padding: 0 14px;
    box-sizing: border-box;
    z-index: 99;
    .up-title{
      height: 65px;
      font-size: 50px;
      font-family: YouSheBiaoTiHei-Regular, YouSheBiaoTiHei;
      font-weight: 400;
      color: #40486C;
      line-height: 70px;
    }
    .up-title-little{
      margin: 20px 0;
      font-size: 24px;
      font-family: YouSheBiaoTiHei-Regular, YouSheBiaoTiHei;
      font-weight: 400;
      color: #40486C;
    }
    .up-box{
      background: url(../../const/application/apppermissionupgrade.png) no-repeat right top;
      margin-right: 60px;
    }
    .up-sec-title{
      height: 21px;
      font-size: 16px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      color: #40486C;
      line-height: 21px;
    }
    .up-con{
      .up-con-item{
        margin-top: 24px;
        height: 38px;
        font-size: 14px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        color: #6F7588;
        font-weight: 400;
        line-height: 24px;
        .up-con-item-title{
          color: #1E6FFF;
          font-family: Microsoft YaHei-Bold, Microsoft YaHei;
          font-weight: 700;
        }
      }
    }
    .up-bottom{
      margin-top: 40px;
      .el-button--primary{
        width: 152px;
        height: 40px;
        background: #1E6FFF;
        border-radius: 4px;
        box-sizing: border-box;
        font-size: 14px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        color: #FFFFFF;
      }
    }
  }
  .add-button{
    height: 36px;
    background: #1E6FFF;
    border-radius: 4px;
    box-sizing: border-box;
    font-size: 14px;
    font-family: Microsoft YaHei, Microsoft YaHei;
    font-weight: 400;
    color: #FFFFFF;
  }
  .content-box{
    height: 100%;
    overflow: hidden;
    display: flex;
    .left-type-list{
      width: 200px;
      overflow: hidden;
      overflow-y: auto;
      box-sizing: border-box;
      .left-type-list-item{
        margin-top: 10px;
        .group-title{
          display: flex;
          align-items: center;
          margin-bottom: 10px;
          i{
            font-size: 18px;
            color: #AEAEB0;
            margin-left: 10px;
            cursor: pointer;
          }
        }
      }
      .left-type-list-item:nth-of-type(1){
        margin-top: 0;
      }
      h4{
        margin: 0;
        font-size: 16px;
        color: #AEAEB0;
        height: 32px;
        line-height: 32px;
      }
      /deep/.group-list {
        .customize-tree-node {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: space-between;
          font-size: 14px;
          padding-right: 8px;
          .customize-tree-node-label{
            display: block;
            overflow: hidden;
            text-overflow: ellipsis;
            width: 135px;
          }
          .more-icon{
            display: none;
          }
        }
        .customize-tree-node:hover{
          .more-icon{
            display: block;
          }
        }
        .el-tree-node{
          cursor: pointer;
          .el-tree-node__content{
            width: 100%;
          }
        }
        .el-tree-node.is-current{
          >.el-tree-node__content{
            background-color: #F5F7FA;
          }
        }
      }
      .group-list::-webkit-scrollbar{
        display: none;
      }
    }
    .right-box{
      flex: 1;
      display: flex;
      flex-direction: column;
      margin-left: 10px;
      overflow: hidden;
      .bottom-box{
        flex: 1;
        display: flex;
        overflow: hidden;
        .right-permission-list{
          flex: 1;
          overflow: hidden;
          .common-border{
            border-right: 1px solid #eee;
            border-bottom: 1px solid #eee;
          }
          .permission-table{
            border-radius: 4px;
            font-size: 13px;
            border-top: 1px solid #eee;
            height: 100%;
            .el-checkbox__input.is-disabled + span.el-checkbox__label{
              color: #333333!important;
            }
            .table-title{
              width: 100%;
              background-color: #f9fafc;
              display: flex;
              align-items: center;
              border: 1px solid #eee;
              border-top: 0;
              box-sizing: border-box;
              .title-item{
                border-right: 1px solid #eee;
                text-align: left;
                padding: 10px 20px;
                color: #a2a3a5;
              }
            }
            .table-body{
              height: calc(100% - 38px);
              overflow-y: auto;
              position: relative;
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
                z-index: 1;
              }
              .table-body-item{
                display: flex;
                border: 1px solid #eee;
                border-top: 0;
                .category{
                  display: flex;
                  align-items: center;
                  padding: 10px 20px;
                  width: 160px;
                  box-sizing: border-box;
                  border-bottom: 0;
                }
                .object{
                  width: calc(100% - 210px);
                  box-sizing: border-box;
                  .object-box{
                    width: 100%;
                    box-sizing: border-box;
                    border-right: 1px solid #eee;
                    .object-item-list{
                      display: flex;
                      flex-wrap: wrap;
                      border-bottom: 1px solid #eee;
                    }
                    .object-item-title{
                      border-bottom: 0;
                      padding: 10px 20px;
                      font-size: 13px;
                      color: #a2a3a5;
                    }
                    .object-item{
                      display: flex;
                      box-sizing: border-box;
                      min-width: 135px;
                      .object-item-name{
                        display: flex;
                        align-items: center;
                        padding: 10px 20px;
                        box-sizing: border-box;
                      }
                    }
                  }
                  .object-box:nth-last-of-type(1){
                    .object-item-list:nth-last-of-type(1){
                      border-bottom: 0;
                    }
                  } 
                }
                .check-box{
                  width: 50px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  box-sizing: border-box;
                  border-bottom: 1px solid #eee;
                }
                .check-label{
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  cursor: pointer;
                  .check{
                    display: inline-block;
                    position: relative;
                    border: 1px solid #dcdfe6;
                    border-radius: 2px;
                    box-sizing: border-box;
                    width: 14px;
                    height: 14px;
                    background-color: #fff;
                    z-index: 1;
                    transition: border-color .25s cubic-bezier(.71,-.46,.29,1.46),background-color .25s cubic-bezier(.71,-.46,.29,1.46);
                  }
                  .check::after {
                    box-sizing: content-box;
                    content: "";
                    border: 1px solid #fff;
                    border-left: 0;
                    border-top: 0;
                    height: 7px;
                    left: 4px;
                    position: absolute;
                    top: 1px;
                    transform: rotate(45deg) scaleY(0);
                    width: 3px;
                    transition: transform .15s ease-in .05s;
                    transform-origin: center;
                  }
                  .label{
                    margin-left: 10px;
                  }
                }
                .isCheck{
                  .check{
                    background: #3471FF;
                    border-color: #3471FF;
                  }
                  .check::after{
                    transform: rotate(45deg) scaleY(1);
                  }
                }
                .indeterminate{
                  .check{
                    background: #3471FF;
                    border-color: #3471FF;
                  }
                  .check::before{
                    content: "";
                    position: absolute;
                    display: block;
                    background-color: #fff;
                    height: 2px;
                    transform: scale(.5);
                    left: 0;
                    right: 0;
                    top: 5px;
                  }
                }
                .isDisabled{
                  .check{
                    background-color: #edf2fc;
                    border-color: #dcdfe6;
                    cursor: not-allowed;
                  }
                  .check::after{
                    cursor: not-allowed;
                    border-color: #c0c4cc;
                  }
                }
              }
            }
            // .table-body::-webkit-scrollbar {
            //   display: none; /* Chrome Safari */
            //   width: 0;
            //   scrollbar-width: none; /* firefox */
            //   -ms-overflow-style: none; /* IE 10+ */
            // }
          }
        }
      }
    }
  }
}
.base-type-list{
  padding: 0;
  margin: 0;
  li{
    display: flex;
    align-items: center;
    margin: 0;
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
.condi-box{
  h4{
    color: #666;
    padding: 0;
    margin: 0;
    font-weight: normal;
  }
  .el-row{
    display: flex;
    // justify-content: space-between;
    align-items: center;
    margin-top: 10px;
    /deep/ .el-select{
      margin-right: 10px;
      .el-input{
        width: 200px;
      }
    }
    .del-button-row{
      cursor: pointer;
      margin-left: 10px;
    }
  }
  p{
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style>