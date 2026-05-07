const app = getApp()
const { request } = require('./request')

const login = () => {
  return new Promise((resolve, reject) => {
    // 模拟登录：生成一个随机code
    const code = 'mock_' + Date.now()
    request({
      url: '/api/wx/login',
      method: 'POST',
      data: { code }
    }).then(res => {
      if (res.code === 200) {
        app.globalData.token = res.data.token
        app.globalData.userInfo = res.data.userInfo
        wx.setStorageSync('token', res.data.token)
        wx.setStorageSync('userInfo', res.data.userInfo)
        resolve(res.data)
      } else {
        reject(res)
      }
    }).catch(reject)
  })
}

const checkLogin = () => {
  return !!app.globalData.token
}

const requireLogin = () => {
  if (!checkLogin()) {
    return login()
  }
  return Promise.resolve({ token: app.globalData.token, userInfo: app.globalData.userInfo })
}

module.exports = { login, checkLogin, requireLogin }
