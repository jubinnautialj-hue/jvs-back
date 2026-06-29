<template>
  <div class="org-logout-page">
    <div class="org-tab-list">
      <div :class="{'org-tab-item': true, 'active': activeType == 'out'}" @click="changeType('out')">{{$langt('orglogout.cancel')}}</div>
      <div :class="{'org-tab-item': true, 'active': activeType == 'transform'}" @click="changeType('transform')">{{$langt('orglogout.trans')}}</div>
    </div>
    <div v-if="activeType == 'out'" class="org-box out">
      <div class="content-icon">
        <img src="./img/out.png" alt="">
      </div>
      <div class="content-text">
        <div class="title">{{$langt('orglogout.cancelTip1')}}</div>
        <div class="text">{{$langt('orglogout.cancelTip2')}}</div>
      </div>
      <div class="footer-btn">
        <jvs-button type="primary" permisionFlag="jvs_unregister_organization" @click="handleTenantLogout">{{$langt('orglogout.diss')}}</jvs-button>
      </div>
    </div>
    <div v-if="activeType == 'transform'" class="org-box">
      <div class="content-icon">
        <img src="./img/transform.png" alt="">
      </div>
      <div class="content-text">
        <div class="title">{{$langt('orglogout.transTip1')}}</div>
        <div class="text">{{$langt('orglogout.transTip2')}}</div>
      </div>
      <div class="footer-btn">
        <jvs-button type="primary" @click="transTenant">{{$langt('orglogout.trans')}}</jvs-button>
      </div>
    </div>
    <!-- 选择转交人 -->
    <userSelector
      ref="userSelector"
      :userEnable="true"
      :isRadio="true"
      @submit="addCheckUSer">
    </userSelector>
  </div>
</template>

<script>
import {tenantLogout} from "@/views/upms/views/orgLogout/api";
import {loginoutHandle} from "@/api/admin/home";
import UserSelector from "@/components/basic-assembly/userSelector";
import { editTenant } from "@/views/upms/views/inviteUser/api"
export default {
  name: "orgLogout",
  components: {
    UserSelector
  },
  data () {
    return {
      activeType: 'out'
    }
  },
  created() {
  },
  methods: {
    changeType (tab) {
      this.activeType = tab
      this.$forceUpdate()
    },
    // 注销组织
    handleTenantLogout() {
      this.$confirm(this.$langt('orglogout.confirm'), this.$langt('common.tip'), {
        confirmButtonText: this.$langt('common.confirm'),
        cancelButtonText: this.$langt('common.cancel'),
        type: "warning"
      }).then(() => {
        tenantLogout().then(res => {
          if (res.data && res.date.code == 0) {
            this.handleLogout()
          }
        })
      })
    },
    // 退出登录
    handleLogout() {
      loginoutHandle().then(res => {
        if(res.data.code == 0) {
          let path = this.$store.state.common.template || '/login'
          sessionStorage.clear()
          this.$store.dispatch("LogOut")
          this.$router.push({ path: path })
        }
      })
    },
    transTenant () {
      this.$refs.userSelector.openDialog()
    },
    addCheckUSer (checkList) {
      if(checkList && checkList.length > 0) {
        this.$confirm(this.$langt('orglogout.transConfirm'), this.$langt('common.tip'), {
          confirmButtonText: this.$langt('common.confirm'),
          cancelButtonText: this.$langt('common.cancel'),
          type: "warning"
        }).then(() => {
          editTenant({adminUserId: checkList[0].id}).then(res => {
            if(res.data && res.data.code == 0) {
              this.$notify({
                title: this.$langt('common.tip'),
                message: this.$langt('orglogout.transSuccess'),
                position: 'bottom-right',
                type: 'success'
              });
              this.handleLogout()
              this.$refs.userSelector.closeDialog()
            }
          })
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.org-logout-page {
  position: relative;
  display: flex;
  flex-direction: column;
  .org-tab-list{
    width: 100%;
    border-bottom: 1px solid #EEEFF0;
    padding: 0 24px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    .org-tab-item{
      margin-right: 40px;
      padding: 16px 0;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #6F7588;
      cursor: pointer;
      position: relative;
      &.active{
        font-family: Microsoft YaHei-Bold, Microsoft YaHei;
        font-weight: 700;
        font-size: 14px;
        color: #1E6FFF;
      }
      &.active::after{
        content: '';
        width: 100%;
        height: 2px;
        background: #1E6FFF;
        border-radius: 2px 0px 2px 0px;
        position: absolute;
        bottom: -1px;
        left: 0;
      }
    }
  }
  .org-box{
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }
  .content-text{
    text-align: center;
    margin: 24px 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    .title{
      height: 21px;
      line-height: 21px;
      font-family: Microsoft YaHei-Bold, Microsoft YaHei;
      font-weight: 700;
      font-size: 16px;
      color: #363B4C;
    }
    .text{
      margin-top: 16px;
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #6F7588;
      line-height: 24px;
    }
  }
  .content-icon{
    text-align: center;
    img{
      display: block;
      width: 120px;
      height: 120px;
    }
  }
  .out{
    .content-text{
      .text{
        width: 476px;
      }
    }
  }
  .footer-btn{
    text-align: center;
    /deep/.el-button{
      min-width: 128px;
      height: 36px;
      background: #1E6FFF;
      border-radius: 6px 6px 6px 6px;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 14px;
      color: #FFFFFF;
    }
  }
}
</style>
