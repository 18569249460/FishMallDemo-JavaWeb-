<template>
  <!-- 管理端使用独立外壳：顶部管理员栏 + 左侧侧边栏 + 右侧内容区。 -->
  <el-container v-if="isAdminRoute" class="admin-shell">
    <el-header class="admin-header">
      <!-- 管理端左上角品牌入口回到前台首页，便于管理员快速切回用户端浏览。 -->
      <router-link class="admin-brand" to="/">
        <span class="admin-logo">鲜</span>
        <span>鲜渔小店管理后台</span>
      </router-link>

      <div class="admin-user">
        <el-avatar :size="34" :src="userStore.user?.avatar || ''">
          {{ userStore.user?.nickname?.slice(0, 1) || '管' }}
        </el-avatar>
        <div class="admin-user-text">
          <strong>{{ userStore.user?.nickname || '管理员' }}</strong>
          <span>系统管理员</span>
        </div>
        <el-button link type="danger" :icon="SwitchButton" @click="logout">退出登录</el-button>
      </div>
    </el-header>

    <el-container class="admin-body">
      <el-aside class="admin-sidebar" width="224px">
        <el-menu router :default-active="activeAdminMenu" class="admin-menu">
          <el-menu-item index="/admin">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>账户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Collection /></el-icon>
            <span>商品分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><Tickets /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container class="admin-content-shell">
        <el-main class="admin-main">
          <router-view />
        </el-main>
        <el-footer class="admin-footer">
          © 2026 水产品管理系统 管理后台 版权所有 · ICP备案号：ICP备2026005188号
        </el-footer>
      </el-container>
    </el-container>
  </el-container>

  <!-- 用户端外壳：顶部导航连接首页、商品、购物车和个人中心页面。 -->
  <el-container v-else class="app-shell">
    <el-header class="app-header">
      <router-link class="brand" to="/">鲜渔小店</router-link>
      <el-menu mode="horizontal" router :default-active="activeMainMenu" :ellipsis="false" class="main-menu">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/products">商品分类</el-menu-item>
        <el-menu-item index="/cart">购物车</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
        <el-menu-item v-if="isAdmin" index="/admin">管理后台</el-menu-item>
      </el-menu>

      <div class="auth-actions">
        <el-button v-if="!userStore.token" type="primary" @click="goLogin">登录 / 注册</el-button>
        <el-dropdown v-else trigger="click" @command="handleUserCommand">
          <button class="avatar-trigger" type="button" :title="userStore.user?.nickname || '我的账号'">
            <el-avatar :size="36" :src="userStore.user?.avatar || ''">
              {{ userStore.user?.nickname?.slice(0, 1) || '用' }}
            </el-avatar>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item v-if="isAdmin" command="admin">管理后台</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
    <el-footer class="site-footer">
      © 2026 水产品管理系统 版权所有 · ICP备案号：ICP备2026005188号
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Collection, Goods, House, SwitchButton, Tickets, User } from '@element-plus/icons-vue'
import { useUserStore } from './stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 管理端入口只对管理员显示；后端仍会再次校验管理员权限。
const isAdmin = computed(() => userStore.user?.role === 1)
const isAdminRoute = computed(() => route.path.startsWith('/admin'))

// 顶部导航需要把详情页、结算页等子路由映射到对应菜单，避免跳转后高亮停在旧页面。
const activeMainMenu = computed(() => {
  const path = route.path
  if (path === '/') {
    return '/'
  }
  if (path.startsWith('/products')) {
    return '/products'
  }
  if (path.startsWith('/cart') || path.startsWith('/order/confirm')) {
    return '/cart'
  }
  if (path.startsWith('/profile') || path.startsWith('/orders')) {
    return '/profile'
  }
  if (path.startsWith('/admin')) {
    return '/admin'
  }
  return path
})

// 管理端侧边栏同样把子路径收敛到一级菜单，保证当前项稳定高亮。
const activeAdminMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/admin/users')) {
    return '/admin/users'
  }
  if (path.startsWith('/admin/categories')) {
    return '/admin/categories'
  }
  if (path.startsWith('/admin/products')) {
    return '/admin/products'
  }
  if (path.startsWith('/admin/orders')) {
    return '/admin/orders'
  }
  return '/admin'
})

// 顶部按钮进入登录页，并记录当前页面，登录后可返回原页面。
const goLogin = () => {
  router.push({
    path: '/login',
    query: { redirect: route.fullPath }
  })
}

// 用户下拉菜单命令处理。
const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'admin') {
    router.push('/admin')
    return
  }
  userStore.clearSession()
  ElMessage.success('已退出登录')
  router.push('/')
}

const logout = () => {
  userStore.clearSession()
  ElMessage.success('已退出登录')
  router.push('/')
}

// 页面刷新后如果本地有 Token，主动向后端确认当前用户仍然有效；失败则清理过期登录态。
onMounted(async () => {
  if (!userStore.token) {
    return
  }
  try {
    await userStore.fetchCurrentUser()
  } catch (error) {
    userStore.clearSession()
    if (route.meta.requiresAuth) {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
    }
  }
})
</script>

<style scoped>
.app-shell,
.admin-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  display: flex;
  align-items: center;
  gap: 28px;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
}

.brand {
  flex: 0 0 auto;
  font-size: 20px;
  font-weight: 700;
  color: #0f766e;
}

.main-menu {
  flex: 1;
  border-bottom: 0;
}

.auth-actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
}

.avatar-trigger {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  cursor: pointer;
}

.avatar-trigger:hover {
  background: #eef5f4;
}

.app-main {
  flex: 1;
}

.site-footer,
.admin-footer {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 10px 16px;
  color: #7c8994;
  font-size: 13px;
  text-align: center;
}

.admin-shell {
  background: #f3f6fa;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  height: 64px;
  border-bottom: 1px solid #dfe5ec;
  background: #ffffff;
}

.admin-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #12313a;
  font-size: 18px;
  font-weight: 700;
}

.admin-logo {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 8px;
  background: #0f766e;
  color: #ffffff;
  font-size: 17px;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-user-text {
  display: grid;
  min-width: 88px;
  color: #1f2a35;
  line-height: 1.25;
}

.admin-user-text span {
  color: #697782;
  font-size: 12px;
}

.admin-body {
  min-height: calc(100vh - 64px);
}

.admin-sidebar {
  border-right: 1px solid #dfe5ec;
  background: #ffffff;
}

.admin-menu {
  height: 100%;
  padding: 12px 10px;
  border-right: 0;
}

.admin-menu :deep(.el-menu-item) {
  height: 44px;
  margin-bottom: 6px;
  border-radius: 8px;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background: #e7f4f1;
  color: #0f766e;
  font-weight: 700;
}

.admin-content-shell {
  min-width: 0;
}

.admin-main {
  min-width: 0;
  padding: 24px;
}

.admin-main :deep(.page) {
  width: 100%;
  margin: 0;
  padding: 0;
}

@media (max-width: 760px) {
  .app-header {
    height: auto;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 8px;
    padding: 10px 16px;
  }

  .main-menu {
    order: 3;
    width: 100%;
  }

  .admin-header {
    height: auto;
    align-items: flex-start;
    flex-direction: column;
    padding: 12px 16px;
  }

  .admin-body {
    min-height: auto;
  }

  .admin-sidebar {
    width: 100% !important;
  }

  .admin-body {
    flex-direction: column;
  }

  .admin-menu {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(138px, 1fr));
    height: auto;
  }

  .admin-main {
    padding: 16px;
  }
}
</style>
