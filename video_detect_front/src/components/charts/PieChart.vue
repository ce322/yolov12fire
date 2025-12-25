<template>
  <div class="chart-container" :style="{ height: height }">
    <div ref="chartDom" class="chart"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts/core';
import { PieChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  PieChart,
  CanvasRenderer
]);

export default {
  name: 'PieChartComponent',
  props: {
    title: {
      type: String,
      default: ''
    },
    chartData: {
      type: Array,
      required: true
    },
    height: {
      type: String,
      default: '300px'
    },
    colorList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      chart: null
    };
  },
  watch: {
    chartData: {
      deep: true,
      handler() {
        this.updateChart();
      }
    }
  },
  mounted() {
    this.initChart();
  },
  beforeUnmount() {
    if (this.chart) {
      this.chart.dispose();
      this.chart = null;
    }
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.chartDom);
      this.updateChart();
      
      // 响应式处理
      window.addEventListener('resize', this.resizeChart);
    },
    updateChart() {
      if (!this.chart) return;
      
      const option = {
        title: {
          text: this.title,
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'horizontal',
          bottom: 10,
          data: this.chartData.map(item => item.name)
        },
        series: [
          {
            name: this.title,
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '18',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: this.chartData,
            color: this.colorList.length > 0 ? this.colorList : undefined
          }
        ]
      };
      
      this.chart.setOption(option);
    },
    resizeChart() {
      if (this.chart) {
        this.chart.resize();
      }
    }
  }
};
</script>

<style scoped>
.chart-container {
  width: 100%;
  padding: 10px;
}

.chart {
  width: 100%;
  height: 100%;
}
</style> 