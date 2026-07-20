import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/user/HomeView.vue'
import ProductListView from '../views/user/ProductListView.vue'
import ProductDetailView from '../views/user/ProductDetailView.vue'
import LoginView from '../views/user/LoginView.vue'
import CartView from '../views/user/CartView.vue'
import OrderConfirmView from '../views/user/OrderConfirmView.vue'
import OrderDetailView from '../views/user/OrderDetailView.vue'
import ProfileView from '../views/user/ProfileView.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminProductView from '../views/admin/AdminProductView.vue'
import AdminCategoryView from '../views/admin/AdminCategoryView.vue'
import AdminOrderView from '../views/admin/AdminOrderView.vue'
import AdminUserView from '../views/admin/AdminUserView.vue'
import { ElMessage } from 'element-plus'

// 路由表：meta.requiresAuth 表示必须登录，meta.requiresAdmin 表示必须管理员。
const routes = [
  { path: '/', component: HomeView, meta: { public: true } },
  { path: '/products', component: ProductListView, meta: { public: true } },
  { path: '/products/:id', component: ProductDetailView, meta: { public: true } },
  { path: '/login', component: LoginView, meta: { guestOnly: true } },
  { path: '/cart', component: CartView, meta: { requiresAuth: true } },
  { path: '/order/confirm', component: OrderConfirmView, meta: { requiresAuth: true } },
  { path: '/orders/:id', component: OrderDetailView, meta: { requiresAuth: true } },
  { path: '/profile', component: ProfileView, meta: { requiresAuth: true } },
  { path: '/admin', component: AdminDashboardView, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/products', component: AdminProductView, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/categories', component: AdminCategoryView, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/orders', component: AdminOrderView, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/users', component: AdminUserView, meta: { requiresAuth: true, requiresAdmin: true } }
]

// 使用 HTML5 history 模式，生产部署时需要后端或静态服务器支持前端路由回退。
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 从 localStorage 读取登录态。路由守卫只做轻量判断，后端拦截器仍是最终权限保障。
const getSession = () => {
  const token = localStorage.getItem('fishMallToken')
  const userText = localStorage.getItem('fishMallUser')
  let user = null
  try {
    user = userText ? JSON.parse(userText) : null
  } catch (error) {
    user = null
  }
  return { token, user }
}

// 前端路由拦截：未登录访问购物车/个人中心/后台时跳转登录；非管理员不能进入后台。
router.beforeEach((to) => {
  const { token, user } = getSession()

  if (to.meta.guestOnly && token) {
    return user?.role === 1 ? '/admin' : '/'
  }

  if (to.meta.requiresAuth && (!token || !user)) {
    ElMessage.warning('请先登录后再访问该页面')
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }

  if (to.meta.requiresAdmin && user?.role !== 1) {
    ElMessage.warning('当前账号没有后台访问权限')
    return '/'
  }

  return true
})

export default router
