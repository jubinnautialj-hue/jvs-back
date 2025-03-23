package cn.bctools.data.factory.service.impl;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.dto.ColumnWhereDto;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.entity.JvsDataFactoryOut;
import cn.bctools.data.factory.mapper.JvsDataFactoryOutMapper;
import cn.bctools.data.factory.service.JvsDataFactoryOutService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.message.push.api.InsideNotificationApi;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 数据工厂输出模型字段 服务实现类
 * </p>
 *
 * @author 作者
 * @since 2022-08-23
 */
@Service
@AllArgsConstructor
public class JvsDataFactoryOutServiceImpl extends ServiceImpl<JvsDataFactoryOutMapper, JvsDataFactoryOut> implements JvsDataFactoryOutService {
    /**
     * 保存数据到Mongo中
     */
    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final InsideNotificationApi insideNotificationApi;


    @Override
    public Page<Map<String, Object>> getData(String dataFactoryId, long size, long current) {
        //这里应该有个排序 然后获取最新的数据
        JvsDataFactoryOut factoryOut = this.getOne(new LambdaQueryWrapper<JvsDataFactoryOut>().eq(JvsDataFactoryOut::getDataId, dataFactoryId).orderByDesc(JvsDataFactoryOut::getCreateTime).last("limit 1"));
        Page<Map<String, Object>> mapPage = new Page<>(current, size);
        mapPage.setTotal(0).setRecords(new ArrayList<>());
        if (ObjectUtil.isNull(factoryOut)) {
            log.warn("api查询输出结果为空!");
            return mapPage;
        }
        //权限过滤
        JvsDataFactoryService jvsDataFactoryService = SpringContextUtil.getBean(JvsDataFactoryService.class);
        List<DataSourceField> column = jvsDataFactoryService.getColumn(dataFactoryId);
        if (column.isEmpty()) {
            return mapPage;
        }
        ColumnWhereDto rowWhere = jvsDataFactoryService.getRowWhere(dataFactoryId);
        List<Map<String, Object>> maps;
        long total = dorisJdbcTemplate.getCount(factoryOut.getDocumentName(), rowWhere.getWhereStr(), rowWhere.getInParameter());
        mapPage.setTotal(total);
        //分页查询
        if (size > 0L) {
            maps = dorisJdbcTemplate.page(size, current, factoryOut.getDocumentName(), column, rowWhere.getWhereStr(), rowWhere.getInParameter());
        } else {
            maps = dorisJdbcTemplate.getData(factoryOut.getDocumentName(), column, rowWhere.getWhereStr(), rowWhere.getInParameter());
        }
        mapPage.setRecords(maps);
        return mapPage;
    }

    @Override
    public Page<Map<String, Object>> getData(String dataFactoryId, long size, long current, ConsanguinityAnalyse consanguinityAnalyse) {
        Page<Map<String, Object>> mapPage = this.getData(dataFactoryId, size, current);
        return mapPage;
    }
}
