const { request } = require('../../utils/request')
Page({
  data: { messages: [], inputContent: '', contactId: '' },
  onLoad(options) {
    this.setData({ contactId: options.userId })
    this.loadMessages()
  },
  loadMessages() {
    request({ url: '/api/message/list', data: { contactId: this.data.contactId } }).then(res => {
      if (res.code === 200) this.setData({ messages: res.data })
    })
  },
  onInput(e) { this.setData({ inputContent: e.detail.value }) },
  send() {
    if (!this.data.inputContent) return
    request({ url: '/api/message/send', method: 'POST', data: { receiverId: this.data.contactId, content: this.data.inputContent } }).then(res => {
      if (res.code === 200) { this.setData({ inputContent: '' }); this.loadMessages() }
    })
  }
})
