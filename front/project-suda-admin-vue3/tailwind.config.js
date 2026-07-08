/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  important: '#app',
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#1a56db',
          hover: '#1e40af',
          light: '#3b82f6',
          dark: '#1e3a8a',
          50: '#eff6ff',
          100: '#dbeafe',
          500: '#1a56db',
          700: '#1e40af',
          900: '#1e3a8a',
        },
        accent: {
          DEFAULT: '#f59e0b',
          hover: '#d97706',
          active: '#b45309',
          light: '#fef3c7',
        },
        sidebar: {
          bg: '#0f172a',
          hover: '#1e293b',
          active: '#1a56db',
        },
      },
      spacing: {
        sidebar: '220px',
        'sidebar-collapsed': '64px',
        navbar: '56px',
      },
      fontFamily: {
        sans: ['Inter', '-apple-system', 'BlinkMacSystemFont', 'PingFang SC', 'Microsoft YaHei', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
