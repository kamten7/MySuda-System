<template>
  <div class="login-page">
    <!-- 粒子波浪 Canvas 背景 -->
    <canvas ref="canvasRef" class="particle-canvas" />

    <!-- 毛玻璃登录卡片 -->
    <div class="login-card">
      <!-- Logo & Brand -->
      <div class="text-center mb-8">
        <div class="admin-logo-badge">
          <span class="admin-logo-char">速</span>
        </div>
        <h1 class="text-3xl font-bold text-[#1a56db] mb-2 tracking-wider">速达外卖</h1>
        <p class="text-sm text-gray-400 tracking-wide">极速送达 · 美味到家</p>
      </div>

      <!-- Login Form -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="账号"
            :prefix-icon="User"
            class="custom-input"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            class="custom-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="flex items-center justify-between mb-6">
          <el-checkbox v-model="rememberMe" class="remember-checkbox">
            <span class="text-sm text-gray-500">记住我</span>
          </el-checkbox>
          <a href="javascript:void(0)" class="text-sm text-[#1a56db] hover:underline">
            忘记密码？
          </a>
        </div>

        <el-button
          class="login-btn"
          :loading="loading"
          round
          @click="handleLogin"
        >
          <template v-if="!loading">立即登录</template>
          <template v-else>登录中...</template>
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' },
  ],
}

async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const result = await userStore.login({
        username: form.username,
        password: form.password,
      })
      if (result) router.push('/dashboard')
    } catch {
      ElMessage.error('登录失败，请检查账号和密码')
    } finally {
      loading.value = false
    }
  })
}

// ================================================================
// Canvas 粒子波浪：以登录卡片为波源，向外涟漪扩散，浅蓝→深蓝
// ================================================================
interface WaveParticle {
  angle: number; dist: number; speed: number; size: number
  opacity: number; phaseOffset: number; colorId: 0 | 1 | 2
}

const canvasRef = ref<HTMLCanvasElement | null>(null)
let animId = 0
let particles: WaveParticle[] = []
let elapsed = 0
let sx = 0; let sy = 0  // 卡片中心坐标

const CLR = [
  { r: 147, g: 197, b: 253 }, // 浅蓝
  { r: 96,  g: 165, b: 250 }, // 中蓝
  { r: 37,  g: 99,  b: 235 }, // 深蓝
]
const COUNT = 250
const MAX_D = 1000
const SPAWN = 4

function updateSrc() {
  const card = document.querySelector('.login-card') as HTMLElement | null
  if (card) { const r = card.getBoundingClientRect(); sx = r.left + r.width / 2; sy = r.top + r.height / 2 }
  else { sx = innerWidth / 2; sy = innerHeight / 2 }
}

function spawn(): WaveParticle {
  return {
    angle: Math.random() * Math.PI * 2,
    dist: 120 + Math.random() * 40,
    speed: 120 + Math.random() * 160,
    size: 3.5 + Math.random() * 5,
    opacity: 0.6 + Math.random() * 0.4,
    phaseOffset: Math.random() * Math.PI * 2,
    colorId: 0,
  }
}

function init() {
  updateSrc()
  particles = []
  for (let i = 0; i < COUNT; i++) {
    const p = spawn()
    p.dist = 120 + Math.random() * MAX_D
    p.colorId = p.dist < 300 ? 0 : p.dist < 600 ? 1 : 2
    particles.push(p)
  }
}

function draw(ctx: CanvasRenderingContext2D, w: number, h: number) {
  const dt = 0.016
  elapsed += dt
  ctx.fillStyle = '#060e1a'
  ctx.fillRect(0, 0, w, h)

  // 卡片光晕
  if (sx > 0) {
    const g = ctx.createRadialGradient(sx, sy, 60, sx, sy, 300)
    g.addColorStop(0, 'rgba(59,130,246,0.18)')
    g.addColorStop(0.4, 'rgba(59,130,246,0.06)')
    g.addColorStop(1, 'rgba(59,130,246,0)')
    ctx.fillStyle = g; ctx.fillRect(sx - 300, sy - 300, 600, 600)
  }

  // 补充粒子
  for (let i = 0; i < SPAWN; i++) {
    let done = false
    for (const p of particles) { if (p.dist > MAX_D) { Object.assign(p, spawn()); done = true; break } }
    if (!done && particles.length < COUNT + 30) particles.push(spawn())
  }

  // 绘制
  for (const p of particles) {
    const wave = Math.sin(elapsed * 2.5 + p.angle * 5 + p.phaseOffset) * 10
    p.dist += p.speed * dt + wave * dt * 2
    const x = sx + Math.cos(p.angle) * p.dist
    const y = sy + Math.sin(p.angle) * p.dist
    if (p.dist > MAX_D || x < -80 || x > w + 80 || y < -80 || y > h + 80) { Object.assign(p, spawn()); continue }
    const ratio = Math.min(1, p.dist / MAX_D)
    p.colorId = ratio < 0.35 ? 0 : ratio < 0.65 ? 1 : 2
    const c = CLR[p.colorId]
    const sz = p.size * (0.4 + ratio * 2.0)
    const alpha = p.opacity * (1 - ratio * 0.55)
    ctx.beginPath(); ctx.arc(x, y, sz, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(${c.r},${c.g},${c.b},${alpha.toFixed(3)})`; ctx.fill()
    if (ratio < 0.5) {
      ctx.beginPath(); ctx.arc(x, y, sz * 3, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(${c.r},${c.g},${c.b},${(alpha*0.12).toFixed(4)})`; ctx.fill()
    }
  }

  // 连线
  ctx.lineWidth = 0.5
  for (let i = 0; i < particles.length; i++) {
    const pi = particles[i]; if (pi.dist > 380) continue
    const xi = sx + Math.cos(pi.angle) * pi.dist; const yi = sy + Math.sin(pi.angle) * pi.dist
    for (let j = i + 1; j < particles.length; j++) {
      const pj = particles[j]; if (pj.dist > 380) continue
      const xj = sx + Math.cos(pj.angle) * pj.dist; const yj = sy + Math.sin(pj.angle) * pj.dist
      const dd = (xi - xj) ** 2 + (yi - yj) ** 2
      if (dd < 3600) {
        ctx.strokeStyle = `rgba(147,197,253,${(0.06 * (1 - Math.sqrt(dd) / 60)).toFixed(4)})`
        ctx.beginPath(); ctx.moveTo(xi, yi); ctx.lineTo(xj, yj); ctx.stroke()
      }
    }
  }
}

function loop() {
  const c = canvasRef.value
  if (!c) { animId = requestAnimationFrame(loop); return }
  const ctx = c.getContext('2d')
  if (!ctx) { animId = requestAnimationFrame(loop); return }
  const w = c.clientWidth; const h = c.clientHeight; const dpr = devicePixelRatio
  if (c.width !== w * dpr || c.height !== h * dpr) {
    c.width = w * dpr; c.height = h * dpr; ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  }
  updateSrc(); draw(ctx, w, h)
  animId = requestAnimationFrame(loop)
}

onMounted(() => {
  const c = canvasRef.value; if (!c) return
  const dpr = devicePixelRatio
  c.width = c.clientWidth * dpr; c.height = c.clientHeight * dpr
  c.getContext('2d')?.setTransform(dpr, 0, 0, dpr, 0, 0)
  init(); loop()
})
onBeforeUnmount(() => cancelAnimationFrame(animId))
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  overflow: hidden;
}

.particle-canvas {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%; z-index: 0;
}

/* Blue "速" logo badge */
.admin-logo-badge {
  width: 64px; height: 64px; margin: 0 auto 16px;
  background: linear-gradient(135deg, #1e3a8a, #2563eb, #3b82f6);
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 32px rgba(30, 58, 138, 0.4);
}
.admin-logo-char { font-size: 34px; font-weight: 800; color: #fff; line-height: 1; }

.login-card {
  position: relative; z-index: 10;
  width: 100%; max-width: 28rem;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  padding: 48px;
}

html.dark .login-card {
  background: rgba(30, 41, 59, 0.88);
}

.custom-input :deep(.el-input__wrapper) {
  background: #f5f7fa; border-radius: 10px; box-shadow: none; padding: 4px 14px; transition: all 0.3s ease;
}
html.dark .custom-input :deep(.el-input__wrapper) { background: #0f172a; border: 1px solid #334155; }
.custom-input :deep(.el-input__wrapper:hover) { background: #ebeef5; }
html.dark .custom-input :deep(.el-input__wrapper:hover) { background: #1e293b; }
.custom-input :deep(.el-input__wrapper.is-focus) { background: #ffffff; box-shadow: 0 0 0 1px #1a56db inset; }
html.dark .custom-input :deep(.el-input__wrapper.is-focus) { background: #0f172a; box-shadow: 0 0 0 1px #3b82f6 inset; }
.custom-input :deep(.el-input__prefix) { color: #909399; }
.remember-checkbox :deep(.el-checkbox__inner) { border-radius: 4px; }

.login-btn {
  width: 100%; height: 48px; font-size: 16px; font-weight: 600; letter-spacing: 4px;
  background: linear-gradient(135deg, #d32f2f, #c62828) !important;
  border: none !important; color: #fff !important; border-radius: 9999px;
  transition: all 0.3s ease; box-shadow: 0 4px 16px rgba(211, 47, 47, 0.35);
}
.login-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 24px rgba(211, 47, 47, 0.5); }
.login-btn:active { transform: translateY(0); }
.login-btn :deep(.el-button__text) { color: #fff; }

@media (max-width: 480px) { .login-card { padding: 32px 24px; } }
</style>
