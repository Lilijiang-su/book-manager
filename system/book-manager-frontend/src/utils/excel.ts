import * as XLSX from 'xlsx'

export interface ColumnDef {
  label: string
  key: string
}

/**
 * 防止 Excel 公式注入：以 = + - @ 开头的单元格值会被当作公式执行，
 * 在此统一加单引号前缀使其按纯文本处理。
 */
function sanitizeCell(value: unknown): unknown {
  if (typeof value === 'string' && /^[=+\-@]/.test(value)) {
    return `'${value}`
  }
  return value ?? ''
}

export function exportToExcel(data: any[], columns: ColumnDef[], fileName: string) {
  const header = columns.map(c => c.label)
  const rows = data.map(row => columns.map(c => sanitizeCell(row[c.key])))
  const sheet = XLSX.utils.aoa_to_sheet([header, ...rows])

  // 设置列宽
  const colWidths = columns.map(() => ({ wch: 18 }))
  sheet['!cols'] = colWidths

  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, sheet, 'Sheet1')
  XLSX.writeFile(wb, `${fileName}.xlsx`)
}
