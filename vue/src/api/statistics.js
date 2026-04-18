import request from '@/utils/request'

export default {
    // 获取首页概览数据
    getOverview() {
        return request.get('/statistics/overview')
    },

    // 获取每日回收统计
    getDailyStats(params) {
        return request.get('/statistics/dailyStats', { params })
    },

    // 获取品类统计
    getApplianceTypeStats() {
        return request.get('/statistics/applianceTypeStats')
    },

    // 获取用户环保排名
    getUserRanking(limit = 10) {
        return request.get('/statistics/userRanking', { params: { limit } })
    },

    // 获取回收员绩效排行
    getCollectorRanking(limit = 10) {
        return request.get('/statistics/collectorRanking', { params: { limit } })
    },

    // 生成今日统计（定时任务）
    generateTodayStats() {
        return request.post('/statistics/generateToday')
    }
}