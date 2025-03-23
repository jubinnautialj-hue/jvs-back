package cn.bctools.data.factory.source.mapper;

import cn.bctools.data.factory.source.entity.JvsSourceToDoris;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author admin
 * @description 不同数据源字段类型与doris字段类型的映射
 */
@Mapper
public interface JvsSourceToDorisMapper extends BaseMapper<JvsSourceToDoris> {

}
