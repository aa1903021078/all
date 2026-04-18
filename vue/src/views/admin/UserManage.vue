<template>
  <div class="user-manage">
    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-box">
        <i class="el-icon-search"></i>
        <input
            v-model="searchKeyword"
            placeholder="搜索用户名/姓名/手机号"
            @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-button type="text" icon="el-icon-refresh" @click="resetSearch">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <div class="table-header">
        <span>用户列表</span>
        <span class="total">共 {{ total }} 条</span>
      </div>

      <el-table
          :data="userList"
          v-loading="loading"
          stripe
          style="width: 100%"
          border
      >
        <el-table-column type="index" label="#" width="50" align="center" />

        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div class="user-info-cell">
              <div class="avatar">{{ row.name ? row.name.charAt(0) : '?' }}</div>
              <div class="info">
                <div class="username">{{ row.username || '-' }}</div>
                <div class="name">{{ row.name || '未设置姓名' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" min-width="120">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>

        <el-table-column prop="points" label="积分" width="100" align="center">
          <template #default="{ row }">
            <span class="points">{{ row.points || 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="totalRecycleCount" label="回收次数" width="100" align="center">
          <template #default="{ row }">
            {{ row.totalRecycleCount || 0 }}
          </template>
        </el-table-column>

        <el-table-column prop="totalRecycleWeight" label="回收重量(kg)" width="120" align="center">
          <template #default="{ row }">
            {{ row.totalRecycleWeight || 0 }}
          </template>
        </el-table-column>

        <!-- 状态开关 -->
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                active-color="#67c23a"
                inactive-color="#f56c6c"
                @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link @click="handleView(row)">查看</el-button>
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="primary" link @click="handleResetPassword(row)">重置密码</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          background
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="pagination"
      />
    </div>

    <!-- 查看详情弹窗 -->
    <el-dialog
        v-model="detailVisible"
        title="用户详情"
        width="600px"
        :close-on-click-modal="false"
    >
      <div v-if="currentRow" class="detail-content">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <label>用户ID：</label>
              <span>{{ currentRow.id }}</span>
            </div>
            <div class="detail-item">
              <label>用户名：</label>
              <span>{{ currentRow.username || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>姓名：</label>
              <span>{{ currentRow.name || '未设置' }}</span>
            </div>
            <div class="detail-item">
              <label>手机号：</label>
              <span>{{ currentRow.phone || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>账户状态：</label>
              <el-tag :type="currentRow.status === 1 ? 'success' : 'danger'" size="small">
                {{ currentRow.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </div>
            <div class="detail-item">
              <label>注册时间：</label>
              <span>{{ currentRow.createTime || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>回收统计</h4>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-value">{{ currentRow.totalRecycleCount || 0 }}</div>
              <div class="stat-label">回收次数</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ currentRow.totalRecycleWeight || 0 }}<small>kg</small></div>
              <div class="stat-label">回收重量</div>
            </div>
            <div class="stat-card highlight">
              <div class="stat-value">{{ currentRow.points || 0 }}</div>
              <div class="stat-label">当前积分</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑用户弹窗 -->
    <el-dialog
        v-model="editVisible"
        title="编辑用户信息"
        width="500px"
        :close-on-click-modal="false"
    >
      <el-form v-if="editForm" :model="editForm" label-width="100px" ref="editFormRef">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input-number v-model="editForm.points" :min="0" :max="999999" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="账户状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitting">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 加载状态
const loading = ref(false)

// 数据列表
const userList = ref([])

// 分页参数
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索关键词
const searchKeyword = ref('')

// 弹窗控制
const detailVisible = ref(false)
const editVisible = ref(false)

// 当前选中行
const currentRow = ref(null)

// 编辑表单
const editForm = reactive({
  id: null,
  username: '',
  name: '',
  phone: '',
  points: 0,
  status: 1
})

// 提交状态
const submitting = ref(false)

// 表单引用
const editFormRef = ref(null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }

    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.name = searchKeyword.value.trim()
    }

    const res = await request.get('/user/selectPage', { params })

    if (res.code === '200' || res.code === 200) {
      const pageData = res.data

      if (pageData && pageData.list) {
        const list = Array.isArray(pageData.list) ? pageData.list : []

        userList.value = list.map(user => ({
          id: user.id,
          username: user.username || '',
          name: user.name || user.realName || '',
          phone: user.phone || user.mobile || '',
          points: user.points || 0,
          totalRecycleCount: user.totalRecycleCount || 0,
          totalRecycleWeight: user.totalRecycleWeight || 0,
          status: user.status !== undefined ? Number(user.status) : 1,
          createTime: user.createTime,
          updateTime: user.updateTime
        }))

        total.value = pageData.total || 0
        console.log('加载数据成功:', userList.value)
      }
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 处理状态切换
const handleStatusChange = async (row) => {
  const newStatus = row.status
  const actionText = newStatus === 1 ? '启用' : '禁用'

  try {
    const res = await request.put('/user/update', {
      id: row.id,
      status: newStatus
    })

    if (res.code === '200' || res.code === 200) {
      ElMessage.success(`${actionText}成功`)
    } else {
      row.status = newStatus === 1 ? 0 : 1
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    row.status = newStatus === 1 ? 0 : 1
    ElMessage.error('操作失败')
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  loadData()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  pageNum.value = 1
  loadData()
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  pageNum.value = 1
  loadData()
}

// 页码变化
const handleCurrentChange = (val) => {
  pageNum.value = val
  loadData()
}

// 查看详情
const handleView = (row) => {
  console.log('查看详情:', row)
  currentRow.value = { ...row }
  detailVisible.value = true
  console.log('detailVisible:', detailVisible.value)
}

// 编辑用户
const handleEdit = (row) => {
  console.log('编辑用户:', row)
  editForm.id = row.id
  editForm.username = row.username
  editForm.name = row.name || ''
  editForm.phone = row.phone || ''
  editForm.points = row.points || 0
  editForm.status = row.status !== undefined ? row.status : 1

  editVisible.value = true
  console.log('editVisible:', editVisible.value)
}

// 提交编辑
const submitEdit = async () => {
  if (!editForm || !editForm.id) {
    ElMessage.error('表单数据异常')
    return
  }

  submitting.value = true

  try {
    const res = await request.put('/user/update', editForm)

    if (res.code === '200' || res.code === 200) {
      ElMessage.success('编辑成功')
      editVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.msg || '编辑失败')
    }
  } catch (error) {
    ElMessage.error('编辑失败')
  } finally {
    submitting.value = false
  }
}

// 重置密码
const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户 "${row.username}" 的密码吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await request.post('/user/resetPassword', { id: row.id })

    if (res.code === '200' || res.code === 200) {
      ElMessage.success('密码重置成功')
    } else {
      ElMessage.error(res.msg || '重置失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
    }
  }
}

// 初始化加载
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-manage {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.search-box {
  display: flex;
  align-items: center;
  flex: 1;
  max-width: 500px;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 4px;
  border: 1px solid #e4e7ed;
}

.search-box i {
  color: #909399;
  font-size: 16px;
  margin: 0 12px;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px 0;
  font-size: 14px;
  outline: none;
  color: #606266;
}

.search-box input::placeholder {
  color: #c0c4cc;
}

.search-box .el-button {
  border-radius: 6px;
  margin-left: 8px;
}

.table-container {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.table-header .total {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #67c23a 0%, #529b2e 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.info .username {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
  margin-bottom: 4px;
}

.info .name {
  font-size: 12px;
  color: #909399;
}

.points {
  color: #e6a23c;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.detail-content {
  padding: 20px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #909399;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-size: 12px;
  color: #909399;
}

.detail-item span {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}

.stat-card.highlight {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.stat-card.highlight .stat-value {
  color: #d97706;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-value small {
  font-size: 12px;
  font-weight: 500;
  margin-left: 2px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}
</style>