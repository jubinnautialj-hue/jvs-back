<template>
  <div class="chart-pie-box">
    <div :id="'echarts' + i" class="chart-item" v-resize="domSizeChange"></div>
  </div>
</template>

<script>
import * as echarts from '../../../../public/jvs-ui-public/plugin/echarts/echarts.min'
import { themeList, legendPosition } from "../tools"
import { byColorTypeAndTypeTransColor } from '@/components/colorPicker/utils/common'
import _ from 'lodash'
export default {
  name: "ChartPie",
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
  computed: {
    colorList () {
      return this.getCurrentTheme(this.componentSetting.theme || 'default')
    },
    renderOptions () {
      let { itemTooltip } = this.seriesSetTooltip(0, null)
      let seriesList = []
      const domW = document.getElementById('echarts' + this.i).clientWidth
      const domH = document.getElementById('echarts' + this.i).clientHeight
      const minSize = Math.min(domW, domH)
      let outsideRadius = parseInt((minSize * (this.componentSetting.outRadius / 100)) / 2)
      let diminishRadius = 0, chartCenter = ['50%', '50%']
      if(['top','top-left','top-right','bottom','bottom-left','bottom-right'].indexOf(this.componentSetting.legendPos) != -1) {
        diminishRadius = 25
      }
      if(['left','left-top','left-bottom','right','right-top','right-bottom'].indexOf(this.componentSetting.legendPos) != -1) {
        if(['left','left-top','left-bottom'].indexOf(this.componentSetting.legendPos) != -1) {
          chartCenter = [domW / 2 + diminishRadius / 4, domH /2]
        }else{
          chartCenter = [domW / 2 - diminishRadius - 5, domH / 2]
        }
      }
      let chartRadius = [`${this.componentSetting.inRadius}%`, outsideRadius - diminishRadius]
      seriesList = [{
        type: 'pie',
        center: chartCenter,
        radius: chartRadius,
        label: {
          show: this.componentSetting.showLabel,
        },
        ...this.getChartGrid(),
        data: this.options.data || [],
      }]
      let option = {
        color: this.colorList,
        tooltip: {
          axisPointer: {
            lineStyle: {
              type: 'solid'
            },
            type: 'shadow'
          },
          show: this.componentSetting.showTooltip,
          className: 'echart-tooltips-box',
          confine: true,
          trigger: 'item',
          valueFormatter: itemTooltip.valueFormatter
        },
        legend: {
          show: this.componentSetting.showLegend ? this.componentSetting.showLegend : (this.componentSetting.showLegend === false ? false : this.componentSetting.legend),
          type: 'scroll',
          orient: 'vertical',
          textStyle: {
            width: ['left','left-top','left-bottom','right','right-top','right-bottom'].includes(this.componentSetting.legendPos) ? 33 : 80,
            overflow: 'truncate',
            ellipsis: '...'
          },
          tooltip: {
            show: true
          },
          ...legendPosition[this.componentSetting.legendPos],
        },
        series: seriesList
      }
      return option
    }
  },

  data() {
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
    chartInit () {
      if(this.chart) {
        this.chart.resize()
      }else {
        this.chart = echarts.init(document.getElementById('echarts' + this.i))
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
    // 通过seriesd或者yAxis的下标获取格式化，设置tooltip样式
    seriesSetTooltip (index, formatConfig) {
      let newFormatConfig = formatConfig,newItem
      let itemTooltip = {
        valueFormatter: (value) => {
          return value + (this.componentSetting.unit || '')
        }
      }
      return {itemTooltip,newFormatConfig,newItem}
    },
    getChartGrid () {
      let chartGrid = {}
      if(this.componentSetting.legendPos) {
        chartGrid = _.cloneDeep(legendPosition[this.componentSetting.legendPos].grid)
        if(this.componentSetting.dataZoom){
          chartGrid.bottom = ['bottom','bottom-left','bottom-right'].includes(this.componentSetting.legendPos) ? '70px' : '50px'
        }
      }else{
        chartGrid = {
          top: 20,
          bottom: 40,
          containLabel: true,
        }
      }
      return chartGrid
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
}
</script>

<style lang="scss" scoped>
.chart-pie-box{
  height: 100%;
  width: 100%;
  .chart-item{
    width: 100%;
    height: 100%;
  }
}
</style>
