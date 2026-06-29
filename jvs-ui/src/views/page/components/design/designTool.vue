<template>
  <div class="tool-box">
    <design-tool :title="'表单设计'" :needLoading="true" :dataModelId="dataModelId" :jvsAppId="jvsAppId" :designId="designId" :dataModelFields="dataModelFields" :fresh="saveLoading">
      <template slot="right">
        <p @click="qingkong()">
          <el-popover placement="bottom" trigger="hover" content="清空" popper-class="custom-right-tool-poper">
            <div slot="reference" class="setting-icon">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-a-zu49531"></use>
              </svg>
            </div>
          </el-popover>
        </p>
        <div class="divider-line"></div>
        <p class="marginRight morethan">
          <el-popover placement="bottom" trigger="hover" content="排列" popper-class="custom-right-tool-poper">
            <div class="form-layout-list">
              <div class="form-layout-list-item" @click="getColumnNum(24)">
                <svg class="icon" aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-yilie"></use>
                </svg>
                <span>一列</span>
              </div>
              <div class="form-layout-list-item" @click="getColumnNum(12)">
                <svg class="icon" aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-lianglie"></use>
                </svg>
                <span>两列</span>
              </div>
              <div class="form-layout-list-item" @click="getColumnNum(8)">
                <svg class="icon" aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-sanlie"></use>
                </svg>
                <span>三列</span>
              </div>
              <div class="form-layout-list-item" @click="getColumnNum(6)">
                <svg class="icon" aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-silie"></use>
                </svg>
                <span>四列</span>
              </div>
            </div>
            <div slot="reference" class="setting-icon setting-icon-morethan">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-pailie"></use>
              </svg>
              <svg class="more-icon" aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-a-zu6027"></use>
              </svg>
            </div>
          </el-popover>
        </p>
        <p class="marginRight" @click="connectionHandle">
          <el-popover placement="bottom" trigger="hover" content="引用" popper-class="custom-right-tool-poper">
            <div slot="reference" class="setting-icon">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-lianjie1"></use>
              </svg>
            </div>
          </el-popover>
        </p>
        <p class="marginRight" @click="toolClick('json')">
          <el-popover placement="bottom" trigger="hover" content="数据结构" popper-class="custom-right-tool-poper">
            <div slot="reference" class="setting-icon">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#jvs-ui-icon-shujujiegou"></use>
              </svg>
            </div>
          </el-popover>
        </p>
        <div class="divider-line"></div>
        <div class="merge-box">
          <p @click="toolClick('preview')">
            <el-popover placement="bottom" trigger="hover" content="预览" popper-class="custom-right-tool-poper">
              <div slot="reference" class="setting-icon">
                <svg class="icon" aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-yulan"></use>
                </svg>
              </div>
            </el-popover>
          </p>
          <div class="divider-line"></div>
          <p>
            <el-popover placement="bottom" width="140" trigger="hover">
              <span id="qrcode" ref="qrcode" style="display:flex;justify-content:center;margin-top:10px;"></span>
              <el-button style="width: 100%;font-size: 14px" size="mini" type="text">移动端预览</el-button>
              <div slot="reference" class="setting-icon">
                <svg class="icon" aria-hidden="true">
                  <use xlink:href="#jvs-ui-icon-shoujiyulan"></use>
                </svg>
              </div>
            </el-popover>
          </p>
        </div>
      </template>
    </design-tool>
  </div>
</template>
<script>
import saveicon from "@/const/img/保存.png"
import titlePageHeader from '@/components/page-header/titlePageHeader'
import designTool from '@/components/design-tool/DesignTool'
import Permission from "../../components/design/permission";
import QRCode from 'qrcodejs2'
export default {
  name: 'design-bar',
  components: {titlePageHeader, designTool, Permission},
  props: {
    dataModelId: {
      type: String
    },
    jvsAppId: {
      type: String
    },
    designId: {
      type: String
    },
    mobileCode: {
      type: String
    },
    // 是否为列表页详情按钮
    isDetail: {
      type: Boolean,
      default: () => {
        return false
      }
    },
    formType: {
      type: String
    },
    dataModelFields: {
      type: Array
    },
    saveLoading: {
      type: Boolean
    }
  },
  data () {
    return {
      saveIcon: saveicon,
      role: [], // 权限设置
      roleType: true, // 权限类型,true 应用 权限，false 自定义权限
      operationList: [], // 操作权限list
      permissionVisible: false, // 权限弹框
      deptId: [],
      dialogVisible: false,
      submitLoading: false, // 保存loading
    }
  },
  created () {
  },
  mounted () {
    if(this.$refs.qrcode) {
      this.$refs.qrcode.innerHTML = ''; //清除二维码方法一
      let loc = location.origin
      let text = `${loc}/jvsCom/form/use?id=${this.designId}&dataModelId=${this.dataModelId}&jvsAppId=${this.jvsAppId}`
      var qrcode = new QRCode(this.$refs.qrcode, {
        text: text, //页面地址 ,如果页面需要参数传递请注意哈希模式#
        width: 120,
        height: 120,
        colorDark: '#000000',
        colorLight: '#ffffff',
        correctLevel: QRCode.CorrectLevel.H,
      })
    }
  },
  methods: {
    // 下载二维码
    handleDownloadCode() {
      if (this.mobileCode) {
        this.downloadImage(this.mobileCode, '二维码.png')
      }
    },
    // imageSrc 下载图片的链接
    // name 图片的名称
    downloadImage (imageSrc, name) {
      let image = new Image()
      // 告知请求的服务器 进行跨域请求
      image.setAttribute('crossOrigin', 'anonymous')
      image.src = imageSrc
      image.onload = function () {
        let canvas = document.createElement('canvas')
        canvas.width = image.width
        canvas.height = image.height
        let context = canvas.getContext('2d')
        context.drawImage(image, 0, 0, image.width, image.height)
        let url = canvas.toDataURL('image/png')
        let a = document.createElement('a')
        a.href = url
        a.download = name || 'photo'
        a.click()
      }
    },
    toolClick(str) {
      this.$root.eventBus.$emit('toolEvent', str)
    },
    // 表单设计 列数改变
    getColumnNum(num) {
      this.$emit('getColumnNum', num)
    },
    // 表单设计 清空
    qingkong() {
      this.$emit('qingkong')
    },
    // 引用
    connectionHandle () {
      this.$emit('connection')
    }
  }
}
</script>
<style lang="scss" scoped>
p{
  width: 28px;
  height: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0;
  background: #fff;
  border-radius: 4px;
  &.marginRight{
    margin-right: 8px;
  }
  &.morethan{
    min-width: 28px;
    padding: 0 8px;
  }
  &:hover{
    svg{
      fill: #1E6FFF;
    }
  }
}
.divider-line{
  width: 1px;
  height: 20px;
  background: #C2C5CF;
  margin: 0 16px;
}
.merge-box{
  padding: 0 2px;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 4px;
  .divider-line{
    height: 16px;
    margin: 0 8px;
  }
}
.setting-icon{
  svg{
    width: 16px;
    height: 16px;
    fill: #363B4C;
  }
}
.setting-icon-morethan{
  display: flex;
  align-items: center;
  .more-icon{
    width: 12px;
    height: 12px;
  }
}
.form-layout-list{
  height: 160px;
  .form-layout-list-item{
    width: 92px;
    height: 32px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    padding: 0 4px;
    box-sizing: border-box;
    cursor: pointer;
    svg{
      width: 16px;
      height: 16px;
      fill: #363B4C;
    }
    span{
      margin-left: 5px;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 14px;
      color: #363B4C;
    }
    &:hover{
      background: #DDEAFF;
      span{
        color: #1E6FFF;
      }
      svg{
        fill: #1E6FFF;
      }
    }
    &+.form-layout-list-item{
      margin-top: 10px;
    }
  }
}
</style>
