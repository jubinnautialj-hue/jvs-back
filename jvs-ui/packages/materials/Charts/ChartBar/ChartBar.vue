<template>
  <div :style="`width: 100%;height: 100%`">
    <div id="echarts" style="width: 100%;height: 100%;"></div>
  </div>
</template>

<script>
import * as echarts from '../../../../public/jvs-ui-public/plugin/echarts/echarts.min';
export default {
  name: "ChartBar",
  props: {
    isUpdate: {
      type: Boolean,
      default() {
        return false
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
  watch: {
    isUpdate(val) {
      // console.log(val)
      this.chart.resize()
    }
  },
  computed:{
    renderOptions() {

      let option = {
        color: [
          '#768CCE',
          '#C3D6F2',
          '#ACBFEA',
          '#D1EAF5',
          '#ACBFEA',
          '#A0DDE0',
        ],
        tooltip: {
          trigger: 'item'
        },
        legend: {
          left: 'center'
        },
        xAxis: {
          type: 'category',
          data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: "系列名称1",
            data: [150, 230, 224, 218, 135, 147, 260],
            type: 'bar'
          },
          {
            name: "系列名称2",
            data: [150, 230, 224, 218, 135, 147, 260],
            type: 'bar'
          },
          {
            name: "系列名称3",
            data: [150, 230, 224, 218, 135, 147, 260],
            type: 'bar'
          }
        ]
      }
      return option
    }
  },
  data() {
    return {
      chart: null,
    }
  },
  created() {
  },
  mounted() {
    console.log(this.options)
    this.$nextTick(() => {
      this.chartInit()
    })
  },
  methods: {
    resizedEvent(data) {
      console.log(data)
    },
    chartInit() {
      this.chart = echarts.init(document.getElementById('echarts'));
      this.chart.setOption(this.renderOptions)
    },
  }
}
</script>

<style lang="scss" scoped>
</style>
