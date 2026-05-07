const { request } = require('../../utils/request')
const app = getApp()

Page({
  data: { userInfo: {} },
  onShow() {
    const userInfo = app.globalData.userInfo
    if (userInfo) this.setData({ userInfo })
    request({ url: '/api/user/info' }).then(res => {
      if (res.code === 200) this.setData({ userInfo: res.data })
    })
  },
  goPage(e) {
    wx.navigateTo({ url: e.currentTarget.dataset.url })
  },
  logout() {
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
    app.globalData.token = ''
    app.globalData.userInfo = null
    wx.reLaunch({ url: '/pages/index/index' })
  }
})
