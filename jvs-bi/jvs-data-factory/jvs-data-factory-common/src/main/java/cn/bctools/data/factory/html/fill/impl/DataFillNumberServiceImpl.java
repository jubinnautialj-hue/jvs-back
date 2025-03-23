package cn.bctools.data.factory.html.fill.impl;

import cn.bctools.data.factory.html.fill.DataFillService;
import cn.bctools.data.factory.html.node.params.DataFillParams;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 数字类型
 *
 * @author Administrator
 */
@Component
public class DataFillNumberServiceImpl implements DataFillService {
    @Override
    public void dataGenerate(DataFillParams.DataFillObj dataFillObj) {
        long startValue = Long.parseLong(dataFillObj.getStartValue());
        long endValue = Long.parseLong(dataFillObj.getEndValue());
        List<Object> list = new ArrayList<>();
        while (startValue <= endValue) {
            list.add(startValue);
            if (list.size() == 1000) {
                this.insertValue(dataFillObj, list);
                list.clear();
            }
            startValue++;
        }
        if (!list.isEmpty()) {
            this.insertValue(dataFillObj, list);
        }

    }
}
