package cn.bctools.document.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.utils.function.Get;
import cn.bctools.document.constant.IndexConstant;
import cn.bctools.document.entity.DcAuthConfig;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.mapper.DcLibraryMapper;
import cn.bctools.document.po.DocumentEsPo;
import cn.bctools.document.po.DocumentLogEsPo;
import cn.bctools.document.po.enums.DocumentLogTypeEnum;
import cn.bctools.document.service.DcAuthConfigService;
import cn.bctools.document.service.DcTagBindingService;
import cn.bctools.document.service.DocumentElasticService;
import cn.bctools.document.util.DcEsUtil;
import cn.bctools.document.util.HtmlUtil;
import cn.bctools.document.vo.req.DocumentEditLogVo;
import cn.bctools.document.vo.req.DocumentSearchVo;
import cn.bctools.document.vo.res.DocumentEditLogResVo;
import cn.bctools.document.vo.res.DocumentSearchResVo;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 知识库-文档es服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class DocumentElasticServiceImpl implements DocumentElasticService {

    private static final String DOC_HTML_KEY = "contentHtml";

    private final ElasticsearchRestTemplate esTemplate;
    private final DcLibraryMapper libraryMapper;
    private final DcTagBindingService dcTagBindingService;
    private final DcAuthConfigService dcAuthConfigService;


    @Override
    public Page<DocumentSearchResVo> searchDocNoAuth(Page page, DcLibrary dcLibrary) {
        Page<DocumentSearchResVo> resVoPage = new Page<>(page.getCurrent(), page.getSize());
        // 构造搜索条件
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        if (dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge)) {
            boolQuery.filter(QueryBuilders.termsQuery(Get.name(DocumentEsPo::getKnowledgeId), dcLibrary.getId()));
        } else {
            List<String> pathId = dcLibrary.getPathId();
            pathId.add(dcLibrary.getId());
            boolQuery.should(QueryBuilders.termsQuery(Get.name(DocumentEsPo::getPathId), pathId));
        }
        // 默认按匹配度降序排序
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of((int) (page.getCurrent() - 1), (int) page.getSize()))
                .withHighlightFields(new HighlightBuilder.Field(Get.name(DocumentEsPo::getName)), new HighlightBuilder.Field(Get.name(DocumentEsPo::getContent)));
        // 若没有关键词查询，则按修改时间倒序
        queryBuilder.withSort(new FieldSortBuilder(Get.name(DocumentEsPo::getUpdateTime)).order(SortOrder.DESC));
        NativeSearchQuery query = queryBuilder.build();
        // 执行搜索
        SearchHits<DocumentEsPo> searchHits = esTemplate.search(query, DocumentEsPo.class);
        SearchPage<DocumentEsPo> searchPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
        // 4. 构造返回
        List<DocumentSearchResVo> resDatas = new ArrayList<>();
        long total = Optional.of(searchPage.getContent().size()).orElse(0) == 0 ? 0 : searchPage.getTotalElements();
        resVoPage = new Page<>(page.getCurrent(), page.getSize(), total);
        for (SearchHit<DocumentEsPo> documentEsPoSearchHit : searchPage.getContent()) {
            DocumentSearchResVo resVo = new DocumentSearchResVo();
            BeanUtils.copyProperties(documentEsPoSearchHit.getContent(), resVo);
            // 若有高亮数据，则返回高亮数据
            List<String> highNames = documentEsPoSearchHit.getHighlightFields().get(Get.name(DocumentEsPo::getName));
            List<String> highContents = documentEsPoSearchHit.getHighlightFields().get(Get.name(DocumentEsPo::getContent));
            resVo.setName(CollectionUtils.isEmpty(highNames) ? resVo.getName() : highNames.get(0));
            String content = CollectionUtils.isEmpty(highContents) ? resVo.getContent() : highContents.get(0);
            if (content != null && content.length() > 200) {
                content = content.substring(0, 200);
            }
            resVo.setContent(content);
            resDatas.add(resVo);
        }
        resVoPage.setRecords(resDatas);
        return resVoPage;
    }

    @Override
    public Page<DocumentSearchResVo> searchDoc(Page page, DocumentSearchVo documentSearchVo) {
        try {
            Page<DocumentSearchResVo> resVoPage = new Page<>(page.getCurrent(), page.getSize());
            // 1. 获取查询的知识库类型，用以限定搜索范围。 目前只支持查询知识库|目录下的文档
            // 搜索知识库范围（未指定知识库时为空， 可指定知识库 | 目录）
            DcLibraryTypeEnum dcType = null;
            if (StringUtils.isNotBlank(documentSearchVo.getKnowledgeId())) {
                DcLibrary dcLibrary = libraryMapper.selectOne(Wrappers.<DcLibrary>lambdaQuery()
                        .eq(DcLibrary::getId, documentSearchVo.getKnowledgeId())
                        .select(DcLibrary::getType));
                if (dcLibrary == null) {
                    return resVoPage;
                }
                dcType = dcLibrary.getType();
                if (!DcLibraryTypeEnum.knowledge.equals(dcType) && !DcLibraryTypeEnum.directory.equals(dcType)) {
                    return resVoPage;
                }
            }
            List<DcAuthConfig> configById = new ArrayList<>();
            if (Optional.ofNullable(documentSearchVo.getShareRole()).orElse(DcLibraryReadEnum.user).equals(DcLibraryReadEnum.user)) {
                //获取设置了权限的数据
                configById = dcAuthConfigService.getAuthConfigById(new ArrayList<>());
            }
            List<String> list = new ArrayList<>();
            if (!Optional.ofNullable(documentSearchVo.getShareRole()).orElse(DcLibraryReadEnum.all).equals(DcLibraryReadEnum.user)) {
                //获取全网与全平台开发的知识库
                list = libraryMapper.selectList(new LambdaQueryWrapper<DcLibrary>()
                                .select(DcLibrary::getId)
                                .eq(DcLibrary::getType, DcLibraryTypeEnum.knowledge)
                                .ne(ObjectUtil.isNull(documentSearchVo.getShareRole()) || documentSearchVo.getShareRole().equals(DcLibraryReadEnum.user), DcLibrary::getShareRole, DcLibraryReadEnum.user)
                                .eq(ObjectUtil.isNotNull(documentSearchVo.getShareRole()) && !documentSearchVo.getShareRole().equals(DcLibraryReadEnum.user), DcLibrary::getShareRole, documentSearchVo.getShareRole()))
                        .stream().map(DcLibrary::getId).collect(Collectors.toList());
            }
            if (list.isEmpty() && configById.isEmpty()) {
                return resVoPage;
            }
            log.info("权限数据:{},文库数据{}",JSONObject.toJSONString(configById),JSONObject.toJSONString(list));
            // 3. 搜索
            SearchPage<DocumentEsPo> searchPage = searchDocEsQuery(page, documentSearchVo, dcType, configById, list);

            // 4. 构造返回
            List<DocumentSearchResVo> resDatas = new ArrayList<>();
            long total = Optional.ofNullable(searchPage.getContent().size()).orElse(0) == 0 ? 0 : searchPage.getTotalElements();
            resVoPage = new Page<>(page.getCurrent(), page.getSize(), total);
            for (SearchHit<DocumentEsPo> documentEsPoSearchHit : searchPage.getContent()) {
                DocumentSearchResVo resVo = new DocumentSearchResVo();
                BeanUtils.copyProperties(documentEsPoSearchHit.getContent(), resVo);
                // 若有高亮数据，则返回高亮数据
                List<String> highNames = documentEsPoSearchHit.getHighlightFields().get(Get.name(DocumentEsPo::getName));
                List<String> highContents = documentEsPoSearchHit.getHighlightFields().get(Get.name(DocumentEsPo::getContent));
                resVo.setName(CollectionUtils.isEmpty(highNames) ? resVo.getName() : highNames.get(0));
                String content = CollectionUtils.isEmpty(highContents) ? resVo.getContent() : highContents.get(0);
                if (content != null && content.length() > 200) {
                    content = content.substring(0, 200);
                }
                resVo.setContent(content);
                resDatas.add(resVo);
            }
            resVoPage.setRecords(resDatas);
            return resVoPage;
        } catch (Exception e) {
            log.error("ES搜索失败. exception: {}", e);
            throw new BusinessException("查询文档失败");
        }
    }


    /**
     * ES搜索文档
     *
     * @param page             分页
     * @param documentSearchVo 查询入参
     * @param dcType           查询的类型
     * @param configList       权限数据
     * @param openIds          公开的知识库
     * @return 查询结果
     */
    private SearchPage<DocumentEsPo> searchDocEsQuery(Page page, DocumentSearchVo documentSearchVo, DcLibraryTypeEnum dcType, List<DcAuthConfig> configList, List<String> openIds) {
        // 构造搜索条件
        // 指定知识库查询， 若无权限，直接返回，若有权限，则只查询指定知识库
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        //过滤权限为文库的
        List<String> authConfigs = configList.stream().filter(e -> e.getType().equals(DcLibraryTypeEnum.knowledge)).map(DcAuthConfig::getDcId).collect(Collectors.toList());
        openIds.addAll(authConfigs);
        //过滤特殊权限
        configList = configList.stream().filter(e -> !e.getType().equals(DcLibraryTypeEnum.knowledge)).collect(Collectors.toList());
        //指定查询的库
        if (StrUtil.isNotBlank(documentSearchVo.getKnowledgeId())) {
            openIds = openIds.stream().filter(e -> e.equals(documentSearchVo.getKnowledgeId())).collect(Collectors.toList());
            configList = configList.stream().filter(e -> e.getKnowledgeId().equals(documentSearchVo.getKnowledgeId())).collect(Collectors.toList());
        }
        boolQuery.should(QueryBuilders.termsQuery(Get.name(DocumentEsPo::getKnowledgeId), openIds));
        //文件类型的数据
        List<String> fileIds = configList.stream().filter(e -> !e.getType().equals(DcLibraryTypeEnum.directory)).map(DcAuthConfig::getDcId).collect(Collectors.toList());
        if (!fileIds.isEmpty()) {
            boolQuery.should(QueryBuilders.termsQuery(Get.name(DocumentEsPo::getDocId), fileIds));
        }
        //文件夹
        configList = configList.stream().filter(e -> e.getType().equals(DcLibraryTypeEnum.directory)).collect(Collectors.toList());
        if (!configList.isEmpty()) {
            //添加设置了文件夹与文件权限的数据
            configList.forEach(e -> {
                List<String> pathId = e.getPathId();
                pathId.add(e.getDcId());
                boolQuery.should(QueryBuilders.prefixQuery("pathId.keyword", String.join("/", pathId)));
            });
        }
        boolQuery.must(QueryBuilders.matchQuery(Get.name(DocumentEsPo::getTenantId), TenantContextHolder.getTenantId()));
        if (ObjectUtil.isNotNull(dcType) && DcLibraryTypeEnum.directory == dcType) {
            boolQuery.filter(QueryBuilders.termQuery(Get.name(DocumentEsPo::getParentId), documentSearchVo.getKnowledgeId()));
        }
        if (ObjectUtil.isNotNull(documentSearchVo.getIsNotNull()) && documentSearchVo.getIsNotNull()) {
            //过滤为null
            boolQuery.must(QueryBuilders.boolQuery().must(QueryBuilders.existsQuery(Get.name(DocumentEsPo::getContent))));
        }
        // 最近修改时间
        if (documentSearchVo.getTimeScope() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery(Get.name(DocumentEsPo::getUpdateTime)).gte(documentSearchVo.getTimeScope().getExpression()));
        }
//        if (documentSearchVo.getShareRole() != null) {
//            boolQuery.filter(QueryBuilders.matchQuery("shareRole.keyword", documentSearchVo.getShareRole().getValue()));
//        }
        // 文档类型搜索
        if (documentSearchVo.getType() != null) {
            boolQuery.filter(QueryBuilders.termsQuery(Get.name(DocumentEsPo::getType), documentSearchVo.getType().getValue()));
        }
        BoolQueryBuilder keyWorkQuery = new BoolQueryBuilder();
        // 关键字搜索
        if (StringUtils.isNotBlank(documentSearchVo.getKeyword())) {
            //分词相隔的数量
            Integer slop = 0;
            if (ObjectUtil.isNotNull(documentSearchVo.getSearchType()) && documentSearchVo.getSearchType() == BigDecimal.ROUND_DOWN) {
                slop = 20;
            }
            if (ObjectUtil.isNull(documentSearchVo.getSearchContentType()) || documentSearchVo.getSearchContentType() == BigDecimal.ROUND_DOWN) {
                //查询标题
                keyWorkQuery.should(QueryBuilders.matchPhraseQuery(Get.name(DocumentEsPo::getName), documentSearchVo.getKeyword()).slop(slop));
            }
            if (ObjectUtil.isNull(documentSearchVo.getSearchContentType()) || documentSearchVo.getSearchContentType() == BigDecimal.ROUND_CEILING) {
                //查询内容
                keyWorkQuery.should(QueryBuilders.matchPhraseQuery(Get.name(DocumentEsPo::getContent), documentSearchVo.getKeyword()).slop(slop));
            }
        }
        boolQuery.minimumShouldMatch(1);
        //标签
        if (StringUtils.isNotBlank(documentSearchVo.getTagName())) {
            boolQuery.must(QueryBuilders.matchQuery(Get.name(DocumentEsPo::getTagName), documentSearchVo.getTagName()).operator(Operator.AND));
        }
        boolQuery.must(keyWorkQuery);
        // 搜索排除类型为“知识库”和“目录”的数据
        boolQuery.mustNot(QueryBuilders.matchQuery(Get.name(DocumentEsPo::getType), DcLibraryTypeEnum.knowledge));
        boolQuery.mustNot(QueryBuilders.matchQuery(Get.name(DocumentEsPo::getType), DcLibraryTypeEnum.directory));
        //回收站的数据不进行查询
        boolQuery.mustNot(QueryBuilders.matchQuery(Get.name(DocumentEsPo::getIsDelete), Boolean.TRUE));


        // 默认按匹配度降序排序
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder().withQuery(boolQuery).withPageable(PageRequest.of((int) (page.getCurrent() - 1), (int) page.getSize())).withHighlightFields(new HighlightBuilder.Field(Get.name(DocumentEsPo::getName)), new HighlightBuilder.Field(Get.name(DocumentEsPo::getContent)));
        // 若没有关键词查询，则按修改时间倒序
        if (StringUtils.isBlank(documentSearchVo.getKeyword())) {
            queryBuilder.withSort(new FieldSortBuilder(Get.name(DocumentEsPo::getUpdateTime)).order(SortOrder.DESC));
        }

        NativeSearchQuery query = queryBuilder.build();
        // 执行搜索
        SearchHits<DocumentEsPo> searchHits = esTemplate.search(query, DocumentEsPo.class);
        return SearchHitSupport.searchPageFor(searchHits, query.getPageable());
    }

    @Override
    public UpdateResponse update(DcLibrary dcLibrary, Document document) {
        log.info("传入的数据为:{}", JSONObject.toJSONString(dcLibrary));
        //先判断es中是否存在 此数据如果不存在 直接返回成功
        DocumentEsPo documentEsPo = esTemplate.get(DcEsUtil.buildEsId(dcLibrary.getTenantId(), dcLibrary.getId()), DocumentEsPo.class);
        if (ObjectUtil.isNull(documentEsPo)) {
            return new UpdateResponse(UpdateResponse.Result.UPDATED);
        }
        return esTemplate.update(UpdateQuery.builder(DcEsUtil.buildEsId(dcLibrary.getTenantId(), dcLibrary.getId())).withDocument(document).build(), IndexConstant.INDEX_DOCUMENT_BASE_INFO);
    }

    @Override
    public void save(UserDto userDto, DcLibrary dcLibrary, String content) {
        try {
            DocumentEsPo esPo = getCreatePo(userDto, dcLibrary, content);
            esTemplate.save(esPo);
        } catch (Exception e) {
            log.error("ES保存文档失败. exception: {}", e);
            throw new BusinessException("ES异常");
        }
    }

    /**
     * 得到要创建的文档信息
     *
     * @param userDto   创建文档的用户信息
     * @param dcLibrary 文档信息
     * @param content   内容
     * @return
     */
    private DocumentEsPo getCreatePo(UserDto userDto, DcLibrary dcLibrary, String content) {
        String conditionId = DcLibraryTypeEnum.knowledge.equals(dcLibrary.getType()) ? dcLibrary.getId() : dcLibrary.getKnowledgeId();
        DcLibrary knowledge = libraryMapper.selectById(conditionId);
        return build(userDto, dcLibrary, content, knowledge);
    }

    /**
     * 封装documentEsPO 入库信息
     *
     * @param userDto   用户
     * @param dcLibrary 知识库文档信息
     * @param content   文档内容
     * @param knowledge 知识库
     * @return
     */
    @Override
    public DocumentEsPo build(UserDto userDto, DcLibrary dcLibrary, String content, DcLibrary knowledge) {
        DocumentEsPo documentEsPo = new DocumentEsPo();
        documentEsPo.setId(buildDocumentEsId(dcLibrary.getTenantId(), dcLibrary.getId()));
        documentEsPo.setDocId(dcLibrary.getId());
        documentEsPo.setIsDelete(Boolean.FALSE);
        documentEsPo.setType(dcLibrary.getType());
        documentEsPo.setShareRole(dcLibrary.getShareRole());
        documentEsPo.setTenantId(dcLibrary.getTenantId());
        documentEsPo.setName(dcLibrary.getName());
        // 存储纯文本内容(不包括html标签，不是JSON字符串)
        if (DcLibraryTypeEnum.document_html.equals(dcLibrary.getType()) && content != null) {
            content = HtmlUtil.replaceHtmlTag(JSONUtil.isJson(content) ? JSONUtil.parseObj(content).getStr(Get.name(DcLibrary::getContent)) : content, " ");
        }
        if (DcLibraryTypeEnum.md.equals(dcLibrary.getType()) && StrUtil.isNotBlank(content)) {
            content = JSONObject.parseObject(content).getString(DOC_HTML_KEY);
            if (StrUtil.isNotBlank(content)) {
                content = HtmlUtil.replaceHtmlTag(content, "");
            }
        }
        //去除前后的空字符串
        content = StrUtil.trim(content);
        if (StrUtil.isEmpty(content)) {
            content = null;
        }
        documentEsPo.setContent(content);
        documentEsPo.setPathId(String.join("/", dcLibrary.getPathId()));
        documentEsPo.setKnowledgeName(knowledge.getName());
        // 类型为knowledge的数据没有knowledgeId，就使用当前知识库id填充
        documentEsPo.setKnowledgeId(StringUtils.isEmpty(dcLibrary.getKnowledgeId()) ? dcLibrary.getId() : dcLibrary.getKnowledgeId());
        documentEsPo.setCreateTime(dcLibrary.getCreateTime());
        documentEsPo.setUpdateTime(LocalDateTimeUtil.parse(LocalDateTimeUtil.format(dcLibrary.getUpdateTime(), DatePattern.UTC_SIMPLE_PATTERN)));
        documentEsPo.setParentId(dcLibrary.getParentId());
        documentEsPo.setAuthorId(userDto.getId());
        documentEsPo.setAuthorName(userDto.getRealName());
        //设置标签
        String tagNameByDcId = dcTagBindingService.getTagNameByDcId(dcLibrary.getId());
        if (StrUtil.isNotBlank(tagNameByDcId)) {
            documentEsPo.setTagName(tagNameByDcId);
        }
        return documentEsPo;
    }

    /**
     * DocumentEs 索引id
     *
     * @param tenantId 租户id
     * @param dcId     知识库id
     * @return
     */
    private String buildDocumentEsId(String tenantId, String dcId) {
        return DcEsUtil.buildEsId(tenantId, dcId);
    }


    @Async
    @Override
    public void saveLog(DcLibrary dcLibrary, String realName, String userId, DocumentLogTypeEnum
            logTypeEnum, LocalDateTime time) {
        DcLibrary knowledge = null;
        // 不是知识库，则根据知识库id查询知识库信息
        if (ObjectUtil.isNotNull(dcLibrary.getType()) && !DcLibraryTypeEnum.knowledge.equals(dcLibrary.getType())) {
            knowledge = libraryMapper.selectById(dcLibrary.getKnowledgeId());
        }

        DocumentLogEsPo documentLogEsPo = new DocumentLogEsPo();
        documentLogEsPo.setId(DcEsUtil.buildEsId(TenantContextHolder.getTenantId(), dcLibrary.getId()) + "_" + time.toInstant(ZoneOffset.of("+8")).toEpochMilli());
        documentLogEsPo.setDocId(dcLibrary.getId());
        documentLogEsPo.setType(dcLibrary.getType());
        documentLogEsPo.setLogType(logTypeEnum);
        documentLogEsPo.setTenantId(TenantContextHolder.getTenantId());
        documentLogEsPo.setName(dcLibrary.getName());
        documentLogEsPo.setUserId(userId);
        documentLogEsPo.setCreateTime(time);
        documentLogEsPo.setUserName(realName);
        documentLogEsPo.setKnowledgeName(Optional.ofNullable(knowledge).orElse(dcLibrary).getName());
        documentLogEsPo.setKnowledgeId(dcLibrary.getKnowledgeId());
        esTemplate.save(documentLogEsPo);
    }

    @Override
    public Page<DocumentEditLogResVo> searchDocumentEditLog(Page page, DocumentEditLogVo documentEditLogVo) {
        try {
            // 构造查询条件
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            //搜索文档的编辑记录
            if (StrUtil.isNotBlank(documentEditLogVo.getId()) && (ObjectUtil.isNull(documentEditLogVo.getType()) || !documentEditLogVo.getType().equals(DocumentLogTypeEnum.SEARCH))) {
                boolQuery.filter(QueryBuilders.matchQuery(Get.name(DocumentLogEsPo::getDocId), documentEditLogVo.getId()));
                boolQuery.must(QueryBuilders.matchQuery(Get.name(DocumentLogEsPo::getLogType), DocumentLogTypeEnum.EDIT));
            }
            //搜索历史搜索记录
            if (ObjectUtil.isNotNull(documentEditLogVo.getType()) && documentEditLogVo.getType().equals(DocumentLogTypeEnum.SEARCH)) {
                boolQuery.filter(QueryBuilders.matchQuery(Get.name(DocumentLogEsPo::getUserId), documentEditLogVo.getUserId()));
                boolQuery.must(QueryBuilders.matchQuery(Get.name(DocumentLogEsPo::getLogType), DocumentLogTypeEnum.SEARCH));
            }

            NativeSearchQuery queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withSort(SortBuilders.fieldSort(Get.name(DocumentLogEsPo::getCreateTime)).order(SortOrder.DESC))
                    .withPageable(PageRequest.of((int) (page.getCurrent() - 1), (int) page.getSize()))
                    .build();

            // 执行搜索
            SearchHits<DocumentLogEsPo> searchHits = esTemplate.search(queryBuilder, DocumentLogEsPo.class);
            SearchPage<DocumentLogEsPo> searchPage = SearchHitSupport.searchPageFor(searchHits, queryBuilder.getPageable());

            // 构造返回
            List<DocumentEditLogResVo> resDatas = new ArrayList<>();
            long total = CollectionUtils.isEmpty(searchPage.getContent()) ? 0 : searchPage.getTotalElements();
            Page<DocumentEditLogResVo> resVoPage = new Page<>(page.getCurrent(), page.getSize(), total);
            for (SearchHit<DocumentLogEsPo> documentLogEsPoSearchHit : searchPage.getContent()) {
                DocumentEditLogResVo resVo = new DocumentEditLogResVo();
                BeanUtils.copyProperties(documentLogEsPoSearchHit.getContent(), resVo);
                resDatas.add(resVo);
            }
            resVoPage.setRecords(resDatas);

            return resVoPage;
        } catch (Exception e) {
            log.error("查询文档编辑记录失败. exception: {}", e);
            throw new BusinessException("ES异常");
        }

    }

    @Override
    public Long searchDocumentReadTotal(String id) {
        try {
            // 构造查询条件
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            boolQuery.filter(QueryBuilders.matchQuery(Get.name(DocumentLogEsPo::getDocId), id));
            boolQuery.must(QueryBuilders.matchQuery(Get.name(DocumentLogEsPo::getLogType), DocumentLogTypeEnum.READ));
            NativeSearchQuery queryBuilder = new NativeSearchQueryBuilder().withQuery(boolQuery).build();

            return esTemplate.count(queryBuilder, DocumentLogEsPo.class);
        } catch (Exception e) {
            log.error("获取文档已读次数失败. exception: {}", e);
            throw new BusinessException("ES异常");
        }
    }

    @Override
    public List<DocumentEsPo> searchDocumentByName(String name) {
        try {
            List<DocumentEsPo> resDatas = new ArrayList<>();
            NativeSearchQuery queryBuilder = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery(Get.name(DocumentEsPo::getName), name)).build();
            SearchHits<DocumentEsPo> searchHits = esTemplate.search(queryBuilder, DocumentEsPo.class);
            for (SearchHit<DocumentEsPo> documentEsPoSearchHit : searchHits.getSearchHits()) {
                DocumentEsPo resVo = new DocumentEsPo();
                BeanUtils.copyProperties(documentEsPoSearchHit.getContent(), resVo);
                resDatas.add(resVo);
            }
            return resDatas;
        } catch (Exception e) {
            log.error("根据文档名称，搜索文档信息集合失败. exception: {}", e);
            throw new BusinessException("ES异常");
        }

    }

    @Override
    public void deleteLog(String tenantId, String userId) {
        try {
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            boolQuery.must(QueryBuilders.matchQuery(Get.name(DocumentLogEsPo::getTenantId), tenantId));
            boolQuery.must(QueryBuilders.matchQuery(Get.name(DocumentLogEsPo::getUserId), userId));
            boolQuery.must(QueryBuilders.matchQuery(Get.name(DocumentLogEsPo::getLogType), DocumentLogTypeEnum.SEARCH));
            NativeSearchQuery queryBuilder = new NativeSearchQueryBuilder().withQuery(boolQuery).build();
            esTemplate.delete(queryBuilder, DocumentLogEsPo.class, IndexConstant.INDEX_DOCUMENT_LOG);
        } catch (Exception e) {
            log.error("删除文档失败", e);
            throw new BusinessException("ES异常");
        }
    }

    @Async
    @Override
    public void deleteDocument(String tenantId, List<String> docIds) {
        try {
            if (CollectionUtils.isEmpty(docIds)) {
                log.warn("文档id为空，不能删除文档");
                return;
            }
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            boolQuery.must(QueryBuilders.matchQuery(Get.name(DocumentEsPo::getTenantId), tenantId));
            boolQuery.must(QueryBuilders.termsQuery(Get.name(DocumentEsPo::getDocId), docIds));
            NativeSearchQuery queryBuilder = new NativeSearchQueryBuilder().withQuery(boolQuery).build();
            esTemplate.delete(queryBuilder, DocumentEsPo.class, IndexConstant.INDEX_DOCUMENT_BASE_INFO);
        } catch (Exception e) {
            log.error("删除文档失败", e);
            throw new BusinessException("ES异常");
        }
    }

    @Override
    public void updateDocumentEs(DcLibrary dcLibrary) {
        try {
            // 封装要修改的文档信息
            String id = buildDocumentEsId(dcLibrary.getTenantId(), dcLibrary.getId());
            Document document = Document.create();
            document.put(Get.name(DocumentEsPo::getName), dcLibrary.getName());
            //日期转换一下
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateFormat.date_hour_minute_second.getPattern());
            String createTime = formatter.format(dcLibrary.getCreateTime());
            String updateTime = formatter.format(dcLibrary.getUpdateTime());
            // 封装完整的文档信息（ES中不存在该文档时，执行新增文档操作）
            DocumentEsPo esPo = getCreatePo(new UserDto().setId(dcLibrary.getCreateById()).setRealName(dcLibrary.getCreateBy()), dcLibrary, "");
            Document createDoc = Document.parse(JSON.toJSONString(esPo));
            createDoc.put("createTime", createTime);
            createDoc.put("updateTime", updateTime);
            UpdateQuery updateQuery = UpdateQuery.builder(id).withDocument(document).withUpsert(createDoc).build();
            esTemplate.update(updateQuery, IndexConstant.INDEX_DOCUMENT_BASE_INFO);

            // 若知识库名称变更，则更新该知识库关联的所有es索引文档中的“知识库名称”
            updateKnowledgeName(dcLibrary);
        } catch (Exception e) {
            log.error("修改文档信息失败. exception: ", e);
            throw new BusinessException("ES异常");
        }
    }

    /**
     * 若知识库名称变更，则更新该知识库关联的所有es索引文档中的“知识库名称”
     *
     * @param dcLibrary
     */
    private void updateKnowledgeName(DcLibrary dcLibrary) {
        if (!DcLibraryTypeEnum.knowledge.equals(dcLibrary.getType())) {
            return;
        }
        try {
            // 需要同步修改知识库名称的条件
            List<UpdateQuery> updateQuerys = new ArrayList<>();

            // 查询需要修改知识库名称的索引
            BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            boolQuery.must(QueryBuilders.matchQuery(Get.name(DocumentEsPo::getKnowledgeId), dcLibrary.getId()));
            boolQuery.must(QueryBuilders.matchQuery(Get.name(DocumentEsPo::getTenantId), dcLibrary.getTenantId()));
            boolQuery.mustNot(QueryBuilders.matchPhraseQuery(Get.name(DocumentEsPo::getKnowledgeName), dcLibrary.getName()));
            NativeSearchQuery queryBuilder = new NativeSearchQueryBuilder().withQuery(boolQuery).build();
            SearchHits<DocumentEsPo> searchHits = esTemplate.search(queryBuilder, DocumentEsPo.class);
            for (SearchHit<DocumentEsPo> documentEsPoSearchHit : searchHits.getSearchHits()) {
                DocumentEsPo resVo = new DocumentEsPo();
                BeanUtils.copyProperties(documentEsPoSearchHit.getContent(), resVo);
                String id = buildDocumentEsId(resVo.getTenantId(), resVo.getDocId());
                Document document = Document.create();
                document.put(Get.name(DocumentEsPo::getKnowledgeName), dcLibrary.getName());
                UpdateQuery updateQuery = UpdateQuery.builder(id).withDocument(document).build();
                updateQuerys.add(updateQuery);
            }
            if (CollectionUtils.isEmpty(updateQuerys)) {
                return;
            }

            // 批量修改知识库名称
            esTemplate.bulkUpdate(updateQuerys, IndexConstant.INDEX_DOCUMENT_BASE_INFO);
        } catch (Exception e) {
            log.error("批量更新知识库名称失败. exception: ", e);
            throw new BusinessException("ES异常");
        }
    }
}
