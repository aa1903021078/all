<template>
  <div class="page-wrap">
    <!-- 用户卡片 -->
    <div class="profile-head card">
      <el-avatar :size="76" :src="auth.user?.avatar" />
      <div class="profile-info">
        <div class="flex items-center gap-8">
          <span class="nickname">{{ auth.user?.nickname }}</span>
          <el-tag v-for="r in roleTags" :key="r.code" :type="r.type" size="small">{{ r.label }}</el-tag>
        </div>
        <div class="muted mt-8">{{ auth.user?.bio || '这个人很懒,还没有简介~' }}</div>
      </div>
      <el-button @click="openEdit"><el-icon><Edit /></el-icon>&nbsp;编辑资料</el-button>
    </div>

    <el-tabs v-model="activeTab" class="card profile-tabs mt-16" @tab-change="onTab">
      <el-tab-pane label="我的笔记" name="notes">
        <div class="grid-cards" v-loading="loading">
          <NoteCard v-for="n in notes" :key="n.id" :note="n" />
        </div>
        <el-empty v-if="!loading && !notes.length" description="还没有发布探店笔记" />
      </el-tab-pane>

      <el-tab-pane label="我的菜谱" name="recipes">
        <div class="grid-cards" v-loading="loading">
          <RecipeCard v-for="r in recipes" :key="r.id" :recipe="r" />
        </div>
        <el-empty v-if="!loading && !recipes.length" description="还没有分享菜谱" />
      </el-tab-pane>

      <el-tab-pane label="点亮的店铺" name="checkin">
        <div class="grid-cards" v-loading="loading">
          <ShopCard v-for="s in checkinShops" :key="s.id" :shop="s" />
        </div>
        <el-empty v-if="!loading && !checkinShops.length" description="还没有点亮任何店铺" />
      </el-tab-pane>

      <el-tab-pane label="我的收藏" name="favorites">
        <div v-loading="loading">
          <!-- 店铺收藏夹 -->
          <div class="fav-head">
            <span class="sub-title">收藏的店铺</span>
            <div class="folder-bar">
              <span class="folder-chip" :class="{ active: shopFolder === null }" @click="selectShopFolder(null)">全部</span>
              <span v-for="f in shopFolders" :key="f.id" class="folder-chip" :class="{ active: shopFolder === f.id }" @click="selectShopFolder(f.id)">
                📁 {{ f.name }}
                <el-icon class="folder-del" @click.stop="removeFolder(f)"><Close /></el-icon>
              </span>
              <span class="folder-chip add" @click="openFolder('SHOP')"><el-icon><Plus /></el-icon>新建</span>
            </div>
          </div>
          <div class="grid-cards">
            <ShopCard v-for="s in favShops" :key="s.id" :shop="s" />
          </div>
          <el-empty v-if="!favShops.length" description="该收藏夹暂无店铺" :image-size="70" />

          <!-- 菜谱收藏夹 -->
          <div class="fav-head mt-16">
            <span class="sub-title">收藏的菜谱</span>
            <div class="folder-bar">
              <span class="folder-chip" :class="{ active: recipeFolder === null }" @click="selectRecipeFolder(null)">全部</span>
              <span v-for="f in recipeFolders" :key="f.id" class="folder-chip" :class="{ active: recipeFolder === f.id }" @click="selectRecipeFolder(f.id)">
                📁 {{ f.name }}
                <el-icon class="folder-del" @click.stop="removeFolder(f)"><Close /></el-icon>
              </span>
              <span class="folder-chip add" @click="openFolder('RECIPE')"><el-icon><Plus /></el-icon>新建</span>
            </div>
          </div>
          <div class="grid-cards">
            <RecipeCard v-for="r in favRecipes" :key="r.id" :recipe="r" />
          </div>
          <el-empty v-if="!favRecipes.length" description="该收藏夹暂无菜谱" :image-size="70" />
        </div>
      </el-tab-pane>

      <el-tab-pane name="messages">
        <template #label>
          我的消息<el-badge v-if="msgTotal" :value="msgTotal" :max="99" class="msg-badge" />
        </template>
        <div v-loading="loading">
          <el-tabs v-model="msgTab">
            <el-tab-pane :label="'收到的评论(' + messages.comments.length + ')'" name="comments">
              <div v-for="m in messages.comments" :key="'c' + m.id" class="msg-item" @click="goTarget(m)">
                <el-avatar :size="40" :src="m.fromUserAvatar" />
                <div class="flex-1">
                  <div><span class="bold">{{ m.fromUserName }}</span>
                    <span class="muted text-sm">&nbsp;{{ m.kind === 'REPLY' ? '回复了你' : '评论了你的' + targetLabel(m.targetType) }}</span>
                  </div>
                  <div class="msg-content">{{ m.content }}</div>
                  <div class="muted text-sm">《{{ m.targetTitle }}》 · {{ formatTime(m.createTime) }}</div>
                </div>
                <el-icon class="muted"><ArrowRight /></el-icon>
              </div>
              <el-empty v-if="!messages.comments.length" description="暂无评论/回复消息" />
            </el-tab-pane>
            <el-tab-pane :label="'收到的赞(' + messages.likes.length + ')'" name="likes">
              <div v-for="m in messages.likes" :key="'l' + m.id" class="msg-item" @click="goTarget(m)">
                <el-avatar :size="40" :src="m.fromUserAvatar" />
                <div class="flex-1">
                  <div><span class="bold">{{ m.fromUserName }}</span>
                    <span class="muted text-sm">&nbsp;赞了你的{{ targetLabel(m.targetType) }}</span>
                  </div>
                  <div class="muted text-sm">《{{ m.targetTitle }}》 · {{ formatTime(m.createTime) }}</div>
                </div>
                <span class="like-heart">❤️</span>
              </div>
              <el-empty v-if="!messages.likes.length" description="暂无点赞消息" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的预约" name="reservations">
        <div v-loading="loading">
          <div v-for="r in reservations" :key="r.id" class="reserve-item card">
            <img :src="r.shopCover" class="reserve-cover" />
            <div class="flex-1">
              <div class="bold">{{ r.shopName }}</div>
              <div class="muted text-sm mt-8">🕐 {{ formatTime(r.reserveTime) }} · {{ r.peopleCount }}人</div>
              <div class="muted text-sm">联系人:{{ r.contactName }} {{ r.contactPhone }}</div>
            </div>
            <div class="flex-col items-center gap-8">
              <el-tag :type="reserveTag(r.status)">{{ reserveText(r.status) }}</el-tag>
              <el-button v-if="r.status === 0 || r.status === 1" size="small" @click="cancelReserve(r)">取消预约</el-button>
              <el-button v-if="r.status === 1 || r.status === 3" size="small" type="danger" plain @click="checkinReserve(r)">🔥 到店点亮</el-button>
            </div>
          </div>
          <el-empty v-if="!reservations.length" description="还没有预约记录" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建收藏夹 -->
    <el-dialog v-model="folderVisible" title="新建收藏夹" width="380px">
      <el-input v-model="folderName" placeholder="收藏夹名称" maxlength="20" show-word-limit />
      <template #footer>
        <el-button @click="folderVisible = false">取消</el-button>
        <el-button type="primary" @click="createFolder">创建</el-button>
      </template>
    </el-dialog>

    <!-- 编辑资料 -->
    <el-dialog v-model="editVisible" title="编辑资料" width="440px">
      <el-form :model="editForm" label-width="70px">
        <el-form-item label="头像">
          <ImageUpload v-model="editForm.avatar" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="0">保密</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="editForm.bio" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noteApi, recipeApi, shopApi, favoriteApi, reservationApi, messageApi } from '@/api'
import { useAuthStore } from '@/store/auth'
import NoteCard from '@/components/NoteCard.vue'
import RecipeCard from '@/components/RecipeCard.vue'
import ShopCard from '@/components/ShopCard.vue'
import ImageUpload from '@/components/ImageUpload.vue'

const auth = useAuthStore()

const activeTab = ref('notes')
const loading = ref(false)
const notes = ref([])
const recipes = ref([])
const checkinShops = ref([])
const favShops = ref([])
const favRecipes = ref([])
const reservations = ref([])
const loaded = reactive({})
const router = useRouter()

const messages = ref({ likes: [], comments: [] })
const msgTab = ref('comments')
const msgTotal = computed(() => messages.value.likes.length + messages.value.comments.length)

const shopFolders = ref([])
const recipeFolders = ref([])
const shopFolder = ref(null)
const recipeFolder = ref(null)
const folderVisible = ref(false)
const folderName = ref('')
const folderType = ref('SHOP')

const editVisible = ref(false)
const editForm = reactive({ nickname: '', avatar: '', gender: 0, phone: '', bio: '' })

const roleMap = {
  ADMIN: { label: '管理员', type: 'danger' },
  REVIEWER: { label: '审核员', type: 'warning' },
  MERCHANT: { label: '商家', type: 'success' },
  USER: { label: '美食家', type: 'info' },
}
const roleTags = computed(() => (auth.roles || []).map((c) => ({ code: c, ...(roleMap[c] || { label: c, type: 'info' }) })))

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}
const reserveTextMap = { 0: '待确认', 1: '已确认', 2: '已拒绝', 3: '已完成', 4: '已取消' }
const reserveTagMap = { 0: 'info', 1: 'success', 2: 'danger', 3: 'primary', 4: 'info' }
function reserveText(s) {
  return reserveTextMap[s] || '待确认'
}
function reserveTag(s) {
  return reserveTagMap[s] || 'info'
}

async function onTab(name) {
  if (loaded[name]) return
  loading.value = true
  try {
    if (name === 'notes') notes.value = (await noteApi.mine()).data || []
    else if (name === 'recipes') recipes.value = (await recipeApi.mine()).data || []
    else if (name === 'checkin') checkinShops.value = (await shopApi.myCheckin()).data || []
    else if (name === 'favorites') {
      await loadFolders()
      await Promise.all([selectShopFolder(shopFolder.value), selectRecipeFolder(recipeFolder.value)])
    } else if (name === 'messages') {
      messages.value = (await messageApi.mine()).data || { likes: [], comments: [] }
    } else if (name === 'reservations') reservations.value = (await reservationApi.mine()).data || []
    loaded[name] = true
  } finally {
    loading.value = false
  }
}

function openEdit() {
  const u = auth.user || {}
  editForm.nickname = u.nickname
  editForm.avatar = u.avatar
  editForm.gender = u.gender || 0
  editForm.phone = u.phone
  editForm.bio = u.bio
  editVisible.value = true
}

async function saveProfile() {
  await userUpdate()
  await auth.fetchMe()
  editVisible.value = false
  ElMessage.success('资料已更新')
}
async function userUpdate() {
  const { userApi } = await import('@/api')
  await userApi.updateProfile({ ...editForm })
}

async function cancelReserve(r) {
  await reservationApi.cancel(r.id)
  r.status = 4
  ElMessage.success('已取消预约')
}

async function checkinReserve(r) {
  await shopApi.checkin(r.shopId)
  ElMessage.success('已点亮 ' + (r.shopName || '该店铺') + ' 🔥')
  loaded.checkin = false
}

// ---- 收藏夹 ----
async function loadFolders() {
  const [s, rc] = await Promise.all([favoriteApi.folders('SHOP'), favoriteApi.folders('RECIPE')])
  shopFolders.value = s.data || []
  recipeFolders.value = rc.data || []
}
async function selectShopFolder(id) {
  shopFolder.value = id
  favShops.value = (await favoriteApi.shops(id)).data || []
}
async function selectRecipeFolder(id) {
  recipeFolder.value = id
  favRecipes.value = (await favoriteApi.recipes(id)).data || []
}
function openFolder(type) {
  folderType.value = type
  folderName.value = ''
  folderVisible.value = true
}
async function createFolder() {
  if (!folderName.value.trim()) return ElMessage.warning('请输入收藏夹名称')
  await favoriteApi.createFolder({ name: folderName.value.trim(), type: folderType.value })
  folderVisible.value = false
  ElMessage.success('收藏夹已创建')
  await loadFolders()
}
async function removeFolder(f) {
  await ElMessageBox.confirm('删除收藏夹「' + f.name + '」? 夹内收藏将移至“全部”', '提示', { type: 'warning' })
  await favoriteApi.deleteFolder(f.id)
  ElMessage.success('已删除')
  if (f.type === 'SHOP' && shopFolder.value === f.id) shopFolder.value = null
  if (f.type === 'RECIPE' && recipeFolder.value === f.id) recipeFolder.value = null
  await loadFolders()
  await selectShopFolder(shopFolder.value)
  await selectRecipeFolder(recipeFolder.value)
}

// ---- 消息 ----
function targetLabel(type) {
  return type === 'RECIPE' ? '菜谱' : '笔记'
}
function goTarget(m) {
  if (m.targetType === 'RECIPE') router.push('/recipes/' + m.targetId)
  else router.push('/notes/' + m.targetId)
}

onMounted(() => onTab('notes'))
</script>

<style scoped>
.profile-head {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
}
.profile-info {
  flex: 1;
}
.nickname {
  font-size: 22px;
  font-weight: 800;
}
.profile-tabs {
  padding: 16px 20px;
}
.sub-title {
  font-weight: 700;
  margin-bottom: 12px;
}
.reserve-item {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 14px;
  margin-bottom: 12px;
}
.reserve-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}
.fav-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}
.folder-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.folder-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 16px;
  background: #f4f5f7;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.folder-chip:hover {
  color: var(--brand);
}
.folder-chip.active {
  background: var(--brand);
  color: #fff;
}
.folder-chip.add {
  border: 1px dashed #c0c4cc;
  background: transparent;
  color: #909399;
}
.folder-del {
  font-size: 12px;
  opacity: 0.6;
}
.folder-del:hover {
  color: #f56c6c;
  opacity: 1;
}
.msg-badge {
  margin-left: 4px;
}
.msg-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 6px;
  border-bottom: 1px solid #f2f2f2;
  cursor: pointer;
  transition: background 0.15s;
}
.msg-item:hover {
  background: var(--el-color-primary-light-9);
}
.msg-content {
  margin: 4px 0;
  color: #303133;
}
.like-heart {
  font-size: 18px;
}
</style>
