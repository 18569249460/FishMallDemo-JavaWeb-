import axios from 'axios'

// Axios 实例：统一接口前缀和超时时间，所有 API 模块都复用它。
const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截：如果本地已有登录 Token，则自动放入 Authorization 请求头。
// 这里直接读取 localStorage，避免 http 与 Pinia store 形成循环依赖。
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('fishMallToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：后端统一返回 ApiResponse；业务 code 非 200 时转成 rejected promise，方便页面统一 catch。
http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body.code !== 'undefined' && body.code !== 200) {
      return Promise.reject({
        response: { data: body },
        message: body.message
      })
    }
    return body
  },
  (error) => {
    // 这里把 Axios/浏览器层面的英文错误翻译成中文，页面 catch 时可以直接展示。
    if (error.code === 'ECONNABORTED') {
      error.message = '请求超时，请稍后重试'
    } else if (error.message === 'Network Error') {
      error.message = '无法连接后端服务，请确认 Tomcat 已启动'
    } else if (error.response?.status === 401) {
      error.message = error.response.data?.message || '请先登录'
    } else if (error.response?.status === 403) {
      error.message = error.response.data?.message || '没有权限'
    } else if (error.response?.status >= 500) {
      error.message = error.response.data?.message || '服务器异常，请稍后重试'
    }
    return Promise.reject(error)
  }
)

export default http
