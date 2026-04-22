<script setup lang="ts" generic="T extends Record<string, unknown>">
export interface ColumnSchema {
  prop: string
  label: string
  minWidth?: number
}

defineProps<{
  columns: ColumnSchema[]
  data: T[]
}>()
</script>

<template>
  <el-table :data="data" border width="100%">
    <el-table-column
      v-for="column in columns"
      :key="column.prop"
      :prop="column.prop"
      :label="column.label"
      :min-width="column.minWidth || 140"
    />
    <slot />
  </el-table>
</template>
