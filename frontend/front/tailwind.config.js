/** @type {import('tailwindcss').Config} */
module.exports = {
  // make dark mode strategy explicit; Tailwind accepts 'class' or ['class'] depending on version
  darkMode: ["class"],
  content: ["./index.html", "./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {},
  },
  plugins: [],
};
