# fitness-auth-refactor

已按“页面归 views、视觉组件归 components、配置归 config、纯函数归 utils”的方式整理。

## 运行

```bash
npm install
npm run dev
```

## 主要结构

```text
src/
├── App.vue
├── main.js
├── router/index.js
├── assets/
│   ├── images/bg-gym-mountain.jpg
│   └── styles/reset.css
├── views/
│   ├── AuthLandingView.vue
│   ├── LoginView.vue
│   └── RegisterView.vue
├── components/landing/
│   ├── DecryptedText.vue
│   ├── DotField.vue
│   └── PillNav.vue
├── config/authLanding.config.js
└── utils/
    ├── scrambleText.js
    ├── dotGrid.js
    └── routeLink.js
```
