<script setup lang="ts">
import PagePlaceholder from '../shared/PagePlaceholder.vue'
</script>

<template>
  <PagePlaceholder
    title="候补管理"
    subtitle="候补管理页先保留路由和目录，方便后续补公开候补接口后直接接入。"
    api-hint="当前候补公开接口尚未单独暴露，联调时主要通过申请详情、学校审核 submit 和 waitlist confirm 命令链路验证。"
  />
</template>
