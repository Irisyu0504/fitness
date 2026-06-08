<template>
    <nav class="floating-sidebar">
        <div class="sidebar-inner">
            <router-link v-if="isAdmin" to="/admin" class="nav-item admin-entry" title="管理后台">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"
                    stroke-linejoin="round">
                    <path d="M12 2L2 7l10 5 10-5-10-5z M2 17l10 5 10-5 M2 12l10 5 10-5" />
                </svg>
            </router-link>
            <router-link v-for="(item, index) in navItems" :key="index" :to="item.path" class="nav-item"
                :title="item.label" active-class="is-active">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"
                    stroke-linejoin="round">
                    <path :d="item.icon" />
                </svg>
            </router-link>
        </div>
    </nav>
</template>

<script setup>
import { ref, computed } from 'vue'

const isAdmin = computed(() => {
    try {
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        return user.role === 'admin'
    } catch {
        return false
    }
})

const navItems = ref([
    { path: '/dashboard', label: '仪表盘', icon: 'M3 3h7v7H3z M14 3h7v7h-7z M14 14h7v7h-7z M3 14h7v7H3z' },
    { path: '/goals', label: '目标', icon: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z M12 16a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M12 2v2 M12 20v2 M2 12h2 M20 12h2' },
    { path: '/body', label: '身体数据', icon: 'M22 12h-4l-3 9L9 3l-3 9H2' },
    { path: '/exercises', label: '健身动作', icon: 'M6.5 6.5l11 11 M3 14l7 7 M14 3l7 7 M4.5 10.5l-2 2 M10.5 4.5l2-2 M19.5 13.5l2-2 M13.5 19.5l-2 2' },
    { path: '/plans', label: '训练计划', icon: 'M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2 M12 11h4 M12 16h4 M8 11h.01 M8 16h.01 M8 2v4 M16 2v4' },
    { path: '/diets', label: '饮食记录', icon: 'M12 21a9.002 9.002 0 0 0 8.716-6.747A4.5 4.5 0 0 0 12 9.5a4.5 4.5 0 0 0-8.716 4.753A9.002 9.002 0 0 0 12 21z M12 9.5V2 M9 5h6' },
    { path: '/records', label: '训练记录', icon: 'M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2 M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2 M9 5h6 M9 14l2 2 4-4' },
    { path: '/profile', label: '用户中心', icon: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z' }
])
</script>

<style scoped>
.floating-sidebar {
    position: fixed;
    left: 20px;
    top: 50%;
    transform: translateY(-50%);
    z-index: 100;
    width: 56px;
    border-radius: 999px;
    background: #FFFFFF;
    border: 1px solid #E8ECF1;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
}

.sidebar-inner {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24px 0;
    gap: 20px;
}

.nav-item {
    width: 32px;
    height: 32px;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 50%;
    color: #A0AEC0;
    transition: all 0.3s cubic-bezier(0.25, 1, 0.5, 1);
    text-decoration: none;
    background: transparent;
}

.nav-item svg {
    width: 18px;
    height: 18px;
    transition: all 0.3s ease;
}

.nav-item:hover {
    background: #F5F7FA;
    color: #7EB8DA;
    transform: scale(1.06);
}

.nav-item.is-active {
    background: #7EB8DA;
    color: #FFFFFF;
    box-shadow: 0 2px 8px rgba(126, 184, 218, 0.3);
    transform: scale(1);
}

.nav-item.admin-entry {
    background: rgba(126, 184, 218, 0.15);
    color: #7EB8DA;
}

.nav-item.admin-entry:hover {
    background: #7EB8DA;
    color: #FFFFFF;
}
</style>
