<template>
  <div class="system-basic-info-page">
    <div class="system-info-page">
      <div class="title">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-jvs-rongqi"></use>
        </svg>
        <span>{{$langt('systeminfo.basic')}}</span>
      </div>
      <div class="basic-info">
        <div class="left-info">
          <div class="icon-img" v-if="basicDataInfo && basicDataInfo.tenant && basicDataInfo.tenant.icon">
            <img :src="basicDataInfo.tenant.icon" alt=""/>
          </div>
          <div class="tenant-info">
            <div class="tenant-title">{{basicDataInfo && basicDataInfo.tenant && basicDataInfo.tenant.shortName}}</div>
            <div class="tenant-other">
              <div>
                <h4>{{$langt('systeminfo.orgName')}}</h4>
                <p v-if="basicDataInfo && basicDataInfo.tenant">{{basicDataInfo.tenant.loginDomain || '-'}}</p>
              </div>
              <div>
                <h4>{{$langt('systeminfo.comName')}}</h4>
                <p v-if="basicDataInfo && basicDataInfo.tenant" class="editable">
                  <span v-if="!showEdit" @dblclick="editTextHandle(basicDataInfo.tenant.shortName)">{{basicDataInfo.tenant.shortName}}</span>
                  <i v-if="!showEdit" class="el-icon-edit-outline" @click="editTextHandle(basicDataInfo.tenant.shortName)"></i>
                  <el-input v-if="showEdit" v-model="editText" size="mini" style="flex: 1;height: 18px;" @blur="editTextSubmit"></el-input>
                </p>
              </div>
              <div>
                <h4>{{$langt('systeminfo.orgAdmin')}}</h4>
                <p v-if="basicDataInfo && basicDataInfo.tenant">{{basicDataInfo.tenant.adminUserName || '-'}}</p>
              </div>
            </div>    
          </div>
          <!-- <div class="link-list">
            <div class="link-item" @click="openTabRouter('用户管理', '/jvs-upms-ui/department')">
              <i class="el-icon-user-solid"></i>
              <span>添加成员</span>
            </div>
            <div class="link-item" @click="openTabRouter('角色管理', '/jvs-upms-ui/role')">
              <i class="el-icon-user-solid"></i>
              <span>创建角色</span>
            </div>
            <div class="link-item" @click="openTabRouter('角色管理', '/jvs-upms-ui/role')">
              <i class="el-icon-user-solid"></i>
              <span>管理员权限</span>
            </div>
          </div> -->
        </div>
        <div class="split-line"></div>
        <div class="right-info">
          <div class="basic-data-info">
            <div class="info-item">
              <div class="title">{{$langt('systeminfo.entNumber')}}</div>
              <div class="number">{{ basicDataInfo.users }}</div>
            </div>
            <div class="info-item">
              <div class="title">{{$langt('systeminfo.teamNumber')}}</div>
              <div class="number">{{ basicDataInfo.groups }}</div>
            </div>
            <div class="info-item">
              <div class="title">{{$langt('systeminfo.deptNumber')}}</div>
              <div class="number">{{ basicDataInfo.depts }}</div>
            </div>
            <div class="info-item">
              <div class="title">{{$langt('systeminfo.userNumber')}}</div>
              <div class="number">{{ basicDataInfo.yesterdays }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="(resourceList && resourceList.length > 0) || (linkList && linkList.length > 0)" class="system-info-page-item system-info-page-resource">
      <div v-if="resourceList && resourceList.length > 0" class="res-item" :style="(linkList && linkList.length > 0) ? 'margin-right: 16px;width: 724px;' : 'flex: 1;'">
        <div class="title">
          <svg class="icon" aria-hidden="true">
            <use xlink:href="#icon-jvs-rongqi"></use>
          </svg>
          <span>{{$langt('systeminfo.capacity')}}</span>
        </div>
        <div class="resource">
          <div v-for="(item, key) in resourceList" :key="key" class="resource-item">
            <div class="name">{{ item.name }}</div>
            <div class="size">{{ item.size }}</div>
          </div>
        </div>
      </div>
      <div v-if="linkList && linkList.length > 0" class="link-item">
        <div class="title">
          <svg class="icon" aria-hidden="true">
            <use xlink:href="#icon-jvs-rongqi"></use>
          </svg>
          <span>{{$langt('systeminfo.manage')}}</span>
        </div>
        <div class="other-link-list">
          <div v-for="(item, index) in linkList" :key="'link-item-'+index" class="other-link-list-item" @click="enterLink(item)">
            <img :src="item.iconUrl" alt="">
            <div class="info">
              <h4>{{item.name}}</h4>
              <span>{{item.desc}}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="system-info-page-item system-info-page-chart" style="overflow: hidden;">
      <div class="title">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-jvs-rongqi"></use>
        </svg>
        <span>{{$langt('systeminfo.seven')}}</span>
      </div>
      <div class="chart-box">
        <div id='userChart'></div>
      </div>
    </div>
  </div>
</template>

<script>
import { getDynamicDesign, getDynamicResource } from '@/api/admin/home'
import {getResourceList, getBasicDataInfo} from "@/views/upms/views/systemInfo/api";
import { mapGetters } from "vuex";
import { Line } from '@antv/g2plot';
import { editTenant } from '@/views/upms/views/inviteUser/api'
export default {
  name: "systemInfo",
  computed: {
    ...mapGetters(["userInfo"]),
  },
  components: {
  },
  data () {
    return {
      basicDataInfo: {},
      resourceList: [],
      linkList: [],
      chart: null,
      legends: [],
      showEdit: false,
      editText: ''
    }
  },
  async created() {
    await this.getDynamicDesign()
    this.getBasicDataInfo()
    this.getDynamicResource()
  },
  mounted () {
    window.onresize = () => {
      if(this.basicDataInfo.trend && this.basicDataInfo.trend.length > 0) {
        this.initChart(this.basicDataInfo.trend)
      }
    };
  },
  methods: {
    getBasicDataInfo() {
      getBasicDataInfo().then(res => {
        this.basicDataInfo = JSON.parse(JSON.stringify(res.data.data))
        if(this.basicDataInfo.trend && this.basicDataInfo.trend.length > 0) {
          this.initChart(this.basicDataInfo.trend)
        }
      })
    },
    getResourceList() {
      getResourceList().then(res => {
        this.resourceList = res.data.data
        this.$nextTick(() => {
          if(this.basicDataInfo.trend && this.basicDataInfo.trend.length > 0) {
            this.initChart(this.basicDataInfo.trend)
          }
        })
      })
    },
    async getDynamicDesign () {
      await getDynamicDesign().then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          if(res.data.data.JVS_DESIGN_MGR) {
            this.getResourceList()
          }
        }
      })
    },
    getDynamicResource () {
      getDynamicResource().then(res => {
        if(res.data && res.data.code == 0) {
          this.linkList = res.data.data
        }
      })
    },
    enterLink (item) {
      this.$openUrl(`${item.url}`, '_blank')
    },
    enterRoute (item) {
      this.$router.push({
        path: this.$router.$jvsRouter.getPath({
          name: '系统设置',
          src: '/jvs-upms-ui/systemconfig'
        })
      });
    },
    initChart(chartData) {
      this.chart && this.chart.destroy() //this.chart存在就销毁重新绘制chart
      this.chart = new Line(document.getElementById('userChart'), {
        padding: 'auto',
        forceFit: true,
        height: $('.system-info-page-chart').height() - 48,
        data: chartData,
        xField: 'date',
        yField: 'count',
        legend: {
          position: 'bottom',
        },
        seriesField: 'client',
        responsive: true,
      })
      this.chart.render()
    },
    updateChartData(chartData) {
      this.chart.changeData(chartData)
    },
    openTabRouter (name, path) {
      this.$router.push({
        path: this.$router.$jvsRouter.getPath({
          name: name,
          src: path
        })
      })
    },
    editTextHandle (str) {
      this.editText = str
      this.showEdit = true
    },
    editTextSubmit () {
      if(this.editText != this.basicDataInfo.tenant.shortName) {
        editTenant({
          id: this.basicDataInfo.tenant.id,
          shortName: this.editText
        }).then(res => {
          if(res.data && res.data.code == 0) {
            this.$notify({
              title: this.$langt('common.tip'),
              message: this.$langt('common.editSuccess'),
              position: 'bottom-right',
              type: 'success'
            });
            this.$set(this.basicDataInfo.tenant, 'shortName', this.editText)
            this.showEdit = false
            this.editText = ''
          }
        })
      }else{
        this.showEdit = false
        this.editText = ''
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.system-basic-info-page{
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  padding-right: 24px;
  box-sizing: border-box;
  background-color: #F5F6F7;
  .title{
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    .icon{
      width:16px;
      height: 16px;
      margin-right: 8px;
    }
    span{
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 16px;
      color: #363B4C;
    }
  }
}
.system-basic-info-page::-webkit-scrollbar{
  display: none;
}
.system-info-page{
  border-radius: 4px;
  background-color: #fff;
  padding: 24px;
  box-sizing: border-box;
  .basic-info{
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background-image: url(./img/image.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
    height: 120px;
    border-radius: 4px;
    padding: 0 24px;
    box-sizing: border-box;
    position: relative;
    .split-line{
      width: 1px;
      height: 64px;
      background: #FFFFFF;
      top: 28px;
      left: 50%;
    }
    .left-info{
      flex: 1;
      display: flex;
      align-items: center;
      .icon-img{
        width: 56px;
        height: 56px;
        background: #FFFFFF;
        border-radius: 6px;
        padding: 9px;
        overflow: hidden;
        box-sizing: border-box;
        img{
          display: block;
          width: 38px;
          height: 38px;
        }
      }
      .tenant-info{
        margin-left: 16px;
        .tenant-title{
          font-family: Microsoft YaHei-Bold, Microsoft YaHei;
          font-weight: 700;
          font-size: 16px;
          color: #363B4C;
        }
        .tenant-other{
          display: flex;
          align-items: center;
          margin-top: 12px;
          div{
            display: flex;
            align-items: center;
            h4, p{
              margin: 0;
              padding: 0;
            }
            h4{
              font-family: Source Han Sans-Regular, Source Han Sans;
              font-weight: 400;
              font-size: 14px;
              color: #6F7588;
            }
            p{
              font-family: Source Han Sans-Regular, Source Han Sans;
              font-weight: 400;
              font-size: 14px;
              color: #363B4C;
            }
            &+div{
              margin-left: 32px;
            }
          }
        }
        .editable{
          display: flex;
          align-items: center;
          span{
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: pre;
          }
          i{
            cursor: pointer;
            margin-left: 5px;
          }
        }
      }
    }
    .right-info{
      flex: 1;
      padding-left: 72px;
      .basic-data-info{
        display: flex;
        align-items: center;
        .info-item{
          width: 25%;
          text-align: left;
          .title{
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 14px;
            color: #6F7588;
            margin-bottom: 8px;
          }
          .number{
            height: 35px;
            font-family: Source Han Sans-Bold, Source Han Sans;
            font-weight: 700;
            font-size: 24px;
            color: #363B4C;
            line-height: 35px;
          }
        }
      }
    }
  }
}
.system-info-page-item{
  margin-top: 16px;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}
.system-info-page-resource{
  display: flex;
  justify-content: space-between;
  .res-item{
    background-color: #fff;
    padding: 24px 8px 8px 24px;
    border-radius: 4px;
    overflow: hidden;
    .resource{  
      display: flex;
      flex-wrap: wrap;
      height: 260px;
      overflow: hidden;
      overflow-y: auto;
      .resource-item{
        width: 220px;
        height: 114px;
        padding: 0 16px;
        margin-right: 16px;
        margin-bottom: 16px;
        background: linear-gradient( 180deg, #F5F6F7 0%, rgba(245,246,247,0) 100%);
        border-radius: 4px;
        border: 1px solid #EEEFF0;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .name{
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          font-size: 14px;
          color: #363B4C;
        }
        .size{
          font-family: Source Han Sans-Bold, Source Han Sans;
          font-weight: 700;
          font-size: 28px;
          color: #363B4C;
        }
      }
    }
  }
  .link-item{
    background-color: #fff;
    padding: 24px 8px 8px 24px;
    flex: 1;
    border-radius: 4px;
    overflow: hidden;
    .other-link-list{
      width: 100%;
      height: 260px;
      display: flex;
      flex-wrap: wrap;
      box-sizing: border-box;
      overflow: hidden;
      overflow-y: auto;
      .other-link-list-item{
        margin-right: 16px;
        margin-bottom: 16px;
        padding: 16px;
        height: 72px;
        background: #F5F6F7;
        border-radius: 4px;
        display: flex;
        align-items: center;
        width: calc(50% - 16px);
        box-sizing: border-box;
        cursor: pointer;
        img{
          display: block;
          width: 40px;
          height: 40px;
        }
        .info{
          height: 40px;
          margin-left: 16px;
          h4{
            margin: 0;
            padding: 0;
            height: 20px;
            font-family: Source Han Sans-Medium, Source Han Sans;
            font-weight: 500;
            font-size: 14px;
            color: #363B4C;
            line-height: 20px;
          }
          span{
            margin-top: 4px;
            height: 17px;
            font-family: Source Han Sans-Regular, Source Han Sans;
            font-weight: 400;
            font-size: 12px;
            color: #6F7588;
            line-height: 17px;
          }
        }
      }
    }
  }
}

.system-info-page-chart{
  flex: 1;
  box-sizing: border-box;
  margin-top: 16px;
  background-color: #fff;
  padding: 24px;
  padding-bottom: 0;
  margin-bottom: 16px;
  box-sizing: border-box;
}
</style>
