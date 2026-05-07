const { request } = require('../../utils/request')
Page({
  data: { favorites: [] },
  onShow() {
    request({ url: '/api/user/my-favorites' }).then(res => {
      if (res.code === 200) this.setData({ favorites: res.data })
    })
  },
  goDetail(e) { wx.navigateTo({ url: `/pages/book-detail/book-detail?id=${e.currentTarget.dataset.id}` }) }
})
