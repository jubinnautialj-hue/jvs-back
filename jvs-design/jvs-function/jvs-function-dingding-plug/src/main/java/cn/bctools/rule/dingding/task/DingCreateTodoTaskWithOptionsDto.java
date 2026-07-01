package cn.bctools.rule.dingding.task;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * The type Ding create
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class DingCreateTodoTaskWithOptionsDto {

    /**
     * The User.
     */
    @ParameterValue(info = "用户对象", necessity = false, type = InputType.user, explain = "获取一个用户,需要参数为用户id值")
    public String user;
    /**
     * The Subject.
     */
    @ParameterValue(info = "待办标题，最大长度1024。", explain = "待办标题，最大长度1024。", type = InputType.text)
    public String subject;
    /**
     * The Description.
     */
    @ParameterValue(info = "待办备注描述，最大长度4096。与url二选一", explain = "待办备注描述，最大长度4096。", necessity = false, type = InputType.input)
    public String description;
    /**
     * The Pc url.
     */
    @ParameterValue(info = "PC端详情页url跳转地址，与备注二选一", explain = "PC端详情页url跳转地址", necessity = false, type = InputType.input)
    public String pcUrl;


}
