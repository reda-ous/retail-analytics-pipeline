<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const summary = ref(null)
const products = ref([])
const error = ref(null)

async function refresh() {
  try {
    const [summaryRes, productsRes] = await Promise.all([
      fetch('/api/stats/summary'),
      fetch('/api/stats/products'),
    ])
    if (!summaryRes.ok || !productsRes.ok) {
      throw new Error('the consumer API did not respond with 2xx')
    }
    summary.value = await summaryRes.json()
    products.value = await productsRes.json()
    error.value = null
  } catch (e) {
    error.value = e.message
  }
}

let timer
onMounted(() => {
  refresh()
  timer = setInterval(refresh, 2000)
})
onUnmounted(() => clearInterval(timer))

function formatMoney(value) {
  if (value == null) return '—'
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value)
}
</script>

<template>
  <main>
    <header>
      <h1>Retail Analytics</h1>
      <p class="subtitle">Live stats from the Kafka pipeline &middot; refreshes every 2s</p>
    </header>

    <p v-if="error" class="error">
      Could not reach the consumer API at <code>/api</code> ({{ error }}). Is it running on
      <code>localhost:8080</code>?
    </p>

    <section v-if="summary" class="summary">
      <div class="card">
        <span class="label">Tracked products</span>
        <span class="value">{{ summary.trackedProducts }}</span>
      </div>
      <div class="card">
        <span class="label">Total orders</span>
        <span class="value">{{ summary.totalOrders }}</span>
      </div>
      <div class="card">
        <span class="label">Units sold</span>
        <span class="value">{{ summary.totalQuantitySold }}</span>
      </div>
      <div class="card">
        <span class="label">Revenue</span>
        <span class="value">{{ formatMoney(summary.totalRevenue) }}</span>
      </div>
    </section>

    <table v-if="products.length">
      <thead>
        <tr>
          <th>Product</th>
          <th>Orders</th>
          <th>Units sold</th>
          <th>Revenue</th>
          <th>Stock</th>
          <th>Price</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="p in products" :key="p.productId">
          <td>{{ p.productId }}</td>
          <td>{{ p.totalOrders }}</td>
          <td>{{ p.totalQuantitySold }}</td>
          <td>{{ formatMoney(p.totalRevenue) }}</td>
          <td>{{ p.currentStock ?? '—' }}</td>
          <td>{{ formatMoney(p.currentPrice) }}</td>
        </tr>
      </tbody>
    </table>

    <p v-else-if="!error" class="waiting">Waiting for the first events to arrive&hellip;</p>
  </main>
</template>

<style scoped>
main {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px 64px;
}

header {
  margin-bottom: 28px;
}

h1 {
  font-size: 1.6rem;
  font-weight: 600;
  margin: 0 0 6px;
}

.subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 0.92rem;
}

.error {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #991b1b;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 0.9rem;
}

.waiting {
  color: #6b7280;
}

.summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 32px;
}

@media (max-width: 640px) {
  .summary {
    grid-template-columns: repeat(2, 1fr);
  }
}

.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.card .label {
  font-size: 0.78rem;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.card .value {
  font-size: 1.5rem;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
}

th,
td {
  text-align: left;
  padding: 10px 14px;
  font-size: 0.9rem;
  border-bottom: 1px solid #e5e7eb;
}

th {
  color: #6b7280;
  font-weight: 600;
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  background: #f9fafb;
}

td {
  font-variant-numeric: tabular-nums;
}

tbody tr:last-child td {
  border-bottom: none;
}
</style>
