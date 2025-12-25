<template>
  <div class="chart-container" :style="{ height: height }">
    <div ref="chartDom" class="chart"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  LineChart,
  CanvasRenderer
]);

export default {
  name: 'LineChartComponent',
  props: {
    title: {
      type: String,
      default: ''
    },
    xAxisData: {
      type: Array,
      required: true
    },
    yAxisData: {
      type: Array,
      required: true
    },
    height: {
      type: String,
      default: '300px'
    },
    xAxisName: {
      type: String,
      default: ''
    },
    yAxisName: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      chart: null
    };
  },
  watch: {
    xAxisData: {
      handler() {
        this.updateChart();
      }
    },
    yAxisData: {
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
          trigger: 'axis'
        },
        legend: {
          data: [this.yAxisName || this.title],
          bottom: 10
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.xAxisData,
          name: this.xAxisName
        },
        yAxis: {
          type: 'value',
          name: this.yAxisName
        },
        series: [
          {
            name: this.yAxisName || this.title,
            type: 'line',
            data: this.yAxisData,
            smooth: true,
            showSymbol: true,
            symbolSize: 7,
            lineStyle: {
              width: 3
            },
            itemStyle: {
              borderRadius: 5
            },
            areaStyle: {
              opacity: 0.3
            }
          }
        ],
        dataZoom: [
          {
            type: 'inside',
            start: 0,
            end: 100
          },
          {
            start: 0,
            end: 100
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