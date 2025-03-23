package cn.bctools.report.render.functions;

import cn.hutool.core.util.NumberUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component("FAvg")
public class FAvg implements FStats{

    @Override
    public Object calculation(List<Object> values,int scale) {
        List<BigDecimal> collect = values.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(NumberUtil::isNumber)
                .map(BigDecimal::new)
                .collect(Collectors.toList());

        BigDecimal[] array = collect.toArray(new BigDecimal[]{});
        BigDecimal sum = NumberUtil.add(array);

        BigDecimal divide = sum.divide(new BigDecimal(collect.size()));
        if(scale<=0){
            return divide.longValue();
        }
        return divide.setScale(scale, RoundingMode.HALF_UP).floatValue();
    }
}
