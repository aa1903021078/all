import request from '@/utils/request'

export default {
    // 新增公告
    add(data) {
        return request.post('/notice/add', data)
    },

    // 删除公告
    deleteById(id) {
        return request.delete(`/notice/delete/${id}`)
    },

    // 修改公告
    update(data) {
        return request.put('/notice/update', data)
    },

    // 根据ID查询
    selectById(id) {
        return request.get(`/notice/selectById/${id}`)
    },

    // 查询所有（管理员用）
    selectAll(params) {
        return request.get('/notice/selectAll', { params })
    },

    // 分页查询
    selectPage(params) {
        return request.get('/notice/selectPage', { params })
    },

    // 查询置顶公告
    selectTop() {
        return request.get('/notice/selectTop')
    },

    // 查询已发布公告（前端展示用）
    selectPublished(type) {
        return request.get('/notice/selectPublished', { params: { type } })
    },

    // 增加浏览次数
    incrementView(id) {
        return request.put(`/notice/increaseView/${id}`)
    },

    // 设置置顶
    setTop(id, isTop) {
        return request.put(`/notice/setTop/${id}`, null, { params: { isTop } })
    }
}