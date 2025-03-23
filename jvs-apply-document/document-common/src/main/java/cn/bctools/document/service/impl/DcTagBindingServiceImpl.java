package cn.bctools.document.service.impl;

import cn.bctools.document.entity.DcTag;
import cn.bctools.document.entity.DcTagBinding;
import cn.bctools.document.mapper.DcTagBindingMapper;
import cn.bctools.document.service.DcTagBindingService;
import cn.bctools.document.service.DcTagService;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Slf4j
@Service
@AllArgsConstructor
public class DcTagBindingServiceImpl extends ServiceImpl<DcTagBindingMapper, DcTagBinding> implements DcTagBindingService {

    private final DcTagService dcTagService;

    @Override
    public String getTagNameByDcId(String dcId) {
        try {
            List<DcTagBinding> list = this.list(Wrappers.<DcTagBinding>lambdaQuery().eq(DcTagBinding::getDcId, dcId));
            if (CollectionUtil.isNotEmpty(list)) {
                List<String> tagIds = list.stream().map(DcTagBinding::getTagId).collect(Collectors.toList());

                List<String> tagNames = dcTagService.listByIds(tagIds).stream().map(DcTag::getTagName).collect(Collectors.toList());
                return tagNames.parallelStream().collect(Collectors.joining(" "));
            }
        } catch (Exception e) {
            log.info("查询标签报错");
            e.printStackTrace();
        }
        return null;
    }
}
