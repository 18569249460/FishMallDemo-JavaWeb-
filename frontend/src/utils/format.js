// 统一时间展示格式，兼容后端返回的数组时间和 ISO 字符串时间。
export const formatDateTime = (value) => {
  if (!value) {
    return ''
  }

  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value
    return buildChineseDateTime(year, month, day, hour, minute, second)
  }

  const text = String(value)
  const matched = text.match(/^(\d{4})-(\d{1,2})-(\d{1,2})[T\s](\d{1,2}):(\d{1,2})(?::(\d{1,2}))?/)
  if (matched) {
    const [, year, month, day, hour, minute, second = 0] = matched
    return buildChineseDateTime(year, month, day, hour, minute, second)
  }

  const date = new Date(text)
  if (!Number.isNaN(date.getTime())) {
    return buildChineseDateTime(
      date.getFullYear(),
      date.getMonth() + 1,
      date.getDate(),
      date.getHours(),
      date.getMinutes(),
      date.getSeconds()
    )
  }

  return text
}

const buildChineseDateTime = (year, month, day, hour, minute, second) => {
  const pad = (number) => String(Number(number || 0)).padStart(2, '0')
  return `${Number(year)}年${Number(month)}月${Number(day)}日 ${pad(hour)}:${pad(minute)}:${pad(second)}`
}
