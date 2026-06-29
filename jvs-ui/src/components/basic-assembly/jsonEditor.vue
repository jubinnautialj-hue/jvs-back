<template>
  <div :class="{'json-editor-entity': true, 'json-editor-entity-showPrintMargin': showPrintMargin}" @contextmenu.prevent="handleContexMenu($event)">
    <editor :id="id" :el="prop" :ref="prop" :value="codeCon" :lang="(lang == 'jsonArray') ? 'json' : lang" :theme="theme || 'tomorrow_night_eighties'" :options="options" @init="init"></editor>
    <ul v-show="contexMenuVisible" :style="{'left': (left+'px'), 'top': (top+'px')}" class="code-editor-contexmenu">
      <li @click="beautifyCLick">格式化</li>
    </ul>
  </div>
</template>
<script>
import Editor from 'vue2-ace-editor'
// 主题
import "brace/theme/chrome";
import "brace/theme/monokai";
import "brace/theme/tomorrow_night_eighties";
// 代码片段
import "brace/snippets/javascript";
import "brace/snippets/dockerfile";
import "brace/snippets/python";
import "brace/snippets/php";
import "brace/snippets/html";
import "brace/snippets/css";
import "brace/snippets/ruby";
import "brace/snippets/groovy";
// 代码语言
import "brace/mode/json";
import "brace/mode/javascript";
import "brace/mode/dockerfile";
import "brace/mode/python";
import "brace/mode/php";
import "brace/mode/html";
import "brace/mode/ruby";
import "brace/mode/css";
import "brace/mode/groovy";
// 语言扩展
import "brace/ext/language_tools";
import "brace/ext/emmet";
import "brace/ext/beautify";
import "brace/ext/searchbox";
import "brace/ext/chromevox";
import "brace/ext/error_marker";
import "brace/ext/keybinding_menu";
import "brace/ext/linking";
import "brace/ext/modelist";
import "brace/ext/old_ie";
import "brace/ext/settings_menu";
import "brace/ext/spellcheck";
import "brace/ext/split";
import "brace/ext/static_highlight";
import "brace/ext/statusbar";
import "brace/ext/whitespace";
import "brace/ext/textarea";
import "brace/ext/themelist";

const beautify_js = require('js-beautify')

export default {
  name: 'coder-editor',
  components: { Editor },
  props: {
    prop: {
      type: String
    },
    code: {
      type: String
    },
    theme: {
      type: String
    },
    lang: {
      type: String,
      default: 'json'
    },
    disabled: {
      type: Boolean
    },
    showPrintMargin: {
      type: Boolean
    },
    onlyJSon: {
      type: Boolean
    }
  },
  data() {
    return {
      codeCon: '',
      options: {
        enableBasicAutocompletion: true, // 启用基本自动完成
        enableSnippets: true, // 后用代码段
        enableLiveAutocompletion: true, // 启用实时自动完成
        printMarginColumn: 30,
        displayIndentGuides: false, // 显示参考线
        enableEmmet: true, // 启用Emmet
        tabsize: 2, // 标签大小
        fontsize: 16, // 设置字号
        useWorker: true, // 使用辅助对象
        showPrintMargin: this.showPrintMargin || false, //去除编辑器里的竖线
        enableMultiselect: true, //选中多处
        readOnly: false, // 是否只读
        showFoldwidgets: true, // 显示折叠部件
        fadeFoldwidgets: true, // 淡入折叠部件
        wrap: true, //换行
        readOnly: this.disabled, // 是否禁用
      },
      contexMenuVisible: false,
      top: 0,
      left: 0,
      id: ''
    }
  },
  created () {
    this.id = this.prop + new Date().getTime()
  },
  mounted () {
    window.addEventListener('scroll', this.closeContexMenu, true)
  },
  beforeDestroy () {
    window.removeEventListener('scroll', this.closeContexMenu, true)
  },
  methods: {
    init (editor) {
      // 监听编辑器变化
      if(this.code) {
        if(['sql', 'groovy'].indexOf(this.lang) == -1) {
          editor.setValue(this.beautifyHandle(this.code), 1)
        }else{
          editor.setValue(this.code, 1)
        }
      }
      if(this.codeCon) {
        if(['sql', 'groovy'].indexOf(this.lang) == -1) {
          editor.setValue(this.beautifyHandle(this.codeCon), 1)
        }else{
          editor.setValue(this.codeCon, 1)
        }
      }
      editor.getSession().on("change", () => {
        this.codeCon = editor.getValue()
        if(!this.codeCon && !this.trim(this.code)) {
          this.$emit('change', this.codeCon, this.prop)
        }else{
          if(this.isJSON(this.codeCon)) {
            this.$emit('change', this.codeCon, this.prop)
          }else{
            if(['json', 'jsonArray'].indexOf(this.lang) > -1) {
              this.$emit('change', "error", this.prop)
            }else{
              this.$emit('change', this.codeCon, this.prop)
            }
          }
        }
      })
    },
    beautifyHandle (code) {
      return beautify_js(code, {
        indent_size: 2,
        html: {
          end_width_newline: true,
          js: {
            indent_size: 2
          },
          css: {
            indent_size: 2
          }
        },
        css: {
          indent_size: 1
        },
        js: {
          "preserve-newlines": true
        }
      })
    },
    trim (str){
      return (str || "").replace(/^\s+|\s+$/g,"");
    },
    isJSON(str) {
      if (typeof str == 'string') {
        try {
          var obj=JSON.parse(str);
          if(typeof obj == 'object' && obj ){
              if(obj instanceof Array) {
                return this.lang == 'jsonArray' ? true : (this.onlyJSon ? false : true)
              }else{
                if(JSON.stringify(obj) == "{}") {
                  return false
                }else{
                  return true
                }
              }
          }else{
              return false;
          }
        }catch(e) {
          // console.log('error：'+str+'!!!'+e);
          return false;
        }
      }
    },
    handleContexMenu (e) {
      if(['sql', 'groovy'].indexOf(this.lang) == -1) {
        this.left = e.clientX
        this.top = e.clientY
        this.contexMenuVisible = true
      }
    },
    closeContexMenu () {
      this.contexMenuVisible = false
    },
    beautifyCLick () {
      if(this.$refs[this.prop]) {
        let con = this.beautifyHandle(this.$refs[this.prop].editor.getValue())
        this.$refs[this.prop].editor.setValue(con, 1)
      }
      this.contexMenuVisible = false
    },
    showEditorFieldAnyWay (data, desc) {
      let _this = this
      let descPath = {}
      let tpdata = JSON.parse(JSON.stringify(data))
      this.getDescKeyPath(tpdata, descPath, '')
      let pathArr = []
      for(let p in descPath) {
        pathArr.push(descPath[p])
      }
      setTimeout(()=> {
        let arr = []
        let a = document.getElementById(_this.id)
        if(a) {
          let s = a.getElementsByClassName("ace_text-layer")
          if(s.length > 0) {
            let variableCount = -1
            for(var c = s[0].getElementsByClassName("ace_line"), l = 0; l < c.length; l++) {
              var u = c[l] , p = u.getElementsByClassName("ace_variable") , d = null;
              if(p && p.length > 0) {
                d = (p[0].innerHTML + "").replace(/^"(.*)"$/g, "$1");
                var h = u.getElementsByClassName("ace-editor-field-description");
                if((!h || h.length == 0)) { // && n != null
                  variableCount += 1
                  var f = document.createElement("span");
                  f.className = "ace-editor-field-description"
                  f.innerHTML = _this.getDescByField(desc, pathArr, variableCount)
                  f.title = f.innerHTML
                  u.appendChild(f)
                  f.onclick = function(e) {
                    console.log(f.title)
                  }
                }
              }
              var m = u.getElementsByClassName("ace_paren");
              if(m && m.length > 0) {
                for(var y = [], g = 0; g < m.length; g++) {
                  y.push(m[g].innerHTML);
                  switch (y.join("")) {
                    case "[":
                    case "{":
                      arr.push(d || 0);
                      break;
                    case "}":
                    case "]":
                      arr.pop()
                  }
                }
              }
            }
          }
        }
      }, 300)
    },
    getDescKeyPath (obj, path, pre) {
      for(let k in obj) {
        this.$set(path, k, pre ? (pre+'.'+k) : k)
        if(typeof obj[k] == 'object') {
          if(obj[k] instanceof Array) {}else{
            this.getDescKeyPath(obj[k], path, path[k])
          }
        }
      }
    },
    getDescByField (info, path, index) {
      let str = ''
      if(info && index > -1 && path[index] && info[path[index]]) {
        str = info[path[index]]
      }
      return str
    },
  },
  watch: {
    contexMenuVisible: {
      handler(newVal, oldVal) {
        if(newVal) {
          document.body.addEventListener('click', this.closeContexMenu)
        }else{
          document.body.removeEventListener('click', this.closeContexMenu)
        }
      }
    }
  }
}
</script>
<style lang="scss">
  .json-editor-entity{
    width: 100%;
    height: 100%;
    position: relative;
    .code-editor-contexmenu{
      margin: 0;
      background: #fff;
      z-index: 999;
      position: fixed;
      list-style-type: none;
      padding: 5px 0;
      font-weight: normal;
      color: #333;
      border: 1px solid #ebebeb;
      li{
        margin: 0;
        padding: 0 10px;
        font-size: 12px;
        line-height: 24px;
        cursor: pointer;
        transition: all .3s;
      }
      li:hover{
        background-color: #ebebeb;
      }
    }
  }
  .json-editor-entity-showPrintMargin{
    position: unset;
    .ace_editor{
      overflow: unset;
    }
    .ace_print-margin-layer{
      .ace_print-margin{
        left: 98%!important;
      }
    }
    .ace-editor-field-description{
      position: absolute;
      color: #8c8c8c;
      left: 100%;
      display: inline-block;
      width: 150px;
      overflow: hidden;
      word-break: keep-all;
      text-overflow: ellipsis;
      white-space: pre;
      height: 13px;
    }
  }
</style>