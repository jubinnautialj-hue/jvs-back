package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.html.NodeHtml;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class OutPutParams extends NodeHtml<OutPutParams> {

    private OutPutObj outPutObj;

    @Data
    @ApiModel("输出时的入参")
    public static class OutPutObj {

        /**
         * 是否为追加
         */
        private Boolean isAdd;
        /**
         * 有效期 最小单位为天 如果用户没有设置就表示永久有效
         */
        private Integer validityDay;
    }

}
