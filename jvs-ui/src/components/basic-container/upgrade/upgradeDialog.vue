<template>
  <el-dialog
    :visible.sync="dialogVisible"
    class="upgrade-dialog"
    width="500px"
    :close-on-press-escape="false"
    :before-close="handleClose">
    <div class="upgrade-box">
      <div class="upgrade-msg">
        <div class="title">温馨提示</div>
        <div class="content">{{ message }}</div>
      </div>
    </div>
    <div class="footer-btn">
      <el-button type="danger" size="medium" @click="handleUpgrade">立即升级</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "upgradeDialog",
  data() {
    return {
      dialogVisible: false,
    };
  },
  computed: {
    // message () {
    //   return this.message
    // },

  },
  methods: {
    init() {
      this.dialogVisible = true
    },
    handleClose() {
      this.dialogVisible = false
    },
    handleUpgrade() {
      this.$openUrl('/#/auth','_blank')
    },
  }
}
</script>

<style lang="scss" scoped>
.upgrade-dialog/deep/{
  .upgrade-box{
    position: relative;
    display: flex;
    //align-items: center;
    justify-content: center;
    //background-color: rgba(255, 255, 255, 0);
    width: 100%;
    height: 100%;
    background-image: url(./upgrade.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
    background-position: center;
    .upgrade-msg{
      margin-top: 35%;
      width: 100%;
      padding: 0 50px;
      .title{
        font-size: 18px;
        line-height: 40px;
      }
      .content{
        padding: 0 16px;
        height: 90px;
        overflow-y: auto;
      }
    }
  }
  .el-dialog{
    background-color: rgba(255, 255, 255, 0);
    box-shadow: none;
  }
  .el-dialog__header{
    display: none!important;
  }
  .el-dialog__body{
    border-radius: 8px!important;
    padding: 0!important;
    height: 400px;
  }
  .footer-btn{
    width: 100%;
    position: absolute;
    bottom: 50px;
    text-align: center;
    .el-button--danger{
      background-color: red;
    }
  }
}
</style>
