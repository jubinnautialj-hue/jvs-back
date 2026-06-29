<template>
  <div class="component-form">
    <el-form
      v-bind="$attrs"
      :model="formData"
      :rules="formRules"
      ref="form"
      size="mini"
      label-position="top"
      class="component-form">
      <el-form-item
        v-for="(item, key) in formAttributes"
        :label="item.label"
        :key="key"
        :prop="item.prop"
        class="form-item-control"
      >
        <!-- select选择框 -->
        <el-select style="width: 100%" v-if="item.type === 'select'" v-model="formData[item.prop]">
          <el-option
            v-for="(it, index) in item.dicData"
            :key="index"
            :label="it.label"
            :value="it.value">
          </el-option>
        </el-select>
        <!-- select多选框 -->
        <el-select multiple style="width: 100%" v-if="item.type === 'selectMultiple'" v-model="formData[item.prop]">
          <el-option
            v-for="(it, index) in item.dicData"
            :key="index"
            :label="it.label"
            :value="it.value">
          </el-option>
        </el-select>
        <!-- input输入框 -->
        <el-input v-if="item.type === 'input'" v-model="formData[item.prop]"></el-input>
        <!-- textarea输入框 -->
        <el-input v-if="item.type === 'textarea'" type="textarea" v-model="formData[item.prop]"></el-input>
        <!-- radio单选框 -->
        <el-radio-group v-if="item.type === 'radio'" v-model="formData[item.prop]">
          <el-radio v-for="(it, index) in item.dicData" :key="index" :label="it.value">{{ it.label}}</el-radio>
        </el-radio-group>
        <!-- 颜色方案 -->
        <el-popover
            v-if="item.type === 'color'"
            placement="bottom"
            width="340"
            popper-class="color-popper"
            :visible-arrow="false"
            trigger="click">
            <div class="color-select-list">
              <div v-for="(itemColors, key) in systemColors" :key="'system' + key" class="form-colors" @click="handleSelectColor(itemColors)">
                <div v-for="(it, key) in itemColors" :key="'system-item' + key" class="colors-item" :style="`background-color: ${it}`"></div>
              </div>
            </div>
            <div slot="reference" class="form-colors">
              <div v-for="(item, key) in formData[item.prop]" :key="key" class="colors-item" :style="`background-color: ${item}`"></div>
            </div>
        </el-popover>
        <!-- 图片列表 -->
        <div v-if="item.type === 'image'" class="card-img">
          <div style="width: 64px; height: 64px; margin-bottom: 16px;" v-loading="uploadLoading">
            <img :src="formData[item.prop]" alt=""/>
          </div>
          <div class="card-img-list">
            <img v-for="(img, key) in cardImgList" :key="key" :src="img" alt="" @click="selectCardImg(img)"/>
            <el-upload
              class="img-uploader"
              action="/mgr/jvs-auth/upload/jvs-public"
              :data="$homePageOptions.uploadData"
              :headers="$homePageOptions.reqHeaders"
              :show-file-list="false"
              :on-success="handleSuccess"
              :before-upload="beforeUpload">
              <i class="el-icon-upload2"></i>
            </el-upload>
          </div>
        </div>
        <!-- 背景图列表 -->
        <div v-if="item.type === 'backImg'" class="back-img">
          <div style="margin-bottom: 16px;" v-loading="uploadLoading">
            <img :src="formData[item.prop]" alt=""/>
          </div>
          <div class="back-img-list">
            <img v-for="(img, key) in backImgList" :key="key" :src="img" alt="" @click="selectBackImg(img)"/>
            <el-upload
              class="img-uploader"
              action="/mgr/jvs-auth/upload/jvs-public"
              :data="$homePageOptions.uploadData"
              :headers="$homePageOptions.reqHeaders"
              :show-file-list="false"
              :on-success="handleBackSuccess"
              :before-upload="beforeUpload">
              <i class="el-icon-upload2"></i>
            </el-upload>
          </div>
        </div>
        <!-- 字体颜色 -->
        <div v-if="item.type === 'textColor'" class="text-color">
          <el-color-picker
            v-model="formData[item.prop]"
            size="mini"
            show-alpha
            :predefine="predefineColors"
          ></el-color-picker>
          <div style="line-height: 28px;height: 28px;margin-left: 10px;">{{formData[item.prop]}}</div>
        </div>
        <!-- 表单组 -->
        <div v-if="item.type === 'list'">
          <el-form-item
            v-for="(it, itKey) in item.column"
            :label="it.label"
            :key="itKey"
            :prop="it.prop"
            class="form-item-control"
          >
            <!-- banner列表 -->
            <div v-if="it.type === 'banner' && formData[item.prop] && formData[item.prop].length > 0" class="banner-img" v-loading="uploadBannerLoading && currentKey === key">
              <div style="height: 140px;width: 520px;box-shadow: 0 0 5px #e7e7e7;">
                <img v-if="formData[item.prop][key][it.prop]" :src="formData[item.prop][key][it.prop]" alt=""/>
                <img v-else :src="bannerList[0]" alt=""/>
              </div>
              <div class="banner-list">
                <img v-for="(img, bannerKey) in bannerList" :key="bannerKey" :src="img" alt="" @click="selectBanner(img, formData[item.prop][key], [it.prop])"/>
                <el-upload
                  class="img-uploader"
                  action="/mgr/jvs-auth/upload/jvs-public"
                  :data="$homePageOptions.uploadData"
                  :headers="$homePageOptions.reqHeaders"
                  :show-file-list="false"
                  :on-success="handleBannerSuccess"
                  :before-upload="beforeBannerUpload">
                  <i class="el-icon-upload2" @click="bannerUploadClick(formData[item.prop][key], [it.prop], key)"></i>
                </el-upload>
                <i v-if="key > 0" class="el-icon-remove-outline remove-icon" @click="removeBanner(key)"/>
              </div>
            </div>
            <!-- input输入框 -->
            <el-input v-if="it.type === 'input'" v-model="formData[item.prop][key][it.prop]">
              <el-color-picker
                slot="append"
                v-model="formData[item.prop][key]['textColor']"
                size="mini"
                show-alpha
                :predefine="predefineColors"
              ></el-color-picker>
            </el-input>
          </el-form-item>
        </div>
      </el-form-item>
      <el-button v-if="formAttributes.length < 3 && formData.bannerList" style="color: #3471ff;" type="text" size="mini" icon="el-icon-plus" @click="addBanner()">添加一项</el-button>
    </el-form>
  </div>
</template>

<script>
import {systemColors} from "../systemColors";
import {cardImgList} from "../cardImg";
import {backImgList} from "../backImg";
import {bannerList} from "../bannerImg";
export default {
  name: "CustomForm",
  props:{
    formAttributes: {
      type: Array,
      default() {
        return []
      }
    },
    defaultBannerList: {
      type: Array,
      default() {
        return []
      }
    },
    // formData: {
    //   type: Object,
    //   default() {
    //     return {}
    //   }
    // },
    customFormData: {
      type: Object,
      default() {
        return {}
      }
    }
  },
  computed: {
    formRules() {
      const rules = {}
      const arr = [...this.formAttributes]
      arr.forEach(item => {
        if (item.validator) {
          item.validator.forEach(it => {
            switch (it.type) {
              case 'required':
                rules[item.prop] = [{
                  required: true,
                  validator: (rule, value, callback) => {
                    if (!this.formData[item.prop]) {
                      callback(new Error(it.message))
                    }
                    callback();
                  },
                }]
                break;
              default:break;
            }
          })
        }
      })
      return rules
    },
  },
  data() {
    return {
      predefineColors: [
        '#ffd700',
        '#ff8c00',
        '#ff4500',
        '#c71585',
        '#FF99CC',
        '#FF6666',
        '#CCCCFF',
        '#CCCCCC',
        '#99CCFF',
        '#99CC99',
        '#90ee90',
        '#66CC66',
        '#669933',
        '#663366',
        '#490954',
        '#3471ff',
        '#333300',
        '#1e90ff',
        '#00ced1',
        '#003399'
      ],
      formData: {},
      cardImgList: cardImgList,
      backImgList: backImgList,
      bannerList: bannerList,
      systemColors: systemColors,
      uploadLoading: false,
      uploadBannerLoading: false,
      currentBannerForm: {},
      currentProp: '',
      currentKey: 0,
    }
  },
  watch: {
    formData: {
      handler(newVal) {
        this.$emit('changeFormData', newVal)
      },
      deep: true
    },
  },
  created() {
  },
  mounted() {
    if (this.customFormData) {
      setTimeout(() => {
        this.$set(this, 'formData', JSON.parse(JSON.stringify(this.customFormData)))
        this.$forceUpdate()
      }, 100)
    }
  },
  methods: {
    // 上传文件前钩子
    beforeUpload(file) {
      const png = 'image/png'
      const jpg = 'image/jpeg'
      const gif = 'image/gif'
      const isPassType = (file.type !== png && file.type !== jpg && file.type !== gif)
      const isLt2M = file.size / 1024 / 1024 < 50;
      if (isPassType && !isLt2M) {
        this.$message.error('上传文件的格式只能是 png、jpg、jpeg、gif 格式，且文件大小不能超过 50MB！')
      } else if (isPassType) {
        this.$message.error('文件格式错误，仅支持上传png、jpg、jpeg、gif格式文件!')
      } else if (!isLt2M) {
        this.$message.error('上传的文件大小不能超过 50MB!')
      }
      this.uploadLoading = !isPassType && isLt2M
      return !isPassType && isLt2M
    },
    handleSuccess(res, file){
      this.uploadLoading = false
      if (res && res.code == 0) {
        this.$set(this.formData, 'cardImg', res.data.fileLink)
      }
    },
    handleBackSuccess(res, file){
      this.uploadLoading = false
      if (res && res.code == 0) {
        this.$set(this.formData, 'backImg', res.data.fileLink)
      }
    },
    // 上传文件前钩子
    beforeBannerUpload(file) {
      this.$set(this.currentBannerForm, this.currentProp, '')
      const png = 'image/png'
      const jpg = 'image/jpeg'
      const gif = 'image/gif'
      const isPassType = (file.type !== png && file.type !== jpg && file.type !== gif)
      const isLt2M = file.size / 1024 / 1024 < 50;
      if (isPassType && !isLt2M) {
        this.$message.error('上传文件的格式只能是 png、jpg、jpeg、gif 格式，且文件大小不能超过 50MB！')
      } else if (isPassType) {
        this.$message.error('文件格式错误，仅支持上传png、jpg、jpeg、gif格式文件!')
      } else if (!isLt2M) {
        this.$message.error('上传的文件大小不能超过 50MB!')
      }
      this.uploadBannerLoading = !isPassType && isLt2M
      return !isPassType && isLt2M
    },
    selectCardImg(img) {
      this.$set(this.formData, 'cardImg', img)
    },
    selectBackImg(img) {
      this.$set(this.formData, 'backImg', img)
    },
    handleBannerSuccess(res, file){
      this.uploadBannerLoading = false
      if (res && res.code == 0) {
        this.$set(this.currentBannerForm, this.currentProp, res.data.fileLink)
      }
    },
    handleSelectColor(colors) {
      this.$set(this.formData, 'colorList', colors)
    },
    bannerUploadClick(formData, prop, key) {
      this.currentBannerForm = formData
      this.currentProp = prop
      this.currentKey = key
    },
    selectBanner(img, formData, prop) {
      if (formData && prop) {
        this.$set(formData, prop, img)
      } else {
        this.$set(this.formData, 'banner', img)
      }
    },
    removeBanner(key) {
      this.formData['bannerList'].splice(key, 1)
      this.$emit('removeBanner', key)
    },
    addBanner() {
      this.formData['bannerList'].push(
          {
            banner: this.bannerList[0],
            title: '',
            content: '',
            textColor: 'rgba(51, 51, 51, 1)',
          }
      )
      this.$emit('addBanner')
    },
    getFormData() {
      return this.formData
    }
  }
}
</script>

<style lang="scss" scoped>
.text-color{
  display: flex;
  align-items: center;
}
.banner-img{
  img{
    //height: 40px;
    height: 100%;
    width: 100%;
  }
  .banner-list{
    margin: 10px 0;
    width: 100%;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    img{
      height: 32px;
      width: 128px;
      cursor: pointer;
      margin-right: 20px;
      box-shadow: 0 0 5px #e7e7e7;
    }
    .remove-icon{
      font-size: 16px;
      color: #3471ff;
      cursor: pointer;
    }
    .img-uploader{
      margin-right: 20px;
      i{
        padding: 8px;
        border-radius: 4px;
        color: #a2a3a5;
        border: 1px dashed #a2a3a5;
        &:hover{
          border-color: #3471ff;
          color: #3471ff;
        }
      }
    }
  }
}
.back-img{
  img{
    height: 100px;
    width: 210px;
  }
  .back-img-list{
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    img{
      width: 68px;
      height: 32px;
      cursor: pointer;
      margin-right: 20px;
    }
  }
}
.card-img{
  img{
    width: 64px;
    height: 64px;
  }
  .card-img-list{
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    img{
      width: 32px;
      height: 32px;
      cursor: pointer;
      margin-right: 20px;
    }
  }
}
.img-uploader{
  i{
    padding: 8px;
    border-radius: 4px;
    color: #a2a3a5;
    border: 1px dashed #a2a3a5;
    &:hover{
      border-color: #3471ff;
      color: #3471ff;
    }
  }
}
.form-colors{
  display: flex;
  align-items: center;
  justify-content: center;
  height: 28px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0);
  &:hover{
    border: 1px solid #DCDFE6;
  }
  .colors-item{
    width: 60px;
    height: 8px;
  }
}
.el-popper.color-popper{
  padding: 0!important;
  border: none!important;
  .color-select-list{
    border-radius: 4px;
    z-index: 2;
    //background-color: #0f1620;
    width: 100%;
    height: 200px;
    overflow-y: auto;
    &::-webkit-scrollbar{
      display: none;
    }
  }
}
.component-form{
  padding: 0 10px;
  .el-form-item--mini.el-form-item{
    margin-bottom: 10px;
  }
  .form-item-control{
  }
}
</style>
