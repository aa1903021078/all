const { request } = require('../../utils/request')

Page({
  data: {
    book: {},
    isFavorite: false
  },
  onLoad(options) {
    this.loadDetail(options.id)
  },
  loadDetail(id) {
    request({ url: `/api/book/${id}` }).then(res => {
      if (res.code === 200) {
        this.setData({ book: res.data })
      }
    })
  },
  toggleFavorite() {
    const { book, isFavorite } = this.data
    if (isFavorite) {
      request({ url: `/api/favorite/${book.id}`, method: 'DELETE' }).then(() => {
        this.setData({ isFavorite: false })
        wx.showToast({ title: '已取消收藏' })
      })
    } else {
      request({ url: '/api/favorite/add', method: 'POST', data: { bookId: book.id } }).then(() => {
        this.setData({ isFavorite: true })
        wx.showToast({ title: '已收藏' })
      })
    }
  },
  goExchange() {
    wx.navigateTo({ url: `/pages/exchange/exchange?bookId=${this.data.book.id}&ownerId=${this.data.book.userId}` })
  },
  contactOwner() {
    wx.navigateTo({ url: `/pages/chat/chat?userId=${this.data.book.userId}` })
  }
})
