<template>
  <main class="auth-view">
    <div class="auth-card-wrapper" :style="cardPositionStyle">
      <section class="flip-scene" aria-label="Authentication panel">
        <div class="flip-card" :class="{ 'is-flipped': !isLogin }">
          <div
            class="auth-card-face auth-card-face--front"
            :style="{ pointerEvents: isLogin ? 'auto' : 'none' }"
          >
            <div class="auth-card-content">
              <p class="auth-kicker">CoreFitness</p>
              <h1>Welcome</h1>

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
                <button type="button" @click="switchMode(false)">立即注册</button>
              </p>
            </div>
          </div>

          <div
            class="auth-card-face auth-card-face--back"
            :style="{ pointerEvents: !isLogin ? 'auto' : 'none' }"
          >
            <div class="auth-card-content">
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
                <button type="button" @click="switchMode(true)">返回登录</button>
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { request } from '@/api/request.js'

const route = useRoute()
const router = useRouter()

const isLogin = ref(route.query.type !== 'register')
const loading = ref(false)

const cardPositionStyle = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)'
}

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  email: '',
  password: ''
})

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
    switchMode(true)
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}

function switchMode(nextIsLogin) {
  isLogin.value = nextIsLogin
  router.replace({
    path: '/auth',
    query: {
      type: nextIsLogin ? 'login' : 'register'
    }
  })
}

watch(
  () => route.query.type,
  (type) => {
    isLogin.value = type !== 'register'
  }
)
</script>

<style scoped>
.auth-view {
  position: relative;
  z-index: 10;
  min-height: 100vh;
  overflow: hidden;
  color: var(--text-primary);
  background: var(--bg-primary);
}

.flip-scene {
  width: min(430px, calc(100vw - 32px));
  height: min(560px, calc(100vh - 40px));
  min-height: 520px;
  perspective: 1400px;
}

.flip-card {
  position: relative;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  transition: transform 820ms cubic-bezier(0.22, 1, 0.36, 1);
  will-change: transform;
}

.flip-card.is-flipped {
  transform: rotateY(180deg);
}

.auth-card-face {
  position: absolute;
  inset: 0;
  display: flex;
  overflow: hidden;
  border-radius: 24px;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.auth-card-face--back {
  transform: rotateY(180deg);
}

.auth-card-content {
  position: relative;
  display: flex;
  width: 100%;
  padding: 42px 40px 34px;
  flex-direction: column;
  background: #FFFFFF;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.auth-kicker {
  margin: 0 0 10px;
  color: var(--text-tertiary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

h1 {
  margin: 0 0 30px;
  color: var(--text-primary);
  font-size: 42px;
  font-weight: 760;
  line-height: 1;
}

.auth-form {
  position: relative;
  z-index: 1;
  display: flex;
  flex: 1;
  flex-direction: column;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth-form :deep(.el-form-item__label) {
  margin-bottom: 8px;
  color: var(--text-primary);
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
  color: var(--text-primary);
  caret-color: #7EB8DA;
}

.auth-form :deep(.el-input__inner::placeholder) {
  color: var(--text-tertiary);
}

.auth-form :deep(.el-input .el-input__password) {
  color: var(--text-secondary);
}

.auth-submit {
  width: 100%;
  height: 50px;
  margin-top: auto;
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
  position: relative;
  z-index: 1;
  margin: 22px 0 0;
  color: var(--text-secondary);
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

@media (max-width: 520px) {
  .flip-scene {
    min-height: 500px;
  }

  .auth-card-content {
    padding: 34px 24px 28px;
  }

  h1 {
    margin-bottom: 24px;
    font-size: 36px;
  }
}
</style>
