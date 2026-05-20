<script setup>
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {})
</script>

<style>
/* Global: prevent outer viewport scrollbar */
html, body {
  height: 100%;
  overflow: hidden;
  background: #faf6f0;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
  overflow: hidden;
}

/* Smooth page transitions */
uni-page {
  transition: opacity 0.15s ease;
}

/* Remove default button styling */
button {
  padding: 0;
  margin: 0;
}

/* Input base reset */
input, textarea {
  font-family: inherit;
  outline: none;
}

/* placeholder global */
::-webkit-input-placeholder {
  color: #c4b8a8;
}
</style>
