<script setup lang="ts">
import PagePlaceholder from '../shared/PagePlaceholder.vue'
</script>

<template>
  <PagePlaceholder
    title="学校审核详情"
    subtitle="保留给学校审核员提交录取、候补、调剂、拒绝。"
    api-hint="当前已对齐 `/api/school-reviews/{applicationId}/submit`，后续直接在这里接评分表单。"
  />
</template>
