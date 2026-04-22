<template>
  <div class="p-4">
    <div class="text-lg font-bold mb-2">客服工单</div>
    <p class="text-sm text-gray-500 mb-3">展示所有用户发起的客服会话。左侧选择用户，右侧回复消息。</p>
    <Chat :support-mode="true" />
  </div>
</template>

<script setup>
import Chat from '@/views/Chat.vue'
</script>
