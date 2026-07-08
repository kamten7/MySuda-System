<template>
  <div class="app-navbar">
    <div class="navbar-left">
      <!-- Hamburger toggle -->
      <AppHamburger
        :is-active="appStore.sidebar.opened"
        @toggle="appStore.toggleSidebar"
      />

      <!-- Brand text -->
      <div class="navbar-brand">
        <div class="navbar-brand-icon">速</div>
        <span class="navbar-brand-text">速达外卖管理系统</span>
      </div>
    </div>

    <div class="navbar-right">
      <!-- Shop status badge -->
      <div class="shop-status-badge" @click="showShopStatusDialog = true">
        <span class="status-indicator" :class="shopStatus === 1 ? 'open' : 'closed'" />
        <span class="status-text">{{ shopStatus === 1 ? '营业中' : '打烊中' }}</span>
      </div>

      <!-- Theme toggle -->
      <el-tooltip :content="isDark ? '切换亮色模式' : '切换暗色模式'" placement="bottom">
        <span class="navbar-action-btn" @click="appStore.toggleTheme">
          <el-icon :size="18">
            <Sunny v-if="isDark" />
            <Moon v-else />
          </el-icon>
        </span>
      </el-tooltip>

      <!-- User dropdown -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" icon="UserFilled" />
          <span class="username">{{ displayName }}</span>
          <el-icon class="arrow-icon"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="password">
              <el-icon><Lock /></el-icon>修改密码
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- Shop status dialog -->
    <ShopStatusDialog
      v-model:visible="showShopStatusDialog"
      :current-status="shopStatus"
      @success="fetchShopStatus"
    />

    <!-- Password dialog -->
    <PasswordDialog v-model:visible="showPasswordDialog" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { getShopStatus } from '@/api/modules/shop'
import { getUserInfo, getUsername } from '@/utils/cookies'
import AppHamburger from './AppHamburger.vue'
import ShopStatusDialog from './ShopStatusDialog.vue'
import PasswordDialog from './PasswordDialog.vue'

const appStore = useAppStore()
const userStore = useUserStore()

const showShopStatusDialog = ref(false)
const showPasswordDialog = ref(false)
const shopStatus = ref<number>(1)

const isDark = computed(() => appStore.theme === 'dark')

const displayName = computed(() => {
  if (userStore.userInfo?.name) return userStore.userInfo.name
  const info = getUserInfo()
  if (info?.name) return info.name as string
  return getUsername() || '管理员'
})

async function fetchShopStatus() {
  try {
    const response = await getShopStatus()
    if (response.data.code === 1) {
      shopStatus.value = response.data.data as number
    }
  } catch {
    // Use default status
  }
}

function handleCommand(command: string) {
  if (command === 'password') {
    showPasswordDialog.value = true
  } else if (command === 'logout') {
    userStore.logout()
  }
}

onMounted(() => {
  fetchShopStatus()
})
</script>

<style lang="scss" scoped>
.app-navbar {
  height: 56px;
  background: linear-gradient(90deg, #1e3a8a 0%, #1a56db 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
  z-index: 1000;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 4px;
}

.navbar-brand-icon {
  width: 30px;
  height: 30px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.navbar-brand-text {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shop-status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  cursor: pointer;
  transition: background 0.2s;
  border: 1px solid rgba(255, 255, 255, 0.2);

  &:hover {
    background: rgba(255, 255, 255, 0.25);
  }
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.open {
    background-color: #4caf50;
    box-shadow: 0 0 6px rgba(76, 175, 80, 0.6);
  }

  &.closed {
    background-color: #9e9e9e;
  }
}

.status-text {
  font-size: 12px;
  color: #fff;
  font-weight: 500;
}

.navbar-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.15);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  color: #fff;

  &:hover {
    background: rgba(255, 255, 255, 0.15);
  }
}

.username {
  font-size: 14px;
  font-weight: 500;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  font-size: 12px;
  transition: transform 0.2s;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-avatar) {
  background-color: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 16px;
}
</style>
