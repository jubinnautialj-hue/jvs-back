package cn.bctools.design.data.source.impl.doris;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.source.impl.sql.BaseDynamicDMLImpl;
import cn.bctools.design.data.source.aspect.DynamicDML;
import cn.hutool.core.io.unit.DataSizeUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @Author: ZhuXiaoKang
 * @Description: Doris数据库DML
 */
@Slf4j
@ConditionalOnProperty(name = "dynamic.data-source", havingValue = "doris")
@Service
@DynamicDML
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public class DorisDMLServiceImpl extends BaseDynamicDMLImpl {

    @Override
    public Long tableDataSize(String collectionName) {
        String sql = String.format(DorisSqlConstant.TABLE_DATA_SIZE, collectionName);
        if (ObjectNull.isNull(sql)) {
            return 0L;
        }
        return dataSourceExecuteResult(sql, resultSet -> {
            try {
                while (resultSet.next()) {
                    // doris返回的存储格式如：4.569 KB 。 转换为字节
                    if ("Total".equals(resultSet.getString("IndexName"))) {
                        String size = Optional.ofNullable(resultSet.getString("Size")).orElse("0B")
                                .replace(StringPool.DOT, StringPool.EMPTY)
                                .replace(StringPool.SPACE, StringPool.EMPTY);
                        return DataSizeUtil.parse(size);
                    }
                }
            } catch (Exception e) {
                log.error("获取存储异常：", e);
                throw new BusinessException(e.getMessage());
            }
            return 0L;
        });
    }
}
