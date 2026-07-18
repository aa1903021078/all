<template>
  <div class="chat-panel card">
    <!-- 会话列表 -->
    <div class="session-list">
      <div class="list-head">💬 消息列表</div>
      <div
        v-for="s in sessions"
        :key="s.id"
        class="session-item"
        :class="{ active: current && current.id === s.id }"
        @click="openSession(s)"
      >
        <el-badge :value="s.unread" :hidden="!s.unread" :max="99">
          <el-avatar :size="42" :src="s.peerAvatar" />
        </el-badge>
        <div class="session-info">
          <div class="flex justify-between">
            <span class="bold line-1">{{ s.peerName || '用户' }}</span>
            <span class="muted text-sm">{{ shortTime(s.lastTime) }}</span>
          </div>
          <div class="muted text-sm line-1">
            <span v-if="s.shopName" class="shop-tag">{{ s.shopName }}</span>
            {{ s.lastMessage || '暂无消息' }}
          </div>
        </div>
      </div>
      <el-empty v-if="!sessions.length" description="暂无会话" :image-size="70" />
    </div>

    <!-- 消息区 -->
    <div class="message-area">
      <template v-if="current">
        <div class="msg-head">
          <span class="bold">{{ current.peerName }}</span>
          <span class="muted text-sm" v-if="current.shopName">· {{ current.shopName }}</span>
          <span class="ws-status" :class="{ on: connected }">{{ connected ? '● 在线' : '○ 连接中' }}</span>
        </div>
        <div class="msg-body" ref="bodyRef">
          <div
            v-for="m in messages"
            :key="m.id"
            class="msg-row"
            :class="{ mine: isMine(m) }"
          >
            <el-avatar :size="34" :src="isMine(m) ? myAvatar : current.peerAvatar" />
            <div class="bubble">{{ m.content }}</div>
          </div>
        </div>
        <div class="msg-input">
          <el-input
            v-model="text"
            placeholder="输入消息,回车发送"
            @keyup.enter="send"
          />
          <el-button type="primary" @click="send">发送</el-button>
        </div>
      </template>
      <div v-else class="empty-chat">
        <el-empty description="选择一个会话开始聊天" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { chatApi } from '@/api'
import { useAuthStore } from '@/store/auth'

const auth = useAuthStore()
const route = useRoute()

const sessions = ref([])
const current = ref(null)
const messages = ref([])
const text = ref('')
const connected = ref(false)
const bodyRef = ref(null)
let ws = null

const myId = auth.user && auth.user.id
const myAvatar = auth.user && auth.user.avatar

function isMine(m) {
  return m.fromUserId === myId
}

function shortTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(5, 16)
}

async function loadSessions(autoOpenId) {
  const res = await chatApi.sessions()
  sessions.value = res.data || []
  const target = autoOpenId
    ? sessions.value.find((s) => s.id === Number(autoOpenId))
    : null
  if (target) openSession(target)
}

async function openSession(s) {
  current.value = s
  const res = await chatApi.messages(s.id)
  messages.value = res.data || []
  s.unread = 0
  scrollBottom()
}

function scrollBottom() {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  })
}

function connect() {
  const token = auth.token
  if (!token) return
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  const url = proto + '://' + location.host + '/api/ws/chat?token=' + token
  ws = new WebSocket(url)
  ws.onopen = () => (connected.value = true)
  ws.onclose = () => (connected.value = false)
  ws.onmessage = (evt) => {
    let msg
    try {
      msg = JSON.parse(evt.data)
    } catch (e) {
      return
    }
    // 当前会话收到消息 -> 追加
    if (current.value && msg.sessionId === current.value.id) {
      messages.value.push(msg)
      scrollBottom()
    }
    // 刷新会话列表的最后一条/未读
    refreshSessionMeta(msg)
  }
}

function refreshSessionMeta(msg) {
  const s = sessions.value.find((x) => x.id === msg.sessionId)
  if (s) {
    s.lastMessage = msg.content
    s.lastTime = msg.createTime
    if ((!current.value || current.value.id !== msg.sessionId) && msg.toUserId === myId) {
      s.unread = (s.unread || 0) + 1
    }
  } else {
    loadSessions()
  }
}

function send() {
  if (!text.value.trim() || !current.value) return
  if (!ws || ws.readyState !== WebSocket.OPEN) return
  ws.send(JSON.stringify({ sessionId: current.value.id, content: text.value.trim(), type: 'TEXT' }))
  text.value = ''
}

onMounted(() => {
  loadSessions(route.query.sessionId)
  connect()
})

onBeforeUnmount(() => {
  if (ws) ws.close()
})
</script>

<style scoped>
.chat-panel {
  display: flex;
  height: calc(100vh - 160px);
  min-height: 460px;
}
.session-list {
  width: 300px;
  border-right: 1px solid var(--border);
  overflow-y: auto;
  flex-shrink: 0;
}
.list-head {
  padding: 16px;
  font-weight: 700;
  border-bottom: 1px solid var(--border);
}
.session-item {
  display: flex;
  gap: 10px;
  padding: 12px 14px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
}
.session-item:hover,
.session-item.active {
  background: var(--el-color-primary-light-9);
}
.session-info {
  flex: 1;
  min-width: 0;
}
.shop-tag {
  color: var(--brand);
  margin-right: 4px;
}
.message-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.msg-head {
  padding: 14px 18px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 8px;
}
.ws-status {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}
.ws-status.on {
  color: #67c23a;
}
.msg-body {
  flex: 1;
  overflow-y: auto;
  padding: 18px;
  background: #f7f8fa;
}
.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: flex-start;
}
.msg-row.mine {
  flex-direction: row-reverse;
}
.bubble {
  max-width: 60%;
  padding: 10px 14px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  line-height: 1.5;
  word-break: break-word;
}
.msg-row.mine .bubble {
  background: var(--brand);
  color: #fff;
}
.msg-input {
  display: flex;
  gap: 10px;
  padding: 14px;
  border-top: 1px solid var(--border);
}
.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
