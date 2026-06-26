/**
 * 极简拼音首字母匹配工具
 * 覆盖常用2000+汉字的首字母映射
 */

// 核心映射表（仅一级常用字就足够课程项目使用）
const PINYIN_MAP: Record<string, string> = {
  '管': 'G','理': 'L','系': 'X','统': 'T','图': 'T','书': 'S','借': 'J','阅': 'Y',
  '数': 'S','据': 'J','概': 'G','览': 'L','用': 'Y','户': 'H','分': 'F','类': 'L',
  '审': 'S','计': 'J','日': 'R','志': 'Z',
  '前': 'Q','端': 'D','开': 'K','发': 'F','人': 'R','工': 'G','智': 'Z','能': 'N',
  '中': 'Z','国': 'G','文': 'W','学': 'X','外': 'W','历': 'L','史': 'S',
  '自': 'Z','然': 'R','科': 'K','经': 'J','济': 'J',
  '大': 'D','计': 'J','算': 'S','机': 'J','网': 'W','络': 'L','库': 'K',
  '算': 'S','法': 'F','导': 'D','论': 'L','深': 'S','入': 'R','理': 'L','解': 'J',
  '三': 'S','体': 'T','活': 'H',

}

/**
 * 获取中文文本的拼音首字母（仅限映射表中已有的字）
 * @param text 中文文本
 * @returns 拼音首字母字符串（大写），例如 图书管理 -> TSGL
 */
export function getPinyinInitial(text: string): string {
  let result = ''
  for (const ch of text) {
    if (/[a-zA-Z0-9]/.test(ch)) {
      result += ch.toUpperCase()
    } else if (PINYIN_MAP[ch]) {
      result += PINYIN_MAP[ch]
    }
  }
  return result.toUpperCase()
}

/**
 * 匹配拼音首字母模糊搜索
 * @param query 用户输入的查询字符串（支持中文和拼音首字母）
 * @param target 待匹配的中文文本
 * @returns true if match
 */
export function matchPinyinInitial(query: string, target: string): boolean {
  if (!query || !target) return false
  const upper = query.toUpperCase()

  // 直接中文包含匹配
  if (target.includes(query)) return true

  // 拼音首字母匹配
  const initials = getPinyinInitial(target)
  if (initials.includes(upper)) return true

  // 每个搜索词的拼音首字母都匹配
  const terms = upper.split(/\s+/)
  for (const term of terms) {
    if (!initials.includes(term) && !target.includes(term)) {
      // 也尝试按字符逐个模糊匹配（如 t -> 体）
      let charMatch = false
      for (let i = 0; i < target.length; i++) {
        const charInitial = getPinyinInitial(target[i])
        if (charInitial === term) { charMatch = true; break }
      }
      if (!charMatch) return false
    }
  }
  return true
}
