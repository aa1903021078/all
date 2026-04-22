import request from '../utils/request'

// User APIs
export const login = (data) => request.post('/user/login', data)
export const register = (data) => request.post('/user/register', data)
export const getUserInfo = (id) => request.get(`/user/info/${id}`)
export const updateUser = (data) => request.put('/user/update', data)
export const getUserList = (params) => request.get('/user/list', { params })
export const addUser = (data) => request.post('/user/add', data)
export const deleteUser = (id) => request.delete(`/user/delete/${id}`)
export const getStaffList = () => request.get('/user/staff')

// Service APIs
export const getServiceList = (params) => request.get('/service/list', { params })
export const getServiceDetail = (id) => request.get(`/service/detail/${id}`)
export const addService = (data) => request.post('/service/add', data)
export const updateService = (data) => request.put('/service/update', data)
export const deleteService = (id) => request.delete(`/service/delete/${id}`)

// Package APIs
export const getPackageList = (params) => request.get('/package/list', { params })
export const getPackageDetail = (id) => request.get(`/package/detail/${id}`)
export const addPackage = (data) => request.post('/package/add', data)
export const updatePackage = (data) => request.put('/package/update', data)
export const deletePackage = (id) => request.delete(`/package/delete/${id}`)

// Order APIs
export const getOrderList = (params) => request.get('/order/list', { params })
export const getOrderDetail = (id) => request.get(`/order/detail/${id}`)
export const addOrder = (data) => request.post('/order/add', data)
export const updateOrder = (data) => request.put('/order/update', data)
export const updateOrderStatus = (data) => request.put('/order/status', data)
export const deleteOrder = (id) => request.delete(`/order/delete/${id}`)
export const getMyOrders = (customerId) => request.get(`/order/my/${customerId}`)

// Order Staff APIs
export const getOrderStaffList = (params) => request.get('/orderStaff/list', { params })
export const addOrderStaff = (data) => request.post('/orderStaff/add', data)
export const deleteOrderStaff = (id) => request.delete(`/orderStaff/delete/${id}`)
export const getStaffByOrder = (orderId) => request.get(`/orderStaff/byOrder/${orderId}`)

// Order Infant APIs
export const getInfantsByOrder = (orderId) => request.get(`/orderInfant/byOrder/${orderId}`)
export const addOrderInfant = (data) => request.post('/orderInfant/add', data)
export const updateOrderInfant = (data) => request.put('/orderInfant/update', data)
export const deleteOrderInfant = (id) => request.delete(`/orderInfant/delete/${id}`)

// Order Maternal APIs
export const getMaternalByOrder = (orderId) => request.get(`/orderMaternal/byOrder/${orderId}`)
export const addOrderMaternal = (data) => request.post('/orderMaternal/add', data)
export const updateOrderMaternal = (data) => request.put('/orderMaternal/update', data)
export const deleteOrderMaternal = (id) => request.delete(`/orderMaternal/delete/${id}`)

// Care Record Life APIs
export const getCareLifeList = (params) => request.get('/careLife/list', { params })
export const getCareLifeDetail = (id) => request.get(`/careLife/detail/${id}`)
export const addCareLife = (data) => request.post('/careLife/add', data)
export const updateCareLife = (data) => request.put('/careLife/update', data)
export const deleteCareLife = (id) => request.delete(`/careLife/delete/${id}`)

// Care Record Medical APIs
export const getCareMedicalList = (params) => request.get('/careMedical/list', { params })
export const getCareMedicalDetail = (id) => request.get(`/careMedical/detail/${id}`)
export const addCareMedical = (data) => request.post('/careMedical/add', data)
export const updateCareMedical = (data) => request.put('/careMedical/update', data)
export const deleteCareMedical = (id) => request.delete(`/careMedical/delete/${id}`)

// Diet Plan APIs
export const getDietPlanList = (params) => request.get('/dietPlan/list', { params })
export const getDietPlanDetail = (id) => request.get(`/dietPlan/detail/${id}`)
export const addDietPlan = (data) => request.post('/dietPlan/add', data)
export const updateDietPlan = (data) => request.put('/dietPlan/update', data)
export const deleteDietPlan = (id) => request.delete(`/dietPlan/delete/${id}`)

// Evaluation APIs
export const getEvaluationList = (params) => request.get('/evaluation/list', { params })
export const addEvaluation = (data) => request.post('/evaluation/add', data)
export const deleteEvaluation = (id) => request.delete(`/evaluation/delete/${id}`)

// Complaint APIs
export const getComplaintList = (params) => request.get('/complaint/list', { params })
export const addComplaint = (data) => request.post('/complaint/add', data)
export const replyComplaint = (data) => request.put('/complaint/reply', data)
export const deleteComplaint = (id) => request.delete(`/complaint/delete/${id}`)

// Staff Change Request APIs
export const getStaffChangeList = (params) => request.get('/staffChange/list', { params })
export const addStaffChange = (data) => request.post('/staffChange/add', data)
export const handleStaffChange = (data) => request.put('/staffChange/handle', data)
export const deleteStaffChange = (id) => request.delete(`/staffChange/delete/${id}`)

// File Upload
export const uploadFile = (formData) => request.post('/file/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
