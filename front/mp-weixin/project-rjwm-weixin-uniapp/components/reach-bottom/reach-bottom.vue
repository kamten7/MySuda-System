<template>
  <view class="bottom-box">
    <view class="bottom-item" v-if="status==='loading'">
      <text class="title">加载中...</text>
    </view>
    <view class="bottom-item" v-if="status==='complete'">
      <!-- <text class="line"></text> -->
      <text class="title">没有更多了</text>
      <!-- <text class="line"></text> -->
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      
    }
  },
  props: {
    status:{
      type: String,
      default: 'complete'
    }
  }
}
</script>

<style lang="scss">
.bottom-box {
  width: 730rpx;
  .bottom-item {
    width: 730rpx;
    height: 100rpx;
    line-height: 100rpx;
    text-align: center;
    .line {
      display: inline-block;
      width: 200rpx;
      border-bottom: 1px solid #989e98;
      vertical-align: middle;
    }
    .title {
      color: #969799;
      font-size: 28rpx;
    }
  }
}
</style>
