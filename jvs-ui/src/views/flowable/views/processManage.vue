<template>
  <div class="process-manage-list">
    <div class="mode-type-list">
      <div v-for="ritem in modeList" :key="ritem.value"
           :class="{'mode-type-list-item': true, 'active': ritem.value == currentType}"
           @click="searchTypeChange(ritem.value)">{{ $langt(`topNav.modeUser.${ritem.value}`) }}
      </div>
    </div>
    <!-- v-if="jvsAppIdList.length > 0" -->
    <jvs-table
      ref="taskTable"
      pageheadertitle="流程管理"
      selectable
      :option="option"
      :data="tableData"
      :formData="formData"
      :page="page"
      :loading="tableLoading"
      @on-load="getList"
      @search-change="searchHandle"
    >
      <template slot="menuLeft">
        <div class="table-btn-box">
          <jvs-button type="primary" size="mini" @click="exportHandle" :loading="butLoading">
            {{ $langt('processmanage.topButton.exportExcel') }}
          </jvs-button>
          <jvs-button type="primary" size="mini" @click="batchStopHandle" :loading="butLoading">
            {{ $langt('processmanage.topButton.batchStop') }}
          </jvs-button>
        </div>
      </template>
      <template slot="menu" slot-scope="scope">
        <div>
          <jvs-button type="text" size="mini" @click="tableListBtn(scope.row)">
            {{ $langt('processmanage.approvalProgress') }}
          </jvs-button>
          <jvs-button
            v-if="permissionsList.indexOf('jvs_flow_task_manage_assign') > -1 && scope.row.taskStatus == 1 && scope.row.nodes && scope.row.nodes.length > 0 && !scope.row.rootNode"
            type="text" size="mini" @click="rowHandle(scope.row, 'assign')">{{ $langt('processmanage.add') }}
          </jvs-button>
          <jvs-button
            v-if="permissionsList.indexOf('jvs_flow_task_manage_remove_approver') > -1 && scope.row.taskStatus == 1 && scope.row.nodes && scope.row.nodes.length > 0 && !scope.row.rootNode"
            type="text" size="mini" @click="rowHandle(scope.row, 'reduce')">{{ $langt('processmanage.reduce') }}
          </jvs-button>
          <jvs-button
            v-if="permissionsList.indexOf('jvs_flow_task_manage_transfer') > -1 && scope.row.taskStatus == 1 && scope.row.nodes && scope.row.nodes.length > 0 && !scope.row.rootNode"
            type="text" size="mini" @click="rowHandle(scope.row, 'trans')">{{ $langt('processmanage.trans') }}
          </jvs-button>
          <jvs-button
            v-if="permissionsList.indexOf('jvs_flow_task_manage_stop') > -1 && scope.row.taskStatus == 1 && scope.row.nodes && scope.row.nodes.length > 0"
            type="text" size="mini" @click="rowHandle(scope.row, 'stop')">{{ $langt('processmanage.stop') }}
          </jvs-button>
        </div>
      </template>
    </jvs-table>
    <el-dialog
      class="custom-header-dialog"
      :visible.sync="dialogVisible"
      append-to-body
      :title="dialogTitle"
      :before-close="dialogClose"
      width="50%"
    >
      <div v-if="dialogVisible" style="padding: 10px 20px;box-sizing: border-box;">
        <div v-if="dialogType == 'assign'" class="assign">
          <div v-for="node in dialogForm.approvers" :key="node.id" class="assign-item">
            <div class="label">{{ getNodeName(node.nodeId) }}</div>
            <div class="cont">
              <userForm :form="node" prop="approvers" :selectable="true" :infoable="true" type="user"
                        :props="{label: 'name', value: 'id'}"
                        :placeholder="$langt('processmanage.chooseUser')"></userForm>
            </div>
          </div>
        </div>
        <div v-if="dialogType == 'reduce'" class="reduce">
          <div v-for="(node, index) in dialogForm" :key="node.id" class="reduce-item">
            <div class="reduce-item-div">
              <div class="label">{{ getNodeName(node.nodeId) }}</div>
              <div class="tag-list">
                <span style="margin-right: 10px;font-size: 12px;">{{ $langt('processmanage.user') }}</span>
                <el-tag v-for="user in getNodeUsers(node.nodeId)" :key="user.id"
                        :closable="node.userIds.indexOf(user.id) == -1" @close="deleteUser(user, node, index)">
                  {{ user.realName }}
                </el-tag>
              </div>
            </div>
            <div class="reduce-item-div">
              <div class="tag-list">
                <span style="margin-right: 10px;font-size: 12px;">{{ $langt('processmanage.reduceUser') }}</span>
                <el-tag v-for="userid in node.userIds" :key="userid" :closable="true" type="danger"
                        @close="deleteReduceUser(userid, node)">{{ getUserRealName(userid, node.nodeId) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
        <div v-if="dialogType == 'trans'" class="trans">
          <div v-for="(node, index) in dialogForm" :key="node.id" class="trans-item">
            <div class="label">{{ getNodeName(node.nodeId) }}</div>
            <div class="cont">
              <div v-for="(trans, tix) in node.transfers" :key="'trans-item-'+index+'-'+tix" class="cont-item">
                <div class="cont-item-div">
                  <span class="name">{{ trans.userName }}</span>
                  <span>{{ $langt('processmanage.transUser') }}</span>
                  <userForm :form="trans" prop="proxyUserId" :selectable="false" :infoable="true" type="user"
                            :props="{label: 'proxyUserName', value: 'proxyUserId'}"
                            :placeholder="$langt('processmanage.chooseUser')"></userForm>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="['assign', 'trans', 'reduce'].indexOf(dialogType) > -1" class="footer-row">
          <el-button type="primary" size="mini" @click="submitHandle">{{ $langt('form.submit') }}</el-button>
          <el-button size="mini" @click="dialogClose">{{ $langt('form.cancel') }}</el-button>
        </div>
        <div v-if="dialogType == 'stop'">
          <jvs-form :option="stopOption" :formData="dialogForm" @submit="submitHandle"
                    @cancalClick="dialogClose"></jvs-form>
        </div>
      </div>
    </el-dialog>
    <el-dialog title="审批进度" :visible.sync="dialogTableVisible" width="60%">
      <el-table :data="tableDataList" style="width: 100%">
        <el-table-column prop="name" label="流程名称"></el-table-column>
        <el-table-column prop="nodeName" label="节点名称"></el-table-column>
        <el-table-column prop="userNames" label="处理人" width="130"></el-table-column>
        <el-table-column prop="reachTime" label="到达时间" width="180"></el-table-column>
        <el-table-column prop="handleTime" label="处理时间" width="180"></el-table-column>
        <el-table-column prop="durationTime" label="处理时长(H)" width="130"></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import {
  getTaskManage,
  assignManage,
  reduceManage,
  stopManage,
  batchStopManage,
  transManage,
  getJvsAppList
} from "../api/flowable"
import userForm from '@/components/basic-assembly/userForm'
import {getStore} from "@/util/store.js"
import {deepClone} from "@/util/util"

export default {
  name: 'task-list',
  components: {userForm},
  props: {
    pageSize: {
      type: Number,
      default: () => {
        return 20
      }
    },
  },
  computed: {
    dialogTitle() {
      let str = ''
      switch (this.dialogType) {
        case 'assign':
          str = this.$langt('processmanage.add');
          break;
        case 'reduce':
          str = this.$langt('processmanage.reduce');
          break;
        case 'trans':
          str = this.$langt('processmanage.trans');
          break;
        case 'stop':
          str = this.$langt('processmanage.stop');
          break;
        default:
          ;
          break;
      }
      return str
    },
    option() {
      return {
        page: true,
        addBtn: false,
        viewBtn: false,
        editBtn: false,
        delBtn: false,
        showOverflow: true,
        search: true,
        searchBtnText: this.$langt('form.search'),
        cancal: false,
        menuWidth: this.pageSize ? '200px' : '',
        // menuFix: true,
        menuFix: 'right', // 操作栏固定位置
        column: [
          {
            label: '流程标题',
            prop: 'title',
            search: true,
            searchSpan: 8,
            width:350
          },
          {
            label: '流程名称',
            prop: 'name',
            search: true,
            searchSpan: 8,
            width:200
          },
          // {
          //   label: '流程编号',
          //   prop: 'taskCode',
          //   search: true,
          //   searchSpan: 6,
          //   width:200
          // },
          {
            label: '应用名称',
            prop: 'jvsAppId',
            type: 'select',
            // dicUrl: "/mgr/jvs-design/base/use/menu?mode=" + this.currentType,
            // method: 'get',
            dicData: this.jvsAppIdList,
            defalutValue: this.jvsAppIdList[0] ? this.jvsAppIdList[0].id : '',
            props: {
              label: 'name',
              value: 'id'
            },
            clearable: false,
            search: true,
            searchSpan: 8,
            width:200
          },
          {
            label: '发起单位',
            prop: 'createDeptName',
            width:120
          },
          {
            label: '审批状态',
            prop: 'taskStatus',
            type: 'select',
            search: true,
            searchSpan: 8,
            dicData: [
              {label: '待审批', value: 1},
              {label: '已通过', value: 2}
            ]
          },
          {
            label: '当前环节',
            prop: 'currentNodeName',
            width:120
          },
          {
            label: '发起人',
            prop: 'createBy',
            width:80
          },
          {
            label: '创建时间',
            prop: 'createTime',
            datetype: 'datetime',
            type: "datePicker",
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
            width: 150
          }
        ]
      }
    },
  },
  data() {
    return {
      currentType: 'GA',
      modeList: [
        {label: '正式模式', value: 'GA'},
        {label: '测试模式', value: 'BETA'},
        {label: '开发模式', value: 'DEV'}
      ],
      // 查询条件
      queryParams: {},
      tableData: [], // 表格数据
      tableLoading: false,
      butLoading: false,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000]
      },
      rowData: null,
      dialogType: '',
      dialogVisible: false,
      dialogForm: null,
      stopOption: {
        emptyBtn: false,
        formAlign: 'top',
        column: [
          {
            label: '终止原因',
            prop: 'reason',
            type: 'textarea'
          }
        ]
      },
      permissionsList: [], //权限列表
      tableDataList: [],
      dialogTableVisible: false,
      proName: '',
      jvsAppIdList: [],
      formData: {},
    }
  },
  async created() {
    this.permissionsList = getStore({name: 'permissions'}) || []
    let language = navigator.language || navigator.userLanguage;
    if (!(/zh/i.test(language) || /zh-CN/i.test(language) || /zh-TW/i.test(language) || /zh-HK/i.test(language))) {
      this.option.menuWidth = '250px'
    }
    this.option.column.filter(col => {
      if (col.label) {
        col.label = this.$langt(`processmanage.column.${col.prop}.label`)
      }
      if (col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`processmanage.column.${col.prop}.placeholder`)
      }
      if (col.dicData && col.dicData.length > 0) {
        col.dicData.filter(dit => {
          dit.label = this.$langt(`processmanage.column.${col.prop}.dicData.${dit.value}`)
        })
      }
    })
    this.stopOption.column.filter(col => {
      if (col.label) {
        col.label = this.$langt(`processmanage.column.${col.prop}.label`)
      }
    })
    await this.getJvsAppIdList(this.currentType)
    this.getList()
  },
  methods: {
    // 获取数据
    getList(page) {
      let query = {
        current: this.page.currentPage,
        // size: this.pageSize > 0 ? this.pageSize : this.page.pageSize
        size: this.page.pageSize
      }
      if (this.currentType) {
        query.mode = this.currentType
      }
      this.tableLoading = true
      let temp = {}
      if (this.queryParams.name) {
        temp.flowName = this.queryParams.name
      }
      if (this.queryParams.title) {
        temp.title = this.queryParams.title
      }
      if (this.queryParams.createBy) {
        temp.sendUser = this.queryParams.createBy
      }
      if (this.queryParams.taskCode) {
        temp.taskCode = this.queryParams.taskCode
      }
      if (this.queryParams.jvsAppId) {
        temp.jvsAppId = this.queryParams.jvsAppId
      }
      if (this.queryParams.taskStatus) {
        temp.taskStatus = this.queryParams.taskStatus
      }
      temp = Object.assign(temp, this.formData, query)
      this.tableData = []
      // console.log('temp', temp)
      getTaskManage(temp).then(res => {
        if (res.data.code == 0) {
          this.tableData = res.data.data.records
          this.tableLoading = false
          this.page.total = res.data.data.total
          this.page.currentPage = res.data.data.current
        }
      }).catch(e => {
        this.tableLoading = false
      })
    },
    // 搜索
    searchHandle(form) {
      this.queryParams = form
      this.getList()
    },
    async searchTypeChange(type) {
      if (type != this.currentType) {
        await this.getJvsAppIdList(type)
        this.page.currentPage = 1
        this.currentType = type
        this.getList()
        // this.$nextTick(() => {
        //   this.$refs.taskTable.init()
        // })
      }
    },
    // 导出审批表
    exportHandle() {
      this.butLoading = true;
      let temp = {}
      if (this.queryParams.name) {
        temp.flowName = this.queryParams.name
      }
      if (this.queryParams.title) {
        temp.title = this.queryParams.title
      }
      if (this.queryParams.createBy) {
        temp.sendUser = this.queryParams.createBy
      }
      if (this.queryParams.taskCode) {
        temp.taskCode = this.queryParams.taskCode
      }
      if (this.queryParams.jvsAppId) {
        temp.jvsAppId = this.queryParams.jvsAppId
      }
      if (this.queryParams.taskStatus) {
        temp.taskStatus = this.queryParams.taskStatus
      }
      temp.mode = this.currentType
      const queryString = new URLSearchParams(temp).toString();
      // 拼接URL和查询字符串
      const url = `/mgr/jvs-design/base/workflow/task/manage/exportTaskManage?${queryString}`;
      // 打开URL
      this.$openUrl(url, '_blank');
      setTimeout(() => {
        this.butLoading = false;
      }, 8000); // 8000毫秒后执行
    },
    // 批量终止
    batchStopHandle() {
      // console.log(this.$refs.taskTable, this.$refs.taskTable.$refs.multipleTable.selection)
      if(!this.$refs.taskTable.$refs.multipleTable.selection?.length){
        this.$message.warning("未选择数据");
        return false
      }
      const taskIds = this.$refs.taskTable.$refs.multipleTable.selection.map(item=>{
        return item.id
      })
      // console.log(taskIds)
      batchStopManage({ taskIds }).then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '终止成功',
            position: 'bottom-right',
            type: 'success'
          })
          this.getList()
        }
      })
    },
    // 审批进度
    tableListBtn(row) {
      this.dialogTableVisible = true;
      const result = [{
        name: row.name,
        time: row.updateTime,
        nodeName: row.currentNodeName,
        userNames: row.nodes.flatMap(node =>
          (node.users || []).map(user => user.realName)
        ).join(', ')
      }];
      const coursesList = row.courses.map(item => ({
        name: row.name,
        nodeName: item.nodeName,
        reachTime: '',
        durationTime: '',
        handleTime: item.time,
        userNames: item.approveResultDtos
          .map(approval => approval.userName)
          .join(", ")
      }));
      let mergedList = [...coursesList, ...result];
      mergedList = mergedList.map((item, index, arr) => {
        if (index === 0) return item;
        const prevItem = arr[index - 1];
        const newItem = {
          ...item,
          reachTime: prevItem.handleTime || prevItem.time || ''
        };
        const handleTime = newItem.handleTime;
        const reachTime = newItem.reachTime;

        if (handleTime && reachTime) {
          const handleDate = new Date(handleTime);
          const reachDate = new Date(reachTime);
          if (!isNaN(handleDate.getTime()) && !isNaN(reachDate.getTime())) {
            const diffMs = handleDate.getTime() - reachDate.getTime();
            const diffHours = parseInt(diffMs / (1000 * 60 * 60));
            if (diffHours === 0) {
              newItem.durationTime = '';
            } else {
              newItem.durationTime = diffHours;
            }
          }
        }

        return newItem;
      });
      this.tableDataList = mergedList;
    },
    rowHandle(row, type) {
      this.rowData = JSON.parse(JSON.stringify(row))
      this.dialogType = type
      if (type == 'assign') {
        let temp = []
        if (row.nodes && row.nodes.length > 0) {
          row.nodes.filter(fit => {
            temp.push({
              nodeId: fit.nodeId,
              approvers: []
            })
          })
        }
        this.dialogForm = {
          taskId: row.id,
          approvers: temp
        }
      }
      if (type == 'reduce') {
        let temp = []
        if (row.nodes && row.nodes.length > 0) {
          row.nodes.filter(fit => {
            temp.push({
              nodeId: fit.nodeId,
              userIds: []
            })
          })
        }
        this.dialogForm = temp
      }
      if (type == 'stop') {
        this.dialogForm = {}
      }
      if (type == 'trans') {
        let temp = []
        if (row.nodes && row.nodes.length > 0) {
          row.nodes.filter(fit => {
            let tp = []
            if (fit.users && fit.users.length > 0) {
              fit.users.filter(uit => {
                tp.push({
                  userId: uit.id,
                  userName: uit.realName,
                  proxyUserId: '',
                  proxyUserName: ''
                })
              })
            }
            temp.push({
              nodeId: fit.nodeId,
              transfers: tp
            })
          })
        }
        this.dialogForm = temp
      }
      this.dialogVisible = true
    },
    dialogClose() {
      this.dialogVisible = false
      this.rowData = null
      this.dialogForm = null
      this.dialogType = ''
    },
    getNodeName(id) {
      let str = ''
      if (this.rowData.nodes && this.rowData.nodes.length > 0) {
        this.rowData.nodes.filter(nit => {
          if (nit.nodeId == id) {
            str = nit.nodeName
          }
        })
      }
      return str
    },
    getNodeUsers(id) {
      let temp = []
      if (this.rowData.nodes && this.rowData.nodes.length > 0) {
        this.rowData.nodes.filter(nit => {
          if (nit.nodeId == id) {
            temp = nit.users
          }
        })
      }
      return temp
    },
    getUserRealName(userId, id) {
      let temp = ''
      if (this.rowData.nodes && this.rowData.nodes.length > 0) {
        this.rowData.nodes.filter(nit => {
          if (nit.nodeId == id) {
            nit.users.filter(user => {
              if (user.id == userId) {
                temp = user.realName
              }
            })
          }
        })
      }
      return temp
    },
    deleteUser(user, node, index) {
      if (node.userIds.indexOf(user.id) == -1) {
        node.userIds.push(user.id)
      }
      this.$forceUpdate()
    },
    deleteReduceUser(id, node) {
      let index = node.userIds.indexOf(id)
      if (index > -1) {
        node.userIds.splice(index, 1)
      }
      this.$forceUpdate()
    },
    submitHandle() {
      if (this.dialogType == 'assign') {
        assignManage(this.dialogForm).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '增员成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.dialogClose()
            this.getList()
          }
        })
      }
      if (this.dialogType == 'reduce') {
        reduceManage(this.rowData.id, this.dialogForm).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '减员成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.dialogClose()
            this.getList()
          }
        })
      }
      if (this.dialogType == 'stop') {
        stopManage(this.rowData.id, this.dialogForm).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '终止成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.dialogClose()
            this.getList()
          }
        })
      }
      if (this.dialogType == 'trans') {
        transManage(this.rowData.id, this.dialogForm).then(res => {
          if (res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '转交成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.dialogClose()
            this.getList()
          }
        })
      }
    },
    async getJvsAppIdList(mode) {
      // 获取应用列表
      this.jvsAppIdList = (await getJvsAppList(mode))?.data?.data || []
      this.queryParams.jvsAppId = this.formData.jvsAppId = this.jvsAppIdList[0] ? this.jvsAppIdList[0].id : ''
      // console.log(this.$refs.taskTable)
      // console.log('jvsAppIdList', this.jvsAppIdList, this.queryParams.jvsAppId)
      if (this.$refs.taskTable && this.$refs.taskTable.searchOption.column) {
        const searchColumnTemp = deepClone(this.$refs.taskTable.searchOption.column)
        searchColumnTemp.forEach(c=>{
          if(c.prop==='jvsAppId'){
            c.dicData = this.jvsAppIdList
          }
        })
        // console.log(searchColumnTemp)
        this.$refs.taskTable.searchOption.column = searchColumnTemp
      }
      // getJvsAppList(mode).then(res => {
      //   this.jvsAppIdList = res.data.data || []
      //   console.log(this.jvsAppIdList)
      //   this.queryParams.jvsAppId = this.formData.jvsAppId = this.jvsAppIdList[0] ? this.jvsAppIdList[0].id : ''
      //   console.log(this.queryParams.jvsAppId)
      // })
    },
  },
}
</script>
<style lang="scss" scoped>
.process-manage-list {
  width: 100%;
  height: 100%;
  display: flex;
  overflow: hidden;

  /deep/ .jvs-table {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;

    .jvs-table-top {
      height: unset;

      .table-top {
        // margin-bottom: 0;
      }
    }

    .table-body-box {
      flex: 1;
      overflow: hidden;
    }
  }
}

.assign, .trans, .reduce {
  .assign-item, .trans-item, .reduce-item {
    margin-bottom: 8px;

    .label {
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
      line-height: 36px;
    }

    /deep/ .user-info-list {
      .el-input__inner {
        height: 36px;
        line-height: 36px;
        border: 0;
        background: #F5F6F7;
      }

      .el-input-group__append {
        border: 0;
      }
    }

    .cont {
      .cont-item {
        width: 100%;
        display: flex;
        align-items: center;
        margin-bottom: 10px;

        .cont-item-div {
          width: 100%;
          display: flex;
          align-items: center;

          .user-info-list {
            flex: 1;
          }

          span {
            margin-right: 10px;

            &.name {
              width: 200px;
              display: inline-block;
              height: 36px;
              line-height: 36px;
              background: #F5F6F7;
              padding: 0 15px;
              border-radius: 4px;
            }
          }
        }
      }
    }

    .reduce-item-div {
      .label {
        height: 28px;
        line-height: 28px;
      }

      .tag-list {
        /deep/ .el-tag {
          margin-right: 10px;
          margin-bottom: 10px;
        }
      }
    }
  }
}

.footer-row {
  margin-top: 20px;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mode-type-list {
  margin-right: 20px;
  width: 200px;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  box-sizing: border-box;

  .mode-type-list-item {
    padding: 0 10px;
    height: 34px;
    line-height: 34px;
    font-family: Microsoft YaHei-Regular, Microsoft YaHei;
    font-weight: 400;
    font-size: 14px;
    color: #6F7588;
    cursor: pointer;
    position: relative;
    word-break: keep-all;

    & + .extend-tab-item {
      margin-top: 10px;
    }

    &.active {
      font-weight: 700;
      color: #606266;
      background: #EFF2F7;
    }

    &:hover {
      background: #EFF2F7;
    }
  }
}
</style>
