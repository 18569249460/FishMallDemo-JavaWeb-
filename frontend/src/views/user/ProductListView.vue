<template>
  <section class="page">
    <div class="toolbar">
      <h1>商品分类</h1>
      <div class="search-row">
        <el-input
          v-model="keyword"
          placeholder="搜索商品名称"
          clearable
          @clear="loadProducts"
          @keyup.enter="loadProducts"
        />
        <el-button type="primary" @click="loadProducts">搜索</el-button>
      </div>
    </div>

    <div class="catalog-layout">
      <aside class="category-panel">
        <el-button
          class="category-button"
          :type="activeCategoryId === null ? 'primary' : 'default'"
          @click="selectCategory(null)"
        >
          全部商品
        </el-button>
        <el-button
          v-for="category in categories"
          :key="category.id"
          class="category-button"
          :type="activeCategoryId === category.id ? 'primary' : 'default'"
          @click="selectCategory(category.id)"
        >
          <span>{{ category.name }}</span>
          <small v-if="Number(category.productCount || 0) === 0">暂无商品</small>
        </el-button>
      </aside>

      <main class="product-area">
        <el-skeleton v-if="loading" :rows="8" animated />
        <el-empty v-else-if="products.length === 0" :description="emptyDescription">
          <el-button v-if="hasFilters" type="primary" @click="resetFilters">查看全部商品</el-button>
        </el-empty>
        <template v-else>
          <div class="product-grid">
            <ProductCard v-for="product in pagedProducts" :key="product.id" :product="product" />
          </div>
          <el-pagination
            v-if="products.length > pageSize"
            v-model:current-page="currentPage"
            class="product-pagination"
            background
            layout="total, prev, pager, next, jumper"
            :page-size="pageSize"
            :total="products.length"
          />
        </template>
      </main>
    </div>
  </section>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import ProductCard from '../../components/ProductCard.vue'
import { categoryApi, productApi } from '../../api'

const categories = ref([])
const products = ref([])
const keyword = ref('')
const activeCategoryId = ref(null)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = 8
const hasFilters = computed(() => Boolean(keyword.value.trim() || activeCategoryId.value !== null))
const activeCategory = computed(() => categories.value.find((category) => category.id === activeCategoryId.value))
const emptyDescription = computed(() => {
  if (activeCategoryId.value !== null && !keyword.value.trim()) {
    return `分类「${activeCategory.value?.name || '当前分类'}」暂无商品`
  }
  return hasFilters.value ? '没有找到符合条件的商品' : '暂无商品数据'
})
const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return products.value.slice(start, start + pageSize)
})

// 分类列表用于左侧筛选栏，加载失败时商品仍可按“全部商品”展示。
const loadCategories = async () => {
  try {
    const result = await categoryApi.list()
    categories.value = result.data || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '分类加载失败')
  }
}

// 按当前分类和关键字查询商品列表。
const loadProducts = async () => {
  loading.value = true
  try {
    const result = await productApi.list({
      categoryId: activeCategoryId.value,
      keyword: keyword.value.trim()
    })
    products.value = result.data || []
    currentPage.value = 1
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '商品加载失败')
  } finally {
    loading.value = false
  }
}

// 切换分类后立即重新查询商品。
const selectCategory = (categoryId) => {
  activeCategoryId.value = categoryId
  currentPage.value = 1
  loadProducts()
}

// 清空分类和关键字筛选，帮助用户从空结果状态回到全量列表。
const resetFilters = () => {
  keyword.value = ''
  activeCategoryId.value = null
  currentPage.value = 1
  loadProducts()
}

onMounted(async () => {
  await loadCategories()
  await loadProducts()
})
</script>

<style scoped>
h1 {
  margin: 0;
}

.search-row {
  display: grid;
  width: min(420px, 100%);
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.catalog-layout {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 20px;
  align-items: start;
}

.category-panel {
  display: grid;
  gap: 10px;
  position: sticky;
  top: 16px;
}

.category-button {
  width: 100%;
  margin-left: 0;
}

.category-button :deep(> span) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
}

.category-button small {
  color: #909399;
  font-size: 12px;
}

.product-area {
  min-width: 0;
}

/* 商品分类页固定每页 8 个商品：桌面端两行四列，分页切换时布局不跳动。 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.product-pagination {
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 760px) {
  .catalog-layout {
    grid-template-columns: 1fr;
  }

  .category-panel {
    position: static;
    grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
