package cn.bctools.data.factory.source.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.enums.DataFieldTypeClassifyEnum;
import cn.bctools.data.factory.enums.DataFieldTypeEnum;
import cn.bctools.data.factory.source.data.excel.ExcelExecuteImpl;
import cn.bctools.data.factory.source.data.po.ExcelReadDataPo;
import cn.bctools.data.factory.source.data.service.DataSourceExecuteInterface;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.entity.JvsSourceToDoris;
import cn.bctools.data.factory.source.entity.SyncStructureLog;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.enums.StateEnums;
import cn.bctools.data.factory.source.mapper.DataSourceMapper;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.source.service.JvsSourceToDorisService;
import cn.bctools.data.factory.source.service.SyncStructureLogService;
import cn.bctools.data.factory.source.util.RedisKey;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 数据源配置信息
 */
@Service
@Slf4j
public class DataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource> implements DataSourceService {

    private static final String KEYWORD_NAME = "\\$";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    DataSourceStructureService datasourceStructureService;
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    JvsSourceToDorisService jvsSourceToDorisService;
    @Autowired
    DataSourceMapper dataSourceMapper;
    @Autowired
    SyncStructureLogService syncStructureLogService;


    @Override
    public Long getCount(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        //判断是否需要获取数据的数据来源
        boolean equals = dataSource.getSourceType().equals(DataSourceTypeEnum.dataModel) || dataSource.getSourceType().equals(DataSourceTypeEnum.dataFactoryDataSource);
        if (!equals) {
            dataSource = this.getById(dataSource.getId());
            if (ObjectUtil.isNull(dataSource)) {
                return 0L;
            }
        }
        DataSourceExecuteInterface bean = (DataSourceExecuteInterface) SpringContextUtil.getApplicationContext().getBean(dataSource.getSourceType().value);
        return bean.getCount(dataSource, dataSourceStructure);
    }

    @Override
    public Page<Map<String, Object>> findAll(DataSource dataSource, DataSourceStructure dataSourceStructure, Long size, Long current) {
        Page<Map<String, Object>> objectPage = new Page<Map<String, Object>>().setTotal(0L).setRecords(new ArrayList<>());
        //默认值设置
        size = Optional.ofNullable(size).orElse(0L);
        current = Optional.ofNullable(current).orElse(1L);
        //判断是否需要获取数据的数据来源
        dataSource = this.getById(dataSource.getId());
        if (ObjectUtil.isNull(dataSource)) {
            return objectPage;
        }
        DataSourceExecuteInterface bean = (DataSourceExecuteInterface) SpringContextUtil.getApplicationContext().getBean(dataSource.getSourceType().value);
        objectPage = bean.findAll(dataSource, dataSourceStructure, size, current);
        //如果是excel数据源就没有别名映射的关系直接返回
        if (dataSource.getSourceType().equals(DataSourceTypeEnum.excelDataSource)) {
            return objectPage;
        }
        List<DataSourceStructure.Structure> structureList = Optional.ofNullable(dataSourceStructure.getStructure()).orElse(new ArrayList<>());
        //统一处理关键字信息
        List<Map<String, Object>> mapList = objectPage.getRecords().stream().map(e -> {
            Map<String, Object> map = new HashMap<>(e.size());
            e.keySet().forEach(v -> {
                Optional<DataSourceStructure.Structure> first = structureList.stream().filter(x -> x.getOriginalColumnName().equals(v)).findFirst();
                Object o = e.get(v);
                if (first.isPresent()) {
                    DataSourceStructure.Structure structure = first.get();
                    String newKey = structure.getColumnName();
                    //变更key  统一使用别名 如果在数据结构中没有 直接删除此数据
                    map.put(newKey, o);
                    try {
                        if (ObjectUtil.isNotEmpty(o)) {
                            String s = JSONObject.toJSONString(o);
                            JSONValidator from = JSONValidator.from(s);
                            if (from.getType().equals(JSONValidator.Type.Array)) {
                                s = s.replaceAll(KEYWORD_NAME, "");
                                JSONArray objects = JSONObject.parseArray(s);
                                map.put(newKey, objects);
                            } else if (from.getType().equals(JSONValidator.Type.Object)) {
                                s = s.replaceAll(KEYWORD_NAME, "");
                                map.put(newKey, JSONObject.parseObject(s));
                            }
                            if (o instanceof String) {
                                o = o.toString().replaceAll(KEYWORD_NAME, "");
                                map.put(newKey, o);
                            } else if (o instanceof Time) {
                                o = DateUtil.format((Date) o, structure.getFormat());
                                map.put(newKey, o);
                            } else if (o instanceof Timestamp) {
                                if (StrUtil.isNotBlank(structure.getFormat())) {
                                    o = DateUtil.format((Date) o, structure.getFormat());
                                } else {
                                    o = DATE_FORMAT.format(o);
                                }
                                map.put(newKey, o);
                            } else if (o instanceof Date) {
                                o = DateUtil.format((Date) o, structure.getFormat());
                                map.put(newKey, o);
                            } else if (o instanceof LocalDateTime) {
                                o = ((LocalDateTime) o).format(DateTimeFormatter.ofPattern(structure.getFormat()));
                                map.put(newKey, o);
                            }
                        }
                    } catch (Exception exception) {
                        log.error("转换异常，将原值转为字符串返回");
                        map.put(newKey, StrUtil.toString(o));
                    }
                }
            });
            return map;
        }).collect(Collectors.toList());
        objectPage.setRecords(mapList);
        return objectPage;
    }

    @Override
    public List<DataSourceStructure.Structure> getStructure(DataSource dataSource, DataSourceStructure dataSourceStructure) {
        return datasourceStructureService.getDataSourceStructure(dataSource, dataSourceStructure);
    }

    @Override
    public void duplicateName(String name, String dataSourceId) {
        //判断名称是否重复
        LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<DataSource>().eq(DataSource::getSourceName, name);
        if (StrUtil.isNotBlank(dataSourceId)) {
            queryWrapper.ne(DataSource::getId, dataSourceId);
        }
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException("名称重复");
        }
    }

    @Override
    public Long upGetOrDataRecover(String id) {
        dataSourceMapper.retrieve(id);
        return this.count(new LambdaQueryWrapper<DataSource>().eq(DataSource::getId, id));
    }

    @Override
    public void check(String json, DataSourceTypeEnum dataSourceTypeEnum) {
        DataSourceExecuteInterface bean = (DataSourceExecuteInterface) SpringContextUtil.getApplicationContext().getBean(dataSourceTypeEnum.value);
        bean.check(json);
    }

    @Override
    public void updateDataFactoryStructure(DataSourceStructure dataSourceStructure) {
        //判断是否存在数据
        DataSourceStructure one = datasourceStructureService.getOne(new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getExecuteName, dataSourceStructure.getExecuteName()));
        if (one != null) {
            List<DataSourceStructure.Structure> list = dataSourceStructure.getStructure().stream().peek(v -> {
                Optional<DataSourceStructure.Structure> optional = one.getStructure().stream().filter(x -> StrUtil.isNotBlank(x.getOriginalColumnName())).filter(x -> x.getOriginalColumnName().equals(v.getOriginalColumnName())).findFirst();
                if (optional.isPresent()) {
                    DataSourceStructure.Structure structure1 = optional.get();
                    v.setColumnName(structure1.getColumnName());
                    if (structure1.getDataFieldTypeEnum().equals(v.getDataFieldTypeEnum())) {
                        v.setNewColumnNameIs(Boolean.FALSE);
                    }
                }
            }).collect(Collectors.toList());
            dataSourceStructure.setStructure(list);
            dataSourceStructure.setId(one.getId());
        }
        dataSourceStructure.setCheckIs(Boolean.TRUE);
        datasourceStructureService.saveOrUpdate(dataSourceStructure);
    }

    @Override
    public void saveDataFactory() {
        //获取菜单的时候 先判断是否存在数据集 没有先添加一条记录
        //获取锁 为什么 需要锁 是因为防止用户重复点击菜单
        boolean lock = redisUtils.tryLock(RedisKey.getDataSourceLockKey(), 10);
        if (lock) {
            try {
                long count = this.count(new LambdaQueryWrapper<DataSource>().eq(DataSource::getSourceType, DataSourceTypeEnum.dataFactoryDataSource));
                if (count == 0) {
                    DataSource dataSource = new DataSource().setSourceName("数据集").setSyncStructure(1).setSourceType(DataSourceTypeEnum.dataFactoryDataSource);
                    this.save(dataSource);
                }
            } catch (Exception e) {
                log.info("新增数据失败", e);
            } finally {
                //释放锁
                redisUtils.unLock(RedisKey.getDataSourceLockKey());
            }
        }
    }

    @Override
    @Async
    public void syncRead(ExcelReadDataPo readDataPo, String tenantId) {
        TenantContextHolder.setTenantId(tenantId);
        SpringContextUtil.getApplicationContext().getBean(ExcelExecuteImpl.class).read((readDataPo));
    }

    @Override
    @Async
    public void syncTableStructure(DataSource dataSource, String tenantId, UserDto userDto) {
        //重新设置租户id
        TenantContextHolder.setTenantId(tenantId);
        DataSourceExecuteInterface bean = (DataSourceExecuteInterface) SpringContextUtil.getApplicationContext().getBean(dataSource.getSourceType().value);
        //同步结果
        int syncStructure = 1;
        Boolean checkIs = Boolean.FALSE;
        Boolean realTimeIsOpen = Boolean.FALSE;
        try {
            //删除原有的结构  这里不能直接 删除 需要 判断那些需要修改 那些需要删除  那些需要新增 防止 其他地方使用了改数据导致 id变更 统一通过executeName 进行判断
            List<DataSourceStructure> structure = bean.syncTableStructure(dataSource);
            //获取是否可以开启实时数据
            realTimeIsOpen = bean.realTimeIsOpen(dataSource);
            List<DataSourceStructure> list = datasourceStructureService.list(new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getDataSourceId, dataSource.getId()));
            if (!list.isEmpty()) {
                //获取需要删除的数据-防止表删除
                List<String> deleteList = list.stream().filter(e -> structure.stream().noneMatch(v -> v.getExecuteName().equals(e.getExecuteName()))).map(DataSourceStructure::getId).collect(Collectors.toList());
                datasourceStructureService.removeByIds(deleteList);
                //设置需要修改的数据id
                structure.stream().peek(e -> {
                    Optional<DataSourceStructure> first = list.stream().filter(v -> v.getExecuteName().equals(e.getExecuteName())).findFirst();
                    if (first.isPresent()) {
                        //判断名称与类型是否变更 只要其中一个变更 就需要重新生成
                        DataSourceStructure dataSourceStructure = first.get();
                        List<DataSourceStructure.Structure> structures = dataSourceStructure.getStructure();
                        e.getStructure().stream().peek(v -> {
                            Optional<DataSourceStructure.Structure> optional = structures.stream().filter(x -> StrUtil.isNotBlank(x.getOriginalColumnName())).filter(x -> x.getOriginalColumnName().equals(v.getOriginalColumnName())).findFirst();
                            if (optional.isPresent()) {
                                DataSourceStructure.Structure structureOld = optional.get();
                                v.setColumnName(structureOld.getColumnName()).setDataFieldTypeEnum(structureOld.getDataFieldTypeEnum()).setDataFieldTypeClassify(structureOld.getDataFieldTypeClassify()).setDorisType(structureOld.getDorisType());
                                v.setFormatDefault(structureOld.getFormatDefault());
                                if (structureOld.getDataType().equals(v.getDataType())) {
                                    v.setNewColumnNameIs(Boolean.FALSE);
                                }
                            }
                        }).collect(Collectors.toList());
                        e.setId(dataSourceStructure.getId());
                    }
                }).collect(Collectors.toList());
            }
            //低代码数据源直接 使用 返回 不进行二次 处理 主打相信
            if (!dataSource.getSourceType().equals(DataSourceTypeEnum.dataModel)) {
                //统一设置别名
                structure.stream().peek(e -> e.getStructure().stream().peek(v -> {
                    //这里需要区分一下 如果原来的存在需要判断是否 需要重新生成别名
                    if (v.getNewColumnNameIs()) {
                        v.setColumnName(IdGenerator.getIdStr());
                        //重新设置doris类型
                        JvsSourceToDoris serviceOne = jvsSourceToDorisService.getOne(new LambdaQueryWrapper<JvsSourceToDoris>().eq(JvsSourceToDoris::getSourceType, dataSource.getSourceType()).eq(JvsSourceToDoris::getFieldType, v.getDataType().toLowerCase()));
                        serviceOne = Optional.ofNullable(serviceOne).orElse(new JvsSourceToDoris().setDataFieldTypeClassify(DataFieldTypeClassifyEnum.字符串).setDataFieldType(DataFieldTypeEnum.STRING).setDorisFieldType(DataFieldTypeEnum.STRING.getCreateTable()));
                        v.setDorisType(serviceOne.getDorisFieldType());
                        v.setFunction(serviceOne.getFunctionStr());
                        v.setDataFieldTypeEnum(serviceOne.getDataFieldType());
                        v.setDataFieldTypeClassify(serviceOne.getDataFieldTypeClassify());
                    }
                }).collect(Collectors.toList())).collect(Collectors.toList());
            }
            //对比日志记录
            syncStructureLogService.comparisonAndSave(list, structure, dataSource.getId(), userDto);
            structure.stream().peek(e -> e.setDataSourceId(dataSource.getId())).collect(Collectors.toList());
            checkIs = (int) structure.stream().filter(DataSourceStructure::getCheckIs).count() > 0;
            datasourceStructureService.saveOrUpdateBatch(structure);
            log.info("------------ finish run sync structure ------------");
        } catch (Exception e) {
            SyncStructureLog syncStructureLog = new SyncStructureLog().setErrorLog(e.getMessage())
                    .setState(StateEnums.fail)
                    .setDataSourceId(dataSource.getId());
            //设置用户信息
            syncStructureLog.setCreateBy(userDto.getRealName());
            syncStructureLog.setCreateById(userDto.getId());
            syncStructureLogService.save(syncStructureLog);
            e.printStackTrace();
            log.info("表结构同步失败", e);
            syncStructure = 0;
        } finally {
            log.info("------------ run data source : set sync status {} ------------", syncStructure);
            this.update(new UpdateWrapper<DataSource>().lambda().eq(DataSource::getId, dataSource.getId())
                    .set(DataSource::getRealTimeOpen, realTimeIsOpen)
                    .set(DataSource::getSyncStructure, syncStructure).set(DataSource::getCheckIs, checkIs));
        }
    }
}
