package cn.bctools.data.factory.source.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PrestoConnectDto extends PublicConnectDto{

    /**
     * 数据源
     */
    private String catalog;

    /**
     * 约等于数据库
     */
    private String schema;

}
