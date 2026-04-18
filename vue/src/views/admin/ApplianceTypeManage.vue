<template>
  <div class="box" style="padding: 20px;">
    <!-- 搜索栏 -->
    <div class="search">
      <el-input
          v-model="params.name"
          placeholder="请输入品类名称"
          style="width: 240px; margin-right: 10px;"
          clearable
      />
      <el-select
          v-model="params.status"
          placeholder="状态"
          style="width: 120px; margin-right: 10px;"
          clearable
      >
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
    </div>

    <!-- 操作栏 -->
    <div class="operation" style="margin: 15px 0;">
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增品类</el-button>
      <el-button
          type="danger"
          :icon="Delete"
          @click="batchDelete"
          :disabled="selectedRows.length === 0"
      >
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
        :data="dataList"
        stripe
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column type="index" label="序号" width="70" align="center" />

      <el-table-column label="品类名称" min-width="140">
        <template #default="{ row }">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span style="font-size: 24px;" v-if="row.icon">{{ row.icon }}</span>
            <el-icon v-else size="24"><Box /></el-icon>
            <span style="font-weight: 500;">{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="unit" label="单位" width="80" align="center" />

      <el-table-column label="价格区间" width="150" align="center">
        <template #default="{ row }">
          <span style="color: #f56c6c;">¥{{ row.priceMin || 0 }} ~ ¥{{ row.priceMax || 0 }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="weightEstimate" label="预估重量" width="110" align="center">
        <template #default="{ row }">
          {{ row.weightEstimate || 0 }} kg
        </template>
      </el-table-column>

      <el-table-column label="积分/kg" width="100" align="center">
        <template #default="{ row }">
          <el-tag type="warning" size="small">{{ row.pointsPerKg || 10 }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="减碳系数" width="100" align="center">
        <template #default="{ row }">
          <el-tag type="success" size="small">{{ row.carbonFactor || 2.5 }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="预估收益" width="180" align="center">
        <template #default="{ row }">
          <div style="display: flex; gap: 5px; justify-content: center;">
            <el-tooltip content="积分 = 重量 × 积分/kg" placement="top">
              <el-tag type="danger" size="small">{{ calculatePoints(row) }}分</el-tag>
            </el-tooltip>
            <el-tooltip content="碳减排 = 重量 × 系数" placement="top">
              <el-tag type="info" size="small">{{ calculateCarbon(row) }}kg</el-tag>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />

      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(row, val)"
          />
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button type="primary" link :icon="View" @click="handleView(row)">详情</el-button>
          <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link :icon="Delete">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination" style="margin-top: 20px;">
      <el-pagination
          v-model:current-page="params.pageNum"
          v-model:page-size="params.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
          background
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑品类' : '新增品类'"
        width="650px"
        destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品类名称" prop="name">
              <el-input v-model="form.name" placeholder="如：冰箱、空调" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标" prop="icon">
              <el-input
                  v-model="form.icon"
                  placeholder="点击选择图标"
                  readonly
                  @click="showIconPicker = true"
                  style="cursor: pointer;"
              >
                <template #prefix>
                  <span style="font-size: 20px;" v-if="form.icon">{{ form.icon }}</span>
                  <el-icon v-else size="20"><Box /></el-icon>
                </template>
                <template #append>
                  <el-button :icon="Pointer" @click.stop="showIconPicker = true" />
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计量单位" prop="unit">
              <el-input v-model="form.unit" placeholder="默认：台" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低价">
              <el-input-number v-model="form.priceMin" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高价">
              <el-input-number v-model="form.priceMax" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预估重量">
              <el-input-number v-model="form.weightEstimate" :min="0" :precision="2" style="width: 100%">
                <template #append>kg</template>
              </el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每公斤积分">
              <el-input-number v-model="form.pointsPerKg" :min="0" :precision="2" style="width: 100%">
                <template #append>分</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="减碳系数">
          <el-input-number v-model="form.carbonFactor" :min="0" :precision="4" style="width: 200px" />
          <span style="margin-left: 10px; color: #999; font-size: 13px;">每kg减少的碳排放(kg)</span>
        </el-form-item>

        <el-form-item label="品类描述">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请输入品类描述..."
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 收益预览 -->
        <div
            v-if="form.weightEstimate && form.pointsPerKg"
            style="background: #f5f7fa; padding: 15px; border-radius: 5px; margin-top: 10px; border-left: 4px solid #409eff;"
        >
          <div style="font-weight: bold; color: #409eff; margin-bottom: 10px; display: flex; align-items: center; gap: 5px;">
            <el-icon><View /></el-icon> 收益预估（{{ form.weightEstimate }}kg）
          </div>
          <div style="display: flex; gap: 30px;">
            <div>预估积分：<span style="color: #f56c6c; font-weight: bold; font-size: 18px;">{{ calculateFormPoints }}</span> 分</div>
            <div>碳减排量：<span style="color: #67c23a; font-weight: bold; font-size: 18px;">{{ calculateFormCarbon }}</span> kg</div>
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 图标选择器弹窗 -->
    <el-dialog v-model="showIconPicker" title="选择图标" width="420px" append-to-body>
      <div style="display: grid; grid-template-columns: repeat(8, 1fr); gap: 10px; padding: 10px;">
        <div
            v-for="icon in iconList"
            :key="icon"
            @click="selectIcon(icon)"
            style="
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            cursor: pointer;
            border-radius: 4px;
            border: 1px solid #dcdfe6;
            transition: all 0.3s;
          "
            :style="form.icon === icon ? 'background: #409eff; color: white; border-color: #409eff;' : 'background: #f5f7fa;'"
        >
          {{ icon }}
        </div>
      </div>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="品类详情" size="400px">
      <div v-if="currentRow" style="padding: 0 20px;">
        <div style="text-align: center; margin-bottom: 30px;">
          <div style="font-size: 60px; margin-bottom: 10px;">{{ currentRow.icon || '📦' }}</div>
          <h2 style="margin: 0;">{{ currentRow.name }}</h2>
          <el-tag :type="currentRow.status === 1 ? 'success' : 'danger'" style="margin-top: 10px;">
            {{ currentRow.status === 1 ? '启用中' : '已禁用' }}
          </el-tag>
        </div>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="计量单位">{{ currentRow.unit || '台' }}</el-descriptions-item>
          <el-descriptions-item label="价格区间">¥{{ currentRow.priceMin || 0 }} ~ ¥{{ currentRow.priceMax || 0 }}</el-descriptions-item>
          <el-descriptions-item label="预估重量">{{ currentRow.weightEstimate || 0 }} kg</el-descriptions-item>
          <el-descriptions-item label="每公斤积分">{{ currentRow.pointsPerKg || 10 }} 分</el-descriptions-item>
          <el-descriptions-item label="减碳系数">{{ currentRow.carbonFactor || 2.5 }}</el-descriptions-item>
          <el-descriptions-item label="排序">{{ currentRow.sortOrder || 0 }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px; display: flex; gap: 10px;">
          <div style="flex: 1; background: #fdf6ec; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #f5dab1;">
            <div style="color: #e6a23c; font-size: 12px; margin-bottom: 5px;">预估积分</div>
            <div style="color: #e6a23c; font-size: 24px; font-weight: bold;">{{ calculatePoints(currentRow) }}</div>
          </div>
          <div style="flex: 1; background: #f0f9eb; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #c2e7b0;">
            <div style="color: #67c23a; font-size: 12px; margin-bottom: 5px;">碳减排量</div>
            <div style="color: #67c23a; font-size: 24px; font-weight: bold;">{{ calculateCarbon(currentRow) }}</div>
          </div>
        </div>

        <div v-if="currentRow.description" style="margin-top: 20px; padding: 15px; background: #f5f7fa; border-radius: 5px;">
          <div style="color: #666; font-size: 13px; margin-bottom: 5px;">品类描述：</div>
          <div style="color: #333; line-height: 1.6;">{{ currentRow.description }}</div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Delete,
  Edit,
  View,
  Box,
  Pointer
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 查询参数
const params = reactive({
  name: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const dataList = ref([])
const total = ref(0)
const loading = ref(false)
const selectedRows = ref([])

// 弹窗控制
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const currentRow = ref(null)

// 图标选择相关
const showIconPicker = ref(false)
const iconList = [
  '❄️', '🌀', '📺', '💻', '📱', '🔌', '🧊', '☕',
  '📻', '🔊', '🎮', '⏰', '📷', '🔦', '🛠️', '📠',
  '🖨️', '⌨️', '🖱️', '🔋', '💡', '📹', '🎥', '📽️',
  '🎙️', '🎚️', '🎛️', '🧭', '⏱️', '⏲️', '🕰️', '🧰'
]

// 表单数据
const form = reactive({
  id: null,
  name: '',
  icon: '',
  unit: '',
  priceMin: 0,
  priceMax: 0,
  weightEstimate: 0,
  pointsPerKg: 10,
  carbonFactor: 2.5,
  description: '',
  sortOrder: 0,
  status: 1
})

// 表单验证
const rules = {
  name: [{ required: true, message: '请输入品类名称', trigger: 'blur' }]
}

// 计算属性 - 表单预览
const calculateFormPoints = computed(() => {
  return Math.floor((form.weightEstimate || 0) * (form.pointsPerKg || 0))
})

const calculateFormCarbon = computed(() => {
  return ((form.weightEstimate || 0) * (form.carbonFactor || 0)).toFixed(2)
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/applianceType/selectPage', {
      params: {
        pageNum: params.pageNum,
        pageSize: params.pageSize,
        name: params.name,
        status: params.status
      }
    })
    if (res.code === '200') {
      dataList.value = res.data?.list || []
      total.value = res.data?.total || 0
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('网络请求失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  params.name = ''
  params.status = ''
  params.pageNum = 1
  loadData()
}

// 计算积分（向下取整）
const calculatePoints = (row) => {
  return Math.floor((row.weightEstimate || 0) * (row.pointsPerKg || 0))
}

// 计算碳减排（保留2位小数）
const calculateCarbon = (row) => {
  return ((row.weightEstimate || 0) * (row.carbonFactor || 0)).toFixed(2)
}

// 状态变更
const handleStatusChange = async (row, val) => {
  try {
    const res = await request.put('/applianceType/update', {
      id: row.id,
      status: val
    })
    if (res.code === '200') {
      ElMessage.success(val === 1 ? '已启用' : '已禁用')
    } else {
      throw new Error(res.msg)
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    row.status = val === 1 ? 0 : 1
  }
}

// 选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 选择图标
const selectIcon = (icon) => {
  form.icon = icon
  showIconPicker.value = false
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 查看详情
const handleView = (row) => {
  currentRow.value = row
  drawerVisible.value = true
}

// 删除
const handleDelete = async (id) => {
  try {
    const res = await request.delete(`/applianceType/delete/${id}`)
    if (res.code === '200') {
      ElMessage.success('删除成功')
      loadData()
    } else {
      throw new Error(res.msg)
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 批量删除
const batchDelete = async () => {
  if (selectedRows.value.length === 0) return

  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 条记录吗？`, '提示', {
      type: 'warning'
    })

    const ids = selectedRows.value.map(row => row.id).join(',')
    const res = await request.delete(`/applianceType/delete/${ids}`)
    if (res.code === '200') {
      ElMessage.success('批量删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      let res
      if (isEdit.value) {
        res = await request.put('/applianceType/update', form)
      } else {
        res = await request.post('/applianceType/add', form)
      }

      if (res.code === '200') {
        ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
        dialogVisible.value = false
        loadData()
      } else {
        throw new Error(res.msg)
      }
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    icon: '',
    unit: '',
    priceMin: 0,
    priceMax: 0,
    weightEstimate: 0,
    pointsPerKg: 10,
    carbonFactor: 2.5,
    description: '',
    sortOrder: 0,
    status: 1
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.box {
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0,0,0,.1);
}

.search {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.operation {
  margin: 10px 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>