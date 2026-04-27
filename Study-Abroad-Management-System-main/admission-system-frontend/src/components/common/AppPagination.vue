<script setup lang="ts">
defineProps<{
  total: number
  pageNum: number
  pageSize: number
}>()

const emit = defineEmits<{
  change: [pageNum: number, pageSize: number]
}>()
</script>

<template>
  <el-pagination
    background
    layout="total, prev, pager, next, sizes"
    :total="total"
    :current-page="pageNum"
    :page-size="pageSize"
    @current-change="emit('change', $event, pageSize)"
    @size-change="emit('change', pageNum, $event)"
  />
</template>
