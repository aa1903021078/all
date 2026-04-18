<template>
  <div class="app-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable style="width: 200px" @keyup.enter="handleQuery" />
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增分配</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="staffName" label="工作人员" min-width="120" />
        <el-table-column prop="staffRole" label="岗位" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="staffRoleTag(row.staffRole)">{{ staffRoleLabel(row.staffRole) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="infantName" label="负责婴儿" min-width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.infantName">{{ row.infantName }}</span>
            <el-tag v-else type="info" size="small">通用/产妇</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.current" v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList" @current-change="getList" />
      </div>
    </el-card>

    <!-- Add Dialog -->
    <el-dialog v-model="dialogVisible" title="新增人员分配" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="选择订单" prop="orderId">
          <el-select v-model="formData.orderId" filterable placeholder="请选择订单" style="width: 100%" @change="handleOrderChange">
            <el-option v-for="item in orderOptions" :key="item.id" :label="item.orderNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作人员" prop="staffId">
          <el-select v-model="formData.staffId" filterable placeholder="请选择工作人员" style="width: 100%">
            <el-option v-for="item in staffOptions" :key="item.id" :label="`${item.realName}（${staffRoleLabel(item.role)}）`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位" prop="staffRole">
          <el-select v-model="formData.staffRole" placeholder="请选择岗位" style="width: 100%">
            <el-option label="月嫂" :value="1" />
            <el-option label="营养师" :value="2" />
            <el-option label="护理人员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责婴儿">
          <el-select v-model="formData.infantId" clearable placeholder="不选则负责产妇/通用" style="width: 100%">
            <el-option v-for="item in infantOptions" :key="item.id" :label="item.name + (item.gender === 1 ? '(男)' : '(女)')" :value="item.id" />
          </el-select>
          <div class="form-tip">月嫂和护理人员可指定负责某个婴儿，营养师一般不需指定</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderStaffList, addOrderStaff, deleteOrderStaff, getStaffList, getOrderList, getInfantsByOrder } from '@/api/index.js'

const staffRoleLabel = (r) => ({ 1: '月嫂', 2: '营养师', 3: '护理人员' })[r] || '未知'
const staffRoleTag = (r) => ({ 1: '', 2: 'success', 3: 'warning' })[r] || 'info'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ current: 1, size: 10, orderNo: '' })

const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({ orderId: null, staffId: null, staffRole: null, infantId: null })
const orderOptions = ref([])
const staffOptions = ref([])
const infantOptions = ref([])

const formRules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  staffId: [{ required: true, message: '请选择工作人员', trigger: 'change' }],
  staffRole: [{ required: true, message: '请选择岗位', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getOrderStaffList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.current = 1; getList() }
const handleReset = () => { queryParams.orderNo = ''; queryParams.current = 1; getList() }

const loadOptions = async () => {
  try {
    const [staffRes, orderRes] = await Promise.all([
      getStaffList(),
      getOrderList({ current: 1, size: 1000 })
    ])
    staffOptions.value = staffRes.data || []
    orderOptions.value = orderRes.data.records || []
  } catch { /* ignore */ }
}

const handleOrderChange = async (orderId) => {
  infantOptions.value = []
  formData.infantId = null
  if (orderId) {
    try {
      const res = await getInfantsByOrder(orderId)
      infantOptions.value = res.data || []
    } catch { /* ignore */ }
  }
}

const handleAdd = () => {
  Object.assign(formData, { orderId: null, staffId: null, staffRole: null, infantId: null })
  infantOptions.value = []
  loadOptions()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    await addOrderStaff(formData)
    ElMessage.success('分配成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该分配记录？', '提示', { type: 'warning' }).then(async () => {
    await deleteOrderStaff(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

onMounted(() => getList())
</script>

<style scoped>
.app-container { padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 10px; }
.toolbar-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.toolbar-right { display: flex; align-items: center; gap: 10px; }
.pagination-container { display: flex; justify-content: flex-end; margin-top: 16px; }
.form-tip { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
