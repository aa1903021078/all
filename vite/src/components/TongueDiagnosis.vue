<script setup>
import { ref } from 'vue'

// 允许的 MIME 类型,与后端保持一致
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_BYTES = 4 * 1024 * 1024 // 4MB

const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')
const loading = ref(false)
const errorMsg = ref('')
const report = ref(null)

function resetPreview() {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = ''
  selectedFile.value = null
  report.value = null
  errorMsg.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

function onFileChange(e) {
  errorMsg.value = ''
  report.value = null
  const file = e.target.files && e.target.files[0]
  if (!file) return
  if (!ALLOWED_TYPES.includes(file.type)) {
    errorMsg.value = '仅支持 JPEG / PNG / WEBP 格式'
    e.target.value = ''
    return
  }
  if (file.size > MAX_BYTES) {
    errorMsg.value = '图片过大,请上传不超过 4MB 的图片'
    e.target.value = ''
    return
  }
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
}

async function analyze() {
  if (!selectedFile.value) {
    errorMsg.value = '请先选择舌头照片'
    return
  }
  loading.value = true
  errorMsg.value = ''
  report.value = null
  try {
    const form = new FormData()
    form.append('file', selectedFile.value)
    const resp = await fetch('/api/tongue/analyze', {
      method: 'POST',
      body: form,
    })
    const data = await resp.json().catch(() => ({}))
    if (!resp.ok) {
      errorMsg.value = data.error || `分析失败(${resp.status})`
      return
    }
    report.value = data
  } catch (e) {
    errorMsg.value = '网络异常,请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="tongue">
    <h2>AI 舌诊分析</h2>

    <div class="tips">
      <p><strong>拍摄小贴士</strong>(直接影响识别效果):</p>
      <ul>
        <li>自然光线下拍摄,避免强烈色偏</li>
        <li>张嘴伸舌,舌尖自然下垂,舌面完整入镜</li>
        <li>对焦清晰,不要模糊</li>
        <li>拍摄前避免食用会染色的食物(如咖啡、火龙果等)</li>
      </ul>
    </div>

    <div class="uploader">
      <input
        ref="fileInput"
        type="file"
        accept="image/jpeg,image/png,image/webp"
        capture="environment"
        @change="onFileChange"
      />
      <div v-if="previewUrl" class="preview">
        <img :src="previewUrl" alt="tongue preview" />
      </div>
      <div class="actions">
        <button :disabled="!selectedFile || loading" @click="analyze">
          {{ loading ? '分析中…' : '开始分析' }}
        </button>
        <button class="secondary" :disabled="loading" @click="resetPreview">
          重新上传
        </button>
      </div>
      <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
    </div>

    <div v-if="loading" class="loading">
      正在请求大模型分析,通常需要 5–15 秒,请稍候…
    </div>

    <div v-if="report" class="report">
      <div class="card" v-if="report.tongueBody">
        <h3>舌质</h3>
        <p>{{ report.tongueBody }}</p>
      </div>
      <div class="card" v-if="report.tongueCoating">
        <h3>舌苔</h3>
        <p>{{ report.tongueCoating }}</p>
      </div>
      <div class="card" v-if="report.tongueShape">
        <h3>舌型</h3>
        <p>{{ report.tongueShape }}</p>
      </div>
      <div class="card" v-if="report.syndrome">
        <h3>证型提示</h3>
        <p>{{ report.syndrome }}</p>
      </div>
      <div class="card" v-if="report.suggestion">
        <h3>调养建议</h3>
        <p>{{ report.suggestion }}</p>
      </div>
      <p class="disclaimer">
        {{ report.disclaimer || '本结果由 AI 根据图像生成,仅供健康参考,不构成医疗诊断,如有不适请及时就医。' }}
      </p>
    </div>
  </div>
</template>

<style scoped>
.tongue {
  max-width: 720px;
  margin: 0 auto;
  padding: 16px;
  text-align: left;
  color: #222;
}
.tongue h2 {
  text-align: center;
}
.tips {
  background: #f6f8fa;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 14px;
  margin-bottom: 16px;
}
.tips ul {
  margin: 6px 0 0 20px;
}
.uploader {
  border: 1px dashed #bbb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}
.preview {
  margin: 12px 0;
  text-align: center;
}
.preview img {
  max-width: 100%;
  max-height: 320px;
  border-radius: 6px;
}
.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.actions button {
  padding: 8px 18px;
  border-radius: 6px;
  border: none;
  background: #1677ff;
  color: #fff;
  cursor: pointer;
  font-size: 15px;
}
.actions button:disabled {
  background: #9dbdf0;
  cursor: not-allowed;
}
.actions button.secondary {
  background: #eee;
  color: #333;
}
.error {
  color: #d4380d;
  margin-top: 8px;
}
.loading {
  padding: 12px;
  background: #fff7e6;
  border-radius: 6px;
  text-align: center;
  margin-bottom: 12px;
}
.report .card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 10px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}
.report .card h3 {
  margin: 0 0 6px;
  font-size: 16px;
  color: #1677ff;
}
.report .card p {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.6;
}
.disclaimer {
  margin-top: 10px;
  font-size: 12px;
  color: #c0392b;
}
</style>
