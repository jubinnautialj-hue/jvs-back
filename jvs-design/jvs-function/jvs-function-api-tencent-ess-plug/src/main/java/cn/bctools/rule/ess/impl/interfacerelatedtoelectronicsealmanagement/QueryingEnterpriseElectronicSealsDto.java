package cn.bctools.rule.ess.impl.interfacerelatedtoelectronicsealmanagement;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.ess.impl.TencenCloudApiSelected;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class QueryingEnterpriseElectronicSealsDto {
    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;

    @ParameterValue(info = "(Limit)指定分页每页返回的数据条数，如果不传默认为 20，单页最大支持 200。\n" +
            "示例值：10", necessity = false, type = InputType.map)
    @SerializedName("Limit")
    @Expose
    public  Long limit;

    @ParameterValue(info = "(Offset)指定分页返回第几页的数据，如果不传默认返回第一页，页码从 0 开始，即首页为 0，最大 20000。\n" +
            "示例值：0", necessity = false, type = InputType.number)
    @SerializedName("Offset")
    @Expose
    public  Long offset;

    @ParameterValue(info = "(InfoType)查询信息类型，取值如下：\n" +
            "\n" +
            "0不返回授权用户\n" +
            "1返回授权用户信息\n" +
            "\n" +
            "\n" +
            "示例值：1", necessity = false, type = InputType.number)
    @SerializedName("InfoType")
    @Expose
    public  Long infoType;

    @ParameterValue(info = "(SealId)印章id（没有输入返回所有）", necessity = false, type = InputType.input)
    @SerializedName("SealId")
    @Expose
    public  String sealId;

    @ParameterValue(info = "(SealTypes)印章类型列表（都是组织机构印章）。\n" +
            "为空时查询所有类型的印章。\n" +
            "目前支持以下类型：\n" +
            "\n" +
            "OFFICIAL：企业公章；\n" +
            "CONTRACT：合同专用章；\n" +
            "ORGANIZATION_SEAL：企业印章(图片上传创建)；\n" +
            "LEGAL_PERSON_SEAL：法定代表人章", necessity = false, type = InputType.list)
    @SerializedName("SealTypes")
    @Expose
    public  List<String> sealTypes;

    @ParameterValue(info = "(SealStatuses)查询的印章状态列表。\n" +
            "\n" +
            "空，只查询启用状态的印章；\n" +
            "ALL，查询所有状态的印章；\n" +
            "CHECKING，查询待审核的印章；\n" +
            "SUCCESS，查询启用状态的印章；\n" +
            "FAIL，查询印章审核拒绝的印章；\n" +
            "DISABLE，查询已停用的印章；\n" +
            "STOPPED，查询已终止的印章；\n" +
            "VOID，查询已作废的印章；\n" +
            "INVALID，查询已失效的印章；", necessity = false, type = InputType.list)
    @SerializedName("SealStatuses")
    @Expose
    public  List<String> sealStatuses;

}
