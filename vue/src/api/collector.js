// src/api/collector.js
import request from '@/utils/request'

// 回收员注册（待审核）
export const register = (data) => request.post('/collector/register', data)

// 登录
export const login = (data) => request.post('/login', { ...data, role: '回收员' })

// 查询待审核列表（管理员）
export const selectPendingAudit = () => request.get('/collector/pendingAudit')

// 审核通过
export const auditPass = (id) => request.put(`/collector/auditPass/${id}`)

// 审核拒绝
export const auditReject = (id, reason) => request.put(`/collector/auditReject/${id}`, null, { params: { reason } })

// 更新工作状态
export const updateWorkStatus = (id, workStatus) => request.put('/collector/updateWorkStatus', null, { params: { id, workStatus } })

// 更新位置
export const updateLocation = (id, lat, lng) => request.put('/collector/updateLocation', null, { params: { id, lat, lng } })

// 查询附近回收员
export const selectNearby = (lat, lng, radius) => request.get('/collector/selectNearby', { params: { lat, lng, radius } })

// 获取今日统计
export const getTodayStats = (id) => request.get(`/collector/todayStats/${id}`)

// 分页查询
export const selectPage = (params) => request.get('/collector/selectPage', { params })

// 查询审核状态
export const queryStatus = (phone, idCard) => request.get('/collector/queryStatus', { params: { phone, idCard } })

// 重新提交申请
export const resubmit = (id, data) => request.put(`/collector/resubmit/${id}`, data)