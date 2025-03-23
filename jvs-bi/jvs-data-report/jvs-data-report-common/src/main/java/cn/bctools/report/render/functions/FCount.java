package cn.bctools.report.render.functions;

import cn.hutool.core.collection.CollectionUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component("FCount")
public class FCount implements FStats{

    @Override
    public Object calculation(List<Object> values, int scale) {
        if(CollectionUtil.isEmpty(values)){
            return "";
        }
        if(scale>0){
            BigDecimal bigDecimal = new BigDecimal(values.size());
            return bigDecimal.setScale(scale, RoundingMode.HALF_UP).floatValue();
        }
        return values.size();
    }
}
