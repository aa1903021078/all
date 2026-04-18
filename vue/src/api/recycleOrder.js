import request from '@/utils/request'

export default {
    // 创建订单
    create(data) {
        return request.post('/recycleOrder/create', data)
    },

    // 智能派单
    dispatch(orderId) {
        return request.post(`/recycleOrder/dispatch/${orderId}`)
    },

    // 手动派单
    manualDispatch(orderId, collectorId) {
        return request.post('/recycleOrder/manualDispatch', null, { params: { orderId, collectorId } })
    },

    // 接单
    accept(orderId, collectorId) {
        return request.post('/recycleOrder/accept', null, { params: { orderId, collectorId } })
    },

    // 到达
    arrive(orderId) {
        return request.post('/recycleOrder/arrive', null, { params: { orderId } })
    },

    // 完成订单
    complete(data) {
        return request.post('/recycleOrder/complete', data)
    },

    // 取消订单
    cancel(orderId, reason) {
        return request.post('/recycleOrder/cancel', null, { params: { orderId, reason } })
    },

    // 查询用户订单
    selectByUser(userId) {
        return request.get(`/recycleOrder/selectByUser/${userId}`)
    },

    // 查询回收员订单
    selectByCollector(collectorId) {
        return request.get(`/recycleOrder/selectByCollector/${collectorId}`)
    },

    // 分页查询
    selectPage(params) {
        return request.get('/recycleOrder/selectPage', { params })
    },

    // 查询待派单
    selectPendingDispatch() {
        return request.get('/recycleOrder/selectPendingDispatch')
    },

    // 评分
    rate(orderId, rating, comment) {
        return request.post('/recycleOrder/rate', null, { params: { orderId, rating, comment } })
    },

    // 获取统计
    getStatistics() {
        return request.get('/recycleOrder/statistics')
    }
}