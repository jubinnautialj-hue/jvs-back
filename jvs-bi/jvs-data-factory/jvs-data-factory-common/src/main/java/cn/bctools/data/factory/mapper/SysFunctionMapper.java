package cn.bctools.data.factory.mapper;

import cn.bctools.data.factory.entity.SysFunction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统函数 doris自定义函数
 *
 * @author zqs
 */
@Mapper
public interface SysFunctionMapper extends BaseMapper<SysFunction> {

}
