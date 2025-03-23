package cn.bctools.screen.service;
import cn.bctools.screen.dto.LinkDataDto;

import java.util.List;

/**
 * 数据联动
 *
 * @author zqs
 */
public interface LinkService {
    /**
     * 联动执行 联动就是数据过滤
     *
     * @param linkDataDto 联动数据
     * @return 过滤后的数据
     */
    List<Object> link(LinkDataDto linkDataDto, StringBuffer whereSql );
}
