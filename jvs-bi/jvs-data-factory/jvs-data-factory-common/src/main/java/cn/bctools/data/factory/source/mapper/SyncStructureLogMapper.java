package cn.bctools.data.factory.source.mapper;


import cn.bctools.data.factory.source.entity.SyncStructureLog;
import cn.bctools.data.factory.source.entity.SysJar;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author admin
 * @description 同步表结构日志
 */
@Mapper
public interface SyncStructureLogMapper extends BaseMapper<SyncStructureLog> {

}
