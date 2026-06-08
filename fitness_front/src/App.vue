<template>
  <!-- 整个应用的外层容器 -->
  <div class="app-shell">
    <FloatingSidebar v-if="showSidebar" />
    <GlobalAiChat v-if="showSidebar" />
    <!-- 页面内容展示区域 -->
    <div class="app-content">
      <!-- 路由视图：渲染当前路由对应的页面组件 -->
      <router-view v-slot="{ Component, route }">
        <transition :name="transitionName" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import FloatingSidebar from '@/components/FloatingSidebar.vue'
import GlobalAiChat from '@/components/GlobalAiChat.vue'

const route = useRoute()

// 判断是否显示侧边栏（管理后台有自己的布局）
const showSidebar = computed(() => {
  const hideOnRoutes = ['/', '/auth', '/login', '/register']
  return !hideOnRoutes.includes(route.path) && !route.path.startsWith('/admin')
})

// 根据路由确定过渡动画名称
const transitionName = computed(() => {
  // 从登录页跳转到仪表盘 - 上滑效果
  if (route.meta?.transition === 'slide-up') {
    return 'slide-up'
  }
  // 默认淡入淡出
  return 'fade'
})
</script>

<style scoped>
/* 应用最外层容器：铺满屏幕、相对定位 */
.app-shell {
  position: relative;
  min-height: 100vh;
  isolation: isolate;
  background: var(--bg-primary);
}

/* 内容区域：相对定位，保证在背景之上 */
.app-content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
}

/* === 路由过渡动画 === */

/* 淡入淡出 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 250ms ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 上滑消失 (登录成功后) */
.slide-up-enter-active {
  transition: all 400ms cubic-bezier(0.22, 1, 0.36, 1);
}

.slide-up-leave-active {
  transition: all 350ms cubic-bezier(0.55, 0, 1, 0.45);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-100vh);
}
</style>
