<template>
  <div class="register-page">
    <div class="register-container">
      <h2 class="page-title">居民用户注册</h2>

      <!-- 提示信息 -->
      <div class="info-alert">
        <el-icon class="info-icon"><Info-Filled /></el-icon>
        <span>注册成功后，您可以在平台上预约废旧家电回收服务，获取环保积分。</span>
      </div>

      <!-- 注册表单 -->
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="register-form"
          label-position="top"
          @keyup.enter="handleRegister"
      >
        <!-- 用户名 -->
        <el-form-item label="用户名" prop="username" required>
          <el-input
              v-model="form.username"
              placeholder="请输入用户名（用于登录）"
              size="large"
              clearable
          />
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item label="手机号" prop="phone" required>
          <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              size="large"
              maxlength="11"
              clearable
          />
        </el-form-item>

        <!-- 所在地区（省市区级联） -->
        <el-form-item label="所在地区" prop="region" required>
          <el-cascader
              v-model="form.region"
              :options="regionOptions"
              :props="regionProps"
              placeholder="请选择省/市/区"
              size="large"
              style="width: 100%"
              filterable
              clearable
          />
        </el-form-item>

        <!-- 社区名称 -->
        <el-form-item label="社区名称" prop="community" required>
          <el-input
              v-model="form.community"
              placeholder="请输入您所在的社区或小区名称"
              size="large"
              clearable
          />
          <!-- 快捷选择 -->
          <div class="quick-tags" v-if="recommendedCommunities.length > 0">
            <span class="tag-label">推荐：</span>
            <el-tag
                v-for="comm in recommendedCommunities"
                :key="comm"
                size="small"
                effect="plain"
                class="quick-tag"
                @click="form.community = comm"
            >
              {{ comm }}
            </el-tag>
          </div>
        </el-form-item>

        <!-- 登录密码 -->
        <el-form-item label="登录密码" prop="password" required>
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请设置登录密码（6-20位）"
              size="large"
              show-password
          />
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
          />
        </el-form-item>

        <!-- 用户协议 -->
        <el-form-item prop="agreement" class="agreement-item">
          <el-checkbox v-model="form.agreement">
            我已阅读并同意
            <el-link type="primary" @click.prevent="showAgreement">《用户协议》</el-link>
            和
            <el-link type="primary" @click.prevent="showPrivacy">《隐私政策》</el-link>
          </el-checkbox>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleRegister"
          >
            立即注册
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部登录链接 -->
      <div class="bottom-link">
        <span>已有账号？</span>
        <el-link type="primary" @click="goLogin">立即登录</el-link>
      </div>
    </div>

    <!-- 用户协议弹窗 -->
    <el-dialog v-model="agreementVisible" title="用户协议" width="600px" align-center>
      <div class="agreement-content">
        <h4>社区废旧家电回收平台用户协议</h4>
        <p><strong>1. 服务说明</strong></p>
        <p>本平台为社区居民提供废旧家电上门回收服务，用户可通过平台预约回收、查询积分、兑换礼品等。</p>
        <p><strong>2. 用户责任</strong></p>
        <p>用户应确保提供的用户名、手机号及所在社区信息真实有效，以便回收人员上门服务。</p>
        <p><strong>3. 回收规则</strong></p>
        <p>用户预约回收后，回收员将在24小时内联系确认上门时间。回收完成后，用户将获得相应环保积分。</p>
        <p><strong>4. 隐私保护</strong></p>
        <p>平台承诺保护用户个人信息，仅用于回收服务及积分管理，不会泄露给第三方。</p>
        <p><strong>5. 违规处理</strong></p>
        <p>对于恶意预约、虚假信息等违规行为，平台有权暂停或注销用户账号。</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="agreementVisible = false">我已阅读</el-button>
      </template>
    </el-dialog>

    <!-- 隐私政策弹窗 -->
    <el-dialog v-model="privacyVisible" title="隐私政策" width="600px" align-center>
      <div class="agreement-content">
        <h4>社区废旧家电回收平台隐私政策</h4>
        <p>本政策说明我们如何收集、使用和保护您的个人信息：</p>
        <p><strong>1. 信息收集</strong></p>
        <p>我们收集您的用户名、手机号、所在社区信息，用于身份验证和上门回收服务。</p>
        <p><strong>2. 信息使用</strong></p>
        <p>您的信息仅用于：身份验证、回收服务联系、积分记录、安全风控。</p>
        <p><strong>3. 信息保护</strong></p>
        <p>我们采用加密技术保护您的数据，确保信息安全存储和传输。</p>
        <p><strong>4. 您的权利</strong></p>
        <p>您有权查看、修改或删除您的个人信息，请联系客服处理。</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="privacyVisible = false">我已阅读</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'
// 导入省市区数据
import regionData from '@/assets/region-data.json'

const router = useRouter()
const formRef = ref()

const loading = ref(false)
const agreementVisible = ref(false)
const privacyVisible = ref(false)

// 省市区数据
const regionOptions = ref(regionData)
const regionProps = {
  value: 'name',
  label: 'name',
  children: 'children',
  emitPath: true
}

// 推荐社区
const recommendedCommunities = ref([])

const form = reactive({
  username: '',
  phone: '',
  region: [],
  community: '',
  password: '',
  confirmPassword: '',
  agreement: false,
  role: 'USER'
})

// 密码确认校验
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 协议同意校验
const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议'))
  } else {
    callback()
  }
}

// 省市区校验
const validateRegion = (rule, value, callback) => {
  if (!value || value.length < 3) {
    callback(new Error('请选择完整的省市区信息'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    {required: true, message: '请输入用户名', trigger: 'blur'},
    {min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur'},
    {pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur'}
  ],
  phone: [
    {required: true, message: '请输入手机号', trigger: 'blur'},
    {pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur'}
  ],
  region: [
    {required: true, validator: validateRegion, trigger: 'change'}
  ],
  community: [
    {required: true, message: '请输入社区名称', trigger: 'blur'},
    {min: 2, max: 30, message: '社区名称长度在 2 到 30 个字符', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请设置密码', trigger: 'blur'},
    {min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur'},
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]+$/,
      message: '密码必须包含字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {required: true, validator: validateConfirmPassword, trigger: 'blur'}
  ],
  agreement: [
    {validator: validateAgreement, trigger: 'change'}
  ]
}

// 监听地区变化，更新推荐社区
watch(() => form.region, (val) => {
  if (val && val.length === 3) {
    const district = val[2]
    recommendedCommunities.value = getRecommendedCommunities(district)
  } else {
    recommendedCommunities.value = []
  }
})

// 根据区域获取推荐社区
const getRecommendedCommunities = (district) => {
  const communityMap = {
    '朝阳区': ['阳光社区', '望京花园', '亚运村小区', '三里屯社区'],
    '海淀区': ['中关村社区', '五道口小区', '西三旗花园', '学院路社区'],
    '东城区': ['东直门社区', '王府井小区', '安定门花园', '朝阳门社区'],
    '西城区': ['西单社区', '金融街小区', '德胜门花园', '新街口社区'],
    '浦东新区': ['陆家嘴社区', '张江小区', '金桥花园', '世博社区'],
    '天河区': ['天河城社区', '珠江新城小区', '石牌花园', '员村社区'],
    '越秀区': ['北京路社区', '东山口小区', '淘金花园', '环市东社区'],
    '南山区': ['科技园社区', '蛇口小区', '前海花园', '深圳湾社区'],
    '福田区': ['华强北社区', '香蜜湖小区', '购物公园花园', '皇岗社区'],
    '罗湖区': ['东门社区', '国贸小区', '罗湖花园', '蔡屋围社区'],
    '鼓楼区': ['鼓楼社区', '三牌楼小区', '宁海路花园', '湖南路社区'],
    '玄武区': ['新街口社区', '珠江路小区', '锁金村花园', '红山社区'],
    '西湖区': ['西湖社区', '黄龙小区', '文三路花园', '古荡社区'],
    '余杭区': ['临平社区', '乔司小区', '塘栖花园', '瓶窑社区'],
    '蜀山区': ['三里庵社区', '南七里站小区', '西园花园', '琥珀社区'],
    '包河区': ['芜湖路社区', '望湖城小区', '包公花园', '骆岗社区'],
    '金水区': ['花园路社区', '经三路小区', '紫荆山花园', '北林社区'],
    '二七区': ['德化街社区', '火车站小区', '万达花园', '福华街社区'],
    '武昌区': ['中南路社区', '水果湖小区', '司门口花园', '积玉桥社区'],
    '江汉区': ['万松园社区', '西北湖小区', '新华路花园', '唐家墩社区'],
    '岳麓区': ['岳麓山社区', '梅溪湖小区', '洋湖花园', '银盆岭社区'],
    '芙蓉区': ['五一广场社区', '火车站小区', '马王堆花园', '韭菜园社区'],
    '锦江区': ['春熙路社区', '盐市口小区', '合江亭花园', '三圣社区'],
    '武侯区': ['桐梓林社区', '玉林小区', '红牌楼花园', '簇桥社区'],
    '雁塔区': ['大雁塔社区', '小寨小区', '电子城花园', '曲江社区'],
    '新城区': ['解放路社区', '火车站小区', '自强路花园', '中山门社区'],
    '思明区': ['鼓浪屿社区', '中山路小区', '厦大花园', '莲前社区'],
    '湖里区': ['SM广场社区', '五缘湾小区', '枋湖花园', '殿前社区'],
    '历下区': ['泉城路社区', '山大路小区', '千佛山花园', '趵突泉社区'],
    '市南区': ['五四广场社区', '八大关小区', '栈桥花园', '台东社区']
  }

  return communityMap[district] || ['阳光社区', '幸福家园', '和谐小区', '绿源花园']
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const {agreement, confirmPassword, region, ...registerData} = form

    const submitData = {
      ...registerData,
      province: region[0],
      city: region[1],
      district: region[2]
    }

    const res = await request.post('/user/register', submitData)
    if (res.code === 200 || res.code === '200') {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || res.msg || '注册失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goLogin = () => {
  router.push('/login')
}

const showAgreement = () => {
  agreementVisible.value = true
}

const showPrivacy = () => {
  privacyVisible.value = true
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  padding: 40px 20px;
}

.register-container {
  width: 100%;
  max-width: 600px;
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.page-title {
  text-align: center;
  font-size: 28px;
  font-weight: 600;
  color: #000000;
  margin-bottom: 24px;
}

.info-alert {
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  padding: 14px 16px;
  margin-bottom: 24px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  color: #000000;
  font-size: 15px;
  line-height: 1.6;
  font-weight: 500;

  .info-icon {
    font-size: 18px;
    margin-top: 2px;
    flex-shrink: 0;
    color: #10b981;
  }
}

.register-form {
  /* 关键修复：标签颜色改为黑色，去掉必填项的红色 */
  :global(.el-form-item__label) {
    font-size: 16px;
    color: #000000 !important; /* 强制黑色 */
    font-weight: 600;
    padding-bottom: 10px;

    /* 去掉必填项的红色星号颜色 */
    &::before {
      color: #f56c6c;
      margin-right: 4px;
    }
  }

  /* 关键修复：输入框文字改为黑色 */
  :global(.el-input__inner) {
    border-radius: 4px;
    height: 48px;
    border-color: #dcdfe6;
    font-size: 16px; /* 增大字体 */
    color: #000000 !important; /* 强制输入文字为黑色 */

    &:focus {
      border-color: #10b981;
    }

    &::placeholder {
      color: #909399; /* placeholder保持灰色 */
      font-size: 14px;
    }
  }

  /* 关键修复：级联选择器文字颜色 */
  :global(.el-cascader .el-input__inner) {
    border-radius: 4px;
    height: 48px;
    font-size: 16px;
    color: #000000 !important;
  }

  /* 关键修复：级联选择器已选中的文字颜色 */
  :global(.el-cascader__tags) {
    font-size: 16px;
  }

  :global(.el-cascader .el-tag) {
    color: #000000;
    font-size: 14px;
  }

  /* 关键修复：输入框wrapper内的文字颜色 */
  :global(.el-input__wrapper) {
    color: #000000;
  }

  .quick-tags {
    margin-top: 10px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;

    .tag-label {
      font-size: 14px;
      color: #000000;
      font-weight: 500;
    }

    .quick-tag {
      cursor: pointer;
      transition: all 0.3s;
      color: #000000;
      font-size: 13px;
      padding: 6px 12px;

      &:hover {
        color: #10b981;
        border-color: #10b981;
        background: #f0f9eb;
      }
    }
  }

  .agreement-item {
    margin-top: 10px;
    margin-bottom: 10px;

    :global(.el-checkbox__label) {
      font-size: 15px;
      color: #000000;
      font-weight: 500;
    }

    :global(.el-link) {
      font-size: 15px;
      font-weight: 600;
    }
  }

  .submit-btn {
    width: 100%;
    height: 50px;
    border-radius: 4px;
    font-size: 18px;
    font-weight: 600;
    background: #10b981;
    border: none;
    color: #ffffff;

    &:hover {
      background: #059669;
    }

    &:active {
      background: #047857;
    }
  }
}

.bottom-link {
  text-align: center;
  margin-top: 24px;
  font-size: 16px;
  color: #000000;
  font-weight: 500;

  span {
    color: #000000;
  }

  :global(.el-link) {
    font-size: 16px;
    font-weight: 600;
  }
}

/* 协议内容样式 */
.agreement-content {
  max-height: 400px;
  overflow-y: auto;
  line-height: 1.8;
  color: #000000;
  font-size: 15px;

  h4 {
    margin-bottom: 16px;
    color: #000000;
    font-size: 18px;
    font-weight: 600;
  }

  p {
    margin-bottom: 12px;
    font-size: 15px;
    color: #000000;

    strong {
      color: #10b981;
      font-size: 15px;
    }
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .register-page {
    padding: 20px;
    background: #fff;
  }

  .register-container {
    box-shadow: none;
    padding: 20px;
  }

  .page-title {
    font-size: 24px;
  }

  .info-alert {
    font-size: 14px;
  }
}
</style>