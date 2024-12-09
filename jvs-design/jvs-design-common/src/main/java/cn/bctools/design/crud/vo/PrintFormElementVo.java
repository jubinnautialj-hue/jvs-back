package cn.bctools.design.crud.vo;

import cn.bctools.function.entity.vo.ElementVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zhuxiaokang
 * 表单打印模板设计返回的字段
 */
@Data
@ApiModel("表单打印模板设计返回的字段")
public class PrintFormElementVo {

    @ApiModelProperty("表格字段")
    private List<ElementVo> tableFields;
}
