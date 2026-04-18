import request from '@/utils/request'

export default {
    // 新增品类
    add(data) {
        return request.post('/applianceType/add', data)
    },

    // 删除品类
    deleteById(id) {
        return request.delete(`/applianceType/delete/${id}`)
    },

    // 修改品类
    update(data) {
        return request.put('/applianceType/update', data)
    },

    // 查询启用的品类（前端用）
    selectEnabled() {
        return request.get('/applianceType/selectEnabled')
    },

    // 分页查询
    selectPage(params) {
        return request.get('/applianceType/selectPage', { params })
    },

    // 计算预估积分
    calculatePoints(typeId, weight) {
        return request.get('/applianceType/calculatePoints', { params: { typeId, weight } })
    },

    // 计算预估碳减排
    calculateCarbon(typeId, weight) {
        return request.get('/applianceType/calculateCarbon', { params: { typeId, weight } })
    }
}