package cn.bctools.remote.config;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.data.factory.config.DorisJdbcTemplate;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author doris jdbc
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DorisUtils {

    private final static String SEARCH_SQL = "SELECT {} FROM {}";

    private final DorisJdbcTemplate dorisJdbcTemplate;

    /**
     * 根据条件查询数据
     * @param tableName 表名称
     * @param params 条件
     * @return
     */
    public List<Map<String,Object>> search(String tableName,Map<String,Object> params,List<String> respField){
        if(CollectionUtil.isEmpty(respField)){
            throw new BusinessException("未设置返回字段");
        }

        String collect = String.join(",", respField);
        String execSql = StrUtil.format(SEARCH_SQL,collect, tableName);

        if(MapUtil.isNotEmpty(params)){
            String whereSql = params.keySet().stream().map(e -> StrUtil.format("`{}` = ?", e)).collect(Collectors.joining(" AND "));
            execSql = execSql+ " where " + whereSql;
        }
        Object[] objects = params.values().toArray();
        return dorisJdbcTemplate.queryForList(execSql, objects);
    }
}
