import request from '@/utils/request'

export default {
    // 新增管理员
    add(data) {
        return request.post('/admin/add', data)
    },

    // 删除管理员
    deleteById(id) {
        return request.delete(`/admin/delete/${id}`)
    },

    // 修改管理员
    update(data) {
        return request.put('/admin/update', data)
    },

    // 根据ID查询
    selectById(id) {
        return request.get(`/admin/selectById/${id}`)
    },

    // 查询所有
    selectAll(params) {
        return request.get('/admin/selectAll', { params })
    },

    // 分页查询
    selectPage(params) {
        return request.get('/admin/selectPage', { params })
    },

    // 登录（在WebController中）
    login(data) {
        return request.post('/login', { ...data, role: '管理员' })
    },

    // 修改密码（在WebController中）
    updatePassword(data) {
        return request.put('/updatePassword', null, { params: { ...data, role: '管理员' } })
    }
}