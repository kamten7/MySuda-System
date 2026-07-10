<template>
<view class="pg">
  <scroll-view class="sc" scroll-y :scroll-top="st">
    <view v-if="msgs.length===0" class="wl">
      <view class="wav"><text class="wavt">AI</text></view>
      <text class="whi">你好，我是小速</text>
      <text class="wsub">你的智能点餐助手</text>
      <view class="wtips">
        <text class="wtl">试试这样问我</text>
        <view class="wtr">
          <view v-for="(h,i) in hints" :key="i" class="wc" @click="doSend(h)">{{ h }}</view>
        </view>
      </view>
    </view>
    <view v-else class="cl">
      <view v-for="(m,i) in msgs" :key="i" class="rw" :class="m.role==='user'?'rr':''">
        <view v-if="m.role==='ai'" class="av aa"><text class="avt">AI</text></view>
        <view class="bb" :class="m.role==='user'?'bU':'bA'">
          <text class="bbt">{{ m.content }}</text>
          <text v-if="m.loading" class="ld">···</text>
        </view>
        <image v-if="m.role==='user'" class="av au" :src="avatar" mode="aspectFill"/>
      </view>
      <view style="height:40rpx"/>
    </view>
  </scroll-view>
  <view class="bar" :style="{paddingBottom:sb+'px'}">
    <view class="bbx"><input class="bi" v-model="txt" placeholder="告诉我你想吃什么..." confirm-type="send" :maxlength="500" :disabled="busy" @confirm="doSend(txt)"/></view>
    <view class="bs" :class="{on:txt.trim()&&!busy}" @click="doSend(txt)"><text class="bsi">↑</text></view>
  </view>
</view>
</template>

<script>
import { mapState, mapMutations } from 'vuex'
import { aiChat } from '@/pages/api/api.js'

export default {
  data() {
    return {
      txt: '',
      busy: false,
      st: 0,
      sb: 0,
      hints: ['推荐一些辣的菜','有什么实惠好吃的？','今天有什么推荐的？','最受欢迎的菜品有哪些？','有没有适合减肥的？']
    }
  },
  computed: {
    ...mapState(['aiMessages','baseUserInfo']),
    msgs() { return this.aiMessages || [] },
    avatar() {
      try { return JSON.parse(this.baseUserInfo||'{}').avatarUrl || '' } catch { return '' }
    }
  },
  mounted() {
    const info = uni.getSystemInfoSync()
    this.sb = info.safeAreaInsets ? info.screenHeight - info.safeAreaInsets.bottom : 0
  },
  methods: {
    ...mapMutations(['setAIMessages','setCartCount']),
    doSend(t) {
      const msg = (t||'').trim()
      if (!msg || this.busy) return
      this.txt = ''
      this.busy = true
      const arr = [...this.msgs]
      arr.push({ role:'user', content:msg })
      arr.push({ role:'ai', content:'', loading:true })
      this.setAIMessages(arr)
      this.$nextTick(() => { this.st = Date.now() })
      const self = this
      aiChat({ message:msg, sessionId:'' }).then(function(r) {
        const c = [...self.msgs]
        const k = c.length - 1
        if (k >= 0 && c[k].role === 'ai') {
          c[k].content = (r && r.data && r.data.content) ? r.data.content : '抱歉，请重试'
          c[k].loading = false
        }
        self.setAIMessages(c)
        self.busy = false
        self.$nextTick(function() { self.st = Date.now() })
        self.rc()
      }).catch(function() {
        const c = [...self.msgs]
        const k = c.length - 1
        if (k >= 0 && c[k].role === 'ai') {
          c[k].content = '服务暂不可用，请重试'
          c[k].loading = false
        }
        self.setAIMessages(c)
        self.busy = false
      })
    },
    async rc() {
      try {
        const m = await import('@/pages/api/api.js')
        const r = await m.getShoppingCartList()
        if (r && r.code === 1 && r.data) {
          this.setCartCount(r.data.reduce(function(s,x){return s + (x.number||0)}, 0))
        }
      } catch(e) {}
    }
  }
}
</script>

<style>
page { height:100vh; overflow:hidden; background:#f3f4f7 }
</style>

<style scoped>
.pg { display:flex; flex-direction:column; height:100vh; background:#f3f4f7 }
.sc { flex:1; overflow-y:auto; padding:0 24rpx }
/* welcome */
.wl { display:flex; flex-direction:column; align-items:center; padding-top:100rpx }
.wav { width:120rpx;height:120rpx;border-radius:50%;background:linear-gradient(135deg,#1e3a8a,#1a56db);display:flex;align-items:center;justify-content:center;margin-bottom:28rpx;box-shadow:0 8rpx 32rpx rgba(26,86,219,.25) }
.wavt { font-size:36rpx;font-weight:800;color:#fff }
.whi { font-size:40rpx;font-weight:700;color:#1f2937;margin-bottom:8rpx }
.wsub { font-size:28rpx;color:#9ca3af;margin-bottom:56rpx }
.wtips { width:100% }
.wtl { display:block;text-align:center;font-size:26rpx;color:#bbb;margin-bottom:20rpx }
.wtr { display:flex;flex-wrap:wrap;gap:16rpx;justify-content:center }
.wc { padding:18rpx 28rpx;background:#fff;border:1rpx solid #e5e7eb;border-radius:40rpx;font-size:28rpx;color:#4b5563;box-shadow:0 2rpx 8rpx rgba(0,0,0,.03) }
.wc:active { background:#1a56db;color:#fff }
/* chat */
.cl { padding:20rpx 0 40rpx }
.rw { display:flex;margin-bottom:28rpx;align-items:flex-start }
.rr { flex-direction:row-reverse }
.av { flex-shrink:0;width:68rpx;height:68rpx;border-radius:50%;overflow:hidden }
.aa { background:linear-gradient(135deg,#dbeafe,#bfdbfe);display:flex;align-items:center;justify-content:center;margin-right:16rpx }
.rr .av { margin-right:0;margin-left:16rpx }
.avt { font-size:24rpx;font-weight:700;color:#1a56db }
.au { background:#e5e7eb }
.bb { max-width:520rpx;padding:18rpx 24rpx;border-radius:20rpx;line-height:1.7 }
.bA { background:#fff;border-top-left-radius:6rpx;box-shadow:0 2rpx 12rpx rgba(0,0,0,.04) }
.bU { background:#1a56db;border-top-right-radius:6rpx }
.bbt { font-size:28rpx;white-space:pre-wrap;word-break:break-all }
.bU .bbt { color:#fff }
.bA .bbt { color:#374151 }
.ld { color:#1a56db;font-size:22rpx;letter-spacing:6rpx }
/* bar */
.bar { flex-shrink:0;display:flex;align-items:center;gap:16rpx;padding:8rpx 24rpx;background:#fff;border-top:1rpx solid #f3f4f7 }
.bbx { flex:1;background:#f3f4f7;border-radius:36rpx;padding:0 28rpx }
.bi { height:44px;line-height:44px;font-size:28rpx;color:#333 }
.bs { flex-shrink:0;width:44px;height:44px;border-radius:50%;background:#e5e7eb;display:flex;align-items:center;justify-content:center }
.bs.on { background:#1a56db }
.bsi { font-size:24px;color:#fff;font-weight:700 }
</style>
