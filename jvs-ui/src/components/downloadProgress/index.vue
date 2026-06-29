<!-- 业务组件：下载栏 -->

<template>
  <!-- <transition name="download-slide"> -->
  <div class="download-container" v-if="visible">
    <div class="download-header">
      <span class="download-header-title">
        <i
          :class="`${
            !isAllFinish ? 'el-icon-loading' : 'el-icon-success'
          } success-icon`"
        ></i>
        <span>{{ isAllFinish ? "下载完成" : "正在进行中" }}</span>
        <span>
          {{
            downloadList.filter((item) => item.percentComplete === 100).length
          }}/{{ downloadList.length }}
        </span>
      </span>
      <div class="download-header-btn" @click="visible = false">×</div>
    </div>
    <div
      v-for="msg in downloadList"
      :key="msg.uid"
      class="download-block"
      :class="[`type-success`]"
    >
      <div class="download-file">
        <div class="download-file-name">{{ msg.name }}</div>
        <div class="download-file-percent">
          {{ `${msg.percentComplete.toFixed(2)}%` }}
        </div>
        <i
          :class="`${
            msg.percentComplete !== 100 ? 'el-icon-loading' : 'el-icon-success'
          } success-icon`"
        ></i>
      </div>
      <!-- <div class="close-btn" @click="close(msg.uid)">×</div> -->
    </div>
  </div>
  <!-- </transition> -->
</template>

<script>
export default {
  name: "downloadProgress",
  data() {
    return {
      downloadList: [], // 下载列表
      visible: false, // 是否可见
    };
  },
  computed: {
    // 是否全部下载完成
    isAllFinish() {
      return this.downloadList.every((item) => item.percentComplete === 100);
    },
  },
  watch: {
    visible(val) {
      !val && (this.downloadList = []);
    },
  },
  methods: {
    // 初始化下载栏
    handleInitDownload(config) {
      this.visible = true;
      if (!this.downloadList.some((item) => item.uid === config.uid)) {
        this.downloadList.push(config);
      } else {
        this.downloadList.forEach((item) => {
          item.uid === config.uid &&
            (item.percentComplete = config.percentComplete);
        });
      }
    },
    // 关闭单条数据
    close(id) {
      this.downloadList.forEach((item, i) => {
        item.uid === id && this.downloadList.splice(i, 1);
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.download-container {
  width: 500px;
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  .download-header {
    height: 56px;
    border-bottom: 1px solid #eeeff0;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    &-title {
      display: flex;
      gap: 8px;
      align-items: center;
      font-size: 16px;
      color: #363b4c;
    }
    &-btn {
      font-size: 18px;
      cursor: pointer;
    }
    .success-icon {
      &::before {
        font-size: 24px;
      }
      &.el-icon-success {
        color: #67c23a;
      }
      &.el-icon-loading {
        color: #409eff;
      }
    }
  }
  .download-block {
    padding: 0 24px;
    font-size: 14px;
    display: flex;
    .download-file {
      height: 52px;
      border-bottom: 1px solid #eeeff0;
      display: flex;
      align-items: center;
      flex: 1;
      justify-content: space-between;
      font-size: 16px;
      &-name {
        width: 310px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      &-percent {
        width: 110px;
        display: flex;
        justify-content: flex-end;
        gap: 8px;
        align-items: center;
        color: #606266;
        font-style: italic;
      }
      .success-icon {
        &.el-icon-success {
          color: #67c23a;
        }
        &.el-icon-loading {
          color: #409eff;
        }
      }
    }
  }
}
.download-slide-enter-active,
.download-slide-leave-active {
  transition: all 0.3s ease;
}
.download-slide-enter,
.download-slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>
