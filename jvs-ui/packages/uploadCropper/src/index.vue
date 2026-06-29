<template>
   <custom-dialog  
    :value.sync="dialogVisible"
    :title="title"
    width="min(800px, 98vw)"
    height="min(600px, 90vh)">
    <div class="cropper-box">
        <div class="cropper">
            <vue-cropper
                ref="cropper"
                :img="option.img"
                :outputSize="option.outputSize"
                :outputType="option.outputType"
                :info="option.info"
                :canScale="option.canScale"
                :autoCrop="option.autoCrop"
                :autoCropWidth="option.autoCropWidth"
                :autoCropHeight="option.autoCropHeight"
                :fixed="option.fixed"
                :fixedNumber="option.fixedNumber"
                :full="option.full"
                :fixedBox="option.fixedBox"
                :canMove="option.canMove"
                :canMoveBox="option.canMoveBox"
                :original="option.original"
                :centerBox="option.centerBox"
                :height="option.height"
                :infoTrue="option.infoTrue"
                :maxImgSize="option.maxImgSize"
                :enlarge="option.enlarge"
                :mode="option.mode"
                @realTime="realTime"
                @imgLoad="imgLoad">
            </vue-cropper>
        </div>
        <div class="cropper-tools">
            <el-tooltip content="放大" placement="top">
                <i class="el-icon-zoom-in" @click="changeScale(1)"></i>
            </el-tooltip>
            <el-tooltip content="缩小" placement="top">
                <i class="el-icon-zoom-out" @click="changeScale(-1)"></i>
            </el-tooltip>
            <el-tooltip content="左旋转" placement="top">
                <i class="el-icon-refresh-left" @click="rotateLeft"></i>
            </el-tooltip>
            <el-tooltip content="右旋转" placement="top">
                <i class="el-icon-refresh-right"  @click="rotateRight"></i>
            </el-tooltip>
            <el-tooltip content="确定" placement="top">
                <i class="el-icon-check" @click="sureImg"></i>
            </el-tooltip>
        </div>
    </div>
</custom-dialog>
</template>

<script>
import { VueCropper } from 'vue-cropper'
export default {
    name:'uploadCropper',
    components: {
        VueCropper
    },
    props:{
        cropOptions:{
            type:Object,
            default(){
                return {
                }
            }
        },
        title:{
            type:String,
            default:'截图上传'
        }
    },
  data() {
    return {
        defaultOptions:{
            img: '',             //裁剪图片的地址
            outputSize: 1,       //裁剪生成图片的质量(可选0.1 - 1)
            outputType: 'jpeg',  //裁剪生成图片的格式（jpeg || png || webp）
            info: true,          //图片大小信息
            canScale: true,      //图片是否允许滚轮缩放
            autoCrop: true,      //是否默认生成截图框
            autoCropWidth: 230,  //默认生成截图框宽度
            autoCropHeight: 150, //默认生成截图框高度
            fixed: true,         //是否开启截图框宽高固定比例
            fixedNumber: [1.53, 1], //截图框的宽高比例
            full: false,         //false按原比例裁切图片，不失真
            fixedBox: true,      //固定截图框大小，不允许改变
            canMove: false,      //上传图片是否可以移动
            canMoveBox: true,    //截图框能否拖动
            original: false,     //上传图片按照原始比例渲染
            centerBox: false,    //截图框是否被限制在图片里面
            height: true,        //是否按照设备的dpr 输出等比例图片
            infoTrue: false,     //true为展示真实输出图片宽高，false展示看到的截图框宽高
            maxImgSize: 3000,    //限制图片最大宽度和高度
            enlarge: 1,          //图片根据截图框输出比例倍数
            mode: '230px 150px'  //图片默认渲染方式
        },
        dialogVisible:false,
        option:{},
    };
  },
  created() {

  },
  mounted() {

  },
  watch:{
    cropOptions:{
        handler(newVal,oldVal){
            this.option = Object.assign({},this.defaultOptions,this.cropOptions)
        },
        immediate:true
    }
  },
  methods: {
    //初始化函数
    imgLoad (msg) {
      console.log("工具初始化函数====="+msg)
    },
    //图片缩放
    changeScale (num) {
      num = num || 1
      this.$refs.cropper.changeScale(num)
    },
    //向左旋转
    rotateLeft () {
      this.$refs.cropper.rotateLeft()
    },
    //向右旋转
    rotateRight () {
      this.$refs.cropper.rotateRight()
    },
    //实时预览函数
    realTime (data) {
      this.$emit('previewsObj',data)
    },
    sureImg(){
        this.$refs.cropper.getCropBlob(async(data)=>{
            this.$emit('cropperData',data)
        })
    }
  }
};
</script>

<style scoped lang="scss">
.cropper-box{
    flex: 1;
    width: 100%;
    height: 100%;
    position: relative;
    .cropper{
      width: 100%;
      height: 100%;
    }
    .cropper-tools{
        position: absolute;
        bottom: 10px;
        left: 50%;
        transform: translateX(-50%);
        i{
            font-size: 18px;
            cursor: pointer;
            color: #fff;
            margin: 0px 5px;
        }
    }
}
    
</style>
