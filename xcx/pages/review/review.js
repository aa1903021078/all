const { request } = require('../../utils/request')
Page({
  data: { orderId: '', creditScore: 5, conditionScore: 5, attitudeScore: 5, content: '' },
  onLoad(options) { this.setData({ orderId: options.orderId }) },
  onScoreChange(e) { this.setData({ [e.currentTarget.dataset.field]: e.detail.value }) },
  onInput(e) { this.setData({ content: e.detail.value }) },
  submit() {
    const { orderId, creditScore, conditionScore, attitudeScore, content } = this.data
    request({ url: '/api/review/submit', method: 'POST', data: { orderId, creditScore, conditionScore, attitudeScore, content } }).then(res => {
      if (res.code === 200) { wx.showToast({ title: '评价成功' }); setTimeout(() => wx.navigateBack(), 1000) }
      else wx.showToast({ title: res.message, icon: 'none' })
    })
  }
})
