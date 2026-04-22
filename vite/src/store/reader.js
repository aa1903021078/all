import { defineStore } from 'pinia'

/** 阅读器设置（字号/行距/夜间模式），localStorage 持久化。 */
export const useReaderStore = defineStore('reader', {
  state: () => ({
    fontSize: Number(localStorage.getItem('yq_reader_font') || 18),
    lineHeight: Number(localStorage.getItem('yq_reader_line') || 2.0),
    night: localStorage.getItem('yq_reader_night') === '1'
  }),
  actions: {
    setFont(v) { this.fontSize = v; localStorage.setItem('yq_reader_font', v) },
    setLine(v) { this.lineHeight = v; localStorage.setItem('yq_reader_line', v) },
    toggleNight() {
      this.night = !this.night
      localStorage.setItem('yq_reader_night', this.night ? '1' : '0')
    }
  }
})
