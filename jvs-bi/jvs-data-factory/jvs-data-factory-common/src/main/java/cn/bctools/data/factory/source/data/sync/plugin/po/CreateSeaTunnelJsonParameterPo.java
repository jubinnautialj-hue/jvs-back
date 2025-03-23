package cn.bctools.data.factory.source.data.sync.plugin.po;

import cn.bctools.data.factory.source.entity.DataSourceStructure;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * datax文件生成的参数
 *
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
public class CreateSeaTunnelJsonParameterPo {
    /**
     * 不同数据源的连接信息
     */
    private JSONObject customJson;
    /**
     * 限制的条数 如果不限制就为-1
     */
    private Long size;
    /**
     * 数据结构
     */
    private DataSourceStructure dataSourceStructure;
}
