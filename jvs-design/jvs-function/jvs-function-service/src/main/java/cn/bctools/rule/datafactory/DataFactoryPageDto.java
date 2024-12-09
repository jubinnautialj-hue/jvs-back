//package cn.bctools.rule.datafactory;
//
//import cn.bctools.rule.annotations.ParameterValue;
//import cn.bctools.rule.entity.enums.InputType;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.experimental.Accessors;
//
//import javax.validation.constraints.NotNull;
//
///**
// * @author guojing
// */
//@Data
//@Accessors(chain = true)
//@AllArgsConstructor
//@NoArgsConstructor
//public class DataFactoryPageDto {
//
//    @NotNull(message = "智仓ID不能为空")
//    @ParameterValue(info = "请选择智仓输出", type = InputType.selected, cls = DataFacrorySelected.class, linkFields = {"fields", "body"}, linkCls = DataFactoryLinkSelected.class)
//    public String dataId;
//
//    @ParameterValue(info = "页大小", type = InputType.number, defaultValue = "20")
//    public Integer size = 20;
//    /**
//     * 当前页
//     */
//    @ParameterValue(info = "页码数", type = InputType.number, defaultValue = "1")
//    public Integer current = 1;
//}
