<template>
  <div class="pill-nav-container" :class="className">
    <nav class="pill-nav" aria-label="Primary" :style="cssVars">
      <component
        :is="isRouterLink(homeHref) ? RouterLink : 'a'"
        v-if="hasLogo"
        ref="logoRef"
        class="pill-logo"
        v-bind="logoLinkProps"
        aria-label="Home"
        @mouseenter="handleLogoEnter"
      >
        <slot name="logo">
          <img ref="logoImgRef" :src="logo" :alt="logoAlt" />
        </slot>
      </component>

      <div ref="navItemsRef" class="pill-nav-items desktop-only">
        <ul class="pill-list" role="menubar">
          <li v-for="(item, index) in items" :key="item.href || `item-${index}`" role="none">
            <component
              :is="isRouterLink(item.href) ? RouterLink : 'a'"
              class="pill"
              :class="{ 'is-active': activeHref === item.href }"
              role="menuitem"
              v-bind="getItemLinkProps(item)"
              :aria-label="item.ariaLabel || item.label"
              @mouseenter="handleEnter(index)"
              @mouseleave="handleLeave(index)"
            >
              <span
                :ref="(el) => setCircleRef(el, index)"
                class="hover-circle"
                aria-hidden="true"
              ></span>
              <span class="label-stack">
                <span class="pill-label">{{ item.label }}</span>
                <span class="pill-label-hover" aria-hidden="true">{{ item.label }}</span>
              </span>
            </component>
          </li>
        </ul>
      </div>

      <button
        ref="hamburgerRef"
        class="mobile-menu-button mobile-only"
        type="button"
        aria-label="Toggle menu"
        :aria-expanded="isMobileMenuOpen"
        @click="toggleMobileMenu"
      >
        <span class="hamburger-line"></span>
        <span class="hamburger-line"></span>
      </button>
    </nav>

    <div ref="mobileMenuRef" class="mobile-menu-popover mobile-only" :style="cssVars">
      <ul class="mobile-menu-list">
        <li v-for="(item, index) in items" :key="item.href || `mobile-item-${index}`">
          <component
            :is="isRouterLink(item.href) ? RouterLink : 'a'"
            class="mobile-menu-link"
            :class="{ 'is-active': activeHref === item.href }"
            v-bind="getItemLinkProps(item)"
            @click="closeMobileMenu"
          >
            {{ item.label }}
          </component>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, useSlots, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { gsap } from 'gsap'
import { isRouterLink } from '@/utils/routeLink.js'

const props = defineProps({
  logo: { type: String, default: '' },
  logoAlt: { type: String, default: 'Logo' },
  items: {
    type: Array,
    default: () => [
      { label: 'Login', href: '/login' },
      { label: 'Register', href: '/register' }
    ]
  },
  activeHref: { type: String, default: '' },
  className: { type: String, default: '' },
  ease: { type: String, default: 'power3.out' },
  baseColor: { type: String, default: '#fff' },
  pillColor: { type: String, default: '#120F17' },
  hoveredPillTextColor: { type: String, default: '#120F17' },
  pillTextColor: { type: String, default: '' },
  initialLoadAnimation: { type: Boolean, default: true }
})

const emit = defineEmits(['mobile-menu-click'])
const slots = useSlots()

const isMobileMenuOpen = ref(false)
const circleRefs = ref([])
const timelineRefs = []
const activeTweenRefs = []
const logoRef = ref(null)
const logoImgRef = ref(null)
const logoTweenRef = ref(null)
const hamburgerRef = ref(null)
const mobileMenuRef = ref(null)
const navItemsRef = ref(null)

const resolvedPillTextColor = computed(() => props.pillTextColor || props.baseColor)
const hasLogo = computed(() => Boolean(props.logo || slots.logo))
const homeHref = computed(() => props.items?.[0]?.href || '/')
const cssVars = computed(() => ({
  '--base': props.baseColor,
  '--pill-bg': props.pillColor,
  '--hover-text': props.hoveredPillTextColor,
  '--pill-text': resolvedPillTextColor.value
}))
const logoLinkProps = computed(() => (
  isRouterLink(homeHref.value)
    ? { to: homeHref.value }
    : { href: homeHref.value || '#' }
))

function getItemLinkProps(item) {
  return isRouterLink(item.href)
    ? { to: item.href }
    : { href: item.href || '#' }
}

function setCircleRef(element, index) {
  if (element) circleRefs.value[index] = element
}

function layoutPills() {
  circleRefs.value.forEach((circle, index) => {
    if (!circle?.parentElement) return

    const pill = circle.parentElement
    const { width, height } = pill.getBoundingClientRect()
    const radius = ((width * width) / 4 + height * height) / (2 * height)
    const diameter = Math.ceil(2 * radius) + 2
    const delta = Math.ceil(radius - Math.sqrt(Math.max(0, radius * radius - (width * width) / 4))) + 1
    const originY = diameter - delta

    circle.style.width = `${diameter}px`
    circle.style.height = `${diameter}px`
    circle.style.bottom = `-${delta}px`

    gsap.set(circle, {
      xPercent: -50,
      scale: 0,
      transformOrigin: `50% ${originY}px`
    })

    const label = pill.querySelector('.pill-label')
    const hoverLabel = pill.querySelector('.pill-label-hover')

    if (label) gsap.set(label, { y: 0 })
    if (hoverLabel) gsap.set(hoverLabel, { y: Math.ceil(height + 100), opacity: 0 })

    timelineRefs[index]?.kill()
    const timeline = gsap.timeline({ paused: true })

    timeline.to(circle, { scale: 1.2, xPercent: -50, duration: 2, ease: props.ease, overwrite: 'auto' }, 0)
    if (label) timeline.to(label, { y: -(height + 8), duration: 2, ease: props.ease, overwrite: 'auto' }, 0)
    if (hoverLabel) timeline.to(hoverLabel, { y: 0, opacity: 1, duration: 2, ease: props.ease, overwrite: 'auto' }, 0)

    timelineRefs[index] = timeline
  })
}

function handleEnter(index) {
  const timeline = timelineRefs[index]
  if (!timeline) return

  activeTweenRefs[index]?.kill()
  activeTweenRefs[index] = timeline.tweenTo(timeline.duration(), {
    duration: 0.3,
    ease: props.ease,
    overwrite: 'auto'
  })
}

function handleLeave(index) {
  const timeline = timelineRefs[index]
  if (!timeline) return

  activeTweenRefs[index]?.kill()
  activeTweenRefs[index] = timeline.tweenTo(0, {
    duration: 0.2,
    ease: props.ease,
    overwrite: 'auto'
  })
}

function handleLogoEnter() {
  const image = logoImgRef.value
  if (!image) return

  logoTweenRef.value?.kill()
  gsap.set(image, { rotate: 0 })
  logoTweenRef.value = gsap.to(image, {
    rotate: 360,
    duration: 0.2,
    ease: props.ease,
    overwrite: 'auto'
  })
}

function toggleMobileMenu() {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
  animateMobileMenu()
  emit('mobile-menu-click')
}

function closeMobileMenu() {
  if (!isMobileMenuOpen.value) return
  isMobileMenuOpen.value = false
  animateMobileMenu()
}

function animateMobileMenu() {
  const hamburger = hamburgerRef.value
  const menu = mobileMenuRef.value

  if (hamburger) {
    const lines = hamburger.querySelectorAll('.hamburger-line')
    gsap.to(lines[0], { rotation: isMobileMenuOpen.value ? 45 : 0, y: isMobileMenuOpen.value ? 3 : 0, duration: 0.3, ease: props.ease })
    gsap.to(lines[1], { rotation: isMobileMenuOpen.value ? -45 : 0, y: isMobileMenuOpen.value ? -3 : 0, duration: 0.3, ease: props.ease })
  }

  if (!menu) return

  if (isMobileMenuOpen.value) {
    gsap.set(menu, { visibility: 'visible' })
    gsap.fromTo(
      menu,
      { opacity: 0, y: 10 },
      { opacity: 1, y: 0, duration: 0.3, ease: props.ease, transformOrigin: 'top center' }
    )
  } else {
    gsap.to(menu, {
      opacity: 0,
      y: 10,
      duration: 0.2,
      ease: props.ease,
      transformOrigin: 'top center',
      onComplete: () => gsap.set(menu, { visibility: 'hidden' })
    })
  }
}

function runInitialAnimation() {
  if (!props.initialLoadAnimation) return

  if (logoRef.value) {
    gsap.set(logoRef.value, { scale: 0 })
    gsap.to(logoRef.value, { scale: 1, duration: 0.6, ease: props.ease })
  }

  if (navItemsRef.value) {
    gsap.set(navItemsRef.value, { width: 0, overflow: 'hidden' })
    gsap.to(navItemsRef.value, { width: 'auto', duration: 0.6, ease: props.ease, clearProps: 'overflow' })
  }
}

onMounted(async () => {
  await nextTick()
  layoutPills()
  gsap.set(mobileMenuRef.value, { visibility: 'hidden', opacity: 0 })
  runInitialAnimation()

  window.addEventListener('resize', layoutPills)
  document.fonts?.ready?.then(layoutPills).catch(() => {})
})

onUnmounted(() => {
  window.removeEventListener('resize', layoutPills)
  timelineRefs.forEach((timeline) => timeline?.kill())
  activeTweenRefs.forEach((tween) => tween?.kill())
  logoTweenRef.value?.kill()
})

watch(
  () => [props.items, props.ease],
  async () => {
    await nextTick()
    layoutPills()
  },
  { deep: true }
)
</script>

<style scoped>
.pill-nav-container {
  position: relative;
  z-index: 4;
}

.pill-nav {
  --nav-h: 42px;
  --logo: 36px;
  --pill-pad-x: 22px;
  --pill-gap: 4px;
  display: flex;
  align-items: center;
  width: max-content;
  box-sizing: border-box;
}

.pill-nav-items {
  position: relative;
  display: flex;
  align-items: center;
  height: var(--nav-h);
  border-radius: 9999px;
  background: var(--base, #fff);
}

.pill-logo {
  display: inline-flex;
  width: var(--nav-h);
  height: var(--nav-h);
  margin-right: 8px;
  padding: 8px;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 50%;
  background: var(--base, #fff);
}

.pill-logo img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pill-list {
  display: flex;
  height: 100%;
  margin: 0;
  padding: 3px;
  align-items: stretch;
  gap: var(--pill-gap);
  list-style: none;
}

.pill-list > li {
  display: flex;
  height: 100%;
}

.pill {
  position: relative;
  display: inline-flex;
  height: 100%;
  padding: 0 var(--pill-pad-x);
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 9999px;
  background: var(--pill-bg, #120f17);
  color: var(--pill-text, #fff);
  box-sizing: border-box;
  cursor: pointer;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.06em;
  line-height: 1;
  text-decoration: none;
  text-transform: uppercase;
  white-space: nowrap;
}

.hover-circle {
  position: absolute;
  left: 50%;
  bottom: 0;
  z-index: 1;
  display: block;
  border-radius: 50%;
  background: var(--base, #fff);
  pointer-events: none;
  will-change: transform;
}

.label-stack {
  position: relative;
  z-index: 2;
  display: inline-block;
  line-height: 1;
}

.pill-label,
.pill-label-hover {
  display: inline-block;
  line-height: 1;
  will-change: transform, opacity;
}

.pill-label {
  position: relative;
  z-index: 2;
}

.pill-label-hover {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 3;
  color: var(--hover-text, #120f17);
}

.pill.is-active::after {
  content: "";
  position: absolute;
  bottom: 3px;
  left: 50%;
  z-index: 4;
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: var(--base, #fff);
  transform: translateX(-50%);
}

.desktop-only {
  display: block;
}

.mobile-only {
  display: none;
}

.mobile-menu-button {
  position: relative;
  display: none;
  width: var(--nav-h);
  height: var(--nav-h);
  padding: 0;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: none;
  border-radius: 50%;
  background: var(--base, #fff);
  cursor: pointer;
}

.hamburger-line {
  width: 16px;
  height: 2px;
  border-radius: 1px;
  background: var(--pill-bg, #120f17);
  transform-origin: center;
}

.mobile-menu-popover {
  position: absolute;
  top: 52px;
  right: 0;
  left: 0;
  z-index: 5;
  visibility: hidden;
  border-radius: 27px;
  background: var(--base, #fff);
  box-shadow: 0 12px 34px rgba(0, 0, 0, 0.2);
  opacity: 0;
}

.mobile-menu-list {
  display: flex;
  margin: 0;
  padding: 3px;
  flex-direction: column;
  gap: 3px;
  list-style: none;
}

.mobile-menu-link {
  display: block;
  padding: 12px 16px;
  border-radius: 50px;
  background: var(--pill-bg, #120f17);
  color: var(--pill-text, #fff);
  font-size: 16px;
  font-weight: 650;
  text-decoration: none;
}

.mobile-menu-link:hover,
.mobile-menu-link.is-active {
  background: var(--base, #fff);
  color: var(--hover-text, #120f17);
}

@media (max-width: 768px) {
  .pill-nav-container,
  .pill-nav {
    width: 100%;
  }

  .pill-nav {
    justify-content: center;
  }

  .desktop-only {
    display: none;
  }

  .mobile-only,
  .mobile-menu-button {
    display: flex;
  }
}
</style>
