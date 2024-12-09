package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.entity.enums.type.RuleFile;
import com.alibaba.fastjson2.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * The type Gentleman signatureapimobanshouquanmafaqi dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignatureapimobanshouquanmafaqiDto extends JunZiQianBaseDto {
    /**
     * The Contract name.
     */
    @ParameterValue(info = "合同名称非空，最长100个字符", type = InputType.input)
    public String contractName;

    /**
     * The Signatories.
     */
    @ParameterValue(info = "签约方，参考后面签约方说明", type = InputType.listMap)
    public JSONArray signatories;

    /**
     * The Server ca.
     */
    @ParameterValue(info = "是否需要服务端云证书：非1不需要(默认);1需要;建议需要，否则影响后续司法服务", defaultValue = "0", type = InputType.number)
    public Integer serverCa;

    /**
     * The Deal type.
     */
    @ParameterValue(info = "处理方式:为空或0时默认为手签合同(用户有感知);2只保全;5部份自动签;6HASH只保全;17收集信息批量签", defaultValue = "0", type = InputType.number)
    public Integer dealType;

    /**
     * The Hash value.
     */
    @ParameterValue(info = "dealType=6时必须传入,文件的sha512HexString值", necessity = false, type = InputType.number)
    public Integer hashValue;

    /**
     * The File suffix.
     */
    @ParameterValue(info = "0或null默认.pdf; 1 ofd;2 word文件（传ofd和word文件发起合同时该参数必传）", necessity = false, defaultValue = "0", type = InputType.number)
    public Integer fileSuffix;

    /**
     * The File type.
     */
    @ParameterValue(info = "合同上传方式:0或null直接上传PDF/ofd/word文件;1url地址下载;2 API模版(HTML源码);3 html文件上传；4 API模板(PDF文件)", defaultValue = "0", type = InputType.number)
    public Integer fileType;

    /**
     * The File.
     */
    @ParameterValue(info = "合同文件;请使用form表单上传文件dealType!=6,fileType=0或null,时必须传入", necessity = false, type = InputType.input)
    public RuleFile file;

    /**
     * The Add page.
     */
    @ParameterValue(info = "ofd文件追加内容（0不能追加内容，1允许追加内容），允许追加内容时noEbqSign需要设置为2", necessity = false, defaultValue = "0", type = InputType.number)
    public Integer addPage;

    /**
     * The Url.
     */
    @ParameterValue(info = "合同PDF文件的url地址（传规范的url地址）;dealType!=6,fileType=1,时必须传入", necessity = false, type = InputType.input)
    public String url;

    /**
     * The Template no.
     */
    @ParameterValue(info = "合同模版编号,fileType=2或4,时必须传入", necessity = false, type = InputType.input)
    public String templateNo;

    /**
     * The Template params.
     */
    @ParameterValue(info = "合同模版参数JSON字符串,fileType=2或4,时必须传入", necessity = false, type = InputType.json)
    public String templateParams;

    /**
     * The Html content.
     */
    @ParameterValue(info = "合同html源码,dealType!=6,fileType=3,时必须传入,utf8编码", necessity = false, type = InputType.input)
    public String htmlContent;

    /**
     * The Position type.
     */
    @ParameterValue(info = "指定公章位置类型:0或null使用签字座标位置或不指定签字位置;1表单域定位(表单域如果上传为pdf时,需pdf自行定义好表单域,html及url及tmpl等需定义好input标签);2关键字定义", necessity = false, defaultValue = "0", type = InputType.number)
    public Integer positionType;

    /**
     * The Face threshold.
     */
    @ParameterValue(info = "验证方式为人脸时必传,人脸识别阀值:默认等级(1-100之间整数)，建议大于72,验证方式在签约方中（signatories）设置", necessity = false, type = InputType.number)
    public Integer faceThreshold;

    /**
     * The Complexity.
     */
    @ParameterValue(info = "人脸难易度 , 1简单模式（不推荐使用）, 2正常模式, 3困难模式，默认不传，使用2正常模式", necessity = false, defaultValue = "2", type = InputType.input)
    public String complexity;

    /**
     * The Order flag.
     */
    @ParameterValue(info = "是否按顺序签字，非1为不按，1为按", necessity = false, defaultValue = "0", type = InputType.number)
    public Integer orderFlag;

    /**
     * The Sequence info.
     */
    @ParameterValue(info = "多合同顺序签约或批量签合同关联信息，参考后面表格说明", necessity = false, type = InputType.map)
    public Object sequenceInfo;

    /**
     * The Notify url.
     */
    @ParameterValue(info = "合同签署完成后异步通知地址", necessity = false, type = InputType.input)
    public String notifyUrl;

    /**
     * The No ebq sign.
     */
    @ParameterValue(info = "不显示ebq的保全章:1 不显示但会签名,2不显示也不签名;0或其它-显示", necessity = false, defaultValue = "0", type = InputType.number)
    public Integer noEbqSign;

    /**
     * The Attach files.
     */
    @ParameterValue(info = "合同附件,虽不限个数,但包括合同原文件,不能超过30MB,*SDK引用中的多文件同名情况的上传说明", necessity = false, type = InputType.files)
    public List<RuleFile> attachFiles;

    /**
     * The Need qifeng sign.
     */
    @ParameterValue(info = "是否使用骑缝章:1使用;其它不使用", necessity = false, defaultValue = "0", type = InputType.number)
    public Integer needQifengSign;

    /**
     * The No border sign.
     */
    @ParameterValue(info = "是否不显示个人标准章边框:1不显示,其它显示边框(默认)", necessity = false, defaultValue = "1", type = InputType.number)
    public Integer noBorderSign;


}
