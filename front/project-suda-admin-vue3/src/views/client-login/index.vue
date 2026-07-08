<template>
  <div class="login-container">
    <!-- ① Canvas 粒子波浪背景 (底层) -->
    <canvas ref="canvasRef" class="particle-canvas" />

    <!-- ② 毛玻璃登录卡片 (z-10 上层) -->
    <div class="login-card">
      <!-- 蓝色 "速" 品牌 Logo -->
      <div class="login-header">
        <div class="brand-badge">
          <span class="brand-char">速</span>
        </div>
        <h1 class="brand-title">速达外卖</h1>
        <p class="brand-subtitle">极速送达 · 美味到家</p>
      </div>

      <!-- 登录表单 — 逻辑完全保留，仅 UI 适配深色背景 -->
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
            placeholder="请输入账号"
            :prefix-icon="User"
            class="glass-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            class="glass-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="form-row">
          <el-checkbox v-model="rememberMe" class="check-light">
            <span class="check-label">记住我</span>
          </el-checkbox>
          <a class="forgot-link" href="javascript:void(0)">忘记密码？</a>
        </div>

        <el-button
          class="submit-btn"
          :loading="loading"
          round
          @click="handleLogin"
        >
          <span v-if="!loading">立即登录</span>
          <span v-else>登录中...</span>
        </el-button>
      </el-form>

      <p class="login-footer">还没有账号？<a href="#">立即注册</a></p>
    </div>
  </div>
</template>

<script setup lang="ts">
// ================================================================
// 登录逻辑 — 完整保留：表单验证 / API 调用 / Pinia Store / 路由跳转
// ================================================================
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({ username: '', password: '' })

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
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
      ElMessage.error('登录失败，请检查账号密码')
    } finally {
      loading.value = false
    }
  })
}

// ================================================================
// Canvas 海浪粒子 — 卡片为波源向外涟漪扩散，浅蓝→深蓝
// ================================================================
import { onMounted, onBeforeUnmount } from 'vue'

interface WaveParticle {
  angle: number
  dist: number
  speed: number
  size: number
  opacity: number
  phaseOffset: number
  colorId: 0 | 1 | 2
}

const canvasRef = ref<HTMLCanvasElement | null>(null)
let animId = 0
let particles: WaveParticle[] = []
let elapsed = 0
let sourceX = 0
let sourceY = 0

const COLOR_LAYERS = [
  { r: 147, g: 197, b: 253 }, // 浅蓝 #93c5fd — 近卡片
  { r: 96,  g: 165, b: 250 }, // 中蓝 #60a5fa
  { r: 37,  g: 99,  b: 235 }, // 深蓝 #2563eb — 屏幕边缘
]

const COUNT = 250
const MAX_DIST = 1000
const SPAWN_PER_FRAME = 4

/** 实时获取卡片中心 */
function updateSource() {
  const card = document.querySelector('.login-card') as HTMLElement | null
  if (card) {
    const r = card.getBoundingClientRect()
    sourceX = r.left + r.width / 2
    sourceY = r.top + r.height / 2
  } else {
    sourceX = window.innerWidth / 2
    sourceY = window.innerHeight / 2
  }
}

function spawn(): WaveParticle {
  return {
    angle: Math.random() * Math.PI * 2,
    dist: 120 + Math.random() * 40,
    speed: 120 + Math.random() * 160,      // 快速扩散 px/s
    size: 3.5 + Math.random() * 5,          // 大粒子 3.5~8.5px
    opacity: 0.6 + Math.random() * 0.4,     // 高透明度
    phaseOffset: Math.random() * Math.PI * 2,
    colorId: 0,
  }
}

function initParticles() {
  updateSource()
  particles = []
  for (let i = 0; i < COUNT; i++) {
    const p = spawn()
    p.dist = 120 + Math.random() * MAX_DIST
    p.colorId = p.dist < 300 ? 0 : p.dist < 600 ? 1 : 2
    particles.push(p)
  }
}

function draw(ctx: CanvasRenderingContext2D, w: number, h: number) {
  const dt = 0.016
  elapsed += dt

  // ① 深色背景
  ctx.fillStyle = '#060e1a'
  ctx.fillRect(0, 0, w, h)

  // ② 卡片周围光晕
  if (sourceX > 0) {
    const g = ctx.createRadialGradient(sourceX, sourceY, 60, sourceX, sourceY, 300)
    g.addColorStop(0, 'rgba(59,130,246,0.18)')
    g.addColorStop(0.4, 'rgba(59,130,246,0.06)')
    g.addColorStop(1, 'rgba(59,130,246,0)')
    ctx.fillStyle = g
    ctx.fillRect(sourceX - 300, sourceY - 300, 600, 600)
  }

  // ③ 每帧补充粒子
  for (let i = 0; i < SPAWN_PER_FRAME; i++) {
    let done = false
    for (const p of particles) {
      if (p.dist > MAX_DIST) { Object.assign(p, spawn()); done = true; break }
    }
    if (!done && particles.length < COUNT + 30) particles.push(spawn())
  }

  // ④ 绘制粒子
  for (const p of particles) {
    const wave = Math.sin(elapsed * 2.5 + p.angle * 5 + p.phaseOffset) * 10
    p.dist += p.speed * dt
    p.dist += wave * dt * 2

    const x = sourceX + Math.cos(p.angle) * p.dist
    const y = sourceY + Math.sin(p.angle) * p.dist

    if (p.dist > MAX_DIST || x < -80 || x > w + 80 || y < -80 || y > h + 80) {
      Object.assign(p, spawn())
      continue
    }

    const ratio = Math.min(1, p.dist / MAX_DIST)
    p.colorId = ratio < 0.35 ? 0 : ratio < 0.65 ? 1 : 2
    const c = COLOR_LAYERS[p.colorId]
    const scale = 0.4 + ratio * 2.0
    const sz = p.size * scale
    const alpha = p.opacity * (1 - ratio * 0.55)

    // 光点
    ctx.beginPath()
    ctx.arc(x, y, sz, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(${c.r},${c.g},${c.b},${alpha.toFixed(3)})`
    ctx.fill()

    // 光晕 (距离近的)
    if (ratio < 0.5) {
      ctx.beginPath()
      ctx.arc(x, y, sz * 3, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(${c.r},${c.g},${c.b},${(alpha * 0.12).toFixed(4)})`
      ctx.fill()
    }
  }

  // ⑤ 近距离粒子之间的连线
  ctx.lineWidth = 0.5
  for (let i = 0; i < particles.length; i++) {
    const pi = particles[i]
    if (pi.dist > 380) continue
    const xi = sourceX + Math.cos(pi.angle) * pi.dist
    const yi = sourceY + Math.sin(pi.angle) * pi.dist
    for (let j = i + 1; j < particles.length; j++) {
      const pj = particles[j]
      if (pj.dist > 380) continue
      const xj = sourceX + Math.cos(pj.angle) * pj.dist
      const yj = sourceY + Math.sin(pj.angle) * pj.dist
      const dd = (xi - xj) ** 2 + (yi - yj) ** 2
      if (dd < 3600) { // < 60px
        const la = 0.06 * (1 - Math.sqrt(dd) / 60)
        ctx.strokeStyle = `rgba(147,197,253,${la.toFixed(4)})`
        ctx.beginPath()
        ctx.moveTo(xi, yi)
        ctx.lineTo(xj, yj)
        ctx.stroke()
      }
    }
  }
}

function loop() {
  const c = canvasRef.value
  if (!c) { animId = requestAnimationFrame(loop); return }
  const ctx = c.getContext('2d')
  if (!ctx) { animId = requestAnimationFrame(loop); return }
  const w = c.clientWidth
  const h = c.clientHeight
  const dpr = devicePixelRatio
  if (c.width !== w * dpr || c.height !== h * dpr) {
    c.width = w * dpr
    c.height = h * dpr
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  }
  updateSource()
  draw(ctx, w, h)
  animId = requestAnimationFrame(loop)
}

onMounted(() => {
  const c = canvasRef.value
  if (!c) return
  const dpr = devicePixelRatio
  c.width = c.clientWidth * dpr
  c.height = c.clientHeight * dpr
  c.getContext('2d')?.setTransform(dpr, 0, 0, dpr, 0, 0)
  initParticles()
  loop()
})

onBeforeUnmount(() => cancelAnimationFrame(animId))
</script>

<style lang="scss" scoped>
/* =============================================================
   Canvas 底层背景
   ============================================================= */
.login-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  overflow: hidden;
}

.particle-canvas {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  z-index: 0;
}

/* =============================================================
   毛玻璃卡片 (z-10 覆盖在 Canvas 上方)
   ============================================================= */
.login-card {
  position: relative;
  z-index: 10;
  width: 420px; max-width: 94%;
  background: rgba(15, 23, 42, 0.38);
  backdrop-filter: blur(22px) saturate(130%);
  -webkit-backdrop-filter: blur(22px) saturate(130%);
  border-radius: 20px;
  padding: 42px 38px 36px;
  border: 1px solid rgba(59, 130, 246, 0.22);
  box-shadow:
    0 0 100px rgba(30, 58, 138, 0.28),
    0 0 35px rgba(59, 130, 246, 0.12),
    0 20px 60px rgba(0, 0, 0, 0.45);
}

/* ── 品牌 Logo ── */
.login-header {
  text-align: center;
  margin-bottom: 34px;
}

.brand-badge {
  width: 62px; height: 62px; margin: 0 auto 16px;
  background: linear-gradient(135deg, #1e3a8a, #2563eb, #3b82f6);
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 0 40px rgba(59,130,246,0.35), 0 8px 20px rgba(0,0,0,0.3);
}

.brand-char {
  font-size: 32px; font-weight: 800; color: #fff; line-height: 1;
}

.brand-title {
  font-size: 26px; font-weight: 700; color: #e2e8f0;
  letter-spacing: 2px; margin-bottom: 4px;
}

.brand-subtitle {
  font-size: 14px; color: #94a3b8; letter-spacing: 1px;
}

/* ── 表单 ── */
.glass-input :deep(.el-input__wrapper) {
  background: rgba(30, 41, 59, 0.45) !important;
  border: 1px solid rgba(59, 130, 246, 0.22) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  padding: 6px 14px;
  transition: border-color 0.25s, box-shadow 0.25s;

  &:hover { border-color: rgba(96,165,250,0.45) !important; }
  &.is-focus, &:focus-within {
    border-color: #3b82f6 !important;
    box-shadow: 0 0 0 3px rgba(59,130,246,0.18) !important;
  }

  .el-input__inner { color: #e2e8f0 !important; font-size: 15px !important;
    &::placeholder { color: #64748b; }
  }
  .el-input__prefix .el-icon { color: #64748b; font-size: 17px; }
}

.glass-input :deep(.el-input__suffix .el-icon) { color: #64748b; }

.form-row {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px;
}

.check-label { font-size: 14px; color: #94a3b8; }

.forgot-link {
  font-size: 14px; color: #93c5fd; text-decoration: none;
  &:hover { color: #bfdbfe; text-decoration: underline; }
}

.check-light :deep(.el-checkbox__label) { color: #94a3b8; }
.check-light :deep(.el-checkbox__inner) {
  background: rgba(30,41,59,0.45); border-color: rgba(59,130,246,0.3);
}

/* ── 提交按钮 (品牌红渐变) ── */
.submit-btn {
  width: 100%; height: 48px; font-size: 16px; font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #d32f2f, #c62828) !important;
  border: none !important; color: #fff !important;
  border-radius: 24px !important;
  transition: all 0.3s ease;
  box-shadow: 0 4px 18px rgba(211,47,47,0.32);

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 26px rgba(211,47,47,0.48);
  }
  &:active { transform: translateY(0); }
}

/* ── 底部 ── */
.login-footer {
  text-align: center; margin-top: 18px; font-size: 14px; color: #64748b;
  a { color: #93c5fd; &:hover { color: #bfdbfe; } }
}

@media (max-width: 480px) {
  .login-card { padding: 32px 20px 24px; }
  .brand-title { font-size: 22px; }
}
</style>
