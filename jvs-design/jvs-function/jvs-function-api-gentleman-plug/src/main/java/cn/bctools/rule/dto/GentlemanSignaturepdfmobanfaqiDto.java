package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import com.alibaba.fastjson2.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturepdfmobanfaqi dto.
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturepdfmobanfaqiDto extends JunZiQianBaseDto {
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
     * The File type.
     */
    @ParameterValue(info = "合同上传方式:2tmpl模版生成;", defaultValue = "0", type = InputType.number)
    public Integer fileType;

    /**
     * The Add page.
     */
    @ParameterValue(info = "ofd文件追加内容（0不能追加内容，1允许追加内容），允许追加内容时noEbqSign需要设置为2", necessity = false, defaultValue = "0", type = InputType.number)
    public Integer addPage;

    /**
     * The Template no.
     */
    @ParameterValue(info = "合同模版编号,dealType!=6,fileType=2,时必须传入", necessity = false, type = InputType.input)
    public String templateNo;

    /**
     * The Template params.
     */
    @ParameterValue(info = "合同模版参数JSON字符串,dealType!=6,fileType=2,时必须传入", necessity = false, type = InputType.json)
    public String templateParams;

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
    @ParameterValue(info = "人脸难易度0简单模式,1正常模式,2困难模式,3地狱模式，默认不传，使用2困难模式", necessity = false, defaultValue = "2", type = InputType.input)
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


}
