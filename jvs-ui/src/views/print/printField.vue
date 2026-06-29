<template>
  <div class="print-filed-list">
    <design-header ref="designHeader" :infoData="{name: 'word打印模板-字段对照'}" :tabList="tabList"  @tabSelect="tabSelect" :hassave="false">
    </design-header>
    <div class="content">
      <div class="text-info">
        <div>若需要插入字段，请对照如下信息，将对应的字段代码复制粘贴到word模板中对应的位置，打印时会根据该字段实际的数据值进行打印。</div>
        <div style="margin-top: 10px;">
          <h4 style="margin: 5px 0;">名称解释：</h4>
          <div style="display: flex;flex-direction: column;line-height: 24px;">
            <span>1.字段名称：字段显示的中文名称</span>
            <span>2.字段代码：字段的唯一标识，如需在word中插入字段值，应使用该代码，点击即可复制</span>
          </div>
        </div>
      </div>
      <div class="datamodel-field-list">
        <div v-for="item in tabList" :key="'table-item-'+item.name" class="table-item" v-if="item.name == currentTab">
          <jvs-table :option="fieldTableOption" :data="item.list">
            <template slot="id" slot-scope="scope">
              <span @click="handleCopy(scope.row.id)" style="cursor: pointer;">{{scope.row.id}}</span>
            </template>
          </jvs-table>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import DesignHeader from "@/components/page-header/DesignHeader";
import { getWordParamList } from './api/index'
export default {
  components: { DesignHeader },
  data () {
    return {
      designData: {},
      tabList: [],
      currentTab: '0',
      fieldTableOption: {
        addBtn: false,
        search: false,
        hideTop: true,
        menu: false,
        page: false,
        column: [
          {
            label: '字段名称',
            prop: 'name'
          },
          {
            label: '字段代码',
            prop: 'id',
            slot: true
          }
        ]
      }
    }
  },
  created () {
    if(this.$route.query && this.$route.query.designId) {
      this.tabList = []
      getWordParamList(this.$route.query.designId).then(res => {
        if(res.data && res.data.code == 0) {
          let rks = Object.keys(res.data.data)
          if(rks && rks.length > 0) {
            let obj = res.data.data[rks[0]]
            let oks = Object.keys(obj)
            for(let i in oks) {
              this.tabList.push({
                name: i+'',
                label: oks[i],
                list: obj[oks[i]].filter(it => {
                  if(oks[i] == '系统参数' && it.id.startsWith('$') == false) {
                    it.id = '${'+it.id+'}'
                  }
                  return it
                })
              })
            }
          }
        }
      })
    }
  },
  methods: {
    // tab选择结果
    tabSelect(val) {
      this.currentTab = val
    },
    // 复制
    handleCopy(con) {
      const text = document.createElement('input')
      text.value = con
      document.body.appendChild(text)
      text.select()
      document.execCommand('Copy')
      document.body.removeChild(text)
    },
  }
}
</script>
<style lang="scss" scoped>
.print-filed-list{
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  background: #f0f2f5;
  /deep/.design-header-box{
    .header-left{
      .el-icon-edit{
        display: none;
      }
    }
    .header-right{
      display: none;
    }
  }
  .content{
    width: 850px;
    margin: 10px auto;
    height: calc(100vh - 67px);
    overflow: hidden;
    display: flex;
    flex-direction: column;
    .text-info, .datamodel-field-list{
      border-radius: 6px;
      background-color: #ffffff;
      padding: 20px 30px;
      margin-top: 10px;
    }
    .datamodel-field-list{
      margin-top: 20px;
      flex: 1;
      overflow: hidden;
      .table-item{
        height: 100%;
        /deep/.jvs-table{
          height: 100%;
          .table-body-box{
            height: 100%;
            .el-table{
              height: 100%;
            }
          }
        }
      }
      /deep/.table-body-box{
        padding: 0;
        .el-table{
          .el-table__body-wrapper{
            height: calc(100% - 47px)!important;
          }
          .el-table__body-wrapper::-webkit-scrollbar{
            display: none;
          }
        }
        .el-table::before{
          display: none;
        }
      }
    }
  }
  .content::-webkit-scrollbar{
    display: none;
  }
}
</style>