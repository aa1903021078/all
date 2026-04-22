/**
 * STOMP over WebSocket 客户端封装：
 * - 单例 Client；
 * - 断线自动重连；
 * - 对外暴露 subscribeChat / subscribeOnline / sendChat。
 */
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'
import { useUserStore } from '@/store/user'

let client = null
let onChatHandlers = []
let onOnlineHandlers = []

export function ensureConnected() {
  if (client && client.active) return client
  const user = useUserStore()
  if (!user.token) return null

  client = new Client({
    // 通过 SockJS + token 握手，后端 JwtHandshakeInterceptor 校验
    webSocketFactory: () => new SockJS('/ws?token=' + encodeURIComponent(user.token)),
    reconnectDelay: 5000,
    heartbeatIncoming: 20000,
    heartbeatOutgoing: 20000,
    debug: () => {}
  })

  client.onConnect = () => {
    // 订阅私聊
    client.subscribe('/user/queue/chat', frame => {
      try {
        const msg = JSON.parse(frame.body)
        onChatHandlers.forEach(fn => fn(msg))
      } catch (e) { /* ignore */ }
    })
    // 订阅在线用户列表
    client.subscribe('/topic/online', frame => {
      try {
        const list = JSON.parse(frame.body)
        onOnlineHandlers.forEach(fn => fn(list))
      } catch (e) { /* ignore */ }
    })
  }

  client.onStompError = () => { /* ignore */ }
  client.activate()
  return client
}

export function disconnect() {
  if (client) {
    client.deactivate()
    client = null
  }
  onChatHandlers = []
  onOnlineHandlers = []
}

export function onChatMessage(fn) {
  onChatHandlers.push(fn)
  return () => { onChatHandlers = onChatHandlers.filter(f => f !== fn) }
}

export function onOnlineChange(fn) {
  onOnlineHandlers.push(fn)
  return () => { onOnlineHandlers = onOnlineHandlers.filter(f => f !== fn) }
}

/** 通过 STOMP 发送消息；若未连接，回退到 HTTP。 */
export function sendChatViaWs(payload) {
  if (client && client.connected) {
    client.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(payload)
    })
    return true
  }
  return false
}
