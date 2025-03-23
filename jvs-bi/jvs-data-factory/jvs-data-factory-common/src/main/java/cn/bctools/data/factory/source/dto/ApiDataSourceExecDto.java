package cn.bctools.data.factory.source.dto;


import cn.bctools.data.factory.source.enums.RequestTypeEnums;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author admin
 */
@Data
@Accessors(chain = true)
public class ApiDataSourceExecDto {
    /**
     * 请求的url
     */
    private String url;
    /**
     * 数据源id
     */
    private String dataSourceId;
    /**
     * jar包地址
     */
    private String jarId;
    /**
     * 请求方式 get 或者 post
     */
    private String requestMethod;
    /**
     * 其他参数
     * 方便 协议包 内部需要做判断
     */
    private String otherJson;

    /**
     * 计算key
     */
    private String computeKey;
    /**
     * 认证信息
     */
    private String authKey;
    /**
     * 入参
     */
    private List<Parameter> inParameter;

    @Data
    @ApiModel("结构")
    @Accessors(chain = true)
    public static class Parameter {
        /**
         * 值
         */
        private Object value;
        /**
         * 默认值
         */
        private Object defaultValue;
        /**
         * key
         */
        private String key;
        /**
         * 参数类型
         * params路径参数例如 xxx?value=xxx, body body参数, header 请求头;
         */
        private RequestTypeEnums inParameterType;
    }
}
