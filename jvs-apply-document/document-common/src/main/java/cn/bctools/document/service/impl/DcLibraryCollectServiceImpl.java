package cn.bctools.document.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.document.entity.DcLibraryCollect;
import cn.bctools.document.mapper.DcLibraryCollectMapper;
import cn.bctools.document.service.DcLibraryCollectService;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库-收藏
 *
 * @Author: GuoZi
 */
@Service
@AllArgsConstructor
public class DcLibraryCollectServiceImpl extends ServiceImpl<DcLibraryCollectMapper, DcLibraryCollect> implements DcLibraryCollectService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> getCollection() {
        String userId = UserCurrentUtils.getUserId();
        List<DcLibraryCollect> list = this.list(Wrappers.<DcLibraryCollect>lambdaQuery()
                .select(DcLibraryCollect::getDocId, DcLibraryCollect::getCreateTime)
                .eq(DcLibraryCollect::getUserId, userId)
                .orderByDesc(DcLibraryCollect::getCreateTime));
        if (ObjectUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(DcLibraryCollect::getDocId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collect(String docId) {
        if (StringUtils.isBlank(docId)) {
            throw new BusinessException("文档id为空");
        }
        String userId = UserCurrentUtils.getUserId();
        long count = this.count(Wrappers.<DcLibraryCollect>lambdaQuery()
                .eq(DcLibraryCollect::getUserId, userId)
                .eq(DcLibraryCollect::getDocId, docId));
        if (count != 0) {
            // 取消收藏
            this.remove(Wrappers.<DcLibraryCollect>lambdaQuery()
                    .eq(DcLibraryCollect::getUserId, userId)
                    .eq(DcLibraryCollect::getDocId, docId));
            return false;
        }
        // 收藏
        DcLibraryCollect collect = new DcLibraryCollect();
        collect.setDocId(docId);
        collect.setUserId(userId);
        collect.setCreateTime(LocalDateTime.now());
        this.save(collect);
        return true;
    }

}
