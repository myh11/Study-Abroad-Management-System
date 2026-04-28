<script setup lang="ts">
import PagePlaceholder from '../shared/PagePlaceholder.vue'
</script>

<template>
  <PagePlaceholder
    title="国内审核列表（占位）"
    subtitle="当前后端还没有开放国内审核列表接口，联调先从详情页进入。"
    api-hint="先通过固定 applicationId 访问 `/domesticreviews/:id`，再调用 `/api/domesticreviews/{applicationId}/claim|submit`。"
  />
</template>
