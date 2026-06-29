import md5 from 'js-md5'
import axios from "axios"
import { getCertificate } from '@/api/common'
/**
* 分片上传函数 支持多个文件
* @param options
* options.file 表示源文件
* options.pieceSize 表示需要分片的大小 默认是5m
* options.fileUrl 整个文件的上传地址
* bucketName 桶名
* progress 进度回调
* success 成功回调
* error 失败回调
*/
export const uploadByPieces = ({files, bucketName='jvs-form-design', module, pieceSize = 5, progress, success, error, businessId}) => {
  if (!files || !files.length) return
  // 上传过程中用到的变量
  let fileList = [] // 总文件列表
  let progressNum = 0 // 进度
  let successAllCount = 0 // 上传成功的片数
  // let currentAllChunk = 0 // 当前上传的片数索引
  let AllChunk = 0 // 所有文件的chunk数之和
  let AllFileSize = 0 // 所有文件size


  // 获取md5
  const readFileMD5 = (files) => {
    // 读取每个文件的md5
    files.map((file, index) => {
      if(file.size < 1024*1024*500){
        let fileRederInstance = new FileReader()
        fileRederInstance.readAsBinaryString(file)
        fileRederInstance.addEventListener('load', e => {
          let fileBolb = e.target.result
          let fileMD5 = md5(fileBolb)
          if (!fileList.some((arr) => arr.md5 === fileMD5)) {
            fileList.push({md5: fileMD5, name: file.name, file})
            AllFileSize = AllFileSize + file.size
          }
          if (index === files.length - 1) readChunkMD5(fileList)
        }, false)
      }else{
        fileList.push({name: file.name, file})
        AllFileSize = AllFileSize + file.size
        if (index === files.length - 1) readChunkMD5(fileList)
      }
    })
  }
  const getChunkInfo = (file, currentChunk, chunkSize) => {
    let start = currentChunk * chunkSize
    let end = Math.min(file.size, start + chunkSize)
    let chunk = file.slice(start, end)
    return { start, end, chunk }
  }
  // 针对每个文件进行chunk处理
  const readChunkMD5 = (fileList) => {
    fileList.map((currentFile, fileIndex) => {
      const chunkSize = pieceSize * 1024 * 1024 // 5MB一片
      const chunkCount = Math.ceil(currentFile.file.size / chunkSize) // 总片数

      const certificateData = {
        bucketName,
        filename: currentFile.name,
        num: chunkCount,
        module: module ? module : '/jvs-ui/form/'
      }
      getCertificate(certificateData).then(async res => {
        if(res.data.code === 0) {
          AllChunk = AllChunk + chunkCount // 计算全局chunk数
          // 针对单个文件进行chunk上传
          const resData = res.data.data
          for (let i = 0; i < chunkCount; i++) {
            const { chunk } = getChunkInfo(currentFile.file, i, chunkSize)
            await uploadChunk(currentFile, {chunk, currentChunk: i, chunkCount}, fileIndex, resData, businessId)
          }
        }
      }).catch(e => {
        error && error(e)
      })
    })
  }
  // 更新进度
  const progressFun = (currentFile) => {
    progressNum = Math.ceil(successAllCount / AllChunk * 100)
    // progress(progressNum, currentFile)
  }
  // 上传每一片
  const uploadChunk = (currentFile, chunkInfo, fileIndex, { bucketName, uploadId, partNumber, fileName }, businessId) => {
    currentFile.fileName = fileName
    return new Promise((resolve, reject) => {
      let fetchForm = new FormData()
      fetchForm.append('file', chunkInfo.chunk)
      fetchForm.append('module', module ? module : '/jvs-ui/form/')
      fetchForm.append('fileName', fileName)
      const chunkUrl = `/mgr/jvs-auth/uploadPart/${bucketName}/${uploadId}/${chunkInfo.currentChunk + 1}`
      let header = {
        serialize:false,
        'type':'FormData',
        'Content-Type': 'application/x-www-form-urlencoded'
      }
      if(businessId) {
        header.businessId = businessId
      }
      axios({
        url:chunkUrl,
        method: 'post',
        headers: header,
        data: fetchForm,
        onUploadProgress(progressEvent){
          let val= parseInt(progressEvent.loaded / progressEvent.total * 100);
          progress(parseInt(successAllCount*(100/AllChunk) + parseInt((100/AllChunk/100)*val)),currentFile);
        }
      }).then(res => {
        resolve(res)
        progressFun(currentFile)
        if (chunkInfo.currentChunk <= chunkInfo.chunkCount - 1) {
          successAllCount++
          if(chunkInfo.currentChunk + 1 === chunkInfo.chunkCount) {
            const successData = {
              filePath:res?.data?.data,
              bucketName,
              fileName,
              name: currentFile.name,
              size: AllFileSize,
            }
            success && success(successData)
          }
        }
      }).catch((e) => {
        console.log(e)
        // error && error(e)
        reject(e)
      })
    })
  }
  readFileMD5(files) // 开始执行代码
}
