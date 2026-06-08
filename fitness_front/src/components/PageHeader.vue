<template>
  <header class="page-header">
    <ShinyText
      text="⭐ Core Fitness"
      :speed="3"
      :delay="0.8"
      :disabled="false"
      :color="'rgba(45, 55, 72, 0.55)'"
      :shine-color="'#2D3748'"
      :spread="120"
      :direction="'left'"
      :yoyo="false"
      :pause-on-hover="false"
    />
    <h1 class="page-title">{{ title }}</h1>
  </header>
</template>

<script setup>
import ShinyText from './ShinyText.vue'

defineProps({
  title: { type: String, required: true }
})
</script>

<style scoped>
.page-header {
  position: relative;
  z-index: 1;
  margin-bottom: 20px;
  margin-left: -40px;
  margin-top: -20px;
}

.page-header :deep(.inline-block) {
  display: block !important;
  margin-bottom: 2px;
  font-size: 15px;
  font-weight: 700;
  font-family: "Gabriola", "Microsoft YaHei", sans-serif;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.page-title {
  margin: 6px 0 0;
  color: var(--text-primary);
  font-size: 25px;
  font-weight: 760;
  font-family: "Inter", "PingFang SC", sans-serif;
  letter-spacing: 0.22em;
  line-height: 1.15;
}
</style>
