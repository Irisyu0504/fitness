import { createRouter, createWebHistory } from 'vue-router'
import AuthLandingView from '../views/AuthLandingView.vue'
import AuthView from '../views/AuthView.vue'
import DashboardView from '../views/DashboardView.vue'
import GoalsView from '../views/GoalsView.vue'
import ProfileView from '../views/ProfileView.vue'
import BodyRecordView from '../views/BodyRecordView.vue'
import ExerciseListView from '../views/ExerciseListView.vue'
import TrainingPlanView from '../views/TrainingPlanView.vue'
import DietRecordView from '../views/DietRecordView.vue'
import WorkoutRecordView from '../views/WorkoutRecordView.vue'
import MembershipView from '../views/MembershipView.vue'
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminOverview from '../views/admin/AdminOverview.vue'
import AdminUsersView from '../views/admin/AdminUsersView.vue'
import AdminExercisesView from '../views/admin/AdminExercisesView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: AuthLandingView,
      meta: { transition: 'fade' }
    },
    {
      path: '/auth',
      name: 'auth',
      component: AuthView,
      meta: { transition: 'fade' }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true, transition: 'slide-up' }
    },
    {
      path: '/goals',
      name: 'goals',
      component: GoalsView,
      meta: { requiresAuth: true }
    },
    {
      path: '/body',
      name: 'body',
      component: BodyRecordView,
      meta: { requiresAuth: true }
    },
    {
      path: '/exercises',
      name: 'exercises',
      component: ExerciseListView,
      meta: { requiresAuth: true }
    },
    {
      path: '/plans',
      name: 'plans',
      component: TrainingPlanView,
      meta: { requiresAuth: true }
    },
    {
      path: '/diets',
      name: 'diets',
      component: DietRecordView,
      meta: { requiresAuth: true }
    },
    {
      path: '/records',
      name: 'records',
      component: WorkoutRecordView,
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
      meta: { requiresAuth: true }
    },
    {
      path: '/membership',
      name: 'membership',
      component: MembershipView,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'admin-overview', component: AdminOverview },
        { path: 'users', name: 'admin-users', component: AdminUsersView },
        { path: 'exercises', name: 'admin-exercises', component: AdminExercisesView }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return '/auth'
  }
})

export default router
