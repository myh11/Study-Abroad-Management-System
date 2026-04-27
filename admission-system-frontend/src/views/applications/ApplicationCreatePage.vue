<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Upload, Document, Close, CircleCheck, Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createApplicationDraft, submitApplication } from '../../api/applications/service'
import { uploadTranscript, uploadAttachment } from '../../api/files/service'

const router = useRouter()
const saving = ref(false)

const draftForm = reactive({
  batchId: 1,
  targetSchoolCode: 'USYD',
  targetMajorCode: 'USYD_SE',
  student: {
    name: '',
    gender: 'MALE',
    birthDate: '',
    currentSchool: '',
    grade: '',
    email: '',
    phone: '',
    idCardNo: '',
  },
  score: {
    transcriptSchoolName: '',
    termStart: '',
    termEnd: '',
    chinese: 0,
    math: 0,
    english: 0,
    physics: 0,
    chemistry: 0,
    history: 0,
    averageScore: 0,
    failedSubjectCount: 0,
    fileId: null as number | null,
  },
  personalStatement: {
    content: '',
  },
})

const transcriptFile = ref<File | null>(null)
const uploadingTranscript = ref(false)
const uploadedTranscriptName = ref('')

const attachmentFiles = ref<{ file: File; name: string; size: string }[]>([])
const uploadingAttachment = ref(false)

async function handleUploadTranscript() {
  if (!transcriptFile.value) {
    ElMessage.error('请先选择成绩单文件')
    return
  }
  uploadingTranscript.value = true
  try {
    const res = await uploadTranscript(transcriptFile.value)
    draftForm.score.fileId = res.fileId
    uploadedTranscriptName.value = res.originalName
    ElMessage.success('成绩单上传成功')
  } catch {
    ElMessage.error('成绩单上传失败')
  } finally {
    uploadingTranscript.value = false
  }
}

function handleAttachmentChange(file: any) {
  const raw = file.raw as File
  const size = raw.size < 1024 * 1024
    ? (raw.size / 1024).toFixed(1) + ' KB'
    : (raw.size / (1024 * 1024)).toFixed(1) + ' MB'
  attachmentFiles.value.push({ file: raw, name: raw.name, size })
}

function removeAttachment(index: number) {
  attachmentFiles.value.splice(index, 1)
}

async function handleUploadAttachments() {
  if (attachmentFiles.value.length === 0) return
  uploadingAttachment.value = true
  try {
    for (const item of attachmentFiles.value) {
      await uploadAttachment(item.file)
    }
    ElMessage.success('附件上传成功')
    attachmentFiles.value = []
  } catch {
    ElMessage.error('附件上传失败')
  } finally {
    uploadingAttachment.value = false
  }
}

async function handleSaveDraft() {
  if (!draftForm.score.fileId) {
    ElMessage.error('请先上传成绩单')
    return
  }
  saving.value = true
  try {
    const res = await createApplicationDraft(draftForm)
    ElMessage.success(`草稿已创建，申请ID：${res.applicationId}`)
    router.push(`/applications/${res.applicationId}`)
  } catch (err: any) {
    ElMessage.error(err.message || '创建草稿失败')
  } finally {
    saving.value = false
  }
}

async function handleSubmit() {
  if (!draftForm.score.fileId) {
    ElMessage.error('请先上传成绩单')
    return
  }
  saving.value = true
  try {
    const res = await createApplicationDraft(draftForm)
    await submitApplication(res.applicationId)
    ElMessage.success(`申请已提交，进入审核流程，ID：${res.applicationId}`)
    router.push(`/applications/${res.applicationId}`)
  } catch (err: any) {
    ElMessage.error(err.message || '提交申请失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="page-shell">
    <!-- Header -->
    <div>
      <button class="back-link" @click="router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </button>
      <h1 class="page-title">创建留学申请</h1>
      <p class="page-subtitle">填写学生信息与材料后保存草稿或直接提交进入审核流程</p>
    </div>

    <!-- ① 学校与专业选择 -->
    <div class="section-card">
      <div class="section-header">
        <h3 class="section-title">① 学校与专业选择</h3>
        <p class="section-desc">选择申请批次、目标学校及专业</p>
      </div>
      <el-form label-width="110px" class="form-grid">
        <el-form-item label="申请批次" required>
          <el-input-number v-model="draftForm.batchId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="学校编码" required>
          <el-input v-model="draftForm.targetSchoolCode" placeholder="如 USYD" />
        </el-form-item>
        <el-form-item label="专业编码" required>
          <el-input v-model="draftForm.targetMajorCode" placeholder="如 USYD_SE" />
        </el-form-item>
      </el-form>
    </div>

    <!-- ② 学生基本信息 -->
    <div class="section-card">
      <div class="section-header">
        <h3 class="section-title">② 学生基本信息</h3>
        <p class="section-desc">请准确填写学生真实信息</p>
      </div>
      <el-form label-width="110px" class="form-grid three-col">
        <el-form-item label="学生姓名" required>
          <el-input v-model="draftForm.student.name" placeholder="例如：张明轩" />
        </el-form-item>
        <el-form-item label="性别" required>
          <el-radio-group v-model="draftForm.student.gender">
            <el-radio label="MALE">男</el-radio>
            <el-radio label="FEMALE">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" required>
          <el-date-picker v-model="draftForm.student.birthDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="draftForm.student.phone" placeholder="+86" />
        </el-form-item>
        <el-form-item label="电子邮箱">
          <el-input v-model="draftForm.student.email" type="email" placeholder="student@example.com" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="draftForm.student.idCardNo" placeholder="证件号" />
        </el-form-item>
        <el-form-item label="当前学校" required class="span-2">
          <el-input v-model="draftForm.student.currentSchool" placeholder="例如：上海某中学" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="draftForm.student.grade" placeholder="例如：高三" />
        </el-form-item>
      </el-form>
    </div>

    <!-- ③ 成绩信息 -->
    <div class="section-card">
      <div class="section-header">
        <h3 class="section-title">③ 成绩信息</h3>
        <p class="section-desc">填写学期成绩，上传成绩单扫描件</p>
      </div>
      <el-form label-width="110px" class="form-grid three-col">
        <el-form-item label="成绩单学校">
          <el-input v-model="draftForm.score.transcriptSchoolName" placeholder="成绩单所属学校" />
        </el-form-item>
        <el-form-item label="学期开始">
          <el-date-picker v-model="draftForm.score.termStart" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="学期结束">
          <el-date-picker v-model="draftForm.score.termEnd" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="语文" required>
          <el-input-number v-model="draftForm.score.chinese" :min="0" :max="100" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数学" required>
          <el-input-number v-model="draftForm.score.math" :min="0" :max="100" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="英语" required>
          <el-input-number v-model="draftForm.score.english" :min="0" :max="100" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="物理">
          <el-input-number v-model="draftForm.score.physics" :min="0" :max="100" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="化学">
          <el-input-number v-model="draftForm.score.chemistry" :min="0" :max="100" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="历史">
          <el-input-number v-model="draftForm.score.history" :min="0" :max="100" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="平均分">
          <el-input-number v-model="draftForm.score.averageScore" :min="0" :max="100" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="挂科数">
          <el-input-number v-model="draftForm.score.failedSubjectCount" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>

      <!-- 成绩单上传 -->
      <div class="upload-area">
        <div class="upload-dropzone">
          <el-icon class="upload-icon"><Upload /></el-icon>
          <div class="upload-title">点击或拖拽文件到此区域上传成绩单</div>
          <div class="upload-hint">支持 PDF / JPG / PNG，单个文件不超过 10MB</div>
          <el-upload :auto-upload="false" :on-change="(file: any) => { transcriptFile = file.raw }" :show-file-list="false">
            <el-button size="small">选择文件</el-button>
          </el-upload>
        </div>
        <div v-if="transcriptFile || uploadedTranscriptName" class="file-list">
          <div v-if="transcriptFile && !uploadedTranscriptName" class="file-item">
            <div class="file-info">
              <el-icon><Document /></el-icon>
              <span>{{ transcriptFile.name }}</span>
            </div>
            <el-button type="primary" size="small" :loading="uploadingTranscript" @click="handleUploadTranscript">上传</el-button>
          </div>
          <div v-if="uploadedTranscriptName" class="file-item success">
            <div class="file-info">
              <el-icon color="#67c23a"><Document /></el-icon>
              <span>{{ uploadedTranscriptName }}</span>
            </div>
            <el-tag type="success" size="small">已上传</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- ④ 附件上传 -->
    <div class="section-card">
      <div class="section-header">
        <h3 class="section-title">④ 附件材料</h3>
        <p class="section-desc">推荐信、获奖证书等附加材料（可选）</p>
      </div>
      <div class="upload-area">
        <div class="upload-dropzone">
          <el-icon class="upload-icon"><Upload /></el-icon>
          <div class="upload-title">点击上传附加材料</div>
          <div class="upload-hint">PDF / JPG / PNG，单个 ≤ 10MB</div>
          <el-upload :auto-upload="false" :on-change="handleAttachmentChange" :show-file-list="false" multiple>
            <el-button size="small">选择文件</el-button>
          </el-upload>
        </div>
        <ul v-if="attachmentFiles.length > 0" class="file-list-ul">
          <li v-for="(f, i) in attachmentFiles" :key="i" class="file-item">
            <div class="file-info">
              <el-icon><FileText /></el-icon>
              <span>{{ f.name }}</span>
              <span class="file-size">{{ f.size }}</span>
            </div>
            <button class="file-remove" @click="removeAttachment(i)">
              <el-icon><Close /></el-icon>
            </button>
          </li>
        </ul>
        <div v-if="attachmentFiles.length > 0" style="margin-top: 12px">
          <el-button type="primary" size="small" :loading="uploadingAttachment" @click="handleUploadAttachments">批量上传附件</el-button>
        </div>
      </div>
    </div>

    <!-- ⑤ 自荐材料 -->
    <div class="section-card">
      <div class="section-header">
        <h3 class="section-title">⑤ 自荐材料</h3>
        <p class="section-desc">个人陈述、自荐信等</p>
      </div>
      <el-form label-width="110px">
        <el-form-item label="个人陈述（PS）">
          <el-input v-model="draftForm.personalStatement.content" type="textarea" :rows="6" placeholder="请输入或粘贴个人陈述内容..." />
        </el-form-item>
      </el-form>
    </div>

    <!-- Sticky bottom bar -->
    <div class="sticky-bar">
      <el-button @click="router.back()">取消</el-button>
      <el-button type="info" :loading="saving" @click="handleSaveDraft">
        <el-icon><CircleCheck /></el-icon> 保存草稿
      </el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">
        <el-icon><Promotion /></el-icon> 提交申请
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #5e6b82;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}
.back-link:hover {
  color: #162033;
}

.section-card {
  border: 1px solid #d9e2ef;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.94);
  padding: 24px;
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4eaf3;
}

.section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a2b4c;
}

.section-desc {
  margin: 4px 0 0;
  font-size: 12px;
  color: #7b879c;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 24px;
}

.form-grid.three-col {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.form-grid .span-2 {
  grid-column: span 2;
}

@media (max-width: 1200px) {
  .form-grid.three-col {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.upload-area {
  margin-top: 16px;
}

.upload-dropzone {
  border: 2px dashed #d1d9e6;
  border-radius: 12px;
  background: #f8fafd;
  padding: 32px;
  text-align: center;
  transition: border-color 0.2s;
}

.upload-dropzone:hover {
  border-color: #409eff;
}

.upload-icon {
  font-size: 32px;
  color: #9aa8bf;
  margin-bottom: 8px;
}

.upload-title {
  font-size: 14px;
  font-weight: 500;
  color: #394760;
}

.upload-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #7b879c;
  margin-bottom: 12px;
}

.file-list {
  margin-top: 12px;
}

.file-list-ul {
  margin: 12px 0 0;
  padding: 0;
  list-style: none;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border: 1px solid #e4eaf3;
  border-radius: 8px;
  background: #f8fafd;
  margin-bottom: 8px;
}

.file-item.success {
  border-color: #c2e7b0;
  background: #f0f9eb;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #394760;
}

.file-size {
  font-size: 11px;
  color: #9aa8bf;
}

.file-remove {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: none;
  background: transparent;
  color: #9aa8bf;
  cursor: pointer;
}

.file-remove:hover {
  background: #fef0f0;
  color: #f56c6c;
}

.sticky-bar {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 24px;
  margin: 0 -24px -24px;
  border-top: 1px solid #e4eaf3;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border-radius: 0 0 16px 16px;
}
</style>
