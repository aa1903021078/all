<template>
  <div>
    <el-tabs v-model="activeTab" class="card sys-tabs">
      <!-- 角色权限 -->
      <el-tab-pane label="🔐 角色权限" name="rbac">
        <div class="rbac-layout">
          <div class="role-col">
            <div class="col-title">角色列表</div>
            <div
              v-for="r in roles"
              :key="r.id"
              class="role-item"
              :class="{ active: currentRole && currentRole.id === r.id }"
              @click="selectRole(r)"
            >
              <div class="bold">{{ r.name }}</div>
              <div class="muted text-sm">{{ r.code }}</div>
            </div>
          </div>
          <div class="perm-col">
            <template v-if="currentRole">
              <div class="flex justify-between items-center mb-12">
                <span class="bold">{{ currentRole.name }} · 权限分配</span>
                <el-button type="primary" size="small" :disabled="currentRole.code === 'ADMIN'" @click="savePerms">保存权限</el-button>
              </div>
              <el-alert
                v-if="currentRole.code === 'ADMIN'"
                type="warning"
                :closable="false"
                title="超级管理员固定拥有全部权限,不可修改"
                class="mb-12"
              />
              <el-checkbox-group v-model="selectedPerms" :disabled="currentRole.code === 'ADMIN'">
                <el-checkbox v-for="p in permissions" :key="p.id" :value="p.id" border class="perm-check">
                  <span class="bold">{{ p.name }}</span>
                  <span class="muted text-sm">&nbsp;{{ p.code }}</span>
                </el-checkbox>
              </el-checkbox-group>
            </template>
            <el-empty v-else description="请选择左侧角色" />
          </div>
        </div>
      </el-tab-pane>

      <!-- 系统参数 -->
      <el-tab-pane label="⚙️ 系统参数" name="config">
        <div class="flex justify-end mb-12">
          <el-button type="primary" size="small" @click="openConfig()"><el-icon><Plus /></el-icon>&nbsp;新增参数</el-button>
        </div>
        <el-table :data="configs" v-loading="configLoading">
          <el-table-column prop="configKey" label="参数键" width="200" />
          <el-table-column label="参数值" min-width="240">
            <template #default="{ row }">
              <el-input v-model="row.configValue" size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="说明" min-width="160" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="primary" text @click="saveConfigValue(row)">保存</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="configVisible" title="新增系统参数" width="420px">
      <el-form :model="configForm" label-width="80px">
        <el-form-item label="参数键"><el-input v-model="configForm.configKey" placeholder="如 site.name" /></el-form-item>
        <el-form-item label="参数值"><el-input v-model="configForm.configValue" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="configForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configVisible = false">取消</el-button>
        <el-button type="primary" @click="addConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { systemApi } from '@/api'

const activeTab = ref('rbac')
const roles = ref([])
const permissions = ref([])
const currentRole = ref(null)
const selectedPerms = ref([])

const configs = ref([])
const configLoading = ref(false)
const configVisible = ref(false)
const configForm = reactive({ configKey: '', configValue: '', remark: '' })

async function loadRbac() {
  roles.value = (await systemApi.roles()).data || []
  permissions.value = (await systemApi.permissions()).data || []
}
async function selectRole(r) {
  currentRole.value = r
  selectedPerms.value = (await systemApi.rolePermissions(r.id)).data || []
}
async function savePerms() {
  await systemApi.assignRolePermissions(currentRole.value.id, selectedPerms.value)
  ElMessage.success('权限已保存')
}

async function loadConfigs() {
  configLoading.value = true
  try {
    configs.value = (await systemApi.configs()).data || []
  } finally {
    configLoading.value = false
  }
}
async function saveConfigValue(row) {
  await systemApi.updateConfig(row.id, row.configValue)
  ElMessage.success('已更新')
}
function openConfig() {
  Object.assign(configForm, { configKey: '', configValue: '', remark: '' })
  configVisible.value = true
}
async function addConfig() {
  if (!configForm.configKey) return ElMessage.warning('请填写参数键')
  await systemApi.saveConfig({ ...configForm })
  ElMessage.success('已保存')
  configVisible.value = false
  loadConfigs()
}

onMounted(() => {
  loadRbac()
  loadConfigs()
})
</script>

<style scoped>
.sys-tabs {
  padding: 6px 20px 20px;
}
.rbac-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 16px;
}
.col-title {
  font-weight: 700;
  margin-bottom: 12px;
}
.role-item {
  padding: 12px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  margin-bottom: 10px;
  cursor: pointer;
}
.role-item:hover,
.role-item.active {
  border-color: var(--brand);
  background: var(--el-color-primary-light-9);
}
.perm-col {
  border-left: 1px solid var(--border);
  padding-left: 20px;
  min-height: 300px;
}
.perm-check {
  display: block;
  margin: 0 0 10px;
}
</style>
