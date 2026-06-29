package cn.bctools.auth.vo;

import cn.bctools.auth.api.dto.UserRangTypeDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs  The type User selected vo.
 */
@Data
@Accessors(chain = true)
public class UserSelectedVo {
    /**
     * 每页显示条数，默认 10
     */
    protected long size = 10;

    /**
     * 当前页
     */
    protected long current = 1;
    /**
     * The Rang type.
     */
    @ApiModelProperty("类型")
    UserRangTypeDto rangType;
    /**
     * The Rang ids.
     */
    @ApiModelProperty("数据值")
    List<String> rangIds;
    /**
     * The Key.
     */
    @ApiModelProperty("多纬度查询条件")
    String key;
    /**
     * The All.
     */
    @ApiModelProperty("false-只查询未删除的用户；true-查询所有用户(包括已删除的)")
    Boolean all;
    /**
     * The Dept id.
     */
    @ApiModelProperty("部门id")
    String deptId;
    /**
     * The Job id.
     */
    @ApiModelProperty("岗位id")
    String jobId;
}
