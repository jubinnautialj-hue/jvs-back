package cn.bctools.design.data.fields.dto;

import cn.bctools.design.data.fields.enums.DataQueryType;
import cn.bctools.design.data.fields.enums.DesignType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 数据字段
 *
 * @Author: GuoZi
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
@Accessors(chain = true)
public class FieldBasicsHtml extends FieldPublicHtml {

    @ApiModelProperty("枚举")
    private String encryptionExpress;

    @ApiModelProperty("是否开启正则")
    private Boolean encryption;

    @ApiModelProperty("数据模型id")
    private String modelId;

    //todo 在表格中的多选、下拉、单选 、级联组件配置了数据筛选和数据联动或间接公式触发的存在关联的字段全路径key集合 未实现
    /**
     * 为了优化表单的网络请求速度，将其把多选，下拉等接口的数据返回存在有筛选时，根据条件触发，如果不存在筛选时，默认触发
     */
    @ApiModelProperty("在表格中的多选、下拉、单选 、级联组件配置了数据筛选和数据联动或间接公式触发的存在关联的字段全路径key集合")
    private List<String> hasRelationPropList;


    @ApiModelProperty("是否可导出")
    private Boolean isExport;

    @ApiModelProperty("支持的查询类型")
    private List<DataQueryType> enabledQueryTypes;


    /**
     * 该字段仅用于前端显示
     */
    @ApiModelProperty("是否有索引")
    private Boolean indexBoolean = false;

    /**
     * 以下字段只在模板创建时使用
     */
    @ApiModelProperty("套件设计id")
    private String designId;
    @ApiModelProperty("套件设计类型(表单,列表页)")
    private DesignType designType;

    @ApiModelProperty("关联表单的设计id值")
    private String dataModelId;
    @ApiModelProperty("文本的前置拼接")
    private String append;
    @ApiModelProperty("文本的后置拼接")
    private String prepend;
    @ApiModelProperty("数字输入框的单位")
    private String unit;

    @Override
    public String getDataModelId() {
        //只有打开了配置后才能获取模型 id,否则获取为空
        if (this.getDataFilterEnable()) {
            return dataModelId;
        } else {
            return null;
        }
    }
}
