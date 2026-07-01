package cn.bctools.rule.tools.alisms;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author My_gj
 */
@Data
@Accessors(chain = true)
public class SmsFunctionDto {

    @ParameterValue(info = "选择模板", type = InputType.selected, cls = AliSmsSelected.class, linkFields = "content", linkCls = AliSmsLinkSelected.class)
    @NotNull(message = "模板CODE不能为空")
    public String templateCode;

    @ParameterValue(info = "选择接收人", explain = "支持选择系统用户人员或配置为手机号。注:选择人员需要配置手机号", type = InputType.userList)
    @NotEmpty(message = "选择接收人不能为空")
    public List<String> userIds;

    @ParameterValue(info = "内容", type = InputType.dataModelField)
    @NotNull(message = "内容不能为空")
    public Map<String, String> content;

}
