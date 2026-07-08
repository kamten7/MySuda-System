import { ref, watchEffect } from 'vue'

const THEME_KEY = 'suda-theme'
const isDark = ref(false)

export function useTheme() {
  // Initialize from localStorage
  const stored = localStorage.getItem(THEME_KEY)
  if (stored === 'dark') {
    isDark.value = true
    document.documentElement.classList.add('dark')
  }

  function toggleTheme() {
    isDark.value = !isDark.value
    document.documentElement.classList.toggle('dark', isDark.value)
    localStorage.setItem(THEME_KEY, isDark.value ? 'dark' : 'light')
  }

  function setTheme(theme: 'light' | 'dark') {
    isDark.value = theme === 'dark'
    document.documentElement.classList.toggle('dark', isDark.value)
    localStorage.setItem(THEME_KEY, theme)
  }

  return {
    isDark,
    toggleTheme,
    setTheme,
  }
}
