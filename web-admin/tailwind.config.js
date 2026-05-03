/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#6B4CE6',
        secondary: '#4ECDC4',
        tertiary: '#FF6B9D',
        danger: '#FF4757',
        warning: '#FFA502',
        success: '#26DE81',
      }
    },
  },
  plugins: [],
}
