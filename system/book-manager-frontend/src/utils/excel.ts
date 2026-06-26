import * as XLSX from 'xlsx'

export interface ColumnDef {
  label: string
  key: string
}

export function exportToExcel(data: any[], columns: ColumnDef[], fileName: string) {
  const header = columns.map(c => c.label)
  const rows = data.map(row => columns.map(c => row[c.key] ?? ''))
  const sheet = XLSX.utils.aoa_to_sheet([header, ...rows])

  // 设置列宽
  const colWidths = columns.map(() => ({ wch: 18 }))
  sheet['!cols'] = colWidths

  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, sheet, 'Sheet1')
  XLSX.writeFile(wb, `${fileName}.xlsx`)
}
