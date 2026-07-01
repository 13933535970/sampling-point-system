<template>
  <el-card class="filter-card" shadow="never">
    <template #header>
      <span>🔍 筛选与搜索</span>
    </template>
    
    <!-- ===== 新增：名称搜索 ===== -->
    <div class="filter-group">
      <div class="filter-label">按名称搜索</div>
      <div class="search-group">
        <el-input 
          v-model="searchKeyword" 
          placeholder="输入采样点名称..."
          size="small"
          @keyup.enter="doSearch"
          clearable
          @clear="doClearSearch"
        />
        <el-button type="primary" size="small" @click="doSearch">搜索</el-button>
      </div>
    </div>
    
    <div class="filter-group">
      <div class="filter-label">按类型筛选</div>
      <div class="type-buttons">
        <el-button 
          :type="selectedType === '' ? 'primary' : 'default'"
          size="small"
          @click="filterByType('')"
        >
          全部
        </el-button>
        <el-button 
          v-for="type in types" 
          :key="type"
          :type="selectedType === type ? 'primary' : 'default'"
          size="small"
          @click="filterByType(type)"
        >
          {{ type }}
        </el-button>
      </div>
    </div>
    
    <el-button 
      v-if="hasFilter || searchKeyword" 
      type="danger" 
      size="small" 
      plain
      @click="clearAll"
      style="width: 100%; margin-top: 10px"
    >
      清除所有筛选
    </el-button>
  </el-card>
</template>

<script setup>
import { ref, computed } from 'vue'
import { usePointStore } from '../stores/pointStore'
import { ElMessage } from 'element-plus'

const pointStore = usePointStore()

const types = ['土壤', '水体', '植被', '空气']
const selectedType = ref('')
const searchKeyword = ref('')

const hasFilter = computed(() => selectedType.value !== '')

const filterByType = (type) => {
  selectedType.value = type
  pointStore.filterByType(type || null)
}

const doSearch = () => {
  if (!searchKeyword.value || searchKeyword.value.trim() === '') {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  pointStore.searchByName(searchKeyword.value)
  // 触发地图更新（通过父组件）
  emit('search', searchKeyword.value)
}

const doClearSearch = () => {
  searchKeyword.value = ''
  pointStore.clearFilter()
  emit('clear')
}

const clearAll = () => {
  selectedType.value = ''
  searchKeyword.value = ''
  pointStore.clearFilter()
  emit('clear')
}

const emit = defineEmits(['filter', 'search', 'clear'])
</script>

<style scoped>
.filter-card {
  margin-bottom: 15px;
}
.filter-group {
  margin-bottom: 15px;
}
.filter-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}
.search-group {
  display: flex;
  gap: 8px;
}
.type-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>