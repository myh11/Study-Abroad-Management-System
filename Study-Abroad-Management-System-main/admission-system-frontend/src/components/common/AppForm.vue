<template>
  <el-form label-width="110px" class="app-form">
    <slot />
  </el-form>
</template>

<style scoped>
.app-form {
  display: grid;
  gap: 16px;
}
</style>
