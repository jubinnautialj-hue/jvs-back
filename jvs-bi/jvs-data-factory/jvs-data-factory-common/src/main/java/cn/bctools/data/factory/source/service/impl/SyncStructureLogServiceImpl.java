package cn.bctools.data.factory.source.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.entity.SyncStructureLog;
import cn.bctools.data.factory.source.enums.OperationStateEnums;
import cn.bctools.data.factory.source.enums.StateEnums;
import cn.bctools.data.factory.source.mapper.SyncStructureLogMapper;
import cn.bctools.data.factory.source.service.SyncStructureLogService;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 表结构同步日志
 */
@Service
@Slf4j
@AllArgsConstructor
public class SyncStructureLogServiceImpl extends ServiceImpl<SyncStructureLogMapper, SyncStructureLog> implements SyncStructureLogService {

    @Override
    public void comparisonAndSave(List<DataSourceStructure> oldStructure, List<DataSourceStructure> newStructure, String dataSourceId, UserDto userDto) {
        List<String> oldTableName = oldStructure.stream().map(DataSourceStructure::getExecuteName).collect(Collectors.toList());
        List<String> newTableName = newStructure.stream().map(DataSourceStructure::getExecuteName).collect(Collectors.toList());
        //获取删除的表
        List<SyncStructureLog.TableDetail> deleteTable = comparison(e -> !newTableName.contains(e.getExecuteName()),
                e -> new SyncStructureLog.TableDetail()
                        .setTableName(e.getName())
                        .setStateEnums(OperationStateEnums.DELETE),
                oldStructure);
        //新增的数据
        List<SyncStructureLog.TableDetail> addTable = comparison(e -> !oldTableName.contains(e.getExecuteName()),
                e -> new SyncStructureLog.TableDetail()
                        .setTableName(e.getName())
                        .setStateEnums(OperationStateEnums.ADD),
                newStructure);
        //修改的数据
        List<SyncStructureLog.TableDetail> updateTable = oldStructure.stream().filter(e -> newTableName.contains(e.getExecuteName()))
                .map(e -> {
                    SyncStructureLog.TableDetail tableDetail = new SyncStructureLog.TableDetail()
                            .setTableName(e.getName());
                    List<DataSourceStructure.Structure> newField = newStructure.stream()
                            .filter(v -> v.getExecuteName().equals(e.getExecuteName()))
                            .findFirst()
                            .orElseThrow(() -> new BusinessException("对比修改数据时，没有找到数据"))
                            .getStructure();
                    List<String> newFieldName = newField.stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
                    List<String> oldFieldName = e.getStructure().stream().map(DataSourceStructure.Structure::getOriginalColumnName).collect(Collectors.toList());
                    //新增的字段
                    List<SyncStructureLog.FieldComparison> addField = comparison(x -> !oldFieldName.contains(x.getOriginalColumnName()), x -> {
                        SyncStructureLog.FieldComparison fieldComparison = new SyncStructureLog.FieldComparison();
                        SyncStructureLog.FieldDetail fieldDetail = JSONObject.parseObject(JSONObject.toJSONString(x), SyncStructureLog.FieldDetail.class);
                        fieldDetail.setColumnName(x.getOriginalColumnName());
                        return fieldComparison.setNewFieldDetail(fieldDetail)
                                .setStateEnums(OperationStateEnums.ADD);
                    }, newField);
                    //删除
                    List<SyncStructureLog.FieldComparison> deleteField = comparison(x -> !newFieldName.contains(x.getOriginalColumnName()), x -> {
                        SyncStructureLog.FieldComparison fieldComparison = new SyncStructureLog.FieldComparison();
                        SyncStructureLog.FieldDetail fieldDetail = JSONObject.parseObject(JSONObject.toJSONString(x), SyncStructureLog.FieldDetail.class);
                        fieldDetail.setColumnName(x.getOriginalColumnName());
                        return fieldComparison.setOldFieldDetail(fieldDetail)
                                .setStateEnums(OperationStateEnums.DELETE);
                    }, e.getStructure());
                    //修改
                    List<SyncStructureLog.FieldComparison> updateField = e.getStructure().stream().filter(x -> newFieldName.contains(x.getOriginalColumnName()))
                            .filter(x -> {
                                DataSourceStructure.Structure structure = newField.stream().filter(v -> v.getOriginalColumnName().equals(x.getOriginalColumnName())).findFirst().orElseThrow(() -> new BusinessException("获取字段时，未找到"));
                                return !StrUtil.equals(structure.getDataType(), x.getDataType());
                            })
                            .map(x -> {
                                DataSourceStructure.Structure structure = newField.stream().filter(v -> v.getOriginalColumnName().equals(x.getOriginalColumnName())).findFirst().orElseThrow(() -> new BusinessException("获取字段时，未找到"));
                                SyncStructureLog.FieldComparison fieldComparison = new SyncStructureLog.FieldComparison();
                                SyncStructureLog.FieldDetail fieldDetailOld = JSONObject.parseObject(JSONObject.toJSONString(x), SyncStructureLog.FieldDetail.class);
                                fieldDetailOld.setColumnName(x.getOriginalColumnName());
                                SyncStructureLog.FieldDetail fieldDetailNew = JSONObject.parseObject(JSONObject.toJSONString(structure), SyncStructureLog.FieldDetail.class);
                                fieldDetailNew.setColumnName(structure.getOriginalColumnName());
                                return fieldComparison.setOldFieldDetail(fieldDetailOld)
                                        .setNewFieldDetail(fieldDetailNew)
                                        .setStateEnums(OperationStateEnums.UPDATE);
                            }).collect(Collectors.toList());
                    addField.addAll(deleteField);
                    addField.addAll(updateField);
                    if (addField.isEmpty()) {
                        return tableDetail.setStateEnums(OperationStateEnums.UNIFORMITY);
                    }
                    return tableDetail.setFieldComparisons(addField)
                            .setStateEnums(OperationStateEnums.UPDATE);
                }).collect(Collectors.toList());
        boolean alterationIs = updateTable.stream().anyMatch(e -> e.getStateEnums().equals(OperationStateEnums.UPDATE));
        addTable.addAll(updateTable);
        addTable.addAll(deleteTable);
        //入库
        SyncStructureLog syncStructureLog = new SyncStructureLog()
                .setState(StateEnums.succeed)
                .setAlterationIs(alterationIs)
                .setRemark(alterationIs ? "本次同步存在字段变更" : "--")
                .setTableDetail(addTable)
                .setDataSourceId(dataSourceId);
        //设置用户信息
        syncStructureLog.setCreateBy(userDto.getRealName());
        syncStructureLog.setCreateById(userDto.getId());
        this.save(syncStructureLog);
    }

    /**
     * 过滤与转换的抽象类
     *
     * @param list        数据
     * @param filter      过滤条件
     * @param mapFunction 转换function
     **/
    private static <T, R> List<T> comparison(Predicate<R> filter, Function<R, T> mapFunction, List<R> list) {
        return list.stream().filter(filter).map(mapFunction).collect(Collectors.toList());
    }
}
