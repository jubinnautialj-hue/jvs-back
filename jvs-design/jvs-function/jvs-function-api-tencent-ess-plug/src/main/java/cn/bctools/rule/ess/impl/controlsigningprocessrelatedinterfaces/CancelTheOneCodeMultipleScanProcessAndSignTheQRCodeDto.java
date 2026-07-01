package cn.bctools.rule.ess.impl.controlsigningprocessrelatedinterfaces;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import cn.bctools.rule.ess.impl.TencenCloudApiSelected;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class CancelTheOneCodeMultipleScanProcessAndSignTheQRCodeDto {

    @NotNull(message = "帐号配置不能为空")
    @ParameterValue(info = "帐号配置", type = InputType.selected, cls = TencenCloudApiSelected.class)
    public String options;

    @ParameterValue(info = "(operator)执行本接口操作的员工信息。", necessity = false, type = InputType.map)
    public Map operator;
    @ParameterValue(info = "(QrCodeId)二维码id。", necessity = false, type = InputType.input)

    /**
     * 二维码id
     */
    @SerializedName("QrCodeId")
    @Expose
    public String qrCodeId;
}
