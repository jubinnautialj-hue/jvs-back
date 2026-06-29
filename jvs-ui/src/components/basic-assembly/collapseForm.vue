<template>
  <el-collapse
    :ref="formItem.prop"
    v-model="activeName"
    :type="option.type || defaultOption.type"
    :tab-position="option.tabPosition || defaultOption.tabPosition"
    :stretch="option.stretch || defaultOption.stretch"
    :accordion="formItem.accordion"
    @change="handleClick"
  >
    <el-collapse-item
      v-for="(item, index) in dealExpress(option.column)"
      :key="item.name"
      :title="item.label"
      :disabled="item.disabled || disabledExpressHandle(item)"
      :name="item.name"
      :lazy="item.lazy"
      v-if="item.display"
    >
      <el-form
        :model="formItem.detachData ? (item.prop ? forms[item.prop] : forms) : forms[item.name]"
        :ref="formRef || 'ruleForm'"
        class="demo-dynamic"
        size='mini'
        :label-position="(originOption && originOption.formAlign) || defalutSet.option.formAlign"
        :label-width="(originOption && originOption.labelWidth) || defalutSet.option.labelWidth"
        :disabled='((originOption && originOption.disabled) || disabledExpressHandle(item))'
        @submit.native.prevent
      >
        <el-row v-if="formItem.column && formItem.column[item.name] && formItem.column[item.name].length > 0" style="width:100%;">
          <el-col
            v-for="it in formItem.column[item.name]"
            :key="it.prop"
            :span="it.span || 24"
            v-show="it.display == false ? it.display : true"
            :class="{'no-label-form-item': it.hideLabel}"
            v-if="displayExpressHandle(it)"
          >
            <div v-if="formItem.detachData && item.prop">
              <!-- 一般项 -->
              <el-form-item
                v-if="(it.type !='title' && (!it.children || it.children.length == 0) || it.type == 'formbox') && $permissionMatch(it.permisionFlag) && (it.display == false ? it.display : true)"
                :label="it.label"
                :prop='it.prop'
                :rules='requireExpressHandle(it.rules, it, index)'
                v-model="forms[item.prop][it.prop]"
                :label-width="it.type==='iframe'?'0':((originOption && originOption.labelWidth) || defalutSet.option.labelWidth)"
                :class='{"form-item-no-label": ( (!it.label && it.type != "tab") || (["tableForm","divider","p","section"].indexOf(it.type) > -1) ), "form-item-no-label-tab": (!it.label && it.type == "tab"), "reportTable-item": it.type == "reportTable"}'
              >
                <FormItem
                  :item="{...it, disabledControl: (disabledExpressHandle(item) || disabledExpressHandle(it))}"
                  :form="forms[item.prop]"
                  :originOption="originOption"
                  :defalutSet="defalutSet"
                  :roleOption="roleOption"
                  :userList="userList"
                  :departmentList="departmentList"
                  :postList="postList"
                  :resetRadom="resetRadom"
                  :designId="designId"
                  :isView="isView"
                  :changeRandom="changeRandom"
                  :changeDomItem="changeDomItem"
                  :execsList="execsList"
                  :jvsAppId="jvsAppId"
                  :originForm="originForm"
                  :parentDomWidth="parentDomWidth"
                  :dataTriggerFresh="dataTriggerFresh"
                  :ruleChange="ruleChange"
                  @formChange="formChange(forms[item.prop], it)"
                  @reInitData="reInitData"
                  @validateHandle="validateHandle" />
              </el-form-item>
            </div>
            <div v-else>
              <!-- 一般项 -->
              <el-form-item
                v-if="(it.type !='title' && (!it.children || it.children.length == 0) || it.type == 'formbox') && $permissionMatch(it.permisionFlag) && (it.display == false ? it.display : true)"
                :label="it.label"
                :prop='it.prop'
                :rules='requireExpressHandle(it.rules, it, index)'
                v-model="forms[formItem.detachData ? it.prop : item.name]"
                :label-width="it.type==='iframe'?'0':((originOption && originOption.labelWidth) || defalutSet.option.labelWidth)"
                :class='{"form-item-no-label": ( (!it.label && it.type != "tab") || (["tableForm","divider","p","section"].indexOf(it.type) > -1) ), "form-item-no-label-tab": (!it.label && it.type == "tab"), "reportTable-item": it.type == "reportTable"}'
              >
                <FormItem
                  :item="{...it, disabledControl: (disabledExpressHandle(item) || disabledExpressHandle(it))}"
                  :form="formItem.detachData ? forms : forms[item.name]"
                  :originOption="originOption"
                  :defalutSet="defalutSet"
                  :roleOption="roleOption"
                  :userList="userList"
                  :departmentList="departmentList"
                  :postList="postList"
                  :resetRadom="resetRadom"
                  :designId="designId"
                  :isView="isView"
                  :changeRandom="changeRandom"
                  :changeDomItem="changeDomItem"
                  :execsList="execsList"
                  :jvsAppId="jvsAppId"
                  :originForm="originForm"
                  :parentDomWidth="parentDomWidth"
                  :dataTriggerFresh="dataTriggerFresh"
                  :ruleChange="ruleChange"
                  @formChange="formChange(formItem.detachData ? forms : forms[item.name], it)"
                  @reInitData="reInitData"
                  @validateHandle="validateHandle" />
              </el-form-item>
            </div>
            
            <!-- 子表单项 -->
            <el-row v-if="it.type != 'formbox' && it.children && it.children.length > 0">
              <div v-if="formItem.detachData && !item.prop">
                <el-form-item
                  :label="it.label"
                  :prop="it.prop"
                  :rules="requireExpressHandle(it.rules, it, index)"
                  v-if="displayExpressHandle(it) && (it.display == false ? it.display : true)"
                  v-model="forms[it.prop]"
                  :class='(!it.label || (["tableForm","divider","p","tab","section"].indexOf(it.type) > -1))? "form-item-no-label" : ""'
                >
                  <FormItem
                    v-if="!it.formSlot"
                    :form="forms"
                    :item="{...it, disabledControl: (disabledExpressHandle(item) || disabledExpressHandle(it))}"
                    :originOption="originOption"
                    :defalutSet="defalutSet"
                    @formChange="formChange(forms, it)"
                    :roleOption="roleOption"
                    :userList="userList"
                    :departmentList="departmentList"
                    :postList="postList"
                    :designId="designId"
                    :resetRadom="resetRadom"
                    :isView="isView"
                    :changeRandom="changeRandom"
                    :changeDomItem="changeDomItem"
                    :execsList="execsList"
                    :jvsAppId="jvsAppId"
                    :originForm="originForm"
                    :parentDomWidth="parentDomWidth"
                    :dataTriggerFresh="dataTriggerFresh"
                    :ruleChange="ruleChange"
                    @reInitData="reInitData"
                    @validateHandle="validateHandle"
                  />
                  <!-- 自定义列插槽 -->
                  <div v-if="it.formSlot">
                    <slot :name="it.prop+'Form'"></slot>
                    <!-- 右侧提示 -->
                    <el-tooltip
                      v-if="it.tipsEnable !== false && it.tips && it.tips.position == 'right' && it.tips.text"
                      class="form-item-tooltip"
                      effect="dark"
                      :content="it.tips.text"
                      placement="top"
                    >
                      <i class="el-icon-question"></i>
                    </el-tooltip>
                  </div>
                  <!-- 换行提示 -->
                  <el-row
                    v-if="it.tipsEnable !== false && it.tips && it.tips.position == 'bottom'"
                    class="form-item-tips"
                  >
                    <span v-html="it.tips.text"></span>
                  </el-row>
                </el-form-item>
              </div>
              <div v-else>
                <el-form-item
                  :label="it.label"
                  :prop="it.prop"
                  :rules="requireExpressHandle(it.rules, it, index)"
                  v-if="displayExpressHandle(it) && (it.display == false ? it.display : true)"
                  v-model="forms[(formItem.detachData && item.prop) ? item.prop : item.name][it.prop]"
                  :class='(!it.label || (["tableForm","divider","p","tab","section"].indexOf(it.type) > -1))? "form-item-no-label" : ""'
                >
                  <FormItem
                    v-if="!it.formSlot"
                    :form="(formItem.detachData && item.prop) ? forms[item.prop] : forms[item.name]"
                    :item="{...it, disabledControl: (disabledExpressHandle(item) || disabledExpressHandle(it))}"
                    :originOption="originOption"
                    :defalutSet="defalutSet"
                    @formChange="formChange((formItem.detachData && item.prop) ? forms[item.prop] : forms[item.name], it)"
                    :roleOption="roleOption"
                    :userList="userList"
                    :departmentList="departmentList"
                    :postList="postList"
                    :designId="designId"
                    :resetRadom="resetRadom"
                    :isView="isView"
                    :changeRandom="changeRandom"
                    :changeDomItem="changeDomItem"
                    :execsList="execsList"
                    :jvsAppId="jvsAppId"
                    :originForm="originForm"
                    :parentDomWidth="parentDomWidth"
                    :dataTriggerFresh="dataTriggerFresh"
                    :ruleChange="ruleChange"
                    @reInitData="reInitData"
                    @validateHandle="validateHandle"
                  />
                  <!-- 自定义列插槽 -->
                  <div v-if="it.formSlot">
                    <slot :name="it.prop+'Form'"></slot>
                    <!-- 右侧提示 -->
                    <el-tooltip
                      v-if="it.tipsEnable !== false && it.tips && it.tips.position == 'right' && it.tips.text"
                      class="form-item-tooltip"
                      effect="dark"
                      :content="it.tips.text"
                      placement="top"
                    >
                      <i class="el-icon-question"></i>
                    </el-tooltip>
                  </div>
                  <!-- 换行提示 -->
                  <el-row
                    v-if="it.tipsEnable !== false && it.tips && it.tips.position == 'bottom'"
                    class="form-item-tips"
                  >
                    <span v-html="it.tips.text"></span>
                  </el-row>
                </el-form-item>
              </div>
              
              <el-col
                v-for="itc in it.children"
                :key="itc.prop"
                :span="itc.span || originOption.span || 24"
                v-if="displayExpressHandle(itc)"
              >
                <div v-if="formItem.detachData && !item.prop">
                  <el-form-item
                    :label="itc.label"
                    :prop="itc.prop"
                    v-if="itc.display === false ? false : linkbindHandle(forms[it.prop], itc.linkbind)"
                    :rules="requireExpressHandle(itc.rules, itc, index)"
                    v-model="forms[itc.prop]"
                    :class='{"form-item-no-label": (!itc.label || (["tableForm","divider","p","tab","section"].indexOf(itc.type) > -1)), "reportTable-item": itc.type == "reportTable"}'
                  >
                    <FormItem
                      v-if="!itc.formSlot"
                      :form="forms"
                      :item="{...itc, disabledControl: (disabledExpressHandle(item) || disabledExpressHandle(itc))}"
                      :formRef="defalutSet.refs"
                      :originOption="originOption"
                      :defalutSet="defalutSet"
                      @formChange="formChange(forms, it)"
                      :roleOption="roleOption"
                      :userList="userList"
                      :departmentList="departmentList"
                      :postList="postList"
                      :resetRadom="resetRadom"
                      :designId="designId"
                      :isView="isView"
                      :changeRandom="changeRandom"
                      :changeDomItem="changeDomItem"
                      :execsList="execsList"
                      :jvsAppId="jvsAppId"
                      :originForm="originForm"
                      :parentDomWidth="parentDomWidth"
                      :dataTriggerFresh="dataTriggerFresh"
                      :ruleChange="ruleChange"
                      @reInitData="reInitData"
                      @validateHandle="validateHandle"
                    />
                    <!-- 自定义列插槽 -->
                    <div v-if="itc.formSlot">
                      <slot :name="itc.prop+'Form'"></slot>
                      <!-- 右侧提示 -->
                      <el-tooltip
                        v-if="itc.tipsEnable !== false && itc.tips && itc.tips.position == 'right' && itc.tips.text"
                        class="form-item-tooltip"
                        effect="dark"
                        :content="itc.tips.text"
                        placement="top"
                      >
                        <i class="el-icon-question"></i>
                      </el-tooltip>
                    </div>
                    <!-- 换行提示 -->
                    <el-row
                      v-if="itc.tipsEnable !== false && itc.tips && itc.tips.position == 'bottom'"
                      class="form-item-tips"
                    >
                      <span v-html="itc.tips.text"></span>
                    </el-row>
                  </el-form-item>
                </div>
                <div v-else>
                  <el-form-item
                    :label="itc.label"
                    :prop="itc.prop"
                    v-if="itc.display === false ? false : linkbindHandle(forms[(formItem.detachData && item.prop) ? item.prop : item.name][it.prop], itc.linkbind)"
                    :rules="requireExpressHandle(itc.rules, itc, index)"
                    v-model="forms[(formItem.detachData && item.prop) ? item.prop : item.name][itc.prop]"
                    :class='{"form-item-no-label": (!itc.label || (["tableForm","divider","p","tab","section"].indexOf(itc.type) > -1)), "reportTable-item": itc.type == "reportTable"}'
                  >
                    <FormItem
                      v-if="!itc.formSlot"
                      :form="forms[(formItem.detachData && item.prop) ? item.prop : item.name]"
                      :item="{...itc, disabledControl: (disabledExpressHandle(item) || disabledExpressHandle(itc))}"
                      :formRef="defalutSet.refs"
                      :originOption="originOption"
                      :defalutSet="defalutSet"
                      @formChange="formChange(forms[(formItem.detachData && item.prop) ? item.prop : item.name], it)"
                      :roleOption="roleOption"
                      :userList="userList"
                      :departmentList="departmentList"
                      :postList="postList"
                      :resetRadom="resetRadom"
                      :designId="designId"
                      :isView="isView"
                      :changeRandom="changeRandom"
                      :changeDomItem="changeDomItem"
                      :execsList="execsList"
                      :jvsAppId="jvsAppId"
                      :originForm="originForm"
                      :parentDomWidth="parentDomWidth"
                      :dataTriggerFresh="dataTriggerFresh"
                      :ruleChange="ruleChange"
                      @reInitData="reInitData"
                      @validateHandle="validateHandle"
                    />
                    <!-- 自定义列插槽 -->
                    <div v-if="itc.formSlot">
                      <slot :name="itc.prop+'Form'"></slot>
                      <!-- 右侧提示 -->
                      <el-tooltip
                        v-if="itc.tipsEnable !== false && itc.tips && itc.tips.position == 'right' && itc.tips.text"
                        class="form-item-tooltip"
                        effect="dark"
                        :content="itc.tips.text"
                        placement="top"
                      >
                        <i class="el-icon-question"></i>
                      </el-tooltip>
                    </div>
                    <!-- 换行提示 -->
                    <el-row
                      v-if="itc.tipsEnable !== false && itc.tips && itc.tips.position == 'bottom'"
                      class="form-item-tips"
                    >
                      <span v-html="itc.tips.text"></span>
                    </el-row>
                  </el-form-item>
                </div>
              </el-col>
            </el-row>
          </el-col>
        </el-row>
        <slot :name="item.name"></slot>
      </el-form>
    </el-collapse-item>
  </el-collapse>
</template>
<script>
import {getUserInfo} from "@/api/admin/user";
import {getDefaultData} from "@/views/page/util/common";
import {doExec} from '@/components/basic-container/formula/api'
import {dataModelTriggering} from '@/components/api'
import FormItem from './formitem'
export default {
  name: "collapseForm",
  components: {
    FormItem
  },
  props: {
    // 绑定值，选中选项卡的 name
    active: {
      type: String,
      default: ''
    },
    // 选项卡配置
    option: {
      type: Object,
      default: () => {
        return {}
      }
    },
    // 表单传递对象
    forms: {
      type: Object,
      default: () => {
        return {}
      }
    },
    // 表单结构对象
    formItem: {
      type: Object,
      default: () => {
        return {}
      }
    },
    formRef: {
      type: String,
      default: 'ruleForm'
    },
    originOption: {
      type: Object
    },
    originForm: {
      type: Object
    },
    // 用户列表
    userList : {
      type: Array,
      default: () => {
        return []
      }
    },
    // 角色列表
    roleOption: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 部门列表
    departmentList: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 岗位列表
    postList: {
      type: Array,
      default: () => {
        return []
      }
    },
    resetRadom: {
      type: Number
    },
    designId: {
      type: String
    },
    isView: {
      type: Boolean
    },
    changeRandom: {
      type: Number
    },
    changeDomItem: {
      type: Object
    },
    execsList: {
      type: Array
    },
    jvsAppId:  {
      type: String
    },
    dataTriggerFresh: {
      type: Number
    }
  },
  data () {
    return {
      activeName: '',
      defaultOption: {
        type: '', // tab风格， card/border-card
        tabPosition: 'top', // 选项卡所在位置, top/right/bottom/left
        stretch: false, // 标签的宽度是否自撑开
        column: [
          {
            label: '', // 选项卡标题
            disabled: false, // 是否禁用
            name: '', // 与选项卡绑定值 value 对应的标识符，表示选项卡别名, 该选项卡在选项卡列表中的顺序值，如第一个选项卡则为'1'
            lazy: false, // 标签是否延迟渲染
            permisionFlag: '', // 权限标识
          }
        ]
      },
      defalutSet: {
        refs: 'ruleForm',
        option: {
          formAlign: 'right', //对其方式
          inline: false, // 表单项是否可以同行,当垂直方向空间受限且表单较简单时，可以在一行内放置表单
          labelWidth: 'auto', // label宽
          searchBtn: true, // 搜索按钮是否显示，默认显示
          searchBtnText: '查询', // 搜索按钮，默认查询
          submitBtn: true, // 提交按钮是否显示，默认显示
          submitBtnText: '提交', // 提交按钮文字，默认 提交
          emptyBtn: true, // 重置按钮，默认显示
          emptyBtnText: '重置', // 重置按钮文字，默认 重置
          cancalBtnText: '取消', // 取消按钮文字， 默认 取消
        }
      },
      initData: "",
      parentDomWidth: 0,
      userInfo: {},
      ruleChange: -1,
    }
  },
  async created () {
    if(this.$store.getters && this.$store.getters.userInfo) {
      this.userInfo = JSON.parse(JSON.stringify(this.$store.getters.userInfo))
    }else{
      await this.getUserInfo()
    }
    this.getDefaultData()
    this.dealPermission()
    if(this.active) {
      if(this.formItem.accordion) {
        this.activeName = this.active
      }
    }
    this.initData = JSON.stringify(this.forms)
  },
  mounted () {
    if(this.$refs[this.formItem.prop]) {
      this.parentDomWidth = this.$refs[this.formItem.prop].$el.offsetWidth
      this.$forceUpdate()
    }
  },
  methods: {
    async getUserInfo() {
      await getUserInfo().then(res => {
        if(res.data.code == 0) {
          this.userInfo = res.data.data
        }
      })
    },
    // 扩展组件默认值
    getDefaultData () {
      if(typeof this.option.column == 'object' && !(this.option.column instanceof Array)) {
        this.option.column.filter(col => {
          if(this.formItem.column[col.name] && this.formItem.column[col.name].length > 0) {
            if(this.formItem.detachData) {
              if(col.prop) {
                this.forms[col.prop] = getDefaultData(this.forms[col.prop], this.formItem.column[col.name], this.userInfo)
              }else{
                this.forms = getDefaultData(this.forms, this.formItem.column[col.name], this.userInfo)
              }
            }else{
              this.forms[col.name] = getDefaultData(this.forms[col.name], this.formItem.column[col.name], this.userInfo)
            }
          }
        })
      }
    },
    handleClick (active) {
      this.activeName = active
    },
    // 处理权限
    dealPermission () {
      for(let i in this.option.column) {
        if(!this.option.column[i].permisionFlag) {
          this.option.column[i].show = true
        }else{
          this.option.column[i].show = this.$permissionMatch(this.option.column[i].permisionFlag)
        }
        // 加入自定义校验
        if(this.formItem && this.formItem.column) {
          let tcol = this.formItem.column[this.option.column[i].name]
          if(tcol && tcol.length > 0) {
            for(let t in tcol) {
              if(tcol[t].regularExpression){
                let required = false
                if(tcol[t].rules && tcol[t].rules[0].required) {
                  required = true
                }
                let str = '/' + tcol[t].regularExpression + '/'
                let _this = this
                tcol[t].rules.push({
                  validator: function(rule, value, callback) {
                    if(eval(str).test(value)) {
                      callback()
                    }else{
                      let msg = '正则校验不通过'
                      if(tcol[t].regularMessage) {
                        msg = tcol[t].regularMessage
                      }
                      if(required == false && !value) {
                        callback()
                      }else{
                        callback(new Error(msg));
                      }
                    }
                  },
                  trigger: ['blur', 'change']
                })
              }
            }
          }
        }
      }
      this.option.column = this.option.column.filter(item => item.show)
    },
    // 值变化
    async formChange (form, item) {
      if(this.isView === true) {
        this.$emit('formChange', this.forms, item)
      }else{
        this.$emit('formChange', this.forms, item)
      }
      this.ruleChange = Math.random()
    },
    // 联动控制
    linkbindHandle (val, bind) {
      let bool = true
      if(bind) {
        if(val) {
          let arr = bind.split(',')
          if(val instanceof Array) {
            let tb = false
            for(let i in val) {
              if(arr.indexOf(val[i]) > -1) {
                tb = true
              }
            }
            bool = tb
          }else{
            if(arr.indexOf(val) > -1) {
              bool = true
            }else{
              bool = false
            }
          }
        }
      }
      return bool
    },
    // 表达式控制显示
    displayExpressHandle (item) {
      let bool = false
      let formStr = 'this.originForm' // 表单值参数名
      if(item.displayExpress && item.displayExpress.length > 0) {
        let list = item.displayExpress
        let temp = []
        let needValidateSpecial = false // 是否单独校验tab项的值
        for(let i in list) {
          let prop = (formStr + '.') // 控制字段名
          if(list[i].parent && list[i].parent.length > 0) {
            let pdomList = []
            this.findParentList(this.originOption.column, list[i].parent.join('.'), pdomList)
            if(pdomList.length > 0 && pdomList[0].type == 'tab' && pdomList[0].detachData) {
              if(pdomList.length >  1) {
                if(pdomList[1].prop) {
                  prop += (pdomList[1].prop + '.')
                  needValidateSpecial = true
                }
              }
            }else{
              prop += list[i].parent.join('.')
              prop += '.'
            }
          }
          prop += list[i].prop
          // 校验层级表单值是否为undefined
          let tpr = ''
          let exValidate = true
          // 存在父级且需要父级数据
          if(prop.split('.').length > 3) {
            if(needValidateSpecial) {
              let pt = []
              let pl = prop.split('.')
              pt = pl.slice(2, pl.length-1)
              tpr += formStr
              for(let p in pt) {
                tpr += ('.' + pt[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }else{
              tpr += formStr
              for(let p in list[i].parent) {
                tpr += ('.' + list[i].parent[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }
          }
          if(exValidate) {
            let tv = JSON.stringify(list[i].value.split(','))
            let isArrayData = false
            try {
              let pda = eval(prop)
              if(pda && typeof pda == 'object' && pda instanceof Array) {
                isArrayData = true
                let isIn = false
                for(let pix in pda) {
                  if(tv.indexOf(pda[pix]+'') > -1) {
                    isIn = true
                  }
                }
                temp.push(isIn)
              }
            } catch (error) {
              console.log(error)
            }
            if(!isArrayData) {
              tv += '.indexOf( '
              let tp = (tv + prop + ' + ' + "''" + ')')
              tp += (' > -1')
              temp.push(tp)
            }
          }
        }
        if(temp.length > 0) {
          if(eval(temp.join(` ${item.showOperator || '||'} `))) {
            bool = true
          }
        }
      }else{
        bool = true
      }
      if(!bool && !item.type && item.name == this.activeName) {
        this.activeName = ''
        this.option.column.filter(cit => {
          if(cit.display && !this.activeName) {
            this.activeName = cit.name
          }
        })
      }
      return bool
    },
    reInitData (prop, parentKey, index, tableType) {
      this.$emit('reInitData', prop, parentKey, index, tableType)
    },
    // 内部触发联动公式
    dataInitHandle (prop, parentKey) {
      if(this.designId) {
        if(this.dataModelId) {
          dataModelTriggering(this.jvsAppId, this.designId, this.dataModelId, this.forms).then(res => {
            if(res.data && res.data.code == 0) {
              if(res.data.data) {
                for(let i in res.data.data) {
                  this.$set(this.formDatas, i , res.data.data[i])
                }
                // 公式
                let obp = {
                  params: this.forms
                }
                if(prop) {
                  obp.modifiedField = prop
                }
                if(parentKey) {
                  obp.parentKey = parentKey.split('.')
                }
                doExec('jvs-design', this.designId, 'formItemValue', obp).then(res => {
                  if(res.data && res.data.code == 0) {
                    if(res.data.data) {
                      for(let i in res.data.data) {
                        this.$set(this.forms, i , res.data.data[i])
                      }
                    }
                    this.$emit('reInitData', this.formItem.prop, this.formItem.parentKey)
                  }
                })
              }
            }
          })
        }else{
          let obp = {
            params: this.formDatas
          }
          if(prop) {
            obp.modifiedField = prop
          }
          if(parentKey) {
            obp.parentKey = parentKey.split('.')
          }
          doExec('jvs-design', this.designId, 'formItemValue', obp).then(res => {
            if(res.data && res.data.code == 0) {
              if(res.data.data) {
                for(let i in res.data.data) {
                  this.$set(this.formDatas, i , res.data.data[i])
                }
              }
              this.$emit('reInitData', this.formItem.prop, this.formItem.parentKey)
            }
          })
        }
      }
    },
    // 表达式控制禁用
    disabledExpressHandle (item) {
      let bool = false
      let formStr = `this.originForm` // 表单值参数名
      if(item.disabledExpress && item.disabledExpress.length > 0) {
        let list = item.disabledExpress
        let temp = []
        let needValidateSpecial = false // 是否单独校验tab项的值
        for(let i in list) {
          let prop = (formStr + '.') // 控制字段名
          if(list[i].parent && list[i].parent.length > 0) {
            let pdomList = []
            this.findParentList(this.originOption.column, list[i].parent.join('.'), pdomList)
            if(pdomList.length > 0 && pdomList[0].type == 'tab' && pdomList[0].detachData) {
              if(pdomList.length >  1) {
                if(pdomList[1].prop) {
                  prop += (pdomList[1].prop + '.')
                  needValidateSpecial = true
                }
              }
            }else{
              prop += list[i].parent.join('.')
              prop += '.'
            }
          }
          prop += list[i].prop
          // 校验层级表单值是否为undefined
          let tpr = ''
          let exValidate = true
          // 存在父级且需要父级数据
          if(prop.split('.').length > 3) {
            if(needValidateSpecial) {
              let pt = []
              let pl = prop.split('.')
              pt = pl.slice(2, pl.length-1)
              tpr += formStr
              for(let p in pt) {
                tpr += ('.' + pt[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }else{
              tpr += formStr
              for(let p in list[i].parent) {
                tpr += ('.' + list[i].parent[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }
          }
          if(exValidate) {
            let tv = JSON.stringify(list[i].value.split(','))
            let isArrayData = false
            try {
              let pda = eval(prop)
              if(pda && typeof pda == 'object' && pda instanceof Array) {
                isArrayData = true
                let isIn = false
                for(let pix in pda) {
                  if(tv.indexOf(pda[pix]+'') > -1) {
                    isIn = true
                  }
                }
                temp.push(isIn)
              }
            } catch (error) {
              console.log(error)
            }
            if(!isArrayData) {
              tv += '.indexOf( '
              let tp = (tv + prop + ' + ' + "''" + ')')
              tp += (' > -1')
              temp.push(tp)
            }
          }
        }
        if(temp.length > 0) {
          if(eval(temp.join(` ${item.showOperator || '||'} `))) {
            bool = true
          }
        }
      }else{
        bool = false
      }
      return bool
    },
    // 表达式控制必填校验
    requireExpressHandle (rules, item, index) {
      if(item.requireExpress && item.requireExpress.length > 0) {
        let bool = false
        let formStr = `this.originForm`
        let list = item.requireExpress
        let temp = []
        let needValidateSpecial = false // 是否单独校验tab项的值
        for(let i in list) {
          let prop = (formStr + '.') // 控制字段名
          if(list[i].parent && list[i].parent.length > 0) {
            let pdomList = []
            this.findParentList(this.originOption.column, list[i].parent.join('.'), pdomList)
            if(pdomList.length > 0 && pdomList[0].type == 'tab' && pdomList[0].detachData) {
              if(pdomList.length >  1) {
                if(pdomList[1].prop) {
                  prop += (pdomList[1].prop + '.')
                  needValidateSpecial = true
                }
              }
            }else{
              prop += list[i].parent.join('.')
              prop += '.'
            }
          }
          prop += list[i].prop
          // 校验层级表单值是否为undefined
          let tpr = ''
          let exValidate = true
          // 存在父级且需要父级数据
          if(prop.split('.').length > 3) {
            if(needValidateSpecial) {
              let pt = []
              let pl = prop.split('.')
              pt = pl.slice(2, pl.length-1)
              tpr += formStr
              for(let p in pt) {
                tpr += ('.' + pt[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }else{
              tpr += formStr
              for(let p in list[i].parent) {
                tpr += ('.' + list[i].parent[p])
                if(eval(tpr) == undefined) {
                  exValidate = false
                  break;
                }
              }
            }
          }
          if(exValidate) {
            let tv = JSON.stringify(list[i].value.split(','))
            let isArrayData = false
            try {
              let pda = eval(prop)
              if(pda && typeof pda == 'object' && pda instanceof Array) {
                isArrayData = true
                let isIn = false
                for(let pix in pda) {
                  if(tv.indexOf(pda[pix]+'') > -1) {
                    isIn = true
                  }
                }
                temp.push(isIn)
              }
            } catch (error) {
              console.log(error)
            }
            if(!isArrayData) {
              tv += '.indexOf( '
              let tp = (tv + prop + ' + ' + "''" + ')')
              tp += (' > -1')
              temp.push(tp)
            }
          }
        }
        if(temp.length > 0) {
          if(eval(temp.join(` ${item.showOperator || '||'} `))) {
            bool = true
          }
        }
        if(bool) {
          if(rules && rules.length > 0) {
            this.$set(rules[0], 'required', true)
            if(this.$refs[this.formRef || 'ruleForm'] && this.$refs[this.formRef || 'ruleForm'].length > 0) {
              this.$refs[this.formRef || 'ruleForm'][index].validateField(item.prop)
            }
          }
        }else{
          if(rules && rules.length > 0) {
            this.$set(rules[0], 'required', false)
            if(this.$refs[this.formRef || 'ruleForm'] && this.$refs[this.formRef || 'ruleForm'].length > 0) {
              this.$refs[this.formRef || 'ruleForm'][index].clearValidate(item.prop)
            }
          }
        }
      }
      return rules
    },
    dealExpress (list) {
      list.filter(li => {
        li.display = (!li.displayExpress && li.display === false) ? false : this.displayExpressHandle(li)
      })
      return list
    },
    validateHandle (data) {
      let type = data.type
      let item = data.item
      this.$nextTick( () => {
        let list = JSON.parse(JSON.stringify(this.dealExpress(this.option.column)))
        for(let index in list) {
          if(this.activeName == list[index].name && this.$refs[this.formRef || 'ruleForm'][index]) {
            if(type == 'clear') {
              this.$refs[this.formRef || 'ruleForm'][index].clearValidate(item.prop)
            }else{
              this.$refs[this.formRef || 'ruleForm'][index].validateField(item.prop)
            }
          }
        }
      })
      this.$forceUpdate()
    },
    // 获取所有父节点信息
    findParentList (list, key, result) {
      if(list && list.length > 0) {
        list.filter(item => {
          if(key && item.parentKey == key) {
            item.parentDom.filter(ppit => {
              result.push(ppit)
            })
          }
          if(['tab', 'step'].indexOf(item.type) > -1) {
            for(let j in item.column) {
              if(item.column[j] && item.column[j].length > 0) {
                this.findParentList(item.column[j], key, result)
              }
            }
          }
        })
      }
    }
  },
  watch: {
    resetRadom: {
      handler (newVal, oldVal) {
        if(newVal > -1) {
          if(this.initData.startsWith('{')) {
            let temp = JSON.parse(this.initData)
            for(let i in this.option.column) {
              this.$set(this.forms, this.option.column[i].name, temp[this.option.column[i].name])
            }
          }
        }
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.el-collapse{
  /deep/.el-collapse-item{
    .el-collapse-item__header{
      display: flex!important;
    }
  }
}

</style>
