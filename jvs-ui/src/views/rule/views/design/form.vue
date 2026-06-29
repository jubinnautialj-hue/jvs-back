<template>
  <div class="task-page">
    <div class="title">按照预定的时间计划执行的任务。它可以周期性地执行，如每天、每周、每月等，也可以在特定的时间点执行，如每天的固定时间、每小时的固定分钟等！</div>
    <el-form
      ref="form"
      size="small"
      :rules="rules"
      :model="form"
      label-position="top"
      label-width="120px"
    >
      <el-form-item label="定时开关" prop="onTask">
        <el-switch v-model="form.onTask"></el-switch>
      </el-form-item>
      <!-- <el-form-item
        label="负责人"
        v-if="form.onTask === true"
        prop="author"
        :rules="[
          {
            required: true,
            message: '定时任务负责人不能为空',
            trigger: 'blur',
          },
        ]"
      >
        <el-input v-model="form.author"></el-input>
      </el-form-item>
      <el-form-item
        label="负责人邮箱"
        v-if="form.onTask === true"
        prop="alarmEmail"
        :rules="[
          {
            required: true,
            message: '负责人邮箱不能为空',
            trigger: 'blur',
          },
        ]"
      >
        <el-input v-model="form.alarmEmail"></el-input>
      </el-form-item> -->
      <el-form-item
        label="开始时间"
        v-if="form.onTask === true"
        prop="startTime"
        :rules="[
          { required: true, message: '开始时间不能为空', trigger: 'blur' },
        ]"
      >
        <el-date-picker
          prefix-icon="el-icon-date"
          :clearable="false"
          value-format="yyyy-MM-dd HH:mm:ss"
          style="width: 100%"
          v-model="form.startTime"
          type="datetime"
          placeholder="选择日期时间"
        >
        </el-date-picker>
      </el-form-item>
      <el-form-item
        label="重复规则"
        v-if="form.onTask === true"
        prop="cron"
        :rules="[
          {
            required: false,
            message: '请选择重复规则',
            trigger: 'change',
          },
        ]"
      >
        <el-select v-model="cronSelect" placeholder="请选择" style="width:100%;" @change="setCronValue">
          <el-option
            v-for="(item, index) in timeList"
            :key="item.cron + '-' + index +'-repeat-item'"
            :label="item.name"
            :value="item.cron">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item
        label="定时设置"
        v-if="form.onTask === true"
        prop="cron"
        :rules="[
          { required: true, message: '表达式不能为空', trigger: 'blur' },
        ]"
      >
        <el-input v-model="form.cron"></el-input>
        <!-- 可选择自动生成，可 -->
        <span class="tip-text">直接输入cron表达式</span>
        <!-- ，在线生成参考https:www.pppet.net/ -->
      </el-form-item>

      <el-form-item v-if="false && form.onTask === true">
        <Crdss @setCron="setcron" />
      </el-form-item>
      <el-form-item label="最近5次运行时间" v-if="false && form.onTask === true">
        <p class="time-list-item" v-for="(item, index) in timeList" :key="item+'timeList'+index">{{item}}</p>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
import Crdss from '../../components/cron'
import {getCronList} from '../../api/rule.js'
export default {
  name: 'set-form',
  props: {
    formData: {
      type: Object
    }
  },
  components: {
    Crdss
  },
  data () {
    return {
      form: {
        // externalEnable: false
      },
      rules: {
        name: [
          { required: true, message: '逻辑名称不能为空', trigger: 'blur' }
        ]
      },
      timeList: [],
      cronSelect: ''
    }
  },
  created(){
    this.init(this.formData)
  },
  methods: {
    init (formData) {
      if(formData.task) {
        this.form = JSON.parse(JSON.stringify(formData.task))
      }
      if(formData.name) {
        this.$set(this.form, 'name', formData.name)
      }
      if(formData.content) {
        this.$set(this.form, 'content', formData.content)
      }
      this.$set(this.form, 'onTask', formData.onTask || false)
      this.setcron(this.form.cron)
      // if(!this.form.externalEnable) {
      //   this.$set(this.form, 'externalEnable', false)
      // }
    },
    setcron (data) {
      if(this.form.onTask) {
        if(data) {
          this.$set(this.form, 'cron', data)
        }else{
          this.$set(this.form, 'cron', '')
        }
      }
      getCronList().then(res => {
        if(res.data.code == 0) {
          let isCustom = true
          res.data.data.filter(item => {
            if(!item.cron) {
              item.cron = 'null'
            }
            if(data && item.cron == data) {
              isCustom = false
            }
          })
          this.timeList = res.data.data
          if(isCustom) {
            this.cronSelect = 'Custom'
          }
        }
      })
    },
    sub () {
      this.query.current=1
      this.getData()
    },
    // 关闭
    handleClose () {
      this.$refs['form'].resetFields();
      this.$emit('close', true)
    },
    // 提交
    onSubmit (name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          this.loading=true
          let temp = JSON.parse(JSON.stringify(this.formData))
          temp.task = JSON.parse(JSON.stringify(this.form))
          temp.name = this.form.name
          temp.content = this.form.content
          temp.onTask = this.form.onTask
          delete temp.task.name
          delete temp.task.content
          // console.log(temp)
          this.$emit('save', temp)
          // saveOrUpdateDesign(temp).then(res => {
          //   this.dialogVisible=false
          //   this.loading=false
          //   this.handleClose()
          //   this.$message.success('设置成功')
          // })
        } else {
          return false
        }
      })
    },
    getSetFormData() {
      let bool = false
      let task = {}
      this.$refs['form'].validate((valid) => {
        if (valid) {
          bool = true
          task = JSON.parse(JSON.stringify(this.form))
        } else {
          return false
        }
      })
      return bool ? task : bool
    },
    setCronValue () {
      if(this.cronSelect != 'null') {
        if(this.cronSelect == 'Custom') {
          this.$set(this.form, 'cron', '')
        }else{
          this.$set(this.form, 'cron', this.cronSelect)
          this.$refs['form'].clearValidate('cron')
        }
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.task-page{
  padding: 0 8px;
  .title {
    font-family: Source Han Sans-Regular, Source Han Sans;
    font-weight: 400;
    font-size: 12px;
    color: #6F7588;
    line-height: 17px;
  }
  /deep/.el-form{
    .el-form-item{
      margin-top: 16px;
      margin-bottom: 0;
      &.is-error{
        margin-bottom: 18px;
      }
      .el-form-item__label{
        padding-bottom: 8px;
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
        line-height: 20px;
      }
      .el-form-item__content{
        line-height: unset;
        .el-input__inner{
          height: 36px;
          background: #F5F6F7;
          border-radius: 4px;
          border: 0;
        }
        .el-input__suffix-inner i{
          color: #6F7588;
          font-weight: bold;
        }
        .el-date-editor{
          .el-input__inner{
            padding-left: 15px;
          }
          .el-input__prefix{
            width: 25px;
            right: 5px;
            left: unset;
            .el-input__icon{
              color: #6F7588;
              font-weight: bold;
            }
          }
        }
        .tip-text{
          display: block;
          margin-top: 8px;
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          font-size: 12px;
          color: #6F7588;
          line-height: 17px;
        }
      }
    }
  } 
}
.time-list-item{
  margin: 0;
}
</style>
