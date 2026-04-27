import { defineStore } from 'pinia'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    role: (s) => s.user?.role || '',
    isLogin: (s) => !!s.token,
    homePath: (s) => {
      const r = s.user?.role
      if (r === 'ADMIN') return '/admin'
      if (r === 'DOCTOR') return '/doctor'
      return '/patient'
    }
  },
  actions: {
    async login(username, password) {
      const r = await authApi.login({ username, password })
      this.token = r.data.token
      this.user = r.data.user
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      return this.user
    },
    async refresh() {
      const r = await authApi.me()
      this.user = r.data
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
