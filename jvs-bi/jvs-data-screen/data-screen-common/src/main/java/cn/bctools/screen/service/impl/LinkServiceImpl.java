package cn.bctools.screen.service.impl;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.data.factory.query.Query;
import cn.bctools.data.factory.query.QueryExecDto;
import cn.bctools.screen.dto.LinkDataDto;
import cn.bctools.screen.service.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author admin
 */
@Slf4j
@Service
public class LinkServiceImpl implements LinkService {
    @Override
    public List<Object> link(LinkDataDto linkDataDto, StringBuffer whereSql) {
        //先check一下
        LinkDataDto dataDto = Optional.ofNullable(linkDataDto).orElse(new LinkDataDto());
        if (dataDto.checkIsEmpty()) {
            return new ArrayList<>();
        }
        List<Object> list = new ArrayList<>();
        whereSql.append(" and (");
        for (LinkDataDto.LinkData linkData : linkDataDto.getLinkData()) {
            whereSql.append("`").append(linkData.getFieldKey()).append("`");
            QueryExecDto queryExecDto = new QueryExecDto()
                    .setMethodValue(linkData.getValue().get(0).toString())
                    .setMethod(linkData.getQueryEnums())
                    .setFieldKey(linkData.getFieldKey())
                    .setFormat(linkData.getFormat());
            Query query = SpringContextUtil.getBean(queryExecDto.getMethod().getCls());
            List<Object> exec = query.exec(queryExecDto, whereSql);
            list.addAll(exec);
            whereSql.append(" and ");
        }
        whereSql.delete(whereSql.length() - 3, whereSql.length());
        whereSql.append(")");
        return list;
    }


}
