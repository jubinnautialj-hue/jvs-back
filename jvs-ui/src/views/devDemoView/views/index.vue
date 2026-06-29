<template>
  <div class="dev-demo-view-page">
    <div class="left">
      <ul class="type-list">
        <li v-for="(item, index) in typeList" :key="'type-li-'+index" class="type-li">
          <div :class="{'depart': true, 'active': activeName == item.name}" @click="openClose(item.name)">
            <i :class="`icon icon${index}`"></i>
            <span>{{item.label}}</span>
            <i class="arrow-down"></i>
          </div>
          <div class="depart-content" v-show="activeName == item.name">
            <div v-if="item.name == 'rank'">
              <div v-for="star in 6" :key="'star'+index+'-'+star" class="rank-row">
                <div class="star-content">
                  <i v-for="c in 5" :key="'star-item-'+index+'-'+star+'-'+c" :class="{'icon-star-on': c < (7 - star), 'icon-star-off': c > (6 - star)}"></i>
                </div>
                <div class="nums">(2,222)</div>
              </div>
            </div>
            <div v-else class="depart-overflow">
              <ul class="con-list">
                <li v-for="(row, rix) in item.list" :key="'con-list-li-'+index+'-'+rix" class="con-li" @click="selectTypeHandle(item, row)">
                  <span :title="row.name">{{row.name}}</span>
                  <em>({{row.phone || 0}})</em>
                </li>
              </ul>
            </div>
          </div>
        </li>
      </ul>
    </div>
    <div class="right">
      <div class="top">
        <div class="top-title">
          <div>共有<span>{{page.total}}</span>个数据目录</div>
          <div class="add-button" @click="addRowData">新增</div>
          <div class="top-list">
            <a href="javascript:void(0);" target="_blank">目录下载<i></i></a>
          </div>
        </div>
        <div class="theme">
          <span>领域筛选：</span>
          <ul style="width:780px;margin-bottom:10px;">
            <li :class="{'active': currentLabel == ''}" style="width: 58px;" @click="setCurrentLabel('')">全部</li>
            <li v-for="label in labelList" :key="label.id" :class="{'active': currentLabel == label.id}" @click="setCurrentLabel(label.id)">{{label.name}}</li>
          </ul>
        </div>
        <div class="format">
          <span>格式筛选：</span>
          <ul>
            <li :class="{'active': currentFormat == ''}" @click="setCurrentFormat('')">全部</li>
            <li v-for="format in formatList" :key="format.value" :class="{'active': currentFormat == format.value}" @click="setCurrentFormat(format.value)">{{format.label}}</li>
          </ul>
        </div>
        <div class="format">
          <span>开放类型：</span>
          <ul>
            <li :class="{'active': currentOpenType == ''}" @click="setCurrentOpenType('')">全部</li>
            <li v-for="format in openTypeList" :key="format.value" :class="{'active': currentOpenType == format.value}" @click="setCurrentOpenType(format.value)">{{format.label}}</li>
          </ul>
        </div>
        <div v-if="selectType" class="selected">
          <span>已选条件：</span>
          <ul>
            <li class="js-filter">
              <span>{{selectType.name}}</span>
              <i class="close" @click="clearSelect">×</i>
            </li>
          </ul>
        </div>
      </div>
      <div class="center">
        <div class="search-content">
          <input type="text" placeholder="请搜索数据目录名称" :value="keyword">
          <i class="search-icon" @click="getListData"></i>
        </div>
        <div class="tool-content">
          <span v-for="sort in sortList" :key="'sort-item-'+sort.fieldKey" :class="{'active': currentSort == sort.fieldKey}" @click="sortClick(sort)">
            <b>{{sort.fieldName}}</b>
            <i :class="{'tool-icon': sort.direction != 'ASC', 'tool-icon-trans': sort.direction == 'ASC'}"></i>
          </span>
        </div>
      </div>
      <div class="bottom">
        <ul>
          <li v-for="(row, rix) in tableData" :key="row.id">
            <div class="cata-title">
              <a title="" target="_blank" style="outline: none;"
                :href="`/#/devDemoViewInfo?id=${row.id}&jvsAppId=${identificationMap['code_demo_app']}&dataModelId=${identificationMap['code_demo_catalogue']}`"
              >{{row.ziYuanMingChen_1 || row.ziYuanMingChen}}</a>
              <div class="collect">
                <span style="color:#F56C6C;" @click="delSingleData(row)">删除</span>
              </div>
              <div class="collect">
                <i></i>
                <span>收藏</span>
              </div>
              <div class="share">
                <i></i>
                <span>分享</span>
              </div>
              <div class="share" style="margin-right: 10px;">
                <span style="color: #409EFF;" @click="editSingleData(row)">修改</span>
              </div>
            </div>
            <div class="describe">{{row.ziYuanMiaoShu}}</div>
            <div class="cata-information">
              <div class="information">
                <span>来源部门：</span>
                <span class="blue depart">{{row.buMenMingChen_1 || row.buMenMingChen}}</span>
              </div>
              <div class="information">
                <span>重点领域：</span>
                <span class="blue education">{{row.shuJuXingYe_1 || row.shuJuXingYe}}</span>
              </div>
              <div class="information">
                <span>发布时间：</span>
                <span class="blue time">{{row.faBuRiQi}}</span>
              </div>
              <div class="information">
                <span>更新时间：</span>
                <span class="blue time">{{row.updateTime}}</span>
              </div>
              <div class="information">
                <span>开放条件：</span>
                <span class="blue">{{row.kaiFangLeiXing_1 || row.kaiFangLeiXing}}</span>
              </div>
            </div>
            <div class="other">
              <div style="width:172px;float:left;">
                <span>数据量：</span>
                <span class="blue">{{row.shuJuRongLiang}}</span>
              </div>
              <div style="width:160px;float:left;">
                <span>评分：</span>
                <span v-for="i in 5" :key="'star-'+i" class="star-icon"></span>
              </div>
              <div style="min-width:300px;float:left;">
                <ul v-if="row.wenJianZiYuanGeShi">
                  <li v-for="gs in row.wenJianZiYuanGeShi" :key="'gs-'+rix +'-'+gs" :class="gs">{{gs}}</li>
                </ul>
              </div>
              <div class="visit-number">{{row.liuLanLiang || 0}}次</div>
              <div class="download-number">{{row.xiaZaiLiang || 0}}次</div>
            </div>
          </li>
        </ul>
      </div>
      <div class="pagination">
        <span :class="{'prve': true, 'current': page.currentPage == 1, 'disabled': page.currentPage == 1}" @click="setPage('start')">首页</span>
        <span :class="{'prve': true, 'disabled': page.currentPage == 1}" @click="setPage('last')">上一页</span>
        <!-- <span :class="{'current': page.currentPage == 1}" @click="setPage('start')">1</span> -->
        <span v-for="p in pageNumberList" :key="p" @click="setPage(p)" :class="{'current': page.currentPage == p}">{{p}}</span>
        <span :class="{'prve': true, 'disabled': !(page.currentPage < (pageNumberList.length > 0 ?  pageNumberList[pageNumberList.length-1] : 1))}" @click="setPage('next')">下一页</span>
        <span :class="{'prve': true, 'disabled': !(page.currentPage < (pageNumberList.length > 0 ?  pageNumberList[pageNumberList.length-1] : 1))}" @click="setPage('end')">尾页</span>
      </div>
    </div>
  </div>
</template>
<script>
import { getIdentificationMappings } from '@/api/common'
import { getPageDataList, getModelDataByPage, addSingleData, editSingleData, delSingleData } from '../api/index'
export default {
  name: 'devDemoView',
  data () {
    return {
      identificationMap: null, // 标识映射
      activeName: '',
      typeList: [
        {
          label: '省直部门',
          name: 'depart',
          list: []
        },
        {
          label: '城市',
          name: 'city'
        },
        {
          label: '企事业单位',
          name: 'unit'
        },
        {
          label: '综合得分',
          name: 'rank'
        },
      ],
      selectType: null,
      currentLabel: '',
      labelList: [],
      currentFormat: '',
      formatList: [
        {label: 'xls', value: 'xls'},
        {label: 'xml', value: 'xml'},
        {label: 'json', value: 'json'},
        {label: 'csv', value: 'csv'},
        {label: 'rdf', value: 'rdf'},
        {label: '接口', value: '接口'},
        {label: '链接', value: '链接'},
        {label: '其他', value: '其他'},
      ],
      currentOpenType: '',
      openTypeList: [
        {label: '有条件开放', value: '有条件开放'},
        {label: '无条件开放', value: '无条件开放'}
      ],
      keyword: '',
      currentSort: '',
      sortList: [
        {fieldName: '更新时间', fieldKey: 'faBuRiQi', direction: ''},
        {fieldName: '数据量', fieldKey: 'shuJuRongLiang', direction: ''},
        {fieldName: '访问量', fieldKey: 'liuLanLiang', direction: ''},
        {fieldName: '下载量', fieldKey: 'xiaZaiLiang', direction: ''},
        {fieldName: '评分', fieldKey: 'pingFen', direction: ''}
      ],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000], // 分页大小
      },
      tableData: []
    }
  },
  computed: {
    pageNumberList () {
      let tp = []
      let max = Math.ceil(this.page.total / this.page.pageSize)
      if(max > 1) {
        if(max <= 7) {
          for(let i = 1; i <= max; i++) {
            tp.push(i)
          }
        }else{
          if(this.page.currentPage < 7) {
            tp = [1,2,3,4,5,6, '...', max]
          }else{
            let num = this.page.currentPage < (max - 1) ? ((this.page.currentPage - 6) + 1) : (max - 6)
            if(this.page.currentPage >= max - 1) {
              tp = [1, '...']
            }
            for(let i =0; i < 6; i++) {
              tp.push(num+i)
            }
            if(this.page.currentPage < max - 1) {
              tp.push('...')
            }
            tp.push(max)
          }
        }
      }else{
        tp = [1]
      }
      return tp
    },
  },
  async created () {
    // 获取需要的标识映射关系
    let list = ['code_demo_app', 'code_demo_department', 'code_demo_label', 'code_demo_catalogue', 'code_demo_data_item']
    await getIdentificationMappings(list).then(res => {
      if(res.data && res.data.code == 0) {
        this.identificationMap = res.data.data
      }
    })
    this.dataInit()
    this.getListData()
  },
  methods: {
    openClose (type) {
      if(this.activeName == type) {
        this.activeName = ''
      }else{
        this.activeName = type
      }
      this.$forceUpdate()
    },
    dataInit () {
      if(this.identificationMap['code_demo_app']) {
        if(this.identificationMap['code_demo_department']) {
          getPageDataList(this.identificationMap['code_demo_app'], this.identificationMap['code_demo_department']).then(res => {
            if(res.data && res.data.code == 0) {
              this.$set(this.typeList[0], 'list', res.data.data)
            }
          })
        }
        if(this.identificationMap['code_demo_label']) {
          getPageDataList(this.identificationMap['code_demo_app'], this.identificationMap['code_demo_label']).then(res => {
            if(res.data && res.data.code == 0) {
              this.labelList = res.data.data
            }
          })
        }
      }
    },
    selectTypeHandle (item, row) {
      let tp = JSON.parse(JSON.stringify(row))
      tp.formOriginType = item.type
      this.selectType = tp
      this.getListData()
    },
    clearSelect () {
      this.selectType = null
      this.getListData()
    },
    setCurrentLabel (val) {
      this.currentLabel = val
      this.getListData()
    },
    setCurrentFormat (val) {
      this.currentFormat = val
      this.getListData()
    },
    setCurrentOpenType (val) {
      this.currentOpenType = val
      this.getListData()
    },
    sortClick (sort) {
      this.currentSort = sort.fieldKey
      if(!sort.direction) {
        sort.direction = 'DESC'
      }else if(sort.direction == 'DESC') {
        sort.direction = 'ASC'
      }else if(sort.direction == 'ASC') {
        sort.direction = 'DESC'
      }
      this.getListData()
      this.$forceUpdate()
    },
    setPage (type) {
      if(type == 'start') {
        if(this.page.currentPage == 1) {
          return false
        }else{
          this.$set(this.page, 'currentPage', 1)
        }
      }
      if(type == 'end') {
        if(this.page.currentPage < (this.pageNumberList.length > 0 ?  this.pageNumberList[this.pageNumberList.length-1] : 1)) {
          this.$set(this.page, 'currentPage', (this.pageNumberList.length > 0 ?  this.pageNumberList[this.pageNumberList.length-1] : 1))
        }else{
          return false
        }
      }
      if(type == 'last') {
        if(this.page.currentPage == 1) {
          return false
        }else{
          this.$set(this.page, 'currentPage', this.page.currentPage - 1)
        }
      }
      if(type == 'next') {
        if(this.page.currentPage < (this.pageNumberList.length > 0 ?  this.pageNumberList[this.pageNumberList.length-1] : 1)) {
          this.$set(this.page, 'currentPage', this.page.currentPage + 1)
        }else{
          return false
        }
      }
      if(typeof type == 'number') {
        if(this.page.currentPage == type) {
          return false
        }
        this.$set(this.page, 'currentPage', type)
      }
      this.getListData()
    },
    getListData () {
      if(this.identificationMap['code_demo_catalogue']) {
        let obj = {}
        obj.size = this.page.pageSize
        obj.current = this.page.currentPage
        obj.conditions = []
        if(this.selectType) {
          obj.conditions.push({
            fieldKey: 'buMenMingChen',
            enabledQueryTypes: 'eq',
            value: this.selectType.id
          })
        }
        if(this.currentLabel) {
          obj.conditions.push({
            fieldKey: 'shuJuXingYe',
            enabledQueryTypes: 'eq',
            value: this.currentLabel
          })
        }
        if(this.currentFormat) {
          obj.conditions.push({
            fieldKey: 'wenJianZiYuanGeShi',
            enabledQueryTypes: 'in',
            value: this.currentFormat
          })
        }
        if(this.currentOpenType) {
          obj.conditions.push({
            fieldKey: 'kaiFangLeiXing',
            enabledQueryTypes: 'eq',
            value: this.currentOpenType
          })
        }
        if(this.keyword) {
          obj.keywords = this.keyword
        }
        let stp = []
        this.sortList.filter(sit => {
          if(sit.direction) {
            stp.push({
              fieldKey: sit.fieldKey,
              direction: sit.direction
            })
          }
        })
        if(stp && stp.length > 0) {
          obj.sorts = stp
        }
        getModelDataByPage(this.identificationMap['code_demo_app'], this.identificationMap['code_demo_catalogue'], obj).then(res => {
          if(res.data && res.data.code == 0) {
            this.tableData = res.data.data.records
            this.page.total = res.data.data.total
            this.page.currentPage = res.data.data.current
          }
        })
      }
    },
    addRowData () {
      let obj = {
        "ziYuanMingChen": "报废汽车回收（拆解）企业资质信息（新）",
        "buMenMingChen": "1737003417197113346",
        "shuJuRongLiang": 996,
        "phone": 33323,
        "email": "2332@163.com",
        "wenJianZiYuanGeShi": ["xls", "xml", "json", "csv", "rdf", "接口"],
        "faBuRiQi": "2023-11-06",
        "shuJuGengXinRiQi": "2023-12-12",
        "gengXinPinLv": "每半年",
        "pingfen": 5,
        "shuJuXingYe": "1737001686958604297",
        "ziYuanMiaoShu": "报废汽车回收（拆解）企业资质信息，包括企业名称、统一社会信用代码、组织机构代码、资质等级名称 资质证书号、有效起始日期、有效到期日期、资质证书核发日期、资质证书核发机关、资质证书状态等信息",
        "tableForm1702970296529": [
          {
            "muLu": "报废汽车回收（拆解）企业资质信息（新）",
            "yingWenXinXiXiangMing": "enterprise_name",
            "zhongWenXinXiXiangMing": "企业名称",
            "shuJuLeiXing": "字符串型C",
            "zhongWenMiaoShu": "企业名称",
          },
          {
            "muLu": "报废汽车回收（拆解）企业资质信息（新）",
            "yingWenXinXiXiangMing": "uniscid",
            "zhongWenXinXiXiangMing": "统一社会信用代码",
            "shuJuLeiXing": "字符串型C",
            "zhongWenMiaoShu": "统一社会信用代码",
          },
          {
            "muLu": "报废汽车回收（拆解）企业资质信息（新）",
            "yingWenXinXiXiangMing": "organ_code",
            "zhongWenXinXiXiangMing": "组织机构代码",
            "shuJuLeiXing": "字符串型C",
            "zhongWenMiaoShu": "组织机构代码",
          },
          {
            "muLu": "报废汽车回收（拆解）企业资质信息（新）",
            "yingWenXinXiXiangMing": "level_name",
            "zhongWenXinXiXiangMing": "资质等级名称",
            "shuJuLeiXing": "字符串型C",
            "zhongWenMiaoShu": "资质等级名称",
          },
        ],
        "kaiFangLeiXing": "无条件开放",
      }
      addSingleData(this.identificationMap['code_demo_app'], this.identificationMap['code_demo_catalogue'], obj).then(res => {
        if(res.data && res.data.code == 0) {
          console.log('新增成功')
          this.getListData()
        }
      })
    },
    editSingleData (row) {
      let obj = JSON.parse(JSON.stringify(row))
      obj.shuJuRongLiang = 1866
      editSingleData(this.identificationMap['code_demo_app'], this.identificationMap['code_demo_catalogue'], row.id, obj).then(res => {
        if(res.data && res.data.code == 0) {
          console.log('修改成功')
          this.getListData()
        }
      })
    },
    delSingleData (row) {
      delSingleData(this.identificationMap['code_demo_app'], this.identificationMap['code_demo_catalogue'], row.id).then(res => {
        if(res.data && res.data.code == 0) {
          console.log('删除成功')
          this.getListData()
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.dev-demo-view-page{
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  font-size: 14px;
  color: #333;
  display: flex;
  em, i{
    font-style: normal;
  }
  .left{
    width: 260px;
    height: 100%;
    overflow: hidden;
    overflow-y: auto;
    background-color: #fff;
    .type-list{
      margin: 0;
      padding: 0;
    }
    .type-li{
      .depart{
        position: relative;
        width: 100%;
        height: 40px;
        line-height: 40px;
        padding-left: 24px;
        background: #1b8ef8;
        color: #fff;
        cursor: pointer;
        margin-bottom: 15px;
        box-sizing: border-box;
        .icon{
          display: inline-block;
          vertical-align: middle;
          margin-right: 16px;
        }
        .icon0{
          width: 24px;
          height: 24px;
          background: url(../img/guojiabuwei.png);
        }
        .icon1{
          width: 24px;
          height: 24px;
          background: url(../img/difang.png);
        }
        .icon2{
          width: 23px;
          height: 19px;
          background: url(../img/depart.png);
        }
        .icon3{
          width: 21px;
          height: 21px;
          background: url(../img/zonghedf.png);
        }
        .arrow-down{
          position: absolute;
          right: 20px;
          top: 10px;
          width: 22px;
          height: 22px;
          background: url(../img/arrow-down.png);
        }
      }
      .active{
        .arrow-down{
          transform: rotate(180deg);
        }
      }
      .depart-content{
        position: relative;
        margin-bottom: 20px;
        box-sizing: border-box;
        .depart-overflow{
          max-height: 485px;
          padding: 0 13px 0 18px;
          overflow: auto;
          .con-list{
            margin: 0;
            width: 210px;
            border: 1px solid #eeeced;
            padding: 0 16px;
            box-sizing: border-box;
          }
          .con-li{
            width: 100%;
            height: 40px;
            padding-left: 14px;
            line-height: 40px;
            cursor: pointer;
            box-sizing: border-box;
            border-bottom: 1px solid #eeeced;
            position: relative;
            display: flex;
            align-items: center;
            span{
              color: #000;
              width: 130px;
              overflow: hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
            }
          }
          .con-li::before{
            position: absolute;
            top: 18px;
            left: 0px;
            content: "";
            width: 4px;
            height: 4px;
            border-radius: 50%;
            background: #999;
          }
        }
        .rank-row{
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin: 0 19px;
          height: 38px;
          border-bottom: 1px solid #D8D8D8;
          cursor: pointer;
          box-sizing: border-box;
          .star-content{
            i{
              color: #9FCDFC;
              font-size: 24px;
            }
            .icon-star-on{
              display: inline-block;
              width: 20px;
              height: 20px;
              background: url(../img/bluestar.png) no-repeat;
              background-size: 100%;
            }
            .icon-star-off{
              display: inline-block;
              width: 20px;
              height: 20px;
              background: url(../img/bluestarempt.png) no-repeat;
              background-size: 100%;
            }
          }
          .nums{
            color: #999;
          }
        }
      }
    }
  }
  .right{
    flex: 1;
    position: relative;
    margin-left: 20px;
    box-sizing: border-box;
    overflow: hidden;
    overflow-y: auto;
    .top, .cneter, .bottom{
      background-color: #fff;
    }
    .top{
      position: relative;
      width: 100%;
      padding: 0 15px 6px;
      background: #fff;
      margin-bottom: 5px;
      box-sizing: border-box;
      .top-title{
        position: relative;
        width: 100%;
        height: 50px;
        line-height: 50px;
        font-size: 18px;
        border-bottom: 1px solid #ededed;
        span{
          color: #4a8ef9;
          margin: 0 6px;
        }
        .top-list, .add-button{
          position: absolute;
          bottom: 11px;
          right: 20px;
          width: 108px;
          height: 26px;
          line-height: 26px;
          text-align: center;
          font-size: 14px;
          background: #149BFF;
          border-radius: 15px;
          a{
            display: block;
            color: #fff;
            i{
              display: inline-block;
              vertical-align: -1px;
              margin-left: 6px;
              width: 14px;
              height: 14px;
              background: url(../img/list-a.png) no-repeat;
            }
          }
        }
        .add-button{     
          right: 138px;
          color: #fff;
          cursor: pointer;
        }
      }
      .theme, .format, .selected{
        overflow: hidden;
        position: relative;
        width: 100%;
        border-bottom: 1px solid #ededed;
        padding-left: 10px;
        span{
          float: left;
          color: #4a8ef9;
          line-height: 50px;
          margin-left: 5px;
        }
        >ul{
          display: inline-block;
          width: 730px;
          padding: 0;
          margin: 0;
          >li{
            display: inline-block;
            vertical-align: middle;
            margin: 12px 0 0;
            padding: 0 15px;
            height: 26px;
            line-height: 26px;
            border-radius: 13px;
            cursor: pointer;
            box-sizing: border-box;
          }
          .active{
            color: #fff;
            background: #218FFF;
          }
        }
      }
      .theme{
        >ul{
          >li{
            width: 88px;
          }
        }
      }
      .selected{
        background: rgba(238,246,255,.85);
        >ul{
          float: left;
          width: 800px;
          >li{
            padding: 2px 30px 2px 15px;
            border: 1px solid #04C4EF;
            border-radius: 12px;
            background-color: #04C4EF;
            height: 26px;
            box-sizing: border-box;
            cursor: auto;
            position: relative;
            span, i{
              color: #FFF;
              font-style: normal;
              line-height: 20px;
            }
            span{
              margin-left: 0;
            }
            i{
              margin-left: 5px;
              font-size: 20px;
              font-weight: 100;
              cursor: pointer;
              position: absolute;
              right: 10px;
            }
          }
        }
      }
    }
    .center{
      width: 100%;
      height: 50px;
      background: #fff;
      display: flex;
      align-items: center;
      justify-content: space-between;
      .search-content{
        position: relative;
        display: inline-block;
        vertical-align: middle;
        margin: 0 40px 0 24px;
        box-sizing: border-box;
        input{
          display: inline-block;
          vertical-align: middle;
          width: 428px;
          height: 32px;
          background: #f2f2f2;
          border: none;
          padding-left: 15px;
          outline: 0;
        }
        .search-icon{
          position: absolute;
          display: block;
          width: 32px;
          height: 32px;
          background: url(../img/sousuo.png) no-repeat;
          background-size: 20px 20px;
          background-position: center;
          top: 0;
          right: 0;
          cursor: pointer;
        }
      }
      .tool-content{
        display: flex;
        align-items: center;
        span{
          cursor: pointer;
          margin-right: 19px;
          display: flex;
          align-items: center;
          b{
            font-weight: normal;
          }
          i{
            margin-left: 3px;
            display: inline-block;
            width: 18px;
            height: 18px;
            background: url(../img/yuanjiantou.png) no-repeat;
            background-size: 100%;
          }
          .tool-icon-trans{
            margin-top: 2px;
            transform: rotate(180deg);
          }
        }
        .active{
          i{
            margin-left: 3px;
            display: inline-block;
            width: 18px;
            height: 18px;
            background: url(../img/yuanjiantou_act.png) no-repeat;
            background-size: 100%;
          }
        }
      }
    }
    .bottom{
      width: 100%;
      background: #f8f8f8;
      box-sizing: border-box;
      .blue{
        color: #0083FF;
      }
      >ul{
        margin: 0;
        padding: 0;
        >li{
          background: #fff;
          position: relative;
          padding: 10px 30px 10px 25px;
          border: 1px solid #f5f5f5;
          transition: box-shadow .5s;
          .cata-title{
            position: relative;
            z-index: 2;
            height: 35px;
            a{
              width: 500px;
              white-space: nowrap;
              text-overflow: ellipsis;
              overflow: hidden;
              font-size: 18px;
              line-height: 35px;
              color: #1B8EF8;
              transition: .5s;
              display: inline-block;
            }
            .collect{
              float: right;
              cursor: pointer;
              margin: 8px 0 0 20px;
              i{
                display: inline-block;
                width: 14px;
                height: 14px;
                margin-right: 5px;
                background: url(../img/shoucang.png) no-repeat;
              }
            }
            .share{
              position: relative;
              float: right;
              cursor: pointer;
              margin-top: 8px;
              i{
                display: inline-block;
                width: 16px;
                height: 16px;
                margin-right: 5px;
                background: url(../img/fenxiang.png) no-repeat;
              }
            }
          }
          .describe{
            margin-top: 5px;
            font-size: 14px;
            color: #666;
          }
          .cata-information{
            margin-top: 12px;
            overflow: hidden;
            .information{
              height: 20px;
              float: left;
              margin-right: 18px;
              span{
                display: inline-block;
                vertical-align: middle;
              }
              .depart{
                min-width: 80px;
                max-width: 135px;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
              }
              .education{
                min-width: 70px;
                max-width: 96px;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
              }
            }
          }
          .other{
            overflow: hidden;
            margin-top: 10px;
            margin-bottom: 8px;
            div{
              font-size: 14px;
              display: flex;
              align-items: center;
              .star-icon{
                display: inline-block;
                width: 16px;
                height: 16px;
                background: url(../img/graystar.png) no-repeat;
                background-size: 100%;
              }
              >ul{
                margin: 0;
                padding: 0;
                overflow: hidden;
                >li{
                  float: left;
                  padding: 0 5px;
                  height: 20px;
                  text-align: center;
                  line-height: 20px;
                  border-radius: 2px;
                  color: #fff;
                  margin-right: 5px;
                }
                .xls{
                  background: #5489BE;
                }
                .xml{
                  background: #6F81C4;
                }
                .json{
                  background: #42A8D3;
                }
                .csv{
                  background: #DAA956;
                }
                .rdf{
                  background: #8DC46F;
                }
                .接口{
                  background: #C9C24C;
                }
                .链接{
                  background: #51C3A4;
                }
                .其他{
                  background: #6F81C4;
                }
              }
            }
            .visit-number{
              float: right;
              padding-left: 25px;
              height: 20px;
              line-height: 20px;
              color: #999;
              background: url(../img/eye.png) left center no-repeat;
            }
            .download-number{
              float: right;
              padding-left: 25px;
              height: 20px;
              line-height: 20px;
              color: #999;
              margin-right: 15px;
              background: url(../img/download.png) left center no-repeat;
            }
          }
        }
      }
    }
    .pagination{
      text-align: center;
      margin: 15px 0;
      display: block;
      font-size: 14px;
      span{
        border-radius: 4px;
        color: #999;
        cursor: pointer;
        display: inline-block;
        float: none;
        padding: 4px 10px;
        margin-right: 5px;
        margin-bottom: 5px;
        border: 1px solid #ddd;
      }
      .disabled{
        cursor: not-allowed;
      }
      .current{
        color: #fff;
        background-color: #0060FF;
        border-color:#0060FF;
      }
      .current.prve{
        border-color: #ddd;
        color: #999;
        background-color: transparent;
      }
    }
  }
}
</style>