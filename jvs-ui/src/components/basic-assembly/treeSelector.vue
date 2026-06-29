<template>
  <div :class="{'jvs-tree-select': true, 'fixedH': collapsetags}" :ref="key">
    <div :class="{'el-cascader': true, 'fixed-height': collapsetags, 'empty-height': (props.multiple ? !(nametagList && nametagList.length > 0) : true)}">
      <el-popover
        v-model="openTree"
        :width="pupopWidth"
        placement="bottom-start"
        :disabled="disabled"
        trigger="click">
        <div class="jvs-tree-select-list">
          <el-tree
            v-if="optionsData && optionsData.length > 0"
            :data="optionsData" :props="props"
            :expand-on-click-node="false"
            @node-expand="openNode"
            @node-collapse="closeNode"
            @node-click="handleNodeClick">
            <span :class="{'custom-tree-node': true, 'isCheck': (checkIds.indexOf(node.data[props.value]) > -1 && !isIndeterminate(node)), 'indeterminate': isIndeterminate(node)}" slot-scope="{ node, data }" :title="node.label">
              <span v-if="props.multiple" :class="{'check-label': true}">
                <span class="check"></span>
              </span>
              <span class="check-text">{{node.label}}</span>
            </span>
          </el-tree>
          <div v-else class="el-select-dropdown__empty">暂无数据</div>
        </div>
        <div slot="reference" :class="{'el-input el-input--suffix': true, 'input-withvalue' : (nametagList && nametagList.length > 0), 'is-disabled': disabled}">
          <div class="input el-input__inner">
            <span v-if="!(nametagList && nametagList.length > 0)" class="placeholder">{{item.placeholder || '请选择'}}</span>
            <span v-if="props.multiple" :class="{'el-cascader__tags': true, 'name-tag-list': true, 'name-tag-list-more': collapsetags}">
              <el-tag v-if="nametagList && nametagList.length > 0 && (collapsetags ? (inx < 1) : true)" v-for="(name, inx) in nametagList" :key="'tree-select-tag-'+inx" :closeable="true" size="mini" type="info" effect="light">
                <span>{{name}}</span>
                <i class="el-tag__close el-icon-close" @click.stop="delTagItem(name, inx)"></i>
              </el-tag>
              <el-tag v-if="nametagList && nametagList.length > 1 && collapsetags" size="mini" type="info" effect="light" class="tag-length-item">+ {{nametagList.length-1}}</el-tag>
            </span>
            <span v-else>{{userStr}}</span>
          </div>
          <span class="el-input__suffix">
            <span class="el-input__suffix-inner">
              <i :class="{'el-input__icon el-icon-arrow-down': true, 'is-reverse': openTree}"></i>
              <i class="el-input__icon el-icon-circle-close" @click.stop="clearHandle"></i>
            </span>
          </span>
        </div>
      </el-popover>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    form: {
      type: Object
    },
    item: {
      type: Object
    },
    options: {
      type: Array
    },
    showalllevels: {
      type: Boolean
    },
    collapsetags: {
      type: Boolean
    },
    disabled: {
      type: Boolean,
    },
    props: {
      type: Object,
      default: () => {
        return {
          multiple: true,
          label: 'label',
          value: 'value',
          children: 'children',
          emitPath: true,
          checkStrictly: false
        }
      }
    },
    createInit: {
      type: Boolean
    }
  },
  computed: {
		nametagList () {
			let list = []
			if(this.userStr) {
				list = this.userStr.split(',')
			}
			return list
		},
    checkIds () {
      let tp = []
      this.checkList.filter(cit => {
        tp.push(cit.id)
      })
      return tp
    }
	},
  data () {
    return {
      key: new Date().getTime(),
      pupopWidth: 180,
      openTree: false,
      userStr: '',
	    userList: [],
	    userNameList: [],
      optionsData: [],
      checkList: [], // 已选择节点
      lastWidth: 180,
    }
  },
  created () {
    if(this.createInit) {
      this.init()
    }
  },
  mounted () {
    this.pupopWidth = this.$refs[this.key].clientWidth+80
  },
  methods: {
    init () {
      this.optionsData = JSON.parse(JSON.stringify(this.options))
			for(let i in this.optionsData) {
				this.optionsData[i].path = [this.optionsData[i][this.props.value]]
				if(this.showalllevels) {
					this.optionsData[i].namePath = [this.optionsData[i][this.props.label]]
				}
        this.getInitData(this.optionsData[i])
				if(this.optionsData[i][this.props.children] && this.optionsData[i][this.props.children].length > 0) {
					this.eachOptions(this.optionsData[i][this.props.children], this.optionsData[i])
				}
			}
      if(this.checkList && this.checkList.length > 0) {
        this.dealSub(this.checkList)
      }
    },
    eachOptions (list, parent) {
			for(let i in list) {
				if(!list[i].path) {
					list[i].path = parent.path ? JSON.parse(JSON.stringify(parent.path)) : []
				}
				if(this.showalllevels) {
					if(!list[i].namePath) {
						list[i].namePath = parent.namePath ? JSON.parse(JSON.stringify(parent.namePath)) : []
					}
				}
				if(list[i][this.props.value]) {
					list[i].path.push(list[i][this.props.value])
					if(this.showalllevels) {
						list[i].namePath.push(list[i][this.props.label])
					}
				}
        this.getInitData(list[i])
				if(list[i][this.props.children] && list[i][this.props.children].length > 0) {
					this.eachOptions(list[i][this.props.children], list[i])
				}
			}
		},
    getInitData (data) {
      if(this.props.multiple) {
        if(this.form[this.item.prop] && this.form[this.item.prop].length > 0) {
          this.form[this.item.prop].filter(fot => {
            if(this.props.emitPath) {
              if(fot[fot.length-1] == data[this.props.value]) {
                this.setInitCheck(data)
              }
            }else{
              if(fot == data[this.props.value]) {
                this.setInitCheck(data)
              }
            }
          })
        }
      }else{
        if(this.props.emitPath) {
          if(this.form[this.item.prop] && this.form[this.item.prop].length > 0) {
            if(this.form[this.item.prop][this.form[this.item.prop].length - 1] == data[this.props.value]) {
              this.setInitCheck(data)
            }
          }
        }else{
          if(this.form[this.item.prop] == data[this.props.value]) {
            this.setInitCheck(data)
          }
        }
      }
    },
    setInitCheck (data) {
      let obj = { id: data[this.props.value], name: data[this.props.label], path: data.path}
			if(data.namePath) {
				obj.namePath = data.namePath
			}
			if(data[this.props.value]) {
				obj[this.props.value] = data[this.props.value]
			}
      this.checkList.push(obj)
    },
    getShowInfo (list) {
			for(let i in list) {
				if(this.form[this.item.prop]) {
					if(this.props.multiple) {
						if(this.props.emitPath) {
							this.form[this.item.prop].filter(it => {
								if(list[i].path == it) {
									this.userNameList.push(this.showalllevels ? list[i].namePath.join('/') : list[i][this.props.label])
								}
							})
						}else{
							this.form[this.item.prop].filter(it => {
								if(list[i][this.props.value] == it) {
									this.userNameList.push(this.showalllevels ? list[i].namePath.join('/') : list[i][this.props.label])
								}
							})
						}
						this.$set(this, 'userStr', this.userNameList.join(','))
					}else{
						if(this.props.emitPath) {
							if(list[i].path == this.form[this.item.prop]) {
								this.$set(this, 'userStr', this.showalllevels ? list[i].namePath.join('/') : list[i][this.props.label])
							}
						}else{
							if(list[i][this.props.value] == this.form[this.item.prop]) {
								this.$set(this, 'userStr', this.showalllevels ? list[i].namePath.join('/') : list[i][this.props.label])
							}
						}
					}
				}
				if(list[i][this.props.children] && list[i][this.props.children].length > 0) {
					this.getShowInfo(list[i][this.props.children], list[i])
				}
			}
		},
    handleNodeClick (data) {
      let obj = { id: data[this.props.value], name: data[this.props.label], path: data.path}
			if(data.namePath) {
				obj.namePath = data.namePath
			}
			if(data[this.props.value]) {
				obj[this.props.value] = data[this.props.value]
			}
			const index = this.checkList.findIndex(item => {
			  return item.id === data[this.props.value]
			})
      if(this.props.multiple) {
        if(index === -1) {
          this.checkList.push(obj)
        }else{
          this.checkList.splice(index, 1)
        }
        if(!this.props.checkStrictly) {
          // 全选所有子节点
          if(data[this.props.children] && data[this.props.children].length > 0) {
            this.selectChildren(data[this.props.children], index > -1 ? 'del' : 'add')
          }
          // 根据父级id设置父节点状态
          if(data.parentId && data.path.length > 1) {
            this.deepEachTree(data.parentId, this.optionsData)
          }
        }
        this.dealSub(this.checkList)
      }else{
        this.checkList = [obj]
        this.submit()
      }
			this.$forceUpdate()
    },
    // 提交数据
		submit () {
			this.dealSub(this.checkList)
			this.closeDialog()
		},
    dealSub (list) {
			if(this.props.multiple) {
			  this.selectChange(list)
			  this.userStr = this.userNameList.join(',')
				let temp = []
				if(this.props.emitPath) {
					list.filter(li => { temp.push(li.path) })
				}else{
					list.filter(li => { temp.push(li[this.props.value]) })
				}
				this.form[this.item.prop] = temp
			}else{
			  if(list && list.length > 0) {
					if(this.props.emitPath) {
						this.form[this.item.prop] = list[0].path
					}else{
						this.form[this.item.prop] = list[0].id
					}
			    this.userStr = this.showalllevels ? list[0].namePath.join('/') : list[0].name
			  }else{
          this.form[this.item.prop] = ''
          this.userStr = ''
        }
			}
			this.$emit('change', this.form)
			this.$forceUpdate()
		},
    selectChange (data) {
		  let temp = []
		  let nm = []
		  for(let i in data) {
		    temp.push(data[i].id)
		    nm.push(this.showalllevels ? data[i].namePath.join('/') : data[i][this.props.label])
		  }
		  this.userList = temp
		  this.userNameList = nm
		},
    closeDialog () {
      this.openTree = false
      // this.checkList = []
			// this.userNameList = []
    },
    delTagItem (name, index) {
      if(this.checkList[index].name == name) {
        this.checkList.splice(index, 1)
        this.dealSub(this.checkList)
      }
      this.$forceUpdate()
    },
    clearHandle () {
      this.checkList = []
      this.dealSub(this.checkList)
      this.$forceUpdate()
    },
    selectChildren (list, type) {
      for(let i in list) {
        let obj = { id: list[i][this.props.value], name: list[i][this.props.label], path: list[i].path}
        if(list[i].namePath) {
          obj.namePath = list[i].namePath
        }
        if(list[i][this.props.value]) {
          obj[this.props.value] = list[i][this.props.value]
        }
        let index = this.checkList.findIndex(cit => {
          return cit.id === list[i][this.props.value]
        })
        if(type == 'add' && index == -1) {
          this.checkList.push(obj)
        }
        // 强关联删除子节点
        if(type == 'del' && index > -1) {
          this.checkList.splice(index, 1)
        }
        if(list[i][this.props.children] && list[i][this.props.children].length > 0) {
          this.selectChildren(list[i][this.props.children], type)
        }
      }
    },
    setParentNode (item) {
      if(item.children && item.children.length > 0) {
        let allchild = true
        let allCancel = true
        item[this.props.children].filter(cit => {
          if(this.checkIds.indexOf(cit[this.props.value]) == -1) {
            allchild = false
          }else{
            allCancel = false
          }
        })
        // 强关联选取上级
        if(allchild && this.checkIds.indexOf(item[this.props.value]) == -1) {
          let obj = { id: item[this.props.value], name: item[this.props.label], path: item.path}
          if(item.namePath) {
            obj.namePath = item.namePath
          }
          if(item[this.props.value]) {
            obj[this.props.value] = item[this.props.value]
          }
          this.checkList.push(obj)
        }
        // 强关联取消上级
        if(allCancel) {
          let index = this.checkIds.indexOf(item[this.props.value])
          if(index > -1) {
            this.checkList.splice(index, 1)
          }
        }
        if(item.parentId && item.path.length > 1) {
          this.deepEachTree(item.parentId, this.optionsData)
        }
        this.$forceUpdate()
      }
    },
    deepEachTree (id, list) {
      for(let i in list) {
        if(list[i][this.props.value] == id) {
          this.setParentNode(list[i])
        }
        if(list[i][this.props.children] && list[i][this.props.children].length > 0) {
          this.deepEachTree(id, list[i][this.props.children])
        }
      }
    },
    isIndeterminate (node) {
      let bool = false
      let childBool = false
      let allchild = true
      if(node.childNodes && node.childNodes.length > 0) {
        node.childNodes.filter(cn => {
          if(this.checkIds.indexOf(cn.data[this.props.value]) > -1) {
            childBool = true
          }else{
            allchild = false
          }
        })
      }
      if(childBool && !allchild) {
        bool = true
      }
      return bool
    },
    openNode (data, node, dom) {
      if(dom.$el.children && dom.$el.children.length > 0) {
        let pad = dom.$el.children[0].attributes.style.nodeValue.replace(/padding-left:/g, '')
        pad = pad.replace(/px;/g, '')
        pad = Number(pad)
        if(!isNaN(pad)) {
          this.openTree = false
          this.lastWidth = this.pupopWidth
          this.pupopWidth = pad + dom.$el.children[0].clientWidth+80
          if(document.clientWidth < this.pupopWidth) {
            this.pupopWidth = document.clientWidth + 80
          }
          this.$nextTick(() => {
            this.openTree = true
          })
        }
      }
    },
    closeNode (data, node, dom) {
      this.pupopWidth = this.lastWidth
    }
  },
  watch: {
		options: {
			handler(newVal, oldVal) {
				if(newVal.length > 0) {
					this.init()
				}
			}
		},
		resetRadom: {
			handler(newVal, oldVal) {
				if(newVal > -1) {
					this.init()
				}
			}
		}
	}
}
</script>
<style lang="scss" scoped>
  .jvs-tree-select{
    width: 100%;
    line-height: 0;
    .el-cascader{
      width: 100%;
      line-height: 36px;
      .el-input{
        .input.el-input__inner{
          position: relative;
          // height: unset;
          .placeholder{
            color: #C0C4CC;
          }
          .name-tag-list{
            position: unset;
            transform: none;
            margin: 2px 0;
            margin-left: -16px;
          }
          .name-tag-list-more{
            margin-top: 6px;
          }
        }
        .el-input__suffix{
          .el-icon-circle-close{
            display: none;
          }
        }
        &.is-disabled{
          .el-input__inner{
            cursor: no-drop;
          }
        }
      }
    }
    .el-cascader:hover{
      .input-withvalue{
        .el-input__suffix{
          .el-icon-circle-close{
            display: inline-block;
          }
          .el-icon-arrow-down{
            display: none;
          }
        }
      }
    }
    .fixed-height{
      .el-input__suffix{
        .el-input__suffix-inner{
          .el-input__icon{
            line-height: 0;
          }
        }
      }
    }
    &.fixedH{
      height: 36px;
    }
  }
  .jvs-tree-select-list{
    padding: 0 10px;
    max-height: 210px;
    overflow: hidden;
    overflow-y: auto;
    /deep/.el-tree-node.is-current > .el-tree-node__content{
      background-color: #F5F7FA;
    }
    .custom-tree-node{
      display: flex;
      align-items: center;
      .check-label{
        display: flex;
        align-items: center;
        cursor: pointer;
        margin-right: 5px;
        .check{
          display: inline-block;
          position: relative;
          border: 1px solid #dcdfe6;
          border-radius: 2px;
          box-sizing: border-box;
          width: 14px;
          height: 14px;
          background-color: #fff;
          z-index: 1;
          transition: border-color .25s cubic-bezier(.71,-.46,.29,1.46),background-color .25s cubic-bezier(.71,-.46,.29,1.46);
        }
        .check::after {
          box-sizing: content-box;
          content: "";
          border: 1px solid #fff;
          border-left: 0;
          border-top: 0;
          height: 7px;
          left: 4px;
          position: absolute;
          top: 1px;
          transform: rotate(45deg) scaleY(0);
          width: 3px;
          transition: transform .15s ease-in .05s;
          transform-origin: center;
        }
      }
    }
    .isCheck{
      .check-label{
        .check{
          background: #3471FF;
          border-color: #3471FF;
        }
        .check::after{
          transform: rotate(45deg) scaleY(1);
        }
      }
      .check-text{
        color: #409EFF;
        font-weight: 700;
      }
    }
    .indeterminate{
      .check-label{
        .check{
          background: #3471FF;
          border-color: #3471FF;
        }
        .check::before{
          content: "";
          position: absolute;
          display: block;
          background-color: #fff;
          height: 2px;
          transform: scale(.5);
          left: 0;
          right: 0;
          top: 5px;
        }
      }
      .check-text{
        color: #409EFF;
        font-weight: 700;
      }
    }
    .isDisabled{
      .check-label{
        .check{
          background-color: #edf2fc;
          border-color: #dcdfe6;
          cursor: not-allowed;
        }
        .check::after{
          cursor: not-allowed;
          border-color: #c0c4cc;
        }
      }
      .check-text{
        color: #edf2fc;
      }
    }
  }
</style>
