package cn.bctools.data.factory.service.impl;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.common.entity.dto.DeptDto;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.constant.RedisKey;
import cn.bctools.data.factory.dto.*;
import cn.bctools.data.factory.entity.ConsanguinityAnalyse;
import cn.bctools.data.factory.entity.JvsDataAuth;
import cn.bctools.data.factory.entity.JvsDataFactory;
import cn.bctools.data.factory.entity.JvsDataFactoryOut;
import cn.bctools.data.factory.entity.enums.JvsDataAuthTypeEnum;
import cn.bctools.data.factory.entity.enums.OperateMethodEnum;
import cn.bctools.data.factory.entity.enums.QueueTaskTypeEnum;
import cn.bctools.data.factory.enums.CollectTypeEnum;
import cn.bctools.data.factory.enums.ConsanguinityViewTypeEnum;
import cn.bctools.data.factory.enums.OperationEnum;
import cn.bctools.data.factory.html.FHtmlGraph;
import cn.bctools.data.factory.html.FNodeType;
import cn.bctools.data.factory.html.node.params.FilterParams;
import cn.bctools.data.factory.html.node.params.InputParams;
import cn.bctools.data.factory.im.MessageImPush;
import cn.bctools.data.factory.mapper.JvsDataFactoryMapper;
import cn.bctools.data.factory.query.QueryExecuteFactory;
import cn.bctools.data.factory.query.po.DataSourceLinkInPo;
import cn.bctools.data.factory.query.value.enums.QueryEnums;
import cn.bctools.data.factory.receiver.ConsanguinityAnalyseConsumer;
import cn.bctools.data.factory.receiver.DataFactoryTaskConsumer;
import cn.bctools.data.factory.service.JvsDataAuthService;
import cn.bctools.data.factory.service.JvsDataFactoryOutService;
import cn.bctools.data.factory.service.JvsDataFactoryQueueService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.source.data.mqtt.MqttClientFactory;
import cn.bctools.data.factory.source.data.mqtt.OnMessageCallback;
import cn.bctools.data.factory.source.dto.ApiDataSourceExecDto;
import cn.bctools.data.factory.source.entity.DataSource;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceService;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.data.factory.source.service.SysJarService;
import cn.bctools.data.factory.source.util.JarExecUtil;
import cn.bctools.data.factory.util.AuthUtil;
import cn.bctools.data.factory.util.MqttUtil;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 数据etl 服务实现类
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Service
@AllArgsConstructor
@Slf4j
public class JvsDataFactoryServiceImpl extends ServiceImpl<JvsDataFactoryMapper, JvsDataFactory> implements JvsDataFactoryService {

    private final JvsDataFactoryOutService jvsDataFactoryOutService;
    private final RedisUtils redisUtils;
    private final JvsDataAuthService jvsDataAuthService;
    private final DataSourceService dataSourceService;
    private final JvsDataFactoryMapper jvsDataFactoryMapper;
    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final DataSourceStructureService dataSourceStructureService;
    private final AuthUtil<OperationEnum, JvsDataFactory> jvsDataFactoryAuthUtil;
    private final QueryExecuteFactory queryExecuteFactory;
    private final MqttClientFactory mqttClientFactory;
    private final MessageImPush messageImPush;
    private final SysJarService sysJarService;


    @Override
    public ColumnWhereDto getRowWhere(String id) {
        ColumnWhereDto columnWhereDto = new ColumnWhereDto().setWhereStr("").setInParameter(new ArrayList<>());
        UserDto user;
        try {
            user = UserCurrentUtils.getCurrentUser();
        } catch (Exception exception) {
            log.info("用户未登录", exception);
            return columnWhereDto;
        }
        JvsDataFactory byId = this.getById(id);
        List<JvsDataAuth> jvsDataAuth = jvsDataAuthService.list(new LambdaQueryWrapper<JvsDataAuth>().eq(JvsDataAuth::getDataFactoryId, id).eq(JvsDataAuth::getAuthType, JvsDataAuthTypeEnum.row));
        if (byId.getCreateById().equals(user.getId()) || jvsDataAuth.isEmpty()) {
            return columnWhereDto;
        }
        List<String> roleList = UserCurrentUtils.init().getRoles();
        //生成sql语句
        StringBuffer whereSql = new StringBuffer();
        List<Object> inParameter = jvsDataAuth.stream().map(e -> e.getAuthData().toJavaObject(JvsDataAuth.ColumnAuthData.class)).flatMap(e -> {
            boolean extracted = extracted(e.getPersonnel(), user, roleList);
            if (extracted) {
                return queryExecuteFactory.execute(e.getQueryDto(), whereSql, Boolean.FALSE).stream();
            }
            return new ArrayList<>().stream();
        }).collect(Collectors.toList());
        if (whereSql.length() == 0) {
            Boolean columnOtherUserVisible = Optional.ofNullable(byId.getRowOtherUserVisible()).orElse(Boolean.FALSE);
            if (columnOtherUserVisible) {
                return columnWhereDto;
            } else {
                throw new BusinessException("您目前没有此数据集的数据权限,请联系此数据集管理人员!");
            }
        }
        columnWhereDto.setWhereStr(whereSql.toString());
        columnWhereDto.setInParameter(inParameter);
        return columnWhereDto;
    }

    /**
     * 验证用户权限是否通过
     *
     * @param personnel 人员信息
     * @param user      当前用户信息
     * @param roleList  当前用户角色信息
     */
    private static boolean extracted(List<PersonnelDto> personnel, UserDto user, List<String> roleList) {
        return personnel.stream()
                .anyMatch(x -> {
                    switch (x.getType()) {
                        case dept:
                            return user.getDept().stream().map(DeptDto::getDeptId).anyMatch(v -> v.equals(x.getId()));
                        case role:
                            return roleList.contains(x.getId());
                        case user:
                            return x.getId().equals(user.getId());
                        default:
                            return false;
                    }
                });
    }

    @Override
    public List<String> getTimeTaskDataSourceId(List<JvsDataFactory> list) {
        return list.stream().flatMap(e -> {
            switch (e.getTaskType()) {
                case mqtt:
                    DataFactoryMqttViewDto mqttViewDto = com.alibaba.fastjson.JSONObject.parseObject(e.getViewJson(), DataFactoryMqttViewDto.class);
                    DataFactoryMqttViewDto.InputDataSource inputDataSource = mqttViewDto.getInputDataSource();
                    return Stream.of(inputDataSource.getDataSourceStructureId());
                case sql:
                    DataFactorySqlViewDto dataFactorySqlViewDto = com.alibaba.fastjson.JSONObject.parseObject(e.getViewJson(), DataFactorySqlViewDto.class);
                    return dataFactorySqlViewDto.getInputDataSource().stream()
                            .map(DataFactorySqlViewDto.InputDataSource::getDataSourceStructureId);
                case api:
                    DataFactoryApiViewDto dataFactoryApiViewDto = com.alibaba.fastjson.JSONObject.parseObject(e.getViewJson(), DataFactoryApiViewDto.class);
                    return Stream.of(dataFactoryApiViewDto.getInputDataSource().getDataSourceStructureId());
                default:
                    throw new BusinessException("未知类型");
            }
        }).collect(Collectors.toList());
    }

    @Override
    public BigDecimal execApi(DataFactoryApiViewDto dataFactoryApiViewDto) {
        //获取数据
        DataSourceStructure dataSourceStructure = dataSourceStructureService.getById(dataFactoryApiViewDto.getDataSourceStructureId());
        ApiDataSourceExecDto apiDataSourceExecDto = JSONObject.parseObject(dataSourceStructure.getCustomJson(), ApiDataSourceExecDto.class);
        //获取数据源 中的jar包id
        DataSource dataSource = dataSourceService.getById(dataSourceStructure.getDataSourceId());
        apiDataSourceExecDto.setJarId(dataSource.getCustomJson().toJavaObject(ApiDataSourceExecDto.class).getJarId());
        BigDecimal resultValue = null;
        String exec;
        if (StrUtil.isNotBlank(apiDataSourceExecDto.getJarId())) {
            File jarFile = sysJarService.getJarFile(apiDataSourceExecDto.getJarId());
            exec = JarExecUtil.exec(jarFile, JSONObject.toJSONString(apiDataSourceExecDto));
        } else {
            exec = JarExecUtil.exec(apiDataSourceExecDto);
        }
        String collectKey = dataFactoryApiViewDto.getCollectKey();
        log.info("请求接口返回的值为:{},路径参数为{}", exec,collectKey);
        JSONPath jsonPath = JSONPath.of(collectKey);
        String extract = JSONObject.toJSONString(jsonPath.extract(exec));
        extract = StrUtil.isNotBlank(extract) ? extract : "0";
        JSONValidator from = JSONValidator.from(extract);
        log.info("api数据集通过路径获取的值为:{},是否为空:{}", extract, StrUtil.isNotBlank(extract));
        //判断这个值是否为数组
        if (dataFactoryApiViewDto.getCollectType().equals(CollectTypeEnum.count)) {
            if (from.getType().equals(JSONValidator.Type.Array)) {
                resultValue = BigDecimal.valueOf(JSONArray.parseArray(extract).size());
            } else {
                resultValue = BigDecimal.ONE;
            }
        } else {
            List<BigDecimal> numbers = new ArrayList<>();
            if (from.getType().equals(JSONValidator.Type.Array)) {
                numbers.addAll(JSONArray.parseArray(extract, BigDecimal.class));
            } else {
                numbers.add(new BigDecimal(extract));
            }
            switch (dataFactoryApiViewDto.getCollectType()) {
                case avg:
                    resultValue = numbers.stream()
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(numbers.size()), BigDecimal.ROUND_HALF_UP);
                    break;
                case max:
                    Optional<BigDecimal> max = numbers.stream()
                            .max(BigDecimal::compareTo);
                    resultValue = max.orElse(BigDecimal.ZERO);
                    break;
                case min:
                    Optional<BigDecimal> min = numbers.stream()
                            .min(BigDecimal::compareTo);
                    resultValue = min.orElse(BigDecimal.ZERO);
                    break;
                case sum:
                    resultValue = numbers.stream()
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    break;
                default:
            }
        }
        return resultValue;
    }

    @Override
    public void execApiTimedTask(String id) {
        //清空线程数据
        SystemThreadLocal.clear();
        TenantContextHolder.clear();
        JvsDataFactory jvsDataFactory = this.getById(id);
        TenantContextHolder.setTenantId(jvsDataFactory.getTenantId());
        DataFactoryApiViewDto dataFactoryApiViewDto = JSONObject.parseObject(jvsDataFactory.getViewJson(), DataFactoryApiViewDto.class);
        BigDecimal resultValue = null;
        try {
            resultValue = this.execApi(dataFactoryApiViewDto);
        } catch (Exception exception) {
            log.info("api实时数据获取数据错误", exception);
        }
        //im通知
        String businessType = "jvs_data_factory_" + id;
        messageImPush.notify(resultValue, businessType);
    }

    @Override
    public BigDecimal execMqttTask(DataFactoryMqttViewDto dataFactoryMqttViewDto, MqttUtil mqttUtil) {
        OnMessageCallback onMessageCallback = mqttUtil.getOnMessageCallback();
        List<String> redisValue = onMessageCallback.getRedisValue();
        String collectKey = dataFactoryMqttViewDto.getCollectKey();
        List<BigDecimal> numbers = redisValue.stream().flatMap(e -> {
            JSONPath jsonPath = JSONPath.of(collectKey);
            Object extract = jsonPath.extract(e);
            extract = ObjectUtil.isNotEmpty(extract) ? extract : "0";
            if (!dataFactoryMqttViewDto.getCollectType().equals(CollectTypeEnum.count)) {
                JSONValidator from = JSONValidator.from(JSONObject.toJSONString(extract));
                if (from.getType().equals(JSONValidator.Type.Array)) {
                    return JSONArray.parseArray(JSONObject.toJSONString(extract), BigDecimal.class).stream();
                } else {
                    return Stream.of(new BigDecimal(extract.toString()));
                }
            }
            return Stream.of(BigDecimal.ZERO);
        }).collect(Collectors.toList());
        BigDecimal resultValue = null;
        //根据不同的聚合方式 进行数据聚合
        switch (dataFactoryMqttViewDto.getCollectType()) {
            case avg:
                resultValue = numbers.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(numbers.size()), BigDecimal.ROUND_HALF_UP);
                break;
            case max:
                Optional<BigDecimal> max = numbers.stream()
                        .max(BigDecimal::compareTo);
                resultValue = max.orElse(BigDecimal.ZERO);
                break;
            case min:
                Optional<BigDecimal> min = numbers.stream()
                        .min(BigDecimal::compareTo);
                resultValue = min.orElse(BigDecimal.ZERO);
                break;
            case sum:
                resultValue = numbers.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                break;
            case count:
                resultValue = new BigDecimal(numbers.size());
                break;
            default:
        }
        return resultValue;
    }

    @Override
    public void execMqttTimedTask(String id) {
        log.info("当前执行mqtt数据计算，数据集id为:{}", id);
        //清空线程数据
        SystemThreadLocal.clear();
        TenantContextHolder.clear();
        JvsDataFactory jvsDataFactory = this.getById(id);
        TenantContextHolder.setTenantId(jvsDataFactory.getTenantId());
        DataFactoryMqttViewDto mqttViewDto = JSONObject.parseObject(jvsDataFactory.getViewJson(), DataFactoryMqttViewDto.class);
        MqttUtil mqttUtil = mqttClientFactory.getMqttUtil(id);
        BigDecimal resultValue = null;
        try {
            resultValue = this.execMqttTask(mqttViewDto, mqttUtil);
        } catch (Exception exception) {
            log.info("执行mqtt数据集错误", exception);
        }
        //im通知
        String businessType = "jvs_data_factory_" + id;
        messageImPush.notify(resultValue, businessType);
    }

    @Override
    public List<DataSourceField> getColumn(String id) {
        JvsDataFactory byId = this.getById(id);
        List<JvsDataAuth> jvsDataAuth = jvsDataAuthService.list(new LambdaQueryWrapper<JvsDataAuth>().eq(JvsDataAuth::getDataFactoryId, id).eq(JvsDataAuth::getAuthType, JvsDataAuthTypeEnum.column));
        List<DataSourceField> fieldList = new ArrayList<>();
        //不同的数据集获取数据结构不一样
        switch (byId.getTaskType()) {
            case ordinary:
                //这里应该有个排序 然后获取最新的数据
                JvsDataFactoryOut factoryOut = jvsDataFactoryOutService.getOne(new LambdaQueryWrapper<JvsDataFactoryOut>().eq(JvsDataFactoryOut::getDataId, id)
                        .orderByDesc(JvsDataFactoryOut::getCreateTime)
                        .last("limit 1"));
                if (factoryOut != null) {
                    fieldList = factoryOut.getFieldList();
                }
                break;
            case sql:
                fieldList = JSONObject.parseObject(byId.getViewJson(), DataFactorySqlViewDto.class).getOutField();
                break;
            default:
        }

        UserDto user = UserCurrentUtils.getCurrentUser();
        if (byId.getCreateById().equals(user.getId()) || jvsDataAuth.isEmpty()) {
            return fieldList;
        }
        List<String> roleList = UserCurrentUtils.init().getRoles();
        List<String> fieldKeys = jvsDataAuth.stream().map(e -> e.getAuthData().toJavaObject(JvsDataAuth.RowAuthData.class)).flatMap(e -> {
                    boolean anyMatch = extracted(e.getPersonnel(), user, roleList);
                    if (anyMatch) {
                        return e.getQueryDto().stream();
                    }
                    return new ArrayList<DataSourceField>().stream();
                })
                .map(DataSourceField::getFieldKey)
                .distinct().collect(Collectors.toList());
        if (fieldKeys.isEmpty()) {
            Boolean rowOtherUserVisible = Optional.ofNullable(byId.getColumnOtherUserVisible()).orElse(Boolean.FALSE);
            if (rowOtherUserVisible) {
                return fieldList;
            } else {
                List<String> list = jvsDataAuth.stream().map(e -> e.getAuthData().toJavaObject(JvsDataAuth.RowAuthData.class))
                        .flatMap(e -> e.getQueryDto().stream())
                        .map(DataSourceField::getFieldKey).distinct().collect(Collectors.toList());
                //不可见的情况 需要排查不可见的列
                fieldList = fieldList.stream().filter(e -> !list.contains(e.getFieldKey())).collect(Collectors.toList());
                return fieldList;
            }
        }
        return fieldList.stream().filter(e -> fieldKeys.contains(e.getFieldKey())).collect(Collectors.toList());
    }

    @Override
    public JvsDataFactory auth(String id) {
        JvsDataFactory byId = this.getById(id);
        if (byId == null) {
            log.info("为空的数据id为:{}", id);
            JvsDataFactory jvsDataFactory = new JvsDataFactory();
            jvsDataFactory.setOperationList(new ArrayList<>());
            return jvsDataFactory;
        }
        return jvsDataFactoryAuthUtil.auth(byId, null, Arrays.asList(OperationEnum.values()));
    }

    @Override
    public JvsDataFactory delete(String id) {
        JvsDataFactory byId = this.getById(id);
        //判断队列中是否存在此任务 如果存在就无法保存
        JvsDataFactoryQueueService jvsDataFactoryQueueService = SpringContextUtil.getBean(JvsDataFactoryQueueService.class);
        String taskExec = jvsDataFactoryQueueService.isTaskExec(byId);
        if (StrUtil.isNotBlank(taskExec)) {
            throw new BusinessException(taskExec);
        }
        if (byId.getEnable()) {
            throw new BusinessException("启用数据无法删除");
        }
        List<String> tableName = dorisJdbcTemplate.getTableName(id);
        if (!tableName.isEmpty()) {
            //这里应该使用  数据集id 模糊查询表名称
            tableName.forEach(dorisJdbcTemplate::dropForce);
            //同步删除数据源里面的数据集数据
        }
        dataSourceStructureService.remove(new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getExecuteName, id));
        this.removeById(id);
        return byId;
    }

    @Override
    public JvsDataFactory copy(CopyDto copyDto) {
        JvsDataFactory byId = this.getById(copyDto.getId());
        if (ObjectUtil.isNull(byId)) {
            throw new BusinessException("设计不存在");
        }
        List<OperationEnum> check = jvsDataFactoryAuthUtil.check(OperationEnum.编辑, byId.getCreateById(), byId.getRole(), Arrays.asList(OperationEnum.values()), byId.getRoleType());
        if (CollectionUtil.isEmpty(check)) {
            throw new BusinessException("未拥有复制权限");
        }
        String id = IdGenerator.getIdStr();
        byId.setId(id).setType(copyDto.getMenuId()).setName(copyDto.getName());
        byId.setCreateBy(null);
        byId.setCreateTime(null);
        byId.setCreateById(null);
        byId.setUpdateBy(null);
        byId.setEnable(false);
        byId.setTask(null);
        byId.setUpdateTime(null);
        //重新设置每个节点的dataId
        String viewJson = byId.getViewJson();
        //不同类型复制方式不一样
        switch (byId.getTaskType()) {
            case ordinary:
                if (viewJson != null) {
                    JSONObject jsonObject = JSONObject.parseObject(viewJson);
                    jsonObject.getList("nodes", JSONObject.class)
                            .stream().peek(e -> e.put("dataId", id))
                            .collect(Collectors.toList());
                    viewJson = jsonObject.toString();
                }
                break;
            case sql:
                break;
            default:
        }
        byId.setViewJson(viewJson);
        this.save(byId);
        //添加血缘视图
        ConsanguinityAnalyse consanguinityAnalyse = new ConsanguinityAnalyse()
                .setDataFactoryId(byId.getId())
                .setTenantId(TenantContextHolder.getTenantId())
                .setViewType(ConsanguinityViewTypeEnum.dataFactoryDataSource)
                .setType(2)
                .setDesignId(byId.getId())
                .setDesignName(byId.getName());
        SpringContextUtil.getBean(ConsanguinityAnalyseConsumer.class).send(consanguinityAnalyse);
        return byId;
    }

    @Override
    public void syncTableStructure(JvsDataFactory factory) {
        switch (factory.getTaskType()) {
            case sql:
                syncTableStructureSql(factory);
                break;
            case mqtt:
                syncTableStructureMqtt(factory);
                break;
            case ordinary:
                syncTableStructureOrdinary(factory);
            default:
        }

    }

    /**
     * sql数据集 结构同步
     *
     * @param factory 数据
     */
    private void syncTableStructureSql(JvsDataFactory factory) {
        DataSourceStructure dataSourceStructure = new DataSourceStructure()
                .setName(factory.getName())
                .setExecuteName(factory.getId())
                .setTableNameDesc(factory.getName());
        DataFactorySqlViewDto dataFactorySqlViewDto = JSONObject.parseObject(factory.getViewJson(), DataFactorySqlViewDto.class);
        List<DataSourceStructure.Structure> fields = dataFactorySqlViewDto.getOutField().stream()
                .map(e -> {
                    DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                            .setOriginalColumnName(e.getFieldKey())
                            .setDataFieldTypeEnum(e.getFieldType())
                            .setFormat(e.getFormat())
                            .setDataFieldTypeClassify(e.getDataFieldTypeClassify())
                            .setFormatDefault(e.getFormatDefault())
                            .setLength(e.getLength())
                            .setPrecision(e.getPrecision())
                            .setDorisType(e.getDorisType())
                            .setColumnName(e.getFieldKey())
                            .setColumnCount(e.getFieldName());
                    return structure;
                })
                .collect(Collectors.toList());
        dataSourceStructure.setStructure(fields).setFieldCount(fields.size());
        //修改
        //防止没有默认数据
        dataSourceService.saveDataFactory();
        //直接获取数据集
        DataSource one = dataSourceService.getOne(new LambdaQueryWrapper<DataSource>().eq(DataSource::getSourceType, DataSourceTypeEnum.dataFactoryDataSource));
        dataSourceStructure.setDataSourceId(one.getId());
        dataSourceService.updateDataFactoryStructure(dataSourceStructure);
    }

    /**
     * mqtt 结构同步
     *
     * @param factory 数据
     */
    private void syncTableStructureMqtt(JvsDataFactory factory) {
        DataSourceStructure dataSourceStructure = new DataSourceStructure()
                .setName(factory.getName())
                .setExecuteName(factory.getId())
                .setTableNameDesc(factory.getName());
        //修改
        //防止没有默认数据
        dataSourceService.saveDataFactory();
        //直接获取数据集
        DataSource one = dataSourceService.getOne(new LambdaQueryWrapper<DataSource>().eq(DataSource::getSourceType, DataSourceTypeEnum.dataFactoryDataSource));
        dataSourceStructure.setDataSourceId(one.getId());
        dataSourceService.updateDataFactoryStructure(dataSourceStructure);
    }

    /**
     * 普通数据集 结构同步
     *
     * @param factory 数据
     */
    private void syncTableStructureOrdinary(JvsDataFactory factory) {
        JvsDataFactoryOut factoryOut = jvsDataFactoryOutService
                .getOne(new LambdaQueryWrapper<JvsDataFactoryOut>()
                        .eq(JvsDataFactoryOut::getDataId, factory.getId())
                        .orderByDesc(JvsDataFactoryOut::getCreateTime).last("limit 1"));
        DataSourceStructure dataSourceStructure = new DataSourceStructure()
                .setName(factory.getName())
                .setExecuteName(factory.getId())
                .setTableNameDesc(factory.getName());
        List<DataSourceStructure.Structure> fields = new ArrayList<>();
        if (ObjectUtil.isNotNull(factoryOut)) {
            List<DataSourceField> objects = JSONArray.parseArray(JSONObject.toJSONString(factoryOut.getFields()), DataSourceField.class);
            fields = objects.stream()
                    .filter(DataSourceField::getIsShow)
                    .map(e -> {
                        DataSourceStructure.Structure structure = new DataSourceStructure.Structure()
                                .setOriginalColumnName(e.getFieldKey())
                                .setDataFieldTypeEnum(e.getFieldType())
                                .setFormat(e.getFormat())
                                .setDataFieldTypeClassify(e.getDataFieldTypeClassify())
                                .setFormatDefault(e.getFormatDefault())
                                .setLength(e.getLength())
                                .setPrecision(e.getPrecision())
                                .setDorisType(e.getDorisType())
                                .setColumnName(e.getFieldKey())
                                .setColumnCount(e.getFieldName());
                        //子表数据
                        if (e.getItems() != null && !e.getItems().isEmpty()) {
                            List<DataSourceStructure.Structure> items = e.getItems().stream().map(v -> new DataSourceStructure.Structure()
                                            .setOriginalColumnName(v.getFieldKey())
                                            .setColumnName(v.getFieldName())
                                            .setDataFieldTypeClassify(v.getDataFieldTypeClassify())
                                            .setDataFieldTypeEnum(v.getFieldType())
                                            .setFormat(v.getFormat())
                                            .setLength(v.getLength())
                                            .setPrecision(v.getPrecision())
                                            .setFormatDefault(v.getFormatDefault())
                                            .setDorisType(v.getDorisType())
                                            .setColumnCount(v.getFieldKey()))
                                    .collect(Collectors.toList());
                            structure.setItems(items);
                        }
                        return structure;
                    })
                    .collect(Collectors.toList());
        }
        dataSourceStructure.setStructure(fields).setFieldCount(fields.size());
        //修改
        //防止没有默认数据
        dataSourceService.saveDataFactory();
        //直接获取数据集
        DataSource one = dataSourceService.getOne(new LambdaQueryWrapper<DataSource>().eq(DataSource::getSourceType, DataSourceTypeEnum.dataFactoryDataSource));
        dataSourceStructure.setDataSourceId(one.getId());
        dataSourceService.updateDataFactoryStructure(dataSourceStructure);
    }

    @Override
    public List<DownGetDataFactoryDto> downGetDataFactoryId(List<String> ids, List<String> figureOutList) {
        //防止为空
        //获取数据
        List<JvsDataFactory> list = this.listByIds(ids);
        if (list.size() != ids.stream().distinct().count()) {
            throw new BusinessException("部分数据集未找到");
        }
        //过来已经处理过的数据
        List<DownGetDataFactoryDto> factoryGetIdDtos = list.stream().filter(e -> !figureOutList.contains(e.getId())).flatMap(e -> {
            FHtmlGraph fHtmlGraph = com.alibaba.fastjson.JSONObject.parseObject(e.getViewJson(), FHtmlGraph.class);
            //获取输入源
            List<DownGetDataFactoryDto> dtos = fHtmlGraph.getNodes().stream().filter(v -> v.getType().equals(FNodeType.Input))
                    .map(v -> {
                        InputParams inputParams = com.alibaba.fastjson.JSONObject.parseObject(com.alibaba.fastjson.JSONObject.toJSONString(v.getSourceData()), InputParams.class);
                        DownGetDataFactoryDto sourceFactoryGetIdDto = new DownGetDataFactoryDto();
                        sourceFactoryGetIdDto.setIsDataSource(Boolean.TRUE);
                        String id = inputParams.getFromSource().getId();
                        DataSourceStructure byId = dataSourceStructureService.getById(id);
                        if (byId == null) {
                            throw new BusinessException("此数据集的输入源未找到");
                        }
                        if (inputParams.getFromSource().getSourceType().equals(DataSourceTypeEnum.dataFactoryDataSource.getValue())) {
                            //获取数据集id
                            id = byId.getExecuteName();
                            sourceFactoryGetIdDto.setIsDataSource(Boolean.FALSE);
                            sourceFactoryGetIdDto.setId(id);
                        } else {
                            //获取数据源id
                            String dataSourceId = byId.getDataSourceId();
                            sourceFactoryGetIdDto.setId(dataSourceId);
                        }
                        return sourceFactoryGetIdDto;
                    }).collect(Collectors.toList());
            //获取过滤节点存在不在其他数据源中的数据
            List<DownGetDataFactoryDto> getIdDtos = fHtmlGraph.getNodes().stream().filter(v -> v.getType().equals(FNodeType.dataFilter))
                    .flatMap(v -> {
                        FilterParams filterParams = com.alibaba.fastjson.JSONObject.parseObject(com.alibaba.fastjson.JSONObject.toJSONString(v.getSourceData()), FilterParams.class);
                        return filterParams.getRuleObj().getNodeTwigs().stream().filter(x -> x.getMethod().equals(QueryEnums.dataSourceLikeIn) || x.getMethod().equals(QueryEnums.dataSourceNotLikeIn))
                                .map(x -> {
                                    DataSourceLinkInPo dataSourceLinkInPo = com.alibaba.fastjson.JSONObject.parseObject(x.getMethodValue(), DataSourceLinkInPo.class);
                                    String id = dataSourceLinkInPo.getId();
                                    DownGetDataFactoryDto sourceFactoryGetIdDto = new DownGetDataFactoryDto();
                                    sourceFactoryGetIdDto.setId(id);
                                    sourceFactoryGetIdDto.setIsDataSource(Boolean.FALSE);
                                    return sourceFactoryGetIdDto;
                                });
                    }).collect(Collectors.toList());
            dtos.addAll(getIdDtos);
            //前置与后置
            List<DownGetDataFactoryDto> prefixTask = e.getPrefixTaskId().stream().map(x -> {
                DownGetDataFactoryDto sourceFactoryGetIdDto = new DownGetDataFactoryDto();
                sourceFactoryGetIdDto.setIsDataSource(Boolean.FALSE);
                sourceFactoryGetIdDto.setId(x);
                return sourceFactoryGetIdDto;
            }).collect(Collectors.toList());
            List<DownGetDataFactoryDto> rearTask = e.getRearTaskId().stream().map(x -> {
                DownGetDataFactoryDto sourceFactoryGetIdDto = new DownGetDataFactoryDto();
                sourceFactoryGetIdDto.setIsDataSource(Boolean.FALSE);
                sourceFactoryGetIdDto.setId(x);
                return sourceFactoryGetIdDto;
            }).collect(Collectors.toList());
            dtos.addAll(prefixTask);
            dtos.addAll(rearTask);
            log.info("前置任务:{}后置任务:{}", com.alibaba.fastjson.JSONObject.toJSONString(prefixTask), com.alibaba.fastjson.JSONObject.toJSONString(rearTask));
            return dtos.stream();
        }).collect(Collectors.toList());
        //获取当前数据集与数据源的绑定关系
        dataSourceStructureService.list(new LambdaQueryWrapper<DataSourceStructure>().in(DataSourceStructure::getExecuteName, ids))
                .forEach(e -> factoryGetIdDtos.add(new DownGetDataFactoryDto().setIsDataSource(Boolean.TRUE).setId(e.getDataSourceId())));
        //判断是否存在数据集 如果存在需要 递归遍历
        List<String> listId = factoryGetIdDtos.stream().filter(e -> !e.getIsDataSource()).map(DownGetDataFactoryDto::getId).collect(Collectors.toList());
        if (!listId.isEmpty()) {
            figureOutList.addAll(ids);
            List<DownGetDataFactoryDto> data = downGetDataFactoryId(listId, figureOutList);
            factoryGetIdDtos.addAll(data);
        }
        return factoryGetIdDtos;

    }

    @Override
    public void sendQueue(JvsDataFactory dataFactory, QueueTaskTypeEnum queueTaskTypeEnum, UserDto userDto, String batchId, OperateMethodEnum operateMethod) {
        //每一个批次的uuid 用于确认是否为同一个批次
        //判断是否存在前置任务
        List<String> taskId = new ArrayList<>();
        if (QueueTaskTypeEnum.PREFIX_TASK.equals(queueTaskTypeEnum)) {
            if (!dataFactory.getPrefixTaskId().isEmpty()) {
                taskId = dataFactory.getPrefixTaskId();
            } else {
                //前置任务没有生成任务本身
                queueTaskTypeEnum = QueueTaskTypeEnum.ITSELF;
            }
        }
        if (QueueTaskTypeEnum.ITSELF.equals(queueTaskTypeEnum)) {
            //如果不存在前置任务就直接执行本任务
            taskId = Arrays.asList(dataFactory.getId());
        }
        if (QueueTaskTypeEnum.REAR_TASK.equals(queueTaskTypeEnum) && !dataFactory.getRearTaskId().isEmpty()) {
            taskId = dataFactory.getRearTaskId();
        }
        this.send(queueTaskTypeEnum, taskId, dataFactory.getId(), batchId, userDto, operateMethod, dataFactory.getViewJson());

    }

    /**
     * @param queueTaskTypeEnum 任务类型
     * @param taskItselfId      任务本身id
     * @param ids               需要入队列的数据id
     * @return 生成的队列id
     */
    private void send(QueueTaskTypeEnum queueTaskTypeEnum, List<String> ids, String taskItselfId, String batchId, UserDto userDto, OperateMethodEnum operateMethod, String executionGraph) {
        ids.forEach(e -> {
            DataFactoryTaskMqDto dataFactoryTaskMqDto = new DataFactoryTaskMqDto()
                    .setDataFactoryId(e)
                    .setUserDto(userDto)
                    .setOperateMethod(operateMethod)
                    .setTaskItselfId(taskItselfId)
                    .setExecutionGraph(executionGraph)
                    .setBatchId(batchId)
                    .setQueueTaskType(queueTaskTypeEnum)
                    .setTenantId(TenantContextHolder.getTenantId());
            if (!userDto.getTenantId().equals(TenantContextHolder.getTenantId())) {
                throw new BusinessException("租户id被变更,变更后的租户id为:{}", userDto.getTenantId());
            }
            try {
                DataFactoryTaskConsumer dataFactoryTaskConsumer = SpringContextUtil.getBean(DataFactoryTaskConsumer.class);
                dataFactoryTaskConsumer.send(dataFactoryTaskMqDto);
            } catch (Exception exception) {
                log.info("-------进入队列失败入参为:{}", JSONObject.toJSONString(dataFactoryTaskMqDto), exception);
                if (exception instanceof BusinessException) {
                    throw exception;
                } else {
                    throw new BusinessException("任务创建失败,请稍候再试!");
                }
            }
        });

    }


    @Override
    public void unLockDataFactory(String dataFactoryId) {
        redisUtils.unLock(RedisKey.getDataFactoryLockKey(dataFactoryId));
    }

    @Override
    public void unLockSyncOdsLock(String dataFactoryId, String nodeId) {
        redisUtils.unLock(RedisKey.getSyncDataLock(dataFactoryId, nodeId));
    }

    @Override
    public Boolean getDataFactoryIsLock(String dataFactoryId) {
        return redisUtils.tryLock(RedisKey.getDataFactoryLockKey(dataFactoryId), 1800);
    }

    @Override
    public JvsDataFactory upGetOrDataRecover(String id) {
        jvsDataFactoryMapper.retrieve(id);
        return this.getById(id);
    }

    @Override
    public Boolean getSyncOdsDataIsLock(String dataFactoryId, String nodeId) {
        return redisUtils.tryLock(RedisKey.getSyncDataLock(dataFactoryId, nodeId), 1800);
    }

}
