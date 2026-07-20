<template>
  <section class="page admin-page">
    <div class="page-heading">
      <div>
        <h1>账户管理</h1>
        <p>查看用户资料和账号状态，支持按昵称或手机号筛选。</p>
      </div>
    </div>

    <section class="admin-table-card">
      <div class="admin-filter-bar">
        <div class="admin-filter-group">
          <el-input
            v-model="keyword"
            placeholder="搜索昵称或手机号"
            clearable
            @clear="applySearch"
            @keyup.enter="applySearch"
          />
          <el-button type="primary" @click="applySearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
        <div class="table-tools">
          <el-button type="primary" @click="openAdminDialog">新增管理员</el-button>
          <el-button text type="primary" @click="loadUsers">刷新</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="pagedUsers">
        <el-table-column prop="id" label="用户ID" width="90" />
        <el-table-column label="头像" width="84">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar || ''">{{ row.nickname?.slice(0, 1) || '用' }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column label="手机号" width="160">
          <template #default="{ row }">{{ row.maskedPhone || row.phone }}</template>
        </el-table-column>
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'warning' : 'info'">{{ row.role === 1 ? '管理员' : '普通用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="190">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="row.status === 1 ? 'danger' : 'primary'"
              :disabled="row.role === 1"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="appliedKeyword ? '未找到相关用户' : '暂无用户数据'">
            <el-button type="primary" @click="loadUsers">刷新</el-button>
          </el-empty>
        </template>
      </el-table>

      <el-pagination
        v-if="filteredUsers.length > 0"
        v-model:current-page="currentPage"
        class="table-pagination"
        background
        layout="total, prev, pager, next, jumper"
        :page-size="pageSize"
        :total="filteredUsers.length"
      />
    </section>

    <el-dialog v-model="adminDialogVisible" title="新增管理员" width="460px">
      <el-form
        ref="adminFormRef"
        label-position="top"
        :model="adminForm"
        :rules="adminRules"
        @submit.prevent="createAdmin"
      >
        <el-form-item label="管理员昵称" prop="nickname">
          <el-input v-model="adminForm.nickname" maxlength="50" placeholder="请输入管理员昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="adminForm.phone" maxlength="11" placeholder="请输入管理员手机号" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input
            v-model="adminForm.password"
            show-password
            maxlength="32"
            placeholder="请输入 6 到 32 位密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="adminForm.confirmPassword"
            show-password
            maxlength="32"
            placeholder="请再次输入密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creatingAdmin" @click="createAdmin">确认新增</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { adminApi } from '../../api'
import { formatDateTime } from '../../utils/format'

const users = ref([])
const loading = ref(false)
const creatingAdmin = ref(false)
const keyword = ref('')
const appliedKeyword = ref('')
const currentPage = ref(1)
const pageSize = 8
const adminDialogVisible = ref(false)
const adminFormRef = ref()
const phonePattern = /^1[3-9]\d{9}$/
const adminForm = reactive({
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

// 管理员新增表单校验：公开注册只创建普通用户，后台这里才允许创建管理员。
const adminRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '昵称长度不能超过 50 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phonePattern, message: '请输入正确的手机格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度必须为 6 到 32 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== adminForm.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const filteredUsers = computed(() => {
  const key = appliedKeyword.value
  if (!key) {
    return users.value
  }
  return users.value.filter((user) => {
    const nickname = String(user.nickname || '')
    const phone = String(user.phone || user.maskedPhone || '')
    return nickname.includes(key) || phone.includes(key)
  })
})

const pagedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredUsers.value.slice(start, start + pageSize)
})

// 加载用户列表，后端返回的 DTO 不包含密码。
const loadUsers = async () => {
  loading.value = true
  try {
    const result = await adminApi.users()
    users.value = result.data || []
    currentPage.value = 1
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '用户加载失败')
  } finally {
    loading.value = false
  }
}

// 启用或禁用普通用户；管理员账号按钮置灰，后端也会再次拒绝。
const toggleStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${actionText}用户「${row.nickname}」吗？`, `${actionText}用户`, {
      type: 'warning'
    })
    await adminApi.updateUserStatus(row.id, { status: nextStatus })
    ElMessage.success(`用户已${actionText}`)
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || `${actionText}失败`)
    }
  }
}

const applySearch = () => {
  appliedKeyword.value = keyword.value.trim()
  currentPage.value = 1
}

const resetSearch = () => {
  keyword.value = ''
  appliedKeyword.value = ''
  currentPage.value = 1
}

const resetAdminForm = () => {
  adminForm.nickname = ''
  adminForm.phone = ''
  adminForm.password = ''
  adminForm.confirmPassword = ''
}

const openAdminDialog = async () => {
  resetAdminForm()
  adminDialogVisible.value = true
  await nextTick()
  adminFormRef.value?.clearValidate()
}

// 提交新增管理员账号；后端会再次检查手机号唯一性和管理员权限。
const createAdmin = async () => {
  if (creatingAdmin.value) {
    return
  }
  try {
    await adminFormRef.value?.validate()
  } catch {
    return
  }

  creatingAdmin.value = true
  try {
    await adminApi.createAdmin({
      nickname: adminForm.nickname.trim(),
      phone: adminForm.phone.trim(),
      password: adminForm.password
    })
    ElMessage.success('管理员账号已新增')
    adminDialogVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '新增管理员失败')
  } finally {
    creatingAdmin.value = false
  }
}

// 注册时间统一转为“年月日 时分秒”展示。
const formatTime = formatDateTime

onMounted(loadUsers)
</script>

<style scoped>
.table-tools {
  align-items: center;
  display: flex;
  gap: 8px;
}

.table-pagination {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
