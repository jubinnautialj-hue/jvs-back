package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.entity.enums.CrossBindingTypeEnum;
import cn.bctools.data.factory.html.NodeHtml;
import cn.bctools.data.factory.html.node.dto.ConnectionField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class CrossBindingParams extends NodeHtml<CrossBindingParams> {

    private CrossBindingObj crossBindingObj;

    @Data
    @Accessors(chain = true)
    public static class CrossBindingObj {
        /**
         * 合并方式
         */
        private CrossBindingTypeEnum connection;
        /**
         * 左侧数据节点id
         */
        private String leftNodeId;
        /**
         * 右侧数据id
         */
        private String rightNodeId;
        /**
         * 合并逻辑
         */
        private List<ConnectionField> connectionFields;
        /**
         * 是否合并字段
         */
        private Boolean isMerge;

    }
}
