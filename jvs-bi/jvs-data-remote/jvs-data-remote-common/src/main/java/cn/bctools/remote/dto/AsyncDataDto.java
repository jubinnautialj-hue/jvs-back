package cn.bctools.remote.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class AsyncDataDto {

    /**
     * 执行批次号
     */
    private String executeNo;

    /**
     * 数据
     */
    private List<Map<String, Object>> data;
}
