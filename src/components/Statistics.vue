<template>
  <el-card class="stats-card" shadow="never">
    <template #header>
      <span>📊 采样统计</span>
    </template>
    
    <div class="total-count">
      <div class="total-number">{{ totalCount }}</div>
      <div class="total-label">总采样点数</div>
    </div>
    
    <div class="type-stats">
      <div v-for="(count, type) in pointStore.typeStats" :key="type" class="type-item">
        <span class="type-name">{{ type }}</span>
        <div class="type-bar">
          <div class="type-bar-fill" :style="{ width: (count / totalCount * 100) + '%' }"></div>
        </div>
        <span class="type-count">{{ count }}</span>
      </div>
    </div>
    
    <div class="recent-points">
      <h4>最近添加</h4>
      <div v-for="point in recentPoints" :key="point.id" class="recent-item">
        <div class="recent-name">{{ point.name }}</div>
        <div class="recent-time">{{ formatDate(point.collectTime) }}</div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { usePointStore } from '../stores/pointStore'

const pointStore = usePointStore()

const totalCount = computed(() => pointStore.points.length)

const recentPoints = computed(() => {
  return [...pointStore.points]
    .sort((a, b) => new Date(b.collectTime) - new Date(a.collectTime))
    .slice(0, 5)
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth()+1}/${date.getDate()}`
}
</script>

<style scoped>
.stats-card {
  margin-top: 15px;
}
.total-count {
  text-align: center;
  margin-bottom: 20px;
}
.total-number {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
}
.total-label {
  color: #909399;
  font-size: 14px;
}
.type-stats {
  margin-bottom: 20px;
}
.type-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.type-name {
  width: 50px;
  font-size: 14px;
}
.type-bar {
  flex: 1;
  height: 8px;
  background-color: #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}
.type-bar-fill {
  height: 100%;
  background-color: #409eff;
  border-radius: 4px;
}
.type-count {
  width: 30px;
  text-align: right;
  font-size: 14px;
  color: #606266;
}
.recent-points h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}
.recent-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #e4e7ed;
}
.recent-name {
  font-size: 14px;
}
.recent-time {
  font-size: 12px;
  color: #909399;
}
</style>