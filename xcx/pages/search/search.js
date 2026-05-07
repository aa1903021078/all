const { request } = require('../../utils/request')
Page({
  data: { keyword: '', books: [] },
  onInput(e) { this.setData({ keyword: e.detail.value }) },
  search() {
    if (!this.data.keyword) return
    request({ url: '/api/book/list', data: { keyword: this.data.keyword, page: 1, size: 20 } }).then(res => {
      if (res.code === 200) this.setData({ books: res.data.records })
    })
  },
  goDetail(e) { wx.navigateTo({ url: `/pages/book-detail/book-detail?id=${e.currentTarget.dataset.id}` }) }
})
