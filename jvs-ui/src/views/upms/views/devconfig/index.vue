<template>
  <div class="dev-config-page">
    <div class="dev-config">
      <div v-for="item in configList" :key="item.key" class="dev-config-item">
        <div class="top">
          <div>
            <svg class="icon" aria-hidden="true">
              <use xlink:href="#icon-jvs-rongqi"></use>
            </svg>
            <span class="title">{{item.title}}</span>
          </div>
          <div>
            <div v-if="item.key == 'mybatis'" class="fresh" @click="freshHandle">
              <i class="el-icon-refresh"></i><span>{{$langt('devconfig.buttonText')}}</span>
            </div>
            <el-switch v-else v-model="statusObj[item.prop]" @change="statusChange(item.prop)"></el-switch>
          </div>
        </div>
        <div class="desc">{{item.desc}}</div>
      </div>
    </div>
  </div>
</template>
<script>
import { refreshCache, getOtherInfo, saveOtherInfo } from './api'
export default {
  name: 'dev-config-page',
  data () {
    return {
      configList: [
        {
          key: 'createHeader',
          prop: 'AUTO_CREATE_USER_HEAD_IMG',
          title: '创建用户是否生成头像',
          desc: '当新用户创建时，系统可以自动为用户生成一个默认的头像，也可以允许用户自定义上传头像。此功能为用户提供了一个个性化的界面体验，同时也有助于提升用户识别度。'
        },
        // {
        //   key: 'isDev',
        //   title: '是否是开发模式',
        //   desc: '开发模式是一种特殊的运行模式，主要用于系统的开发和调试。在开发模式下，系统会提供完善的日志输出和错误提示，以帮助开发人员快速定位和解决问题。同时，开发模式可能会关闭某些性能优化和缓存机制。'
        // },
        {
          key: 'ding',
          prop: 'ERROR_MESSAGE_SEND_DING',
          title: '配置钉钉的异常消息通知',
          desc: '当系统发生异常或错误时，通过钉钉发送消息通知是一种及时、有效的提醒方式。配置钉钉的异常消息通知涉及设置钉钉机器人的Webhook地址、密钥等，以便系统能够将异常信息发送到指定的钉钉群或用户。这有助于运维人员快速响应和处理系统问题，提高系统的稳定性和可用性。'
        },
        {
          key: 'mybatis',
          title: '启用数据查询缓存优化（mybatis plus 二级缓存）',
          desc: '为了提高数据查询的性能和响应速度，系统可以配置缓存机制来缓存查询结果。mybatis plus 是一个流行的MyBatis增强工具，它支持二级缓存功能。启用并配置二级缓存后，这可以大大减少数据库访问次数，提高系统的吞吐量和响应速度。只在多台服务时有明显效果。'
        }
      ],
      getResult: {},
      statusObj: {}
    }
  },
  created () {
    this.configList.filter(col => {
      col.title = this.$langt(`devconfig.column.${col.key}.title`)
      col.desc = this.$langt(`devconfig.column.${col.key}.desc`)
    })
    this.getOtherInfoHandle()
  },
  methods: {
    freshHandle () {
      refreshCache().then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('devconfig.opsuccess'),
            position: 'bottom-right',
            type: 'success'
          });
        }
      })
    },
    getOtherInfoHandle () {
      getOtherInfo().then(res => {
        if(res.data && res.data.code == 0 && res.data.data) {
          this.getResult = res.data.data
          for(let i in res.data.data) {
            if(res.data.data[i].content) {
              if(res.data.data[i].content.startsWith('{')) {
                this.$set(this.statusObj, i, JSON.parse(res.data.data[i].content).enable)
              }
            }
          }
        }
      })
    },
    statusChange (prop) {
      let obj = {type: prop, content: JSON.stringify({enable: this.statusObj[prop]})}
      if(this.getResult[prop] && this.getResult[prop].id) {
        obj.id = this.getResult[prop].id
      }
      saveOtherInfo(obj).then(res => {
        if(res.data && res.data.code == 0) {
          this.$notify({
            title: this.$langt('common.tip'),
            message: this.$langt('common.setSuccess'),
            position: 'bottom-right',
            type: 'success'
          })
          this.getOtherInfoHandle()
        }else{
          this.$set(this.statusObj, prop, !this.statusObj[prop])
        }
      }).catch(e => {
        this.$set(this.statusObj, prop, !this.statusObj[prop])
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.dev-config-page{
  height: 100%;
  overflow-y: auto;
  background-color: #fff;
  .dev-config{
    padding: 4px;
    display: flex;
    flex-wrap: wrap;
    .dev-config-item{
      width: calc(50% - 8px);
      padding: 18px;
      margin-bottom: 16px;
      border-radius: 6px;
      border: 1px solid #EEEFF0;
      box-sizing: border-box;
      .top{
        display: flex;
        align-items: center;
        justify-content: space-between;
        .icon{
          width: 16px;
          height: 16px;
          margin-right: 8px;
        }
        .title{
          height: 21px;
          font-family: Microsoft YaHei-Regular, Microsoft YaHei;
          font-weight: 400;
          font-size: 16px;
          color: #363B4C;
          line-height: 21px;
        }
        .fresh{
          cursor: pointer;
          i{
            font-size: 16px;
            margin-right: 5px;
          }
          span{
            font-size: 14px;
          }
          &:hover{
            color: #1E6FFF;
          }
        }
      }
      .desc{
        margin-top: 17px;
        font-family: Microsoft YaHei-Regular, Microsoft YaHei;
        font-weight: 400;
        font-size: 14px;
        color: #6F7588;
        line-height: 18px;
      }
    }
    .dev-config-item:nth-of-type(2n) {
      margin-left: 16px;
    }
  }
}
</style>