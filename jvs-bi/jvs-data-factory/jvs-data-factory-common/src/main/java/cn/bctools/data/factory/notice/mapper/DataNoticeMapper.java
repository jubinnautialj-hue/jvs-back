package cn.bctools.data.factory.notice.mapper;

import cn.bctools.data.factory.notice.po.DataNoticePo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息通知
 */
@Mapper
public interface DataNoticeMapper extends BaseMapper<DataNoticePo> {
}
