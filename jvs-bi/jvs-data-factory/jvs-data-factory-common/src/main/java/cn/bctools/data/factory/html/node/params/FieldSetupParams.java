package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.enums.FieldSetupReplaceEnum;
import cn.bctools.data.factory.html.NodeHtml;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
public class FieldSetupParams extends NodeHtml<FieldSetupParams> {

    private List<FieldSetup> fieldObj;

    @Data
    @Accessors(chain = true)
    public static class FieldSetup extends DataSourceField {
        /**
         * 字段替换值
         */
        private List<FieldSetupReplace> replaceList;
        /**
         * 是否变更
         */
        private Boolean propertyIsChange;
        /**
         * 函数替换映射key
         */
        private List<String> keyFillingSequence;
        /**
         * 函数
         */
        private String functionStr;

    }

    @Data
    @Accessors(chain = true)
    public static class FieldSetupReplace {
        /**
         * 值替换
         */
        private FieldSetupReplaceEnum replaceEnum;

        /**
         * 替换的值
         */
        private String replaceValue;

        /**
         * 原来的值
         */
        private String replaceOriginalValue;
    }

}
