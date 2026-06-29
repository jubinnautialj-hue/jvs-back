<template>
  <div :class="{'application-relation-map': true, 'application-relation-map-empty': !(relationData && relationData.nodes && relationData.nodes.length > 0)}">
    <div id="appRelationMap">
      <div id="toolbarContainer" :class="{'hide-fish': useFishEye, 'hide-edge-tag': showEdgeTag, 'hide-search': showSearch}"></div>
      <div v-if="showSearch" class="search-node-tool">
        <el-popover
          placement="bottom"
          trigger="manual"
          width="183"
          v-model="searchResultShow"
          popper-class="app-relation-popover">
          <div class="result-list">
            <div v-if="resultNodeList && resultNodeList.length > 0">
              <div v-for="item in resultNodeList" :key="item.id" class="result-list-item" @click="viewNode(item)">
                <span class="text">{{item.name}}</span>
                <span class="type">{{getTypeNameLabel(item)}}</span>
              </div>
            </div>
            <div v-else style="text-align: center;">
              <span>没有匹配项</span>
            </div>
          </div>
          <el-input slot="reference" v-model="keyword" size="mini" @input="searchHandle"></el-input>
        </el-popover>
      </div>
      <div v-if="realNodeTypeColor" style="position: absolute;right: 10px;top: 10px;font-size: 12px;">
        <div v-for="(item, key) in realNodeTypeColor" :key="key" style="display: flex;align-items: center;margin-bottom: 10px;">
          <span :style="`background:${item.activeStroke};display:block;width: 20px;height: 20px;border-radius: 10px;overflow: hidden;'`"></span>
          <span style="margin-left: 10px;">{{key | getTypeLabel}}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import G6 from '@antv/g6';
import insertCss from 'insert-css';
import { isNumber, isArray } from '@antv/util';
import { getAppRelation } from './api'
insertCss(`
  .g6-component-contextmenu {
    position: absolute;
    z-index: 2;
    list-style-type: none;
    background-color: #fafafa; // #363b40;
    border-radius: 6px;
    font-size: 14px;
    color: #aaa; // hsla(0,0%,100%,.85);
    width: fit-content;
    transition: opacity .2s;
    text-align: center;
    padding: 0px 20px 0px 20px;
		box-shadow: 0 5px 18px 0 rgba(0, 0, 0, 0.6);
		border: 0px;
  }
  .g6-component-contextmenu ul {
		padding-left: 0px;
		margin: 0;
  }
  .g6-component-contextmenu li {
    cursor: pointer;
    list-style-type: none;
    list-style: none;
    margin-left: 0;
    line-height: 38px;
  }
  .g6-component-contextmenu li:hover {
    color: #363b40; // #aaaaaa;
	}
`);

const { labelPropagation, louvain, findShortestPath } = G6.Algorithm;
const { uniqueId } = G6.Util;
// 显示的类型名称
const getTypeLabel = (item) => {
  let label = item.designType
  switch(item.designType) {
    case 'app': label = '应用';break;
    case 'chart': label = '图表';break;
    case 'form': label = '表单';break;
    case 'h5': label = 'H5';break;
    case 'URL': label = '自定义页面';break;
    case 'page': label = '列表';break;
    case 'screen': label = '大屏';break;
    case 'workflow': label = '工作流';break;
    case 'data': label = '数据模型';break;
    case 'rule': label = '逻辑引擎';break;
    case 'other': label = '其它';break;
    default: ;break;
  }
  return label
}

const NODESIZEMAPPING = 'degree';
const SMALLGRAPHLABELMAXLENGTH = 5;
let labelMaxLength = SMALLGRAPHLABELMAXLENGTH;
const DEFAULTNODESIZE = 30;
const DEFAULTAGGREGATEDNODESIZE = 53;
const NODE_LIMIT = 40; // TODO: find a proper number for maximum node number on the canvas
const duration = 2000;
const animateOpacity = 0.6;
const animateBackOpacity = 0.1;
const virtualEdgeOpacity = 0.1;
const realEdgeOpacity = 0.2;

const darkBackColor = '#ddd'; // 'rgb(43, 47, 51)';
const disableColor = '#777';
const theme = 'light'; // 'default'; // 'dark';
const subjectColors = [
  '#5F95FF',
  '#61DDAA',
  '#F6BD16',
  '#78D3F8',
  '#F6903D',
  '#9661BC',
  '#65789B',
  '#7262FD',
  '#008685',
  '#F08BB4',
  '#FCECBD'
];

const colorSets = G6.Util.getColorSetsBySubjectColors(
  subjectColors,
  darkBackColor,
  theme,
  disableColor,
);

const global = {
  node: {
    style: {
      fill: '#2B384E',
    },
    labelCfg: {
      style: {
        fill: '#000', // '#acaeaf',
        stroke: '#000', // '#191b1c',
      },
    },
    stateStyles: {
      focus: {
        fill: '#000', // '#2B384E',
        stroke: '#000'
      },
    },
  },
  edge: {
    style: {
      stroke: '#acaeaf',
      realEdgeStroke: '#acaeaf', //'#f00',
      realEdgeOpacity,
      strokeOpacity: realEdgeOpacity,
    },
    labelCfg: {
      style: {
        fill: '#333', // '#acaeaf',
        realEdgeStroke: '#acaeaf', //'#f00',
        realEdgeOpacity: 0.5,
        stroke: '#fafafa', // #191b1c,
      },
    },
    stateStyles: {
      focus: {
        stroke: '#3C9AE8',
      },
    },
  },
};

let fisheye = new G6.Fisheye({
  trigger: 'mousemove',
  d: 2,
  r: 200,
  showLabel: false,
  showDPercent: false
});

let tooltip = new G6.Tooltip({
  offsetX: 10,
  offsetY: 20,
  getContent(e) {
    let model = e.item.getModel()
    return `<div>
      <div>类型：${getTypeLabel(model.extendData)}</div>
      <div>名称：${model.extendData.name}</div>
      <div>id: ${model.extendData.id}</div>
    </div>`;
  },
  itemTypes: ['node']
});

export default {
  props: {
    jvsAppId: {
      type: String
    }
  },
  filters: {
    getTypeLabel (type) {
      let label = type
      switch(type) {
        case 'app': label = '应用';break;
        case 'chart': label = '图表';break;
        case 'form': label = '表单';break;
        case 'h5': label = 'H5';break;
        case 'URL': label = '自定义页面';break;
        case 'page': label = '列表';break;
        case 'screen': label = '大屏';break;
        case 'workflow': label = '工作流';break;
        case 'data': label = '数据模型';break;
        case 'rule': label = '逻辑引擎';break;
        case 'other': label = '其它';break;
        default: ;break;
      }
      return label
    }
  },
  data () {
    return {
      graph: null,
      relationData: {},
      currentUnproccessedData: { nodes: [], edges: [] },
      nodeMap: {},
      aggregatedNodeMap: {},
      hiddenItemIds: [], // 隐藏的元素 id 数组
      largeGraphMode: true,
      cachePositions: {},
      manipulatePosition: undefined,
      descreteNodeCenter: {},
      layout: {
        type: '',
        instance: null,
        destroyed: true,
      },
      expandArray: [],
      collapseArray: [],
      shiftKeydown: false,
      CANVAS_WIDTH: 800,
      CANVAS_HEIGHT: 800,
      showEdgeTag: false,
      useFishEye: false,
      showSearch: false,
      keyword: '',
      searchResultShow: false,
      resultNodeList: [],
      colorSets: colorSets,
      realNodeTypeColor: null
    }
  },
  created () {
    let types = ['app', 'data', 'page', 'form', 'workflow', 'rule', 'URL', 'chart', 'h5', 'screen', 'other']
    this.realNodeTypeColor = {}
    for(let i in types) {
      this.$set(this.realNodeTypeColor, types[i], colorSets[i])
    }
    this.registerInit()
  },
  mounted () {
    if(this.jvsAppId) {
      getAppRelation(this.jvsAppId).then(res => {
        if(res.data && res.data.code == 0) {
          this.relationData = res.data.data
          if(this.relationData.nodes && this.relationData.nodes.length > 0) {
            this.dataInit(this.relationData)
          }
        }
      })
    }
  },
  methods: {
    // 搜索
    searchHandle () {
      if(this.keyword) {
        this.searchResultShow = true
        this.resultNodeList = []
        if(this.relationData && this.relationData.nodes && this.relationData.nodes.length > 0) {
          this.relationData.nodes.filter(node => {
            if(node.name.includes(this.keyword)) {
              this.resultNodeList.push(node)
            }
          })
        }
      }else{
        this.searchResultShow = false
      }
    },
    getTypeNameLabel (item) {
      return getTypeLabel(item)
    },
    // 聚焦节点
    viewNode (node) {
      const realNode = this.graph.findById(node.id)
      if(realNode) {
        this.graph.focusItem(realNode)
        this.graph.setItemState(realNode, 'focus', true);
        if(!this.shiftKeydown) {
          // 将相关边也高亮
          let relatedEdges = realNode.getEdges();
          relatedEdges.forEach((edge) => {
            this.graph.setItemState(edge, 'focus', true);
          });
        }
      }else{
        let nodes = this.currentUnproccessedData.nodes
        let edges = this.currentUnproccessedData.edges
        if(this.aggregatedNodeMap[node.clusterId].nodes && this.aggregatedNodeMap[node.clusterId].nodes.length > 0) {
          this.aggregatedNodeMap[node.clusterId].nodes.filter(nit => {
            if(nit.id == node.id) {
              nodes.push(nit)
            }
          })
        }
        edges.push({source: node.clusterId, target: node.id})
        this.handleRefreshGraph(
          this.graph,
          { nodes, edges },
          this.CANVAS_WIDTH,
          this.CANVAS_HEIGHT,
          this.largeGraphMode,
          true,
          false,
        );
        let real = this.graph.findById(node.id)
        if(real) {
        this.graph.focusItem(real)
        this.graph.setItemState(real, 'focus', true);
        if(!this.shiftKeydown) {
          // 将相关边也高亮
          let relatedEdges = real.getEdges();
          relatedEdges.forEach((edge) => {
            this.graph.setItemState(edge, 'focus', true);
          });
        }
      }
      }
    },
    // 注册节点 线
    registerInit () {
      // 聚合节点
      G6.registerNode(
        'aggregated-node',
        {
          draw(cfg, group) {
            let width = 53,
              height = 27;
            const style = cfg.style || {};
            const colorSet = cfg.colorSet || colorSets[0];

            // hover 外层边框
            group.addShape('rect', {
              attrs: {
                x: -width * 0.55,
                y: -height * 0.6,
                width: width * 1.1,
                height: height * 1.2,
                fill: colorSet.edgeActiveStroke, // mainFill,
                opacity: 0.9,
                lineWidth: 0,
                radius: (height / 2 || 13) * 1.2,
              },
              name: 'halo-shape',
              visible: false,
            });

            // focus 外层边框
            group.addShape('rect', {
              attrs: {
                x: -width * 0.55,
                y: -height * 0.6,
                width: width * 1.1,
                height: height * 1.2,
                fill: '#fff', // colorSet.mainFill
                stroke: colorSet.edgeActiveStroke, // '#AAB7C4',
                lineWidth: 1,
                lineOpacty: 0.85,
                radius: (height / 2 || 13) * 1.2,
              },
              name: 'stroke-shape',
              visible: false,
            });

            // 内容矩形
            const keyShape = group.addShape('rect', {
              attrs: {
                ...style,
                x: -width / 2,
                y: -height / 2,
                width,
                height,
                fill: colorSet.mainStroke, // mainFill || '#3B4043',
                stroke: colorSet.edgeActiveStroke, // mainStroke
                lineWidth: 2,
                cursor: 'pointer',
                radius: height / 2 || 13,
                lineDash: [2, 2],
              },
              name: 'aggregated-node-keyShape',
            });

            let labelStyle = {};
            if (cfg.labelCfg) {
              labelStyle = Object.assign(labelStyle, cfg.labelCfg.style);
            }
            group.addShape('text', {
              attrs: {
                text: `${cfg.count}`,
                x: 0,
                y: 0,
                textAlign: 'center',
                textBaseline: 'middle',
                cursor: 'pointer',
                fontSize: 12,
                fill: '#fff',
                opacity: 0.85,
                fontWeight: 400,
              },
              name: 'count-shape',
              className: 'count-shape',
              draggable: true,
            });

            // tag for new node
            // if(cfg.new) {
            //   group.addShape('circle', {
            //     attrs: {
            //       x: width / 2 - 3,
            //       y: -height / 2 + 3,
            //       r: 4,
            //       fill: '#6DD400',
            //       lineWidth: 0.5,
            //       stroke: '#FFFFFF',
            //     },
            //     name: 'typeNode-tag-circle',
            //   });
            // }
            return keyShape;
          },
          setState: (name, value, item) => {
            const group = item.get('group');
            if (name === 'layoutEnd' && value) {
              const labelShape = group.find((e) => e.get('name') === 'text-shape');
              if (labelShape) labelShape.set('visible', true);
            } else if (name === 'hover') {
              if (item.hasState('focus')) {
                return;
              }
              const halo = group.find((e) => e.get('name') === 'halo-shape');
              const keyShape = item.getKeyShape();
              const colorSet = item.getModel().colorSet || colorSets[0];
              if (value) {
                halo && halo.show();
                keyShape.attr('fill', colorSet.mainStroke); // activeFill
                keyShape.attr('stroke', '#fff') // light主题
              } else {
                halo && halo.hide();
                keyShape.attr('fill', colorSet.mainStroke); // mainFill
                keyShape.attr('stroke', colorSet.edgeActiveStroke) // light主题
              }
            } else if (name === 'focus') {
              const stroke = group.find((e) => e.get('name') === 'stroke-shape');
              const keyShape = item.getKeyShape();
              const colorSet = item.getModel().colorSet || colorSets[0];
              if (value) {
                stroke && stroke.show();
                keyShape.attr('fill', colorSet.activeStroke); // selectedFill
              } else {
                stroke && stroke.hide();
                keyShape.attr('fill', colorSet.activeStroke); // mainFill
              }
            }
          },
          update: undefined,
        },
        'single-node',
      );

      // 真实节点
      G6.registerNode(
        'real-node',
        {
          draw(cfg, group) {
            let r = 30;
            if (isNumber(cfg.size)) {
              r = cfg.size / 2;
            } else if (isArray(cfg.size)) {
              r = cfg.size[0] / 2;
            }
            const style = cfg.style || {};
            const colorSet = cfg.colorSet || colorSets[0];
            // hover 外层边框
            group.addShape('circle', {
              attrs: {
                x: 0,
                y: 0,
                r: r + 5,
                fill: style.fill || colorSet.edgeActiveStroke || colorSet.mainFill || '#2B384E',
                opacity: 1,
                lineWidth: 0,
              },
              name: 'halo-shape',
              visible: false,
            });

            // focus 外层边框
            group.addShape('circle', {
              attrs: {
                x: 0,
                y: 0,
                r: r + 5,
                fill: style.fill || colorSet.mainStroke || colorSet.mainFill || '#2B384E',
                stroke: colorSet.edgeActiveStroke, // '#fff'
                strokeOpacity: 1,
                lineWidth: 1,
              },
              name: 'stroke-shape',
              visible: false,
            });

            group.addShape('text', {
              attrs: {
                x: 0,
                y: 0,
                fontFamily: 'icon-setting',
                textAlign: 'center',
                textBaseline: 'middle'
              },
              name: 'stroke-shape',
              visible: false,
            });
            // 内容圆形
            const keyShape = group.addShape('circle', {
              attrs: {
                ...style,
                x: 0,
                y: 0,
                r,
                fill: colorSet.mainStroke, // mainFill,
                stroke: colorSet.edgeActiveStroke, // mainStroke,
                lineWidth: 2,
                cursor: 'pointer',
              },
              name: 'aggregated-node-keyShape',
            });

            let labelStyle = {};
            if (cfg.labelCfg) {
              labelStyle = Object.assign(labelStyle, cfg.labelCfg.style);
            }

            if (cfg.label) {
              const text = cfg.label;
              let labelStyle = {};
              let refY = 0;
              if (cfg.labelCfg) {
                labelStyle = Object.assign(labelStyle, cfg.labelCfg.style);
                refY += cfg.labelCfg.refY || 0;
              }
              let offsetY = 0;
              const fontSize = labelStyle.fontSize < 8 ? 8 : labelStyle.fontSize;
              const lineNum = cfg.labelLineNum || 1;
              offsetY = lineNum * (fontSize || 12);
              group.addShape('text', {
                attrs: {
                  text,
                  x: 0,
                  y: r + refY + offsetY + 5,
                  textAlign: 'center',
                  textBaseLine: 'alphabetic',
                  cursor: 'pointer',
                  fontSize,
                  fill: colorSet.edgeActiveStroke, // '#fff',
                  opacity: 0.85,
                  fontWeight: 400,
                  stroke: global.edge.labelCfg.style.stroke,
                },
                name: 'text-shape',
                className: 'text-shape',
              });
            }
            return keyShape;
          },
          setState: (name, value, item) => {
            const group = item.get('group');
            if (name === 'layoutEnd' && value) {
              const labelShape = group.find((e) => e.get('name') === 'text-shape');
              if (labelShape) labelShape.set('visible', true);
            } else if (name === 'hover') {
              if (item.hasState('focus')) {
                return;
              }
              const halo = group.find((e) => e.get('name') === 'halo-shape');
              const keyShape = item.getKeyShape();
              const colorSet = item.getModel().colorSet || colorSets[0];
              if (value) {
                halo && halo.show();
                keyShape.attr('fill', colorSet.mainStroke); // activeFill
                keyShape.attr('stroke', '#fff'); //  light主题
              } else {
                halo && halo.hide();
                keyShape.attr('fill', colorSet.mainStroke); // mainFill
                keyShape.attr('stroke', colorSet.edgeActiveStroke); //  light主题
              }
            } else if (name === 'focus') {
              const stroke = group.find((e) => e.get('name') === 'stroke-shape');
              const label = group.find((e) => e.get('name') === 'text-shape');
              const keyShape = item.getKeyShape();
              const colorSet = item.getModel().colorSet || colorSets[0];
              if (value) {
                stroke && stroke.show();
                keyShape.attr('fill', colorSet.edgeActiveStroke); // selectedFill
                keyShape.attr('stroke', '#fff'); //  light主题
                label && label.attr('fontWeight', 800);
              } else {
                stroke && stroke.hide();
                keyShape.attr('fill', colorSet.mainStroke); // mainFill '#2B384E'
                keyShape.attr('stroke', colorSet.edgeActiveStroke); //  light主题
                label && label.attr('fontWeight', 400);
              }
            }
          },
          update: undefined,
        },
        'aggregated-node',
      ); // 这样可以继承 aggregated-node 的 setState

      // 真实节点连线
      G6.registerEdge(
        'custom-quadratic',
        {
          setState: (name, value, item) => {
            const group = item.get('group');
            const model = item.getModel();
            const sourceNodeModel = item.getSource().getModel()
            if(name === 'focus') {
              const back = group.find((ele) => ele.get('name') === 'back-line');
              if(back) {
                back.stopAnimate();
                back.remove();
                back.destroy();
              }
              const keyShape = group.find((ele) => ele.get('name') === 'edge-shape');
              const arrow = model.style.endArrow;
              if(value) {
                if (keyShape.cfg.animation) {
                  keyShape.stopAnimate(true);
                }
                keyShape.attr({
                  strokeOpacity: animateOpacity,
                  opacity: animateOpacity,
                  stroke: sourceNodeModel.colorSet.mainStroke, // '#fff'
                  endArrow: {
                    ...arrow,
                    stroke: '#fff',
                    fill: sourceNodeModel.colorSet.mainStroke, // '#fff'
                  },
                });
                if(model.isReal) {
                  const { lineWidth, path, endArrow, stroke } = keyShape.attr();
                  const back = group.addShape('path', {
                    attrs: {
                      lineWidth,
                      path,
                      stroke,
                      endArrow,
                      opacity: animateBackOpacity,
                    },
                    name: 'back-line',
                  });
                  back.toBack();
                  const length = keyShape.getTotalLength();
                  keyShape.animate( (ratio) => {
                    const startLen = ratio * length;
                    const cfg = { lineDash: [startLen, length - startLen] };
                    return cfg;
                  },
                  {
                    repeat: true,
                    duration
                  });
                }else {
                  let index = 0;
                  const lineDash = keyShape.attr('lineDash');
                  const totalLength = lineDash[0] + lineDash[1];
                  keyShape.animate( () => {
                    index++;
                    if (index > totalLength) {
                      index = 0;
                    }
                    const res = {
                      lineDash,
                      lineDashOffset: -(index + 10),
                    };
                    return res;
                  },
                  {
                    repeat: true,
                    duration,
                  });
                }
              } else {
                keyShape.stopAnimate();
                const stroke = '#7e8183' // '#acaeaf'
                const opacity = model.isReal ? realEdgeOpacity : virtualEdgeOpacity;
                keyShape.attr({
                  stroke,
                  strokeOpacity: opacity,
                  opacity,
                  endArrow: {
                    ...arrow,
                    stroke,
                    fill: stroke,
                  },
                });
              }
            }
          },
        },
        'quadratic',
      );
      // 聚合连线
      G6.registerEdge(
        'custom-line',
        {
          setState: (name, value, item) => {
            const group = item.get('group');
            const model = item.getModel();
            const sourceNodeModel = item.getSource().getModel()
            if(name === 'focus') {
              const keyShape = group.find((ele) => ele.get('name') === 'edge-shape');
              const back = group.find((ele) => ele.get('name') === 'back-line');
              if(back) {
                back.stopAnimate();
                back.remove();
                back.destroy();
              }
              const arrow = model.style.endArrow;
              if(value) {
                if(keyShape.cfg.animation) {
                  keyShape.stopAnimate(true);
                }
                keyShape.attr({
                  strokeOpacity: animateOpacity,
                  opacity: animateOpacity,
                  stroke: sourceNodeModel.colorSet.mainStroke, // '#fff'
                  endArrow: {
                    ...arrow,
                    stroke: '#fff',
                    fill: sourceNodeModel.colorSet.mainStroke, // '#fff'
                  },
                });
                if(model.isReal) {
                  const { path, stroke, lineWidth } = keyShape.attr();
                  const back = group.addShape('path', {
                    attrs: {
                      path,
                      stroke,
                      lineWidth,
                      opacity: animateBackOpacity,
                    },
                    name: 'back-line',
                  });
                  back.toBack();
                  const length = keyShape.getTotalLength();
                  keyShape.animate( (ratio) => {
                    const startLen = ratio * length;
                    const cfg = {
                      lineDash: [startLen, length - startLen],
                    };
                    return cfg;
                  },
                  {
                    repeat: true,
                    duration,
                  });
                }else {
                  let lineDash = keyShape.attr('lineDash');
                  let totalLength = lineDash[0] + lineDash[1];
                  let index = 0;
                  keyShape.animate( () => {
                    index++;
                    if (index > totalLength) {
                      index = 0;
                    }
                    let res = {
                      lineDash,
                      lineDashOffset: -index,
                    };
                    return res;
                  },
                  {
                    repeat: true,
                    duration
                  });
                }
              }else {
                const stroke = '#7e8183' // '#acaeaf'
                const opacity = model.isReal ? realEdgeOpacity : virtualEdgeOpacity;
                if(keyShape.cfg.animation) {
                  keyShape.stopAnimate(true);
                }
                keyShape.attr({
                  stroke,
                  strokeOpacity: opacity,
                  opacity: opacity,
                  endArrow: {
                    ...arrow,
                    stroke,
                    fill: stroke,
                  },
                });
                
              }
            }
          },
        },
        'single-edge',
      );
    },
    // 渲染数据
    dataInit (data) {
      let _this = this
      const container = document.getElementById('appRelationMap');
      container.style.backgroundColor = '#fafafa'; // '#2b2f33';
      this.CANVAS_WIDTH = container.scrollWidth;
      this.CANVAS_HEIGHT = container.scrollHeight || 500;

      this.nodeMap = {};
      const clusteredData = this.initData(louvain(data, false, 'weight'), data);
      const aggregatedData = { nodes: [], edges: [] };
      clusteredData.clusters.forEach((cluster, i) => {
        cluster.nodes.forEach((node) => {
          node.level = 0;
          node.label = node.extendData ? node.extendData.name : node.id;
          node.type = '';
          node.colorSet = (node.extendData && node.extendData.designType && this.realNodeTypeColor) ? this.realNodeTypeColor[node.extendData.designType] : colorSets[i];
          console.log(node)
          this.nodeMap[node.id] = node;
        });
        let joinColorSet = G6.Util.getColorSetsBySubjectColors(['#acaeaf'], darkBackColor, theme, disableColor)
        const cnode = {
          id: cluster.id,
          type: 'aggregated-node',
          count: cluster.nodes.length,
          level: 1,
          label: cluster.id,
          colorSet: joinColorSet[0], // colorSets[i],
          idx: i,
          nodes: cluster.nodes
        };
        this.aggregatedNodeMap[cluster.id] = cnode;
        if(cluster.nodes.length > 1) {
          aggregatedData.nodes.push(cnode);
        }else{
          aggregatedData.nodes = aggregatedData.nodes.concat(cluster.nodes)
        }
      });
      clusteredData.clusterEdges.forEach((clusterEdge) => {
        const cedge = {
          ...clusterEdge,
          size: 2, // Math.log(clusterEdge.count),
          label: '',
          id: `${uniqueId('edge')}`,
        };
        if(cedge.source === cedge.target) {
          cedge.type = 'loop';
          cedge.loopCfg = {
            dist: 20,
          };
        }else{
          cedge.type = 'line'
        }
        aggregatedData.edges.push(cedge)
      });

      data.edges.forEach((edge) => {
        edge.label = `${edge.relationType}`
        edge.id = `${uniqueId('edge')}`
      });

      this.currentUnproccessedData = aggregatedData;

      const { edges: processedEdges } = this.processNodesEdges(
        this.currentUnproccessedData.nodes,
        this.currentUnproccessedData.edges,
        this.CANVAS_WIDTH,
        this.CANVAS_HEIGHT,
        this.largeGraphMode,
        true,
        true,
      );

      const contextMenu = new G6.Menu({
        shouldBegin(evt) {
          if (evt.target && evt.target.isCanvas && evt.target.isCanvas()) return true;
          if (evt.item) return true;
          return false;
        },
        getContent(evt) {
          const { item } = evt;
          if (evt.target && evt.target.isCanvas && evt.target.isCanvas()) {
            return `<ul>
              <li id='show'>显示所有隐藏项</li>
              <li id='collapseAll'>收起</li>
            </ul>`;
          } else if (!item) return;
          const itemType = item.getType();
          const model = item.getModel();
          if (itemType && model) {
            if (itemType === 'node') {
              if (model.level !== 0) {
                return `<ul>
                  <li id='expand'>展开</li>
                  <li id='hide'>隐藏</li>
                </ul>`;
              } else {
                // <li id='neighbor-1'>Find 1-degree Neighbors</li>
                // <li id='neighbor-2'>Find 2-degree Neighbors</li>
                // <li id='neighbor-3'>Find 3-degree Neighbors</li>
                if(_this.aggregatedNodeMap[model.clusterId].count > 1) {
                  if(['page', 'form', 'workflow', 'rule'].indexOf(model.extendData.designType) == -1){
                    return `<ul>
                      <li id='collapse'>收起</li>
                      <li id='hide'>隐藏</li>
                    </ul>`;
                  }else{
                    return `<ul>
                      <li id='design'>设计</li>
                      <li id='collapse'>收起</li>
                      <li id='hide'>隐藏</li>
                    </ul>`;
                  }
                }else{
                  if(['page', 'form', 'workflow', 'rule'].indexOf(model.extendData.designType) == -1){
                    return `<ul>
                      <li id='design'>设计</li>
                      <li id='hide'>隐藏</li>
                    </ul>`;
                  }else{
                    return `<ul>
                      <li id='design'>设计</li>
                      <li id='hide'>隐藏</li>
                    </ul>`;
                  }
                }
              }
            } else {
              return `<ul>
                <li id='hide'>隐藏</li>
              </ul>`;
            }
          }
        },
        handleMenuClick: (target, item) => {
          const model = item && item.getModel();
          const liIdStrs = target.id.split('-');
          let mixedGraphData;
          switch (liIdStrs[0]) {
            case 'hide':
              this.graph.hideItem(item);
              this.hiddenItemIds.push(model.id);
              break;
            case 'expand':
              const newArray = this.manageExpandCollapseArray(
                this.graph.getNodes().length,
                model,
                this.collapseArray,
                this.expandArray,
              );
              this.expandArray = newArray.expandArray;
              this.collapseArray = newArray.collapseArray;
              mixedGraphData = this.getMixedGraph(
                clusteredData,
                data,
                this.nodeMap,
                this.aggregatedNodeMap,
                this.expandArray,
                this.collapseArray,
              );
              break;
            case 'collapse':
              const aggregatedNode = this.aggregatedNodeMap[model.clusterId];
              this.manipulatePosition = { x: aggregatedNode.x, y: aggregatedNode.y };
              this.collapseArray.push(aggregatedNode);
              for(let i = 0; i < this.expandArray.length; i++) {
                if (this.expandArray[i].id === model.clusterId) {
                  this.expandArray.splice(i, 1);
                  break;
                }
              }
              mixedGraphData = this.getMixedGraph(
                clusteredData,
                data,
                this.nodeMap,
                this.aggregatedNodeMap,
                this.expandArray,
                this.collapseArray,
              );
              break;
            case 'collapseAll':
              this.expandArray = [];
              this.collapseArray = [];
              mixedGraphData = this.getMixedGraph(
                clusteredData,
                data,
                this.nodeMap,
                this.aggregatedNodeMap,
                this.expandArray,
                this.collapseArray,
              );
              break;
            case 'neighbor':
              const expandNeighborSteps = parseInt(liIdStrs[1]);
              mixedGraphData = this.getNeighborMixedGraph(
                model,
                expandNeighborSteps,
                data,
                clusteredData,
                this.currentUnproccessedData,
                this.nodeMap,
                this.aggregatedNodeMap,
                10,
              );
              break;
            case 'show':
              this.showItems(this.graph);
              break;
            case 'design':
              this.handleDesign(model.extendData);
              break;
            default:
              break;
          }
          if (mixedGraphData) {
            this.cachePositions = this.cacheNodePositions(this.graph.getNodes());
            this.currentUnproccessedData = mixedGraphData;
            this.handleRefreshGraph(
              this.graph,
              this.currentUnproccessedData,
              this.CANVAS_WIDTH,
              this.CANVAS_HEIGHT,
              this.largeGraphMode,
              true,
              false,
            );
          }
        },
        // offsetX and offsetY include the padding of the parent container
        // 需要加上父级容器的 padding-left 16 与自身偏移量 10
        offsetX: 16 + 10,
        // 需要加上父级容器的 padding-top 24 、画布兄弟元素高度、与自身偏移量 10
        offsetY: 0,
        // the types of items that allow the menu show up
        // 在哪些类型的元素上响应
        itemTypes: ['node', 'edge', 'canvas'],
      });
    
      const toolbar = new G6.ToolBar({
        container: document.getElementById('toolbarContainer'),
        getContent: () => {
          return `
            <ul>
              <li code='edgeTag' title="显示边标签" class="viewLineTag">
                <svg class="icon" viewBox="64 64 896 896" version="1.1" xmlns="http://www.w3.org/2000/svg" width="20" height="24">
                  <path d="M938 458.8l-29.6-312.6c-1.5-16.2-14.4-29-30.6-30.6L565.2 86h-.4c-3.2 0-5.7 1-7.6 2.9L88.9 557.2a9.96 9.96 0 000 14.1l363.8 363.8c1.9 1.9 4.4 2.9 7.1 2.9s5.2-1 7.1-2.9l468.3-468.3c2-2.1 3-5 2.8-8zM459.7 834.7L189.3 564.3 589 164.6 836 188l23.4 247-399.7 399.7zM680 256c-48.5 0-88 39.5-88 88s39.5 88 88 88 88-39.5 88-88-39.5-88-88-88zm0 120c-17.7 0-32-14.3-32-32s14.3-32 32-32 32 14.3 32 32-14.3 32-32 32z"></path>
                </svg>
              </li>
              <li code='closeEdgeTag' title="隐藏边标签" class="clearLineTag">
                <svg class="icon" viewBox="64 64 896 896" version="1.1" xmlns="http://www.w3.org/2000/svg" width="20" height="24">
                  <path d="M832.6 191.4c-84.6-84.6-221.5-84.6-306 0l-96.9 96.9 51 51 96.9-96.9c53.8-53.8 144.6-59.5 204 0 59.5 59.5 53.8 150.2 0 204l-96.9 96.9 51.1 51.1 96.9-96.9c84.4-84.6 84.4-221.5-.1-306.1zM446.5 781.6c-53.8 53.8-144.6 59.5-204 0-59.5-59.5-53.8-150.2 0-204l96.9-96.9-51.1-51.1-96.9 96.9c-84.6 84.6-84.6 221.5 0 306s221.5 84.6 306 0l96.9-96.9-51-51-96.8 97zM260.3 209.4a8.03 8.03 0 00-11.3 0L209.4 249a8.03 8.03 0 000 11.3l554.4 554.4c3.1 3.1 8.2 3.1 11.3 0l39.6-39.6c3.1-3.1 3.1-8.2 0-11.3L260.3 209.4z"></path>
                </svg>
              </li>
              <li code='fisheye' title="打开鱼眼放大器" class="viewFish">
                <svg class="icon" viewBox="64 64 896 896" version="1.1" xmlns="http://www.w3.org/2000/svg" width="20" height="24">
                  <path d="M942.2 486.2C847.4 286.5 704.1 186 512 186c-192.2 0-335.4 100.5-430.2 300.3a60.3 60.3 0 000 51.5C176.6 737.5 319.9 838 512 838c192.2 0 335.4-100.5 430.2-300.3 7.7-16.2 7.7-35 0-51.5zM512 766c-161.3 0-279.4-81.8-362.7-254C232.6 339.8 350.7 258 512 258c161.3 0 279.4 81.8 362.7 254C791.5 684.2 673.4 766 512 766zm-4-430c-97.2 0-176 78.8-176 176s78.8 176 176 176 176-78.8 176-176-78.8-176-176-176zm0 288c-61.9 0-112-50.1-112-112s50.1-112 112-112 112 50.1 112 112-50.1 112-112 112z"></path>
                </svg>
              </li>
              <li code='closefisheye' title="关闭鱼眼放大器" class="clearFish">
                <svg class="icon" viewBox="64 64 896 896" version="1.1" xmlns="http://www.w3.org/2000/svg" width="20" height="24">
                  <path d="M942.2 486.2Q889.47 375.11 816.7 305l-50.88 50.88C807.31 395.53 843.45 447.4 874.7 512 791.5 684.2 673.4 766 512 766q-72.67 0-133.87-22.38L323 798.75Q408 838 512 838q288.3 0 430.2-300.3a60.29 60.29 0 000-51.5zm-63.57-320.64L836 122.88a8 8 0 00-11.32 0L715.31 232.2Q624.86 186 512 186q-288.3 0-430.2 300.3a60.3 60.3 0 000 51.5q56.69 119.4 136.5 191.41L112.48 835a8 8 0 000 11.31L155.17 889a8 8 0 0011.31 0l712.15-712.12a8 8 0 000-11.32zM149.3 512C232.6 339.8 350.7 258 512 258c54.54 0 104.13 9.36 149.12 28.39l-70.3 70.3a176 176 0 00-238.13 238.13l-83.42 83.42C223.1 637.49 183.3 582.28 149.3 512zm246.7 0a112.11 112.11 0 01146.2-106.69L401.31 546.2A112 112 0 01396 512z"></path><path d="M508 624c-3.46 0-6.87-.16-10.25-.47l-52.82 52.82a176.09 176.09 0 00227.42-227.42l-52.82 52.82c.31 3.38.47 6.79.47 10.25a111.94 111.94 0 01-112 112z"></path>
                </svg>
              </li>
              <li code='zoomIn' title="缩小" class="zoom-tool-in">
                <span class="li-icon-span">-</span>
                <!-- <svg class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="24" height="24">
                  <path d="M639.936 416a32 32 0 0 1-32 32h-256a32 32 0 0 1 0-64h256a32 32 0 0 1 32 32z m289.28 503.552a41.792 41.792 0 0 1-58.752-6.656l-182.656-213.248A349.76 349.76 0 0 1 480 768 352 352 0 1 1 832 416a350.4 350.4 0 0 1-83.84 227.712l185.664 216.768a41.856 41.856 0 0 1-4.608 59.072zM479.936 704c158.784 0 288-129.216 288-288S638.72 128 479.936 128a288.32 288.32 0 0 0-288 288c0 158.784 129.216 288 288 288z" p-id="3853"></path>
                </svg> -->
              </li>
              <li code='autoZoom' title="图内容适配容器">
                <svg class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="20" height="24">
                  <path d="M684.288 305.28l0.128-0.64-0.128-0.64V99.712c0-19.84 15.552-35.904 34.496-35.712a35.072 35.072 0 0 1 34.56 35.776v171.008h170.944c19.648 0 35.84 15.488 35.712 34.432a35.072 35.072 0 0 1-35.84 34.496h-204.16l-0.64-0.128a32.768 32.768 0 0 1-20.864-7.552c-1.344-1.024-2.816-1.664-3.968-2.816-0.384-0.32-0.512-0.768-0.832-1.088a33.472 33.472 0 0 1-9.408-22.848zM305.28 64a35.072 35.072 0 0 0-34.56 35.776v171.008H99.776A35.072 35.072 0 0 0 64 305.216c0 18.944 15.872 34.496 35.84 34.496h204.16l0.64-0.128a32.896 32.896 0 0 0 20.864-7.552c1.344-1.024 2.816-1.664 3.904-2.816 0.384-0.32 0.512-0.768 0.768-1.088a33.024 33.024 0 0 0 9.536-22.848l-0.128-0.64 0.128-0.704V99.712A35.008 35.008 0 0 0 305.216 64z m618.944 620.288h-204.16l-0.64 0.128-0.512-0.128c-7.808 0-14.72 3.2-20.48 7.68-1.28 1.024-2.752 1.664-3.84 2.752-0.384 0.32-0.512 0.768-0.832 1.088a33.664 33.664 0 0 0-9.408 22.912l0.128 0.64-0.128 0.704v204.288c0 19.712 15.552 35.904 34.496 35.712a35.072 35.072 0 0 0 34.56-35.776V753.28h170.944c19.648 0 35.84-15.488 35.712-34.432a35.072 35.072 0 0 0-35.84-34.496z m-593.92 11.52c-0.256-0.32-0.384-0.768-0.768-1.088-1.088-1.088-2.56-1.728-3.84-2.688a33.088 33.088 0 0 0-20.48-7.68l-0.512 0.064-0.64-0.128H99.84a35.072 35.072 0 0 0-35.84 34.496 35.072 35.072 0 0 0 35.712 34.432H270.72v171.008c0 19.84 15.552 35.84 34.56 35.776a35.008 35.008 0 0 0 34.432-35.712V720l-0.128-0.64 0.128-0.704a33.344 33.344 0 0 0-9.472-22.848zM512 374.144a137.92 137.92 0 1 0 0.128 275.84A137.92 137.92 0 0 0 512 374.08z"></path>
                </svg>
              </li>
              <li code='zoomOut' title="放大" class="zoom-tool-out">
                <span class="li-icon-span">+</span>
                <!-- <svg class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="24" height="24">
                  <path d="M658.432 428.736a33.216 33.216 0 0 1-33.152 33.152H525.824v99.456a33.216 33.216 0 0 1-66.304 0V461.888H360.064a33.152 33.152 0 0 1 0-66.304H459.52V296.128a33.152 33.152 0 0 1 66.304 0V395.52H625.28c18.24 0 33.152 14.848 33.152 33.152z m299.776 521.792a43.328 43.328 0 0 1-60.864-6.912l-189.248-220.992a362.368 362.368 0 0 1-215.36 70.848 364.8 364.8 0 1 1 364.8-364.736 363.072 363.072 0 0 1-86.912 235.968l192.384 224.64a43.392 43.392 0 0 1-4.8 61.184z m-465.536-223.36a298.816 298.816 0 0 0 298.432-298.432 298.816 298.816 0 0 0-298.432-298.432A298.816 298.816 0 0 0 194.24 428.8a298.816 298.816 0 0 0 298.432 298.432z"></path>
                </svg> -->
              </li>
              <!-- <li code='realZoom' title="初始比例">
                <svg class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="20" height="24">
                  <path d="M384 320v384H320V320h64z m256 0v384H576V320h64zM512 576v64H448V576h64z m0-192v64H448V384h64z m355.968 576H92.032A28.16 28.16 0 0 1 64 931.968V28.032C64 12.608 76.608 0 95.168 0h610.368L896 192v739.968a28.16 28.16 0 0 1-28.032 28.032zM704 64v128h128l-128-128z m128 192h-190.464V64H128v832h704V256z"></path>
                </svg>
              </li> -->
              <li code='searchNode' title="输入名称搜索节点" class="viewSearch">
                <svg class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="20" height="24">
                  <path d="M909.6 854.5L649.9 594.8C690.2 542.7 712 479 712 412c0-80.2-31.3-155.4-87.9-212.1-56.6-56.7-132-87.9-212.1-87.9s-155.5 31.3-212.1 87.9C143.2 256.5 112 331.8 112 412c0 80.1 31.3 155.5 87.9 212.1C256.5 680.8 331.8 712 412 712c67 0 130.6-21.8 182.7-62l259.7 259.6a8.2 8.2 0 0011.6 0l43.6-43.5a8.2 8.2 0 000-11.6zM570.4 570.4C528 612.7 471.8 636 412 636s-116-23.3-158.4-65.6C211.3 528 188 471.8 188 412s23.3-116.1 65.6-158.4C296 211.3 352.2 188 412 188s116.1 23.2 158.4 65.6S636 352.2 636 412s-23.3 116.1-65.6 158.4z"></path>
                </svg>
              </li>
              <li code='closeSearch' title="关闭搜索" class="clearSearch">
                <svg class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="20" height="24">
                  <path d="M909.6 854.5L649.9 594.8C690.2 542.7 712 479 712 412c0-80.2-31.3-155.4-87.9-212.1-56.6-56.7-132-87.9-212.1-87.9s-155.5 31.3-212.1 87.9C143.2 256.5 112 331.8 112 412c0 80.1 31.3 155.5 87.9 212.1C256.5 680.8 331.8 712 412 712c67 0 130.6-21.8 182.7-62l259.7 259.6a8.2 8.2 0 0011.6 0l43.6-43.5a8.2 8.2 0 000-11.6zM570.4 570.4C528 612.7 471.8 636 412 636s-116-23.3-158.4-65.6C211.3 528 188 471.8 188 412s23.3-116.1 65.6-158.4C296 211.3 352.2 188 412 188s116.1 23.2 158.4 65.6S636 352.2 636 412s-23.3 116.1-65.6 158.4z"></path>
                </svg>
              </li>
            </ul>
          `
        },
        handleClick: (code, graph) => {
          this.stopLayout()
          if(code == 'edgeTag') {
            graph.getEdges().filter(item => {
              let model = item.getModel();
              let currentLabel = model.label;
              item.update({
                label: model.oriLabel,
              });
              model.oriLabel = currentLabel;
              item.toFront();
              item.getSource().toFront();
              item.getTarget().toFront();
            })
            this.showEdgeTag = true
          }else if(code == 'closeEdgeTag'){
            graph.getEdges().filter(item => {
              let model = item.getModel();
              let currentLabel = model.label;
              item.update({
                label: model.oriLabel,
              });
              model.oriLabel = currentLabel;
            })
            this.showEdgeTag = false
          }else if(code == 'zoomOut') {
            toolbar.zoomOut()
          }else if(code == 'zoomIn') {
            toolbar.zoomIn()
          }else if(code == 'realZoom') {
            toolbar.realZoom()
          }else if(code == 'autoZoom') {
            toolbar.autoZoom()
          }else if(code == 'fisheye') {
            if(fisheye.destroyed) {
              fisheye = new G6.Fisheye({
                trigger: 'mousemove',
                d: 2,
                r: 200,
                showLabel: false,
                showDPercent: false
              })
            }
            graph.addPlugin(fisheye)
            this.useFishEye = true
          }else if(code == 'closefisheye') {
            graph.removePlugin(fisheye)
            this.useFishEye = false
          }else if(code == 'searchNode') {
            this.showSearch = true
          }else if(code == 'closeSearch') {
            this.showSearch = false
          }
        }
      });

      this.graph = new G6.Graph({
        container: 'appRelationMap',
        width: this.CANVAS_WIDTH,
        height: this.CANVAS_HEIGHT,
        linkCenter: true,
        minZoom: 0.1,
        groupByTypes: false,
        modes: {
          default: [
            {
              type: 'drag-canvas',
              enableOptimize: true,
            },
            {
              type: 'zoom-canvas',
              enableOptimize: true,
              optimizeZoom: 0.01,
            },
            'drag-node',
            'shortcuts-call',
          ],
          lassoSelect: [
            {
              type: 'zoom-canvas',
              enableOptimize: true,
              optimizeZoom: 0.01,
            },
            {
              type: 'lasso-select',
              selectedState: 'focus',
              trigger: 'drag',
            },
          ],
          fisheyeMode: [],
        },
        defaultNode: {
          type: 'aggregated-node',
          size: DEFAULTNODESIZE,
        },
        plugins: [contextMenu, toolbar]
      });

      this.graph.get('canvas').set('localRefresh', false);

      const layoutConfig = this.getForceLayoutConfig(this.graph, this.largeGraphMode);
      layoutConfig.center = [this.CANVAS_WIDTH / 2, this.CANVAS_HEIGHT / 2];
      this.layout.instance = new G6.Layout['gForce'](layoutConfig);
      this.layout.instance.init({
        nodes: this.currentUnproccessedData.nodes,
        edges: processedEdges,
      });
      this.layout.instance.animate = false
      this.layout.instance.execute();

      this.bindListener(this.graph);
      this.graph.data({ nodes: aggregatedData.nodes, edges: processedEdges });
      this.graph.render();
    },
    // 数据处理
    initData (deal, data) {
      deal.clusters.filter(clt => {
        if(clt.nodes && clt.nodes.length > 0) {
          clt.nodes.filter(nit => {
            nit.extendData = this.getResData(nit.id, 'nodes', data)
          })
        }
      })
      return deal
    },
    // 获取数据值
    getResData (id, type, data) {
      let temp = null
      for(let i in data[type]) {
        if(data[type][i].id == id) {
          temp = data[type][i]
        }
      }
      return temp
    },
    // 比较函数
    descendCompare (p) {
      return function (m, n) {
        const a = m[p];
        const b = n[p];
        return b - a; // 降序
      }
    },
    clearFocusItemState (graph) {
      if (!graph) return;
      this.clearFocusNodeState(graph)
      this.clearFocusEdgeState(graph)
    },
    // 清除图上所有节点的 focus 状态及相应样式
    clearFocusNodeState (graph) {
      const focusNodes = graph.findAllByState('node', 'focus');
      focusNodes.forEach((fnode) => {
        graph.setItemState(fnode, 'focus', false); // false
      });
    },
    // 清除图上所有边的 focus 状态及相应样式
    clearFocusEdgeState (graph) {
      const focusEdges = graph.findAllByState('edge', 'focus');
      focusEdges.forEach((fedge) => {
        graph.setItemState(fedge, 'focus', false);
      });
    },
    // 截断长文本。length 为文本截断后长度，elipsis 是后缀
    formatText (text, length = 5, elipsis = '...') {
      if (!text) return '';
      if (text.length > length) {
        return `${text.substr(0, length)}${elipsis}`;
      }
      return text;
    },
    labelFormatter (text, minLength = 10) {
      if (text && text.split('').length > minLength) return `${text.substr(0, minLength)}...`;
      return text;
    },
    processNodesEdges (nodes, edges, width, height, largeGraphMode, edgeLabelVisible, isNewGraph = false) {
      if (!nodes || nodes.length === 0) return {};
      const currentNodeMap = {};
      let maxNodeCount = -Infinity;
      const paddingRatio = 0.3;
      const paddingLeft = paddingRatio * width;
      const paddingTop = paddingRatio * height;
      nodes.forEach((node) => {
        node.type = node.level === 0 ? 'real-node' : 'aggregated-node';
        node.isReal = node.level === 0 ? true : false;
        node.label = `${node.label}`;
        node.labelLineNum = undefined;
        node.oriLabel = node.label;
        node.label = this.formatText(node.label, labelMaxLength, '...');
        node.degree = 0;
        node.inDegree = 0;
        node.outDegree = 0;
        if (currentNodeMap[node.id]) {
          console.warn('node exists already!', node.id);
          node.id = `${node.id}${Math.random()}`;
        }
        currentNodeMap[node.id] = node;
        if(node.count > maxNodeCount) maxNodeCount = node.count;
        const cachePosition = this.cachePositions ? this.cachePositions[node.id] : undefined;
        if(cachePosition) {
          node.x = cachePosition.x;
          node.y = cachePosition.y;
          node.new = false;
        }else {
          node.new = isNewGraph ? false : true;
          if(this.manipulatePosition && !node.x && !node.y) {
            node.x = this.manipulatePosition.x + 30 * Math.cos(Math.random() * Math.PI * 2);
            node.y = this.manipulatePosition.y + 30 * Math.sin(Math.random() * Math.PI * 2);
          }
        }
      });

      let maxCount = -Infinity;
      let minCount = Infinity;
      edges.forEach((edge) => {
        if (!edge.id) edge.id = `${uniqueId('edge')}`;
        else if (edge.id.split('-')[0] !== 'edge') edge.id = `edge-${edge.id}`;
        if (!currentNodeMap[edge.source] || !currentNodeMap[edge.target]) {
          console.warn('edge source target does not exist', edge.source, edge.target, edge.id);
          return;
        }
        const sourceNode = currentNodeMap[edge.source];
        const targetNode = currentNodeMap[edge.target];

        if (!sourceNode || !targetNode)
          console.warn('source or target is not defined!!!', edge, sourceNode, targetNode);

        // calculate the degree
        sourceNode.degree++;
        targetNode.degree++;
        sourceNode.outDegree++;
        targetNode.inDegree++;

        if (edge.count > maxCount) maxCount = edge.count;
        if (edge.count < minCount) minCount = edge.count;
      });

      nodes.sort(this.descendCompare(NODESIZEMAPPING));
      const maxDegree = nodes[0].degree || 1;

      const descreteNodes = [];
      nodes.forEach((node, i) => {
        const countRatio = node.count / maxNodeCount;
        const isRealNode = node.level === 0;
        node.size = isRealNode ? DEFAULTNODESIZE : DEFAULTAGGREGATEDNODESIZE;
        node.isReal = isRealNode;
        node.labelCfg = {
          position: 'bottom',
          offset: 5,
          style: {
            fill: global.node.labelCfg.style.fill,
            fontSize: 10, //  6 + countRatio * 6 || 12,
            stroke: global.node.labelCfg.style.stroke,
            lineWidth: 3,
          },
        };

        if (!node.degree) {
          descreteNodes.push(node);
        }
      });

      const countRange = maxCount - minCount;
      const minEdgeSize = 2 // 1
      const maxEdgeSize = 2 // 7
      const edgeSizeRange = maxEdgeSize - minEdgeSize;
      edges.forEach((edge) => {
        const targetNode = currentNodeMap[edge.target];

        const size = ((edge.count - minCount) / countRange) * edgeSizeRange + minEdgeSize || 1;
        edge.size = 2 // size;  节点连线宽

        const arrowWidth = Math.max(size / 2 + 2, 3);
        const arrowLength = 10;
        const arrowBeging = targetNode.size + arrowLength;
        let arrowPath = `M ${arrowBeging},0 L ${arrowBeging + arrowLength},-${arrowWidth} L ${
          arrowBeging + arrowLength
        },${arrowWidth} Z`;
        let d = targetNode.size / 2 + arrowLength;
        if (edge.source === edge.target) {
          edge.type = 'loop';
          arrowPath = undefined;
        }
        const sourceNode = currentNodeMap[edge.source];
        const isRealEdge = targetNode.isReal && sourceNode.isReal;
        edge.isReal = isRealEdge;
        const stroke = isRealEdge ? global.edge.style.realEdgeStroke : global.edge.style.stroke;
        const opacity = isRealEdge
          ? global.edge.style.realEdgeOpacity
          : global.edge.style.strokeOpacity;
        const dash = Math.max(size, 2);
        const lineDash = isRealEdge ? undefined : [dash, dash];
        edge.style = {
          stroke,
          strokeOpacity: opacity,
          cursor: 'pointer',
          lineAppendWidth: Math.max(edge.size || 5, 5),
          fillOpacity: 1,
          lineDash,
          endArrow: arrowPath
            ? {
                path: arrowPath,
                d,
                fill: stroke,
                strokeOpacity: 0,
              }
            : false,
        };
        edge.labelCfg = {
          autoRotate: true,
          style: {
            stroke: global.edge.labelCfg.style.stroke,
            fill: global.edge.labelCfg.style.fill,
            lineWidth: 4,
            fontSize: 8,
            lineAppendWidth: 10,
            opacity: 1,
          },
        };
        if (!edge.oriLabel) edge.oriLabel = edge.label;
        if (largeGraphMode || !edgeLabelVisible) edge.label = '';
        else {
          edge.label = this.labelFormatter(edge.label, labelMaxLength);
        }

        // arrange the other nodes around the hub
        const sourceDis = sourceNode.size / 2 + 20;
        const targetDis = targetNode.size / 2 + 20;
        if (sourceNode.x && !targetNode.x) {
          targetNode.x = sourceNode.x + sourceDis * Math.cos(Math.random() * Math.PI * 2);
        }
        if (sourceNode.y && !targetNode.y) {
          targetNode.y = sourceNode.y + sourceDis * Math.sin(Math.random() * Math.PI * 2);
        }
        if (targetNode.x && !sourceNode.x) {
          sourceNode.x = targetNode.x + targetDis * Math.cos(Math.random() * Math.PI * 2);
        }
        if (targetNode.y && !sourceNode.y) {
          sourceNode.y = targetNode.y + targetDis * Math.sin(Math.random() * Math.PI * 2);
        }

        if (!sourceNode.x && !sourceNode.y && this.manipulatePosition) {
          sourceNode.x = this.manipulatePosition.x + 30 * Math.cos(Math.random() * Math.PI * 2);
          sourceNode.y = this.manipulatePosition.y + 30 * Math.sin(Math.random() * Math.PI * 2);
        }
        if (!targetNode.x && !targetNode.y && this.manipulatePosition) {
          targetNode.x = this.manipulatePosition.x + 30 * Math.cos(Math.random() * Math.PI * 2);
          targetNode.y = this.manipulatePosition.y + 30 * Math.sin(Math.random() * Math.PI * 2);
        }
      });

      this.descreteNodeCenter = {
        x: width - paddingLeft,
        y: height - paddingTop,
      };
      descreteNodes.forEach((node, index) => {
        if (!node.x && !node.y) {
          node.x = this.descreteNodeCenter.x + 30 * Math.cos(Math.random() * Math.PI * 2);
          node.y = this.descreteNodeCenter.y + 30 * Math.sin(Math.random() * Math.PI * 2);
        }
        // 固定单个真实节点
        // if(node.level === 0 && this.aggregatedNodeMap[node.clusterId].count == 1) {
        //   node.fx = node.x + (Number(index) * 30)
        //   node.fy = node.y + (Number(index) * 30)
        // }
      });

      G6.Util.processParallelEdges(edges, 12.5, 'custom-quadratic', 'custom-line');
      return {
        maxDegree,
        edges,
      };
    },
    getForceLayoutConfig (graph, largeGraphMode, configSettings) {
      let _this = this
      let {
        linkDistance,
        edgeStrength,
        nodeStrength,
        nodeSpacing,
        preventOverlap,
        nodeSize,
        collideStrength,
        alpha,
        alphaDecay,
        alphaMin,
      } = configSettings || { preventOverlap: true };

      if (!linkDistance && linkDistance !== 0) linkDistance = 225;
      if (!edgeStrength && edgeStrength !== 0) edgeStrength = 50;
      if (!nodeStrength && nodeStrength !== 0) nodeStrength = 200;
      if (!nodeSpacing && nodeSpacing !== 0) nodeSpacing = 15;

      const config = {
        type: 'gForce',
        minMovement: 0.01,
        maxIteration: 5000,
        preventOverlap,
        damping: 0.99,
        animate: true,
        linkDistance: (d) => {
          let dist = linkDistance;
          const sourceNode = this.nodeMap[d.source] || this.aggregatedNodeMap[d.source];
          const targetNode = this.nodeMap[d.target] || this.aggregatedNodeMap[d.target];
          // // 两端都是聚合点
          // if (sourceNode.level && targetNode.level) dist = linkDistance * 3;
          // // 一端是聚合点，一端是真实节点
          // else if (sourceNode.level || targetNode.level) dist = linkDistance * 1.5;
          if (!sourceNode.level && !targetNode.level) dist = linkDistance * 0.6;
          return dist;
        },
        edgeStrength: (d) => {
          const sourceNode = this.nodeMap[d.source] || this.aggregatedNodeMap[d.source];
          const targetNode = this.nodeMap[d.target] || this.aggregatedNodeMap[d.target];
          // 聚合节点之间的引力小
          if (sourceNode.level && targetNode.level) return edgeStrength / 2;
          // 聚合节点与真实节点之间引力大
          if (sourceNode.level || targetNode.level) return edgeStrength;
          return edgeStrength;
        },
        nodeStrength: (d) => {
          // 给离散点引力，让它们聚集
          if (d.degree === 0) {
            if(d.level === 0 && _this.aggregatedNodeMap[d.clusterId].count == 1) {
              return 40;
            }else{
              return -10;
            }
          }
          // 聚合点的斥力大
          if (d.level) return nodeStrength * 2;
          return nodeStrength;
        },
        nodeSize: (d) => {
          if (!nodeSize && d.size) return d.size;
          return 50;
        },
        nodeSpacing: (d) => {
          if (d.degree === 0) return nodeSpacing * 2; 
          if (d.level) return nodeSpacing;
          return nodeSpacing;
        },
        onLayoutEnd: () => {
          if (largeGraphMode) {
            graph.getEdges().forEach((edge) => {
              if (!edge.oriLabel) return;
              edge.update({
                label: this.labelFormatter(edge.oriLabel, labelMaxLength),
              });
            });
          }
        },
        tick: () => {
          graph.refreshPositions();
        },
      };

      if (nodeSize) config['nodeSize'] = nodeSize;
      if (collideStrength) config['collideStrength'] = collideStrength;
      if (alpha) config['alpha'] = alpha;
      if (alphaDecay) config['alphaDecay'] = alphaDecay;
      if (alphaMin) config['alphaMin'] = alphaMin;

      return config;
    },
    hideItems (graph) {
      this.hiddenItemIds.forEach((id) => {
        graph.hideItem(id);
      });
    },
    showItems (graph) {
      graph.getNodes().forEach((node) => {
        if (!node.isVisible()) graph.showItem(node);
      });
      graph.getEdges().forEach((edge) => {
        if (!edge.isVisible()) edge.showItem(edge);
      });
      this.hiddenItemIds = [];
    },
    handleRefreshGraph (graph, graphData, width, height, largeGraphMode, edgeLabelVisible, isNewGraph) {
      if (!graphData || !graph) return;
      this.clearFocusItemState(graph);
      // reset the filtering
      graph.getNodes().forEach((node) => {
        if (!node.isVisible()) node.show();
      });
      graph.getEdges().forEach((edge) => {
        if (!edge.isVisible()) edge.show();
      });

      let nodes = [],
        edges = [];

      nodes = graphData.nodes;
      const processRes = this.processNodesEdges(
        nodes,
        graphData.edges || [],
        width,
        height,
        largeGraphMode,
        edgeLabelVisible,
        isNewGraph,
      );

      edges = processRes.edges;

      graph.changeData({ nodes, edges });

      this.hideItems(graph);
      graph.getNodes().forEach((node) => {
        node.toFront();
      });

      // layout.instance.stop();
      // force 需要使用不同 id 的对象才能进行全新的布局，否则会使用原来的引用。因此复制一份节点和边作为 force 的布局数据
      this.layout.instance.init({
        nodes: graphData.nodes,
        edges,
      });

      this.layout.instance.minMovement = 0.0001;

      this.layout.instance.getMass = (d) => {
        const cachePosition = this.cachePositions[d.id];
        if (cachePosition) return 5;
        return 1;
      };
      this.layout.instance.animate = true
      this.layout.instance.execute();
      return { nodes, edges };
    },
    getMixedGraph (aggregatedData, originData, nodeMap, aggregatedNodeMap, expandArray, collapseArray) {
      let nodes = [], edges = [];
      const expandMap = {},
        collapseMap = {};
      expandArray.forEach((expandModel) => {
        expandMap[expandModel.id] = true;
      });
      collapseArray.forEach((collapseModel) => {
        collapseMap[collapseModel.id] = true;
      });

      aggregatedData.clusters.forEach((cluster, i) => {
        if(expandMap[cluster.id]) {
          nodes = nodes.concat(cluster.nodes);
          aggregatedNodeMap[cluster.id].expanded = true;
        } else {
          if(cluster.nodes.length > 1) {
            nodes.push(aggregatedNodeMap[cluster.id]);
            aggregatedNodeMap[cluster.id].expanded = false;
          }else{
            nodes = nodes.concat(cluster.nodes)
          }
        }
      });
      originData.edges.forEach((edge) => {
        const isSourceInExpandArray = expandMap[nodeMap[edge.source].clusterId];
        const isTargetInExpandArray = expandMap[nodeMap[edge.target].clusterId];
        if (isSourceInExpandArray && isTargetInExpandArray) {
          edges.push(edge);
        } else if (isSourceInExpandArray) {
          const targetClusterId = nodeMap[edge.target].clusterId;
          const vedge = {
            source: edge.source,
            target: targetClusterId,
            id: `${uniqueId('edge')}`,
            label: '',
          };
          edges.push(vedge);
        } else if (isTargetInExpandArray) {
          const sourceClusterId = nodeMap[edge.source].clusterId;
          const vedge = {
            target: edge.target,
            source: sourceClusterId,
            id: `${uniqueId('edge')}`,
            label: '',
          };
          edges.push(vedge);
        }
      });
      aggregatedData.clusterEdges.forEach((edge) => {
        if (expandMap[edge.source] || expandMap[edge.target]) return;
        else edges.push(edge);
      });
      return { nodes, edges };
    },
    getNeighborMixedGraph ( centerNodeModel, step, originData, clusteredData, currentData, nodeMap, aggregatedNodeMap, maxNeighborNumPerNode = 5) {
      // update the manipulate position for center gravity of the new nodes
      this.manipulatePosition = { x: centerNodeModel.x, y: centerNodeModel.y };

      // the neighborSubGraph does not include the centerNodeModel. the elements are all generated new nodes and edges
      const neighborSubGraph = this.generateNeighbors(centerNodeModel, step, maxNeighborNumPerNode);
      // update the origin data
      originData.nodes = originData.nodes.concat(neighborSubGraph.nodes);
      originData.edges = originData.edges.concat(neighborSubGraph.edges);
      // update the origin nodeMap
      neighborSubGraph.nodes.forEach((node) => {
        nodeMap[node.id] = node;
      });
      // update the clusteredData
      const clusterId = centerNodeModel.clusterId;
      clusteredData.clusters.forEach((cluster) => {
        if (cluster.id !== clusterId) return;
        cluster.nodes = cluster.nodes.concat(neighborSubGraph.nodes);
        cluster.sumTot += neighborSubGraph.edges.length;
      });
      // update the count
      aggregatedNodeMap[clusterId].count += neighborSubGraph.nodes.length;

      currentData.nodes = currentData.nodes.concat(neighborSubGraph.nodes);
      currentData.edges = currentData.edges.concat(neighborSubGraph.edges);
      return currentData;
    },
    generateNeighbors (centerNodeModel, step, maxNeighborNumPerNode = 5) {
      if (step <= 0) return undefined;
      let nodes = [],
        edges = [];
      const clusterId = centerNodeModel.clusterId;
      const centerId = centerNodeModel.id;
      const neighborNum = Math.ceil(Math.random() * maxNeighborNumPerNode);
      for (let i = 0; i < neighborNum; i++) {
        const neighborNode = {
          id: uniqueId('nenode'),
          clusterId,
          level: 0,
          colorSet: centerNodeModel.colorSet,
        };
        nodes.push(neighborNode);
        const dire = Math.random() > 0.5;
        const source = dire ? centerId : neighborNode.id;
        const target = dire ? neighborNode.id : centerId;
        const neighborEdge = {
          id: uniqueId('nenode'),
          source,
          target,
          label: `${source}-${target}`,
        };
        edges.push(neighborEdge);
        const subNeighbors = this.generateNeighbors(neighborNode, step - 1, maxNeighborNumPerNode);
        if (subNeighbors) {
          nodes = nodes.concat(subNeighbors.nodes);
          edges = edges.concat(subNeighbors.edges);
        }
      }
      return { nodes, edges };
    },
    examAncestors (model, expandedArray, length, keepTags) {
      for (let i = 0; i < length; i++) {
        const expandedNode = expandedArray[i];
        if (!keepTags[i] && model.parentId === expandedNode.id) {
          keepTags[i] = true; // 需要被保留
          this.examAncestors(expandedNode, expandedArray, length, keepTags);
          break;
        }
      }
    },
    manageExpandCollapseArray (nodeNumber, model, collapseArray, expandArray) {
      this.manipulatePosition = { x: model.x, y: model.y };
      // 维护 expandArray，若当前画布节点数高于上限，移出 expandedArray 中非 model 祖先的节点)
      if (nodeNumber > NODE_LIMIT) {
        // 若 keepTags[i] 为 true，则 expandedArray 的第 i 个节点需要被保留
        const keepTags = {};
        const expandLen = expandArray.length;
        // 检查 X 的所有祖先并标记 keepTags
        this.examAncestors(model, expandArray, expandLen, keepTags);
        // 寻找 expandedArray 中第一个 keepTags 不为 true 的点
        let shiftNodeIdx = -1;
        for (let i = 0; i < expandLen; i++) {
          if (!keepTags[i]) {
            shiftNodeIdx = i;
            break;
          }
        }
        // 如果有符合条件的节点，将其从 expandedArray 中移除
        if (shiftNodeIdx !== -1) {
          let foundNode = expandArray[shiftNodeIdx];
          if (foundNode.level === 2) {
            let foundLevel1 = false;
            // 找到 expandedArray 中 parentId = foundNode.id 且 level = 1 的第一个节点
            for (let i = 0; i < expandLen; i++) {
              const eNode = expandArray[i];
              if (eNode.parentId === foundNode.id && eNode.level === 1) {
                foundLevel1 = true;
                foundNode = eNode;
                expandArray.splice(i, 1);
                break;
              }
            }
            // 若未找到，则 foundNode 不变, 直接删去 foundNode
            if (!foundLevel1) expandArray.splice(shiftNodeIdx, 1);
          } else {
            // 直接删去 foundNode
            expandArray.splice(shiftNodeIdx, 1);
          }
          // const removedNode = expandedArray.splice(shiftNodeIdx, 1); // splice returns an array
          const idSplits = foundNode.id.split('-');
          let collapseNodeId;
          // 去掉最后一个后缀
          for (let i = 0; i < idSplits.length - 1; i++) {
            const str = idSplits[i];
            if (collapseNodeId) collapseNodeId = `${collapseNodeId}-${str}`;
            else collapseNodeId = str;
          }
          const collapseNode = {
            id: collapseNodeId,
            parentId: foundNode.id,
            level: foundNode.level - 1,
          };
          collapseArray.push(collapseNode);
        }
      }

      const currentNode = {
        id: model.id,
        level: model.level,
        parentId: model.parentId,
      };

      // 加入当前需要展开的节点
      expandArray.push(currentNode);

      this.graph.get('canvas').setCursor('default');
      return { expandArray, collapseArray };
    },
    cacheNodePositions (nodes) {
      const positionMap = {};
      const nodeLength = nodes.length;
      for (let i = 0; i < nodeLength; i++) {
        const node = nodes[i].getModel();
        positionMap[node.id] = {
          x: node.x,
          y: node.y,
          level: node.level,
        };
      }
      return positionMap;
    },
    stopLayout () {
      this.layout.instance.animate = true
      this.layout.instance.stop();
    },
    bindListener (graph) {
      graph.on('keydown', (evt) => {
        const code = evt.key;
        if (!code) {
          return;
        }
        if (code.toLowerCase() === 'shift') {
          this.shiftKeydown = true;
        } else {
          this.shiftKeydown = false;
        }
      });
      graph.on('keyup', (evt) => {
        const code = evt.key;
        if (!code) {
          return;
        }
        if (code.toLowerCase() === 'shift') {
          this.shiftKeydown = false;
        }
      });
      graph.on('node:mouseenter', (evt) => {
        const { item } = evt;
        const model = item.getModel();
        const currentLabel = model.label;
        model.oriFontSize = model.labelCfg.style.fontSize;
        item.update({
          label: model.oriLabel,
        });
        model.oriLabel = currentLabel;
        graph.setItemState(item, 'hover', true);
        item.toFront();
        if(model.level === 1) {
          graph.removePlugin(tooltip)
        }
        if(model.level === 0){
          if(tooltip.destroyed) {
            tooltip = new G6.Tooltip({
              offsetX: 10,
              offsetY: 20,
              getContent(e) {
                let model = e.item.getModel()
                return `<div>
                  <div>类型：${getTypeLabel(model.extendData)}</div>
                  <div>名称：${model.extendData.name}</div>
                  <div>id: ${model.extendData.id}</div>
                </div>`;
              },
              itemTypes: ['node']
            })
          }
          graph.addPlugin(tooltip)
        }
      });

      graph.on('node:mouseleave', (evt) => {
        const { item } = evt;
        const model = item.getModel();
        const currentLabel = model.label;
        item.update({
          label: model.oriLabel,
        });
        model.oriLabel = currentLabel;
        graph.setItemState(item, 'hover', false);
      });

      graph.on('edge:mouseenter', (evt) => {
        const { item } = evt;
        const model = item.getModel();
        const currentLabel = model.label;
        item.update({
          label: model.oriLabel,
        });
        model.oriLabel = currentLabel;
        item.toFront();
        item.getSource().toFront();
        item.getTarget().toFront();
      });

      graph.on('edge:mouseleave', (evt) => {
        const { item } = evt;
        const model = item.getModel();
        const currentLabel = model.label;
        item.update({
          label: model.oriLabel,
        });
        model.oriLabel = currentLabel;
      });
      // click node to show the detail drawer
      graph.on('node:click', (evt) => {
        this.stopLayout();
        graph.stopAnimate();
        if(!this.shiftKeydown) {
          this.clearFocusItemState(graph);
        }
        this.clearFocusEdgeState(graph);
        const { item } = evt;

        // highlight the clicked node, it is down by click-select
        graph.setItemState(item, 'focus', true);

        if (!this.shiftKeydown) {
          // 将相关边也高亮
          const relatedEdges = item.getEdges();
          relatedEdges.forEach((edge) => {
            graph.setItemState(edge, 'focus', true);
          });
        }
      });

      // click edge to show the detail of integrated edge drawer
      graph.on('edge:click', (evt) => {
        this.stopLayout();
        if (!this.shiftKeydown) this.clearFocusItemState(graph);
        this.clearFocusEdgeState(graph);
        const { item } = evt;
        // highlight the clicked edge
        graph.setItemState(item, 'focus', true);
      });

      // click canvas to cancel all the focus state
      graph.on('canvas:click', (evt) => {
        this.stopLayout();
        this.clearFocusItemState(graph);
        graph.stopAnimate()
        // console.log(graph.getGroup(), graph.getGroup().getBBox(), graph.getGroup().getCanvasBBox());
      });

      graph.on('canvas:mousemove', (evt) => {
        if(tooltip.destroyed) {
          tooltip = new G6.Tooltip({
            offsetX: 10,
            offsetY: 20,
            getContent(e) {
              let model = e.item.getModel()
              return `<div>
                <div>类型：${getTypeLabel(model.extendData)}</div>
                <div>名称：${model.extendData.name}</div>
                <div>id: ${model.extendData.id}</div>
              </div>`;
            },
            itemTypes: ['node']
          })
        }
        graph.addPlugin(tooltip)
      })
    },
    handleDesign(obj) {
      let str = ''
      if (obj.designType) {
        switch (obj.designType) {
          case 'page':
            str = location.origin + (`/page-design-ui/#/crud/design?jvsAppId=${this.jvsAppId}&id=`+obj.id + (obj.dataModelId ? `&dataModelId=${obj.dataModelId}` : ''))
            this.$openUrl(str, '_blank')
            break
          case 'form':
            str = location.origin + (`/page-design-ui/#/form?jvsAppId=${this.jvsAppId}&id=`+obj.id + (obj.dataModelId ? `&dataModelId=${obj.dataModelId}` : ''))
            this.$openUrl(str, '_blank')
            break
          case 'workflow':
            str = location.origin + (`/flowable-ui/#/processDesign?jvsAppId=${this.jvsAppId}&id=${obj.id}`)
            this.$openUrl(str, '_blank')
            break
          case 'rule':
            str = location.origin + (`/rule-design-ui/#/ruleDesign?jvsAppId=${this.jvsAppId}&id=${obj.id}&name=${obj.name}`)
            this.$openUrl(str, '_blank')
            break
          case 'chart':
            str = location.origin + (`/chart-design-ui/#/chartDesign?jvsAppId=${this.jvsAppId}&id=`+obj.id)
            this.$openUrl(str, '_blank')
            break
          case 'screen':
            str = location.origin + (`/data-screen-ui/#/datascreendesign?id=${obj.id}`)
            this.$openUrl(str, '_blank')
            break
          case 'report':
            str = location.origin + (`/data-report-ui/#/datareportdesign?id=${obj.id}`)
            this.$openUrl(str, '_blank')
            break
          default: ;break;
        }
      }
    },
  }
}
</script>
<style lang="scss" scoped>
.application-relation-map{
  width: 100%;
  height: 100%;
  position: relative;
  #appRelationMap{
    width: 100%;
    height: 100%;
    /deep/.g6-component-toolbar{
      z-index: 2;
      left: 16px;
      top: 16px;
      width: fit-content;
      padding: 8px 16px;
      background-color: rgba(54, 59, 64, 0);
      border-radius: 24px;
      transition: all 0.2s linear;
      border: 0;
      li{
        svg{
          fill: #2b2f33; // #fff;
        }
        .li-icon-span{
          color: #2b2f33; // #fff;
          font-size: 22px;
          line-height: 24px;
          opacity: 0.7;
        }
      }
      .zoom-tool-in, .zoom-tool-out{
        width: auto;
      }
      .zoom-tool-in{
        margin-left: 10px;
      }
      .zoom-tool-out{
        margin-right: 10px;
      }
      .clearFish, .clearLineTag, .clearSearch{
        display: none;
        svg{
          fill: #5273e0;
        }
      }
    }
    /deep/.g6-component-toolbar:hover{
      box-shadow: 0 5px 18px 0 #e4e7ed; // #000;
    }
    .hide-fish{
      /deep/.g6-component-toolbar{
        .viewFish{
          display: none;
        }
        .clearFish{
          display: list-item;
        }
      }
    }
    .hide-edge-tag{
      /deep/.g6-component-toolbar{
        .viewLineTag{
          display: none;
        }
        .clearLineTag{
          display: list-item;
        }
      }
    }
    .hide-search{
      /deep/.g6-component-toolbar{
        width: 373px;
        .viewSearch{
          display: none;
        }
        .clearSearch{
          display: list-item;
        }
      }
    }
    .search-node-tool{
      position: absolute;
      top: 16px;
      left: 220px;
      height: 40px;
      display: flex;
      align-items: center;
      z-index: 999;
      /deep/.el-input{
        .el-input__inner{
          background: none;
          background-color: transparent;
        }
        .el-input__inner:focus{
          border-color: #5273e0;
        }
      }
    }
  }
}
.application-relation-map-empty{
  background-image: url('../../const/img/emptyImage.svg');
  background-repeat: no-repeat;
  background-position: center;
  background-size: 260px 123px;
  position: relative;
  background-color: #fafafa;
}
.application-relation-map-empty::after{
  content: '暂无数据';
  line-height: 30px;
  color: #909399;
  font-size: 12px;
  text-align: center;
  display: block;
  width: 100%;
  position: absolute;
  top: calc(50% + 65px);
}
.result-list{
  box-sizing: border-box;
  max-height: 204px;
  overflow: hidden;
  overflow-y: auto;
  .result-list-item{
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 34px;
    cursor: pointer;
    padding: 0 10px;
    box-sizing: border-box;
    font-size: 12px;
    .text{
      flex: 1;
      color: #a2a3a5;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: pre;
    }
    .type{
      margin-left: 10px;
      color: #606266;
    }
  }
  .result-list-item:hover{
    background-color: rgba(255, 255, 255, 0.1);
    .text{
      font-size: 13px;
      color: #ccc;
    }
  }
}
.result-list::-webkit-scrollbar{
  opacity: .3;
}
</style>
<style lang="scss">
.el-popover.app-relation-popover{
  background: none;
  background-color: transparent;
  border: 0;
  .popper__arrow, .popper__arrow::after{
    border-bottom-color: rgba(255,255,255,.1);
  }
}
</style>