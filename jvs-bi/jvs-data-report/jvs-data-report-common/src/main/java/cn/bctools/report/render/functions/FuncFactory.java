package cn.bctools.report.render.functions;

import cn.bctools.report.enums.EStatsType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FuncFactory {

    private final Map<String,? extends FStats> BEANS;

    /**
     * 计算
     * @param type 计算类型
     * @param args 参数
     * @param scale 精度
     * @return 值
     */
    public Object apply(EStatsType type, List<Object> args,int scale) {
        String name = type.getFClass().getSimpleName();
        return BEANS.get(name).calculation(args,scale);
    }
}
