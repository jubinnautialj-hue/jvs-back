package cn.bctools.data.factory.source.dto;


import cn.bctools.data.factory.source.data.po.InParameterJsonPo;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
public class InParameterDto extends InParameterJsonPo {
    /**
     * 节点id
     */
    private String nodeId;
}
