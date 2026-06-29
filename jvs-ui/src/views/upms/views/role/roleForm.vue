<!-- 权限弹窗 -->
<template>
  <div class=''>
    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      :before-close="handleClose"
      :close-on-click-modal='false'
      :close-on-press-escape='true'
    >
      <div class="text-tips-list">
        <div class="tips">
          <div class="title">
            <span class="el-icon-info" style="color: #376DFF;"></span>
            <span style="margin-left: 10px;">{{$langt('common.tips')}}</span>
          </div>
          <ul class="tips-list">
            <li>
              <div>
                <i class="dot"></i>
                <span>{{$langt('role.roleDesc1')}}</span>
              </div>
            </li>
            <li>
              <div>
                <i class="dot"></i>
                <span>{{$langt('role.roleDesc2')}}</span>
              </div>
            </li>
          </ul>
        </div>
      </div>
      <el-form
        ref="form"
        :model="form"
        label-width="80px"
        label-position="top"
        :rules="rules"
      >
        <el-form-item
          :label="$langt('role.column.roleGroupId.label')"
          prop="roleGroupId"
        >
          <el-select v-model="form.roleGroupId" :placeholder="$langt('role.column.roleGroupId.placeholder')" size="mini" filterable clearable style="width: 100%;">
            <el-option v-for="group in roleGroupArray" :key="group.id+'-group-option'" :label="group.name" :value="group.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$langt('role.column.roleName.label')"
          prop="roleName"
        >
          <el-input
            v-model="form.roleName"
            :placeholder="$langt('role.column.roleName.placeholder')"
            @blur="noRepeatName(form.roleName)"
            size="mini"
          ></el-input>
          <span class="el-form-item__error" v-if="disSubmit">{{$langt('role.column.roleName.error')}}</span>
        </el-form-item>
        <el-form-item
          :label="$langt('role.column.roleDesc.label')"
          prop="roleDesc"
        >
          <el-input
            :placeholder="$langt('role.column.roleDesc.placeholder')"
            type="textarea"
            v-model="form.roleDesc"
            size="mini"
          ></el-input>
        </el-form-item>
        <el-form-item
          :label="$langt('role.column.memberScope.label')"
          prop="memberScope"
        >
          <el-radio-group v-model="form.memberScope">
            <el-radio label="USER">{{$langt('role.column.memberScope.dicData.USER')}}</el-radio>
            <el-radio label="DEPT">{{$langt('role.column.memberScope.dicData.DEPT')}}</el-radio>
            <el-radio label="ALL">{{$langt('role.column.memberScope.dicData.ALL')}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="form.memberScope == 'USER'"
          :label="$langt('role.column.autoGrant.label')"
          prop="autoGrant"
        >
          <el-switch v-model="form.autoGrant"></el-switch>
        </el-form-item>
      </el-form>
      <span
        slot="footer"
        class="dialog-footer"
        style="display:flex;justify-content: flex-end;"
      >
        <jvs-button
          size="mini"
          type="primary"
          :loading="submitLoading"
          @click="doSubmit"
          :disabled="disSubmit"
        >{{$langt('common.confirm')}}</jvs-button>
        <jvs-button
          size="mini"
          @click="handleClose"
        >{{$langt('common.cancel')}}</jvs-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// 这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）
// 例如：import 《组件名称》 from '《组件路径》';
import { addRole, editRole, getRoleGroupArray } from './api'
export default {
  // import引入的组件需要注入到对象中才能使用
  components: {},
  props: {
    list: {
      type: Array
    }
  },
  data () {
    // 这里存放数据
    return {
      name:'',
      dialogVisible: false,
      title: this.$langt('role.addRole'),
      method: '',
      form: {
        roleName: '',
        roleDesc: '',
        memberScope: 'USER'
      },
      rules: {
        roleName: [{ required: true, message: this.$langt('role.column.roleName.rule') }],
        roleDesc: [{ required: true, message: this.$langt('role.column.roleDesc.rule')  }],
        memberScope: [{ required: true, message: this.$langt('role.column.memberScope.rule')  }]
      },
      disSubmit: false,
      submitLoading: false,
      roleGroupArray: []
    }
  },
  // 监听属性 类似于data概念
  computed: {},
  // 监控data中的数据变化
  watch: {},
  // 方法集合
  methods: {
    handleClose () {
      if (this.$refs.form) {
        this.$refs.form.resetFields();
        this.form = {
          roleName: '',
          roleDesc: ''
        }
      }
      this.dialogVisible = false
    },
    async init (method, row) {
      await getRoleGroupArray().then(res => {
        if(res.data && res.data.code == 0) {
          console.log(res.data.data)
          this.roleGroupArray = res.data.data
        }
      })
      this.method = method
      if (method === 'add') {
        this.title = this.$langt('role.roleAdd')
        this.form = {
          roleName: '',
          roleDesc: ''
        }
      } else if (method === 'edit') {
        this.title = this.$langt('role.roleEdit')
        this.name=row.roleName
        this.form = JSON.parse(JSON.stringify(row))
      }
      this.dialogVisible = true
    },
    doSubmit () {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.submitLoading = true
          const params = JSON.parse(JSON.stringify(this.form))
          if (this.method === 'add') {
            params.type = 'userRole'
            addRole(params).then(({ data }) => {
              if (data.code === 0) {
                this.submitLoading = false
                this.$notify({
                  title: this.$langt('common.tip') ,
                  message: this.$langt('role.roleAddSuccess'),
                  position: 'bottom-right',
                  type: 'success'
                });
                this.handleClose()
                this.$emit('reFresh')
              }
            }).catch(e => {
              this.submitLoading = false
            })
          } else {
            params.type = 'userRole'
            editRole(params).then(({ data }) => {
              if (data.code === 0) {
                this.submitLoading = false
                this.$notify({
                  title: this.$langt('common.tip') ,
                  message: this.$langt('role.roleEditSuccess'),
                  position: 'bottom-right',
                  type: 'success'
                });
                this.handleClose()
                this.$emit('reFresh')
              }
            }).catch(e => {
              this.submitLoading = false
            })
          }
        } else {
          return false
        }
      })
    },
    // 名称不可重复
    noRepeatName (name) {
      for(let i in this.list) {
        if(this.list[i].roleName == name && this.list[i].roleName!=this.name) {
          this.disSubmit = true
          return false
        }
      }
      this.disSubmit = false
    }
  }
}
</script>
<style lang="scss" scoped>
.text-tips-list{
  .tips{
    padding: 15px 20px;
    background-color: #E2EAFF;
    border-radius: 5px;
    overflow: hidden;
    .title{
      font-size: 16px;
      color: #212A33;
      font-weight: 550;
    }
    .secTitle{
      color: #20252B;
      margin-top: 10px;
      font-size: 15px;
      text-indent: 26px;
    }
    .tips-list{
      margin: 0;
      padding: 0;
      list-style: none;
      margin-top: 5px;
      padding-left: 26px;
      li{
        margin: 0;
        padding: 0;
        color: #393F4D;
        list-style: none;
        font-size: 14px;
        display: flex;
        align-items: center;
        display: flex;
        margin-top: 10px;
        .dot{
          display: inline-block;
          width: 6px;
          height: 6px;
          border-radius: 6px;
          background-color: #3471FE;
          line-height: 0;
          margin-right: 10px;
        }        
        span{
          line-height: 20px;
          text-indent: 10px;
        }
      }
    }
  }
}
</style>
