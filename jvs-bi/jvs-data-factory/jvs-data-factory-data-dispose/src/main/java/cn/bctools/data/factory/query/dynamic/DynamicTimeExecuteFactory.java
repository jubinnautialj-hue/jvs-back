package cn.bctools.data.factory.query.dynamic;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeDto;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 动态时间的工厂类
 *
 * @author xiaohui
 */
@Component
@Slf4j
public class DynamicTimeExecuteFactory {
    public DynamicTimeValue execute(DynamicTimeDto dynamicTimeDto) {
        return SpringContextUtil.getBean(dynamicTimeDto.getType().getCls())
                .getValue(dynamicTimeDto);

    }
}
