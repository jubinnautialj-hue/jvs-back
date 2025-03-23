package cn.bctools.design.workflow.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 审批意见
 */
@Data
@Accessors(chain = true)
@ApiModel("审批意见")
public class ApproveOpinionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "意见内容")
    private String content;

    /**
     * {@link cn.bctools.oss.dto.BaseFile}
     */
    @ApiModelProperty(value = "签名")
    private List<Map<String, Object>> sign;
}
