package cn.bctools.design.data.source.impl.sql;

import cn.bctools.common.utils.ObjectNull;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 用以构建查询条件
 */
@Getter
public class DynamicQuery {
    @ApiModelProperty("条件")
    private DynamicCriteria criteria;
    @ApiModelProperty("指定排除字段")
    private List<String> exclude;
    @ApiModelProperty("指定查询字段")
    private List<String> include;
    @ApiModelProperty("所有字段")
    private List<String> columns;
    @ApiModelProperty("查询数量")
    private Long limit;
    @ApiModelProperty("偏移量")
    private Long offset;
    @ApiModelProperty("排序")
    private Sort sort;

    public DynamicQuery() {

    }

    public DynamicQuery(DynamicCriteria criteria) {
        this.criteria = criteria;

    }

    public DynamicQuery addCriteria(DynamicCriteria criteria) {
        Assert.notNull(criteria, "条件不能为空!");
        if (ObjectNull.isNull(this.criteria)) {
            this.criteria = criteria;
        } else {
            this.criteria.andOperator(criteria);
        }
        return this;
    }

    public DynamicQuery exclude(String... excludes) {
        if (excludes.length == 0) {
            return this;
        }
        this.exclude = Arrays.asList(excludes);
        return this;
    }

    public DynamicQuery include(String... includes) {
        if (includes.length == 0) {
            return this;
        }
        this.include = Arrays.asList(includes);
        return this;
    }

    public DynamicQuery columns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    public DynamicQuery limit(Long limit) {
        this.limit = limit;
        return this;
    }

    public DynamicQuery offset(Long offset) {
        this.offset = offset;
        return this;
    }

    public DynamicQuery sort(Sort sort) {
        this.sort = sort;
        return this;
    }
}
