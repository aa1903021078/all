import request from '@/utils/request'

export default {
    // 上传文件
    upload(file) {
        const formData = new FormData()
        formData.append('file', file)
        return request.post('/files/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })
    },

    // 获取下载地址
    getDownloadUrl(fileName) {
        return `http://localhost:9090/files/download/${fileName}`
    }
}