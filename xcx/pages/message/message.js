const { request } = require('../../utils/request')
Page({
  data: { messages: [], unreadCount: 0 },
  onShow() {
    request({ url: '/api/message/unread-count' }).then(res => {
      if (res.code === 200) this.setData({ unreadCount: res.data })
    })
    request({ url: '/api/notification/list' }).then(res => {
      if (res.code === 200) this.setData({ messages: res.data })
    })
  }
})
