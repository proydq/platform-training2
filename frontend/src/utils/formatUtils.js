export function formatDuration(seconds) {
  if (!seconds || seconds === 0) return '0分钟'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (h > 0) {
    return m > 0 ? `${h}小时${m}分钟` : `${h}小时`
  }
  return `${m}分钟`
}
