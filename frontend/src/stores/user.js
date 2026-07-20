import { defineStore } from 'pinia'
import { authApi } from '../api'

// 用户状态仓库：保存登录 Token 和用户信息，并同步到 localStorage 以支持刷新后保持登录态。
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('fishMallToken') || '',
    user: JSON.parse(localStorage.getItem('fishMallUser') || 'null')
  }),
  actions: {
    // 调用后端登录接口，成功后保存 Token 和用户信息。
    async login(form) {
      const result = await authApi.login(form)
      this.setSession(result.data.token, result.data.user)
      return result.data.user
    },
    // 调用后端注册接口，成功后自动保存登录态。
    async register(form) {
      const result = await authApi.register(form)
      this.setSession(result.data.token, result.data.user)
      return result.data.user
    },
    // 根据当前 Token 查询后端用户信息，用于刷新页面后校验登录态是否仍有效。
    async fetchCurrentUser() {
      const result = await authApi.me()
      this.user = result.data
      localStorage.setItem('fishMallUser', JSON.stringify(result.data))
      return result.data
    },
    // 修改当前用户昵称和头像后，同步刷新本地用户信息。
    async updateProfile(form) {
      const result = await authApi.updateMe(form)
      this.user = result.data
      localStorage.setItem('fishMallUser', JSON.stringify(result.data))
      return result.data
    },
    // 登录或注册成功后调用，写入内存状态和浏览器本地存储。
    setSession(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem('fishMallToken', token)
      localStorage.setItem('fishMallUser', JSON.stringify(user))
    },
    // 退出登录时调用，清理所有登录态。
    clearSession() {
      this.token = ''
      this.user = null
      localStorage.removeItem('fishMallToken')
      localStorage.removeItem('fishMallUser')
    }
  }
})
