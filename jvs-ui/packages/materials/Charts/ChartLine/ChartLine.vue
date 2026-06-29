<template>
  <div class="chart-box">
    <div :id="'echarts' + i" class="chart-item" v-resize="domSizeChange"></div>
  </div>
</template>

<script>
import * as echarts from '../../../../public/jvs-ui-public/plugin/echarts/echarts.min'
import { themeList, legendPosition, formatField } from "../tools"
import { transColorArr, byColorTypeAndTypeTransColor } from '@/components/colorPicker/utils/common'
import _ from 'lodash'
export default {
  name: "ChartLine",
  props: {
    isUpdate: {
      type: Boolean,
      default() {
        return false
      }
    },
    i: {
      type: String,
      default() {
        return ''
      }
    },
    w: {
      type: Number,
      default() {
        return 0
      }
    },
    h: {
      type: Number,
      default() {
        return 0
      }
    },
    options: {
      type: Object,
      default() {
        return {}
      }
    },
    componentSetting: {
      type: Object,
      default() {
        return {}
      }
    },
  },
  // 监听元素变化
  directives: {  // 使用局部注册指令的方式
    resize: { // 指令的名称
      bind(el, binding) { // el为绑定的元素，binding为绑定给指令的对象
        let width = '', height = '';
        function isReize() {
          const style = document.defaultView.getComputedStyle(el);
          if (width !== style.width || height !== style.height) {
            binding.value();  // 关键
          }
          width = style.width;
          height = style.height;
        }
        el.__vueSetInterval__ = setInterval(isReize, 10);
      },
      unbind(el) {
        clearInterval(el.__vueSetInterval__);
      }
    }
  },
  computed:{
    colorList () {
      return this.getCurrentTheme(this.componentSetting.theme || 'default')
    },
    renderOptions() {
      let _this = this
      let firstFormatConfig = null
      let tinyObj = { tooltip: {}, axis: {}, legend: {}, symbol: null, smooth: null, grid: {} }
      let otherOption = {}
      let seriesList = []
      if(this.options.series) {
        this.options.series.filter((item, index) => {
          let {itemTooltip, newFormatConfig,newItem} = this.seriesSetTooltip(0, firstFormatConfig)
          if(newFormatConfig) {
            firstFormatConfig = newFormatConfig
          }
          if(this.componentSetting.tiny) {
            tinyObj.tooltip.formatter = (params)=> {
              let str = ''
              if(newFormatConfig) {
                str += this.formatPolarAxis(item.value, newFormatConfig)+(' '+ newItem.unit ? newItem.unit : (this.componentSetting.unit || ''))
              }else{
                str += (params.value + (' '+(this.componentSetting.unit || '')))
              }
              return str
            }
          }
          seriesList.push({
            ...item,
            large: true,
            type: 'line',
            polarIndex: index,
            stack: item.stack || false,
            tooltip: itemTooltip,
            symbol: tinyObj.symbol,
            symbolSize: this.componentSetting.tiny ? 2 : 4,
            smooth: this.componentSetting.tiny
          })
        })
      }
      otherOption = {
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: (this.options.xAxis && this.options.xAxis.data) ? this.options.xAxis.data : [],
          ...tinyObj.axis,
          axisPointer: {
            show: true,
            z: 100,
            lineStyle: {type: 'cross'},
            label: {show: false}
          }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: function (value) {
              return _this.formatAxisItem(value,firstFormatConfig)
            }
          },
          ...tinyObj.axis,
          axisPointer: {
            show: false
          }
        }
      }
      // 是否开启了迷你图
      if(this.componentSetting.tiny) {
        tinyObj.tooltip = {
          formatter: (value) => {
            if(typeof value == 'object'){
              return value.value + (' '+ (this.componentSetting.unit || ''))
            }else{
              if(this.componentSetting.type === 'percent') {
                return Math.round(value*1000) / 10+'%' + (' '+ (this.componentSetting.unit || ''))
              }else{
                return value + (' '+ (this.componentSetting.unit || ''))
              }
            }
          },
          axisPointer: {
            lineStyle: {type:'solid'},
            type: 'cross'
          },
          trigger: 'item'
        }
        tinyObj.grid = {
          left: 10,  
          top: 10,
          bottom: 10,
          right: 10
        },
        tinyObj.legend = {show:false}
        tinyObj.symbol = 'circle'
        tinyObj.axis = {
          splitLine: {show: false},
          axisTick: {show: false},
          axisLabel: {show: false},
          axisLine: {show: false},
          axisPointer: {show: false}
        }
      }
      // 添加目标线的渲染
      if(this.componentSetting.tragetLines) {
        this.componentSetting.tragetLines.map((item)=> {
          if(item.tragetValue && item.tragetName) {
            let colorArr = transColorArr(item.tragetColor).sort((a,b)=>{
              return  b.alpha - a.alpha
            })
            seriesList.push({
              type: 'line',
              data: [item.tragetValue],
              tooltip: {
                show: false
              },
              showSymbol: false,
              markLine: {
                symbol: ['none','none'],
                label: {
                  position: 'insideStartTop',
                  color: colorArr[0].color,
                  formatter: `${item.tragetName}`
                },
                lineStyle:{
                  width:2,
                  color:{
                    type: 'linear',
                    x: 0,
                    y: 0,
                    x2: 1,
                    y2: 1,
                    colorStops: colorArr,
                  }
                },
                emphasis: {
                  disabled: true
                },
                data: [{ type: 'max' }]
              }
            })
          }
        })
      }
      // 是否开启了数据缩放
      if(this.componentSetting.dataZoom && !this.componentSetting.tiny) {
        otherOption.dataZoom = [{
          type: 'inside',
          xAxisIndex: [0, 1],
        },
        {
          xAxisIndex: [0, 1],
          type: 'slider',
          bottom: ['bottom','bottom-left','bottom-right'].includes(this.componentSetting.legendPos) ? '35px' : null,
        }]
      }
      let option = {
        color: this.colorList,
        grid: {
          ...this.getChartGrid(),
          ...tinyObj.grid,
        },
        tooltip: {
          axisPointer: {
            lineStyle:{
              type:'solid'
            },
            type: 'line'
          },
          show: this.componentSetting.showTooltip,
          className: 'echart-tooltips-box',
          confine: true,
          trigger: 'axis',
          ...tinyObj.tooltip
        },
        legend: {
          show: this.componentSetting.showLegend ? this.componentSetting.showLegend : (this.componentSetting.showLegend === false ? false : this.componentSetting.legend),
          type: 'scroll',
          textStyle: {
            width: ['left','left-top','left-bottom','right','right-top','right-bottom'].includes(this.componentSetting.legendPos) ? 33 : 80,
            overflow: 'truncate',
            ellipsis: '...'
          },
          tooltip: {
            show: true
          },
          ...legendPosition[this.componentSetting.legendPos],
          ...tinyObj.legend
        },
        ...otherOption,
        series: seriesList,
        width:'auto',
        height:'auto',
      }
      return option
    }
  },
  data () {
    return {
      chart: null,
    }
  },
  methods: {
    updateChart () {
      if(!this.chart) {
        this.chartInit()
      }
      this.chart.setOption(this.renderOptions, true)
      this.$forceUpdate()
    },
    domSizeChange () {
      this.chartInit()
    },
    resizedEvent (data) {
      console.log(data)
    },
    chartInit () {
      if(this.chart) {
        this.chart.resize()
      }else {
        this.chart = echarts.init(document.getElementById('echarts' + this.i));
        this.chart.setOption(this.renderOptions)
      }
    },
    getCurrentTheme (val) {
      let color =  []
      themeList.forEach((item) => {
        if(item.value == val) color = item.list
      })
      if(this.componentSetting.useThemeGradient) {
        let newColor = []
        color.map((item)=>{
          newColor.push({
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops:[
              {offset:0, color: byColorTypeAndTypeTransColor(item,100,'css','rgb')},
              {offset:1, color: byColorTypeAndTypeTransColor(item,15,'css','rgb')}
            ]
          })
        })
        return newColor
      }else{
        return color
      }
    },
    getChartGrid () {
      let chartGrid = {}
      if(this.componentSetting.legendPos) {
        chartGrid = _.cloneDeep(legendPosition[this.componentSetting.legendPos].grid)
        if(this.componentSetting.dataZoom){
          chartGrid.bottom = ['bottom','bottom-left','bottom-right'].includes(this.componentSetting.legendPos)?'70px':'50px'
        }
      }else{
        chartGrid = {
          left: '24px',
          right: '24px',
          bottom: '24px',
          top: '36px',
          containLabel: true,
        }
      }
      return chartGrid
    },
    // 通过seriesd或者yAxis的下标获取格式化，设置tooltip样式
    seriesSetTooltip (index, formatConfig) {
      let newFormatConfig = formatConfig,newItem
      let itemTooltip = {
        valueFormatter: (value)=>{
          return value + (this.componentSetting.unit || '')
        }
      }
      return {itemTooltip, newFormatConfig, newItem}
    },
    // 格式化直角坐标系Axis
    formatAxisItem (value, item) {
      let formatStr = value
      if(item && item.formatConfig){
        switch (item.formatConfig.formatBase) {
          case '1':
            formatStr = `${item.formatConfig.dataPre} ${formatField(value, item.formatConfig)} ${item.formatConfig.dataSuff || ''}`;
            break;
          case '2':
            formatStr = `${item.formatConfig.dataPre} ${formatField(value, item.formatConfig)}% ${item.formatConfig.dataSuff || ''}`;
            break;
          case '3':
            formatStr = `${item.formatConfig.dataPre} ${formatField(value, item.formatConfig)}‰ ${item.formatConfig.dataSuff || ''}`;
            break;
          default:
            formatStr = value;
            break;
        }
        return `${formatStr}${item.unit ? item.unit : (this.componentSetting.unit || '')}`
      }
      return `${formatStr}${this.componentSetting.unit || ''}`
    },
    // 格式化极坐标系Axis
    formatPolarAxis (value, formatConfig) {
      let str = parseFloat(value)
      switch(formatConfig.formatBase) {
      case '0':
        break;
      case '1':
        str = `${formatField(str, formatConfig)}`;
        break;
      case '2':
        str = `${formatField(str, formatConfig)}%`;
        break;
      case '3':
        str = `${formatField(str, formatConfig)}‰`;
        break;
      default:
        break;
      }
      return `${formatConfig.dataPre|| ''} ${str} ${formatConfig.dataSuff|| ''}`
    },
  }
}
</script>

<style lang="scss" scoped>
.chart-box{
  height: 100%;
  width: 100%;
  .chart-item{
    width: 100%;
    height: 100%;
  }
}
</style>
