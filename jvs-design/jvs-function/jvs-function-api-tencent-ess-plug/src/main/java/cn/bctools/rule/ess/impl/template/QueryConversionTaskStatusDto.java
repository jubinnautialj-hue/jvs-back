package cn.bctools.rule.ess.impl.template;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.ess.impl.TencenCloudApiSelected;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author jvs
 * The type Query conversion task status dto.
 */
@Data
@Accessors(chain = true)
public class QueryConversionTaskStatusDto {


    /**
     * The Options.
     */
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    /**
     * The Operator.
     */
    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;
    /**
     * The Task id.
     */
    @ParameterValue(info = "(TaskId)转换任务Id，通过接口<a href=\"https://qian.tencent.com/developers/companyApis/templatesAndFiles/CreateConvertTaskApi\" target=\"_blank\">创建文件转换任务接口</a>或<a href=\"https://qian.tencent" +
            ".com/developers/companyApis/templatesAndFiles/CreateMergeFileTask\" target=\"_blank\">创建多文件转换任务接口</a>\n" +
            "     得到的转换任务id", necessity = false, type = InputType.input)

    /**
     * 转换任务Id，通过接口<a href="https://qian.tencent.com/developers/companyApis/templatesAndFiles/CreateConvertTaskApi" target="_blank">创建文件转换任务接口</a>或<a href="https://qian.tencent.com/developers/companyApis/templatesAndFiles/CreateMergeFileTask" target="_blank">创建多文件转换任务接口</a>
     得到的转换任务id
     */
    @SerializedName("TaskId")
    @Expose
    public String taskId;
}
