<template>
  <div class="custom-back-img-box">
    <el-popover
      ref="popoverRef"
      v-model="backImgVisible"
      :show-arrow="false"
      trigger="click"
      width="474"
      placement="bottom-end"
      popper-class="cutsom-img-popover-box"
    >
      <div class="custom-img-popover-body">
        <div class="back-img-top">
          <div class="back-img-type">
            <div
              class="img-type-item"
              :class="[backImgType == 'material' && 'type-active']"
              @click="changeType('material')"
            >
              使用素材
            </div>
            <div
              class="img-type-item"
              v-if="isUseUpload"
              :class="[backImgType == 'custom' && 'type-active']"
              @click="changeType('custom')"
            >
              自定义图片
            </div>
          </div>
        </div>
        <div class="back-img-contianer">
          <div class="back-img-upload-box" v-if="backImgType == 'custom'">
            <el-upload
              action="/mgr/jvs-auth/upload/jvs-public"
              :data="paramsData"
              :headers="headers"
              :on-success="handleSuccess"
              :show-file-list="false"
              :before-upload="beforeUpload"
              accept=".jpg,.png,.gif,.jpge"
            >
              <div class="upload-box-button">
                <svg class="upload-icon">
                  <use xlink:href="#bi-a-zu5758"></use>
                </svg>
                <div class="upload-text">上传本地图片</div>
              </div>
            </el-upload>
            <!-- svg、 -->
            <div class="uploda-tips">
              支持jpg、png、gif、jpge格式，不超过2MB
            </div>
          </div>
          <el-scrollbar
            ref="containerScrollRef"
            :max-height="backImgType == 'custom' ? 'calc(216px - 80px)' : ''"
          >
            <div
              class="contianer-scroll-box"
              :class="[backImgType == 'custom' && 'custom-scroll-box']"
            >
              <div
                class="contianer-scroll-item"
                :class="[
                  item == backOption.backImg && 'contianer-scroll-item-active',
                ]"
                v-for="(item, index) in getBackImgList"
                :key="index"
                @click="changeBackItem(item)"
              >
                <img class="img-item" :src="item" />
                <div
                  class="img-action-box"
                  v-if="backImgType == 'custom'"
                  @click.stop="deleteBackImg(item, index)"
                >
                  <i class="bi-iconfont bi-shanchu"></i>
                </div>
              </div>
            </div>
          </el-scrollbar>
          <div v-if="backImgType == 'custom' && imgListOptions && imgListOptions.length > 0" class="back-img-list">
            <img v-for="(img, key) in imgListOptions" :key="key" :src="img" alt="" @click="selectBackImg(img)"/>
          </div>
        </div>
        <div class="back-img-bottom">
          <div>显示位置</div>
          <el-popover
            ref="posPopoverRef"
            v-model="isClickInPop"
            placement="bottom"
            width="136"
            trigger="click"
            :show-arrow="false"
            popper-class="position-popover-box"
          >
            <div class="drop-position-box">
              <div
                class="position-item"
                v-for="(item, index) in posList"
                :key="index"
                :class="[backOption.pos == item.value && 'active']"
                @click="changePos(item.value)"
              >
                {{ item.label }}
              </div>
            </div>
            <div slot="reference" class="img-position-box">
              <span>{{ getLabel }}</span>
              <svg class="arrow-icon">
                <use xlink:href="#jvs-public-shouqi1"></use>
              </svg>
            </div>
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
      <div
        slot="reference"
        class="preview-back-img"
        ref="backImgButtonRef"
      >
        <svg class="check-icon" @click.stop="changeOpenStatus">
          <use
            :xlink:href="`#${!backOption.isCheckBackImg ? 'jvs-public-a-juxing2862biankuang' : 'jvs-public-a-zu3739'}`"
          ></use>
        </svg>
        <div class="title">背景图案</div>
        <div class="back-img-box" v-if="backOption.isCheckBackImg">
          <div class="img-box">
            <img class="img" :src="backOption.backImg" />
          </div>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script>
import { Notification } from "element-ui";
import defaultBackImg from "../../assets/images/backimg/default.png";
import defaultBackImg1 from "../../assets/images/backimg/default1.png";
import defaultBackImg2 from "../../assets/images/backimg/default2.png";
import defaultBackImg3 from "../../assets/images/backimg/default3.png";
import defaultBackImg4 from "../../assets/images/backimg/default4.png";
import defaultBackImg5 from "../../assets/images/backimg/default5.png";
import defaultBackImg6 from "../../assets/images/backimg/default6.png";
import defaultBackImg7 from "../../assets/images/backimg/default7.png";
import defaultBackImg8 from "../../assets/images/backimg/default8.png";
import defaultBackImg9 from "../../assets/images/backimg/default9.png";
import defaultBackImg10 from "../../assets/images/backimg/default10.png";
import defaultBackImg11 from "../../assets/images/backimg/default11.png";
import { cloneDeep, isEqual } from "lodash";
export default {
  name: 'jvsBackImg',
  props: {
    isUseUpload: {
      type: Boolean,
      default: true,
    },
    isDesign: {
      type: Boolean,
      default: true,
    },
    backImgOpt: {
      type: Object,
      default() {
        return {};
      },
    },
    imgListOptions: Array
  },
  computed: {
    getBackImgList () {
      if(this.backImgType == 'material') {
        return this.backImgList;
      }else {
        return this.uploadImgList
      }
    },
    getLabel () {
      let str = ''
      this.posList.forEach((item, index) => {
        if (item.value == this.backOption.pos) {
          str = item.label
        }
      });
      return str
    },
  },
  data () {
    return {
      uploadImgList: [],
      backImgVisible: false,
      backImgType: 'material',
      isClickInPop: false,
      defaultBackOption: {
        isCheckBackImg: false,
        backImgType: 'material',
        pos: 'center',
        backImg: defaultBackImg,
        horizontalFlip: false,
        verticalFlip: false,
        rotate: 0,
      },
      backOption: {
        isCheckBackImg: false,
        backImgType: 'material',
        pos: 'center',
        backImg: defaultBackImg,
        horizontalFlip: false,
        verticalFlip: false,
        rotate: 0,
      },
      oldBackOption: {},
      headers: {
        Authorization: "Bearer " + this.$store.getters.access_token,
      },
      paramsData: {
        module: "/jvs-ui/backImg",
      },
      uplodaLoding: false,
      backImgList: [
        defaultBackImg,
        defaultBackImg1,
        defaultBackImg2,
        defaultBackImg3,
        defaultBackImg4,
        defaultBackImg5,
        defaultBackImg6,
        defaultBackImg7,
        defaultBackImg8,
        defaultBackImg9,
        defaultBackImg10,
        defaultBackImg11,
      ],
      posList: [
        {
          label: "中",
          value: "center",
        },
        {
          label: "左",
          value: "left",
        },
        {
          label: "右",
          value: "right",
        },
        {
          label: "左上",
          value: "left-top",
        },
        {
          label: "左下",
          value: "left-bottom",
        },
        {
          label: "右上",
          value: "right-top",
        },
        {
          label: "右下",
          value: "right-bottom",
        },
      ],

    }
  },
  created () {
    this.backOption = Object.assign({}, this.defaultBackOption, this.backImgOpt)
    this.backImgType = this.backOption.backImgType
    this.oldBackOption = cloneDeep(this.backOption)
    if(this.isDesign) {
      if(!this.uploadImgList) {
        this.$set(this, 'uploadImgList', [])
      }
      if(this.uploadImgList && this.uploadImgList.length == 0 && this.backOption.backImg) {
        this.$set(this, 'uploadImgList', [this.backOption.backImg])
      }
    }
  },
  mounted() {
    window.addEventListener("scroll", this.handleScroll, true)
  },
  unmounted() {
    window.removeEventListener("scroll", this.handleScroll, true)
  },
  methods: {
    handleScroll (e) {
      if(!(e && e.srcElement && e.srcElement.parentNode && e.srcElement.parentNode.className == 'back-img-contianer')) {
        this.commSettingScroll()
      }
    },
    commSettingScroll () {
      this.isClickInPop = false
      this.backImgVisible = false
      this.$forceUpdate()
    },
    changeOpenStatus () {
      if(this.backOption.isCheckBackImg) {
        this.backOption.isCheckBackImg = false
      }else {
        this.backOption.isCheckBackImg = true
      }
      if(this.backOption.isCheckBackImg && !this.backOption.backImg) {
        this.backOption.backImg = defaultBackImg
      }
    },
    openBackImgPop () {
      if(this.backOption.isCheckBackImg) {
        this.backImgVisible = true
      }
    },
    changeType (val) {
      this.backImgType = val;
      this.$refs.containerScrollRef.update()
    },
    changePos (val) {
      this.backOption.pos = val
      this.isClickInPop = false
    },
    changeVerticalFlip () {
      this.backOption.verticalFlip = !this.backOption.verticalFlip
    },
    changeHorizontalFlip () {
      this.backOption.horizontalFlip = !this.backOption.horizontalFlip
    },
    changeRotate () {
      this.backOption.rotate = (this.backOption.rotate + 45) % 360
    },
    resetDefault () {
      this.backOption.pos = "center"
      this.backOption.rotate = 0
      this.backOption.horizontalFlip = false
      this.backOption.verticalFlip = false
      this.$forceUpdate()
    },
    changeBackItem (item) {
      this.backOption.backImgType = this.backImgType
      this.backOption.backImg = item
      this.$forceUpdate()
    },
    deleteBackImg (item, index) {
      if(item == this.backOption.backImg) {
        this.backOption.backImg = defaultBackImg
      }
      this.uploadImgList.splice(index, 1)
      this.$forceUpdate()
    },
    handleSuccess(res) {
      if(res.code == 0) {
        this.backOption.backImg = res.data.fileLink
        this.backOption.backImgType = "custom"
        this.uploadImgList.push(res.data.fileLink)
        this.uplodaLoding = false
        this.$forceUpdate()
      }
    },
    beforeUpload (file) {
      const isLt20M = file.size / 1024 / 1024 < 2;
      let isExcel = true;
      let ocrFileType = ["png", "jpeg", "jpg", "gif"];
      let fileType = file.name.split(".").slice(-1)[0].toLowerCase();
      if(!ocrFileType.includes(fileType)) {
        isExcel = false;
        Notification({
          title: "提示",
          message: "只支持上传jpg、png、gif、jpge格式文件",
          type: "warning",
          position: "bottom-right",
        })
      }
      if(!isLt20M) {
        Notification({
          title: "提示",
          message: "上传的图片大小不能超过 2MB!",
          type: "warning",
          position: "bottom-right",
        })
      }
      this.uplodaLoding = isExcel && isLt20M
      return isExcel && isLt20M;
    },
    selectBackImg (img) {
      this.backOption.backImg = img
      this.$forceUpdate()
    },
  },
  watch: {
    backOption: {
      handler(newV, oldV) {
        if(!isEqual(newV, this.oldBackOption)) {
          this.oldBackOption = cloneDeep(newV)
          this.$emit("change", newV)
        }
      },
      deep: true
    },
    backImgOpt: {
      handler(newV, oldV) {
        this.backOption = Object.assign({}, this.defaultBackOption, newV)
        this.oldBackOption = cloneDeep(this.backOption)
        this.backImgType = this.backOption.backImgType
      },
    }
  }
}
</script>
<style lang="scss" scoped>
.custom-back-img-box {
  height: 36px;
  width: fit-content;
  display: flex;
  align-items: center;
  background: #f5f6f7;
  padding: 0px 8px 0px 16px;
  border-radius: 4px 4px 4px 4px;
  .preview-back-img {
    display: flex;
    align-items: center;
    user-select: none;
    cursor: pointer;
    .check-icon {
      width: 16px;
      height: 16px;
      min-width: 16px;
      cursor: pointer;
    }
    .title {
      font-size: 14px;
      color: #363b4c;
      margin-left: 8px;
      margin-right: 8px;
    }
    .back-img-box {
      width: 100px;
      height: 36px;
      padding: 4px;
      box-sizing: border-box;
      .img-box {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        .img {
          max-width: 100%;
          max-height: 100%;
          position: relative;
          z-index: 1;
          border-radius: 4px 4px 4px 4px;
          border: 1px solid #c2c5cf;
          background: rgba(54, 59, 76, 0.7);
          background-image: linear-gradient(
              45deg,
              #c2c5cf 25%,
              transparent 25%,
              transparent 75%,
              #c2c5cf 75%
            ),
            linear-gradient(
              45deg,
              #c2c5cf 25%,
              transparent 25%,
              transparent 75%,
              #c2c5cf 75%
            );
          background-blend-mode: multiply;
          background-position: 0 0, 4px 4px;
          background-size: 8px 8px;
        }
      }
    }
  }
}
.back-img-list{
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  padding: 0 24px;
  box-sizing: border-box;
  img{
    width: 68px;
    height: 32px;
    cursor: pointer;
    margin-right: 20px;
  }
}
</style>
<style lang="scss">
.cutsom-img-popover-box {
  padding: 0px !important;
}
.cutsom-img-popover-box[data-popper-placement^="bottom"] {
  transform: translateY(-12px) translateX(8px);
}
.cutsom-img-popover-box[data-popper-placement^="top"] {
  transform: translateY(12px) translateX(8px);
}
.cutsom-img-popover-box[data-popper-placement^="left"] {
  transform: translateX(12px);
}
.cutsom-img-popover-box[data-popper-placement^="right"] {
  transform: translateX(-12px);
}
.custom-img-popover-body {
  .back-img-top {
    padding: 0px 24px;
    .back-img-type {
      display: flex;
      padding: 16px 0px;
      border-bottom: 1px solid #eeeff0;
      .img-type-item {
        font-size: 16px;
        color: #6f7588;
        cursor: pointer;
      }
      .img-type-item + .img-type-item {
        margin-left: 40px;
      }
      .type-active {
        font-family: Source Han Sans-Bold, Source Han Sans;
        color: #1e6fff;
        position: relative;
        &::after {
          position: absolute;
          content: "";
          left: 0px;
          bottom: -16px;
          height: 2px;
          width: 100%;
          background-color: #1e6fff;
          border-radius: 2px 0px 2px 0px;
        }
      }
    }
  }
  .back-img-contianer {
    height: 216px;
    width: 100%;
    overflow: hidden;
    .back-img-upload-box {
      padding: 16px 24px 8px;
      display: flex;
      align-items: center;
      .upload-box-button {
        width: 100px;
        height: 56px;
        background: linear-gradient(171deg, #e6efff 6%, #ffffff 79%);
        border-radius: 4px;
        border: 1px solid #eeeff0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        .upload-icon {
          width: 16px;
          height: 16px;
        }
        .upload-text {
          margin-top: 4px;
          font-size: 12px;
          color: #363b4c;
        }
      }
      .uploda-tips {
        color: #6f7588;
        font-size: 12px;
        margin-left: 8px;
      }
    }
    .contianer-scroll-box {
      margin: 0px 24px;
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      grid-column-gap: 8px;
      grid-row-gap: 8px;
      padding: 16px 0px;
      .contianer-scroll-item {
        height: 56px;
        border-radius: 4px;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;
        // background: #F5F6F7;
        background: rgba(54, 59, 76, 0.7);
        background-image: linear-gradient(
            45deg,
            #c2c5cf 25%,
            transparent 25%,
            transparent 75%,
            #c2c5cf 75%
          ),
          linear-gradient(
            45deg,
            #c2c5cf 25%,
            transparent 25%,
            transparent 75%,
            #c2c5cf 75%
          );
        background-blend-mode: multiply;
        background-position: 0 0, 4px 4px;
        background-size: 8px 8px;
        position: relative;
        cursor: pointer;
        .img-item {
          max-width: 100%;
          max-height: 100%;
        }
        .img-action-box {
          position: absolute;
          bottom: 0px;
          width: 100%;
          height: 24px;
          background: rgba(54, 59, 76, 0.5);
          border-radius: 0px 0px 4px 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          visibility: hidden;
          i {
            font-size: 16px;
            color: #fff;
          }
        }
        &:hover {
          box-shadow: 0 0 0 1px #1e6fff !important;
          .img-action-box {
            visibility: visible;
          }
        }
      }
      .contianer-scroll-item-active {
        box-shadow: 0 0 0 1px #1e6fff !important;
      }
    }
    .custom-scroll-box {
      padding: 0px 0px 16px;
    }
  }
  .back-img-bottom {
    border-top: 1px solid #eeeff0;
    padding: 8px 24px;
    display: flex;
    align-items: center;
    user-select: none;
    .img-position-box {
      margin-left: 16px;
      padding: 0px 8px 0px 16px;
      box-sizing: border-box;
      width: 136px;
      height: 36px;
      background: #f5f6f7;
      border-radius: 4px 4px 4px 4px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      cursor: pointer;
      .arrow-icon {
        width: 16px;
        min-width: 16px;
        height: 16px;
      }
    }
    .action-item {
      width: 36px;
      height: 36px;
      border-radius: 4px 4px 4px 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-left: 8px;
      cursor: pointer;
      .action-icon {
        width: 16px;
        height: 16px;
      }
      &:hover {
        background: #f5f6f7;
      }
    }
    .reset-action {
      font-size: 14px;
      color: #1e6fff;
      cursor: pointer;
      margin-left: 28px;
    }
  }
}
.position-popover-box {
  padding: 8px !important;
  min-width: 136px !important;
  .drop-position-box {
    .position-item {
      height: 32px;
      line-height: 32px;
      padding: 0px 8px;
      box-sizing: border-box;
      border-radius: 4px 4px 4px 4px;
      cursor: pointer;
      font-size: 14px;
      color: #363b4c;
      &:hover {
        background-color: #eeeff0;
      }
    }
    .active {
      background: #e4edff;
      color: #1e6fff;
      &:hover {
        background-color: #e4edff;
      }
    }
  }
}
.position-popover-box[data-popper-placement^="bottom"] {
  transform: translateY(-12px);
}
.position-popover-box[data-popper-placement^="top"] {
  transform: translateY(12px);
}
.position-popover-box[data-popper-placement^="left"] {
  transform: translateX(12px);
}
.position-popover-box[data-popper-placement^="right"] {
  transform: translateX(-12px);
}
</style>