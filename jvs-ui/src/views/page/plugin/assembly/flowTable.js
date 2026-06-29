
import assembly from './assembly'
import formatKey from './format'
import Minput from './input'
import MSelect from './select'
import MUser from './user'
import enumConst from "@/views/flowable/views/design/common/enumConst";
export default class MFLowTable extends assembly{
  constructor (
    type='flowTable',
    label='流程设计',
    span=24,
    prop='flowTable'+ formatKey.numberToString(new Date().getTime()),
    tableColumn=[]
  ) {
    super(type,label,span,prop,tableColumn);
    this.span = 24
    this.showFrom = ['label', 'span', 'prop','border','stripe','sqlType', 'editable'] // 'disabled'
    this.border = true
    this.page = false
    this.editable = true
    this.addBtn = true
    this.editBtn = false
    this.viewBtn = false
    this.delBtn = true
    this.stripe = false
    this.sqlType = 'array'
    // 表格的校验在内部的组件中
    this.rules = []
    // 表单表头
    this.tableColumn = [
        { ...new Minput(),
            label: '步骤名称',
            prop: 'name',
            span: 24,
            placeholder: '步骤名称',
            width: (24 * 8),
            parentType: this.type,
            parentKey: (this.parentKey ? (this.parentKey + '.') : '') + this.prop,
            clearable: false,
            showFrom: ['label', 'span','minlength','maxlength','showwordlimit','placeholder','disabled','defaultValue'],
            rules: [ { required: true, message: '步骤名称不能为空', trigger: 'change'} ]
        },
        { ...new MSelect(),
            label: '审批人',
            prop: 'assignType',
            span: 24,
            placeholder: '审批人',
            width: (24 * 8),
            parentType: this.type,
            parentKey: (this.parentKey ? (this.parentKey + '.') : '') + this.prop,
            showFrom: ['label', 'span','placeholder','disabled'],
            rules: [ { required: true, message: '审批人不能为空', trigger: 'change'} ],
            multiple: false,
            defaultValue: enumConst.approvalType.ASSIGN_USER,
            props: {
                label: 'text',
                value: 'label'
            },
            dicData: [
                { label: enumConst.approvalType.ASSIGN_USER, text: "指定人员" },
                // { label: enumConst.approvalType.SELF_SELECT, text: "发起人自选" },
                // { label: enumConst.approvalType.LEADER_TOP, text: "连续多级主管"},
                // { label: enumConst.approvalType.LEADER, text: "直属主管"},
                { label: enumConst.approvalType.ROLE, text: "角色" },
                // { label: enumConst.approvalType.SELF, text: "发起人自己" },
                { label: enumConst.approvalType.JOB, text: "岗位" },
            ]
        },
        { ...new MUser(),
            label: '人员选择',
            prop: 'userSelect',
            span: 24,
            placeholder: '人员选择',
            width: (24 * 8),
            parentType: this.type,
            parentKey: (this.parentKey ? (this.parentKey + '.') : '') + this.prop,
            showFrom: ['label', 'span','placeholder','disabled'],
            rules: [ { required: true, message: '人员选择不能为空', trigger: 'change'} ],
            multiple: true,
            infoable: true
        }
    ]
    this.menuFix = false
    this.align = 'left'
  }
  addcolumn (data) {
    data.parentType = this.type
    data.parentKey = (this.parentKey ? (this.parentKey + '.') : '') + this.prop
    this.tableColumn.push(data)
  }
  deletecolumn (id) {
    this.tableColumn = this.tableColumn.filter(item => item.prop != id)
  }
}