<template>
  <div :class="{'signature-wrap': true, 'signature-wrap-editable': !disabled}">
    <div class="img-wrap" @click="showSignature()" @mousedown="touchSignature()">
      <img v-if="absPrevView" :src="absPrevView" mode="scaleToFill" />
      <div class="empty-signature" v-else>
        <i class="el-icon-edit" style="color: #dadbde;font-size: 20px;"></i>
        <span class="empty-place">点击签名</span>
      </div>
    </div>
    <div v-if="!disabled" v-show="show" :key="show" class="signature-contain">
      <div class="signature-main" style="z-index: 3000">
        <div class="signature-title">
          <span v-for="t in titles" :key="t">{{ t }}</span>
          <div class="signature-close" @click="cancelSignature">
            <i class="el-icon-close"></i>
          </div>
        </div>
        <div style="width: 100%;flex: 1;">
          <vue-signature-pad :ref="prop" width="100%" height="100%" class="signature" :options="options" />
        </div>
        <div class="signature-btns">
          <el-button size="mini" @click="clearSignature">清空</el-button>
          <el-button size="mini" type="primary" @click="onOK">确定</el-button>
          <div class="pen-tool">
            <span>笔刷大小</span>
            <div :class="{'active': currentSize == 1}" @click="selSize(1)"><b></b></div>
            <div :class="{'active': currentSize == 2}" @click="selSize(2)"><b></b></div>
            <div :class="{'active': currentSize == 3}" @click="selSize(3)"><b></b></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  cxt: null,
  data() {
    return {
      options: {
        penColor: "#000",
        minWidth: 1,    //控制画笔最小宽度
        maxWidth: 1,    //控制画笔最大宽度
      },
      currentSize: 3,
      show: false,
      ctrl: null,
      listeners: [],
      prevView: "",
      draws: [],
      lines: [],
      line: null,
      isDrawing: false,
      originPoint: {
        x: 40,
        y: 48
      },
    };
  },
  props: {
    value: {
      default: "",
    },
    title: {
      type: String,
      default: "请签字",
    },
    prop: {
      type: String
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    value() {
      this.prevView = (this.value && this.value.length > 0) ? this.value[0].url : ''
    },
  },
  computed: {
    titles() {
      return this.title.split("");
    },
    absPrevView() {
      var pv = this.prevView;
      return pv;
    },
  },
  created () {
    this.prevView = (this.value && this.value.length > 0) ? this.value[0].url : ''
  },
  methods: {
    onOK () {
      const { isEmpty, data } = this.$refs[this.prop].saveSignature();
      this.$emit("submit", data);
      this.prevView = data;
      this.hideSignature();
    },
    touchSignature () {
      let sig = this.prevView;
      if (!sig || !sig.length) {
        this.showSignature();
      }
    },
    showSignature () {
      if(this.disabled) return;
      this.show = true;
      this.selSize(3)
      this.$forceUpdate()
    },
    cancelSignature () {
      this.hideSignature();
    },
    hideSignature () {
      this.$refs[this.prop].clearSignature();
      this.show = false;
    },
    clearSignature () {
      this.$refs[this.prop].clearSignature();
    },
    selSize (val) {
      this.currentSize = val
      this.options = {
        penColor: "#000",
        minWidth: val,
        maxWidth: val,
      }
    }
  },
};
</script>

<style lang="scss" scoped>
$jvsFormItemHeight: 36px;
.signature-wrap {
  height: 100%;
  width: 100%;
  .img-wrap {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    text-align: center;
    align-content: center;
    justify-content: center;
    position: relative;
    img {
      width: 100%;
      height: 100%;
    }
    .empty-signature {
      width: 100%;
      box-sizing: border-box;
      background: #F5F6F7;
      border-radius: 6px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      height: calc(100% - 4px);
      .empty-place {
        margin-top: 10px;
        color: #dadbde;
      }
    }
  }
}

.signature-contain {
  z-index: 9000;
  position: fixed;
  left: 0;
  top: 0;
  width: 100vw;
  height: 100vh;
  .signature-main {
    background: #fff;
    height: 100%;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
  .signature-title {
    font-weight: bold;
    font-size: 18px;
    display: flex;
    justify-content: center;
    color: #222;
    width: calc(100% - 80px);
    position: relative;
    span{
      padding: 0 10px;
    }
    .signature-close{
      position: absolute;
      right: -20px;
      top: 0;
      width: 20px;
      text-align: center;
      cursor: pointer;
      i{
        font-size: 16px;
        color: #BFBFBF;
      }
    }
  }
  .signature {
    background: #F5F6F7;
    align-self: center;
  }
  .signature-btns {
    display: flex;
    height: $jvsFormItemHeight;
    position: relative;
    padding: 10px 0;
    .pen-tool{
      position: absolute;
      left: calc(50% + 100px);
      width: 200px;
      display: flex;
      align-items: center;
      span{
        word-break: keep-all;
      }
      div{
        margin-left: 20px;
        padding: 5px;
        cursor: pointer;
      }
      div.active{
        border: 1px dashed #0074d9;
      }
      b{
        display: block;
        border-radius: 100%;
        width: 4px;
        height: 4px;
        background-color: #000;
        overflow: hidden;
      }
      div:nth-of-type(2) b{
        width: 6px;
        height: 6px;
      }
      div:nth-of-type(3) b{
        width: 8px;
        height: 8px;
      }
    }
  }
}
.signature-wrap-editable{
  cursor: pointer;
}
</style>
