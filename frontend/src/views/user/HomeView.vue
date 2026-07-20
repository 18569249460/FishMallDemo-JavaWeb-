<template>
  <section class="page">
    <el-carousel
      v-if="heroProducts.length > 0"
      class="home-hero"
      height="280px"
      indicator-position="outside"
      arrow="always"
    >
      <el-carousel-item v-for="product in heroProducts" :key="product.id">
        <button class="hero-slide" type="button" @click="goDetail(product.id)">
          <img :src="product.imageUrl" :alt="product.name" />
          <span class="hero-mask"></span>
          <span class="hero-copy">
            <span class="eyebrow">销量精选 · {{ product.categoryName || '鲜活水产' }}</span>
            <strong>鲜渔小店</strong>
            <span class="summary">{{ product.name }}，销量 {{ product.sales || 0 }}，今日库存实时展示。</span>
          </span>
          <span class="hero-action">查看商品</span>
        </button>
      </el-carousel-item>
    </el-carousel>

    <div v-else class="home-hero hero-fallback">
      <div class="hero-copy">
        <p class="eyebrow">当日鲜选</p>
        <h1>鲜渔小店</h1>
        <p class="summary">鱼虾贝蟹一站选购，库存状态实时展示。</p>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/products')">查看商品</el-button>
    </div>

    <div class="toolbar">
      <h2>热门水产</h2>
      <el-button text type="primary" @click="$router.push('/products')">全部商品</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="products.length === 0" description="暂无热门商品">
      <el-button type="primary" @click="$router.push('/products')">查看全部商品</el-button>
    </el-empty>
    <div v-else class="hot-grid">
      <ProductCard v-for="product in hotProducts" :key="product.id" :product="product" />
    </div>
  </section>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ProductCard from '../../components/ProductCard.vue'
import { productApi } from '../../api'

const router = useRouter()
const products = ref([])
const loading = ref(false)

// 轮播图使用销量最高且有图片的前三个商品，保证图片来自数据库商品数据。
const heroProducts = computed(() => products.value.filter((product) => product.imageUrl).slice(0, 3))

// 热门水产固定展示前 8 个商品，桌面端正好一行四个、共两行。
const hotProducts = computed(() => products.value.slice(0, 8))

// 首页加载销量排序靠前的商品，后端默认限制 8 条。
const loadHotProducts = async () => {
  loading.value = true
  try {
    const result = await productApi.hot()
    products.value = result.data || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '热门商品加载失败')
  } finally {
    loading.value = false
  }
}

// 点击轮播图进入对应商品详情页。
const goDetail = (id) => {
  router.push(`/products/${id}`)
}

onMounted(loadHotProducts)
</script>

<style scoped>
.home-hero {
  margin-bottom: 40px;
  border-radius: 8px;
}

.home-hero :deep(.el-carousel__container) {
  overflow: hidden;
  border-radius: 8px;
}

.hero-slide {
  position: relative;
  display: block;
  overflow: hidden;
  width: 100%;
  height: 100%;
  padding: 0;
  border: 0;
  color: #ffffff;
  text-align: left;
  cursor: pointer;
}

.hero-slide img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(7, 89, 77, 0.9), rgba(15, 118, 110, 0.35));
}

.hero-copy {
  position: absolute;
  top: 50%;
  left: 32px;
  z-index: 1;
  display: grid;
  max-width: min(560px, calc(100% - 240px));
  transform: translateY(-50%);
  gap: 8px;
}

.eyebrow {
  font-weight: 700;
}

.hero-copy strong {
  margin: 0;
  font-size: 44px;
  font-weight: 700;
  line-height: 1.1;
}

h2 {
  margin: 0;
}

.summary {
  max-width: 460px;
  color: rgba(255, 255, 255, 0.88);
}

.hero-action {
  position: absolute;
  right: 36px;
  top: 50%;
  z-index: 1;
  padding: 11px 18px;
  transform: translateY(-50%);
  border-radius: 6px;
  background: #409eff;
  color: #ffffff;
}

.hero-fallback {
  display: flex;
  min-height: 220px;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 32px;
  background:
    linear-gradient(90deg, rgba(7, 89, 77, 0.88), rgba(15, 118, 110, 0.52)),
    url("https://images.unsplash.com/photo-1534766555764-ce878a5e3a2b?auto=format&fit=crop&w=1600&q=80") center / cover;
  color: #ffffff;
}

.hero-fallback .hero-copy {
  position: static;
  max-width: none;
  transform: none;
}

.hero-fallback h1,
.eyebrow,
.summary {
  margin: 0;
}

.hero-fallback h1 {
  font-size: 44px;
  line-height: 1.1;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 720px) {
  .home-hero :deep(.el-carousel__container) {
    height: 240px !important;
  }

  .hero-copy {
    left: 22px;
    max-width: calc(100% - 44px);
  }

  .hero-copy strong {
    font-size: 34px;
  }

  .hero-action {
    display: none;
  }

  .hero-fallback {
    align-items: flex-start;
    flex-direction: column;
    padding: 24px;
  }

  .hero-fallback h1 {
    font-size: 34px;
  }

  .hot-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 460px) {
  .hot-grid {
    grid-template-columns: 1fr;
  }
}
</style>
