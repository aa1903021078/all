<template>
  <div v-for="c in items" :key="c.id" class="py-3 border-b last:border-0">
    <div class="flex items-start gap-3">
      <el-avatar :size="36" :src="c.avatar || defaultAvatar" />
      <div class="flex-1">
        <div class="flex items-center gap-2 text-sm">
          <span class="font-medium">{{ c.username || '用户' + c.userId }}</span>
          <span class="text-gray-400 text-xs">{{ c.createTime }}</span>
        </div>
        <div class="mt-1 whitespace-pre-wrap">{{ c.content }}</div>
        <div class="flex gap-3 mt-2 text-xs text-gray-500">
          <a class="cursor-pointer hover:text-red-600" @click="toggle(c.id)">回复</a>
          <a v-if="canDelete(c)" class="cursor-pointer hover:text-red-600" @click="$emit('delete', c.id)">删除</a>
        </div>
        <div v-if="replyingTo === c.id" class="mt-2 flex gap-2">
          <el-input v-model="replyText" size="small" :rows="2" type="textarea" />
          <div class="flex flex-col gap-1">
            <el-button size="small" type="primary" @click="send(c.id)">回复</el-button>
            <el-button size="small" @click="replyingTo = null">取消</el-button>
          </div>
        </div>
        <!-- 子评论（盖楼） -->
        <div v-if="c.children?.length" class="mt-2 pl-4 border-l-2 border-gray-100">
          <CommentTree :items="c.children" :current-user-id="currentUserId" :is-admin="isAdmin"
                       @reply="(pid, text) => $emit('reply', pid, text)"
                       @delete="(id) => $emit('delete', id)" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const props = defineProps({
  items: { type: Array, default: () => [] },
  currentUserId: { type: Number, default: null },
  isAdmin: { type: Boolean, default: false }
})
const emit = defineEmits(['reply', 'delete'])
const replyingTo = ref(null)
const replyText = ref('')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

function toggle(id) {
  replyingTo.value = replyingTo.value === id ? null : id
  replyText.value = ''
}
function send(parentId) {
  if (!replyText.value.trim()) return
  emit('reply', parentId, replyText.value)
  replyText.value = ''
  replyingTo.value = null
}
function canDelete(c) {
  return props.isAdmin || props.currentUserId === c.userId
}
</script>
