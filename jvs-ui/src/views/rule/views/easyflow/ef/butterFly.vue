<template>
  <div v-if="easyFlowVisible">
    <div style="display: flex;height: 100%;position: relative;">
      <div :ref="refs" class="container rule-design-container">
        <butterfly-vue
          :ref="refs+'Container'"
          :refs="refs"
          :canvasConf="canvasConf"
          :canvasData="loadData"
          @onCreateEdge="onCreateEdge"
          @onOtherEvent="onOtherEvent"
        >
        </butterfly-vue>
      </div>
      <!-- 连线更多工具 -->
      <!-- @mouseleave="moreLeaveHandle" -->
      <div v-if="showLineMore && moreEdge" class="line-more-info" :style="lineMorePos ? `left: ${lineMorePos.left}px;top: ${lineMorePos.top}px;` : ''">
        <div class="top">
          <div class="more-item">
            <span class="label">描述</span>
            <div class="con">
              <el-input v-model="moreEdge.desc" type="textarea" placeholder="请输入描述" :autosize="true" @focus="descEditing=true;" @blur="descEditing=false;">
              </el-input>
            </div>
          </div>
          <div class="more-item sort">
            <span class="label">排序</span>
            <div class="con">
              <el-input-number v-model="moreEdge.sort" controls-position="right" :min="1" size="mini" @change="sortSubmit" @focus="sortEditing=true;" @blur="sortEditing=false;"></el-input-number>
            </div>
          </div>
        </div>
        <div class="bottom">
          <div v-if="moreEdge.state != 'async'" class="more-item button" @click="warnLine">
            <div class="label">设为异常线</div>
            <div v-if="moreEdge.state == 'abnormal'" class="con">
              <svg aria-hidden="true">
                <use xlink:href="#icon-jvs-xuanzhong"></use>
              </svg>
            </div>
          </div>
          <div v-if="moreEdge.state != 'abnormal'" class="more-item button" @click="asyncLine">
            <div class="label">设为异步执行</div>
            <div v-if="moreEdge.state == 'async'" class="con">
              <svg aria-hidden="true">
                <use xlink:href="#icon-jvs-xuanzhong"></use>
              </svg>
            </div>
          </div>
        </div>
      </div>
      <!-- 结果分页 -->
      <div v-if="showPage && page" style="position: fixed;bottom: 10px;left: 66px;z-index: 4;">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
          :total="page.total"
          :current-page="page.currentPage"
          :page-size="1"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
      <!-- 分组的选区 -->
      <div v-show="selectCount > 0 && selectPos" ref="selectMoveBack" class="group-select-back"></div>
      <div v-if="selectCount > 0 && selectPos && selectSize" :class="{'group-selection': true, 'group-selection-overlap': showOverlap}" :style="`width: ${selectSize.width}px;height: ${selectSize.height}px;top: ${selectMovePos.top}px;left: ${selectMovePos.left}px;`">
        <span v-if="showOverlap">当前区域与已有分组存在重叠，请重新选择</span>
      </div>
      <!-- 分组的缩放 -->
      <div v-if="groupScalIndex > -1" class="group-scal-back" @mousemove="groupScalHandle" @mouseup="groupScalEnd"></div>
    </div>
    <!-- 节点详细信息设置 -->
    <detail-panel :ruleInfo="ruleInfo" :isClickCanvas="isClickCanvas" :activeCanvas="activeCanvas" :activeEl="activeEl" @fresh="freshHandle" />
    <!-- 修改分组的标题、描述 -->
    <el-dialog
      title="编辑分组"
      width="30%"
      :visible.sync="groupVisible"
      :before-close="groupClose">
      <div v-if="groupVisible" class="group-form-dialog">
        <jvs-form :option="groupFormOption" :formData="groupForm" @submit="groupSubmit" @cancalClick="groupClose"></jvs-form>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { copyRuleNode } from '@/views/rule/api/rule'
import { ButterflyVue } from 'butterfly-vue'
import flowNode from './butterflyNode'
import cutomGroup from './butterflyGroup'
import { guid } from "@/util/util"
import draggable from 'vuedraggable'
import eventBus from "../../../utils/eventBus"
import DetailPanel from "../DetailPanel"
export default {
  components: {
    ButterflyVue, flowNode, cutomGroup, draggable, DetailPanel,
  },
  props: {
    refs: {
      type: String
    },
    activeCanvas: {
      type: String
    },
    originData: {
      type: Object
    },
    nodeResult: {
      type: Object
    },
    ruleInfo: {
      type: Object
    },
    activeEl: {
      type: Object
    },
    page: {
      type: Object
    },
    showPage: {
      type: Boolean
    },
    disabled: {
      type: Boolean
    }
  },
  computed: {
    moreEdge () {
      let edge = null
      if(this.showLineMore) {
        for(let i in this.canvas.edges) {
          if(this.canvas.edges[i].id == this.activeElement.nodeId) {
            edge = this.canvas.edges[i].options.edge
          }
        }
      }
      return edge
    }
  },
  data() {
    return {
      loadData: {}, // 加载数据
      data: {},
      canvasConf: {
        zoomable: false, // 缩放(可传)
        moveable: true, // 平移(可传)
        linkable: true, // 节点可连接(选填)
        disLinkable: true, // 节点可取消连接(选填)
        draggable: true, // 节点可拖动(可传)
        theme: {
          edge: {
            arrow: false,
            shapeType: 'Manhattan',
            draggable: true,
            hasRadius: false,
            isExpandWidth: true, // 增加线条交互区域
            defaultAnimate: false, // 默认开启线条动画
            labelPosition: 0.5,
          },
          zoomGap: 0.001, // 鼠标放大缩小间隙设置；取值[0-1]之间，默认 0.001
          autoFixCanvas: { //节点拖动或连线拖动到画布边缘时，画布自动延展
            enable: true,
            autoMovePadding: [20, 20, 20, 20] // 触发自动延展的画布内边距
          },
          autoResizeRootSize: true, // 自动适配root大小，默认为true
          isMouseMoveStopPropagation: true, // 拖动事件是否阻止冒泡
          endpoint: {
            isAllowLinkInSameNode: false, // 锚点连接限制: 是否允许同一节点中的锚点连接
            position: [], // 限制锚点位置['Top', 'Bottom', 'Left', 'Right'],
            linkableHighlight: true, // 连线时会触发point.linkable的方法，可做高亮
            limitNum: 10, // 限制锚点的连接数目
            expandArea: { //锚点过小时，可扩大连线热区
              left: 20,
              right: 20,
              top: 20,
              botton: 20
            }
          },
          group: {
            type: 'normal', // 节点组类型(选填): normal(随意拖入拖出),inner(只能拖入不能拖出)
            includeGroups: false, // 节点组是否允许嵌套节点组
            dragGroupZIndex: 50  //节点组的默认z-index（选填，默认值：50）
          }
        },
      },
      canvas: null,
      // 控制画布销毁
      easyFlowVisible: true,
      // 激活的元素，可能是节点、可能是连线
      activeElement: {
        // 可选值 node 、line
        type: undefined,
        // 节点ID
        nodeId: undefined,
        // 连线ID
        sourceId: undefined,
        targetId: undefined,
        name: undefined
      },
      isClickCanvas: -1,
      activeLineState: '',
      showLineMore: false,
      lineMorePos: null,
      moveCount: 0, // 判断节点是否处于移动中
      selectCount: 0, // 判断是否处于选区中
      selectPos: null, // 选区起点的坐标点
      selectSize: null, // 选区的大小
      selectMovePos: null,
      selectedNodeIds: [], // 选区中的节点id集合
      showOverlap: false, // 选区是否与其他分组重叠
      groupVisible: false,
      groupForm: null,
      groupFormOption: {
        emptyBtn: false,
        column: [
          {
            label: '标题',
            prop: 'label'
          },
          {
            label: '描述',
            prop: 'desc',
            type: 'textarea',
            autoSize: true
          }
        ]
      },
      groupScalIndex: -1,
      groupMovePos: null,
      mousePos: null,
      mouseStartClient: null,
      descEditing: false,
      sortEditing: false,
      offsetXY: []
    }
  },
  created() {
    this.bindEvent()
  },
  mounted() {
    this.init()
    eventBus.$on('setLineColor', this.setLineColor)
    // 划分事件
    let _this = this
    document.addEventListener('keydown', function(event) {
      if(event.key == 'Control' || event.key == 'Shift') {
        _this.selectCount = 1
        _this.canvas.setMoveable(false)
        _this.$refs.selectMoveBack.oncontextmenu = function (e) {
          e.preventDefault()
        }
        _this.$refs.selectMoveBack.addEventListener('mousemove', _this.selectMove)
      }
    })
    document.addEventListener('keyup', function(event) {
      if(event.key == 'Control' || event.key == 'Shift') {
        if(_this.selectedNodeIds && _this.selectedNodeIds.length > 0) {
          let overlapGroupIds = []
          if(_this.canvas.groups && _this.canvas.groups.length > 0) {
            let offsetX = _this.canvas.getOffset()[0]
            let offsetY = _this.canvas.getOffset()[1]
            overlapGroupIds = _this.checkPosHandle('select', 'end', {
              left: _this.selectMovePos.left - offsetX,
              top: _this.selectMovePos.top - offsetY,
              width: _this.selectSize.width,
              height: _this.selectSize.height,
            })
          }
          if(!(overlapGroupIds && overlapGroupIds.length > 0)) {
            let groupId = 'Rg' + guid(new Date().getTime())
            let offsetX = _this.canvas.getOffset()[0]
            let offsetY = _this.canvas.getOffset()[1]
            _this.data.groups.push({
              id: groupId,
              top: _this.selectMovePos.top - offsetY,
              left: _this.selectMovePos.left - offsetX,
              width: _this.selectSize.width,
              height: _this.selectSize.height,
              label: '新建分组',
              desc: '',
              nodes: _this.selectedNodeIds
            })
            _this.data.nodeList.filter(node => {
              if(_this.selectedNodeIds.indexOf(node.id) > -1) {
                node.group = groupId
                node.left = (Number.parseFloat(node.left) - _this.selectMovePos.left + offsetX) + 'px'
                node.top = (Number.parseFloat(node.top) - _this.selectMovePos.top + offsetY) + 'px'
              }
            })
            let data = JSON.parse(JSON.stringify(_this.data))
            _this.dataReload(data)
          }
        }
        _this.selectCount = 0
        _this.selectPos = null
        _this.selectSize = null
        _this.selectMovePos = null
        _this.selectedNodeIds = []
        eventBus.$emit('setData', 'selectedNodeIds', [])
        _this.canvas.setMoveable(true)
        _this.$refs.selectMoveBack.removeEventListener('mousemove', _this.selectMove)
      }
    })
  },
  onUnmounted () {
    eventBus.$off('setLineColor', this.setLineColor)
  },
  methods: {
    bindEvent() {
      eventBus.$on("add", (evt, nodeMenu) => {
        if(nodeMenu) {
          var screenX = evt.originalEvent.clientX, screenY = evt.originalEvent.clientY
          if(!this.$refs[this.refs]) {
            return false
          }
          let efContainer = this.$refs[this.refs]
          var containerRect = efContainer.getBoundingClientRect()
          var left = screenX, top = screenY
          // 计算是否拖入到容器中
          if (left < containerRect.x || left > (containerRect.width + containerRect.x) || top < containerRect.y || containerRect.y > (containerRect.y + containerRect.height)) {
            if(containerRect.x > 0 && containerRect.y > 0) {
              this.$notify({
                title: '提示',
                message: '请把节点拖入到画布中',
                position: 'bottom-right',
                type: 'error'
              });
            }
            return
          }
          left = left - containerRect.x + efContainer.scrollLeft
          top = top - containerRect.y + efContainer.scrollTop
          // 居中
          left -= 85
          top -= 16
          var nodeId = 'R'+guid(new Date().getTime())
          // 动态生成名字
          var origName = nodeMenu.name
          var nodeName = origName
          var index = 1
          while (index < 10000) {
            var repeat = false
            for (var i = 0; i < this.data.nodeList.length; i++) {
              let node = this.data.nodeList[i]
              if (node.name === nodeName) {
                nodeName = origName + index
                repeat = true
              }
            }
            if (repeat) {
              index++
              continue
            }
            break
          }
          var node = {
            id: nodeId,
            no: nodeId,
            name: nodeName,
            remark: nodeMenu.remark,
            type: nodeMenu.type,
            left: left + 'px',
            top: top + 'px',
            ico: nodeMenu.ico,
            data: nodeMenu.data,
            canvasId: this.activeCanvas
          }
          const arr = [...this.data.nodeList]
          const findStart = arr.findIndex(function (item) {
            return item.type === 'START'
          })
          const findEnd = arr.findIndex(function (item) {
            return item.type === 'END'
          })
          if (node.type === 'START' && findStart !== -1) {
            this.$notify({
              title: '提示',
              message: '开始节点已存在',
              position: 'bottom-right',
              type: 'error'
            });
            return false
          }
          if (node.type === 'END' && findEnd !== -1) {
            this.$notify({
              title: '提示',
              message: '结束节点已存在',
              position: 'bottom-right',
              type: 'error'
            });
            return false
          }
          let groupIds = this.checkPosHandle('node', 'add', {left: left, top: top, width: 160, height: 36})
          if(groupIds && groupIds.length > 0) {
            this.data.groups.filter(group => {
              if(group.id == groupIds[0]) {
                node.group = groupIds[0]
                node.left = (left - group.left) + 'px'
                node.top = (top - group.top) + 'px'
                group.nodes.push(nodeId)
              }
            })
          }
          this.data.nodeList.push(node)
          this.$emit('addNode', node)
          let tpdata = JSON.parse(JSON.stringify(this.data))
          this.dataReload(tpdata)
        }
      });
      eventBus.$on("clickNode", (node) => {
        this.$nextTick(() => {
          this.clickNode(this.canvas.getNode(node.id))
        })
      })
      eventBus.$on("copyNode", (node) => {
        this.copyElement(node)
      })
      eventBus.$on("delNode", (node) => {
        this.deleteElement(node)
      })
      eventBus.$on("setGroup", (id, key, data) => {
        this.canvas.groups.filter(group => {
          if(group.id == id) {
            group[key] = data
          }
        })
      })
      eventBus.$on("editDelGroup", (type, id) => {
        this.canvas.groups.filter((group, index) => {
          if(group.id == id) {
            if(type == 'edit') {
              this.editGroupLabel(group)
            }
            if(type == 'del') {
              this.delGroup(group)
            }
            if(type == 'scal') {
              group.draggable = false
              this.canvas.setMoveable(false)
              this.groupScalIndex = Number.parseInt(index)
            }
          }
        })
      })
    },
    // 初始化
    init () {
      let data = {
        nodeList: [
          {
            id: this.refs == 'efContainer' ? 'start' : (this.refs+'start'),
            name: '开始',
            type: 'START',
            left: '500px',
            top: '20px',
            ico: 'el-icon-video-play',
            state: ''
          }
        ],
        lineList: [],
        groups: []
      }
      if(this.originData && this.originData.nodeList) {
        data = this.originData
      }
      let arr = []
      data.lineList.forEach(item => {
        let obj = { id: item.id, from: item.from, to: item.to, label: item.label, formula: item.formula ? item.formula : '', formulaContent: item.formulaContent ? item.formulaContent : '', desc: item.desc || '' }
        if(item.data) {
          obj.data = item.data
        }
        if(item.sort) {
          obj.sort = item.sort
        }
        if(item.state) {
          obj.state = (['abnormal', 'async'].indexOf(item.state) > -1) ? item.state : ''
        }
        if(item.breakPoints) {
          obj.breakPoints = item.breakPoints
        }
        arr.push(obj)
      })
      data.lineList = [...arr]
      if(!data.groups) {
        data.groups = []
      }
      if(this.nodeResult && this.nodeResult.nodeResult) {
        this.dealNodeResult(this.nodeResult, data)
      }else{
        this.dataReload(data)
      }
    },
    // 格式化数据，兼容处理
    formatButterFlyData (data) {
      let temp = {
        nodes: [],
        edges: [],
        groups: []
      }
      if(data && data.nodeList) {
        data.nodeList.filter(node => {
          node.endpoints = this.findPoint(node.id, node.type, node)
          temp.nodes.push({
            width: 160,
            height: 36,
            left: Number.parseFloat(node.left),
            top: Number.parseFloat(node.top),
            id: node.id,
            group: node.group,
            node: node,
            endpoints: node.endpoints,
            render: flowNode
          })
        })
      }
      if(data && data.lineList) {
        data.lineList.filter(edge => {
          let label = edge.label == '已配置公式' ? '' : edge.label
          if(label && edge.sort && (edge.label.includes('执行顺序') || !isNaN(Number(edge.label)))) {
            label = `${edge.sort}`
          }
          let obj = {
            id: edge.id,
            edge: edge,
            sourceNode: edge.from,
            targetNode: edge.to,
            source: this.findLinePoint(edge, temp.nodes),
            target: 'top',
            shapeType: 'Manhattan',
            hasRadius: true,
            label: label,
            // breakPoints: edge.breakPoints
          }
          if(edge.breakPoints) {
            obj.breakPoints = JSON.parse(JSON.stringify(edge.breakPoints))
          }
          temp.edges.push(obj)
        })
      }
      if(data && data.groups) {
        data.groups.filter(group => {
          temp.groups.push({
            id: group.id,
            left: Number.parseFloat(group.left),
            top: Number.parseFloat(group.top),
            width: group.width,
            height: group.height,
            options: group,
            draggable: true,
            resize: true,
            render: cutomGroup
          })
        })
      }
      return temp
    },
    // 根据节点给锚点
    findPoint (id, type, node) {
      let endpoint = [{
        id: 'top',
        orientation: [0, -1],
        pos: [0.5, 0.5],
      }]
      let from = []
      let to = []
      this.data.lineList.filter(line => {
        if(line.from == id) {
          from.push(line.id)
        }
        if(line.to == id) {
          to.push(line.id)
        }
      })
      if(from.length > 0) {
        endpoint = [{
          id: 'bottom',
          orientation: [0, 1],
          pos: [0.5, 0.5],
          edgeId: from[0],
          type: 'source'
        }]
        if(from.length == 2) {
          endpoint = [
            {
              id: 'bottom1',
              orientation: [0, 1],
              pos: [0.25, 0.5],
              edgeId: from[0],
              type: 'source'
            },
            {
              id: 'bottom2',
              orientation: [0, 1],
              pos: [0.75, 0.5],
              edgeId: from[1],
              type: 'source'
            }
          ]
        }
        if(from.length == 3) {
          endpoint = [
            {
              id: 'bottom1',
              orientation: [0, 1],
              pos: [0.25, 0.5],
              edgeId: from[0],
              type: 'source'
            },
            {
              id: 'bottom',
              orientation: [0, 1],
              pos: [0.5, 0.5],
              edgeId: from[1],
              type: 'source'
            },
            {
              id: 'bottom2',
              orientation: [0, 1],
              pos: [0.75, 0.5],
              edgeId: from[2],
              type: 'source'
            }
          ]
        }
      }else{
        if(!(node && node.data && node.data.functionName == '提示消息')) {
          endpoint.push({
            id: 'bottom',
            orientation: [0, 1],
            pos: [0.5, 0.5],
            type: 'source'
          })
        }
      }
      if(to.length > 0) {
        let addTop = true
        endpoint.filter(edp => {
          if(edp.id == 'top') {
            addTop = false
          }
        })
        if(addTop) {
          endpoint.push({
            id: 'top',
            orientation: [0, -1],
            pos: [0.5, 0.5],
          })
        }
      }else{
        let addTop = true
        endpoint.filter(edp => {
          if(edp.id == 'top') {
            addTop = false
          }
        })
        if(['start', 'START'].indexOf(type) == -1 && addTop) {
          endpoint.push({
            id: 'top',
            orientation: [0, -1],
            pos: [0.5, 0.5],
          })
        }
      }
      return endpoint
    },
    // 根据线找到来源锚点
    findLinePoint (edge, nodeList) {
      let source = 'bottom'
      nodeList.filter(node => {
        if(node.id == edge.from) {
          if(node.endpoints && node.endpoints.length > 0) {
            node.endpoints.filter(ep => {
              if(ep.edgeId == edge.id) {
                source = ep.id
              }
            })
          }
        }
      })
      return source
    },
    // 加载流程图
    dataReload (data) {
      this.easyFlowVisible = false
      this.data.nodeList = []
      this.data.lineList = []
      this.$nextTick(() => {
        data = JSON.parse(JSON.stringify(data))
        this.easyFlowVisible = true
        this.data = data
        this.loadData = this.formatButterFlyData(data)
        this.$emit('saveGraph', data)
        this.$nextTick(() => {
          this.canvas = this.$refs[this.refs+'Container'].canvas
          this.setMinimap()
          this.setLineStatusAndLabel()
          if(this.offsetXY && this.offsetXY.length > 0) {
            this.canvas.move(this.offsetXY)
          }
        })
      })
    },
    // 导航
    setMinimap () {
      this.canvas.setMinimap(true, {
        height: 150,
        nodeColor: "#1e6fff",
        containerStyle: {
          border: 'none',
          cursor: 'pointer',
          boxShadow: '0 0 5px #ddd'
        },
        viewportStyle: {
          backgroundColor: 'rgba(157,197,255,0.2)'
        }
      })
      this.canvas.setGuideLine(true, {
        limit: 1, // 限制辅助线条数
        adsorp: {
          enable: false, // 开启吸附效果
          gap: 5 // 吸附间隔
        },
        theme: {
          lineColor: '#1E6FFF', // 网格线条颜色
          lineWidth: 1, // 网格粗细
        }
      })
    },
    // 新增连线
    onCreateEdge (data) {
      if (data) {
        this.deleteBreakPoints(data.sourceNodeId, this.data)
        this.data.lineList.push({id: guid(new Date().getTime()), from: data.sourceNodeId, to: data.targetNodeId, label: ''})
        this.$emit('autoSave', this.data)
        // 更新连线
        let freshData = JSON.parse(JSON.stringify(this.data))
        this.dataReload(freshData)
      }
    },
    // 事件监听
    onOtherEvent (data, canvas) {
      // 点击画布
      if(data.type === 'canvas:click') {
        // 清除选择节点
        this.activeElement = {}
        eventBus.$emit('setData', 'activeElement', this.activeElement)
        this.isClickCanvas = Math.random()
        eventBus.$emit('setData', 'isClickCanvas', this.isClickCanvas)
        this.$emit('canvasClick')
        // 清除高亮线
        eventBus.$emit('setLineColor', 'unHighlight', null)
        // 清除线工具
        let arr = this.canvas.getDataMap().edges
        arr.forEach(item => {
          item.labelDom.classList.remove('hover')
        })
        this.showLineMore = false
        this.lineMorePos = null
      }
      // 移动节点
      if(data.type === 'node:move') {
        this.moveCount++
        eventBus.$emit('setData', 'moveCount', this.moveCount)
        if(data.nodes && data.nodes.length > 0) {
          this.data.nodeList.filter(node => {
            if(node.id == data.nodes[0].id) {
              node.left = data.nodes[0].left + 'px'
              node.top = data.nodes[0].top + 'px'
            }
          })
        }
        if(this.canvas.groups && this.canvas.groups.length > 0) {
          this.checkPosHandle('node', 'move', data.nodes[0])
        }
      }
      if(data.type === 'drag:start') {
        if(data.dragType === 'node:drag') {
          this.moveCount = 0
          eventBus.$emit('setData', 'moveCount', this.moveCount)
        }
        if(data.dragType === 'group:drag') {
          this.canvas.theme.isMouseMoveStopPropagation = false
          this.$refs[this.refs].addEventListener('mousemove', this.getMousePosition)
          this.groupMovePos = {
            x: data.dragGroup.left,
            y: data.dragGroup.top
          }
          this.mouseStartClient = {
            x: data.position.clientX - 56 - data.dragGroup.left,
            y: data.position.clientY - 56 - data.dragGroup.top
          }
          this.canvas.setMoveable(false)
        }
        if(this.selectCount > 0) {
          this.selectPos = {
            top: data.position.clientY,
            left: data.position.clientX
          }
        }
      }
      // 移动分组
      if(data.type === 'group:move') {
        // fix 辅助吸附导致断崖式位移
        if(!((Math.abs(data.group.left - (this.mousePos.x - this.mouseStartClient.x)) < 20) && (Math.abs(data.group.top - (this.mousePos.y - this.mouseStartClient.y)) < 20))) {
          data.group.left = this.mousePos.x - this.mouseStartClient.x
          data.group.top = this.mousePos.y - this.mouseStartClient.y
        }
        if(this.canvas.groups && this.canvas.groups.length > 1) {
          this.checkPosHandle('group', 'move', data.group)
        }
      }
      // 移动结束，更新位置
      if(data.type === 'drag:end') {
        if(data.dragType == 'canvas:drag') {
          this.offsetXY = this.canvas.getOffset()
        }
        let {dragGroup, dragNode} = data;
        if(dragGroup !== null && this.data.groups) {
          let groupIndex = this.data.groups.findIndex((item) => {
            return item.id === dragGroup.id;
          })
          if(groupIndex !== -1) {
            this.data.groups[groupIndex].left = dragGroup.left + 'px'
            this.data.groups[groupIndex].top = dragGroup.top + 'px'
          }
          if(dragGroup.nodes && dragGroup.nodes.length > 0) {
            let nodeIds = []
            dragGroup.nodes.filter(node => {
              nodeIds.push(node.id)
            })
            this.data.lineList.filter(line => {
              if((nodeIds.indexOf(line.form) > -1) || (nodeIds.indexOf(line.to) > -1)) {
                line.breakPoints = []
              }
            })
            let red = JSON.parse(JSON.stringify(this.data))
            this.dataReload(red)
          }
          this.canvas.setMoveable(true)
          this.groupMovePos = null
          this.canvas.theme.isMouseMoveStopPropagation = true
          this.$refs[this.refs].removeEventListener('mousemove', this.getMousePosition)
        }
        if(dragNode !== null && Array.isArray(this.data.nodeList)) {
          let nodeIndex = this.data.nodeList.findIndex((item) => {
            return item.id === dragNode.id;
          })
          if(nodeIndex !== -1) {
            this.data.nodeList[nodeIndex].left = dragNode.left + 'px'
            this.data.nodeList[nodeIndex].top = dragNode.top + 'px'
          }
        }
        if(data.dragType == 'node:drag') {
          eventBus.$emit('setData', 'moveCount', 0)
          this.updateLineBreakPoints()
          this.updateGroupNode()
        }
        this.$forceUpdate()
      }
    },
    // 设置线条颜色 
    // highLight：当前节点关联的线条高亮 || unHighlight：取消线条高亮 
    // node 鼠标操作的节点
    setLineColor (type, node) {
      if(type === 'highLight') {
        let arr = this.canvas.getNeighborEdges(node.id)
        arr.forEach(item => {
          item.setZIndex(999)
          item.dom.classList.remove('line-status', 'in-line', 'out-line')
          item.dom.classList.add('line-status')
          if(item.options.targetNode == node.id) {
            item.dom.classList.add('in-line')
          }
          if(item.options.sourceNode == node.id) {
            item.dom.classList.add('out-line')
          }
        })
      }
      if(type === 'unHighlight') {
        let arr = this.canvas.getDataMap().edges
        arr.forEach(item => {
          item.setZIndex(0)
          item.dom.classList.remove('line-status', 'in-line', 'out-line')
        })
        this.$nextTick( () => {
          let arr = this.canvas.getDataMap().edges
          arr.forEach(item => {
            item.setZIndex(0)
            item.dom.classList.remove('line-status', 'in-line', 'out-line')
          })
        })
      }
      this.$forceUpdate()
    },
    // 点击节点
    clickNode (node) {
      if(node) {
        this.activeElement.type = 'node'
        this.activeElement.nodeId = node.id
        this.activeElement.name = node.options.node.name
        this.isClickCanvas = -1
        if(['start', 'START'].indexOf(node.options.node.type) == -1) {
          eventBus.$emit('nodeselectchange', node)
          eventBus.$emit('setData', 'isClickCanvas', this.isClickCanvas)
          eventBus.$emit('setData', 'activeElement', this.activeElement)
          this.$emit('nodeSelect')
        }
      }
    },
    // 设置线状态、label
    setLineStatusAndLabel () {
      for(let i in this.canvas.edges) {
        // 状态
        if(this.canvas.edges[i].options && this.canvas.edges[i].options.edge && this.canvas.edges[i].options.edge.state) {
          if(this.nodeResult && JSON.stringify(this.nodeResult) != '{}') {
            this.canvas.edges[i].dom.classList.add('active')
          }
          this.canvas.edges[i].dom.classList.add(this.canvas.edges[i].options.edge.state)
        }
        let _this = this
        let edge = this.canvas.edges[i]
        edge.eventHandlerDom.addEventListener('mouseenter', () => {
          if(_this.activeElement.nodeId && _this.activeElement.nodeId != edge.id) {
            _this.showLineMore = false
            _this.lineMorePos = null
            for(let j in _this.canvas.edges) {
              _this.setEdgeStateEvent(_this.canvas.edges[j], 'leave')
            }
          }
          _this.setEdgeStateEvent(edge, 'enter')
        })
        edge.eventHandlerDom.addEventListener('mouseleave', () => {
          if(_this.showLineMore && _this.activeElement.nodeId == edge.id)  return
          _this.setEdgeStateEvent(edge, 'leave')
        })
        // label盒子
        let div = document.createElement('div')
        div.classList.add('edge-label-div')
        div.addEventListener('mouseenter', function () {
          _this.setEdgeStateEvent(edge, 'enter')
        })
        div.addEventListener('mouseleave', function () {
          if(_this.showLineMore && _this.activeElement.nodeId == edge.id)  return
          _this.setEdgeStateEvent(edge, 'leave')
        })
        // label文本
        if(this.canvas.edges[i].label) {
          div.classList.add('edge-label-div-text')
          div.innerHTML = `
            <div class="edge-label${isNaN(Number(this.canvas.edges[i].label)) ? ' show-text' : ''}">
              <div class="text ${edge.options.edge.state == 'async' ? 'async' : ''}">${this.canvas.edges[i].label}</div>
            </div>
          `
        }
        // label工具栏
        let edgeTool = document.createElement('div')
        edgeTool.classList.add('edge-tool')
        // 设置公式
        if(edge.options.edge.state !== 'abnormal') {
          let formulaTool = document.createElement('div')
          formulaTool.classList.add('tool-item')
          if(edge.options.edge.formula && edge.options.edge.formulaContent && edge.options.edge.formulaContent.trim().length > 0) {
            formulaTool.classList.add('tool-item-formula')
          }
          formulaTool.innerHTML = `
            <svg aria-hidden="true" class="no-formlua">
              <use xlink:href="#icon-jvs-rongqi1"></use>
            </svg>
            <svg aria-hidden="true" class="has-formlua">
              <use xlink:href="#icon-jvs-a-zu10923"></use>
            </svg>
          `
          formulaTool.addEventListener('click', function() {
            _this.setEdgeStateEvent(edge, 'enter')
            _this.setLineFormula(edge)
          })
          edgeTool.appendChild(formulaTool)
        }
        // 删除连线
        let delTool = document.createElement('div')
        delTool.classList.add('tool-item')
        delTool.innerHTML = `
          <svg aria-hidden="true">
            <use xlink:href="#jvs-ui-icon-shanchuyonghu"></use>
          </svg>
        `
        delTool.addEventListener('click', function() {
          _this.setEdgeStateEvent(edge, 'enter')
          _this.deleteElement(null)
        })
        edgeTool.appendChild(delTool)
        // 线更多
        let moreTool = document.createElement('div')
        moreTool.classList.add('tool-item')
        moreTool.innerHTML = `
          <svg aria-hidden="true" class="normal-svg">
            <use xlink:href="#icon-jvs-a-gengduo1x"></use>
          </svg>
          <svg aria-hidden="true" class="active-svg">
            <use xlink:href="#icon-jvs-a-gengduo-xuanzhong1x"></use>
          </svg>
        `
        moreTool.addEventListener('click', function(e) {
          _this.setEdgeStateEvent(edge, 'enter')
          _this.lineMorePos = {
            left: Number.parseInt(edge.labelDom.style.left) + (24 * (edge.options.edge.state !== 'abnormal' ? 3 : 2) + 10),
            top: Number.parseFloat(edge.labelDom.style.top)
          }
          let canvasOffset = _this.canvas.getOffset()
          if(canvasOffset.length > 1) {
            _this.lineMorePos.left += canvasOffset[0]
            _this.lineMorePos.top += canvasOffset[1]
          }
          _this.showLineMore = true
          
        })
        edgeTool.appendChild(moreTool)
        div.appendChild(edgeTool)
        this.canvas.edges[i].labelDom.appendChild(div)
      }
    },
    // 设置线的状态事件
    setEdgeStateEvent (edge, type) {
      if(type == 'enter') {
        this.activeElement = {
          type: 'line',
          nodeId: edge.id,
          sourceId: edge.options.sourceNode,
          targetId: edge.options.targetNode
        }
        eventBus.$emit('setData', 'activeElement', this.activeElement)
        edge.labelDom.classList.add('hover')
        edge.setZIndex(999)
        edge.dom.classList.remove('line-status', 'in-line', 'out-line')
        edge.dom.classList.add('line-status', 'out-line')
      }
      if(type == 'leave') {
        this.activeElement = {}
        eventBus.$emit('setData', 'activeElement', this.activeElement)
        edge.setZIndex(0)
        edge.dom.classList.remove('line-status', 'in-line', 'out-line')
        edge.labelDom.classList.remove('hover')
      }
    },
    // 缩放分组
    groupScalHandle (e) {
      if(e.button == 0 && this.groupScalIndex > -1) {
        if((e.clientX - 56 - this.canvas.groups[this.groupScalIndex].left) > 180 && (e.clientY - 56 - this.canvas.groups[this.groupScalIndex].top) > 68) {
          this.canvas.groups[this.groupScalIndex].setSize(e.clientX - 56 - this.canvas.groups[this.groupScalIndex].left, e.clientY - 56 - this.canvas.groups[this.groupScalIndex].top)
          this.canvas.groups[this.groupScalIndex].options.width = e.clientX - 56 - this.canvas.groups[this.groupScalIndex].left
          this.canvas.groups[this.groupScalIndex].options.height = e.clientY - 56 - this.canvas.groups[this.groupScalIndex].top
          this.data.groups.filter(group => {
            if(group.id == this.canvas.groups[this.groupScalIndex].id) {
              group.width = this.canvas.groups[this.groupScalIndex].getWidth()
              group.height = this.canvas.groups[this.groupScalIndex].getHeight()
            }
          })
        }
      }
    },
    groupScalEnd (e) {
      if(e.button == 0) {
        this.canvas.groups[this.groupScalIndex].draggable = true
        this.canvas.setMoveable(true)
        this.groupScalIndex = -1
      }
    },
    // 修改分组label
    editGroupLabel (group) {
      this.groupForm = JSON.parse(JSON.stringify(group.options))
      if(this.groupForm.desc) {
        this.groupForm.desc = this.groupForm.desc.replace(/<br\/>/g, '\n')
      }
      this.groupVisible = true
    },
    // 删除分组
    delGroup (group) {
      let name = group.options.label
      this.$confirm('确定要删除 ' + name + ' ?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        closeOnClickModal: false
      }).then(() => {
        this.data.groups = this.data.groups.filter(item => {
          if(item.id != group.id) {
            return item
          }
        })
        this.data.nodeList.filter(node => {
          if(node.group == group.id) {
            node.group = ''
            node.left = Number.parseFloat(node.left) + group.left + 'px'
            node.top = Number.parseFloat(node.top) + group.top + 'px'
          }
        })
        this.canvas.removeGroup(group.id)
      }).catch(e => {})
    },
    // 更新拐点
    updateLineBreakPoints () {
      this.canvas.getDataMap().edges.forEach(item => {
        for(let i in this.data.lineList) {
          if(this.data.lineList[i].id == item.id) {
            this.data.lineList[i].breakPoints = item.getBreakPoints()
          }
        }
      })
    },
    // 更新分组及节点信息
    updateGroupNode () {
      this.canvas.groups.filter(group => {
        group.options.nodes = []
        if(group.nodes && group.nodes.length > 0) {
          for(let i in group.nodes) {
            group.options.nodes.push(group.nodes[i].id)
          }
        }
        this.data.groups.filter(git => {
          if(git.id == group.id) {
            git.nodes = group.options.nodes
          }
        })
      })
      this.canvas.nodes.filter(node => {
        node.options.group = ''
        node.options.node.group = ''
        if(node.group) {
          node.options.group = node.group
          node.options.node.group = node.group
        }
        this.data.nodeList.filter(nit => {
          if(nit.id == node.id) {
            nit.group = node.options.group
          }
        })
      })
      eventBus.$emit('setData', 'entryGroup', [])
    },
    freshHandle (node, fromType) {
      let data = JSON.parse(JSON.stringify(this.data))
      if(node.type == 'line') {
        data.lineList.forEach(item => {
          if(item.from == node.sourceId && item.to == node.targetId) {
            item.label = node.label
          }
        })
      }else{
        data.nodeList.forEach(item => {
          if(item.id == node.id) {
            if(node.node && node.node.data) {
              item.data = node.node.data
            }else{
              item.data = node.node
            }
            item.name = node.node.name
            item.desc = node.node.desc || ''
          }
        })
      }
      this.dataReload(data)
      this.$emit('fresh', data, fromType ? node : null)
      this.$forceUpdate()
    },
    // 线-更多移出
    moreLeaveHandle () {
      if(this.descEditing || this.sortEditing) return false
      // 清除选择节点
      this.activeElement = {}
      eventBus.$emit('setData', 'activeElement', this.activeElement)
      // 清除高亮线
      eventBus.$emit('setLineColor', 'unHighlight', null)
      // 清除线工具
      let arr = this.canvas.getDataMap().edges
      arr.forEach(item => {
        item.labelDom.classList.remove('hover')
      })
      this.showLineMore = false
      this.lineMorePos = null
      // 更新视图
      let freshData = JSON.parse(JSON.stringify(this.data))
      this.dataReload(freshData)
    },
    // 线排序
    sortSubmit () {
      this.data.lineList.filter(it => {
        if(it.from == this.activeElement.sourceId && it.to == this.activeElement.targetId) {
          it.sort = this.moreEdge.sort || 1
          it.label = `${it.sort}`
        }
      })
      // 更新视图
      let freshData = JSON.parse(JSON.stringify(this.data))
      this.dataReload(freshData)
    },
    // 线的异常设置
    warnLine () {
      if(this.activeElement.type === 'line') {
        this.data.lineList.filter(it => {
          if(it.from == this.activeElement.sourceId && it.to == this.activeElement.targetId) {
            it.state = 'abnormal'
            it.label = `异常处理`
            this.$set(this.moreEdge, 'state', 'abnormal')
          }
        })
        // 更新连线
        let freshData = JSON.parse(JSON.stringify(this.data))
        this.dataReload(freshData)
      }
      this.$forceUpdate()
    },
    // 线的异步
    asyncLine () {
      if(this.activeElement.type === 'line') {
        this.data.lineList.filter(it => {
          if(it.from == this.activeElement.sourceId && it.to == this.activeElement.targetId) {
            if(it.state == 'async') {
              it.state = ''
              it.label = ``
              this.$set(this.moreEdge, 'state', '')
            }else{
              it.state = 'async'
              it.label = `异步处理`
              this.$set(this.moreEdge, 'state', 'async')
            }
          }
        })
        // 更新连线
        let freshData = JSON.parse(JSON.stringify(this.data))
        this.dataReload(freshData)
      }
      this.$forceUpdate()
    },
    // 线设置公式
    setLineFormula (edge) {
      let moreEdge = JSON.parse(JSON.stringify(edge.options.edge))
      let pdata = {
        businessId: moreEdge.id
      }
      if(this.ruleInfo.jvsAppId) {
        pdata.jvsAppId = this.ruleInfo.jvsAppId
      }
      if(this.ruleInfo.id) {
        pdata.designId = this.ruleInfo.id
      }
      if(moreEdge && moreEdge.id) {
        let obj = {
          graphId: moreEdge.id
        }
        if(this.activeCanvas != 'main') {
          obj.canvasId = this.activeCanvas
        }
        pdata.extendJson = JSON.stringify(obj)
      }
      this.lineFormula(pdata, moreEdge)
    },
    // 线打开公式
    async lineFormula (pdata, moreEdge) {
      await this.$emit('autoSave', this.data)
      this.$setFormula({
        title: '公式设置',
        hasModal: true,
        label: moreEdge.label ? (moreEdge.label.includes('执行顺序') ? moreEdge.label.split('<br/>')[0] : `执行顺序：${moreEdge.label}`) : '连线',
        execId: moreEdge.formula ? moreEdge.formula : '',
        apiPrefix: 'jvs-design',
        useCase: 'RULE',
        props: pdata,
        afterSave: (dialog, data) => {
          if(data && data.id) {
            this.data.lineList.filter(it => {
              if(it.id == pdata.businessId) {
                this.$set(it, 'formula', data.id)
                this.$set(it, 'formulaContent', data.body)
              }
            })
            // 更新连线
            let freshData = JSON.parse(JSON.stringify(this.data))
            this.dataReload(freshData)
          }
          dialog.handleClose()
          this.activeElement = {}
          eventBus.$emit('setData', 'activeElement', this.activeElement)
        },
        afterCLose: (dialog) => {
          this.activeElement = {}
          eventBus.$emit('setData', 'activeElement', this.activeElement)
        }
      })
    },
    // 执行结果处理
    dealNodeResult (newValue, designData) {
      if(newValue.graph) {
        this.data = JSON.parse(JSON.stringify(newValue.graph))
      }
      if(newValue.clear) {
        for(let i in this.data.nodeList) {
          this.data.nodeList[i].testData = null
          this.data.nodeList[i].state = ''
        }
      }
      let newVal = ''
      if(newValue.nodeResult) {
        newVal = newValue.nodeResult
      }
      let data = designData ? designData : JSON.parse(JSON.stringify(this.data))
      for(let i in data.lineList) {
        if(data.lineList[i].state !== 'async') {
          if(newValue.errorNodeId && newValue.errorNodeId.indexOf(data.lineList[i].id) > -1) {
            data.lineList[i].state = 'error'
          }else{
            if(newValue.lines && newValue.lines.indexOf(data.lineList[i].id) > -1) {
              data.lineList[i].state = 'success'
            }else{
              data.lineList[i].state = data.lineList[i].state == 'abnormal' ? 'abnormal' : ''
            }
          }
        }
      }
      for(let i in data.nodeList) {
        // 执行中
        if(newValue.execNodeId && data.nodeList[i].id == newValue.execNodeId) {
          data.nodeList[i].state = 'running'
          data.nodeList[i].testData = {}
        }
        // 成功
        if(JSON.stringify(newVal) != '{}') {
          if(newVal[data.nodeList[i].id]) {
            data.nodeList[i].testData = newVal[data.nodeList[i].id]
            data.nodeList[i].state = 'success'
          }else{
            data.nodeList[i].testData = {}
            data.nodeList[i].state = ''
          }
        }
        // 失败
        if(newValue.errorNodeId && newValue.errorNodeId.indexOf(data.nodeList[i].id) > -1) {
          data.nodeList[i].testData = newVal[data.nodeList[i].id] || {value: newValue.errorMessage}
          data.nodeList[i].state = 'error'
        }
      }
      // 结束清除连线loading
      if(newValue.isEnd) {
        for(let i in data.lineList) {
          if(newValue.errorNodeId && newValue.errorNodeId.indexOf(data.lineList[i].id) > -1) {
            data.lineList[i].state = 'error'
            this.$notify({
              title: '提示',
              message: newValue.errorMessage,
              position: 'bottom-right',
              type: 'error'
            });
          }else{
            if(data.lineList[i].state == 'error') {
              data.lineList[i].state = ''
            }
          }
        }
      }
      this.dataReload(data)
    },
    // 分页
    handleCurrentChange (val) {
      this.$set(this.page, 'currentPage', val)
      this.$emit('currentChange')
    },
    // 复制节点
    copyElement (node) {
      if(node.canvasId == this.activeCanvas) {
        let copyData = JSON.parse(JSON.stringify(node))
        copyData.id = 'R'+guid(new Date().getTime())
        copyData.no = copyData.id
        copyData.top = Number.parseFloat(copyData.top) + 56 + 'px'
        copyRuleNode(this.ruleInfo.jvsAppId, this.ruleInfo.id, copyData).then(res => {
          if(res.data && res.data.code == 0 && res.data.data) {
            this.data.nodeList.push(res.data.data)
            this.$emit('addNode', res.data.data)
            let tpdata = JSON.parse(JSON.stringify(this.data))
            this.dataReload(tpdata)
          }
        })
      }
    },
    // 删除激活的元素
    deleteElement (node) {
      if(node) {
        this.deleteNode(node.id, node.name)
      }else{
        if(this.activeElement.type === 'node'){
          this.deleteNode(this.activeElement.nodeId, this.activeElement.name)
        }else if(this.activeElement.type === 'line'){
          let lineId = this.activeElement.nodeId
          let fromNodeId = ''
          this.canvas.edges.filter(edge => {
            if(edge.id == lineId) {
              fromNodeId = edge.options.sourceNode
            }
          })
          this.$confirm('确定删除所点击的线吗?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.rightMenu = false
            if(fromNodeId) {
              this.deleteBreakPoints(fromNodeId, this.data)
            }
            let data = JSON.parse(JSON.stringify(this.data))
            data.lineList = data.lineList.filter(line => {
              if(line.id !== lineId) {
                return line
              }
            })
            this.canvas.removeEdge(lineId)
            eventBus.$emit('setData', 'activeElement', {})
            this.dataReload(data)
          }).catch(() => {})
        }
      }
    },
    /**
     * 删除节点
     * @param nodeId 被删除节点的ID
     */
    deleteNode(nodeId, name) {
      this.$confirm('确定要删除节点 ' + name + ' ?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        closeOnClickModal: false
      }).then(() => {
        /**
         * 这里需要进行业务判断，是否可以删除
         */
        this.data.nodeList = this.data.nodeList.filter(function (node) {
          if (node.id === nodeId) {
            // 伪删除，将节点隐藏，否则会导致位置错位
            // node.show = false
            return false
          }
          return true
        })
        this.$nextTick(function () {
          this.rightMenu = false
        })
        eventBus.$emit('nodeselectchange', null)
        eventBus.$emit('setData', 'activeElement', {})
        // 删除相关连线
        let data = JSON.parse(JSON.stringify(this.data))
        let fromNodeIds = []
        data.lineList = data.lineList.filter(line => {
          if(line.to == nodeId) {
            this.data.nodeList.filter(node => {
              if(node.id == line.from) {
                fromNodeIds.push(node.id)
              }
            })
          }
          if(line.to != nodeId && line.from != nodeId) {
            return line
          }
        })
        this.deleteBreakPoints(fromNodeIds, data)
        this.dataReload(data)
        this.$emit('deleteNode', nodeId)
        this.$forceUpdate()
      }).catch(e => {})
      return true
    },
    // 删除--新增或被删连线的来源节点的起源连线的拐点
    deleteBreakPoints (from, data) {
      let temp = []
      if(typeof from == 'string') {
        temp = [from]
      }else{
        temp = from
      }
      data.lineList.filter(line => {
        if(temp.indexOf(line.from) > -1) {
          line.breakPoints = []
        }
      })
    },
    // 选区变化
    selectMove (e) {
      if(this.selectCount > 0 && this.selectPos) {
        let offsetX = this.canvas.getOffset()[0]
        let offsetY = this.canvas.getOffset()[1]
        // 设置选区块的位置大小
        this.selectSize = {
          width: Math.abs(e.clientX - this.selectPos.left),
          height: Math.abs(e.clientY - this.selectPos.top)
        }
        if(e.clientX < this.selectPos.left && e.clientY < this.selectPos.top) {
          this.selectMovePos = {
            left: e.clientX - 56,
            top: e.clientY - 56
          }
        }else if(e.clientX < this.selectPos.left && !(e.clientY < this.selectPos.top)){
          this.selectMovePos = {
            left: e.clientX - 56,
            top: this.selectPos.top -56
          }
        }else if(!(e.clientX < this.selectPos.left) && e.clientY < this.selectPos.top){
          this.selectMovePos = {
            left: this.selectPos.left - 56,
            top: e.clientY - 56
          }
        }else{
          this.selectMovePos = {
            left: this.selectPos.left - 56,
            top: this.selectPos.top -56
          }
        }
        // 判断选区中的节点
        let left = this.selectMovePos.left // 最左边
        let right = this.selectMovePos.left + this.selectSize.width // 最右边
        let top = this.selectMovePos.top // 最上边
        let bottom = this.selectMovePos.top + this.selectSize.height // 最下边
        let nodeArray = []
        this.data.nodeList.filter(node => {
          if(!node.group) {
            let nl = Number.parseFloat(node.left) + offsetX
            let nt = Number.parseFloat(node.top) + offsetY
            if((!(nl < left)) && (!(nl > right-160)) && (!(nt < top)) && (!(nt > bottom-36))) {
              nodeArray.push(node.id)
            }
          }
        })
        this.selectedNodeIds = nodeArray
        eventBus.$emit('setData', 'selectedNodeIds', nodeArray)
        this.showOverlap = false
        if(nodeArray && nodeArray.length > 0) {
          let overlapGroupIds = []
          if(this.canvas.groups && this.canvas.groups.length > 0) {
            let offsetX = this.canvas.getOffset()[0]
            let offsetY = this.canvas.getOffset()[1]
            overlapGroupIds = this.checkPosHandle('select', 'end', {
              left: this.selectMovePos.left - offsetX,
              top: this.selectMovePos.top - offsetY,
              width: this.selectSize.width,
              height: this.selectSize.height,
            })
          }
          if(overlapGroupIds && overlapGroupIds.length > 0) {
            this.showOverlap = true
          }
        }
      }
    },
    groupSubmit () {
      this.data.groups.filter(group => {
        if(group.id == this.groupForm.id) {
          group.label = this.groupForm.label
          group.desc = this.groupForm.desc
        }
      })
      this.canvas.groups.filter(group => {
        if(group.id == this.groupForm.id) {
          group.options.label = this.groupForm.label
          group.options.desc = this.groupForm.desc
        }
      })
      this.groupClose()
    },
    groupClose () {
      this.groupForm = null
      this.groupVisible = false
    },
    // 碰撞检测
    checkPosHandle (type, optype, node) {
      let  groupIds = []
      let leftTop = null
      let rightTop = null
      let leftBottom = null
      let rightBottom = null
      
      // 节点拖拽进分组
      if(type == 'node') {
        let nodeGroup = null
        if(node.group) {
          nodeGroup = this.canvas.getGroup(node.group)
        }
        leftTop = {
          x: node.left + (nodeGroup ? nodeGroup.left : 0),
          y: node.top + (nodeGroup ? nodeGroup.top : 0)
        }
        rightTop = {
          x: node.left + node.width + (nodeGroup ? nodeGroup.left : 0),
          y: node.top + (nodeGroup ? nodeGroup.top :  0)
        }
        leftBottom = {
          x: node.left + (nodeGroup ? nodeGroup.left : 0),
          y: node.top + node.height + (nodeGroup ? nodeGroup.top : 0)
        }
        rightBottom = {
          x: node.left + node.width + (nodeGroup ? nodeGroup.left : 0),
          y: node.top + node.height + (nodeGroup ? nodeGroup.top : 0)
        }
      }else{
        leftTop = {
          x: node.left,
          y: node.top
        }
        rightTop = {
          x: node.left + node.width,
          y: node.top
        }
        leftBottom = {
          x: node.left,
          y: node.top + node.height
        }
        rightBottom = {
          x: node.left + node.width,
          y: node.top + node.height
        }
      }
      if(this.canvas.groups && this.canvas.groups.length > 0) {
        this.canvas.groups.filter(group => {
          let glt = {
            x: group.left,
            y: group.top
          }
          let grt = {
            x: group.left + group.width,
            y: group.top
          }
          let glb = {
            x: group.left,
            y: group.top+ group.height
          }
          let grb = {
            x: group.left + group.width,
            y: group.top + group.height
          }
          if(type == 'node') {
            if((leftTop.x > glt.x && leftTop.y > glt.y) &&
              (rightTop.x < grt.x && rightTop.y > grt.y) &&
              (leftBottom.x > glb.x && leftBottom.y < glb.y) &&
              (rightBottom.x < grb.x && rightBottom.y < grb.y))
            {
              groupIds.push(group.id)
            }
          }else{
            if((type == 'group' ? (group.id != node.id) : true)) {
              // 左边缘
              if((!(rightTop.x < glt.x)) && (!(rightBottom.y < glt.y) && (leftTop.y < glb.y)) && (leftTop.x < glt.x)) {
                // console.log('左')
                if(groupIds.indexOf(group.id) == -1) {
                  groupIds.push(group.id)
                }
              }
              // 右边缘
              if((!(leftTop.x > grt.x)) && (!(leftBottom.y < grt.y) && (leftTop.y < glb.y)) && (rightTop.x > grt.x)) {
                // console.log('右')
                if(groupIds.indexOf(group.id) == -1) {
                  groupIds.push(group.id)
                }
              }
              // 上边缘
              if((!(leftBottom.y < glt.y)) && ((!(rightBottom.x < glt.x)) && (leftTop.x < grb.x)) && (leftTop.y < glt.y)) {
                // console.log('上')
                if(groupIds.indexOf(group.id) == -1) {
                  groupIds.push(group.id)
                }
              }
              // 下边缘
              if(!(leftTop.y > glb.y) && ((!(leftTop.x < glb.x)) && (leftTop.x < grb.x)) && (leftBottom.y > glb.y)) {
                // console.log('下')
                if(groupIds.indexOf(group.id) == -1) {
                  groupIds.push(group.id)
                }
              }
            }
          }
        })
      }
      if(type == 'node' && optype == 'move') {
        eventBus.$emit('setData', 'entryGroup', groupIds)
      }
      if(type != 'node' && optype == 'move') {
        if(groupIds.length > 0) {
          if(this.groupMovePos) {
            node.left = this.groupMovePos.x
            node.top = this.groupMovePos.y
          }
        }else{
          if(node.left && node.top) {
            this.groupMovePos = {
              x: node.left,
              y: node.top
            }
          } 
        }
      }
      if(type == 'select' && optype == 'end') {
        return groupIds
      }
      if(type == 'node' && optype == 'add') {
        return groupIds
      }
    },
    // 记录鼠标相对画布的点位
    getMousePosition (e) {
      this.mousePos = {
        x: e.clientX - 56,
        y: e.clientY - 56
      }
    }
  },
  watch: {
    nodeResult: {
      handler(newValue, oldVal) {
        this.dealNodeResult(newValue)
      }
    },
    activeCanvas: {
      handler(newVal, oldVal) {
        this.isClickCanvas = Math.random()
        eventBus.$emit('setData', 'isClickCanvas', this.isClickCanvas)
        if(newVal){
          let tpdata = JSON.parse(JSON.stringify(this.data))
          this.dataReload(tpdata)
        }
      }
    },
  }
}
</script>
<style lang="scss" scoped>
.line-more-info{
  position: absolute;
  z-index: 600;
  width: 200px;
  background: #FFFFFF;
  box-shadow: 0px 2px 6px 0px rgba(54,59,76,0.15);
  border-radius: 4px 4px 4px 4px;
  border: 0px solid #D7D8DB;
  padding-top: 12px;
  box-sizing: border-box;
  .more-item{
    display: flex;
    overflow: hidden;
    .label{
      width: 24px;
      height: 17px;
      font-family: Source Han Sans-Regular, Source Han Sans;
      font-weight: 400;
      font-size: 12px;
      color: #363B4C;
      line-height: 17px;
    }
    .con{
      flex: 1;
      margin-left: 8px;
      overflow: hidden;
      /deep/.el-textarea{
        .el-textarea__inner{
          border: 0;
          background-color: #F5F6F7;
          min-height: unset;
          max-height: 153px;
          font-family: Source Han Sans-Regular, Source Han Sans;
          font-weight: 400;
          font-size: 12px;
          color: #363B4C;
        }
        .el-input__count{
          background: none;
        }
      }
      /deep/.el-input-number{
        width: 100%;
        height: 32px;
        background: #F5F6F7;
        border-radius: 4px;
        .el-input-number__decrease, .el-input-number__increase{
          line-height: 16px;
          border: 0;
        }
        .el-input{
          .el-input__inner{
            border: 0;
            background: none;
            height: 32px;
            line-height: 32px;
            text-align: left;
          }
        }
      }
    }
  }
  .top{
    padding: 0 16px;
    .more-item+.more-item{
      margin-top: 8px;
    }
    .sort{
      align-items: center;
    }
  }
  .bottom{
    margin-top: 16px;
    padding: 8px 0;
    border-top: 1px solid #EEEFF0;
    .more-item{
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 33px;
      padding: 0 16px;
      cursor: pointer;
      &:hover{
        background-color: #F5F6F7;
      }
      .label{
        flex: 1;
      }
      .con{
        display: flex;
        align-items: center;
        justify-content: flex-end;
        svg{
          width: 16px;
          height: 16px;
        }
      }
    }
  }
}
.group-select-back{
  position: absolute;
  left: 0;
  top: 0;
  z-index: 700;
  width: 100%;
  height: 100%;
  cursor: crosshair;
}
.group-scal-back{
  position: absolute;
  left: 0;
  top: 0;
  z-index: 700;
  width: 100%;
  height: 100%;
  cursor: nw-resize;
}
.group-form-dialog{
  /deep/.jvs-form{
    .el-textarea{
      .el-textarea__inner{
        max-height: 360px;
      }
    }
  }
}
.group-selection{
  position: absolute;
  left: 0;
  top: 0;
  z-index: 699;
  width: 200px;
  height: 200px;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  span{
    color: #FF194C;
  }
  &.group-selection-overlap{
    background: rgba(255, 25, 76, 0.2);
  }
}
/deep/.rule-design-container{
  background-color: #F5F6F7;
  background-repeat: repeat;
  background-attachment: fixed;
  -webkit-font-smoothing: antialiased;
  background-size: 11px 11px;
  position: relative;
  overflow: auto;
  flex: 1;

  // butter-fly
  .butterfly-vue-container {
    // 连线
    .butterfly-svg{
      width: fit-content;
      height: fit-content;
      .butterflies-link-event-handler{
        cursor: pointer;
      }
      .butterflies-link{
        stroke-width: 2px;
        stroke-dasharray: 4 4;
        stroke: #C2C5CF;
        cursor: pointer;
        &.active{
          animation-name: ring;
          animation-duration: 2s;
          animation-timing-function: linear;
          animation-iteration-count: infinite;
          stroke-dasharray: 5;
        }
        &.error{
          stroke: #F56C6C;
        }
        &.success{
          stroke: #84CF65;
          animation-name: ring;
          animation-duration: 2s;
          animation-timing-function: linear;
          animation-iteration-count: infinite;
          stroke-dasharray: 5;
        }
        &.abnormal{
          stroke: pink;
        }
        &.async{
          stroke: #FF9736;
        }
        &:hover{
          stroke: #1e6fff;
          stroke-width: 3px;
          stroke-dasharray: none;
        }
        &:active{
          stroke: #1e6fff;
          stroke-width: 3px;
          stroke-dasharray: none;
        }
      }
      .line-status{
        stroke-dasharray: none;
        stroke-width: 3px;
      }
      .in-line{
        stroke: #36B452;
      }
      .out-line{
        stroke: #1e6fff;
      }
    }
    // 锚点
    .butterflie-circle-endpoint{
      border-color: #C2C5CF;
      overflow: hidden;
      background: #C2C5CF;
      cursor: crosshair;
      &[id^='top']{
        width: 0;
        height: 0;
        border: 0;
        border-radius: 0;
        background: none;
        border-top: 8px solid #C2C5CF;
        border-left: 4px solid transparent;
        border-right: 4px solid transparent;
        border-bottom: 4px solid transparent;
        display: none;
        cursor: unset;
      }
    }
    // 线标签
    .butterflies-label{
      &.hover{
        .edge-label{
          display: none;
        }
        .edge-tool{
          display: flex;
        }
      }
      .edge-label-div{
        display: flex;
      }
      .edge-label{
        width: 16px;
        height: 16px;
        font-size: 12px;
        border-radius: 50%;
        border: 1px dotted #D7D9DE;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        &.show-text{
          width: unset;
          border-radius: 0;
          border: 0;
          .text{
            width: unset;
            border-radius: 0;
            background: none;
            color: #FF194C;
            &.async{
              color: #FF9736;
            }
          }
        }
        .text{
          display: block;
          width: 14px;
          height: 14px;
          color: #fff;
          background-color: #6F7588;
          border-radius: 50%;
          line-height: 14px;
          text-align: center;
          overflow: hidden;
        }
      }
      .edge-tool{
        height: 24px;
        background: #FFFFFF;
        box-shadow: 0px 2px 8px 0px rgba(54,59,76,0.2);
        border-radius: 4px 4px 4px 4px;
        display: none;
        align-items: center;
        overflow: hidden;
        .tool-item{
          width: 24px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          svg{
            width: 16px;
            height: 16px;
          }
          .normal-svg{
            display: block;
          }
          .active-svg{
            display: none;
          }
          .no-formlua{
            display: block;
          }
          .has-formlua{
            display: none;
          }
          &:hover{
            background: #DEEAFF;
            svg{
              fill: #1E6FFF;
            }
            .normal-svg{
              display: none;
            }
            .active-svg{
              display: block;
            }
          }
        }
        .tool-item-formula{
          .no-formlua{
            display: none;
          }
          .has-formlua{
            display: block;
          }
        }
      }
    }
  }
}
@keyframes ring {
  0% {
    stroke-dashoffset: 50;
  }
  100% {
    stroke-dashoffset: 0;
  }
}
</style>
