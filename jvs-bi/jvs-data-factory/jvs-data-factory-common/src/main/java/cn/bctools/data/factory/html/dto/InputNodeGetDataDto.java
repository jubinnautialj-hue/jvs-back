package cn.bctools.data.factory.html.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 输入节点获取数据返回值
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
public class InputNodeGetDataDto {
    /**
     * 本次获取的总条数
     */
    private Long total;
    /**
     * 返回值
     */
    private List<Map<String, Object>> data;
    /**
     * 是否完成 如果已经完成就表示没有数据了
     */
    private Boolean isAccomplish;
}
