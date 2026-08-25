import request from '../utils/request'

/* ===== 用户 ===== */
export function login(data: { username: string; password: string }) {
  return request.post('/user/login', data)
}

export function register(data: {
  username: string
  password: string
  name: string
  phone?: string
  email?: string
}) {
  return request.post('/user/register', data)
}

export function getUserInfo() {
  return request.get('/user/info')
}

export function getUsers() {
  return request.get('/user/list')
}

// 分页
export function getUsersPage(params?: { page?: number; pageSize?: number }) {
  return request.get('/user/page', { params })
}

export function updateUserStatus(id: number, status: number) {
  return request.post('/user/status', null, { params: { id, status } })
}

export function updateUser(data: any) {
  return request.post('/user/update', data)
}

export function deleteUser(id: number) {
  return request.delete(`/user/${id}`)
}

export function batchDeleteUsers(ids: number[]) {
  return request.post('/user/batch-delete', { ids })
}

/* ===== 图书 ===== */
export function getBooks(keyword?: string) {
  if (keyword) {
    return request.get('/book/search', { params: { keyword } })
  }
  return request.get('/book/list')
}

// 分页
export function getBooksPage(params?: { page?: number; pageSize?: number; keyword?: string; categoryId?: number; stockStatus?: string; startDate?: string; endDate?: string }) {
  return request.get('/book/page', { params })
}

export function getBooksWithFilters(params?: {
  keyword?: string
  categoryId?: number
  stockStatus?: string
  startDate?: string
  endDate?: string
}) {
  return request.get('/book/search', { params })
}

export function getBookDetail(id: number) {
  return request.get(`/book/${id}`)
}

export function getBooksByCategory(categoryId: number) {
  return request.get(`/book/category/${categoryId}`)
}

export function addBook(data: any) {
  return request.post('/book/add', data)
}

export function updateBook(data: any) {
  return request.put('/book/update', data)
}

export function updateBookStatus(id: number, status: number) {
  return request.put('/book/status', null, { params: { id, status } })
}

export function deleteBook(id: number) {
  return request.delete(`/book/${id}`)
}

export function batchDeleteBooks(ids: number[]) {
  return request.post('/book/batch-delete', { ids })
}

export function batchUpdateBookCategory(ids: number[], categoryId: number) {
  return request.put('/book/batch-category', { ids, categoryId })
}

/* ===== 借阅 ===== */
export function borrowBook(bookId: number) {
  return request.post('/borrow/add', null, { params: { bookId } })
}

export function returnBook(id: number) {
  return request.post(`/borrow/return/${id}`)
}

export function getBorrowRecords() {
  return request.get('/borrow/list')
}

// 分页
export function getBorrowRecordsPage(params?: { page?: number; pageSize?: number; status?: string; startDate?: string; endDate?: string }) {
  return request.get('/borrow/page', { params })
}

export function getBorrowRecordsWithFilters(params?: {
  status?: string
  startDate?: string
  endDate?: string
}) {
  return request.get('/borrow/list', { params })
}

export function getUserBorrowRecords() {
  return request.get('/borrow/user')
}

export function getBorrowRecordsByStatus(status: string) {
  return request.get(`/borrow/status/${status}`)
}

/* ===== 分类 ===== */
export function getCategories() {
  return request.get('/category/list')
}

export function getCategoryTree() {
  return request.get('/category/tree')
}

export function getCategoriesEnabled() {
  return request.get('/category/enabled')
}

export function addCategory(data: any) {
  return request.post('/category/add', data)
}

export function updateCategory(data: any) {
  return request.put('/category/update', data)
}

export function updateCategoryStatus(id: number, status: number) {
  return request.put('/category/status', null, { params: { id, status } })
}

export function updateCategorySort(id: number, sortOrder: number) {
  return request.put('/category/sort', null, { params: { id, sortOrder } })
}

export function deleteCategory(id: number) {
  return request.delete(`/category/${id}`)
}

export function batchDeleteCategories(ids: number[]) {
  return request.post('/category/batch-delete', { ids })
}

/* ===== 审计日志 ===== */
export function getAuditLogs(params?: {
  action?: string
  targetType?: string
  operatorName?: string
  startDate?: string
  endDate?: string
}) {
  return request.get('/audit/list', { params })
}

// 分页
export function getAuditLogsPage(params?: { page?: number; pageSize?: number; action?: string; targetType?: string; operatorName?: string; startDate?: string; endDate?: string }) {
  return request.get('/audit/page', { params })
}

/* ===== 通知 ===== */
export function getNotifications() {
  return request.get('/notice/list')
}

export function getUnreadCount() {
  return request.get('/notice/unread-count')
}

export function markNotificationRead(id: number) {
  return request.put(`/notice/read/${id}`)
}

export function markAllNotificationsRead() {
  return request.put('/notice/read-all')
}

/* ===== 罚款规则 ===== */
export function getFineRule() {
  return request.get('/fine-rule')
}

export function updateFineRule(data: any) {
  return request.put('/fine-rule', data)
}
