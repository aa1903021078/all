import request from '@/utils/request'

export const login = data => request.post('/api/auth/login', data)
export const register = data => request.post('/api/auth/register', data)
export const logout = () => request.post('/api/auth/logout')
export const listTestUsers = () => request.get('/api/auth/test-users')
export const getMe = () => request.get('/api/users/me')
export const updateMe = data => request.put('/api/users/me', data)
export const changePassword = data => request.put('/api/users/me/password', data)
