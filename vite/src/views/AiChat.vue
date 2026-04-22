<template>
  <div class="yq-ai-page">
    <aside class="yq-ai-side">
      <div class="p-3 border-b flex items-center justify-between">
        <div class="font-bold">AI 对话</div>
        <el-button size="small" type="primary" plain @click="newOne">新对话</el-button>
      </div>
      <div class="yq-ai-list">
        <div v-for="c in conversations" :key="c.conversationId"
             class="yq-ai-item" :class="{ active: conversationId === c.conversationId }"
             @click="openOne(c.conversationId)">
          <div class="truncate text-sm">{{ c.title || '新对话' }}</div>
          <div class="text-[11px] text-gray-400">{{ fmtTime(c.lastTime) }}</div>
        </div>
        <el-empty v-if="conversations.length === 0" description="暂无对话" />
      </div>
    </aside>

    <section class="yq-ai-main">
      <div class="yq-ai-header">
        <div class="font-bold">DeepSeek · 阅读助手</div>
        <div class="text-xs text-gray-400">AI 生成内容仅供参考，请谨慎判断。</div>
      </div>
      <div ref="bodyRef" class="yq-ai-body">
        <div v-for="m in messages" :key="m.id" class="yq-ai-msg" :class="{ mine: m.role === 'user' }">
          <div class="yq-ai-role">{{ m.role === 'user' ? '我' : 'AI' }}</div>
          <div class="yq-ai-bubble">{{ m.content }}</div>
        </div>
        <el-empty v-if="messages.length === 0" description="问点什么？例如：推荐一本武侠小说" />
      </div>
      <div class="yq-ai-input">
        <el-input v-model="draft" type="textarea" :rows="3" resize="none"
                  placeholder="输入问题，Ctrl+Enter 发送"
                  :disabled="sending"
                  @keydown.ctrl.enter.prevent="send" />
        <div class="flex justify-between items-center mt-2">
          <div class="text-xs text-gray-400">
            <span v-if="sending">AI 正在思考...</span>
          </div>
          <el-button type="primary" :loading="sending" @click="send" :disabled="!draft.trim()">
            发送
          </el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { aiChat, aiConversations, aiHistory, aiNew } from '@/api/comm'

const conversations = ref([])
const conversationId = ref('')
const messages = ref([])
const draft = ref('')
const sending = ref(false)
const bodyRef = ref(null)

async function loadConvs() {
  conversations.value = await aiConversations()
}

async function openOne(id) {
  conversationId.value = id
  messages.value = await aiHistory(id)
  await scrollBottom()
}

async function newOne() {
  const id = await aiNew()
  conversationId.value = id
  messages.value = []
  await loadConvs()
}

async function send() {
  if (!draft.value.trim()) return
  if (!conversationId.value) {
    conversationId.value = await aiNew()
  }
  const text = draft.value.trim()
  // 乐观展示
  messages.value.push({ id: 'local-' + Date.now(), role: 'user', content: text })
  draft.value = ''
  sending.value = true
  await scrollBottom()
  try {
    const reply = await aiChat(conversationId.value, text)
    messages.value.push(reply)
    await scrollBottom()
    await loadConvs()
  } finally {
    sending.value = false
  }
}

async function scrollBottom() {
  await nextTick()
  if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
}

function fmtTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(5, 16)
}

onMounted(async () => {
  await loadConvs()
  if (conversations.value[0]) {
    await openOne(conversations.value[0].conversationId)
  }
})
</script>

<style scoped>
.yq-ai-page { display: flex; height: calc(100vh - 60px); background: #f5f5f7; }
.yq-ai-side { width: 280px; background: #fff; border-right: 1px solid #eee; display: flex; flex-direction: column; }
.yq-ai-list { flex: 1; overflow-y: auto; padding: 6px; }
.yq-ai-item { padding: 10px 12px; border-radius: 8px; cursor: pointer; }
.yq-ai-item:hover { background: #fafafa; }
.yq-ai-item.active { background: #fdecec; }

.yq-ai-main { flex: 1; display: flex; flex-direction: column; }
.yq-ai-header { padding: 16px 24px; background: #fff; border-bottom: 1px solid #eee; }
.yq-ai-body { flex: 1; overflow-y: auto; padding: 20px 30px; background: #fafafa; }
.yq-ai-msg { margin-bottom: 16px; max-width: 75%; }
.yq-ai-msg.mine { margin-left: auto; text-align: right; }
.yq-ai-role { font-size: 11px; color: #888; margin-bottom: 4px; }
.yq-ai-bubble { display: inline-block; background: #fff; padding: 10px 14px; border-radius: 10px; box-shadow: 0 1px 2px rgba(0,0,0,0.06); white-space: pre-wrap; text-align: left; }
.yq-ai-msg.mine .yq-ai-bubble { background: #c20c0c; color: #fff; }
.yq-ai-input { padding: 14px 20px; background: #fff; border-top: 1px solid #eee; }
</style>
