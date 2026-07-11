<template>
<view class="ai-page">
	<!-- ====== 消息区（从顶部开始）====== -->
	<scroll-view
		class="msc"
		scroll-y
		:scroll-top="st"
		:scroll-with-animation="true"
		:style="{ height: mscH + 'px' }"
	>
		<!-- 欢迎 -->
		<view v-if="msgs.length===0" class="w">
			<view class="wc">
				<view class="wci"><view class="wcii"><text class="wcit">AI</text></view></view>
				<text class="wct">你好！我是小速 👋</text>
				<text class="wcd">你的智能点餐助手</text>
				<view class="wcf">
					<view class="wcfi"><text class="wcfd"></text><text>推荐今日美食</text></view>
					<view class="wcfi"><text class="wcfd"></text><text>帮你快速点餐</text></view>
					<view class="wcfi"><text class="wcfd"></text><text>解答菜品问题</text></view>
				</view>
			</view>
			<text class="whl">试试下面这些</text>
			<view class="wp">
				<view v-for="(p,i) in prompts" :key="i" class="wpi" @click="send(p.text)">
					<text class="wpie">{{ p.icon }}</text>
					<text class="wpit">{{ p.text }}</text>
				</view>
			</view>
		</view>

		<!-- 对话 -->
		<view v-else class="cl">
			<view class="cl-head">
				<text class="cl-head-title">AI 小速</text>
				<view class="cl-head-clear" @click="handleClear"><text>🗑️ 清空对话</text></view>
			</view>
			<view
				v-for="(m,i) in msgs" :key="i"
				class="rw" :class="m.role==='user'?'rr':''"
			>
				<view v-if="m.role!=='user'" class="av aa"><text class="avt">AI</text></view>
				<view class="bw">
					<view class="bu" :class="m.role==='user'?'bU':'bA'">
						<view v-if="m.loading" class="ty">
							<view class="td" v-for="d in 3" :key="d"></view>
						</view>
						<text v-else class="bt" user-select>{{ m.content }}</text>
					</view>
					<text v-if="m.time" class="tm" :class="m.role==='user'?'tmR':'tmL'">{{ m.time }}</text>
				</view>
				<view v-if="m.role==='user'" class="av au">
					<image v-if="ua" class="aim" :src="ua" mode="aspectFill"/>
					<text v-else>👤</text>
				</view>
			</view>
			<view class="sp"/>
		</view>
	</scroll-view>

	<!-- ====== 输入栏（fixed bottom）====== -->
	<view class="bar" :style="{ height: barH + 'px', paddingBottom: sbi + 'px' }">
		<view class="bb">
			<textarea
				class="bi" v-model="txt"
				placeholder="告诉小速你想吃什么..."
				placeholder-style="color:#c0c4cc"
				:maxlength="500" :cursor-spacing="12"
				:show-confirm-bar="false"
				:adjust-position="true"
				:auto-height="true"
				:disabled="busy"
				@confirm="send(txt)"
			/>
		</view>
		<view class="bs" :class="{on:txt.trim()&&!busy,stop:busy}" @click="send(txt)">
			<text v-if="!busy">➤</text>
			<text v-else class="st">■</text>
		</view>
	</view>
</view>
</template>

<script>
import { mapState, mapMutations } from 'vuex'
import { aiChat, aiClearHistory } from '@/pages/api/api.js'

export default {
	data(){
		return {
			txt:'', busy:false, st:0,
			barH:56, // 输入栏总高
			sbi:0,  // safeBottom inset
			mscH:400, // 消息区高度
			lum:'', // 上一条用户消息(用于重试)
			prompts:[
				{icon:'🍜',text:'今天吃什么好？'},
				{icon:'🔥',text:'推荐热门菜品'},
				{icon:'💰',text:'性价比高的套餐'},
				{icon:'🌶️',text:'想吃辣的'},
				{icon:'🥗',text:'低卡减脂推荐'},
				{icon:'🍚',text:'有什么主食推荐？'}
			]
		}
	},
	computed:{
		...mapState(['aiMessages','baseUserInfo']),
		msgs(){ return this.aiMessages||[] },
		ua(){
			try{return JSON.parse(this.baseUserInfo||'{}').avatarUrl||''}catch{return''}
		}
	},
	mounted(){ this.calc() },
	methods:{
		...mapMutations(['setAIMessages','setCartCount','clearAIMessages','setSessionId']),
		calc(){
			var h=667,b=0
			try{
				if(typeof wx!='undefined'&&wx.getWindowInfo){
					var w=wx.getWindowInfo()
					h=w.screenHeight||667
					if(w.safeArea)b=h-w.safeArea.bottom
				}else{
					var i=uni.getSystemInfoSync()
					h=i.screenHeight||667
					if(i.safeAreaInsets)b=h-i.safeAreaInsets.bottom
				}
			}catch(e){}
			this.sbi=Math.max(0,b)
			this.barH=56+this.sbi
			this.mscH=h-this.barH
		},
		send(t){
			var m=(t||'').trim()
			if(!m||this.busy)return
			this.txt=''; this.lum=m; this.busy=true
			var a=[...this.msgs]
			var tm=this.tm()
			a.push({role:'user',content:m,time:tm})
			a.push({role:'ai',content:'',loading:true,time:''})
			this.setAIMessages(a)
			this.scb()
			var self=this
			aiChat({message:m,sessionId:''}).then(function(r){
				var c=[...self.msgs],k=c.length-1
				if(k>=0&&c[k].role==='ai'){
					c[k].content=r&&r.data&&r.data.content?r.data.content:'抱歉，请重试'
					c[k].loading=false; c[k].time=self.tm()
				}
				self.setAIMessages(c)
				self.busy=false
				self.$nextTick(function(){self.scb()})
				self.rc()
			}).catch(function(){
				var c=[...self.msgs],k=c.length-1
				if(k>=0&&c[k].role==='ai'){
					c[k].content='服务暂不可用，请重试'; c[k].loading=false; c[k].time=self.tm()
				}
				self.setAIMessages(c)
				self.busy=false
			})
		},
		scb(){ this.st=Date.now() },
		tm(){ var d=new Date(); return String(d.getHours()).padStart(2,'0')+':'+String(d.getMinutes()).padStart(2,'0') },
		handleClear(){
			var self=this
			uni.showModal({
				title:'清空对话', content:'确定要清空当前对话吗？', confirmColor:'#1a56db',
				success:function(r){
					if(r.confirm){
						aiClearHistory('').catch(function(){})
						self.clearAIMessages()
						self.setSessionId('')
						self.lum=''
						uni.showToast({title:'已清空',icon:'success',duration:1200})
					}
				}
			})
		},
		async rc(){
			try{
				var m=await import('@/pages/api/api.js')
				var r=await m.getShoppingCartList()
				if(r&&r.code===1&&r.data) this.setCartCount(r.data.reduce(function(s,x){return s+(x.number||0)},0))
			}catch(e){}
		}
	}
}
</script>

<style> page{height:100vh;overflow:hidden;background:#f3f4f7} </style>
<style scoped lang="scss">
$b:#1a56db; $bl:#2563eb; $bg:#eff6ff;

/* === msg scroll (full height minus input bar) === */
.msc{position:fixed;left:0;right:0;top:0;overflow-y:auto;box-sizing:border-box;-webkit-overflow-scrolling:touch}

/* === welcome === */
.w{padding:48rpx 24rpx 60rpx;display:flex;flex-direction:column;align-items:center}
.wc{background:#fff;border-radius:32rpx;padding:56rpx 40rpx 40rpx;box-shadow:0 4rpx 20rpx rgba(0,0,0,.06);width:100%;max-width:680rpx;display:flex;flex-direction:column;align-items:center;margin-bottom:48rpx}
.wci{width:120rpx;height:120rpx;border-radius:50%;background:linear-gradient(135deg,$b,$bl);display:flex;align-items:center;justify-content:center;margin-bottom:28rpx;box-shadow:0 8rpx 40rpx rgba($b,.25)}
.wcii{width:104rpx;height:104rpx;border-radius:50%;background:linear-gradient(135deg,$bl,#3b82f6);display:flex;align-items:center;justify-content:center}
.wcit{font-size:40rpx;font-weight:900;color:#fff}
.wct{font-size:38rpx;font-weight:700;color:#1f2937;margin-bottom:12rpx}
.wcd{font-size:26rpx;color:#6b7280;margin-bottom:36rpx}
.wcf{display:flex;flex-direction:column;gap:16rpx;width:100%;padding:0 20rpx}
.wcfi{display:flex;align-items:center;gap:14rpx;font-size:28rpx;color:#6b7280}
.wcfd{width:12rpx;height:12rpx;border-radius:50%;background:$b;flex-shrink:0}
.whl{font-size:26rpx;color:#9ca3af;margin-bottom:24rpx}
.wp{display:flex;flex-wrap:wrap;gap:20rpx;justify-content:center;padding:0 8rpx}
.wpi{display:flex;align-items:center;gap:12rpx;padding:20rpx 28rpx;background:#fff;border:1rpx solid #e5e7eb;border-radius:24rpx;box-shadow:0 2rpx 12rpx rgba(0,0,0,.03);transition:all .2s}
.wpi:active{background:linear-gradient(135deg,$bg,#dbeafe);border-color:$b;transform:scale(.97)}
.wpie{font-size:34rpx}
.wpit{font-size:27rpx;color:#1f2937;font-weight:500}

/* === chat === */
.cl{padding:24rpx 20rpx 40rpx}
.cl-head{display:flex;align-items:center;justify-content:space-between;padding:16rpx 20rpx 24rpx;margin-bottom:8rpx}
.cl-head-title{font-size:32rpx;font-weight:700;color:#1f2937}
.cl-head-clear{font-size:24rpx;color:#9ca3af;padding:8rpx 16rpx;border-radius:16rpx;background:#f3f4f6;transition:all .2s}
.cl-head-clear:active{color:$b;background:$bg}
.rw{display:flex;margin-bottom:28rpx;align-items:flex-start;animation:mi .3s ease}
.rr{flex-direction:row-reverse}
@keyframes mi{from{opacity:0;transform:translateY(12rpx)}to{opacity:1;transform:translateY(0)}}
.av{flex-shrink:0;width:68rpx;height:68rpx;border-radius:50%;overflow:hidden;display:flex;align-items:center;justify-content:center}
.aa{background:linear-gradient(135deg,$bg,#dbeafe);margin-right:16rpx;box-shadow:0 2rpx 8rpx rgba($b,.1)}
.aa .avt{font-size:22rpx;font-weight:800;color:$b}
.au{background:linear-gradient(135deg,$b,$bl);margin-left:16rpx;box-shadow:0 2rpx 12rpx rgba($b,.25)}
.aim{width:68rpx;height:68rpx;border-radius:50%}
.rr .av{margin-right:0;margin-left:16rpx}
.bw{max-width:72%;display:flex;flex-direction:column}
.bu{padding:20rpx 26rpx;border-radius:24rpx;line-height:1.65}
.bA{background:#f3f4f6;border-top-left-radius:8rpx;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04)}
.bU{background:linear-gradient(135deg,$b,$bl);border-top-right-radius:8rpx;box-shadow:0 2rpx 12rpx rgba($b,.2)}
.bt{font-size:28rpx;white-space:pre-wrap;word-break:break-all;color:#1f2937;line-height:1.7}
.bU .bt{color:#fff}
.tm{font-size:22rpx;color:#9ca3af;margin-top:8rpx;padding:0 8rpx}
.tmL{text-align:left}.tmR{text-align:right}
.ty{display:flex;gap:8rpx;padding:6rpx 0}
.td{width:14rpx;height:14rpx;border-radius:50%;background:$b;animation:b 1.4s infinite}
.td:nth-child(1){animation-delay:0s}.td:nth-child(2){animation-delay:.16s}.td:nth-child(3){animation-delay:.32s}
@keyframes b{0%,80%,100%{transform:translateY(0);opacity:.3}40%{transform:translateY(-10rpx);opacity:1}}
.sp{height:40rpx}

/* === input bar === */
.bar{position:fixed;bottom:0;left:0;right:0;z-index:99;display:flex;align-items:flex-end;gap:16rpx;padding:16rpx 20rpx 0;background:#fff;border-top:1rpx solid #f0f0f0;box-shadow:0 -4rpx 20rpx rgba(0,0,0,.04);box-sizing:border-box}
.bb{flex:1;background:#f5f6f8;border-radius:28rpx;padding:0 24rpx;transition:background .2s}
.bi{width:100%;min-height:44px;max-height:120px;padding:10rpx 0;font-size:28rpx;color:#1f2937;line-height:1.5;box-sizing:border-box}
.bs{flex-shrink:0;width:48px;height:48px;border-radius:50%;background:#e5e7eb;display:flex;align-items:center;justify-content:center;transition:.2s;font-size:28rpx;color:#fff;font-weight:700}
.bs.on{background:linear-gradient(135deg,$b,$bl);box-shadow:0 4rpx 16rpx rgba($b,.35)}
.bs.on:active{transform:scale(.92)}
.bs.stop{background:#ef4444;box-shadow:0 4rpx 16rpx rgba(239,68,68,.3)}
.st{font-size:22rpx}
</style>
