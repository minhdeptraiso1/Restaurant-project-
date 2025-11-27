<script setup lang="ts">
import { computed, ref, onMounted, watch } from "vue";

const props = withDefaults(
  defineProps<{
    label: string;
    value: number;
    total?: number;
    color?: string;
    size?: number;
    thickness?: number;
    icon?: string;
  }>(),
  {
    total: 100,
    color: "#10b981",
    size: 160,
    thickness: 14,
    icon: "??",
  }
);

const animPct = ref(0);
const animValue = ref(0);

const percentage = computed(() => {
  return props.total > 0 ? Math.round((props.value / props.total) * 100) : 0;
});

const circumference = computed(() => {
  const radius = (props.size - props.thickness) / 2;
  return 2 * Math.PI * radius;
});

const strokeDashoffset = computed(() => {
  return circumference.value - (circumference.value * animPct.value) / 100;
});

const gradientId = computed(() => {
  return `gradient-${Math.random().toString(36).substr(2, 9)}`;
});

function animate() {
  const duration = 1200;
  const startTime = performance.now();

  const easeOutCubic = (t: number) => 1 - Math.pow(1 - t, 3);

  const step = (currentTime: number) => {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    const eased = easeOutCubic(progress);

    animPct.value = percentage.value * eased;
    animValue.value = props.value * eased;

    if (progress < 1) {
      requestAnimationFrame(step);
    } else {
      animPct.value = percentage.value;
      animValue.value = props.value;
    }
  };

  requestAnimationFrame(step);
}

onMounted(() => {
  animate();
});

watch([() => props.value, () => props.total], () => {
  animate();
});
</script>

<template>
  <div class="stat-ring-card">
    <svg :width="size" :height="size" class="stat-ring-svg">
      <defs>
        <linearGradient :id="gradientId" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" :style="{ stopColor: color, stopOpacity: 1 }" />
          <stop offset="100%" :style="{ stopColor: color, stopOpacity: 0.6 }" />
        </linearGradient>
        <filter :id="`glow-${gradientId}`">
          <feGaussianBlur stdDeviation="3" result="coloredBlur" />
          <feMerge>
            <feMergeNode in="coloredBlur" />
            <feMergeNode in="SourceGraphic" />
          </feMerge>
        </filter>
      </defs>

      <circle
        :cx="size / 2"
        :cy="size / 2"
        :r="(size - thickness) / 2"
        fill="none"
        stroke="rgba(255, 255, 255, 0.05)"
        :stroke-width="thickness"
      />

      <circle
        class="progress-circle"
        :cx="size / 2"
        :cy="size / 2"
        :r="(size - thickness) / 2"
        fill="none"
        :stroke="`url(#${gradientId})`"
        :stroke-width="thickness"
        :stroke-dasharray="circumference"
        :stroke-dashoffset="strokeDashoffset"
        stroke-linecap="round"
        :style="{ filter: `url(#glow-${gradientId})` }"
      />
    </svg>

    <div class="stat-ring-content">
      <div class="stat-icon">{{ icon }}</div>
      <div class="stat-value">{{ Math.round(animValue) }}</div>
      <div class="stat-percentage">{{ Math.round(animPct) }}%</div>
      <div class="stat-label">{{ label }}</div>
    </div>

    <div class="pulse-ring" :style="{ borderColor: color }"></div>
  </div>
</template>

<style scoped>
.stat-ring-card {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.stat-ring-card:hover {
  transform: scale(1.05);
  filter: brightness(1.2);
}

.stat-ring-svg {
  transform: rotate(-90deg);
}

.progress-circle {
  transition: stroke-dashoffset 0.3s ease;
  animation: dash 1.2s ease-out forwards;
}

.stat-ring-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  pointer-events: none;
}

.stat-icon {
  font-size: 2rem;
  margin-bottom: 0.25rem;
  animation: float 3s ease-in-out infinite;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  line-height: 1;
  animation: countUp 1.2s ease-out;
}

.stat-percentage {
  font-size: 0.875rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 0.25rem;
}

.stat-label {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 0.5rem;
  font-weight: 500;
}

.pulse-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  border: 2px solid;
  border-radius: 50%;
  opacity: 0;
  animation: pulse-ring 2s ease-out infinite;
}

@keyframes dash {
  from {
    stroke-dashoffset: var(--circumference, 440);
  }
  to {
    stroke-dashoffset: var(--offset, 0);
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-5px);
  }
}

@keyframes countUp {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pulse-ring {
  0% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 0.5;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0;
  }
}
</style>
