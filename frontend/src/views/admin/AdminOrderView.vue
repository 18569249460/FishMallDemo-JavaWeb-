<template>
  <section class="page admin-page">
    <div class="page-heading">
      <div>
        <h1>订单管理</h1>
        <p>查看订单状态、商品件数和总金额，并处理待发货订单。</p>
      </div>
    </div>

    <section class="admin-table-card">
      <div class="admin-filter-bar">
        <div class="admin-filter-group">
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 180px" @change="loadOrders">
            <el-option label="待发货" :value="0" />
            <el-option label="待收货" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
          <el-input
            v-model="keyword"
            placeholder="搜索订单号或用户昵称"
            clearable
            @clear="applySearch"
            @keyup.enter="applySearch"
          />
          <el-button type="primary" @click="applySearch">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
        <el-button text type="primary" @click="loadOrders">刷新</el-button>
      </div>

      <el-table v-loading="loading" :data="pagedOrders">
        <el-table-column label="订单号" min-width="190">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">{{ row.orderNo }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="userNickname" label="用户" width="140" />
        <el-table-column label="商品件数" width="110">
          <template #default="{ row }">{{ productCount(row) }}</template>
        </el-table-column>
        <el-table-column label="总金额" width="120">
          <template #default="{ row }">￥{{ formatPrice(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="下单时间" width="190">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" link type="success" @click="openShipDialog(row)">发货</el-button>
            <span v-else class="muted-action">暂无操作</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="hasFilters ? '无符合条件的订单' : '暂无订单数据'">
            <el-button type="primary" @click="loadOrders">刷新</el-button>
          </el-empty>
        </template>
      </el-table>

      <el-pagination
        v-if="filteredOrders.length > pageSize"
        v-model:current-page="currentPage"
        class="table-pagination"
        background
        layout="total, prev, pager, next, jumper"
        :page-size="pageSize"
        :total="filteredOrders.length"
      />
    </section>

    <el-dialog v-model="detailVisible" title="订单详情" width="640px">
      <div v-if="currentOrder" class="detail-panel">
        <p><strong>订单号：</strong>{{ currentOrder.orderNo }}</p>
        <p><strong>下单时间：</strong>{{ formatTime(currentOrder.createdAt) }}</p>
        <p><strong>用户：</strong>{{ currentOrder.userNickname || '未知用户' }}</p>
        <p><strong>收货人：</strong>{{ currentOrder.receiverName }}，{{ currentOrder.receiverPhone }}</p>
        <p><strong>地址：</strong>{{ currentOrder.addressDetail }}</p>
        <p v-if="currentOrder.trackingNo"><strong>物流单号：</strong>{{ currentOrder.trackingNo }}</p>
        <el-table :data="currentOrder.items || []">
          <el-table-column prop="productName" label="商品" />
          <el-table-column label="单价" width="110">
            <template #default="{ row }">￥{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="110">
            <template #default="{ row }">￥{{ formatPrice(row.subtotal) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="shipDialogVisible" title="订单发货" width="420px">
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="物流单号">
          <el-input v-model="shipForm.trackingNo" maxlength="100" placeholder="请输入物流单号，可留空" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipping" @click="ship">确认发货</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '../../api'
import { formatDateTime } from '../../utils/format'

const orders = ref([])
const statusFilter = ref(null)
const keyword = ref('')
const appliedKeyword = ref('')
const loading = ref(false)
const detailVisible = ref(false)
const currentOrder = ref(null)
const currentPage = ref(1)
const pageSize = 10
const shipDialogVisible = ref(false)
const shipping = ref(false)
const shippingOrder = ref(null)
const shipForm = ref({ trackingNo: '' })

const hasFilters = computed(() => Boolean(appliedKeyword.value || (statusFilter.value !== null && statusFilter.value !== '')))
const filteredOrders = computed(() => {
  const key = appliedKeyword.value
  if (!key) {
    return orders.value
  }
  return orders.value.filter((order) => {
    const orderNo = String(order.orderNo || '')
    const nickname = String(order.userNickname || '')
    return orderNo.includes(key) || nickname.includes(key)
  })
})
const pagedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredOrders.value.slice(start, start + pageSize)
})

// 管理端订单列表，支持按状态筛选。
const loadOrders = async () => {
  loading.value = true
  try {
    const params = statusFilter.value === null || statusFilter.value === '' ? {} : { status: statusFilter.value }
    const result = await adminApi.orders(params)
    orders.value = result.data || []
    currentPage.value = 1
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '订单加载失败')
  } finally {
    loading.value = false
  }
}

// 打开订单详情，列表已有 items；后续如果要减少列表体积，也可以改为点击时请求详情接口。
const openDetail = (order) => {
  currentOrder.value = order
  detailVisible.value = true
}

const openShipDialog = (order) => {
  shippingOrder.value = order
  shipForm.value = { trackingNo: order.trackingNo || '' }
  shipDialogVisible.value = true
}

// 管理员发货，后端只允许“待发货 -> 待收货”，并保存物流单号。
const ship = async () => {
  const order = shippingOrder.value
  if (!order) {
    return
  }
  try {
    await ElMessageBox.confirm(`确认订单 ${order.orderNo} 已发货吗？`, '订单发货', {
      type: 'warning'
    })
    shipping.value = true
    await adminApi.shipOrder(order.id, { trackingNo: shipForm.value.trackingNo.trim() || null })
    ElMessage.success('已发货')
    shipDialogVisible.value = false
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '发货失败')
    }
  } finally {
    shipping.value = false
  }
}

const applySearch = () => {
  appliedKeyword.value = keyword.value.trim()
  currentPage.value = 1
}

const resetFilters = () => {
  statusFilter.value = null
  keyword.value = ''
  appliedKeyword.value = ''
  loadOrders()
}

const productCount = (order) => (order.items || []).reduce((sum, item) => sum + Number(item.quantity || 0), 0)
const statusText = (status) => ({ 0: '待发货', 1: '待收货', 2: '已完成', 3: '已取消' }[status] || '未知')
const statusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }[status] || 'info')
const formatPrice = (price) => Number(price || 0).toFixed(2)
// 下单时间统一格式化，避免数组时间直接被字符串化。
const formatTime = formatDateTime

onMounted(loadOrders)
</script>

<style scoped>
.detail-panel {
  display: grid;
  gap: 10px;
}

.detail-panel p {
  margin: 0;
}

.muted-action {
  color: #909399;
  font-size: 13px;
}

.table-pagination {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
