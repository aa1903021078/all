import request from './request'

// ---------------- 认证 ----------------
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  me: () => request.get('/auth/me'),
  logout: () => request.post('/auth/logout'),
}

// ---------------- 用户 ----------------
export const userApi = {
  profile: (id) => request.get(`/users/${id}`),
  updateProfile: (data) => request.put('/users/profile', data),
  adminPage: (params) => request.get('/users/admin/page', { params }),
  changeStatus: (id, status) => request.put(`/users/${id}/status`, null, { params: { status } }),
}

// ---------------- 文件上传 ----------------
export const fileApi = {
  uploadImage: (file) => {
    const fd = new FormData()
    fd.append('file', file)
    return request.post('/files/image', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
}

// ---------------- 菜系 ----------------
export const categoryApi = {
  list: () => request.get('/categories'),
  save: (data) => request.post('/categories', data),
  update: (id, data) => request.put(`/categories/${id}`, data),
  remove: (id) => request.delete(`/categories/${id}`),
}

// ---------------- 店铺 / 探店 ----------------
export const shopApi = {
  page: (params) => request.get('/shops', { params }),
  detail: (id) => request.get(`/shops/${id}`),
  map: (params) => request.get('/shops/map', { params }),
  nearby: (params) => request.get('/shops/nearby', { params }),
  recommend: (limit = 6) => request.get('/shops/recommend', { params: { limit } }),
  ranking: (limit = 10) => request.get('/shops/ranking', { params: { limit } }),
  dishes: (id) => request.get(`/shops/${id}/dishes`),
  checkin: (id) => request.post(`/shops/${id}/checkin`),
  myCheckin: () => request.get('/shops/my-checkin'),
  mine: () => request.get('/shops/mine'),
  merchantCreate: (data) => request.post('/shops/merchant', data),
  merchantUpdate: (id, data) => request.put(`/shops/merchant/${id}`, data),
  saveDish: (data) => request.post('/shops/dishes', data),
  updateDish: (id, data) => request.put(`/shops/dishes/${id}`, data),
  deleteDish: (id) => request.delete(`/shops/dishes/${id}`),
  adminPage: (params) => request.get('/shops/admin/page', { params }),
  adminCreate: (data) => request.post('/shops/admin', data),
  adminUpdate: (id, data) => request.put(`/shops/admin/${id}`, data),
  changeStatus: (id, status) => request.put(`/shops/${id}/status`, null, { params: { status } }),
  setRecommend: (id, recommend) => request.put(`/shops/${id}/recommend`, null, { params: { recommend } }),
  remove: (id) => request.delete(`/shops/${id}`),
}

// ---------------- 菜谱 ----------------
export const recipeApi = {
  page: (params) => request.get('/recipes', { params }),
  detail: (id) => request.get(`/recipes/${id}`),
  recommend: (limit = 6) => request.get('/recipes/recommend', { params: { limit } }),
  mine: () => request.get('/recipes/mine'),
  create: (data) => request.post('/recipes', data),
  update: (id, data) => request.put(`/recipes/${id}`, data),
  remove: (id) => request.delete(`/recipes/${id}`),
  like: (id) => request.post(`/recipes/${id}/like`),
  favorite: (id, folderId) => request.post(`/recipes/${id}/favorite`, null, { params: { folderId } }),
  reposts: (id) => request.get(`/recipes/${id}/reposts`),
  addRepost: (id, data) => request.post(`/recipes/${id}/reposts`, data),
  adminPage: (params) => request.get('/recipes/admin/page', { params }),
  adminUpdate: (id, data) => request.put(`/recipes/admin/${id}`, data),
  adminRemove: (id) => request.delete(`/recipes/admin/${id}`),
  changeStatus: (id, status) => request.put(`/recipes/${id}/status`, null, { params: { status } }),
  setRecommend: (id, recommend) => request.put(`/recipes/${id}/recommend`, null, { params: { recommend } }),
}

// ---------------- 探店笔记 ----------------
export const noteApi = {
  page: (params) => request.get('/notes', { params }),
  feed: (limit = 10) => request.get('/notes/feed', { params: { limit } }),
  mine: () => request.get('/notes/mine'),
  byShop: (shopId) => request.get(`/notes/shop/${shopId}`),
  detail: (id) => request.get(`/notes/${id}`),
  create: (data) => request.post('/notes', data),
  remove: (id) => request.delete(`/notes/${id}`),
  like: (id) => request.post(`/notes/${id}/like`),
  adminPage: (params) => request.get('/notes/admin/page', { params }),
  adminUpdate: (id, data) => request.put(`/notes/admin/${id}`, data),
  adminRemove: (id) => request.delete(`/notes/admin/${id}`),
  review: (id, status) => request.put(`/notes/${id}/status`, null, { params: { status } }),
  setRecommend: (id, recommend) => request.put(`/notes/${id}/recommend`, null, { params: { recommend } }),
}

// ---------------- 评论 ----------------
export const commentApi = {
  list: (targetType, targetId) => request.get('/comments', { params: { targetType, targetId } }),
  add: (data) => request.post('/comments', data),
  remove: (id) => request.delete(`/comments/${id}`),
  like: (id) => request.post(`/comments/${id}/like`),
}

// ---------------- 收藏 ----------------
export const favoriteApi = {
  toggle: (targetType, targetId, folderId) =>
    request.post('/favorites/toggle', null, { params: { targetType, targetId, folderId } }),
  shops: (folderId) => request.get('/favorites/shops', { params: { folderId } }),
  recipes: (folderId) => request.get('/favorites/recipes', { params: { folderId } }),
  folders: (type) => request.get('/favorites/folders', { params: { type } }),
  createFolder: (data) => request.post('/favorites/folders', data),
  deleteFolder: (id) => request.delete(`/favorites/folders/${id}`),
}

// ---------------- 采购清单 ----------------
export const shoppingApi = {
  list: () => request.get('/shopping'),
  add: (data) => request.post('/shopping', data),
  addFromRecipe: (recipeId) => request.post(`/shopping/from-recipe/${recipeId}`),
  update: (id, data) => request.put(`/shopping/${id}`, data),
  updateStatus: (id, status) => request.put(`/shopping/${id}/status`, null, { params: { status } }),
  remove: (id) => request.delete(`/shopping/${id}`),
  clearDone: () => request.delete('/shopping/done'),
}

// ---------------- 预约 ----------------
export const reservationApi = {
  create: (data) => request.post('/reservations', data),
  mine: () => request.get('/reservations/mine'),
  cancel: (id) => request.put(`/reservations/${id}/cancel`),
  merchant: (status) => request.get('/reservations/merchant', { params: { status } }),
  handle: (id, status) => request.put(`/reservations/${id}/handle`, null, { params: { status } }),
}

// ---------------- 统计 ----------------
export const statsApi = {
  overview: () => request.get('/stats/overview'),
  categoryDistribution: () => request.get('/stats/category-distribution'),
  priceDistribution: () => request.get('/stats/price-distribution'),
  publishTrend: () => request.get('/stats/publish-trend'),
  dashboard: () => request.get('/stats/dashboard'),
  merchant: () => request.get('/stats/merchant'),
  merchantCheckinTrend: (shopId) => request.get('/stats/merchant/checkin-trend', { params: { shopId } }),
}

// ---------------- 搜索 ----------------
export const searchApi = {
  search: (keyword) => request.get('/search', { params: { keyword } }),
  hot: (limit = 10) => request.get('/search/hot', { params: { limit } }),
}

// ---------------- 聊天 ----------------
export const chatApi = {
  session: (shopId) => request.post('/chat/session', null, { params: { shopId } }),
  sessions: () => request.get('/chat/sessions'),
  messages: (id) => request.get(`/chat/sessions/${id}/messages`),
  read: (id) => request.put(`/chat/sessions/${id}/read`),
}

// ---------------- 我的消息 ----------------
export const messageApi = {
  mine: () => request.get('/messages'),
}

// ---------------- 差评申诉 ----------------
export const appealApi = {
  submit: (data) => request.post('/appeals', data),
  mine: () => request.get('/appeals/mine'),
  adminList: (status) => request.get('/appeals/admin', { params: { status } }),
  handle: (id, status, reply) => request.put(`/appeals/${id}/handle`, null, { params: { status, reply } }),
}

// ---------------- 系统 / RBAC ----------------
export const systemApi = {
  roles: () => request.get('/system/roles'),
  permissions: () => request.get('/system/permissions'),
  userRoles: (userId) => request.get(`/system/users/${userId}/roles`),
  assignUserRoles: (userId, roleIds) => request.put(`/system/users/${userId}/roles`, roleIds),
  rolePermissions: (roleId) => request.get(`/system/roles/${roleId}/permissions`),
  assignRolePermissions: (roleId, permissionIds) => request.put(`/system/roles/${roleId}/permissions`, permissionIds),
  publicConfigs: () => request.get('/system/configs/public'),
  configs: () => request.get('/system/configs'),
  saveConfig: (data) => request.post('/system/configs', data),
  updateConfig: (id, value) => request.put(`/system/configs/${id}`, null, { params: { value } }),
}
