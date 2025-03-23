package cn.bctools.data.factory.source.mapper;

import cn.bctools.data.factory.source.entity.DataSource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author admin
 * @description 数据源配置信息
 */
@Mapper
public interface DataSourceMapper extends BaseMapper<DataSource> {
    /**
     * 修改删除状态的值
     *
     * @param id 数据id
     */
    @Update("update jvs_data_source set del_flag = 0  where id =#{id}")
    void retrieve(@Param("id") String id);
}
