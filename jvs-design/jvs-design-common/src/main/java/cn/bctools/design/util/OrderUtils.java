package cn.bctools.design.util;

import cn.bctools.design.common.OrderFormat;
import cn.bctools.design.common.OrderResetRuleEnum;
import cn.bctools.design.common.OrderTimeMarkEnum;
import cn.bctools.design.data.entity.DataIdPo;
import cn.bctools.function.utils.ExpressionUtils;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

import java.util.Objects;

/**
 * @author hs
 * 流水号工具
 */
public class OrderUtils {

    private OrderUtils() {
    }

    /**
     * 根据流水号格式得到前缀
     *
     * @param orderFormat 流水号格式
     * @param time        时间
     * @return 前缀
     */
    public static String parseOrderPrefix(OrderFormat orderFormat, DateTime time) {
        // 获取流水号前缀
        String prefix = ExpressionUtils.trimBlank(orderFormat.getOrderPrefix());
        // 获取时间标识
        OrderTimeMarkEnum timeMark = orderFormat.getOrderTimeMark();
        if (Objects.isNull(timeMark)) {
            timeMark = OrderTimeMarkEnum.n;
        }
        String dateFormat = DateUtil.format(time, timeMark.getFormat());
        if (Objects.nonNull(dateFormat)) {
            prefix += dateFormat;
        }
        return prefix;
    }

    /**
     * 得到完整的流水号
     *
     * @param prefix      前缀
     * @param orderFormat 流水号格式
     * @param nextId      序号
     * @return 流水号
     */
    public static String getOrderNumber(String prefix, OrderFormat orderFormat, DataIdPo nextId) {
        OrderResetRuleEnum resetRule = orderFormat.getOrderResetRule();
        if (Objects.isNull(resetRule)) {
            resetRule = OrderResetRuleEnum.n;
        }
        Integer id = resetRule.getIdGetter().apply(nextId);
        Integer orderDigit = orderFormat.getOrderDigit();
        if (Objects.isNull(orderDigit)) {
            orderDigit = 5;
        }
        // 例: %05d
        String suffix = ExpressionUtils.trimBlank(orderFormat.getOrderSuffix());
        return prefix + String.format(String.format("%%0%dd", orderDigit), id) + suffix;
    }

    /**
     * 得到完整的流水号
     *
     * @param orderFormat 流水号格式
     * @param nextId      序号
     * @param time        时间
     * @return 流水号
     */
    public static String getOrderNumber(OrderFormat orderFormat, DataIdPo nextId, DateTime time) {
        String prefix = parseOrderPrefix(orderFormat, time);
        return getOrderNumber(prefix, orderFormat, nextId);
    }
}
