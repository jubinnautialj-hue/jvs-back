package cn.bctools.rule.dto;


import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturepdfmobanchaxun dto.
 *
 * @author jvs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class GentlemanSignaturepdfmobanchaxunDto extends JunZiQianBaseDto {
    /**
     * The Current page.
     */
    @ParameterValue(info = "分页查询", necessity = false, defaultValue = "1", type = InputType.number)
    public Integer currentPage;

    /**
     * The Page size.
     */
    @ParameterValue(info = "指定每页查询多少条记录，1<=pageSize<=20", necessity = false, defaultValue = "20", type = InputType.number)
    public Integer pageSize;


}
