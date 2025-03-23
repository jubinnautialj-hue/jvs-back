package cn.bctools.data.factory.query.value.enums;

import cn.bctools.data.factory.query.*;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @author xiaohui
 */

@Getter
@AllArgsConstructor
public enum QueryEnums {
    eq("eq", "等于", Eq.class),
    ne("ne", "不等于", NotEq.class),
    in("in", "等于任一", In.class),
    nin("nin", "不等于任一", NotIn.class),
    like("like", "包含", Like.class),
    NotLike("NotLike", "不包含", NotLike.class),
    likeIn("likeIn", "包含任一", LikeIn.class),
    dataSourceLikeIn("dataSourceLikeIn", "数据源包含任一", DataSourceLikeIn.class),
    NotLikeIn("NotLikeIn", "不包含任一", NotLikeIn.class),
    dataSourceNotLikeIn("dataSourceNotLikeIn", "数据源不包含任一", DataSourceNotLikeIn.class),
    gt("gt", "大于", Gt.class),
    ge("ge", "大于等于", Ge.class),
    lt("lt", "小于", Lt.class),
    le("le", "小于等于", Le.class),
    timeDynamic("timeDynamic", "动态范围", TimeDynamic.class),
    between("between", "介于 范围", Between.class),
    noBetween("noBetween", "不介于 范围外", NotBetween.class),
    notEmpty("notEmpty", "不为空", NotEmpty.class),
    empty("empty", "为空", Empty.class);

    @EnumValue
    String value;
    String desc;
    Class<? extends Query> cls;
}
