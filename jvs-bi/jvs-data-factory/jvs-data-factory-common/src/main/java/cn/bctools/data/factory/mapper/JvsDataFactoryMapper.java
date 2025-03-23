package cn.bctools.data.factory.mapper;

import cn.bctools.data.factory.entity.JvsDataFactory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 数据etl Mapper 接口
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Mapper
public interface JvsDataFactoryMapper extends BaseMapper<JvsDataFactory> {

    /**
     * 修改删除状态的值
     *
     * @param id 数据id
     */
    @Update("update jvs_data_factory set del_flag = 0  where id =#{id}")
    void retrieve(@Param("id") String id);

}
