package cn.bctools.rule.ess.impl.institutionalandorganizationalrelatedinterfaces;

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
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class QueryEnterpriseExtensionServiceAuthorizationInformationDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(ExtendServiceType)要查询的扩展服务类型。\n" +
            "默认为空，即查询当前支持的所有扩展服务信息。\n" +
            "若需查询单个扩展服务的开通情况，请传递相应的值，如下所示：\n" +
            "OPEN_SERVER_SIGN：企业静默签署\n" +
            "OVERSEA_SIGN：企业与港澳台居民签署合同\n" +
            "MOBILE_CHECK_APPROVER：使用手机号验证签署方身份\n" +
            "PAGING_SEAL：骑缝章\n" +
            "BATCH_SIGN：批量签署\n" +
            "AGE_LIMIT_EXPANSION：拓宽签署方年龄限制", necessity = false, type = InputType.input)
    @SerializedName("ExtendServiceType")
    @Expose
    public  String extendServiceType;
}
