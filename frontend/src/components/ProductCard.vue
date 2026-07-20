<template>
  <!-- 商品卡片：用于首页热门商品和商品列表，点击图片或标题进入详情页。 -->
  <article class="product-card">
    <button class="image-button" type="button" @click="goDetail">
      <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.name" />
      <span v-else class="image-fallback">{{ product.categoryName || '水产' }}</span>
      <el-tag class="stock-tag" :type="product.stock > 0 ? 'success' : 'info'" effect="dark">
        {{ product.stock > 0 ? '有货' : '已售罄' }}
      </el-tag>
    </button>
    <div class="product-body">
      <button class="title-button" type="button" @click="goDetail">{{ product.name }}</button>
      <p class="spec">{{ product.specification || product.categoryName || '新鲜水产' }}</p>
      <div class="meta-row">
        <span class="price">￥{{ formatPrice(product.price) }}</span>
        <span class="sales">销量 {{ product.sales || 0 }}</span>
      </div>
      <el-button type="primary" plain class="detail-button" @click="goDetail">查看详情</el-button>
    </div>
  </article>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()

// 价格统一保留两位小数，避免不同页面格式不一致。
const formatPrice = (price) => Number(price || 0).toFixed(2)

// 商品详情路径与后端商品 ID 对齐。
const goDetail = () => {
  router.push(`/products/${props.product.id}`)
}
</script>

<style scoped>
.product-card {
  overflow: hidden;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
  border-color: #67c23a;
  box-shadow: 0 8px 24px rgba(31, 42, 53, 0.08);
}

.image-button {
  position: relative;
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  padding: 0;
  border: 0;
  background: #e9f5f1;
  cursor: pointer;
}

.image-button img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-fallback {
  display: grid;
  width: 100%;
  height: 100%;
  place-items: center;
  color: #0f766e;
  font-weight: 700;
}

.stock-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.product-body {
  display: grid;
  gap: 8px;
  padding: 14px;
}

.title-button {
  overflow: hidden;
  padding: 0;
  border: 0;
  background: transparent;
  color: #1f2a35;
  font-size: 16px;
  font-weight: 700;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.spec {
  min-height: 21px;
  margin: 0;
  color: #697782;
  font-size: 14px;
}

.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.price {
  color: #d97706;
  font-size: 20px;
  font-weight: 700;
}

.sales {
  color: #697782;
  font-size: 13px;
}

.detail-button {
  width: 100%;
}
</style>
