package cn.bctools.data.factory.html.fill.impl;

import cn.bctools.data.factory.html.fill.DataFillService;
import cn.bctools.data.factory.html.node.params.DataFillParams;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数字类型
 * 3*365*7
 *
 * @author Administrator
 */
@Component
public class DataFillEnumServiceImpl implements DataFillService {
    @Override
    public void dataGenerate(DataFillParams.DataFillObj dataFillObj) {
        List<Object> enumsValue = dataFillObj.getEnumsValue()
                .stream().map(e -> (Object) e).collect(Collectors.toList());
        this.insertValue(dataFillObj, enumsValue);
    }
}
