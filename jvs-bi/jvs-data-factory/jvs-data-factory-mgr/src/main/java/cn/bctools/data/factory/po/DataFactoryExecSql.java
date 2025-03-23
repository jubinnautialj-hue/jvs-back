package cn.bctools.data.factory.po;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;

/**
 * 执行sql
 *
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@ApiOperation("执行sql")
@Data
public class DataFactoryExecSql extends Page {
    @ApiModelProperty("sql")
    private String sql;
    @ApiModelProperty("执行时间")
    private Long execTime;
    @ApiModelProperty("key值")
    private Set<String> key;
}
