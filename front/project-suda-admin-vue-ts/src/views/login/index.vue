<template>
  <div class="login">
    <div class="login-card">
      <img class="login-logo" src="@/assets/login/suda.png" alt="速达外卖" />

      <!-- 品牌标题 + 口号 -->
      <h1 class="brand-title">速达外卖</h1>
      <p class="brand-slogan">极速送达·美味到家</p>

      <!-- 登录表单 -->
      <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            type="text"
            auto-complete="off"
            placeholder="账号"
            prefix-icon="iconfont icon-user"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="iconfont icon-lock"
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>

        <!-- 记住我 + 忘记密码 -->
        <div class="form-options">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <a class="forgot-link" href="javascript:void(0)">忘记密码?</a>
        </div>

        <el-form-item style="width: 100%">
          <el-button
            :loading="loading"
            class="login-btn"
            size="medium"
            style="width: 100%"
            @click.native.prevent="handleLogin"
          >
            <span v-if="!loading">立即登录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator'
import { Route } from 'vue-router'
import { Form as ElForm, Input } from 'element-ui'
import { UserModule } from '@/store/modules/user'

@Component({
  name: 'Login',
})
export default class extends Vue {
  private validateUsername = (rule: any, value: string, callback: Function) => {
    if (!value) {
      callback(new Error('请输入用户名'))
    } else {
      callback()
    }
  }
  private validatePassword = (rule: any, value: string, callback: Function) => {
    if (value.length < 6) {
      callback(new Error('密码必须在6位以上'))
    } else {
      callback()
    }
  }
  private loginForm = {
    username: 'admin',
    password: '123456',
  } as {
    username: String
    password: String
  }

  loginRules = {
    username: [{ validator: this.validateUsername, trigger: 'blur' }],
    password: [{ validator: this.validatePassword, trigger: 'blur' }],
  }
  private loading = false
  private redirect?: string
  private rememberMe = false

  @Watch('$route', { immediate: true })
  private onRouteChange(route: Route) {}

  private handleLogin() {
    ;(this.$refs.loginForm as ElForm).validate(async (valid: boolean) => {
      if (valid) {
        this.loading = true
        await UserModule.Login(this.loginForm as any)
          .then((res: any) => {
            if (String(res.code) === '1') {
              this.$router.push('/')
            } else {
              this.loading = false
            }
          })
          .catch(() => {
            this.loading = false
          })
      } else {
        return false
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100%;
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #c62828 0%, #d32f2f 50%, #b71c1c 100%);
  overflow: hidden;
}

.login-card {
  width: 400px;
  max-width: 90%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  padding: 40px 36px 32px;
  box-sizing: border-box;
}

.login-logo {
  display: block;
  width: 180px;
  height: auto;
  margin: 0 auto 20px;
}

/* 品牌标题 */
.brand-title {
  text-align: center;
  font-size: 26px;
  font-weight: 700;
  color: #333;
  margin: 0 0 6px;
  letter-spacing: 2px;
}

/* 口号 */
.brand-slogan {
  text-align: center;
  font-size: 14px;
  color: #999;
  margin: 0 0 28px;
}

/* 表单 */
.login-form {
  .el-form-item {
    margin-bottom: 22px;
  }
  .el-form-item.is-error .el-input__inner {
    border-color: #d32f2f !important;
  }
  .el-input__inner {
    border: 1px solid #e0e0e0;
    border-radius: 6px;
    font-size: 14px;
    color: #333;
    height: 44px;
    line-height: 44px;
    transition: border-color 0.3s;
    &:focus,
    &:hover {
      border-color: #ffc107;
    }
  }
  .el-input__prefix {
    left: 12px;
    color: #999;
  }
  .el-input--prefix .el-input__inner {
    padding-left: 36px;
  }
  .el-input__inner::placeholder {
    color: #bdbdbd;
  }
}

/* 记住我 + 忘记密码 行 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
  .el-checkbox {
    color: #666;
    font-size: 13px;
  }
  .forgot-link {
    font-size: 13px;
    color: #ffc107;
    text-decoration: none;
    &:hover {
      color: #ffb300;
      text-decoration: underline;
    }
  }
}

/* 登录按钮 */
.login-btn {
  border-radius: 24px;
  padding: 13px 20px !important;
  font-weight: 600;
  font-size: 15px;
  border: 0;
  color: #333;
  background-color: #ffc107;
  letter-spacing: 2px;
  transition: all 0.3s;
  &:hover,
  &:focus {
    background-color: #ffb300;
    color: #333;
    box-shadow: 0 4px 12px rgba(255, 193, 7, 0.4);
  }
  &:active {
    background-color: #f9a825;
  }
}

/* 响应式 */
@media screen and (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 32px 20px 24px;
  }
  .brand-title {
    font-size: 22px;
  }
  .brand-slogan {
    font-size: 12px;
  }
}
</style>
