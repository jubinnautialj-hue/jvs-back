package cn.bctools.rule.ess.impl.createsigningprocessrelatedinterfaces;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.ess.impl.TencenCloudApiSelected;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * The type Create template signing process dto.
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class CreateTemplateSigningProcessDto {
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
     * The Flow name.
     */
    @ParameterValue(info = "(FlowName)合同流程的名称（可自定义此名称），长度不能超过200，只能由中文、字母、数字和下划线组成。\n" +
            "\n" +
            "该名称还将用于合同签署完成后的下载文件名。\n" +
            "示例值：张三的入职合同", necessity = false, type = InputType.input)
    public String flowName;

    /**
     * The Approvers.
     */
    @ParameterValue(info = "(Approvers)合同流程的参与方列表，最多可支持50个参与方，可在列表中指定企业B端签署方和个人C端签署方的联系和认证方式等信息，具体定义可以参考开发者中心的ApproverInfo结构体。", type = InputType.list)
    public List approvers;

    /**
     * The Flow description.
     */
    @ParameterValue(info = "(FlowDescription)合同流程描述信息(可自定义此描述)，最大长度1000个字符。", necessity = false, type = InputType.input)
    public String flowDescription;
    /**
     * The Flow type.
     */
    @ParameterValue(info = "(FlowType)合同流程的类别分类（可自定义名称，如销售合同/入职合同等），最大长度为200个字符，仅限中文、字母、数字和下划线组成。", necessity = false, type = InputType.input)
    public String flowType;
    /**
     * The Dead line.
     */
    @ParameterValue(info = "(DeadLine)合同流程的签署截止时间，格式为Unix标准时间戳（秒），如果未设置签署截止时间，则默认为合同流程创建后的365天时截止。\n" +
            "如果在签署截止时间前未完成签署，则合同状态会变为已过期，导致合同作废。", necessity = false, type = InputType.number)
    public Long deadLine;
    /**
     * The Reminded on.
     */
    @ParameterValue(info = "(RemindedOn)合同到期提醒时间，为Unix标准时间戳（秒）格式，支持的范围是从发起时间开始到后10年内。\n" +
            "\n" +
            "到达提醒时间后，腾讯电子签会短信通知发起方企业合同提醒，可用于处理合同到期事务，如合同续签等事宜。", necessity = false, type = InputType.number)
    public Long remindedOn;
    /**
     * The User data.
     */
    @ParameterValue(info = "(UserData)调用方自定义的个性化字段(可自定义此名称)，并以base64方式编码，支持的最大数据大小为 20480长度。", necessity = false, type = InputType.number)
    public String userData;
    /**
     * The Unordered.
     */
    @ParameterValue(info = "(Unordered)合同流程的签署顺序类型：\n" +
            "false：(默认)有序签署, 本合同多个参与人需要依次签署\n" +
            "true：无序签署, 本合同多个参与人没有先后签署限制", necessity = false, type = InputType.onOff)
    public Boolean unordered;
    /**
     * The Custom show map.
     */
    @ParameterValue(info = "(CustomShowMap)您可以自定义腾讯电子签小程序合同列表页展示的合同内容模板，模板中支持以下变量：\n" +
            "{合同名称}\n" +
            "{发起方企业}\n" +
            "{发起方姓名}\n" +
            "{签署方N企业}\n" +
            "{签署方N姓名}\n" +
            "\n" +
            "其中，N表示签署方的编号，从1开始，不能超过签署人的数量。", necessity = false, type = InputType.input)
    public String customShowMap;
    /**
     * The Need sign review.
     */
    @ParameterValue(info = "(NeedSignReview)发起方企业的签署人进行签署操作前，是否需要企业内部走审批流程，取值如下：\n" +
            "false：（默认）不需要审批，直接签署。\n" +
            "true：需要走审批流程。当到对应参与人签署时，会阻塞其签署操作，等待企业内部审批完成。", necessity = false, type = InputType.onOff)
    public Boolean needSignReview;

    /**
     * The Cc infos.
     */
    @ParameterValue(info = "(CcInfos)合同流程的抄送人列表，最多可支持50个抄送人，抄送人可查看合同内容及签署进度，但无需参与合同签署。", type = InputType.list)
    public List ccInfos;
    /**
     * The Auto sign scene.
     */
    @ParameterValue(info = "(AutoSignScene)个人自动签名的使用场景包括以下, 个人自动签署(即ApproverType设置成个人自动签署时)业务此值必传：\n" +
            "E_PRESCRIPTION_AUTO_SIGN：处方单（医疗自动签）" +
            "注: 个人自动签名场景是白名单功能，使用前请与对接的客户经理联系沟通。\n" +
            "示例值：E_PRESCRIPTION_AUTO_SIGN", necessity = false, type = InputType.input)
    public String autoSignScene;

}
