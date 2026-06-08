import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import * as echarts from 'echarts'
import App from './App.vue'
import router from './router'
import { lightChartTheme } from '@/utils/echartsTheme'

import 'element-plus/dist/index.css'
import '@/assets/styles/reset.css'
import '@/assets/styles/element-overrides.css'

// 注册 ECharts 浅色主题
echarts.registerTheme('light', lightChartTheme)

createApp(App).use(router).use(ElementPlus).mount('#app')
