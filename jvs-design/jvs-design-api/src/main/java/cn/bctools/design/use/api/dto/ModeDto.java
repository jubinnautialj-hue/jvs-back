package cn.bctools.design.use.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模式信息,此接口只能内部使用，包含链接信息
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class ModeDto {

    /**
     * 模式
     */
    String mode;
    /**
     * 数据源链接信息，MongoProperties 对象的 json， 目前返回的是 mongo链接信息，后续可能会添加基它数据源；
     */
    String datasource;


}
