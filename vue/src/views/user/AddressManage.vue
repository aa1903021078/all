<template>
  <div class="address-book">
    <!-- 简洁头部 -->
    <div class="book-header">
      <div class="header-left">
        <h1>我的地址</h1>
        <span class="count-badge">{{ addresses.length }} 个地址</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">
        新增地址
      </el-button>
    </div>

    <!-- 地址列表 -->
    <div v-if="addresses.length > 0" class="address-list">
      <div
          v-for="addr in addresses"
          :key="addr.id"
          class="list-row"
          :class="{ 'default-row': addr.isDefault === 1 }"
      >
        <!-- 左侧头像 -->
        <div class="user-avatar" :style="{ backgroundColor: stringToColor(addr.contactName) }">
          {{ addr.contactName ? addr.contactName.charAt(0) : '?' }}
        </div>

        <!-- 中间信息区 -->
        <div class="info-section">
          <!-- 第一行：姓名 + 电话 + 默认标签 -->
          <div class="primary-line">
            <span class="user-name">{{ addr.contactName || '未知联系人' }}</span>
            <span class="user-phone">{{ addr.contactPhone || '暂无电话' }}</span>
            <span v-if="addr.isDefault === 1" class="default-mark">
              <el-icon><Select /></el-icon>
              默认
            </span>
          </div>

          <!-- 第二行：详细地址 -->
          <div class="address-line">
            <el-icon class="pin-icon"><Location /></el-icon>
            <span class="full-address">
              {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}
            </span>
          </div>

          <!-- 第三行：建筑信息 -->
          <div class="meta-line" v-if="addr.buildingType !== undefined">
            <span class="building-label" :class="{ 'is-elevator': addr.buildingType === 1 }">
              {{ addr.buildingType === 1 ? '🏢 电梯房' : '🏠 楼梯房' + (addr.floor ? ' · ' + addr.floor + '层' : '') }}
            </span>
          </div>
        </div>

        <!-- 右侧操作 -->
        <div class="action-section">
          <button
              v-if="addr.isDefault !== 1"
              class="action-btn set-default"
              @click="setDefault(addr)"
              title="设为默认"
          >
            <el-icon><Star /></el-icon>
          </button>
          <button class="action-btn edit" @click="editAddress(addr)" title="编辑">
            <el-icon><Edit /></el-icon>
          </button>
          <button class="action-btn delete" @click="deleteAddress(addr)" title="删除">
            <el-icon><Delete /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">
        <el-icon :size="48"><Location /></el-icon>
      </div>
      <h3>暂无收货地址</h3>
      <p>添加地址后即可预约上门回收服务</p>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">
        添加地址
      </el-button>
    </div>

    <!-- 弹窗 -->
    <el-dialog
        v-model="showDialog"
        :title="isEdit ? '编辑地址' : '新增地址'"
        width="520px"
        class="dialog-style"
        destroy-on-close
    >
      <AddressForm
          :edit-data="editData"
          @success="onFormSuccess"
          @cancel="showDialog = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Star, Select, Location } from '@element-plus/icons-vue'
import request from '@/utils/request'
import AddressForm from '@/components/AddressForm.vue'

const addresses = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const editData = ref(null)
const user = JSON.parse(localStorage.getItem('user') || '{}')

// 根据姓名生成颜色
const stringToColor = (str) => {
  if (!str) return '#999'
  const colors = ['#4f46e5', '#0891b2', '#059669', '#d97706', '#dc2626', '#7c3aed', '#db2777']
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

onMounted(() => {
  loadAddresses()
})

const normalizeAddressData = (rawData) => {
  if (!Array.isArray(rawData)) return []
  return rawData.map(item => ({
    id: item.id,
    contactName: item.contactName || item.name || '未知',
    contactPhone: item.contactPhone || item.phone || '',
    province: item.province || '',
    city: item.city || '',
    district: item.district || item.area || '',
    detailAddress: item.detailAddress || item.address || item.detail || '',
    buildingType: item.buildingType !== undefined ? Number(item.buildingType) : 0,
    floor: item.floor ? Number(item.floor) : null,
    isDefault: item.isDefault === 1 || item.isDefault === true ? 1 : 0
  }))
}

const loadAddresses = async () => {
  try {
    const res = await request.get(`/userAddress/selectByUser/${user.id}`)
    let addressList = []
    if (res.code === '200' && Array.isArray(res.data)) {
      addressList = res.data
    } else if (Array.isArray(res)) {
      addressList = res
    } else if (res.data && Array.isArray(res.data)) {
      addressList = res.data
    }
    addresses.value = normalizeAddressData(addressList)
  } catch (e) {
    ElMessage.error('加载失败')
  }
}

const setDefault = async (addr) => {
  try {
    await request.put(`/userAddress/setDefault/${addr.id}`, null, {
      params: { userId: user.id }
    })
    ElMessage.success('已设为默认地址')
    loadAddresses()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

const openAddDialog = () => {
  isEdit.value = false
  editData.value = null
  showDialog.value = true
}

const editAddress = (addr) => {
  isEdit.value = true
  editData.value = { ...addr }
  showDialog.value = true
}

const deleteAddress = async (addr) => {
  try {
    await ElMessageBox.confirm(`确定删除 ${addr.contactName} 的地址吗？`, '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.delete(`/userAddress/delete/${addr.id}`)
    ElMessage.success('删除成功')
    loadAddresses()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const onFormSuccess = () => {
  showDialog.value = false
  loadAddresses()
}
</script>

<style scoped lang="scss">
.address-book {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

// 头部样式
.book-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: baseline;
    gap: 12px;

    h1 {
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
      margin: 0;
    }

    .count-badge {
      font-size: 14px;
      color: #6b7280;
      background: #fff;
      padding: 2px 10px;
      border-radius: 12px;
      border: 1px solid #e5e7eb;
    }
  }
}

// 列表容器
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

// 单行样式
.list-row {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 10px;
  padding: 20px 24px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    border-color: #d1d5db;
  }

  &.default-row {
    background: #f0fdf4;
    border-color: #86efac;
    border-left: 4px solid #22c55e;
    padding-left: 21px;
  }
}

// 头像
.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  color: white;
  margin-right: 20px;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

// 信息区
.info-section {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

// 第一行：姓名 + 电话 + 标签
.primary-line {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  .user-name {
    font-size: 17px;
    font-weight: 600;
    color: #111827; // 深黑色
  }

  .user-phone {
    font-size: 15px;
    font-weight: 500; // 中等粗细
    color: #374151; // 深灰色，确保清晰可见
    font-family: 'SF Mono', Monaco, monospace;
    letter-spacing: 0.5px;
  }

  .default-mark {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #16a34a;
    background: #dcfce7;
    padding: 3px 10px;
    border-radius: 6px;
    font-weight: 500;
    border: 1px solid #86efac;

    .el-icon {
      font-size: 14px;
    }
  }
}

// 第二行：地址
.address-line {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.5;

  .pin-icon {
    color: #9ca3af;
    font-size: 16px;
    flex-shrink: 0;
  }

  .full-address {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 第三行：建筑信息
.meta-line {
  .building-label {
    font-size: 13px;
    padding: 4px 10px;
    border-radius: 6px;
    background: #fef3c7;
    color: #92400e;
    border: 1px solid #fcd34d;

    &.is-elevator {
      background: #dbeafe;
      color: #1e40af;
      border-color: #93c5fd;
    }
  }
}

// 操作区
.action-section {
  display: flex;
  gap: 6px;
  margin-left: 16px;

  .action-btn {
    width: 36px;
    height: 36px;
    border: none;
    background: transparent;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #6b7280;
    font-size: 18px;
    transition: all 0.15s;

    &:hover {
      background: #f3f4f6;
      color: #1f2937;
    }

    &.set-default:hover {
      background: #fef9c3;
      color: #ca8a04;
    }

    &.edit:hover {
      background: #dbeafe;
      color: #2563eb;
    }

    &.delete:hover {
      background: #fee2e2;
      color: #dc2626;
    }
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #ffffff;
  border-radius: 10px;
  border: 1px dashed #d1d5db;

  .empty-icon {
    color: #d1d5db;
    margin-bottom: 16px;
  }

  h3 {
    margin: 0 0 8px;
    color: #374151;
    font-weight: 500;
  }

  p {
    margin: 0 0 24px;
    color: #9ca3af;
    font-size: 14px;
  }
}

// 响应式
@media (max-width: 768px) {
  .address-book {
    padding: 20px 16px;
  }

  .list-row {
    flex-wrap: wrap;
    padding: 16px;

    &.default-row {
      padding-left: 13px;
    }
  }

  .action-section {
    width: 100%;
    margin-left: 0;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f3f4f6;
    justify-content: flex-end;
  }
}
</style>

<style lang="scss">
.dialog-style {
  .el-dialog__header {
    margin: 0;
    padding: 20px 24px;
    border-bottom: 1px solid #f3f4f6;

    .el-dialog__title {
      font-weight: 600;
      font-size: 16px;
      color: #1f2937;
    }
  }

  .el-dialog__body {
    padding: 24px;
  }
}
</style>