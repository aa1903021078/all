import request from '@/utils/request'

export default {
    // 新增地址
    add(data) {
        return request.post('/userAddress/add', data)
    },

    // 删除地址
    deleteById(id) {
        return request.delete(`/userAddress/delete/${id}`)
    },

    // 修改地址
    update(data) {
        return request.put('/userAddress/update', data)
    },

    // 设置默认地址
    setDefault(id, userId) {
        return request.put(`/userAddress/setDefault/${id}`, null, { params: { userId } })
    },

    // 查询用户地址
    selectByUser(userId) {
        return request.get(`/userAddress/selectByUser/${userId}`)
    },

    // 查询默认地址
    selectDefault(userId) {
        return request.get(`/userAddress/selectDefault/${userId}`)
    }
}