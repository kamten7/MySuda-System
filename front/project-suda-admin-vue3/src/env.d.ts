/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<Record<string, never>, Record<string, never>, unknown>
  export default component
}

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_SERVER_URL: string
  readonly VITE_WS_URL: string
  readonly VITE_DELETE_PERMISSIONS: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
