const { request } = require('../../utils/request')

Page({
  data: { ownerBookId: '', myBooks: [], selectedBookId: '', remark: '' },
  onLoad(options) {
    this.setData({ ownerBookId: options.bookId })
    this.loadMyBooks()
  },
  loadMyBooks() {
    request({ url: '/api/user/my-books' }).then(res => {
      if (res.code === 200) {
        this.setData({ myBooks: res.data.filter(b => b.status === 1) })
      }
    })
  },
  selectBook(e) {
    this.setData({ selectedBookId: e.currentTarget.dataset.id })
  },
  onRemarkInput(e) { this.setData({ remark: e.detail.value }) },
  submit() {
    if (!this.data.selectedBookId) { wx.showToast({ title: '请选择用来交换的书', icon: 'none' }); return }
    request({
      url: '/api/exchange/apply', method: 'POST',
      data: { ownerBookId: this.data.ownerBookId, requesterBookId: this.data.selectedBookId, remark: this.data.remark }
    }).then(res => {
      if (res.code === 200) {
        wx.showToast({ title: '申请已发送' })
        setTimeout(() => wx.navigateBack(), 1500)
      } else {
        wx.showToast({ title: res.message, icon: 'none' })
      }
    })
  }
})
