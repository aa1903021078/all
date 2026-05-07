const { request } = require('../../utils/request')
const { requireLogin } = require('../../utils/auth')

Page({
  data: {
    recommendBooks: [],
    hotBooks: [],
    latestBooks: []
  },
  onLoad() {
    requireLogin().then(() => {
      this.loadData()
    })
  },
  onPullDownRefresh() {
    this.loadData().then(() => wx.stopPullDownRefresh())
  },
  loadData() {
    return Promise.all([
      request({ url: '/api/book/recommend' }),
      request({ url: '/api/book/hot' }),
      request({ url: '/api/book/latest' })
    ]).then(([rec, hot, latest]) => {
      this.setData({
        recommendBooks: rec.code === 200 ? rec.data : [],
        hotBooks: hot.code === 200 ? hot.data : [],
        latestBooks: latest.code === 200 ? latest.data : []
      })
    })
  },
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/book-detail/book-detail?id=${id}` })
  },
  goSearch() {
    wx.navigateTo({ url: '/pages/search/search' })
  }
})
