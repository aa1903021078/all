const { request } = require('../../utils/request')

Page({
  data: {
    categories: [],
    books: [],
    currentCategory: null,
    page: 1
  },
  onLoad() {
    this.loadCategories()
  },
  loadCategories() {
    request({ url: '/api/category/list' }).then(res => {
      if (res.code === 200) {
        this.setData({ categories: res.data })
        if (res.data.length > 0) {
          this.setData({ currentCategory: res.data[0].id })
          this.loadBooks()
        }
      }
    })
  },
  loadBooks() {
    request({ url: '/api/book/list', data: { page: this.data.page, size: 10, categoryId: this.data.currentCategory } }).then(res => {
      if (res.code === 200) {
        this.setData({ books: res.data.records })
      }
    })
  },
  switchCategory(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ currentCategory: id, page: 1 })
    this.loadBooks()
  },
  goDetail(e) {
    wx.navigateTo({ url: `/pages/book-detail/book-detail?id=${e.currentTarget.dataset.id}` })
  }
})
