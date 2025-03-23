package cn.bctools.data.factory.source.data.sync.plugin.po;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * datax文件生成的参数
 *
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
public class CreateDataXJsonParameterPo {
    /**
     * 不同数据源的连接信息
     */
    private JSONObject customJson;

    /**
     * 是否开启增量模式
     */
    private Boolean incrementalMode;
    /**
     * 增量key 源数据的字段名称
     */
    private String incrementalModeKey;
    /**
     * 增量key 类型
     */
    private DataFieldTypeEnum incrementalModeKeyType;
    /**
     * 最新的值
     */
    private Object incrementalModeKeyValue;

    /**
     * 字段信息
     */
    private List<DataSourceField> dataSourceFields;
    /**
     * 限制的条数 如果不限制就为-1
     */
    private Long size;
    /**
     * 数据结构
     */
    private DataSourceStructure dataSourceStructure;
}
