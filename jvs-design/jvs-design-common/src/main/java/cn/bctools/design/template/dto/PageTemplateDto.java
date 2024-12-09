package cn.bctools.design.template.dto;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 列表页模板数据
 *
 * @Author: GuoZi
 */
@Data
public class PageTemplateDto {

    @ApiModelProperty("列表页id")
    private String id;
    @ApiModelProperty("列表名称")
    private String name;
    @ApiModelProperty("分类")
    private String type;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("设计数据")
    private String viewJson;
    @ApiModelProperty("图标")
    private String icon;
    @ApiModelProperty("数据模型ID")
    private String dataModelId;

    @ApiModelProperty("按钮对应的表单数据")
    private List<FormTemplateDto> formList;

}
