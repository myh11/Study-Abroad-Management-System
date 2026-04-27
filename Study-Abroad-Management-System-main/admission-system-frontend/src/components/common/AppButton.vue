<script setup lang="ts">
defineProps<{
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  plain?: boolean
  loading?: boolean
}>()
</script>

<template>
  <el-button :type="type" :plain="plain" :loading="loading" round>
    <slot />
  </el-button>
</template>
