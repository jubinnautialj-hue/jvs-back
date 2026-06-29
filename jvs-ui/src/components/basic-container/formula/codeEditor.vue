<template>
  <div :id="refs" class="code-block-editor">
    <codemirror
      :ref="refs"
      v-model="formulaStr"
      placeholder="支持输入搜索"
      :options="cmOptions"
      @input="codeMirrorChange"
      @changes="changeHandle"
    ></codemirror>
    <div v-if="!formulaStr" class="code-placeholder" @click="focus">支持输入搜索</div>
    <div v-if="checkCount > 0" class="error-tip">提示：{{checkMsg}}</div>
    <div ref="matchDiv" v-if="matchVisible && matchResult && matchResult.length > 0" class="words-match-list" :style="`left: ${matchPosX}px;top: ${matchPosY}px;`">
      <div :class="{'words-match-list-item': true, 'words-match-list-item-active': rix == currentIndex}" v-for="(rit, rix) in matchResult" :key="'match-item-'+rit.id" @click="matchClick(rit)">
        <div class="info">
          <div>
            <span :class="{'param-icon': rit.param, 'func-icon': !rit.param}">{{rit.param ? 'abc': 'fx'}}</span>
            <span :class="{'paramname': rit.param, 'funcname': !rit.param}">{{rit.name}}{{rit.param ? '': '()'}}</span>
            <span v-if="(!rit.param) && rit.shortName" class="shortname" :title="rit.shortName">{{rit.shortName}}</span>
          </div>
          <span :class="`colorspan ${rit.color}`">{{rit.typeLabelName}}</span>
        </div>
        <div v-if="rit.param" class="id-text">
          <span :title="rit.id">{{rit.id}}</span>
        </div>
      </div>
    </div>
  </div>

</template>
<script>
import {codemirror} from "vue-codemirror";
import "codemirror/lib/codemirror.css";
import "codemirror/theme/idea.css";
export default {
  name: 'codeBlockEditor',
  components: {codemirror},
  props: {
    nodeDataList: {
      type: Object
    },
    matchResult: {
      type: Array,
      default: () => {
        return []
      }
    },
    refs: {
      type: String,
      default: 'codeEditor'
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      editor: null,
      arrTemp: [],
      expression: "",
      formulaStr: '',
      cmOptions: {
        // 语言及语法模式
        mode: 'text/javascript',
        // 主题
        theme: "idea",
        // 显示函数
        line: true,
        lineNumbers: true,
        // 软换行
        lineWrapping: true,
        // tab宽度
        tabSize: 4,
        readOnly: this.disabled
      },
      checkCount: 0,
      checkMsg: '请仔细检查函数表达式',
      lastEditRange: null, // 最后光标对象
      matchword: '',
      matchVisible: false,
      matchPosX: 0,
      matchPosY: 0,
      currentIndex: -1,
    }
  },
  methods: {
    init (str) {
      if(str) {
        this.formulaStr = str
      }
      this.initCoder()
    },
    codeMirrorChange () {
      this.expression = ''
      this.autoMark()
      this.$emit('change', this.editor)
    },
    changeHandle () {
      let str = this.getExpression().expression
      if(!str || !str.includes('${')) {
        this.checkCount = 0
      }
    },
    initCoder () {
      this.$nextTick(() => {
        this.editor = this.$refs[this.refs].codemirror
        this.autoMarkText()
        if(this.disabled) return false
        // 当光标或选中(内容)发生变化，或者编辑器的内容发生了更改的时候触发
        this.editor.on('cursorActivity', (coder) => {
          this.matchword = ''
          let pos = coder.getCursor()
          let con = coder.getLine(pos.line)
          if(con && con.length > 0) {
            if(/[a-zA-Z0-9_\u4e00-\u9fa5]/.test(con[pos.ch-1])) {
              let start = pos.ch-1
              let ischange = false
              for(let i = pos.ch-1; i > -1; i--) {
                if(/[a-zA-Z0-9_\u4e00-\u9fa5]/.test(con[i]) == false) {
                  start = i+1;
                  ischange = true;
                  break;
                }
              }
              if(!ischange) {
                start = 0
              }
              if(start == pos.ch-1) {
                this.matchWords(con[start], coder, {line: pos.line, ch: start}, pos)
              }else{
                this.matchWords(con.slice(start, pos.ch), coder, {line: pos.line, ch: start}, pos)
              }
            }else{
              this.matchVisible = false
              this.lastEditRange = null
            }
          }else{
            this.matchVisible = false
            this.lastEditRange = null
          }
        })
        // // 按键触发，用于匹配项选择
        // this.editor.on('keydown', (coder, e) => {
        //   let keyCode = e.keyCode
        //   if(this.matchVisible && this.matchResult && this.matchResult.length > 0) {
        //     if(keyCode == 38) {
        //       this.editor.execCommand('goCharRight')
        //       this.chooseMatch('last')
        //     }
        //     if(keyCode == 40) {
        //       this.editor.execCommand('goCharLeft')
        //       this.chooseMatch('next')
        //     }
        //     if(keyCode == 13) {
        //       this.editor.undo()
        //       this.submitMatch()
        //     }
        //   }
        // })
      })
    },
    autoMarkText() {
      if(this.formulaStr) {
        this.autoMark(this.formulaStr);
        this.focus(this.formulaStr);
      }
    },
    focus(value) {
      this.editor.setCursor({
        line: 0,
        ch: value ? value.length : 0
      });
      this.editor.focus()
      this.$forceUpdate()
    },
    handleCheckFun (str, matches, index) {
      this.checkCount = 0
      let checkStr = this.expression.trim()
      if (checkStr.indexOf('`$') > -1 || checkStr.indexOf(')`') > -1) this.checkCount ++
      // if (checkStr.indexOf('`') > -1 && checkStr.indexOf('$') === -1) this.checkCount ++
      let arr = [...this.funcArr]
      if(this.funcArr.length === 0) return
      this.evalFunc(arr[index], matches, index)
    },
    evalFunc (funcStr, matches, index) {
      let checkCount = 0
      let msg = ''
      let formluaDom = document.getElementById(this.refs)
      let funcDomList = formluaDom.getElementsByClassName('cm-func')
      const that = this
      funcStr = funcStr.replace(matches[index], 'checkFun')
      // 处理转义字符
      funcStr = funcStr.replace(/"\\(.*?).*"/g, `'$1'`)
      try {
        funcDomList[index].title = ''
        eval(funcStr)
      } catch (e) {
        checkCount ++
        that.checkCount++
        msg = `${matches[index]}函数参数格式错误`
        funcDomList[index].title = msg.replace(/(\${|})/g, '')
        if (checkCount > 0) {
          funcDomList[index].style.color = '#f56c6c'
          funcDomList[index].style.textDecoration = 'underline'
        } else {
          funcDomList[index].style.color = '#ae4597'
          funcDomList[index].style.textDecoration = 'none'
        }
      }
      function checkFun(...ares){
        let funcKey = ''
        let returnType = ''
        if(matches[index]) {
          funcKey = matches[index].slice(2, matches[index].length-1)
        }
        if(that.nodeDataList[funcKey]) {
          let item = that.nodeDataList[funcKey]
          returnType = item.jvsParamType
          if(item.inParamTypes) {
            if(item.dynamicParam) {
              if(ares.length == 0) {
                checkCount++
                that.checkCount++
                msg = `${matches[index]}函数参数不能为空`
                funcDomList[index].title = msg.replace(/(\${|})/g, '')
              }else if(item.inParamTypes[0] == 'any'){}else{
                ares.forEach((ite, inx) => {
                  let kv = that.nodeDataList[ite+'']
                  if(kv){
                    if(kv.jvsParamType !== item.inParamTypes[0]) {
                      if(msg) {
                        msg += `[第${inx+1}个]`
                      }else{
                        msg = `${matches[index]}函数参数类型错误，全部应为${that.getLabeType(item.inParamTypes[0])}，[第${inx+1}个]`
                      }
                    }
                  }else {
                    if(ite !== 'any') {
                      let tpstr = ((typeof ite == 'object' && ite instanceof Array) ? 'array' : (typeof ite == 'string' ? 'text' : (typeof ite == 'boolean' ? 'bool' : (typeof ite))))
                      if(tpstr !== item.inParamTypes[0]) {
                        console.log(tpstr, item.inParamTypes[0], inx)
                        if(msg) {
                          msg += `[第${inx+1}个]`
                        }else{
                          msg = `${matches[index]}函数参数类型错误，全部应为${that.getLabeType(item.inParamTypes[0])}，[第${inx+1}个]`
                        }
                      }
                    }
                  }
                })
                if(msg) {
                  checkCount++
                  that.checkCount++
                  funcDomList[index].title = msg.replace(/(\${|})/g, '')
                }
              }
            }else if(ares.length !== item.inParamTypes.length) {
              checkCount++
              that.checkCount++
              msg = `${matches[index]}函数参数${ares.length < item.inParamTypes.length ? '不完整' : '过多'}`
              msg += `，当前${ares.length}个，应为${item.inParamTypes.length}个`
              funcDomList[index].title = msg.replace(/(\${|})/g, '')
            }else {
              ares.forEach((ite, inx) => {
                let kv = that.nodeDataList[ite+'']
                if(kv){
                  if(item.inParamTypes[inx] != 'any' && kv.jvsParamType != 'any' && kv.jvsParamType !== item.inParamTypes[inx]) {
                    if(msg) {
                      msg += `[第${inx+1}个应为${that.getLabeType(item.inParamTypes[inx])}]`
                    }else{
                      msg = `${matches[index]}函数参数类型错误，[第${inx+1}个应为${that.getLabeType(item.inParamTypes[inx])}]`
                    }
                  }
                }else {
                  if(ite !== 'any' && item.inParamTypes[inx] != 'any') {
                    let tpstr = ((typeof ite == 'object' && ite instanceof Array) ? 'array' : (typeof ite == 'string' ? 'text' : (typeof ite == 'boolean' ? 'bool' : (typeof ite))))
                    if(tpstr !== item.inParamTypes[inx]) {
                      if(msg) {
                        msg += `[第${inx+1}个应为${that.getLabeType(item.inParamTypes[inx])}]`
                      }else{
                        msg = `${matches[index]}函数参数类型错误，[第${inx+1}个应为${that.getLabeType(item.inParamTypes[inx])}]`
                      }
                    }
                  }
                }
                if(msg) {
                  checkCount++
                  that.checkCount++
                  funcDomList[index].title = msg.replace(/(\${|})/g, '')
                }
              })
            }
          }
          if(checkCount > 0) {
            funcDomList[index].style.color = '#f56c6c'
            funcDomList[index].style.textDecoration = 'underline'
          }else {
            funcDomList[index].style.color = '#ae4597'
            funcDomList[index].style.textDecoration = 'none'
          }
        }
        let arr = JSON.parse(JSON.stringify(that.funcArr))
        if (index > 0) {
          index--
          let str = arr[index]
          for(let i in arr) {
            if(i > index) {
              if(arr[index].includes(arr[i])) {
                let ed = arr[i].indexOf('}')
                let key = arr[i].slice(2, ed)
                if(that.nodeDataList[key]) {
                  if (arr[index] !== arr[i]) {
                    str = str.replace(arr[i], (that.nodeDataList[key].param ? ("'" + that.nodeDataList[key].jvsParamType + "'") : ('`' + key + '`')))
                  }
                }
              }
            }
          }
          for(let i = that.arrTemp.length-1; i > -1; i--) {
            if(i > index) {
              let $count = 0
              for(let c in str) {
                if(str[c] == '$') {
                  $count++
                }
              }
              if(str.includes(that.arrTemp[i]) && ($count > 1 ? true : (str !== that.arrTemp[i]))) {
                let ed = that.arrTemp[i].indexOf('}')
                let key = that.arrTemp[i].slice(2, ed)
                if (that.nodeDataList[key]) {
                  str = str.replace(that.arrTemp[i], (that.nodeDataList[key].param ? ("'" + that.nodeDataList[key].jvsParamType + "'") : ('`' + key + '`')))
                }
              }
            }
          }
          that.evalFunc(str, matches, index)
          that.arrTemp[index] = str
        }
      }
    },
    /**
     * editor 中的对内容进行处理
     * @param label
     * @param value
     * @param type variable | func，variable为表单变量，需标记，func 为函数，也需要做标记
     */
    insertContent (label, value, type, index) {
      const from = this.editor.getCursor()
      if(type === 'variable') {
        this.editor.replaceSelection(label)
        const to = this.editor.getCursor()
        this.markText(from, to, label, value, `cm-field cm-field-param${index+1}`)
      } else if (type === 'func') {
        this.editor.replaceSelection(`${value}()`)
        const to = this.editor.getCursor()
        this.markText(from, {line: to.line, ch: to.ch - 2}, null, value, 'cm-func')
        this.editor.setCursor({line: to.line, ch: to.ch - 1})
      } else if (typeof value === 'string') {
        this.editor.replaceSelection(value)
      }
      this.editor.focus()
      this.matchVisible = false
    },
    markText (from, to, label, value, className) {
      let text = document.createElement("span");
      text.className = className
      if(label) {
        text.innerText = label;
        text.setAttribute('data-id', value)
      } else {
        text.innerText = value;
        text.setAttribute('data-id', '${' + value + '}')
      }
      this.editor.markText(from, to, {
        atomic: true,
        replacedWith: text,
      });
      this.$nextTick(() => {
        let str = this.getExpression().expression
        this.funcArr = []
        const regex = /\${(.*?)}/g
        const matches = str.match(regex) || []
        matches.forEach((item, index) => {
          this.setFuncStr(str, index)
        })
        this.handleCheckFun(str, matches, this.funcArr.length - 1)
      })
    },
    // 解析 editor 的内容，分别对表单变量和函数进行标记
    autoMark () {
      if(!this.editor) return
      const editor = this.editor;
      const lines = editor.lineCount();
      for(let line = 0; line < lines; line++) {
        const content = editor.getLine(line);
        for(let k in this.nodeDataList) {
          let v = this.nodeDataList[k]
          let from = 0;
          let idx = -1;
          if(v.param) {
            while (~(idx = content.indexOf(v.id, from))) {
              if(idx > 1 && content[idx-1] == '"' && content[idx + v.id.length] == '"') {
                from = idx + v.id.length;
              }else if(idx > 1 && content[idx-1] != '"' && content[idx + v.id.length] == '"'){
                from = idx + v.id.length;
              }else{
                this.markText({line: line, ch: idx}, {line: line, ch: idx + v.id.length}, v.name, ('`' + v.id + '`'), `cm-field cm-field-param${v.parent}`);
                from = idx + v.id.length;
              }
            }
          }else {
            while (~(idx = content.indexOf(v.name + '(', from))) {
              this.markText({line: line, ch: idx}, {line: line, ch: idx + v.name.length}, v.name, ('${' + v.name + '}'), 'cm-func');
              from = idx + v.name.length + 1;
            }
          }
        }
      }
    },
    getExpression () {
      this.expression = ''
      let formluaDom = document.getElementById(this.refs)
      this.getHtml(formluaDom.getElementsByClassName('CodeMirror-line'))
      let obj = {
        renderStr: this.formulaStr,
      }
      return { expression: this.expression, render: JSON.stringify(obj) }
    },
    getHtml (arr) {
      for (let i = 0; i < arr.length; i++) {
        if (arr[i].nodeType === 3 && arr[i].parentNode.className.indexOf('cm-') === -1) {
          this.expression += arr[i].textContent
        }
        if (arr[i].className && arr[i].className.indexOf('cm-') > -1) {
          if(arr[i].className.includes('cm-field') || arr[i].className === 'cm-func') {
            this.expression += arr[i].getAttribute('data-id')
            if(this.nodeDataList[arr[i].getAttribute('data-id')]) {
              this.expression += arr[i].innerText
            }
          }
        }
        if(arr[i].childNodes) {
          this.getHtml(arr[i].childNodes)
        }
      }
    },
    setFuncStr(str, index) {
      let count = 0
      let newStr = ''
      let bool = false
      for(let char of str) {
        if (char === '$') count++
        if (count > index) newStr += char
      }
      let leftBracket = 0
      let rightBracket = 0
      let funcStr = ''
      for(let i = 0; i < newStr.length; i++) {
        if(newStr[i] == '"') {
          bool = !bool
        }
        if(newStr[i] === '(' && !bool) {
          leftBracket ++
        }
        if(newStr[i] === ')' && !bool) {
          rightBracket ++
        }
        funcStr += newStr[i]
        if(leftBracket === rightBracket && leftBracket !== 0) {
          this.funcArr.push(funcStr)
          break
        }
      }
    },
    // 校验后获取表达式
    validateResult () {
      if (this.checkCount > 0) return
      let str = this.getExpression().expression
      this.funcArr = []
      const regex = /\${(.*?)}/g
      const matches = str.match(regex) || []
      matches.forEach((item, index) => {
        this.setFuncStr(str, index)
      })
      this.handleCheckFun(str, matches, this.funcArr.length - 1)
      return str
    },
    matchWords (word, coder, start, end) {
      let cursor = coder.getCursor() // 获取当前光标位置
      let cursorCoords = coder.cursorCoords(cursor) // 获取光标的相对位置
      this.matchword = word
      this.lastEditRange = {
        from: start,
        to: end
      }
      if(this.matchword) {
        this.$emit('matchWords', this.matchword)
        this.matchPosX = cursorCoords.left + 10
        this.matchPosY = cursorCoords.top + 20
        this.matchVisible = true
        this.currentIndex = -1
      }
    },
    matchClick (item) {
      this.editor.setSelection(this.lastEditRange.from, this.lastEditRange.to)
      this.editor.replaceSelection('')
      this.$emit('matchClick', {item: item, index: item.parent})
      this.currentIndex = -1
      this.lastEditRange = null
    },
    getLabeType(type) {
      let str = ''
      switch(type) {
        case 'text':
          str = '文字';break;
        case 'number':
          str = '数字';break;
        case 'date':
          str = '时间';break;
        case 'array':
          str = '数组';break;
        case 'object':
          str = '对象';break;
        case 'bool':
          str = '布尔';break;
        case 'file':
          str = '文件';break;
        case 'unknown':
          str = '未知';break;
        default: ;break;
      }
      return str
    },
    // chooseMatch (type) {
    //   if(this.matchVisible && this.matchResult && this.matchResult.length > 0) {
    //     if(type == 'last') {
    //       if(this.currentIndex  > 0) {
    //         this.currentIndex -= 1
    //       }else{
    //         this.currentIndex = this.matchResult.length - 1
    //       }
    //     }
    //     if(type == 'next') {
    //       if(this.currentIndex == (this.matchResult.length - 1)) {
    //         this.currentIndex = 0
    //       }else{
    //         this.currentIndex += 1
    //       }
    //     }
    //     if(((Number(this.currentIndex) + 1) * 32) > 200) {
    //       this.$refs.matchDiv.scrollTop = (Number(this.currentIndex) + 1) * 32 - 200 + 2
    //     }else{
    //       this.$refs.matchDiv.scrollTop = 0
    //     }
    //   }
    // },
    // submitMatch () {
    //   if(this.currentIndex > -1) {
    //     this.matchClick(this.matchResult[this.currentIndex])
    //   }
    // },
  }
}
</script>
<style lang="scss" scoped>
.code-block-editor{
  height: 100%;
  box-sizing: border-box;
  .code-placeholder{
    position: absolute;
    top: 95px;
    left: 53px;
    font-size: 12px;
    font-family: Source Han Sans, Source Han Sans;
    font-weight: 400;
    color: #b2b4bb;
  }
  .error-tip{
    position: absolute;
    top: 30px;
    right: 45px;
    height: 30px;
    line-height: 30px;
    color: #f56c6c;
  }
}
.editdiv, .vue-codemirror{
  height: 100%;
  box-sizing: border-box;
  padding: 0;
  /*
  清除编辑器获取焦点时的默认样式：
  -webkit-tap-highlight-color:rgba(0,0,0,0);
  -webkit-user-modify:read-write-plaintext-only;
  outline:none;
  */
  /* 设置光标颜色：*/
  caret-color: #606266;
  /* 防止全局设置 -webkit-user-select: none 后不会获取焦点 */
  -webkit-user-select:text;
  .cm-field{
    margin: 0 10px;
    display: inline-block;
  }
  /deep/.CodeMirror{
    height: 100%;
    .CodeMirror-scroll{
      width: 100%;
      .CodeMirror-sizer{
        border-right-width: 0!important;
        .CodeMirror-code{
          .CodeMirror-gutter-wrapper{
            left: -30px!important;
          }
        }
        .CodeMirror-cursor{
          min-height: 16px;
        }
      }
      .CodeMirror-gutters{
        left: 0!important;
      }
    }
  }
}
/* 设置提示文字 */
.editdiv:empty::before {
  content: attr(placeholder);
  color: #999;
}
.editdiv:focus::before {
  content: none;
}
.words-match-list{
  position: fixed;
  background-color: #fff;
  border: 1px solid #eff2f7;
  max-width: calc(50% - 50px);
  max-height: calc(200px - 30px);
  box-sizing: border-box;
  overflow: hidden;
  overflow-y: auto;
  -webkit-touch-callout: none; /* iOS Safari */
  -webkit-user-select: none; /* Chrome/Safari/Opera */
  -khtml-user-select: none; /* Konqueror */
  -moz-user-select: none; /* Firefox */
  -ms-user-select: none; /* Internet Explorer/Edge */
  user-select: none;
  .words-match-list-item{
    margin: 0;
    padding: 0 10px;
    font-size: 12px;
    cursor: pointer;
    .info{
      display: flex;
      justify-content: space-between;
      align-items: center;
      height: 32px;
      line-height: 32px;
      >div{
        flex: 1;
        display: flex;
        align-items: center;
        overflow: hidden;
        .shortname{
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: pre;
          margin-left: 10px;
        }
      }
    }
    .id-text{
      margin-left: 27px;
      display: none;
      line-height: 24px;
    }
    &:hover{
      .id-text{
        display: block;
      }
    }
    .param-icon{
      font-size: 10px;
      width: 22px;
      color: #1B64E5;
    }
    .paramname{
      display: inline-block;
      border-radius: 4px;
      padding: 0 5px;
      margin: 1px;
      color: #fff;
      font-size: 12px;
      line-height: 20px;
      background: #1B64E5;
    }
    .func-icon{
      font-size: 10px;
      width: 25px;
      color: #651573;
    }
    .funcname{
      display: inline-block;
      border-radius: 4px;
      padding: 0 5px;
      margin: 1px;
      color: #fff;
      font-size: 12px;
      line-height: 20px;
      background: #651573;
    }
    .colorspan{
      display: block;
      height: 24px;
      line-height: 24px;
      padding: 0 8px;
      margin-left: 10px;
      border-radius: 4px;
      overflow: hidden;
    }
    .blue{
      color: #0089ff;
      background: #ebf6ff;
    }
    .yellow{
      color: #ff9200;
      background: #fff8eb;
    }
    .green{
      color: #00b853;
      background: #ebfaf2;
    }
    .purple{
      color: #5c72ff;
      background: #f2f4ff;
    }
    .red{
      color: #ff7357;
      background: #fff4f2;
    }
    .cyan{
      color: #70acc3;
      background: #eaf0f1;
    }
    .pink{
      color: #FF69B4;
      background: #fdf3ff;
    }
    .dark-yellow{
      color: #ebc60f;
      background: #f7f7f7;
    }
  }
  .words-match-list-item:hover{
    background: #eff2f7;
  }
  .words-match-list-item-active{
    background: #eff2f7;
  }
}
</style>