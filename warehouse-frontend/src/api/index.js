import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
      config.headers['X-User-Id'] = token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    const message = error.response?.data?.message || error.message
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 通用请求方法
const request = {
  get(url, params) {
    return api.get(url, { params })
  },
  post(url, data) {
    return api.post(url, data)
  },
  put(url, data) {
    return api.put(url, data)
  },
  delete(url) {
    return api.delete(url)
  }
}

// API 方法
export default {
  // 通用方法
  ...request,

  // 登录
  login(data) {
    return api.post('/user/login', data)
  },

  // 农产品
  getProducts() {
    return api.get('/products')
  },
  getProduct(id) {
    return api.get(`/products/${id}`)
  },
  createProduct(data) {
    return api.post('/products', data)
  },
  updateProduct(id, data) {
    return api.put(`/products/${id}`, data)
  },
  deleteProduct(id) {
    return api.delete(`/products/${id}`)
  },
  searchProducts(name) {
    return api.get(`/products/search?name=${name}`)
  },

  // 货位
  getLocations() {
    return api.get('/locations')
  },
  getFreeLocations() {
    return api.get('/locations/free')
  },
  createLocation(data) {
    return api.post('/locations', data)
  },
  updateLocation(id, data) {
    return api.put(`/locations/${id}`, data)
  },
  deleteLocation(id) {
    return api.delete(`/locations/${id}`)
  },

  // 入库
  inbound(data) {
    return api.post('/inbound', data)
  },
  getInboundRecords() {
    return api.get('/inbound')
  },

  // 出库
  outbound(data) {
    return api.post('/outbound', data)
  },
  getOutboundRecords() {
    return api.get('/outbound')
  },
  // 操作日志
  getLogs(params) {
    return request.post('/logs/query', params)
  },
  getMyLogs(page, size) {
    return request.get(`/logs/my?page=${page}&size=${size}`)
  },
  getLogStatistics() {
    return request.get('/logs/statistics')
  },
  // 用户注册
  register(data) {
    return api.post('/user/register', data)
  },
  // 供应商
  getSuppliers() {
    return api.get('/suppliers')
  },
  getSupplier(id) {
    return api.get(`/suppliers/${id}`)
  },
  createSupplier(data) {
    return api.post('/suppliers', data)
  },
  updateSupplier(id, data) {
    return api.put(`/suppliers/${id}`, data)
  },
  deleteSupplier(id) {
    return api.delete(`/suppliers/${id}`)
  },
}