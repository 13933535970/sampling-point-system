import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { pointApi } from '../api/points'

export const usePointStore = defineStore('point', () => {
  // 数据
  const points = ref([])           // 所有采样点
  const filteredPoints = ref([])   // 筛选后的采样点
  const tempLocation = ref(null)   // 临时存储点击的位置
  const searchKeyword = ref('')  // 新增：搜索关键词

  // 获取所有点
  const fetchPoints = async () => {
    const res = await pointApi.getAll()
    points.value = res.data
    filteredPoints.value = res.data
    return res.data
  }
  
  // 新增点
  const createPoint = async (data) => {
    const res = await pointApi.create(data)
    points.value.push(res.data)
    filteredPoints.value.push(res.data)
    tempLocation.value = null
    return res.data
  }
  
  // 更新点
  const updatePoint = async (id, data) => {
    const res = await pointApi.update(id, data)
    const index = points.value.findIndex(p => p.id === id)
    points.value[index] = res.data
    
    const filterIndex = filteredPoints.value.findIndex(p => p.id === id)
    if (filterIndex !== -1) filteredPoints.value[filterIndex] = res.data
    return res.data
  }
  
  // 删除点
  const deletePoint = async (id) => {
    await pointApi.delete(id)
    points.value = points.value.filter(p => p.id !== id)
    filteredPoints.value = filteredPoints.value.filter(p => p.id !== id)
  }
  
  // 按类型筛选
  const filterByType = async (type) => {
    if (!type) {
      filteredPoints.value = points.value
      return
    }
    const res = await pointApi.filterByType(type)
    filteredPoints.value = res.data
  }
  
  // 半径搜索
  const searchByRadius = async (lng, lat, radius) => {
    const res = await pointApi.searchByRadius(lng, lat, radius)
    filteredPoints.value = res.data
    return res.data
  }

  // ===== 新增：按名称搜索 =====
  const searchByName = async (keyword) => {
    searchKeyword.value = keyword
    if (!keyword || keyword.trim() === '') {
      filteredPoints.value = points.value
      return
    }
    const res = await pointApi.searchByName(keyword.trim())
    filteredPoints.value = res.data
    return res.data
  }

  // 清除筛选
  const clearFilter = () => {
    filteredPoints.value = points.value
  }
  
  // 设置临时位置
  const setTempLocation = (location) => {
    tempLocation.value = location
  }
  
  // 统计各类型数量
  const typeStats = computed(() => {
    const stats = {}
    points.value.forEach(p => {
      const type = p.type || '未分类'
      stats[type] = (stats[type] || 0) + 1
    })
    return stats
  })
  
  return {
    points,
    filteredPoints,
    tempLocation,
    searchKeyword,
    typeStats,
    fetchPoints,
    createPoint,
    updatePoint,
    deletePoint,
    filterByType,
    searchByRadius,
    searchByName,      // 新增
    clearFilter,
    setTempLocation
  }
})