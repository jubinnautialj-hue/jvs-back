<template>
  <custom-dialog
        v-model="dialogVisible"
        :title="$hit('添加卡片')"
        width="min(760px, 94vw)"
        height="min(520px, 80vh)"
        @close="close">
        <div class="material-wrapper" v-if="beginLoad">
            <div class="material" v-for="item in materialList" :key="item.value" @click="handleSelect(item)">
                <div class="img-wrapper">
                    <!-- <img v-if="item.img" :src="item.img" /> -->
                    <svg class="icons" aria-hidden="true" v-if="item.img">
                        <use v-bind:xlink:href="item.img"></use>
                    </svg>
                </div>
                <div class="content">
                    <div class="label">{{item.label}}</div>
                    <div class="tips">{{$hit(item.text)}}</div>
                </div>
            </div>
            <div class="material-fake" v-for="item in 4" :key="item"></div>
        </div>
    </custom-dialog>
</template>

<script>
export default {
    name:'cardMaterialSelector',
    props:{
        disabled: {
            type: Boolean,
            default: false
        },
        componentList: {
            type: Array,
            default() {
              return []
            }
        }
    },
  data() {
    return {
        beginLoad:false,
        dialogVisible:false,
        materialList:[],
        imgList:{
            EmptyImg:'#icon-bianji3',
            BijiImg:'#icon-biji',
            SousuoImg:'#icon-sousuo2',
            DayImg:'#icon-riqi',
            XinwenImg:'#icon-xinwen',
            ClockImg:'#icon-shizhong'
        }
    };
  },
  created() {

  },
  mounted() {
      const arr = []
    this.componentList.forEach(item => {
      const obj = {
        // value: obj.type,
        label: item.name,
        text: item.description ? item.description : '',
        // img: this.imgList[it.img],
        img: `#${item.icon}`,
        material:item.type,
        // hidden:obj.hidden,
        componentMetaData: JSON.parse(JSON.stringify(item))
      }
      delete obj.componentMetaData.formAttributes
      arr.push(obj)
    })
    this.materialList = [...arr]
  },
  methods: {
    open(){
        this.dialogVisible = true
        if (!this.beginLoad) this.beginLoad = true
    },
    handleSelect(item){
      this.$emit('materialSelect',item)
      this.close()
    },
    close(){
      this.dialogVisible = false
    }
  }
};
</script>

<style scoped lang="scss">
.material-wrapper{
  padding: 0 80px;
  display: grid;
  grid-template-columns: auto auto;
  //grid-row-gap: 20px;
  grid-column-gap: 20px;
    //display: flex;
    flex-wrap: wrap;
    // justify-content: space-around;
    font-size: 16px;
    //margin-left: 35px;
    //height: 100%;
    //padding-top: 15px;
    .material{
        //width: 300px;
        height: 100px;
        display: flex;
        align-items: center;
        //padding: 20px 20px 15px 15px;
        box-sizing: border-box;
        border: 2px solid #e8eaee;
        //margin-left: 20px;
        margin-bottom: 14px;
        &:hover{
            border: 2px solid #5d9bfc;
            cursor: pointer;
        }
        .img-wrapper{
            width:57px;
            height: 57px;
            img{
                width: 100%;
                height: 100%;
            }
            .icons{
                width: 100%;
                height: 100%;
                vertical-align: -0.15em;
                fill: currentColor;
                overflow: hidden;
            }
        }
        .content{
            margin-left: 20px;
            .label{
                margin-bottom: 0;
                color: #182b50;
                font-size: 14px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }
            .tips{
                color: #8c95a8;
                font-size: 12px;
                margin-top: 7px;
                word-break: break-all;
                width: 185px;
                max-height: 50px;
                overflow: hidden;
            }
        }
    }
}
</style>
