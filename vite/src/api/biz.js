import request from '@/utils/request'

export const listComments = bookId => request.get(`/api/comments/book/${bookId}`)
export const createComment = data => request.post('/api/comments', data)
export const deleteComment = id => request.delete(`/api/comments/${id}`)

export const listAnnouncements = () => request.get('/api/announcements')
export const adminPageAnnouncements = params => request.get('/api/admin/announcements', { params })
export const saveAnnouncement = data => request.post('/api/admin/announcements', data)
export const removeAnnouncement = id => request.delete(`/api/admin/announcements/${id}`)

export const pageItems = params => request.get('/api/items', { params })
export const getItem = id => request.get(`/api/items/${id}`)
export const saveItem = data => request.post('/api/admin/items', data)
export const removeItem = id => request.delete(`/api/admin/items/${id}`)

export const listSongs = () => request.get('/api/songs')
export const adminPageSongs = params => request.get('/api/admin/songs', { params })
export const saveSong = data => request.post('/api/admin/songs', data)
export const removeSong = id => request.delete(`/api/admin/songs/${id}`)

export const myFavorites = () => request.get('/api/favorites')
export const addFavorite = bookId => request.post(`/api/favorites/${bookId}`)
export const removeFavorite = bookId => request.delete(`/api/favorites/${bookId}`)
export const checkFavorite = bookId => request.get(`/api/favorites/check/${bookId}`)

export const createOrder = data => request.post('/api/orders', data)
export const myOrders = params => request.get('/api/orders/my', { params })
export const adminPageOrders = params => request.get('/api/admin/orders', { params })
export const setOrderStatus = (orderId, status) =>
  request.put(`/api/admin/orders/${orderId}/status`, null, { params: { status } })

export const pageUsers = params => request.get('/api/admin/users', { params })
export const setUserStatus = (id, status) =>
  request.put(`/api/admin/users/${id}/status`, null, { params: { status } })
export const deleteUser = id => request.delete(`/api/admin/users/${id}`)
