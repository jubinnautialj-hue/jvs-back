package cn.bctools.rule.ess.impl.createsigningprocessrelatedinterfaces;

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
 * @author jvs
 * The type Obtain the signing link to jump to tencents electronic signature mini program dto.
 */
@Data
@Accessors(chain = true)
public class ObtainTheSigningLinkToJumpToTencentsElectronicSignatureMiniProgramDto {

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
     * The Organization name.
     */
    @ParameterValue(info = "(OrganizationName)合同流程签署方的组织机构名称。如果名称中包含英文括号()，请使用中文括号（）代替。", necessity = false, type = InputType.input)

    /**
     * 合同流程签署方的组织机构名称。
     * 如果名称中包含英文括号()，请使用中文括号（）代替。
     */
    @SerializedName("OrganizationName")
    @Expose
    public String organizationName;
    /**
     * The Name.
     */
    @ParameterValue(info = "(Name)合同流程里边签署方经办人的姓名", necessity = false, type = InputType.input)

    /**
     * 合同流程里边签署方经办人的姓名。
     */
    @SerializedName("Name")
    @Expose
    public String name;
    /**
     * The Mobile.
     */
    @ParameterValue(info = "(Mobile)合同流程里边签署方经办人手机号码， 支持国内手机号11位数字(无需加+86前缀或其他字符)。", necessity = false, type = InputType.input)

    /**
     * 合同流程里边签署方经办人手机号码， 支持国内手机号11位数字(无需加+86前缀或其他字符)。
     */
    @SerializedName("Mobile")
    @Expose
    public String mobile;
    /**
     * The End point.
     */
    @ParameterValue(info = "(EndPoint)要跳转的链接类型" +
            "<ul><li> **HTTP**：跳转电子签小程序的http_url, 短信通知或者H5跳转适合此类型  ，此时返回长链 (默认类型)</li>\n" +
            "<li>**HTTP_SHORT_URL**：跳转电子签小程序的http_url, 短信通知或者H5跳转适合此类型，此时返回短链</li>\n" +
            "<li>**APP**： 第三方APP或小程序跳转电子签小程序的path,  APP或者小程序跳转适合此类型</li></ul>", necessity = false, type = InputType.input)

    /**
     * 要跳转的链接类型
     *
     * <ul><li> **HTTP**：跳转电子签小程序的http_url, 短信通知或者H5跳转适合此类型  ，此时返回长链 (默认类型)</li>
     * <li>**HTTP_SHORT_URL**：跳转电子签小程序的http_url, 短信通知或者H5跳转适合此类型，此时返回短链</li>
     * <li>**APP**： 第三方APP或小程序跳转电子签小程序的path,  APP或者小程序跳转适合此类型</li></ul>
     */
    @SerializedName("EndPoint")
    @Expose
    public String endPoint;
    /**
     * The Flow id.
     */
    @ParameterValue(info = "(FlowId)合同流程ID,注: `如果准备跳转到合同流程签署的详情页面(即PathType=1时)必传,   跳转其他页面可不传`", necessity = false, type = InputType.input)

    /**
     * 合同流程ID
     * 注: `如果准备跳转到合同流程签署的详情页面(即PathType=1时)必传,   跳转其他页面可不传`
     */
    @SerializedName("FlowId")
    @Expose
    public String flowId;
    /**
     * The Flow group id.
     */
    @ParameterValue(info = "(FlowGroupId)合同流程组的组ID, 在合同流程组场景下，生成合同流程组的签署链接时需要赋值", necessity = false, type = InputType.input)

    /**
     * 合同流程组的组ID, 在合同流程组场景下，生成合同流程组的签署链接时需要赋值
     */
    @SerializedName("FlowGroupId")
    @Expose
    public String flowGroupId;
    /**
     * The Path type.
     */
    @ParameterValue(info = "(PathType)要跳转到的页面类型" +
            "<ul><li> **0** : 腾讯电子签小程序个人首页 (默认)</li>\n" +
            "<li> **1** : 腾讯电子签小程序流程合同的详情页 (即合同签署页面)</li>\n" +
            "<li> **2** : 腾讯电子签小程序合同列表页</li><li> **3** : 腾讯电子签小程序合同封面页\n" +
            "注：`生成动态签署人补充链接时，必须指定为封面页`</li></ul>", necessity = false, type = InputType.number)

    /**
     * 要跳转到的页面类型
     *
     * <ul><li> **0** : 腾讯电子签小程序个人首页 (默认)</li>
     * <li> **1** : 腾讯电子签小程序流程合同的详情页 (即合同签署页面)</li>
     * <li> **2** : 腾讯电子签小程序合同列表页</li><li> **3** : 腾讯电子签小程序合同封面页
     * 注：`生成动态签署人补充链接时，必须指定为封面页`</li></ul>
     */
    @SerializedName("PathType")
    @Expose
    public Long pathType;
    /**
     * The Auto jump back.
     */
    @ParameterValue(info = "(AutoJumpBack)签署完成后是否自动回跳\n" +
            "<ul><li>**false**：否, 签署完成不会自动跳转回来(默认)</li><li>**true**：是, 签署完成会自动跳转回来</li></ul>\n" +
            "注:  ` 该参数只针对\"APP\" 类型的签署链接有效`", type = InputType.onOff)

    /**
     * 签署完成后是否自动回跳
     * <ul><li>**false**：否, 签署完成不会自动跳转回来(默认)</li><li>**true**：是, 签署完成会自动跳转回来</li></ul>
     * 注:  ` 该参数只针对"APP" 类型的签署链接有效`
     */
    @SerializedName("AutoJumpBack")
    @Expose
    public Boolean autoJumpBack;
    /**
     * The Hides.
     */
    @ParameterValue(info = "(Hides)生成的签署链接在签署页面隐藏的按钮列表，可设置如下：\n" +
            "<ul><li> **0** :合同签署页面更多操作按钮</li>\n" +
            "<li> **1** :合同签署页面更多操作的拒绝签署按钮</li>\n" +
            "<li> **2** :合同签署页面更多操作的转他人处理按钮</li>\n" +
            "<li> **3** :签署成功页的查看详情按钮</li></ul>\n" +
            "<p>\n" +
            "注:  `字段为数组, 可以传值隐藏多个按钮`", type = InputType.list)


    /**
     * 生成的签署链接在签署页面隐藏的按钮列表，可设置如下：
     *
     * <ul><li> **0** :合同签署页面更多操作按钮</li>
     * <li> **1** :合同签署页面更多操作的拒绝签署按钮</li>
     * <li> **2** :合同签署页面更多操作的转他人处理按钮</li>
     * <li> **3** :签署成功页的查看详情按钮</li></ul>
     * <p>
     * 注:  `字段为数组, 可以传值隐藏多个按钮`
     */
    @SerializedName("Hides")
    @Expose
    public List<Long> hides;
    /**
     * The Recipient id.
     */
    @ParameterValue(info = "(RecipientId)签署节点ID，用于生成动态签署人链接完成领取。`注：`生成动态签署人补充链接时必传。`", necessity = false, type = InputType.input)

    /**
     * 签署节点ID，用于生成动态签署人链接完成领取。
     * <p>
     * 注：`生成动态签署人补充链接时必传。`
     */
    @SerializedName("RecipientId")
    @Expose
    public String recipientId;

}
