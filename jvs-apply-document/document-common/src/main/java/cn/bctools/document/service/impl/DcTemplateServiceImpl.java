package cn.bctools.document.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.document.entity.DcTemplate;
import cn.bctools.document.mapper.DcTemplateMapper;
import cn.bctools.document.service.DcTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 文档模板
 *
 * @Author: GuoZi
 */
@Slf4j
@Service
public class DcTemplateServiceImpl extends ServiceImpl<DcTemplateMapper, DcTemplate> implements DcTemplateService {

    @Override
    public DcTemplate get(String id) {
        if (StringUtils.isBlank(id)) {
            throw new BusinessException("模板id为空");
        }
        DcTemplate dcLibrary = this.getById(id);
        if (Objects.isNull(dcLibrary)) {
            throw new BusinessException("模板不存在");
        }
        return dcLibrary;
    }


}
