package cn.bctools.data.factory.html.node;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.data.factory.constant.RedisKey;
import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.entity.JvsDataFactoryOut;
import cn.bctools.data.factory.html.FData;
import cn.bctools.data.factory.html.node.params.InputParams;
import cn.bctools.data.factory.html.run.Frun;
import cn.bctools.data.factory.query.QueryExecuteFactory;
import cn.bctools.data.factory.service.JvsDataFactoryOutService;
import cn.bctools.data.factory.service.JvsDataFactoryService;
import cn.bctools.data.factory.source.data.sync.plugin.DataSyncPlugin;
import cn.bctools.data.factory.source.entity.DataSourceStructure;
import cn.bctools.data.factory.source.enums.DataSourceTypeEnum;
import cn.bctools.data.factory.source.service.DataSourceStructureService;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author guojing
 * 输入节点
 */
@Data
@Service
@Slf4j
public class InputNode implements Frun<InputParams> {

    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final RedisUtils redisUtils;
    private final DataSourceStructureService dataSourceStructureService;
    private final QueryExecuteFactory queryExecuteFactory;

    @Override
    public FData run(Boolean formal, Map linkBody, InputParams nodeHtml) {
        long count = dataSourceStructureService.count(new LambdaQueryWrapper<DataSourceStructure>().eq(DataSourceStructure::getId, nodeHtml.getSourceData().getFromSource().getId()));
        if (count == 0) {
            throw new BusinessException("数据源不存在!");
        }
        //判断是否存在此建模数据如果存在就不用同步了
        String nodeHtmlTableName = nodeHtml.getTableName();
        boolean isLock = false;
        if (!formal) {
            String jsonString = JSONObject.toJSONString(nodeHtml.getFieldList());
            String md5 = SecureUtil.md5(jsonString);
            String inputKey = RedisKey.getDataFactoryInputKey(nodeHtml.getDataId(), nodeHtml.getId());
            boolean existsTable = dorisJdbcTemplate.ifNotExistsTable(nodeHtmlTableName);
            if (!existsTable) {
                redisUtils.set(inputKey, md5, (long) (5 * 24 * 60 * 60));
                isLock = true;
            } else {
                //需要判断key值是否存在 如果不存在或者md5不一致都需要重新同步
                String value = Optional.ofNullable(redisUtils.get(inputKey)).orElse("").toString();
                if (!value.equals(md5)) {
                    redisUtils.set(inputKey, md5, (long) (5 * 24 * 60 * 60));
                    isLock = true;
                    //如果是重新同步 需要删除原有的表1
                    dorisJdbcTemplate.dropForce(nodeHtmlTableName);
                }
            }
        }
        if (formal || isLock) {
            long size = 10000L;
            if (formal) {
                size = 0L;
            }
            syncData(nodeHtml, isLock, size);
        }
        return new FData().setDocumentName(nodeHtmlTableName).setTitle(nodeHtml.getFieldList());
    }

    /**
     * 同步数据
     *
     * @param nodeHtml 输入节点的设计数据
     * @param isLock   是否加锁
     */
    public void syncData(InputParams nodeHtml, Boolean isLock, Long size) {
        JvsDataFactoryService factoryService = SpringContextUtil.getBean(JvsDataFactoryService.class);
        Boolean lock = Boolean.TRUE;
        if (isLock) {
            lock = factoryService.getSyncOdsDataIsLock(nodeHtml.getDataId(), nodeHtml.getId());
        }
        if (lock) {
            try {
                //数据源如果是 excel 是不需要通过 datax同步数据
                InputParams sourceData = nodeHtml.getSourceData();
                InputParams.FromSource fromSource = sourceData.getFromSource();
                if (Arrays.asList(DataSourceTypeEnum.excelDataSource.getValue(), DataSourceTypeEnum.dataFactoryDataSource.getValue()).contains(fromSource.getSourceType())) {
                    //上一个节点的数据库名称
                    StringBuffer sql = new StringBuffer();
                    DataSourceStructureService structureService = SpringContextUtil.getBean(DataSourceStructureService.class);
                    DataSourceStructure dataSourceStructure = structureService.getById(fromSource.getId());
                    //同步数据
                    List<DataSourceField> fieldList = nodeHtml.getFieldList();
                    String executeName = dataSourceStructure.getExecuteName();
                    //如果是数据集 需要获取表名称
                    if (DataSourceTypeEnum.dataFactoryDataSource.getValue().equals(fromSource.getSourceType())) {
                        JvsDataFactoryOutService dataFactoryOutService = SpringContextUtil.getBean(JvsDataFactoryOutService.class);
                        JvsDataFactoryOut one = dataFactoryOutService.getOne(new LambdaQueryWrapper<JvsDataFactoryOut>().eq(JvsDataFactoryOut::getDataId, executeName).orderByDesc(JvsDataFactoryOut::getCreateTime).last("limit 1"));
                        executeName = Optional.ofNullable(one).orElseThrow(() -> new BusinessException("没有找到此数据集")).getDocumentName();
                        fieldList = one.getFieldList();
                    }
                    List<Object> objects = createSql(new ArrayList<>(), fieldList, sql, executeName, nodeHtml.getTableName(), Boolean.TRUE, Boolean.FALSE, sourceData);
                    //添加分页限制
                    if (size > 0) {
                        //如果有size 现在表示 是在设计阶段 就需要添加查询条件
                        if (nodeHtml.getSourceData().getFromSource().getQueryDto() != null) {
                            List<Object> execute = queryExecuteFactory.execute(nodeHtml.getSourceData().getFromSource().getQueryDto(), sql, Boolean.TRUE);
                            objects.addAll(execute);
                        }
                        sql.append(" limit ").append(size);
                    }
                    this.save(sql.toString(), nodeHtml.getTableName(), fieldList, Boolean.TRUE, null, Boolean.FALSE, new ArrayList<>(), objects.toArray());
                    //建表
                } else {
                    //获取表结构
                    List<DataSourceField> fieldList = nodeHtml.getFieldList().stream().filter(DataSourceField::getIsShow).collect(Collectors.toList());
                    List<DataSourceField> fields = JSONArray.parseArray(JSONObject.toJSONString(fieldList), DataSourceField.class);
                    //如果不是正式运行 增量模式永远为 false
                    //同步数据
                    InputParams.IncrementalSetting incrementalSetting = Optional.ofNullable(fromSource.getIncrementalSetting()).orElse(new InputParams.IncrementalSetting().setIncrementalMode(Boolean.FALSE));
                    if (size > 0) {
                        incrementalSetting.setIncrementalMode(false);
                    }
                    DataSyncPlugin syncPlugin = SpringContextUtil.getBean(sourceData.getFromSource().getDataSyncPlugin().getAClass());
                    //获取泛型
                    Type actualTypeArgument = ((ParameterizedTypeImpl) sourceData.getFromSource().getDataSyncPlugin().getAClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
                    Object object = JSONObject.parseObject(JSONObject.toJSONString(incrementalSetting), actualTypeArgument);
                    syncPlugin.syncData(fromSource.getId(), fields, size, nodeHtml.getTableName(), object);
                }
            } catch (Exception exception) {
                log.error("同步数据错误", exception);
                if (exception instanceof BusinessException) {
                    throw exception;
                } else {
                    throw new BusinessException("同步数据错误!");
                }
            } finally {
                if (isLock) {
                    factoryService.unLockSyncOdsLock(nodeHtml.getDataId(), nodeHtml.getId());
                }
            }
        }
    }

}
