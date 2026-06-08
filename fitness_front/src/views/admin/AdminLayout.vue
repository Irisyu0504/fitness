<template>
  <main class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <span class="admin-logo">A</span>
        <h2>管理后台</h2>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/admin" class="nav-item" exact-active-class="active">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>
          </svg>
          <span>数据概览</span>
        </router-link>
        <router-link to="/admin/users" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          <span>用户管理</span>
        </router-link>
        <router-link to="/admin/exercises" class="nav-item" active-class="active">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6.5 6.5l11 11 M3 14l7 7 M14 3l7 7"/>
          </svg>
          <span>动作库管理</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <router-link to="/dashboard" class="nav-item back">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 12H5 M12 19l-7-7 7-7"/>
          </svg>
          <span>返回前台</span>
        </router-link>
      </div>
    </aside>

    <div class="admin-content">
      <router-view />
    </div>
  </main>
</template>

<script setup>
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #F8F9FA;
}

/* ── 侧边栏 ── */
.admin-sidebar {
  width: 220px;
  background: #FFFFFF;
  border-right: 1px solid #E8ECF1;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 100;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 24px 20px;
  border-bottom: 1px solid #E8ECF1;
}

.admin-logo {
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  background: #7EB8DA;
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 800;
  border-radius: 10px;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 12px;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background: #F5F7FA;
  color: var(--text-primary);
}

.nav-item.active {
  background: rgba(126, 184, 218, 0.1);
  color: #7EB8DA;
}

.nav-item.active svg {
  stroke: #7EB8DA;
}

.sidebar-footer {
  padding: 12px 10px;
  border-top: 1px solid #E8ECF1;
}

.nav-item.back {
  color: var(--text-tertiary);
  font-size: 13px;
}

/* ── 内容区 ── */
.admin-content {
  flex: 1;
  margin-left: 220px;
  padding: 32px 36px;
  min-height: 100vh;
}

@media (max-width: 768px) {
  .admin-sidebar {
    width: 60px;
  }

  .sidebar-header h2,
  .sidebar-nav span,
  .sidebar-footer span {
    display: none;
  }

  .sidebar-header {
    justify-content: center;
    padding: 20px 0;
  }

  .nav-item {
    justify-content: center;
    padding: 10px;
  }

  .admin-content {
    margin-left: 60px;
    padding: 24px 16px;
  }
}
</style>
