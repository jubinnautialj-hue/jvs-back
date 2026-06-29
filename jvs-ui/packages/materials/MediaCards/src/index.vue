<template>
  <div style="height: 100%">
    <div class="media-card-header">
      <span class="header-title">{{options.name}}</span>
      <span v-if="options.targetUrl" class="header-more" @click="handleMore">更多<i class="el-icon-arrow-right"/></span>
    </div>
    <div :key="renderIndex" class="media-card-box" :style="`grid-template-columns:${gridColumn};`">
      <div class="media-card-item" :style="`width: ${imgWidth}px;height: ${imgHeight + 30}px;`" v-for="(item, key) in options.mediaItemList" :key="key">
        <div class="media-content">
          <img v-if="item.mediaType === 'image'"
               :width="imgWidth"
               :height="imgHeight" :src="item.filePath"  alt="" @click="viewImg(item.filePath)"/>
          <div
              class="video-box"
              v-if="item.mediaType === 'video'"
              @click="viewVideo(item.filePath)">
            <video
                style="object-fit: fill;"
                :src="item.filePath"
                :width="imgWidth"
                :height="imgHeight"
                :controls="false"
                :autoplay="false"
                :loop="false"
            ></video>
            <div class="play-btn-box">
              <i class="el-icon-caret-right"></i>
            </div>
          </div>
        </div>
        <div class="media-footer" :title="item.title">{{item.title}}</div>
      </div>
    </div>
    <el-dialog
        title=""
        :visible.sync="dialogVisible"
        width="900px"
        append-to-body
        :before-close="handleClose">
        <video
            v-if="dialogVisible"
            style="object-fit: fill;"
            :src="videoUrl"
            width="100%"
            height="100%"
            :controls="true"
            :autoplay="true"
            :loop="false"
        ></video>
    </el-dialog>
    <el-image-viewer v-if="showViewer" :z-index="9999" :on-close="closeViewer" :url-list="previewSrcList"/>
  </div>
</template>

<script>
import ElImageViewer from 'element-ui/packages/image/src/image-viewer'
export default {
  name:'MediaCards',
  components: {
    ElImageViewer
  },
  props:{
    componentSetting: {
      type: Object,
      required: true
    },
    options: {
      type: Object,
      required: true
    },
    h: {
      type: Number,
      default() {
        return 0
      }
    },
    rowHeight: {
      type: Number,
      default() {
        return 0
      }
    },
    w: {
      type: Number,
      default() {
        return 0
      }
    },
    isLock:{
      type:Boolean,
      default:false
    }
  },
  data() {
    return {
      previewSrcList: [],
      dialogVisible: false,
      showViewer: false,
      videoUrl: '',
      imgWidth: 0,
      imgHeight: 0,
      rightWidth: 0,
      gridColumn: 'auto auto',
      renderIndex: 0,
    };
  },
  created() {
    console.log(this.options)
    window.addEventListener('resize',this.onWindowSizeChange)
  },
  mounted() {
    this.onWindowSizeChange()
  },
  watch:{
    w(val) {
      this.$nextTick(() => {
        this.getRightWidth(val)
      })
    }
  },
  computed:{
    colNum() {
      return this.h
    },
    rowNum() {
      return this.w
    },
  },
  methods: {
    getRightWidth(val) {
      const num1 = window.screen.width / 24 * val - 20
      const num2 = document.getElementsByClassName('media-card-item')[0].clientWidth + 10
      let multiple = Math.floor(num1 / num2) > document.getElementsByClassName('media-card-item').length ? document.getElementsByClassName('media-card-item').length : Math.floor(num1 / num2)
      this.gridColumn = ''
      // console.log(multiple)
      for (let i=0;i<multiple;i++) {
        this.gridColumn += 'auto '
      }
      this.renderIndex++
    },
    handleMore() {
      if (this.isLock) {
        window.open(this.options.targetUrl,this.options.jumpMethod)
      }
    },
    viewImg(url) {
      if (!this.isLock) {
        return
      }
      this.previewSrcList = [url]
      this.showViewer = true
    },
    closeViewer() {
      this.showViewer = false
    },
    viewVideo(url) {
      if (!this.isLock) {
        return
      }
      this.videoUrl = url
      this.dialogVisible = true
    },
    handleClose() {
      this.videoUrl = ''
      this.dialogVisible = false
    },
    onWindowSizeChange() {
      this.imgWidth = Math.round((window.screen.width / 24 * 2))
      this.imgHeight = this.imgWidth * 0.6

      this.$nextTick(() => {
        this.getRightWidth(this.w)
      })
    }
  }
};
</script>

<style scoped lang="scss">
.media-card-header{
  display: flex;
  justify-content: space-between;
  padding: 10px 20px 0;
  .header-title{
    font-size: 16px;
    cursor: pointer;
    font-weight: bold;
  }
  .header-more{
    font-size: 14px;
    cursor: pointer;
  }
}
.media-card-box{
  position: relative;
  padding: 0 10px;
  height: calc(100% - 40px);
  display: grid;
  //text-align: center;
  align-items: center;
  flex-wrap: wrap;
  grid-row-gap: 10px;
  grid-column-gap: 10px;
  cursor: pointer;
  overflow: auto;
  .media-card-item{
    //background-color: red;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    font-size: 14px;
    //border: 1px solid #f0f0f0;
    box-shadow: 0 0 6px #f0f0f0;
    border-radius: 4px;
    .media-content{
      img{
        //width: 100%;
        //height: 100%;
      }
      .video-box{
        position: relative;
        .play-btn-box{
          display: flex;
          align-items: center;
          justify-content: center;
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          transition: 0.3s;
          i{
            color: rgba(255, 255, 255, 0);
            font-size: 24px;
            background-color: rgba(255, 255, 255, 0);
            //display: none;
            border-radius: 50%;
            transition: 0.3s;
          }
          &:hover{
            i{
              color: rgba(0, 0, 0, 0.4);
              background-color: #fff;
              transition: 0.3s;
            }
            background: rgba(0, 0, 0, 0.2);
            transition: 0.3s;
          }
        }
      }
    }
    .media-footer{
      padding: 0 10px;
      height: 30px;
      line-height: 28px;
      border-top: 1px solid #f0f0f0;
      //background-color: #1f5fff;
      width: 100%;
      text-overflow: ellipsis;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 1;
      -webkit-box-orient: vertical;
    }
  }
}
.media-card-box::-webkit-scrollbar{
  width: 4px;
  height: 4px;
}
.media-card-box::-webkit-scrollbar-thumb{
  border-radius: 20px;
}
.media-card-box:hover::-webkit-scrollbar{
  background: #eee;
}
.media-card-box:hover::-webkit-scrollbar-thumb{
  background: #ccc;
}
</style>
