<template>
  <section class="page admin-page">
    <div class="page-heading">
      <div>
        <h1>后台首页</h1>
        <p>实时查看用户、商品和订单概况，数据每 30 秒自动刷新一次。</p>
      </div>
      <el-button type="primary" :loading="loading" @click="loadStats">刷新数据</el-button>
    </div>

    <div class="stat-grid">
      <article v-for="card in statCards" :key="card.label" class="stat-card" :class="card.tone">
        <span>{{ card.label }}</span>
        <strong>{{ formatNumber(card.value) }}</strong>
        <small>{{ card.hint }}</small>
      </article>
    </div>

    <div class="dashboard-layout">
      <section class="admin-card chart-card">
        <div class="card-head">
          <div>
            <h2>近七天销量折线图</h2>
            <p>按订单商品数量汇总，已取消订单不计入销量</p>
          </div>
        </div>
        <div class="line-chart">
          <svg viewBox="0 0 640 260" role="img" aria-label="近七天销量折线图">
            <line
              v-for="(tick, index) in yTicks"
              :key="`grid-${index}`"
              class="grid-line"
              x1="52"
              x2="612"
              :y1="tick.y"
              :y2="tick.y"
            />
            <text v-for="(tick, index) in yTicks" :key="`label-${index}`" class="axis-label" x="42" :y="tick.y + 4">
              {{ formatNumber(tick.value) }}
            </text>
            <polyline class="sales-line" :points="lineChartPoints" />
            <g v-for="point in chartPoints" :key="point.date">
              <circle class="sales-dot" :cx="point.x" :cy="point.y" r="5" />
              <text class="point-value" :x="point.x" :y="point.y - 12">{{ formatNumber(point.sales) }}</text>
              <text class="axis-label date-label" :x="point.x" y="238">{{ point.date }}</text>
            </g>
          </svg>
          <div class="chart-summary">
            <span>七天总销量：{{ formatNumber(totalRecentSales) }}</span>
            <span>最高单日：{{ formatNumber(maxRecentSales) }}</span>
          </div>
        </div>
      </section>

      <section class="admin-card order-card">
        <div class="card-head">
          <div>
            <h2>订单处理</h2>
            <p>当天待发货订单占比</p>
          </div>
        </div>
        <div class="order-meter">
          <div class="meter-circle" :style="{ '--percent': `${pendingRate}%` }">
            <strong>{{ pendingRate }}%</strong>
            <span>待处理</span>
          </div>
          <div class="meter-list">
            <p><span>今日待发货</span><strong>{{ formatNumber(stats.todayWaitShipOrderCount) }}</strong></p>
            <p><span>今日已处理</span><strong>{{ formatNumber(todayProcessedOrderCount) }}</strong></p>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { adminApi } from '../../api'

const stats = reactive({
  userCount: 0,
  productCount: 0,
  orderCount: 0,
  waitShipOrderCount: 0,
  todayOrderCount: 0,
  todayWaitShipOrderCount: 0,
  recentSales: []
})
const loading = ref(false)
let refreshTimer = null

const statCards = computed(() => [
  { label: '用户总数', value: stats.userCount, hint: '当前注册用户', tone: 'teal' },
  { label: '商品总数', value: stats.productCount, hint: '已维护商品', tone: 'blue' },
  { label: '订单总数', value: stats.orderCount, hint: '累计订单', tone: 'amber' },
  { label: '待处理订单', value: stats.waitShipOrderCount, hint: '待发货订单', tone: 'red' }
])

const chartRows = computed(() => stats.recentSales || [])
const maxRecentSales = computed(() => Math.max(0, ...chartRows.value.map((row) => Number(row.sales || 0))))
const totalRecentSales = computed(() => chartRows.value.reduce((sum, row) => sum + Number(row.sales || 0), 0))

// 折线图使用 SVG 绘制，避免为了一个小图表额外增加前端依赖。
const chartPoints = computed(() => {
  const rows = chartRows.value
  const maxValue = Math.max(1, maxRecentSales.value)
  const step = rows.length > 1 ? 560 / (rows.length - 1) : 0
  return rows.map((row, index) => ({
    date: row.date,
    sales: Number(row.sales || 0),
    x: 52 + index * step,
    y: 200 - (Number(row.sales || 0) / maxValue) * 150
  }))
})
const lineChartPoints = computed(() => chartPoints.value.map((point) => `${point.x},${point.y}`).join(' '))
const yTicks = computed(() => {
  const maxValue = Math.max(1, maxRecentSales.value)
  return [maxValue, Math.round(maxValue / 2), 0].map((value) => ({
    value,
    y: 200 - (value / maxValue) * 150
  }))
})

const todayProcessedOrderCount = computed(() => {
  return Math.max(0, Number(stats.todayOrderCount || 0) - Number(stats.todayWaitShipOrderCount || 0))
})
const pendingRate = computed(() => {
  if (!stats.todayOrderCount) {
    return 0
  }
  return Math.round((Number(stats.todayWaitShipOrderCount || 0) / Number(stats.todayOrderCount)) * 100)
})

// 管理端首页进入时加载真实统计数据，并按参考文档要求定时刷新。
const loadStats = async () => {
  loading.value = true
  try {
    const result = await adminApi.dashboard()
    Object.assign(stats, result.data || {})
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '后台统计加载失败')
  } finally {
    loading.value = false
  }
}

const formatNumber = (value) => Number(value || 0).toLocaleString('zh-CN')

onMounted(() => {
  loadStats()
  refreshTimer = window.setInterval(loadStats, 30000)
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  display: grid;
  gap: 8px;
  min-height: 130px;
  padding: 20px;
  border: 1px solid #e1e7ef;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(24, 38, 52, 0.04);
}

.stat-card span,
.stat-card small {
  color: #697782;
}

.stat-card strong {
  color: #17212b;
  font-size: 34px;
  line-height: 1;
}

.stat-card.red strong {
  color: #dc2626;
}

.stat-card.teal {
  border-top: 4px solid #0f766e;
}

.stat-card.blue {
  border-top: 4px solid #2563eb;
}

.stat-card.amber {
  border-top: 4px solid #d97706;
}

.stat-card.red {
  border-top: 4px solid #dc2626;
}

.dashboard-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(280px, 0.8fr);
  gap: 16px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.card-head h2,
.card-head p {
  margin: 0;
}

.card-head h2 {
  font-size: 18px;
}

.card-head p {
  margin-top: 4px;
  color: #697782;
}

.line-chart {
  display: grid;
  gap: 12px;
}

.line-chart svg {
  width: 100%;
  height: auto;
  min-height: 260px;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
  background: #fbfdff;
}

.grid-line {
  stroke: #e6ebf2;
  stroke-width: 1;
}

.axis-label,
.point-value {
  fill: #697782;
  font-size: 13px;
  text-anchor: end;
}

.date-label,
.point-value {
  text-anchor: middle;
}

.sales-line {
  fill: none;
  stroke: #0f766e;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 4;
}

.sales-dot {
  fill: #ffffff;
  stroke: #0f766e;
  stroke-width: 3;
}

.point-value {
  fill: #17212b;
  font-weight: 700;
}

.chart-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  color: #52616d;
  font-size: 14px;
}

.order-meter {
  display: grid;
  gap: 20px;
  justify-items: center;
}

.meter-circle {
  display: grid;
  width: 190px;
  height: 190px;
  place-items: center;
  align-content: center;
  border-radius: 50%;
  background:
    radial-gradient(circle at center, #ffffff 0 58%, transparent 59%),
    conic-gradient(#dc2626 var(--percent), #e8edf3 0);
}

.meter-circle strong {
  color: #dc2626;
  font-size: 34px;
  line-height: 1;
}

.meter-circle span {
  color: #697782;
}

.meter-list {
  display: grid;
  width: 100%;
  gap: 10px;
}

.meter-list p {
  display: flex;
  justify-content: space-between;
  margin: 0;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f6f8fb;
}

@media (max-width: 980px) {
  .stat-grid,
  .dashboard-layout {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 680px) {
  .stat-grid,
  .dashboard-layout {
    grid-template-columns: 1fr;
  }
}
</style>
