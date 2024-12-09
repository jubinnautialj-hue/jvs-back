package cn.bctools.rule.tools.mail;


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
@Accessors(chain = true)
@Data
public class MailFunctionDto {
    @ParameterValue(info = "选择接收人", explain = "支持选择系统用户人员或配置为邮件地址.注:选择人员需要配置邮件地址", type = InputType.userList)
    @NotEmpty(message = "选择接收人不能为空")
    public List<String> userIds;
    @NotNull(message = "标题不能为空")
    @ParameterValue(info = "邮件标题")
    public String title;
    @ParameterValue(info = "内容", type = InputType.html)
    @NotNull(message = "内容不能为空")
    public String content;
    @ParameterValue(info = "内容替换值", explain = "内容中如果存在变量,则自动替换,变量示例:${变量名}", type = InputType.map, necessity = false)
    public Map<String, String> text;
    @ParameterValue(info = "附件内容", explain = "参数名为文件名，值为 base64的文件数据或生成的文件，或文件的地址必须以 http开头", type = InputType.map, necessity = false)
    public Map<String, Object> files;

}
