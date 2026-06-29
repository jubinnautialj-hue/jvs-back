<template>
  <div class="run-log-page">
    <div class="title">
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#icon-jvs-rongqi"></use>
      </svg>
      <span>执行日志</span>
    </div>
    <div class="page-header-data">
      <div class="data-box">
        <div class="data-title">
          <svg class="icon" aria-hidden="true" style="fill: #1E6FFF;">
            <use xlink:href="#jvs-ui-icon-zhexiantu"></use>
          </svg>
          <span>测试执行次数</span>
        </div>
        <div class="data-number">{{ testNum | getNumber }}</div>
      </div>
      <div class="data-box">
        <div class="data-title">
          <svg class="icon" aria-hidden="true" style="fill: #FF9736;">
            <use xlink:href="#jvs-ui-icon-gudingshijian"></use>
          </svg>
          <span>定时执行次数</span>
        </div>
        <div class="data-number">{{ jobNum | getNumber }}</div>
      </div>
      <div class="data-box">
        <div class="data-title">
          <svg class="icon" aria-hidden="true" style="fill: #36B452;">
            <use xlink:href="#jvs-ui-icon-gongzhonghaoxiaoximoban"></use>
          </svg>
          <span>正式执行次数</span>
        </div>
        <div class="data-number">{{ realNum | getNumber }}</div>
      </div>
      <div class="data-box">
        <div class="data-title">
          <svg class="icon" aria-hidden="true" style="fill: #57BDFF;">
            <use xlink:href="#jvs-ui-icon-liushuihao"></use>
          </svg>
          <span>API执行次数</span>
        </div>
        <div class="data-number">{{ apiNum | getNumber }}</div>
      </div>
    </div>
    <div class="query-box">
      <el-date-picker
        v-model="dataRange"
        size="mini"
        value-format="yyyy-MM-dd HH:mm:ss"
        type="datetimerange"
        :clearable="false"
        :default-time="['00:00:00', '23:59:59']"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        prefix-icon="el-icon-date"
        @change="handleSelectDate"
      >
      </el-date-picker>
      <jvs-button type="primary" style="margin-left: 8px;" @click="handleQuery">查询</jvs-button>
      <jvs-button style="margin-left: 8px;width: 90px;" @click="clearHandle">清除日志</jvs-button>
    </div>
    <el-tabs v-model="activeName" class="rule-log-tab">
      <el-tab-pane label="统计指标" name="1">
        <div v-if="loading" class="loading-back"/>
        <div class="page-target" v-if="targetCharts && targetCharts.length > 0">
          <div class="target-chart-list" v-for="(item, key) in targetCharts" :key="key">
            <div class="target-chart-title">{{ item.name }}</div>
            <target-chart :chart-id="'targetChart' + key" :chart-data="item.index" :line-color="item.color"/>
          </div>
        </div>
        <div v-else class="page-target-none">
          <div style="text-align: center">
            <div class="no-data-img"></div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="请求列表" name="2">
        <div class="page-table">
          <jvs-table :loading="logLoading" :option="logOption" :data="logData" :page="logPage" @on-load="getList" @current-change="getList">
            <template slot="runType" slot-scope="scope">
              {{ getRunType(scope.row) }}
            </template>
            <template slot="status" slot-scope="scope">
              <el-tag size="mini" :type="scope.row.status ? '' : 'info'">{{ scope.row.status ? '成功' : '失败' }}</el-tag>
            </template>
            <template slot="menu" slot-scope="scope">
              <jvs-button size="mini" type="text" @click="handleDetail(scope.row)">详情</jvs-button>
              <jvs-button size="mini" type="text" @click="handleDelete(scope.row)">清除</jvs-button>
            </template>
          </jvs-table>
        </div>
      </el-tab-pane>
    </el-tabs>
    <el-dialog
      title="日志详情"
      class="form-fullscreen-dialog"
      append-to-body
      fullscreen
      v-if="dialogVisible"
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      :before-close="handleClose">
      <jvs-form style="padding: 0 160px;" :option="formOption" :formData="formData">
        <template slot="runTypeForm">
          <div>{{ getRunType(formData) }}</div>
        </template>
        <template slot="reqDataForm">
          <div>{{ getRunType(formData) }}</div>
        </template>
        <template slot="returnObjForm">
          <json-viewer
            style="margin-top: 10px"
            :value="formData.reqData"
            :expand-depth="2000"
            copyable
            boxed
            sort
          ></json-viewer>
        </template>
        <template slot="errorMsgForm">
          <json-viewer
            style="margin-top: 10px"
            :value="formData.errorMsg"
            :expand-depth="2000"
            copyable
            boxed
            sort
          ></json-viewer>
        </template>
        <template slot="logsForm">
          <div v-html="formData.logs"></div>
        </template>
        <template slot="resultForm">
          <json-viewer
            style="margin-top: 10px"
            :value="formData.result"
            :expand-depth="2000"
            copyable
            boxed
            sort
          ></json-viewer>
        </template>
        <template slot="statusForm" slot-scope="scope">
          {{ formData.status ? '成功' : '失败' }}
        </template>
      </jvs-form>
    </el-dialog>
  </div>
</template>

<script>
import {getLogCount, getLogInfo, pageLog, pageTarget, clearRunLogByDateTime, clearRunLogById} from "@/views/rule/api/rule";
import TargetChart from "@/views/rule/views/components/targetChart";

export default {
  name: "runLog",
  components: {TargetChart},
  props: {
    secret: {
      type: String,
      default () {
        return ''
      }
    }
  },
  filters: {
    getNumber: function (val) {
      if (!val) return '-'
      return val
    }
  },
  data () {
    return {
      loading: false,
      targetCharts: [], // 函数指标列表
      colorList: ["#409EFF", "#67C23A", "#E6A23C", "#F56C6C"],
      colorIndex: 0,
      activeName: '1',
      dataRange: null,
      formOption: {
        btnHide: true,
        disabled: true,
        column: [
          {
            label: '类型',
            prop: 'runType',
            formSlot: true
          },
          {
            label: 'tid',
            prop: 'tid',
          },
          {
            label: '执行状态',
            prop: 'status',
            formSlot: true
          },
          {
            label: '开始时间',
            prop: 'startTime',
            datetype: 'datetime',
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
          },
          {
            label: '结束时间',
            prop: 'endTime',
            datetype: 'datetime',
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
          },
          {
            label: '耗时(ms)',
            prop: 'totalExecutionTime'
          },
          {
            label: '请求参数',
            prop: 'reqData',
            formSlot: true
          },
          {
            label: '错误参数',
            prop: 'errorMsg',
            formSlot: true
          },
          {
            label: '版本号',
            prop: 'version'
          },
          {
            label: '返回值',
            prop: 'result',
            formSlot: true
          },
          {
            label: '打印日志',
            prop: 'logs',
            formSlot: true
          },
        ]
      },
      formData: {},
      dialogVisible: false,
      jobNum: undefined,
      testNum: undefined,
      realNum: undefined,
      apiNum: undefined,
      logLoading: false,
      logPage: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000], // 分页大小
        layout: "total, sizes, prev, pager, next, jumper" // 分页工具
      },
      logData: [],
      logOption: {
        addBtn: false,
        editBtn: false,
        viewBtn: false,
        delBtn: false,
        menuWidth: '80px',
        page: true,
        showOverflow: true,
        cancal: false,
        hideTop: true,
        column: [
          {
            label: '类型',
            prop: 'runType',
            slot: true
          },
          {
            label: 'tid',
            prop: 'tid',
          },
          {
            label: '执行状态',
            prop: 'status',
            slot: true,
            dicData: [
              {label: '成功', value: true},
              {label: '失败', value: false}
            ]
          },
          {
            label: '开始时间',
            prop: 'startTime',
            datetype: 'datetime',
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
          },
          {
            label: '结束时间',
            prop: 'endTime',
            datetype: 'datetime',
            format: "yyyy-MM-dd hh:mm:ss",
            valueFormat: "yyyy-MM-dd hh:mm:ss",
          },
          {
            label: '耗时(ms)',
            prop: 'totalExecutionTime'
          },
        ]
      },
    }
  },
  created() {
    if (!this.dataRange) {
      const myDate = new Date()
      const startDate = myDate.getFullYear() + '-' + ('0' + (myDate.getMonth() + 1)).slice(-2) + '-' + ('0' + myDate.getDate()).slice(-2) + ' 00:00:00'
      const endDate = myDate.getFullYear() + '-' + ('0' + (myDate.getMonth() + 1)).slice(-2) + '-' + ('0' + myDate.getDate()).slice(-2) + ' 23:59:59'
      this.dataRange = [startDate, endDate]
    }
    this.getCountData()
    this.getTarget()
  },
  methods: {
    // 查询
    handleQuery() {
      if (this.activeName === '2') {
        this.getList()
      } else {
        this.getTarget()
      }
    },
    // 获取指标
    getTarget() {
      this.loading = true
      pageTarget(this.$route.query.jvsAppId, this.secret, {
        startDate: this.dataRange[0],
        endDate: this.dataRange[1]
      }).then(res => {
        if (res.data && res.data.code == 0) {
          this.targetCharts = res.data.data ? [...res.data.data] : []
          this.targetCharts.forEach(item => {
            item.color = this.colorList[this.colorIndex]
            if (this.colorIndex < 3) {
              this.colorIndex ++
            } else {
              this.colorIndex = 0
            }
          })
          this.loading = false
        } else {
          this.loading = false
        }
      }).catch(err => {
        this.loading = false
      })
    },
    // 日期选择
    handleSelectDate(e) {
      this.dataRange = [...e]
    },
    getRunType(row) {
      let type = ''
      switch (row.runType) {
        case 'JOB':
          type = '定时任务';
          break;
        case 'TEST':
          type = '设计测试';
          break;
        case 'REAL':
          type = '正式运行';
          break;
        case 'API':
          type = 'API';
          break;
        default: break;
      }
      return type
    },
    handleClose() {
      this.dialogVisible = false
    },
    getCountData() {
      getLogCount(this.$route.query.jvsAppId, this.secret).then(res => {
        if (res.data && res.data.code == 0) {
          this.jobNum = res.data.data.JOB
          this.testNum = res.data.data.TEST
          this.realNum = res.data.data.REAL
          this.apiNum = res.data.data.API
        }
      })
    },
    getList () {
      // return
      this.logLoading = true
      pageLog(this.$route.query.jvsAppId, this.secret, {
        current: this.logPage.currentPage,
        size: this.logPage.pageSize,
        startDate: this.dataRange[0],
        endDate: this.dataRange[1],
      }).then(res => {
        if(res.data && res.data.code == 0) {
          this.logData = res.data.data.records
          this.logPage.currentPage = res.data.data.current
          this.logPage.total = res.data.data.total
          this.logLoading = false
        }else{
          this.logLoading = false
        }
      }).catch(e => {
        this.logLoading = false
      })
    },
    handleDetail(row) {
      getLogInfo(this.$route.query.jvsAppId, row.id).then(res => {
        if (res.data && res.data.code == 0) {
          this.formData = JSON.parse(JSON.stringify(res.data.data))
          this.dialogVisible = true
        }
      })
    },
    clearHandle () {
      this.$confirm('此操作将清除查询时间段的所有日志, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let param = {
          startDate: this.dataRange[0],
          endDate: this.dataRange[1],
        }
        clearRunLogByDateTime(this.$route.query.jvsAppId, this.secret, param).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '清除日志成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.handleQuery()
          }
        })
      }).catch(() => {});
    },
    handleDelete (row) {
      this.$confirm('此操作将删除该条日志, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        clearRunLogById(this.$route.query.jvsAppId, this.secret, row.id).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: '提示',
              message: '清除日志成功',
              position: 'bottom-right',
              type: 'success'
            })
            this.handleQuery()
          }
        })
      }).catch(() => {});
    }
  }
}
</script>

<style lang="scss" scoped>
.run-log-page{
  padding: 24px;
  background-color: #ffffff;
  border-radius: 4px;
  .title {
    display: flex;
    align-items: center;
    svg{
      width: 16px;
      height: 16px;
      margin-right: 8px;
    }
    span{
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 16px;
      color: #363B4C;
      line-height: 21px;
    }
  }
  .page-header-data{
    margin-top: 16px;
    display: flex;
    align-items: center;
    height: 88px;
    background: #F5F6F7;
    border-radius: 4px;
    .data-box{
      width: 25%;
      text-align: left;
      padding: 0 24px;
      margin: 15px 0;
      box-sizing: border-box;
      .data-title{
        display: flex;
        align-items: center;
        span{
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
          line-height: 18px;
        }
        svg{
          width: 16px;
          height: 16px;
          margin-right: 4px;
        }
      }
      .data-number{
        margin-top: 8px;
        font-family: Source Han Sans-Medium, Source Han Sans;
        font-weight: 500;
        font-size: 24px;
        color: #363B4C;
        line-height: 32px;
      }
      &+.data-box{
        border-left: 1px solid #D7D8DB;
      }
    }
  }
  .query-box{
    margin-top: 24px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    height: 32px;
    /deep/.el-date-editor{
      width: 325px;
      height: 32px;
      .el-range__close-icon{
        display: none;
      }
      .el-icon-date{
        position: absolute;
        right: 9px;
        top: 0;
      }
    }
    /deep/.el-button{
      width: 76px;
      height: 32px;
      background: #E4EDFF;
      border-radius: 4px;
      border: 1px solid #1E6FFF;
      color: #1E6FFF;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
    }
  }
  .rule-log-tab{
    /deep/.el-tabs__nav-wrap::after{
      height: 1px;
    }
  }
  .page-target{
    overflow-y: auto;
    height: calc(100vh - 344px);
    background-color: #ffffff;
    width: 100%;
    display: grid;
    grid-template-columns: calc(50% - 16px) calc(50% - 16px);
    grid-row-gap: 16px;
    grid-column-gap: 16px;
    &::-webkit-scrollbar{
      display: none;
    }
    .target-chart-list{
      width: 100%;
      padding: 16px 24px;
      height: 303px;
      background: linear-gradient( 180deg, #F5F6F7 0%, rgba(255,255,255,0) 97%);
      border-radius: 4px;
      border: 1px solid #EEEFF0;
      box-sizing: border-box;
      .target-chart-title{
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 16px;
        color: #363B4C;
        line-height: 21px;
        margin-bottom: 16px;
      }
    }
  }
  .page-target-none{
    height: calc(100vh - 344px);
    background-color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    .no-data-img{
      width: 457px;
      height: 180px;
      background-image: url(/jvs-ui/static/img/emptyImage.ca3665f2.png);
      background-size: 260px 123px;
      background-repeat: no-repeat;
      background-position: center;
    }
  }
  .loading-back{
    position: absolute;
    width: 100%;
    height: calc(100vh - 344px);
    box-sizing: border-box;
    background-color: rgba(255, 255, 255, 0.8);
    background-image: url('../../../../../public/jvs-ui-public/img/loading.gif');
    background-repeat: no-repeat;
    background-position: center;
    background-position: center;
    //background-size: 200px 160px;
  }
  /deep/.page-table{
    width: 100%;
    .jvs-table .table-body-box .el-table .el-table__body-wrapper{
      height: calc(100vh - 460px)!important;
      width: 100%;
    }
  }
}
</style>
