<script setup lang="ts">
import { reactive } from 'vue'
import AppButton from '../../components/common/AppButton.vue'
import AppForm from '../../components/common/AppForm.vue'
import { createApplicationDraft } from '../../api/applications/service'
import { toast } from '../../components/common/toast'

const draftForm = reactive({
  batch_id: 1,
  school_code: 'USYD',
  major_code: 'USYD_SE',
  student: {
    full_name: '',
    gender: 'MALE',
    birth_date: '',
    current_school: '',
    current_grade: '',
    email: '',
    phone: '',
    id_number: '',
  },
  transcript: {
    file_id: undefined,
    average_score: 85,
    failed_subject_count: 0,
    course_scores: '',
  },
  personal_statement: {
    statement_content: '',
    word_count: 500,
  },
})

async function handleSaveDraft() {
  const response = await createApplicationDraft(draftForm)
  toast.success(`草稿已创建，申请ID：${response.application_id}`)
}
</script>

<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h1 class="page-title">代理申请创建</h1>
        <p class="page-subtitle">已接真实创建草稿接口，表单字段先按当前后端最小可用口径组织。</p>
      </div>
      <AppButton type="primary" @click="handleSaveDraft">保存草稿</AppButton>
    </header>

    <el-card shadow="never" class="page-card">
      <AppForm>
        <el-form-item label="批次ID">
          <el-input-number v-model="draftForm.batch_id" :min="1" />
        </el-form-item>
        <el-form-item label="学校编码">
          <el-input v-model="draftForm.school_code" />
        </el-form-item>
        <el-form-item label="专业编码">
          <el-input v-model="draftForm.major_code" />
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model="draftForm.student.full_name" />
        </el-form-item>
        <el-form-item label="平均分">
          <el-input-number v-model="draftForm.transcript.average_score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="自荐材料">
          <el-input v-model="draftForm.personal_statement.statement_content" type="textarea" :rows="5" />
        </el-form-item>
      </AppForm>
    </el-card>
  </section>
</template>
