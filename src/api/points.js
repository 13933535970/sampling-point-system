import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE,
  timeout: 10000
})

export const pointApi = {
  // 获取所有采样点
  getAll() {
    return api.get('/points')
  },
  
  // 新增采样点
  create(data) {
    return api.post('/points', data)
  },
  
  // 更新采样点
  update(id, data) {
    return api.put(`/points/${id}`, data)
  },
  
  // 删除采样点
  delete(id) {
    return api.delete(`/points/${id}`)
  },
  
  // 按类型筛选
  filterByType(type) {
    return api.get('/points/filter', { params: { type } })
  },
  
  // 半径搜索
  searchByRadius(lng, lat, radius) {
    return api.get('/points/spatial/radius', { params: { lng, lat, radius } })
  },
  
  // 按名称搜索 
  searchByName(keyword) {
    return api.get('/points/search', { params: { keyword } })
  }
}