import dayjs from 'dayjs'

export type DateRangeType = 'yesterday' | 'last7' | 'last30' | 'thisWeek' | 'thisMonth'

/**
 * Format date to string
 */
export function formatDate(date: string | Date | number, format = 'YYYY-MM-DD'): string {
  return dayjs(date).format(format)
}

/**
 * Format date to datetime string
 */
export function formatDateTime(date: string | Date | number): string {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * Get date range by type
 */
export function getDateRange(type: DateRangeType): { start: string; end: string } {
  const now = dayjs()

  switch (type) {
    case 'yesterday':
      return {
        start: now.subtract(1, 'day').format('YYYY-MM-DD'),
        end: now.subtract(1, 'day').format('YYYY-MM-DD'),
      }
    case 'last7':
      return {
        start: now.subtract(7, 'day').format('YYYY-MM-DD'),
        end: now.subtract(1, 'day').format('YYYY-MM-DD'),
      }
    case 'last30':
      return {
        start: now.subtract(30, 'day').format('YYYY-MM-DD'),
        end: now.subtract(1, 'day').format('YYYY-MM-DD'),
      }
    case 'thisWeek':
      return {
        start: now.startOf('week').format('YYYY-MM-DD'),
        end: now.format('YYYY-MM-DD'),
      }
    case 'thisMonth':
      return {
        start: now.startOf('month').format('YYYY-MM-DD'),
        end: now.format('YYYY-MM-DD'),
      }
  }
}

/**
 * Get today's start and end datetime
 */
export function getTodayRange(): { start: string; end: string } {
  return {
    start: dayjs().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
    end: dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss'),
  }
}

/**
 * Format currency amount
 */
export function formatAmount(amount: number | string): string {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  if (isNaN(num)) return '0.00'
  return num.toFixed(2)
}

/**
 * Format order completion rate
 */
export function formatRate(rate: number | string): string {
  const num = typeof rate === 'string' ? parseFloat(rate) : rate
  if (isNaN(num)) return '0%'
  return `${(num * 100).toFixed(1)}%`
}
