<template>
  <div
    class="relative overflow-hidden rounded-xl border border-white/10 bg-white/5 backdrop-blur-sm p-6"
  >
    <div
      class="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-purple-500/5 opacity-50"
    ></div>

    <div class="relative">
      <div v-if="totalValue > 0">
        <VueECharts :option="option" :style="{ height: height + 'px', width: '100%' }" autoresize />
      </div>

      <div
        v-else
        :style="{ height: height + 'px' }"
        class="flex flex-col items-center justify-center text-white/40"
      >
        <div class="text-5xl mb-3">📊</div>
        <p class="text-sm">Chưa có dữ liệu</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { use } from "echarts/core";
import VueECharts from "vue-echarts";
import { PieChart } from "echarts/charts";
import { TitleComponent, TooltipComponent, LegendComponent } from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";

use([PieChart, TitleComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const props = defineProps<{
  title: string;
  data: Record<string, number>;
  labels?: Record<string, string>;
  colors?: string[];
  height?: number;
}>();

const defaultColors = [
  "#3b82f6", // blue
  "#10b981", // emerald
  "#f59e0b", // amber
  "#ef4444", // red
  "#8b5cf6", // purple
  "#ec4899", // pink
  "#06b6d4", // cyan
  "#22c55e", // green
];

const totalValue = computed(() => {
  return Object.values(props.data).reduce((sum, val) => sum + val, 0);
});

const chartData = computed(() => {
  const entries = Object.entries(props.data);
  const colors = props.colors || defaultColors;

  return entries.map(([key, value], index) => ({
    name: props.labels?.[key] || key,
    value: value,
    itemStyle: {
      color: colors[index % colors.length],
    },
  }));
});

const option = computed(() => ({
  title: {
    text: props.title,
    left: "center",
    top: 10,
    textStyle: {
      color: "#fff",
      fontSize: 18,
      fontWeight: "bold",
    },
  },
  tooltip: {
    trigger: "item",
    backgroundColor: "#1e293b",
    borderColor: "#334155",
    borderWidth: 1,
    textStyle: {
      color: "#e2e8f0",
    },
    formatter: (params: any) => {
      const value = Number(params.value).toLocaleString("vi-VN");
      const percent = params.percent.toFixed(1);
      return `<strong>${params.name}</strong><br/>Số lượng: ${value} (${percent}%)`;
    },
  },
  legend: {
    orient: "horizontal",
    bottom: 10,
    left: "center",
    itemGap: 15,
    textStyle: {
      color: "#e2e8f0",
      fontSize: 12,
    },
    icon: "circle",
  },
  series: [
    {
      type: "pie",
      radius: "60%",
      center: ["50%", "50%"],
      data: chartData.value,
      label: {
        show: true,
        fontSize: 11,
        color: "#fff",
        formatter: (params: any) => `${params.percent.toFixed(1)}%`,
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: "rgba(0, 0, 0, 0.5)",
        },
      },
      animationType: "scale",
      animationEasing: "elasticOut",
      animationDelay: (idx: number) => idx * 50,
    },
  ],
}));
</script>
