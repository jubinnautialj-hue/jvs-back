<template>
  <div class="list-map">
    <div v-for="(it, ix) in list" :key="'list-map-'+ix" class="list-map-item">
      <div v-for="col in item.column" :key="'list-map-item-col-'+ix+'-'+col.prop" class="list-map-item-col">
        <div class="label">{{col.label}}</div>
        <div class="content">
          <el-input v-if="col.type == 'input'" v-model="it[col.prop]" @change="$emit('change', list, prop)"></el-input>
          <el-input v-if="col.type == 'textarea'" v-model="it[col.prop]" type="textarea" @change="$emit('change', list, prop)"></el-input>
          <div v-if="col.type == 'imageSelector'" class="image-select">
            <div style="position: relative;">
              <div class="title-style-item">
                <jvs-select-icon :iconOption="it[col.prop]" :imgListOptions="cardImgList" @change="val=>iconChange(val, col, it)"></jvs-select-icon>
                <jvs-colorpicker :modelValue="it.iconColor" resetColor="#000000" prop="iconColor" :form="it" @update:modelValue="changeHandle" style="margin-left: 10px;"></jvs-colorpicker>
              </div>
            </div>
          </div>
        </div>
        <div class="right-top">
          <i class="el-icon-copy-document" @click="copyHandle(it, ix, list)"></i>
          <i class="el-icon-close" @click="deleteItem(ix)"></i>
        </div>
      </div>
    </div>
    <div>
      <el-button type="primary" @click="addHandle">添加一项</el-button>
    </div>
  </div>
</template>
<script>
import { cardImgList } from "../../customForm/cardImg"
import img1 from '../../customForm/cardImg/1.png'

export default {
  name: 'listMap',
  props: {
    form: {
      type: Object
    },
    item: {
      type: Object
    },
    prop: {
      type: String
    }
  },
  data () {
    return {
      cardImgList: cardImgList,
      list: [],
      index: -1
    }
  },
  created () {
    if(this.form[this.prop]) {
      this.list = this.form[this.prop]
      let imgProps = []
      if(this.item.column) {
        this.item.column.filter(col => {
          if(col.type == 'imageSelector') {
            imgProps.push(col.prop)
          }
        })
      }
      for(let i in this.list) {
        imgProps.filter(ip => {
          if(typeof this.list[i][ip] == 'string') {
            this.$set(this.list[i], ip, {
              value:'',
              backImg: this.list[i][ip],
              type: 'custom',
              pos: 'center',
              verticalFlip: false,
              horizontalFlip: false,
              rotate:0
            })
            this.$set(this.list[i], 'iconColor', '#000000')
          }
        })
      }
    }
  },
  methods: {
    addHandle () {
      let tp = {}
      if(this.item.column) {
        this.item.column.filter(col => {
          if(col.type == 'imageSelector') {
            tp[col.prop] = img1
          }
        })
      }
      this.list.push({})
      this.$forceUpdate()
    },
    deleteItem (index) {
      this.list.splice(index, 1)
      this.$forceUpdate()
    },
    iconChange (val, rItem, item) {
      this.$set(item, rItem.prop, val)
    },
    changeHandle (value, prop, form) {
      if(form) {
        this.$set(form, prop, value)
      }
    },
    copyHandle (item, index, list) {
      let copyItem = JSON.parse(JSON.stringify(item))
      list.splice(Number(index)+1, 0, copyItem)
      this.$forceUpdate()
    }
  },
  watch: {
    list: {
      handler(newVal, oldVal) {
        this.$emit('change', newVal, this.prop)
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.list-map{
  .list-map-item{
    background: #F5F6F7;
    padding: 15px;
    border-radius: 6px;
    margin-bottom: 15px;
    position: relative;
    .right-top{
      position: absolute;
      top: 10px;
      right: 10px;
      line-height: 16px;
      i{
        font-size: 16px;
        cursor: pointer;
        margin-left: 10px;
      }
    }
    .list-map-item-col{
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      .label{
        margin-right: 10px;
      }
      .content{
        flex: 1;
        .image-select{
          position: relative;
          display: flex;
          align-items: center;
          img{
            display: block;
            height: 50px;
          }
          .delete-select-image-tool{
            position: absolute;
            top: 0;
            right: 0;
            color: #FF194C;
            cursor: pointer;
            font-size: 16px;
          }
        }
        /deep/.el-input{
          .el-input__inner{
            background: #fff!important;
            border: 1px solid #DCDFE6!important;
          }
        }
      }
    }
  }
  /deep/.title-style-item{
    display: flex;
    align-items: center;
    .custom-select-icon-com, .jvs-color-picker-show-box{
      background: #fff;
    }
    .color-picker-com{
      height: 30px;
    }
    .com-style-other{
      height: 36px;
      background: #fff;
      border-radius: 4px 4px 4px 4px;
      padding: 4px 8px;
      display: grid;
      grid-column-gap: 8px;
      grid-template-columns: repeat(3,1fr);
      box-sizing: border-box;
      .action-box{
        width: 32px;
        height: 28px;
        border-radius: 4px 4px 4px 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        &:hover{
          background: #eeeff0;
        }
        .bi-iconfont{
          color: #363B4C;
        }
      }
      .active{
        background: #ffffff !important;
        .bi-iconfont{
          color: #1E6FFF;
        }
      }
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
}
</style>