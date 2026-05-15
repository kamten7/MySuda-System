<template>
  <view class="empty-box" :style="{height: boxHeight}">
    <image class="empty-img" src="./../../static/no_order.png" mode="" />
    <text class="empty-des">{{textLabel}}</text>
  </view>
</template>

<script>
export default {
  props:{
    'boxHeight':{
      type: String,
      default: '100vh'
    },
    'textLabel':{
      type: String,
      default: '暂无数据'
    }
  },
  data () {
    return {
    }
  },
  components: {},
  created () {},
  mounted () {},
  computed: {},
  methods: {},
  watch: {},
  filters: {}
}
</script>
<style lang="scss" scoped>
.empty-box{
  width: 100%;
  display: flex;
  flex-flow: column;
  align-items: center;
  justify-content: center;
  .empty-img{
    width: 480rpx;
    height: 258rpx;
  }
  .empty-des{
    height: 66rpx;
    line-height: 66rpx;
    margin-top: 40rpx;
    font-size: 32rpx;
    font-family: PingFangSC, PingFangSC-Medium;
    font-weight: 500;
    text-align: center;
    color: #333333;
  }

}
</style>
