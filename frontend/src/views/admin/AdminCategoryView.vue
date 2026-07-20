<template>
  <section class="page admin-page">
    <div class="page-heading">
      <div>
        <h1>商品分类管理</h1>
        <p>维护前台商品筛选分类，删除前会校验是否关联商品。</p>
      </div>
    </div>

    <section class="admin-table-card">
      <div class="admin-filter-bar">
        <div />
        <el-button type="primary" @click="openCreate">新增分类</el-button>
      </div>

      <el-table v-loading="loading" :data="pagedCategories">
        <el-table-column prop="id" label="分类ID" width="100" />
        <el-table-column prop="name" min-width="180">
          <template #header>
            <button class="sort-head" type="button" @click="toggleSort('name')">
              <span>分类名称</span>
              <small>{{ sortButtonText('name') }}</small>
            </button>
          </template>
        </el-table-column>
        <el-table-column label="关联商品" width="110">
          <template #header>
            <button class="sort-head" type="button" @click="toggleSort('productCount')">
              <span>关联商品</span>
              <small>{{ sortButtonText('productCount') }}</small>
            </button>
          </template>
          <template #default="{ row }">{{ row.productCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="220">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleCategoryStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="removeCategory(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无分类，请新增">
            <el-button type="primary" @click="openCreate">新增分类</el-button>
          </el-empty>
        </template>
      </el-table>

      <el-pagination
        v-if="sortedCategories.length > 0"
        v-model:current-page="currentPage"
        class="table-pagination"
        background
        layout="total, prev, pager, next, jumper"
        :page-size="pageSize"
        :total="sortedCategories.length"
      />
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="420px">
      <el-form ref="categoryFormRef" label-position="top" :model="form" :rules="rules" @submit.prevent="saveCategory">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" maxlength="50" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio-button :label="1">启用</el-radio-button>
            <el-radio-button :label="0">禁用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { adminApi } from '../../api'
import { formatDateTime } from '../../utils/format'

const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const categoryFormRef = ref()
const currentPage = ref(1)
const pageSize = 12
const sortKey = ref('')
const sortOrder = ref('asc')
const form = reactive({
  name: '',
  sortOrder: 0,
  status: 1
})
const sortedCategories = computed(() => {
  const rows = [...categories.value]
  if (!sortKey.value) {
    return rows
  }
  return rows.sort((first, second) => {
    const direction = sortOrder.value === 'asc' ? 1 : -1
    if (sortKey.value === 'name') {
      return String(first.name || '').localeCompare(String(second.name || ''), 'zh-CN') * direction
    }
    return (Number(first.productCount || 0) - Number(second.productCount || 0)) * direction
  })
})
const pagedCategories = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return sortedCategories.value.slice(start, start + pageSize)
})

const validateName = (rule, value, callback) => {
  if (!String(value || '').trim()) {
    callback(new Error('请输入分类名称'))
    return
  }
  callback()
}

const rules = {
  name: [
    { validator: validateName, trigger: 'blur' },
    { max: 50, message: '分类名称不能超过 50 个字符', trigger: 'blur' }
  ],
  sortOrder: [{ type: 'number', min: 0, message: '分类排序不能小于 0', trigger: 'change' }]
}

// 加载后台分类列表，商品管理中的分类下拉也依赖同一套数据。
const loadCategories = async () => {
  loading.value = true
  try {
    const result = await adminApi.categories()
    categories.value = result.data || []
    currentPage.value = 1
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '分类加载失败')
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗，清空上一次编辑状态。
const openCreate = () => {
  editingId.value = null
  form.name = ''
  form.sortOrder = 0
  form.status = 1
  dialogVisible.value = true
  nextTick(() => categoryFormRef.value?.clearValidate())
}

// 打开编辑弹窗，回显当前分类名称。
const openEdit = (row) => {
  editingId.value = row.id
  form.name = row.name
  form.sortOrder = row.sortOrder || 0
  form.status = row.status ?? 1
  dialogVisible.value = true
  nextTick(() => categoryFormRef.value?.clearValidate())
}

// 保存分类；前端做必填和长度校验，唯一性由后端和数据库唯一索引保证。
const saveCategory = async () => {
  try {
    await categoryFormRef.value?.validate()
  } catch (error) {
    return
  }

  saving.value = true
  try {
    if (editingId.value) {
      await adminApi.updateCategory(editingId.value, buildPayload())
      ElMessage.success('分类已更新')
    } else {
      await adminApi.createCategory(buildPayload())
      ElMessage.success('分类已新增')
    }
    dialogVisible.value = false
    await loadCategories()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '分类保存失败')
  } finally {
    saving.value = false
  }
}

const buildPayload = () => ({
  name: form.name.trim(),
  sortOrder: form.sortOrder,
  status: form.status
})

// 表头排序按钮只在前端对当前列表排序，不影响后端保存的分类排序字段。
const toggleSort = (key) => {
  if (sortKey.value === key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortOrder.value = 'asc'
  }
  currentPage.value = 1
}

const sortButtonText = (key) => {
  if (sortKey.value !== key) {
    return '排序'
  }
  return sortOrder.value === 'asc' ? '升序' : '降序'
}

// 操作栏快捷切换分类状态，禁用后前台分类和该分类商品会自动隐藏。
const toggleCategoryStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${actionText}分类「${row.name}」吗？`, `${actionText}分类`, {
      type: 'warning'
    })
    await adminApi.updateCategory(row.id, {
      name: row.name,
      sortOrder: row.sortOrder || 0,
      status: nextStatus
    })
    ElMessage.success(`分类已${actionText}`)
    await loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || `${actionText}失败`)
    }
  }
}

// 删除分类前确认；后端会拒绝删除仍有关联商品的分类。
const removeCategory = async (row) => {
  try {
    await ElMessageBox.confirm(`分类「${row.name}」当前关联 ${row.productCount || 0} 个商品，确定删除吗？`, '删除分类', {
      type: 'warning'
    })
    await adminApi.removeCategory(row.id)
    ElMessage.success('分类已删除')
    await loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '分类删除失败')
    }
  }
}

// 创建时间统一格式化，兼容后端 LocalDateTime 的数组序列化。
const formatTime = formatDateTime

onMounted(loadCategories)
</script>

<style scoped>
.sort-head {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
}

.sort-head small {
  color: #409eff;
  font-size: 12px;
}

.table-pagination {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
