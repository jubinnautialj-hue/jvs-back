<template>
  <div class="app-permission-page">
    <div v-if="dataLoading" class="loading-back"/>
    <div class="tips" style="margin-top: 0;">
      <div class="title">
        <span class="el-icon-info" style="color: #1E6FF;font-size: 14px;"></span>
        <span style="margin-left: 8px;">权限管理， 为该应用配置管理员，应用管理员可以对应用进行编辑、设置、应用数据的管理</span>
      </div>
      <div class="con-box">
        <div class="secTitle">
          <span>可以设置多个开发人员，同时进行开发和设计页面加快开发进度</span>
        </div>
        <ul class="tips-list">
          <li>
            <span class="dot"></span>
            <span>为保证设计的信息安全性,轻应用开发人员设计时产生的数据都为测试数据,不具有具体的业务功能</span>
          </li>
          <li>
            <span class="dot"></span>
            <span>轻应用的开发人员需要和使用人员分开</span>
          </li>
          <li>
            <span class="dot"></span>
            <span>清除所有测试数据时将自动把所有开发人员产生的数据全部删除，谨慎操作</span>
          </li>
        </ul>
      </div>
    </div>
    <div class="manager-permission">
      <div class="role"> 
        <div class="role-box">
          <div class="role-title">应用主管理员</div>
          <div class="role-con">
            <div class="role-con-left">
              <span class="label">角色描述</span>
            </div>
            <div class="role-con-right">
              <span class="desc">应用主管理员拥有应用的全部权限，可进行应用的搭建、编辑、发布、卸载、日志查看以及数据权限管理。</span>
            </div>
          </div>
          <div class="role-con member">
            <div class="role-con-left">
              <span class="label">权限成员</span>
            </div>
            <div class="role-con-right">
              <el-button class="add-btn" icon="el-icon-plus" type="text" @click="handleSetPermission('adminMember')">设置成员</el-button>
              <div class="user-list">
                <div class="user-list-item" v-for="(item, key) in appInfo.role.adminMember" :key="key">
                  <img :src="item.headImg" alt=""/>
                  <span>{{item.name}}</span>
                  <i class="el-icon-error" @click="deleteMember(key, appInfo.role.adminMember, 'adminMember')"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="modeUserInfo ? (['DEV', 'BETA'].indexOf(modeUserInfo.mode) > -1) : true" class="role-box">
          <div class="role-title">{{(modeUserInfo && modeUserInfo.mode == 'BETA') ? '测试' : '开发'}}成员</div>
          <div class="role-con">
            <div class="role-con-left">
              <span class="label">角色描述</span>
            </div>
            <div class="role-con-right">
              <span class="desc">{{(modeUserInfo && modeUserInfo.mode == 'BETA') ? '测试' : '开发'}}管理员拥有此应用的编辑以及数据权限管理。</span>
            </div>
          </div>
          <div class="role-con member">
            <div class="role-con-left">
              <span class="label">权限成员</span>
            </div>
            <div class="role-con-right">
              <el-button class="add-btn" icon="el-icon-plus" type="text" @click="handleSetPermission('devMember')">设置成员</el-button>
              <div class="user-list">
                <div class="user-list-item" v-for="(item, key) in appInfo.role.devMember" :key="key">
                  <img :src="item.headImg" alt=""/>
                  <span>{{item.name}}</span>
                  <i class="el-icon-error" @click="deleteMember(key, appInfo.role.devMember, 'devMember')"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <el-dialog
      class="template-dialog"
      title="设置成员"
      append-to-body
      :visible.sync="permissionVisible"
      :before-close="handlePermissionClose">
      <div v-if="permissionVisible">
        <div class="permission-content-item">
          <el-form label-width="80px">
            <el-button size="mini" type="primary" icon="el-icon-plus" @click="handleUserSelectOpen(setList)">选择人员</el-button>
            <div style="position: relative; padding: 10px 0">
              <el-tag
                v-for="tag in setList"
                size="small"
                style="margin-right: 4px;margin-bottom: 4px;"
                :key="tag.id"
                @close="handleDelUser(tag.id, setList)"
                closable>
                {{getTagName(tag)}}
              </el-tag>
            </div>
          </el-form>
        </div>
      </div>
      <div style="margin-top: 10px;text-align: center;">
        <jvs-button size="mini" type="primary" :disabled="(!(setList && setList.length > 0) && setType == 'adminMember')" @click="handlePermissionSubmit">确定</jvs-button>
        <jvs-button size="mini" @click="handlePermissionClose">取消</jvs-button>
      </div>
    </el-dialog>
    <userSelector
      ref="userSelector"
      :selectable="true"
      :userEnable="true"
      :roleEnable="false"
      @submit="addCheckUSer">
    </userSelector>
  </div>
</template>

<script>
import UserSelector from "@/components/basic-assembly/userSelector";
import { getSelectedUsers } from "@/api/common";
import {edit} from "@/components/template/api";

export default {
  name: "appPermission",
  components: {UserSelector},
  props: {
    appInfo: {
      type: Object
    },
    modeUserInfo: {
      type: Object
    }
  },
  data () {
    return {
      permissionVisible: false,
      dataLoading: false,
      userAll: [],
      setType: '',
      setList: []
    }
  },
  created() {
    this.getPermission()
  },
  methods: {
    getPermission() {
      this.dataLoading = true
      if(this.appInfo && this.appInfo.role) {
        if(!this.appInfo.role.adminMember) {
          this.appInfo.role.adminMember = []
        }
        if(!this.appInfo.role.devMember) {
          this.appInfo.role.devMember = []
        }
      }
      let userids = []
      this.appInfo.role.adminMember.filter(ait => {
        userids.push(ait.id)
      })
      this.appInfo.role.devMember.filter(dit => {
        if(userids.indexOf(dit.id) == -1) {
          userids.push(dit.id)
        }
      })
      if(userids.length > 0) {
        getSelectedUsers(userids).then(res => {
          if (res.data && res.data.code == 0 && res.data.data) {
            this.userAll = [...res.data.data]
            if(this.appInfo.role.adminMember && this.appInfo.role.adminMember.length > 0) {
              this.appInfo.role.adminMember.forEach(item => {
                this.userAll.forEach(it => {
                  if (item.id === it.id) {
                    item.headImg = it.headImg
                  }
                })
              })
            }
            if(this.appInfo.role.devMember && this.appInfo.role.devMember.length > 0) {
              this.appInfo.role.devMember.forEach(item => {
                this.userAll.forEach(it => {
                  if (item.id === it.id) {
                    item.headImg = it.headImg
                  }
                })
              })
            }
            this.dataLoading = false
          } else {
            this.dataLoading = false
          }
        }).catch(err => {
          this.dataLoading = false
        })
      }else{
        this.dataLoading = false
      }
    },
    // 设置权限弹窗打开
    handleSetPermission (type) {
      this.setType = type
      this.setList = JSON.parse(JSON.stringify(this.appInfo.role[type]))
      this.permissionVisible = true
    },
    // 添加人员 提交
    addCheckUSer(checkList) {
      this.setList = checkList
      this.$forceUpdate()
      this.$refs.userSelector.closeDialog()
    },
    // 权限数据提交
    handlePermissionSubmit () {
      let temp = JSON.parse(JSON.stringify(this.appInfo))
      temp.role[this.setType] = this.setList
      edit(temp).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '设置成功',
            position: 'bottom-right',
            type: 'success'
          })
          this.$emit('addUser', this.setType, this.setList)
          this.handlePermissionClose()
          this.getPermission()
        }
      })
    },
    // 获取tag名字
    getTagName(obj) {
      let header = ''
      switch (obj.type) {
        case 'user':
          break;
        case 'role':
          header = '（角色）';
          break;
        default: break;
      }
      return header + obj.name
    },
    handleDelUser(id, arr) {
      const index = arr.findIndex(item => {
        return item.id === id
      })
      arr.splice(index, 1)
      this.$forceUpdate()
    },
    // 打开人员选择组件
    handleUserSelectOpen(item) {
      // this.userIds = item.userIds
      this.$refs.userSelector.openDialog(item)
    },
    // 关闭权限设置弹窗
    handlePermissionClose() {
      this.permissionVisible = false
      this.setType = ''
      this.setList = []
    },
    deleteMember (index, list, type) {
      let tl = JSON.parse(JSON.stringify(list))
      tl.splice(index, 1)
      let temp = JSON.parse(JSON.stringify(this.appInfo))
      temp.role[type] = tl
      edit(temp).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: '提示',
            message: '删除成功',
            position: 'bottom-right',
            type: 'success'
          })
          list.splice(index, 1)
          this.$forceUpdate()
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.app-permission-page{
  position: relative;
  margin-top: 9px;
  height: 100%;
  box-sizing: border-box;
  .loading-back{
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    box-sizing: border-box;
    background-color: rgba(255, 255, 255, 0.8);
    background-image: url('../../../public/jvs-ui-public/img/loading.gif');
    background-repeat: no-repeat;
    background-position: center;
    background-position: center;
    //background-size: 200px 160px;
    z-index: 1;
  }
  .manager-permission{
    .role{
      .role-box{
        margin-top: 16px;
        height: 100%;
        border-radius: 4px;
        border: 1px solid #EEEFF0;
        .role-title{
          height: 40px;
          background: #F5F6F7;
          font-size: 16px;
          font-family: Microsoft YaHei, Microsoft YaHei;
          font-weight: 700;
          color: #363B4C;
          line-height: 40px;
          padding: 0 16px;
        }
        .role-con{
          border-bottom: 1px solid #e7e7e7;
          display: flex;
          height: 50px;
          box-sizing: border-box;
          .role-con-left{
            display: flex;
            align-items: center;
            padding-left: 16px;
            margin-right: 56px;
            .label{
              word-break: keep-all;
              font-size: 14px;
              font-family: Microsoft YaHei, Microsoft YaHei;
              font-weight: 400;
              color: #363B4C;
            }
          }
          .role-con-right{
            display: flex;
            .desc{
              font-size: 14px;
              font-family: Microsoft YaHei, Microsoft YaHei;
              font-weight: 400;
              color: #363B4C;
              line-height: 50px;
            }
            .add-btn{
              font-size: 14px;
              font-family: Microsoft YaHei, Microsoft YaHei;
              font-weight: 400;
              color: #1E6FFF;
              line-height: 18px;
            }
            .user-list{
              display: flex;
              flex-wrap: wrap;
              margin-left: 20px;
              padding-bottom: 16px;
              max-height: 200px;
              overflow: hidden;
              overflow-y: auto;
              .user-list-item{
                width: 114px;
                display: flex;
                align-items: center;
                padding: 2px 4px;
                height: 36px;
                background: #F5F6F7;
                border-radius: 4px;
                margin-top: 16px;
                margin-right: 16px;
                overflow: hidden;
                img {
                  width: 32px;
                  height: 32px;
                  margin-right: 4px;
                  border-radius: 4px;
                }
                span{
                  font-size: 14px;
                  font-family: Microsoft YaHei, Microsoft YaHei;
                  font-weight: 400;
                  color: #494F6A;
                  word-break: break-all;
                  display: block;
                  width: 60px;
                  overflow: hidden;
                  white-space: pre;
                  text-overflow: ellipsis;
                }
                i{
                  color: #6F7588;
                  font-size: 14px;
                  cursor: pointer;
                  margin-left: 7px;
                }
              }
            }
          }
        }
        .role-con.member{
          border: 0;
          min-height: 68px;
          height: auto;
        }
      }
    }
  }
}
</style>
