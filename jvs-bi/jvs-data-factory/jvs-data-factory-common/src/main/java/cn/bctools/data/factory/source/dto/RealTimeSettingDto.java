package cn.bctools.data.factory.source.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 实时数据配置
 *
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class RealTimeSettingDto {
    /**
     * 平行度
     */
    private Integer parallelism;
    /**
     * 切片key 只能为 数字，字符串
     */
    private String splitKey;
}
