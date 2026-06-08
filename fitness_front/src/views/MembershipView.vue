<template>
  <main class="membership-view">
    <PageHeader title="会员中心" />

    <!-- 当前会员状态 -->
    <section class="glass-card status-card">
      <div class="status-info">
        <span class="status-label">当前状态</span>
        <strong v-if="vipStatus.isVip" class="status-value active">会员生效中</strong>
        <strong v-else class="status-value inactive">未开通</strong>
      </div>
      <div v-if="vipStatus.isVip" class="status-detail">
        <span>到期时间：{{ formatTime(vipStatus.expireTime) }}</span>
        <span>剩余 {{ vipStatus.remainingDays }} 天</span>
      </div>
      <div v-else class="status-detail">
        <span>开通会员即可使用 AI 智能健身教练</span>
      </div>
    </section>

    <!-- 定价卡片 -->
    <section class="pricing-section">
      <h2 class="section-title">选择套餐</h2>
      <div class="pricing-cards">
        <div class="price-card" v-for="plan in plans" :key="plan.id">
          <div class="price-tag">
            <span class="price-amount">¥{{ plan.price }}</span>
            <span class="price-unit">/{{ plan.label }}</span>
          </div>
          <div class="price-daily">约 ¥{{ plan.daily }}/天</div>
          <div v-if="plan.hot" class="hot-badge">推荐</div>
        </div>
      </div>
    </section>

    <!-- 付款区域 -->
    <section class="glass-card pay-card">
      <h3>开通方式</h3>
      <div class="pay-steps">
        <div class="step">
          <span class="step-num">1</span>
          <span>选择套餐，扫描下方二维码付款</span>
        </div>
        <div class="step">
          <span class="step-num">2</span>
          <span>付款时备注你的用户名：<strong>{{ username }}</strong></span>
        </div>
        <div class="step">
          <span class="step-num">3</span>
          <span>管理员确认后自动激活，刷新页面即可使用</span>
        </div>
      </div>

      <div class="qr-section">
        <div class="qr-box">
          <img src="/1.png" alt="微信收款码" class="qr-image" />
          <p class="qr-label">微信扫码付款</p>
        </div>
      </div>
    </section>

    <!-- 权益说明 -->
    <section class="glass-card benefits-card">
      <h3>会员权益</h3>
      <div class="benefits-list">
        <div class="benefit-item">
          <span class="benefit-icon">AI</span>
          <div>
            <strong>AI 智能教练</strong>
            <p>基于 DeepSeek 大模型，为你定制专属训练计划</p>
          </div>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon">无限</span>
          <div>
            <strong>无限对话</strong>
            <p>不限次数，随时咨询健身问题</p>
          </div>
        </div>
        <div class="benefit-item">
          <span class="benefit-icon">个性化</span>
          <div>
            <strong>个性化建议</strong>
            <p>根据你的身体数据和目标给出针对性方案</p>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { request } from '@/api/request.js'

const username = ref('')
const vipStatus = reactive({
  isVip: false,
  expireTime: null,
  remainingDays: 0
})

const plans = [
  { id: 'month', price: '9.9', label: '月卡', daily: '0.33', days: 30 },
  { id: 'quarter', price: '25.9', label: '季卡', daily: '0.29', days: 90, hot: true },
  { id: 'year', price: '79.9', label: '年卡', daily: '0.22', days: 365 }
]

function formatTime(t) {
  if (!t) return '--'
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(async () => {
  try {
    const profile = await request('/user/profile')
    username.value = profile.username
  } catch { /* ignore */ }

  try {
    const data = await request('/user/vip-status')
    Object.assign(vipStatus, data)
  } catch { /* ignore */ }
})
</script>

<style scoped>
.membership-view {
  position: relative;
  z-index: 1;
  width: min(100%, 1560px);
  box-sizing: border-box;
  margin: 0 auto;
  padding: 36px 56px 48px 128px;
  color: var(--text-primary);
  background: transparent;
}

.glass-card {
  position: relative;
  box-sizing: border-box;
  padding: 24px;
  overflow: hidden;
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* ── 状态卡片 ── */
.status-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.status-label {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 600;
}

.status-value {
  display: block;
  font-size: 20px;
  font-weight: 800;
  margin-top: 4px;
}

.status-value.active {
  color: #A8D8B9;
}

.status-value.inactive {
  color: var(--text-tertiary);
}

.status-detail {
  display: flex;
  gap: 16px;
  color: var(--text-secondary);
  font-size: 13px;
}

/* ── 定价区域 ── */
.pricing-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 700;
}

.pricing-cards {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.price-card {
  position: relative;
  flex: 1;
  min-width: 160px;
  padding: 24px;
  background: #FFFFFF;
  border: 1px solid #E8ECF1;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.price-card:hover {
  border-color: #7EB8DA;
  box-shadow: 0 4px 20px rgba(126, 184, 218, 0.15);
}

.price-tag {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.price-amount {
  font-size: 32px;
  font-weight: 840;
  color: var(--text-primary);
}

.price-unit {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 600;
}

.price-daily {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.hot-badge {
  position: absolute;
  top: -1px;
  right: 20px;
  padding: 4px 12px;
  background: #7EB8DA;
  color: #FFFFFF;
  font-size: 11px;
  font-weight: 700;
  border-radius: 0 0 8px 8px;
}

/* ── 付款区域 ── */
.pay-card {
  margin-bottom: 24px;
}

.pay-card h3 {
  margin: 0 0 20px;
  font-size: 16px;
  font-weight: 700;
}

.pay-steps {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 24px;
}

.step {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--text-secondary);
}

.step strong {
  color: #7EB8DA;
  font-weight: 700;
}

.step-num {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  background: #7EB8DA;
  color: #FFFFFF;
  font-size: 13px;
  font-weight: 700;
  border-radius: 50%;
  flex-shrink: 0;
}

.qr-section {
  display: flex;
  justify-content: center;
}

.qr-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.qr-image {
  width: 200px;
  height: 200px;
  object-fit: contain;
  border-radius: 16px;
  border: 1px solid #E8ECF1;
  background: #FFFFFF;
}

.qr-label {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 600;
}

/* ── 权益说明 ── */
.benefits-card h3 {
  margin: 0 0 20px;
  font-size: 16px;
  font-weight: 700;
}

.benefits-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.benefit-icon {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  background: rgba(126, 184, 218, 0.1);
  color: #7EB8DA;
  font-size: 12px;
  font-weight: 800;
  border-radius: 12px;
  flex-shrink: 0;
}

.benefit-item strong {
  display: block;
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 4px;
}

.benefit-item p {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

@media (max-width: 992px) {
  .membership-view {
    padding: 32px 24px 48px;
  }
}

@media (max-width: 640px) {
  .membership-view {
    padding: 24px 16px 40px;
  }

  .pricing-cards {
    flex-direction: column;
  }
}
</style>
