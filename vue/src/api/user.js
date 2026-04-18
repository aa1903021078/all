import request from '@/utils/request'

export default {
    // 注册
    register(data) {
        return request.post('/user/register', data)
    },

    // 登录
    login(data) {
        return request.post('/login', { ...data, role: '普通用户' })
    },

    // 获取用户信息
    getUserProfile(id) {
        return request.get(`/user/profile/${id}`)
    },

    // 更新用户信息
    update(data) {
        return request.put('/user/update', data)
    },

    // 查询所有（管理员用）
    selectAll(params) {
        return request.get('/user/selectAll', { params })
    },

    // 分页查询
    selectPage(params) {
        return request.get('/user/selectPage', { params })
    },

    // 删除用户
    deleteById(id) {
        return request.delete(`/user/delete/${id}`)
    }
}