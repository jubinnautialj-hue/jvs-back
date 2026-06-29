<template>
  <div :class="{ 'table-form': true }">
    <jvs-table
      v-if="readyShow"
      :pageheadertitle="''"
      :option="options"
      :data="tableData"
      :index="true"
      :editable="true"
      @on-load="getList"
    >
      <template v-for="(titem, tindex) in options.tableColumn" :slot="titem.prop" slot-scope="scope">
        <div :key="item.prop + scope.index + '-' + titem.prop + 'node' + tindex">
          <div v-if=" titem.text && titem.text.label && scope.row[titem.prop] == titem.text.value" :key="titem.prop + 'text'">
            <span>{{ titem.text.label }}</span>
          </div>
          <div v-if="titem.needSlot !== true && !( titem.text && titem.text.label && scope.row[titem.prop] == titem.text.value )" :key="item.prop + scope.index + '-' + titem.prop + 'item'">
            <el-form :model="scope.row" :ref="(formRef || 'ruleForm')+'_'+scope.index" class="demo-dynamic" size="mini">
              <el-form-item label="" :prop="titem.prop" :rules="titem.rules" style="margin-bottom: 0">
                <!-- 名称 -->
                <tableFormItem
                  v-if="titem.prop == 'name'"
                  :tableRowAIndex="scope.index"
                  :style="'justify-content:' +(options.align == 'center' ? 'center' : 'flex-start')"
                  :form="scope.row"
                  :item="{...titem}"
                  :roleOption="roleOption"
                  :userList="userList"
                  :departmentList="departmentList"
                  :postList="postList"
                  :designId="designId"
                  :isView="isView"
                  :execsList="execsList"
                  :jvsAppId="jvsAppId"
                  :originForm="forms"
                  :dataTriggerFresh="dataTriggerFresh"
                  @formChange="formChange"
                  @reInitData="reInitData"
                ></tableFormItem>
              </el-form-item>
            </el-form>
            <!-- 审批人 -->
            <el-form :model="customForm" :ref="(formRef || 'ruleForm')+'_'+scope.index" class="demo-dynamic" size="mini">
              <el-form-item label="" :prop="titem.prop" :rules="titem.rules" style="margin-bottom: 0">
                <el-select
                  v-if="titem.prop == 'assignType'"
                  v-model="scope.row.props.type"
                  :placeholder="titem.placeholder || titem.label"
                  :multiple="titem.multiple"
                  :collapse-tags="!titem.collapsetags"
                  :disabled="titem.disabled"
                  :clearable="false"
                  :filterable="titem.filterable"
                  :allow-create="titem.allowcreate"
                  :size="$store.state.params.form.size || titem.size || 'mini'"
                  @change="assignChange(scope.row.props.type, scope.row, scope.index, titem)"
                >
                  <el-option
                    v-for="sitem in titem.dicData"
                    :key=" sitem[(titem.props && titem.props.value) || 'value'] + titem.prop + Math.random() + Date.now().toString()"
                    :label="sitem[(titem.props && titem.props.label) || 'label']"
                    :value="sitem[(titem.props && titem.props.value) || 'value']"
                  >
                    <span style="float: left">{{ sitem[(titem.props && titem.props.label) || "label"] }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-form>
            <!-- 人员选择 -->
            <el-form v-if="titem.prop == 'userSelect'" :model="customForm" :ref="(formRef || 'ruleForm')+'_'+scope.index" class="demo-dynamic" size="mini">
              <el-form-item label="" :prop="titem.prop" :rules="titem.rules" style="margin-bottom: 0">
                <tableFormItem
                  :tableRowAIndex="scope.index"
                  :style="'justify-content:' +(options.align == 'center' ? 'center' : 'flex-start')"
                  :form="scope.row.props.targetObj"
                  :item="{...titem, prop: 'personnels', type: getUserChooseType(scope.row.props.type), props: titem.props ? titem.props : ({'label': (titem.prop+'_1'), 'value': titem.prop})}"
                  :roleOption="roleOption"
                  :userList="userList"
                  :departmentList="departmentList"
                  :postList="postList"
                  :designId="designId"
                  :isView="isView"
                  :execsList="execsList"
                  :jvsAppId="jvsAppId"
                  :originForm="forms"
                  :dataTriggerFresh="dataTriggerFresh"
                  @formChange="userChange"
                  @reInitData="reInitData"
                ></tableFormItem>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </template>
    </jvs-table>
  </div>
</template>
<script>
import { getDeptList, getRoleList, getPostList } from "../api";
import { getUserInfo } from "@/api/admin/user";
import { approvalType } from '@/views/flowable/views/design/common/enumConst'
import userForm from '@/components/basic-assembly/userForm'
export default {
  name: "table-Form",
  components: {
    // 异步import，formitem引用了tableForm，嵌套时异步引用
    tableFormItem: () => import("@/components/basic-assembly/formitem"),
    userForm
  },
  props: {
    formRef: {
      type: String,
      default: "ruleForm",
    },
    item: {
      type: Object,
    },
    option: {
      type: Object,
    },
    data: {
      type: Array,
      default: () => {
        return [];
      },
    },
    originOption: {
      type: Object,
    },
    defalutSet: {
      type: Object,
    },
    rowData: {
      type: Object,
    },
    // 用户列表
    userList: {
      type: Array,
      default: () => {
        return [];
      },
    },
    // 角色列表
    roleOption: {
      type: Array,
      default: () => {
        return [];
      },
    },
    // 部门列表
    departmentList: {
      type: Array,
      default: () => {
        return [];
      },
    },
    // 岗位列表
    postList: {
      type: Array,
      default: () => {
        return [];
      },
    },
    resetRadom: {
      type: Number,
    },
    designId: {
      type: String,
    },
    forms: {
      type: Object,
    },
    dataModelId: {
      type: String,
    },
    changeRandom: {
      type: Number,
    },
    changeDomItem: {
      type: Object,
    },
    isView: {
      type: Boolean,
    },
    execsList: {
      type: Array,
    },
    jvsAppId: {
      type: String,
    },
    dataTriggerFresh: {
      type: Number,
    },
  },
  computed: {
    options() {
      let temp = JSON.parse(JSON.stringify(this.option));
      if(temp.column && !temp.tableColumn){
        temp.tableColumn = temp.column;
      }
      if(this.item.editable){
        for (let i in temp.tableColumn){
          temp.tableColumn[i].slot = true;
        }
      }
      if(!temp.column) {
        temp.column = temp.tableColumn;
      }
      if (temp.menuAlign != "left") {
        temp.menuAlign = "center";
      }
      temp.indexLabel = "序号";
      temp.border = false;
      temp.delBtn = true;
      return temp;
    },
  },
  created() {
    if (this.$store.getters && this.$store.getters.userInfo) {
      this.userInfo = JSON.parse(JSON.stringify(this.$store.getters.userInfo));
    } else {
      this.getUserInfo();
    }
    this.init();
  },
  data() {
    return {
      title: "",
      tableData: [],
      userInfo: {},
      openType: "", // 弹框类型
      rowIndex: -1, // 行数据index
      loadTimes: -1, // 加载次数
      initData: [],
      readyShow: false,
      customForm: {}
    };
  },
  methods: {
    getUserInfo() {
      getUserInfo().then((res) => {
        if (res.data.code == 0) {
          this.userInfo = res.data.data;
        }
      });
    },
    async init() {
      let deptBool = false;
      let roleBool = false;
      let postBool = false;
      for(let i in this.options.tableColumn){
        if(this.options.tableColumn[i].type == "department"){
          deptBool = true;
        }
        if(this.options.tableColumn[i].type == "role"){
          roleBool = true;
        }
        if(this.options.tableColumn[i].type == "post"){
          postBool = true;
        }
        // 配置字典
        if (this.options.tableColumn[i].dicData) {
          let temp = [];
          let bool = false;
          for (let j in this.options.tableColumn[i].dicData) {
            if (typeof this.options.tableColumn[i].dicData[j] == "string") {
              bool = true;
              temp.push({
                label: this.options.tableColumn[i].dicData[j],
                value: this.options.tableColumn[i].dicData[j],
              });
            }
          }
          if (bool) {
            this.options.tableColumn[i].dicData = temp;
          }
        }
      }
      // 部门回显
      if (deptBool) {
        await getDeptList().then((res) => {
          if (res.data.code == 0) {
            for (let i in this.options.tableColumn) {
              if (this.options.tableColumn[i].type == "department") {
                this.options.tableColumn[i].dicData = res.data.data;
              }
            }
          }
        });
      }
      // 角色回显
      if (roleBool) {
        await getRoleList().then((res) => {
          if (res.data.code == 0) {
            for (let i in this.options.tableColumn) {
              if (this.options.tableColumn[i].type == "role") {
                this.options.tableColumn[i].dicData = res.data.data;
              }
            }
          }
        });
      }
      // 岗位回显
      if (postBool) {
        await getPostList().then((res) => {
          if (res.data.code == 0) {
            for (let i in this.options.tableColumn) {
              if (this.options.tableColumn[i].type == "post") {
                this.options.tableColumn[i].dicData = res.data.data;
              }
            }
          }
        });
      }
      this.readyShow = true;
    },
    // 表格数据api来源
    getList() {
      if (this.loadTimes == -1 && this.data) {
        this.tableData = JSON.parse(JSON.stringify(this.data));
        this.initData = JSON.parse(JSON.stringify(this.data));
        this.$forceUpdate();
      }
      this.validateForm()
      this.loadTimes++;
    },
    reset() {
      this.$set(this, "tableData", this.data);
    },
    reInitData(prop, parentKey, index, tableType) {
      this.$emit("reInitData", prop, parentKey, index, tableType);
    },
    validateForm () {
      this.tableData.filter((row, index) => {
        if(this.$refs[(this.formRef || "ruleForm")+'_'+index] && this.$refs[(this.formRef || "ruleForm")+'_'+index].length > 0){
          for(let r in this.$refs[(this.formRef || "ruleForm")+'_'+index]){
            this.$refs[(this.formRef || "ruleForm")+'_'+index][r].validate((valid, props) => {
              if(valid) {
                if(!this.tableData[index].props.type) {
                  this.$refs[(this.formRef || "ruleForm")+'_'+index][r].validateField('assignType')
                }
                if(!this.tableData[index].props.targetObj.personnels || this.tableData[r].props.targetObj.personnels.length == 0) {
                  this.$refs[(this.formRef || "ruleForm")+'_'+index][r].validateField('userSelect')
                }
              }else{
                let keys = Object.keys(props)
                if(keys.length > 0) {
                  if(keys[0] == 'assignType') {
                    if(row.props.type) {
                      this.$refs[(this.formRef || "ruleForm")+'_'+index][r].clearValidate(['assignType'])
                      this.$forceUpdate()
                    }
                  }
                  if(keys[0] == 'userSelect') {
                    if(row.props.targetObj.personnels && row.props.targetObj.personnels.length > 0) {
                      this.$refs[(this.formRef || "ruleForm")+'_'+index][r].clearValidate(['userSelect'])
                      this.$forceUpdate()
                    }
                  }
                }
              }
            })
          }
        }
      })
    },
    formChange(form, item) {
      this.$emit("setTable", this.tableData)
      this.$emit("formChange", form, item)
      this.validateForm()
    },
    assignChange (val, row, index, item) {
      this.$set(this.tableData[index].props, 'type', val)
      this.$set(this.tableData[index].props.targetObj, 'personnels', [])
      this.$set(this.customForm, 'assignType', val)
      this.$set(this.customForm, 'userSelect', [])
    },
    userChange (form, item) {
      this.$emit("setTable", this.tableData)
      this.$emit("formChange", form, item)
      this.$set(this.customForm, 'userSelect', form[item.prop])
    },
    getUserChooseType (val) {
      let type = 'user'
      switch(val) {
        case approvalType.ASSIGN_USER: type = 'user';break;
        case approvalType.ROLE: type = 'role';break;
        case approvalType.JOB: type = 'job';break;
        default: type = 'user';break;
      }
      return type
    }
  },
  watch: {
    resetRadom: {
      handler(newVal, oldVal) {
        if (newVal > -1) {
          this.reset();
        }
      },
    },
    data: {
      handler(newVal, oldVal) {
        if(this.item.editable){
          this.tableData = this.data
          this.validateForm()
          this.$forceUpdate()
        }
      },
    }
  },
};
</script>
