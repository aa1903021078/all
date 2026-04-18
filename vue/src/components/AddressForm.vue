<template>
  <div class="address-form">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">

      <!-- 城市选择 -->
      <el-form-item label="选择城市" required>
        <div class="city-selector">
          <el-cascader
              v-model="selectedCityPath"
              :options="cityOptions"
              :props="{ value: 'code', label: 'name', children: 'cities' }"
              placeholder="请选择省份 / 城市"
              filterable
              style="width: 100%"
              @change="onCityChange"
          />

          <div v-if="currentCity.code" class="city-info">
            <el-icon><MapLocation /></el-icon>
            <span>当前定位城市：<strong>{{ currentCity.name }}</strong></span>
            <span class="coord-hint">中心坐标：{{ currentCity.center.join(', ') }}</span>
          </div>
        </div>
      </el-form-item>

      <!-- 地图定位 -->
      <el-form-item label="地图定位" prop="latitude" v-if="currentCity.code">
        <div class="map-wrapper">
          <div class="map-container">
            <!-- 工具栏 -->
            <div class="map-toolbar">
              <div class="search-box">
                <el-icon><Search /></el-icon>
                <input
                    v-model="searchKeyword"
                    :placeholder="`在${currentCity.name}搜索地点`"
                    @focus="showSearchPanel = true"
                    @keyup.enter="handleSearch"
                />
                <el-icon v-if="searchKeyword" class="clear-btn" @click="clearSearch"><CircleClose /></el-icon>
              </div>
              <el-button size="small" class="locate-btn" @click="getCurrentLocation">
                <el-icon><Aim /></el-icon>
                定位当前位置
              </el-button>
            </div>

            <!-- 模拟地图 - 统一蓝色主题 -->
            <div
                class="mock-map"
                ref="mockMap"
                @click="handleMapClick"
            >
              <div class="city-watermark">{{ currentCity.name }}</div>
              <div class="grid-bg"></div>

              <!-- 区域标签 -->
              <div
                  v-for="(area, idx) in currentCity.areas"
                  :key="idx"
                  class="area-label"
                  :style="{ left: area.x + '%', top: area.y + '%' }"
              >
                {{ area.name }}
              </div>

              <!-- 定位标记 -->
              <div
                  v-if="form.latitude && form.longitude"
                  class="map-marker"
                  :style="markerStyle"
                  @mousedown.stop="startDrag"
                  @touchstart.stop="startDrag"
                  :key="markerKey"
              >
                <div class="marker-pin">
                  <el-icon><Location /></el-icon>
                </div>
                <div class="marker-pulse"></div>
                <div class="marker-label">拖拽调整位置</div>
              </div>

              <div v-else class="click-hint">
                <el-icon><Pointer /></el-icon>
                <span>点击地图选择具体位置</span>
              </div>

              <div class="zoom-controls">
                <div class="zoom-btn" @click.stop="zoomIn">+</div>
                <div class="zoom-level">{{ mapState.zoom }}</div>
                <div class="zoom-btn" @click.stop="zoomOut">-</div>
              </div>
            </div>

            <!-- 选中地址信息 -->
            <div class="selected-location" v-if="selectedAddress" :key="locationKey">
              <el-icon class="loc-icon"><LocationFilled /></el-icon>
              <div class="loc-info">
                <div class="loc-name">{{ selectedAddress.name }}</div>
                <div class="loc-detail">{{ selectedAddress.detail }}</div>
                <div class="loc-coord">
                  经纬度：{{ form.longitude?.toFixed(6) }}, {{ form.latitude?.toFixed(6) }}
                  <span class="update-tag" v-if="justUpdated">已更新</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 搜索结果面板 -->
          <div v-if="showSearchPanel && searchResults.length" class="search-panel">
            <div class="panel-header">
              <span>搜索结果（{{ currentCity.name }}）</span>
              <el-icon class="close-btn" @click="showSearchPanel = false"><Close /></el-icon>
            </div>
            <div
                v-for="(item, index) in searchResults"
                :key="index"
                class="search-item"
                @click="selectSearchResult(item)"
            >
              <el-icon><Location /></el-icon>
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-addr">{{ item.address }}</div>
                <div class="item-dist">距市中心 {{ item.distance }}km</div>
              </div>
            </div>
          </div>
        </div>
      </el-form-item>

      <el-form-item v-else>
        <div class="city-required-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>请先选择上方城市，再使用地图定位功能</span>
        </div>
      </el-form-item>

      <!-- 省市区选择 -->
      <el-form-item label="所在地区" prop="region">
        <el-cascader
            :key="cascaderKey"
            v-model="form.region"
            :options="regionData"
            :props="{
            value: 'name',
            label: 'name',
            children: 'children',
            checkStrictly: false
          }"
            placeholder="省 / 市 / 区"
            filterable
            clearable
            style="width: 100%"
            @change="onRegionChange"
        />
      </el-form-item>

      <!-- 详细地址 -->
      <el-form-item label="详细地址" prop="detailAddress">
        <el-input
            v-model="form.detailAddress"
            type="textarea"
            :rows="2"
            placeholder="街道、门牌号、小区楼栋等"
            maxlength="100"
            show-word-limit
        />
        <div class="input-hint" v-if="addressFromMap">
          <el-icon><InfoFilled /></el-icon>
          <span>已根据地图位置自动填充，可手动修改</span>
        </div>
      </el-form-item>

      <!-- 联系人信息 -->
      <el-divider content-position="left">
        <el-icon><User /></el-icon>
        联系人信息
      </el-divider>

      <el-form-item label="联系人" prop="contactName">
        <el-input v-model="form.contactName" placeholder="姓名" maxlength="20">
          <template #prefix><el-icon><User /></el-icon></template>
        </el-input>
      </el-form-item>

      <el-form-item label="手机号" prop="contactPhone">
        <el-input v-model="form.contactPhone" placeholder="手机号" maxlength="11">
          <template #prefix><el-icon><Phone /></el-icon></template>
        </el-input>
      </el-form-item>

      <!-- 楼栋信息 -->
      <el-divider content-position="left">
        <el-icon><OfficeBuilding /></el-icon>
        楼栋信息
      </el-divider>

      <el-form-item label="楼栋类型" prop="buildingType">
        <el-radio-group v-model="form.buildingType">
          <el-radio-button :label="1">
            <el-icon><OfficeBuilding /></el-icon> 电梯房
          </el-radio-button>
          <el-radio-button :label="2">
            <el-icon><House /></el-icon> 楼梯房
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="楼层" v-if="form.buildingType === 2" prop="floor">
        <el-input-number v-model="form.floor" :min="1" :max="50" controls-position="right" />
        <span class="unit">层</span>
      </el-form-item>

      <el-form-item label="设为默认">
        <el-switch
            v-model="form.isDefault"
            :active-value="1"
            :inactive-value="0"
            active-text="默认地址"
            inactive-text="普通地址"
        />
      </el-form-item>

      <el-form-item class="form-actions">
        <el-button type="primary" @click="submit" :loading="loading" size="large">
          <el-icon><Check /></el-icon>
          保存地址
        </el-button>
        <el-button @click="$emit('cancel')" size="large">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  MapLocation, Search, Aim, Location, InfoFilled, OfficeBuilding, House,
  User, Phone, LocationFilled, Pointer, Check, Close, CircleClose
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import regionData from '@/assets/region-data.json'

const emit = defineEmits(['success', 'cancel'])
const formRef = ref()
const loading = ref(false)
const searchKeyword = ref('')
const showSearchPanel = ref(false)
const selectedAddress = ref(null)
const searchResults = ref([])
const mockMap = ref(null)
const selectedCityPath = ref([])
const addressFromMap = ref(false)
const justUpdated = ref(false)
const markerKey = ref(0)
const locationKey = ref(0)
const cascaderKey = ref(0)

// 地图状态
const mapState = reactive({
  zoom: 14,
  centerX: 50,
  centerY: 50,
  dragging: false
})

// 扩展的城市数据库 - 包含更多区域点
const cityDatabase = {
  // 直辖市
  '110000': {
    code: '110000',
    name: '北京市',
    center: [116.4074, 39.9042],
    areas: [
      { name: '朝阳区', x: 55, y: 45 },
      { name: '海淀区', x: 35, y: 35 },
      { name: '东城区', x: 50, y: 48 },
      { name: '西城区', x: 45, y: 50 },
      { name: '丰台区', x: 40, y: 60 },
      { name: '通州区', x: 60, y: 55 }
    ],
    streets: ['建国路', '三里屯路', '中关村大街', '王府井大街', '西单北大街', '国贸路'],
    districts: ['朝阳区', '海淀区', '东城区', '西城区', '丰台区', '通州区']
  },
  '120000': {
    code: '120000',
    name: '天津市',
    center: [117.2009, 39.0842],
    areas: [
      { name: '和平区', x: 50, y: 50 },
      { name: '南开区', x: 45, y: 55 },
      { name: '河西区', x: 48, y: 60 },
      { name: '河东区', x: 55, y: 48 },
      { name: '滨海新区', x: 60, y: 65 },
      { name: '河北区', x: 52, y: 42 }
    ],
    streets: ['南京路', '滨江道', '和平路', '解放北路', '卫津路', '五大道'],
    districts: ['和平区', '南开区', '河西区', '河东区', '滨海新区', '河北区']
  },
  '310000': {
    code: '310000',
    name: '上海市',
    center: [121.4737, 31.2304],
    areas: [
      { name: '浦东新区', x: 60, y: 55 },
      { name: '黄浦区', x: 50, y: 50 },
      { name: '徐汇区', x: 45, y: 55 },
      { name: '静安区', x: 48, y: 45 },
      { name: '闵行区', x: 40, y: 65 },
      { name: '长宁区', x: 42, y: 48 }
    ],
    streets: ['南京路', '淮海中路', '陆家嘴环路', '徐家汇路', '人民大道', '延安西路'],
    districts: ['浦东新区', '黄浦区', '徐汇区', '静安区', '闵行区', '长宁区']
  },
  '500000': {
    code: '500000',
    name: '重庆市',
    center: [106.5516, 29.5630],
    areas: [
      { name: '渝中区', x: 50, y: 50 },
      { name: '江北区', x: 52, y: 45 },
      { name: '南岸区', x: 55, y: 55 },
      { name: '沙坪坝区', x: 40, y: 48 },
      { name: '九龙坡区', x: 45, y: 60 },
      { name: '渝北区', x: 58, y: 40 }
    ],
    streets: ['解放碑', '观音桥步行街', '南坪西路', '三峡广场', '杨家坪正街', '天街'],
    districts: ['渝中区', '江北区', '南岸区', '沙坪坝区', '九龙坡区', '渝北区']
  },
  // 广东省
  '440100': {
    code: '440100',
    name: '广州市',
    center: [113.2644, 23.1291],
    areas: [
      { name: '天河区', x: 55, y: 40 },
      { name: '越秀区', x: 45, y: 45 },
      { name: '海珠区', x: 50, y: 55 },
      { name: '白云区', x: 40, y: 30 },
      { name: '番禺区', x: 45, y: 70 },
      { name: '荔湾区', x: 42, y: 50 }
    ],
    streets: ['中山大道', '天河路', '北京路', '江南大道', '广州大道', '珠江新城'],
    districts: ['天河区', '越秀区', '海珠区', '白云区', '番禺区', '荔湾区']
  },
  '440300': {
    code: '440300',
    name: '深圳市',
    center: [114.0859, 22.547],
    areas: [
      { name: '福田区', x: 45, y: 45 },
      { name: '南山区', x: 40, y: 55 },
      { name: '罗湖区', x: 50, y: 40 },
      { name: '宝安区', x: 35, y: 35 },
      { name: '龙岗区', x: 55, y: 30 },
      { name: '龙华区', x: 48, y: 38 }
    ],
    streets: ['深南大道', '滨河大道', '华强北路', '南海大道', '宝安大道', '科技路'],
    districts: ['福田区', '南山区', '罗湖区', '宝安区', '龙岗区', '龙华区']
  },
  // 江苏省
  '320100': {
    code: '320100',
    name: '南京市',
    center: [118.7969, 32.0603],
    areas: [
      { name: '玄武区', x: 52, y: 42 },
      { name: '秦淮区', x: 48, y: 50 },
      { name: '建邺区', x: 42, y: 55 },
      { name: '鼓楼区', x: 45, y: 40 },
      { name: '雨花台区', x: 45, y: 65 },
      { name: '栖霞区', x: 58, y: 35 }
    ],
    streets: ['中山路', '汉中路', '江东中路', '雨花西路', '夫子庙步行街', '软件大道'],
    districts: ['玄武区', '秦淮区', '建邺区', '鼓楼区', '雨花台区', '栖霞区']
  },
  '320500': {
    code: '320500',
    name: '苏州市',
    center: [120.5853, 31.2989],
    areas: [
      { name: '姑苏区', x: 48, y: 48 },
      { name: '工业园区', x: 55, y: 45 },
      { name: '高新区', x: 40, y: 45 },
      { name: '吴中区', x: 45, y: 60 },
      { name: '相城区', x: 48, y: 35 },
      { name: '吴江区', x: 50, y: 70 }
    ],
    streets: ['人民路', '观前街', '星海街', '狮山路', '东吴北路', '金鸡湖大道'],
    districts: ['姑苏区', '工业园区', '高新区', '吴中区', '相城区', '吴江区']
  },
  // 浙江省
  '330100': {
    code: '330100',
    name: '杭州市',
    center: [120.1551, 30.2741],
    areas: [
      { name: '西湖区', x: 40, y: 45 },
      { name: '上城区', x: 50, y: 50 },
      { name: '滨江区', x: 55, y: 55 },
      { name: '余杭区', x: 35, y: 35 },
      { name: '萧山区', x: 60, y: 60 },
      { name: '拱墅区', x: 45, y: 42 }
    ],
    streets: ['延安路', '解放路', '文三路', '江南大道', '西湖大道', '钱江新城'],
    districts: ['西湖区', '上城区', '滨江区', '余杭区', '萧山区', '拱墅区']
  },
  // 安徽省
  '340100': {
    code: '340100',
    name: '合肥市',
    center: [117.2272, 31.8206],
    areas: [
      { name: '庐阳区', x: 48, y: 45 },
      { name: '蜀山区', x: 40, y: 50 },
      { name: '包河区', x: 52, y: 55 },
      { name: '瑶海区', x: 55, y: 40 },
      { name: '政务区', x: 42, y: 58 },
      { name: '滨湖新区', x: 50, y: 65 }
    ],
    streets: ['长江中路', '徽州大道', '金寨路', '芜湖路', '望江西路', '政务环路'],
    districts: ['庐阳区', '蜀山区', '包河区', '瑶海区', '政务区', '滨湖新区']
  },
  // 河南省
  '410100': {
    code: '410100',
    name: '郑州市',
    center: [113.6253, 34.7466],
    areas: [
      { name: '金水区', x: 50, y: 42 },
      { name: '二七区', x: 45, y: 55 },
      { name: '中原区', x: 40, y: 48 },
      { name: '管城回族区', x: 55, y: 50 },
      { name: '郑东新区', x: 58, y: 40 },
      { name: '惠济区', x: 48, y: 30 }
    ],
    streets: ['金水路', '中原路', '紫荆山路', '花园路', '商务外环路', '农业路'],
    districts: ['金水区', '二七区', '中原区', '管城回族区', '郑东新区', '惠济区']
  },
  // 湖北省
  '420100': {
    code: '420100',
    name: '武汉市',
    center: [114.3054, 30.5931],
    areas: [
      { name: '江岸区', x: 52, y: 42 },
      { name: '江汉区', x: 48, y: 45 },
      { name: '硚口区', x: 42, y: 48 },
      { name: '武昌区', x: 55, y: 55 },
      { name: '洪山区', x: 58, y: 60 },
      { name: '汉阳区', x: 45, y: 50 }
    ],
    streets: ['解放大道', '中山大道', '珞喻路', '徐东大街', '建设大道', '光谷大道'],
    districts: ['江岸区', '江汉区', '硚口区', '武昌区', '洪山区', '汉阳区']
  },
  // 湖南省
  '430100': {
    code: '430100',
    name: '长沙市',
    center: [112.9388, 28.2282],
    areas: [
      { name: '芙蓉区', x: 55, y: 48 },
      { name: '天心区', x: 50, y: 58 },
      { name: '岳麓区', x: 40, y: 50 },
      { name: '开福区', x: 48, y: 38 },
      { name: '雨花区', x: 58, y: 55 },
      { name: '望城区', x: 35, y: 35 }
    ],
    streets: ['五一大道', '芙蓉中路', '岳麓大道', '韶山北路', '湘江中路', '金星路'],
    districts: ['芙蓉区', '天心区', '岳麓区', '开福区', '雨花区', '望城区']
  },
  // 四川省
  '510100': {
    code: '510100',
    name: '成都市',
    center: [104.0668, 30.5728],
    areas: [
      { name: '锦江区', x: 50, y: 50 },
      { name: '青羊区', x: 45, y: 45 },
      { name: '武侯区', x: 45, y: 55 },
      { name: '成华区', x: 55, y: 45 },
      { name: '金牛区', x: 40, y: 40 },
      { name: '高新区', x: 52, y: 58 }
    ],
    streets: ['人民南路', '蜀都大道', '红星路', '二环路', '天府大道', '益州大道'],
    districts: ['锦江区', '青羊区', '武侯区', '成华区', '金牛区', '高新区']
  },
  // 陕西省
  '610100': {
    code: '610100',
    name: '西安市',
    center: [108.9398, 34.3416],
    areas: [
      { name: '新城区', x: 52, y: 45 },
      { name: '碑林区', x: 48, y: 50 },
      { name: '莲湖区', x: 45, y: 42 },
      { name: '雁塔区', x: 50, y: 60 },
      { name: '未央区', x: 48, y: 35 },
      { name: '灞桥区', x: 55, y: 48 }
    ],
    streets: ['解放路', '长安南路', '未央路', '西大街', '高新路', '唐延路'],
    districts: ['新城区', '碑林区', '莲湖区', '雁塔区', '未央区', '灞桥区']
  },
  // 福建省
  '350100': {
    code: '350100',
    name: '福州市',
    center: [119.2965, 26.0745],
    areas: [
      { name: '鼓楼区', x: 48, y: 42 },
      { name: '台江区', x: 50, y: 50 },
      { name: '仓山区', x: 48, y: 60 },
      { name: '晋安区', x: 55, y: 45 },
      { name: '马尾区', x: 60, y: 55 },
      { name: '长乐区', x: 58, y: 65 }
    ],
    streets: ['五四路', '八一七路', '台江路', '则徐大道', '福马路', '江滨大道'],
    districts: ['鼓楼区', '台江区', '仓山区', '晋安区', '马尾区', '长乐区']
  },
  '350200': {
    code: '350200',
    name: '厦门市',
    center: [118.0894, 24.4798],
    areas: [
      { name: '思明区', x: 50, y: 55 },
      { name: '湖里区', x: 52, y: 45 },
      { name: '集美区', x: 40, y: 40 },
      { name: '海沧区', x: 35, y: 50 },
      { name: '翔安区', x: 60, y: 48 },
      { name: '同安区', x: 45, y: 35 }
    ],
    streets: ['厦禾路', '湖滨南路', '嘉禾路', '海沧大道', '翔安大道', '集美大道'],
    districts: ['思明区', '湖里区', '集美区', '海沧区', '翔安区', '同安区']
  },
  // 山东省
  '370200': {
    code: '370200',
    name: '青岛市',
    center: [120.3826, 36.0671],
    areas: [
      { name: '市南区', x: 50, y: 55 },
      { name: '市北区', x: 48, y: 45 },
      { name: '李沧区', x: 55, y: 40 },
      { name: '崂山区', x: 60, y: 50 },
      { name: '黄岛区', x: 40, y: 60 },
      { name: '城阳区', x: 52, y: 35 }
    ],
    streets: ['香港中路', '台东三路', '金水路', '海尔路', '长江中路', '山东路'],
    districts: ['市南区', '市北区', '李沧区', '崂山区', '黄岛区', '城阳区']
  },
  '370100': {
    code: '370100',
    name: '济南市',
    center: [117.1205, 36.6510],
    areas: [
      { name: '历下区', x: 52, y: 50 },
      { name: '市中区', x: 48, y: 55 },
      { name: '槐荫区', x: 42, y: 48 },
      { name: '天桥区', x: 48, y: 42 },
      { name: '历城区', x: 58, y: 48 },
      { name: '长清区', x: 40, y: 60 }
    ],
    streets: ['经十路', '纬二路', '经七路', '济洛路', '花园路', '奥体中路'],
    districts: ['历下区', '市中区', '槐荫区', '天桥区', '历城区', '长清区']
  }
}

// 级联选择器数据 - 按省份分组
const cityOptions = [
  {
    name: '直辖市',
    code: '000000',
    cities: [
      { name: '北京市', code: '110000', center: [116.4074, 39.9042] },
      { name: '天津市', code: '120000', center: [117.2009, 39.0842] },
      { name: '上海市', code: '310000', center: [121.4737, 31.2304] },
      { name: '重庆市', code: '500000', center: [106.5516, 29.5630] }
    ]
  },
  {
    name: '广东省',
    code: '440000',
    cities: [
      { name: '广州市', code: '440100', center: [113.2644, 23.1291] },
      { name: '深圳市', code: '440300', center: [114.0859, 22.547] }
    ]
  },
  {
    name: '江苏省',
    code: '320000',
    cities: [
      { name: '南京市', code: '320100', center: [118.7969, 32.0603] },
      { name: '苏州市', code: '320500', center: [120.5853, 31.2989] }
    ]
  },
  {
    name: '浙江省',
    code: '330000',
    cities: [
      { name: '杭州市', code: '330100', center: [120.1551, 30.2741] }
    ]
  },
  {
    name: '安徽省',
    code: '340000',
    cities: [
      { name: '合肥市', code: '340100', center: [117.2272, 31.8206] }
    ]
  },
  {
    name: '河南省',
    code: '410000',
    cities: [
      { name: '郑州市', code: '410100', center: [113.6253, 34.7466] }
    ]
  },
  {
    name: '湖北省',
    code: '420000',
    cities: [
      { name: '武汉市', code: '420100', center: [114.3054, 30.5931] }
    ]
  },
  {
    name: '湖南省',
    code: '430000',
    cities: [
      { name: '长沙市', code: '430100', center: [112.9388, 28.2282] }
    ]
  },
  {
    name: '四川省',
    code: '510000',
    cities: [
      { name: '成都市', code: '510100', center: [104.0668, 30.5728] }
    ]
  },
  {
    name: '陕西省',
    code: '610000',
    cities: [
      { name: '西安市', code: '610100', center: [108.9398, 34.3416] }
    ]
  },
  {
    name: '福建省',
    code: '350000',
    cities: [
      { name: '福州市', code: '350100', center: [119.2965, 26.0745] },
      { name: '厦门市', code: '350200', center: [118.0894, 24.4798] }
    ]
  },
  {
    name: '山东省',
    code: '370000',
    cities: [
      { name: '济南市', code: '370100', center: [117.1205, 36.6510] },
      { name: '青岛市', code: '370200', center: [120.3826, 36.0671] }
    ]
  }
]

const currentCity = computed(() => {
  if (!selectedCityPath.value.length) return {}
  const cityCode = selectedCityPath.value[selectedCityPath.value.length - 1]
  return cityDatabase[cityCode] || {}
})

const form = reactive({
  userId: '',
  contactName: '',
  contactPhone: '',
  region: [],
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  latitude: null,
  longitude: null,
  buildingType: 1,
  floor: 1,
  isDefault: 0
})

const rules = {
  region: [{ required: true, message: '请选择所在地区', trigger: 'change' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  floor: [{ required: true, message: '请输入楼层', trigger: 'change', type: 'number' }],
  latitude: [{ required: true, message: '请在地图上选择位置', trigger: 'change', type: 'number' }]
}

const markerStyle = computed(() => ({
  left: `${mapState.centerX}%`,
  top: `${mapState.centerY}%`
}))

// 城市切换
const onCityChange = (val) => {
  if (!val || !val.length) return
  const cityCode = val[val.length - 1]
  const city = cityDatabase[cityCode]

  if (city) {
    mapState.centerX = 50
    mapState.centerY = 50
    form.latitude = null
    form.longitude = null
    selectedAddress.value = null
    form.detailAddress = ''
    addressFromMap.value = false
    markerKey.value++

    const provinceName = cityOptions.find(p => p.cities?.some(c => c.code === cityCode))?.name || city.name
    form.province = provinceName === '直辖市' ? city.name : provinceName
    form.city = city.name
    form.district = city.districts[0]
    form.region = [form.province, city.name, form.district]

    ElMessage.success(`已切换到${city.name}，请在地图上选择具体位置`)
  }
}

// 生成城市地址
const generateCityAddress = (x, y) => {
  const city = currentCity.value
  if (!city.code) return { name: '未知位置', detail: '' }

  let nearestArea = city.areas[0]
  let minDist = Infinity

  city.areas.forEach(area => {
    const dist = Math.sqrt(Math.pow(area.x - x, 2) + Math.pow(area.y - y, 2))
    if (dist < minDist) {
      minDist = dist
      nearestArea = area
    }
  })

  const district = nearestArea.name
  const streetIndex = Math.floor((x * 3 + y * 2) % city.streets.length)
  const street = city.streets[streetIndex]
  const number = Math.floor(100 + x * 8 + y * 5)

  return {
    name: `${city.name}${district}选中位置`,
    detail: `${city.name}${district}${street}${number}号附近`,
    district: district
  }
}

// 选择位置
const selectLocation = (x, y, name, detail, district) => {
  mapState.centerX = x
  mapState.centerY = y

  const city = currentCity.value
  const scale = 20 / mapState.zoom
  const lngOffset = (x - 50) * 0.001 * scale
  const latOffset = (y - 50) * 0.001 * scale

  form.longitude = parseFloat((city.center[0] + lngOffset).toFixed(6))
  form.latitude = parseFloat((city.center[1] - latOffset).toFixed(6))

  selectedAddress.value = {
    name: name,
    detail: detail,
    timestamp: Date.now()
  }

  form.detailAddress = detail
  addressFromMap.value = true

  // 关键修复：确保省市区同步更新
  if (district) {
    // 确保 province 和 city 已设置（直辖市情况下可能相同）
    if (!form.province) form.province = city.name
    if (!form.city) form.city = city.name

    form.district = district

    // 重新赋值整个数组触发响应式更新
    form.region = [form.province, form.city, district]

    // 强制刷新级联选择器组件
    cascaderKey.value++

    console.log('[Map] 更新省市区:', form.region)
  }

  markerKey.value++
  locationKey.value++

  justUpdated.value = true
  setTimeout(() => {
    justUpdated.value = false
  }, 2000)
}

// 地图点击
const handleMapClick = (e) => {
  if (!mockMap.value || mapState.dragging || !currentCity.value.code) return

  const rect = mockMap.value.getBoundingClientRect()
  const x = ((e.clientX - rect.left) / rect.width) * 100
  const y = ((e.clientY - rect.top) / rect.height) * 100

  if (x < 0 || x > 100 || y < 0 || y > 100) return

  const address = generateCityAddress(x, y)
  selectLocation(x, y, address.name, address.detail, address.district)

  ElMessage.success(`已定位到：${address.district}`)
}

// 拖拽
const startDrag = (e) => {
  e.preventDefault()
  e.stopPropagation()
  mapState.dragging = false

  const startX = e.type.includes('touch') ? e.touches[0].clientX : e.clientX
  const startY = e.type.includes('touch') ? e.touches[0].clientY : e.clientY

  const handleMove = (moveEvent) => {
    mapState.dragging = true
    moveEvent.preventDefault()

    const clientX = moveEvent.type.includes('touch') ? moveEvent.touches[0].clientX : moveEvent.clientX
    const clientY = moveEvent.type.includes('touch') ? moveEvent.touches[0].clientY : moveEvent.clientY

    if (!mockMap.value) return
    const rect = mockMap.value.getBoundingClientRect()

    let newX = ((clientX - rect.left) / rect.width) * 100
    let newY = ((clientY - rect.top) / rect.height) * 100

    mapState.centerX = Math.max(5, Math.min(95, newX))
    mapState.centerY = Math.max(5, Math.min(95, newY))
  }

  const handleEnd = () => {
    document.removeEventListener('mousemove', handleMove)
    document.removeEventListener('mouseup', handleEnd)
    document.removeEventListener('touchmove', handleMove)
    document.removeEventListener('touchend', handleEnd)

    if (mapState.dragging) {
      const address = generateCityAddress(mapState.centerX, mapState.centerY)
      selectLocation(mapState.centerX, mapState.centerY, address.name, address.detail, address.district)
      ElMessage.success('位置已调整至：' + address.district)
    }

    setTimeout(() => { mapState.dragging = false }, 100)
  }

  document.addEventListener('mousemove', handleMove)
  document.addEventListener('mouseup', handleEnd)
  document.addEventListener('touchmove', handleMove, { passive: false })
  document.addEventListener('touchend', handleEnd)
}

// 搜索
const handleSearch = () => {
  if (!searchKeyword.value.trim() || !currentCity.value.code) return

  const city = currentCity.value
  const keyword = searchKeyword.value

  searchResults.value = [
    {
      name: `${city.name}${keyword}大厦`,
      address: `${city.name}${city.districts[0]}${keyword}路100号`,
      x: 45,
      y: 45,
      distance: (Math.random() * 3 + 1).toFixed(1)
    },
    {
      name: `${keyword}广场`,
      address: `${city.name}${city.districts[1]}${keyword}大街88号`,
      x: 55,
      y: 48,
      distance: (Math.random() * 5 + 2).toFixed(1)
    },
    {
      name: `${keyword}商业中心`,
      address: `${city.name}${city.districts[2]}${keyword}街156号`,
      x: 48,
      y: 52,
      distance: (Math.random() * 8 + 3).toFixed(1)
    }
  ]
  showSearchPanel.value = true
}

const clearSearch = () => {
  searchKeyword.value = ''
  showSearchPanel.value = false
}

const selectSearchResult = (item) => {
  const city = currentCity.value
  const district = item.address.match(/(.*?区)/)?.[0]?.replace(city.name, '') || city.districts[0]

  selectLocation(item.x, item.y, item.name, item.address, district)
  searchKeyword.value = item.name
  showSearchPanel.value = false
  ElMessage.success(`已定位到：${item.name}`)
}

// 定位当前位置
const getCurrentLocation = () => {
  if (!currentCity.value.code) {
    ElMessage.warning('请先选择城市')
    return
  }

  ElMessage.info(`正在${currentCity.value.name}定位...`)
  setTimeout(() => {
    const offsetX = (Math.random() - 0.5) * 20
    const offsetY = (Math.random() - 0.5) * 20
    const x = 50 + offsetX
    const y = 50 + offsetY

    const address = generateCityAddress(x, y)
    selectLocation(x, y, `${currentCity.value.name}当前位置`, address.detail, address.district)
    ElMessage.success(`已在${currentCity.value.name}定位成功`)
  }, 800)
}

const zoomIn = () => { if (mapState.zoom < 20) mapState.zoom++ }
const zoomOut = () => { if (mapState.zoom > 3) mapState.zoom-- }

const onRegionChange = (val) => {
  if (val && val.length === 3) {
    form.province = val[0]
    form.city = val[1]
    form.district = val[2]
  }
}

// 提交表单
const submit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  if (!form.latitude || !form.longitude) {
    ElMessage.warning('请在地图上选择位置')
    return
  }

  loading.value = true
  try {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      ElMessage.error('登录信息已失效')
      return
    }

    const user = JSON.parse(userStr)
    if (!user?.id) {
      ElMessage.error('用户ID不存在')
      return
    }

    form.userId = user.id
    const submitData = { ...form }
    delete submitData.region

    const res = await request.post('/userAddress/add', submitData)

    if (res?.code === '200' || res?.data?.id || res?.id) {
      ElMessage.success('地址添加成功')
      emit('success', res.data || res)
    } else {
      throw new Error(res?.msg || '添加失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '添加失败')
  } finally {
    loading.value = false
  }
}

const handleClickOutside = (e) => {
  if (!e.target.closest('.search-box') && !e.target.closest('.search-panel')) {
    showSearchPanel.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped lang="scss">
.address-form {
  max-width: 800px;
  margin: 0 auto;

  .city-selector {
    .city-info {
      margin-top: 8px;
      padding: 8px 12px;
      background: #f0f9ff;
      border-radius: 4px;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #096dd9;
      font-size: 13px;

      .coord-hint {
        margin-left: auto;
        color: #8c8c8c;
        font-size: 12px;
      }
    }
  }

  .city-required-tip {
    padding: 40px;
    text-align: center;
    color: #909399;
    background: #f5f7fa;
    border-radius: 8px;
    border: 2px dashed #dcdfe6;

    .el-icon {
      font-size: 24px;
      margin-bottom: 8px;
      display: block;
    }
  }

  .map-wrapper {
    position: relative;
  }

  .map-container {
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    overflow: hidden;

    .map-toolbar {
      padding: 12px;
      background: #f5f7fa;
      border-bottom: 1px solid #e4e7ed;
      display: flex;
      gap: 10px;
      align-items: center;

      .search-box {
        flex: 1;
        display: flex;
        align-items: center;
        background: white;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        padding: 0 12px;
        height: 32px;

        &:focus-within {
          border-color: #409eff;
        }

        input {
          flex: 1;
          border: none;
          outline: none;
          font-size: 13px;
          margin: 0 8px;
          color: #606266;
        }

        .clear-btn {
          cursor: pointer;
          color: #c0c4cc;

          &:hover {
            color: #909399;
          }
        }
      }

      .locate-btn {
        border-color: #dcdfe6;
        color: #606266;

        &:hover {
          color: #409eff;
          border-color: #409eff;
          background: #ecf5ff;
        }
      }
    }

    // 统一蓝色主题地图
    .mock-map {
      height: 250px;
      position: relative;
      overflow: hidden;
      cursor: crosshair;
      background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%); // 统一蓝色背景

      .city-watermark {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 64px;
        font-weight: bold;
        color: rgba(25, 118, 210, 0.08); // 蓝色水印
        pointer-events: none;
        user-select: none;
      }

      .grid-bg {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-image:
            linear-gradient(rgba(255,255,255,0.5) 1px, transparent 1px),
            linear-gradient(90deg, rgba(255,255,255,0.5) 1px, transparent 1px);
        background-size: 40px 40px;
        pointer-events: none;
      }

      .area-label {
        position: absolute;
        transform: translate(-50%, -50%);
        padding: 4px 8px;
        background: rgba(255,255,255,0.9);
        border-radius: 4px;
        font-size: 12px;
        color: #1976d2; // 蓝色文字
        pointer-events: none;
        border: 1px solid rgba(25, 118, 210, 0.2);
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        font-weight: 500;
      }

      .click-hint {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        color: #546e7a;
        font-size: 14px;
        pointer-events: none;

        .el-icon {
          font-size: 32px;
          color: #90a4ae;
        }
      }

      .map-marker {
        position: absolute;
        transform: translate(-50%, -100%);
        z-index: 10;
        cursor: grab;
        transition: all 0.3s ease;

        &:active {
          cursor: grabbing;
        }

        .marker-pin {
          width: 36px;
          height: 36px;
          background: #1976d2; // 蓝色标记
          border-radius: 50% 50% 50% 0;
          transform: rotate(-45deg);
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 2px 8px rgba(25, 118, 210, 0.4);

          .el-icon {
            transform: rotate(45deg);
            color: white;
            font-size: 18px;
            margin-top: -2px;
          }
        }

        .marker-pulse {
          width: 60px;
          height: 60px;
          border: 3px solid rgba(25, 118, 210, 0.3);
          border-radius: 50%;
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          animation: pulse 2s infinite;
          pointer-events: none;
        }

        .marker-label {
          position: absolute;
          top: 40px;
          left: 50%;
          transform: translateX(-50%);
          white-space: nowrap;
          background: rgba(25, 118, 210, 0.9);
          color: white;
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 12px;
          opacity: 0;
          transition: opacity 0.3s;
          pointer-events: none;
        }

        &:hover .marker-label {
          opacity: 1;
        }
      }

      @keyframes pulse {
        0% { transform: translate(-50%, -50%) scale(0.5); opacity: 1; }
        100% { transform: translate(-50%, -50%) scale(1.2); opacity: 0; }
      }

      .zoom-controls {
        position: absolute;
        right: 12px;
        bottom: 80px;
        display: flex;
        flex-direction: column;
        gap: 2px;
        background: white;
        border-radius: 4px;
        box-shadow: 0 2px 6px rgba(0,0,0,0.1);

        .zoom-btn {
          width: 32px;
          height: 32px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          font-size: 18px;
          color: #606266;
          font-weight: bold;

          &:hover {
            background: #e3f2fd;
            color: #1976d2;
          }

          &:first-child {
            border-radius: 4px 4px 0 0;
            border-bottom: 1px solid #e8e8e8;
          }

          &:last-child {
            border-radius: 0 0 4px 4px;
          }
        }

        .zoom-level {
          width: 32px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          color: #1976d2;
          background: #e3f2fd;
          font-weight: 600;
        }
      }
    }

    .selected-location {
      padding: 4px;
      background: #e3f2fd; // 蓝色背景
      border-top: 1px solid #bbdefb;
      display: flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s;

      .loc-icon {
        color: #1976d2;
        font-size: 20px;
      }

      .loc-info {
        flex: 1;
        overflow: hidden;


        .loc-name {
          color: #1565c0;
          font-weight: 600;
          font-size: 14px;
          margin-bottom: 2px;
          line-height: 1.8;
        }

        .loc-detail {
          color: #424242;
          font-size: 13px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          margin-bottom: 0px;  /* 减小与经纬度的间距 */
          line-height: 1.0;
        }

        .loc-coord {
          color: #757575;
          font-size: 12px;
          margin-top: 2px;
          line-height: 1.8;
          display: flex;
          align-items: center;
          gap: 8px;

          .update-tag {
            background: #1976d2;
            color: white;
            padding: 0 6px;
            border-radius: 4px;
            font-size: 11px;
            animation: fadeInOut 2s ease;
          }
        }
      }
    }
  }

  @keyframes fadeInOut {
    0% { opacity: 0; transform: translateY(-10px); }
    20% { opacity: 1; transform: translateY(0); }
    80% { opacity: 1; transform: translateY(0); }
    100% { opacity: 0; transform: translateY(-10px); }
  }

  .search-panel {
    position: absolute;
    top: 50px;
    left: 12px;
    right: 12px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    max-height: 300px;
    overflow-y: auto;
    z-index: 100;

    .panel-header {
      padding: 12px;
      border-bottom: 1px solid #e3f2fd;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 500;
      color: #1976d2;

      .close-btn {
        cursor: pointer;
        color: #90a4ae;

        &:hover {
          color: #1976d2;
        }
      }
    }

    .search-item {
      padding: 12px;
      display: flex;
      align-items: flex-start;
      gap: 8px;
      cursor: pointer;
      border-bottom: 1px solid #f5f5f5;

      &:hover {
        background: #e3f2fd;
      }

      .el-icon {
        color: #1976d2;
        margin-top: 2px;
      }

      .item-info {
        flex: 1;

        .item-name {
          font-size: 14px;
          color: #1976d2;
          margin-bottom: 4px;
          font-weight: 500;
        }

        .item-addr {
          font-size: 12px;
          color: #616161;
          margin-bottom: 2px;
        }

        .item-dist {
          font-size: 12px;
          color: #9e9e9e;
        }
      }
    }
  }

  .input-hint {
    margin-top: 6px;
    font-size: 12px;
    color: #1976d2;
    display: flex;
    align-items: center;
    gap: 4px;

    .el-icon {
      font-size: 14px;
    }
  }

  .unit {
    margin-left: 8px;
    color: #909399;
  }

  .form-actions {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #e0e0e0;

    .el-button {
      min-width: 120px;
    }

    .el-button--primary {
      background: #1976d2;
      border-color: #1976d2;

      &:hover {
        background: #1565c0;
        border-color: #1565c0;
      }
    }
  }

  :global(.el-divider__text) {
    font-size: 14px;
    color: #424242;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 6px;
  }
}
</style>