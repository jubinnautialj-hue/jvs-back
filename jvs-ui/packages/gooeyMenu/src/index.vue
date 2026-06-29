<template>
  <div id="gooey-meun-page">
    <div v-if="toolType == 'button'" class="button-menu-list">
      <div class="button-item with-icon" @click="roleConfig">
        <svg class="svg-icon" aria-hidden="true">
          <use xlink:href="#icon-jvs-zhanghuguanli-weixuanzhong"></use>
        </svg>
        <span>权限设置</span>
      </div>
      <el-tooltip effect="dark" :content="`另存为(Alt+S)`" placement="top">
        <div class="button-item normal" @click="handleOtherSave">
          <span>另存为新门户</span>
        </div>
      </el-tooltip>
      <el-tooltip effect="dark" :content="'保存' + `(Ctrl+S)`" placement="top">
        <div class="button-item primary" @click="handleSave">
          <span>保存</span>
        </div>
      </el-tooltip>
    </div>
    <div v-else>
      <nav class="menu" :class="[isOpenMenu && 'menu-open']">
        <label class="menu-open-button" @click="openMenuButton">
          <span class="hamburger hamburger-1"></span>
          <span class="hamburger hamburger-2"></span>
          <span class="hamburger hamburger-3"></span>
        </label>
        <!-- <el-tooltip effect="dark" :content="`辅助功能(Alt+X)`" placement="top">
                <div class="menu-item" @click="handleShowAuxiliaryConfig">
                    <Icon name="tools" />
                </div>
            </el-tooltip> -->
        <el-tooltip effect="dark" :content="`另存为(Alt+S)`" placement="top">
          <div class="menu-item" @click="handleOtherSave">
            <svg class="svg-icon" aria-hidden="true">
              <use xlink:href="#jvs-ui-icon-fuzhi1"></use>
            </svg>
          </div>
        </el-tooltip>
        <el-tooltip
          effect="dark"
          :content="'保存' + `(Ctrl+S)`"
          placement="top"
        >
          <div class="menu-item" @click="handleSave">
            <Icon name="save" viewBox="0 0 1024 1024" />
          </div>
        </el-tooltip>
        <el-tooltip
          effect="dark"
          :content="(!isLock ? '解锁' : '锁定') + `(Alt+E)`"
          placement="top"
        >
          <div
            class="menu-item"
            @click="handleSetLock"
            :title="$hit('editStatueWarningText')"
          >
            <Icon :name="isLock ? 'unlock' : 'lock'" />
          </div>
        </el-tooltip>
        <el-tooltip
          effect="dark"
          :content="'全局设置' + `(Alt+W)`"
          placement="top"
          v-if="!$homePageOptions.globalConfigHidden"
        >
          <div class="menu-item" @click="handleShowGlobalConfig">
            <Icon name="setting-4" />
          </div>
        </el-tooltip>
        <el-tooltip
          effect="dark"
          :content="'添加卡片' + `(Alt+Q)`"
          placement="top"
        >
          <div class="menu-item" @click="handleAddComponent">
            <Icon name="add" />
          </div>
        </el-tooltip>
        <el-tooltip effect="dark" :content="'权限设置'" placement="top">
          <div class="menu-item" @click="roleConfig">
            <svg class="svg-icon" aria-hidden="true">
              <use xlink:href="#jvs-ui-icon-jiaosexuanze"></use>
            </svg>
          </div>
        </el-tooltip>
      </nav>
      <svg
        v-if="!isSafari"
        id="filterSvg"
        xmlns="http://www.w3.org/2000/svg"
        version="1.1"
      >
        <defs>
          <filter id="shadowed-goo">
            <feGaussianBlur
              in="SourceGraphic"
              result="blur"
              stdDeviation="10"
            />
            <feColorMatrix
              in="blur"
              mode="matrix"
              values="1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 18 -7"
              result="goo"
            />
            <feGaussianBlur in="goo" stdDeviation="3" result="shadow" />
            <feColorMatrix
              in="shadow"
              mode="matrix"
              values="0 0 0 0 0  0 0 0 0 0  0 0 0 0 0  0 0 0 1 -0.2"
              result="shadow"
            />
            <feOffset in="shadow" dx="1" dy="1" result="shadow" />
            <feComposite in2="shadow" in="goo" result="goo" />
            <feComposite in2="goo" in="SourceGraphic" result="mix" />
          </filter>
          <filter id="goo">
            <feGaussianBlur
              in="SourceGraphic"
              result="blur"
              stdDeviation="10"
            />
            <feColorMatrix
              in="blur"
              mode="matrix"
              values="1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 18 -7"
              result="goo"
            />
            <feComposite in2="goo" in="SourceGraphic" result="mix" />
          </filter>
        </defs>
      </svg>
    </div>
  </div>
</template>

<script>
export default {
  name: "gooeyMenu",
  props: {
    isLock: {
      type: Boolean,
      default: false,
    },
    toolType: {
      type: String,
    },
  },
  data() {
    return {
      isSafari:
        /Safari/.test(navigator.userAgent) &&
        !/Chrome/.test(navigator.userAgent),
      isOpenMenu: false,
    };
  },
  created() {
    
  },
  mounted() {
    document.addEventListener("keydown", this.keyDownEvent);
    if(this.toolType == "button") {
      this.$emit("update:isLock", false);
    }
  },
  unmounted() {
    document.addEventListener("keydown", this.keyDownEvent);
    if(this.toolType == "button") {
      this.$emit("update:isLock", true);
    }
  },
  methods: {
    closeOtherDialog() {
      if (document.querySelector(".easy-dialog-main")) {
        const EscapeEvent = new KeyboardEvent("keydown", { key: "Escape" });
        document.dispatchEvent(EscapeEvent);
      }
      if (document.querySelector(".__menu__wrapper")) {
        const MouseDownEvent = new MouseEvent("mousedown");
        document.dispatchEvent(MouseDownEvent);
      }
    },
    keyDownEvent(e) {
      if (e) {
        if (e.key === "q" && e.altKey) {
          e.preventDefault();
          this.closeOtherDialog();
          this.$emit("addComponent");
        }
        // if (e.key === 'w' && e.altKey) {
        //     e.preventDefault()
        //     this.closeOtherDialog()
        //     emit('showGlobalConfig')
        // }
        if (e.key === "e" && e.altKey) {
          e.preventDefault();
          this.closeOtherDialog();
          this.$emit("update:isLock", !this.isLock);
        }
        if (e.key === "s" && e.ctrlKey) {
          e.preventDefault();
          this.closeOtherDialog();
          this.$emit("handleSave");
        }
        if (e.key === "s" && e.altKey) {
          e.preventDefault();
          this.closeOtherDialog();
          this.$emit("handleSave", "new");
        }
        // if (e.key === 'x' && e.altKey) {
        //     e.preventDefault()
        //     this.closeOtherDialog()
        //     emit('showAuxiliaryConfig')
        // }
      }
    },
    handleSave() {
      this.$emit("handleSave");
    },
    handleOtherSave() {
      this.$emit("handleSave", "new");
    },
    handleSetLock() {
      this.$emit("update:isLock", !this.isLock);
    },
    handleAddComponent() {
      this.$emit("addComponent");
    },
    handleShowGlobalConfig() {
      this.$emit("showGlobalConfig");
    },
    handleShowAuxiliaryConfig() {
      this.$emit("showAuxiliaryConfig");
    },
    openMenuButton() {
      this.isOpenMenu = !this.isOpenMenu;
    },
    roleConfig() {
      this.$emit("roleConfig");
    },
  },
};
</script>

<style scoped lang="scss">
#gooey-meun-page {
  $menu-items: 5;
  %goo {
    filter: url("#shadowed-goo");
  }
  %ball {
    background: $color-primary;
    border-radius: 100%;
    width: 40px;
    height: 40px;
    position: absolute;
    top: 4px;
    right: 0;
    color: white;
    transform: translate3d(0, 0, 0);
    transition: transform ease-out 0.2s, background-color ease-out 0.2s,
      color ease-out 0.2s;
    box-shadow: 0 0 2px $color-primary;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .menu-item {
    @extend %ball;
    &:focus {
      outline: none;
    }
    .svg-icon {
      width: 20px;
      height: 20px;
      fill: #fff;
    }
  }
  .hamburger {
    width: 16px;
    height: 2px;
    background: #fff;
    display: block;
    position: absolute;
    top: 50%;
    left: 50%;
    margin-left: -8px;
    margin-top: -1px;
    transition: transform 200ms;
  }
  $hamburger-spacing: 6px;
  .hamburger-1 {
    transform: translate3d(0, -$hamburger-spacing, 0);
  }
  .hamburger-2 {
    transform: translate3d(0, 0, 0);
  }
  .hamburger-3 {
    transform: translate3d(0, $hamburger-spacing, 0);
  }
  .menu {
    @extend %goo;
    $height: 48px;
    position: fixed;
    right: 3vw;
    bottom: 5vh;
    height: $height;
    box-sizing: border-box;
    font-size: 20px;
    text-align: right;
    z-index: 999;
  }
  .menu-item {
    &:hover {
      background: lighten($color-primary, 80);
      color: $color-primary;
      cursor: pointer;
      .svg-icon {
        fill: $color-primary;
      }
    }
    @for $i from 1 through $menu-items {
      &:nth-child(#{$i + 2}) {
        transition-duration: 180ms;
      }
    }
  }
  .menu-open-button {
    @extend %ball;
    z-index: 2;
    transition-timing-function: cubic-bezier(0.175, 0.885, 0.32, 1.275);
    transition-duration: 400ms;
    transform: scale(1.1, 1.1) translate3d(0, 0, 0);
    cursor: pointer !important;
  }
  .menu-open-button:hover {
    transform: scale(1.2, 1.2) translate3d(0, 0, 0);
  }
  .menu-open {
    .menu-open-button {
      .hamburger-1 {
        transform: translate3d(0, 0, 0) rotate(45deg);
      }
      .hamburger-2 {
        transform: translate3d(0, 0, 0) scale(0.1, 1);
      }
      .hamburger-3 {
        transform: translate3d(0, 0, 0) rotate(-45deg);
      }
    }
    .menu-open-button {
      transition-timing-function: linear;
      transition-duration: 200ms;
      transform: scale(0.8, 0.8) translate3d(0, 0, 0);
    }
    .menu-item {
      transition-timing-function: cubic-bezier(0.165, 0.84, 0.44, 1);
      @for $i from 1 through $menu-items {
        &:nth-child(#{$i + 1}) {
          transition-duration: 90ms+ (100ms * $i);
          transform: translate3d(-60px * $i, 0, 0);
        }
      }
    }
  }
  #filterSvg {
    display: none;
  }
}
.button-menu-list {
  display: flex;
  align-items: center;
  .button-item {
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    span {
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
    }
    .svg-icon {
      display: block;
      width: 16px;
      height: 16px;
      margin-right: 4px;
    }
    &.width-icon {
      padding: 0 8px;
    }
    &.normal {
      padding: 0 14px;
      color: #1E6FFF;
      background: #DDEAFF;
      border-radius: 4px;
    }
    &.primary {
      width: 80px;
      color: #fff;
      background: #1E6FFF;
      border-radius: 4px;
    }
    &+.button-item {
      margin-left: 8px;
    }
  }
}
</style>
