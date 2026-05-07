const { request } = require('../../utils/request')
Page({
  data: { books: [] },
  onShow() {
    request({ url: '/api/user/my-books' }).then(res => {
      if (res.code === 200) this.setData({ books: res.data })
    })
  },
  goDetail(e) { wx.navigateTo({ url: `/pages/book-detail/book-detail?id=${e.currentTarget.dataset.id}` }) }
})
