import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout, getMe } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('yq_token') || '',
    user: JSON.parse(localStorage.getItem('yq_user') || 'null')
  }),
  getters: {
    isLogin: s => !!s.token,
    isAdmin: s => s.user?.role === 'ADMIN'
  },
  actions: {
    async login(payload) {
      const data = await apiLogin(payload)
      this.token = data.token
      this.user = {
        userId: data.userId,
        username: data.username,
        role: data.role,
        avatar: data.avatar
      }
      localStorage.setItem('yq_token', this.token)
      localStorage.setItem('yq_user', JSON.stringify(this.user))
      return data
    },
    async refreshMe() {
      if (!this.token) return
      try {
        const u = await getMe()
        this.user = {
          userId: u.userId,
          username: u.username,
          role: u.role,
          avatar: u.avatar,
          phone: u.phone
        }
        localStorage.setItem('yq_user', JSON.stringify(this.user))
      } catch (e) {
        // ignore
      }
    },
    async logout() {
      try { await apiLogout() } catch (e) { /* ignore */ }
      this.token = ''
      this.user = null
      localStorage.removeItem('yq_token')
      localStorage.removeItem('yq_user')
    }
  }
})
