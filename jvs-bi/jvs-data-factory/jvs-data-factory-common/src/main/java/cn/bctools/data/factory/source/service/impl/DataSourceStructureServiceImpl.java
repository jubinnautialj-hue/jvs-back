package cn.bctools.data.factory.source.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.source.data.po.JsonAnalysisPo;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.mapper.DataSourceStructureMapper;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 数据源配置信息
 */
@Service
public class DataSourceStructureServiceImpl extends ServiceImpl<DataSourceStructureMapper, DataSourceStructure> implements DataSourceStructureService {

    @Override
    public void saveOrUpdateStructure(DataSourceStructure datasourceStructure) {
        //获取绑定的key
        String dataKey = datasourceStructure.getSettingJson().getDataKey();
        List<JsonAnalysisPo> carveBuiltJson = datasourceStructure.getJsonAnalysis();
        if (StrUtil.isNotEmpty(dataKey)) {
            String[] split = dataKey.split("\\.");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                JsonAnalysisPo analysisPo = carveBuiltJson.stream().filter(e -> e.getColumnName().equals(s)).findFirst().orElseThrow(() -> new BusinessException("未找到计算key"));
                carveBuiltJson = analysisPo.getItems();
            }
        }
        //获取保存的数据
        //数据结构
        List<DataSourceStructure.Structure> structure;

        DataSourceStructure byId = this.getById(datasourceStructure.getId());
        List<DataSourceStructure.Structure> oldStructure = Optional.ofNullable(byId).map(DataSourceStructure::getStructure).orElse(Collections.emptyList());
        Map<String, String> keyMap = oldStructure.stream().collect(Collectors.toMap(e -> e.getColumnCount() + e.getDataFieldTypeEnum().name(), DataSourceStructure.Structure::getColumnName));
        //判断是否存在勾选
        structure = getStructure(carveBuiltJson, Boolean.TRUE, keyMap);
        datasourceStructure.setStructure(structure)
                .setCheckIs(Boolean.TRUE);
        this.saveOrUpdate(datasourceStructure);
    }

    public List<DataSourceStructure.Structure> getStructure(List<JsonAnalysisPo> list, Boolean isFirst, Map<String, String> keyMap) {
        return list.stream().map(e -> {
            DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                    .setColumnName(e.getColumnName())
                    .setOriginalColumnName(e.getColumnName())
                    .setColumnCount(e.getColumnCount())
                    .setDataFieldTypeEnum(e.getDataFieldTypeEnum());
            if (isFirst) {
                //如果类型 key不一致 才重新设置columnName
                String key = e.getColumnCount() + e.getDataFieldTypeEnum().name();
                structure.setColumnName(keyMap.getOrDefault(key, IdGenerator.getIdStr()));
            }
            if (ObjectUtil.isNotNull(e.getItems()) && !e.getItems().isEmpty()) {
                List<DataSourceStructure.Structure> structureList = getStructure(e.getItems(), Boolean.FALSE, keyMap);
                structure.setItems(structureList);
            }
            return structure;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DataSourceStructure.Structure> getDataSourceStructure(DataSource dataSource, DataSourceStructure datasourceStructure) {
        DataSourceExecuteInterface bean = (DataSourceExecuteInterface) SpringContextUtil.getApplicationContext().getBean(dataSource.getSourceType().value);
        return bean.getTableStructure(datasourceStructure);
    }


}
