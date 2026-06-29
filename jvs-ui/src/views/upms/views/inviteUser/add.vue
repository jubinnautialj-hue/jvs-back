<template>
  <!-- 2022.11.29 加入组织改版 -->
  <el-dialog
    v-if="dialogVisible"
    append-to-body
    :title="$langt('common.rightDrawer.createOrg')"
    :visible.sync="dialogVisible"
    :before-close="handleClose"
  >
    <div class="content-input">
      <jvs-form ref="basicForm" :option="option" :formData="form" @submit="submitHandle"></jvs-form>
    </div>
  </el-dialog>
</template>
<script>

import {createTenant} from "@/views/upms/views/inviteUser/api";

export default {
  props: {
    dialogVisible: {
      type: Boolean,
      default: () => {
        return false
      }
    }
  },
  computed: {
  },
  data () {
    return {
      form: {},
      option: {
        cancal: false,
        emptyBtn: false,
        submitLoading: false,
        formAlign: 'top',
        column: [
          {
            label: '组织名称',
            prop: 'shortName',
            rules: [
              { required: true, message: '请输入组织名称', trigger: 'blur' },
            ]
          },
          {
            label: '公司全称',
            prop: 'name',
            rules: [
              { required: true, message: '请输入公司全称', trigger: 'blur' },
            ]
          },
          {
            label: '用户初始密码',
            prop: 'defaultPassword',
            rules: [
              { required: true, message: '请输入用户初始密码', trigger: 'blur' },
            ]
          },
        ]
      },
      userInfo: {},
      tenantInfo: {},
      invite: '',
    }
  },
  methods: {
    handleClose() {
      this.$emit('handleClose')
    },
    submitHandle (form) {
      createTenant(form).then(res => {
        if (res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip') ,
            message: this.$langt('messagePush.settingTypeOptions.created'),
            position: 'bottom-right',
            type: 'success'
          });
          this.handleClose()
        }
      })
    },
  },
  created () {
    this.option.column.filter(col => {
      if(col.label) {
        col.label = this.$langt(`tenant.column.${col.prop}.label`)
      }
      if(col.rules && col.rules.length > 0) {
        col.rules[0].message = this.$langt(`tenant.column.${col.prop}.placeholder`)
      }
    })
  }
}
</script>
<style lang="scss">
</style>
