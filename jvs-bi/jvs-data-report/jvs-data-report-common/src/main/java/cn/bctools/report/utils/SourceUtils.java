package cn.bctools.report.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.data.factory.api.DataFactoryApi;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.bctools.report.enums.EValueType;
import cn.bctools.report.model.JoinSetting;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.UCellExpand;
import cn.bctools.report.model.univer.conf.UField;
import cn.bctools.report.model.univer.conf.UJoin;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wl
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SourceUtils {

    private final DorisJdbcTemplate dorisJdbcTemplate;
    private final DataFactoryApi dataFactoryApi;

    private static final String JOIN_FORMAT = "`{}`.`{}`";
    private static final String CONNECTOR = " JOIN ";
    private static final String LEFT_CONNECTOR = " LEFT JOIN ";

    /**
     * 表名缓存10秒
     */
    private static final TimedCache<String,String> TABLE_NAME = CacheUtil.newTimedCache(10*1000);

    public String getTableNames(List<UCell> cells){
        LinkedHashMap<String, UCell> collect = cells.stream()
                .filter(e -> EValueType.数据集.equals(e.getCustom().getValueType()))
                .collect(Collectors.toMap(e -> e.getCustom().getField().getExecuteName(), Function.identity(), (v1, v2) -> {
                    boolean present = Optional.of(v1.getCustom()).map(UCellExpand::getJoin).map(UJoin::getJoinSettings).filter(CollectionUtil::isNotEmpty).isPresent();
                    boolean present2 = Optional.of(v1.getCustom()).map(UCellExpand::getJoin).map(UJoin::getJoinSettings).filter(CollectionUtil::isNotEmpty).isPresent();
                    if(present&&!present2){
                        return v1;
                    }
                    if(!present&&present2){
                        return v2;
                    }
                    return v1;
                }, LinkedHashMap::new));

        List<UCell> values = new ArrayList<>(collect.values());
        if(CollectionUtil.isEmpty(values)){
            return null;
        }
        if(values.size()==1){
            UField field = values.get(0).getCustom().getField();
            return getTableName(field.getExecuteName(),field.getIsMock());
        }

        StringBuilder builder = new StringBuilder();
        for (UCell value : values) {
            UField field = value.getCustom().getField();
            String tableName = getTableName(field.getExecuteName(), field.getIsMock());
            if(builder.length()<=0){
                builder.append(tableName);
            }else{
                UJoin join = value.getCustom().getJoin();
                boolean isMain = StrUtil.isNotBlank(join.getMainExecuteName());
                if(isMain || CollectionUtil.isNotEmpty(join.getJoinSettings())){
                    builder.append(LEFT_CONNECTOR);
                }else{
                    builder.append(CONNECTOR);
                }
                builder.append(tableName);
                List<JoinSetting> joinSettings = join.getJoinSettings();
                if(CollectionUtil.isNotEmpty(joinSettings)){
                    String on = joinSettings.stream()
                            .filter(JoinSetting::isAllNotBlank)
                            .map(e -> {
                                String left = StrUtil.format(JOIN_FORMAT,e.getLeftExecuteName(),e.getLeftColumn());
                                String right = StrUtil.format(JOIN_FORMAT,e.getRightExecuteName(),e.getRightColumn());
                                if(isMain && join.getMainExecuteName().equals(e.getRightExecuteName())){
                                    return right+StringPool.EQUALS+left;
                                }
                                return left+ StringPool.EQUALS+right;
                            })
                            .collect(Collectors.joining(" AND "));
                    builder.append(" ON ").append(on);
                }
            }
        }
        return builder.toString();
    }

    /**
     * 根据数据集id获取doris表名称
     * @param dataFactoryId 数据集id
     * @return doris表名称
     */
    public String getTableName(String dataFactoryId,boolean isMock) {
        try {
            if(isMock){
                return dataFactoryId;
            }
            if(TABLE_NAME.containsKey(dataFactoryId)){
                return TABLE_NAME.get(dataFactoryId,false);
            }
            R<String> r = dataFactoryApi.getTableName(dataFactoryId);
            if(r.is()){
                String tableName = r.getData();
                if (StrUtil.isNotBlank(tableName)) {
                    TABLE_NAME.put(dataFactoryId,tableName);
                }
                return tableName;
            }
            return null;
        }catch (Exception e) {
            if(e instanceof FeignException){
                if(e.getCause()!=null && e.getCause() instanceof BusinessException){
                    throw new BusinessException(e.getMessage());
                }
                throw new BusinessException("数据集服务异常");
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行sql 获取数据
     * @param sql doris Sql语句
     * @return 数据
     */
    public List<Map<String,Object>> getData(String sql){
        try {
            return dorisJdbcTemplate.execSql(sql);
        }catch (UncategorizedSQLException uncategorizedSQLException){
            throw new RuntimeException("查询数据失败：查询语句异常");
        }catch (Exception e) {
            throw new RuntimeException("查询数据失败");
        }
    }
}
