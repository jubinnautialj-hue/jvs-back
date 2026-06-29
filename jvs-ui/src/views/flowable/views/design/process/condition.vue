<template>
  <div :key="conditionKey">
    <div class="condition" v-for="(group, index) in selectedNode.groups" :key="'group-item'+index">
      <div class="condition-header">
        <div class="left"><span>条件组 {{ index + 1 }}</span></div>
        <div class="right">
          <el-popover placement="bottom" title="" width="300" trigger="click">
            <div style="padding: 0 10px;">
              <span>选择审批条件</span>
              <el-checkbox-group v-model="group.cids" :disabled="disabled" @change="conditionSelect(group)" style="margin-top: 10px;">
                <el-checkbox
                  v-for="condition in formList"
                  :label="condition.id"
                  :key="condition.id"
                >
                  {{ condition.name }}
                </el-checkbox>
              </el-checkbox-group>
            </div>
            <i class="el-icon-plus" slot="reference"></i>
          </el-popover>
          <i class="el-icon-close" style="margin-left: 10px;" @click="delGroup(index)"></i>
        </div>
      </div>
      <div class="condition-box">
        <div class="condition-box-left">
          <div v-if="group.condition && group.condition.length > 0" :class="{'connection': true, 'and': group.connection == logicType.AND}">
            <div :class="{'connection-text': true}" @click="group.connection=logicType[group.connection == logicType.AND ? 'OR' : 'AND'];">{{group.connection == logicType.AND ? '且' : '或'}}</div>
          </div>
        </div>
        <div class="condition-box-right">
          <div class="condition-list">
            <div  v-for="(condition, condindex) in group.condition" :key="condition.id" class="condition-list-item">
              <div class="left">
                <div class="top">
                  <div class="label">{{condition.name}}</div>
                  <div v-if="[type.DEPT, type.USER, 'ORG', 'org', 'FUN', 'fun'].indexOf(condition.type) > -1 && !disabled" class="top-add-button">
                    <div class="button" @click="addClick(condition)">
                      <div class="icon">
                        <svg aria-hidden="true">
                          <use xlink:href="#jvs-ui-icon-xinjian"></use>
                        </svg>
                      </div>
                      <span>选择</span>
                    </div>
                  </div>
                </div>
                <div class="bottom">
                  <div v-if="condition.type === 'number'">
                    <el-select
                      v-model="condition.symbol"
                      style="width: 140px; margin-right: 20px"
                      size="small"
                      :disabled="disabled"
                    >
                      <el-option
                        :value="sb.symbol"
                        :label="sb.name"
                        v-for="sb in symbolOptions"
                        :key="sb.symbol"
                      ></el-option>
                    </el-select>
                    <el-input
                      v-model="condition.left"
                      :disabled="disabled"
                      size="small"
                      type="number"
                      :placeholder="isBetween(condition.symbol) ? '最小值' : '比较值'"
                      :style="'width: ' + (isBetween(condition.symbol) ? '120px' : '260px')"
                    ></el-input>
                    <span v-if="isBetween(condition.symbol)"> ~ </span>
                    <el-input
                      v-model="condition.right"
                      :disabled="disabled"
                      placeholder="最大值"
                      type="number"
                      v-if="isBetween(condition.symbol)"
                      size="small"
                      style="width: 120px"
                    ></el-input>
                  </div>

                  <div v-else-if="condition.type === type.ONE || condition.type === type.MORE">
                    <el-select
                      v-model="condition.symbol"
                      :disabled="disabled"
                      style="width: 140px; margin-right: 20px"
                      size="small"
                    >
                      <el-option :value="'='" label="完全等于"></el-option>
                      <el-option :value="'in'" label="包含在"></el-option>
                    </el-select>
                    <el-select
                      v-model="condition.values"
                      :disabled="disabled"
                      style="width: 200px"
                      size="small"
                      multiple
                    >
                      <el-option
                        :value="op"
                        :label="op"
                        v-for="(op, id) in condition.options"
                        :key="id"
                      ></el-option>
                    </el-select>
                  </div>

                  <div v-else>
                    <div v-if="['FUN', 'fun'].indexOf(condition.type) === -1" class="tag-list-box">
                      <el-tag
                        :type="type.DEPT === user.type ? 'info' : 'primary'"
                        v-for="(user, index) in condition.values"
                        :key="index"
                        @close="condition.values.splice(index, 1)"
                        :closable="disabled ? false : true"
                      >
                        {{ user.name }}
                      </el-tag>
                    </div>
                    <div class="formula-box" v-else>
                      <codeBlockEditor :ref="condition.id" :refs="condition.id+'CodeEditor'" :disabled="true" :nodeDataList="nodeDataList"></codeBlockEditor>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="['ORG', 'org', 'FUN', 'fun'].indexOf(condition.type) > -1 && !disabled" class="right" @click="deleteGroupItem(condindex, group.condition, group.cids)">
                <svg>
                  <use xlink:href="#jvs-ui-icon-qingchulishijilu"></use>
                </svg>
              </div>
            </div> 
          </div>
        </div>
      </div>
    </div>

    <userSelector
      ref="condiUserSelector"
      :selectable="true"
      :autoClose="true"
      userEnable
      :deptEnable="!onlySelectUser"
      :roleEnable="!onlySelectUser"
      :jobEnable="!onlySelectUser"
      @submit="submitHandle">
    </userSelector>
  </div>
</template>

<script>
import { conditionType, logicType } from "../common/enumConst";
import userSelector from '@/components/basic-assembly/userSelector'
import codeBlockEditor from '@/components/basic-container/formula/codeEditor'
import { getFuncList } from "@/components/basic-container/formula/api"
export default {
  name: "condition",
  components: { userSelector, codeBlockEditor },
  props: {
    infoData: {
      type: Object,
      default: () => {
        return {}
      },
    },
    disabled: {
      type: Boolean
    }
  },
  data() {
    return {
      type: conditionType,
      onlySelectUser: false,
      itemCd: {},
      symbolOptions: [
        { name: "大于", symbol: ">" },
        { name: "大于等于", symbol: ">=" },
        { name: "小于", symbol: "<" },
        { name: "小于等于", symbol: "<=" },
        { name: "等于", symbol: "=" },
        { name: "介于两者之间", symbol: "between" },
        { name: "包含并介于两者之间", symbol: "between-eq" },
      ],
      conditionKey: '',
      nodeDataList: {},
      logicType: logicType,
    };
  },
  computed: {
    selectedNode() {
      return this.$store.state.flow.selectedNode;
    },
    formList() {
      //这个条件有5种类型 user 人员选择、 dept 部门选择、 number 数字、single 单选、 more 多选
      let result = [];
      this.$store.state.flow.template.form.forEach((atom) => {
        if (atom.valid) {
          if (atom.name === "jInput" && atom.props.type === "number") {
            result.push({
              id: atom.id,
              name: atom.text,
              type: "number",
            });
          } else if (atom.name === "jSelect") {
            result.push({
              id: atom.id,
              name: atom.text,
              type: atom.props.type,
              options: atom.props.options,
            });
          } else if (atom.name === "orgSelect") {
            result.push({
              id: atom.id,
              name: atom.text,
              type: atom.props.type,
            });
          }
        }
      });
      result.unshift({
        id: this.$store.state.flow.template.process.id,
        name: "发起人",
        type: "org",
      });
      result.push({
        id: this.selectedNode.id + '_func',
        name: "公式",
        type: "fun",
      })
      return result;
    },
  },
  methods: {
    isBetween(symbol) {
      return symbol === "between" || symbol === "between-eq";
    },
    selected (select) {
      this.$set(this.itemCd, "values", select.map((s) => {
        return { id: s.id, name: s.name, type: s.type ? s.type : 'user' };
      }));
      this.$set(this.selectedNode, 'groups', this.selectedNode.groups)
      this.$store.commit('selectedNode', this.selectedNode)
      this.$forceUpdate()
    },
    conditionSelect (group) {
      let condition = [];
      group.cids.forEach((cd) => {
        let bool = true
        group.condition.filter(cit => {
          if(cit.id == cd || (cd.endsWith('_func') && cit.type == 'fun')) {
            condition.push(JSON.parse(JSON.stringify(cit)))
            bool = false
          }
        })
        if(bool) {
          for(let key in this.formList) {
            if(this.formList[key].id === cd) {
              condition.push(JSON.parse(JSON.stringify(this.formList[key])))
              break;
            }
          }
        } 
      });
      group.condition = condition;
      this.$store.commit('setCondition', condition)
    },
    delGroup(index) {
      let temp = []
      for(let i in this.selectedNode.groups) {
        if(i != index) {
          temp.push(this.selectedNode.groups[i])
        }
      }
      this.$set(this.selectedNode, 'groups', temp)
    },
    // 选择成员
    chooseUser (condition) {
      this.itemCd = condition
      this.$refs.condiUserSelector.openDialog(condition.values)
    },
    // 选择公式
    chooseFun (condition) {
      this.itemCd = condition
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: "字段的中文显示",
        execId: condition.id.endsWith('_func') ? '' : condition.id,
        apiPrefix: 'jvs-design',
        useCase: 'flowItemValue',
        props: {
          businessId: this.selectedNode.id + '_func',
          jvsAppId: this.infoData.jvsAppId,
          designId: this.infoData.id
        },
        afterSave: (dialog, data) => {
          if(data && data.id) {
            condition.id = data.id
            condition.values = [{
              expr: data.body
            }]
            // data.id 公式id
            // data.body 公式内容
          }
          dialog.handleClose()
          this.updataFormulaShow()
        }
      })
    },
    // 确定成员
    submitHandle (list) {
      this.selected(list)
    },
    deleteGroupItem (index, list, cids) {
      let item = JSON.parse(JSON.stringify(list[index]))
      list = list.splice(index, 1)
      let delIndex = -1
      for(let i in cids) {
        if(cids[i] == item.id) {
          delIndex = i
        }
      }
      cids.splice(delIndex, 1)
    },
    addClick (condition) {
      if(['ORG', 'org'].indexOf(condition.type) > -1) {
        this.chooseUser(condition)
      }
      if(['FUN', 'fun'].indexOf(condition.type) > -1) {
        this.chooseFun(condition)
      }
    },
    // 获取全部函数
    async getFuncListHandle () {
      let nodeDataList = {}
      await getFuncList('jvs-design', 'flowItemValue', this.infoData.id).then(res => {
        if(res.data && res.data.code == 0) {
          let count = 0
          for(let i in res.data.data) {
            let obj = {
              title: i
            }
            if(typeof res.data.data[i] == 'object') {
              if(res.data.data[i] instanceof Array) {
                obj.type = 'list'
                obj.data = res.data.data[i]
                for(let o in obj.data) {
                  this.$set(nodeDataList, obj.data[o].id, {...obj.data[o], parent: (i+1)})
                }
              }else{
                obj.type = 'tree'
                let temp = []
                for(let k in res.data.data[i]) {
                  temp.push({
                    title: k,
                    data: res.data.data[i][k]
                  })
                  this.setParentHandle(res.data.data[i][k], Number(count)+1, nodeDataList, 0)
                }
                obj.data = temp
              }
            }
            count++
          }
          this.nodeDataList = nodeDataList
        }
      })
    },
    setParentHandle (list, parent, nodeDataList, index) {
      for(let i in list) {
        list[i].level = index
        this.$set(nodeDataList, list[i].id, {...list[i], parent: parent})
        if(list[i].children && list[i].children.length > 0) {
          this.setParentHandle(list[i].children, parent, nodeDataList, index+1)
        }
      }
    },
    findParamItem (key, item) {
      if(this.nodeDataList[key]) {
        let fit = this.nodeDataList[key]
        if(fit.id == key) {
          this.$set(item, 'id', fit.id)
          this.$set(item, 'name', fit.name)
          this.$set(item, 'param', fit.param)
          this.$set(item, 'parentIndex', i)
        }
      }
    },
    updataFormulaShow () {
      this.selectedNode.groups.filter(group => {
        if(group.condition && group.condition.length > 0) {
          group.condition.filter(condition => {
            if(condition.id && condition.values && condition.values.length > 0 && condition.values[0].expr) {
              let str = ``
              let code = condition.values[0].expr
              // 兼容codemirror的历史数据
              code = code.replaceAll(/[\u0000-\u001f\u007f-\u009f\u00ad\u061c\u200b\u200e\u200f\u2022\u2028\u2029\ufeff\ufff9-\ufffc]/gi, '\0')
              code = code.replaceAll(/\0\0/gi, '\0')
              if(code.includes('\0') > -1 || code.includes('\u0000') > -1) {
                code = code.replaceAll(/\0/gi, '<>&nbsp;<>')
                code = code.replaceAll(/\u0000/gi, '<>&nbsp;<>')
                let list = code.split('<>')
                for(let i in list) {
                  if(list[i]) {
                    if(list[i].startsWith('$')) {
                      let key = list[i].slice(2, list[i].length-1)
                      let item = {}
                      this.findParamItem(key, item)
                      if(item.id) {
                        str += `${item.param ? item.id : item.name}`
                      }else{
                        str += (list[i] == '&nbsp;' ? '' : list[i])
                      }
                    }else{
                      str += (list[i] == '&nbsp;' ? '' : list[i])
                    }
                  }
                }
              }else{
                str = code
              }
              str = str.replaceAll(/`/g, '')
              str = str.replaceAll(/\$\{/g, '')
              str = str.replaceAll(/\}/g, '')
              this.$nextTick(() => {
                this.$refs[condition.id][0].init(str)
              })
            }
          })
        }
      })
    }
  },
  async mounted () {
    await this.getFuncListHandle()
    this.updataFormulaShow()
  },
  watch: {
    'selectedNode.groups': {
      handler(newVal, oldVal) {
        this.conditionKey = Math.random().toString()
        this.updataFormulaShow()
        this.$forceUpdate()
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.top-add-button{
  .button{
    display: flex;
    align-items: center;
    cursor: pointer;
    .icon{
      width: 16px;
      height: 16px;
      background: #1E6FFF;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 4px;
      svg{
        width: 12px;
        height: 12px;
        fill: #fff;
      }
    }
    span{
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #1E6FFF;
      line-height: 18px;
    }
  } 
}
.condition {
  padding: 12px 16px 16px 8px;
  background: #F5F6F7;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  &::before{
    content: "";
    position: absolute;
    left: 0;
    top: 13px;
    width: 3px;
    height: 16px;
    background: #1E6FFF;
    border-radius: 0px 4px 4px 0px;
  }
  &+.condition{
    margin-top: 16px;
  }
  .condition-header{
    display: flex;
    align-items: center;
    justify-content: space-between;
    line-height: 18px;
    .left{
      padding-left: 8px;
      span{
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 14px;
        color: #363B4C;
      }
    }
    .right{
      i{
        color: #6F7588;
        font-weight: bold;
        font-size: 18px;
        cursor: pointer;
      }
    }
  }
  .condition-box{
    margin-top: 13px;
    display: flex;
    align-items: center;
    position: relative;
    .connection{
      height: 100%;
      position: relative;
      display: flex;
      align-items: center;
      .connection-text{
        width: 24px;
        height: 24px;
        line-height: 24px;
        text-align: center;
        background: #DFF3E3;
        border-radius: 4px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #36B452;
        cursor: pointer;
        &.disabled{
          cursor: not-allowed;
        }
      }
      &::before, &::after{
        content: "";
        position: absolute;
        height: calc(50% - 12px);
        left: 50%;
        top: 0;
        border-left: 1px solid #9AD9A8;
      }
      &::after{
        top: calc(50% + 12px);
      }
      &.and{
        .connection-text{
          background: #DBE8FF;
          color: #1E6FFF;
        }
        &::before, &::after{
          border-color: #D2E2FF;
        }
      }
    }
    .condition-box-left{
      width: 24px;
      height: 100%;
      position: absolute;
    }
    .condition-box-right{
      margin-left: 32px;
      flex: 1;
      overflow: hidden;
      .condition-list{
        .condition-list-item{
          display: flex;
          align-items: center;
          .left{
            flex: 1;
            overflow: hidden;
            background: #FFFFFF;
            border-radius: 4px;
            .top{
              padding: 0 8px;
              height: 36px;
              border-bottom: 1px solid #EEEFF0;
              display: flex;
              align-items: center;
              justify-content: space-between;
            }
            .bottom{
              padding: 8px;
              .tag-list-box{
                /deep/.el-tag{
                  margin: 4px;
                  background: #F5F6F7;
                  border-radius: 4px;
                  border: 0;
                  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
                  font-weight: 400;
                  font-size: 12px;
                  color: #363B4C;
                  .el-tag__close{
                    font-size: 16px;
                    color: #6F7588;
                    &:hover{
                      background: #F5F6F7;
                    }
                  }
                }
              }
              .formula-box{
                min-height: 48px;
                line-height: 16px;
                /deep/.code-block-editor{
                  .code-placeholder{
                    display: none;
                  }
                  .CodeMirror-scroll{
                    .CodeMirror-gutters{
                      display: none;
                    }
                    .cm-field{
                      display: inline-block;
                      border-radius: 4px;
                      padding: 0 5px;
                      margin: 1px;
                      color: #fff;
                      font-size: 12px;
                      line-height: 20px;
                      font-family: Helvetica Neue,Helvetica,PingFang SC,Hiragino Sans GB,Microsoft YaHei,Arial,sans-serif;
                    }
                    .cm-field-function{
                      color: #651573;
                      padding: 0;
                    }
                    .cm-field-param1{
                      background: #1B64E5;
                    }
                    .cm-field-param2{
                      background: #E6A23C;
                    }
                    .cm-field-param3{
                      background: #2ea320;
                    }
                    .cm-bracket{
                      color: #997;
                    }
                  }
                  .CodeMirror-cursors{
                    display: none;
                  }
                }
              }
            }
          }
          .right{
            margin-left: 8px;
            width: 24px;
            height: 24px;
            background: #FFFFFF;
            border-radius: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            svg{
              width: 16px;
              height: 16px;
              fill: #6F7588;
            }
          }
        }
        .condition-list-item+.condition-list-item{
          margin-top: 8px
        }
      }
    }
  }
}
</style>
