<template>
  <div class="proxy-list">
    <jvs-table
      pageheadertitle="代理列表"
      :option="option"
      :data="tableData"
      :page="page"
      :loading="tableLoading"
      @on-load="getList"
      @search-change="searchHandle"
    >
      <template slot="menuLeft">
        <el-button v-if="permissionsList.indexOf('jvs_flow_proxy_add') > -1" type="primary" size="mini" @click="addHandle(null)">{{$langt('proxylist.add')}}</el-button>
        <el-button v-if="permissionsList.indexOf('jvs_flow_proxy_depart_proxy') > -1" type="primary" size="mini" @click="leaveHandle(null)">{{$langt('proxylist.leave')}}</el-button>
      </template>
      <template slot="beginTime" slot-scope="scope">
        <span v-if="scope.row && scope.row.proxyType == 'DEPART'">{{$langt('proxylist.forever')}}</span>
        <span v-else>{{scope.row.beginTime}}~{{scope.row.endTime}}</span>
      </template>
      <template slot="menu" slot-scope="scope">
        <jvs-button v-if="scope.row && scope.row.proxyType == 'DEPART'" type="text" size="mini" @click="leaveHandle(scope.row)">{{$langt('table.view')}}</jvs-button>
        <jvs-button v-else type="text" size="mini" @click="addHandle(scope.row)">{{$langt('table.view')}}</jvs-button>
        <jvs-button v-if="permissionsList.indexOf('jvs_flow_proxy_revoke') > -1 && [3, 4].indexOf(scope.row.status) == -1" type="text" size="mini" @click="dealHandle(scope.row)">{{$langt('proxylist.revoke')}}</jvs-button>
      </template>
    </jvs-table>
    <el-dialog
      :title="$langt('proxylist.normal')"
      append-to-body
      :visible.sync="dialogVisible"
      :before-close="handleClose">
      <div v-if="dialogVisible">
        <jvs-form :option="formOption" :formData="formData" @submit="subProxyUser" @cancalClick="handleClose">
          <template slot="userIdForm">
            <span class="slot-item-content" style="display: inline-block;color: #363B4C;cursor: text;background-color: #F5F6F7;
              border-radius: 4px;padding: 0 15px;width: 100%;box-sizing: border-box;font-size: 14px;font-family: Source Han Sans-Regular, Source Han Sans;font-weight: 400;">
              {{formData.userName}}
            </span>
          </template>
          <template slot="proxyUserIdForm">
            <span class="slot-item-content" style="display: inline-block;color: #363B4C;cursor: text;background-color: #F5F6F7;
              border-radius: 4px;padding: 0 15px;width: 100%;box-sizing: border-box;font-size: 14px;font-family: Source Han Sans-Regular, Source Han Sans;font-weight: 400;">
              {{formData.proxyUserName}}
            </span>
          </template>
          <template slot="proxyTimeForm">
            <span class="slot-item-content" style="display: inline-block;color: #363B4C;cursor: text;background-color: #F5F6F7;
              border-radius: 4px;padding: 0 15px;width: 100%;box-sizing: border-box;font-size: 14px;font-family: Source Han Sans-Regular, Source Han Sans;font-weight: 400;">
              {{formData.beginTime + '~' + formData.endTime}}
            </span>
          </template>
        </jvs-form>
      </div>
    </el-dialog>
    <el-dialog
      :title="$langt('proxylist.leave')"
      append-to-body
      :visible.sync="leaveVisible"
      :before-close="leaveClose">
      <div v-if="leaveVisible">
        <h4 style="margin-top: 0;">{{$langt('proxylist.rightNow')}}</h4>
        <jvs-form :option="leaveOption" :formData="leaveForm" @submit="subLeave" @cancalClick="leaveClose">
          <template slot="userIdForm">
            <span class="slot-item-content" style="display: inline-block;color: #363B4C;cursor: text;background-color: #F5F6F7;
              border-radius: 4px;padding: 0 15px;width: 100%;box-sizing: border-box;font-size: 14px;font-family: Source Han Sans-Regular, Source Han Sans;font-weight: 400;">
              {{leaveForm.userName}}
            </span>
          </template>
          <template slot="proxyUserIdForm">
            <span class="slot-item-content" style="display: inline-block;color: #363B4C;cursor: text;background-color: #F5F6F7;
              border-radius: 4px;padding: 0 15px;width: 100%;box-sizing: border-box;font-size: 14px;font-family: Source Han Sans-Regular, Source Han Sans;font-weight: 400;">
              {{leaveForm.proxyUserName}}
            </span>
          </template>
        </jvs-form>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {queryProxyList, addProxy, delProxy} from "../api/flowable"
import { getStore } from "@/util/store.js"
export default {
  data () {
    return {
      // 查询条件
      queryParams: {},
      tableData: [], // 表格数据
      tableLoading: false,
      option: {
        page: true,
        addBtn: false,
        viewBtn: false,
        editBtn: false,
        delBtn: false,
        showOverflow: true,
        search: true,
        searchBtnText: this.$langt('form.search'),
        cancal: false,
        column: [
          {
            label: '用户名',
            prop: 'userName',
            hide: true,
            search: true,
            viewDisplay: false
          },
          {
            label: '被代理人',
            prop: 'userName',
            searchSpan: 4
          },
          {
            label: '代理人',
            prop: 'proxyUserName',
          },
          {
            label: '代理时间',
            prop: 'beginTime',
            slot: true,
            searchSpan: 4
          },
          {
            label: '状态',
            prop: 'status',
            search: true,
            type: 'select',
            dicData: [
              {label: '待生效', value: 1},
              {label: '代理中', value: 2},
              {label: '已过期', value: 3},
              {label: '已撤销', value: 4}
            ],
            searchSpan: 4
          },
          {
            label: '创建人',
            prop: 'createBy',
            searchSpan: 4
          },
          {
            label: '创建时间',
            prop: 'createTime',
            datetype: 'datetime',
            type: "datePicker",
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
          },
          {
            label: '说明',
            prop: 'description',
            hide: true
          }
        ]
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000]
      },
      dialogVisible: false,
      formData: {},
      formOption: {
        emptyBtn: false,
        formAlign: 'top',
        column: [
          {
            label: '被代理人',
            prop: 'userId',
            type: 'user',
            props: {
              label: 'userName',
              value: 'userId'
            },
            rules: [
              { required: true, message: '请选择被代理人', trigger: 'change' }
            ],
          },
          {
            label: '代理人',
            prop: 'proxyUserId',
            type: 'user',
            props: {
              label: 'proxyUserName',
              value: 'proxyUserId'
            },
            rules: [
              { required: true, message: '请选择代理人', trigger: 'change' }
            ],
          },
          {
            label: '代理时间',
            prop: 'proxyTime',
            type: 'datePicker',
            datetype: 'datetimerange',
            startplaceholder: '开始时间',
            endplaceholder: '结束时间',
            rules: [
              { required: true, message: '请选择代理时间', trigger: 'change' }
            ],
            startLimit: '',
            formSlot: false
          },
          {
            label: '说明',
            prop: 'description',
            type: 'textarea'
          }
        ]
      },
      leaveVisible: false,
      leaveForm: {},
      leaveOption: {
        emptyBtn: false,
        formAlign: 'top',
        column: [
          {
            label: '离职人',
            prop: 'userId',
            type: 'user',
            allEnable: true,
            props: {
              label: 'userName',
              value: 'userId',
              allEnable: true
            },
            rules: [
              { required: true, message: '请选择离职人', trigger: 'change' }
            ],
          },
          {
            label: '代理人',
            prop: 'proxyUserId',
            type: 'user',
            props: {
              label: 'proxyUserName',
              value: 'proxyUserId'
            },
            rules: [
              { required: true, message: '请选择代理人', trigger: 'change' }
            ],
          }
        ]
      },
      permissionsList: [], //权限列表
    }
  },
  created () {
    this.permissionsList = getStore({name: 'permissions'}) || []
    this.option.column.filter((col, cix) => {
      if(col.label) {
        col.label = this.$langt(`proxylist.column.${col.prop}.label`)
        if(cix == 0 && col.prop == 'userName') {
          col.label = this.$langt(`proxylist.column.${col.prop}.label_search`)
        }
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`proxylist.column.${col.prop}.placeholder`)
      }
      if(col.dicData && col.dicData.length > 0) {
        col.dicData.filter(dit => {
          dit.label = this.$langt(`proxylist.column.${col.prop}.dicData.${dit.value}`)
          if(dit.tip) {
            dit.tip = this.$langt(`proxylist.column.${col.prop}.dicData.${dit.value}_tip`)
          }
        })
      }
    })
    this.formOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`proxylist.column.${col.prop}.label`)
        col.placeholder = this.$langt(`proxylist.column.${col.prop}.label`)
      }
      if(col.startplaceholder && col.endplaceholder) {
        col.startplaceholder = this.$langt(`proxylist.column.${col.prop}.startplaceholder`)
        col.endplaceholder = this.$langt(`proxylist.column.${col.prop}.endplaceholder`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`proxylist.column.${col.prop}.placeholder`)
      }
      if(col.dicData && col.dicData.length > 0) {
        col.dicData.filter(dit => {
          dit.label = this.$langt(`proxylist.column.${col.prop}.dicData.${dit.value}`)
        })
      }
    })
    this.leaveOption.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`proxylist.leaveColumn.${col.prop}.label`)
        col.placeholder = this.$langt(`proxylist.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`proxylist.leaveColumn.${col.prop}.placeholder`)
      }
    })
  },
  methods: {
    // 获取数据
    getList (page) {
      let query={
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      this.tableLoading=true
      let temp= {}
      temp=Object.assign(query, this.queryParams)
      queryProxyList(temp).then(res => {
        if (res.data.code==0) {
          // console.log(res.data.data)
          this.tableData=res.data.data.records
          this.tableLoading=false
          this.page.total=res.data.data.total
          this.page.currentPage=res.data.data.current
        }
      }).catch(e => {
        this.tableLoading=false
      })
    },
    // 搜索
    searchHandle (form) {
      this.queryParams=form
      this.getList()
    },
    // 新增
    addHandle (row) {
      if(row) {
        this.formData = JSON.parse(JSON.stringify(row))
        this.formOption.disabled = true
        this.formOption.btnHide = true
        this.formOption.column.filter(fit => {
          if(fit.prop == 'proxyTime') {
            fit.formSlot  = true
          }
        })
      }else{
        this.formOption.disabled = false
        this.formOption.btnHide = false
        this.formOption.column.filter(fit => {
          if(fit.prop == 'proxyTime') {
            fit.formSlot  = false
          }
        })
      }
      let userInfo = this.$store.getters.userInfo
      this.$set(this.formData, 'userId', userInfo.id)
      this.$set(this.formData, 'userName', userInfo.realName)
      let disabled = true
      if(!row && userInfo.adminFlag) {
        disabled = false
      }else{
        disabled = true
      }
      this.formOption.column.filter(item => {
        if(['userId', 'proxyUserId'].indexOf(item.prop ) > -1) {
          item.formSlot = disabled
        }
        if(item.prop == 'proxyTime') {
          item.startLimit = new Date().getTime()
        }
      })
      this.dialogVisible = true
    },
    // 提交
    subProxyUser () {
      let temp = JSON.parse(JSON.stringify(this.formData))
      delete temp.proxyTime
      if(this.formData.proxyTime && this.formData.proxyTime.length > 0) {
        temp.beginTime = this.formData.proxyTime[0]
        temp.endTime =  this.formData.proxyTime[1]
      }
      temp.proxyType = 'DEFAULT'
      addProxy(temp).then(res => {
       if(res.data && res.data.code == 0) {
         this.$notify({
           title: this.$langt('common.tip'),
           message: this.$langt('proxylist.addSuccess'),
           position: 'bottom-right',
           type: 'success'
         });
         this.getList()
         this.handleClose()
       }
     })
    },
    // 关闭弹框
    handleClose () {
      this.formData = {}
      this.dialogVisible = false
    },
    // 撤销
    dealHandle (row) {
      this.$confirm(this.$langt('proxylist.confirm'), this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: 'warning'
      }).then(() => {
        delProxy(row.id).then(res => {
          if (res.data.code==0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('proxylist.delSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.getList()
          }
        })
      }).catch(e => { })
    },
    // 离职代理
    leaveHandle (row) {
      if(row) {
        this.leaveForm = JSON.parse(JSON.stringify(row))
        this.leaveOption.btnHide = true
        this.leaveOption.disabled = true
        this.leaveOption.column.filter(col => {
          col.formSlot = true
        })
      }else{
        this.leaveOption.btnHide = false
        this.leaveOption.disabled = false
        this.leaveOption.column.filter(col => {
          col.formSlot = false
        })
      }
      this.leaveVisible = true
    },
    subLeave () {
      this.leaveForm.proxyType = 'DEPART'
      addProxy(this.leaveForm).then(res => {
       if(res.data && res.data.code == 0) {
         this.$notify({
           title: this.$langt('common.tip'),
           message: this.$langt('proxylist.leaveSuccess'),
           position: 'bottom-right',
           type: 'success'
         });
        this.getList()
        this.leaveClose()
       }
     })
    },
    leaveClose () {
      this.leaveVisible = false
      this.leaveForm = {}
    }
  }
}
</script>
