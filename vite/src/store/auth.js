import { defineStore } from 'pinia'
import { authApi } from '@/api'

const TOKEN_KEY = 'foodie_token'
const USER_KEY = 'foodie_user'

function loadUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  } catch (e) {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: loadUser(),
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    roles: (s) => (s.user && s.user.roles) || [],
    perms: (s) => (s.user && s.user.perms) || [],
    isAdmin: (s) => {
      const roles = (s.user && s.user.roles) || []
      const perms = (s.user && s.user.perms) || []
      return roles.includes('ADMIN') || perms.includes('*:*:*')
    },
    isMerchant: (s) => ((s.user && s.user.roles) || []).includes('MERCHANT'),
    isReviewer: (s) => ((s.user && s.user.roles) || []).includes('REVIEWER'),
  },
  actions: {
    hasRole(role) {
      return this.roles.includes(role)
    },
    hasPerm(perm) {
      return this.perms.includes('*:*:*') || this.perms.includes(perm)
    },
    // 是否可进入后台(超管/审核员/任一后台权限)
    canAccessAdmin() {
      if (this.isAdmin || this.isReviewer) return true
      const adminPerms = ['content:review', 'content:manage', 'shop:manage', 'user:manage', 'operation:manage', 'config:manage', 'dashboard:view']
      return adminPerms.some((p) => this.hasPerm(p))
    },
    setAuth(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    async login(dto) {
      const res = await authApi.login(dto)
      this.setAuth(res.data.token, res.data.user)
      return res.data.user
    },
    async register(dto) {
      return authApi.register(dto)
    },
    async fetchMe() {
      const res = await authApi.me()
      this.user = res.data
      localStorage.setItem(USER_KEY, JSON.stringify(res.data))
      return res.data
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },
  },
})
