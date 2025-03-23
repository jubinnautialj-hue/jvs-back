package cn.bctools.data.factory.html.node.dto;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.FNodeType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Data
@Accessors(chain = true)
public class SupplierNodeData implements Serializable {
    List<DataSourceField> title;
    String documentName;
    /**
     * 节点类型
     */
    FNodeType type;
    ;
    Supplier<Stream<Map<String, Object>>> dataStream;
}