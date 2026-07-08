import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getSidebarStatus, setSidebarStatus } from '@/utils/cookies'

export const useAppStore = defineStore('app', () => {
  // State
  const sidebar = ref({
    opened: getSidebarStatus() !== 'closed',
    withoutAnimation: false,
  })
  const device = ref<'desktop' | 'mobile'>('desktop')
  const theme = ref<'light' | 'dark'>(
    (localStorage.getItem('suda-theme') as 'light' | 'dark') || 'light',
  )

  // First-enter flag for sidebar entrance animation
  const isFirstEnter = ref(false)

  // Initialize theme on load (persist across refreshes)
  if (theme.value === 'dark') {
    document.documentElement.classList.add('dark')
  }

  // Actions
  function toggleSidebar(withoutAnimation = false) {
    sidebar.value.opened = !sidebar.value.opened
    sidebar.value.withoutAnimation = withoutAnimation
    setSidebarStatus(sidebar.value.opened ? 'opened' : 'closed')
  }

  function closeSidebar(withoutAnimation = false) {
    sidebar.value.opened = false
    sidebar.value.withoutAnimation = withoutAnimation
    setSidebarStatus('closed')
  }

  function setDevice(d: 'desktop' | 'mobile') {
    device.value = d
  }

  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    document.documentElement.classList.toggle('dark', theme.value === 'dark')
    localStorage.setItem('suda-theme', theme.value)
  }

  function setFirstEnter(val: boolean) {
    isFirstEnter.value = val
  }

  function finishFirstEnter() {
    isFirstEnter.value = false
  }

  return {
    sidebar,
    device,
    theme,
    isFirstEnter,
    toggleSidebar,
    closeSidebar,
    setDevice,
    toggleTheme,
    setFirstEnter,
    finishFirstEnter,
  }
})
