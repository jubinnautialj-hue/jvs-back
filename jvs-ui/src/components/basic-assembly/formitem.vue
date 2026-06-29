<template>
  <div :ref="item.prop" :class="{'jvs-form-item': true, 'jvs-form-item-disabled': item.disabled}" style="display:flex;align-items:center;">
    <!-- 普通文本框 -->
    <el-input
      v-model="forms[item.prop]"
      v-if='(item.type==="input" || !item.type) && !item.searchable && getDisableexpress("show")'
      :show-word-limit="item.showwordlimit"
      :minlength="item.minlength"
      :maxlength="item.maxlength"
      :placeholder="item.placeholder || item.label"
      :clearable="item.clearable"
      :show-password="item.showpassword"
      :disabled="item.disabled"
      :prefix-icon="item.prefixicon"
      :suffix-icon="item.suffixicon"
      :size="$store.state.params.form.size || item.size || 'mini'"
      :class="{'has-pend': (item.prepend || item.append)}"
      @focus="beforeValue = forms[item.prop]"
      @blur="formChange"
    >
      <template v-if='item.prepend' slot="prepend"><span>{{item.prepend}}</span></template>
      <template v-if='item.append' slot="append"><span>{{item.append}}</span></template>
    </el-input>
    <span
      v-if='(item.type==="input" || !item.type) && !item.searchable && !getDisableexpress("show")'
      class="disable-input-text"
      :style="(getDisableexpress('bgcolor') ? ('background-color:' + getDisableexpress('bgcolor') + ';') : '') +';'">
        <span :style="'width: 100%;overflow: hidden;text-overflow: ellipsis;white-space: pre;display: inline-block;' +
        (getDisableexpress('color') ? (getDisableexpress('color').startsWith('#') ? `color: ${getDisableexpress('color')};` : `${getDisableexpress('color')}`) : '')"
      >{{forms[item.prop]}}</span>
    </span>
    <!-- 搜索文本框 -->
    <el-input v-if="(item.type === 'input' || !item.type) && item.searchable && (item.disabled ? true : item.editable)" v-model="forms[item.prop]" :disabled="item.disabled ? true : !item.editable" placeholder="搜索选择所需数据" class="show-disable" @blur="formChange">
      <template slot="append" v-if="!item.disabled">
        <jvs-button icon="el-icon-search" type="info" :loading="openSearchInputLoading" @click="openDialog"></jvs-button>
        <jvs-button v-if="(originOption && !originOption.isSearch) && forms[item.prop]" icon="el-icon-copy-document" type="info" @click="copyHandle(forms[item.prop])"></jvs-button>
        <jvs-button icon="el-icon-delete" type="warning" @click="clearSearch"></jvs-button>
      </template>
    </el-input>
    <div v-if="(item.type==='input' || !item.type) && item.searchable && !item.editable && !item.disabled" class="show-disable el-input el-input--mini el-input-group el-input-group--append" style="display: flex;align-items: center;box-sizing: border-box;overflow: hidden;">
      <div class="el-input__inner" :style="`flex: 1;overflow: hidden;line-height: 34px;cursor: pointer;word-break: keep-all;box-sizing: border-box;cursor: ${item.disabled ? 'notallowed' : 'pointer'};${forms[item.prop] ? '' : 'color: #6F7588;'}`" @click="openDialog">
        <span style="display: block;width: 100%;overflow: hidden;">{{forms[item.prop] || '搜索选择所需数据'}}</span>
      </div>
      <div class="el-input-group__append" :style="`width: ${(originOption && !originOption.isSearch && forms[item.prop]) ? '111px' : '74px'};height: 36px;line-height: 36px;padding: 0;box-sizing: border-box;`">
        <jvs-button icon="el-icon-search" type="info" :loading="openSearchInputLoading" @click="openDialog" style="margin: 0;padding: 0;width: 37px;height: 26px;"></jvs-button>
        <jvs-button v-if="originOption && !originOption.isSearch && forms[item.prop]" icon="el-icon-copy-document" type="info" style="margin: 0;padding: 0;width: 37px;height: 26px;" @click="copyHandle(forms[item.prop])"></jvs-button>
        <jvs-button icon="el-icon-delete" type="warning" @click="clearSearch" style="margin: 0;padding: 0;width: 37px;height: 26px;"></jvs-button>
      </div>
    </div>
    <span class="el-form-item__error" v-if='errorShow'>{{item.regularMessage}}</span>
    <el-input
      v-model="forms[item.prop]"
      v-if='item.type==="InputReadOnly" || item.type==="inputReadOnly"'
      :disabled="item.disabled || true"
      :placeholder="item.placeholder || item.label"
      :size="$store.state.params.form.size || item.size || 'mini'"
    ></el-input>
    <el-input
      type="textarea"
      v-if='item.type==="textarea"'
      v-model="forms[item.prop]"
      :rows="item.rows"
      :show-word-limit="item.showwordlimit"
      :minlength="item.minlength"
      :maxlength="item.maxlength"
      :placeholder="item.placeholder || item.label"
      :clearable="item.clearable"
      :disabled="item.disabled"
      :size="$store.state.params.form.size || item.size || 'mini'"
      :autosize="item.disabled? true : (item.autoSize || false)"
      @focus="beforeValue = forms[item.prop]"
      @blur="formChange"
    ></el-input>
    <el-input
      type="textarea"
      v-if='item.type==="textareaReadOnly"'
      v-model="forms[item.prop]"
      rows="2"
      :placeholder="item.placeholder || item.label"
      :disabled="item.disabled || true"
      :size="$store.state.params.form.size || item.size || 'mini'"
    ></el-input>
    <el-input-number
      v-if='item.type==="inputNumber"'
      v-model="forms[item.prop]"
      :min="item.min"
      :max="item.max"
      :step="item.step"
      :step-strictly="item.stepstrictly"
      :precision="item.precision"
      :disabled="item.disabled"
      :controls="item.controlsposition != 'none'"
      :controls-position="item.controlsposition"
      :placeholder="item.placeholder || item.label"
      :size="$store.state.params.form.size || item.size || 'mini'"
      :class="{'input-number-hide': [undefined, null].indexOf(forms[item.prop]) > -1, 'show-thoudsandth-number': (item.thoudsandthable && showThoudsandth), 'input-number-unit': item.unit}"
      @change="formChange"
      @focus="showThoudsandthHandle(false); beforeValue = forms[item.prop];"
      @blur="showThoudsandthHandle(true);blurValidate();"
    ></el-input-number>
    <span v-if='item.type==="inputNumber" && item.thoudsandthable && showThoudsandth' :class="{'input-number-Thousandth': true, 'input-number-Thousandth-disabled': item.disabled, 'unit': item.unit}" @click="showThoudsandthHandle(false)">{{getThousandthNumber(forms[item.prop])}}</span>
    <span v-if='item.type==="inputNumber" && item.disabled && !item.thoudsandthable' class="input-number-textcon">{{( (forms[item.prop] != null && forms[item.prop] != undefined && forms[item.prop] != '') || forms[item.prop] == 0 ) ? forms[item.prop] : (item.unit ? '' : '-')}}</span>

    <el-select
      v-if='item.type==="select"'
      v-model="forms[item.prop]"
      :placeholder="item.placeholder || item.label"
      :multiple="item.multiple"
      :collapse-tags="!item.collapsetags"
      :disabled="item.disabled"
      :clearable="item.clearable === false ? false : true"
      :filterable="item.filterable"
      :allow-create="item.allowcreate"
      :size="$store.state.params.form.size || item.size || 'mini'"
      :class="{'jvs-select-multiple': item.multiple}"
      @change="formChange"
    >
      <el-option
        v-for="(sitem) in selectOption"
        :key="sitem[(item.props && item.props.value) || 'value']+item.prop +Math.random() + Date.now().toString()"
        :label="sitem[(item.props && item.props.label) || 'label']"
        :value="sitem[(item.props && item.props.value) || 'value']"
        :disabled="sitem.disabled"
      >
        <span style="float: left">{{ sitem[(item.props && item.props.label) || 'label'] }}</span>
        <span v-if="item.props && item.props.secTitle && sitem[item.props.secTitle]" style="float: right; color: #8492a6; font-size: 13px">{{ sitem[item.props.secTitle] }}</span>
        <span v-if="sitem.tip" style="float: right; color: #8492a6; font-size: 13px">{{ sitem.tip }}</span>
      </el-option>
    </el-select>

    <el-switch
      v-if='item.type==="switch"'
      v-model="forms[item.prop]"
      :disabled="item.disabled"
      :active-text="item.activetext"
      :inactive-text="item.inactivetext"
      :active-color="item.activecolor"
      :inactive-color="item.inactivecolor"
      :size="$store.state.params.form.size || item.size || 'mini'"
      @change="formChange"
    ></el-switch>

    <el-slider
      v-if='item.type==="slider"'
      v-model="forms[item.prop]"
      :min="item.min"
      :max="item.max"
      :disabled="item.disabled"
      :step="item.step"
      :show-stops="item.showstops"
      :show-input="item.showinput"
      :input-size="$store.state.params.form.size || item.size || 'mini'"
      :range="item.range"
      :size="$store.state.params.form.size || item.size || 'mini'"
      @change="formChange"
    ></el-slider>

    <el-time-select
      v-if='item.type==="timeSelect"'
      v-model="forms[item.prop]"
      :disabled="item.disabled"
      :clearable="item.clearable"
      :picker-options="item.pickeroptions"
      :placeholder="item.placeholder || item.label"
      :prefix-icon="item.prefixicon"
      value-format="HH:mm:ss"
      :default-value="item.defaultValue"
      :size="$store.state.params.form.size || item.size || 'mini'"
      @change="formChange"
    ></el-time-select>

    <el-time-picker
      v-if='item.type==="timePicker"'
      v-model="forms[item.prop]"
      :disabled="item.disabled"
      :clearable="item.clearable"
      :placeholder="item.placeholder || item.label"
      :prefix-icon="item.prefixicon"
      :is-range="item.isrange"
      :start-placeholder="item.startplaceholder"
      :end-placeholder="item.endplaceholder"
      :range-separator="item.rangeseparator"
      value-format="HH:mm:ss"
      :default-value="item.defaultValue"
      :size="$store.state.params.form.size || item.size || 'mini'"
      @change="formChange"
    ></el-time-picker>

    <el-date-picker
      v-if='(item.type==="datePicker") && ( item.datetype=="date" || item.datetype=="dates" || item.datetype=="daterange")'
      v-model="forms[item.prop]"
      :type="item.datetype"
      :placeholder="item.placeholder || item.label"
      :clearable="item.clearable"
      :disabled="item.disabled"
      :prefix-icon="item.prefixicon"
      value-format="yyyy-MM-dd"
      :start-placeholder="item.startplaceholder"
      :end-placeholder="item.endplaceholder"
      :range-separator="item.rangeseparator"
      :picker-options="startEndLimitHandle"
      :default-value="item.defaultValue"
      :size="$store.state.params.form.size || item.size || 'mini'"
      align="center"
      @change="formChange"
    ></el-date-picker>
    <el-date-picker
      v-if='(item.type==="datePicker") && item.datetype=="week"'
      v-model="forms[item.prop]"
      type="week"
      format="yyyy 第 WW 周"
      value-format="yyyy-MM-dd"
      :placeholder="item.placeholder || item.label"
      :clearable="item.clearable"
      :disabled="item.disabled"
      :prefix-icon="item.prefixicon"
      :start-placeholder="item.startplaceholder"
      :end-placeholder="item.endplaceholder"
      :range-separator="item.rangeseparator"
      :picker-options="startEndLimitHandle"
      :default-value="item.defaultValue"
      :size="$store.state.params.form.size || item.size || 'mini'"
      align="center"
      @change="formChange"
    ></el-date-picker>
    <el-date-picker
      v-if='(item.type==="datePicker")&& ( item.datetype=="month"|| item.datetype=="monthrange" )'
      v-model="forms[item.prop]"
      :type="item.datetype"
      :placeholder="item.placeholder || item.label"
      :clearable="item.clearable"
      :disabled="item.disabled"
      :prefix-icon="item.prefixicon"
      value-format="yyyy-MM"
      :start-placeholder="item.startplaceholder"
      :end-placeholder="item.endplaceholder"
      :range-separator="item.rangeseparator"
      :picker-options="startEndLimitHandle"
      :default-value="item.defaultValue"
      :size="$store.state.params.form.size || item.size || 'mini'"
      align="center"
      @change="formChange"
    ></el-date-picker>
    <el-date-picker
      v-if='(item.type==="datePicker") && item.datetype=="year"'
      v-model="forms[item.prop]"
      type="year"
      :placeholder="item.placeholder || item.label"
      :clearable="item.clearable"
      :disabled="item.disabled"
      :prefix-icon="item.prefixicon"
      value-format="yyyy"
      :start-placeholder="item.startplaceholder"
      :end-placeholder="item.endplaceholder"
      :range-separator="item.rangeseparator"
      :picker-options="startEndLimitHandle"
      :default-value="item.defaultValue"
      :size="$store.state.params.form.size || item.size || 'mini'"
      align="center"
      @change="formChange"
    ></el-date-picker>
    <el-date-picker
      v-if='(item.type==="datePicker") && (item.datetype=="datetime" || item.datetype=="datetimerange")'
      v-model="forms[item.prop]"
      :type="item.datetype"
      :placeholder="item.placeholder || item.label"
      :clearable="item.clearable"
      :disabled="item.disabled"
      :prefix-icon="item.prefixicon"
      value-format="yyyy-MM-dd HH:mm:ss"
      :start-placeholder="item.startplaceholder"
      :end-placeholder="item.endplaceholder"
      :range-separator="item.rangeseparator"
      :picker-options="startEndLimitHandle"
      :default-value="item.defaultValue"
      :size="$store.state.params.form.size || item.size || 'mini'"
      :default-time="item.defaultTime || (item.datetype=='datetimerange' ? ['00:00:00', '00:00:00'] : '00:00:00')"
      align="center"
      @change="formChange"
    ></el-date-picker>

    <el-radio-group
      v-if='(item.type==="radio")'
      v-model="forms[item.prop]"
      :disabled="item.disabled"
      :size="$store.state.params.form.size || item.size || 'mini'"
      :class="{'vertical-arrange': item.arrangeType == 'vertical'}"
      @change="formChange"
    >
      <div v-if='item.radiotype==="yuan" || !item.radiotype'>
        <el-radio
          v-for="(item2) in selectOption"
          :key="item2[(item.props && item.props.value) || 'value']+item2[(item.props && item.props.label) || 'label']+'yuan'"
          :label="item2[(item.props && item.props.value) || 'value']"
          :disabled="item2.disabled"
        >{{item2[(item.props && item.props.label) || 'label']}}</el-radio>
      </div>
      <div v-if='item.radiotype==="button"'>
        <el-radio-button
          v-for="(item2) in selectOption"
          :key="item2[(item.props && item.props.value) || 'value'] + item2[(item.props && item.props.label) || 'label'] +'but'"
          :label="item2[(item.props && item.props.value) || 'value']"
          :disabled="item2.disabled"
        >{{item2[(item.props && item.props.label) || 'label']}}</el-radio-button>
      </div>
    </el-radio-group>

    <el-checkbox-group
      v-if='(item.type==="checkbox") && forms[item.prop]'
      v-model="forms[item.prop]"
      :disabled="item.disabled"
      :border="item.border"
      :min="item.min"
      :max="item.max"
      :size="$store.state.params.form.size || item.size || 'mini'"
      :class="{'vertical-arrange': item.arrangeType == 'vertical'}"
      @change="formChange"
    >
      <div v-if='(item.checkboxtype=== "fang" || !item.checkboxtype) && selectOption && selectOption.length > 0'>
        <el-checkbox
          v-for="(item2) in selectOption"
          :key="item2[(item.props && item.props.value) || 'value']+item.label"
          :label="item2[(item.props && item.props.value) || 'value']"
          :disabled="item2.disabled"
        >{{item2[(item.props && item.props.label) || 'label']}}</el-checkbox>
      </div>
      <div v-if='item.checkboxtype=== "button" && selectOption && selectOption.length > 0'>
        <el-checkbox-button
          v-for="(item2) in selectOption"
          :key="'checkbut'+item2[(item.props && item.props.value) || 'value']+item.label"
          :label="item2[(item.props && item.props.value) || 'value']"
          :disabled="item2.disabled"
        >{{item2[(item.props && item.props.label) || 'label']}}</el-checkbox-button>
      </div>
    </el-checkbox-group>

    <!-- 颜色选择器 -->
    <el-color-picker v-if="(item.type == 'colorSelect')" v-model="forms[item.prop]" :placeholder="item.placeholder || item.label" :predefine="predefineColors" @change="formChange"></el-color-picker>
    <jvs-colorpicker v-if="item.type == 'colorPicker'" :modelValue="forms[item.prop] ? forms[item.prop] : undefined" :resetColor="item.resetColor || item.defaultValue" :prop="item.prop" :form="forms" :allowEmpty="item.clearable" @update:modelValue="colorChangeHandle"></jvs-colorpicker>

    <!-- 图片 -->
    <ul
      class="el-upload-list el-upload-list--picture-card"
      v-if='item.type==="image" && forms[item.prop] && forms[item.prop].length > 0'
    >
      <li
        tabindex="0"
        class="el-upload-list__item is-success"
        v-for="mi in forms[item.prop]"
        :key="'image'+mi.url"
        @click="handlePictureCardPreview(mi.url)"
      >
        <el-image style="width: 100%; height: 100%;" :src="mi.url" :fit="item.fit || 'contain'">
          <div slot="error" class="image-slot loading-back" style="position:absolute;">
            <i class="el-icon-loading" style="font-size: 24px;color:#999;"></i>
          </div>
        </el-image>
      </li>
      <li
        tabindex="0"
        class="el-upload-list__item"
        v-if='!forms[item.prop] || forms[item.prop].length==0'
      >
        <el-image style="width: 100%; height: 100%;" src :fit="item.fit || 'contain'">
          <div slot="error" class="image-slot loading-back" style="position:absolute;">
            <i class="el-icon-loading" style="font-size: 24px;color:#999;"></i>
          </div>
        </el-image>
      </li>
    </ul>
    <!-- 没有图片 -->
    <span v-if='item.type==="image" && (!forms[item.prop] || forms[item.prop].length==0)'>无</span>

    <!-- 上传图片 -->
    <el-upload
      v-if='(item.type === "imageUpload")'
      :class="((item.parentKey && item.parentType == 'tableForm') ? (tableFileList[tableRowAIndex] && tableFileList[tableRowAIndex].length || 0) : item.fileList.length) < (item.limit ? item.limit : 5) ? 'form-list-upload-img' : 'form-list-upload-img-none'"
      :ref="'uploadImageBtn'+'_'+item.prop"
      :action="item.action || (item.uploadHttp && item.uploadHttp.url) || ''"
      :multiple="item.multipleUpload"
      :limit="item.limit || 5"
      :headers="item.headers || {}"
      :file-list="(item.parentKey && item.parentType == 'tableForm') ? tableFileList[tableRowAIndex] : item.fileList"
      :size="$store.state.params.form.size || item.size || 'mini'"
      list-type="picture-card"
      :data="formatUploadData(item)"
      accept=".jpg,.jpeg,.png,.gif,.bmp,.JPG,.JPEG,.PBG,.GIF,.BMP"
      :auto-upload="true"
      :disabled="item.disabled"
      :on-success="handleSuccess"
      :on-error="item.handleError"
      :on-preview="handlePictureCardPreviewUpload"
      :on-remove="handleRemove"
      :on-change="uploadChangeHandle"
      :before-upload="beforeUpload"
    >
      <i class="el-icon-plus"></i>
      <div v-if="imageValidate" slot="tip" class="el-upload__tip" style="color: #F56C6C;font-size: 12px;">只能上传图片，且不超过20M</div>
    </el-upload>

    <!-- 预览图片 -->
    <el-dialog
      v-if='item.type === "imageUpload" || item.type === "image"'
      class="preViewDialog"
      :visible.sync="dialogVisible"
      append-to-body
    >
      <img width="100%" :src="dialogImageUrl" alt />
    </el-dialog>

    <!-- 上传文件 -->
    <el-upload
      v-if='item.type === "fileUpload"'
      :accept="item.fileType ? item.fileType : '*'"
      :class="((item.parentKey && item.parentType == 'tableForm') ? (tableFileList[tableRowAIndex] && tableFileList[tableRowAIndex].length || 0) : item.fileList.length) < (item.limit ? item.limit : 5) ? 'form-list-upload-file' : 'form-list-upload-file-none'"
      :ref="'uploadFileBtn'+'_'+item.prop"
      action=""
      :show-file-list="false"
      :multiple="item.multipleUpload"
      :limit="item.limit"
      :headers="item.headers"
      :file-list="(item.parentKey && item.parentType == 'tableForm') ? ((tableFileList[tableRowAIndex] && tableFileList[tableRowAIndex].length > 0) ? tableFileList[tableRowAIndex] : item.fileList) : item.fileList"
      :size="$store.state.params.form.size || item.size || 'mini'"
      :data="formatUploadData(item)"
      :disabled="item.disabled"
      :on-remove="handleRemove"
      :on-change="uploadChangeHandle"
      :on-preview="handleFilePreviewUpload"
      :on-exceed="handleExceed"
      :http-request="handleUploadRequest"
    >
      <el-button slot="trigger" size="mini" type="primary">选取文件</el-button>
      <div slot="tip">
        <upload-list
          class="jvs-upload-file-text-list"
          listType="text"
          :disabled="item.disabled"
          :handlePreview="previewFile"
          :files="(item.parentKey && item.parentType == 'tableForm') ? ((tableFileList[tableRowAIndex] && tableFileList[tableRowAIndex].length > 0) ? tableFileList[tableRowAIndex] : item.fileList) : item.fileList"
          @remove="handleRemove"
          @download="handleFilePreviewUpload"
        >
        </upload-list>
        <div v-if="fileValidate" class="el-upload__tip" style="color: #F56C6C;font-size: 12px;line-height: 12px;">文件大小不超过{{item.fileSize}}MB</div>
      </div>
    </el-upload>

    <!-- 文件列表 -->
    <ul class="el-upload-list el-upload-list--text" v-if="item.type==='file' && forms[item.prop] && forms[item.prop].length > 0">
      <li
        class="el-upload-list__item is-success"
        v-for="(fi,index) in forms[item.prop]"
        :key="'file' + fi.url"
      >
        <a
          class="el-upload-list__item-name"
          target="_blank"
          @click="getPreviewUrl(forms, item, index)"
          >
          <!-- :href="fi.url ? fi.url : 'javascript:void(0)'" -->
          <i class="el-icon-document"></i>
          {{fi.name}}
        </a>
        <label class="el-upload-list__item-status-label">
          <i class="el-icon-upload-success el-icon-circle-check"></i>
        </label>
        <i class="el-icon-close"></i>
      </li>
      <!-- <li class="el-upload-list__item" v-if='!forms[item.prop] || forms[item.prop].length==0'>
        <a class="el-upload-list__item-name">
          <i class="el-icon-document"></i>
          {{'文件名称'}}
        </a>
      </li> -->
    </ul>
    <span v-if="item.type==='file' && (!forms[item.prop] || forms[item.prop].length==0)">无</span>

    <!-- 选项卡 -->
    <div v-if="item.type=='tab'" style="width: 100%;">
      <collapseForm
        v-if="item.status == 'collapse'"
        :originOption="originOption"
        :defalutSet="defalutSet"
        :formRef="formRef"
        :active="item.activeName"
        :formItem="item"
        :forms="item.detachData ? forms : forms[item.prop]"
        :option="{type:'card', column: item.dicData}"
        :roleOption="roleOption"
        :userList="userList"
        :departmentList="departmentList"
        :postList="postList"
        :resetRadom="resetRadom"
        :originForm="forms"
        :designId="designId"
        :changeRandom="changeRandom"
        :changeDomItem="changeDomItem"
        :isView="isView"
        :dataModelId="dataModelId"
        :execsList="execsList"
        :jvsAppId="jvsAppId"
        :dataTriggerFresh="dataTriggerFresh"
        :ruleChange="ruleChange"
        @reInitData="reInitData"
        @tab-click="tabClick"
        @formChange="tabFormchange"
      ></collapseForm>
      <jvs-tab
        v-else
        :originOption="originOption"
        :defalutSet="defalutSet"
        :formRef="formRef"
        :active="item.activeName"
        :formItem="item"
        :forms="item.detachData ? forms : forms[item.prop]"
        :option="{type:'card', column: item.dicData}"
        :roleOption="roleOption"
        :userList="userList"
        :departmentList="departmentList"
        :postList="postList"
        :resetRadom="resetRadom"
        :originForm="forms"
        :designId="designId"
        :changeRandom="changeRandom"
        :changeDomItem="changeDomItem"
        :isView="isView"
        :dataModelId="dataModelId"
        :execsList="execsList"
        :jvsAppId="jvsAppId"
        :dataTriggerFresh="dataTriggerFresh"
        :ruleChange="ruleChange"
        @reInitData="reInitData"
        @tab-click="tabClick"
        @formChange="tabFormchange">
      </jvs-tab>
    </div>


    <!-- 展示的表格 -->
    <!-- <el-table
      v-if="item.type==='TableReadOnly' || item.type==='tableReadOnly'"
      :data="forms[item.prop]"
      :border="item.border"
      highlight-current-row
      class="tb-edit"
      align="center"
      style="width: 100%;cursor:pointer;"
    >
      <el-table-column
        v-for="(ti) in item.option"
        :key="''+ti.value+'table'"
        :prop="ti.value"
        :label="ti.label"
        style="text-align:center;"
      ></el-table-column>
    </el-table> -->

    <!-- 描述框 -->
    <div
      v-if="item.type==='box'"
      :style="'width:100%;padding:16px;border-radius:4px;white-space: normal;text-align:'+item.contentposition+';font-size:'+item.fontsize+'px;color:'+item.textcolor+';font-weight:'+item.fontweight+';background-color:'+(item.boxback?item.boxback:'none')+';'"
    >
      {{forms[item.prop]}}
      <span v-if='!forms[item.prop]' v-html="getHtmlText(item.text)" style="white-space: normal;"></span>
    </div>
    <!-- 链接 -->
    <a
      v-if="item.type =='link'"
      :href="forms[item.prop]?formatUrl(forms[item.prop]):'javascript:void(0);'"
      :target="item.openType"
      :style="'height: 36px;line-height: 36px;text-align:'+item.contentposition+';font-size:'+item.fontsize+'px;color:'+item.textcolor+';font-weight:'+item.fontweight+';text-decoration:'+item.textdecoration+';'"
    >{{item.text}}</a>
    <!-- 嵌入页面 -->
    <div v-if="item.type==='iframe'" :style="'width:100%;height:'+item.iframeheight+'px;background:#ecf5ff;'">
      <iframe
        :name="item.id"
        :id="item.prop"
        :src="forms[item.prop] || item.iframeurl"
        frameborder="0"
        width="100%"
        height="100%"
        scrolling="scroll"
      ></iframe>
    </div>

    <!-- 图标选择器 -->
     <div class="form-item-icon-selct" style="position: relative;" v-if="item.type == 'iconSelect'" :id="'icon-select-item-'+item.prop">
       <el-popover
          placement="bottom"
          :width="iconToolWidth"
          v-if="!item.disabled"
          trigger="click">
          <div v-if="item.iconType == 'svg'" class="icon-select-tool">
            <svg v-for="(it, itx) in iconList" :key="itx+it" @click="checkIcon(item.prop, it)" class="icon" aria-hidden="true">
              <use :xlink:href="'#'+it"></use>
            </svg>
          </div>
          <div v-else class="icon-select-tool">
            <i v-for="(it, itx) in iconList" :key="itx+it" :class="it" @click="checkIcon(item.prop, it)"></i>
          </div>
          <div slot="reference" style="display:flex;align-items: center;">
            <el-input v-model="forms[item.prop]" placeholder="图标" :disabled="item.disabled"></el-input>
            <svg v-if="forms[item.prop] && item.iconType == 'svg'" class="icon" aria-hidden="true" style="margin-left:10px;width: 20px;height: 20px;">
              <use :xlink:href="'#'+forms[item.prop]"></use>
            </svg>
            <i v-if="forms[item.prop] && item.iconType != 'svg'" :class="forms[item.prop]" style="margin-left:10px;"></i>
          </div>
        </el-popover>
        <div v-else style="display:flex;align-items: center;">
          <el-input v-model="forms[item.prop]" placeholder="图标" :disabled="item.disabled"></el-input>
          <i v-if="forms[item.prop] && item.iconType != 'svg'" :class="forms[item.prop]" style="margin-left:10px;"></i>
          <svg v-if="forms[item.prop] && item.iconType == 'svg'" class="icon" aria-hidden="true" style="margin-left:10px;width: 20px;height: 20px;">
            <use :xlink:href="'#'+forms[item.prop]"></use>
          </svg>
        </div>
      </div>

    <!-- p文字 -->
    <p v-if="item.type === 'p'" class="form-item-p" :style="`text-align:${item.contentposition};font-size:${item.fontsize}px;color:${item.textcolor};margin:0;`">
      <span :style="item.barHide ? 'padding-left: 0;' : ''">
        <i v-if="item.barHide !== true"></i>
        <b>{{item.text}}</b>
      </span>
    </p>

    <!-- 分割线 -->
    <el-divider v-if="item.type === 'divider'" :content-position='item.contentposition'>{{item.text}}</el-divider>

    <!-- 流水号 -->
   <!-- <div
     v-if="item.type==='serialNumber' && forms[item.prop]"
     :style="'width:100%;padding:0 16px;border-radius:4px;'"
   >
     {{forms[item.prop]}}
     <span v-if='!forms[item.prop]'>&#45;&#45;</span>
   </div> -->
    <el-input v-if="item.type==='serialNumber'" disabled v-model="forms[item.prop]" placeholder="根据流水号规则自动生成" class="show-disable"></el-input>

    <el-input v-if="item.type==='positionMap'" disabled v-model="forms[item.prop]" placeholder="请用移动端打开页面获取位置信息" class="show-disable"></el-input>

    <!-- 签字版 -->
		<div v-if="item.type =='signature'" class="jvs-signature-item">
			<div :class="{'signature-content': true, 'empty-signature': !forms[item.prop]}">
				<Signature ref="sig" v-model="forms[item.prop]" :prop="item.prop" :disabled="item.disabled" @submit="signatureSubmit"></Signature>
			</div>
		</div>

    <!-- 用户组件 -->
    <userForm
      v-if="['user', 'role', 'department', 'group', 'job'].indexOf(item.type) > -1"
      :form="forms"
      :prop="item.prop"
      :placeholder="item.placeholder || ''"
      :selectable="item.multiple"
      :defaultValue="item.defaultValue"
      :enableinput="item.allowinput"
      :disabled="item.disabled"
      :deptable="item.deptable"
      :infoable="item.infoable"
      :props="item.props ? item.props : {'label': (item.prop+'_1'), 'value': item.prop}"
      :type="item.type"
      :resetRadom="resetRadom"
      :userAllList="userList"
      :departmentList="departmentList"
      :roleOption="roleOption"
      :postList="postList"
      :dataTriggerFresh="dataTriggerFresh"
      :rangeOption="item.rangeOption || null"
      :showalllevels="item.showalllevels"
      @change="formChange" />
    <!-- 部门组件 -->
    <el-cascader
      v-if="false && item.type==='department'"
      v-model="forms[item.prop]"
      size="mini"
      :options="departmentList"
      clearable
      :show-all-levels="item.showalllevels"
      :collapse-tags="!item.collapsetags"
      :disabled="item.disabled"
      :props="{
        expandTrigger: 'hover',
        multiple: item.multiple === false ? item.multiple : true,
        children: 'childList',
        label: 'name',
        value: 'id',
        emitPath: item.emitPath
      }"
      @change="formChange"
    >
    </el-cascader>
    <!-- 角色组件 -->
    <el-select v-model="forms[item.prop]" filterable placeholder="请选择角色" :disabled="item.disabled" v-if="false && item.type==='role'" :multiple='item.multiple' @change="changeHandle">
      <el-option
        v-for="ir in roleOption"
        :key="ir.id+'role'+ir.roleName"
        :label="ir.roleName"
        :value="ir.id">
      </el-option>
    </el-select>
    <!-- 岗位组件 -->
    <el-select v-model="forms[item.prop]" filterable placeholder="请选择岗位" v-if="false && item.type==='job'"
      :multiple='item.multiple' :disabled="item.disabled" @change="changeHandle">
      <el-option
        v-for="ir in postList"
        :key="ir.id+'job'+ir.name"
        :label="ir.name"
        :value="ir.id">
      </el-option>
    </el-select>

    <!-- 可编辑表格 -->
    <div v-if="['tableForm'].indexOf(item.type) > -1" style="flex:1;width:100%;">
      <tableForm :formRef="formRef" :item="item" :option="tableFormOption"
        :data="forms[item.prop]" :originOption="originOption"
        :defalutSet="defalutSet" :rowData="rowData"
        :roleOption="roleOption"
        :userList="userList"
        :departmentList="departmentList"
        :postList="postList"
        :resetRadom="resetRadom"
        :designId="designId"
        :forms="forms"
        :originForm="originForm ? originForm : forms"
        :dataModelId="dataModelId"
        :changeRandom="changeRandom"
        :changeDomItem="changeDomItem"
        :isView="isView"
        :execsList="execsList"
        :jvsAppId="jvsAppId"
        :dataTriggerFresh="dataTriggerFresh"
        :tableFormAddHandleIndex="tableFormAddHandleIndex"
        :ruleChange="ruleChange"
        @formChange="$emit('formChange', forms)"
        @reInitData="reInitData"
        @setTable="setTableHandle"
        @resetAddIndex="resetAddIndex">
        <template slot="menuBtn" slot-scope="scope">
          <jvs-button v-if="!item.disabled  && !item.iconBtn && item.addBtnOrigin != 'table' && !item.editable && item.editBtn" type="text" @click="openFormOutHandle('edit', scope.row, scope.index)">{{item.editBtnText || '编辑'}}</jvs-button>
          <jvs-button v-if="!item.disabled  && !item.iconBtn && item.addBtnOrigin != 'table' && !item.editable && item.viewBtn" type="text" @click="openFormOutHandle('view', scope.row, scope.index)">{{item.viewBtnText || '详情'}}</jvs-button>
          <jvs-button v-if="!item.disabled  && !item.iconBtn && item.delBtn" type="text" @click="deleteRow(scope.row, scope.index)">{{item.delBtnText || '删除'}}</jvs-button>
          <span v-if="!item.disabled && item.iconBtn === true && (item.delBtn !== false)" class="delete-icon-button" @click="deleteRow(scope.row, scope.index)">
            <span class="border-line"></span>
          </span>
        </template>
      </tableForm>
      <el-row style="margin-top:10px;" v-if="item.addBtn && !item.disabled && !item.iconBtn">
        <jvs-button :loading="item.addBtnOrigin == 'table' ? this.searchLoading : false" size="small" type="primary" @click="addRowHandle">{{item.addBtnText || '新增'}}</jvs-button>
      </el-row>
      <div v-if="item.addBtn && !item.disabled && item.iconBtn === true" class="bottom-add-button">
        <div class="button" @click="addRowHandle">
          <div class="icon">
            <svg aria-hidden="true">
              <use xlink:href="#jvs-ui-icon-xinjian"></use>
            </svg>
          </div>
          <span>{{item.addBtnText || '新增'}}</span>
        </div>
      </div>
    </div>

    <!-- 流程设计 -->
    <div v-if="['flowTable'].indexOf(item.type) > -1" style="flex:1;width:100%;">
      <flowTable :formRef="formRef" :item="item" :option="tableFormOption"
        :data="forms[item.prop]" :originOption="originOption"
        :defalutSet="defalutSet" :rowData="rowData"
        :roleOption="roleOption"
        :userList="userList"
        :departmentList="departmentList"
        :postList="postList"
        :resetRadom="resetRadom"
        :designId="designId"
        :forms="forms"
        :dataModelId="dataModelId"
        :changeRandom="changeRandom"
        :changeDomItem="changeDomItem"
        :isView="isView"
        :execsList="execsList"
        :jvsAppId="jvsAppId"
        :dataTriggerFresh="dataTriggerFresh"
        @reInitData="reInitData"
        @setTable="setTableHandle">
        <template slot="menuBtn" slot-scope="scope">
          <jvs-button v-if="item.editable && item.delBtn" type="text" @click="deleteRow(scope.row, scope.index)">删除</jvs-button>
        </template>
      </flowTable>
      <el-row style="margin-top:10px;" v-if="item.editable && item.addBtn">
        <jvs-button size="mini" @click="addFlowRowHandle">新增</jvs-button>
      </el-row>
    </div>

    <!-- 执行流程 -->
    <div v-if="['flowNode'].indexOf(item.type) > -1" style="flex:1;width:100%;">
      <flowNode
        :formRef="formRef" :item="item"
        :data="forms[item.prop]"
        :originOption="originOption"
        :defalutSet="defalutSet"
        :rowData="rowData"
        :roleOption="roleOption"
        :userList="userList"
        :departmentList="departmentList"
        :postList="postList"
        :resetRadom="resetRadom"
        :designId="designId"
        :forms="forms"
        :dataModelId="dataModelId"
        :changeRandom="changeRandom"
        :changeDomItem="changeDomItem"
        :isView="isView"
        :execsList="execsList"
        :jvsAppId="jvsAppId"
        :dataTriggerFresh="dataTriggerFresh"
        :openByButton="openByButton"
        @reInitData="reInitData"
        @setFlowNodeData="setTableHandle">
      </flowNode>
    </div>

    <!-- 计数器   滑块   显示单位 -->
    <span v-if="['inputNumber', 'slider'].indexOf(item.type) > -1 && item.unit" class="unit-item-empty-span" style="padding-left: 5px;">{{(item.disabled && (!forms[item.prop] && forms[item.prop] !== 0)) ? '-  ' : ''}}</span>
    <span v-if="['inputNumber', 'slider'].indexOf(item.type) > -1 && item.unit" :class="{'unit-span': item.type == 'inputNumber'}">{{item.unit.trim()}}</span>

    <!-- 地区选择 -->
    <el-cascader
      v-if="item.type==='chinaArea'"
      v-model="forms[item.prop]"
      size="mini"
      :options="chinaAreaList"
      clearable
      :show-all-levels="item.showalllevels"
      :collapse-tags="!item.collapsetags"
      :disabled="item.disabled"
      :props="{
        expandTrigger: 'hover',
        multiple: item.multiple === false ? item.multiple : true,
        label: 'name',
        value: item.emitKey ? item.emitKey : 'code',
        emitPath: item.emitPath
      }"
      @change="formChange"
    >
    </el-cascader>

    <!-- 富文本 -->
    <div v-if="item.type === 'htmlEditor'" :id="item.prop+'-'+editorUUID+'-editor'" style="z-index: 1000;width:100%;"></div>

    <!-- JSON编译器 -->
    <div v-if="['jsonEditor'].indexOf(item.type) > -1" style="flex:1;width:100%;height: 200px;position: relative;">
      <jsonEditor :lang="item.lang || 'json'" :disabled="item.disabled" :code="forms[item.prop]" :prop="item.prop" @change="jsonChange"></jsonEditor>
    </div>

    <!-- 按钮 -->
    <jvs-button v-if="item.type === 'button'" :disabled="item.disabled" :type="item.buttonType" :round="item.buttonRound" :size="item.size" :loading="item.loading" @click="btnClick">{{item.text}}</jvs-button>

    <!-- 级联选择 -->
    <el-cascader
      v-if="item.type==='cascader' && item.pickType !== 'tree' "
      v-model="forms[item.prop]"
      size="mini"
      :options="cascaderList"
      clearable
      :show-all-levels="item.showalllevels"
      :collapse-tags="!item.collapsetags"
      :disabled="item.disabled"
      :props="{
        expandTrigger: 'hover',
        multiple: item.multiple === false ? item.multiple : true,
        label: item.datatype == 'rule' ? (item.props ? item.props.label : 'name') : 'name',
        value: item.datatype == 'rule' ? (item.props ? item.props.value : 'value') : (item.datatype == 'dataModel' ? 'value' : (item.emitKey ? item.emitKey : 'value')),
        emitPath: item.emitPath,
        checkStrictly: true
      }"
      :class="{'fixed-height': !item.collapsetags}"
      @change="formChange"
    >
    </el-cascader>

    <tree-selector
      v-if="item.type==='cascader' && item.pickType === 'tree'"
      :form="forms"
      :item="item"
      :options="cascaderList"
      :showalllevels="item.showalllevels"
      :collapsetags="!item.collapsetags"
      :disabled="item.disabled"
      :props="{
        multiple: item.multiple === false ? item.multiple : true,
        label: item.datatype == 'rule' ? (item.props ? item.props.label : 'name') : 'name',
        value: item.datatype == 'rule' ? (item.props ? item.props.value : 'value') : (item.datatype == 'dataModel' ? 'value' : (item.emitKey ? item.emitKey : 'value')),
        children: 'children',
        emitPath: item.emitPath,
        checkStrictly: true
      }"
      @change="formChange"
    ></tree-selector>

    <!-- 内嵌列表页 -->
    <div v-if="item.type === 'pageTable'" class="form-page-table" :style="`max-height: ${item.maxheight}px;min-height: 200px;width: 100%;`">
      <div v-if="item.formId && item.dataModelId && item.jvsAppId" :key="item.formId" style="width: 100%;">
        <listPage
          ref="openPage"
          class="jvs-table-opend-by-form"
          :routerQuery="{id: item.formId, dataModelId: item.dataModelId, jvsAppId: item.jvsAppId}"
          :fromOtherOpen="true"
          :dialogHeight="item.maxheight"
          :pageQueryFixCondition="getPageFixQuery()"
          :pageSetting="getPageSetting()"
          :originRowData="forms"
          :isView="isView"
          :changeRandom="changeRandom"
          :changeDomItem="changeDomItem"
          @pageGetList="pageGetList"
        ></listPage>
      </div>
    </div>

    <!-- 蓝牙信标 -->
    <div v-if="item.type==='bluetoothBeacon'" style="width: 100%;">
      <div v-if="forms[item.prop] && forms[item.prop].length > 0" class="blue-tool-beacon-list">
        <div v-for="(beacon, beaindex) in forms[item.prop]" :key="item.prop + '-' + 'beacon-item-' + beaindex" class="blue-tool-beacon-list-item">
          <div class="name">{{beacon.name}}</div>
          <i v-if="!item.disabled" class="el-icon-error delete-beacon" @click="deleteBeaconHandle(beacon, beaindex)"></i>
        </div>
      </div>
      <div v-else class="no-blue-tool-beacon"></div>
    </div>

    <!-- 动态表单 -->
    <div v-if="item.type === 'dynamicForm'" style="width: 100%;" class="form-dynamic-form">
      <dynamicForm
        :ref="'dynamicForm_'+item.prop"
        :formRef="formRef" :item="item" :option="tableFormOption"
        :data="forms[item.prop]" :originOption="originOption"
        :defalutSet="defalutSet" :rowData="rowData"
        :resetRadom="resetRadom"
        :designId="designId"
        :forms="forms"
        :originForm="originForm ? originForm : forms"
        :dataModelId="dataModelId"
        :changeRandom="changeRandom"
        :changeDomItem="changeDomItem"
        :isView="isView"
        :execsList="execsList"
        :jvsAppId="jvsAppId"
        :dataTriggerFresh="dataTriggerFresh"
        @comTypeChange="comTypeChange"
        @deleteCom="deleteCom">
      </dynamicForm>
      <el-row style="margin-top:10px;">
        <jvs-button size="mini" @click="addDymicFormRowHandle">新增</jvs-button>
      </el-row>
    </div>

    <!-- 数据源 -->
    <datasourceForm
      style="flex:1;"
      v-if="item.type==='datasource'"
      :item="item"
      :form="forms"
      :prop="item.prop"
      :selectable="item.multiple"
      :disabled="item.disabled"
      :resetRadom="resetRadom"
      @change="formChange" />

    <!-- 步骤条 -->
    <stepBar
      :formRef="formRef"
      v-if="item.type=='step'"
      :active="item.activeName"
      :formItem="item"
      :forms="forms[item.prop]"
      :option="{column: item.dicData}"
      :originOption="originOption"
      :defalutSet="defalutSet"
      :roleOption="roleOption"
      :userList="userList"
      :departmentList="departmentList"
      :postList="postList"
      :rowData="rowData"
      :resetRadom="resetRadom"
      :originForm="forms"
      :designId="designId"
      :changeRandom="changeRandom"
      :changeDomItem="changeDomItem"
      :isView="isView"
      :dataModelId="dataModelId"
      :execsList="execsList"
      :jvsAppId="jvsAppId"
      :dataTriggerFresh="dataTriggerFresh"
      :ruleChange="ruleChange"
      @reInitData="reInitData"
      @formChange="tabFormchange">
    </stepBar>

    <!-- 表单卡片 -->
    <formCard
      v-if="item.type == 'formbox'"
      :formItem="item"
      :forms="forms[item.prop]"
      :originOption="originOption"
      :defalutSet="defalutSet"
      :roleOption="roleOption"
      :userList="userList"
      :departmentList="departmentList"
      :postList="postList"
      :resetRadom="resetRadom"
      :designId="designId"
      :execsList="execsList"
      :jvsAppId="jvsAppId"
      :dataTriggerFresh="dataTriggerFresh"
      @formChange="tabFormchange"
      >
    </formCard>

    <!-- 静态表格  报表类 -->
    <reportTable
      v-if="item.type == 'reportTable'"
      :formItem="item"
      :forms="forms[item.prop]"
      :originOption="originOption"
      :defalutSet="defalutSet"
      :roleOption="roleOption"
      :userList="userList"
      :departmentList="departmentList"
      :postList="postList"
      :resetRadom="resetRadom"
      :designId="designId"
      :execsList="execsList"
      :jvsAppId="jvsAppId"
      :dataTriggerFresh="dataTriggerFresh"
      @formChange="tabFormchange"
    ></reportTable>

    <!-- 时间线 -->
    <el-timeline v-if="item.type == 'timeline' && forms[item.prop]" style="max-height: 300px; overflow: hidden; overflow-y: auto;">
      <el-timeline-item
        v-for="(activity, index) in forms[item.prop]"
        :key="item.prop + 'timeline-' + index"
        :timestamp="activity[item.timestamp || 'timestamp']">
        <div v-html="activity[item.content || 'content']"></div>
      </el-timeline-item>
    </el-timeline>

    <!-- 子表单 -->
    <childrenForm
      v-if="item.type == 'childrenForm'"
      :formItem="item"
      :originOption="originOption"
      :forms="forms[item.prop]"
      :resetRadom="resetRadom"
      :designId="designId"
      @formChange="tabFormchange"
    ></childrenForm>

    <!-- 关联表单 -->
    <childrenForm
      v-if="item.type == 'connectForm'"
      :formItem="item"
      :selectOption="selectOption"
      :originOption="originOption"
      :forms="forms[item.prop]"
      :resetRadom="resetRadom"
      :designId="designId"
      @formChange="formChange"
    ></childrenForm>

    <!-- 右侧提示 -->
    <el-tooltip v-if="item.tipsEnable !== false && item.tips && item.tips.position == 'right' && item.tips.text" class="form-item-tooltip" effect="dark" :content="item.tips.text" placement="top">
      <i class="el-icon-question"></i>
    </el-tooltip>

    <!-- 后置插槽 -->
    <slot v-if="item.appendSlot" :name="item.prop+'AppendItem'"></slot>

    <!-- 搜索弹框 -->
    <el-dialog
      v-if="((item.type==='input' || !item.type) && item.searchable) || (['tableForm'].indexOf(item.type) > -1 && item.addBtnOrigin == 'table' && item.addBtnFormId)"
      :title="openSearchOprate == 'addOpenTable' ? (item.addBtnText || '新增') : item.label"
      :visible.sync="searchVisible"
      append-to-body
      :close-on-click-modal="false"
      :width="(['tableForm'].indexOf(item.type) > -1 && item.addBtnOrigin == 'table') ? (item.addPopupWidth ? (item.addPopupWidth + '%') : '50%') : (item.searchPopupWidth ? (item.searchPopupWidth + '%') : '50%')"
      :fullscreen="((['tableForm'].indexOf(item.type) > -1 && item.addBtnOrigin == 'table') ? (item.addPopupWidth ? (item.addPopupWidth + '%') : '50%') : (item.searchPopupWidth ? (item.searchPopupWidth + '%') : '50%')) == '100%' ? true : false"
      :before-close="searchClose">
      <div v-if="searchVisible" class="table-show search-form-item-table">
        <jvs-table
          :option="searchOption"
          :data="searchData"
          :index="true"
          :page="page"
          :loading="searchLoading"
          :selectable="openSearchOprate == 'addOpenTable' ? true : item.multiple"
          :selectedRows="selectedRows"
          @on-load="getListData"
          @current-change="handleCurrentChange"
          @row-click="rowClick"
          @selection-change="selectChange">
          <!-- 查询条件 -->
          <template slot="headerTop">
            <jvs-form class="search-form" v-if="searchFormOption.column.length > 0" :option="searchFormOption" :formData="queryParams" @submit="queryHandle" @reset="resetQueryHandle" :isSearch="true" :designId="$route.query.id || designId" :jvsAppId="jvsAppId" :initNotRequest="true">
              <template v-for="sitem in queryBeforeSlot" :slot="sitem.prop+'Before'">
                <el-select v-model="queryOprator[sitem.prop]" placeholder="请选择" :key="sitem.prop+'-before-search'" class="bofore-append" size="mini">
                  <el-option v-for="op in sitem.list" :key="sitem.prop+'-option-item-'+op.value" :label="op.label" :value="op.value"></el-option>
                </el-select>
              </template>
            </jvs-form>
          </template>
          <!-- 多选底部左侧确认按钮 -->
          <template slot="menuLeftBottom">
            <div v-if="openSearchOprate == 'addOpenTable' ? true : item.multiple">
              <el-button type="primary" size="mini" @click="searchSubmit">确认</el-button>
              <el-button size="mini" @click="searchClose">取消</el-button>
            </div>
          </template>
        </jvs-table>
      </div>
    </el-dialog>

    <!-- 打开表单 -->
    <el-dialog
      v-if="['tableForm'].indexOf(item.type) > -1 && ((item.addBtn && item.addBtnOrigin == 'form') || (!item.editable && (item.editBtn || item.viewBtn)))"
      :title="openFormTitle"
      :width="openFormWidth || '50%'"
      :visible.sync="openFormVisible"
      append-to-body
      :fullscreen="openFormWidth == '100%'"
      :close-on-click-modal="false"
      :before-close="openFormClose">
      <div v-if="openFormVisible">
        <jvs-form :option="openFormOption" :formData="openForm" @submit="openFormSubmit" @cancalClick="openFormClose"></jvs-form>
      </div>
    </el-dialog>
    <formDialog
      v-if="['tableForm'].indexOf(item.type) > -1 && ((item.addBtn && item.addBtnOrigin == 'form') || (!item.editable && (item.editBtn || item.viewBtn)))"
      ref="formDialog"
      :title="openFormTitle"
      :jvsAppId="jvsAppId"
      :dataModelId="item.addBtnOrigin == 'table' ? (item.addBtnFormId || dataModelId) : (item.addBtnOrigin == 'form' ? (item.formId || item.dataModelId || dataModelId) : dataModelId)"
      :getEcho="item.dataFilterEnable && item.formId"
      @submit="openFormOutSubmit"
    ></formDialog>
  </div>
</template>

<script>
import userForm from './userForm'
import iconList from '@/const/iconfont'
import entryIcon from '@/const/entryIcon'
import tableForm from '@/components/basic-assembly/tableForm'
import flowTable from '@/components/basic-assembly/flowTable'
import flowNode from '@/components/basic-assembly/flowNode'
import treeSelector from '@/components/basic-assembly/treeSelector'
import jsonEditor from '@/components/basic-assembly/jsonEditor'
import Signature from '@/components/basic-assembly/signature'
import uploadList from '@/components/basic-assembly/upload-list'
import formDialog from '@/plugin/components/dialogInfo'
import dynamicForm from '@/components/basic-assembly/dynamicForm'
import datasourceForm from './datasourceForm'
import systemIcon from '@/const/systemIcon'
import stepBar from './stepBar'
import formCard from './formcard'
import reportTable from './reportTable'
import childrenForm from './childrenForm.vue'
import {ruleRun, ruleDownLoad} from "@/api/common";
// import {getSelectData, getFetchTable, getLineDataOfGet, getFormReviewData} from '../api/tableDesignsenior'
import {getSelectData, customUpload} from '@/api/index'
import {areaList} from '@/const/chinaArea.js'
import {getClassifyDictTree, getSystemDictItems} from '@/api/newDesign'
import E from 'wangeditor'
import { getCrudDataPage } from '@/views/page/api/newDesign'
import { getUseModelAllFields, getAllWorkflowByAppUse } from '@/components/template/api'
import { uploadByPieces } from '@/util/uploadFile'
import { fileImport } from '../api'
import { getDefaultNodeProps, nodeType, nodeButtonList } from '@/views/flowable/views/design/common/enumConst'
import { Base64 } from 'js-base64'
export default {
  name: "formitem",
  components: {
    userForm,
    tableForm,
    datasourceForm,
    stepBar,
    formCard,
    reportTable,
    childrenForm,
    flowTable,
    flowNode,
    treeSelector,
    jsonEditor,
    listPage: () => import('@/views/page/views/show/listUse'),
    Signature,
    uploadList,
    formDialog,
    collapseForm: () => import('@/components/basic-assembly/collapseForm'),
    dynamicForm
  },
  props: {
    // 表单对象
    form: {
      type: Object,
      default: () => {
        return {}
      }
    },
    // 表单内的组件对象
    item: {
      type: Object,
      default: () => {
        return {}
      }
    },
    originOption: {
      type: Object
    },
    defalutSet: {
      type: Object
    },
    formRef: {
      type: String,
      default: 'ruleForm'
    },
    // 用户列表
    userList : {
      type: Array,
      default: () => {
        return []
      }
    },
    // 角色列表
    roleOption: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 部门列表
    departmentList: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 岗位列表
    postList: {
      type: Array,
      default: () => {
        return []
      }
    },
    // 是否需要刷新组件
    freshBoolean: {
      type: Boolean
    },
    // 是否需要重新初始化
    reinitFlag: {
      type: Number
    },
    // 表格行数据
    rowData: {
      type: Object
    },
    resetRadom: {
      type: Number
    },
    tableRowAIndex: {
      type: Number
    },
    designId: {
      type: String
    },
    dataModelId: {
      type: String
    },
    changeRandom: {
      type: Number
    },
    changeDomItem: {
      type: Object
    },
    isView: {
      type: Boolean
    },
    disabledControl: {
      type: Boolean
    },
    execsList: {
      type: Array
    },
    jvsAppId:  {
      type: String
    },
    originForm: {
      type: Object
    },
    parentDomWidth: {
      type: Number,
      default: ()=> {
        return 0
      }
    },
    dataTriggerFresh: {
      type: Number
    },
    delcomRandom: {
      type: Number
    },
    ruleChange: {
      type: Number
    },
    openByButton: {
      type: Object
    }
  },
  computed: {
    forms () {
      return this.form
    },
    tableFormOption: {
      get () {
        let obj = {
          addBtn: false,
          viewBtn: false,
          delBtn: false,
          editBtn: false,
          page: false,
          border: this.item.border,
          align: this.item.align || 'left',
          menuAlign: this.item.menuAlign || 'left',
          cancal: false,
          showOverflow: true,
          hideTop: this.item.hideTop || false,
          tableColumn: this.item.tableColumn
        }
        return obj
      },
      set () {}
    },
    selectedRows () {
      let temp = []
      if(['tableForm'].indexOf(this.item.type) > -1) {
        if(this.form[this.item.prop] &&  this.form[this.item.prop].length > 0) {
          this.form[this.item.prop].filter(fit => {
            temp.push({
              id: fit.id
            })
          })
        }
      }
      return temp
    }
  },
  data () {
    return {
      selectOption: [],
      startEndLimitHandle: {
        disabledDate: time => {
          let bool=false
          if (!this.item.startLimit) {
            let end=new Date(this.item.endLimit).getTime()
            if (time.getTime()<=end) {
              bool=false
            } else {
              bool=true
            }
          }
          if (!this.item.endLimit) {
            let start=new Date(this.item.startLimit).getTime()
            if (time.getTime()>=start-8.64e7) {
              bool=false
            } else {
              bool=true
            }
          }
          if (!this.item.startLimit&&!this.item.endLimit) {
            bool=false
          }
          if (this.item.startLimit&&this.item.endLimit) {
            let start=new Date(this.item.startLimit).getTime()
            let end=new Date(this.item.endLimit).getTime()
            if (time.getTime()>=start-8.64e7&&time.getTime()<=end) {
              bool=false
            } else {
              bool=true
            }
          }
          return bool
        }
      },
      errorShow: false, // 自定义验证提示错误
      dialogVisible: false, // 预览图片弹框
      dialogImageUrl: '', // 预览图片地址
      iconList: this.item.iconType == 'svg' ? [...entryIcon] : [...iconList, ...systemIcon], // 图标列表
      chinaAreaList: areaList,
      cascaderList: [], // 级联选择数据
      editor: null, // 富文本
      pathArr: [], // 路径结果
      imageValidate: false,
      fileValidate: false,
      eventList: ['button', 'input', 'textarea', 'inputNumber', 'select', 'slider', 'switch', 'datePicker', 'timeSelect', 'timePicker',
      'radio', 'checkbox', 'imageUpload', 'fileUpload', 'htmlEditor', 'cascader', 'datasource', 'chinaArea', 'department', 'role', 'user', 'job'],
      initHtml: '', // 记录富文本初始值
      iconToolWidth: 400, // 图标组件工具栏宽
      tableFileList: [ [] ],
      searchVisible: false,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        pageSizes: [20, 50, 100, 200, 500, 1000], // 分页大小
      },
      searchLoading: false,
      queryParams: {},
      searchData: [],
      searchOption: {
        page: true,
        search: true,
        menu: false,
        addBtn: false,
        isSearch: true,
        submitBtnText: '查询',
        labelWidth: 'auto',
        column: [],
        showOverflow: true,
      },
      searchFormOption: {
        page: true,
        search: true,
        menu: false,
        addBtn: false,
        isSearch: true,
        submitBtnText: '查询',
        labelWidth: 'auto',
        column: []
      },
      queryBeforeSlot: [],
      queryOprator: {},
      showThoudsandth: true,
      predefineColors: [
        '#ffd700',
        '#ff8c00',
        '#ff4500',
        '#c71585',
        '#FF99CC',
        '#FF6666',
        '#CCCCFF',
        '#CCCCCC',
        '#99CCFF',
        '#99CC99',
        '#90ee90',
        '#66CC66',
        '#669933',
        '#663366',
        '#490954',
        '#3471ff',
        '#333300',
        '#1e90ff',
        '#00ced1',
        '#003399'
      ],
      beforeValue: null,
      fileList: [],
      fileListCopy: [],
      fileUploadIndex: 0,
      fileUploadFailIndex: 0,
      uploadLoading: false,
      channelBroad: null,
      notMatch: false,
      tableFormAddHandleIndex: -1,
      searchResult: '',
      bluetoothBeacon: '',
      lastItemData: null,
      openSearchInputLoading: false,
      openFormTitle: '',
      openFormWidth: '',
      openFormIndex: -1,
      openFormVisible: false,
      openForm: null,
      openFormOption: {
        emptyBtn: false,
        column: []
      },
      openSearchOprate: '',
      openSearchWidth: '',
      openSearchList: [],
      editorUUID: URL.createObjectURL(new Blob()).substr(-36),
    };
  },
  methods: {
    changeHandle (val) {
      this.$set(this.item, 'currVal', val)
      this.$set(this.forms, this.item.prop, val)
      this.formChange()
      this.$forceUpdate()
    },
    uploadChangeHandle (file, fileList) {
      // console.log('upload change.....')
      // console.log(fileList)
      this.$forceUpdate()
      let obj={}
      obj[this.item.prop]=fileList
      this.$emit('currentValueHandle', obj)
    },
    beforeUpload (file) {
      if(file.size > 20971520) {
        if(this.item.type == 'imageUpload') {
          this.imageValidate = true
        }else{
          this.fileValidate = true
        }
        return false
      }else{
        if(this.item.type == 'imageUpload') {
          this.imageValidate = false
        }else{
          this.fileValidate = false
        }
      }
    },
    async initItem (stopGetDicUrl, loadType) {
      if(!this.item.disabled) {
        if(this.item.disabledControl == true) {
          this.item.disabled = true
        }
      }
      // 图标选择器
      if(this.item.type == 'iconSelect') {
        this.item.iconType = 'svg'
        this.iconList = this.$store.state.iconLib.iconList ? [...this.$store.state.iconLib.iconList] : [...entryIcon]
        // if(this.item.iconType == 'svg') {
        //   this.iconList = [...entryIcon]
        // }else{
        //   this.iconList = [...iconList, ...systemIcon]
        // }
      }
      if(this.item.type == 'textarea' && this.forms[this.item.prop] && this.forms[this.item.prop].length > 0) {
        this.$set(this.forms, this.item.prop, this.form[this.item.prop].replaceAll(/\\n/gi, '\n'))
      }
      if (!this.item.dicUrl && !this.item.url) {
        // 获取应用所有工作流
        if(this.item.datatype == 'flowable'){
          if(this.jvsAppId) {
            getAllWorkflowByAppUse(this.jvsAppId).then(res => {
              if (res.data && res.data.code == 0 && res.data.data) {
                this.selectOption = [...res.data.data]
              }
            })
          }
        }else{
          if(this.item.datatype == 'dom'){
            if(this.jvsAppId && this.item.formId && this.item.others && this.item.others.length > 0) {
              getUseModelAllFields(this.jvsAppId, this.item.formId, this.designId).then(res => {
                if(res.data && res.data.code == 0) {
                  this.selectOption = []
                  res.data.data.filter(rit => {
                    if(this.item.others.indexOf(rit.prop || rit.fieldKey) > -1) {
                      this.selectOption.push({
                        label: rit.label || rit.fieldName,
                        prop: rit.prop || rit.fieldKey
                      })
                    }
                  })
                }
              })
            }
          }else if(this.item.datatype == 'rule'){
            // 优化👌 下拉框(来自模型或逻辑)
            if(loadType == 'create' && this.item.type == 'select' && ['dataModel', 'rule'].indexOf(this.item.datatype) > -1 && !(this.selectOption && this.selectOption.length > 0)) {
              if(this.forms[this.item.prop] && this.forms[this.item.prop+'_1']) {
                if(this.item.multiple){
                  let fitv = this.forms[this.item.prop+'_1'].split(',')
                  if(this.forms[this.item.prop].length > 0 && fitv.length > 0 && this.forms[this.item.prop].length == fitv.length) {
                    let tl = []
                    for(let fix in this.forms[this.item.prop]) {
                      if(fitv[fix]) {
                        let tlb = {}
                        tlb[(this.item.props && this.item.props.value) || 'value'] = this.forms[this.item.prop][fix]
                        tlb[(this.item.props && this.item.props.label) || 'label'] = fitv[fix]
                        tl.push(tlb)
                      }
                    }
                    if(tl.length > 0) {
                      this.selectOption = tl
                    }
                  }
                }else{
                  let tlb = {}
                  tlb[(this.item.props && this.item.props.value) || 'value'] = this.forms[this.item.prop]
                  tlb[(this.item.props && this.item.props.label) || 'label'] = this.forms[this.item.prop+'_1']
                  this.selectOption = [tlb]
                }
              }
            }
            if(!this.isView && !stopGetDicUrl) {
              this.getSelectRuleData(loadType)
            }
          }else{
            if(this.item.dicData) {
              this.selectOption = this.item.dicData
              this.getDicDataByExpress(this.selectOption)
              if(['cascader', 'tab', 'department', 'dept'].indexOf(this.item.type) == -1 && this.forms[this.item.prop] && this.item.matchClear !== false) {
                if((this.item.multiple || this.item.type == 'checkbox')) {
                  if(this.forms[this.item.prop].length > 0) {
                    let allIn = true
                    for(let sif in this.forms[this.item.prop]) {
                      let isIn = false
                      this.selectOption.filter(sis => {
                        if(this.forms[this.item.prop][sif] == sis[(this.item.props && this.item.props.value) || 'value']) {
                          isIn = true
                        }
                      })
                      if(!isIn) {
                        allIn = false
                      }
                    }
                    if(!allIn) {
                      this.$set(this.forms, this.item.prop, [])
                      this.notMatch = true
                    }
                  }
                }else{
                  let needClear = true
                  this.selectOption.filter(sis => {
                    if(sis[(this.item.props && this.item.props.value) || 'value'] == this.forms[this.item.prop]) {
                      needClear = false
                    }
                  })
                  if(needClear) {
                    this.$set(this.forms, this.item.prop, '')
                    this.notMatch = true
                  }
                }
              }
            }
          }
        }
      } else {
        // 优化👌 下拉框(来自模型或逻辑)
        if(loadType == 'create' && this.item.type == 'select' && ['dataModel', 'rule'].indexOf(this.item.datatype) > -1 && !(this.selectOption && this.selectOption.length > 0)) {
          if(this.forms[this.item.prop] && this.forms[this.item.prop+'_1']) {
            if(this.item.multiple){
              let fitv = this.forms[this.item.prop+'_1'].split(',')
              if(this.forms[this.item.prop].length > 0 && fitv.length > 0 && this.forms[this.item.prop].length == fitv.length) {
                let tl = []
                for(let fix in this.forms[this.item.prop]) {
                  if(fitv[fix]) {
                    let tlb = {}
                    tlb[(this.item.props && this.item.props.value) || 'value'] = this.forms[this.item.prop][fix]
                    tlb[(this.item.props && this.item.props.label) || 'label'] = fitv[fix]
                    tl.push(tlb)
                  }
                }
                if(tl.length > 0) {
                  this.selectOption = tl
                }
              }
            }else{
              let tlb = {}
              tlb[(this.item.props && this.item.props.value) || 'value'] = this.forms[this.item.prop]
              tlb[(this.item.props && this.item.props.label) || 'label'] = this.forms[this.item.prop+'_1']
              this.selectOption = [tlb]
            }
          }
        }
        if((this.isView ? !(this.item.dataFilterGroupList && this.item.dataFilterGroupList.length > 0) : true) && !stopGetDicUrl) {
          if(this.item.datatype == 'rule') {
            this.getSelectRuleData(loadType)
          }else{
            if(this.item.type != 'cascader') {
              this.getSelectUrlData(loadType)
            }
          }
        }else{
          this.getDicDataByExpress(this.selectOption)
        }
      }
      // 系统字典
      if(this.item.datatype == 'system' && ['select', 'radio', 'checkbox'].indexOf(this.item.type) > -1 && this.item.systemDict) {
        this.selectOption = []
        await getSystemDictItems(this.item.systemDict).then(res => {
          if(res.data.code == 0 && res.data.data) {
            this.selectOption = res.data.data
          }
        })
      }
      if(['checkbox', 'formbox'].indexOf(this.item.type) > -1) {
        if(!this.forms[this.item.prop]) {
          this.$set(this.forms, this.item.prop, [])
          if(this.item.type == 'checkbox' && this.item.defaultValue) {
            this.$set(this.forms, this.item.prop, this.item.defaultValue.split(','))
          }
        }
      }
      // 下拉切换是否多选时，初始化数据类型
      if(this.item.type == 'select') {
        if(this.item.multiple) {
          !this.forms[this.item.prop] && (this.$set(this.forms, this.item.prop, []))
          if(JSON.stringify(this.forms[this.item.prop]) == '[]' && this.item.defaultValue) {
            this.$set(this.forms, this.item.prop, this.item.defaultValue.split(','))
          }
        }else{
          if((!this.forms[this.item.prop] && this.forms[this.item.prop] !== 0 && this.forms[this.item.prop] !== false) || this.forms[this.item.prop] instanceof Array) {
            this.$set(this.forms, this.item.prop, "")
          }
        }
        if(this.item.datatype == 'option') {
          if(this.item.props && !this.item.props.label) {
            this.item.props.label = 'label'
            this.item.props.value = 'value'
          }
        }
      }
      // 滑块
      if(this.item.type == 'slider') {
        if(this.item.range) {
          !this.forms[this.item.prop] && (this.$set(this.forms, this.item.prop, [0, this.item.max / 2]))
        }else{
          if(!this.forms[this.item.prop] || this.forms[this.item.prop] instanceof Array) {
            this.$set(this.forms, this.item.prop, 0)
          }
        }
      }
      if(this.item.type == 'timeSelect') {
        if(this.item.pickeroptions && this.item.pickeroptions.start && this.item.pickeroptions.start != this.item.defaultValue) {
          this.item.defaultValue = this.item.pickeroptions.start
        }
      }
      // 表单项默认值填充，权重小于表单初始化值
      if((this.item.defaultValue || this.item.defaultValue === false || this.item.defaultValue === "" || this.item.defaultValue === 0) && !this.notMatch) {
        // 时间选择 多选
        if(this.item.type == 'timePicker') {
          if(!this.item.isrange) {
            let date = new Date(this.item.defaultValue)
            this.item.defaultValue = ''+ (date.getHours() > 9 ? date.getHours() : `0${date.getHours()}`) + ':' + (date.getMinutes() > 9 ? date.getMinutes() : `0${date.getMinutes()}`) + ':' + (date.getSeconds() > 9 ? date.getSeconds() : `0${date.getSeconds()}`)
            this.$set(this.forms, this.item.prop, this.item.defaultValue)
          }
        }else{
          if(!this.forms[this.item.prop] && this.forms[this.item.prop] !== false && this.forms[this.item.prop] !== 0) {
            if(this.item.type == 'checkbox' && this.item.defaultValue && (!this.forms[this.item.prop] || JSON.stringify(this.forms[this.item.prop]) == '[]')) {
              this.$set(this.forms, this.item.prop, this.item.defaultValue.split(','))
            }else{
              if((this.item.type == 'timeSelect') ? !this.item.notRightNow : true) {
                this.$set(this.forms, this.item.prop, this.item.defaultValue)
              }
            }
          }
        }
      }
      // 级联选择类
      if(this.item.type == 'cascader') {
        if(this.item.datatype == 'option' && this.item.dicData) {
          this.cascaderList = this.item.dicData
        }
        if(!stopGetDicUrl && this.item.datatype == 'system' && this.item.dictName) {
          getClassifyDictTree(this.item.dictName).then(res => {
            if(res.data.code == 0 && res.data.data && res.data.data.children) {
              let tplist = res.data.data.children
              this.formatCascaderOptions(tplist)
              if(this.item.filterProp && this.forms[this.item.filterProp]) {
                this.cascaderDataFilter(tplist)
              }
              this.cascaderList = JSON.parse(JSON.stringify(tplist))
            }
          })
        }
        if(!stopGetDicUrl && this.item.datatype == 'dataModel' && this.item.formId && this.item.url && this.item.props.label && this.item.props.value && this.item.props.secTitle) {
          this.getCascaderData()
        }
        if(!this.isView && !stopGetDicUrl && this.item.datatype == 'rule') {
          this.getSelectRuleData()
        }
      }
      // tab选项卡  step步骤条
      if(['tab', 'step'].indexOf(this.item.type) > -1) {
        // 脱离数据
        if(this.item.detachData) {
          if(this.item.dicData && this.item.dicData.length > 0) {
            this.item.dicData.filter(dct => {
              if(dct.prop) {
                if(this.item.column[dct.name] && this.item.column[dct.name] .length > 0) {
                  !this.forms[dct.prop] && (this.$set(this.forms, dct.prop, {}))
                }
              }
            })
          }
        }else{
          if(this.item.dicData && this.item.dicData.length > 0) {
            !this.forms[this.item.prop] && (this.$set(this.forms, this.item.prop, {}))
            for(let col in this.item.column) {
              if(this.item.column[col] && this.item.column[col].length > 0) {
                !this.forms[this.item.prop][col] && (this.$set(this.forms[this.item.prop], col, {}))
              }
            }
          }
        }
      }
      if(['tableForm'].indexOf(this.item.type) > -1) {
        !this.forms[this.item.prop] && (this.$set(this.forms, this.item.prop, []))
      }
      if(this.item.type == 'reportTable') {
        if(this.item.dicData && this.item.dicData.length > 0) {
          !this.forms[this.item.prop] && (this.$set(this.forms, this.item.prop, {}))
          for(let di in this.item.dicData) {
            if(!this.forms[this.item.prop][this.item.dicData[di].value]) {
              this.$set(this.forms[this.item.prop], this.item.dicData[di].value, {})
            }
          }
        }
      }
      if(['imageUpload', 'fileUpload'].indexOf(this.item.type) > -1) {
        if(this.forms[this.item.prop]) {
          if(!this.item.parentKey || this.item.parentType != 'tableForm'){
            if(typeof this.forms[this.item.prop] == 'object' && (this.forms[this.item.prop] instanceof Array) == false) {
              this.$set(this.forms, this.item.prop, [])
            }
            this.$set(this.item, 'fileList', this.forms[this.item.prop])
          }
          this.$set(this.tableFileList, this.tableRowAIndex, this.forms[this.item.prop])
        }
        if(!this.item.headers) {
          this.$set(this.item, 'headers', {})
        }
        this.$set(this.item.headers, 'Authorization', ('Bearer ' + this.$store.getters.access_token))
        if(this.jvsAppId) {
          this.$set(this.item.headers, 'businessId', this.jvsAppId)
        }
      }
      if(this.item.type == 'timeline' && !this.forms[this.item.prop]) {
        this.$set(this.forms, this.item.prop, [])
      }
      if(['childrenForm', 'connectForm'].indexOf(this.item.type) > -1 && !this.forms[this.item.prop]) {
        this.$set(this.forms, this.item.prop, {})
      }
      if(this.item.type == 'button') {
        this.$set(this.item, 'loading', false)
      }
      // 时间  日期 组件单位变化兼容历史数据
      if(['timePicker', 'datePicker'].indexOf(this.item.type) > -1) {
        if(this.item.type == 'timePicker') {
          if(this.item.isrange) {
            if(typeof this.form[this.item.prop] == 'string') {
              this.$set(this.form, this.item.prop, this.form[this.item.prop] ? [this.form[this.item.prop]] : [])
            }
          }else{
            if(typeof this.form[this.item.prop] == 'object' && this.form[this.item.prop] instanceof Array) {
              this.$set(this.form, this.item.prop, (this.form[this.item.prop] && this.form[this.item.prop].length > 0) ? this.form[this.item.prop][0] : '')
            }
            if(!this.form[this.item.prop] || this.form[this.item.prop].includes('NaN')) {
              if(!this.item.notRightNow) {
                let date = new Date()
                this.$set(this.form, this.item.prop, ''+ (date.getHours() > 9 ? date.getHours() : `0${date.getHours()}`) + ':' + (date.getMinutes() > 9 ? date.getMinutes() : `0${date.getMinutes()}`) + ':' + (date.getSeconds() > 9 ? date.getSeconds() : `0${date.getSeconds()}`))
              }
            }
          }
        }
        if(this.item.type == 'datePicker') {
          if(['date', 'week', 'month', 'year', 'datetime'].indexOf(this.item.datetype) > -1) {
            if(typeof this.form[this.item.prop] == 'object' && this.form[this.item.prop] instanceof Array) {
              this.$set(this.form, this.item.prop, (this.form[this.item.prop] && this.form[this.item.prop].length > 0) ? this.formatDateHandle(this.form[this.item.prop][0]) : '')
            }else{
              if(this.form[this.item.prop]) {
                this.$set(this.form, this.item.prop, this.formatDateHandle(this.form[this.item.prop]))
              }
            }
          }else{
            if(typeof this.form[this.item.prop] == 'string') {
              this.$set(this.form, this.item.prop, this.form[this.item.prop] ? [this.formatDateHandle(this.form[this.item.prop])] : [])
            }else{
              if(this.form[this.item.prop] && this.form[this.item.prop].length > 0) {
                for(let dix in this.form[this.item.prop]) {
                  this.$set(this.form[this.item.prop], dix, this.formatDateHandle(this.form[this.item.prop][dix]))
                }
              }
            }
          }
        }
      }
      this.lastItemData = JSON.parse(JSON.stringify(this.item))
      this.$forceUpdate()
    },
    formatDateHandle (val) {
      let date = new Date(val)
      let year = date.getFullYear().toString()
      let month = (date.getMonth() + 1) < 10 ? ('0' + (date.getMonth() + 1)) : ('' + (date.getMonth() + 1))
      let day = date.getDate() < 10 ? ('0' + date.getDate()) : ('' + date.getDate())
      let hour = date.getHours() < 10 ? ('0' + date.getHours()) : ('' + date.getHours())
      let minute = date.getMinutes() < 10 ? ('0' + date.getMinutes()) : ('' + date.getMinutes())
      let seconds = date.getSeconds() < 10 ? ('0' + date.getSeconds()) : ('' + date.getSeconds())
      switch(this.item.datetype) {
        case 'date':
        case 'dates':
        case 'daterange':
        case 'week':
          return year+'-'+month+'-'+day;
        case 'datetime':
        case 'datetimerange':
          return year+'-'+month+'-'+day+' '+ hour+':'+minute+':'+seconds;
        case 'month':
        case 'monthrange':
          return year+'-'+month;
        case 'year':
          return year;
        default: return val;
      }
    },
    getSelectUrlData (loadType) {
      let url=this.item.dicUrl || this.item.url
      if (!url) {
        return false
      }
      if(!this.item.formId || (this.item.formId && this.item.props && this.item.props.label)) {
        let fs = []
        fs = (this.item.props && this.item.props.label) ? [this.item.props.label] : []
        if(this.item.props && this.item.props.secTitle && fs.length > 0) {
          fs.push(this.item.props.secTitle)
        }
        let postData = {
          fieldList: fs,
          conditions: []
        }
        if(this.item.props && this.item.props.sourceFieldId) {
          postData.sourceFieldId = this.item.props.sourceFieldId
        }
        let nomptyValue = true
        let filterAble = true
        if(((this.item.showFrom && this.item.showFrom.indexOf("url") !== -1 && this.item.datatype === "dataModel") || this.item.searchable) && !this.item.dataFilterable){
          filterAble = false
        }
        // 搜索文本中的查询条件字段不带条件
        if(!this.item.isSearch) {
          if(this.item.dataFilterGroupList) {
            let gtarr = []
            for(let gi in this.item.dataFilterGroupList) {
              let gtp = []
              for(let df in this.item.dataFilterGroupList[gi]) {
                let dfit = {
                  enabledQueryTypes: this.item.dataFilterGroupList[gi][df].enabledQueryTypes,
                  fieldKey: this.item.dataFilterGroupList[gi][df].fieldKey,
                }
                if(['cust', 'role', 'department', 'job', 'user'].indexOf(this.item.dataFilterGroupList[gi][df].type) > -1) {
                  dfit.value = this.item.dataFilterGroupList[gi][df].value
                }else{
                  let bindDomNode = {}
                  let bindkey = this.item.dataFilterGroupList[gi][df].value
                  let formTemp = this.forms
                  if(typeof this.item.dataFilterGroupList[gi][df].value == 'object' && this.item.dataFilterGroupList[gi][df].value instanceof Array) {
                    formTemp = this.originForm
                    bindkey = this.item.dataFilterGroupList[gi][df].value[this.item.dataFilterGroupList[gi][df].value.length-1]
                    if(this.item.dataFilterGroupList[gi][df].value.length > 1) {
                      this.getNodeDom(this.originOption.column, this.item.dataFilterGroupList[gi][df].value, bindDomNode)
                    }
                    if(bindDomNode && bindDomNode.prop) {
                      if(bindDomNode.parentDom && bindDomNode.parentDom.length > 0) {
                        if(bindDomNode.parentDom[0].type == 'tab') {
                          if(bindDomNode.parentDom[0].detachData) {
                            if(bindDomNode.parentDom[1].prop) {
                              formTemp = this.originForm[bindDomNode.parentDom[1].prop]
                            }
                          }else{
                            formTemp = this.forms
                          }
                        }
                      }
                    }
                  }
                  dfit.value = formTemp[bindkey]
                  if((dfit.value == undefined || dfit.value == null || dfit.value == '') &&  this.item.dataFilterGroupList[gi][df].enabledQueryTypes != 'isNull') {
                    nomptyValue = false
                  }
                }
                gtp.push(dfit)
              }
              gtarr.push(gtp)
            }
            if(filterAble) {
              postData.groupConditions = gtarr
            }
          }else{
            if(this.item.dataFilterList) {
              for(let df in this.item.dataFilterList) {
                let dfit = {
                  enabledQueryTypes: this.item.dataFilterList[df].enabledQueryTypes,
                  fieldKey: this.item.dataFilterList[df].fieldKey,
                }
                if(this.item.dataFilterList[df].type == 'cust') {
                  dfit.value = this.item.dataFilterList[df].value
                }else{
                  dfit.value = this.forms[this.item.dataFilterList[df].value]
                  if((dfit.value == undefined || dfit.value == null || dfit.value == '') &&  this.item.dataFilterList[df].enabledQueryTypes != 'isNull') {
                    nomptyValue = false
                  }
                }
                if(filterAble) {
                  postData.conditions.push(dfit)
                }
              }
            }
          }
          if(this.item.dataItemExpressable) {
            if(this.item.dataItemExpressGroupList) {
              let gtarr = []
              for(let gi in this.item.dataItemExpressGroupList) {
                let gtp = []
                for(let df in this.item.dataItemExpressGroupList[gi]) {
                  let dfit = {
                    enabledQueryTypes: this.item.dataItemExpressGroupList[gi][df].enabledQueryTypes,
                    fieldKey: this.item.dataItemExpressGroupList[gi][df].fieldKey,
                  }
                  postData.fieldList.push(this.item.dataItemExpressGroupList[gi][df].fieldKey)
                  if(this.item.dataItemExpressGroupList[gi][df].type == 'cust') {
                    dfit.value = this.item.dataItemExpressGroupList[gi][df].value
                  }else{
                    let bindDomNode = {}
                    let bindkey = this.item.dataItemExpressGroupList[gi][df].value
                    let formTemp = this.forms
                    if(typeof this.item.dataItemExpressGroupList[gi][df].value == 'object' && this.item.dataItemExpressGroupList[gi][df].value instanceof Array) {
                      formTemp = this.originForm
                      bindkey = this.item.dataItemExpressGroupList[gi][df].value[this.item.dataItemExpressGroupList[gi][df].value.length-1]
                      if(this.item.dataItemExpressGroupList[gi][df].value.length > 1) {
                        this.getNodeDom(this.originOption.column, this.item.dataItemExpressGroupList[gi][df].value, bindDomNode)
                      }
                      if(bindDomNode && bindDomNode.prop) {
                        if(bindDomNode.parentDom && bindDomNode.parentDom.length > 0) {
                          if(bindDomNode.parentDom[0].type == 'tab') {
                            if(bindDomNode.parentDom[0].detachData) {
                              if(bindDomNode.parentDom[1].prop) {
                                formTemp = this.originForm[bindDomNode.parentDom[1].prop]
                              }
                            }else{
                              formTemp = this.forms
                            }
                          }
                        }
                      }
                    }
                    dfit.value = formTemp[bindkey]
                    if((dfit.value == undefined || dfit.value == null || dfit.value == '') &&  this.item.dataItemExpressGroupList[gi][df].enabledQueryTypes != 'isNull') {
                      nomptyValue = false
                    }
                  }
                  gtp.push(dfit)
                }
                gtarr.push(gtp)
              }
              postData.enableConditions = gtarr
            }
          }
        }
        if(url.startsWith('/mgr/jvs-design/dynamic/data/query/list')) {
          url = `/mgr/jvs-design/app/use/${this.jvsAppId}/dynamic/data/query/list/${this.item.formId}`
        }
        // 兼容2.1.6设计
        if(url == `/mgr/jvs-design/app/${this.jvsAppId}/use/dynamic/data/query/list/${this.item.formId}`) {
          url = `/mgr/jvs-design/app/use/${this.jvsAppId}/dynamic/data/query/list/${this.item.formId}`
        }
        if(!filterAble) {
          nomptyValue = true
        }
        if(nomptyValue) {
          getSelectData(url, this.item.method ? this.item.method : 'get', (this.item.method == 'post') ? postData : null, (this.item.method == 'post') ? this.designId : (this.item.formId ? this.item.formId : this.designId)).then(res => {
            if(res.data.code === 0) {
              // 优化👌 下拉框(来自模型或逻辑)
              if(loadType == 'create' && this.item.type == 'select' && ['dataModel', 'rule'].indexOf(this.item.datatype) > -1 && this.selectOption && this.selectOption.length > 0) {
                // 首次初始化存在labelvalue默认选项不清空
              }else{
                this.selectOption = []
              }
              for(let sitem in res.data.data){
                if(typeof res.data.data[sitem] == 'string') {
                  let needAdd = true
                  if(loadType == 'create' && this.item.type == 'select' && ['dataModel', 'rule'].indexOf(this.item.datatype) > -1 && this.forms[this.item.prop] && this.forms[this.item.prop+'_1']) {
                    if(this.item.multiple) {
                      if(this.forms[this.item.prop+'_1'].length > 0 && this.forms[this.item.prop].indexOf(res.data.data[sitem]) > -1) {
                        needAdd = false
                      }
                    }else{
                      if(this.forms[this.item.prop] == res.data.data[sitem]) {
                        needAdd = false
                      }
                    }
                  }
                  if(needAdd) {
                    this.selectOption.push({
                      label: res.data.data[sitem],
                      value: res.data.data[sitem]
                    })
                  }
                }else{
                  // this.selectOption.push({
                  //   label: res.data.data[sitem][this.item.props.label ? this.item.props.label : 'label'],
                  //   value: res.data.data[sitem][this.item.props.value ? this.item.props.value : 'value']
                  // })
                  let needAdd = true
                  if(loadType == 'create' && this.item.type == 'select' && ['dataModel', 'rule'].indexOf(this.item.datatype) > -1 && this.forms[this.item.prop] && this.forms[this.item.prop+'_1']) {
                    if(this.item.multiple) {
                      if(this.forms[this.item.prop+'_1'].length > 0 && this.forms[this.item.prop].indexOf(res.data.data[sitem][this.item.props.value ? this.item.props.value : 'value']) > -1) {
                        needAdd = false
                      }
                    }else{
                      if(this.forms[this.item.prop] == res.data.data[sitem][this.item.props.value ? this.item.props.value : 'value']) {
                        needAdd = false
                      }
                    }
                  }
                  if(needAdd) {
                    this.selectOption.push({...res.data.data[sitem], disabled: (res.data.data[sitem].disabled || res.data.data[sitem].jvsDisableItem)})
                  }
                }
              }
              this.getDicDataByExpress(this.selectOption)
              // console.log(this.selectOption)
              if(loadType != 'create' && this.forms[this.item.prop]) {
                if((this.item.multiple || this.item.type == 'checkbox')) {
                  if(this.forms[this.item.prop].length > 0) {
                    let allIn = true
                    for(let sif in this.forms[this.item.prop]) {
                      let isIn = false
                      this.selectOption.filter(sis => {
                        if(this.forms[this.item.prop][sif] == sis[(this.item.props && this.item.props.value) || 'value']) {
                          isIn = true
                        }
                      })
                      if(!isIn) {
                        allIn = false
                      }
                    }
                    if(!allIn) {
                      this.$set(this.forms, this.item.prop, [])
                      this.notMatch = true
                    }
                  }
                }else{
                  let needClear = true
                  this.selectOption.filter(sis => {
                    if(sis[(this.item.props && this.item.props.value) || 'value'] == this.forms[this.item.prop]) {
                      needClear = false
                    }
                  })
                  if(needClear) {
                    this.$set(this.forms, this.item.prop, '')
                    this.notMatch = true
                  }
                }
              }
            }
          })
        }else{
          this.selectOption = []
        }
      }
    },
    getSelectRuleData (loadType) {
      // 优化👌 下拉框(来自模型或逻辑)
      if(loadType == 'create' && this.item.type == 'select' && this.selectOption && this.selectOption.length > 0) {
        // 首次初始化存在labelvalue默认选项补清空
      }else{
        this.selectOption = []
      }
      this.cascaderList = []
      if(this.item.optionHttp) {
        let obj = {}
        obj = JSON.parse(JSON.stringify(this.forms))
        ruleRun(this.jvsAppId, this.item.optionHttp, obj).then(res => {
          if(res.data && res.data.code == 0 && res.data.data) {
            if(this.item.type == 'cascader') {
              let tplist = res.data.data
              if(this.item.filterProp && this.forms[this.item.filterProp]) {
                this.cascaderDataFilter(tplist)
              }
              this.cascaderList = JSON.parse(JSON.stringify(tplist))
            }else{
              for(let sitem in res.data.data){
                let needAdd = true
                if(loadType == 'create' && this.item.type == 'select' && ['dataModel', 'rule'].indexOf(this.item.datatype) > -1 && this.forms[this.item.prop] && this.forms[this.item.prop+'_1']) {
                  if(this.item.multiple) {
                    if(this.forms[this.item.prop+'_1'].length > 0 && this.forms[this.item.prop].indexOf(res.data.data[sitem][this.item.props.value ? this.item.props.value : 'value']) > -1) {
                      needAdd = false
                    }
                  }else{
                    if(this.forms[this.item.prop] == res.data.data[sitem][this.item.props.value ? this.item.props.value : 'value']) {
                      needAdd = false
                    }
                  }
                }
                if(needAdd) {
                  this.selectOption.push(res.data.data[sitem])
                }
              }
              if(loadType != 'create' && this.forms[this.item.prop]) {
                if((this.item.multiple || this.item.type == 'checkbox')) {
                  if(this.forms[this.item.prop].length > 0) {
                    let allIn = true
                    for(let sif in this.forms[this.item.prop]) {
                      let isIn = false
                      this.selectOption.filter(sis => {
                        if(this.forms[this.item.prop][sif] == sis[(this.item.props && this.item.props.value) || 'value']) {
                          isIn = true
                        }
                      })
                      if(!isIn) {
                        allIn = false
                      }
                    }
                    if(!allIn) {
                      this.$set(this.forms, this.item.prop, [])
                      this.notMatch = true
                    }
                  }
                }else{
                  let needClear = true
                  this.selectOption.filter(sis => {
                    if(sis[(this.item.props && this.item.props.value) || 'value'] == this.forms[this.item.prop]) {
                      needClear = false
                    }
                  })
                  if(needClear) {
                    this.$set(this.forms, this.item.prop, '')
                    this.notMatch = true
                  }
                }
              }
            }
          }
        })
      }
    },
    getCascaderData () {
      // 获取模型树形结构
      let paramData = {label: this.item.props.label, value: this.item.props.value, secTitle: this.item.props.secTitle, filter: {}}
      let nomptyValue = true
      // 搜索文本查询条件字段不带条件
      if(!this.item.isSearch && this.item.dataFilterable !== false) {
        if(this.item.dataFilterGroupList) {
          let gtarr = []
          for(let gi in this.item.dataFilterGroupList) {
            let gtp = []
            for(let df in this.item.dataFilterGroupList[gi]) {
              let dfit = {
                enabledQueryTypes: this.item.dataFilterGroupList[gi][df].enabledQueryTypes,
                fieldKey: this.item.dataFilterGroupList[gi][df].fieldKey,
              }
              if(['cust', 'role', 'department', 'job', 'user'].indexOf(this.item.dataFilterGroupList[gi][df].type) > -1) {
                dfit.value = this.item.dataFilterGroupList[gi][df].value
              }else{
                dfit.value = this.forms[this.item.dataFilterGroupList[gi][df].value]
                if((dfit.value == undefined || dfit.value == null || dfit.value == '') &&  this.item.dataFilterGroupList[gi][df].enabledQueryTypes != 'isNull') {
                  nomptyValue = false
                }
              }
              gtp.push(dfit)
            }
            gtarr.push(gtp)
          }
          paramData.filter.groupConditions = gtarr
        }else{
          if(this.item.dataFilterList) {
            let conditions = []
            for(let df in this.item.dataFilterList) {
              let dfit = {
                enabledQueryTypes: this.item.dataFilterList[df].enabledQueryTypes,
                fieldKey: this.item.dataFilterList[df].fieldKey,
              }
              if(this.item.dataFilterList[df].type == 'cust') {
                dfit.value = this.item.dataFilterList[df].value
              }else{
                dfit.value = this.forms[this.item.dataFilterList[df].value]
                if((dfit.value == undefined || dfit.value == null || dfit.value == '') &&  this.item.dataFilterList[df].enabledQueryTypes != 'isNull') {
                  nomptyValue = false
                }
              }
              conditions.push(dfit)
            }
            paramData.filter.conditions = conditions
          }
        }
      }
      getSelectData(this.item.url, 'post', paramData, (this.item.method == 'post') ? this.designId : (this.item.formId ? this.item.formId : this.designId)).then(res => {
        if(res.data.code === 0) {
          let tplist = res.data.data
          if(this.item.filterProp && this.forms[this.item.filterProp]) {
            this.cascaderDataFilter(tplist)
          }
          this.cascaderList = JSON.parse(JSON.stringify(tplist))
        }
      })
    },
    handlePictureCardPreview (url) {
      this.dialogImageUrl=url
      this.dialogVisible=true
    },
    handlePictureCardPreviewUpload (file) {
      this.dialogImageUrl=file.url
      this.dialogVisible=true
    },
    handleFilePreviewUpload (file) {
      if(file.url) {
        this.$openUrl(file.url, '_blank')
      }
    },
    checkIcon (key, icon) {
      this.form[key] = icon
      this.$set(this.item, 'defaultValue', icon)
      this.$forceUpdate()
    },
    // 字段值改变传出表单
    async formChange (value) {
      if(this.item.type == 'colorSelect') {
        if(!this.form[this.item.prop]) {
          this.$set(this.form, this.item.prop, '')
        }
      }
      if(this.item.type == 'cascader' && this.item.pickType === 'tree') {
        this.$set(this.form, this.item.prop, value[this.item.prop])
      }
      if(this.item.type == 'inputNumber') {
        if(this.item.parentType == 'tableForm') {
          if(this.forms[this.item.prop] === null || this.forms[this.item.prop] === undefined) {
            this.$set(this.forms, this.item.prop, this.item.precision ? Number(this.item.min || 0).toFixed(this.item.precision) : (this.item.min || 0))
          }
        }
      }
      this.$emit('formChange', this.form, this.item, this.beforeValue)
      if(this.eventList.indexOf(this.item.type) > -1) {
        this.eventRequireHandle('reinit')
      }else{
        if(this.isView !== true) {
          this.$emit('reInitData', this.item.prop, this.item.parentKey, this.tableRowAIndex)
          // if(this.execsList && this.execsList.indexOf(this.item.parentKey ? this.item.parentKey+'.'+this.item.prop : this.item.prop) > -1) {
          //   this.$emit('reInitData', this.item.prop, this.item.parentKey, this.tableRowAIndex)
          // }
        }
      }
      if(['user', 'role', 'department', 'group', 'job'].indexOf(this.item.type) > -1 || (this.item.type == 'cascader' && this.item.pickType === 'tree') || (this.item.searchable)) {
        if(this.item.rules && this.item.rules.length > 0) {
          if(this.item.rules[0].required) {
            if(this.item.multiple) {
              if(this.form[this.item.prop].length > 0) {
                this.$emit('validateHandle', {type: 'clear', item: this.item})
              }else{
                this.$emit('validateHandle', {type: 'validate', item: this.item})
              }
            }else{
              if(this.form[this.item.prop]) {
                this.$emit('validateHandle', {type: 'clear', item: this.item})
              }else{
                this.$emit('validateHandle', {type: 'validate', item: this.item})
              }
            }
          }
        }
      }
    },
    // 选项卡formchange
    tabFormchange (data, item) {
      if(this.item.type == 'tab' && this.item.detachData) {
        this.$set(this.form, item.prop, data[item.prop])
      }else{
        this.$set(this.form, this.item.prop, data)
      }
      this.$forceUpdate()
    },
    addRowHandle () {
      if(!this.forms[this.item.prop]) {
        this.$set(this.forms, this.item.prop, [])
      }
      if(this.item.addBtnOrigin == 'form') {
        if(this.item.addBtnFormCode) {
          this.openForm = {}
          this.openFormTitle = this.item.addBtnText ? this.item.addBtnText : '新增'
          this.$refs.formDialog.childFormInit(this.item.addBtnFormCode, this.openForm, 'add')
        }else{
          this.openTableBtnFormColumn('add')
        }
      }else if(this.item.addBtnOrigin == 'table') {
        if(this.item.addBtnFormId) {
          this.openSearchWidth = this.item.addPopupWidth ? (this.item.addPopupWidth + '%') : '50%'
          this.openDialog('addOpenTable')
          this.openSearchList = JSON.parse(JSON.stringify(this.forms[this.item.prop]))
        }
      }else{
        let adrow = {}
        let keys = []
        let pk = []
        this.tableFormOption.tableColumn.filter(item => {
          keys.push(item.prop)
          pk = item.parentKey
          // 时间范围  日期范围
          if((item.type == 'timePicker' && item.isrange)) {
            let val = new Date()
            let h = val.getHours()
            let m = val.getMinutes()
            let s = val.getSeconds()
            let vastr = (h<10?('0'+h) : h) + ':' + (m<10?('0'+m) : m) + ':' + (s<10?('0'+s) : s)
            this.$set(adrow, item.prop, [vastr, vastr])
          }
          if(item.type == 'datePicker' && ['dates', 'daterange', 'monthrange', 'datetimerange'].indexOf(item.datetype) > -1) {
            this.$set(adrow, item.prop, [])
          }
          if(item.type == 'switch' && item.defaultValue == true) {
            this.$set(adrow, item.prop, true)
          }
          if(!this.item.editable) {
            if(item.defaultValue !== null && item.defaultValue !== undefined) {
              this.$set(adrow, item.prop, item.defaultValue)
            }
          }
        })
        this.forms[this.item.prop].push(adrow)
        this.tableFormAddHandleIndex = this.forms[this.item.prop].length -1
        let _this = this
        setTimeout( ()=> {
          _this.$emit('reInitData', keys.join(','), pk, (this.forms[this.item.prop].length -1), 'add')
        }, 0)
      }
    },
    openFormOutHandle (type, row, index) {
      let useDefault = true
      if(type == 'edit') {
        if(this.item.editBtnFormCode) {
          this.openForm = JSON.parse(JSON.stringify(row))
          this.openFormTitle = this.item.editBtnText ? this.item.editBtnText : '编辑'
          this.$refs.formDialog.childFormInit(this.item.editBtnFormCode, this.openForm, 'edit')
          useDefault = false
          if(index > -1) {
            this.openFormIndex = index
          }
        }
      }
      if(type == 'view') {
        if(this.item.viewBtnFormCode) {
          this.openForm = JSON.parse(JSON.stringify(row))
          this.openFormTitle = this.item.viewBtnText ? this.item.viewBtnText : '详情'
          this.$refs.formDialog.childFormInit(this.item.viewBtnFormCode, this.openForm, 'view')
          useDefault = false
          if(index > -1) {
            this.openFormIndex = index
          }
        }
      }
      if(useDefault) {
        this.openTableBtnFormColumn(type, row, index)
      }
    },
    openTableBtnFormColumn (type, row, index) {
      let title = ''
      if(type == 'add') {
        title = this.item.addBtnText ? this.item.addBtnText : '新增'
      }
      if(type == 'edit') {
        title = this.item.editBtnText ? this.item.editBtnText : '编辑'
      }
      if(type == 'view') {
        title = this.item.viewBtnText ? this.item.viewBtnText : '详情'
      }
      this.openFormTitle = title
      this.openFormWidth = this.item[type+'PopupWidth'] ? (this.item[type+'PopupWidth'] + '%') : '50%'
      this.openFormOption.column = JSON.parse(JSON.stringify(this.item.tableColumn))
      if(type == 'view') {
        this.openFormOption.disabled = true
        this.openFormOption.btnHide = true
      }else{
        this.openFormOption.disabled = false
        this.openFormOption.btnHide = false
      }
      if(row) {
        this.openForm = JSON.parse(JSON.stringify(row))
      }else{
        this.openForm = {}
      }
      if(index > -1) {
        this.openFormIndex = index
      }
      this.openFormVisible = true
    },
    openFormSubmit () {
      if(this.openFormIndex > -1) {
        this.$set(this.forms[this.item.prop], this.openFormIndex, this.openForm)
        this.$emit('formChange', this.forms)
        this.openFormClose()
        this.$forceUpdate()
      }else{
        let keys = []
        let pk = []
        this.tableFormOption.tableColumn.filter(item => {
          keys.push(item.prop)
          pk = item.parentKey
        })
        this.form[this.item.prop].push(JSON.parse(JSON.stringify(this.openForm)))
        this.tableFormAddHandleIndex = this.forms[this.item.prop].length -1
        let _this = this
        setTimeout( ()=> {
          _this.$emit('reInitData', keys.join(','), pk, (this.forms[this.item.prop].length -1), 'add')
        }, 0)
        this.openFormClose()
        this.$forceUpdate()
      }
    },
    openFormOutSubmit (form) {
      if(this.openFormIndex > -1) {
        this.$set(this.forms[this.item.prop], this.openFormIndex, form)
        this.$emit('formChange', this.forms)
      }else{
        this.form[this.item.prop].push(JSON.parse(JSON.stringify(form)))
      }
      this.$forceUpdate()
    },
    openFormClose () {
      this.openFormVisible = false
      this.openFormTitle = ''
      this.openFormWidth = ''
      this.openFormIndex = -1
      this.openForm = null
      this.openFormOption.column = []
    },
    resetAddIndex () {
      this.tableFormAddHandleIndex = -1
    },
    deleteRow (row, index) {
      if(this.item.formId) {
        if(!this.form[this.item.prop+'_del']) {
          this.$set(this.form, (this.item.prop+'_del'), [])
        }
        let list = JSON.parse(JSON.stringify(this.form[this.item.prop+'_del']))
        list.push(row)
        this.$set(this.form, (this.item.prop+'_del'), list)
      }
      this.forms[this.item.prop].splice(index, 1)

      this.$emit('reInitData', this.item.prop, this.item.parentKey, index, 'del')
    },
    // 同步表格数据
    setTableHandle (data, bindFlowId, dynamicNode) {
      this.$set(this.forms, this.item.prop, data)
      if(this.item.type == 'tableForm' && data.length == 1 && this.forms[this.item.prop+'_line']) {
        delete this.forms[this.item.prop+'_line']
      }
      if(bindFlowId) {
        this.$set(this.forms, this.item.prop+'_flowId', bindFlowId)
      }
      if(dynamicNode) {
        this.$set(this.forms, this.item.prop+'_dynamicNode', dynamicNode)
      }
    },
    // 获取宽度占比
    getWidth (item) {
      let w = 400
      if(item.prop && document.getElementById('icon-select-item-'+item.prop)) {
        w = document.getElementById('icon-select-item-'+item.prop).clientWidth
      }
      return w
    },
    // 初始化富文本
    initEditor (prop) {
      let that = this
      this.$nextTick(() => {
        let _this = that
        if(_this.editor) {
          _this.editor.destroy()
        }
        _this.editor = null
        $('#' + prop + '-' + this.editorUUID + '-editor').html("")
        _this.editor = new E('#' + prop + '-' + this.editorUUID + '-editor')
        _this.editor.config.height = 400
        // _this.editor.config.uploadImgShowBase64 = true
        _this.editor.config.menus = [
          'head',
          'bold',
          'fontSize',
          'fontName',
          'italic',
          'underline',
          'strikeThrough',
          'indent',
          'lineHeight',
          'foreColor',
          'backColor',
          'link',
          'list',
          'justify',
          'quote',
          'emoticon',
          'image',
          'table',
          'code',
          'splitLine',
          'undo',
          'redo',
        ]
        _this.editor.config.onblur = function (newHtml) {
          let vb = false
          if(!newHtml || JSON.stringify(newHtml) == '" "' || newHtml == '<p></p>' && newHtml == '<p><br></p>') {
            _this.$set(_this.form, prop, "")
            vb = false
          }else{
            _this.$set(_this.form, prop, newHtml)
            _this.$set(_this.item, 'defaultValue', newHtml)
            vb = true
          }
          if(_this.item.rules && _this.item.rules.length > 0) {
            if(_this.item.rules[0].required) {
              if(vb) {
                _this.$emit('validateHandle', {type: 'clear', item: _this.item})
              }else{
                _this.$emit('validateHandle', {type: 'validate', item: _this.item})
              }
            }
          }
          _this.eventRequireHandle()
        }
        _this.editor.config.onchange = function (newHtml) {
          let vb = false
          if(!newHtml || JSON.stringify(newHtml) == '" "' || newHtml == '<p></p>' && newHtml == '<p><br></p>') {
            _this.$set(_this.form, prop, "")
            vb = false
          }else{
            _this.$set(_this.form, prop, newHtml)
            _this.$set(_this.item, 'defaultValue', newHtml)
            vb = true
          }
          if(_this.item.rules && _this.item.rules.length > 0) {
            if(_this.item.rules[0].required) {
              if(vb) {
                _this.$emit('validateHandle', {type: 'clear', item: _this.item})
              }else{
                _this.$emit('validateHandle', {type: 'validate', item: _this.item})
              }
            }
          }
        }
        _this.editor.config.uploadImgServer = '/mgr/jvs-auth/upload/jvs-public'
        _this.editor.config.uploadFileName = 'file'
        let upheader = {
          Authorization: 'Bearer ' + this.$store.getters.access_token
        }
        if(this.jvsAppId) {
          upheader.businessId = this.jvsAppId
        }
        _this.editor.config.uploadImgHeaders = upheader
        if(_this.item.uploadHttp && _this.item.uploadHttp.parameters) {
          _this.editor.config.uploadImgParams = JSON.parse(JSON.stringify(_this.item.uploadHttp.parameters))
        }else{
          _this.editor.config.uploadImgParams = {
            module: '/jvs-ui/form/'
          }
        }
        _this.editor.config.uploadImgHooks = {
          // 图片上传并返回了结果，图片插入已成功
          success: function(xhr) {
            console.log('success', xhr)
          },
          // 图片上传并返回了结果，但图片插入时出错了
          fail: function(xhr, editor, resData) {
            console.log('fail', resData)
          },
          // 上传图片出错，一般为 http 请求的错误
          error: function(xhr, editor, resData) {
            console.log('error', xhr, resData)
          },
          // 图片上传并返回了结果，想要自己把图片插入到编辑器中
          // 例如服务器端返回的不是 { errno: 0, data: [...] } 这种格式，可使用 customInsert
          customInsert: function(insertImgFn, result) {
            // insertImgFn 可把图片插入到编辑器，传入图片 src ，执行函数即可
            if(result.code == 0 && result.data && result.data.fileLink) {
              let url = result.data.fileLink.indexOf('?') ? result.data.fileLink.split('?')[0] : result.data.fileLink
              insertImgFn(url)
            }
          }
        }
        _this.editor.create()
        if(_this.item.defaultValue) {
          _this.editor.txt.html(_this.item.defaultValue)
          _this.initHtml = _this.item.defaultValue
        }
        if(_this.form[prop]) {
          _this.form[prop] = _this.form[prop].replace(/&lt;/g, "<")
          _this.form[prop] = _this.form[prop].replace(/&gt;/g, ">")
          _this.editor.txt.html(_this.form[prop])
          _this.initHtml = _this.form[prop]
        }
        if(_this.item.disabled) {
          _this.editor.disable()
        }else{
          _this.editor.enable()
        }
      })
      this.$forceUpdate()
    },
    tabClick (name) {
      if(this.item.handleClick) {
        this.item.handleClick(name)
      }
    },
    // 按钮点击
    btnClick () {
      if(this.item.eventType == 'url') {
        if(this.item.openUrl) {
          this.$openUrl(this.formatUrl(this.item.openUrl), this.item.newWindowOpen ? '_blank' : '_self')
        }
      }else{
        this.eventRequireHandle()
      }
    },
    // 上传成功回调
    handleSuccess (res, file, fileList) {
      if(this.item.multipleUpload && fileList){
        if(fileList.every(it => (it.status == 'success' || it.isFinish == true))) {
          fileList.map(item => {
            if(item.response && item.response.code == 0) {
              let obj = {
                name: item.response.data.originalFileName || item.response.data.name,
                url: item.response.data.fileLink,
                fileName: item.response.data.fileName,
                bucketName: item.response.data.bucketName,
                fileSize: item.response.data.fileSize,
              }
              if(this.item.parentKey && this.item.parentType == 'tableForm') {
                if(!this.tableFileList[this.tableRowAIndex]) {
                  this.tableFileList[this.tableRowAIndex] = []
                }
                this.tableFileList[this.tableRowAIndex].push(obj)
                this.$set(this.forms, this.item.prop, this.tableFileList[this.tableRowAIndex])
              }else{
                this.item.fileList.push(obj)
                let temp = {
                  key: this.item.prop,
                  fileList: this.item.fileList
                }
                this.$emit('file', temp)
                this.$set(this.forms, this.item.prop, this.item.fileList)
              }
            }
          })
        }
        this.eventRequireHandle()
        this.$forceUpdate()
      }else{
        if(res && res.code == 0 && res.data) {
          let obj = {
            name: res.data.originalFileName,
            url: res.data.fileLink,
            fileName: res.data.fileName,
            bucketName: res.data.bucketName,
            fileSize: res.data.fileSize,
          }
          if(this.item.parentKey && this.item.parentType == 'tableForm') {
            if(!this.tableFileList[this.tableRowAIndex]) {
              this.tableFileList[this.tableRowAIndex] = []
            }
            this.tableFileList[this.tableRowAIndex].push(obj)
            this.$set(this.forms, this.item.prop, this.tableFileList[this.tableRowAIndex])
          }else{
            this.item.fileList.push(obj)
            let temp = {
              key: this.item.prop,
              fileList: this.item.fileList
            }
            this.$emit('file', temp)
            this.$set(this.forms, this.item.prop, this.item.fileList)
          }
          this.eventRequireHandle()
        }
      }
      if(['imageUpload', 'fileUpload'].indexOf(this.item.type) > -1) {
        if(this.item.rules && this.item.rules.length > 0) {
          if(this.item.rules[0].required) {
            if(this.forms[this.item.prop] && this.forms[this.item.prop].length > 0) {
              this.$emit('validateHandle', {type: 'clear', item: this.item})
            }else{
              this.$emit('validateHandle', {type: 'validate', item: this.item})
            }
          }
        }
        this.$emit('uploadChange') // 单独使用formitem组件
      }
    },
    eventRequireHandle (op) {
      if(this.item.eventHttpEnable !== false && this.item.eventHttp && !this.isView) {
        if([undefined, null].indexOf(this.forms[this.item.prop]) > -1 && this.item.type != 'button') {
          return false
        }
        let obj = {}
        obj = JSON.parse(JSON.stringify(this.forms))
        if(this.item.type == 'button') {
          this.$set(this.item, 'loading', true)
        }
        this.$forceUpdate()
        ruleRun(this.jvsAppId, this.item.eventHttp, obj, {formDesignId: this.designId}).then(res => {
          if(res.data && res.data.code == 0) {
            if(res && res.data && res.headers["output_format"] && res.data.data){
              let name = decodeURI(res.headers["output_format"])
              this.ruleDownLoad(name, res.data.data)
            }else if(res && res.data && res.headers["output_type"] == 'preview' && res.data.data) {
              this.previewFile(res.data.data)
            }else{
              if(res.data.msg) {
                this.$notify({
                  title: '提示',
                  message: res.data.msg,
                  position: 'bottom-right',
                  type: (res && res.data && res.headers["output_status"] == 'false') ? 'error' : 'success',
                  duration: (res && res.data && res.headers["output_status"] == 'false' && res.headers["message_close"] == 'false') ? 0 : 4500,
                  dangerouslyUseHTMLString: true,
                });
              }
              if (res.data.data) {
                for(let i in res.data.data) {
                  this.$set(this.form, i, res.data.data[i])
                }
                this.$emit('formChange', this.form)
              }
            }
            if(op == 'reinit' && this.isView !== true) {
              this.$emit('reInitData', this.item.prop, this.item.parentKey, this.tableRowAIndex)
            }
            if(this.item.type == 'button') {
              this.$set(this.item, 'loading', false)
            }
            this.$forceUpdate()
          }
        })
      }else{
        if(op == 'reinit' && this.isView !== true) {
          this.$emit('reInitData', this.item.prop, this.item.parentKey, this.tableRowAIndex)
        }
      }
    },
    // 处理上传参数
    formatUploadData (item) {
      let obj = {}
      if(item.uploadHttp && item.uploadHttp.parameters) {
        obj = item.uploadHttp.parameters
      }else{
        obj = { module: '/jvs-ui/form/' }
      }
      if(item.uploadModule) {
        let str = ''
        if(item.uploadModule.startsWith('/') == false) {
          str = '/'
        }
        str += item.uploadModule
        if(item.uploadModule.endsWith('/') == false) {
          str += '/'
        }
        obj.module = str
      }
      return obj
    },
    // 删除
    handleRemove (file, fileList) {
      if(this.item.parentKey && this.item.parentType == 'tableForm') {
        for(let i in this.tableFileList[this.tableRowAIndex]) {
          if(this.tableFileList[this.tableRowAIndex][i].uid == file.uid) {
            this.tableFileList[this.tableRowAIndex].splice(i, 1)
            this.$set(this.forms, this.item.prop, this.tableFileList[this.tableRowAIndex])
          }
        }
        this.$forceUpdate()
      }else{
        for(let i in this.item.fileList) {
          if(this.item.fileList[i].uid == file.uid) {
            this.item.fileList.splice(i, 1)
            let temp = {
              key: this.item.prop,
              fileList: this.item.fileList
            }
            this.$emit('file', temp)
            this.$set(this.forms, this.item.prop, this.item.fileList)
          }
        }
        this.$forceUpdate()
      }
      this.eventRequireHandle()
    },
    formatCascaderOptions (list) {
      for(let i in list) {
        if(list[i].extend && list[i].extend.uniqueName) {
          list[i].uniqueName = list[i].extend.uniqueName
        }
        if(list[i].children && list[i].children.length > 0) {
          this.formatCascaderOptions(list[i].children)
        }
      }
    },
    getHtmlText (text) {
      if(text) {
        return text.replace(/[\n]/g, "<br/>")
      }else{
        return ''
      }
    },
    reInitData (prop, parentKey, index, tableType) {
      this.$emit('reInitData', prop, parentKey, index, tableType)
    },
    openDialog (oprate) {
      if(!this.isView && !this.openSearchInputLoading) {
        this.openSearchOprate = oprate || ''
        this.searchOption.column = []
        let seachCoumn = []
        this.searchFormOption.column = []
        let seachFormCoumn = []
        this.queryBeforeSlot = []
        let slots = []
        if(this.item.others && this.item.others.length > 0) {
          this.openSearchInputLoading = true
          getUseModelAllFields(this.jvsAppId, oprate == 'addOpenTable' ? this.item.addBtnFormId : this.item.formId, this.designId).then(res => {
            if(res.data && res.data.code == 0) {
              res.data.data.filter(it => {
                if(it.type == 'SWITCH') {
                  it.type = 'switch'
                }
                if(['textarea', 'serialNumber'].indexOf(it.type) > -1) {
                  it.type = 'input'
                }
                if(it.type == 'imageUpload') {
                  it.type = 'image'
                }
                if(it.type == 'fileUpload') {
                  it.type = 'file'
                }
                if(this.item.others.indexOf(it.fieldKey) > -1) {
                  seachCoumn.push({
                    label: it.fieldName,
                    prop: it.fieldKey,
                    type: it.type ? it.type : 'input',
                    enabledQueryTypes: it.enabledQueryTypes,
                    searchSpan: (window.devicePixelRatio > 1.5 && document.body.clientWidth < 1500) ? 12 : 8,
                    datetype: 'date',
                    isSearch: true,
                    imgWidth: 80,
                    imgHeight: 80,
                    precision: ['inputNumber', 'slider'].indexOf(it.type) > -1 ? (it.designJson && it.designJson.precision) : ''
                  })
                }
                if(this.item.othersQuery && this.item.othersQuery.length > 0) {
                  if(this.item.othersQuery.indexOf(it.fieldKey) > -1 && ['imageUpload', 'image', 'fileUpload', 'file'].indexOf(it.type) == -1) {
                    seachFormCoumn.push(Object.assign(JSON.parse(JSON.stringify(it.designJson || it)), {
                      label: it.fieldName,
                      prop: it.fieldKey,
                      type: it.type ? (['radio', 'checkbox', 'connectForm'].indexOf(it.type) > -1 ? 'select' : it.type) : 'input',
                      enabledQueryTypes: it.enabledQueryTypes,
                      searchSpan: (window.devicePixelRatio > 1.5 && document.body.clientWidth < 1500) ? 12 : 8,
                      datetype: 'date',
                      beforeSlot: ['switch'].indexOf(it.type) > -1 ? false : true,
                      isSearch: true,
                      rules: [],
                      disabled: false,
                      display: true,
                      defaultValue: null,
                      eventHttp: '',
                      maxlength: '',
                    }))
                    let queryTypes = []
                    for(let q in it.enabledQueryTypes) {
                      let str = ''
                      switch(it.enabledQueryTypes[q]) {
                        case 'eq':
                          str = '等于';
                          break;
                        case 'ne':
                          str = '不等于';
                          break;
                        case 'gt':
                          str = '大于';
                          break;
                        case 'ge':
                          str = '大于等于';
                          break;
                        case 'lt':
                          str = '小于';
                          break;
                        case 'le':
                          str = '小于等于';
                          break;
                        case 'in':
                          str = '包含';
                          break;
                        case 'like':
                          str = '模糊匹配';
                          break;
                        case 'between':
                          str = '之间';
                          break;
                        default: ;break;
                      }
                      if(str) {
                        queryTypes.push({label: str, value: it.enabledQueryTypes[q]})
                      }
                    }
                    slots.push({prop: it.fieldKey, list: queryTypes})
                    if(queryTypes.length > 0) {
                      this.$set(this.queryOprator, it.fieldKey, (it.type=='input')?'like':queryTypes[0].value)
                    }
                  }
                }else{
                  if(this.item.others.indexOf(it.fieldKey) > -1 && ['imageUpload', 'image', 'fileUpload', 'file'].indexOf(it.type) == -1) {
                    seachFormCoumn.push(Object.assign(JSON.parse(JSON.stringify(it.designJson || it)), {
                      label: it.fieldName,
                      prop: it.fieldKey,
                      type: it.type ? (['radio', 'checkbox', 'connectForm'].indexOf(it.type) > -1 ? 'select' : it.type) : 'input',
                      enabledQueryTypes: it.enabledQueryTypes,
                      searchSpan: (window.devicePixelRatio > 1.5 && document.body.clientWidth < 1500) ? 12 : 8,
                      datetype: 'date',
                      beforeSlot: ['switch'].indexOf(it.type) > -1 ? false : true,
                      isSearch: true,
                      rules: [],
                      disabled: false,
                      display: true,
                      defaultValue: null,
                      eventHttp: '',
                      maxlength: '',
                    }))
                    let queryTypes = []
                    for(let q in it.enabledQueryTypes) {
                      let str = ''
                      switch(it.enabledQueryTypes[q]) {
                        case 'eq':
                          str = '等于';
                          break;
                        case 'ne':
                          str = '不等于';
                          break;
                        case 'gt':
                          str = '大于';
                          break;
                        case 'ge':
                          str = '大于等于';
                          break;
                        case 'lt':
                          str = '小于';
                          break;
                        case 'le':
                          str = '小于等于';
                          break;
                        case 'in':
                          str = '包含';
                          break;
                        case 'like':
                          str = '模糊匹配';
                          break;
                        case 'between':
                          str = '之间';
                          break;
                        default: ;break;
                      }
                      if(str) {
                        queryTypes.push({label: str, value: it.enabledQueryTypes[q]})
                      }
                    }
                    slots.push({prop: it.fieldKey, list: queryTypes})
                    if(queryTypes.length > 0) {
                      this.$set(this.queryOprator, it.fieldKey,  (it.type=='input')?'like':queryTypes[0].value)
                    }
                  }
                }
              })
              for(let k in this.item.others) {
                seachCoumn.filter(scol => {
                  if(scol.prop == this.item.others[k]) {
                    this.searchOption.column.push(scol)
                  }
                })
                seachFormCoumn.filter(sfol => {
                  if(sfol.prop == this.item.others[k]) {
                    this.searchFormOption.column.push(sfol)
                  }
                })
                slots.filter(slot => {
                  if(slot.prop == this.item.others[k]) {
                    this.queryBeforeSlot.push(slot)
                  }
                })
              }
              if(this.item.othersQuery && this.item.othersQuery.length > 0) {
                this.searchFormOption.column = []
                this.queryBeforeSlot = []
                for(let q in this.item.othersQuery) {
                  seachFormCoumn.filter(sfol => {
                    if(sfol.prop == this.item.othersQuery[q]) {
                      this.searchFormOption.column.push(sfol)
                    }
                  })
                  slots.filter(slot => {
                    if(slot.prop == this.item.othersQuery[q]) {
                      this.queryBeforeSlot.push(slot)
                    }
                  })
                }
              }
              this.searchVisible = true
            }
            this.openSearchInputLoading = false
          }).catch(e => {
            this.openSearchInputLoading = false
          })
        }
      }
    },
    searchClose () {
      this.searchResult = ''
      this.searchVisible = false
      this.openSearchList = []
      this.$set(this.page, 'total', 0)
      this.$set(this.page, 'currentPage', 1)
      this.$set(this.page, 'pageSize', 20)
    },
    clearSearch () {
      this.$set(this.forms, this.item.prop, '')
      this.formChange()
    },
    // 条件查询
    queryHandle (form) {
      this.queryParams = form
      this.getListData()
    },
    // 分页change
    handleCurrentChange(e) {
      this.getListData()
    },
    resetQueryHandle () {
      this.queryParams = {}
    },
    rowClick (data) {
      if(this.openSearchOprate == 'addOpenTable') {
        return false
      }
      if(this.item.multiple) {
        return false
      }
      let row = data.row
      if(this.item.props && this.item.props.label && row[this.item.props.label]) {
        this.$set(this.forms, this.item.prop, row[this.item.props.label])
      }else{
        this.$set(this.forms, this.item.prop, '')
      }
      this.formChange()
      this.searchVisible = false
    },
    selectChange (data) {
      if(this.openSearchOprate == 'addOpenTable') {
        if(data.length == 0) {
          let ids = []
          this.searchData.filter(sit => {
            ids.push(sit.id)
          })
          let temp = []
          this.openSearchList.filter(lit => {
            if(ids.indexOf(lit.id) == -1) {
              temp.push(lit)
            }
          })
          this.openSearchList = temp
        }else{
          let hasIds = []
          this.openSearchList.filter(sit => {
            hasIds.push(sit.id)
          })
          let dataIds = []

          for(let i in data) {
            dataIds.push(data[i].id)
            if(hasIds.indexOf(data[i].id) == -1) {
              this.openSearchList.push(data[i])
            }
          }
          let moveIds = []
          this.searchData.filter(sit => {
            if(dataIds.indexOf(sit.id) == -1) {
              moveIds.push(sit.id)
            }
          })
          let temp = []
          this.openSearchList.filter(lit => {
            if(moveIds.indexOf(lit.id) == -1) {
              temp.push(lit)
            }
          })
          this.openSearchList = temp
        }
        this.$forceUpdate()
      }else{
        let temp = []
        for(let i in data) {
          if(this.item.props && this.item.props.label && data[i][this.item.props.label]) {
            temp.push(data[i][this.item.props.label])
          }
        }
        this.searchResult = temp.join(',')
      }
    },
    searchSubmit () {
      this.$set(this.forms, this.item.prop, this.openSearchOprate == 'addOpenTable' ? this.openSearchList : this.searchResult)
      this.formChange()
      if(this.openSearchOprate == 'addOpenTable' && this.tableFormOption.tableColumn) {
        let keys = []
        let pk = []
        this.tableFormOption.tableColumn.filter(item => {
          keys.push(item.prop)
          pk = item.parentKey
        })
        this.$emit('reInitData', keys.join(','), pk, this.forms[this.item.prop].length - 1, 'add')
      }
      this.searchClose()
    },
    getListData () {
      this.searchLoading = true
      let obj = {
        fieldList: (this.item.props && this.item.props.label) ? [this.item.props.label, ...(this.item.others || [])] : [],
      }
      if(this.openSearchOprate == 'addOpenTable') {
        obj.fieldList = this.item.others
      }
      obj.size = this.page.pageSize
      obj.current = this.page.currentPage
      let nomptyValue = true
      if(this.openSearchOprate == 'addOpenTable') {
        if(this.item.addBtnDataFilterable && this.item.addBtnFilterGroupList && this.item.addBtnFilterGroupList.length > 0){
          obj.groupConditions = []
          for(let gi in this.item.addBtnFilterGroupList) {
            let tgarr = []
            for(let i in this.queryParams) {
              if(i.endsWith('_1') == false) {
                if(this.queryParams[i] || this.queryParams[i] === 0 || this.queryParams[i] === false || (typeof this.queryParams[i] == 'number')) {
                  tgarr.push({fieldKey: i, enabledQueryTypes: this.queryOprator[i] || 'eq', value: this.queryParams[i]})
                }
              }
            }
            for(let df in this.item.addBtnFilterGroupList[gi]) {
              let dfit = {
                enabledQueryTypes: this.item.addBtnFilterGroupList[gi][df].enabledQueryTypes,
                fieldKey: this.item.addBtnFilterGroupList[gi][df].fieldKey,
              }
              if(['cust', 'role', 'department', 'job', 'user'].indexOf(this.item.addBtnFilterGroupList[gi][df].type) > -1) {
                dfit.value = this.item.addBtnFilterGroupList[gi][df].value
              }else{
                let tval = null
                if(typeof this.item.addBtnFilterGroupList[gi][df].value == 'object' && (this.item.addBtnFilterGroupList[gi][df].value instanceof Array)) {
                  if(this.item.addBtnFilterGroupList[gi][df].value.length > 0) {
                    if(this.item.addBtnFilterGroupList[gi][df].value.length == 1) {
                      if(this.item.parentType && this.item.parentKey && this.originForm) {
                        tval = this.originForm[this.item.addBtnFilterGroupList[gi][df].value[0]]
                      }
                    }else{
                      tval = this.forms[this.item.addBtnFilterGroupList[gi][df].value[this.item.addBtnFilterGroupList[gi][df].value.length - 1]]
                    }
                  }
                }else{
                  tval = this.forms[this.item.addBtnFilterGroupList[gi][df].value]
                }
                if((tval == undefined || tval == null || tval == '') &&  this.item.addBtnFilterGroupList[gi][df].enabledQueryTypes != 'isNull') {
                  nomptyValue = false
                }else{
                  dfit.value = tval
                }
              }
              tgarr.push(dfit)
            }
            obj.groupConditions.push(tgarr)
          }
        }
      }else{
        if(this.item.dataFilterable !== false) {
          if(this.item.dataFilterGroupList && this.item.dataFilterGroupList.length > 0){
            obj.groupConditions = []
            for(let gi in this.item.dataFilterGroupList) {
              let tgarr = []
              for(let i in this.queryParams) {
                if(i.endsWith('_1') == false) {
                  if(this.queryParams[i] || this.queryParams[i] === 0 || this.queryParams[i] === false || (typeof this.queryParams[i] == 'number')) {
                    tgarr.push({fieldKey: i, enabledQueryTypes: this.queryOprator[i] || 'eq', value: this.queryParams[i]})
                  }
                }
              }
              for(let df in this.item.dataFilterGroupList[gi]) {
                let dfit = {
                  enabledQueryTypes: this.item.dataFilterGroupList[gi][df].enabledQueryTypes,
                  fieldKey: this.item.dataFilterGroupList[gi][df].fieldKey,
                }
                if(['cust', 'role', 'department', 'job', 'user'].indexOf(this.item.dataFilterGroupList[gi][df].type) > -1) {
                  dfit.value = this.item.dataFilterGroupList[gi][df].value
                }else{
                  let tval = null
                  if(typeof this.item.dataFilterGroupList[gi][df].value == 'object' && (this.item.dataFilterGroupList[gi][df].value instanceof Array)) {
                    if(this.item.dataFilterGroupList[gi][df].value.length > 0) {
                      if(this.item.dataFilterGroupList[gi][df].value.length == 1) {
                        if(this.item.parentType && this.item.parentKey && this.originForm) {
                          tval = this.originForm[this.item.dataFilterGroupList[gi][df].value[0]]
                        }
                      }else{
                        tval = this.forms[this.item.dataFilterGroupList[gi][df].value[this.item.dataFilterGroupList[gi][df].value.length - 1]]
                      }
                    }
                  }else{
                    tval = this.forms[this.item.dataFilterGroupList[gi][df].value]
                  }
                  if((tval == undefined || tval == null || tval == '') &&  this.item.dataFilterGroupList[gi][df].enabledQueryTypes != 'isNull') {
                    nomptyValue = false
                  }else{
                    dfit.value = tval
                  }
                }
                tgarr.push(dfit)
              }
              obj.groupConditions.push(tgarr)
            }
          }else{
            obj.conditions = []
            for(let i in this.queryParams) {
              if(i.endsWith('_1') == false) {
                if(this.queryParams[i] || this.queryParams[i] === 0 || this.queryParams[i] === false || (typeof this.queryParams[i] == 'number')) {
                  obj.conditions.push({fieldKey: i, enabledQueryTypes: this.queryOprator[i] || 'eq', value: this.queryParams[i]})
                }
              }
            }
            if(this.item.dataFilterList) {
              for(let df in this.item.dataFilterList) {
                if(this.item.dataFilterList[df].fieldKey) {
                  let dfit = {
                    enabledQueryTypes: this.item.dataFilterList[df].enabledQueryTypes,
                    fieldKey: this.item.dataFilterList[df].fieldKey,
                  }
                  if(this.item.dataFilterList[df].type == 'cust') {
                    dfit.value = this.item.dataFilterList[df].value
                  }else{
                    let tval = null
                    if(typeof this.item.dataFilterList[df].value == 'object' && (this.item.dataFilterList[df].value instanceof Array)) {
                      if(this.item.dataFilterList[df].value.length > 0) {
                        if(this.item.dataFilterList[df].value.length == 1) {
                          if(this.item.parentType && this.item.parentKey && this.originForm) {
                            tval = this.originForm[this.item.dataFilterList[df].value[0]]
                          }
                        }else{
                          tval = this.forms[this.item.dataFilterList[df].value[this.item.dataFilterList[df].value.length - 1]]
                        }
                      }
                    }else{
                      tval = this.forms[this.item.dataFilterList[df].value]
                    }
                    if((tval == undefined || tval == null || tval == '') &&  this.item.dataFilterList[df].enabledQueryTypes != 'isNull') {
                      nomptyValue = false
                    }else{
                      dfit.value = tval
                    }
                  }
                  obj.conditions.push(dfit)
                }
              }
            }
          }
        }else{
          obj.conditions = []
          for(let i in this.queryParams) {
            if(i.endsWith('_1') == false) {
              if(this.queryParams[i] || this.queryParams[i] === 0 || this.queryParams[i] === false || (typeof this.queryParams[i] == 'number')) {
                obj.conditions.push({fieldKey: i, enabledQueryTypes: this.queryOprator[i] || 'eq', value: this.queryParams[i]})
              }
            }
          }
        }
      }
      if(nomptyValue) {
        getCrudDataPage(this.jvsAppId, obj, this.openSearchOprate == 'addOpenTable' ? this.item.addBtnFormId : this.item.formId, this.designId, this.item.nopermission ? this.item.prop : '', 1, 'fromForm').then(res => {
          let totalPage = 1
          if(res.data.code == 0 && res.data.data) {
            this.searchData = res.data.data.records
            this.page.total = res.data.data.total
            this.page.currentPage = res.data.data.current
            this.searchLoading = false
            totalPage = res.data.data.pages
          }
          if(this.page.total > 0 && this.searchData.length > 0) {
            if(Math.ceil(this.page.total / this.page.pageSize) !== Number.parseInt(totalPage)) {
              this.page.currentPage = 1
            }
          }else{
            this.page.currentPage = 1
          }
          this.$forceUpdate()
        }).catch(e => {
          this.searchLoading = false
        })
      }else{
        this.searchLoading = false
      }
    },
    getThousandthNumber(num) {
      let str = ''
      if(typeof num == 'number' || (typeof num == 'string' && num)) {
        str = num + ''
        str = str.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
        if(str.includes('.') == false && this.item.precision > 0) {
          str += '.'
          for(let i=0; i < this.item.precision; i++) {
            str += '0'
          }
        }
      }
      return str
    },
    showThoudsandthHandle (bool) {
      if(this.item.thoudsandthable && !this.item.disabled) {
        this.showThoudsandth = bool
      }
    },
    blurValidate () {
      if(this.item.rules && this.item.rules.length > 0) {
        if(this.item.rules[0].required) {
          if(this.forms[this.item.prop] !== null && this.forms[this.item.prop] !== undefined && this.forms[this.item.prop] !== '') {
            this.$emit('validateHandle', {type: 'clear', item: this.item})
          }else{
            this.$emit('validateHandle', {type: 'validate', item: this.item})
          }
        }
      }
    },
    getDisableexpress (type) {
      let bool = true
      let color = ""
      let bgcolor = ''
      if(this.item.disabled) {
        if(this.item.expressDisplay) {
          if(this.item.expressDisplay.backColor || this.item.expressDisplay.textcolor) {
            bool = false
            if(this.item.expressDisplay.backColor) {
              bgcolor = this.item.expressDisplay.backColor
            }
            color = this.item.expressDisplay.textcolor
          }
          if(this.item.expressDisplay.conditionControl){
            this.item.expressDisplay.conditionControl.filter(cit => {
              if(cit.value == this.form[this.item.prop]) {
                bgcolor = cit.bgcolor
                color = cit.color
                bool = false
              }
            })
          }
        }
      }
      if(type == 'show') {
        if(!this.form[this.item.prop]) {
          bool = true
        }
        return bool
      }
      if(type == 'color') {
        return color ? (color.startsWith('#') ? color : `;-webkit-text-fill-color: transparent;background:${color};background-clip: text!important;-webkit-background-clip: text!important;`) : ``
      }
      if(type == 'bgcolor') {
        return bgcolor
      }
    },
    formatUrl (url) {
      if(url.includes('?')) {
        if(!url.includes('jvsAppId')) {
          url += `&jvsAppId=${this.jvsAppId}`
        }
      }else{
        url += `?jvsAppId=${this.jvsAppId}`
      }
      return url
    },
    handleExceed (){
      // this.$message.warning(`最多可同时选择${this.item.limit} 个文件，请重新选择文件`);
      this.$notify({
        title: '提示',
        message: `最多可同时选择${this.item.limit} 个文件，请重新选择文件`,
        position: 'bottom-right',
        type: 'warning'
      });
    },
    handleUploadRequest (file){
      if(this.item.fileSize &&  file.file.size > (Number(this.item.fileSize) * 1024 * 1024)) {
        this.fileValidate = true
        if(this.$refs['uploadFileBtn'+'_'+this.item.prop]) {
          if(this.item.limit < 2) {
            this.$refs['uploadFileBtn'+'_'+this.item.prop].clearFiles()
          }else{
            for(let i in this.$refs['uploadFileBtn'+'_'+this.item.prop].uploadFiles) {
              if(this.$refs['uploadFileBtn'+'_'+this.item.prop].uploadFiles[i].uid == file.file.uid) {
                this.$refs['uploadFileBtn'+'_'+this.item.prop].uploadFiles.splice(i, 1)
              }
            }
            let _this = this
            setTimeout(()=> {
              _this.fileValidate = false
            }, 500)
          }
        }
      }else{
        this.fileValidate = false
        this.fileList.push(file.file);
        this.fileListCopy.push(file.file)
      }
    },
    dealUpload () {
      let newFileList = []
      this.fileListCopy.sort((a, b)=>{
        return a.size - b.size
      })
      this.fileListCopy.forEach( (item)=>{
        newFileList.push({
          name: item.name,
          percentage: 0
        })
      })
      if(this.fileListCopy.length > 0){
        let channelBroad = new BroadcastChannel('upload-tree')
        channelBroad.postMessage({type:'uploadFile', data:{
          userId: this.$store.getters.userInfo.id,
          fileList: newFileList,
          isFinish: false
        }})
        this.startUploadList(newFileList, this.fileListCopy.shift())
      }
    },
    async startUploadList(newFileList, item){
      let that = this
      // 休眠
      function sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms))
      }
       // 更新文件状态
      function changeFileStatus(fileList, isFinish){
        let channelBroad = new BroadcastChannel('upload-tree')
        channelBroad.postMessage({type:'uploadFile',data:{
          userId: that.$store.getters.userInfo.id,
          fileList: fileList,
          isFinish: isFinish
        }})
      }
      // 上传失败
      function uploadError(){
        newFileList[that.fileUploadFailIndex].isFinish = true
        newFileList[that.fileUploadFailIndex].isfail = true
        that.fileUploadFailIndex ++
        that.startUploadList(newFileList,that.fileListCopy.shift())
        changeFileStatus(newFileList,false)
      }
      // 上传成功
      function uploadSuccess(response){
        newFileList[that.fileUploadFailIndex].percentage = 100
        newFileList[that.fileUploadFailIndex].isfail = false
        newFileList[that.fileUploadFailIndex].isFinish = true
        if(response) {
          newFileList[that.fileUploadFailIndex].response = response
        }
        that.$forceUpdate()
        that.fileUploadFailIndex ++
        that.startUploadList(newFileList, that.fileListCopy.shift())
        changeFileStatus(newFileList,false)
      }
      await sleep(500)
      if(!item){
        this.fileList = []
        this.fileUploadFailIndex = 0
        let channelBroad = new BroadcastChannel('upload-tree')
        channelBroad.postMessage({type:'uploadFile', data:{ userId: this.$store.getters.userInfo.id, newFileList }},"*")
        changeFileStatus(newFileList, true)
        return
      }
      if(item.size / 1024 / 1024 >= 20) {
        await uploadByPieces({
          files: [item],
          module: '/jvs-ui/form/',
          pieceSize: 5,
          progress: (num, file) => {
            newFileList[this.fileUploadFailIndex].percentage = parseInt(num)>=98?98:parseInt(num)
            let channelBroad = new BroadcastChannel('upload-tree')
            channelBroad.postMessage({type:'uploadFile',data:{
              userId: this.$store.getters.userInfo.id,
              fileList: newFileList
            }})
          },
          success: (data) => {
            uploadSuccess({code: 0, data: data})
            that.handleSuccess({
              code: 0,
              data: {
                bucketName: data.bucketName,
                fileName: data.fileName,
                originalFileName: data.name,
                size: data.size,
                fileLink: data.filePath,
                fileSize: data.fileSize,
              }
            }, item, newFileList)
          },
          error: (e) => {
            uploadError()
          },
          businessId: this.jvsAppId
        });
      } else {
        let fetchForm = new FormData()
        fetchForm.append("file", item)
        fetchForm.append("module", '/jvs-ui/form/')
        await fileImport(fetchForm, (e) => {
          newFileList[that.fileUploadFailIndex].percentage = parseInt(e)>=98?98:parseInt(e)
          let channelBroad = new BroadcastChannel('upload-tree')
          channelBroad.postMessage({
            type: 'uploadFile',
            data: {
              userId: that.$store.getters.userInfo.id,
              fileList: newFileList
            }})
        }, this.jvsAppId).then((res) => {
          uploadSuccess(res.data)
          that.handleSuccess(res.data)
        }).catch(err => {
          console.log(err)
        });
      }
    },
    // 添加流程设计
    getRandomId(){
      return (Math.floor(Math.random() * (99999 - 10000)) + 10000).toString() + new Date().getTime().toString().substring(5, 11)
    },
    addFlowRowHandle () {
      let obj = {
        name: '',
        id: this.getRandomId(),
        nodeForm: {formId: "", sendUserForm: true, version: "" },
        type: nodeType.SP
      }
      let props = JSON.parse(JSON.stringify(getDefaultNodeProps(obj.type)))
      props.btn = JSON.parse(JSON.stringify(nodeButtonList))
      obj.props = props
      if(!this.forms[this.item.prop]) {
        this.forms[this.item.prop] = []
      }
      this.forms[this.item.prop].push(obj)
      this.$forceUpdate()
    },
    cascaderDataFilter (list, bool) {
      for(let i in list) {
        if(list[i][this.item.datatype == 'dataModel' ? 'value' : (this.item.emitKey ? this.item.emitKey : 'value')] == this.forms[this.item.filterProp]) {
          list[i].disabled = true
          if(list[i].children && list[i].children.length > 0) {
            this.cascaderDataFilter(list[i].children, true)
          }
        }else{
          if(bool == true) {
            list[i].disabled = true
            if(list[i].children && list[i].children.length > 0) {
              this.cascaderDataFilter(list[i].children, true)
            }
          }else{
            if(list[i].children && list[i].children.length > 0) {
              this.cascaderDataFilter(list[i].children)
            }
          }
        }
      }
    },
    jsonChange (con) {
      if(con == 'error') {
        this.$set(this.forms, this.item.prop, '')
      }else{
        this.$set(this.forms, this.item.prop, con)
      }
      this.formChange()
    },
    deleteBeaconHandle (beacon, index) {
      this.forms[this.item.prop].splice(index, 1)
      this.$forceUpdate()
    },
    getPageFixQuery () {
      let temp = []
      if(this.item.pageQuery && this.item.pageQuery.length > 0) {
        this.item.pageQuery.filter(it => {
          if(it.fieldKey && it.enabledQueryTypes) {
            temp.push({
              fieldKey: it.fieldKey,
              enabledQueryTypes: it.enabledQueryTypes,
              value: this.forms[it.value]
            })
          }
        })
      }
      return temp
    },
    getPageSetting () {
      let temp = {}
      if(this.item.pageSearch && this.item.pageSearch.length > 0) {
        temp.pageSearch = this.item.pageSearch
      }
      if(this.item.pageQuery && this.item.pageQuery.length > 0) {
        let tl = []
        this.item.pageQuery.filter(it => {
          if(it.fieldKey && it.enabledQueryTypes) {
            tl.push({
              fieldKey: it.fieldKey,
              enabledQueryTypes: it.enabledQueryTypes,
              value: this.forms[it.value]
            })
          }
        })
        temp.pageQuery = tl
      }
      return temp
    },
    ruleDownLoad (name, data) {
      const dotIndex = name.lastIndexOf('.');
      let downLoadName = name
      if (dotIndex === -1 || dotIndex === name.length - 1) {
          // 没有找到点，没有后缀
          downLoadName = name+data.fileType
      }
      ruleDownLoad(this.jvsAppId, {
        ...data,
        name:downLoadName
      }).then(res => {
        if(res && res.data && res.headers["output_format"]){
          this.downloadFile(downLoadName, res.data)
        }
      })
    },
    // 下载文件
    downloadFile (filename, content, attr) {
      var elink = document.createElement('a')
      if(filename) {
        elink.download = filename
      }
      elink.style.display = 'none'
      var blob = new Blob([content], attr == 'type' ? {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8'} : {})
      elink.href = URL.createObjectURL(blob)
      document.body.appendChild(elink)
      elink.click()
      document.body.removeChild(elink)
    },
    getPreviewUrl (row, item, index) {
      this.previewFile(row[item.prop][index])
    },
    // 预览文件
    previewFile(row) {
      let protocolhost = this.$store.getters.kkfileUrl || "";
      // console.log(row, protocolhost);
      if (protocolhost && row.url) {
        let view_url =
          `${protocolhost}/onlinePreview?forceUpdatedCache=true&officePreviewType=pdf&url=` +
          encodeURIComponent(Base64.encode(decodeURIComponent(row.url)));
        // console.log(
        //   decodeURIComponent(row.url),
        //   Base64.encode(decodeURIComponent(row.url)),
        //   view_url
        // );
        this.$openUrl(view_url, "_blank");
      }
    },
    // 签字板
    async signatureSubmit (data) {
      if(data) {
        customUpload(`/mgr/jvs-auth/upload/jvs-form-design`, data, this.item.label).then(res => {
          if(res && res.data && res.data.code == 0 && res.data.data && res.data.data.fileLink) {
            let obj = {
              name: res.data.data.originalFileName || res.data.data.name,
              url: res.data.data.fileLink,
              fileName: res.data.data.fileName,
              bucketName: res.data.data.bucketName,
              fileSize: res.data.data.fileSize,
            }
            this.$set(this.forms, this.item.prop, [obj])
          }
        })
      }
    },
    // 内嵌列表页获取了数据
    pageGetList (data) {
      let temp = []
      data.filter(dit => {
        temp.push(dit.id)
      })
      this.$set(this.forms, this.item.prop, temp)
    },
    getDicDataByExpress (list) {
      if(this.item.dataItemExpressable) {
        if(this.item.dataItmeBindProp) {
          let temp = []
          let formTemp = this.forms
          let bindDomNode = {}
          let bindkey = this.item.dataItmeBindProp
          if(typeof this.item.dataItmeBindProp == 'object' && this.item.dataItmeBindProp instanceof Array) {
            formTemp = this.originForm
            bindkey = this.item.dataItmeBindProp[this.item.dataItmeBindProp.length-1]
            if(this.item.dataItmeBindProp.length > 1) {
              this.getNodeDom(this.originOption.column, this.item.dataItmeBindProp, bindDomNode)
            }
          }
          if(bindDomNode && bindDomNode.prop) {
            if(bindDomNode.parentDom && bindDomNode.parentDom.length > 0) {
              if(bindDomNode.parentDom[0].type == 'tab') {
                if(bindDomNode.parentDom[0].detachData) {
                  if(bindDomNode.parentDom[1].prop) {
                    formTemp = this.originForm[bindDomNode.parentDom[1].prop]
                  }
                }else{
                  formTemp = this.forms
                }
              }
            }
          }
          list.filter(dit => {
            if(formTemp && formTemp[bindkey]) {
              if(typeof formTemp[bindkey] == 'object' && formTemp[bindkey] instanceof Array) {
                if(formTemp[bindkey].indexOf(dit[this.item.props ? this.item.props.value : 'value']) > -1) {
                  temp.push(dit)
                }
              }else{
                if(dit[this.item.props ? this.item.props.value : 'value'] == formTemp[bindkey]) {
                  temp.push(dit)
                }
              }
            }
          })
          this.selectOption = temp
          this.$forceUpdate()
        }
      }
    },
    getNodeDom (list, keys, currentDomNode) {
      for(let i in list) {
        if(list[i].parentKey) {
          if((list[i].parentKey+'.'+list[i].prop) == keys.join('.')) {
            for(let k in list[i]) {
              this.$set(currentDomNode, k, list[i][k])
            }
          }
        }else{
          if(list[i].prop == keys.join('')) {
            for(let k in list[i]) {
              this.$set(currentDomNode, k, list[i][k])
            }
          }
        }
        if(list[i].type == 'tab' && list[i].column) {
          for(let j in list[i].column) {
            if(list[i].column[j] && list[i].column[j].length > 0) {
              this.getNodeDom(list[i].column[j], keys, currentDomNode)
            }
          }
        }
        if(list[i].type == 'tableForm' && list[i].tableColumn && list[i].tableColumn.length > 0) {
          this.getNodeDom(list[i].tableColumn, keys, currentDomNode)
        }
      }
    },
    addDymicFormRowHandle () {
      if(!this.forms[this.item.prop]) {
        this.$set(this.forms, this.item.prop, [])
      }
      if(this.$refs['dynamicForm_'+this.item.prop]) {
        let tob = this.$refs['dynamicForm_'+this.item.prop].getobj({type: 'input'})
        if(tob) {
          this.forms[this.item.prop].push(tob)
        }
      }else{
        this.forms[this.item.prop].push({})
      }
      this.$forceUpdate()
    },
    comTypeChange (row, index) {
      if(this.$refs['dynamicForm_'+this.item.prop]) {
        let tob = this.$refs['dynamicForm_'+this.item.prop].getobj(row)
        if(tob) {
          this.$set(this.forms[this.item.prop], index, tob)
        }
      }
      this.$forceUpdate()
    },
    deleteCom (index) {
      this.forms[this.item.prop].splice(index, 1)
      this.$forceUpdate()
    },
    colorChangeHandle (value, prop, form) {
      if(form) {
        this.$set(form, prop, value)
      }
      this.formChange(value)
    },
    // 复制
    copyHandle (value) {
      const text = document.createElement('input')
      text.value = value
      document.body.appendChild(text)
      text.select()
      document.execCommand('Copy')
      document.body.removeChild(text)
      this.$notify({
        title: this.$langt('common.tip'),
        message: this.$langt('common.copySuccess'),
        position: 'bottom-right',
        type: 'success'
      });
    },
  },
  watch: {
    item: {
      handler (newVal, oldVal) {
        if(this.delcomRandom && this.delcomRandom > -1) {
          if(newVal.datatype == 'option') {
            this.selectOption = newVal.dicData
          }
          return false
        }
        let bool = false
        if(newVal.dicUrl && oldVal.dicUrl && newVal.dicUrl == oldVal.dicUrl) {
          bool = true
        }
        if(newVal.url && oldVal.url && newVal.url == oldVal.url) {
          bool = true
        }
        if(newVal.datatype == 'rule' && newVal.optionHttp == oldVal.optionHttp) {
          bool = true
        }
        // 优化级联重复请求设置选项数据
        if(newVal.type == 'cascader') {
          if(newVal.datatype == 'system') {
            if(newVal.dictName == oldVal.dictName) {
              bool = true
            }
          }else{
            if(newVal.formId == oldVal.formId) {
              bool = true
            }
          }
        }
        let reInitBool = false
        if(['tab', 'tableForm'].indexOf(newVal.type) > -1) {
          reInitBool = true
        }else{
          if(newVal.parentKey && (newVal.parentDom || newVal.parent)) {
            let newOb = Object.assign({}, newVal)
            let oldOb = Object.assign({}, oldVal)
            delete newOb.parentDom
            delete oldOb.parentDom
            delete newOb.parent
            delete oldOb.parent
            reInitBool = (JSON.stringify(newOb) !== JSON.stringify(oldOb))
          }else{
            reInitBool = (JSON.stringify(newVal) !== JSON.stringify(oldVal))
          }
          // 设计时，其余属性变动需重新请求
          if(!reInitBool) {
            if(this.lastItemData) {
              if(newVal.parentKey && newVal.parentDom) {
                let newOb = Object.assign({}, newVal)
                let oldOb = Object.assign({}, this.lastItemData)
                delete newOb.parentDom
                delete oldOb.parentDom
                let sameBool = true
                let ks = Object.keys(newOb)
                for(let i in ks) {
                  if(typeof newOb[ks[i]] != 'function' && (ks[i] == 'dicData' ? (newOb.datatype == 'option') : true) && JSON.stringify(newOb[ks[i]]) != JSON.stringify(oldOb[ks[i]])) {
                    sameBool = false
                  }
                }
                reInitBool = !sameBool
              }else{
                let sameBool = true
                let ks = Object.keys(newVal)
                for(let i in ks) {
                  if(typeof newVal[ks[i]] != 'function' && (ks[i] == 'dicData' ? (newVal.datatype == 'option') : true) && JSON.stringify(newVal[ks[i]]) != JSON.stringify(this.lastItemData[ks[i]])) {
                    sameBool = false
                  }
                }
                reInitBool = !sameBool
                bool = !reInitBool
              }
              if(reInitBool) {
                this.selectOption = []
              }
            }
          }
        }
        if(reInitBool) {
          this.initItem(bool, 'create')
        }
        // 下拉框 单选 自定义选择
        if(this.item.currVal) {
          if(this.item.multiple) {
            if(this.item.currVal instanceof Array !== true) {
              this.$set(this.forms, this.item.prop, [])
            }else{
              this.$set(this.forms, this.item.prop, this.item.currVal)
            }
          }else{
            if(this.item.currVal instanceof Array !== true) {
              this.$set(this.forms, this.item.prop, this.item.currVal)
            }else{
              this.$set(this.forms, this.item.prop, "")
            }
          }
        }
        if(this.item.type == 'htmlEditor'){
          if(newVal.prop != oldVal.prop) {
            if($('#' + this.item.prop + '-editor')) {
              $('#' + this.item.prop + '-editor').html("")
            }
            if(this.editor) {
              this.editor.destroy()
            }
            this.initEditor(this.item.prop)
          }else{
            if(this.editor) {
              if(this.item.disabled) {
                this.editor.disable()
              }else{
                this.editor.enable()
              }
            }
          }
        }
      },
      deep: true
    },
    freshBoolean: {
      handler(newVal, oldVal) {
        if(this.item.type == 'htmlEditor') {
          $('#' + this.item.prop + '-editor').html("")
          this.initEditor(this.item.prop)
        }else{
          $('#' + this.item.prop + '-editor').html("")
          this.editor.destroy()
        }
      }
    },
    reinitFlag: {
      handler(newVal, oldVal) {
        if(newVal != -1) {
          this.initItem()
        }
      }
    },
    resetRadom: {
      handler (newVal, oldVal) {
        if(newVal > -1) {
          if(this.item.type == 'htmlEditor') {
            this.editor.txt.html(this.initHtml)
          }
          if(this.item.type == 'checkbox') {
            if(!this.forms[this.item.prop]) {
              this.$set(this.forms, this.item.prop, [])
            }
          }
          if(this.item.type == 'reportTable') {
            this.initItem()
          }
        }
      }
    },
    changeRandom: {
      handler(newVal, oldVal) {
        if(newVal > -1) {
          if((this.item.dataFilterable || this.item.dataItemExpressable) && !this.isView && this.item.type != 'cascader' && this.item.datatype != 'rule') {
            if(this.item.parentType == 'tableForm') {
              if(this.item.hasRelationPropList && this.item.hasRelationPropList.length > 0) {
                if(this.item.hasRelationPropList.indexOf(this.changeDomItem.parentKey ? `${this.changeDomItem.parentKey}.${this.changeDomItem.prop}` : this.changeDomItem.prop) > -1) {
                  this.getSelectUrlData()
                }
              }
            }else{
              this.getSelectUrlData()
            }
          }
          if(this.item.datatype == 'rule' && !this.isView) {
            if(this.item.ruleOptionDom && this.item.ruleOptionDom.indexOf(this.changeDomItem.prop) > -1) {
              if(this.item.parentType == 'tableForm') {
                if(this.item.hasRelationPropList && this.item.hasRelationPropList.length > 0) {
                  if(this.item.hasRelationPropList.indexOf(this.changeDomItem.parentKey ? `${this.changeDomItem.parentKey}.${this.changeDomItem.prop}` : this.changeDomItem.prop) > -1) {
                    this.getSelectRuleData()
                  }
                }
              }else{
                this.getSelectRuleData()
              }
            }
          }
          if(this.item.type == 'cascader') {
            let needRequire = false
            if(this.item.dataFilterGroupList && this.item.dataFilterGroupList.length > 0) {
              for(let i in this.item.dataFilterGroupList) {
                this.item.dataFilterGroupList[i].filter(dit => {
                  if(dit.fieldKey == this.changeDomItem.prop) {
                    needRequire = true
                  }
                })
              }
            }
            if(this.item.dataFilterList && this.item.dataFilterList.length > 0) {
              this.item.dataFilterList.filter(dit => {
                if(dit.fieldKey == this.changeDomItem.prop) {
                  needRequire = true
                }
              })
            }
            if(needRequire) {
              this.getCascaderData()
            }
          }
          if(this.item.datatype == 'option' && this.item.dataItmeBindProp == this.changeDomItem.prop) {
            this.getDicDataByExpress(this.item.dicData)
          }
        }
      }
    },
    // 被嵌套表格自适应宽判断
    parentDomWidth: {
      handler(newVal, oldVal) {
        if(newVal && newVal > 0) {
          if(this.item.type == 'tableForm') {
            if(this.$refs[this.item.prop]) {
              let tw = 0
              this.item.tableColumn.filter(ti => {
                tw += ti.span * ((this.item.columWidth && this.item.columWidth > 8) ? this.item.columWidth : 8)
              })
              let w = this.$refs[this.item.prop].offsetWidth - 4
              if(!this.item.editable) {
                w -= 16 // 外部padding: 0 10px;排除边框4
              }
              if(w <= 0) {
                w = (newVal / 24) * (this.item.span)
              }
              let otherColumnW = 50 // 序号50
              if(!this.item.disabled && !this.item.iconBtn) {
                if(this.item.addBtnOrigin != 'table') {
                  if(!this.item.editable){
                    if(this.item.editBtn || this.item.viewBtn || this.item.delBtn) {
                      if(this.item.editBtn && this.item.viewBtn && this.item.delBtn) {
                        otherColumnW += 120
                      }else{
                        otherColumnW += 80
                      }
                    }
                  }else{
                    if(this.item.delBtn !== false) {
                      otherColumnW += 80
                    }
                  }
                }else{
                  if(this.item.delBtn !== false) {
                    otherColumnW += 80
                  }
                }
              }
              let difference = w - otherColumnW - tw
              if(difference > 0) {
                let avew = difference / this.item.tableColumn.length
                this.item.tableColumn.filter(ti => {
                  ti.width = ti.span * ((this.item.columWidth && this.item.columWidth > 8) ? this.item.columWidth : 8) + avew
                })
              }
            }
          }
        }
      }
    },
    fileList(fileList, oldFileList) {
      if (fileList.length) {
        this.$nextTick(() => {
          this.dealUpload();
        });
      }
    },
    dataTriggerFresh: {
      handler(newVal, oldVal) {
        if(newVal > -1) {
          if(this.item.type == 'htmlEditor') {
            if(this.forms[this.item.prop] && this.forms[this.item.prop] != '<p></p>') {
              this.editor.txt.html(this.forms[this.item.prop])
            }else{
              this.editor.txt.html('')
            }
          }
        }
      }
    },
    ruleChange: {
      handler(newVal, oldVal) {
        if(newVal > -1) {
          if(['imageUpload', 'fileUpload'].indexOf(this.item.type) > -1) {
            if(this.form[this.item.prop]) {
              if(!this.item.parentKey || this.item.parentType != 'tableForm'){
                if(typeof this.form[this.item.prop] == 'object' && (this.form[this.item.prop] instanceof Array) == false) {
                  this.$set(this.form, this.item.prop, [])
                }
                this.$set(this.item, 'fileList', this.form[this.item.prop])
              }
              this.$set(this.tableFileList, this.tableRowAIndex, this.form[this.item.prop])
            }
          }
          if(this.item.type == 'htmlEditor') {
            $('#' + this.item.prop + '-editor').html("")
            this.initEditor(this.item.prop)
          }
        }
      }
    },
    form: {
      handler(newVal, oldVal) {
        if(this.item.type == 'htmlEditor') {
          $('#' + this.item.prop + '-editor').html("")
          this.initEditor(this.item.prop)
        }
      }
    }
  },
  created () {
    if(this.item.shortcutEnable) {
      if(this.item.datetype == 'daterange') {
        this.startEndLimitHandle.shortcuts = [
          {
            text: '今天',
            onClick(picker) {
              picker.$emit('pick', [new Date(), new Date()]);
            }
          },
          {
            text: '最近三天',
            onClick(picker) {
              const date = new Date();
              date.setTime(date.getTime() - 3600 * 1000 * 24 * 2)
              picker.$emit('pick', [date, new Date()]);
            }
          },
          {
            text: '最近一周',
            onClick(picker) {
              const date = new Date();
              date.setTime(date.getTime() - 3600 * 1000 * 24 * 6);
              picker.$emit('pick', [date, new Date()]);
            }
          },
          {
            text: '本月',
            onClick(picker) {
              let today = new Date();
              let ty = today.getFullYear();
              let tm = today.getMonth() + 1;
              const date = new Date(ty + '-' + `${tm > 9 ? tm : ('0'+tm)}` + '-01');
              let days = ([1, 3, 5, 7, 8, 10, 12].indexOf(tm) > -1) ? 31 : (tm == 2 ? (ty % 4 == 0 ? 29 : 28) : 30)
              let lastOneDay = new Date(ty + '-' + `${tm > 9 ? tm : ('0'+tm)}` + `-${days}`);
              picker.$emit('pick', [date, lastOneDay]);
            }
          },
          {
            text: '三个月内',
            onClick(picker) {
              const date = new Date();
              date.setTime(date.getTime() - 3600 * 1000 * 24 * (30 * 3 - 1));
              picker.$emit('pick', [date, new Date()]);
            }
          },
          {
            text: '今年',
            onClick(picker) {
              let ty = new Date().getFullYear();
              picker.$emit('pick', [new Date(ty + '-01-01'), new Date(ty + '-12-31')]);
            }
          }
        ]
      }
      if(this.item.datetype == 'monthrange') {
        this.startEndLimitHandle.shortcuts = [
          {
            text: '本月',
            onClick(picker) {
              picker.$emit('pick', [new Date(), new Date()]);
            }
          },
          {
            text: '近三月',
            onClick(picker) {
              let y = new Date().getFullYear()
              let m = new Date().getMonth() + 1
              let cou = m - 2
              if(cou < 0) {
                y -= 1
                m += cou
              }else{
                m -= 2
              }
              const date = new Date(y + '-' + (m < 10 ? `0${m}` : m));
              picker.$emit('pick', [date, new Date()]);
            }
          },
          {
            text: '近半年',
            onClick(picker) {
              let y = new Date().getFullYear()
              let m = new Date().getMonth() + 1
              let cou = m - 5
              if(cou < 0) {
                y -= 1
                m += cou
              }else{
                m -= 5
              }
              const date = new Date(y + '-' + (m < 10 ? `0${m}` : m));
              picker.$emit('pick', [date, new Date()]);
            }
          },
        ]
      }
      if(this.item.datetype == 'datetimerange') {
        this.startEndLimitHandle.shortcuts = [
          {
            text: '最近五分钟',
            onClick(picker) {
              const date = new Date();
              date.setTime(date.getTime() - 60 * 5 * 1000)
              picker.$emit('pick', [date, new Date()]);
            }
          },
          {
            text: '最近一小时',
            onClick(picker) {
              const date = new Date();
              date.setTime(date.getTime() - 3600 * 1000, new Date())
              picker.$emit('pick', [date, new Date()]);
            }
          },
          {
            text: '今天',
            onClick(picker) {
              let date = new Date()
              let y = date.getFullYear()
              let m = date.getMonth() + 1
              if(m < 9) {
                m = `0${m}`
              }
              let d = date.getDate()
              if(d < 9) {
                d = `0${d}`
              }
              picker.$emit('pick', [new Date(`${y}-${m}-${d} 00:00:00`), new Date()]);
            }
          },
          {
            text: '最近三天',
            onClick(picker) {
              const date = new Date();
              date.setTime(date.getTime() - 3600 * 1000 * 24)
              picker.$emit('pick', [date, new Date()]);
            }
          },
          {
            text: '最近一周',
            onClick(picker) {
              const date = new Date();
              date.setTime(date.getTime() - 3600 * 1000 * 24 * 6);
              picker.$emit('pick', [date, new Date()]);
            }
          },
          {
            text: '本月',
            onClick(picker) {
              let today = new Date();
              let ty = today.getFullYear();
              let tm = today.getMonth() + 1;
              const date = new Date(ty + '-' + `${tm > 9 ? tm : ('0'+tm)}` + '-01 00:00:00');
              let days = ([1, 3, 5, 7, 8, 10, 12].indexOf(tm) > -1) ? 31 : (tm == 2 ? (ty % 4 == 0 ? 29 : 28) : 30)
              let lastOneDay = new Date(ty + '-' + `${tm > 9 ? tm : ('0'+tm)}` + `-${days} 23:59:59`);
              picker.$emit('pick', [date, lastOneDay]);
            }
          },
          {
            text: '三个月内',
            onClick(picker) {
              const date = new Date();
              date.setTime(date.getTime() - 3600 * 1000 * 24 * 30 * 3);
              picker.$emit('pick', [date, new Date()]);
            }
          }
        ]
      }
    }
    this.initItem(false, 'create')
  },
  mounted () {
    if(this.item.type == 'htmlEditor') {
      $('#' + this.item.prop + '-editor').html("")
      this.initEditor(this.item.prop)
    }
    if(this.item.type == 'iconSelect') {
      this.iconToolWidth = this.getWidth(this.item)
    }
    if(this.item.type == 'tableForm') {
      if(this.$refs[this.item.prop]) {
        let tw = 0
        this.item.tableColumn.filter(ti => {
          tw += ti.span * ((this.item.columWidth && this.item.columWidth > 8) ? this.item.columWidth : 8)
        })
        let otherColumnW = 50 // 序号50
        if(!this.item.disabled && !this.item.iconBtn) {
          if(this.item.addBtnOrigin != 'table') {
            if(!this.item.editable){
              if(this.item.editBtn || this.item.viewBtn || this.item.delBtn) {
                if(this.item.editBtn && this.item.viewBtn && this.item.delBtn) {
                  otherColumnW += 120
                }else{
                  otherColumnW += 80
                }
              }
            }else{
              if(this.item.delBtn !== false) {
                otherColumnW += 80
              }
            }
          }else{
            if(this.item.delBtn !== false) {
              otherColumnW += 80
            }
          }
        }
        let difference = this.$refs[this.item.prop].offsetWidth - 4 - otherColumnW - tw
        if(!this.item.editable) {
          difference -= 16 // 外部padding: 0 10px;排除边框4
        }
        if(difference > 0) {
          let avew = difference / this.item.tableColumn.length
          this.item.tableColumn.filter(ti => {
            ti.width = (ti.span * ((this.item.columWidth && this.item.columWidth > 8) ? this.item.columWidth : 8)) + avew
          })
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.search-form{
  /deep/.before-append-item{
    .el-form-item__label{
      // min-width: 80px;
      word-break: keep-all;
    }
    .el-form-item__content{
      margin-left: 0!important;
      .form-item-tips{
        display: none;
      }
    }
    .before-append-content{
      .bofore-append{
        width: 70px;
        display: block;
        .el-input__inner{
          padding-right: 15px;
          border-right: 0;
          border-radius: 4px 0 0 4px;
        }
        .el-input__suffix{
          display: none;
        }
        .el-input.is-focus .el-input__inner, .el-input__inner:focus{
          border-color: #DCDFE6;
        }
      }
    }
    .jvs-form-item{
      width: calc(100% - 70px);
      .el-input__inner{
        border-radius: 0 4px 4px 0;
        padding: 0;
      }
      .el-date-editor{
        width: 100%;
        padding-right: 0!important;
      }
    }
  }
  .user-info-list{
    /deep/.input-with-select{
      .el-input__inner{
        border-right: 0;
        border-radius: 0!important;
      }
    }
  }
}
.bottom-add-button{
  margin-top: 8px;
  .button{
    width: 80px;
    display: flex;
    align-items: center;
    cursor: pointer;
    .icon{
      width: 16px;
      height: 16px;
      background: #1E6FFF;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 4px;
      svg{
        width: 12px;
        height: 12px;
        fill: #fff;
      }
    }
    span{
      font-family: Microsoft YaHei-Regular, Microsoft YaHei;
      font-weight: 400;
      font-size: 14px;
      color: #1E6FFF;
      line-height: 18px;
    }
  }
}
.delete-icon-button{
  width: 16px;
  height: 16px!important;
  background: #36B452;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  cursor: pointer;
  .border-line{
    width: 10px;
    height: 2px;
    background: #fff;
    border-radius: 2px;
  }
}
.jvs-upload-file-text-list{
  /deep/.el-upload-list__item:first-child{
    margin-top: 0;
  }
}
</style>
<style lang="scss">
$jvsFormItemHeight: 36px;
.loading-back {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
}
.form-list-upload-img {
  .el-upload--picture-card {
    display: inline-block;
  }
}
.form-list-upload-img-none {
  .el-upload--picture-card {
    display: none;
  }
  .el-upload-list {
    height: 148px!important;
  }
}
.form-list-upload-file {
  width: 100%;
  .el-upload--text {
    display: inline-block;
  }
}
.form-list-upload-file-none {
  width: 100%;
  .el-upload--text {
    display: none;
  }
}
.icon-select-tool{
  display: flex;
  flex-wrap: wrap;
  height: 200px;
  scrollbar-width: none; /* firefox */
  -ms-overflow-style: none; /* IE 10+ */
  overflow-x: hidden;
  overflow-y: auto;
  padding-left: 15px;
  i{
    margin: 10px;
    display: block;
    width: 20px;
    height: 20px;
    line-height: 20px;
    cursor: pointer;
  }
  i:hover{
    color: #409EFF;
  }
  .icon {
    fill: currentColor;
    overflow: hidden;
    cursor: pointer;
    width: 30px;
    height: 30px;
    margin-bottom: 10px;
    margin-left: 10px;
  }
}
.icon-select-tool::-webkit-scrollbar {
  display: none; /* Chrome Safari */
}
.icon-select-tool-position{
  position: absolute;
  height: 158px;
  top: 45px;
  margin: 0;
  z-index: 9;
}
.jvs-form-item{
  min-height: $jvsFormItemHeight;
  position: relative;
  .el-slider, p, .el-input-number, .el-select, .el-date-editor, .form-item-icon-selct, .el-tabs, .el-cascader, .user-info-list{
    flex: 1;
  }
  .el-input-number{
    .el-input__inner{
      text-align: left;
    }
  }
  .el-input-number.is-disabled{
    // flex: none;
    width: 100%;
    .el-input{
      width: auto;
      .el-input__inner{
        padding-right: 0;
      }
    }
  }
  .show-thoudsandth-number{
    .el-input__inner{
      color: transparent!important;
    }
  }
  .input-number-Thousandth{
    position: absolute;
    left: 16px;
    font-size: 12px;
  }
  .input-number-Thousandth-disabled{
    left: 0;
    font-size: 14px;
  }
  // 隐藏文本框前置图标
  .el-input--prefix{
    .el-input__prefix{
      display: none;
    }
    .el-input__inner{
      padding-left: 15px;
    }
  }
  // 隐藏禁用状态下placeholder
  .el-input.is-disabled{
    font-size: 12px;
    .el-input__inner::-webkit-input-placeholder{
      color: transparent;
    }
    .el-input__inner::-moz-input-placeholder{
      color: transparent;
    }
    .el-input__inner::-ms-input-placeholder{
      color: transparent;
    }
  }
  // p文字
  .form-item-p{
    span{
      display: inline-block;
      box-sizing: border-box;
      overflow: hidden;
      position: relative;
      padding-left: 10px;
      height: $jvsFormItemHeight;
      i,b{
        font-weight: normal;
        font-style: normal;
      }
      i{
        width: 4px;
        border-radius: 2px;
        height: 20px;
        background-color: rgb(52, 113, 255);
        display: inline-block;
        line-height: $jvsFormItemHeight;
        position: absolute;
        top: 4px;
        left: 0;
      }
    }
  }
  .table-form{
    overflow: hidden;
    .jvs-table-notitle {
      display: none;
    }
    .el-card {
      border-width: 0;
    }
    .jvs-table{
      .el-table__header-wrapper {
        margin-top: 0;
      }
      .el-table__body-wrapper {
        .el-table__empty-block {
          border-top: 0;
        }
      }
      .cell {
        > div {
          width: 100%;
        }
        .el-radio-group,
        .el-checkbox-group {
          width: 100%;
          div {
            display: flex;
            flex-wrap: wrap;
            .el-radio,
            .el-checkbox {
              min-width: 50%;
              margin-right: 0;
              text-align: left;
            }
          }
        }
        .demo-dynamic {
          .el-form-item {
            padding: 0;
            .el-input.is-disabled {
              .el-input__inner {
                padding-right: 0;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: pre;
                border: 0;
                background: #F5F6F7;
              }
            }
          }
        }
      }
      .el-table__fixed,
      .el-table__fixed-right {
        margin-top: 0;
      }
      .el-table.el-table--border {
        .el-table__body-wrapper {
          .el-table__empty-block {
            border-top: 0;
            border-left: 0;
          }
        }
      }
    }
    .table-body-box{
      padding: 0;
      .el-table__body-wrapper {
        height: auto !important;
        tr td{
        .cell.el-tooltip{
            >span{
              display: inline-block;
              width: 100%;
            }
          }
        }
      }
      .el-table__body-wrapper::-webkit-scrollbar {
        height: 8px;
      }
      .el-table__body-wrapper::-webkit-scrollbar-thumb {
        border-radius: 20px;
      }
      .el-table__fixed-right {
        padding-bottom: 4px;
      }
    }
    .el-table__row {
      .el-upload-list {
        .el-upload-list__item {
          .el-icon-close-tip {
            display: none !important;
          }
        }
      }
    }
    &.empty-data{
      .table-body-box{
       .el-table__body-wrapper{
         min-height: 300px;
       }
      }
    }
  }
  .table-form-noteditable {
    padding: 10px;
    .jvs-table {
      .jvs-table-titleTop {
        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
      }
      .table-body-box {
        padding: 0;
      }
    }
  }
  .show-form {
    .table-form {
      .table-body-box {
        .el-table__body-wrapper {
          .el-table__empty-block {
            display: none !important;
          }
        }
      }
    }
  }
  .user-info-list{
    .user-info-input-div{
      height: $jvsFormItemHeight;
    }
  }
  .w-e-text-container{
    table{
      td{
        *{
          margin: 0;
          padding: 0;
        }
      }
    }
  }
  .form-page-table{
    overflow: hidden;
    overflow-y: auto;
    .jvs-table-opend-by-form{
      p{
        display: flex;
        align-items: center;
        flex-wrap: nowrap;
        span{
          word-break: keep-all;
        }
        .el-popover__reference{
          display: flex;
          align-items: center;
          flex-wrap: nowrap;
        }
      }
      .table-body-box{
        .el-table__body-wrapper{
          height: auto!important;
        }
      }
    }
  }
  .jvs-signature-item{
    width: 100%;
		.signature-content{
			width: 100%;
			box-sizing: border-box;
			height: 200px;
			overflow: hidden;
		}
	}
  // 蓝牙信标列表
  .blue-tool-beacon-list{
    display: flex;
    flex-wrap: wrap;
    max-height: 300px;
    overflow: hidden;
    overflow-y: auto;
    .blue-tool-beacon-list-item{
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      height: 36px;
      background: #F5F6F7;
      border-radius: 4px;
      padding: 0 8px;
      .name{
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 16px;
        color: #363B4C;
      }
      .delete-beacon{
        color: #C2C5CF;
        font-size: 15px;
        margin-left: 12px;
        cursor: pointer;
      }
      &+.blue-tool-beacon-list-item{
        margin-left: 12px;
      }
    }
  }
  .no-blue-tool-beacon{
    position: relative;
    min-height: 160px;
    background-color: #fff;
    background-image: url('../../const/img/emptyImage.svg');
    background-repeat: no-repeat;
    background-position: center;
    background-size: 260px 123px;
    padding: 10px 0;
  }
  .no-blue-tool-beacon::after{
    content: '暂无数据';
    line-height: 30px;
    color: #909399;
    font-size: 12px;
    text-align: center;
    display: block;
    width: 100%;
    position: absolute;
    top: 148px;
  }
}
.jvs-form-item-disabled{
  .el-upload-list{
    .el-upload-list__item{
      .el-upload-list__item-status-label{
        display: none!important;;
      }
    }
    .el-upload-list__item:hover{
      .el-icon-close{
        display: none!important;
      }
    }
  }
  .w-e-toolbar{
    display: none;
  }
  .w-e-text-container{
    width: 100%;
    border: 0!important;
    height: auto!important;
  }
  .form-item-icon-selct{
    .el-input{
      display: none;
    }
  }
}
.search-form-item-table{
  .jvs-table{
    .search-form{
      padding: 0!important;
    }
    .table-top{
      padding: 0!important;
      padding-top: 10px!important;
    }
    .table-body-box{
      .el-table__body-wrapper{
        height: auto!important;
      }
    }
  }
  .tablepagination{
    .el-pagination__sizes{
      .el-input__suffix{
        .el-icon-circle-check{
          display: none;
        }
      }
    }
  }
  .search-form{
    .jvs-form-item{
      .el-input-number{
        .el-input-number__decrease, .el-input-number__increase{
          display: none;
        }
        &.input-number-unit .el-input{
          .el-input__inner{
            border-radius: 0;
          }
        }
      }
    }
  }
}
</style>
