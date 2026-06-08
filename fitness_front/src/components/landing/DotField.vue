<template>
  <!-- 点阵动画外层容器 -->
  <div ref="containerRef" class="dot-field">
    <!-- Canvas 负责绘制所有圆点 -->
    <canvas ref="canvasRef" class="dot-field__canvas"></canvas>

    <!-- SVG 负责鼠标跟随的发光效果 -->
    <svg class="dot-field__glow" aria-hidden="true">
      <defs>
        <!-- 鼠标发光渐变 -->
        <radialGradient :id="glowId">
          <stop offset="0%" :stop-color="glowColor" />
          <stop offset="100%" stop-color="transparent" />
        </radialGradient>
      </defs>
      <!-- 跟随鼠标的发光圆 -->
      <circle
        ref="glowRef"
        cx="-9999"
        cy="-9999"
        :r="glowRadius"
        :fill="`url(#${glowId})`"
      />
    </svg>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { buildDotGrid } from '@/utils/dotGrid.js'

// 圆周率 2 倍（画圆用）
const TWO_PI = Math.PI * 2

// 组件参数（和 React DotField 完全对应）
const props = defineProps({
  dotRadius: { type: Number, default: 30 },                // 圆点半径
  dotSpacing: { type: Number, default: 14 },             // 圆点间距
  cursorRadius: { type: Number, default: 500 },           // 鼠标感应半径
  cursorForce: { type: Number, default: 0.1 },           // 鼠标推力
  bulgeOnly: { type: Boolean, default: true },           // 仅膨胀模式（无物理惯性）
  bulgeStrength: { type: Number, default: 67 },           // 膨胀强度
  glowRadius: { type: Number, default: 160 },            // 发光半径
  sparkle: { type: Boolean, default: false },            // 随机闪烁效果
  waveAmplitude: { type: Number, default: 0 },           // 波浪振幅
  gradientFrom: { type: String, default: '#ffffff' },    // 渐变起始色
  gradientTo: { type: String, default: '#639714' },       // 渐变结束色
  glowColor: { type: String, default: '#120F17' }         // 发光颜色
})

// DOM 引用
const canvasRef = ref(null)     // Canvas 画布
const containerRef = ref(null)   // 组件容器
const glowRef = ref(null)        // 鼠标发光圆
const glowId = `dot-field-glow-${Math.random().toString(36).slice(2, 9)}` // 唯一ID防冲突

// 监听圆点配置变化，用于重建点阵
const layoutProps = computed(() => [props.dotRadius, props.dotSpacing])

// Canvas 上下文
let ctx

// 所有圆点数据
let dots = []

// 动画帧计数（控制波浪/闪烁）
let frameCount = 0

// 动画句柄（用于取消）
let animationId = 0

// 监听窗口大小变化
let resizeObserver

// 鼠标速度计时器
let speedInterval = 0

// 设备像素比（高清屏适配）
let dpr = 1

// 交互活跃度（鼠标移动越快值越大）
let engagement = 0

// 发光透明度
let glowOpacity = 0

// 画布尺寸与偏移
const size = {
  w: 0,
  h: 0,
  offsetX: 0,
  offsetY: 0
}

// 鼠标位置与速度
const pointer = {
  x: -9999,
  y: -9999,
  prevX: -9999,
  prevY: -9999,
  speed: 0
}

// 重新生成圆点网格
function rebuildDots() {
  dots = buildDotGrid({
    width: size.w,
    height: size.h,
    dotRadius: props.dotRadius,
    dotSpacing: props.dotSpacing
  })
}

// 重置画布大小与适配
function resizeCanvas() {
  const canvas = canvasRef.value
  const container = containerRef.value

  if (!canvas || !container) return

  // 获取容器尺寸
  const rect = container.getBoundingClientRect()
  size.w = rect.width
  size.h = rect.height
  size.offsetX = rect.left + window.scrollX
  size.offsetY = rect.top + window.scrollY

  // 适配高清屏幕
  dpr = Math.min(window.devicePixelRatio || 1, 2)
  canvas.width = Math.max(1, Math.floor(size.w * dpr))
  canvas.height = Math.max(1, Math.floor(size.h * dpr))
  canvas.style.width = `${size.w}px`
  canvas.style.height = `${size.h}px`

  // 获取绘图上下文
  ctx = canvas.getContext('2d', { alpha: true })
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)

  // 重建圆点
  rebuildDots()
}

// 鼠标移动时更新相对坐标
function handlePointerMove(event) {
  pointer.x = event.pageX - size.offsetX
  pointer.y = event.pageY - size.offsetY
}

// 鼠标离开时重置坐标
function handlePointerLeave() {
  pointer.x = -9999
  pointer.y = -9999
}

// 计算鼠标移动速度
function updatePointerSpeed() {
  const dx = pointer.prevX - pointer.x
  const dy = pointer.prevY - pointer.y
  const distance = Math.hypot(dx, dy)

  pointer.speed += (distance - pointer.speed) * 0.5
  if (pointer.speed < 0.001) pointer.speed = 0
  pointer.prevX = pointer.x
  pointer.prevY = pointer.y
}

// ==============================================
// 【核心】每一帧渲染：圆点计算 + 绘制 + 交互逻辑
// ==============================================
function renderFrame() {
  if (!ctx) return

  frameCount += 1

  // 鼠标活跃度缓动（越动越亮）
  const targetEngagement = Math.min(pointer.speed / 5, 1)
  engagement += (targetEngagement - engagement) * 0.06
  if (engagement < 0.001) engagement = 0

  // 发光透明度缓动
  glowOpacity += (engagement - glowOpacity) * 0.08
  if (glowRef.value) {
    glowRef.value.setAttribute('cx', pointer.x)
    glowRef.value.setAttribute('cy', pointer.y)
    glowRef.value.style.opacity = glowOpacity
  }

  // 清空画布
  ctx.clearRect(0, 0, size.w, size.h)

  // 设置渐变
  const gradient = ctx.createLinearGradient(0, 0, size.w, size.h)
  gradient.addColorStop(0, props.gradientFrom)
  gradient.addColorStop(1, props.gradientTo)
  ctx.fillStyle = gradient

  // 常用变量缓存
  const cursorRadiusSq = props.cursorRadius * props.cursorRadius
  const drawRadius = props.dotRadius / 2
  const time = frameCount * 0.02

  ctx.beginPath()

  // 遍历所有圆点
  for (let i = 0; i < dots.length; i++) {
    const dot = dots[i]
    const dx = pointer.x - dot.ax
    const dy = pointer.y - dot.ay
    const distSq = dx * dx + dy * dy

    // 鼠标在感应范围内
    if (distSq < cursorRadiusSq && engagement > 0.01) {
      const dist = Math.sqrt(distSq) || 1
      const angle = Math.atan2(dy, dx)

      // 膨胀模式：圆点被鼠标推开
      if (props.bulgeOnly) {
        const t = 1 - dist / props.cursorRadius
        const push = t * t * props.bulgeStrength * engagement
        dot.sx += (dot.ax - Math.cos(angle) * push - dot.sx) * 0.15
        dot.sy += (dot.ay - Math.sin(angle) * push - dot.sy) * 0.15
      } else {
        // 物理模式：圆点受惯性影响
        const move = (500 / dist) * (pointer.speed * props.cursorForce)
        dot.vx += Math.cos(angle) * -move
        dot.vy += Math.sin(angle) * -move
      }
    } else if (props.bulgeOnly) {
      // 远离鼠标后自动复位
      dot.sx += (dot.ax - dot.sx) * 0.1
      dot.sy += (dot.ay - dot.sy) * 0.1
    }

    // 物理惯性衰减
    if (!props.bulgeOnly) {
      dot.vx *= 0.9
      dot.vy *= 0.9
      dot.x = dot.ax + dot.vx
      dot.y = dot.ay + dot.vy
      dot.sx += (dot.x - dot.sx) * 0.1
      dot.sy += (dot.y - dot.sy) * 0.1
    }

    // 最终绘制坐标
    let drawX = dot.sx
    let drawY = dot.sy

    // 波浪动画
    if (props.waveAmplitude > 0) {
      drawY += Math.sin(dot.ax * 0.03 + time) * props.waveAmplitude
      drawX += Math.cos(dot.ay * 0.03 + time * 0.7) * props.waveAmplitude * 0.5
    }

    // 随机闪烁效果（3%圆点变大）
    const radius = props.sparkle && (((i * 2654435761) ^ (frameCount >> 3)) >>> 0) % 100 < 3
      ? drawRadius * 1.8
      : drawRadius

    // 绘制圆点
    ctx.moveTo(drawX + radius, drawY)
    ctx.arc(drawX, drawY, radius, 0, TWO_PI)
  }

  // 统一填充所有圆点
  ctx.fill()

  // 下一帧
  animationId = requestAnimationFrame(renderFrame)
}

// 挂载后初始化
onMounted(async () => {
  await nextTick()
  resizeCanvas()

  // 监听容器大小变化
  resizeObserver = new ResizeObserver(resizeCanvas)
  resizeObserver.observe(containerRef.value)

  // 鼠标事件监听
  containerRef.value.addEventListener('pointermove', handlePointerMove, { passive: true })
  containerRef.value.addEventListener('pointerleave', handlePointerLeave)

  // 定时计算鼠标速度
  speedInterval = window.setInterval(updatePointerSpeed, 20)

  // 启动渲染
  animationId = requestAnimationFrame(renderFrame)
})

// 销毁时清理
onUnmounted(() => {
  cancelAnimationFrame(animationId)
  clearInterval(speedInterval)
  resizeObserver?.disconnect()
  containerRef.value?.removeEventListener('pointermove', handlePointerMove)
  containerRef.value?.removeEventListener('pointerleave', handlePointerLeave)
})

// 圆点大小/间距变化时重建网格
watch(layoutProps, rebuildDots)

defineExpose({
  updatePointerFromEvent: handlePointerMove,
  clearPointer: handlePointerLeave
})
</script>

<style scoped>
/* 主容器：铺满父级 */
.dot-field {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

/* Canvas 和发光层绝对定位铺满 */
.dot-field__canvas,
.dot-field__glow {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.dot-field__canvas {
  display: block;
}

/* 发光层不拦截鼠标 */
.dot-field__glow {
  pointer-events: none;
}

.dot-field__glow circle {
  opacity: 0;
  will-change: opacity;
}
</style>
