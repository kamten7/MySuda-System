import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/modules/auth'
import {
  getToken,
  setToken,
  removeToken,
  getUserInfo,
  setUserInfo,
  removeUserInfo,
  getUsername,
  setUsername,
  removeUsername,
} from '@/utils/cookies'
import router from '@/router'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'

export interface UserInfo {
  id: number
  userName: string
  name: string
  token: string
}

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string>(getToken())
  const userInfo = ref<UserInfo | null>(getUserInfo() as UserInfo | null)
  const username = ref<string>(getUsername())

  // Getters
  const isLoggedIn = computed(() => !!token.value)

  // Actions
  async function login(credentials: { username: string; password: string }) {
    try {
      const { data } = await loginApi({
        username: credentials.username.trim(),
        password: credentials.password,
      })

      if (data.code === 1 && data.data) {
        const info = data.data as UserInfo
        token.value = info.token
        userInfo.value = info
        username.value = info.userName

        setToken(info.token)
        setUserInfo({ ...info, token: info.token })
        setUsername(info.userName)

        ElMessage.success('登录成功')

        // Trigger sidebar entrance animation on first login
        const appStore = useAppStore()
        appStore.setFirstEnter(true)

        return data
      } else {
        ElMessage.error(data.msg || '登录失败')
        return null
      }
    } catch {
      return null
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      // Ignore logout API errors
    } finally {
      resetState()
      router.push('/login')
    }
  }

  function resetState() {
    token.value = ''
    userInfo.value = null
    username.value = ''
    removeToken()
    removeUserInfo()
    removeUsername()
  }

  function loadFromCookies() {
    const t = getToken()
    if (t) {
      token.value = t
      userInfo.value = getUserInfo() as UserInfo | null
      username.value = getUsername()
    }
  }

  return {
    token,
    userInfo,
    username,
    isLoggedIn,
    login,
    logout,
    resetState,
    loadFromCookies,
  }
})
