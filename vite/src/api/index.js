import request from '../utils/request'

// 管理员登录
export const adminLogin = (data) => request.post('/api/admin/login', data)

// 用户管理
export const getUserList = (params) => request.get('/api/admin/user/list', { params })
export const verifyUser = (id) => request.put(`/api/admin/user/verify/${id}`)
export const banUser = (id) => request.put(`/api/admin/user/ban/${id}`)
export const unbanUser = (id) => request.put(`/api/admin/user/unban/${id}`)
export const updateCredit = (data) => request.put('/api/admin/user/credit', data)

// 图书管理
export const getBookList = (params) => request.get('/api/admin/book/list', { params })
export const auditBook = (id, data) => request.put(`/api/admin/book/audit/${id}`, data)
export const deleteBook = (id) => request.delete(`/api/admin/book/${id}`)

// 分类管理
export const getCategoryList = () => request.get('/api/admin/category/list')
export const addCategory = (data) => request.post('/api/admin/category/add', data)
export const updateCategory = (data) => request.put('/api/admin/category/update', data)
export const deleteCategory = (id) => request.delete(`/api/admin/category/${id}`)

// 订单管理
export const getExchangeList = (params) => request.get('/api/admin/exchange/list', { params })
export const handleExchange = (id, data) => request.put(`/api/admin/exchange/handle/${id}`, data)

// 举报管理
export const getReportList = (params) => request.get('/api/admin/report/list', { params })
export const handleReport = (id, data) => request.put(`/api/admin/report/handle/${id}`, data)

// 公告管理
export const getAnnouncementList = () => request.get('/api/admin/announcement/list')
export const addAnnouncement = (data) => request.post('/api/admin/announcement/add', data)
export const updateAnnouncement = (data) => request.put('/api/admin/announcement/update', data)
export const deleteAnnouncement = (id) => request.delete(`/api/admin/announcement/${id}`)

// 数据统计
export const getStatsOverview = () => request.get('/api/admin/stats/overview')
