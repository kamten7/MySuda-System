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
  clearAuthCookies,
} from './cookies'

export function isAuthenticated(): boolean {
  return !!getToken()
}

export function saveAuthData(token: string, userInfo: Record<string, unknown>): void {
  setToken(token)
  setUserInfo(userInfo)
  if (userInfo.userName) {
    setUsername(userInfo.userName as string)
  }
}

export function clearAuthData(): void {
  clearAuthCookies()
}

export { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo, getUsername, setUsername, removeUsername }
