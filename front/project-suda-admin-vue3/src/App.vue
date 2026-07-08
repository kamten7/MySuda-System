<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade-transform" mode="out-in">
      <component :is="Component" :key="route.name === 'Login' || route.name === 'NotFound' ? route.name : 'app'" />
    </transition>
  </router-view>
</template>

<script setup lang="ts">
// Root app component - layouts are handled by route-level components
// Key strategy: only remount when switching between Login/404 and the main app
// This prevents the entire layout from being destroyed on page navigation
</script>
