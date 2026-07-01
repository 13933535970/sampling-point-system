<template>
  <div class="app">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="logo">
        <span class="logo-icon">🗺️</span>
        <span class="logo-text">野外采样点记录与管理GIS系统</span>
      </div>
    </div>
    
    <!-- 主体内容 -->
    <div class="main-layout">
      <!-- 左侧面板 -->
      <div class="sidebar">
        <!-- 标签页 -->
        <div class="tabs">
          <div 
            class="tab" 
            :class="{ active: activeTab === 'list' }"
            @click="activeTab = 'list'"
          >
            📋 采样列表
          </div>
          <div 
            class="tab" 
            :class="{ active: activeTab === 'stats' }"
            @click="activeTab = 'stats'"
          >
            📊 统计筛选
          </div>
        </div>
        
        <!-- 列表视图 -->
        <div v-show="activeTab === 'list'" class="tab-content">
          <PointList 
            :points="pointStore.filteredPoints" 
            @add="openAddForm"
            @edit="openEditForm"
            @delete="handleDelete"
          />
        </div>
        
        <!-- 统计视图 -->
        <div v-show="activeTab === 'stats'" class="tab-content">
          <FilterPanel 
            @filter="handleFilter"
            @radiusSearch="handleRadiusSearch"
            @clear="handleClearFilter"
          />
          <Statistics />
        </div>
      </div>
      
      <!-- 右侧地图 -->
      <div class="map-area">
        <MapView 
          ref="mapViewRef" 
          @openForm="openAddForm"
          @editPoint="openEditForm"
        />
      </div>
    </div>
    
    <!-- 表单弹窗 -->
    <el-dialog 
      v-model="formDialogVisible" 
      :title="formTitle" 
      width="500px"
      @close="closeForm"
    >
      <PointForm 
        ref="formRef"
        :initial-data="editingPoint"
        :location="pointStore.tempLocation"
        @submit="handleFormSubmit"
        @cancel="closeForm"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MapView from './components/MapView.vue'
import PointList from './components/PointList.vue'
import PointForm from './components/PointForm.vue'
import Statistics from './components/Statistics.vue'
import FilterPanel from './components/FilterPanel.vue'
import { usePointStore } from './stores/pointStore'

const pointStore = usePointStore()
const mapViewRef = ref(null)
const formRef = ref(null)

const activeTab = ref('list')
const formDialogVisible = ref(false)
const editingPoint = ref(null)

const formTitle = computed(() => editingPoint.value ? '✏️ 编辑采样点' : '➕ 新增采样点')

// 打开新增表单
const openAddForm = () => {
  editingPoint.value = null
  formDialogVisible.value = true
}

// 打开编辑表单
const openEditForm = (point) => {
  editingPoint.value = point
  formDialogVisible.value = true
}

// 提交表单
const handleFormSubmit = async (formData) => {
  try {
    if (editingPoint.value) {
      await pointStore.updatePoint(editingPoint.value.id, formData)
      ElMessage.success('更新成功')
    } else {
      await pointStore.createPoint(formData)
      ElMessage.success('添加成功')
    }
    closeForm()
    mapViewRef.value?.refreshMap()
  } catch (error) {
    ElMessage.error(editingPoint.value ? '更新失败' : '添加失败')
  }
}

// 删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这个采样点吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await pointStore.deletePoint(id)
    ElMessage.success('删除成功')
    mapViewRef.value?.refreshMap()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 筛选
const handleFilter = async (type) => {
  await pointStore.filterByType(type)
  mapViewRef.value?.refreshMap()
}

// 半径搜索
const handleRadiusSearch = async (radius) => {
  // 需要获取地图中心点，这个功能需要传递map实例
  ElMessage.info('请在地图上点击"半径搜索"按钮')
}

// 清除筛选
const handleClearFilter = async () => {
  await pointStore.clearFilter()
  mapViewRef.value?.refreshMap()
}

// 关闭表单
const closeForm = () => {
  formDialogVisible.value = false
  editingPoint.value = null
  pointStore.tempLocation = null
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  background: linear-gradient(135deg, #1a2a3a 0%, #0f1a24 100%);
  color: white;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 1px;
}

.main-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 360px;
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
}

.tabs {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  background: white;
  flex-shrink: 0;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}

.tab:hover {
  color: #409eff;
}

.tab.active {
  color: #409eff;
  border-bottom-color: #409eff;
}

.tab-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.map-area {
  flex: 1;
  position: relative;
  overflow: hidden;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>