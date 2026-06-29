<template>
  <jvs-form ref="listenForm" :option="formOption" :formData="infoData"></jvs-form>
</template>

<script>
import {isNumber} from "@/util/validate";

export default {
  name: "listenConfig",
  props:{
    formData: {
      type: Object,
      default() {
        return {}
      }
    }
  },
  computed: {
    infoData() {
      return this.formData.mqttDto ? this.formData.mqttDto : {
        serverURI: 'tcp://broker.emqx.io:1883',
        topic: '/test/#',
        qos: 0,
      }
    }
  },
  created() {
  },
  data() {
    let validateNumberReg = (rule, value, callback) => {
      if(isNumber(value)) {
        callback();
      }else{
        callback(new Error('请输入数字'));
      }
    };
    return {
      formOption: {
        labelWidth: '90px',
        submitBtn: false,
        emptyBtn: false,
        cancal: false,
        btnHide: true,
        formAlign: 'top',
        column: [
          {
            label: '服务地址',
            prop: 'serverURI',
            rules: [
              { required: true, message: '请输入服务地址', trigger: 'blur' }
            ],
          },
          {
            label: '账号',
            prop: 'userName',
            // rules: [
            //   { required: true, message: '请输入账号', trigger: 'blur' }
            // ],
          },
          {
            label: '密码',
            prop: 'password',
            showpassword: true,
            // rules: [
            //   { required: true, message: '请输入密码', trigger: 'blur' }
            // ],
          },
          {
            label: 'topic',
            prop: 'topic',
            rules: [
              { required: true, message: '请输入topic', trigger: 'blur' }
            ],
          },
          {
            label: 'qos',
            prop: 'qos',
            rules: [
              { required: true, message: '请输入qos', trigger: 'blur' },
              { validator: validateNumberReg, trigger: 'change'}
            ],
          },
        ],
      }
    }
  },
  methods: {
    getFormData() {
      let listenConfig = {}
      this.$refs.listenForm.$refs.ruleForm.validate((valid) => {
        if (valid) {
          listenConfig = JSON.parse(JSON.stringify(this.infoData))
        } else {
          listenConfig = null
          return false
        }
      })
      return listenConfig
    }
  }
}
</script>
