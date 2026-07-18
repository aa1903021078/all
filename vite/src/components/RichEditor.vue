<template>
  <div class="rich-editor" :class="{ 'is-focus': focused }">
    <div class="re-toolbar">
      <button type="button" title="加粗" @mousedown.prevent="exec('bold')"><b>B</b></button>
      <button type="button" title="斜体" @mousedown.prevent="exec('italic')"><i>I</i></button>
      <button type="button" title="下划线" @mousedown.prevent="exec('underline')"><u>U</u></button>
      <span class="re-sep"></span>
      <button type="button" title="标题" @mousedown.prevent="formatBlock('h3')">H</button>
      <button type="button" title="正文" @mousedown.prevent="formatBlock('p')">¶</button>
      <span class="re-sep"></span>
      <button type="button" title="无序列表" @mousedown.prevent="exec('insertUnorderedList')">• 列表</button>
      <button type="button" title="有序列表" @mousedown.prevent="exec('insertOrderedList')">1. 列表</button>
      <span class="re-sep"></span>
      <button type="button" title="引用" @mousedown.prevent="formatBlock('blockquote')">❝</button>
      <button type="button" title="插入图片" :disabled="uploading" @mousedown.prevent="pickImage">
        <el-icon v-if="uploading" class="is-loading"><Loading /></el-icon>
        <span v-else>🖼 图片</span>
      </button>
      <button type="button" title="清除格式" @mousedown.prevent="exec('removeFormat')">清除</button>
    </div>
    <div
      ref="editorRef"
      class="re-body"
      :style="{ minHeight: minHeight }"
      contenteditable="true"
      :data-placeholder="placeholder"
      @input="onInput"
      @focus="focused = true"
      @blur="focused = false"
      @paste="onPaste"
    ></div>
    <input ref="fileRef" type="file" accept="image/*" class="re-file" @change="onFile" />
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { fileApi } from '@/api'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入内容...' },
  minHeight: { type: String, default: '180px' },
})
const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
const fileRef = ref(null)
const focused = ref(false)
const uploading = ref(false)

onMounted(() => {
  if (editorRef.value && props.modelValue) {
    editorRef.value.innerHTML = props.modelValue
  }
})

// 外部值变化时同步(避免光标跳动: 仅在与当前 DOM 不一致时写入)
watch(
  () => props.modelValue,
  (val) => {
    if (editorRef.value && val !== editorRef.value.innerHTML) {
      editorRef.value.innerHTML = val || ''
    }
  }
)

function emitChange() {
  emit('update:modelValue', editorRef.value ? editorRef.value.innerHTML : '')
}

function onInput() {
  emitChange()
}

function exec(command, value) {
  document.execCommand(command, false, value)
  editorRef.value && editorRef.value.focus()
  emitChange()
}

function formatBlock(tag) {
  // 不同浏览器对 formatBlock 的参数格式要求不同, 用尖括号包裹兼容性更好
  document.execCommand('formatBlock', false, '<' + tag + '>')
  editorRef.value && editorRef.value.focus()
  emitChange()
}

function pickImage() {
  fileRef.value && fileRef.value.click()
}

async function onFile(e) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await fileApi.uploadImage(file)
    const url = res.data
    // 在光标处插入图片
    editorRef.value && editorRef.value.focus()
    document.execCommand('insertImage', false, url)
    emitChange()
    ElMessage.success('图片已插入')
  } catch (err) {
    // 请求拦截器已提示
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}

// 粘贴时仅保留纯文本, 避免带入杂乱样式
function onPaste(e) {
  e.preventDefault()
  const text = (e.clipboardData || window.clipboardData).getData('text/plain')
  document.execCommand('insertText', false, text)
  emitChange()
}
</script>

<style scoped>
.rich-editor {
  border: 1px solid var(--el-border-color, #dcdfe6);
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.2s;
}
.rich-editor.is-focus {
  border-color: var(--brand, #ff6a3d);
}
.re-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  padding: 6px 8px;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}
.re-toolbar button {
  min-width: 30px;
  height: 28px;
  padding: 0 8px;
  border: 1px solid transparent;
  border-radius: 5px;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  color: #555;
  transition: all 0.15s;
}
.re-toolbar button:hover {
  background: #fff;
  border-color: #dcdfe6;
  color: var(--brand, #ff6a3d);
}
.re-toolbar button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.re-sep {
  width: 1px;
  height: 16px;
  background: #dcdfe6;
  margin: 0 4px;
}
.re-body {
  padding: 12px 14px;
  outline: none;
  line-height: 1.7;
  font-size: 14px;
  color: #303133;
  word-break: break-word;
}
.re-body:empty::before {
  content: attr(data-placeholder);
  color: #c0c4cc;
}
.re-body :deep(img) {
  max-width: 100%;
  border-radius: 6px;
  margin: 6px 0;
}
.re-body :deep(blockquote) {
  margin: 8px 0;
  padding: 6px 12px;
  border-left: 3px solid var(--brand, #ff6a3d);
  background: #fff7f4;
  color: #666;
}
.re-body :deep(h3) {
  margin: 10px 0 6px;
  font-size: 17px;
}
.re-file {
  display: none;
}
</style>
