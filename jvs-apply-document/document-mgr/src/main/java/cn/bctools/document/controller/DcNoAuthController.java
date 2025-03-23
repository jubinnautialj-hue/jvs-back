package cn.bctools.document.controller;


import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.document.dto.TreeAuthDto;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcLibraryComment;
import cn.bctools.document.entity.DcShareEntity;
import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.log.DocumentLog;
import cn.bctools.document.service.DcLibraryCommentService;
import cn.bctools.document.service.DcLibraryLogService;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.DcShareService;
import cn.bctools.document.vo.DcLibraryCommentVo;
import cn.bctools.document.vo.req.ShareCheckReqVo;
import cn.bctools.document.vo.res.PreviewDocumentResVo;
import cn.bctools.document.vo.res.ShareCheckResVo;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@Slf4j
@Api(tags = "不需要授权请求的接口")
@RestController
@RequestMapping(value = "/no/auth")
@AllArgsConstructor
public class DcNoAuthController {
    private final DcLibraryService dcLibraryService;
    private final DcShareService dcShareService;
    private final DcLibraryCommentService dcLibraryCommentService;
    private final OssTemplate ossTemplate;
    private final DcLibraryLogService dcLibraryLogService;
    private final static String REDIRECT_URL = "detail?{}";


    @Log
    @ApiOperation(value = "短链接转发")
    @GetMapping("/link/{url}")
    @SneakyThrows
    public R link(@PathVariable String url) {
        url = StrUtil.trim(url);
        if (url.length() < 6) {
            return R.failed("地址无效请确认是否正确");
        }
        DcShareEntity dcShareEntity = dcShareService.getOne(new LambdaQueryWrapper<DcShareEntity>().likeRight(DcShareEntity::getShareLink, url).last("limit 1"));
        if (ObjectUtil.isNull(dcShareEntity)) {
            return R.failed("此分享已经失效请联系文档分享者");
        }
        if (!dcShareEntity.getShare()) {
            return R.failed("分享链接已经被取消");
        }
        String urlLink = StrUtil.format(REDIRECT_URL, dcShareEntity.getShareLinkOriginal());
        return R.ok(urlLink);
    }


    @Log
    @GetMapping("/tree")
    @ApiOperation("获取公网知识库")
    public R<TreeAuthDto> list(@RequestParam String id) {
        log.info("-------------------------收到的值为:{}", id);
        TenantContextHolder.clear();
        dcShareService.checkShare(id,Boolean.FALSE);
        DcLibrary byId = dcLibraryService.getById(id);
        TreeAuthDto treeAuthDto;
        List<DcLibrary> pathList = new ArrayList<>();
        //获取数据
        List<DcLibrary> list = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>().eq(DcLibrary::getParentId, id).orderByDesc(DcLibrary::getOrderId));
        if (!byId.getPathId().isEmpty()) {
            pathList = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>().select(DcLibrary::getId, DcLibrary::getName, DcLibrary::getType, DcLibrary::getPathId).in(DcLibrary::getId, byId.getPathId()))
                    .stream().sorted(Comparator.comparingInt(e -> e.getPathId().size())).collect(Collectors.toList());
        }
        list.stream().filter(e -> e.getType().equals(DcLibraryTypeEnum.directory)).peek(e -> e.setChildren(Arrays.asList(new DcLibrary().setId("-1")))).collect(Collectors.toList());
        //获取当前目录路径
        treeAuthDto = new TreeAuthDto().setDirectoryStructure(pathList).setData(list).setDcLibrary(byId);
        return R.ok(treeAuthDto);
    }

    @Log
    @ApiOperation("下载文件")
    @GetMapping("/get/file/{id}")
    public void getFile(@PathVariable String id) {
        TenantContextHolder.clear();
        dcShareService.checkShare(id,Boolean.TRUE);
        dcLibraryService.getFile(id);
    }

    @Log(back = false)
    @ApiOperation("下载文件选择下载的文件类型")
    @GetMapping("/down/{type}/{id}")
    @SneakyThrows
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.DOWNLOAD)
    public R down(@PathVariable String id, @ApiParam(value = "下载的类型，docx,pdf", required = true) @PathVariable String type) {
        TenantContextHolder.clear();
        dcShareService.checkShare(id,Boolean.TRUE);
        String url = dcLibraryService.downOffice(id, type);
        return R.ok(url);
    }

    @Log
    @GetMapping("/dcLibrary/info/{id}")
    @ApiOperation("获取文库信息")
    public R<DcLibrary> dcLibraryInfo(@PathVariable String id) {
        DcLibrary dcLibrary = dcLibraryService.getById(id);
        return R.ok(dcLibrary);
    }

    @Log
    @PostMapping("/event/report")
    @ApiOperation("cs")
    public R eventReport(@RequestBody String jsonObject) {
        log.info("入参为{}:", jsonObject);
        return R.ok();
    }


    @Log
    @ApiOperation("查询指定知识库目录下(包含所有下级节点)文件总数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "目录id", required = true)
    })
    @GetMapping("/statistics/document/total/{id}")
    public R<Integer> searchLibraryDocumentTotal(@PathVariable String id) {
        Integer count = 0;
        Set<String> docIds = dcLibraryService.getAllChildDocumentId(id);
        if (CollectionUtils.isEmpty(docIds)) {
            return R.ok(count);
        }
        return R.ok(docIds.size());
    }

    @Log
    @ApiOperation("查询文档下载次数与浏览次数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "目录i d或者知识库id", required = true)
    })
    @GetMapping("/statistics/{id}")
    public R<Dict> getCount(@PathVariable String id) {
        Dict dict = dcLibraryLogService.groupByCount(id);
        return R.ok(dict);
    }

    @Log
    @ApiOperation("查询指定知识库目录下(包含所有下级节点)文件总大小")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "目录id -1查询整个数据", required = true)
    })
    @GetMapping("/statistics/document/file/size/{id}")
    public R<String> getFileSize(@PathVariable String id) {
        LambdaQueryWrapper<DcLibrary> queryWrapper = new LambdaQueryWrapper<>();
        if (!id.equals(String.valueOf(BigDecimal.ROUND_UP))) {
            DcLibrary dcLibrary = dcLibraryService.getById(id);
            if (dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge)) {
                queryWrapper.eq(DcLibrary::getKnowledgeId, id);
            } else {
                queryWrapper.eq(DcLibrary::getParentId, id);
            }
        }
        List<DcLibrary> list = dcLibraryService.list(queryWrapper);
        if (!list.isEmpty()) {
            Long sum = list.stream()
                    .filter(e -> ObjectUtil.isNotNull(e.getSize()) && e.getSize() > 0L)
                    //todo 防止数据加起来超过了long的最大值 所以用BigDecimal 这种情况的可能性不是很大
//                    .map(e -> new BigDecimal(e.toString()))
                    .mapToLong(DcLibrary::getSize)
                    .sum();
            if (sum == 0L) {
                return R.ok("0KB");
            }
            StringBuffer stringBuffer = new StringBuffer();
            DecimalFormat format = new DecimalFormat("###.0");
            if (sum >= 1024 * 1024 * 1024) {
                double i = (sum / (1024 * 1024 * 1024));
                stringBuffer.append(format.format(i)).append("GB");
            } else if (sum >= 1024 * 1024) {
                double i = (sum / (1024 * 1024));
                stringBuffer.append(format.format(i)).append("MB");
            } else if (sum >= 1024) {
                long i = sum / 1024;
                stringBuffer.append(format.format(i)).append("KB");
            } else {
                stringBuffer.append("0kb");
            }
            return R.ok(stringBuffer.toString());
        }
        return R.ok("0KB");
    }

    @Log
    @GetMapping("/search/{id}")
    @ApiOperation("搜索某个指定文库或者文件夹的数据-用于全网文库与分享的文件搜索文件功能")
    public R<DcLibrary> search(@PathVariable String id) {
        DcLibrary dcLibrary = dcLibraryService.getById(id);
        if (ObjectUtil.isNull(dcLibrary)) {
            R.failed("未找到此文库信息");
        }
        if (dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge) && !dcLibrary.getShareRole().equals(DcLibraryReadEnum.all)) {
            R.failed("搜索只支持全网文库与分享的文件夹");
        }
        if (dcLibrary.getType().equals(DcLibraryTypeEnum.directory)) {
            //判断是否存在文件夹分享
            List<String> pathId = dcLibrary.getPathId();
            //添加本身 防止本身为分享主体
            pathId.add(dcLibrary.getId());
            long count = dcShareService.count(new LambdaQueryWrapper<DcShareEntity>().in(DcShareEntity::getDcId, pathId));
            if (count == 0) {
                return R.failed("此文件夹未分享");
            }
        }


        return R.ok(dcLibrary);
    }

    @Log
    @ApiOperation(value = "校验分享链接")
    @PostMapping("/dcLibrary/share/check")
    public R<ShareCheckResVo> share(@RequestBody @Validated ShareCheckReqVo reqVo) {
        if (StringUtils.isBlank(reqVo.getKey())) {
            throw new BusinessException("分享链接key不能为空");
        }
        return R.ok(dcShareService.checkShare(reqVo));
    }


    @SneakyThrows
    @Log(back = false)
    @GetMapping("/document/preview/{id}")
    @ApiOperation("预览文档")
    public R<PreviewDocumentResVo> preview(@PathVariable("id") String id) {
        DcLibrary byId = dcLibraryService.getById(id);
        if (byId == null) {
            return R.failed("未找到此数据");
        }
        DcLibrary dcLibrary = dcLibraryService.getById(byId.getKnowledgeId());
        DcShareEntity shareEntity = dcShareService.getOne(new LambdaQueryWrapper<DcShareEntity>().eq(DcShareEntity::getDcId, id));
        if (!dcLibrary.getShareRole().equals(DcLibraryReadEnum.all)) {
            //判断是否存在上级文件夹 分享
            //判断是否存在文件夹分享
            List<String> pathId = byId.getPathId();
            long count = dcShareService.count(new LambdaQueryWrapper<DcShareEntity>().in(DcShareEntity::getDcId, pathId));
            if (count == 0) {
                if (ObjectUtil.isNull(shareEntity) || !shareEntity.getShare()) {
                    return R.failed("权限不足请联系文档所有者");
                }
            }
        }
        return R.ok(dcLibraryService.preview(id, null));
    }

    @Log
    @ApiOperation("获取文件url")
    @GetMapping("/get/url/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "数据id")
    })
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.SEE)
    public R<Dict> getUrl(@PathVariable String id) {
        TenantContextHolder.clear();
        DcLibrary byId = dcLibraryService.getById(id);
        if (byId == null) {
            return R.failed("未找到此数据");
        }
        DcLibrary dcLibrary = dcLibraryService.getById(byId.getKnowledgeId());
        DcShareEntity shareEntity = dcShareService.getOne(new LambdaQueryWrapper<DcShareEntity>().eq(DcShareEntity::getDcId, id));
        if (!dcLibrary.getShareRole().equals(DcLibraryReadEnum.all)) {
            //判断是否存在上级文件夹 分享
            //判断是否存在文件夹分享
            List<String> pathId = byId.getPathId();
            long count = dcShareService.count(new LambdaQueryWrapper<DcShareEntity>().in(DcShareEntity::getDcId, pathId));
            if (count == 0) {
                if (ObjectUtil.isNull(shareEntity) || !shareEntity.getShare()) {
                    return R.failed("权限不足请联系文档所有者");
                }
            }
        }
        Dict dict = Dict.create();
        String s = ossTemplate.fileLink(byId.getFilePath(), byId.getBucketName());
        dict.put("url", URLUtil.decode(s));
        String key = byId.getBucketName() + byId.getFilePath();
        key = SecureUtil.md5(key);
        if (StrUtil.isNotBlank(key)) {
            //key值不不能大于20
            if (key.length() > 20) {
                key = Integer.toString(key.hashCode());
            }
            key = key.replace("[^0-9-.a-zA-Z_=]", "_");
            key = key.substring(0, Math.min(key.length(), 20));
            dict.put("key", key);
        }
        return R.ok(dict);
    }

    @Log
    @ApiOperation("分页")
    @GetMapping("/dcLibrary/comment/page/{knowledgeId}")
    public R<Page<DcLibraryCommentVo>> page(Page<DcLibraryComment> page,
                                            @ApiParam(value = "知识库id", required = true) @PathVariable("knowledgeId") String knowledgeId,
                                            @ApiParam("上级留言id") String parentId) {
        Page<DcLibraryCommentVo> servicePage = dcLibraryCommentService.getPage(page, knowledgeId, parentId);
        return R.ok(servicePage);
    }

}
