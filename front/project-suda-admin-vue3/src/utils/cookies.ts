import Cookies from 'js-cookie'

const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'user_info'
const SIDEBAR_KEY = 'sidebar_status'
const USERNAME_KEY = 'username'

// Token
export function getToken(): string {
  return Cookies.get(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  Cookies.set(TOKEN_KEY, token)
}

export function removeToken(): void {
  Cookies.remove(TOKEN_KEY)
}

// User Info
export function getUserInfo(): Record<string, unknown> | null {
  const info = Cookies.get(USER_INFO_KEY)
  if (!info) return null
  try {
    return JSON.parse(info)
  } catch {
    return null
  }
}

export function setUserInfo(info: Record<string, unknown>): void {
  Cookies.set(USER_INFO_KEY, JSON.stringify(info))
}

export function removeUserInfo(): void {
  Cookies.remove(USER_INFO_KEY)
}

// Sidebar Status
export function getSidebarStatus(): string {
  return Cookies.get(SIDEBAR_KEY) || 'opened'
}

export function setSidebarStatus(status: string): void {
  Cookies.set(SIDEBAR_KEY, status)
}

// Username
export function getUsername(): string {
  return Cookies.get(USERNAME_KEY) || ''
}

export function setUsername(name: string): void {
  Cookies.set(USERNAME_KEY, name)
}

export function removeUsername(): void {
  Cookies.remove(USERNAME_KEY)
}

// Clear all auth cookies
export function clearAuthCookies(): void {
  removeToken()
  removeUserInfo()
  removeUsername()
}
