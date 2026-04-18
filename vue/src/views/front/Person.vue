<template>
  <div class="front-container" style="width: 40%">
    <div class="card" style="padding: 20px; ">
      <div style="font-size: 20px; margin-bottom: 20px; text-align: center;">个人信息页面</div>

      <el-form ref="formRef" :model="data.user" :rules="data.rules" label-width="80px" style="padding-right: 30px">
        <el-form-item prop="avatar" label="头像" >
          <el-upload
              class="avatar-uploader"
              :action="baseUrl + '/files/upload' "
              :show-file-list="false"
              :on-success="handleFileUpload"
          >
            <img v-if="data.user.avatar" :src="data.user.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item prop="username" label="账号" >
          <el-input :disabled="data.user.id !== undefined" v-model="data.user.username" placeholder="请输入账号" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="name" label="姓名" >
          <el-input v-model="data.user.name" placeholder="请输入姓名" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="account" label="账户余额" >
          <div style="color: red; font-weight: bold">${{ data.user.account }}</div>
        </el-form-item>
        <div style="text-align: center">
          <el-button type="primary" size="large" @click="update">保 存</el-button>
        </div>

      </el-form>

    </div>
  </div>
</template>

<script setup >
import { reactive,ref} from "vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";

//定义一个事件发射器
const emit =defineEmits(['updateUser'])

const baseUrl = import.meta.env.VITE_BASE_URL
const formRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  rules:{
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },  //trigger是鼠标触发时机,blur是释交触发
    ]
  }
})

const loadUser = () => {
  request.get('/user/selectById/' + data.user.id).then(res => {
    data.user = res.data
    //存储最新的用户信息
    localStorage.setItem('system-user', JSON.stringify(res.data))
    emit('updateUser')  //发射信息，将存储后的信息直接发射更换
  })
}
loadUser()

const handleFileUpload = (res) => {
  data.user.avatar = res.data
}

/**  更新*/
const update = () => {
  request.put('/user/update',data.user).then(res =>{   //res是后端访问数据
    if(res.code === '200'){
      ElMessage.success('更新成功')
      loadUser()
    }else{
      ElMessage.error(res.msg)
    }
  })
}

</script>

<style scoped>
.avatar-uploader .avatar {
  width: 130px;
  height: 130px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 130px;
  height: 130px;
  text-align: center;
}
</style>
