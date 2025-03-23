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
public class FData {
    /**
     * 标题
     */
    List<DataSourceField> title;


    /**
     * 数据存储的数据库名称
     */
    String documentName;

    /**
     * 节点类型
     */
    FNodeType type;

}
