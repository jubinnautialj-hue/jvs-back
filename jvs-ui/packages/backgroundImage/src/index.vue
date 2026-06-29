<template>
  <div
    class="wrapper"
    :style="{
      background: !backgroundURL ? background : 'none'
    }"
  >
    <div v-if="videoURL" :class="['bg-media-wrapper', showBackgroundEffect && 'system-bg-effect']">
      <!-- <video
        class="bg-video"
        :src="videoURL"
        autoplay
        muted
        loop
        playsinline
        disablePictureInPicture
        disableRemotePlayback
        @error="handleVideoError"
        :style="{
          filter: filter
        }"
      ></video> -->
    </div>
    <div
      v-else-if="realBackgroundURL"
      :class="['bg-media-wrapper', showBackgroundEffect && 'system-bg-effect']"
    >
      <div :style="`width:100%;height:100%;filter:${filter}`">
        <img
          class="global-bg-img"
          :src="realBackgroundURL"
          style="width: 100%; height: 100%; object-fit: cover; opacity: 0"
          ref="bgDom"
          @load="handleImgLoad"
          @error="hanleImgError"
        />
      </div>
      <div class="icon-wrapper">
        <Icon
          v-if="showRefreshBtn && (backgroundURL.includes('randomPhoto') || backgroundURL.includes('localImg'))"
          name="refresh"
          class="btn-refresh"
          :title="$hit('刷新背景图')"
          size="20"
          @click="refresh"
        ></Icon>
        <Icon
          :class="['btn-heart', hasLike && 'active']"
          :title="$hit('喜欢')"
          size="22"
          @click="like"
          v-if="showRefreshBtn && backgroundURL.includes('randomPhoto')"
          name="`${hasLike ? 'heart-fill': 'heart'}`"
        /></Icon>
      </div>
    </div>
  </div>
</template>

<script>
import { getFileType } from '@/utils'
const getURL = (input) => {
  const reg = /url\(['"]?(.*?)['"]?\)/      
  const match = input.match(reg)
  return match && match.length >= 2 ? match[1] : ''
}
export default {
    name:'BackgroundImage',
    props:{
        background:{
            type:String,
            default:''
        },
        realBackgroundURL:{
            type:String,
            default:''
        },
        showBackgroundEffect:{
            type:Boolean,
            default:false
        },
        showRefreshBtn:{
            type:Boolean,
            default:true
        },
        filter:{
            type:String
        }
    },
  data() {
    return {
        time:+new Date(),
        timer:null,
        loadFirstError:false,
        leaveAnimation:null
    };
  },
  created() {

  },
  mounted() {

  },
  watch:{
    background:{
        handler(newVal,oldVal){
            if (newVal && (newVal.includes('randomPhoto') || newVal.includes('localImg'))) {
                if (this.timer) clearInterval(this.timer)
                const url = getURL(newVal)
                const _url = new URL(url)
                const duration = ~~(_url.searchParams.get('duration') || 0)
                if (duration) {
                    this.timer = setInterval(() => {
                        this.refresh()
                    },duration < 30 ? 30 * 1000 : duration * 1000)
                }
            }
        },
        immediate:true
    }
  },
  computed:{
    backgroundURL(){
        if(this.background && this.background.includes('url')){
            let url = getURL(this.background)
            url += `${url.includes('?') ? '&' : '?'}t=${this.time}`
            return url
        }
        return ''
    },
    videoURL(){
        if (this.background && this.background.includes('url')) {
            const url = getURL(this.background)
            const fileType = getFileType(url)
            if (fileType &&['mp4', 'avi', 'wmv', 'mpg', 'mpeg', 'mov', 'ts', 'flv', 'webm'].includes(fileType)) {
                return url
            }
        }
        return ''
    },
    hasLike(){
        let bgURL = this.realBackgroundURL
        if (bgURL.includes('ixid=')) {
            // unsplash随机图的ixid每次不一样，为识别为同一张图需去除
            bgURL = bgURL.replace(/&ixid=.+?&/, '&')
        }
        return ~this.$store.state.baseConfig.wallpaperCollectionList.indexOf(bgURL)
    }
  },
  watch:{
    backgroundURL:{
        async handler(newVal,oldVal){
            if (newVal && newVal.includes('randomPhoto')) {
                try {
                    let result
                    if (newVal.includes('personal')) {
                        // 从个人壁纸库随机一张
                        const index = ~~(Math.random() * this.$store.state.baseConfig.wallpaperCollectionList.length)
                        result = this.$store.state.baseConfig.wallpaperCollectionList[index]
                        if (result === this.realBackgroundURL) {
                            // 随机出的图片跟原本一致会导致onload不执行
                            // result = 'https://dogefs.s3.ladydaily.com/~/source/unsplash/photo-1612342222980-e549ae573834'
                            setTimeout(() => {
                                if (this.$refs.bgDom.style) this.$refs.bgDom.style.filter = 'blur(0)'
                            }, 500)
                        }
                    } else {
                        let target = newVal
                        if (process.env.NODE_ENV) {
                            target = target.replace('https://kongfandong.cn', '/api') // For Dev Proxy
                        }
                        const res = await fetch(`${target}&json=1`)
                        const json = await res.json()
                        result = json.url
                    }
                    this.$emit('update:realBackgroundURL',newVal)
                    localStorage.setItem('cacheBackgroundURL', result)
                } catch (e) {
                    this.$emit('update:realBackgroundURL',newVal)
                    localStorage.removeItem('cacheBackgroundURL')
                }
            } else if (newVal && newVal.includes('localImg')) {
                const imgList = await localImg.keys()
                const index = ~~(Math.random() * imgList.length)
                const result = await localImg.getItem(imgList[index]) || null
                if (result) {
                    if (result === realBackgroundURL.value) {
                    setTimeout(() => {
                        if (bgDom.value.style) bgDom.value.style.filter = 'blur(0)'
                    }, 500)
                    }
                    this.$emit('update:realBackgroundURL',newVal)
                } else {
                    this.$emit('update:realBackgroundURL','https://dogefs.s3.ladydaily.com/~/source/unsplash/photo-1612342222980-e549ae573834')
                }
            } else {
                this.$emit('update:realBackgroundURL',newVal)
                localStorage.removeItem('cacheBackgroundURL')
            }
        },
        immediate:true
    }
  },
  methods: {
    async refresh(){
        let bgDom = this.$refs.bgDom
        this.time = +new Date()
        if (bgDom) {
            if (!bgDom.animate) return
        }
        try {
            this.leaveAnimation = bgDom.animate(
            [
                {
                filter: 'blur(20px)',
                tarnsform: 'scale(1,1)'
                },
                {
                filter: 'blur(60px)'
                }
            ],
            400
            )
            if (this.leaveAnimation) {
                await this.leaveAnimation.finished
            }
            bgDom.style.filter = 'blur(60px)'
        } catch {
            // cancel
            console.log('cancel')
        }
    },
    like(){

    },
    hanleImgError(){
        const localCacheImg = localStorage.getItem('cacheBackgroundURL')
        if (!this.loadFirstError && localCacheImg) {
            this.$emit('update:realBackgroundURL',localCacheImg)
            this.loadFirstError = true
        }
    },
    async handleImgLoad(){
        let bgDom = this.$refs.bgDom
        if (bgDom) {
        if (bgDom.style) bgDom.style.opacity = 1
        if (!bgDom.animate) return
        if (this.leaveAnimation) this.leaveAnimation.cancel()
        const changeAnimation = bgDom.animate(
        [
            {
            filter: 'blur(20px)',
            tarnsform: 'scale(1,1)'
            },
            {
            filter: 'blur(0)',
            tarnsform: 'scale(1)'
            }
        ],
        400
        )
        await changeAnimation.finished
        if (bgDom.style) bgDom.style.filter = 'blur(0)'
    }
    }
  }
};
</script>

<style scoped lang="scss">
.wrapper {
  position: absolute;
  width: 100%;
  height: 100%;
  font-size: 0;
  overflow: hidden;
  .bg-media-wrapper {
    width: 100%;
    height: 100%;
    overflow: hidden;
    transition: transform 0.3s ease-in-out, filter 0.3s ease-in-out;
    &.system-bg-effect {
      filter: blur(10px);
      transform: scale(1.08);
    }
  }
}
.bg-img {
  width: 100%;
  height: 100%;
  :deep(.el-image__inner) {
    transform: scale(1.02);
    filter: var(--filter);
  }
}
.bg-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.bg-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, #12c2e9, #c471ed, #f64f59);
  background-size: 400% 400%;
  animation: bgmove 15s ease infinite;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  color: #e7e7e7;
  font-size: 14px;
  padding-bottom: 30px;
}
.icon-wrapper {
  position: absolute;
  left: 16px;
  bottom: 16px;
  display: flex;
  align-items: center;
  font-size: 20px;
  z-index: 20;
  .btn-refresh {
    color: $color-white;
    cursor: pointer;
    margin-right: 16px;
    &:hover {
      color: $color-grey5;
    }
  }

  .btn-heart {
    color: $color-white;
    cursor: pointer;
    &:hover {
      color: $color-grey5;
    }
    &.active {
      color: $color-danger;
      &:hover {
        color: $color-danger;
      }
    }
  }
}
</style>
