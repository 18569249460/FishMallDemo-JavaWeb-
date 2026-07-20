<template>
  <section class="page">
    <div class="toolbar">
      <h1>个人中心</h1>
      <el-button @click="logout">退出登录</el-button>
    </div>

    <div v-if="userStore.user" class="profile-summary">
      <el-avatar :size="64" :src="userStore.user.avatar || ''">
        {{ userStore.user.nickname?.slice(0, 1) || '用' }}
      </el-avatar>
      <div>
        <h2>{{ userStore.user.nickname }}</h2>
        <p>{{ userStore.user.maskedPhone || userStore.user.phone }}</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="profile-tabs">
      <el-tab-pane label="个人资料" name="profile">
        <section class="profile-card">
          <el-form ref="profileFormRef" label-position="top" :model="profileForm" :rules="profileRules">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" maxlength="50" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="头像" prop="avatar">
              <div class="avatar-upload-row">
                <el-avatar :size="72" :src="profileForm.avatar || ''">
                  {{ profileForm.nickname?.slice(0, 1) || '用' }}
                </el-avatar>
                <div>
                  <el-upload
                    :auto-upload="false"
                    :show-file-list="false"
                    :disabled="uploadingAvatar"
                    accept="image/*"
                    :on-change="uploadAvatar"
                  >
                    <el-button :loading="uploadingAvatar">上传头像</el-button>
                  </el-upload>
                  <p class="form-tip">图片会保存到后端本地目录，系统只把图片访问地址写入数据库。</p>
                </div>
              </div>
            </el-form-item>
            <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
          </el-form>
        </section>
      </el-tab-pane>

      <el-tab-pane label="常用地址" name="addresses">
        <section class="profile-card">
          <div class="toolbar compact-toolbar">
            <h2>常用收货地址</h2>
            <el-button type="primary" @click="openAddressDialog()">新增地址</el-button>
          </div>
          <el-empty v-if="addresses.length === 0" description="暂无常用地址">
            <el-button type="primary" @click="openAddressDialog()">新增地址</el-button>
          </el-empty>
          <template v-else>
            <div class="address-grid">
              <article v-for="address in pagedAddresses" :key="address.id" class="address-item">
                <div>
                  <strong>{{ address.receiverName }}，{{ address.receiverPhone }}</strong>
                  <p>{{ address.addressDetail }}</p>
                </div>
                <el-tag v-if="address.isDefault === 1" type="success">默认</el-tag>
                <div class="address-actions">
                  <el-button v-if="address.isDefault !== 1" link type="primary" @click="setDefaultAddress(address)">设为默认</el-button>
                  <el-button link type="primary" @click="openAddressDialog(address)">编辑</el-button>
                  <el-button link type="danger" @click="removeAddress(address)">删除</el-button>
                </div>
              </article>
            </div>
            <el-pagination
              v-if="addresses.length > addressPageSize"
              v-model:current-page="currentAddressPage"
              class="profile-pagination"
              background
              layout="total, prev, pager, next, jumper"
              :page-size="addressPageSize"
              :total="addresses.length"
            />
          </template>
        </section>
      </el-tab-pane>

      <el-tab-pane label="我的订单" name="orders">
        <section class="profile-card">
          <div class="toolbar compact-toolbar">
            <h2>我的订单</h2>
            <div class="order-filter">
              <el-select v-model="statusFilter" placeholder="全部状态" @change="applyOrderFilter">
                <el-option label="全部状态" value="all" />
                <el-option label="待发货" :value="0" />
                <el-option label="待收货" :value="1" />
                <el-option label="已完成" :value="2" />
                <el-option label="已取消" :value="3" />
              </el-select>
              <el-input
                v-model="orderKeyword"
                placeholder="搜索订单号"
                clearable
                @clear="applyOrderFilter"
                @keyup.enter="applyOrderFilter"
              />
              <el-button text type="primary" @click="loadOrders">刷新</el-button>
            </div>
          </div>

          <el-skeleton v-if="loadingOrders" :rows="8" animated />
          <el-empty v-else-if="filteredOrders.length === 0" :description="hasOrderFilter ? '没有符合条件的订单' : '暂无订单数据'">
            <el-button type="primary" @click="router.push('/products')">去选商品</el-button>
          </el-empty>
          <template v-else>
            <div class="order-list">
              <article v-for="order in pagedOrders" :key="order.id" class="order-item">
                <div class="order-head">
                  <div>
                    <button class="order-no" type="button" @click="router.push(`/orders/${order.id}`)">{{ order.orderNo }}</button>
                    <p>{{ formatTime(order.createdAt) }}</p>
                  </div>
                  <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
                </div>
                <div class="order-products">
                  <span v-for="item in order.items" :key="item.id">{{ item.productName }} x {{ item.quantity }}</span>
                </div>
                <p v-if="order.trackingNo" class="tracking-text">物流单号：{{ order.trackingNo }}</p>
                <div class="order-foot">
                  <strong>￥{{ formatPrice(order.totalAmount) }}</strong>
                  <div>
                    <el-button v-if="order.status === 0" type="danger" size="small" @click="cancelOrder(order)">取消订单</el-button>
                    <el-button v-if="order.status === 1" type="primary" size="small" @click="confirmReceipt(order)">确认收货</el-button>
                    <el-button size="small" @click="router.push(`/orders/${order.id}`)">详情</el-button>
                  </div>
                </div>
              </article>
            </div>
            <el-pagination
              v-if="filteredOrders.length > orderPageSize"
              v-model:current-page="currentOrderPage"
              class="profile-pagination"
              background
              layout="total, prev, pager, next, jumper"
              :page-size="orderPageSize"
              :total="filteredOrders.length"
            />
          </template>
        </section>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="addressDialogVisible" :title="editingAddressId ? '编辑地址' : '新增地址'" width="520px">
      <el-form ref="addressFormRef" label-position="top" :model="addressForm" :rules="addressRules">
        <el-form-item label="收货人姓名" prop="receiverName">
          <el-input v-model="addressForm.receiverName" maxlength="50" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" maxlength="11" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="详细收货地址" prop="addressDetail">
          <el-input v-model="addressForm.addressDetail" type="textarea" maxlength="255" show-word-limit />
        </el-form-item>
        <el-checkbox v-model="addressForm.defaultAddress">设为默认地址</el-checkbox>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingAddress" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addressApi, orderApi, uploadApi } from '../../api'
import { useUserStore } from '../../stores/user'
import { formatDateTime } from '../../utils/format'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('orders')
const orders = ref([])
const addresses = ref([])
const loadingOrders = ref(false)
const savingProfile = ref(false)
const savingAddress = ref(false)
const uploadingAvatar = ref(false)
const addressDialogVisible = ref(false)
const editingAddressId = ref(null)
const profileFormRef = ref()
const addressFormRef = ref()
const statusFilter = ref('all')
const orderKeyword = ref('')
const appliedOrderKeyword = ref('')
const currentOrderPage = ref(1)
const orderPageSize = 4
const currentAddressPage = ref(1)
const addressPageSize = 8
const phonePattern = /^1[3-9]\d{9}$/

const profileForm = reactive({
  nickname: userStore.user?.nickname || '',
  avatar: userStore.user?.avatar || ''
})
const addressForm = reactive(defaultAddressForm())

const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '昵称长度不能超过 50 个字符', trigger: 'blur' }
  ],
  avatar: [{ max: 255, message: '头像地址不能超过 255 个字符', trigger: 'blur' }]
}
const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { max: 50, message: '收货人姓名不能超过 50 个字符', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!phonePattern.test(String(value || '').trim())) {
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

const isAllOrderStatus = computed(() => statusFilter.value === 'all')
const hasOrderFilter = computed(() => Boolean(appliedOrderKeyword.value || !isAllOrderStatus.value))
const filteredOrders = computed(() => {
  return orders.value.filter((order) => {
    const statusMatched = isAllOrderStatus.value || order.status === statusFilter.value
    const keywordMatched = !appliedOrderKeyword.value || String(order.orderNo || '').includes(appliedOrderKeyword.value)
    return statusMatched && keywordMatched
  })
})
// 我的订单和常用地址都使用前端本地分页，避免列表过长撑开个人中心页面。
const pagedOrders = computed(() => {
  const start = (currentOrderPage.value - 1) * orderPageSize
  return filteredOrders.value.slice(start, start + orderPageSize)
})
const pagedAddresses = computed(() => {
  const start = (currentAddressPage.value - 1) * addressPageSize
  return addresses.value.slice(start, start + addressPageSize)
})

const logout = () => {
  userStore.clearSession()
  ElMessage.success('已退出登录')
  router.push('/')
}

// 保存个人资料后同步 Pinia 和 localStorage，顶部导航会立即更新昵称。
const saveProfile = async () => {
  try {
    await profileFormRef.value?.validate()
  } catch (error) {
    return
  }
  savingProfile.value = true
  try {
    await userStore.updateProfile({
      nickname: profileForm.nickname.trim(),
      avatar: profileForm.avatar.trim() || null
    })
    ElMessage.success('资料已保存')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '资料保存失败')
  } finally {
    savingProfile.value = false
  }
}

// 头像上传后立即保存到用户资料，保证数据库中的 avatar 字段是本地图片访问地址。
const uploadAvatar = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file) {
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('头像图片不能超过 5MB')
    return
  }
  uploadingAvatar.value = true
  try {
    const result = await uploadApi.avatar(file)
    profileForm.avatar = result.data
    await userStore.updateProfile({
      nickname: profileForm.nickname.trim() || userStore.user?.nickname || '用户',
      avatar: profileForm.avatar
    })
    ElMessage.success('头像已上传并保存')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '头像上传失败')
  } finally {
    uploadingAvatar.value = false
  }
}

const loadAddresses = async () => {
  try {
    const result = await addressApi.list()
    addresses.value = result.data || []
    currentAddressPage.value = 1
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '常用地址加载失败')
  }
}

const openAddressDialog = (address) => {
  editingAddressId.value = address?.id || null
  Object.assign(addressForm, address ? {
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    addressDetail: address.addressDetail,
    defaultAddress: address.isDefault === 1
  } : defaultAddressForm())
  addressDialogVisible.value = true
  nextTick(() => addressFormRef.value?.clearValidate())
}

const saveAddress = async () => {
  try {
    await addressFormRef.value?.validate()
  } catch (error) {
    return
  }
  savingAddress.value = true
  const payload = {
    receiverName: addressForm.receiverName.trim(),
    receiverPhone: addressForm.receiverPhone.trim(),
    addressDetail: addressForm.addressDetail.trim(),
    defaultAddress: addressForm.defaultAddress
  }
  try {
    if (editingAddressId.value) {
      await addressApi.update(editingAddressId.value, payload)
      ElMessage.success('地址已更新')
    } else {
      await addressApi.create(payload)
      ElMessage.success('地址已新增')
    }
    addressDialogVisible.value = false
    await loadAddresses()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '地址保存失败')
  } finally {
    savingAddress.value = false
  }
}

const setDefaultAddress = async (address) => {
  try {
    await addressApi.setDefault(address.id)
    ElMessage.success('默认地址已更新')
    await loadAddresses()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '设置默认地址失败')
  }
}

const removeAddress = async (address) => {
  try {
    await ElMessageBox.confirm('确定删除该收货地址吗？', '删除地址', { type: 'warning' })
    await addressApi.remove(address.id)
    ElMessage.success('地址已删除')
    await loadAddresses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '地址删除失败')
    }
  }
}

const loadOrders = async () => {
  loadingOrders.value = true
  try {
    const result = await orderApi.my()
    orders.value = result.data || []
    currentOrderPage.value = 1
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '订单加载失败')
  } finally {
    loadingOrders.value = false
  }
}

const applyOrderFilter = () => {
  appliedOrderKeyword.value = orderKeyword.value.trim()
  currentOrderPage.value = 1
}

const confirmReceipt = async (order) => {
  try {
    await ElMessageBox.confirm(`确认已收到订单 ${order.orderNo} 吗？`, '确认收货', { type: 'warning' })
    await orderApi.confirm(order.id)
    ElMessage.success('已确认收货')
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '确认收货失败')
    }
  }
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm(`确定取消订单 ${order.orderNo} 吗？`, '取消订单', { type: 'warning' })
    await orderApi.cancel(order.id)
    ElMessage.success('订单已取消')
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '取消订单失败')
    }
  }
}

function defaultAddressForm() {
  return {
    receiverName: '',
    receiverPhone: '',
    addressDetail: '',
    defaultAddress: false
  }
}

const statusText = (status) => ({ 0: '待发货', 1: '待收货', 2: '已完成', 3: '已取消' }[status] || '未知')
const statusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }[status] || 'info')
const formatPrice = (price) => Number(price || 0).toFixed(2)
// 订单列表时间统一转为中文日期时间格式。
const formatTime = formatDateTime

onMounted(() => {
  loadOrders()
  loadAddresses()
})
</script>

<style scoped>
.profile-summary,
.profile-card,
.order-item,
.address-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
}

.profile-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 20px;
}

.profile-summary h2,
.profile-summary p,
.compact-toolbar h2,
.address-item p {
  margin: 0;
}

.profile-summary p,
.address-item p,
.tracking-text {
  color: #697782;
}

.profile-card {
  padding: 20px;
}

.profile-card :deep(.el-form) {
  max-width: 560px;
}

.avatar-upload-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.form-tip {
  margin: 8px 0 0;
  color: #909399;
  font-size: 13px;
}

.compact-toolbar {
  margin-bottom: 16px;
}

.order-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.order-filter .el-select,
.order-filter .el-input {
  width: 180px;
}

.order-list,
.address-grid {
  display: grid;
  gap: 14px;
}

.address-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.address-item {
  display: grid;
  gap: 12px;
  padding: 16px;
}

.address-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.order-item {
  display: grid;
  gap: 12px;
  padding: 18px;
}

.order-head,
.order-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.order-head p {
  margin: 4px 0 0;
  color: #697782;
  font-size: 13px;
}

.order-no {
  padding: 0;
  border: 0;
  background: transparent;
  color: #409eff;
  font-weight: 700;
  cursor: pointer;
}

.order-products {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  color: #52616d;
}

.profile-pagination {
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 680px) {
  .order-filter,
  .order-filter .el-select,
  .order-filter .el-input,
  .order-head,
  .order-foot {
    width: 100%;
  }

  .order-head,
  .order-foot {
    align-items: flex-start;
    flex-direction: column;
  }

  .address-grid {
    grid-template-columns: 1fr;
  }
}
</style>
