<template>
  <VueECharts
    :option="option"
    :style="{ height: height + 'px', width: '100%' }"
    autoresize
    theme="custom-font"
  />
</template>

<script setup>
import { computed } from "vue";
import { use } from "echarts/core";
import VueECharts from "vue-echarts";
import { BarChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";

use([BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer]);

const props = defineProps({
  title: String,
  labels: Array,
  datas: Array,
  height: {
    type: Number,
    default: 400,
  },
  horizontal: {
    type: Boolean,
    default: false,
  },
  originalData: {
    type: [Array, Object],
    default: null,
  },
});

// Computed để map originalData theo thứ tự của labels
const mappedOriginalData = computed(() => {
  if (!props.originalData) return null;

  if (Array.isArray(props.originalData)) {
    return props.originalData;
  }

  // Nếu originalData là object, map theo key của labels
  if (typeof props.originalData === "object") {
    return props.labels.map((label) => props.originalData[label] || 0);
  }

  return null;
});

const horizontalOption = () => ({
  title: {
    text: props.title,
    left: "center",
    top: 20,
  },
  tooltip: {
    trigger: "axis",
    formatter: (params) => {
      const idx = params[0].dataIndex;
      let original = null;
      if (mappedOriginalData.value) {
        original = mappedOriginalData.value[idx];
      }
      let tooltip = `${props.labels[idx]}<br/>`;
      params.forEach((item) => {
        tooltip += `<span style=\"display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:${
          item.color
        }\"></span> ${item.seriesName.replace(" (triệu)", "")}: <b>${
          original ? Number(original).toLocaleString("vi-VN") : item.value
        }</b><br/>`;
      });
      return tooltip;
    },
  },
  legend: {
    data: [props.datas[0].label],
    top: 50,
  },
  grid: { left: 180, right: 40, bottom: 40, top: 90 },
  xAxis: { type: "value" },
  yAxis: {
    type: "category",
    data: props.labels,
    axisLabel: { fontSize: 13 },
  },
  series: [
    {
      type: "bar",
      name: props.datas[0].label,
      data: props.datas[0].data,
      label: {
        show: true,
        position: props.horizontal ? "right" : "top",
        fontSize: 12,
        fontWeight: "bold",
        formatter: (params) => {
          return Number(params.value).toLocaleString("en-US", {
            minimumFractionDigits: 0,
            maximumFractionDigits: 1,
          });
        },
      },
      itemStyle: { color: props.datas[0].color || "#20607B" },
    },
  ],
});

const verticalOption = () => ({
  title: {
    text: props.title,
    left: "center",
    top: 20,
  },
  tooltip: {
    trigger: "axis",
    formatter: (params) => {
      const dataIndex = params[0].dataIndex;
      let tooltip = `${props.labels[dataIndex]}<br/>`;
      params.forEach((item) => {
        let original = null;
        if (mappedOriginalData.value) {
          original = mappedOriginalData.value[dataIndex];
        }
        tooltip += `<span style=\"display:inline-block;margin-right:5px;border-radius:10px;width:9px;height:9px;background-color:${
          item.color
        }\"></span> ${item.seriesName.replace(" (triệu)", "")}: <b>${
          original ? Number(original).toLocaleString("vi-VN") : item.value
        }</b><br/>`;
      });
      return tooltip;
    },
  },
  legend: {
    show: true,
    data: [props.datas[0].label],
    top: 50,
  },
  grid: { left: 60, right: 40, bottom: 60, top: 90 },
  xAxis: {
    type: "category",
    data: props.labels,
    axisLabel: {
      fontSize: 13,
      interval: 0,
      rotate: 30,
      overflow: "break",
    },
  },
  yAxis: { type: "value" },
  series: [
    {
      type: "bar",
      name: props.datas[0].label,
      data: props.datas[0].data,
      label: {
        show: true,
        position: "top",
        fontSize: 12,
        fontWeight: "bold",
        formatter: (params) => {
          return Number(params.value).toLocaleString("en-US", {
            minimumFractionDigits: 0,
            maximumFractionDigits: 1,
          });
        },
      },
      itemStyle: { color: props.datas[0].color || "#20607B" },
    },
  ],
});

const option = computed(() => (props.horizontal ? horizontalOption() : verticalOption()));
</script>
