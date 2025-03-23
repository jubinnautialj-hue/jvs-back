package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.entity.enums.FieldSetupSortEnum;
import cn.bctools.data.factory.html.NodeHtml;
import cn.bctools.data.factory.html.enums.RankRuleEnum;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class RankParams extends NodeHtml<RankParams> {

    private RankObj rankObj;

    @Data
    @Accessors(chain = true)
    @ApiModel("排名配置")
    public static class RankObj {
        /**
         * 分组字段
         */
        private List<String> groupKey;
        /**
         * 排名字段
         */
        private String rankKey;
        /**
         * 排序规则
         */
        private FieldSetupSortEnum sortType;
        /**
         * 排名规则
         */
        private RankRuleEnum rankRule;
    }

    public Boolean check() {
        if (StrUtil.isNotBlank(this.getRankObj().getRankKey()) && ObjectUtil.isNotNull(this.getRankObj().getRankRule())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
