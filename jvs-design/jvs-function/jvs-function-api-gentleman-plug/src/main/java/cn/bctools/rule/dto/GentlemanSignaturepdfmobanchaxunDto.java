package cn.bctools.rule.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.experimental.Accessors;

/**
 * The type Gentleman signaturepdfmobanchaxun dto.
 *
 * @author jvs
 */
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
