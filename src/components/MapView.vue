<template>
  <div class="map-container">
    <div ref="mapContainer" class="map"></div>
    
    <!-- 地图控制栏 -->
    <div class="map-controls">
      <!-- 搜索框 -->
      <div class="search-group">
        <input 
          type="text" 
          v-model="searchText" 
          placeholder="🔍 搜索采样点名称..."
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <button class="btn btn-success" @click="handleSearch">搜索</button>
        <button class="btn btn-warning" @click="clearSearch" v-if="searchText">✕</button>
      </div>
      
      <button 
        :class="['btn', addMode ? 'btn-danger' : 'btn-primary']"
        @click="toggleAddMode"
      >
        {{ addMode ? '❌ 退出' : '➕ 添加点' }}
      </button>
      
      <div class="radius-group">
        <input 
          type="number" 
          v-model.number="searchRadius" 
          placeholder="半径(米)"
          class="radius-input"
        />
        <button class="btn btn-primary" @click="searchByRadius">半径</button>
      </div>
    </div>
    
    <!-- 搜索结果提示 -->
    <div v-if="searchResultCount > 0" class="search-result-info">
      🎯 找到 <strong>{{ searchResultCount }}</strong> 个结果
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import L from 'leaflet'
import { usePointStore } from '../stores/pointStore'
import { ElMessage } from 'element-plus'

// ============================================
// 1. 修复 Leaflet 默认图标
// ============================================
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
})

// ============================================
// 2. 自定义导航定位图标
// ============================================
const locationIcon = L.divIcon({
  className: 'custom-location-icon',
  html: `
    <div class="location-marker">
      <div class="location-pulse"></div>
      <div class="location-dot"></div>
      <div class="location-ring"></div>
    </div>
  `,
  iconSize: [40, 40],
  iconAnchor: [20, 20],
  popupAnchor: [0, -20]
})

const searchHighlightIcon = L.divIcon({
  className: 'custom-location-icon search-highlight',
  html: `
    <div class="location-marker search-marker">
      <div class="location-pulse search-pulse"></div>
      <div class="location-dot search-dot"></div>
      <div class="location-ring search-ring"></div>
      <div class="search-label">📍 搜索结果</div>
    </div>
  `,
  iconSize: [50, 60],
  iconAnchor: [25, 30],
  popupAnchor: [0, -30]
})

// ============================================
// 3. 组件变量
// ============================================
const emit = defineEmits(['openForm', 'editPoint'])

const mapContainer = ref(null)
const addMode = ref(false)
const searchRadius = ref(1000)
const searchText = ref('')
const searchResultCount = ref(0)

let map = null
let markers = {}
let highlightLayer = null

const pointStore = usePointStore()

// ============================================
// 4. 获取经纬度（增强版，更健壮）
// ============================================
const getLngLat = (point) => {
  if (!point) return { lng: null, lat: null }
  
  let lng = null, lat = null
  
  // 如果 point.location 存在
  if (point.location) {
    const loc = point.location
    
    // 格式1：{x, y} 或 {x: 116, y: 39}
    if (loc.x !== undefined && loc.x !== null) {
      lng = parseFloat(loc.x)
      lat = parseFloat(loc.y)
      return { lng, lat }
    }
    
    // 格式2：{coordinates: [lng, lat]}
    if (Array.isArray(loc.coordinates) && loc.coordinates.length >= 2) {
      lng = parseFloat(loc.coordinates[0])
      lat = parseFloat(loc.coordinates[1])
      return { lng, lat }
    }
    
    // 格式3：字符串 "POINT(lng lat)"
    if (typeof loc === 'string') {
      const match = loc.match(/POINT\s*\(([\d.]+)\s+([\d.]+)\)/i)
      if (match) {
        lng = parseFloat(match[1])
        lat = parseFloat(match[2])
        return { lng, lat }
      }
    }
    
    // 格式4：对象转字符串后解析
    try {
      const str = String(loc)
      const match = str.match(/POINT\s*\(([\d.]+)\s+([\d.]+)\)/i)
      if (match) {
        lng = parseFloat(match[1])
        lat = parseFloat(match[2])
        return { lng, lat }
      }
    } catch (e) {}
  }
  
  // 如果 point 本身有 lng/lat
  if (point.lng !== undefined && point.lat !== undefined) {
    lng = parseFloat(point.lng)
    lat = parseFloat(point.lat)
    return { lng, lat }
  }
  
  console.warn('⚠️ 无法解析坐标:', point)
  return { lng: null, lat: null }
}


// ============================================
// 5. 判断点是否有效
// ============================================
const isValidPoint = (point) => {
  const { lng, lat } = getLngLat(point)
  return lng !== null && lat !== null && !isNaN(lng) && !isNaN(lat)
}

// ============================================
// 6. 过滤有效点
// ============================================
const filterValidPoints = (points) => {
  if (!points || !Array.isArray(points)) return []
  return points.filter(p => isValidPoint(p))
}

// ============================================
// 7. 地图初始化
// ============================================
const initMap = () => {
  if (!mapContainer.value) return
  
  map = L.map(mapContainer.value, {
    center: [39.9042, 116.4074],
    zoom: 13
  })
  
  L.tileLayer('https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
    attribution: '© 高德地图',
    maxZoom: 18
  }).addTo(map)
  
  map.on('click', onMapClick)
  loadPointsToMap()
}

const onMapClick = (e) => {
  if (!addMode.value) return
  const { lat, lng } = e.latlng
  pointStore.setTempLocation({ lng, lat })
  emit('openForm')
  addMode.value = false
}

// ============================================
// 8. 加载和更新标记（修复版）
// ============================================
const loadPointsToMap = async () => {
  try {
    await pointStore.fetchPoints()
    updateMarkers()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请检查后端服务')
  }
}

const updateMarkers = () => {
  // 清除旧标记
  Object.values(markers).forEach(marker => marker.remove())
  markers = {}
  
  // 获取数据
  const rawPoints = pointStore.filteredPoints.length > 0 
    ? pointStore.filteredPoints 
    : pointStore.points
  
  // ===== 关键修复：严格过滤无效数据 =====
  const validPoints = rawPoints.filter(point => {
    if (!point) return false
    if (!point.location) return false
    
    const { lng, lat } = getLngLat(point)
    const isValid = lng !== null && lat !== null && !isNaN(lng) && !isNaN(lat)
    
    if (!isValid) {
      console.warn('⚠️ 跳过无效点:', point.id, point.name, point.location)
    }
    return isValid
  })
  
  if (validPoints.length === 0) {
    console.log('⚠️ 没有有效的采样点数据')
    return
  }
  
  console.log(`✅ 有效采样点: ${validPoints.length} 个`)
  
  validPoints.forEach(point => {
    const { lng, lat } = getLngLat(point)
    if (lng === null || lat === null) return
    
    try {
      const marker = L.marker([lat, lng], { icon: locationIcon }).addTo(map)
      
      marker.bindPopup(`
        <div style="min-width:150px;">
          <b>📍 ${point.name || '未命名'}</b><br/>
          📂 类型: ${point.type || '未分类'}<br/>
          👤 采集人: ${point.collector || '未知'}<br/>
          <button onclick="window.editPoint(${point.id})" style="cursor:pointer;margin-top:4px;">✏️ 编辑</button>
          <button onclick="window.deletePoint(${point.id})" style="cursor:pointer;margin-top:4px;margin-left:4px;">🗑️ 删除</button>
        </div>
      `)
      markers[point.id] = marker
    } catch (e) {
      console.warn('添加标记失败:', point.id, e)
    }
  })
}

// ============================================
// 9. 搜索功能（修复版）
// ============================================
const handleSearch = async () => {
  const keyword = searchText.value?.trim()
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  try {
    console.log('🔍 搜索关键词:', keyword)
    await pointStore.searchByName(keyword)
    const results = pointStore.filteredPoints
    console.log('📊 搜索结果数量:', results.length)

    // 先更新普通标记
    updateMarkers()
    // 再高亮搜索结果
    highlightSearchResults(results)
    
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请检查后端服务')
  }
}

// ============================================
// 10. 高亮搜索结果（修复版）
// ============================================
const highlightSearchResults = (results) => {
  // 清除旧高亮
  if (highlightLayer) {
    highlightLayer.clearLayers()
    map.removeLayer(highlightLayer)
    highlightLayer = null
  }

  // ===== 关键修复：严格过滤 =====
  const validResults = results.filter(point => {
    if (!point) return false
    if (!point.location) return false
    const { lng, lat } = getLngLat(point)
    return lng !== null && lat !== null && !isNaN(lng) && !isNaN(lat)
  })

  searchResultCount.value = validResults.length

  if (validResults.length === 0) {
    ElMessage.info('未找到匹配的采样点')
    return
  }

  highlightLayer = L.layerGroup().addTo(map)

  const bounds = []

  validResults.forEach((point, index) => {
    const { lng, lat } = getLngLat(point)
    if (lng === null || lat === null) return
    
    bounds.push([lat, lng])

    try {
      const marker = L.marker([lat, lng], { icon: searchHighlightIcon }).addTo(highlightLayer)
      
      marker.bindPopup(`
        <div style="min-width:150px; background:#fff3e0; border:2px solid #ff6b35; border-radius:8px; padding:8px;">
          <b style="color:#ff6b35;">⭐ 搜索结果 #${index + 1}</b><br/>
          <b>📍 ${point.name || '未命名'}</b><br/>
          📂 类型: ${point.type || '未分类'}<br/>
          👤 采集人: ${point.collector || '未知'}
        </div>
      `)

      if (index === 0) {
        setTimeout(() => marker.openPopup(), 500)
      }
    } catch (e) {
      console.warn('添加高亮标记失败:', point.id, e)
    }
  })

  if (bounds.length > 0) {
    try {
      const latLngBounds = L.latLngBounds(bounds)
      map.fitBounds(latLngBounds, { padding: [80, 80], maxZoom: 16 })
    } catch (e) {
      console.warn('缩放失败:', e)
    }
  }

  ElMessage.success(`🎯 找到 ${validResults.length} 个匹配的采样点`)
}

// ============================================
// 11. 清除搜索
// ============================================
const clearSearch = () => {
  searchText.value = ''
  searchResultCount.value = 0
  
  if (highlightLayer) {
    highlightLayer.clearLayers()
    map.removeLayer(highlightLayer)
    highlightLayer = null
  }
  
  pointStore.clearFilter()
  updateMarkers()
  ElMessage.info('已清除搜索')
}

// ============================================
// 12. 半径搜索
// ============================================
const searchByRadius = async () => {
  if (!map) return
  const center = map.getCenter()
  try {
    await pointStore.searchByRadius(center.lng, center.lat, searchRadius.value)
    updateMarkers()
    ElMessage.success(`找到 ${pointStore.filteredPoints.length} 个采样点`)
  } catch (error) {
    ElMessage.error('半径搜索失败')
  }
}

// ============================================
// 13. 切换添加模式
// ============================================
const toggleAddMode = () => {
  addMode.value = !addMode.value
  ElMessage.info(addMode.value ? '点击地图添加采样点' : '已退出添加模式')
}

// ============================================
// 14. 刷新地图
// ============================================
const refreshMap = () => {
  loadPointsToMap()
  if (highlightLayer) {
    highlightLayer.clearLayers()
    map.removeLayer(highlightLayer)
    highlightLayer = null
  }
  searchResultCount.value = 0
  searchText.value = ''
}

defineExpose({ refreshMap })

// ============================================
// 15. 生命周期
// ============================================
onMounted(() => {
  setTimeout(initMap, 100)
})

onUnmounted(() => {
  if (map) map.remove()
})

// ============================================
// 16. 全局函数
// ============================================
window.editPoint = (id) => {
  const point = pointStore.points.find(p => p.id === id)
  if (point) emit('editPoint', point)
}

window.deletePoint = async (id) => {
  if (confirm('确定删除吗？')) {
    try {
      await pointStore.deletePoint(id)
      refreshMap()
      ElMessage.success('删除成功')
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.map-container {
  position: relative;
  height: 100%;
  width: 100%;
  min-height: 500px;
}
.map {
  height: 100%;
  width: 100%;
  min-height: 500px;
  background-color: #f0f0f0;
}

/* ===== 控制栏 ===== */
.map-controls {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1000;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-width: 520px;
  justify-content: flex-end;
}
.search-group {
  display: flex;
  gap: 4px;
  background: white;
  padding: 4px 8px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.search-input {
  width: 150px;
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}
.search-input:focus {
  border-color: #409eff;
}
.radius-group {
  display: flex;
  gap: 4px;
  background: white;
  padding: 4px 8px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.radius-input {
  width: 70px;
  padding: 6px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}
.radius-input:focus {
  border-color: #409eff;
}
.btn {
  padding: 6px 14px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
  white-space: nowrap;
}
.btn-primary {
  background-color: #409eff;
  color: white;
}
.btn-primary:hover {
  background-color: #66b1ff;
}
.btn-success {
  background-color: #67c23a;
  color: white;
}
.btn-success:hover {
  background-color: #85ce61;
}
.btn-warning {
  background-color: #e6a23c;
  color: white;
}
.btn-warning:hover {
  background-color: #ebb563;
}
.btn-danger {
  background-color: #f56c6c;
  color: white;
}
.btn-danger:hover {
  background-color: #f89898;
}

/* ===== 搜索结果提示 ===== */
.search-result-info {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 107, 53, 0.92);
  color: white;
  padding: 8px 24px;
  border-radius: 20px;
  font-size: 14px;
  z-index: 1000;
  box-shadow: 0 2px 12px rgba(255, 107, 53, 0.4);
}
.search-result-info strong {
  font-size: 18px;
}

/* ============================================
   自定义导航定位图标
   ============================================ */
.custom-location-icon {
  background: transparent !important;
  border: none !important;
}
.location-marker {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.location-dot {
  width: 14px;
  height: 14px;
  background: #1a73e8;
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(26, 115, 232, 0.3);
  z-index: 2;
  position: relative;
}
.location-pulse {
  position: absolute;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(26, 115, 232, 0.15);
  border: 2px solid rgba(26, 115, 232, 0.3);
  animation: pulse-ring 2s ease-out infinite;
  z-index: 1;
}
.location-ring {
  position: absolute;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid rgba(26, 115, 232, 0.1);
  animation: pulse-ring 2.5s ease-out infinite 0.5s;
  z-index: 0;
}
@keyframes pulse-ring {
  0% { transform: scale(0.5); opacity: 0.8; }
  100% { transform: scale(1.8); opacity: 0; }
}

/* ===== 搜索高亮 ===== */
.search-marker .search-dot {
  width: 18px;
  height: 18px;
  background: #ff6b35;
  border: 3px solid white;
  box-shadow: 0 0 0 6px rgba(255, 107, 53, 0.3);
}
.search-marker .search-pulse {
  width: 44px;
  height: 44px;
  background: rgba(255, 107, 53, 0.2);
  border-color: rgba(255, 107, 53, 0.4);
  animation: search-pulse 1.2s ease-out infinite;
}
.search-marker .search-ring {
  width: 60px;
  height: 60px;
  border-color: rgba(255, 107, 53, 0.15);
  animation: search-pulse 1.8s ease-out infinite 0.4s;
}
@keyframes search-pulse {
  0% { transform: scale(0.5); opacity: 0.9; }
  100% { transform: scale(2.2); opacity: 0; }
}
.search-label {
  position: absolute;
  bottom: -18px;
  left: 50%;
  transform: translateX(-50%);
  background: #ff6b35;
  color: white;
  font-size: 10px;
  font-weight: bold;
  padding: 1px 8px;
  border-radius: 10px;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.4);
  z-index: 10;
  animation: label-bounce 1s ease-in-out infinite;
}
@keyframes label-bounce {
  0%, 100% { transform: translateX(-50%) scale(1); }
  50% { transform: translateX(-50%) scale(1.05); }
}
.leaflet-marker-icon.custom-location-icon {
  background: transparent !important;
}
</style>