<script setup lang="ts">
import PagePlaceholder from '../shared/PagePlaceholder.vue'
</script>

<template>
  <PagePlaceholder
    title="国内审核详情"
    subtitle="详情页骨架保留给 B 端，后续直接填补件、通过、拒绝操作。"
    api-hint="当前可直接接 `/api/applications/{id}` 查看详情，再调用 `/api/domesticreviews/{id}/submit` 提交审核。"
  />
</template>
