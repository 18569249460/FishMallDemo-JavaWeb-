<template>
  <section class="page">
    <div class="toolbar">
      <h1>购物车</h1>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />
    <el-empty v-else-if="items.length === 0" description="购物车为空">
      <el-button type="primary" @click="router.push('/products')">去选商品</el-button>
    </el-empty>
    <div v-else class="cart-panel">
      <el-table :data="items" @selection-change="selectedRows = $event">
        <el-table-column type="selection" width="48" :selectable="canSelect" />
        <el-table-column label="商品">
          <template #default="{ row }">
            <div class="product-cell">
              <img v-if="row.productImageUrl" :src="row.productImageUrl" :alt="row.productName" />
              <div class="product-info">
                <strong>{{ row.productName }}</strong>
                <span v-if="row.productStatus !== 1" class="danger">商品已下架</span>
                <span v-else :class="{ danger: row.quantity > row.productStock }">库存 {{ row.productStock }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">￥{{ formatPrice(row.productPrice) }}</template>
        </el-table-column>
        <el-table-column label="数量" width="180">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="Math.max(row.productStock, 1)"
              size="small"
              @change="updateQuantity(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">￥{{ formatPrice(subtotal(row)) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-footer">
        <span>已选 {{ selectedRows.length }} 件</span>
        <strong>合计：￥{{ formatPrice(selectedTotal) }}</strong>
      </div>
      <div class="cart-action-row">
        <el-button :disabled="invalidItems.length === 0" @click="clearInvalidItems">清理失效商品</el-button>
        <el-button :disabled="selectedRows.length === 0" @click="batchRemove">批量删除</el-button>
        <el-button type="primary" :disabled="selectedRows.length === 0" @click="checkout">结算</el-button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { cartApi } from '../../api'

const router = useRouter()
const items = ref([])
const selectedRows = ref([])
const loading = ref(false)

// 加载当前登录用户购物车。
const loadCart = async () => {
  loading.value = true
  try {
    const result = await cartApi.list()
    items.value = result.data || []
    selectedRows.value = []
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '购物车加载失败')
  } finally {
    loading.value = false
  }
}

const invalidItems = computed(() => items.value.filter((item) => !canSelect(item)))

// 库存不足、数量异常或已下架的购物车项不能勾选结算。
const canSelect = (row) => row.productStatus === 1 && row.productStock > 0 && row.quantity <= row.productStock

// 修改数量后立即同步后端，后端会再次校验库存。
const updateQuantity = async (row) => {
  try {
    const result = await cartApi.update(row.id, { quantity: row.quantity })
    Object.assign(row, result.data)
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '数量修改失败')
    await loadCart()
  }
}

// 批量删除使用后端批量接口，减少用户逐条删除的操作成本。
const batchRemove = async () => {
  try {
    await ElMessageBox.confirm(`确定删除已选的 ${selectedRows.value.length} 件商品吗？`, '批量删除', {
      type: 'warning'
    })
    await cartApi.batchRemove(selectedRows.value.map((row) => row.id))
    ElMessage.success('已批量删除')
    await loadCart()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '批量删除失败')
    }
  }
}

// 一键清理库存不足或已下架商品，帮助用户快速恢复可结算购物车。
const clearInvalidItems = async () => {
  try {
    await ElMessageBox.confirm(`确定清理 ${invalidItems.value.length} 件失效商品吗？`, '清理失效商品', {
      type: 'warning'
    })
    await cartApi.batchRemove(invalidItems.value.map((row) => row.id))
    ElMessage.success('失效商品已清理')
    await loadCart()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '清理失败')
    }
  }
}

// 删除购物车项前做一次确认，避免误删。
const removeItem = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.productName} 吗？`, '删除购物车项', {
      type: 'warning'
    })
    await cartApi.remove(row.id)
    ElMessage.success('已删除')
    await loadCart()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

// 保存本次结算的购物车项 ID，订单确认页会基于这些 ID 再次向后端提交。
const checkout = () => {
  const invalid = selectedRows.value.find((row) => !canSelect(row))
  if (invalid) {
    ElMessage.warning('存在库存不足商品，不能结算')
    return
  }
  sessionStorage.setItem('fishMallCheckoutCartIds', JSON.stringify(selectedRows.value.map((row) => row.id)))
  router.push('/order/confirm')
}

const subtotal = (row) => Number(row.productPrice || 0) * Number(row.quantity || 0)
const selectedTotal = computed(() => selectedRows.value.reduce((sum, row) => sum + subtotal(row), 0))
const formatPrice = (price) => Number(price || 0).toFixed(2)

onMounted(loadCart)
</script>

<style scoped>
.cart-panel {
  display: grid;
  gap: 16px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-cell img {
  width: 64px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
}

.product-info {
  display: grid;
  gap: 4px;
}

.product-info span {
  color: #697782;
  font-size: 13px;
}

.product-info .danger {
  color: #f56c6c;
}

.cart-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 24px;
  padding: 16px 0 4px;
}

/* 操作按钮放在合计金额下方，结算前先让用户看到本次选择的金额。 */
.cart-action-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  padding-bottom: 16px;
}
</style>
