package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * The type Gentleman signaturetianjiaweitianchongbiaodandepdfwenjian dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturetianjiaweitianchongbiaodandepdfwenjianDto extends JunZiQianBaseDto {
    /**
     * The Template name.
     */
    @ParameterValue(info = "模板名称 不超过50字符;当templateNo为空时必传", type = InputType.input)
    public String templateName;

    /**
     * The Template no.
     */
    @ParameterValue(info = "模版id （注：修改模板时传入）", necessity = false, type = InputType.input)
    public String templateNo;

    /**
     * The File.
     */
    @ParameterValue(info = "文件模板（支持PDF或word）,且以.pdf或.doc或.docx结尾", type = InputType.input)
    public MultipartFile file;

    /**
     * The Attach file.
     */
    @ParameterValue(info = "模板附件", necessity = false, type = InputType.input)
    public MultipartFile[] attachFile;


}
