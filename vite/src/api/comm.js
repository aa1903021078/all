import request from '@/utils/request'

// ===== chat =====
export const listOnline = () => request.get('/api/chat/online')
export const listMySessions = () => request.get('/api/chat/sessions')
export const listHistory = (peerId, limit = 100) =>
  request.get('/api/chat/history', { params: { peerId, limit } })
export const sendMessage = data => request.post('/api/chat/send', data)

// ===== support =====
export const startSupport = () => request.post('/api/support/start')
export const listSupportSessions = () => request.get('/api/admin/support/sessions')

// ===== ai =====
export const aiChat = (conversationId, content) =>
  request.post('/api/ai/chat', { conversationId, content })
export const aiConversations = () => request.get('/api/ai/conversations')
export const aiHistory = id => request.get(`/api/ai/conversations/${id}/messages`)
export const aiNew = () => request.post('/api/ai/conversations/new')

// ===== payment =====
export const orderDetail = orderId => request.get(`/api/orders/${orderId}`)
export const cancelOrder = orderId => request.post(`/api/orders/${orderId}/cancel`)
export const mockPay = orderId =>
  request.post('/api/payments/alipay/mock-pay', null, { params: { orderId } })
/** 获取支付表单 HTML（未登录时服务端会 401）。 */
export const payUrl = orderId => `/api/orders/${orderId}/pay`
