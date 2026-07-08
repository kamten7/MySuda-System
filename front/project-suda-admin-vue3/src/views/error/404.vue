<template>
  <div class="not-found-page">
    <!-- Decorative floating shapes (CSS only) -->
    <div class="decorations" aria-hidden="true">
      <div class="deco deco-circle deco-circle--1"></div>
      <div class="deco deco-circle deco-circle--2"></div>
      <div class="deco deco-circle deco-circle--3"></div>
      <div class="deco deco-dots deco-dots--1"></div>
      <div class="deco deco-dots deco-dots--2"></div>
      <div class="deco deco-ring deco-ring--1"></div>
      <div class="deco deco-wave deco-wave--1"></div>
    </div>

    <!-- Main content -->
    <div class="not-found-content">
      <!-- 404 Number -->
      <h1 class="not-found-number" aria-label="404">404</h1>

      <!-- Illustration -->
      <div class="not-found-illustration" aria-hidden="true">
        <svg
          width="240"
          height="160"
          viewBox="0 0 240 160"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <!-- Map/compass body -->
          <rect x="70" y="30" width="100" height="70" rx="12" fill="#f5f5f5" stroke="#e0e0e0" stroke-width="2" />
          <!-- Blue pin -->
          <circle cx="120" cy="58" r="14" fill="#1a56db" opacity="0.15" />
          <circle cx="120" cy="58" r="8" fill="#1a56db" />
          <circle cx="120" cy="58" r="3" fill="#fff" />
          <!-- Pin line -->
          <line x1="120" y1="66" x2="120" y2="82" stroke="#1a56db" stroke-width="2" stroke-dasharray="4 3" />
          <!-- Scattered dots -->
          <circle cx="90" cy="45" r="2" fill="#ccc" />
          <circle cx="150" cy="50" r="2" fill="#ccc" />
          <circle cx="105" cy="72" r="2" fill="#ccc" />
          <circle cx="140" cy="70" r="2" fill="#ccc" />
          <!-- Question marks -->
          <text x="190" y="55" font-size="22" fill="#ccc" font-family="serif">?</text>
          <text x="55" y="52" font-size="22" fill="#ccc" font-family="serif">?</text>
          <!-- Fork in road lines -->
          <line x1="30" y1="115" x2="75" y2="105" stroke="#e0e0e0" stroke-width="1.5" />
          <line x1="75" y1="105" x2="100" y2="95" stroke="#e0e0e0" stroke-width="1.5" />
          <line x1="75" y1="105" x2="85" y2="118" stroke="#e0e0e0" stroke-width="1.5" />
        </svg>
      </div>

      <!-- Message -->
      <h2 class="not-found-message">哎呀，页面走丢了！</h2>
      <p class="not-found-description">
        您要查找的页面不存在或已被移除，请检查访问地址是否正确
      </p>

      <!-- Action buttons -->
      <div class="not-found-actions">
        <el-button type="primary" size="large" round @click="goHome">
          <el-icon class="mr-8"><HomeFilled /></el-icon>
          返回首页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/dashboard')
}
</script>

<style scoped lang="scss">
// ==================== Page Layout ====================
.not-found-page {
  position: relative;
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eff6ff 0%, #fefefe 30%, #eff6ff 70%, #eff6ff 100%);
  overflow: hidden;
}

// ==================== Decorative Elements ====================
.decorations {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.deco {
  position: absolute;
}

// Circles
.deco-circle {
  border-radius: 50%;
  opacity: 0.12;

  &--1 {
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, #1a56db 0%, transparent 70%);
    top: -60px;
    right: -80px;
    animation: floatCircle1 12s ease-in-out infinite;
  }

  &--2 {
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, #f59e0b 0%, transparent 70%);
    bottom: -40px;
    left: -40px;
    opacity: 0.1;
    animation: floatCircle2 10s ease-in-out infinite;
  }

  &--3 {
    width: 150px;
    height: 150px;
    background: radial-gradient(circle, #1a56db 0%, transparent 70%);
    top: 40%;
    left: 10%;
    opacity: 0.06;
    animation: floatCircle3 14s ease-in-out infinite;
  }
}

// Dots grid
.deco-dots {
  width: 100px;
  height: 100px;
  opacity: 0.08;

  &--1 {
    top: 15%;
    right: 12%;
    background-image: radial-gradient(circle, #1a56db 1.5px, transparent 1.5px);
    background-size: 16px 16px;
    animation: floatDots1 16s ease-in-out infinite;
  }

  &--2 {
    bottom: 20%;
    right: 25%;
    background-image: radial-gradient(circle, #f59e0b 1.5px, transparent 1.5px);
    background-size: 14px 14px;
    animation: floatDots2 18s ease-in-out infinite;
  }
}

// Ring
.deco-ring {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 2px dashed rgba(26, 86, 219, 0.15);
  top: 20%;
  left: -20px;
  animation: spinSlow 20s linear infinite;
}

// Wave
.deco-wave {
  width: 500px;
  height: 500px;
  bottom: -250px;
  left: 50%;
  transform: translateX(-50%);
  background: radial-gradient(
    ellipse at center,
    rgba(26, 86, 219, 0.03) 0%,
    transparent 60%
  );
  border-radius: 40%;
  animation: waveDrift 15s ease-in-out infinite;
}

// ==================== Animations ====================
@keyframes floatCircle1 {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(-20px, 15px) scale(1.05);
  }
  50% {
    transform: translate(10px, -10px) scale(0.95);
  }
  75% {
    transform: translate(-15px, -20px) scale(1.02);
  }
}

@keyframes floatCircle2 {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(20px, -15px) scale(1.08);
  }
  66% {
    transform: translate(-10px, 20px) scale(0.93);
  }
}

@keyframes floatCircle3 {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(30px, -25px);
  }
}

@keyframes floatDots1 {
  0%,
  100% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(-20px, 20px) rotate(5deg);
  }
}

@keyframes floatDots2 {
  0%,
  100% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(25px, -15px) rotate(-3deg);
  }
}

@keyframes spinSlow {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes waveDrift {
  0%,
  100% {
    transform: translateX(-50%) scale(1) rotate(0deg);
    border-radius: 40%;
  }
  25% {
    transform: translateX(-48%) scale(1.1) rotate(3deg);
    border-radius: 43% 37% 45% 35%;
  }
  50% {
    transform: translateX(-52%) scale(1.05) rotate(-2deg);
    border-radius: 37% 43% 38% 42%;
  }
  75% {
    transform: translateX(-47%) scale(1.08) rotate(2deg);
    border-radius: 42% 38% 40% 36%;
  }
}

// ==================== Content ====================
.not-found-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 40px 20px;
  max-width: 480px;
}

// ==================== 404 Number ====================
.not-found-number {
  font-size: 140px;
  font-weight: 900;
  line-height: 1;
  margin: 0 0 8px;
  background: linear-gradient(135deg, #1a56db 0%, #3b82f6 40%, #f59e0b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
  animation: numberPulse 3s ease-in-out infinite;
  user-select: none;
}

@keyframes numberPulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.03);
  }
}

// ==================== Illustration ====================
.not-found-illustration {
  margin: 0 auto 24px;
  opacity: 0.9;
}

// ==================== Message ====================
.not-found-message {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px;
}

.not-found-description {
  font-size: 14px;
  color: #999;
  line-height: 1.8;
  margin: 0 0 32px;
  max-width: 360px;
  margin-left: auto;
  margin-right: auto;
}

// ==================== Actions ====================
.not-found-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

// ==================== Element Plus dark mode ====================
html.dark {
  .not-found-page {
    background: linear-gradient(135deg, #0f172a 0%, #1e293b 30%, #0f172a 70%, #1e293b 100%);
  }

  .not-found-message {
    color: #e5e5e5;
  }

  .not-found-description {
    color: #777;
  }
}

// ==================== Responsive ====================
@media (max-width: 480px) {
  .not-found-number {
    font-size: 100px;
  }

  .not-found-message {
    font-size: 18px;
  }
}
</style>
