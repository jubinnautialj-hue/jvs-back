package cn.bctools.document.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcLibraryComment;
import cn.bctools.document.service.DcLibraryCommentService;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.vo.DcLibraryCommentVo;
import cn.bctools.document.vo.req.DcCommentSaveReqVo;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @author xiaohui
 * @Desc 知识库-评论
 **/
@Slf4j
@Api(tags = "评论")
@RestController
@AllArgsConstructor
@RequestMapping("/dcLibrary/comment")
public class DcLibraryCommentController {

    private final DcLibraryCommentService service;
    private final DcLibraryService dcLibraryService;

    @Log
    @ApiOperation("分页")
    @GetMapping("/page/{knowledgeId}")
    public R<Page<DcLibraryCommentVo>> page(Page<DcLibraryComment> page,
                                            @ApiParam(value = "知识库id", required = true) @PathVariable("knowledgeId") String knowledgeId,
                                            @ApiParam("上级留言id") String parentId) {
        Page<DcLibraryCommentVo> servicePage = service.getPage(page, knowledgeId, parentId);
        return R.ok(servicePage);
    }

    @ApiOperation("新增")
    @PostMapping("/save")
    public R<DcLibraryComment> save(@RequestBody @Validated DcCommentSaveReqVo reqVo) {
        // 判断是否可评论
        DcLibrary dcLibrary = Optional.ofNullable(dcLibraryService.getById(reqVo.getKnowledgeId())).orElseThrow(() -> new BusinessException("评论失败"));
        if (Boolean.FALSE.equals(dcLibrary.getCommentable())) {
            throw new BusinessException("未开启评论");
        }
        // 保存评论
        UserDto userDto = UserCurrentUtils.getCurrentUser();
        DcLibraryComment dto = new DcLibraryComment().setKnowledgeId(reqVo.getKnowledgeId()).setMessage(reqVo.getMessage()).setParentId(reqVo.getParentId());
        dto.setName(userDto.getRealName());
        dto.setUserId(userDto.getId());
        service.save(dto);
        return R.ok(dto);
    }

    @ApiOperation("删除")
    @DeleteMapping("/del/{id}")
    public R<Boolean> remove(@PathVariable("id") String id) {
        boolean data = service.removeById(id);
        return R.ok(data);
    }

}
