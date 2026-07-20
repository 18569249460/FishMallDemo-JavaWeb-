<template>
  <section class="page">
    <el-page-header content="订单详情" @back="router.back()" />

    <el-skeleton v-if="loading" :rows="8" animated class="detail-loading" />
    <el-empty v-else-if="!order" description="订单不存在">
      <el-button type="primary" @click="router.push('/profile')">返回个人中心</el-button>
    </el-empty>

    <div v-else class="order-detail">
      <section class="detail-card">
        <div class="detail-head">
          <div>
            <h1>{{ order.orderNo }}</h1>
            <p>{{ formatTime(order.createdAt) }}</p>
          </div>
          <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
        </div>
        <el-steps :active="stepActive(order.status)" finish-status="success" simple>
          <el-step title="待发货" />
          <el-step title="待收货" />
          <el-step title="已完成" />
        </el-steps>
      </section>

      <section class="detail-card">
        <h2>收货信息</h2>
        <p>收货人：{{ order.receiverName }}，{{ order.receiverPhone }}</p>
        <p>收货地址：{{ order.addressDetail }}</p>
        <p v-if="order.trackingNo">物流单号：{{ order.trackingNo }}</p>
        <p v-else class="muted">暂无物流信息</p>
      </section>

      <section class="detail-card">
        <h2>商品明细</h2>
        <el-table :data="order.items || []">
          <el-table-column prop="productName" label="商品" />
          <el-table-column label="单价" width="120">
            <template #default="{ row }">￥{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">￥{{ formatPrice(row.subtotal) }}</template>
          </el-table-column>
        </el-table>
        <div class="total-row">
          <strong>订单总额：￥{{ formatPrice(order.totalAmount) }}</strong>
        </div>
      </section>

      <div class="action-row">
        <el-button v-if="order.status === 0" type="danger" @click="cancelOrder">取消订单</el-button>
        <el-button v-if="order.status === 1" type="primary" @click="confirmReceipt">确认收货</el-button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '../../api'
import { formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(false)

// 订单详情页单独请求后端，避免个人中心列表承担过多展示逻辑。
const loadOrder = async () => {
  loading.value = true
  try {
    const result = await orderApi.detail(route.params.id)
    order.value = result.data
  } catch (error) {
    order.value = null
    ElMessage.error(error?.response?.data?.message || '订单详情加载失败')
  } finally {
    loading.value = false
  }
}

// 取消订单只允许待发货订单，后端会回滚库存和销量。
const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm(`确定取消订单 ${order.value.orderNo} 吗？`, '取消订单', { type: 'warning' })
    await orderApi.cancel(order.value.id)
    ElMessage.success('订单已取消')
    await loadOrder()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '取消订单失败')
    }
  }
}

const confirmReceipt = async () => {
  try {
    await ElMessageBox.confirm(`确认已收到订单 ${order.value.orderNo} 吗？`, '确认收货', { type: 'warning' })
    await orderApi.confirm(order.value.id)
    ElMessage.success('已确认收货')
    await loadOrder()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '确认收货失败')
    }
  }
}

const statusText = (status) => ({ 0: '待发货', 1: '待收货', 2: '已完成', 3: '已取消' }[status] || '未知')
const statusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }[status] || 'info')
const stepActive = (status) => {
  if (status === 3) {
    return 0
  }
  return Math.min(Number(status || 0) + 1, 3)
}
const formatPrice = (price) => Number(price || 0).toFixed(2)
// 订单时间由公共工具统一格式化，避免数组时间显示成逗号分隔。
const formatTime = formatDateTime

onMounted(loadOrder)
</script>

<style scoped>
.detail-loading,
.order-detail {
  margin-top: 20px;
}

.order-detail {
  display: grid;
  gap: 16px;
}

.detail-card {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
}

.detail-head,
.total-row,
.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.detail-head {
  margin-bottom: 16px;
}

h1,
h2,
p {
  margin: 0;
}

h2 {
  margin-bottom: 12px;
  font-size: 18px;
}

p {
  color: #52616d;
}

.muted {
  color: #909399;
}

.total-row {
  justify-content: flex-end;
  padding-top: 16px;
  color: #d97706;
  font-size: 18px;
}

.action-row {
  justify-content: flex-end;
}
</style>
