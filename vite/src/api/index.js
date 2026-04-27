import http from './http'

export const authApi = {
  login: (data) => http.post('/api/auth/login', data),
  register: (data) => http.post('/api/auth/register', data),
  me: () => http.get('/api/auth/me'),
}

export const catalogApi = {
  hospitals: () => http.get('/api/catalog/hospitals'),
  departments: (hospitalId) => http.get('/api/catalog/departments', { params: { hospitalId } }),
  schedules: (params) => http.get('/api/catalog/schedules', { params })
}

export const patientApi = {
  book: (scheduleId) => http.post('/api/patient/appointments', { scheduleId }),
  myAppointments: () => http.get('/api/patient/appointments'),
  cancel: (id) => http.post(`/api/patient/appointments/${id}/cancel`),
  records: () => http.get('/api/patient/records'),
  recordByAppointment: (id) => http.get(`/api/patient/records/by-appointment/${id}`),
  referrals: () => http.get('/api/patient/referrals'),
  notifications: () => http.get('/api/patient/notifications'),
  markRead: (id) => http.post(`/api/patient/notifications/${id}/read`)
}

export const doctorApi = {
  appointments: (status) => http.get('/api/doctor/appointments', { params: { status } }),
  recordByAppointment: (id) => http.get(`/api/doctor/records/by-appointment/${id}`),
  diagnose: (id, payload) => http.post(`/api/doctor/diagnose/${id}`, payload),
  schedules: () => http.get('/api/doctor/schedules'),
  createSchedule: (s) => http.post('/api/doctor/schedules', s),
  closeSchedule: (id, reason) => http.post(`/api/doctor/schedules/${id}/close`, { reason }),
  outgoing: () => http.get('/api/doctor/referrals/outgoing'),
  incoming: () => http.get('/api/doctor/referrals/incoming'),
  createReferral: (data) => http.post('/api/doctor/referrals', data),
  approveReferral: (id, targetScheduleId) => http.post(`/api/doctor/referrals/${id}/approve`, { targetScheduleId }),
  rejectReferral: (id, reason) => http.post(`/api/doctor/referrals/${id}/reject`, { reason })
}

export const adminApi = {
  stats: () => http.get('/api/admin/stats'),
  hospitals: () => http.get('/api/admin/hospitals'),
  createHospital: (h) => http.post('/api/admin/hospitals', h),
  updateHospital: (id, h) => http.put(`/api/admin/hospitals/${id}`, h),
  deleteHospital: (id) => http.delete(`/api/admin/hospitals/${id}`),
  departments: (hospitalId) => http.get('/api/admin/departments', { params: { hospitalId } }),
  createDept: (d) => http.post('/api/admin/departments', d),
  updateDept: (id, d) => http.put(`/api/admin/departments/${id}`, d),
  deleteDept: (id) => http.delete(`/api/admin/departments/${id}`),
  users: (params) => http.get('/api/admin/users', { params }),
  createUser: (u) => http.post('/api/admin/users', u),
  updateUser: (id, u) => http.put(`/api/admin/users/${id}`, u),
  resetPwd: (id, password) => http.post(`/api/admin/users/${id}/reset-password`, { password }),
  toggleUser: (id) => http.post(`/api/admin/users/${id}/toggle`),
  schedules: () => http.get('/api/admin/schedules'),
  updateSchedule: (id, s) => http.put(`/api/admin/schedules/${id}`, s),
  closeSchedule: (id, reason) => http.post(`/api/admin/schedules/${id}/close`, { reason }),
  referrals: (params) => http.get('/api/admin/referrals', { params }),
  setReferralStatus: (id, status) => http.post(`/api/admin/referrals/${id}/status`, { status }),
  logs: (params) => http.get('/api/admin/logs', { params })
}

export const uploadUrl = '/api/upload'
