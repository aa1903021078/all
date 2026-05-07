const { request } = require('../../utils/request')
Page({
  data: { orders: [], statusTexts: ['待确认','已接受','已拒绝','待交换','已完成','已取消'] },
  onShow() {
    request({ url: '/api/exchange/list' }).then(res => {
      if (res.code === 200) this.setData({ orders: res.data })
    })
  },
  accept(e) {
    request({ url: `/api/exchange/accept/${e.currentTarget.dataset.id}`, method: 'PUT', data: { exchangeMethod: '校内自提', exchangeLocation: '图书馆' } }).then(() => { wx.showToast({ title: '已接受' }); this.onShow() })
  },
  reject(e) {
    request({ url: `/api/exchange/reject/${e.currentTarget.dataset.id}`, method: 'PUT' }).then(() => { wx.showToast({ title: '已拒绝' }); this.onShow() })
  },
  confirm(e) {
    request({ url: `/api/exchange/confirm/${e.currentTarget.dataset.id}`, method: 'PUT' }).then(() => { wx.showToast({ title: '已确认完成' }); this.onShow() })
  },
  goReview(e) {
    wx.navigateTo({ url: `/pages/review/review?orderId=${e.currentTarget.dataset.id}` })
  }
})
