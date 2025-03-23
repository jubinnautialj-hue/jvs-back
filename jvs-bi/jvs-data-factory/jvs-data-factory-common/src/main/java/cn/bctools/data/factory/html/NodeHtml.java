package cn.bctools.data.factory.html;

import cn.bctools.data.factory.dto.DataSourceField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class NodeHtml<T> {

    /**
     * 名称
     */
    String name;
    /**
     * 节点id
     */
    String id;
    /**
     * 设计id
     */
    String dataId;
    /**
     * 本次存储的数据表名称
     */
    String tableName;
    /**
     * 节点类型
     */
    FNodeType type;
    /**
     * 其它参数值  根据不同的参数值，在下级实现类中自行添加属性
     */
    T sourceData;
    /**
     * 数据字段集
     */
    List<DataSourceField> fieldList;

}
