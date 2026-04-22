import request from '@/utils/request'

export const pageBooks = params => request.get('/api/books', { params })
export const getBook = id => request.get(`/api/books/${id}`)
export const listChapters = bookId => request.get(`/api/books/${bookId}/chapters`)
export const getChapter = chapterId => request.get(`/api/books/chapters/${chapterId}`)
export const hotBooks = (limit = 8) => request.get('/api/public/hot-books', { params: { limit } })
export const stats = () => request.get('/api/public/stats')

// admin
export const saveBook = data => request.post('/api/admin/books', data)
export const removeBook = id => request.delete(`/api/admin/books/${id}`)
export const saveChapter = data => request.post('/api/admin/books/chapters', data)
export const removeChapter = id => request.delete(`/api/admin/books/chapters/${id}`)
