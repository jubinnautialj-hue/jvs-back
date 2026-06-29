<template>
    <div>
        <h1>{{userName}}</h1>
        <div>{{catalogue }}</div>
    </div>
</template>

<script>
import { ruleRunApi } from "@/api/common";
import { getIdentificationMappings } from '@/api/common'
import { getPageDataList } from '../api/index'
export default {
    data() {
        return {
            userName: '',
            catalogue: {},
            identificationMap: null, // 标识映射
            permisionList: [], // 权限标识集合
        }
    },
    async created() {
        this.testHandle()
        let list = ['code_demo_app', 'code_demo_catalogue'] // 源代码开发标识集合
        await getIdentificationMappings(list).then(res => {
            if (res.data && res.data.code == 0) {
                this.identificationMap = res.data.data
            }
        })
        this.getInfoData()

    },
    methods: {
        testHandle() {
            //标识获取请在应用中心标识管理添加获取
            ruleRunApi('code_demo_app', '/thisuser').then(res => {
                console.log(res.data)
                if (res.data && res.data.code == 0 && res.data.data) {
                    this.userName = res.data.data
                    this.$forceUpdate()
                }
            })
        },
        getInfoData() {
            let jvsAppId = this.identificationMap['code_demo_app'] //根据应用标识获取id
            let dataModelId = this.identificationMap['code_demo_catalogue'] //根据模型标识获取id
            let dataId = '922423572876398592'
            console.log(jvsAppId)
            console.log(dataModelId)
            if (jvsAppId && dataModelId) {
                //获取目录模型中数据第一条的详细信息
                getPageDataList(jvsAppId, dataModelId).then(res => {
                    if (res.data && res.data.code == 0 && res.data.data && res.data.data.length > 0) {
                        this.catalogue = res.data.data[0]
                    }
                })
            }
        }
    }
}
</script>