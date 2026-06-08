<template>
    <section class="carousel-wrapper" aria-label="健康洞察轮播" @mouseenter="pauseAutoPlay"
        @mouseleave="startAutoPlay">
        <div class="carousel-viewport">
            <div class="carousel-track">
                <article v-for="(item, index) in items" :key="item.id" class="carousel-item"
                    :class="{ 'is-active': currentIndex === index }" :style="getItemStyle(index)" @click="goTo(index)">
                    <div class="insight-card">
                        <div class="card-topline">
                            <span class="icon-box" v-html="item.icon"></span>
                            <span class="item-title">{{ item.title }}</span>
                        </div>

                        <div class="card-content">
                            <strong class="item-value">{{ item.value }}</strong>
                            <p class="item-description">{{ item.description }}</p>
                        </div>
                    </div>
                </article>
            </div>
        </div>

        <div class="carousel-indicators" aria-label="轮播指示器">
            <button v-for="item in items" :key="`indicator-${item.id}`" type="button" class="dot"
                :class="{ active: currentIndex === item.index }" :aria-label="`查看${item.title}`"
                :aria-pressed="currentIndex === item.index" @click="goTo(item.index)"></button>
        </div>
    </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
    todayCalories: { type: Number, default: 0 },
    todayDuration: { type: Number, default: 0 },
    weeklyCount: { type: Number, default: 0 },
    weeklyCalories: { type: Number, default: 0 },
    calorieBalance: { type: Number, default: 0 }
})

const items = computed(() => [
    {
        id: 'load',
        index: 0,
        title: '今日训练',
        value: props.todayDuration > 0 ? `${props.todayDuration} min` : '未开始',
        description: props.todayDuration > 0
            ? `已消耗 ${props.todayCalories} kcal，${props.todayDuration >= 60 ? '训练量充足' : '可以再加一组'}`
            : '今天还没有训练记录，开始动起来吧。',
        icon: '<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>'
    },
    {
        id: 'weekly',
        index: 1,
        title: '本周训练',
        value: `${props.weeklyCount} 次`,
        description: props.weeklyCount >= 4
            ? `本周已消耗 ${props.weeklyCalories} kcal，训练频率优秀。`
            : `本周已消耗 ${props.weeklyCalories} kcal，建议再训练 ${Math.max(0, 4 - props.weeklyCount)} 次。`,
        icon: '<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>'
    },
    {
        id: 'balance',
        index: 2,
        title: '热量平衡',
        value: `${Math.abs(props.calorieBalance)} kcal`,
        description: props.calorieBalance > 0
            ? `今日热量盈余 ${props.calorieBalance} kcal，注意控制摄入。`
            : props.calorieBalance < 0
                ? `今日热量缺口 ${Math.abs(props.calorieBalance)} kcal，减脂效果良好。`
                : '今日热量收支平衡。',
        icon: '<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v4"/><path d="M12 18v4"/><path d="m4.93 4.93 2.83 2.83"/><path d="m16.24 16.24 2.83 2.83"/><path d="M2 12h4"/><path d="M18 12h4"/><path d="m4.93 19.07 2.83-2.83"/><path d="m16.24 7.76 2.83-2.83"/><circle cx="12" cy="12" r="3"/></svg>'
    }
])

const currentIndex = ref(0)
let autoPlayTimer = null

const goTo = (index) => {
    currentIndex.value = index
}

const next = () => {
    currentIndex.value = (currentIndex.value + 1) % items.value.length
}

const pauseAutoPlay = () => {
    if (autoPlayTimer) {
        clearInterval(autoPlayTimer)
        autoPlayTimer = null
    }
}

const startAutoPlay = () => {
    pauseAutoPlay()
    autoPlayTimer = setInterval(next, 3200)
}

const getItemStyle = (index) => {
    const total = items.value.length
    let offset = index - currentIndex.value

    if (offset > total / 2) offset -= total
    if (offset < -total / 2) offset += total

    const abs = Math.abs(offset)
    const isActive = offset === 0
    const transform = isActive
        ? 'translate3d(0, 0, 0) rotateY(0deg) scale(1)'
        : `translate3d(${offset * 58}%, 8px, -120px) rotateY(${offset * -12}deg) scale(0.84)`

    return {
        transform,
        opacity: isActive ? 1 : 0.28,
        zIndex: 20 - abs,
        filter: isActive ? 'blur(0)' : 'blur(0.4px)'
    }
}

onMounted(startAutoPlay)
onBeforeUnmount(pauseAutoPlay)
</script>

<style scoped>
.carousel-wrapper {
    position: relative;
    width: 100%;
    min-height: 178px;
    padding: 2px 0 0;
    overflow: visible;
}

.carousel-viewport {
    position: relative;
    width: 100%;
    height: 140px;
    perspective: 1120px;
    perspective-origin: 50% 48%;
    overflow: visible;
}

.carousel-track {
    position: relative;
    width: 100%;
    height: 100%;
    transform-style: preserve-3d;
    overflow: visible;
}

.carousel-item {
    position: absolute;
    inset: 0;
    width: 68%;
    max-width: 330px;
    height: 128px;
    margin: auto;
    cursor: pointer;
    transform-origin: center center;
    transform-style: preserve-3d;
    backface-visibility: hidden;
    transition:
        transform 0.68s cubic-bezier(0.22, 1, 0.36, 1),
        opacity 0.42s ease,
        filter 0.42s ease;
    will-change: transform, opacity, filter;
}

.carousel-item.is-active {
    cursor: default;
}

.insight-card {
    position: relative;
    display: flex;
    height: 100%;
    flex-direction: column;
    justify-content: space-between;
    box-sizing: border-box;
    padding: 14px;
    color: var(--text-primary);
    background: #FFFFFF;
    border: 1px solid #E8ECF1;
    border-radius: 24px;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
    overflow: hidden;
}

.card-topline,
.card-content {
    position: relative;
    z-index: 1;
}

.card-topline {
    display: flex;
    min-width: 0;
    align-items: center;
    gap: 11px;
}

.icon-box {
    display: grid;
    width: 30px;
    height: 30px;
    flex: 0 0 30px;
    place-items: center;
    color: #7EB8DA;
    background: rgba(126, 184, 218, 0.1);
    border: 1px solid transparent;
    border-radius: 14px;
}

.item-title {
    min-width: 0;
    overflow: hidden;
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 720;
    line-height: 1.2;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.item-value {
    display: block;
    margin-bottom: 8px;
    color: var(--text-primary);
    font-size: 22px;
    font-weight: 820;
    line-height: 1;
}

.item-description {
    display: -webkit-box;
    margin: 0;
    overflow: hidden;
    color: var(--text-secondary);
    font-size: 11.5px;
    line-height: 1.45;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

.carousel-indicators {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-top: 13px;
}

.dot {
    width: 7px;
    height: 7px;
    padding: 0;
    background: #E8ECF1;
    border: 0;
    border-radius: 999px;
    cursor: pointer;
    transition:
        width 0.28s ease,
        background 0.28s ease,
        box-shadow 0.28s ease;
}

.dot.active {
    width: 26px;
    background: #7EB8DA;
    box-shadow: 0 2px 8px rgba(126, 184, 218, 0.3);
}

.dot:focus-visible {
    outline: 2px solid rgba(126, 184, 218, 0.6);
    outline-offset: 3px;
}

@media (max-width: 720px) {
    .carousel-wrapper {
        min-height: 218px;
    }

    .carousel-item {
        width: 76%;
    }

    .insight-card {
        padding: 17px;
    }

    .item-value {
        font-size: 26px;
    }
}

@media (prefers-reduced-motion: reduce) {
    .carousel-item {
        transition: none;
    }
}
</style>
