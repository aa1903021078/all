<template>
  <el-card class="card-shadow !border-0">
    <template #header>
      <div class="flex justify-between items-center">
        <span class="font-medium">用户与权限管理</span>
        <div class="space-x-2">
          <el-input v-model="keyword" placeholder="搜索用户名/姓名/手机" clearable style="width:220px" @keyup.enter="load" />
          <el-select v-model="role" clearable placeholder="角色" style="width:120px" @change="load">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="医生" value="DOCTOR" />
            <el-option label="患者" value="PATIENT" />
          </el-select>
          <el-button :icon="Search" @click="load">查询</el-button>
          <el-button type="primary" :icon="Plus" @click="openEdit(null)">新增用户</el-button>
        </div>
      </div>
    </template>
    <el-table :data="list" stripe>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="role" label="角色" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="roleType(row.role)">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column label="所属医院" width="180">
        <template #default="{ row }">{{ hMap[row.hospitalId]?.name || '-' }}</template>
      </el-table-column>
      <el-table-column label="科室" width="120">
        <template #default="{ row }">{{ dMap[row.deptId]?.name || '-' }}</template>
      </el-table-column>
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column prop="enabled" label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link @click="resetPwd(row)">重置密码</el-button>
          <el-button link :type="row.enabled ? 'danger' : 'success'" @click="toggle(row)">
            {{ row.enabled ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="医生" value="DOCTOR" />
            <el-option label="患者" value="PATIENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始密码" v-if="!form.id"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" clearable style="width:100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <template v-if="form.role === 'DOCTOR'">
          <el-form-item label="医院">
            <el-select v-model="form.hospitalId" style="width:100%" @change="onHospitalChange">
              <el-option v-for="h in hospitals" :key="h.id" :label="h.name" :value="h.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="科室">
            <el-select v-model="form.deptId" style="width:100%">
              <el-option v-for="d in deptsForForm" :key="d.id" :label="d.name" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="职称"><el-input v-model="form.title" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { adminApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

const list = ref([]); const hospitals = ref([]); const depts = ref([])
const role = ref(''); const keyword = ref('')
const visible = ref(false)
const form = reactive({ id: null, username: '', realName: '', role: 'PATIENT', password: '',
  phone: '', gender: '', hospitalId: null, deptId: null, title: '' })

const hMap = computed(() => Object.fromEntries(hospitals.value.map(x => [x.id, x])))
const dMap = computed(() => Object.fromEntries(depts.value.map(x => [x.id, x])))
const deptsForForm = computed(() => depts.value.filter(d => !form.hospitalId || d.hospitalId === form.hospitalId))

const roleLabel = r => ({ ADMIN: '管理员', DOCTOR: '医生', PATIENT: '患者' })[r] || r
const roleType = r => ({ ADMIN: 'warning', DOCTOR: 'success', PATIENT: 'info' })[r] || ''

async function load() { list.value = (await adminApi.users({ role: role.value, keyword: keyword.value })).data }
function onHospitalChange() { form.deptId = null }
function openEdit(row) {
  Object.assign(form, row
    ? { ...row, password: '' }
    : { id: null, username: '', realName: '', role: 'PATIENT', password: '', phone: '', gender: '', hospitalId: null, deptId: null, title: '' })
  visible.value = true
}
async function submit() {
  if (!form.username || !form.realName) return ElMessage.warning('请填写完整')
  if (!form.id && (!form.password || form.password.length < 6)) return ElMessage.warning('初始密码至少 6 位')
  if (form.id) await adminApi.updateUser(form.id, form)
  else await adminApi.createUser(form)
  visible.value = false; ElMessage.success('已保存'); load()
}
async function resetPwd(row) {
  const { value } = await ElMessageBox.prompt('请输入新密码（≥6 位）', '重置密码', { inputPattern: /.{6,}/, inputErrorMessage: '至少 6 位' })
  await adminApi.resetPwd(row.id, value); ElMessage.success('已重置')
}
async function toggle(row) {
  await adminApi.toggleUser(row.id); ElMessage.success('已切换状态'); load()
}
onMounted(async () => {
  hospitals.value = (await adminApi.hospitals()).data
  depts.value = (await adminApi.departments()).data
  load()
})
</script>
