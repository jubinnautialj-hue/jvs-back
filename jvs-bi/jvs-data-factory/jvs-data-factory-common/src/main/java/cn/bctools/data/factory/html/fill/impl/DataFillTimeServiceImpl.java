package cn.bctools.data.factory.html.fill.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.html.enums.DataFillTypeEnum;
import cn.bctools.data.factory.html.fill.DataFillService;
import cn.bctools.data.factory.html.node.params.DataFillParams;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段类型为时间
 *
 * @author Administrator
 */
@Component
@AllArgsConstructor
public class DataFillTimeServiceImpl implements DataFillService {

    @Override
    public void dataGenerate(DataFillParams.DataFillObj dataFillObj) {
        //计算开始时间与结束时间
        DateTime startTime;
        DateTime endTime;
        //区分是否为动态时间
        if (dataFillObj.getType().equals(DataFillTypeEnum.time)) {
            //获取当前时间为基础时间
            endTime = DateUtil.date();
            int differenceValue = -dataFillObj.getDifferenceValue();
            DateField dateField = DateField.valueOf(dataFillObj.getSpanUnit());
            startTime = DateUtil.offset(endTime, dateField, differenceValue);
            //开始时间
            switch (dateField) {
                case MONTH:
                    startTime = DateUtil.beginOfMonth(startTime);
                    break;
                case YEAR:
                    startTime = DateUtil.beginOfYear(startTime);
                    break;
                case WEEK_OF_YEAR:
                    startTime = DateUtil.beginOfWeek(startTime);
                    break;
                default:
            }
        } else {
            startTime = DateUtil.parse(dataFillObj.getStartValue(), dataFillObj.getFormat());
            endTime = DateUtil.parse(dataFillObj.getEndValue(), dataFillObj.getFormat());
        }
        List<Object> list = new ArrayList<>();
        while (true) {
            list.add(DateUtil.format(startTime, dataFillObj.getFormat()));
            if (list.size() == 10000) {
                this.insertValue(dataFillObj, list);
                list.clear();
            }
            long differenceValue;
            //判断是否入库与结束
            switch (dataFillObj.getFieldType()) {
                case DATE:
                    startTime = DateUtil.offset(startTime, DateField.DAY_OF_YEAR, 1);
                    differenceValue = DateUtil.betweenDay(startTime, endTime, Boolean.TRUE);
                    break;
                case DATE_YEAR:
                    startTime = DateUtil.offset(startTime, DateField.YEAR, 1);
                    differenceValue = DateUtil.betweenYear(startTime, endTime, Boolean.TRUE);
                    break;
                case DATE_MONTH:
                    startTime = DateUtil.offset(startTime, DateField.MONTH, 1);
                    differenceValue = DateUtil.betweenMonth(startTime, endTime, Boolean.TRUE);
                    break;
                default:
                    throw new BusinessException("未知类型");
            }
            if (differenceValue == 0) {
                this.insertValue(dataFillObj, list);
                break;
            }
        }
    }
}
