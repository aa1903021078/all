import request from '@/utils/request'

export const pageBooks = params => request.get('/api/books', { params })
export const getBook = id => request.get(`/api/books/${id}`)
export const listChapters = bookId => request.get(`/api/books/${bookId}/chapters`)
export const getChapter = chapterId => request.get(`/api/books/chapters/${chapterId}`)
export const hotBooks = (limit = 8) => request.get('/api/public/hot-books', { params: { limit } })
export const realtimeHotBooks = (n = 8) => request.get('/api/public/hot/realtime/books', { params: { n } })
export const realtimeHotItems = (n = 6) => request.get('/api/public/hot/realtime/items', { params: { n } })
export const myRecommend = (n = 10) => request.get('/api/recommend/my', { params: { n } })
export const similarBooks = (bookId, n = 6) =>
  request.get(`/api/public/recommend/similar/${bookId}`, { params: { n } })
export const rebuildRecommend = () => request.post('/api/admin/recommend/rebuild')
export const recommendStats = () => request.get('/api/admin/recommend/stats')
export const stats = () => request.get('/api/public/stats')

// bigscreen
export const dashOverview = () => request.get('/api/admin/dashboard/overview')
export const dashHotBooks = (n = 8) => request.get('/api/admin/dashboard/hot-books', { params: { n } })
export const dashOrderTrend = () => request.get('/api/admin/dashboard/order-trend')
export const dashBookTypeDist = () => request.get('/api/admin/dashboard/book-type-dist')
export const dashRecentEvents = (n = 50) => request.get('/api/admin/dashboard/recent-events', { params: { n } })
export const resetHot = () => request.post('/api/admin/dashboard/reset-hot')

// action track
export const trackAction = data => request.post('/api/actions', data)

// admin
export const saveBook = data => request.post('/api/admin/books', data)
export const removeBook = id => request.delete(`/api/admin/books/${id}`)
export const saveChapter = data => request.post('/api/admin/books/chapters', data)
export const removeChapter = id => request.delete(`/api/admin/books/chapters/${id}`)
