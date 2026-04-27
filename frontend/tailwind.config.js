/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          400: '#818cf8',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
          800: '#3730a3',
          900: '#312e81',
        },
        surface: {
          DEFAULT: '#ffffff',
          50:  '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
        },
        sidebar: {
          DEFAULT: '#0f172a',
          hover:   '#1e293b',
          active:  '#1e1b4b',
          border:  '#1e293b',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        card:  '0 1px 3px rgba(0,0,0,0.05), 0 8px 24px rgba(15,23,42,0.07)',
        'card-hover': '0 4px 6px rgba(0,0,0,0.05), 0 16px 40px rgba(15,23,42,0.12)',
        glow:  '0 0 0 3px rgba(99,102,241,0.2)',
        'glow-sm': '0 0 0 2px rgba(99,102,241,0.15)',
      },
      backgroundImage: {
        'brand-gradient': 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
        'dark-gradient':  'linear-gradient(180deg, #0f172a 0%, #1e1b4b 100%)',
        'page-gradient':  'linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%)',
        'hero-gradient':  'linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #0f172a 100%)',
      },
      borderRadius: {
        '2xl': '1rem',
        '3xl': '1.5rem',
      },
      animation: {
        'fade-in': 'fadeIn 0.2s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%':   { opacity: 0, transform: 'translateY(4px)' },
          '100%': { opacity: 1, transform: 'translateY(0)' },
        },
      },
    },
  },
  plugins: [],
};
