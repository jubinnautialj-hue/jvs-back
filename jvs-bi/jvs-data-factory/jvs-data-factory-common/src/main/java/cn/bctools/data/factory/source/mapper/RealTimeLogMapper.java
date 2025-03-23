package cn.bctools.data.factory.source.mapper;

import cn.bctools.data.factory.source.entity.RealTimeLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author admin
 * @description 实时数据日志
 */
@Mapper
public interface RealTimeLogMapper extends BaseMapper<RealTimeLog> {

}
