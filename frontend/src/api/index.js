import http from './http'

// 认证接口：登录、注册。
export const authApi = {
  login: (data) => http.post('/auth/login', data),
  register: (data) => http.post('/auth/register', data),
  me: () => http.get('/users/me'),
  updateMe: (data) => http.put('/users/me', data)
}

// 文件上传接口：头像保存到 /uploads/images/touxiang，商品图保存到 /uploads/images/shangpin。
export const uploadApi = {
  avatar: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return http.post('/uploads/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  product: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return http.post('/uploads/product', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

// 用户常用收货地址接口。
export const addressApi = {
  list: () => http.get('/users/addresses'),
  create: (data) => http.post('/users/addresses', data),
  update: (id, data) => http.put(`/users/addresses/${id}`, data),
  remove: (id) => http.delete(`/users/addresses/${id}`),
  setDefault: (id) => http.put(`/users/addresses/${id}/default`)
}

// 分类接口：前台分类列表。
export const categoryApi = {
  list: () => http.get('/categories')
}

// 商品接口：商品列表、热门商品和详情。
export const productApi = {
  list: (params) => http.get('/products', { params }),
  hot: () => http.get('/products/hot'),
  detail: (id) => http.get(`/products/${id}`)
}

// 购物车接口：当前用户购物车增删改查。
export const cartApi = {
  list: () => http.get('/cart/items'),
  add: (data) => http.post('/cart/items', data),
  update: (id, data) => http.put(`/cart/items/${id}`, data),
  remove: (id) => http.delete(`/cart/items/${id}`),
  batchRemove: (ids) => http.post('/cart/items/batch-delete', { ids })
}

// 订单接口：提交订单、我的订单、订单详情、取消订单和确认收货。
export const orderApi = {
  create: (data) => http.post('/orders', data),
  my: () => http.get('/orders/my'),
  detail: (id) => http.get(`/orders/${id}`),
  confirm: (id) => http.put(`/orders/${id}/confirm`),
  cancel: (id) => http.put(`/orders/${id}/cancel`)
}

// 管理端接口：仪表盘、用户、分类、商品、订单管理。
export const adminApi = {
  dashboard: () => http.get('/admin/dashboard'),
  users: () => http.get('/admin/users'),
  createAdmin: (data) => http.post('/admin/users/admins', data),
  updateUserStatus: (id, data) => http.put(`/admin/users/${id}/status`, data),
  categories: () => http.get('/admin/categories'),
  createCategory: (data) => http.post('/admin/categories', data),
  updateCategory: (id, data) => http.put(`/admin/categories/${id}`, data),
  removeCategory: (id) => http.delete(`/admin/categories/${id}`),
  products: (params) => http.get('/admin/products', { params }),
  createProduct: (data) => http.post('/admin/products', data),
  updateProduct: (id, data) => http.put(`/admin/products/${id}`, data),
  removeProduct: (id) => http.delete(`/admin/products/${id}`),
  orders: (params) => http.get('/admin/orders', { params }),
  orderDetail: (id) => http.get(`/admin/orders/${id}`),
  shipOrder: (id, data) => http.put(`/admin/orders/${id}/ship`, data || {})
}
