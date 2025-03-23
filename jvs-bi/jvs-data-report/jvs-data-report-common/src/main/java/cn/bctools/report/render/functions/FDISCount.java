package cn.bctools.report.render.functions;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component("FDISCount")
public class FDISCount implements FStats{

    @Override
    public Object calculation(List<Object> values, int scale) {
        if(CollectionUtil.isEmpty(values)){
            return "";
        }
        long count = values.stream()
                .map(StrUtil::toString)
                .distinct().count();
        if(scale>0){
            BigDecimal bigDecimal = new BigDecimal(count);
            return bigDecimal.setScale(scale, RoundingMode.HALF_UP).floatValue();
        }
        return count;
    }
}
