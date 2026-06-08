<template>
  <span
    class="decrypted-text"
    :class="{ 'is-revealed': isRevealed }"
    :style="textVars"
    role="text"
    :aria-label="text"
  >
    {{ displayText }}
  </span>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { DEFAULT_DECRYPTED_TEXT_OPTIONS, createScrambledText } from '@/utils/scrambleText.js'

const props = defineProps({
  // 修改标题真实文字。
  text: { type: String, default: DEFAULT_DECRYPTED_TEXT_OPTIONS.text },

  // 修改字体大小：单位 px。
  fontSize: { type: Number, default: DEFAULT_DECRYPTED_TEXT_OPTIONS.fontSize },

  // 修改未完成解密时的文字颜色。
  textColor: { type: String, default: DEFAULT_DECRYPTED_TEXT_OPTIONS.textColor },

  // 修改解密完成后的高亮颜色。
  revealColor: { type: String, default: DEFAULT_DECRYPTED_TEXT_OPTIONS.revealColor },

  // 修改解密速度：每个字符揭示的间隔，单位 ms。
  speed: { type: Number, default: DEFAULT_DECRYPTED_TEXT_OPTIONS.speed }
})

const displayText = ref(createScrambledText(props.text))
const isRevealed = ref(false)
let timerId = 0
let startTimerId = 0

const textVars = computed(() => ({
  '--title-font-size': `${props.fontSize}px`,
  '--text-color': props.textColor,
  '--reveal-color': props.revealColor
}))

function resetScramble() {
  clearInterval(timerId)
  isRevealed.value = false
  displayText.value = createScrambledText(props.text)
}

function decrypt() {
  if (isRevealed.value) return
  clearInterval(timerId)
  isRevealed.value = false

  const chars = displayText.value.split('')
  let index = 0

  timerId = window.setInterval(() => {
    if (index >= props.text.length) {
      clearInterval(timerId)
      displayText.value = props.text
      isRevealed.value = true
      return
    }

    chars[index] = props.text[index]
    for (let i = index + 1; i < props.text.length; i++) {
      if (props.text[i] !== ' ') {
        chars[i] = createScrambledText(props.text[i])
      }
    }
    displayText.value = chars.join('')
    index += 1
  }, props.speed)
}

onMounted(() => {
  // 自动开始一次解密：延迟一点点，让初始乱码状态先出现。
  startTimerId = window.setTimeout(decrypt, 220)
})

watch(
  () => props.text,
  () => {
    resetScramble()
  }
)

onBeforeUnmount(() => {
  clearTimeout(startTimerId)
  clearInterval(timerId)
})
</script>

<style scoped>
.decrypted-text {
  display: inline-block;
  color: var(--text-color);
  font-family: "Gabriola", Gadget, sans-serif;
  font-size: var(--title-font-size);
  font-weight: 700;
  line-height: 1;
  letter-spacing: 0.2em;
  white-space: nowrap;
  cursor: default;
  user-select: none;
  /* 标题立体感：前几层是浅色边缘，后几层是深色厚度和投影。 */
  text-shadow:
    1px 1px 0 rgba(255, 255, 255, 0.34),
    2px 2px 0 rgba(0, 0, 0, 0.22),
    4px 5px 0 rgba(0, 0, 0, 0.18),
    8px 12px 22px rgba(0, 0, 0, 0.34);
  transition:
    color 420ms ease,
    text-shadow 420ms ease,
    filter 420ms ease;
}

.decrypted-text.is-revealed {
  color: var(--reveal-color);
  filter: none;
  /* 解密完成后保持白色，只保留一点立体阴影，不做强发光。 */
  text-shadow:
    1px 1px 0 rgba(255, 255, 255, 0.28),
    2px 2px 0 rgba(0, 0, 0, 0.24),
    5px 7px 0 rgba(0, 0, 0, 0.18),
    10px 14px 24px rgba(0, 0, 0, 0.34);
}

</style>
