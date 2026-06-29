<template>
  <div class="background-selector-box inline-form-flex">
    <el-radio-group v-model="mode" @change="handleBackgroundChange">
        <el-radio :label="1">{{$hit('无')}}</el-radio>
        <el-radio :label="2">{{$hit('纯色')}}</el-radio>
        <!-- <el-radio v-if="isSupportIndexDB && isFullScreen" :label="5">{{$hit('本地图片')}}</el-radio> -->
        <el-radio :label="6">{{$hit('本地图片')}}</el-radio>
        <el-radio :label="3">{{$hit('网络图片')}}</el-radio>
        <!-- <el-radio :label="4">{{$hit('随机图片')}}</el-radio> -->
    </el-radio-group>
    <div class="color-wrapper" v-if="mode === 2">
        <div class="form-row-control">
            <div class="label">{{$hit('选择颜色')}}</div>
            <div class="content">
                <standard-color-picker
                    :color.sync="color"
                    :showAlpha="true"
                    @change="handleBackgroundChange"
                ></standard-color-picker>
            </div>
        </div>
    </div>
    <div class="online-img-wrapper" v-if="mode === 3 || mode === 6">
        <div class="form-row-control">
            <div class="label">URL</div>
            <div class="content" style="flex-wrap: wrap">
                <div class="flex-center-y" style="width: 100%" v-if="mode===3">
                    <el-input
                        v-model="bgImg"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 8 }"
                        :placeholder="isFullScreen ? $hit('输入图片或动态壁纸URL') : $hit('输入图片URL')"
                        @change="handleBackgroundChange"
                    />
                    <Tips
                        v-if="isFullScreen"
                        :content="$hit('bgImgTips')"
                    ></Tips>
                </div>
                <div v-if="mode===6">
                    <uploadFile :value="bgImg" @changeVal="changeVal"></uploadFile>
                </div>
            </div>
        </div>
    </div>
    <div class="random-img-wrapper" v-if="mode === 4">
        <div class="form-row-control">
        <label class="label">{{$hit('图片源')}}</label>
        <div class="content">
            <el-radio-group v-model="randomSource" @change="handleBackgroundChange">
            <el-radio label="sina" class="row-radio">{{$hit('新浪')}}</el-radio>
            <el-radio label="unsplash" class="row-radio">UNSPLASH</el-radio>
            <el-radio
                v-if="isFullScreen"
                label="personal"
                :disabled="!wallpaperCollectionList || wallpaperCollectionList.length < 2"
                class="row-radio">{{$hit('个人壁纸库')}}</el-radio>
            </el-radio-group>
        </div>
        </div>
        <template v-if="randomSource === 'unsplash'">
        <div class="form-row-control">
            <label class="label" style="line-height: 32px">{{$hit('关键词')}}</label>
            <div class="content" style="flex-wrap: wrap;">
            <el-radio-group v-model="imgType" @change="handleBackgroundChange">
                <el-radio v-for="(value, key) in BG_IMG_TYPE_MAP" :key="key" :label="key">{{
                store.global.lang === 'zh-cn' ? value: key
                }}</el-radio>
                <el-radio label="Custom">{{$hit('自定义')}}</el-radio>
            </el-radio-group>
            <el-input
                v-if="imgType === 'Custom'"
                v-model.lazy="customImgType"
                :placeholder="$hit('自定义关键词(英文)')"
                @change="handleBackgroundChange"
            ></el-input>
            </div>
        </div>
        <div class="form-row-control">
            <label class="label">{{$hit('国内镜像')}}</label>
            <div class="content">
            <el-switch v-model="mirror" @change="handleBackgroundChange"></el-switch>
            </div>
        </div>
        </template>
        <template v-if="randomSource === 'personal'">
        <div class="form-row-control">
            <label class="label"></label>
            <div class="content">
            <PersonalWallpaper />
            </div>
        </div>
        </template>
        <div v-if="isFullScreen" class="form-row-control" >
        <label class="label">{{$hit('定时刷新')}}</label>
        <div class="content flex-center-y">
            <el-input-number
            v-model="duration"
            :min="0"
            :max="3600"
            controls-position="right"
            @change="handleBackgroundChange"
            ></el-input-number>
            <Tips :content="$hit('refreshDurationTips')" />
        </div>
        </div>
        <div v-if="isFullScreen" class="form-row-control" >
        <label class="label ellipsis">{{$hit('操作按钮')}}</label>
        <div class="content flex-center-y">
            <el-switch v-model="showRefreshBtn" style="width: 150px" />
            <Tips :content="$hit('refreshBtnTips')" />
        </div>
        </div>
    </div>
    <div class="local-img-wrapper" v-if="mode === 5">
        <div class="form-row-control" style="margin: 10px 0 20px;">
        <label class="label">{{$hit('本地图片库')}}</label>
        <div class="content">
            <LocalImg />
        </div>
        </div>
        <div class="form-row-control">
        <label class="label">{{$hit('定时刷新')}}</label>
        <div class="content flex-center-y">
            <el-input-number
            v-model="duration"
            :min="0"
            :max="3600"
            controls-position="right"
            @change="handleBackgroundChange"
            ></el-input-number>
            <Tips :content="$hit('refreshDurationTips')" />
        </div>
        </div>
        <div v-if="isFullScreen" class="form-row-control">
        <label class="label ellipsis">{{$hit('操作按钮')}}</label>
        <div class="content flex-center-y">
            <el-switch v-model="showRefreshBtn" style="width: 150px" />
            <Tips :content="$hit('refreshBtnTips')" />
        </div>
        </div>
    </div>
  </div>
</template>

<script>
export default {
    name:'backgroundSelector',
    props: {
        background: {
            type: String,
            default: ''
        },
        w: {
            type: Number,
            default: 4
        },
        h: {
            type: Number,
            default: 4
        },
        positionMode: {
            type: Number,
            default: 1
        },
        isFullScreen: {
            type: Boolean,
            default: false
        },
    },
  data() {
    return {
        mode:1,
        color:'rgba(255,255,255,1)',
        bgImg:'',
        randomSource:'sina',
        imgType:'Nature',
        customImgType:'',
        mirror:true,
        duration:0,
        wallpaperCollectionList:[]
    };
  },
  created() {

  },
  mounted() {

  },
  watch:{
    h:{
        handler(){
            this.handleBackgroundChange()
        }
    },
    w:{
        handler(){
            this.handleBackgroundChange()
        }
    },
    positionMode:{
        handler(){
            this.handleBackgroundChange()
        }
    },
    background:{
        handler(newVal,oldVal){
            if (!newVal || newVal.includes('transparent')) {
                this.mode = 1
            } else if (newVal.includes('url')) {
                const getURL = (input) => {
                    const reg = /url\(['"]?(.*?)['"]?\)/
                    const match = input.match(reg)
                    return match && match.length >= 2 ? match[1] : ''
                }
                const url = getURL(newVal)
                if (url.includes('/api/randomPhoto')) {
                    const _url = new URL(url)
                    if (url.includes('sina')) {
                    this.randomSource = 'sina'
                    this.duration = ~~(_url.searchParams.get('duration') || 0)
                    } else if (url.includes('personal')) {
                    this.randomSource = 'personal'
                    this.duration = ~~(_url.searchParams.get('duration') || 0)
                    } else {
                    const keyword = _url.searchParams.get('keyword')
                    if (keyword) {
                        if (Object.keys(BG_IMG_TYPE_MAP).includes(keyword)) {
                            this.imgType = keyword
                        } else {
                            this.imgType = 'Custom'
                            this.customImgType = keyword
                        }
                    }
                    this.mirror = url.includes('mirror')
                    this.randomSource = 'unsplash'
                    this.duration = ~~(_url.searchParams.get('duration') || 0)
                    }
                    this.mode = 4
                } else if (url.includes('localhost/localImg')) {
                    const _url = new URL(url)
                    this.duration = ~~(_url.searchParams.get('duration') || 0)
                    this.mode = 5
                } else {
                    this.mode = 3
                    this.bgImg = url
                }
            } else {
                this.mode = 2
                this.color = newVal || 'rgba(255,255,255,1)'
            }
        },
        immediate:true
    },
  },
  computed:{
    getW(){
        if (this.isFullScreen) {
            return window.innerWidth
        } else if (this.positionMode === 2) {
            return this.w
        } else {
            return this.w * 80
        }
    },
    getH(){
        if (this.isFullScreen) {
            return window.innerHeight
        } else if (this.positionMode === 2) {
            return this.h
        } else {
            return this.h * 80
        }
    }
  },
  methods: {
    changeVal(val){
        this.bgImg = val.val
    },
    handleRecommendSelect(url){
        this.bgImg = url
        this.handleBackgroundChange()
    },
    handleBackgroundChange(){
        let output = ''
        switch (this.mode) {
            case 1:
                output = 'transparent'
            break
            case 2:
                output = this.color
            break
            case 3:
            case 6:
                output = `#242428 url(${this.bgImg}) center center / cover`
            break
            case 4:
                if (this.randomSource === 'sina') {
                    output = `#242428 url(https://kongfandong.cn/api/randomPhoto/sina?duration=${this.duration}) center center / cover`
                } else if (this.randomSource === 'personal') {
                    output = `#242428 url(https://kongfandong.cn/api/randomPhoto/personal?duration=${this.duration}) center center / cover`
                } else {
                    const keyword = this.imgType === 'Custom' ? this.customImgType: this.imgType
                    const mirrorStr = this.mirror ? '&type=mirror' : ''
                    output = `#242428 url(https://kongfandong.cn/api/randomPhoto?keyword=${keyword}&w=${this.getW}&h=${this.getH}${mirrorStr}&duration=${this.duration}) center center / cover`
                }
            break
            case 5:
                output = `#242428 url(https://localhost/localImg?duration=${this.duration})`
            break
        }
        this.$emit('update:background', output)
    }
  }
};
</script>

<style scoped lang="scss">
.background-selector-box{
    .el-radio-group {
        display: inline-flex;
        margin-right: 10px;
        align-items: center;
        label {
            cursor: pointer;
            padding-left: 3px;
        }
    }
    .random-img-wrapper {
        margin-top: 10px;
    }
    :deep(.el-radio) {
        margin-bottom: 5px;
        margin-right: 24px;
    }
    .el-radio {
        display: block;
        height: 32px;
        line-height: 32px;
        margin-bottom: 0;
    }
}
</style>
