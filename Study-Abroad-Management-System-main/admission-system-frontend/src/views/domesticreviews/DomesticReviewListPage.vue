<script setup lang="ts">
import PagePlaceholder from '../shared/PagePlaceholder.vue'
</script>

<template>
  <PagePlaceholder
    title="国内审核列表"
    subtitle="页面目录和角色路由已就位，当前后端尚未提供专门列表接口。"
    api-hint="先复用申请详情和 `/api/domesticreviews/{applicationId}/claim|submit` 做联调；列表接口待后端补充。"
  />
</template>
