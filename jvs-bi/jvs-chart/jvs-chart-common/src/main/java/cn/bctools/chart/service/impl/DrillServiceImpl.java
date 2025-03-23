package cn.bctools.chart.service.impl;

import cn.bctools.chart.chart.bo.DrillSetting;
import cn.bctools.chart.chart.bo.FieldsData;
import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.dto.DrillReturnDto;
import cn.bctools.chart.service.DrillService;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.dto.OrderField;
import cn.bctools.data.factory.enums.SortTypeEnums;
import cn.bctools.data.factory.query.Query;
import cn.bctools.data.factory.query.QueryExecDto;
import cn.bctools.data.factory.query.value.enums.QueryEnums;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author admin
 */
@Slf4j
@Service
public class DrillServiceImpl implements DrillService {
    @Override
    public DrillReturnDto drill(DrillSetting drillSetting, ChartDesignInParameter chartDesignInParameter, StringBuffer whereSql) {
        DrillReturnDto drillReturnDto = new DrillReturnDto();
        FieldsData sourceField = drillSetting.getSourceField();
        whereSql.append(" and (");
        QueryExecDto queryExecDto = new QueryExecDto()
                .setMethodValue(drillSetting.getValue() != null ? drillSetting.getValue().toString() : null)
                .setMethod(QueryEnums.eq)
                .setFieldKey(sourceField.getFieldKey())
                .setFieldType(sourceField.getFieldType())
                .setFormat(sourceField.getFormat());
        Query query = SpringContextUtil.getBean(queryExecDto.getMethod().getCls());
        List<Object> exec = query.exec(queryExecDto, whereSql);
        whereSql.append(")");
        drillReturnDto.setParameter(exec);
        //重新设置 维度 这里直接替换  第一个 维度
        List<FieldsData> xAxis = chartDesignInParameter.getXAxis();
        FieldsData targetField = drillSetting.getTargetField();
        xAxis.set(0, targetField);
        chartDesignInParameter.setXAxis(xAxis);
        //判断是否需要重置排序字段
        SortTypeEnums sort = targetField.getSort();
        if (sort != null) {
            OrderField orderField = new OrderField()
                    .setFieldKey(targetField.getFieldKey())
                    .setSortType(sort);
            drillReturnDto.setSort(orderField);
        }
        return drillReturnDto;
    }

}
