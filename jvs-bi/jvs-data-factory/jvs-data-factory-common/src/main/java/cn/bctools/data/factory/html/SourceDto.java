package cn.bctools.data.factory.html;

import cn.bctools.data.factory.dto.DataSourceField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 前端预览的数据
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
public class SourceDto {
    /**
     * 数据
     */
    List data;
    /**
     * 节点id
     */
    String nodeId;
    /**
     * 数据存储的表名称
     */
    String documentName;
    /**
     * 标题
     */
    List<DataSourceField> headers;
}
