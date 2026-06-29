<template>
  <el-row :gutter="20">
    <el-col :span="isChildren(itemContent) ? 24 : 4" v-for="(itemContent, i) in tagList" :key="itemContent[labelKey]">
      <template v-if="isChildren(itemContent)">
        <div :class="['popover_title', `popover_title_${level}`]">
          {{ itemContent.name }}
          <div v-if="children[i].extend.designRole" class="more">
            <el-popover
              placement="right-start"
              size="mini"
              trigger="hover">
              <div class="more-box">
                <div class="more-item" @click="moreHandle('rename', children[i])">
                  <span>修改名称</span>
                </div>
                <div class="more-item" @click="moreHandle('move', children[i])">
                  <span>移动目录</span>
                </div>
                <div class="more-item" @click="addCatalogue(children[i].id)">
                  <span>添加目录</span>
                </div>
                <div v-if="(!children[i].children || (children[i].children && children[i].children.length == 0))" class="more-item"
                     @click="moreHandle('del', children[i])">
                  <span style="color: #F56C6C;">删除</span>
                </div>
              </div>
              <div slot="reference">
                <i class="el-icon-more" style="color: #fff; font-size: 14px"/>
              </div>
            </el-popover>
          </div>
        </div>
        <page_main_index_sidebar_LeafNode :tag-list="itemContent.children" :level="level + 1" :label-key="labelKey"
                                          @open="extend => $emit('open', extend)" @moreHandle="moreHandle" @handleCreate="handleCreate" @handleDesign="handleDesign" :children="children[i].children"/>
      </template>
      <template v-else>
        <div class="popover_content_item" :title="itemContent.name" @click="handleOpen(itemContent)">
          <div class="popover_content_item_icon">
            <i :class="itemContent.extend.icon || 'iconfont icon-gengduo'"/>
            <div v-if="children[i].extend.designRole" class="more">
              <el-popover
                placement="right-start"
                size="mini"
                trigger="hover">
                <div class="more-box">
                  <div v-if="['url', 'URL'].indexOf(children[i].extend.design) == -1" class="more-item"
                       @click="handleDesign(children[i])">
                    <span>设计页面</span>
                  </div>
                  <div class="more-item" @click="moreHandle('move', children[i])">
                    <span>移动页面</span>
                  </div>
                  <div v-if="children[i].extend.design === 'URL'" class="more-item" @click="moreHandle('edit', children[i])">
                    <span>修改页面</span>
                  </div>
                  <div v-else class="more-item" @click="moreHandle('rename', children[i])">
                    <span>修改名称</span>
                  </div>
                  <div class="more-item" @click="moreHandle('pc', children[i])">
                    <span>{{ (children[i].extend && children[i].extend.pcDisplay === false) ? '显示' : '隐藏' }}/PC端</span>
                  </div>
                  <div class="more-item" @click="moreHandle('mobile', children[i])">
                    <span>{{ (children[i].extend && children[i].extend.mobileDisplay === false) ? '显示' : '隐藏' }}/移动端</span>
                  </div>
                  <div class="more-item" @click="moreHandle('del', children[i])">
                    <span style="color: #F56C6C;">删除页面</span>
                  </div>
                  <div
                    v-if="['chart', 'URL', 'url'].indexOf(children[i].extend.design) == -1 && children[i].extend.designTypes && children[i].extend.designTypes.length > 0">
                    <div style="height: 1px; background-color: #f2f2f2;margin: 6px 12px;"/>
                    <div class="model-header">根据模型快速创建</div>
                    <div class="model-box" v-if="children[i].extend.designTypes.indexOf('page') > -1">
                      <svg t="1647939001234" class="icon" viewBox="0 0 1024 1024" version="1.1"
                           xmlns="http://www.w3.org/2000/svg" p-id="8640" width="20" height="20" style="margin-right: 6px;">
                        <path d="M226.3 70.4C151.1 91.6 91.6 151.1 70.4 226.3L226.3 70.4z" fill="#FFA65A"
                              p-id="8641"></path>
                        <path d="M277.9 62.2c-116.5 4.7-211 99.1-215.7 215.7L277.9 62.2z" fill="#FFA659" p-id="8642"></path>
                        <path d="M321.5 62H287C163.3 62 62 163.3 62 287v34.5L321.5 62z" fill="#FFA558" p-id="8643"></path>
                        <path d="M365 62h-78C163.3 62 62 163.3 62 287v78L365 62z" fill="#FFA557" p-id="8644"></path>
                        <path d="M408.4 62H287C163.3 62 62 163.3 62 287v121.4L408.4 62z" fill="#FFA556" p-id="8645"></path>
                        <path
                          d="M451.8 62H287c-35.9 0-69.8 8.5-100 23.6L85.6 187C70.5 217.2 62 251.1 62 287v164.8L451.8 62z"
                          fill="#FFA555" p-id="8646"></path>
                        <path d="M495.3 62H287c-12.2 0-24.2 1-35.9 2.9L64.9 251.1C63 262.8 62 274.8 62 287v208.3L495.3 62z"
                              fill="#FFA454" p-id="8647"></path>
                        <path d="M62 538.7L538.7 62H297.5L62 297.5z" fill="#FFA453" p-id="8648"></path>
                        <path d="M62 582.1L582.1 62H340.9L62 340.9z" fill="#FFA452" p-id="8649"></path>
                        <path d="M62 625.6L625.6 62H384.3L62 384.3z" fill="#FFA451" p-id="8650"></path>
                        <path d="M62 427.8V669L669 62H427.8z" fill="#FFA450" p-id="8651"></path>
                        <path d="M62 471.2v241.2L712.4 62H471.2z" fill="#FFA34F" p-id="8652"></path>
                        <path d="M737 62H514.6L62 514.6V737c0 6.1 0.3 12.1 0.7 18.1L755.1 62.7c-6-0.4-12-0.7-18.1-0.7z"
                              fill="#FFA34E" p-id="8653"></path>
                        <path d="M737 62H558.1L62 558.1V737c0 19.1 2.4 37.6 6.9 55.4L792.4 68.9C774.6 64.4 756.1 62 737 62z"
                              fill="#FFA34D" p-id="8654"></path>
                        <path
                          d="M737 62H601.5L62 601.5V737c0 31.1 6.4 60.8 17.9 87.8L824.8 79.9C797.8 68.4 768.1 62 737 62z"
                          fill="#FFA34C" p-id="8655"></path>
                        <path
                          d="M853.5 94.7C819.4 74 779.5 62 737 62h-92.1L62 644.9V737c0 42.5 12 82.4 32.7 116.5L853.5 94.7z"
                          fill="#FFA24B" p-id="8656"></path>
                        <path
                          d="M878.9 112.7C840.1 81.1 790.7 62 737 62h-48.6L62 688.4V737c0 53.7 19.1 103.1 50.7 141.9l766.2-766.2z"
                          fill="#FFA24A" p-id="8657"></path>
                        <path
                          d="M737 62h-5.2L62 731.8v5.2c0 64.7 27.7 123.2 71.7 164.3l767.6-767.6C860.2 89.7 801.7 62 737 62z"
                          fill="#FFA249" p-id="8658"></path>
                        <path
                          d="M64.8 772.4c9.8 61 44.3 114.1 92.8 148.4l763.2-763.2c-34.3-48.6-87.4-83.1-148.4-92.8L64.8 772.4z"
                          fill="#FFA248" p-id="8659"></path>
                        <path
                          d="M73.3 807.3c18.7 56.4 59.2 103 111.3 129.9l752.6-752.6C910.4 132.5 863.7 92 807.3 73.3l-734 734z"
                          fill="#FFA247" p-id="8660"></path>
                        <path d="M86.1 838c26.5 52.3 72.9 93.1 129.1 112.2l735-735C931.1 159 890.3 112.6 838 86.1L86.1 838z"
                              fill="#FFA147" p-id="8661"></path>
                        <path
                          d="M102.4 865.2c34 48.7 86.7 83.5 147.5 93.7l709-709c-10.2-60.8-45-113.5-93.7-147.5L102.4 865.2z"
                          fill="#FFA146" p-id="8662"></path>
                        <path
                          d="M962 287c0-65.2-28.1-124.1-72.7-165.3L121.7 889.3C162.9 933.9 221.8 962 287 962h3.2L962 290.2V287z"
                          fill="#FFA145" p-id="8663"></path>
                        <path d="M962 287c0-54.2-19.4-104-51.6-143L144 910.4c39 32.2 88.8 51.6 143 51.6h46.6L962 333.6V287z"
                              fill="#FFA144" p-id="8664"></path>
                        <path
                          d="M962 287c0-43.1-12.3-83.4-33.5-117.7L169.3 928.5C203.6 949.7 243.9 962 287 962h90.1L962 377.1V287z"
                          fill="#FFA143" p-id="8665"></path>
                        <path
                          d="M287 962h133.5L962 420.5V287c0-31.6-6.6-61.8-18.5-89.2L197.8 943.4c27.4 12 57.6 18.6 89.2 18.6z"
                          fill="#FFA042" p-id="8666"></path>
                        <path
                          d="M287 962h176.9L962 463.9V287c0-19.7-2.6-38.7-7.4-56.9L230.1 954.6c18.2 4.8 37.2 7.4 56.9 7.4z"
                          fill="#FFA041" p-id="8667"></path>
                        <path d="M287 962h220.4L962 507.4V287c0-6.7-0.3-13.4-0.9-20L267 961.1c6.6 0.6 13.3 0.9 20 0.9z"
                              fill="#FFA040" p-id="8668"></path>
                        <path d="M550.8 962L962 550.8V309.6L309.6 962z" fill="#FFA03F" p-id="8669"></path>
                        <path d="M594.2 962L962 594.2V353L353 962z" fill="#FF9F3E" p-id="8670"></path>
                        <path d="M637.7 962L962 637.7V396.4L396.4 962z" fill="#FF9F3D" p-id="8671"></path>
                        <path d="M681.1 962L962 681.1V439.9L439.9 962z" fill="#FF9F3C" p-id="8672"></path>
                        <path d="M724.5 962L962 724.5V483.3L483.3 962z" fill="#FF9F3B" p-id="8673"></path>
                        <path d="M962 737V526.7L526.7 962H737c11.4 0 22.5-0.9 33.5-2.5l189-189c1.6-11 2.5-22.1 2.5-33.5z"
                              fill="#FF9F3A" p-id="8674"></path>
                        <path
                          d="M962 737V570.2L570.2 962H737c34.3 0 66.9-7.8 96.1-21.7l107.2-107.2c13.9-29.2 21.7-61.8 21.7-96.1z"
                          fill="#FF9E39" p-id="8675"></path>
                        <path d="M962 613.6L613.6 962H737c123.8 0 225-101.3 225-225V613.6z" fill="#FF9E38"
                              p-id="8676"></path>
                        <path d="M962 657L657 962h80c123.8 0 225-101.3 225-225v-80z" fill="#FF9E37" p-id="8677"></path>
                        <path d="M962 700.5L700.5 962H737c123.8 0 225-101.3 225-225v-36.5z" fill="#FF9E36"
                              p-id="8678"></path>
                        <path d="M961.9 744L744 961.9c118.2-3.7 214.2-99.7 217.9-217.9z" fill="#FF9D35" p-id="8679"></path>
                        <path d="M954.4 795L795 954.4c77.4-20.8 138.6-82 159.4-159.4z" fill="#FF9D34" p-id="8680"></path>
                        <path
                          d="M736.3 622.9L523.5 747.3c-5.6 3.3-12.4 3.3-18 0.1L287.8 622.6c-12.2-7-12-24.6 0.3-31.4l212.8-116.7c5.3-2.9 11.8-3 17.2-0.1l217.7 117c12.3 6.7 12.6 24.4 0.5 31.5z"
                          fill="#FFD9C0" p-id="8681"></path>
                        <path
                          d="M736.3 523.9L523.5 648.3c-5.6 3.3-12.4 3.3-18 0.1L287.8 523.6c-12.2-7-12-24.6 0.3-31.4l212.8-116.7c5.3-2.9 11.8-3 17.2-0.1l217.7 117c12.3 6.7 12.6 24.4 0.5 31.5z"
                          fill="#FFE8D9" p-id="8682"></path>
                        <path
                          d="M736.3 424.9L523.5 549.3c-5.6 3.3-12.4 3.3-18 0.1L287.8 424.6c-12.2-7-12-24.6 0.3-31.4l212.8-116.7c5.3-2.9 11.8-3 17.2-0.1l217.7 117c12.3 6.7 12.6 24.4 0.5 31.5z"
                          fill="#FFF6F0" p-id="8683"></path>
                      </svg>
                      <div @click="handleCreate('list', children[i])">
                        <div class="model-title">创建数据管理</div>
                        <div class="model-explain">快速信息搜集</div>
                      </div>
                    </div>
                    <div class="model-box" v-if="children[i].extend.designTypes.indexOf('form') > -1">
                      <svg t="1647938974487" class="icon" viewBox="0 0 1024 1024" version="1.1"
                           xmlns="http://www.w3.org/2000/svg" p-id="7006" width="20" height="20" style="margin-right: 6px;">
                        <path
                          d="M511.744 509.5936m-450.816 0a450.816 450.816 0 1 0 901.632 0 450.816 450.816 0 1 0-901.632 0Z"
                          fill="#59ADF8" p-id="7007"></path>
                        <path
                          d="M630.8864 224H320.8192c-42.24 0-76.4928 34.2528-76.4928 76.4928v405.1968c0 42.24 34.2528 76.4928 76.4928 76.4928h165.12a192.75264 192.75264 0 0 1-42.1888-120.576c0-106.9056 86.6304-193.536 193.536-193.536 24.7296 0 48.384 4.7104 70.0928 13.1584v-180.736c0.0512-42.24-34.2016-76.4928-76.4928-76.4928z m-175.9232 293.888H350.9248c-17.3568 0-31.4368-14.08-31.4368-31.4368 0-17.3568 14.08-31.4368 31.4368-31.4368h104.0384c17.3568 0 31.4368 14.08 31.4368 31.4368 0 17.408-14.08 31.4368-31.4368 31.4368z m150.4256-124.928h-254.464c-17.3568 0-31.4368-14.08-31.4368-31.4368 0-17.3568 14.08-31.4368 31.4368-31.4368h254.464c17.3568 0 31.4368 14.08 31.4368 31.4368 0 17.3568-14.08 31.4368-31.4368 31.4368z"
                          fill="#FFFFFF" p-id="7008"></path>
                        <path
                          d="M637.2864 517.888c-79.3088 0-143.8208 64.512-143.8208 143.8208s64.512 143.8208 143.8208 143.8208 143.8208-64.512 143.8208-143.8208-64.512-143.8208-143.8208-143.8208z m60.416 175.2064h-29.3376v29.3376c0 17.3568-14.08 31.4368-31.4368 31.4368-17.3568 0-31.4368-14.08-31.4368-31.4368v-29.3376h-29.3376c-17.3568 0-31.4368-14.08-31.4368-31.4368s14.08-31.4368 31.4368-31.4368h29.3376v-29.3376c0-17.3568 14.08-31.4368 31.4368-31.4368 17.3568 0 31.4368 14.08 31.4368 31.4368v29.3376h29.3376c17.3568 0 31.4368 14.08 31.4368 31.4368s-14.08 31.4368-31.4368 31.4368z"
                          fill="#FFFFFF" p-id="7009"></path>
                      </svg>
                      <div @click="handleCreate('form', children[i])">
                        <div class="model-title">创建表单设计</div>
                        <div class="model-explain">快速信息搜集</div>
                      </div>
                    </div>
                    <div class="model-box" v-if="children[i].extend.designTypes.indexOf('workflow') > -1">
                      <svg t="1647938948264" class="icon" viewBox="0 0 1024 1024" version="1.1"
                           xmlns="http://www.w3.org/2000/svg" p-id="4727" width="20" height="20" style="margin-right: 6px;">
                        <path
                          d="M512 241.777778m-139.377778 0a139.377778 139.377778 0 1 0 278.755556 0 139.377778 139.377778 0 1 0-278.755556 0Z"
                          fill="#37ADDB" p-id="4728"></path>
                        <path
                          d="M230.4 731.363556m-139.377778 0a139.377778 139.377778 0 1 0 278.755556 0 139.377778 139.377778 0 1 0-278.755556 0Z"
                          fill="#37ADDB" p-id="4729"></path>
                        <path
                          d="M221.639111 500.622222l-53.816889-12.401778A351.573333 351.573333 0 0 1 512 213.560889v55.296A296.846222 296.846222 0 0 0 221.639111 500.622222zM788.48 787.114667l-43.235556-34.133334A295.822222 295.822222 0 0 0 718.620444 352.711111l38.343112-39.936a350.776889 350.776889 0 0 1 31.516444 474.339556z"
                          fill="#37ADDB" p-id="4730"></path>
                        <path
                          d="M793.6 730.794667m-139.377778 0a139.377778 139.377778 0 1 0 278.755556 0 139.377778 139.377778 0 1 0-278.755556 0Z"
                          fill="#37ADDB" p-id="4731"></path>
                        <path
                          d="M520.078222 921.6a353.848889 353.848889 0 0 1-306.289778-176.469333L261.688889 717.596444A297.984 297.984 0 0 0 608.028444 853.333333l16.611556 52.679111A344.974222 344.974222 0 0 1 520.078222 921.6z"
                          fill="#37ADDB" p-id="4732"></path>
                      </svg>
                      <div @click="handleCreate('flow', children[i])">
                        <div class="model-title">创建OA流程</div>
                        <div class="model-explain">快速信息搜集</div>
                      </div>
                    </div>
                    <div style="height: 1px; background-color: #f2f2f2;margin: 6px 12px;"/>
                    <div class="model-learn" @click="handleLearn('learn-data-model')">
                      <svg class="help-entry">
                        <use xlink:href="#icon-help"></use>
                      </svg>
                      <div class="model-help">了解数据模型</div>
                    </div>
                  </div>
                </div>
                <div slot="reference">
                  <i class="el-icon-more" style="color: #fff; font-size: 14px"/>
                </div>
              </el-popover>
            </div>
          </div>
          <div class="popover_content_item_text">{{ itemContent.name }}</div>
        </div>
      </template>
    </el-col>
  </el-row>
</template>

<script>
export default {
  name: 'page_main_index_sidebar_LeafNode',
  props: {
    tagList: {
      type: Array,
      required: true
    },
    labelKey: {
      type: String,
      required: true
    },
    level: {
      type: Number,
      default: 0
    },
    children: {
      type: Array,
      default: () => []
    },
  },
  methods: {
    isChildren(item) {
      return item.children && item.children.length
    },
    handleOpen(item) {
      this.$emit('open', item.extend)
    },
    // 创建设计
    handleCreate(type, it) {
      this.$emit('handleCreate', type, it)
    },
    handleDesign(obj) {
      this.$emit('handleDesign', obj)
    },
    // 应用更多操作
    moreHandle(type, item) {
      this.$emit('moreHandle', type, item)
    },
  },
}
</script>

<style scoped lang="scss">
.popover_title {
  margin-bottom: 8px;
  font-weight: 700;
  position: relative;
  display: flex;

  .more {
    display: none;
    cursor: pointer;
    margin-left: 8px;
  }

  &:hover {
    .more {
      display: block;
    }
  }

  &.popover_title_0 {
    font-size: 16px;

    &::before {
      content: '';
      width: 4px;
      height: 14px;
      background: rgb(47, 106, 185);
      display: block;
      position: absolute;
      left: -10px;
      top: 4px;
    }
  }

  &.popover_title_1 {
    font-size: 14px;
    margin-left: 16px;
  }
}

.popover_content_item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;

  > .popover_content_item_icon {
    padding: 6px 12px;
    margin-bottom: 8px;
    border-radius: 8px;
    background-color: #409EFF;
    position: relative;

    > .iconfont {
      font-size: 32px;
      color: #fff;
    }
    .more {
      display: none;
      cursor: pointer;
      margin-left: 8px;
      position: absolute;
      top: 0;
      right: 0;
    }

    &:hover {
      .more {
        display: block;
      }
    }
  }

  > .popover_content_item_text {
    text-align: center;
    height: 40px;
    // 两行文字省略
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }
}
</style>
