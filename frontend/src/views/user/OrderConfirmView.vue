<template>
  <section class="page">
    <div class="toolbar">
      <h1>订单确认</h1>
      <el-button type="primary" :disabled="checkoutItems.length === 0" :loading="submitting" @click="submitOrder">
        提交订单
      </el-button>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />
    <el-empty v-else-if="checkoutItems.length === 0" description="暂无待结算商品">
      <el-button type="primary" @click="router.push('/cart')">返回购物车</el-button>
    </el-empty>
    <div v-else class="confirm-layout">
      <section class="confirm-section">
        <h2>商品清单</h2>
        <el-table :data="checkoutItems">
          <el-table-column prop="productName" label="商品" />
          <el-table-column label="单价" width="120">
            <template #default="{ row }">￥{{ formatPrice(row.productPrice) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">￥{{ formatPrice(subtotal(row)) }}</template>
          </el-table-column>
        </el-table>
        <div class="total-row">
          <strong>订单总额：￥{{ formatPrice(totalAmount) }}</strong>
        </div>
      </section>

      <section class="confirm-section">
        <h2>收货信息</h2>
        <div v-if="addresses.length > 0" class="address-list">
          <button
            v-for="address in addresses"
            :key="address.id"
            class="address-card"
            :class="{ active: selectedAddressId === address.id }"
            type="button"
            @click="useAddress(address)"
          >
            <strong>{{ address.receiverName }}，{{ address.receiverPhone }}</strong>
            <span>{{ address.addressDetail }}</span>
            <em v-if="address.isDefault === 1">默认</em>
          </button>
        </div>
        <el-form
          ref="receiverFormRef"
          label-position="top"
          :model="receiver"
          :rules="receiverRules"
          @submit.prevent="submitOrder"
        >
          <el-form-item label="收货人姓名" prop="receiverName">
            <el-input v-model="receiver.receiverName" maxlength="50" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="联系电话" prop="receiverPhone">
            <el-input v-model="receiver.receiverPhone" maxlength="11" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="详细收货地址" prop="addressDetail">
            <el-input
              v-model="receiver.addressDetail"
              type="textarea"
              maxlength="255"
              placeholder="请输入详细收货地址"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </section>
    </div>
  </section>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addressApi, cartApi, orderApi } from '../../api'

const router = useRouter()
const checkoutItems = ref([])
const loading = ref(false)
const submitting = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)
const receiverFormRef = ref()
const receiver = reactive({
  receiverName: '',
  receiverPhone: '',
  addressDetail: ''
})
const phonePattern = /^1[3-9]\d{9}$/

// 收货信息表单规则全部使用中文文案，避免默认英文校验提示影响体验。
const receiverRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { max: 50, message: '收货人姓名不能超过 50 个字符', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const phone = String(value || '').trim()
        if (!phone) {
          callback()
          return
        }
        if (!phonePattern.test(phone)) {
          callback(new Error('请输入正确的手机格式'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  addressDetail: [
    { required: true, message: '请输入详细收货地址', trigger: 'blur' },
    { max: 255, message: '详细收货地址不能超过 255 个字符', trigger: 'blur' }
  ]
}

// 订单确认页只展示购物车页勾选的商品；实际提交时后端仍会重新校验归属、库存和价格。
const loadCheckoutItems = async () => {
  const ids = readCheckoutIds()
  if (ids.length === 0) {
    checkoutItems.value = []
    return
  }
  loading.value = true
  try {
    const result = await cartApi.list()
    const allItems = result.data || []
    checkoutItems.value = allItems.filter((item) => ids.includes(item.id))
    if (checkoutItems.value.length !== ids.length) {
      ElMessage.warning('部分购物车商品已失效，请重新选择')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '结算商品加载失败')
  } finally {
    loading.value = false
  }
}

// 加载常用地址，并自动带入默认地址，减少下单时重复填写。
const loadAddresses = async () => {
  try {
    const result = await addressApi.list()
    addresses.value = result.data || []
    const defaultAddress = addresses.value.find((item) => item.isDefault === 1) || addresses.value[0]
    if (defaultAddress) {
      useAddress(defaultAddress)
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '常用地址加载失败')
  }
}

const useAddress = (address) => {
  selectedAddressId.value = address.id
  receiver.receiverName = address.receiverName
  receiver.receiverPhone = address.receiverPhone
  receiver.addressDetail = address.addressDetail
  receiverFormRef.value?.clearValidate()
}

// 提交订单前做轻量前端校验；后端 DTO 和事务逻辑仍会做最终校验。
const submitOrder = async () => {
  if (checkoutItems.value.length === 0) {
    ElMessage.warning('暂无可结算商品')
    return
  }
  try {
    await receiverFormRef.value?.validate()
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    await orderApi.create({
      cartItemIds: checkoutItems.value.map((item) => item.id),
      receiver: {
        receiverName: receiver.receiverName.trim(),
        receiverPhone: receiver.receiverPhone.trim(),
        addressDetail: receiver.addressDetail.trim()
      }
    })
    sessionStorage.removeItem('fishMallCheckoutCartIds')
    ElMessage.success('订单提交成功')
    router.push('/profile')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '订单提交失败')
  } finally {
    submitting.value = false
  }
}

const readCheckoutIds = () => {
  try {
    return JSON.parse(sessionStorage.getItem('fishMallCheckoutCartIds') || '[]')
  } catch (error) {
    return []
  }
}

const subtotal = (row) => Number(row.productPrice || 0) * Number(row.quantity || 0)
const totalAmount = computed(() => checkoutItems.value.reduce((sum, row) => sum + subtotal(row), 0))
const formatPrice = (price) => Number(price || 0).toFixed(2)

onMounted(() => {
  loadCheckoutItems()
  loadAddresses()
})
</script>

<style scoped>
.confirm-layout {
  display: grid;
  gap: 20px;
}

.confirm-section {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
}

h2 {
  margin: 0 0 16px;
  font-size: 18px;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.address-card {
  position: relative;
  display: grid;
  gap: 4px;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #ffffff;
  color: #1f2a35;
  text-align: left;
  cursor: pointer;
}

.address-card.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.address-card span {
  color: #697782;
  font-size: 13px;
}

.address-card em {
  position: absolute;
  right: 10px;
  top: 10px;
  color: #409eff;
  font-size: 12px;
  font-style: normal;
}

.total-row {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  color: #d97706;
  font-size: 18px;
}
</style>
