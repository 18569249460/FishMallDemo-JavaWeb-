<template>
  <section class="page admin-page">
    <div class="page-heading">
      <div>
        <h1>商品管理</h1>
        <p>按商品名称和分类筛选，维护价格、库存、图片和描述信息。</p>
      </div>
    </div>

    <section class="admin-table-card">
      <div class="admin-filter-bar">
        <div class="admin-filter-group">
          <el-input
            v-model="keyword"
            placeholder="搜索商品名称"
            clearable
            @clear="loadProducts"
            @keyup.enter="loadProducts"
          />
          <el-select v-model="categoryFilter" clearable placeholder="全部分类" style="width: 180px" @change="loadProducts">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
          <el-button type="primary" @click="loadProducts">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
        <el-button type="primary" @click="openCreate">新增商品</el-button>
      </div>

      <el-table v-loading="loading" :data="pagedProducts">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.imageUrl" class="thumb" :src="row.imageUrl" fit="cover" />
            <span v-else class="empty-thumb">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column label="单价" width="110">
          <template #default="{ row }">￥{{ formatPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column width="100">
          <template #header>
            <button class="sort-head" type="button" @click="toggleStockSort">
              <span>库存</span>
              <small>{{ stockSortText }}</small>
            </button>
          </template>
          <template #default="{ row }">{{ row.stock }}</template>
        </el-table-column>
        <el-table-column label="库存状态" width="110">
          <template #default="{ row }">
            <el-tag :type="stockType(row)">{{ stockText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上下架" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="specification" label="规格" width="130" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleProductStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="removeProduct(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="hasProductFilters ? '没有匹配的商品' : '暂无商品'">
            <el-button type="primary" @click="openCreate">新增商品</el-button>
          </el-empty>
        </template>
      </el-table>

      <el-pagination
        v-if="sortedProducts.length > pageSize"
        v-model:current-page="currentPage"
        class="table-pagination"
        background
        layout="total, prev, pager, next, jumper"
        :page-size="pageSize"
        :total="sortedProducts.length"
      />
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑商品' : '新增商品'" width="680px">
      <el-form
        ref="productFormRef"
        label-position="top"
        class="product-form"
        :model="form"
        :rules="rules"
        @submit.prevent="saveProduct"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" maxlength="200" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :precision="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="form.specification" maxlength="100" placeholder="如：500g/份" />
        </el-form-item>
        <el-form-item label="图片 URL" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="请输入图片地址，或点击下方选择图片" />
        </el-form-item>
        <el-form-item label="上传图片" class="full-row">
          <el-upload
            :auto-upload="false"
            :disabled="uploadingImage"
            :show-file-list="false"
            accept="image/*"
            :on-change="handleImageChange"
          >
            <el-button :loading="uploadingImage">选择图片</el-button>
          </el-upload>
          <p class="form-tip">图片会上传到后端 /uploads/images/shangpin 目录，数据库只保存图片访问地址。</p>
          <el-image v-if="form.imageUrl" class="preview-image" :src="form.imageUrl" fit="cover" />
        </el-form-item>
        <el-form-item label="产地">
          <el-input v-model="form.origin" maxlength="100" placeholder="请输入产地" />
        </el-form-item>
        <el-form-item label="保质期">
          <el-input v-model="form.shelfLife" maxlength="100" placeholder="例如：冷藏 2 天" />
        </el-form-item>
        <el-form-item label="储存方式">
          <el-input v-model="form.storageMethod" maxlength="100" placeholder="例如：0-4℃ 冷藏" />
        </el-form-item>
        <el-form-item label="配送说明">
          <el-input v-model="form.deliveryInfo" maxlength="200" placeholder="例如：同城冷链配送" />
        </el-form-item>
        <el-form-item label="商品状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio-button :label="1">上架</el-radio-button>
            <el-radio-button :label="0">下架</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商品描述" class="full-row">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { adminApi, uploadApi } from '../../api'

const products = ref([])
const categories = ref([])
const keyword = ref('')
const categoryFilter = ref(null)
const loading = ref(false)
const saving = ref(false)
const uploadingImage = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const productFormRef = ref()
const form = reactive(defaultForm())
const currentPage = ref(1)
const pageSize = 8
const stockSortOrder = ref('')
const hasProductFilters = computed(() => Boolean(keyword.value.trim() || categoryFilter.value))
const sortedProducts = computed(() => {
  const rows = [...products.value]
  if (!stockSortOrder.value) {
    return rows
  }
  const direction = stockSortOrder.value === 'asc' ? 1 : -1
  return rows.sort((first, second) => (Number(first.stock || 0) - Number(second.stock || 0)) * direction)
})
const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return sortedProducts.value.slice(start, start + pageSize)
})
const stockSortText = computed(() => {
  if (!stockSortOrder.value) {
    return '排序'
  }
  return stockSortOrder.value === 'asc' ? '升序' : '降序'
})

const validateName = (rule, value, callback) => {
  if (!String(value || '').trim()) {
    callback(new Error('请输入商品名称'))
    return
  }
  callback()
}

const validatePrice = (rule, value, callback) => {
  if (value === null || value === undefined || value === '') {
    callback(new Error('请输入商品价格'))
    return
  }
  if (Number(value) <= 0) {
    callback(new Error('商品价格必须大于 0'))
    return
  }
  callback()
}

const validateStock = (rule, value, callback) => {
  if (value === null || value === undefined || value === '') {
    callback(new Error('请输入商品库存'))
    return
  }
  if (Number(value) < 0) {
    callback(new Error('商品库存不能小于 0'))
    return
  }
  callback()
}

const rules = {
  name: [
    { validator: validateName, trigger: 'blur' },
    { max: 200, message: '商品名称不能超过 200 个字符', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ validator: validatePrice, trigger: 'change' }],
  stock: [{ validator: validateStock, trigger: 'change' }],
  specification: [{ max: 100, message: '商品规格不能超过 100 个字符', trigger: 'blur' }],
  imageUrl: [{ max: 200000, message: '图片内容过大，请更换较小图片', trigger: 'blur' }]
}

// 商品管理页进入时同时加载商品列表和分类下拉数据。
const loadInitialData = async () => {
  await Promise.all([loadCategories(), loadProducts()])
}

// 加载商品分类，用于新增/编辑商品时选择分类。
const loadCategories = async () => {
  try {
    const result = await adminApi.categories()
    categories.value = result.data || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '分类加载失败')
  }
}

// 加载商品列表，支持按商品名称搜索。
const loadProducts = async () => {
  loading.value = true
  try {
    const params = { keyword: keyword.value.trim() }
    if (categoryFilter.value !== null && categoryFilter.value !== '') {
      params.categoryId = categoryFilter.value
    }
    const result = await adminApi.products(params)
    products.value = result.data || []
    currentPage.value = 1
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '商品加载失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  keyword.value = ''
  categoryFilter.value = null
  loadProducts()
}

// 库存表头排序只影响当前商品列表的展示顺序，不修改数据库中的商品数据。
const toggleStockSort = () => {
  stockSortOrder.value = stockSortOrder.value === 'asc' ? 'desc' : 'asc'
  currentPage.value = 1
}

// 打开新增弹窗，重置表单。
const openCreate = () => {
  editingId.value = null
  Object.assign(form, defaultForm())
  dialogVisible.value = true
  nextTick(() => productFormRef.value?.clearValidate())
}

// 打开编辑弹窗，回显商品当前信息。
const openEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, {
    categoryId: row.categoryId,
    name: row.name,
    price: Number(row.price || 0),
    stock: Number(row.stock || 0),
    specification: row.specification || '',
    imageUrl: row.imageUrl || '',
    description: row.description || '',
    origin: row.origin || '',
    shelfLife: row.shelfLife || '',
    storageMethod: row.storageMethod || '',
    deliveryInfo: row.deliveryInfo || '',
    status: row.status ?? 1
  })
  dialogVisible.value = true
  nextTick(() => productFormRef.value?.clearValidate())
}

// 保存商品；前端做基础必填和格式校验，后端 DTO 和业务层仍会最终校验。
const saveProduct = async () => {
  try {
    await productFormRef.value?.validate()
  } catch (error) {
    return
  }

  saving.value = true
  const payload = buildPayload()
  try {
    if (editingId.value) {
      await adminApi.updateProduct(editingId.value, payload)
      ElMessage.success('商品已更新')
    } else {
      await adminApi.createProduct(payload)
      ElMessage.success('商品已新增')
    }
    dialogVisible.value = false
    await loadProducts()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '商品保存失败')
  } finally {
    saving.value = false
  }
}

// 删除商品前确认；历史订单通过订单商品快照保留原商品信息。
const removeProduct = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除商品「${row.name}」吗？`, '删除商品', {
      type: 'warning'
    })
    await adminApi.removeProduct(row.id)
    ElMessage.success('商品已删除')
    await loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '商品删除失败')
    }
  }
}

// 构造与后端 ProductSaveRequest 对齐的请求体。
const buildPayload = () => ({
  categoryId: form.categoryId,
  name: form.name.trim(),
  price: form.price,
  stock: form.stock,
  specification: trimToNull(form.specification),
  imageUrl: trimToNull(form.imageUrl),
  description: trimToNull(form.description),
  origin: trimToNull(form.origin),
  shelfLife: trimToNull(form.shelfLife),
  storageMethod: trimToNull(form.storageMethod),
  deliveryInfo: trimToNull(form.deliveryInfo),
  status: form.status
})

// 操作栏快捷上下架商品，前台列表只展示上架商品。
const toggleProductStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定${actionText}商品「${row.name}」吗？`, `${actionText}商品`, {
      type: 'warning'
    })
    await adminApi.updateProduct(row.id, buildRowPayload(row, nextStatus))
    ElMessage.success(`商品已${actionText}`)
    await loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || `${actionText}失败`)
    }
  }
}

// 快捷上下架需要把编辑接口的必填字段一并带回，避免只传 status 导致后端校验失败。
const buildRowPayload = (row, status) => ({
  categoryId: row.categoryId,
  name: row.name,
  price: row.price,
  stock: row.stock,
  specification: trimToNull(row.specification),
  imageUrl: trimToNull(row.imageUrl),
  description: trimToNull(row.description),
  origin: trimToNull(row.origin),
  shelfLife: trimToNull(row.shelfLife),
  storageMethod: trimToNull(row.storageMethod),
  deliveryInfo: trimToNull(row.deliveryInfo),
  status
})

function defaultForm() {
  return {
    categoryId: null,
    name: '',
    price: 1,
    stock: 0,
    specification: '',
    imageUrl: '',
    description: '',
    origin: '',
    shelfLife: '',
    storageMethod: '',
    deliveryInfo: '',
    status: 1
  }
}

const trimToNull = (value) => {
  const trimmed = String(value || '').trim()
  return trimmed || null
}
// 商品图片上传到后端商品图片目录，数据库只保存后端返回的访问地址。
const handleImageChange = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file) {
    return
  }
  if (!file.type || !file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('商品图片不能超过 5MB')
    return
  }

  uploadingImage.value = true
  try {
    const result = await uploadApi.product(file)
    form.imageUrl = result.data
    ElMessage.success('商品图片已上传')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '商品图片上传失败')
  } finally {
    uploadingImage.value = false
  }
}

const formatPrice = (price) => Number(price || 0).toFixed(2)
const stockText = (row) => {
  if (row.status !== 1) {
    return '已下架'
  }
  if (row.stock <= 0) {
    return '售罄'
  }
  if (row.stock <= 10) {
    return '库存偏低'
  }
  return '库存充足'
}
const stockType = (row) => {
  if (row.status !== 1) {
    return 'info'
  }
  if (row.stock <= 0) {
    return 'danger'
  }
  if (row.stock <= 10) {
    return 'warning'
  }
  return 'success'
}

onMounted(loadInitialData)
</script>

<style scoped>
.admin-actions {
  display: grid;
  width: min(560px, 100%);
  grid-template-columns: 1fr auto auto;
  gap: 10px;
}

.thumb {
  width: 56px;
  height: 42px;
  border-radius: 6px;
}

.empty-thumb {
  color: #909399;
}

.form-tip {
  margin: 8px 0;
  color: #909399;
  font-size: 13px;
}

.preview-image {
  width: 160px;
  height: 100px;
  border-radius: 8px;
}

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

.product-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.full-row {
  grid-column: 1 / -1;
}

@media (max-width: 760px) {
  .product-form {
    grid-template-columns: 1fr;
  }
}

.table-pagination {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
