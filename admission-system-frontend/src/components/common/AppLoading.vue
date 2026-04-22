<script setup lang="ts">
defineProps<{
  loading: boolean
  description?: string
}>()
</script>

<template>
  <div v-loading="loading" class="loading-shell" :element-loading-text="description || '加载中...'">
    <slot />
  </div>
</template>

<style scoped>
.loading-shell {
  min-height: 120px;
}
</style>
