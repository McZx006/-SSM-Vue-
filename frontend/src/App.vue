<template>
  <router-view />
</template>

<script setup>
import { onBeforeUnmount, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const applyTheme = () => {
  const darkMode = localStorage.getItem('darkMode') === 'true'
  document.body.classList.toggle('dark-theme', darkMode)
}

onMounted(() => {
  applyTheme()
  window.addEventListener('theme-changed', applyTheme)
  userStore.loadUserFromStorage()
})

onBeforeUnmount(() => {
  window.removeEventListener('theme-changed', applyTheme)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  --app-bg: #f5f7fa;
  --app-text: #1f2937;
  --app-muted: #64748b;
  --app-card: #ffffff;
  --app-soft-card: #f8fafc;
  --app-border: rgba(148, 163, 184, 0.18);
  --app-primary: #0f766e;
  --app-primary-soft: #ecfdf5;
  font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
  background: var(--app-bg);
  color: var(--app-text);
  transition: background 0.2s ease, color 0.2s ease;
}

body.dark-theme {
  --app-bg: #0f172a;
  --app-text: #e5eefb;
  --app-muted: #94a3b8;
  --app-card: #172033;
  --app-soft-card: #1e293b;
  --app-border: rgba(148, 163, 184, 0.22);
  --app-primary: #2dd4bf;
  --app-primary-soft: rgba(45, 212, 191, 0.12);
  color-scheme: dark;
}

#app {
  min-height: 100vh;
}

body.dark-theme .el-card,
body.dark-theme .el-dialog,
body.dark-theme .el-table,
body.dark-theme .el-table__inner-wrapper,
body.dark-theme .el-picker-panel,
body.dark-theme .el-popper,
body.dark-theme .el-select-dropdown,
body.dark-theme .el-dropdown-menu {
  background: var(--app-card);
  color: var(--app-text);
  border-color: var(--app-border);
}

body.dark-theme .el-card__header,
body.dark-theme .el-table th.el-table__cell,
body.dark-theme .el-table tr,
body.dark-theme .el-table__row,
body.dark-theme .el-dialog__header,
body.dark-theme .el-dialog__footer {
  background: var(--app-card);
  color: var(--app-text);
}

body.dark-theme .el-input__wrapper,
body.dark-theme .el-textarea__inner,
body.dark-theme .el-input-number,
body.dark-theme .el-date-editor,
body.dark-theme .el-select__wrapper {
  background: var(--app-soft-card);
  border-color: var(--app-border);
  box-shadow: 0 0 0 1px var(--app-border) inset;
}

body.dark-theme .el-input__inner,
body.dark-theme .el-textarea__inner,
body.dark-theme .el-select__placeholder,
body.dark-theme .el-form-item__label,
body.dark-theme .el-dialog__title,
body.dark-theme .el-table {
  color: var(--app-text);
}

body.dark-theme .el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell {
  background: rgba(45, 212, 191, 0.08);
}

body.dark-theme .el-menu,
body.dark-theme .el-menu-item {
  background: transparent;
  color: var(--app-text);
}

body.dark-theme .el-menu-item.is-active,
body.dark-theme .el-radio-button__original-radio:checked + .el-radio-button__inner {
  color: #0f172a;
  background: #5eead4;
  border-color: #5eead4;
}
</style>
