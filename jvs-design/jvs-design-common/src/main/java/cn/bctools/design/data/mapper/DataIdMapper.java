package cn.bctools.design.data.mapper;

import cn.bctools.design.data.entity.DataIdPo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据自增序号
 *
 * @Author: GuoZi
 */
@Mapper
public interface DataIdMapper extends BaseMapper<DataIdPo> {

}
