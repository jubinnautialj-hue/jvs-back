package cn.bctools.document.auth;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.document.auth.cofig.DataAuthConfig;
import cn.bctools.document.auth.dto.DocumentAuthDto;
import cn.bctools.document.auth.util.AuthSystemTool;
import cn.bctools.document.dto.IdentifyingAuthDto;
import cn.bctools.document.entity.DcAuthConfig;
import cn.bctools.document.entity.DcIdentifying;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.entity.enums.IdentifyingKeyEnum;
import cn.bctools.document.service.DcAuthConfigService;
import cn.bctools.document.service.DcIdentifyingService;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * [Description]:
 * 注意
 *
 * @author : admin
 * @date : 2020-12-04 11:44
 */
@Configuration
@AllArgsConstructor
@Lazy(false)
@Slf4j
public class DocumentAuthInterceptor implements HandlerInterceptor {
    public static final String PATH = "gateway:path";
    /**
     * 文库或文件id
     */
    public static final String HEADER_DOCUMENT_ID = "documentId";
    private final DataAuthConfig dataAuthConfig;
    private final DcAuthConfigService dcAuthConfigService;
    private final DcLibraryService dcLibraryService;
    private final DcIdentifyingService dcIdentifyingService;
    private final RedisUtils redisUtils;
    static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUrl = request.getRequestURI();
        //判断是否需要清除租户
        clearTenant(requestUrl);
        Optional<String> first = dataAuthConfig.getCheckUrl().stream().filter(e -> ANT_PATH_MATCHER.match(e, requestUrl)).findFirst();
        if (first.isPresent()) {
            //获取数据id
            String documentId = request.getHeader(HEADER_DOCUMENT_ID);
            if (StrUtil.isEmpty(documentId)) {
                throw new BusinessException("当前操作需要进行权限认证，获取数据id为空!");
            }
            //获取文档类型 如果是文库或者文件夹需要获取下级所有权限
            DcLibrary byId = dcLibraryService.getById(documentId);
            if (ObjectUtil.isNull(byId)) {
                throw new BusinessException("未找到此数据");
            }
            //获取当前目录下面的所有文档与文件夹权限
            DocumentAuthDto documentAuthDto = getAuth(byId);
//            log.info("---------------------前置放入线程中的数据为:{}", JSONObject.toJSONString(documentAuthDto));
            AuthSystemTool.setDocumentAuth(documentAuthDto);
        }
        return true;
    }

    /**
     * 权限分组方便后续业务使用
     * 业务使用时 判断map中的key是否存在这个id
     * 如果存在直接使用这个权限集合 如果不存在就使用上级的
     * 这里只会存储当前级的权限 上级与下级不会存储
     */
    private DocumentAuthDto getAuth(DcLibrary dcLibrary) {
        String id = dcLibrary.getId();
        List<DcLibraryTypeEnum> list = Arrays.asList(DcLibraryTypeEnum.knowledge, DcLibraryTypeEnum.directory);
        //判断是否是文件如果是文件 就需要获取上级id 作为本次权限的主id
        if (!list.contains(dcLibrary.getType())) {
            id = dcLibrary.getParentId();
        }
        //获取所有上级id与此目录下面的文件id
        String finalId = id;
        List<DcLibrary> libraries = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>()
                .select(DcLibrary::getId, DcLibrary::getPathId, DcLibrary::getType)
                .eq(DcLibrary::getId, id)
                .or(e -> e.eq(DcLibrary::getParentId, finalId)));
        //路径id
        List<String> pathId = libraries.stream().filter(e -> e.getId().equals(finalId)).findFirst().orElseThrow(() -> new BusinessException("未找到此数据")).getPathId();
        //本次权限查询的文档id集合
        List<String> ids = libraries.stream().map(DcLibrary::getId).collect(Collectors.toList());
        //添加上级
        ids.addAll(pathId);
        List<DcAuthConfig> authConfigs = dcAuthConfigService.getAuthConfigById(ids);
        //获取权限分组
        Map<String, List<IdentifyingAuthDto>> map = authConfigs.stream()
                //现通过文档id分组并获取权限标识
                .collect(Collectors.groupingBy(DcAuthConfig::getDcId, Collectors.mapping(DcAuthConfig::getAuthSign, Collectors.toList())))
                //分组后的value 是一个 list<list<T>>对象 需要二次平铺
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        v -> v.getValue()
                                .stream()
                                .flatMap(List::stream)
                                .distinct()
                                .collect(Collectors.toList()))
                );
        //合并基础权限 根据路径合并
        List<IdentifyingAuthDto> identifying = pathId.stream().flatMap(e -> map.getOrDefault(e, new ArrayList<>()).stream()).collect(Collectors.toList());
        //添加本身
        identifying.addAll(map.getOrDefault(id, new ArrayList<>()));
        //判断是否为全网或者全平台文库 如果是全平台文库需要默认权限
        boolean shareRole;
        if (dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge)) {
            shareRole = !dcLibrary.getShareRole().equals(DcLibraryReadEnum.user);
        } else {
            DcLibrary knowledge = dcLibraryService.getById(dcLibrary.getKnowledgeId());
            shareRole = !knowledge.getShareRole().equals(DcLibraryReadEnum.user);
        }
        //设置当前文件夹下面的所有数据权限
        Map<String, List<IdentifyingAuthDto>> mapAuth = new HashMap<>(libraries.size());
        //获取查看权限
        List<IdentifyingAuthDto> defaultList = dcIdentifyingService.list(new LambdaQueryWrapper<DcIdentifying>()
                        .eq(DcIdentifying::getIdentifyingKey, IdentifyingKeyEnum.view))
                .stream()
                .map(e -> BeanUtil.copyProperties(e, IdentifyingAuthDto.class))
                .peek(e -> e.setSelect(Boolean.TRUE).setIsSelect(Boolean.TRUE))
                .collect(Collectors.toList());
        libraries.forEach(e -> {
            //设置默认权限
            if (shareRole) {
                mapAuth.put(e.getId(), defaultList);
            }
            List<IdentifyingAuthDto> dcIdentifyingList = JSONObject.parseArray(JSONObject.toJSONString(identifying), IdentifyingAuthDto.class);
            //如果是本身只需要添加顶级权限
            if (!e.getId().equals(finalId)) {
                dcIdentifyingList.addAll(map.getOrDefault(e.getId(), new ArrayList<>()));
            }
            if (!dcIdentifyingList.isEmpty()) {
                mapAuth.put(e.getId(), dcIdentifyingList);
            }
            if (!mapAuth.containsKey(e.getId()) && e.getType().equals(DcLibraryTypeEnum.directory)) {
                //判断下级是否存在
                if (dcAuthConfigService.childrenIsAuth(e.getId())) {
                    mapAuth.put(e.getId(), defaultList);
                }
            }
        });
        DocumentAuthDto documentAuthDto = new DocumentAuthDto();
        //需要去掉本身
        Set<String> keySet = mapAuth.keySet().stream().filter(e -> !e.equals(finalId)).collect(Collectors.toSet());
        documentAuthDto.setParentId(id)
                .setIds(keySet)
                .setId(dcLibrary.getId())
                .setIdentifyingRoleMap(mapAuth);
        return documentAuthDto;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        AuthSystemTool.removeAll();
    }


    /**
     * 如果是放开的接口 直接把租户清除了 防止没有获取到租户id时 系统默认为1的情况
     *
     * @param requestUrl 请求的url
     */
    private void clearTenant(String requestUrl) {
        Optional<Object> first = redisUtils.lGet(PATH, 0, -1).stream().filter(e -> ANT_PATH_MATCHER.match(e.toString(), requestUrl)).findFirst();
        if (first.isPresent()) {
            TenantContextHolder.clear();
        }
    }

}
