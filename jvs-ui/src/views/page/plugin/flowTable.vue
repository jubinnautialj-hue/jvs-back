<template>
  <div style="cursor: pointer">
    <el-form
      :model="dynamicValidateForm"
      ref="dynamicValidateForm"
      class="demo-dynamic"
      style="width: 100%"
      :label-position="formsetting.labelposition"
      :label-width="formsetting.labelwidth + 'px'"
      :size="formsetting.formsize || 'mini'"
    >
      <el-table
        empty-text="流程设计"
        :key="tablekey"
        :border="data.border"
        :data="dynamicValidateForm.domains"
        class="tb-edit design-table-form"
        style="width: 100%"
        highlight-current-row
      >
        <el-table-column
          :align="item.align || data.align || 'center'"
          type="index"
          width="50"
          v-if="false"
        ></el-table-column>
        <el-table-column
          :align="item.align || data.align || 'center'"
          v-for="(item, index) in data.tableColumn"
          :label="item.label + (item.rules.length > 0 ? '(必填)' : '(可选)')"
          :key="'column' + index"
          :width="item.width"
        >
          <template slot="header">
            <div class="headeritem">
              <span style="color: #f56c6c" v-if="item.rules.length > 0 && item.rules[0].required">*</span>
              <span>{{ item.label }}</span>
              <div class="handle-btn">
                <span class="type-name">{{ item.name }}</span>
                <i class="el-icon-setting icons" @click.stop="set(item)"></i>
              </div>
            </div>
          </template>
          <template scope="scope">
            <div>
              <el-form-item
                :key="scope.$index"
                :rules="item.rules"
                :prop="'domains.' + scope.$index + '.' + item.prop"
                class="tableform-no-label-item"
              >
                <FormItem
                  v-if="item.prop == 'name'"
                  :item="item"
                  :isView="true"
                  :designId="designId"
                  :form="scope.row"
                />
                <!-- 审批人 -->
                <el-select
                  v-if="item.prop == 'assignType'"
                  v-model="scope.row.props.type"
                  :placeholder="item.placeholder || item.label"
                  :multiple="item.multiple"
                  :collapse-tags="!item.collapsetags"
                  :disabled="item.disabled"
                  :clearable="false"
                  :filterable="item.filterable"
                  :allow-create="item.allowcreate"
                  :size="$store.state.params.form.size || item.size || 'mini'"
                  @change="assignChange(scope.row.props.type, scope.row, scope.$index)"
                >
                  <el-option
                    v-for="sitem in item.dicData"
                    :key=" sitem[(item.props && item.props.value) || 'value'] + item.prop + Math.random() + Date.now().toString()"
                    :label="sitem[(item.props && item.props.label) || 'label']"
                    :value="sitem[(item.props && item.props.value) || 'value']"
                  >
                    <span style="float: left">{{ sitem[(item.props && item.props.label) || "label"] }}</span>
                  </el-option>
                </el-select>
                <!-- 人员选择 -->
                <userForm
                  v-if="item.prop == 'userSelect'"
                  :form="scope.row.props.targetObj"
                  prop="personnels"
                  :selectable="item.multiple"
                  :defaultValue="item.defaultValue"
                  :enableinput="item.allowinput"
                  :disabled="item.disabled"
                  :deptable="item.deptable"
                  :infoable="item.infoable"
                  :props="item.props ? item.props : {'label': (item.prop+'_1'), 'value': item.prop}"
                  :type="getUserChooseType(scope.row.props.type)"/>
              </el-form-item>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          :align="data.align || 'center'"
          label="操作"
          :fixed="data.menuFix"
          v-if="data.editBtn || data.delBtn"
        >
          <template scope="scope">
            <div style="width: 100%; justify-content: center">
              <jvs-button v-if="data.delBtn" size="mini" type="text" @click="removeDomain(scope.row)">删除</jvs-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-form-item
        class="tableform-no-label-item"
        style="margin-top: 10px"
        v-if="data.editable && data.addBtn"
      >
        <el-button @click="addRow">新增</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import userForm from '@/components/basic-assembly/userForm'
import { getDefaultNodeProps, nodeType, approvalType, nodeButtonList } from '@/views/flowable/views/design/common/enumConst'
export default {
  components: {
    FormItem: () => import("@/components/basic-assembly/formitem"),
    userForm
  },
  props: {
    field: {
      type: String,
      default: () => {
        return "";
      },
    },
    // 表格的对象
    data: {
      type: Object,
      default: () => {
        return {
          tableColumn: [],
        };
      },
    },
    // 左侧被拉动的组件 如果在table 中放开 则添加到table 中
    com: {
      type: Object,
    },
    action: {
      type: String,
      default: () => {
        return "";
      },
    },
    formsetting: {
      type: Object,
    },
    // 是否为列表页详情按钮
    isDetail: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    designId: {
      type: String,
    },
  },
  computed: {
    column() {
      return this.data.tableColumn;
    },
  },
  data() {
    return {
      tablekey: "tablekey" + new Date().getTime(),
      dynamicValidateForm: {
        domains: [],
      },
      columnDragStartIndex: -1,
      columnDragDom: null,
      columnDragTargetIndex: -1,
      columnDragTargetDom: null,
    };
  },
  methods: {
    submitForm(formName) {
      console.log(this.dynamicValidateForm);
      this.$refs[formName].validate((valid) => {
        if (valid) {
          console.log(this.dynamicValidateForm);
          alert("submit!");
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
    getRandomId(){
      return (Math.floor(Math.random() * (99999 - 10000)) + 10000).toString() + new Date().getTime().toString().substring(5, 11)
    },
    addRow () {
      let obj = {
        name: '',
        id: this.getRandomId(),
        nodeForm: {formId: "", sendUserForm: true, version: "" },
        type: nodeType.SP
      }
      let props = JSON.parse(JSON.stringify(getDefaultNodeProps(obj.type)))
      props.btn = JSON.parse(JSON.stringify(nodeButtonList))
      obj.props = props
      this.dynamicValidateForm.domains.push(obj)
      this.$forceUpdate()
    },
    removeDomain(item) {
      var index = this.dynamicValidateForm.domains.indexOf(item);
      if (index !== -1) {
        this.dynamicValidateForm.domains.splice(index, 1);
      }
    },
    set(data) {
      this.$emit("clickitem", data);
    },
    assignChange (val, row, index) {
      this.$set(this.dynamicValidateForm.domains[index].props.targetObj, 'personnels', [])
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
    data: {
      handler() {
        this.tablekey = "tablekey" + new Date().getTime();
        for (let i in this.data.tableColumn) {
          if (this.isDetail) {
            this.data.tableColumn[i].disabled = true;
          }
          this.data.tableColumn[i].width = this.data.tableColumn[i].span * 8;
        }
      },
      deep: true,
    },
  },
};
</script>

<style lang="scss" scoped>
.headeritem {
  /* background: red; */
  position: relative;
  box-sizing: border-box;
  .handle-btn {
    display: none;
    color: #ffffff;
    background-color: #0d76fc;
    font-size: 12px;
    padding: 0 5px;
    position: absolute;
    right: 0;
    bottom: 0;
    z-index: 1;
    .icons {
      text-align: right;
      cursor: pointer;
      font-size: 14px;
    }
  }
  &:hover {
    .handle-btn {
      display: inline-block;
    }
  }
}
.cont {
  position: relative;
  width: 100%;
  height: 117px;
  /* background: blue; */
}
//.icons:hover{
//  color: red;
//}
.table-form {
  .el-form-item {
    margin-bottom: 0;
  }
}

/deep/.tb-edit {
  .el-form-item {
    margin-bottom: 0;
  }
  .el-table__body-wrapper {
    height: auto !important;
  }
  .el-table__body-wrapper::-webkit-scrollbar {
    height: 8px;
  }
  .el-table__body-wrapper::-webkit-scrollbar-thumb {
    border-radius: 20px;
  }
  .el-table__fixed-right {
    padding-bottom: 4px;
  }
  .cell {
    > div {
      width: 100%;
    }
    .el-radio-group,
    .el-checkbox-group {
      width: 100%;
      div {
        display: flex;
        flex-wrap: wrap;
        .el-radio,
        .el-checkbox {
          min-width: 50%;
          margin-right: 0;
          text-align: left;
        }
      }
    }
  }
}
/deep/.tableform-no-label-item {
  .el-form-item__content {
    margin-left: 0 !important;
  }
}
.design-table-form {
  /deep/.el-table__header {
    .el-table__cell {
      padding: 0 !important;
      .cell {
        padding: 0;
        .headeritem {
          padding: 10px;
          cursor: move;
        }
      }
    }
    .el-table__cell:nth-last-of-type(2) {
      padding: 0 10px !important;
    }
  }
}
</style>
