<template>
  <div class="project-navgation">
    <div class="link-list-box">
      <div class="link-list-item" v-for="(lit, lix) in linkList" :key="lit.name+'-item-'+lix" @click="enterLink(lit)">
        <div>
          <img :src="lit.iconUrl" />
          <span>{{lit.name}}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getDynamicResource } from '../../../../src/api/admin/home'
export default {
  name: 'ProjectNavigation',
  props: {
    element: {
      type: Object
    },
    componentSetting: {
      type: Object,
      default() {
        return {}
      }
    },
    options: {
      type: Object,
      default() {
        return {}
      }
    },
  },
  data () {
    return {
      linkList: []
    }
  },
  created () {
    this.getDynamicResource()
  },
  methods: {
    getDynamicResource () {
      getDynamicResource().then(res => {
        if(res.data && res.data.code == 0) {
          this.linkList = res.data.data
        }
      })
    },
    enterLink (item) {
      this.$openUrl(`${item.url}`, '_blank')
    },
  }
}
</script>

<style lang="scss" scoped>
.project-navgation{
  width: 100%;
  height: 100%;
  padding: 0 24px;
  box-sizing: border-box;
  overflow: hidden;
  .link-list-box{
    height: 100%;
    box-sizing: border-box;
    display: grid;
    grid-template-columns: repeat(4,1fr);
    column-gap: 26px;
    row-gap: 16px;
    overflow: auto;
    .link-list-item{
      width: 64px;
      height: 64px;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-direction: column;
      cursor: pointer;
      >div{
        max-width: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        border-radius: 4px;
        box-sizing: border-box;
        cursor: pointer;
        overflow: hidden;
        text-align: center;
        img{
          width: 32px;
          height: 32px;
          display: inline-block;
        }
        span{
          margin-top: 4px;
          font-size: 14px;
          font-family: Source Han Sans-Regular, Source Han Sans;
          color: #363b4c;
          max-width: 100%;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      &:hover{
        background-color: #eeeff0;
      }
    }
    .link-list-item:hover{
      background: #f5f7fa;
    }
  }
}
</style>