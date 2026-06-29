<template>
  <div class="right-menu" :style="{left:leftPostion+'px',top:topPostion+'px'}" id="right-menu-box" v-if="visible" @contextmenu.prevent.stop="stopeContextMenu">
    <div v-for="(item,index) in menuList" :key="index" class="right-menu-item" :class="[item.line && 'right-menu-line',item.customClass]" @click="menuClickItem(item)"
        v-if="!getHidden(item.hidden)">
        <Icon v-if="item.icon" :name="item.icon.name" :size="item.icon.size" class="right-menu-icon"/>
        <span v-if="!item.line">{{getLabel(item.label)}}</span>
    </div>
  </div>
</template>

<script>
export default {
  components: {  },
    name:'rightMenu',
    props:{
        isLock:{
            type:Boolean,
            default:false
        },
        visible:{
            type:Boolean,
            default:false
        },
        menuList:{
            type:Array,
            default:[]
        },
        mousePosition:{
            type:Array,
            default:[0,0]
        },
        ids:{
            type:String
        }
    },
  data() {
    return {
        leftPostion:-1000,
        topPostion:-1000,
    };
  },
  created() {

  },
  mounted() {

  },
  watch:{
    mousePosition:{
        handler(newVal,oldVal){
            if(newVal){
                console.log(newVal,this.mousePosition)
                setTimeout(()=>{
                    let cWidth = document.getElementById(this.ids).clientWidth,cHeight = document.getElementById(this.ids).clientHeight
                    let rWidth = document.getElementById('right-menu-box')?.clientWidth || 0 , rHeight = document.getElementById('right-menu-box')?.clientHeight || 0
                    if(this.mousePosition[0]+rWidth<cWidth){
                        this.leftPostion = this.mousePosition[0]
                    }else{
                        this.leftPostion = this.mousePosition[0]-rWidth
                    }
                    if(this.mousePosition[1]+rHeight<cHeight){
                        this.topPostion = this.mousePosition[1]
                    }else{
                        this.topPostion = this.mousePosition[1]-rHeight
                    }
                },1000 * 0.001)
            }
        },
        immediate:true
    }
  },
  methods: {
    stopeContextMenu(e){
      e.preventDefault();
    },
    getLabel(labelFun){
        if(typeof labelFun =='function'){
            return labelFun()
        }else{
            return false
        }
    },
    getHidden(hiddenFun){
        if(typeof hiddenFun =='function'){
            return hiddenFun()
        }
    },
    menuClickItem(item){
        if(item.fn){
            item.fn()
        }
        this.$emit('update:visible',!this.visible)
    }
  },
};
</script>

<style scoped lang="scss">
.right-menu{
    position: fixed;
    padding: 10px;
    background-color: #fff;
    border-radius: 5px;
    width: 160px;
    box-shadow: rgba(153,153,153,0.08) 0px 4px 26px;
    z-index: 9999;
    .right-menu-item{
        color: #5e6370;
        display: flex;
        align-items: center;
        font-size: 14px;
        height: 30px;
        align-items: center;
        cursor: pointer;
        padding: 0px 10px;
        border-radius: 6px;
        &:not(.title):hover {
            color: #409EFF !important;
            background: rgba(217,236,255,1) !important;
        }
        .right-menu-icon{
            margin-right: 4px;
        }
    }
    .delete {
        &:not(.title):hover {
            color: $color-danger !important;
            background: rgba($color-danger, 0.2) !important;
        }
    }
    .title {
        background: none;
        cursor: default;
        .right-menu-icon {
            display: none;
        }
        span {
            font-size: 12px !important;
            color: $color-grey4 !important;
            text-transform: uppercase;
        }
    }
    .right-menu-line{
        height: 0px;
        background-color: #eee;
        padding: 1px 0px;
        margin: 5px 10px;
    }
}
</style>
