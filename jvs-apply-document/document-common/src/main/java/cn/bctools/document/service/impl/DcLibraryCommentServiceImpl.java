package cn.bctools.document.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.document.component.UserComponent;
import cn.bctools.document.entity.DcLibraryComment;
import cn.bctools.document.entity.DcLibraryLike;
import cn.bctools.document.entity.enums.DcLibraryLikeTypeEnum;
import cn.bctools.document.mapper.DcLibraryCommentMapper;
import cn.bctools.document.service.DcLibraryCommentService;
import cn.bctools.document.service.DcLibraryLikeService;
import cn.bctools.document.vo.DcLibraryCommentVo;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author wenxin
 * @Desc
 **/
@Service
@AllArgsConstructor
public class DcLibraryCommentServiceImpl extends ServiceImpl<DcLibraryCommentMapper, DcLibraryComment> implements DcLibraryCommentService {
    private final DcLibraryLikeService dcLibraryLikeService;
    private final UserComponent userComponent;

    @Override
    public Page<DcLibraryCommentVo> getPage(Page<DcLibraryComment> page, String knowledgeId, String parentId) {
        // 分页查询
        this.page(page, Wrappers.<DcLibraryComment>lambdaQuery()
                .eq(DcLibraryComment::getKnowledgeId, knowledgeId)
                .isNull(StrUtil.isEmpty(parentId), DcLibraryComment::getParentId)
                .eq(StrUtil.isNotEmpty(parentId), DcLibraryComment::getParentId, parentId)
                .orderByDesc(DcLibraryComment::getCreateTime)
        );

        // 封装响应
        List<String> userIds = new ArrayList<>();
        UserDto nullableUser = UserCurrentUtils.getNullableUser();
        List<DcLibraryCommentVo> result = page.getRecords().stream().map(e -> {
            DcLibraryCommentVo vo = BeanUtil.copyProperties(e, DcLibraryCommentVo.class);
            userIds.add(vo.getUserId());
            if (this.lambdaQuery().eq(DcLibraryComment::getKnowledgeId, e.getKnowledgeId()).eq(DcLibraryComment::getParentId, e.getId()).count() >= 1) {
                vo.setChildren(true);
            }
            // 封装点赞信息
            LambdaQueryWrapper<DcLibraryLike> likeCountQuery = Wrappers.<DcLibraryLike>lambdaQuery()
                    .eq(DcLibraryLike::getBizResourceId, e.getId())
                    .eq(DcLibraryLike::getBizType, DcLibraryLikeTypeEnum.COMMENT);
            List<DcLibraryLike> likes = dcLibraryLikeService.list(likeCountQuery);
            vo.setLikeTotal(likes.size());
            vo.setCurrentUserLike(likes.stream().anyMatch(l -> ObjectUtil.isNotNull(nullableUser) && l.getUserId().equals(nullableUser.getId())));
            return vo;
        }).collect(Collectors.toList());

        // 查询用户头像
        Map<String, UserDto> userMap = userComponent.getUserMap(userIds);
        result.stream().forEach(r -> r.setHeadImg(userMap.get(r.getUserId()).getHeadImg()));
        return new Page<DcLibraryCommentVo>().setTotal(page.getTotal()).setCurrent(page.getCurrent()).setSize(page.getSize()).setRecords(result);
    }
}
