<template>
  <el-popover
    ref="popoverRef"
    v-model="iconVisible"
    :show-arrow="false"
    trigger="click"
    width="474"
    popper-class="select-icon-popover-box"
  >
    <div class="select-icon-popover-body">
      <div class="select-icon-top">
        <div class="select-icon-type">
          <div class="select-icon-type-item" :class="[iconType=='material' && 'type-active']" @click="changeType('material')">使用素材</div>
          <div class="select-icon-type-item" v-if="isUseUpload" :class="[iconType=='custom' && 'type-active']" @click="changeType('custom')">自定义图片</div>
        </div>
      </div>
      <div class="select-icon-contianer">
        <div class="back-img-upload-box" v-if="iconType=='custom'">
          <el-upload
            action="/mgr/jvs-auth/upload/jvs-public"
            :data="paramsData"
            :headers="headers"
            :on-success="handleSuccess"
            :show-file-list="false"
            :before-upload="beforeUpload"
            accept=".jpg,.png,.gif,.jpge">
            <div class="upload-box-button">
              <svg class="upload-icon">
                <use xlink:href="#bi-a-zu5758"></use>
              </svg>
              <div class="upload-text">上传本地图片</div>
            </div>
          </el-upload>
          <div class="uploda-tips">支持jpg、png、gif、jpge格式，不超过2MB</div>
        </div>
        <el-scrollbar ref="containerScrollRef" :max-height="iconType == 'custom' ? 'calc(216px - 80px)': ''" :style="{height: iconType == 'custom' ? '' : '100%'}">
          <div class="contianer-scroll-box" :class="[iconType=='custom' && 'img-scroll-box']">
            <template v-if="iconType=='material'">
              <div class="select-icon-list-box" v-for="(item,index) in iconList" :key="index">
                <div class="select-icon-title">{{item.label}}</div>
                <div class="select-icon-list-box">
                  <div class="select-icon-list-item" :class="[cItem==iconOpt.value && 'icon-active']" v-for="(cItem,cIndex) in item.list" :key="cIndex" @click="setIcon(cItem)">
                    <i class="select-iconfont" :class="cItem"></i>
                  </div>
                </div>
              </div>
            </template>
            <template  v-if="iconType=='custom'">
              <div class="contianer-scroll-item" :class="[item==iconOpt.backImg &&'contianer-scroll-item-active']" v-for="(item,index) in uploadIconImgList" :key="index"
                @click="changeBackItem(item)">
                <img class="img-item" :src="item">
                <div class="img-action-box" v-if="iconType=='custom'" @click.stop="deleteBackImg(item,index)">
                  <i class="bi-iconfont bi-shanchu"></i>
                </div>
              </div>
            </template>
          </div>
        </el-scrollbar>
        <div v-if="iconType == 'custom' && imgListOptions && imgListOptions.length > 0" class="back-img-list">
          <img v-for="(img, key) in imgListOptions" :key="key" :src="img" alt="" @click="selectBackImg(img)"/>
        </div>
      </div>
      <div class="select-icon-bottom" v-if="iconType=='custom'">
        <div>显示位置</div>
        <el-popover v-model="isClickInPop" ref="posPopoverRef" placement="bottom" width="136" trigger="click"  :show-arrow="false" popper-class="position-popover-box">
          <div class="drop-position-box">
            <div class="position-item" v-for="(item,index) in posList" :key="index" :class="[iconOpt.pos==item.value && 'active']" @click="changePos(item.value)">{{item.label}}</div>
          </div>
          <template #reference>
            <div class="img-position-box">
              <span>{{getLabel}}</span>
              <svg class="arrow-icon">
                <use xlink:href="#jvs-public-shouqi1"></use>
              </svg>
            </div>
          </template>
        </el-popover>
        <div class="action-item" @click="changeHorizontalFlip">
          <svg class="action-icon">
            <use xlink:href="#bi-shuipingfanzhuan"></use>
          </svg>
        </div>
        <div class="action-item" @click="changeVerticalFlip">
          <svg class="action-icon">
            <use xlink:href="#bi-chuizhifanzhuan"></use>
          </svg>
        </div>
        <div class="action-item" @click="changeRotate">
          <svg class="action-icon">
            <use xlink:href="#bi-xuanzhuan"></use>
          </svg>
        </div>
        <div class="reset-action" @click="resetDefault">恢复默认</div>
      </div>
    </div>
    <div slot="reference" class="custom-select-icon-com" ref="iconButtonRef" :style="{width:width}">
      <div class="icon-div-box">
        <i class="select-iconfont" :class="iconOpt.value" v-if="iconType=='material' || !iconOpt.backImg"></i>
        <img class="select-img" :src="iconOpt.backImg" v-if="iconType=='custom' && iconOpt.backImg">
      </div>
      <svg class="arrow-icon">
        <use xlink:href="#bi-xiala1"></use>
      </svg>
    </div>
  </el-popover>
</template>

<script>
import { iconList } from './iconfont'
import { Notification } from 'element-ui'
import { cloneDeep,isEqual } from 'lodash'
const defaulIconOption = {
  value:'',
  backImg:'',
  type:"material",
  pos:'center',
  verticalFlip:false,
  horizontalFlip:false,
  rotate:0
}
export default {
  name: 'jvsSelectIcon',
  props: {
    isUseUpload: {
      type: Boolean,
      default: true
    },
    isDesign: {
      type: Boolean,
      default: true
    },
    iconOption:{
      type: Object,
      default() {
        return {}
      }
    },
    width: {
      type: String,
      default: "130px"
    },
    imgListOptions: Array
  },
  computed: {
    getLabel () {
      let str = ''
      this.posList.forEach((item,index) => {
        if(item.value == this.iconOpt.pos){
          str = item.label
        }
      })
      return str
    },
  },
  data () {
    return {
      iconList: iconList,
      iconButtonRef: null,
      iconVisible: false,
      iconType: 'material',
      iconOpt: {
        value: '',
        backImg: '',
        type: 'material',
        pos: 'center',
        verticalFlip: false,
        horizontalFlip: false,
        rotate:0
      },
      oldIconOpt: {},
      posPopoverRef: null,
      posList: [
        { label:'中', value:'center' },
        { label:'左', value:'left' },
        { label:'右', value:'right' },
        { label:'左上', value:'left-top' },
        { label:'左下', value:'left-bottom' },
        { label:'右上', value:'right-top' },
        { label:'右下', value:'right-bottom' }
      ],
      isClickInPop: false,
      uploadIconImgList: [],
      headers: {
        Authorization: "Bearer " + this.$store.getters.access_token,
      },
      paramsData: {
        module: "/jvs-ui/backImg",
      },
      uplodaLoding: false,
    }
  },
  created () {
    this.iconOpt = Object.assign({}, defaulIconOption, this.iconOption)
    this.oldIconOpt = cloneDeep(this.iconOpt)
    this.iconType = this.iconOpt.type
    if(this.isDesign) {
      if(!this.uploadIconImgList) {
        this.$set(this, 'uploadIconImgList', [])
      }
      if(this.uploadIconImgList && this.uploadIconImgList.length == 0 && this.iconOpt.backImg) {
        this.$set(this, 'uploadIconImgList', [this.iconOpt.backImg])
      }
    }
    window.addEventListener('scroll', this.handleScroll, true)
  },
  destroyed () {
    window.removeEventListener('scroll', this.handleScroll, true)
  },
  methods: {
    handleScroll (e) {
      if(!(e && e.srcElement && e.srcElement.parentNode && e.srcElement.parentNode.className == 'back-img-contianer')) {
        // this.commSettingScroll()
      }
    },
    commSettingScroll () {
      this.iconVisible = false
      this.isClickInPop = false
    },
    changeType (val) {
      if(this.iconType == 'custom' && val != 'custom') {
        this.iconOpt.type = val  
      }
      if(this.iconType != 'custom' && val == 'custom' && this.iconOpt.backImg) {
        this.iconOpt.type = val  
      }
      this.iconType = val
      this.$refs.containerScrollRef.update()
    },
    setIcon (val) {
      this.iconOpt.value = val
      this.iconOpt.type = 'material'
    },
    changePos (val) {
      this.iconOpt.pos = val
      this.posPopoverRef.hide()
    },
    changeVerticalFlip () {
      this.iconOpt.verticalFlip = !this.iconOpt.verticalFlip
    },
    changeHorizontalFlip () {
      this.iconOpt.horizontalFlip = !this.iconOpt.horizontalFlip
    },
    changeRotate () {
      this.iconOpt.rotate = (this.iconOpt.rotate + 45) % 360
    },
    resetDefault () {
      this.iconOpt.pos = 'center'
      this.iconOpt.rotate = 0
      this.iconOpt.horizontalFlip = false
      this.iconOpt.verticalFlip = false
    },
    handleSuccess (res) {
      if(res.code == 0) {
        this.iconOpt.backImg = res.data.fileLink
        this.iconOpt.type = 'custom'
        this.uploadIconImgList.push(res.data.fileLink)
        this.uplodaLoding = false
      }
    },
    beforeUpload (file) {
      const isLt20M = file.size / 1024 / 1024 < 2;
      let isExcel = true
      let ocrFileType = ['png','jpeg','jpg','gif']
      let fileType = file.name.split(".").slice(-1)[0].toLowerCase()
      if(!ocrFileType.includes(fileType)) {
        isExcel = false
        Notification({
          title: '提示',
          message: '只支持上传jpg、png、gif、jpge格式文件',
          type: 'warning',
          position: 'bottom-right'
        })
      }
      if(!isLt20M) {
        Notification({
          title: '提示',
          message: '上传的图片大小不能超过 2MB!',
          type: 'warning',
          position: 'bottom-right'
        })
      }
      this.uplodaLoding = (isExcel && isLt20M)
      return isExcel && isLt20M
    },
    changeBackItem (item) {
      this.iconOpt.backImg = item
      this.iconOpt.type = 'custom'
    },
    deleteBackImg (item, index) {
      if(item == this.iconOpt.backImg){
        this.iconOpt.backImg = ''
      }
      this.uploadIconImgList.splice(index,1)
    },
    selectBackImg (img) {
      this.iconOpt.backImg = img
      this.$forceUpdate()
    },
  },
  watch: {
    iconOpt: {
      handler(newV, oldV) {
        if(!isEqual(newV, this.oldIconOpt)){
          this.$emit('change', newV)
        }
      },
      deep: true
    },
    iconOption: {
      handler(newV, oldV) {
        this.iconOpt = Object.assign({},defaulIconOption, newV)
        this.oldIconOpt = cloneDeep(this.iconOpt)
        this.iconType = this.iconOpt.type
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.custom-select-icon-com{
  height: 36px;
  // width: fit-content;
  width: 130px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #F5F6F7;
  padding: 0px 16px;
  box-sizing: border-box;
  border-radius: 4px 4px 4px 4px;
  cursor: pointer;
  .icon-div-box{
    display: flex;
    align-items: center;
    .select-iconfont{
      font-size: 24px;
    }
    .select-img{
      border-radius: 4px;
      border: 1px solid #C2C5CF;
      width: 24px;
      height: 24px;
      background: rgba(54, 59, 76, 0.7);
      background-image: linear-gradient(45deg, #C2C5CF 25%, transparent 25%, transparent 75%, #C2C5CF 75%),
          linear-gradient(45deg, #C2C5CF 25%, transparent 25%, transparent 75%, #C2C5CF 75%);
      background-blend-mode: multiply;
      background-position: 0 0, 4px 4px;
      background-size: 8px 8px;
    }
  }
  .arrow-icon{
    width: 16px;
    height: 16px;
    min-width: 16px;
  }
}
.back-img-list{
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  padding: 0 24px;
  box-sizing: border-box;
  img{
    width: 32px;
    height: 32px;
    cursor: pointer;
    margin-right: 10px;
    margin-bottom: 10px;
  }
}
</style>
<style lang="scss">
.select-icon-popover-box{
  padding: 0px !important;
  .select-icon-popover-body{
    .select-icon-top{
      padding: 0px 24px;
      .select-icon-type{
        display: flex;
        padding: 16px 0px;
        border-bottom: 1px solid #EEEFF0;
        .select-icon-type-item{
          font-size: 16px;
          color: #6f7588;
          cursor: pointer;
        }  
        .select-icon-type-item+.select-icon-type-item{
          margin-left: 40px;
        }
        .type-active{
          font-family: Source Han Sans-Bold, Source Han Sans;
          color: #1E6FFF;
          position: relative;
          &::after{
            position: absolute;
            content: '';
            left: 0px;
            bottom: -16px;
            height: 2px;
            width: 100%;
            background-color: #1E6FFF;
            border-radius: 2px 0px 2px 0px;
          }
        }
      }   
    }
    .select-icon-contianer{
      height: 216px;
      width: 100%;
      overflow: hidden;
      .back-img-upload-box{
        padding: 16px 24px 8px;
        display: flex;
        align-items: center;
        .upload-box-button{
          width: 100px;
          height: 56px;
          background: linear-gradient( 171deg, #E6EFFF 6%, #FFFFFF 79%);
          border-radius: 4px;
          border: 1px solid #EEEFF0;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          .upload-icon{
            width: 16px;
            height: 16px;
          }
          .upload-text{
            margin-top: 4px;
            font-size: 12px;
            color: #363B4C;
          }
        }
        .uploda-tips{
          color: #6F7588;
          font-size: 12px;
          margin-left: 8px;
        }
      }
      .contianer-scroll-box{
        margin: 0px 24px;
        .select-icon-list-box{
          .select-icon-title{
            font-family: Source Han Sans-Bold, Source Han Sans;
            padding-top: 12px;
            padding-bottom: 8px;
            color: #6F7588;
            font-size: 14px;
          }
          .select-icon-list-box{
            display: grid;
            grid-template-columns: repeat(11,1fr);
            grid-row-gap: 8px;
            grid-column-gap: 8px;
            transform:translateX(-4px);
            .select-icon-list-item{
              width: 32px;
              height: 32px;
              border-radius: 4px;
              display: flex;
              align-items: center;
              justify-content: center;
              cursor: pointer;
              color: #363B4C;
              .select-iconfont{
                font-size: 24px;
              }
              &:hover{
                background-color: #EEEFF0;
              }
            }
            .icon-active{
              color: #1E6FFF;
              background-color: #E4EDFF !important;
            }
          }
        }
      }
      .img-scroll-box{
        display: grid;
        grid-template-columns: repeat(4,1fr);
        grid-column-gap: 8px;
        grid-row-gap: 8px;
        padding: 0px 0px 16px;
        .contianer-scroll-item{
          height: 56px;
          border-radius: 4px;
          overflow: hidden;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(54, 59, 76, 0.7);
          background-image: linear-gradient(45deg, #C2C5CF 25%, transparent 25%, transparent 75%, #C2C5CF 75%),
              linear-gradient(45deg, #C2C5CF 25%, transparent 25%, transparent 75%, #C2C5CF 75%);
          background-blend-mode: multiply;
          background-position: 0 0, 4px 4px;
          background-size: 8px 8px;
          position: relative;
          cursor: pointer;
          .img-item{
            max-width: 100%;
            max-height: 100%;
          }
          .img-action-box{
            position: absolute;
            bottom: 0px;
            width: 100%;
            height: 24px;
            background: rgba(54,59,76,0.5);
            border-radius: 0px 0px 4px 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            visibility: hidden;
            i{
              font-size: 16px;
              color: #fff;
            }
          }
          &:hover{
            box-shadow: 0 0 0 1px #1E6FFF !important;
            .img-action-box{
              visibility: visible;
            }
          }
        }
        .contianer-scroll-item-active{
          box-shadow: 0 0 0 1px #1E6FFF !important;
        }
      }
    }
    .select-icon-bottom{
      border-top: 1px solid #EEEFF0;
      padding: 8px 24px;
      display: flex;
      align-items: center;
      user-select: none;
      .img-position-box{
        margin-left: 16px;
        padding: 0px 8px 0px 16px;
        box-sizing: border-box;
        width: 136px;
        height: 36px;
        background: #F5F6F7;
        border-radius: 4px 4px 4px 4px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        cursor: pointer;
        .arrow-icon{
          width: 16px;
          min-width: 16px;
          height: 16px;
        }
      }
      .action-item{
        width: 36px;
        height: 36px;
        border-radius: 4px 4px 4px 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-left: 8px;
        cursor: pointer;
        .action-icon{
          width: 16px;
          height: 16px;
        }
        &:hover{
          background: #F5F6F7;
        }
      }
      .reset-action{
        font-size: 14px;
        color: #1E6FFF;
        cursor: pointer;
        margin-left: 28px;
      }
    }
  }
}
.select-icon-popover-box[data-popper-placement^=bottom]{
  transform:translateY(-12px);
}
.select-icon-popover-box[data-popper-placement^=top]{
  transform:translateY(12px);
}
.select-icon-popover-box[data-popper-placement^=left]{
  transform:translateX(12px);
}
.select-icon-popover-box[data-popper-placement^=right]{
  transform:translateX(-12px);
}
.position-popover-box{
  padding: 8px !important;
  min-width: 136px !important;
  .drop-position-box{
    .position-item{
      height: 32px;
      line-height: 32px;
      padding: 0px 8px;
      box-sizing: border-box;
      border-radius: 4px 4px 4px 4px;
      cursor: pointer;
      font-size: 14px;
      color: #363b4c;
      &:hover{
        background-color: #eeeff0;
      }
    }
    .active{
      background: #E4EDFF;
      color: #1E6FFF;
      &:hover{
        background-color: #E4EDFF;
      }
    }
  }
}
.position-popover-box[data-popper-placement^=bottom]{
  transform:translateY(-12px);
}
.position-popover-box[data-popper-placement^=top]{
  transform:translateY(12px);
}
.position-popover-box[data-popper-placement^=left]{
  transform:translateX(12px);
}
.position-popover-box[data-popper-placement^=right]{
  transform:translateX(-12px);
}
</style>