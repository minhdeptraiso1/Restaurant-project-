<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { Pie } from "vue-chartjs";
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  type ChartData,
  type ChartOptions,
} from "chart.js";

ChartJS.register(ArcElement, Tooltip, Legend);

const props = defineProps<{
  title: string;
  data: Record<string, number>;
  colors?: string[];
  labels?: Record<string, string>; // Map từ key sang label hiển thị
}>();

const defaultColors = [
  "#10b981", // emerald-500
  "#3b82f6", // blue-500
  "#f59e0b", // amber-500
  "#ef4444", // red-500
  "#8b5cf6", // purple-500
  "#ec4899", // pink-500
  "#06b6d4", // cyan-500
];

const chartData = computed<ChartData<"pie">>(() => {
  const entries = Object.entries(props.data);
  const labels = entries.map(([key]) => props.labels?.[key] || key);
  const values = entries.map(([, value]) => value);

  return {
    labels,
    datasets: [
      {
        data: values,
        backgroundColor: props.colors || defaultColors.slice(0, entries.length),
        borderColor: "#1e293b",
        borderWidth: 2,
      },
    ],
  };
});

const chartOptions = computed<ChartOptions<"pie">>(() => ({
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: {
      position: "bottom",
      labels: {
        color: "#e2e8f0",
        padding: 15,
        font: {
          size: 12,
          family: "Inter, system-ui, sans-serif",
        },
      },
    },
    tooltip: {
      backgroundColor: "#1e293b",
      titleColor: "#e2e8f0",
      bodyColor: "#cbd5e1",
      borderColor: "#334155",
      borderWidth: 1,
      padding: 12,
      displayColors: true,
      callbacks: {
        label: function (context) {
          const label = context.label || "";
          const value = context.parsed || 0;
          const total = context.dataset.data.reduce(
            (a, b) => (a as number) + (b as number),
            0
          ) as number;
          const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
          return `${label}: ${value} (${percentage}%)`;
        },
      },
    },
  },
}));

const totalValue = computed(() => {
  return Object.values(props.data).reduce((sum, val) => sum + val, 0);
});
</script>

<template>
  <div
    class="relative overflow-hidden rounded-xl border border-white/10 bg-white/5 backdrop-blur-sm p-6"
  >
    <div
      class="absolute inset-0 bg-gradient-to-br from-blue-500/5 to-purple-500/5 opacity-50"
    ></div>

    <div class="relative">
      <h3 class="text-xl font-bold text-white mb-2">{{ title }}</h3>
      <p class="text-sm text-white/60 mb-4">Tổng: {{ totalValue }}</p>

      <div v-if="totalValue > 0" class="h-[300px] flex items-center justify-center">
        <Pie :data="chartData" :options="chartOptions" />
      </div>

      <div v-else class="h-[300px] flex flex-col items-center justify-center text-white/40">
        <div class="text-5xl mb-3">📊</div>
        <p class="text-sm">Chưa có dữ liệu</p>
      </div>
    </div>
  </div>
</template>
