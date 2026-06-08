export const DEFAULT_DECRYPTED_TEXT_OPTIONS = {
  text: 'CoreFitness',
  fontSize: 64,
  textColor: '#FFFFFF',
  revealColor: '#FFFFFF',
  speed: 50
}

const DEFAULT_CHARACTERS = '#%&*@ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'

export function createScrambledText(text, characters = DEFAULT_CHARACTERS) {
  return text
    .split('')
    .map((char) => {
      if (char === ' ') return ' '
      return characters[Math.floor(Math.random() * characters.length)]
    })
    .join('')
}
