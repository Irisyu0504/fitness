export function buildDotGrid({ width, height, dotRadius, dotSpacing }) {
  const step = dotRadius + dotSpacing

  if (width <= 0 || height <= 0 || step <= 0) {
    return []
  }

  const cols = Math.floor(width / step)
  const rows = Math.floor(height / step)
  const padX = (width % step) / 2
  const padY = (height % step) / 2
  const dots = []

  for (let row = 0; row < rows; row++) {
    for (let col = 0; col < cols; col++) {
      const ax = padX + col * step + step / 2
      const ay = padY + row * step + step / 2
      dots.push({ ax, ay, sx: ax, sy: ay, vx: 0, vy: 0, x: ax, y: ay })
    }
  }

  return dots
}
