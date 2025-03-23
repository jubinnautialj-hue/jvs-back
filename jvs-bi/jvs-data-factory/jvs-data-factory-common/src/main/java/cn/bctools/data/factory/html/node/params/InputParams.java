package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.enums.DataSyncPluginEnums;
import cn.bctools.data.factory.html.NodeHtml;
import cn.bctools.data.factory.query.QueryDto;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class InputParams extends NodeHtml<InputParams> {

    private FromSource fromSource;

    @Data
    @ApiModel("输入节点入参")
    public static class FromSource {
        /**
         * 表结构id
         */
        private String id;
        /**
         * 数据源类型
         */
        private String sourceType;
        /**
         * 需要查询的条数
         */
        private Long size;
        /**
         * 增量配置
         */
        private IncrementalSetting incrementalSetting;
        /**
         * 查询条件
         */
        private QueryDto queryDto;
        /**
         * 本次同步使用的插件
         */
        private DataSyncPluginEnums dataSyncPlugin;

    }

    @Data
    @ApiModel("增量配置")
    public static class IncrementalSetting {
        /**
         * 是否开启增量模式
         */
        private Boolean incrementalMode;
        /**
         * 增量key 源数据的字段名称
         */
        private String incrementalModeKey;
        /**
         * 字段名称
         */
        private String fieldKey;
        /**
         * 字段格式
         */
        private String format;
        /**
         * 并行度
         */
        private Integer parallelism;
        /**
         * 增量key 类型
         */
        private DataFieldTypeEnum incrementalModeKeyType;
    }
}
