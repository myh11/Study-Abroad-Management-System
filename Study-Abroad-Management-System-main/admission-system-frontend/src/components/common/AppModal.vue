<script setup lang="ts">
defineProps<{
  modelValue: boolean
  title: string
  width?: string | number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width || '520px'"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <slot />
  </el-dialog>
</template>
