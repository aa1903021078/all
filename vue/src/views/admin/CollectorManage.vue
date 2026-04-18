<template>
  <div class="collector-manage" style="padding: 20px;">
    <!-- 搜索栏 -->
    <div class="search">
      <el-input
          v-model="params.username"
          placeholder="请输入用户名"
          style="width: 180px; margin-right: 10px;"
          clearable
          @keyup.enter="loadData"
      />
      <el-input
          v-model="params.name"
          placeholder="请输入姓名"
          style="width: 180px; margin-right: 10px;"
          clearable
          @keyup.enter="loadData"
      />
      <el-input
          v-model="params.phone"
          placeholder="请输入手机号"
          style="width: 180px; margin-right: 10px;"
          clearable
          @keyup.enter="loadData"
      />
      <el-select
          v-model="params.auditStatus"
          placeholder="审核状态"
          style="width: 140px; margin-right: 10px;"
          clearable
          @change="loadData"
      >
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
      <el-button :icon="RefreshRight" @click="loadData" title="刷新数据">刷新</el-button>
    </div>

    <!-- 操作栏 -->
    <div class="operation" style="margin: 15px 0;">
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增回收员</el-button>
      <el-button
          type="danger"
          :icon="Delete"
          @click="batchDisable"
          :disabled="selectedRows.length === 0"
      >
        批量禁用
      </el-button>
      <el-tag type="info" style="margin-left: 10px;">
        共 {{ total }} 条记录
      </el-tag>
    </div>

    <!-- 数据表格 -->
    <el-table
        :data="collectorList"
        stripe
        border
        v-loading="loading"
        row-key="id"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" reserve-selection />
      <el-table-column type="index" label="序号" width="70" align="center" :index="(index) => (params.pageNum - 1) * params.pageSize + index + 1" />

      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="name" label="姓名" min-width="100" />
      <el-table-column prop="phone" label="手机号" min-width="130" />

      <el-table-column label="工作状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.workStatus === 1 ? 'success' : 'info'">
            {{ row.workStatus === 1 ? '接单中' : '休息中' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="评分" width="90" align="center">
        <template #default="{ row }">
          <div v-if="row.auditStatus === 1" style="display: flex; align-items: center; justify-content: center; gap: 3px;">
            <el-icon color="#e6a23c"><StarFilled /></el-icon>
            <span>{{ row.rating || '5.0' }}</span>
          </div>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>

      <el-table-column label="总接单" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.auditStatus === 1">{{ row.orderCount || 0 }}</span>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>

      <el-table-column label="今日接单" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.auditStatus === 1">{{ row.todayOrderCount || 0 }}</span>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>

      <el-table-column label="审核状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getAuditStatusType(row.auditStatus)">
            {{ getAuditStatusText(row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="账号状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :loading="row.statusLoading"
              @change="(val) => handleStatusChange(row, val)"
          />
        </template>
      </el-table-column>

      <el-table-column label="操作" width="250" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" :icon="View" @click="handleView(row)">详情</el-button>
          <el-popconfirm
              :title="row.status === 1 ? '确定禁用该回收员吗？' : '确定启用该回收员吗？'"
              @confirm="handleToggleStatus(row)"
          >
            <template #reference>
              <el-button link :type="row.status === 1 ? 'danger' : 'success'">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑回收员' : '新增回收员'"
        width="550px"
        destroy-on-close
        :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input
                  v-model="form.username"
                  placeholder="请输入用户名"
                  :disabled="isEdit"
                  maxlength="20"
                  show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入真实姓名" maxlength="20" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                  v-model="form.password"
                  type="password"
                  :placeholder="isEdit ? '不修改请留空' : '默认123456'"
                  show-password
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="每日上限">
              <el-input-number v-model="form.maxDailyOrders" :min="1" :max="50" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账号状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="工作状态">
          <el-radio-group v-model="form.workStatus">
            <el-radio :value="1">接单中</el-radio>
            <el-radio :value="0">休息中</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="审核状态" v-if="isEdit">
          <el-radio-group v-model="form.auditStatus">
            <el-radio :value="0">待审核</el-radio>
            <el-radio :value="1">已通过</el-radio>
            <el-radio :value="2">已拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="回收员详情" size="450px">
      <div v-if="currentRow" style="padding: 0 20px;">
        <div style="text-align: center; margin-bottom: 30px;">
          <el-avatar :size="80" :icon="UserFilled" style="background-color: #409eff; font-size: 40px;" />
          <h2 style="margin: 15px 0 5px;">{{ currentRow.name }}</h2>
          <div style="color: #666; font-size: 14px;">{{ currentRow.username }}</div>
          <div style="margin-top: 10px;">
            <el-tag :type="currentRow.status === 1 ? 'success' : 'danger'" style="margin-right: 5px;">
              {{ currentRow.status === 1 ? '账号正常' : '账号禁用' }}
            </el-tag>
            <el-tag :type="currentRow.workStatus === 1 ? 'success' : 'info'">
              {{ currentRow.workStatus === 1 ? '接单中' : '休息中' }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="手机号" :span="2">{{ currentRow.phone }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getAuditStatusType(currentRow.auditStatus)" size="small">
              {{ getAuditStatusText(currentRow.auditStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="每日上限">{{ currentRow.maxDailyOrders || 10 }} 单</el-descriptions-item>

          <el-descriptions-item v-if="currentRow.auditStatus === 1" label="综合评分">
            <span style="color: #e6a23c; font-weight: bold;">
              <el-icon><StarFilled /></el-icon> {{ currentRow.rating || '5.0' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item v-else label="综合评分">
            <span style="color: #999;">未审核通过</span>
          </el-descriptions-item>
        </el-descriptions>

        <template v-if="currentRow.auditStatus === 1">
          <div style="margin-top: 20px; display: flex; gap: 10px;">
            <div style="flex: 1; background: #f0f9eb; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #c2e7b0;">
              <div style="color: #67c23a; font-size: 12px; margin-bottom: 5px;">总接单数</div>
              <div style="color: #67c23a; font-size: 28px; font-weight: bold;">{{ currentRow.orderCount || 0 }}</div>
            </div>
            <div style="flex: 1; background: #fdf6ec; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #f5dab1;">
              <div style="color: #e6a23c; font-size: 12px; margin-bottom: 5px;">今日接单</div>
              <div style="color: #e6a23c; font-size: 28px; font-weight: bold;">{{ currentRow.todayOrderCount || 0 }}</div>
            </div>
          </div>

          <div style="margin-top: 20px; background: #f5f7fa; padding: 15px; border-radius: 5px; border-left: 4px solid #409eff;">
            <div style="font-weight: bold; color: #409eff; margin-bottom: 10px;">工作统计</div>
            <div style="display: flex; justify-content: space-between; color: #666; font-size: 14px; line-height: 2;">
              <span>完成率：{{ currentRow.completionRate || '98%' }}</span>
              <span>准时率：{{ currentRow.ontimeRate || '95%' }}</span>
            </div>
          </div>
        </template>

        <template v-else>
          <div style="margin-top: 20px; background: #fef0f0; padding: 20px; border-radius: 8px; text-align: center; border: 1px solid #fde2e2;">
            <el-icon style="font-size: 48px; color: #f56c6c; margin-bottom: 10px;"><Warning /></el-icon>
            <div style="color: #f56c6c; font-size: 16px; font-weight: bold; margin-bottom: 5px;">
              {{ currentRow.auditStatus === 0 ? '等待审核中' : '审核未通过' }}
            </div>
            <div style="color: #909399; font-size: 14px;">
              {{ currentRow.auditStatus === 0 ? '该回收员正在等待管理员审核，审核通过后才能开始接单' : '该回收员审核未通过，无法接单' }}
            </div>
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  RefreshRight,
  Plus,
  Delete,
  Edit,
  View,
  StarFilled,
  UserFilled,
  Warning
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const params = reactive({
  username: '',
  name: '',
  phone: '',
  auditStatus: '',
  pageNum: 1,
  pageSize: 10
})

const collectorList = ref([])
const total = ref(0)
const loading = ref(false)
const selectedRows = ref([])
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const currentRow = ref(null)
let autoRefreshTimer = null

const form = reactive({
  id: null,
  username: '',
  name: '',
  phone: '',
  password: '',
  maxDailyOrders: 10,
  status: 1,
  workStatus: 1,
  auditStatus: 0
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const getAuditStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

const getAuditStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const loadData = async (showLoading = true) => {
  if (showLoading) {
    loading.value = true
  }

  try {
    const queryParams = {}
    if (params.username) queryParams.username = params.username
    if (params.name) queryParams.name = params.name
    if (params.phone) queryParams.phone = params.phone
    if (params.auditStatus !== '' && params.auditStatus !== null && params.auditStatus !== undefined) {
      queryParams.auditStatus = params.auditStatus
    }

    const res = await request.get('/collector/selectPage', {
      params: {
        pageNum: params.pageNum,
        pageSize: params.pageSize,
        ...queryParams
      }
    })

    if (res.code === '200' || res.code === 200) {
      const list = res.data?.list || res.data?.records || res.data?.rows || res.data || []
      const totalCount = res.data?.total || res.data?.totalCount || list.length

      collectorList.value = list.map(item => ({
        ...item,
        statusLoading: false
      }))
      total.value = totalCount
    } else {
      throw new Error(res.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error(error.message || '网络请求失败，请检查后端服务')
    collectorList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size) => {
  params.pageSize = size
  params.pageNum = 1
  loadData()
}

const handleCurrentChange = (page) => {
  params.pageNum = page
  loadData()
}

const resetSearch = () => {
  params.username = ''
  params.name = ''
  params.phone = ''
  params.auditStatus = ''
  params.pageNum = 1
  params.pageSize = 10
  loadData()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows || []
}

const handleStatusChange = async (row, val) => {
  row.statusLoading = true
  try {
    const res = await request.put('/collector/update', {
      id: row.id,
      status: val
    })
    if (res.code === '200' || res.code === 200) {
      ElMessage.success(val === 1 ? '已启用' : '已禁用')
      await refreshRowData(row.id)
    } else {
      throw new Error(res.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '状态更新失败')
    row.status = val === 1 ? 0 : 1
  } finally {
    row.statusLoading = false
  }
}

// 刷新单行数据 - 使用正确的后端接口 /selectById/{id}
const refreshRowData = async (id) => {
  try {
    const res = await request.get(`/collector/selectById/${id}`)
    if (res.code === '200' || res.code === 200) {
      const index = collectorList.value.findIndex(item => item.id === id)
      if (index !== -1) {
        collectorList.value[index] = {
          ...res.data,
          statusLoading: false
        }
      }
    }
  } catch (error) {
    console.error('刷新行数据失败:', error)
    // 如果单行刷新失败，刷新整个列表
    loadData(false)
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    const res = await request.put('/collector/update', {
      id: row.id,
      status: newStatus
    })
    if (res.code === '200' || res.code === 200) {
      ElMessage.success(newStatus === 1 ? '启用成功' : '禁用成功')
      await refreshRowData(row.id)
    } else {
      throw new Error(res.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const batchDisable = async () => {
  if (!selectedRows.value || selectedRows.value.length === 0) {
    ElMessage.warning('请选择要禁用的回收员')
    return
  }

  try {
    await ElMessageBox.confirm(`确定禁用选中的 ${selectedRows.value.length} 个回收员吗？`, '提示', {
      type: 'warning'
    })

    const promises = selectedRows.value.map(row =>
        request.put('/collector/update', { id: row.id, status: 0 })
    )

    const results = await Promise.allSettled(promises)
    const successCount = results.filter(r => r.status === 'fulfilled' && (r.value.code === '200' || r.value.code === 200)).length
    const failCount = selectedRows.value.length - successCount

    if (failCount === 0) {
      ElMessage.success(`批量禁用成功，共 ${successCount} 个`)
    } else {
      ElMessage.warning(`成功 ${successCount} 个，失败 ${failCount} 个`)
    }

    selectedRows.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量禁用失败')
    }
  }
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogVisible.value = true
  nextTick(() => {
    Object.assign(form, JSON.parse(JSON.stringify(row)))
    form.password = ''
  })
}

// 查看详情 - 使用正确的后端接口 /selectById/{id}
const handleView = async (row) => {
  try {
    // 调用后端接口获取最新数据
    const res = await request.get(`/collector/selectById/${row.id}`)
    if (res.code === '200' || res.code === 200) {
      currentRow.value = res.data
    } else {
      // 如果接口调用失败，使用列表中的数据
      currentRow.value = JSON.parse(JSON.stringify(row))
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    // 接口调用失败时使用列表中的数据
    currentRow.value = JSON.parse(JSON.stringify(row))
  }
  drawerVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitting.value = true
    let res

    if (isEdit.value) {
      const submitData = { ...form }
      if (!submitData.password) delete submitData.password
      res = await request.put('/collector/update', submitData)
    } else {
      res = await request.post('/collector/add', form)
    }

    if (res.code === '200' || res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      await loadData()
    } else {
      throw new Error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  nextTick(() => {
    if (formRef.value) {
      formRef.value.resetFields()
      formRef.value.clearValidate()
    }
    Object.assign(form, {
      id: null,
      username: '',
      name: '',
      phone: '',
      password: '',
      maxDailyOrders: 10,
      status: 1,
      workStatus: 1,
      auditStatus: 0
    })
  })
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
  }
})
</script>

<style scoped>
.collector-manage {
  background: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0,0,0,.1);
}

.search {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 0;
}

.operation {
  margin: 10px 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:global(.el-table__row:hover) {
  background-color: #f5f7fa !important;
}
</style>