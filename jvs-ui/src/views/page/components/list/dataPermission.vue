<template>
  <div class="permission-page">
    <div class="permission-body">
      <div class="title">
        <span>列表数据权限</span>
        <el-switch v-model="stepDataPermission" size="mini" style="margin-left: 10px;"></el-switch>
      </div>
      <p>默认有权限的用户可以看所有数据。</p>
      <p>可根据实际场景调整数据权限功能。</p>
      <div style="display: flex;align-items: center;justify-content: space-between;">
        <el-radio-group v-model="dataRoleType" v-show="stepDataPermission">
          <el-radio label="datamodel">模型数据权限</el-radio>
          <el-radio label="custom">自定义数据权限</el-radio>
        </el-radio-group>
        <jvs-button type="primary" @click="addPermissionGroup" icon="el-icon-plus" v-show="stepDataPermission && dataRoleType == 'custom'">添加权限组</jvs-button>
      </div>
      <div class="permission-content" v-show="stepDataPermission && dataRoleType == 'custom'">
        <div class="loading" v-if="loading"></div>
        <div v-else class="permission-content-item" v-for="(item, key) in permissionList" :key="key">
          <div style="width: 100%; background-color: #DCDFE6;height: 1px;margin-bottom: 16px;"/>
          <el-form label-width="80px" label-position="top">
            <el-button size="mini" type="primary" icon="el-icon-plus" @click="handleUserSelectOpen(item, key)">选择人员/角色/部门</el-button>
            <div style="position: relative; padding: 10px 0">
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
              <el-form-item label="数据权限" class="cust-radio-line">
                <el-checkbox-group v-model="item.scopeList">
                  <el-checkbox v-for="(op, index) in dataPermissionList" :key="op.value+index" :label="op.value">{{op.label}}</el-checkbox>
                  <el-button v-if="item.scopeList.indexOf('form_item') > -1" size="mini" type="text" style="margin-left:10px;" @click="setFieldCondition(item)">设置条件</el-button>
                </el-checkbox-group>
              </el-form-item>
          </el-form>
          <!--          <i v-if="key !== 0" class="el-icon-delete del-btn" @click="deleteGroupItem(item)"></i>-->
          <jvs-button class="del-btn" type="info" @click="deleteGroupItem(item, key)">删除权限组</jvs-button>
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
            <el-select v-else v-model="condi.value" placeholder="请选择或输入" size="mini" filterable allow-create multiple collapse-tags>
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
export default {
  name: "dataPermission",
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
    },
    stepDataPermissionBool: {
      type: Boolean
    },
    dataRoleTypeString: {
      type: String
    },
    dataRoleList: {
      type: Array
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
        {label: '提交\\流转\\抄送包含本人', value: 'flowTaskPersons'},
        {label: '根据表单内容设置过滤条件', value: 'form_item'},
      ],
      condiVisible: false,
      condiList: [],
      userIds: [],
      options: [],
      userList: [],
      custFilterList: [],
      stepDataPermission: false,
      dataRoleType: 'datamodel'
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
    // this.getDataModelField()
    if(this.stepDataPermissionBool) {
      this.$set(this, 'stepDataPermission', this.stepDataPermissionBool)
    }
    if(this.dataRoleTypeString) {
      this.$set(this, 'dataRoleType', this.dataRoleTypeString)
    }
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
          personnels: item.personnels,
          scopeList: item.scopeList,
          conditionList: item.conditionList
        }
      })
      return params
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
      if(this.dataRoleList && this.dataRoleList.length > 0) {
        this.permissionList = JSON.parse(JSON.stringify(this.dataRoleList))
      }
      getDataModelDataFilter(this.jvsAppId, this.dataModelId).then(res => {
        if(res.data && res.data.code == 0) {
          this.custFilterList = res.data.data
          // console.log(res.data.data)
        }
      })
      this.loading = false
      this.$forceUpdate()
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
    },
    // 添加人员 提交
    addCheckUSer(checkList) {
      this.permissionList[this.key].personnels = checkList
      this.$refs.userSelector.closeDialog()
    },
    // 权限类型 change
    handleTypeChange(e) {
    },
    // 添加权限组
    addPermissionGroup() {
      const obj = {
        id: new Date().getTime(),
        operation: [],
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
    }
  }
}
</script>

<style lang="scss" scoped>
.permission-page{
  margin-top: 16px;
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
    /deep/.el-select__tags{
      height: 24px;
    }
  }
  p{
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
.cust-radio-line{
  /deep/.el-checkbox-group{
    .el-checkbox:nth-last-of-type(1){
      margin-left: 0;
    }
  }
}
</style>
