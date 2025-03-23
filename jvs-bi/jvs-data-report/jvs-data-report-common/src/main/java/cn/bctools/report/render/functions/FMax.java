package cn.bctools.report.render.functions;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Component("FMax")
public class FMax implements FStats{

    @Override
    public Object calculation(List<Object> values, int scale) {
        if(CollectionUtil.isEmpty(values)){
            return "";
        }
        BigDecimal stats = values.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(NumberUtil::isNumber)
                .map(BigDecimal::new)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        if(scale<=0){
            return stats.longValue();
        }
        return stats.setScale(scale, RoundingMode.HALF_UP).floatValue();
    }
}
