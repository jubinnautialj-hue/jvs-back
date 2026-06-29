<template>
  <div class="permission-page">
    <div class="permission-body">
      <div class="title">列表功能权限</div>
      <p>功能权限是指在列表页上的对应操作按钮的权限。<span @click="handleMore('permission-help')">了解更多</span></p>
      <p>例如列表页上新增数据的控制、删除数据的控制。</p>
      <div class="permission-header">
        <el-radio-group v-show="false" v-model="roleTypeTemp" @change="handleTypeChange">
          <el-radio :label="true">使用应用权限（所有功能都可使用）</el-radio>
          <el-radio :label="false">自定义权限</el-radio>
        </el-radio-group>
        <jvs-button type="primary" @click="addPermissionGroup" icon="el-icon-plus" :disabled="roleTypeTemp && false">添加权限组</jvs-button>
      </div>
      <div v-if="!roleTypeTemp || true" class="permission-content">
        <div class="loading" v-if="loading"></div>
        <div v-else class="permission-content-item" v-for="(item, key) in permissionList" :key="key">
          <div style="width: 100%; background-color: #DCDFE6;height: 1px;margin-bottom: 16px;"/>
          <el-form label-width="80px" label-position="top">
            <el-radio-group v-model="item.personType" style="margin-right:20px;padding: 10px 0;">
              <el-radio label="all">所有人全部权限</el-radio>
              <el-radio label="custom">自定义</el-radio>
            </el-radio-group>
            <el-button v-if="item.personType == 'custom'" size="mini" type="primary" icon="el-icon-plus" @click="handleUserSelectOpen(item, key)">选择人员/角色/部门</el-button>
            <div v-if="item.personType == 'custom'" style="position: relative; padding: 10px 0">
              <el-tag
                v-for="tag in item.personnels"
                size="small"
                style="margin-right: 4px;margin-bottom: 4px;"
                :key="tag.id"
                @close="handleDelUser(tag.id, item.personnels)"
                closable>
                {{getTagName(tag)}}
              </el-tag>
            </div>
            <div v-if="item.personType == 'all'" style="position: relative;padding: 10px 0;"></div>
            <el-form-item label="操作权限" v-if="item.personType != 'all'">
              <el-checkbox-group v-model="item.operation">
                <el-checkbox v-for="(op, key) in operationList" :key="key" :label="op">{{ op }}</el-checkbox>
                <el-checkbox label="查看" style="margin-left:0;">查看</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="目录权限" v-if="item.personType != 'all'">
              <el-checkbox-group v-model="item.treeOperation">
                <el-checkbox v-for="(op, key) in leftOperationList" :key="key" :label="op">{{ op }}</el-checkbox>
                <el-checkbox label="查看" style="margin-left:0;">查看</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-form>
          <jvs-button v-if="key !== 0" class="del-btn" type="info" @click="deleteGroupItem(item, key)">删除权限组</jvs-button>
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
    <!-- 设置条件 -->
    <el-dialog
      title="设置条件"
      :visible.sync="condiVisible"
      :close-on-click-modal="false"
      :before-close="condiClose">
      <div v-if="condiVisible" class="condi-box">
        <h4>数据会按照如下条件进行筛选</h4>
        <div v-if="currentItem">
          <el-row v-for="(condi, index) in condiList" :key="'condi'+index">
            <el-select v-model="condi.key" placeholder="请选择" size="mini">
              <el-option
                v-for="it in fieldsData"
                :key="it.fieldKey+'-'+index"
                :label="it.fieldName"
                :value="it.fieldKey">
              </el-option>
            </el-select>
            <el-select v-model="condi.operator" placeholder="请选择" size="mini">
              <el-option label="等于" value="eq"></el-option>
              <el-option label="不等于" value="ne"></el-option>
            </el-select>
            <el-input v-model="condi.value" size="mini"></el-input>
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
import {getDataStr} from "../../api/formlist";
export default {
  name: "permission",
  components: {UserSelector},
  props: {
    permissionVisible: {
      type: Boolean,
      default: false
    },
    operationList: {
      type: Array,
      default() {
        return []
      }
    },
    leftOperationList: {
      type: Array,
      default() {
        return []
      }
    },
    roleType: {
      type: Boolean,
      default: true
    },
    role: {
      type: Array,
      default() {
        return []
      }
    },
    dataModelId: {
      type: String
    },
    jvsAppId: {
      type: String
    }
  },
  data () {
    return {
      key: null,
      roleTypeTemp: true,
      loading: true,
      permissionList: [],
      dataPermissionList: [
        {label: '全部数据', value: 'all'},
        {label: '本人提交', value: 'self'},
        {label: '本部门提交', value: 'curr_dept'},
        {label: '下级部门提交', value: 'curr_dept_tree'},
        {label: '根据表单内容设置过滤条件', value: 'form_item'},
      ],
      fieldsData: [], // 数据模型字段列表
      condiVisible: false,
      condiList: [],
      userIds: [],
      options: [],
      userList: [],
    }
  },
  computed: {
  },
  watch: {
    permissionVisible(val) {
      if (val) {
        this.initData()
      }
    }
  },
  created() {
    this.initData()
    this.getDataModelField()
  },
  methods: {
    // 了解更多
    handleMore(str) {
      this.$openUrl('', '_blank', str)
    },
    // 获取当前设置权限信息
    getPermissionData() {
      const arr = [...this.permissionList]
      const params = arr.map(item => {
        return {
          operation: item.operation,
          treeOperation: item.treeOperation,
          personType: item.personType || 'all',
          personnels: item.personnels,
          scopeList: item.scopeList,
          conditionList: item.conditionList
        }
      })
      return {role: params, roleType: this.roleTypeTemp}
    },
    // 获取tag中文标识
    getTagName(obj) {
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
    // 表单数据初始化
    async initData() {
      this.loading = true
      this.roleTypeTemp = this.roleType
      this.permissionList = (this.role && this.role.length > 0) ? this.getRole([...this.role]) : [{
        operation: [],
        treeOperation: [],
        personType: 'all',
        personnels: [],
        scopeList: [],
        conditionList: []
      }]
      this.loading = false
      this.$forceUpdate()
    },
    // 获取权限列表
    getRole(arr) {
      const arrTemp = [...arr]
      arrTemp.forEach((item, key) => {
        if(!item.personType) {
          if(item.personnels && item.personnels.length > 0) {
            this.$set(item, 'personType', 'custom')
          }else{
            this.$set(item, 'personType', 'all')
          }
        }
        item.id = new Date().getTime() + key
        this.$set(item, 'scopeList', item.scopeList ? item.scopeList : [])
        this.$set(item, 'conditionList', item.conditionList ? item.conditionList : [])
        this.$set(item, 'operation', item.operation ? item.operation : [])
        this.$set(item, 'treeOperation', item.treeOperation ? item.treeOperation: [])
      })
      return arrTemp
    },
    // 打开人员选择组件
    handleUserSelectOpen(item, key) {
      this.$refs.userSelector.openDialog(item.personnels)
      this.key = key
    },
    // 删除标签
    handleDelUser(id, arr) {
      const index = arr.findIndex(item => {
        return item.id === id
      })
      arr.splice(index, 1)
      this.$forceUpdate()
    },
    // 添加人员 提交
    addCheckUSer(checkList) {
      this.permissionList[this.key].personnels = checkList
      this.$refs.userSelector.closeDialog()
      this.$forceUpdate()
    },
    // 权限类型 change
    handleTypeChange(e) {
    },
    // 关闭权限设置弹框
    handleClosePermission() {
      this.permissionList = []
      this.$emit('handleClosePermission')
    },
    // 权限设置确认
    handleConfirm() {
      const arr = [...this.permissionList]
      const params = arr.map(item => {
        return {
          operation: item.operation,
          treeOperation: item.treeOperation,
          personnels: item.personnels,
          scopeList: item.scopeList,
          conditionList: item.conditionList
        }
      })
      this.$emit('submitPermission', params, this.roleTypeTemp)
      this.handleClosePermission()
    },
    // 添加权限组
    addPermissionGroup() {
      const obj = {
        id: new Date().getTime(),
        operation: [],
        treeOperation: [],
        personType: 'all',
        personnels: [],
        scopeList: [],
        conditionList: []
      }
      this.permissionList.push(obj)
    },
    // 删除单个权限组
    deleteGroupItem(obj, key) {
      this.permissionList.splice(key, 1)
    },
    getDataModelField () {
      if(this.dataModelId) {
        getDataStr(this.jvsAppId, this.dataModelId).then(res => {
          if(res.data && res.data.code == 0 && res.data.data) {
            let arr = [...res.data.data]
            this.fieldsData = arr.filter(item => {
              return item.fieldName !== ''
            })
          }
        })
      }
    },
    // 设置条件
    setFieldCondition (item) {
      if(item.conditionList) {
        this.condiList = JSON.parse(JSON.stringify(item.conditionList))
      }
      this.currentItem = item
      this.condiVisible = true
    },
    // 关闭设置条件
    condiClose () {
      this.condiList = []
      this.condiVisible = false
      this.currentItem = null
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
      for(let i in this.permissionList) {
        if(this.permissionList[i].id == this.currentItem.id) {
          this.$set(this.permissionList[i], 'conditionList', this.condiList)
        }
      }
      this.condiClose()
    },
  }
}
</script>

<style lang="scss" scoped>
.permission-page{
  .permission-body{
    padding: 20px 30px;
    background-color: #ffffff;
    border-radius: 6px;
    .title {
      font-weight: bold;
      font-size: 16px;
      margin-bottom: 16px;
    }
    p {
      color: #b3b3b3;
      span {
        color: #3471ff;
        cursor: pointer;
      }
    }
    .permission-header{
      margin-top: 32px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .permission-content{
      margin-top: 16px;
      max-height: calc(100vh - 200px);
      overflow-y: auto;
      position: relative;
      padding-bottom: 30px;
      box-sizing: border-box;
      .loading{
        z-index: 1000;
        position: absolute;
        width: 100%;
        height: 100%;
        background-image: url('../../../../../public/jvs-ui-public/img/loading.gif');
        background-repeat: no-repeat;
        background-position: center;
        //background-size: 300px 240px;
      }
      .permission-content-item{
        //min-height: 240px;
        position: relative;
        .del-btn{
          position: absolute;
          right: 20px;
          top: 16px;
          cursor: pointer;
        }
        .user-select{

        }
        /deep/.el-checkbox{
          margin-left: 0;
        }
      }
    }
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
    justify-content: space-between;
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
