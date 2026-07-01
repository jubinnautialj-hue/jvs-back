package cn.bctools.design.util;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.hutool.core.lang.Dict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jvs
 */
public class DynamicFieldAttributeUtils {
    /**
     * 获取表单的动态渲染字段属性值
     * [{label: '文本框', type: 'input', showFrom
     * : ['label', 'span']}]
     *
     * @return
     */
    public static List get() {
        List<Dict> list = new ArrayList();
        List<String> showFrom = Arrays.asList("label", "span", "defaultValue", "disabled", "rules");
        list.add(new Dict().set("type", DataFieldType.input.toString()).set("label", DataFieldType.input.getDesc()).set("showFrom", showFrom));
        list.add(new Dict().set("type", DataFieldType.textarea.toString()).set("label", DataFieldType.textarea.getDesc()).set("showFrom", showFrom));
        list.add(new Dict().set("type", DataFieldType.timePicker.toString()).set("label", DataFieldType.timePicker.getDesc()).set("showFrom", showFrom));
        {
            List<String> copys = BeanCopyUtil.copys(showFrom, String.class);
            copys.add("pickeroptions");
            list.add(new Dict().set("type", DataFieldType.timeSelect.toString()).set("label", DataFieldType.timeSelect.getDesc()).set("showFrom", copys));
        }
        {
            List<String> copys = BeanCopyUtil.copys(showFrom, String.class);
            copys.add("datatype");
            copys.add("option");
            copys.add("multiple");
            list.add(new Dict().set("type", DataFieldType.select.toString()).set("label", DataFieldType.select.getDesc()).set("showFrom", copys));
        }
        {
            List<String> copys = BeanCopyUtil.copys(showFrom, String.class);
            copys.add("max");
            copys.add("step");
            copys.add("unit");
            copys.add("precision");

            list.add(new Dict().set("type", DataFieldType.inputNumber.toString()).set("label", DataFieldType.inputNumber.getDesc()).set("showFrom", copys));
        }
        {
            List<String> copys = BeanCopyUtil.copys(showFrom, String.class);
            copys.add("datetype");
            list.add(new Dict().set("type", DataFieldType.datePicker.toString()).set("label", DataFieldType.datePicker.getDesc()).set("showFrom", copys));
        }
        {
            List<String> copys = BeanCopyUtil.copys(showFrom, String.class);
            copys.add("datatype");
            copys.add("option");
            list.add(new Dict().set("type", DataFieldType.radio.toString()).set("label", DataFieldType.radio.getDesc()).set("showFrom", copys));
            list.add(new Dict().set("type", DataFieldType.checkbox.toString()).set("label", DataFieldType.checkbox.getDesc()).set("showFrom", copys));
        }
        list.add(new Dict().set("type", DataFieldType.image.toString()).set("label", DataFieldType.image.getDesc()).set("showFrom", showFrom));
        list.add(new Dict().set("type", DataFieldType.signature.toString()).set("label", DataFieldType.signature.getDesc()).set("showFrom", showFrom));
        {
            List<String> copys = BeanCopyUtil.copys(showFrom, String.class);
            copys.add("limit");
            list.add(new Dict().set("type", DataFieldType.imageUpload.toString()).set("label", DataFieldType.imageUpload.getDesc()).set("showFrom", copys));
        }
        {
            List<String> copys = BeanCopyUtil.copys(showFrom, String.class);
            copys.add("limit");
            copys.add("fileType");
            copys.add("fileSize");
            list.add(new Dict().set("type", DataFieldType.fileUpload.toString()).set("label", DataFieldType.fileUpload.getDesc()).set("showFrom", copys));
        }
        return list;
    }
}
