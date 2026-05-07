const { request } = require('../../utils/request')
const app = getApp()
Page({
  data: { form: { studentId: '', realName: '', college: '', phone: '', nickname: '' } },
  onLoad() {
    const userInfo = app.globalData.userInfo
    if (userInfo) {
      this.setData({ form: { studentId: userInfo.studentId || '', realName: userInfo.realName || '', college: userInfo.college || '', phone: userInfo.phone || '', nickname: userInfo.nickname || '' } })
    }
  },
  onInput(e) { this.setData({ [`form.${e.currentTarget.dataset.field}`]: e.detail.value }) },
  submit() {
    const { form } = this.data
    request({ url: '/api/user/register', method: 'POST', data: form }).then(res => {
      if (res.code === 200) { wx.showToast({ title: '保存成功' }); setTimeout(() => wx.navigateBack(), 1000) }
      else wx.showToast({ title: res.message, icon: 'none' })
    })
  }
})
