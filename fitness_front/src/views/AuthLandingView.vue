<template>
  <div class="landing-page">
    <!-- 第一屏：Hero 区域 -->
    <section class="hero-section" :style="backgroundVars" aria-label="Gym interior with mountain view">
      <div class="bg-wrapper">
        <div class="background-scene__image" aria-hidden="true"></div>
        <div class="background-scene__bottom-shade" aria-hidden="true"></div>

        <div class="login-title">
          <DecryptedText v-bind="titleConfig" />
        </div>

        <div class="interaction-rails" aria-label="Workout encouragement tips">
          <div
            v-for="item in railData"
            :key="item.id"
            class="rail"
            @mouseenter="setActiveRail(item.id, $event)"
            @mousemove="syncDotFieldPointer"
            @mouseleave="clearActiveRail"
          >
            <span v-if="item.id < railData.length" class="divider-line" aria-hidden="true"></span>

            <transition name="fade-slide">
              <div
                v-if="activeRail === item.id"
                class="rail-tip"
                :style="{ '--tip-x': item.x, '--tip-y': item.y }"
              >
                {{ item.text }}
              </div>
            </transition>
          </div>
        </div>

        <div class="background-scene__dot-field" aria-hidden="true">
          <DotField ref="dotFieldRef" v-bind="dotFieldConfig" />
        </div>
      </div>
    </section>

    <!-- 第二屏：功能介绍区域 -->
    <section class="features-section" id="features">
      <div class="features-container">
        <div class="section-header animate-on-scroll">
          <p class="section-tag">为什么选择 Core Fitness</p>
          <h2 class="section-title">智能健身，科学管理</h2>
          <p class="section-desc">一站式健身管理平台，让每一次训练都有据可依</p>
        </div>

        <div class="features-grid">
          <article class="feature-card animate-on-scroll" style="--delay: 0ms">
            <div class="feature-icon" style="background: linear-gradient(135deg, #7EB8DA, #A8D4E6)">
              <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                <path d="M3 3v18h18" />
                <path d="M18.7 8l-5.1 5.2-2.8-2.7L7 14.3" />
              </svg>
            </div>
            <h3>数据追踪</h3>
            <p>记录体重、体脂、围度变化，可视化图表展示训练进展</p>
          </article>

          <article class="feature-card animate-on-scroll" style="--delay: 100ms">
            <div class="feature-icon" style="background: linear-gradient(135deg, #ffdb69, #F5D5A0)">
              <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                <circle cx="12" cy="12" r="10" />
                <circle cx="12" cy="12" r="6" />
                <circle cx="12" cy="12" r="2" />
              </svg>
            </div>
            <h3>目标管理</h3>
            <p>设定减脂、增肌、塑形目标，智能追踪完成进度</p>
          </article>

          <article class="feature-card animate-on-scroll" style="--delay: 200ms">
            <div class="feature-icon" style="background: linear-gradient(135deg, #e8a18b, #F0BFB0)">
              <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                <path d="M12 2a9 9 0 0 1 9 9c0 3.9-3.3 7.5-9 11-5.7-3.5-9-7.1-9-11a9 9 0 0 1 9-9z" />
                <path d="M12 6v6l4 2" />
              </svg>
            </div>
            <h3>训练计划</h3>
            <p>制定个性化训练计划，记录每次训练详情</p>
          </article>

          <article class="feature-card animate-on-scroll" style="--delay: 300ms">
            <div class="feature-icon" style="background: linear-gradient(135deg, #A8D8B9, #C5E8D0)">
              <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                <path d="M18 8h1a4 4 0 0 1 0 8h-1" />
                <path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z" />
                <line x1="6" y1="1" x2="6" y2="4" />
                <line x1="10" y1="1" x2="10" y2="4" />
                <line x1="14" y1="1" x2="14" y2="4" />
              </svg>
            </div>
            <h3>饮食记录</h3>
            <p>记录每日摄入，计算热量平衡，科学控制饮食</p>
          </article>
        </div>
      </div>
    </section>

    <!-- 第三屏：登录注册区域 -->
    <section class="auth-section" id="auth">
      <div class="auth-container animate-on-scroll">
        <div class="auth-card">
          <div class="auth-card-inner">
            <!-- 登录表单 -->
            <div v-if="isLogin" class="auth-form-side">
              <p class="auth-kicker">CoreFitness</p>
              <h1>Welcome Back</h1>

              <el-form
                class="auth-form"
                :model="loginForm"
                label-position="top"
                autocomplete="on"
                @submit.prevent="handleLogin"
              >
                <el-form-item label="用户名">
                  <el-input v-model="loginForm.username" placeholder="请输入用户名" autocomplete="username" />
                </el-form-item>

                <el-form-item label="密码">
                  <el-input
                    v-model="loginForm.password"
                    placeholder="请输入密码"
                    show-password
                    type="password"
                    autocomplete="current-password"
                  />
                </el-form-item>

                <el-button class="auth-submit" type="primary" native-type="submit" :loading="loading">
                  登录
                </el-button>
              </el-form>

              <p class="auth-switch">
                还没有加入我们？
                <button type="button" @click="isLogin = false">立即注册</button>
              </p>
            </div>

            <!-- 注册表单 -->
            <div v-else class="auth-form-side">
              <p class="auth-kicker">CoreFitness</p>
              <h1>Join Us</h1>

              <el-form
                class="auth-form"
                :model="registerForm"
                label-position="top"
                autocomplete="off"
                @submit.prevent="handleRegister"
              >
                <el-form-item label="用户名">
                  <el-input v-model="registerForm.username" placeholder="设置用户名" autocomplete="username" />
                </el-form-item>

                <el-form-item label="邮箱地址">
                  <el-input v-model="registerForm.email" placeholder="name@example.com" autocomplete="email" />
                </el-form-item>

                <el-form-item label="密码">
                  <el-input
                    v-model="registerForm.password"
                    placeholder="不少于 6 位"
                    show-password
                    type="password"
                    autocomplete="new-password"
                  />
                </el-form-item>

                <el-button class="auth-submit" type="primary" native-type="submit" :loading="loading">
                  注册
                </el-button>
              </el-form>

              <p class="auth-switch">
                已有账号？
                <button type="button" @click="isLogin = true">返回登录</button>
              </p>
            </div>

            <!-- 右侧装饰 -->
            <div class="auth-visual-side">
              <div class="visual-content">
                <h2>开启你的健身之旅</h2>
                <p>加入 Core Fitness，用数据驱动每一次训练</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import DecryptedText from '@/components/landing/DecryptedText.vue'
import DotField from '@/components/landing/DotField.vue'
import { request } from '@/api/request.js'
import {
  backgroundVars,
  dotFieldConfig,
  railData,
  titleConfig
} from '@/config/authLanding.config.js'

const router = useRouter()
const activeRail = ref(null)
const dotFieldRef = ref(null)
const isLogin = ref(true)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  email: '',
  password: ''
})

// 滚动动画观察器
let observer = null

onMounted(() => {
  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible')
      }
    })
  }, {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  })

  document.querySelectorAll('.animate-on-scroll').forEach(el => {
    observer.observe(el)
  })
})

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
  }
})

function setActiveRail(id, event) {
  activeRail.value = id
  syncDotFieldPointer(event)
}

function clearActiveRail() {
  activeRail.value = null
  dotFieldRef.value?.clearPointer()
}

function syncDotFieldPointer(event) {
  dotFieldRef.value?.updatePointerFromEvent(event)
}

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  loading.value = true
  try {
    const token = await request('/user/login', {
      method: 'POST',
      body: JSON.stringify({
        username: loginForm.username,
        password: loginForm.password
      })
    })
    localStorage.setItem('token', token)

    const user = await request('/user/profile')
    localStorage.setItem('user', JSON.stringify(user))

    ElMessage.success('登录成功')
    if (user.role === 'admin') {
      router.push('/admin')
    } else {
      router.push('/dashboard')
    }
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.username || !registerForm.password) {
    ElMessage.warning('请填写完整的注册信息')
    return
  }
  if (registerForm.password.length < 6) {
    ElMessage.warning('密码长度不能少于 6 位')
    return
  }
  loading.value = true
  try {
    await request('/user/register', {
      method: 'POST',
      body: JSON.stringify({
        username: registerForm.username,
        password: registerForm.password,
        email: registerForm.email || null
      })
    })
    ElMessage.success('注册成功，请登录')
    isLogin.value = true
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.landing-page {
  width: 100%;
  overflow-x: hidden;
}

/* ===== 滚动动画基础样式 ===== */
.animate-on-scroll {
  opacity: 0;
  transform: translateY(40px);
  transition: opacity 0.8s ease, transform 0.8s ease;
  transition-delay: var(--delay, 0ms);
}

.animate-on-scroll.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* ===== 第一屏：Hero 区域 ===== */
.hero-section {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  isolation: isolate;
  background: var(--scene-bg);
}

.bg-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.background-scene__image {
  position: absolute;
  inset: 0;
  z-index: 0;
  background-image: url('@/assets/images/bg-gym-mountain.jpg');
  background-position: center;
  background-size: cover;
}

.background-scene__bottom-shade {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 1;
  height: var(--bottom-shade-height);
  min-height: var(--bottom-shade-min-height);
  background:
    linear-gradient(
      to bottom,
      rgba(7, 7, 7, 0),
      var(--bottom-shade-middle) var(--bottom-shade-middle-stop),
      var(--scene-bg) 100%
    ),
    linear-gradient(90deg, var(--bottom-shade-accent), transparent 34%, var(--bottom-shade-highlight));
}

.login-title {
  position: absolute;
  top: 30%;
  left: 50%;
  z-index: 3;
  transform: translate(-50%, -50%);
}

.interaction-rails {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  height: var(--rails-height);
  pointer-events: none;
}

.rail {
  position: static;
  pointer-events: auto;
}

.divider-line {
  position: absolute;
  top: var(--rail-line-top);
  width: 1px;
  height: var(--rail-line-height);
  background: linear-gradient(to bottom, transparent, var(--rail-line-color), transparent);
  box-shadow: 0 0 var(--rail-line-glow) var(--rail-line-color);
}

.rail:nth-child(1) .divider-line {
  left: calc(100% / 3);
}

.rail:nth-child(2) .divider-line {
  left: calc(100% / 3 * 2);
}

.rail-tip {
  position: absolute;
  left: var(--tip-x);
  top: var(--tip-y);
  max-width: 28vw;
  color: var(--rail-tip-color);
  font-family: var(--rail-tip-font-family);
  font-size: var(--rail-tip-font-size);
  font-weight: var(--rail-tip-font-weight);
  letter-spacing: var(--rail-tip-letter-spacing);
  line-height: 1.25;
  text-align: center;
  text-shadow: 0 0 var(--rail-tip-glow) var(--rail-tip-color);
  white-space: nowrap;
  transform: translate(-50%, 0);
}

.background-scene__dot-field {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 2;
  height: var(--dot-field-height);
  min-height: var(--dot-field-min-height);
  opacity: var(--dot-field-opacity);
  pointer-events: none;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 260ms ease, transform 260ms ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translate(-50%, 10px);
}

.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
  transform: translate(-50%, 0);
}

/* ===== 第二屏：功能介绍区域 ===== */
.features-section {
  padding: 120px 0;
  background: #FFFFFF;
}

.features-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 48px;
}

.section-header {
  text-align: center;
  margin-bottom: 80px;
}

.section-tag {
  color: #7EB8DA;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  margin: 0 0 16px;
}

.section-title {
  color: #2D3748;
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 20px;
  line-height: 1.2;
}

.section-desc {
  color: #718096;
  font-size: 18px;
  margin: 0;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
}

.feature-card {
  padding: 40px 32px;
  background: #F8F6F3;
  border-radius: 24px;
  text-align: center;
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
}

.feature-icon {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.feature-icon svg {
  width: 32px;
  height: 32px;
}

.feature-card h3 {
  color: #2D3748;
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 12px;
}

.feature-card p {
  color: #718096;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

/* ===== 第三屏：登录注册区域 ===== */
.auth-section {
  padding: 120px 0;
  background: #F8F6F3;
  min-height: 100vh;
  display: flex;
  align-items: center;
}

.auth-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 48px;
  width: 100%;
}

.auth-card {
  background: #FFFFFF;
  border-radius: 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.auth-card-inner {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 580px;
}

.auth-form-side {
  padding: 60px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-kicker {
  margin: 0 0 10px;
  color: #A0AEC0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.auth-form-side h1 {
  margin: 0 0 32px;
  color: #2D3748;
  font-size: 36px;
  font-weight: 760;
  line-height: 1;
}

.auth-form {
  display: flex;
  flex-direction: column;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.auth-form :deep(.el-form-item__label) {
  margin-bottom: 8px;
  color: #2D3748;
  font-size: 13px;
  font-weight: 650;
  line-height: 1.2;
}

.auth-form :deep(.el-input__wrapper) {
  min-height: 48px;
  border: 1px solid #E8ECF1;
  border-radius: 14px;
  background: #F8F6F3;
  box-shadow: none;
  transition: border-color 180ms ease, background 180ms ease, box-shadow 180ms ease;
}

.auth-form :deep(.el-input__wrapper:hover),
.auth-form :deep(.el-input__wrapper.is-focus) {
  border-color: #7EB8DA;
  background: #FFFFFF;
  box-shadow: 0 0 0 3px rgba(126, 184, 218, 0.12);
}

.auth-form :deep(.el-input__inner) {
  color: #2D3748;
  caret-color: #7EB8DA;
}

.auth-form :deep(.el-input__inner::placeholder) {
  color: #A0AEC0;
}

.auth-submit {
  width: 100%;
  height: 50px;
  margin-top: 8px;
  border: none;
  border-radius: 15px;
  background: #7EB8DA;
  color: #FFFFFF;
  font-size: 15px;
  font-weight: 760;
  letter-spacing: 0.08em;
  box-shadow: 0 4px 14px rgba(126, 184, 218, 0.3);
  transition: all 0.2s ease;
}

.auth-submit:hover,
.auth-submit:focus {
  background: #9DCEE6;
  box-shadow: 0 6px 20px rgba(126, 184, 218, 0.4);
  transform: translateY(-1px);
}

.auth-switch {
  margin: 24px 0 0;
  color: #718096;
  font-size: 13px;
  text-align: center;
}

.auth-switch button {
  padding: 0;
  border: 0;
  color: #7EB8DA;
  background: transparent;
  cursor: pointer;
  font-weight: 760;
}

.auth-switch button:hover {
  color: #5A9BC0;
}

/* 右侧装饰区域 */
.auth-visual-side {
  background: linear-gradient(135deg, #7EB8DA, #B8A9C9);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 48px;
}

.visual-content {
  text-align: center;
  color: #FFFFFF;
}

.visual-content h2 {
  font-size: 32px;
  font-weight: 800;
  margin: 0 0 16px;
  line-height: 1.2;
}

.visual-content p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 48px;
  line-height: 1.5;
}

.visual-stats {
  display: flex;
  gap: 40px;
  justify-content: center;
}

.stat-item {
  text-align: center;
}

.stat-item strong {
  display: block;
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 8px;
}

.stat-item span {
  font-size: 13px;
  opacity: 0.8;
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .auth-card-inner {
    grid-template-columns: 1fr;
  }

  .auth-visual-side {
    padding: 48px;
  }
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: 1fr;
  }

  .section-title {
    font-size: 32px;
  }

  .auth-form-side {
    padding: 40px 24px;
  }

  .auth-visual-side {
    padding: 40px 24px;
  }

  .visual-stats {
    flex-direction: column;
    gap: 24px;
  }
}

@media (max-width: 480px) {
  .section-title {
    font-size: 28px;
  }

  .auth-form-side h1 {
    font-size: 28px;
  }
}
</style>
