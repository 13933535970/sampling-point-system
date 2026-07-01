<template>
  <div>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="采样点名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入采样点名称" />
      </el-form-item>
      
      <el-form-item label="采样类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择类型">
          <el-option label="土壤" value="土壤" />
          <el-option label="水体" value="水体" />
          <el-option label="植被" value="植被" />
          <el-option label="空气" value="空气" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="描述" prop="description">
        <el-input 
          v-model="form.description" 
          type="textarea" 
          :rows="3"
          placeholder="请输入描述信息" 
        />
      </el-form-item>
      
      <el-form-item label="采集人" prop="collector">
        <el-input v-model="form.collector" placeholder="请输入采集人姓名" />
      </el-form-item>
      
      <el-form-item label="采集时间" prop="collectTime">
        <el-date-picker 
          v-model="form.collectTime" 
          type="datetime" 
          placeholder="选择采集时间"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      
      <el-form-item label="位置">
        <el-tag v-if="location">
          经度: {{ location.lng?.toFixed(6) }}, 纬度: {{ location.lat?.toFixed(6) }}
        </el-tag>
        <el-tag v-else-if="initialData?.location" type="info">
          原位置: {{ initialData.location.x?.toFixed(6) }}, {{ initialData.location.y?.toFixed(6) }}
        </el-tag>
        <span v-else style="color: #909399;">请先点击地图选择位置</span>
      </el-form-item>
    </el-form>
    
    <div class="dialog-footer">
      <el-button @click="emit('cancel')">取消</el-button>
      <el-button type="primary" @click="submitForm">确定</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  initialData: {
    type: Object,
    default: null
  },
  location: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref(null)

// 获取当前时间（YYYY-MM-DD HH:mm:ss 格式）
const getCurrentDateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const form = reactive({
  name: '',
  type: '',
  description: '',
  collector: '',
  collectTime: getCurrentDateTime()
})

const rules = {
  name: [{ required: true, message: '请输入采样点名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择采样类型', trigger: 'change' }],
  collector: [{ required: true, message: '请输入采集人', trigger: 'blur' }]
}

onMounted(() => {
  if (props.initialData) {
    form.name = props.initialData.name || ''
    form.type = props.initialData.type || ''
    form.description = props.initialData.description || ''
    form.collector = props.initialData.collector || ''
    form.collectTime = props.initialData.collectTime || getCurrentDateTime()
  }
})

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    let lng = props.location?.lng
    let lat = props.location?.lat
    
    if ((!lng || !lat) && props.initialData?.location) {
      lng = props.initialData.location.x
      lat = props.initialData.location.y
    }
    
    if (!lng || !lat) {
      ElMessage.warning('请先在地图上点击选择位置')
      return
    }
    
    const submitData = {
      name: form.name,
      type: form.type,
      description: form.description,
      collector: form.collector,
      collectTime: form.collectTime,  // 保持 YYYY-MM-DD HH:mm:ss 格式
      lng: lng,
      lat: lat
    }
    
    console.log('提交数据:', submitData)
    emit('submit', submitData)
    
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style>