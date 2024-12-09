package cn.bctools.design.rule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.bctools.design.rule.entity.RunLogPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author guojing
 */
@Mapper
public interface RunLogDao extends BaseMapper<RunLogPo> {
}
