<template>
  <div class="yq-chat-page">
    <aside class="yq-chat-side">
      <div class="yq-chat-side-header">
        <div class="text-base font-bold">{{ supportMode ? '我的客服会话' : '消息' }}</div>
        <el-tag v-if="!supportMode" size="small" type="danger">在线 {{ online.length }}</el-tag>
      </div>

      <el-tabs v-if="!supportMode" v-model="tab" class="px-3">
        <el-tab-pane label="会话" name="session" />
        <el-tab-pane label="在线用户" name="online" />
      </el-tabs>

      <!-- 会话列表 -->
      <div v-show="supportMode || tab === 'session'" class="yq-chat-list">
        <div v-for="s in sessions" :key="s.sessionId"
             class="yq-chat-item" :class="{ active: peer && s.peerId === peer.peerId }"
             @click="openSession(s)">
          <el-avatar :size="40" :src="s.peerAvatar || defaultAvatar" />
          <div class="flex-1 min-w-0">
            <div class="flex justify-between items-center">
              <span class="font-medium truncate">
                {{ s.peerName }}
                <el-tag v-if="s.isSupport === 1" size="small" type="danger" class="ml-1">客服</el-tag>
              </span>
              <span class="text-[11px] text-gray-400">{{ fmtTime(s.lastTime) }}</span>
            </div>
            <div class="text-xs text-gray-500 truncate">{{ s.lastMessage || '（无消息）' }}</div>
          </div>
          <span v-if="s.peerOnline" class="yq-dot online"></span>
        </div>
        <el-empty v-if="sessions.length === 0" description="暂无会话" />
      </div>

      <!-- 在线用户列表 -->
      <div v-show="!supportMode && tab === 'online'" class="yq-chat-list">
        <div v-for="u in online" :key="u.username" class="yq-chat-item"
             @click="startChatWith(u)">
          <el-avatar :size="40" :src="defaultAvatar" />
          <div class="flex-1 min-w-0">
            <div class="font-medium truncate">{{ u.username }}</div>
            <div class="text-xs text-green-500">● 在线</div>
          </div>
        </div>
        <el-empty v-if="online.length === 0" description="暂无其他在线用户" />
      </div>
    </aside>

    <section class="yq-chat-main">
      <template v-if="peer">
        <div class="yq-chat-header">
          <el-avatar :size="36" :src="peer.peerAvatar || defaultAvatar" />
          <div class="ml-3">
            <div class="font-bold">{{ peer.peerName }}
              <el-tag v-if="peer.peerRole === 'ADMIN'" size="small" type="danger">管理员</el-tag>
            </div>
            <div class="text-xs text-gray-400">
              <span v-if="peer.peerOnline" class="text-green-500">在线</span>
              <span v-else>离线</span>
            </div>
          </div>
        </div>
        <div ref="msgBoxRef" class="yq-chat-body">
          <div v-for="m in messages" :key="m.messageId || (m._localId)"
               class="yq-msg" :class="{ mine: m.senderId === myUserId }">
            <el-avatar :size="32" :src="m.senderId === myUserId ? (me?.avatar || defaultAvatar) : (peer.peerAvatar || defaultAvatar)" />
            <div class="yq-bubble">
              <div class="whitespace-pre-wrap break-words">{{ m.content }}</div>
              <div class="yq-meta">{{ fmtTime(m.createTime) }}</div>
            </div>
          </div>
          <el-empty v-if="messages.length === 0" description="开始聊天吧" />
        </div>
        <div class="yq-chat-input">
          <el-input v-model="draft" type="textarea" :rows="2" resize="none"
                    placeholder="输入消息，Ctrl+Enter 发送"
                    @keydown.ctrl.enter.prevent="send" />
          <el-button type="primary" class="mt-2" @click="send" :disabled="!draft.trim()">
            发送
          </el-button>
        </div>
      </template>
      <el-empty v-else description="选择一个会话开始聊天" />
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listOnline, listMySessions, listHistory, sendMessage, startSupport } from '@/api/comm'
import { ensureConnected, onChatMessage, sendChatViaWs } from '@/utils/ws'
import { useUserStore } from '@/store/user'

const props = defineProps({
  supportMode: { type: Boolean, default: false }
})
const route = useRoute()
const userStore = useUserStore()
const me = computed(() => userStore.user)
const myUserId = computed(() => userStore.user?.userId)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const tab = ref('session')
const online = ref([])
const sessions = ref([])
const peer = ref(null)
const messages = ref([])
const draft = ref('')
const msgBoxRef = ref(null)
let unsub = null
let pollTimer = null

async function loadSessions() {
  const list = await listMySessions()
  sessions.value = props.supportMode ? list.filter(s => s.isSupport === 1) : list
}
async function loadOnline() {
  online.value = (await listOnline()).filter(u => u.username !== me.value?.username)
}

async function openSession(s) {
  peer.value = s
  messages.value = await listHistory(s.peerId, 200)
  await scrollBottom()
}

async function startChatWith(u) {
  // 通过后端找到/创建会话（借用 support/start 是 admin 专用，这里走 send 时会自动建会话）
  // 这里直接把用户加入 peer 结构，发送时建会话
  peer.value = { peerId: await lookupId(u.username), peerName: u.username, peerAvatar: null, peerOnline: true }
  messages.value = await listHistory(peer.value.peerId, 200)
  await scrollBottom()
}

async function lookupId(username) {
  // 从会话列表里找已有会话
  const found = sessions.value.find(s => s.peerName === username)
  if (found) return found.peerId
  // 没有则让后端自己建会话时 lookup；这里先传 0，会触发 404
  ElMessage.warning('尚未建立会话，请先在管理端或通过客服发起')
  return 0
}

async function send() {
  if (!peer.value || !draft.value.trim()) return
  const content = draft.value.trim()
  const payload = {
    receiverId: peer.value.peerId,
    content,
    msgType: 0,
    support: props.supportMode || peer.value.peerRole === 'ADMIN'
  }
  const ok = sendChatViaWs(payload)
  if (!ok) {
    // HTTP 兜底
    const m = await sendMessage(payload)
    handleIncoming({ ...m, senderId: myUserId.value, createTime: m.createTime })
  }
  draft.value = ''
}

function handleIncoming(m) {
  const related = peer.value && (
    (m.senderId === peer.value.peerId && m.receiverId === myUserId.value) ||
    (m.senderId === myUserId.value && m.receiverId === peer.value.peerId)
  )
  if (related) {
    messages.value.push(m)
    scrollBottom()
  }
  // 更新会话列表
  loadSessions()
}

async function scrollBottom() {
  await nextTick()
  if (msgBoxRef.value) msgBoxRef.value.scrollTop = msgBoxRef.value.scrollHeight
}

function fmtTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  ensureConnected()
  unsub = onChatMessage(handleIncoming)
  await loadSessions()
  if (!props.supportMode) await loadOnline()
  pollTimer = setInterval(loadOnline, 30000)

  // 如果是客服入口（support 路由），自动打开客服会话
  if (props.supportMode && route.query.auto === '1') {
    try {
      const s = await startSupport()
      await loadSessions()
      const target = sessions.value.find(x => x.peerId === s.peerId)
      if (target) await openSession(target)
      else await openSession({ peerId: s.peerId, peerName: s.peerName, isSupport: 1 })
    } catch (e) { /* ignore */ }
  }
})
onBeforeUnmount(() => {
  unsub && unsub()
  pollTimer && clearInterval(pollTimer)
})

// 切换 supportMode（管理员后台 vs 用户端）时重载
watch(() => props.supportMode, loadSessions)
</script>

<style scoped>
.yq-chat-page { display: flex; height: calc(100vh - 60px); background: #f5f5f7; }
.yq-chat-side { width: 320px; background: #fff; border-right: 1px solid #eee; display: flex; flex-direction: column; }
.yq-chat-side-header { padding: 14px 16px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; }
.yq-chat-list { flex: 1; overflow-y: auto; padding: 6px; }
.yq-chat-item { display: flex; align-items: center; gap: 10px; padding: 8px 10px; border-radius: 8px; cursor: pointer; position: relative; }
.yq-chat-item:hover { background: #fafafa; }
.yq-chat-item.active { background: #fdecec; }
.yq-dot { width: 8px; height: 8px; border-radius: 50%; background: #bbb; position: absolute; right: 8px; top: 10px; }
.yq-dot.online { background: #52c41a; }

.yq-chat-main { flex: 1; display: flex; flex-direction: column; background: #fafafa; }
.yq-chat-header { height: 56px; padding: 0 20px; display: flex; align-items: center; background: #fff; border-bottom: 1px solid #eee; }
.yq-chat-body { flex: 1; overflow-y: auto; padding: 16px 24px; }
.yq-msg { display: flex; gap: 10px; margin-bottom: 14px; align-items: flex-start; }
.yq-msg .yq-bubble { max-width: 60%; background: #fff; padding: 8px 12px; border-radius: 10px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
.yq-msg.mine { flex-direction: row-reverse; }
.yq-msg.mine .yq-bubble { background: #c20c0c; color: #fff; }
.yq-meta { font-size: 10px; opacity: .6; margin-top: 4px; text-align: right; }
.yq-chat-input { padding: 12px 16px; background: #fff; border-top: 1px solid #eee; }
</style>
