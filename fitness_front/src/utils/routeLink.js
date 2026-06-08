export function isExternalLink(href = '') {
  return (
    href.startsWith('http://') ||
    href.startsWith('https://') ||
    href.startsWith('//') ||
    href.startsWith('mailto:') ||
    href.startsWith('tel:') ||
    href.startsWith('#')
  )
}

export function isRouterLink(href = '') {
  return Boolean(href) && !isExternalLink(href)
}
