<script>
import { copyBranchNode } from '@/views/flowable/api/flowable'
import { getDefaultNodeProps, logicType, nodeButtonList } from '../common/enumConst'
//导入所有节点组件
import Approval from '../common/nodes/ApprovalNode.vue'
import Cc from '../common/nodes/CcNode.vue'
import Concurrent from '../common/nodes/ConcurrentNode.vue'
import Condition from '../common/nodes/ConditionNode.vue'
import Trigger from '../common/nodes/TriggerNode.vue'
import Delay from '../common/nodes/DelayNode.vue'
import Empty from '../common/nodes/EmptyNode.vue'
import Root from '../common/nodes/RootNode.vue'
import Node from '../common/nodes/Node.vue'
import Automation from '../common/nodes/AutomationNode.vue'
import Message from '../common/nodes/MessageNode.vue'

export default {
  name: "ProcessTree",
  components: {Node, Root, Approval, Cc, Trigger, Concurrent, Condition, Delay, Empty, Automation, Message},
  props: {
    process: {
      type: Object
    },
    disabled: Boolean,
    jvsAppId: {
      type: String
    }
  },
  data() {
    return {
      valid: true
    }
  },
  computed:{
    nodeMap(){
      return this.$store.state.flow.parentMap;
    },
    dom(){
      return this.process ? this.process : this.$store.state.flow.template.process;
    }
  },
  render(h, ctx) {
    // console.log("渲染流程树")
    // console.log(this.dom)
    this.nodeMap.clear()
    let processTrees = this.getDomTree(h, this.dom)
    //插入末端节点
    processTrees.push(h('div', {style:{'display': 'flex', 'align-items': 'center', 'justify-content': 'center'}}, [
      h('div', {class:{'process-end': true}, domProps: {innerHTML: `
        <div class="svg-icon" style="width: 24px;height: 24px;margin-right: 8px;background: rgba(30,111,255,0.12);border-radius: 4px;display: flex;align-items: center;justify-content: center;">
          <svg style="width: 16px;height: 16px;">
            <use xlink:href="#icon-jvs-liuchengjieshu"></use>
          </svg>
        </div>
        <div>流程结束</div>
      `}})
    ]))
    return h('div', {class:{'_root': true, 'disabled': this.disabled}, ref:'_root'}, processTrees)
  },
  methods: {
    getDomTree(h, node, spacialClass) {
      this.toMapping(node);
      if (this.isPrimaryNode(node)){
        //普通业务节点
        let childDoms = this.getDomTree(h, node.node)
        this.decodeAppendDom(h, node, childDoms)
        return [h('div', {'class':{'primary-node': true}}, childDoms)];
      }else if (this.isBranchNode(node)){
        let index = 0;
        let tpList = []
        if(node.type == 'PARALLEL') {
          tpList = node.parallels
        }else{
          tpList = node.conditions
        }
        let centerIndex = -1
        if(tpList.length % 2 == 1) {
          centerIndex = Math.ceil(tpList.length / 2)
        }
        //遍历分支节点，包含并行及条件节点
        let branchItems = tpList.map(branchNode => {
          //处理每个分支内子节点
          if(!branchNode.type) {
            branchNode.type = node.type == 'PARALLEL' ? 'PB' : 'TJ'
          }
          this.toMapping(branchNode);
          let childDoms = this.getDomTree(h, branchNode.node)
          // 条件节点边线
          childDoms.push(h('div', {'class': {'line-top-left': true}}, [h('div', {'class': 'before-line'}), h('div', {'class': 'after-line'})]))
          childDoms.push(h('div', {'class': {'line-top-right': true}}, [h('div', {'class': 'before-line'}), h('div', {'class': 'after-line'})]))
          childDoms.push(h('div', {'class': {'line-bot-left': true}}, [h('div', {'class': 'before-line'}), h('div', {'class': 'after-line'})]))
          childDoms.push(h('div', {'class': {'line-bot-right': true}}, [h('div', {'class': 'before-line'}), h('div', {'class': 'after-line'})]))
          this.decodeAppendDom(h, branchNode, childDoms, {level: index + 1, size: tpList.length})
          //遍历子分支尾部分支
          index++;
          return h('div', {'class':{
            'branch-node-item': true,
            'start-item': index == 1, // 第一个分支
            'end-item': index == tpList.length, // 最后一个分支
            'center-item': index == centerIndex, // 中间的分支
            'has-children-item': (branchNode.node && branchNode.node.type), // 存在后续节点的分支
          }}, childDoms);
        })
        //插入添加分支/条件的按钮
        branchItems.unshift(h('div',{'class':{'add-branch-btn': true, 'condition' : this.isConditionNode(node)}}, [
          h('div', {
           'class':{'add-branch-btn-div': true},
            on:{click: () => this.addBranchNode(node)},
            domProps: {innerHTML: this.isConditionNode(node) ? `
              <div style="position: absolute;left: calc(50% - 4px);top: -6px;width: 0;height: 0;border-left: 4px solid transparent;border-right: 4px solid transparent;border-top: 6px solid #C2C5CF;"></div>
              <svg aria-hidden="true" style="width: 16px;height: 16px;margin-right: 4px;">
                <use xlink:href="#icon-jvs-tiaojianfenzhi"></use>
              </svg>
              <span style="font-family: Microsoft YaHei-Regular, Microsoft YaHei;font-weight: 400;font-size: 14px;color: #FFFFFF;">添加条件</span>
            ` : `
              <div style="position: absolute;left: calc(50% - 4px);top: -6px;width: 0;height: 0;border-left: 4px solid transparent;border-right: 4px solid transparent;border-top: 6px solid #C2C5CF;"></div>
              <svg aria-hidden="true" style="width: 16px;height: 16px;margin-right: 4px;">
                <use xlink:href="#icon-jvs-binghangfenzhi"></use>
              </svg>
              <span style="font-family: Microsoft YaHei-Regular, Microsoft YaHei;font-weight: 400;font-size: 14px;color: #FFFFFF;">并行分支</span>
            `},
          }, [])
        ]));
        let bchDom = [h('div', {'class':{'branch-node': true, 'has-center': centerIndex > -1, 'only-two-branch': tpList.length == 2}}, branchItems)]
        //继续遍历分支后的节点
        let afterChildDoms = this.getDomTree(h, node.node, (centerIndex > -1 ? 'hascenter' : 'branch'))
        return [h('div', {}, [bchDom, afterChildDoms])]
      }else if (this.isEmptyNode(node)){
        if(spacialClass == 'branch') {
          node.spacialClass = 'pre-branc'
        }else if(spacialClass == 'hascenter') {
          node.spacialClass = 'pre-branch-has-center'
        }else {
          if(node.spacialClass) {
            delete node.spacialClass
          }
        }
        //空节点，存在于分支尾部
        let childDoms = this.getDomTree(h, node.node)
        this.decodeAppendDom(h, node, childDoms)
        return [h('div', {'class':{'empty-node': true}}, childDoms)];
      }else {
        //遍历到了末端，无子节点
        return [];
      }
    },
    //解码渲染的时候插入dom到同级
    decodeAppendDom(h, node, dom, props = {}){
      props.config = node
      let tag = ''
      switch(node.type){
        case 'SP': tag = 'approval';break;
        case 'TJ': tag = 'condition';break;
        case 'CS': tag = 'cc';break;
        case 'AUTOMATION': tag = 'automation';break;
        case 'MESS': tag = 'message';break;
        case 'PARALLEL': tag = 'Concurrent';break;
        case 'PB': tag = 'Concurrent';break;
        default: tag = node.type.toLowerCase();break;
      }
      dom.unshift(h(tag, {
        props: props,
        ref: node.id,
        key: node.id,
        //定义事件，插入节点，删除节点，选中节点，复制/移动
        on:{
          insertNode: type => this.insertNode(type, node),
          delNode: () => this.delNode(node),
          selected: () => this.selectNode(node),
          copy:() => this.copyBranch(node),
          leftMove: () => this.branchMove(node, -1),
          rightMove: () => this.branchMove(node, 1)
        }
      }, []))
    },
    //id映射到map，用来向上遍历
    toMapping(node){
      if (node && node.id){
        //console.log("node=> " + node.id + " name:" + node.name + " type:" + node.type)
        this.nodeMap.set(node.id, node)
      }
    },
    copyBranch(node){
      let parentNode = this.nodeMap.get(node.pid)
      // let branchNode = this.$deepCopy(node)
      // branchNode.name = branchNode.name + '-copy'
      // this.forEachNode(parentNode, branchNode, (parent, node) => {
      //   let id = this.getRandomId()
      //   node.id = id
      //   node.pid = parent.id
      // })
      copyBranchNode(this.jvsAppId, node).then(res => {
        if(res.data && res.data.code == 0) {
          let branchNode = res.data.data
          parentNode.conditions.splice((parentNode.conditions.indexOf(node) + 1), 0, branchNode)
        }
      })
      this.$forceUpdate()
    },
    branchMove(node, offset){
      let parentNode = this.nodeMap.get(node.pid)
      let index = parentNode.conditions.indexOf(node)
      let branch = parentNode.conditions[index + offset]
      parentNode.conditions[index + offset] = parentNode.conditions[index]
      parentNode.conditions[index] = branch
      this.$forceUpdate()
    },
    //判断是否为主要业务节点
    isPrimaryNode (node){
      return node && (['ROOT', 'SP', 'CS', 'AUTOMATION', 'MESS', 'DELAY', 'TRIGGER'].indexOf(node.type) > -1)
    },
    isBranchNode (node){
      return node && (node.type === 'CONDITION' || node.type === 'PARALLEL')
    },
    isEmptyNode (node){
      return node && (node.type === 'EMPTY')
    },
    //是分支节点
    isConditionNode (node){
      return node.type === 'CONDITION';
    },
    //是分支节点
    isBranchSubNode (node){
      return node && (node.type === 'CONDITION' || node.type === 'CONCURRENT');
    },
    isConcurrentNode (node){
      return node.type === 'PARALLEL'
    },
    getRandomId(){
      return (Math.floor(Math.random() * (99999 - 10000)) + 10000).toString() + new Date().getTime().toString().substring(5, 11)
    },
    //选中一个节点
    selectNode(node){
      this.$store.commit('selectedNode', node)
      this.$emit('selectedNode', node)
    },
    //处理节点插入逻辑
    insertNode(type, parentNode){
      this.$refs['_root'].click()
      //缓存一下后面的节点
      let afterNode = parentNode.node
      //插入新节点
      parentNode.node = {
        id: this.getRandomId(),
        pid: parentNode.id,
        props: {},
        type: type
      }
      if(parentNode.type == 'PB') {
        parentNode.node.parallelFlag = parentNode.parallelFlag
      }else{
        if(parentNode.parallelFlag) {
          parentNode.node.parallelFlag = parentNode.parallelFlag
        }
      }
      switch (type){
        case 'SP': this.insertApprovalNode(parentNode, afterNode); break;
        case 'CONDITION': this.insertConditionsNode(parentNode); break;
        case 'CS': this.insertCcNode(parentNode); break;
        case 'AUTOMATION': this.insertAutomationNode(parentNode); break;
        case 'MESS': this.insertMessageNode(parentNode); break;
        case 'DELAY': this.insertDelayNode(parentNode); break;
        case 'TRIGGER': this.insertTriggerNode(parentNode); break;
        case 'PARALLEL': this.insertConcurrentsNode(parentNode); break;
        default: break;
      }
      //拼接后续节点
      if(this.isBranchNode({type: type})){
        if(afterNode && afterNode.id){
          afterNode.pid = parentNode.node.node.id
        }
        this.$set(parentNode.node.node, 'node', afterNode)
      }else{
        if(afterNode && afterNode.id){
          afterNode.pid = parentNode.node.id
          this.$set(parentNode.node, 'node', afterNode)
        }
      }
      this.$store.commit('selectedNode', parentNode.node)
      this.$forceUpdate()
    },
    insertApprovalNode (parentNode){
      this.$set(parentNode.node, "name", "审批人")
      let props = JSON.parse(JSON.stringify(getDefaultNodeProps(parentNode.node.type)))
      props.btn = JSON.parse(JSON.stringify(nodeButtonList))
      this.$set(parentNode.node, "props", props)
      this.$set(parentNode.node, "nodeForm", {formId: "", sendUserForm: true, version: "" })
    },
    insertCcNode (parentNode){
      this.$set(parentNode.node, "name", "抄送人")
      this.$set(parentNode.node, "props", JSON.parse(JSON.stringify(getDefaultNodeProps(parentNode.node.type))))
      this.$set(parentNode.node, "nodeForm", {formId: "", sendUserForm: true, version: "" })
      this.$forceUpdate()
    },
    insertConditionsNode (parentNode){
      this.$set(parentNode.node, "name", "条件分支")
      this.$set(parentNode.node, 'node', {
        id: this.getRandomId(),
        pid: parentNode.node.id,
        type: "EMPTY",
        parallelFlag: parentNode.parallelFlag || ''
      })
      this.$set(parentNode.node, "conditions", [
        {
          id: this.getRandomId(),
          pid: parentNode.node.id,
          type: "TJ",
          props: JSON.parse(JSON.stringify(getDefaultNodeProps('TJ'))),
          name: "条件1",
          node: {},
          defaultCondition: false,
          groups: [ { connection: logicType.OR, cids: [], condition: [] } ],
          connection: logicType.AND,
          parallelFlag: parentNode.node.parallelFlag || ''
        },{
          id: this.getRandomId(),
          pid: parentNode.node.id,
          type: "TJ",
          props: JSON.parse(JSON.stringify(getDefaultNodeProps('TJ'))),
          name: "其他情况",
          node: {},
          defaultCondition: true,
          groups: [ { connection: logicType.OR, cids: [], condition: [] } ],
          connection: logicType.AND,
          parallelFlag: parentNode.node.parallelFlag || ''
        }
      ])
      this.$forceUpdate()
    },
    insertAutomationNode (parentNode) {
      this.$set(parentNode.node, "name", "业务逻辑")
      this.$set(parentNode.node, "props", JSON.parse(JSON.stringify(getDefaultNodeProps(parentNode.node.type))))
    },
    insertMessageNode (parentNode) {
      this.$set(parentNode.node, "name", "消息通知")
      this.$set(parentNode.node, "props", JSON.parse(JSON.stringify(getDefaultNodeProps(parentNode.node.type))))
    },
    insertDelayNode(parentNode){
      this.$set(parentNode.node, "name", "延时处理")
      this.$set(parentNode.node, "props", JSON.parse(JSON.stringify(getDefaultNodeProps(parentNode.node.type))))
    },
    insertTriggerNode(parentNode){
      this.$set(parentNode.node, "name", "触发器")
      this.$set(parentNode.node, "props", JSON.parse(JSON.stringify(getDefaultNodeProps(parentNode.node.type))))
    },
    insertConcurrentsNode(parentNode){
      this.$set(parentNode.node, "name", "并行分支")
      this.$set(parentNode.node, 'node',{
        id: this.getRandomId(),
        pid: parentNode.node.id,
        type: "EMPTY",
        parallelFlag: parentNode.parallelFlag || ''
      })
      this.$set(parentNode.node, "parallelFlag", parentNode.parallelFlag || '')
      this.$set(parentNode.node, "parallels", [{
        id: this.getRandomId(),
        pid: parentNode.node.id,
        type: "PB",
        props: JSON.parse(JSON.stringify(getDefaultNodeProps('PB'))),
        name: "分支1",
        node: {},
        parallelFlag: parentNode.node.id
      },{
        id: this.getRandomId(),
        pid: parentNode.node.id,
        type: "PB",
        props: JSON.parse(JSON.stringify(getDefaultNodeProps('PB'))),
        name: "分支2",
        node: {},
        parallelFlag: parentNode.node.id
      }])
    },
    getBranchEndNode(conditionNode){
      if (!conditionNode.node || !conditionNode.node.id){
        return conditionNode;
      }
      return this.getBranchEndNode(conditionNode.node);
    },
    addBranchNode (node){
      if(this.isConcurrentNode(node)) {
        if(node.parallels.length < 8){
          node.parallels.push({
            id: this.getRandomId(),
            pid: node.id,
            name: '分支' + (node.parallels.length+1),
            props: {},
            type: "PB",
            node: {},
            parallelFlag: node.id
          })
        }else {
          // this.$message.warning("最多只能添加8个分支")
          this.$notify({
            title: '提示',
            message: '最多只能添加8个分支',
            position: 'bottom-right',
            type: 'warning'
          });
        }
      }else{
        if(node.conditions.length < 8){
          node.conditions.splice(node.conditions.length-1, 0, {
            id: this.getRandomId(),
            pid: node.id,
            name: '条件' + (node.conditions.length),
            props: JSON.parse(JSON.stringify(getDefaultNodeProps('TJ'))),
            type: "TJ",
            node: {},
            connection: logicType.AND,
            defaultCondition: false,
            condition: [],
            groups: [ { connection: logicType.OR, cids: [], condition: [] } ],
            parallelFlag: node.parallelFlag || ''
          })
        }else {
          // this.$message.warning("最多只能添加8个条件")
          this.$notify({
            title: '提示',
            message: '最多只能添加8个条件',
            position: 'bottom-right',
            type: 'warning'
          });
        }
      }
      this.$forceUpdate()
    },
    //删除当前节点
    delNode(node){
      //获取该节点的父节点
      let parentNode = this.nodeMap.get(node.pid)
      if (parentNode){
        //判断该节点的父节点是不是分支节点
        if (this.isBranchNode(parentNode)){
          // 并行分支
          if(this.isConcurrentNode(parentNode)) {
            //移除该分支
            parentNode.parallels.splice(parentNode.parallels.indexOf(node), 1)
            //处理只剩1个分支的情况
            if (parentNode.parallels.length < 2){
              //获取条件组的父节点
              let ppNode = this.nodeMap.get(parentNode.pid)
              //判断唯一分支是否存在业务节点
              if (parentNode.parallels[0].node && parentNode.parallels[0].node.id){
                //将剩下的唯一分支头部合并到主干
                ppNode.node = parentNode.parallels[0].node
                ppNode.node.pid = ppNode.id
                ppNode.node.parallelFlag = ppNode.parallelFlag ? ppNode.parallelFlag : ''
                this.eachNodeDom(ppNode.node) // 递归后续节点修改或删除并行标识
                //搜索唯一分支末端最后一个节点
                let endNode = this.getBranchEndNode(parentNode.parallels[0])
                //后续节点进行拼接, 这里要取EMPTY后的节点
                endNode.node = parentNode.node.node
                if(endNode.node && endNode.node.id){
                  endNode.node.pid = endNode.id
                }
              }else {
                //直接合并分支后面的节点，这里要取EMPTY后的节点
                ppNode.node = parentNode.node.node
                if (ppNode.node && ppNode.node.id){
                  ppNode.node.pid = ppNode.id
                }
              }
            }
          }else{
            //移除该分支
            parentNode.conditions.splice(parentNode.conditions.indexOf(node), 1)
            //处理只剩1个分支的情况
            if (parentNode.conditions.length < 2){
              //获取条件组的父节点
              let ppNode = this.nodeMap.get(parentNode.pid)
              //判断唯一分支是否存在业务节点
              if (parentNode.conditions[0].node && parentNode.conditions[0].node.id){
                //将剩下的唯一分支头部合并到主干
                ppNode.node = parentNode.conditions[0].node
                ppNode.node.pid = ppNode.id
                //搜索唯一分支末端最后一个节点
                let endNode = this.getBranchEndNode(parentNode.conditions[0])
                //后续节点进行拼接, 这里要取EMPTY后的节点
                endNode.node = parentNode.node.node
                if (endNode.node && endNode.node.id){
                  endNode.node.pid = endNode.id
                }
              }else {
                //直接合并分支后面的节点，这里要取EMPTY后的节点
                ppNode.node = parentNode.node.node
                if (ppNode.node && ppNode.node.id){
                  ppNode.node.pid = ppNode.id
                }
              }
            }
          }
        }else {
          //不是的话就直接删除
          if (node.node && node.node.id) {
            node.node.pid = parentNode.id
          }
          parentNode.node = node.node
        }
        this.$forceUpdate()
      }else {
        this.$notify({
          title: '提示',
          message: '出现错误，找不到上级节点',
          position: 'bottom-right',
          type: 'warning'
        });
      }
    },
    validateProcess(){
      this.valid = true
      let err = []
      this.validate(err, this.dom)
      return err
    },
    validateNode(err, node){
      if (this.$refs[node.id].validate){
        this.valid = this.$refs[node.id].validate(err)
      }
    },
    //更新指定节点的dom
    nodeDomUpdate(node){
      this.$refs[node.id].$forceUpdate()
    },
    //给定一个起始节点，遍历内部所有节点
    forEachNode(parent, node, callback){
      if (this.isBranchNode(node)){
        callback(parent, node)
        this.forEachNode(node, node.node, callback)
        node.conditions.map(branchNode => {
          callback(node, branchNode)
          this.forEachNode(branchNode, branchNode.node, callback)
        })
      }else if (this.isPrimaryNode(node) || this.isEmptyNode(node) || this.isBranchSubNode(node)){
        callback(parent, node)
        this.forEachNode(node, node.node, callback)
      }
    },
    //校验所有节点设置
    validate(err, node){
      if (this.isPrimaryNode(node)){
        this.validateNode(err, node)
        this.validate(err, node.node)
      }else if (this.isBranchNode(node)){
        //校验每个分支
        node.conditions.map(branchNode => {
          //校验条件节点
          this.validateNode(err, branchNode)
          //校验条件节点后面的节点
          this.validate(err, branchNode.node)
        })
        this.validate(err, node.node)
      }else if (this.isEmptyNode(node)){
        this.validate(err, node.node)
      }
    },
    $deepCopy (obj) {
      return JSON.parse(JSON.stringify(obj))
    },
    eachNodeDom (node) {
      if(node.node && node.node.id) {
        node.node.parallelFlag = node.parallelFlag || ''
        this.eachNodeDom(node.node)
      }
      if(this.isConditionNode(node) && node.conditions) {
        for(let i in node.conditions) {
          node.conditions[i].parallelFlag = node.parallelFlag || ''
          this.eachNodeDom(node.conditions[i])
        }
      }
      if(this.isConcurrentNode(node) && node.parallels) {
        for(let i in node.parallels) {
          node.parallels[i].parallelFlag = node.id || node.parallelFlag
          this.eachNodeDom(node.parallels[i])
        }
      }
    }
  },
  watch:{

  }
}
</script>

<style lang="scss" scoped>
._root{
 margin: 0 auto;
}
._root.disabled{
  /deep/.node-footer{
    .btn{
      visibility: hidden;
    }
  }
  /deep/.node-body{
    .node-body-header, .node-body-main-header{
      .el-icon-close, .el-icon-copy-document{
        visibility: hidden;
      }
    }
  }
  /deep/.branch-node{
    .add-branch-btn{
      visibility: hidden;
    }
  }
}
.process-end{
  width: 112px;
  height: 40px;
  background: #FFFFFF;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: Microsoft YaHei-Regular, Microsoft YaHei;
  font-weight: 400;
  font-size: 14px;
  color: #363B4C;
  position: relative;
  &::before{
    content: "";
    position: absolute;
    left: calc(50% - 4px);
    top: -6px;
    width: 0;
    height: 0;
    border-left: 4px solid transparent;
    border-right: 4px solid transparent;
    border-top: 6px solid #C2C5CF;
  }
}
.primary-node{
  display: flex;
  align-items: center;
  flex-direction: column;
  .node::before{
    background: none;
    display: none;
  }
}
.branch-node{
  display: flex;
  justify-content: center;
  position: relative;
  .branch-node:not(.has-center){
    &::after{
      content: "";
      position: absolute;
      top: 38px;
      left: 50%;
      height: calc(100% - 38px);
      border-left: 1px solid #F5F6F7;
      z-index: 2;
    }
    /deep/.branch-node-item{
      >.node{
        >.node-footer{
          z-index: 3;
          &::before{
            top: 0;
          }
        }
      }
      >.node+div{
        >.node{
          >.node-footer{
            z-index: 3;
            &::before{
              top: 0;
            }
          }
        }
      }
    }
  }
}
.branch-node-item{
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  &::before{
    content: "";
    position: absolute;
    top: 38px;
    margin: auto;
    border-left: 1px solid #C2C5CF;
    height: 20px;
    z-index: 3;
  }
  &::after{
    content: "";
    position: absolute;
    left: 50%;
    top: 58px;
    height: calc(100% - 58px);
    border-right: 1px solid #C2C5CF;
  }
  // 底部边线左侧
  >.line-bot-left{
    position: absolute;
    width: 100%;
    left: 0;
    bottom: 0;
    .after-line{
      width: 50%;
      position: absolute;
      left: 0;
      top: 0;
      border-top: 1px solid #C2C5CF;
      z-index: 3;
    }
  }
  // 底部边线右侧
  >.line-bot-right{
    position: absolute;
    width: 100%;
    left: 50%;
    bottom: 0;
    .after-line{
      position: absolute;
      width: 50%;
      position: absolute;
      left: 0;
      top: 0;
      border-top: 1px solid #C2C5CF;
      z-index: 3;
    }
  }
  &.start-item, &.end-item{
    &::before{
      height: 0;
    }
    &::after{
      height: calc(100% - 78px);
    }
  }
  &.start-item{
    >.line-top-left{
      display: none;
    }
    >.line-top-right{
      position: absolute;
      width: 100%;
      left: 50%;
      top: 28px;
      .before-line{
        width: 20px;
        height: 20px;
        position: absolute;
        border-radius: 10px 0 0 0;
        left: 0;
        top: 10px;
        border: 1px solid #C2C5CF;
        border-right: 0;
        border-bottom: 0;
        z-index: 3;
      }
      .after-line{
        position: absolute;
        left: 20px;
        top: 10px;
        width: calc(50% - 20px);
        border-top: 1px solid #C2C5CF;
        z-index: 3;
      }
    }
    >.line-bot-left{
      display: none;
    }
    >.line-bot-right{
      position: absolute;
      width: 100%;
      left: 50%;
      bottom: 0;
      .before-line{
        width: 20px;
        height: 20px;
        position: absolute;
        border-radius: 0 0 0 10px;
        left: 0;
        top: -20px;
        border: 1px solid #C2C5CF;
        border-right: 0;
        border-top: 0;
        z-index: 3;
      }
      .after-line{
        width: calc(50% - 20px);
        left: 20px;
        z-index: 3;
      }
    }
  }
  &.end-item{
    >.line-top-left{
      position: absolute;
      width: 100%;
      left: 0;
      top: 28px;
      .before-line{
        width: 20px;
        height: 20px;
        position: absolute;
        border-radius: 0 10px 0 0;
        left: calc(50% - 20px);
        top: 10px;
        border: 1px solid #C2C5CF;
        border-left: 0;
        border-bottom: 0;
        z-index: 3;
      }
      .after-line{
        position: absolute;
        width: calc(50% - 20px);
        left: 0;
        top: 10px;
        border-top: 1px solid #C2C5CF;
        z-index: 3;
      }
    }
    >.line-top-right{
      display: none;
    }
    >.line-bot-left{
      .before-line{
        width: 20px;
        height: 20px;
        position: absolute;
        border-radius: 0 0 10px 0;
        left: calc(50% - 20px);
        top: -20px;
        border: 1px solid #C2C5CF;
        border-left: 0;
        border-top: 0;
        z-index: 3;
      }
      .after-line{
        width: calc(50% - 20px);
        z-index: 3;
      }
    }
    >.line-bot-right{
      display: none;
    }
  }
  &:not(.start-item):not(.end-item){
    >.line-top-left, >.line-top-right{
      position: absolute;
      top: 38px;
      left: 0;
      width: 50%;
      border-top: 1px solid #C2C5CF;
    }
    >.line-top-right{
      left: 50%;
    }
  }
}
.add-branch-btn{
  position: absolute;
  width: 96px;
  height: 28px;
  .add-branch-btn-div{
    position: absolute;
    top: -4px;
    z-index: 999;
    width: 100%;
    height: 100%;
    background: #363B4C;
    border-radius: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }
  &.condition{
    .add-branch-btn-div{
      background: #14C9C9;
    }
  }
  &::before{
    content: "";
    position: absolute;
    left: 50%;
    top: 18px;
    height: 20px;
    border-left: 1px solid #C2C5CF;
    box-sizing: border-box;
  }
}
.empty-node{
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
}
.currentNode {
  /deep/.node-body{
    position: relative;
    text-transform: uppercase;
    transition: .5s;
    letter-spacing: 4px;
    // padding: 4px;
    overflow: hidden;
    .lightLine{
      position: absolute;
      display: block;
    }
    .lightLine1 {
      top: 0;
      left: 0;
      width: 100%;
      height: 2px;
      background: linear-gradient(90deg,transparent,#03e9f4);
      animation: animate1 1s linear infinite;
    }
    .lightLine2 {
      top: -100%;
      right: 0;
      width: 2px;
      height: 100%;
      background: linear-gradient(180deg,transparent,#03e9f4);
      animation: animate2 1s linear infinite;
      animation-delay: .25s;
    }
    .lightLine3 {
      bottom: 0;
      right: 0;
      width: 100%;
      height: 2px;
      background: linear-gradient(270deg,transparent,#03e9f4);
      animation: animate3 1s linear infinite;
      animation-delay: .5s;
    }
    .lightLine4 {
      bottom: -100%;
      left: 0;
      width: 2px;
      height: 100%;
      background: linear-gradient(360deg,transparent,#03e9f4);
      animation: animate4 1s linear infinite;
      animation-delay: .75s;
    }
  }
}
.currentNode:hover{
  border-color: transparent!important;
}
@keyframes animate1 {
  0% {
    left: -100%;
  }
  50%, 100% {
    left: 100%;
  }
}
@keyframes animate2 {
  0% {
    top: -100%;
  }
  50%, 100% {
    top: 100%;
  }
}
@keyframes animate3 {
  0% {
    right: -100%;
  }
  50%, 100% {
    right: 100%;
  }
}
@keyframes animate4 {
  0% {
    bottom: -100%;
  }
  50%, 100% {
    bottom: 100%;
  }
}
</style>
<style lang="scss">
.empty-node.pre-branch{
  .node-footer{
    &::before{
      top: 15px;
      height: calc(100% - 15px);
    }
  }
}
</style>
