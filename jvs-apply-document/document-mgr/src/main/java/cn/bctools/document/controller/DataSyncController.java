package cn.bctools.document.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.document.component.UserComponent;
import cn.bctools.document.constant.IndexConstant;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.mapper.DcLibraryMapper;
import cn.bctools.document.office.OfficeContentUtil;
import cn.bctools.document.po.DocumentEsPo;
import cn.bctools.document.service.DocumentElasticService;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 数据同步
 */

@Slf4j
@Api(tags = "数据同步")
@RestController
@RequestMapping(value = "/dcLibrary/sync")
@AllArgsConstructor
public class DataSyncController {

    DocumentElasticService documentElasticService;
    ElasticsearchRestTemplate esTemplate;
    DcLibraryMapper libraryMapper;
    OssTemplate ossTemplate;
    UserComponent userComponent;

    @Async
    @ApiOperation(value = "全量同步知识库数据到ES[document_base_info索引]", notes = "索引名[document_base_info]")
    @PostMapping("/full/es")
    public R<String> fullSyncDocumentToElastic() {
        Integer count = 0;
        log.info("开始全量同步索引document_base_info");
        // 查询所有租户的知识库信息，所以要先清空tenantId
        TenantContextHolder.clear();

        // 每页同步数量
        final int size = 1000;
        // 页码
        int current = 0;
        Page<DcLibrary> batchPage = new Page<>(current, size);
        //删除所有数据重新同步
        NativeSearchQuery queryBuilder = new NativeSearchQueryBuilder().withQuery(new BoolQueryBuilder()).build();
        esTemplate.delete(queryBuilder, DocumentEsPo.class, IndexConstant.INDEX_DOCUMENT_BASE_INFO);
        // 分批同步全量知识库文档信息
        while (current == 0 || batchPage.hasNext()) {
            try {
                // 分页查询
                current += 1;
                batchPage.setCurrent(current);
                batchPage = libraryMapper.selectPage(batchPage, Wrappers.<DcLibrary>lambdaQuery()
                        .orderByAsc(DcLibrary::getCreateTime));
                List<DcLibrary> dcLibraries = batchPage.getRecords();
                log.info("全量同步索引document_base_info，总批次：{}, 当前批次：{}, 每批数量：{}", batchPage.getPages(), batchPage.getCurrent(), batchPage.getSize());
                if (CollectionUtils.isEmpty(dcLibraries)) {
                    break;
                }
                // 查询知识库信息
                Set<String> knowledgeIds = dcLibraries.stream().map(DcLibrary::getKnowledgeId).collect(Collectors.toSet());
                Map<String, DcLibrary> knowledgeMap = libraryMapper.selectList(Wrappers.<DcLibrary>lambdaQuery()
                        .in(DcLibrary::getId, knowledgeIds)).stream().collect(Collectors.toMap(DcLibrary::getId, Function.identity()));

                // es数据集合
                List<DocumentEsPo> documentEsPos = new ArrayList<>();

                List<DcLibraryTypeEnum> typeEnums = Arrays.asList(DcLibraryTypeEnum.document_html, DcLibraryTypeEnum.md, DcLibraryTypeEnum.xmind, DcLibraryTypeEnum.document_xlsx);
                // 遍历知识库各类文档信息，封装为es索引对象
                for (DcLibrary dcLibrary : dcLibraries) {
                    // 获取文档内容。 只获取类型为document_html的内容
                    String content = "";
                    try {
                        if (typeEnums.contains(dcLibrary.getType())) {
                            if (StringUtils.isBlank(dcLibrary.getFilePath())) {
                                continue;
                            }
                            String url = ossTemplate.fileLink(dcLibrary.getFilePath(), dcLibrary.getBucketName());
                            byte[] bytes = HttpUtil.downloadBytes(url);
                            content = bytes == null ? null : ObjectUtil.deserialize(bytes);
                        }
                        //是否为office数据
                        if (DcLibraryTypeEnum.isOffice(dcLibrary.getType())) {
                            log.info("开始获取office内容:{},", JSONUtil.toJsonStr(dcLibrary));
                            content = OfficeContentUtil.getOfficeContent(FileUtil.getSuffix(dcLibrary.getFilePath()), new URL(ossTemplate.fileLink(dcLibrary.getFilePath(), dcLibrary.getBucketName())).openStream(), Boolean.FALSE);
                        }
                    } catch (Exception e) {
                        log.info("异步入es,获取内容错误,文档数据:{}", JSONObject.toJSONString(dcLibrary), e);
                    }
                    // 用户信息
                    UserDto userDto = new UserDto();
                    userDto.setId(dcLibrary.getCreateById());
                    userDto.setRealName(dcLibrary.getCreateBy());
                    DcLibrary knowledge = knowledgeMap.get(dcLibrary.getKnowledgeId());
                    if (Optional.ofNullable(knowledge).isPresent()) {
                        DocumentEsPo documentEsPo = documentElasticService.build(userDto, dcLibrary, content, knowledge);
                        documentEsPos.add(documentEsPo);
                    }
                }
                if (!documentEsPos.isEmpty()) {
                    int size1 = documentEsPos.size();
                    count += size1;
                    log.info("es入库，入库条数为:{}", size1);
                    esTemplate.save(documentEsPos);
                }
            } catch (Exception e) {
                log.error("同步异常。exception：", e);
            }
        }
        log.info("完成全量同步索引document_base_info,本次同步总条数为:{}", count);
        return R.ok();
    }


}
