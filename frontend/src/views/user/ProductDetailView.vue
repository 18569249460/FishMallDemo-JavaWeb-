<template>
  <section class="page">
    <el-page-header content="商品详情" @back="$router.back()" />

    <el-skeleton v-if="loading" :rows="8" animated class="detail-loading" />
    <el-empty v-else-if="!product" description="商品不存在或已下架">
      <el-button type="primary" @click="router.push('/products')">返回商品列表</el-button>
    </el-empty>
    <div v-else class="detail-layout">
      <div class="detail-image">
        <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" />
        <span v-else>{{ product.categoryName || '水产' }}</span>
      </div>

      <div class="detail-info">
        <el-tag class="category-tag">{{ product.categoryName || '未分类' }}</el-tag>
        <h1>{{ product.name }}</h1>
        <p class="description">{{ product.description || '暂无商品描述' }}</p>

        <dl class="info-list">
          <div>
            <dt>价格</dt>
            <dd class="price">￥{{ formatPrice(product.price) }}</dd>
          </div>
          <div>
            <dt>库存</dt>
            <dd>{{ product.stock > 0 ? `${product.stock} 件` : '已售罄' }}</dd>
          </div>
          <div>
            <dt>规格</dt>
            <dd>{{ product.specification || '以门店实际称重为准' }}</dd>
          </div>
          <div>
            <dt>销量</dt>
            <dd>{{ product.sales || 0 }}</dd>
          </div>
          <div>
            <dt>产地</dt>
            <dd>{{ product.origin || '门店精选产地' }}</dd>
          </div>
          <div>
            <dt>保质期</dt>
            <dd>{{ product.shelfLife || '以商品实际标签为准' }}</dd>
          </div>
          <div>
            <dt>储存</dt>
            <dd>{{ product.storageMethod || '建议冷藏或冷冻保存' }}</dd>
          </div>
          <div>
            <dt>配送</dt>
            <dd>{{ product.deliveryInfo || '同城冷链配送，具体以门店安排为准' }}</dd>
          </div>
        </dl>

        <el-alert
          v-if="product.stock <= 0"
          title="商品已售罄，暂不可加入购物车"
          type="info"
          :closable="false"
          show-icon
        />
        <div v-else class="cart-actions">
          <el-input-number v-model="quantity" :min="1" :max="product.stock" />
          <el-button type="primary" size="large" :loading="adding" @click="addToCart">
            加入购物车
          </el-button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { cartApi, productApi } from '../../api'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref(null)
const quantity = ref(1)
const loading = ref(false)
const adding = ref(false)

// 详情页通过路由参数中的商品 ID 请求后端。
const loadProduct = async () => {
  loading.value = true
  try {
    const result = await productApi.detail(route.params.id)
    product.value = result.data
    quantity.value = product.value?.stock > 0 ? 1 : 0
  } catch (error) {
    product.value = null
    ElMessage.error(error?.response?.data?.message || '商品详情加载失败')
  } finally {
    loading.value = false
  }
}

// 加入购物车前先检查登录态；未登录时跳转登录页并记录回跳地址。
const addToCart = async () => {
  if (!userStore.token) {
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }
  adding.value = true
  try {
    await cartApi.add({
      productId: product.value.id,
      quantity: quantity.value
    })
    // 加购成功只在当前商品详情页提示，不打断用户继续浏览当前商品。
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '加入购物车失败')
  } finally {
    adding.value = false
  }
}

// 价格统一保留两位小数。
const formatPrice = (price) => Number(price || 0).toFixed(2)

onMounted(loadProduct)
</script>

<style scoped>
.detail-loading,
.detail-layout {
  margin-top: 20px;
}

.detail-layout {
  display: grid;
  grid-template-columns: 520px minmax(0, 1fr);
  gap: 32px;
  align-items: start;
}

/* 商品主图使用固定正方形比例，和当前商品图片素材比例保持一致。 */
.detail-image {
  display: grid;
  overflow: hidden;
  width: 520px;
  aspect-ratio: 1 / 1;
  place-items: center;
  border-radius: 8px;
  background: #e9f5f1;
  color: #0f766e;
  font-weight: 700;
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.category-tag {
  width: 100px;
  justify-content: center;
}

.detail-info {
  display: grid;
  gap: 16px;
}

h1 {
  margin: 0;
  font-size: 32px;
}

.description {
  margin: 0;
  color: #52616d;
}

.info-list {
  display: grid;
  gap: 14px;
  margin: 0;
}

.info-list div {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 12px;
  align-items: center;
}

dt {
  color: #697782;
}

dd {
  margin: 0;
}

.price {
  color: #d97706;
  font-size: 28px;
  font-weight: 700;
}

.cart-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

@media (max-width: 820px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-image {
    width: 100%;
    aspect-ratio: 1 / 1;
  }
}
</style>
