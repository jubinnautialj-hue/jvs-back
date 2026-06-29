<template>
  <div class="uploadFile">
    <el-upload
      v-loading="isStartUploda"
      class="avatar-uploader"
      :action="$homePageOptions.uploadUrl"
      list-type="picture-card"
      :show-file-list="false"
      :data="$homePageOptions.uploadData"
      :headers="$homePageOptions.uploadHeaders"
      :on-success="uploadSuccess"
      :before-upload="beforeAvatarUpload">
      <img v-if="value || isStartUploda" :src="value" class="avatar">
      <i v-else class="el-icon-plus avatar-uploader-icon"></i>
    </el-upload>
  </div>
</template>

<script>
export default {
  name:'uploadFile',
  props:{
    value:{
      type:String,
      default:'https://cn.bing.com//th?id=OHR.AmericanRobin_ZH-CN0667508209_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp&t=1678848687527'
    },
    pramaKey:{
      type:String
    }
  },
  data() {
    return {
      isStartUploda:false
    };
  },
  created() {

  },
  mounted() {

  },
  methods: {
    handlePictureCardPreview(){

    },
    handleRemove(){

    },
    uploadSuccess(response, file, fileList){
      if(response.code==0){
        this.$emit('changeVal',{key:this.pramaKey,val:response.data.fileLink},this)
      }
      this.$nextTick(()=>{
        this.isStartUploda = false
      })
    },
    beforeAvatarUpload(file){
      var img = file.name.substring(file.name.lastIndexOf('.') + 1)
      const isImg = ['jpg','jpeg','png'].indexOf(img)!=-1
      const isLimitM = file.size / 1024 / 1024 < this.$homePageOptions.uploadFileSize;
      if(!isImg){
          this.$oneMessage({
              message:"上传文件的只能是图片",
              type:'error'
          })
      }
      if(!isLimitM){
          this.$oneMessage({
              message:`上传的文件大小不能超过${this.$homePageOptions.uploadFileSize}M`,
              type:'error'
          })
      }
      this.isStartUploda = isImg && isLimitM
      return isImg && isLimitM
    }
  }
};
</script>

<style scoped lang="scss">
.uploadFile{
  .avatar-uploader {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }
  .avatar-uploader .el-upload:hover {
    border-color: #409EFF;
  }
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 148px;
    height: 148px;
    line-height: 148px;
    text-align: center;
  }
  .avatar {
    width: 148px;
    height: 148px;
    border-radius: 5px;
  }
}
</style>
