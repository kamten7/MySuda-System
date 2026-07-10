<template>
	<view class="chat-input">
		<view class="chat-input__wrapper">
			<view class="chat-input__box">
				<textarea
					class="chat-input__textarea"
					:value="value"
					:placeholder="placeholder"
					:disabled="disabled"
					:maxlength="500"
					:cursor-spacing="20"
					:show-confirm-bar="false"
					:adjust-position="true"
					:auto-height="true"
					@input="onInput"
					@confirm="onConfirm"
					@focus="onFocus"
					@blur="onBlur"
				/>
			</view>
			<view class="chat-input__send" v-if="!isStreaming">
				<view
					class="send-btn"
					:class="{ 'send-btn--active': value.trim() }"
					@click="onSend"
				>
					<text class="send-icon">📤</text>
				</view>
			</view>
			<view class="chat-input__stop" v-else>
				<view class="stop-btn" @click="onStop">
					<text class="stop-icon">⏹</text>
					<text class="stop-text">停止</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'ChatInput',
	props: {
		value: { type: String, default: '' },
		placeholder: { type: String, default: '输入消息，让小速帮你点餐...' },
		disabled: { type: Boolean, default: false },
		isStreaming: { type: Boolean, default: false }
	},
	methods: {
		onInput(e) {
			this.$emit('input', e.detail.value)
		},
		onConfirm() {
			if (this.value.trim() && !this.disabled && !this.isStreaming) {
				this.$emit('send', this.value.trim())
			}
		},
		onSend() {
			if (this.value.trim() && !this.disabled && !this.isStreaming) {
				this.$emit('send', this.value.trim())
			}
		},
		onStop() {
			this.$emit('stop')
		},
		onFocus() {
			this.$emit('focus')
		},
		onBlur() {
			this.$emit('blur')
		}
	}
}
</script>

<style lang="scss" scoped>
.chat-input {
	background: #fff;
	border-top: 1rpx solid $border-light;
	padding: 16rpx 20rpx;
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}

.chat-input__wrapper {
	display: flex;
	align-items: flex-end;
	gap: 12rpx;
}

.chat-input__box {
	flex: 1;
	background: #f3f4f7;
	border-radius: 24rpx;
	padding: 12rpx 20rpx;
}

.chat-input__textarea {
	width: 100%;
	max-height: 200rpx;
	font-size: $font-base;
	line-height: 1.5;
	color: $text-primary;

	::v-deep textarea {
		// placeholder 样式不支持 scoped，通过全局样式控制
	}
}

.chat-input__send {
	flex-shrink: 0;
}

.send-btn {
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	background: #e5e7eb;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.2s ease;

	.send-icon {
		font-size: 32rpx;
	}

	&--active {
		background: $brand-primary;
		box-shadow: 0 4rpx 16rpx rgba($brand-primary, 0.3);

		&:active {
			transform: scale(0.92);
			box-shadow: 0 2rpx 8rpx rgba($brand-primary, 0.2);
		}
	}
}

.chat-input__stop {
	flex-shrink: 0;
}

.stop-btn {
	display: flex;
	align-items: center;
	gap: 6rpx;
	padding: 14rpx 24rpx;
	background: #fee2e2;
	border: 1rpx solid #fecaca;
	border-radius: $radius-round;

	.stop-icon {
		font-size: 24rpx;
	}
	.stop-text {
		font-size: $font-sm;
		color: $brand-danger;
		font-weight: 500;
	}

	&:active {
		background: #fecaca;
	}
}
</style>
