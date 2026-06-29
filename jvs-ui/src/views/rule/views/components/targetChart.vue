<template>
  <div :id="chartId"></div>
</template>

<script>
import {Chart} from '@antv/g2'
export default {
  name: "targetChart",
  props: {
    chartId: {
      type: String,
    },
    chartData: {
      type: Array,
    },
    lineColor: {
      type: String,
    }
  },
  data() {
    return {
      chart: null
    }
  },
  created () {
  },
  mounted () {
    this.init(this.chartData)
  },
  watch: {
    chartData: {
      handler(newVal, oldVal) {
        this.updateData(newVal)
      }
    }
  },
  methods: {
    init(chartData) {
      this.chart && this.chart.destroy() //this.chart存在就销毁重新绘制chart
      // 初始化实例
      this.chart = new Chart({
        container: this.chartId,
        autoFit: true,
        height: 234,
      })
      this.chart.data(chartData); // 挂载数据
      this.chart.scale('y',  {
        min: 0,
        nice: true,
      });
      // 辅助线
      this.chart.tooltip({
        showCrosshairs: true, // 展示 Tooltip 辅助线
        shared: true,
      });
      this.chart.line().position('x*y').label('y').color(this.lineColor).style({
        lineWidth: 3
      });
      this.chart.point().position('x*y');
      this.chart.render();
    },
    updateData(chartData) {
      this.chart.changeData(chartData)
    }
  }
}
</script>

<style scoped>

</style>
