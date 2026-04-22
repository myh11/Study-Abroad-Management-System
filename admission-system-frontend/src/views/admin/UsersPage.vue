<script setup lang="ts">
import PagePlaceholder from '../shared/PagePlaceholder.vue'
</script>

<template>
  <PagePlaceholder
    title="用户管理"
    subtitle="账号、角色、学校绑定都可以直接接当前后端管理端接口。"
    api-hint="已对齐 `/api/users`、`/api/users/{id}`、`/api/users/{id}/status`。"
  />
</template>
