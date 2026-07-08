<template>
  <div
    class="hamburger-container"
    :class="{ 'is-active': isActive }"
    @click="toggleClick"
  >
    <svg
      class="hamburger-icon"
      viewBox="0 0 20 20"
      width="20"
      height="20"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
    >
      <path
        class="top-line"
        d="M3 4h14"
        :class="{ active: isActive }"
      />
      <path
        class="middle-line"
        d="M3 10h14"
        :class="{ active: isActive }"
      />
      <path
        class="bottom-line"
        d="M3 16h14"
        :class="{ active: isActive }"
      />
    </svg>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  isActive: boolean
}>()

const emit = defineEmits<{
  toggle: []
}>()

function toggleClick() {
  emit('toggle')
}
</script>

<style lang="scss" scoped>
.hamburger-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
  -webkit-tap-highlight-color: transparent;
  color: #fff;

  &:hover {
    background: rgba(255, 255, 255, 0.15);
  }
}

.hamburger-icon {
  transition: transform 0.3s;

  path {
    transition:
      d 0.3s,
      opacity 0.3s;
    transform-origin: center;
  }

  .middle-line.active {
    opacity: 0;
  }

  .top-line.active {
    d: path('M4 4l12 12');
  }

  .bottom-line.active {
    d: path('M4 16l12-12');
  }
}
</style>
