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
 * The type Gentleman signaturezhuijiaqianshufang dto.
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturezhuijiaqianshufangDto extends JunZiQianBaseDto {
    /**
     * The Apply no.
     */
    @ParameterValue(info = "合同编号（合同发起接口中生成的APL开头的编号）", type = InputType.input)
    public String applyNo;

    /**
     * The Signatories.
     */
    @ParameterValue(info = "追加签署人信息，参数说明请参考签约发起接口中的signatories字段说明;isArchive=0时必传，isArchive=1时非必传", necessity = false, type = InputType.listMap)
    public JSONArray signatories;

    /**
     * The Is archive.
     */
    @ParameterValue(info = "是否归档；0不归档，1归档", type = InputType.number)
    public Integer isArchive;

    /**
     * The Html content.
     */
    @ParameterValue(info = "如果使用表单域+html，需要对原html重新上传，注：需要在HTML源码中加上<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">", necessity = false, type = InputType.input)
    public String htmlContent;

    /**
     * The Position type.
     */
    @ParameterValue(info = "指定公章位置类型:0或null使用签字座标位置或不指定签字位置;1表单域定位(表单域如果上传为pdf时,需pdf自行定义好表单域,html及url及tmpl等需定义好input标签);2关键字定义", necessity = false, defaultValue = "0", type = InputType.number)
    public Integer positionType;


}
