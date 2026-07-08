import { ref, onMounted, onUnmounted, watch, type Ref } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption, ECharts } from 'echarts'

export function useChart(chartRef: Ref<HTMLElement | undefined>) {
  const instance = ref<ECharts | null>(null)

  function initChart(options?: EChartsOption) {
    if (!chartRef.value) return
    instance.value = echarts.init(chartRef.value)
    if (options) {
      instance.value.setOption(options)
    }
  }

  function setOption(options: EChartsOption, notMerge = true) {
    instance.value?.setOption(options, notMerge)
  }

  function resize() {
    instance.value?.resize()
  }

  function dispose() {
    instance.value?.dispose()
    instance.value = null
  }

  let resizeObserver: ResizeObserver | null = null

  onMounted(() => {
    initChart()
    resizeObserver = new ResizeObserver(() => {
      resize()
    })
    if (chartRef.value) {
      resizeObserver.observe(chartRef.value)
    }
    window.addEventListener('resize', resize)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', resize)
    resizeObserver?.disconnect()
    dispose()
  })

  return {
    instance,
    setOption,
    resize,
    dispose,
  }
}

export type { EChartsOption, ECharts }
