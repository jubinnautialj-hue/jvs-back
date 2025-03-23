package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturechaxunweitianchongbianliangdewenjianmobanliebiao dto.
 *
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturechaxunweitianchongbianliangdewenjianmobanliebiaoDto extends JunZiQianBaseDto {
    /**
     * The Current page.
     */
    @ParameterValue(info = "页码", necessity = false, type = InputType.number)
    public Integer currentPage;

    /**
     * The Page size.
     */
    @ParameterValue(info = "每页条数", necessity = false, type = InputType.number)
    public Integer pageSize;

    /**
     * The Template no.
     */
    @ParameterValue(info = "模板编号", necessity = false, type = InputType.input)
    public String templateNo;


}
