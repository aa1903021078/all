const { request, uploadFile } = require('../../utils/request')

Page({
  data: {
    categories: [],
    form: {
      title: '', author: '', isbn: '', publisher: '',
      grade: '', major: '', courseName: '',
      categoryId: '', conditionDesc: '', conditionLevel: 8,
      wantBookDesc: '', acceptCategory: ''
    },
    coverImg: '',
    detailImgs: [],
    gradeOptions: ['大一', '大二', '大三', '大四', '不限'],
    conditionOptions: [1,2,3,4,5,6,7,8,9,10]
  },
  onLoad() {
    request({ url: '/api/category/list' }).then(res => {
      if (res.code === 200) this.setData({ categories: res.data })
    })
  },
  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [`form.${field}`]: e.detail.value })
  },
  onPickerChange(e) {
    const field = e.currentTarget.dataset.field
    const index = e.detail.value
    if (field === 'grade') {
      this.setData({ 'form.grade': this.data.gradeOptions[index] })
    } else if (field === 'categoryId') {
      this.setData({ 'form.categoryId': this.data.categories[index].id })
    } else if (field === 'conditionLevel') {
      this.setData({ 'form.conditionLevel': this.data.conditionOptions[index] })
    }
  },
  chooseCover() {
    wx.chooseImage({
      count: 1,
      success: (res) => {
        uploadFile(res.tempFilePaths[0]).then(data => {
          if (data.code === 200) this.setData({ coverImg: data.data })
        })
      }
    })
  },
  chooseDetail() {
    wx.chooseImage({
      count: 5,
      success: (res) => {
        const promises = res.tempFilePaths.map(p => uploadFile(p))
        Promise.all(promises).then(results => {
          const imgs = results.filter(r => r.code === 200).map(r => r.data)
          this.setData({ detailImgs: [...this.data.detailImgs, ...imgs] })
        })
      }
    })
  },
  submit() {
    const { form, coverImg, detailImgs } = this.data
    if (!form.title) { wx.showToast({ title: '请输入书名', icon: 'none' }); return }
    const data = { ...form, coverImg, detailImgs: detailImgs.join(',') }
    request({ url: '/api/book/publish', method: 'POST', data }).then(res => {
      if (res.code === 200) {
        wx.showToast({ title: '发布成功' })
        setTimeout(() => wx.navigateBack(), 1500)
      } else {
        wx.showToast({ title: res.message, icon: 'none' })
      }
    })
  }
})
