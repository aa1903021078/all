import request from '@/utils/request'

export default {
    // 查询商品列表（管理员用）
    selectAll(params) {
        return request.get('/pointsGoods/selectAll', { params })
    },

    // 查询可兑换商品（用户用）
    selectEnabled() {
        return request.get('/pointsGoods/selectAvailable')
    },

    // 新增商品
    add(data) {
        return request.post('/pointsGoods/add', data)
    },

    // 修改商品
    update(data) {
        return request.put('/pointsGoods/update', data)
    },

    // 删除商品
    deleteById(id) {
        return request.delete(`/pointsGoods/delete/${id}`)
    },

    // 兑换商品
    exchange(userId, goodsId) {
        return request.post('/pointsExchange/exchange', null, { params: { userId, goodsId } })
    },

    // 查询用户的兑换记录
    selectByUser(userId) {
        return request.get(`/pointsExchange/selectByUser/${userId}`)
    },

    // 查询所有兑换记录（管理员用）
    selectAllRecords(params) {
        return request.get('/pointsExchange/selectPage', { params })
    },

    // 确认发放
    ship(id) {
        return request.put(`/pointsExchange/ship/${id}`)
    }
}