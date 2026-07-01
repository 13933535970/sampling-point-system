<template>
  <div class="point-list">
    <div class="list-header">
      <h3>采样点列表</h3>
      <el-button type="primary" size="small" @click="emit('add')">+ 新增</el-button>
    </div>
    
    <el-table :data="points" stripe height="400" style="width: 100%">
      <el-table-column prop="name" label="名称" width="100" />
      <el-table-column prop="type" label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="getTagType(row.type)" size="small">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="collector" label="采集人" width="80" />
      <el-table-column label="位置" width="140">
        <template #default="{ row }">
          {{ row.location?.x?.toFixed(4) }}, {{ row.location?.y?.toFixed(4) }}
        </template>
      </el-table-column>
      <el-table-column prop="collectTime" label="采集时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.collectTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="emit('edit', row)">编辑</el-button>
          <el-button link type="danger" @click="emit('delete', row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { usePointStore } from '../stores/pointStore'

const props = defineProps(['points'])

const emit = defineEmits(['add', 'edit', 'delete'])

const pointStore = usePointStore()

const getTagType = (type) => {
  const map = {
    '土壤': 'warning',
    '水体': 'primary',
    '植被': 'success',
    '空气': 'info'
  }
  return map[type] || ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth()+1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
.point-list {
  padding: 10px;
}
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.list-header h3 {
  margin: 0;
}
</style>