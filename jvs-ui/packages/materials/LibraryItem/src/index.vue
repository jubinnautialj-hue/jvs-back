<template>
  <div class="library-item" :class="[isLock && 'libray-item-cursor']" :style="{
    boxShadow:componentSetting.boxShadow||'0 4px 26px #99999914',
    borderRadius:`${componentSetting.borderRadius || '5'}px`,
    width:boxHW.width,
    height:boxHW.height
  }">
    <div class="library-title" :style="{
        color:componentSetting.titleFontColor,
        fontSize:componentSetting.titleFontSize+'px'
    }">
        <svg class="library-title-svg" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" fill="none" version="1.1" width="32" height="29" viewBox="0 0 32 29"><g style="mix-blend-mode:passthrough"><g><path d="M4,0C1.79086,0,0,1.79086,0,4L0,17C0,19.2091,1.79086,21,4,21L9,21L9,23L7.5,23C6.67157,23,6,23.6716,6,24.5C6,25.3284,6.67157,26,7.5,26L16.5,26C17.3284,26,18,25.3284,18,24.5C18,23.6716,17.3284,23,16.5,23L15,23L15,21L20,21C22.2091,21,24,19.2091,24,17L24,4C24,1.79086,22.2091,0,20,0L4,0Z" fill-rule="evenodd" fill="#3092FB" fill-opacity="1"/></g><g><rect x="6" y="5" width="12" height="4" rx="2" fill="#FFFFFF" fill-opacity="0.30000001192092896"/></g><g><rect x="6" y="13" width="12" height="4" rx="2" fill="#FFFFFF" fill-opacity="0.30000001192092896"/></g><g><ellipse cx="22" cy="19" rx="10" ry="10" fill="#65DFEA" fill-opacity="1"/></g><g><path d="M21.01198,24.6737C21.31138,24.8533,21.5509,24.7335,21.5509,24.374299999999998L21.5509,20.18263C21.5509,19.823349999999998,21.31138,19.46407,21.01198,19.28443L17.538921,17.12874C17.239521,17.00898,17,17.12874,17,17.42814L17,21.61976C17,21.97904,17.239521,22.33832,17.538921,22.517960000000002L21.01198,24.6737ZM18.07784,15.81138C17.778442,15.99102,17.778442,16.23054,18.07784,16.41018L21.49102,18.56587C21.79042,18.74551,22.20958,18.74551,22.50898,18.62575L25.922159999999998,16.88922C26.22156,16.70958,26.22156,16.47006,25.922159999999998,16.29042L22.329340000000002,14.134729C22.08982,13.9550896,21.61078,13.9550896,21.31138,14.134731L18.07784,15.81138ZM22.50898,24.374299999999998C22.50898,24.7335,22.7485,24.8533,23.0479,24.6737L26.461080000000003,22.93713C26.76048,22.75749,27,22.3982,27,22.038919999999997L27,17.84731C27,17.48802,26.76048,17.36826,26.461080000000003,17.5479L23.0479,19.28443C22.7485,19.46407,22.50898,19.823349999999998,22.50898,20.18263L22.50898,24.374299999999998Z" fill="#FFFFFF" fill-opacity="1"/></g></g></svg>
        <span>{{options.name}}</span>
    </div>
    <div class="library-List">
        <div v-for="(item,index) in options.children" :key="index" class="library-list-item"  :style="{
            color:componentSetting.descFontColor,
            fontSize:componentSetting.descFontSize+'px'
        }" @click="openLibraryItem(item)">
            <span>{{item.name}}</span>
        </div>
    </div>
    <div class="dynamic-footer-box">
        <span class="footer-btn" @click="openLibrary">{{ componentSetting.btnText || '查看全部' }}</span>
    </div>
  </div>
</template>

<script>
export default {
    name:'LibraryItem',
    props:{
        componentSetting: {
            type: Object,
            required: true
        },
        options: {
            type: Object,
            required: true
        },
        isLock:{
            type:Boolean,
            default:false
        },
        data:{
            type:Object,
            default:() => {
                return {
                    name:"这里是文库名称",
                    children:[]
                }
            }
        },
        boxHW:{
            type:Object,
            default:()=>{
                return {
                    height:"100%",
                    width:'100%'
                }
            }
        }
    },
  data() {
    return {
    };
  },
  created() {

  },
  mounted() {
      console.log(this.options)
  },
  methods: {
    openLibraryItem(item){
        if (this.isLock) {
          console.log(item)
          window.open(item.targetUrl, item.jumpMethod)
            // let openUrl = `/#/view?id=${this.data.id}&docId=${item.id}&type=${item.type}`
            // if(this.componentSetting.btnClickType==1){
            //     window.open(openUrl,'_blank')
            // }else{
            //     window.open(openUrl,'_self')
            // }
        }
    },
    openLibrary(){
        if (this.isLock) {
          window.open(this.options.targetUrl,this.options.jumpMethod)
            // let openUrl = `/#/view?id=${this.data.id}`
            // if(this.componentSetting.btnClickType==1){
            //     window.open(openUrl,'_blank')
            // }else{
            //     window.open(openUrl,'_self')
            // }
        }
    }
  }
};
</script>

<style scoped lang="scss">
.library-item{
    width: 100%;
    overflow: hidden;
    .library-title{
        margin-top: 8px;
        height: 84px;
        display: flex;
        flex-direction: row;
        align-items: center;
        padding-left: 48px;
        box-sizing: border-box;
        position: relative;
        .library-title-svg{
            position: absolute;
            left: 10px;
        }
        span{
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    }
    .library-List{
        margin-bottom: 30px;
        margin-left: 32px;
        height: calc(100% - 180px);
        width: calc(100% - 32px);
        overflow-y: auto;
        white-space: nowrap;
        text-overflow: ellipsis;
        .library-list-item{
            position: relative;
            display: flex;
            align-items: center;
            height: 40px;
            padding-left: 30px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            width: calc(100% - 20px);
            span{
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            &::before{
                position: absolute;
                left: 15px;
                content: "";
                height: 5px;
                width: 5px;
                background-color: #758296;
                border-radius: 50%;
            }
        }
    }
    .dynamic-footer-box{
        border-top: 1px solid #F4F4F4;
        width: 100%;
        padding: 20px 0px 20px 48px;
        .footer-btn{
            font-size: 14px;
            color: #1f5fff;
        }
    }
}
.libray-item-cursor{
    &:hover{
        box-shadow: 0 4px 26px #99999929 !important;
    }
    .library-List{
        .library-list-item{
            cursor: pointer !important;
            &:hover{
                color: #1f5fff !important;
            }
        }
    }
    .dynamic-footer-box{
        .footer-btn{
            cursor: pointer !important;
        }
    }
}
.libray-item-height-width{
    width: 384px;
    height: 340px;
}
</style>
